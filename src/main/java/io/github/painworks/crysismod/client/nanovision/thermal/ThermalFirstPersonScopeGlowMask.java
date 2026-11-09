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
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.slf4j.Logger;

public final class ThermalFirstPersonScopeGlowMask {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation SOURCE_MASK_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_first_person_item_glow_source_mask");
   private static final ResourceLocation COMPOSITE_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_first_person_scope_glow_composite");
   private static RenderTarget maskTarget;
   private static ShaderInstance sourceMaskShader;
   private static ShaderInstance compositeShader;
   private static boolean warned;

   private ThermalFirstPersonScopeGlowMask() {
   }

   public static void beginFrame() {
   }

   public static void registerShaders(RegisterShadersEvent event) {
      try {
         event.registerShader(
            new ShaderInstance(event.getResourceProvider(), SOURCE_MASK_SHADER, DefaultVertexFormat.POSITION_TEX), shader -> sourceMaskShader = shader
         );
         event.registerShader(
            new ShaderInstance(event.getResourceProvider(), COMPOSITE_SHADER, DefaultVertexFormat.POSITION_TEX), shader -> compositeShader = shader
         );
      } catch (Exception var2) {
         sourceMaskShader = null;
         compositeShader = null;
         LOGGER.error("[CrysisMod][Nanovision] failed to register TACZ scope outline shaders", var2);
      }
   }

   public static void compositeVisibleThermalTarget(RenderTarget scopeTarget) {
      if (isEnabled() && scopeTarget != null && ensureMaskTarget(scopeTarget.width, scopeTarget.height)) {
         ThermalFramebufferState caller = ThermalFramebufferState.capture();
         RenderSystem.backupProjectionMatrix();
         Matrix4fStack stack = RenderSystem.getModelViewStack();
         stack.pushMatrix();

         try {
            renderSourceMask(scopeTarget);
            renderGlow(scopeTarget);
         } catch (Throwable var7) {
            if (!warned) {
               warned = true;
               LOGGER.warn("[CrysisMod][Nanovision] visible TACZ scope outline failed; scope spectrum kept", var7);
            }
         } finally {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            stack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
            caller.restore();
         }
      }
   }

   public static void close() {
      if (maskTarget != null) {
         maskTarget.destroyBuffers();
      }

      maskTarget = null;
   }

   private static boolean isEnabled() {
      return sourceMaskShader != null
         && compositeShader != null
         && !NanovisionConfig.thermalMarkerDebugDisableGlow()
         && NanovisionConfig.thermalHeldItemOuterGlowRatio() > 1.0E-4
         && NanovisionConfig.thermalHeldItemOuterGlowStrength() > 0.001;
   }

   private static boolean ensureMaskTarget(int width, int height) {
      if (maskTarget != null && maskTarget.width == width && maskTarget.height == height) {
         return true;
      } else {
         if (maskTarget != null) {
            maskTarget.destroyBuffers();
         }

         maskTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
         maskTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         maskTarget.setFilterMode(9729);
         return true;
      }
   }

   private static void renderSourceMask(RenderTarget scopeTarget) {
      maskTarget.bindWrite(false);
      RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
      RenderSystem.clear(16384, Minecraft.ON_OSX);
      sourceMaskShader.setSampler("ThermalSampler", scopeTarget.getColorTextureId());
      drawFullscreen(sourceMaskShader, maskTarget.width, maskTarget.height, false);
      sourceMaskShader.clear();
   }

   private static void renderGlow(RenderTarget scopeTarget) {
      scopeTarget.bindWrite(false);
      compositeShader.setSampler("MaskSampler", maskTarget.getColorTextureId());
      set(compositeShader, "InSize", maskTarget.width, maskTarget.height);
      float radius = (float)Math.max(
         2.0, Math.min(Math.min(maskTarget.width, maskTarget.height) * NanovisionConfig.thermalHeldItemOuterGlowRatio() * 0.18, 24.0)
      );
      set(compositeShader, "GlowRadiusPx", radius);
      set(compositeShader, "GlowStrength", (float)NanovisionConfig.thermalHeldItemOuterGlowStrength());
      drawFullscreen(compositeShader, scopeTarget.width, scopeTarget.height, true);
      compositeShader.clear();
   }

   private static void drawFullscreen(ShaderInstance shader, int width, int height, boolean blend) {
      RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, width, height, 0.0F, 1000.0F, 3000.0F), VertexSorting.ORTHOGRAPHIC_Z);
      Matrix4fStack stack = RenderSystem.getModelViewStack();
      stack.identity();
      stack.translate(0.0F, 0.0F, -2000.0F);
      RenderSystem.applyModelViewMatrix();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      if (blend) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
      } else {
         RenderSystem.disableBlend();
      }

      RenderSystem.setShader(() -> shader);
      BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      buffer.addVertex(0.0F, height, 0.0F).setUv(0.0F, 0.0F);
      buffer.addVertex(width, height, 0.0F).setUv(1.0F, 0.0F);
      buffer.addVertex(width, 0.0F, 0.0F).setUv(1.0F, 1.0F);
      buffer.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, 1.0F);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
   }

   private static void set(ShaderInstance shader, String name, float x, float y) {
      Uniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(x, y);
      }
   }

   private static void set(ShaderInstance shader, String name, float value) {
      Uniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }
}
