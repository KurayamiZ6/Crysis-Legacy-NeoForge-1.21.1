package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanovision.debug.NanovisionPerformanceProfiler;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.slf4j.Logger;

public final class ThermalFirstPersonArmComposite {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static boolean pendingShaderpackComposite;
   private static float pendingArmProjectionScale = 1.0F;
   private static boolean loggedLateComposite;
   private static boolean warnedLateCompositeFailure;

   private ThermalFirstPersonArmComposite() {
   }

   public static boolean render(HumanoidArm arm, float armProjectionScale) {
      return renderInternal(arm, armProjectionScale, ThermalFirstPersonArmRenderTarget.armOwnerState());
   }

   public static void beginFrame() {
      pendingShaderpackComposite = false;
      pendingArmProjectionScale = 1.0F;
   }

   public static void markShaderpackCompositePending(float armProjectionScale) {
      if (!pendingShaderpackComposite) {
         pendingArmProjectionScale = armProjectionScale;
      }

      pendingShaderpackComposite = true;
   }

   public static boolean hasPendingShaderpackComposite() {
      return pendingShaderpackComposite;
   }

   public static boolean renderPendingShaderpackComposite() {
      if (!pendingShaderpackComposite) {
         return false;
      } else {
         pendingShaderpackComposite = false;
         float armProjectionScale = pendingArmProjectionScale;
         pendingArmProjectionScale = 1.0F;
         Minecraft minecraft = Minecraft.getInstance();
         RenderTarget mainTarget = minecraft.getMainRenderTarget();
         if (mainTarget == null) {
            return false;
         } else {
            ThermalFramebufferState previousState = ThermalFramebufferState.capture();

            boolean rendered;
            try {
               List<HumanoidArm> pendingArms = ThermalFirstPersonArmDeferredBuffer.drainPendingArms();
               rendered = false;

               for (HumanoidArm pendingArm : pendingArms) {
                  if (!ThermalFirstPersonArmRenderTarget.prepare()) {
                     ThermalFirstPersonArmDeferredBuffer.discardDeferred();
                     break;
                  }

                  if (!ThermalFirstPersonArmDeferredBuffer.flushDeferred(pendingArm)) {
                     ThermalFirstPersonArmDeferredBuffer.discardDeferred();
                     break;
                  }

                  ThermalFirstPersonItemArmSnapshots.capture(pendingArm, ThermalFirstPersonArmRenderTarget.getOrCreate());
                  mainTarget.bindWrite(true);
                  ThermalFramebufferState mainState = ThermalFramebufferState.capture();
                  rendered |= renderInternal(null, armProjectionScale, mainState);
               }

               if (rendered && !loggedLateComposite) {
                  loggedLateComposite = true;
                  LOGGER.info("[CrysisMod][Nanovision] separate first-person thermal arms composited after shaderpack finalization");
               }

               return rendered;
            } catch (Throwable var12) {
               ThermalFirstPersonArmDeferredBuffer.discardDeferred();
               if (!warnedLateCompositeFailure) {
                  warnedLateCompositeFailure = true;
                  LOGGER.warn("[CrysisMod][Nanovision] late first-person thermal replay failed; keeping physical arm fallback", var12);
               }

               rendered = false;
            } finally {
               ThermalFirstPersonArmOcclusionDepth.consumeFrame();
               previousState.restore();
            }

            return rendered;
         }
      }
   }

   private static boolean renderInternal(HumanoidArm arm, float armProjectionScale, ThermalFramebufferState destinationState) {
      Minecraft minecraft = Minecraft.getInstance();
      RenderTarget mainTarget = minecraft.getMainRenderTarget();
      RenderTarget maskTarget = ThermalFirstPersonArmRenderTarget.getOrCreate();
      boolean shaderpackLateComposite = arm == null;
      ShaderInstance shader = shaderpackLateComposite
         ? ThermalFirstPersonArmShaderpackShaders.armCompositeShader()
         : ThermalFirstPersonArmShaders.armCompositeShader();
      if (mainTarget != null && maskTarget != null && destinationState != null && shader != null) {
         if (arm != null) {
            ThermalFirstPersonItemArmSnapshots.capture(arm, maskTarget);
         }

         int maskWidth = maskTarget.width;
         int maskHeight = maskTarget.height;
         int drawWidth = destinationState.viewportWidth();
         int drawHeight = destinationState.viewportHeight();
         if (shaderpackLateComposite) {
            ThermalFirstPersonArmShaderpackShaders.prepareComposite(
               maskTarget.getColorTextureId(), maskTarget.getDepthTextureId(), maskWidth, maskHeight, armProjectionScale
            );
         } else {
            ThermalFirstPersonArmShaders.prepareComposite(
               maskTarget.getColorTextureId(), maskTarget.getDepthTextureId(), maskWidth, maskHeight, armProjectionScale
            );
         }

         long cpuStarted = NanovisionPerformanceProfiler.beginCpu();
         RenderSystem.backupProjectionMatrix();
         Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
         modelViewStack.pushMatrix();

         boolean ortho;
         try {
            if (destinationState.restoreAndValidateDrawTarget()) {
               if (arm != null) {
                  ThermalFirstPersonArmRenderTarget.logOwnerOnce(mainTarget);
               }

               Matrix4f orthox = new Matrix4f().setOrtho(0.0F, drawWidth, drawHeight, 0.0F, 1000.0F, 3000.0F);
               RenderSystem.setProjectionMatrix(orthox, VertexSorting.ORTHOGRAPHIC_Z);
               modelViewStack.identity();
               modelViewStack.translate(0.0F, 0.0F, -2000.0F);
               RenderSystem.applyModelViewMatrix();
               boolean nativeArmDepthOcclusion = arm != null;
               RenderSystem.colorMask(true, true, true, true);
               RenderSystem.depthMask(false);
               if (nativeArmDepthOcclusion) {
                  RenderSystem.enableDepthTest();
                  RenderSystem.depthFunc(515);
               } else {
                  RenderSystem.disableDepthTest();
               }

               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               if (shaderpackLateComposite) {
                  RenderSystem.setShader(ThermalFirstPersonArmShaderpackShaders::armCompositeShader);
               } else {
                  RenderSystem.setShader(ThermalFirstPersonArmShaders::armCompositeShader);
               }

               if (arm != null) {
                  int scissorX = arm == HumanoidArm.RIGHT ? Math.round(drawWidth * 0.18F) : 0;
                  int scissorWidth = Math.max(1, Math.round(drawWidth * 0.82F));
                  RenderSystem.enableScissor(scissorX, 0, Math.min(scissorWidth, drawWidth - scissorX), drawHeight);
               }

               BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
               buffer.addVertex(0.0F, drawHeight, 0.0F).setUv(0.0F, 0.0F);
               buffer.addVertex(drawWidth, drawHeight, 0.0F).setUv(1.0F, 0.0F);
               buffer.addVertex(drawWidth, 0.0F, 0.0F).setUv(1.0F, 1.0F);
               buffer.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, 1.0F);
               int gpuQuery = NanovisionPerformanceProfiler.beginGpu(NanovisionPerformanceProfiler.Section.FIRST_PERSON_ARM_COMPOSITE_GPU);

               try {
                  BufferUploader.drawWithShader(buffer.buildOrThrow());
               } finally {
                  NanovisionPerformanceProfiler.endGpu(NanovisionPerformanceProfiler.Section.FIRST_PERSON_ARM_COMPOSITE_GPU, gpuQuery);
               }

               return true;
            }

            ortho = false;
         } finally {
            RenderSystem.disableScissor();
            if (shader != null) {
               shader.clear();
            }

            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.colorMask(true, true, true, true);
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
            destinationState.restore();
            NanovisionPerformanceProfiler.endCpu(NanovisionPerformanceProfiler.Section.FIRST_PERSON_ARM_COMPOSITE, cpuStarted);
         }

         return ortho;
      } else {
         return false;
      }
   }
}
