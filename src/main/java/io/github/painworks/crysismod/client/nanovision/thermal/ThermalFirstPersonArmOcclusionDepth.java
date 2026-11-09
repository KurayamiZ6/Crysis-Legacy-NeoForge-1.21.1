package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.slf4j.Logger;

public final class ThermalFirstPersonArmOcclusionDepth {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation DEPTH_COPY_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_first_person_arm_depth_copy");
   private static volatile ShaderInstance depthCopyShader;
   private static RenderTarget sceneDepthTarget;
   private static boolean capturedThisFrame;
   private static boolean loggedActive;
   private static boolean warnedFailure;

   private ThermalFirstPersonArmOcclusionDepth() {
   }

   public static void register(RegisterShadersEvent event) {
      try {
         event.registerShader(
            new ShaderInstance(event.getResourceProvider(), DEPTH_COPY_SHADER, DefaultVertexFormat.POSITION_TEX), shader -> depthCopyShader = shader
         );
      } catch (IOException var2) {
         depthCopyShader = null;
         LOGGER.error("[CrysisMod][Nanovision] failed to register first-person arm occlusion-depth shader {}", DEPTH_COPY_SHADER, var2);
      }
   }

   public static void beginFrame() {
      RenderSystem.assertOnRenderThread();
      capturedThisFrame = false;
   }

   public static void captureAfterLevel(Minecraft minecraft) {
      RenderSystem.assertOnRenderThread();
      capturedThisFrame = false;
      if (ThermalFirstPersonArmComposite.hasPendingShaderpackComposite()
         && minecraft != null
         && minecraft.player != null
         && minecraft.getMainRenderTarget() != null
         && depthCopyShader != null
         && hasHeldItem(minecraft)) {
         RenderTarget mainTarget = minecraft.getMainRenderTarget();
         ThermalFramebufferState previousState = ThermalFramebufferState.capture();

         try {
            ensureTarget(mainTarget.width, mainTarget.height);
            copyDepthTexture(mainTarget, sceneDepthTarget);
            capturedThisFrame = true;
         } catch (Throwable var7) {
            if (!warnedFailure) {
               warnedFailure = true;
               LOGGER.warn("[CrysisMod][Nanovision] failed to capture first-person held-item cover depth", var7);
            }
         } finally {
            previousState.restore();
         }
      }
   }

   public static void prepareComposite(ShaderInstance shader, int fallbackTextureId) {
      RenderSystem.assertOnRenderThread();
      ThermalFirstPersonArmDeferredBuffer.DepthProjection projection = ThermalFirstPersonArmDeferredBuffer.depthProjection();
      boolean enabled = capturedThisFrame && sceneDepthTarget != null && projection != null;
      shader.setSampler("SceneDepthSampler", enabled ? sceneDepthTarget.getColorTextureId() : fallbackTextureId);
      setInt(shader, "ArmOcclusionEnabled", enabled ? 1 : 0);
      if (enabled) {
         set(shader, "CanonicalDepthProjection", projection.canonicalM22(), projection.canonicalM32(), projection.canonicalM23(), projection.canonicalM33());
         set(shader, "ExternalDepthProjection", projection.externalM22(), projection.externalM32(), projection.externalM23(), projection.externalM33());
         if (!loggedActive) {
            loggedActive = true;
            LOGGER.info("[CrysisMod][Nanovision] held item now covers the completed first-person thermal arm");
         }
      } else {
         set(shader, "CanonicalDepthProjection", 0.0F, 0.0F, 0.0F, 1.0F);
         set(shader, "ExternalDepthProjection", 0.0F, 0.0F, 0.0F, 1.0F);
      }
   }

   public static void consumeFrame() {
      RenderSystem.assertOnRenderThread();
      capturedThisFrame = false;
   }

   public static void close() {
      RenderSystem.assertOnRenderThread();
      if (sceneDepthTarget != null) {
         sceneDepthTarget.destroyBuffers();
         sceneDepthTarget = null;
      }

      capturedThisFrame = false;
      loggedActive = false;
      warnedFailure = false;
   }

   private static boolean hasHeldItem(Minecraft minecraft) {
      return !minecraft.player.getMainHandItem().isEmpty() || !minecraft.player.getOffhandItem().isEmpty();
   }

   private static void ensureTarget(int width, int height) {
      int requiredWidth = Math.max(width, 1);
      int requiredHeight = Math.max(height, 1);
      if (sceneDepthTarget == null || sceneDepthTarget.width != requiredWidth || sceneDepthTarget.height != requiredHeight) {
         if (sceneDepthTarget != null) {
            sceneDepthTarget.destroyBuffers();
         }

         sceneDepthTarget = new TextureTarget(requiredWidth, requiredHeight, false, Minecraft.ON_OSX);
         sceneDepthTarget.setFilterMode(9728);
      }
   }

   private static void copyDepthTexture(RenderTarget source, RenderTarget destination) {
      ShaderInstance shader = depthCopyShader;
      if (shader == null) {
         throw new IllegalStateException("arm cover-depth shader is unavailable");
      } else {
         int width = destination.width;
         int height = destination.height;
         shader.setSampler("DepthSampler", source.getDepthTextureId());
         RenderSystem.backupProjectionMatrix();
         Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
         modelViewStack.pushMatrix();

         try {
            destination.bindWrite(true);
            Matrix4f ortho = new Matrix4f().setOrtho(0.0F, width, height, 0.0F, 1000.0F, 3000.0F);
            RenderSystem.setProjectionMatrix(ortho, VertexSorting.ORTHOGRAPHIC_Z);
            modelViewStack.identity();
            modelViewStack.translate(0.0F, 0.0F, -2000.0F);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.setShader(() -> shader);
            BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.addVertex(0.0F, height, 0.0F).setUv(0.0F, 0.0F);
            buffer.addVertex(width, height, 0.0F).setUv(1.0F, 0.0F);
            buffer.addVertex(width, 0.0F, 0.0F).setUv(1.0F, 1.0F);
            buffer.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, 1.0F);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
         } finally {
            shader.clear();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.colorMask(true, true, true, true);
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
         }
      }
   }

   private static void setInt(ShaderInstance shader, String name, int value) {
      Uniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }

   private static void set(ShaderInstance shader, String name, float x, float y, float z, float w) {
      Uniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(x, y, z, w);
      }
   }
}
