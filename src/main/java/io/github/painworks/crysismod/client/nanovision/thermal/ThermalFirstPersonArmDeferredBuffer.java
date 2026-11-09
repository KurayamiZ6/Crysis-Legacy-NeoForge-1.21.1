package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.slf4j.Logger;

public final class ThermalFirstPersonArmDeferredBuffer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int BUFFER_SIZE = 16384;
   private static final Map<HumanoidArm, ThermalFirstPersonArmDeferredBuffer.ArmStorage> ARM_STORAGE = new EnumMap<>(HumanoidArm.class);
   private static final List<HumanoidArm> PENDING_ORDER = new ArrayList<>(2);
   private static ThermalFirstPersonArmDeferredBuffer.DepthProjection activeDepthProjection;
   private static boolean loggedReplay;
   private static boolean loggedCanonicalProjection;

   private ThermalFirstPersonArmDeferredBuffer() {
   }

   public static void beginFrame() {
      RenderSystem.assertOnRenderThread();
      if (PENDING_ORDER.isEmpty() && !ARM_STORAGE.values().stream().anyMatch(storage -> storage.pending)) {
         activeDepthProjection = null;
      } else {
         resetAllStorage();
      }
   }

   public static VertexConsumer getBuffer(HumanoidArm arm, RenderType renderType) {
      RenderSystem.assertOnRenderThread();
      ThermalFirstPersonArmDeferredBuffer.ArmStorage storage = storage(arm);
      storage.fixedBuffers.computeIfAbsent(renderType, ignored -> new ByteBufferBuilder(Math.max(16384, renderType.bufferSize())));
      return storage.source.getBuffer(renderType);
   }

   public static void deferCurrentDraw(HumanoidArm arm) {
      RenderSystem.assertOnRenderThread();
      ThermalFirstPersonArmDeferredBuffer.ArmStorage storage = storage(arm);
      if (!storage.pending) {
         ThermalFirstPersonArmDeferredBuffer.ProjectionCapture projectionCapture = canonicalizeDepthProjection(new Matrix4f(RenderSystem.getProjectionMatrix()));
         storage.handProjection = projectionCapture.projection();
         storage.handModelView = new Matrix4f(RenderSystem.getModelViewStack());
         storage.depthProjection = projectionCapture.depthProjection();
         PENDING_ORDER.add(arm);
      }

      storage.pending = true;
   }

   public static List<HumanoidArm> drainPendingArms() {
      RenderSystem.assertOnRenderThread();
      if (PENDING_ORDER.isEmpty()) {
         return List.of();
      } else {
         List<HumanoidArm> result = List.copyOf(PENDING_ORDER);
         PENDING_ORDER.clear();
         return result;
      }
   }

   public static boolean flushDeferred(HumanoidArm arm) {
      RenderSystem.assertOnRenderThread();
      ThermalFirstPersonArmDeferredBuffer.ArmStorage storage = ARM_STORAGE.get(arm);
      if (storage != null && storage.pending && storage.handProjection != null && storage.handModelView != null && storage.depthProjection != null) {
         Matrix4f projection = storage.handProjection;
         Matrix4f modelView = storage.handModelView;
         activeDepthProjection = storage.depthProjection;
         storage.pending = false;
         storage.handProjection = null;
         storage.handModelView = null;
         RenderSystem.backupProjectionMatrix();
         Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
         modelViewStack.pushMatrix();
         boolean replayCompleted = false;

         boolean throwable;
         try {
            RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);
            modelViewStack.set(modelView);
            RenderSystem.applyModelViewMatrix();
            storage.source.endBatch();
            replayCompleted = true;
            if (!loggedReplay) {
               loggedReplay = true;
               LOGGER.info("[CrysisMod][Nanovision] replayed separate first-person thermal arm masks outside shaderpack hand pass");
            }

            throwable = true;
         } catch (Throwable var10) {
            storage.reset();
            throw var10;
         } finally {
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
            if (replayCompleted) {
               storage.finishFlush();
            }
         }

         return throwable;
      } else {
         return false;
      }
   }

   public static void discardDeferred() {
      RenderSystem.assertOnRenderThread();
      resetAllStorage();
   }

   private static ThermalFirstPersonArmDeferredBuffer.ProjectionCapture canonicalizeDepthProjection(Matrix4f activeProjection) {
      Matrix4f vanillaProjection = Minecraft.getInstance().gameRenderer.getProjectionMatrix(70.0);
      float externalM22 = activeProjection.m22();
      float externalM32 = activeProjection.m32();
      float externalM23 = activeProjection.m23();
      float externalM33 = activeProjection.m33();
      activeProjection.m02(vanillaProjection.m02()).m12(vanillaProjection.m12()).m22(vanillaProjection.m22()).m32(vanillaProjection.m32());
      ThermalFirstPersonArmDeferredBuffer.DepthProjection depthProjection = new ThermalFirstPersonArmDeferredBuffer.DepthProjection(
         activeProjection.m22(), activeProjection.m32(), activeProjection.m23(), activeProjection.m33(), externalM22, externalM32, externalM23, externalM33
      );
      if (!loggedCanonicalProjection) {
         loggedCanonicalProjection = true;
         LOGGER.info(
            "[CrysisMod][Nanovision] restored vanilla first-person thermal depth projection (m22 {} -> {}, m32 {} -> {})",
            new Object[]{externalM22, activeProjection.m22(), externalM32, activeProjection.m32()}
         );
      }

      return new ThermalFirstPersonArmDeferredBuffer.ProjectionCapture(activeProjection, depthProjection);
   }

   static ThermalFirstPersonArmDeferredBuffer.DepthProjection depthProjection() {
      return activeDepthProjection;
   }

   private static ThermalFirstPersonArmDeferredBuffer.ArmStorage storage(HumanoidArm arm) {
      return ARM_STORAGE.computeIfAbsent(arm, ignored -> new ThermalFirstPersonArmDeferredBuffer.ArmStorage());
   }

   private static void resetAllStorage() {
      for (ThermalFirstPersonArmDeferredBuffer.ArmStorage storage : ARM_STORAGE.values()) {
         storage.reset();
      }

      PENDING_ORDER.clear();
      activeDepthProjection = null;
   }

   private static final class ArmStorage {
      private final SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers = new LinkedHashMap<>();
      private ByteBufferBuilder fallbackBuffer = new ByteBufferBuilder(16384);
      private BufferSource source = this.createSource();
      private boolean pending;
      private Matrix4f handProjection;
      private Matrix4f handModelView;
      private ThermalFirstPersonArmDeferredBuffer.DepthProjection depthProjection;

      private BufferSource createSource() {
         return MultiBufferSource.immediateWithBuffers(this.fixedBuffers, this.fallbackBuffer);
      }

      private void reset() {
         for (ByteBufferBuilder buffer : this.fixedBuffers.values()) {
            buffer.close();
         }

         this.fixedBuffers.clear();
         this.fallbackBuffer.close();
         this.fallbackBuffer = new ByteBufferBuilder(16384);
         this.source = this.createSource();
         this.pending = false;
         this.handProjection = null;
         this.handModelView = null;
         this.depthProjection = null;
      }

      private void finishFlush() {
         this.pending = false;
         this.handProjection = null;
         this.handModelView = null;
         this.depthProjection = null;
      }
   }

   record DepthProjection(
      float canonicalM22,
      float canonicalM32,
      float canonicalM23,
      float canonicalM33,
      float externalM22,
      float externalM32,
      float externalM23,
      float externalM33
   ) {
      float reprojectCanonicalDepth(float canonicalDepth) {
         float canonicalNdc = canonicalDepth * 2.0F - 1.0F;
         float solveDenominator = canonicalNdc * this.canonicalM23 - this.canonicalM22;
         if (Float.isFinite(solveDenominator) && !(Math.abs(solveDenominator) < 1.0E-7F)) {
            float viewZ = (this.canonicalM32 - canonicalNdc * this.canonicalM33) / solveDenominator;
            float externalW = this.externalM23 * viewZ + this.externalM33;
            if (Float.isFinite(viewZ) && Float.isFinite(externalW) && !(Math.abs(externalW) < 1.0E-7F)) {
               float externalNdc = (this.externalM22 * viewZ + this.externalM32) / externalW;
               return Math.max(0.0F, Math.min(1.0F, externalNdc * 0.5F + 0.5F));
            } else {
               return canonicalDepth;
            }
         } else {
            return canonicalDepth;
         }
      }
   }

   private record ProjectionCapture(Matrix4f projection, ThermalFirstPersonArmDeferredBuffer.DepthProjection depthProjection) {
   }
}
