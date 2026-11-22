package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import java.io.IOException;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;

public final class ThermalFirstPersonItemGlowCapture {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation COMPOSITE_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_first_person_item_glow_composite");
   private static final ResourceLocation VISIBLE_MASK_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_first_person_item_glow_visible_mask");
   private static RenderTarget maskTarget;
   private static RenderTarget vanillaPreHandDepth;
   private static RenderTarget shaderOffFinalHand;
   private static ShaderInstance compositeShader;
   private static ShaderInstance visibleMaskShader;
   private static boolean handPassSeen;
   private static boolean vanillaPreHandReady;
   private static boolean oculusBridgeResolved;
   private static boolean oculusBridgeUnavailable;
   private static boolean warnedShaderOffFramebufferMismatch;
   private static boolean warnedShaderOffSnapshotFailure;
   private static int shaderOffPreHandFramebuffer = -1;
   private static Method oculusPreHandDepth;
   private static Method oculusCurrentDepth;

   private ThermalFirstPersonItemGlowCapture() {
   }

   public static void registerShaders(RegisterShadersEvent event) {
      try {
         event.registerShader(
            new ShaderInstance(event.getResourceProvider(), COMPOSITE_SHADER, DefaultVertexFormat.POSITION_TEX), shader -> compositeShader = shader
         );
         event.registerShader(
            new ShaderInstance(event.getResourceProvider(), VISIBLE_MASK_SHADER, DefaultVertexFormat.POSITION_TEX), shader -> visibleMaskShader = shader
         );
      } catch (IOException var2) {
         compositeShader = null;
         visibleMaskShader = null;
         LOGGER.error("[CrysisMod][Nanovision] failed to register first-person visible-outline shaders", var2);
      }
   }

   public static void beginFrame() {
      handPassSeen = false;
      vanillaPreHandReady = false;
      shaderOffPreHandFramebuffer = -1;
   }

   public static void capturePreHandDepth() {
      if (!NanovisionState.isActive()) return;
      if (NanoGeoShaderpackCompatibility.isShaderPackActive()) {
         if (isEnabled()) {
            handPassSeen = true;
            ThermalFramebufferState state = ThermalFramebufferState.capture();
            if (state.restoreAndValidateDrawTarget()) {
               try {
                  if (!NanoGeoShaderpackCompatibility.isShaderPackActive()) {
                     if (ensureVanillaPreHandDepth(state.viewportWidth(), state.viewportHeight(), false)) {
                        vanillaPreHandReady = copyCallerColor(state, vanillaPreHandDepth);
                        shaderOffPreHandFramebuffer = vanillaPreHandReady ? state.drawFramebuffer() : -1;
                        return;
                     }

                     return;
                  }

                  Minecraft minecraft = Minecraft.getInstance();
                  RenderTarget main = minecraft.getMainRenderTarget();
                  if (main != null && ensureVanillaPreHandDepth(main.width, main.height, main.isStencilEnabled())) {
                     GL30.glBindFramebuffer(36008, main.frameBufferId);
                     GL30.glBindFramebuffer(36009, vanillaPreHandDepth.frameBufferId);
                     GL30.glBlitFramebuffer(0, 0, main.width, main.height, 0, 0, vanillaPreHandDepth.width, vanillaPreHandDepth.height, 16640, 9728);
                     vanillaPreHandReady = GL11.glGetError() == 0;
                     return;
                  }
               } finally {
                  state.restore();
               }
            }
         }
      }
   }

   public static VertexConsumer capture(RenderType ignored) {
      return null;
   }

   public static VertexConsumer wrapInlineTaczScopeBody(BufferSource ignoredSource, RenderType ignoredType, VertexConsumer acceptedConsumer) {
      return acceptedConsumer;
   }

   public static void flush(boolean spectrumRendered) {
      if (NanoGeoShaderpackCompatibility.isShaderPackActive()) {
         if (spectrumRendered && handPassSeen && isEnabled()) {
            ThermalFramebufferState caller = ThermalFramebufferState.capture();
            if (caller.restoreAndValidateDrawTarget()) {
               ThermalFirstPersonItemGlowCapture.DepthPair depths;
               if (NanoGeoShaderpackCompatibility.isShaderPackActive()) {
                  Minecraft minecraft = Minecraft.getInstance();
                  RenderTarget main = minecraft.getMainRenderTarget();
                  if (main == null || !ensureMaskTarget(main.width, main.height)) {
                     return;
                  }

                  depths = resolveDepthPair(main);
               } else {
                  if (!vanillaPreHandReady || shaderOffPreHandFramebuffer != caller.drawFramebuffer()) {
                     if (!warnedShaderOffFramebufferMismatch) {
                        warnedShaderOffFramebufferMismatch = true;
                        LOGGER.warn(
                           "[CrysisMod][Nanovision] shader-off item outline skipped: hand FBO {} changed to {} before composite",
                           shaderOffPreHandFramebuffer,
                           caller.drawFramebuffer()
                        );
                     }

                     return;
                  }

                  if (!ensureMaskTarget(caller.viewportWidth(), caller.viewportHeight())
                     || !ensureShaderOffFinalHand(caller.viewportWidth(), caller.viewportHeight())
                     || !copyCallerColor(caller, shaderOffFinalHand)) {
                     if (!warnedShaderOffSnapshotFailure) {
                        warnedShaderOffSnapshotFailure = true;
                        LOGGER.warn("[CrysisMod][Nanovision] shader-off item outline skipped: could not snapshot the active hand framebuffer");
                     }

                     return;
                  }

                  depths = new ThermalFirstPersonItemGlowCapture.DepthPair(
                     vanillaPreHandDepth.getDepthTextureId(),
                     shaderOffFinalHand.getDepthTextureId(),
                     0,
                     vanillaPreHandDepth.getColorTextureId(),
                     shaderOffFinalHand.getColorTextureId(),
                     1
                  );
               }

               if (depths != null) {
                  RenderSystem.backupProjectionMatrix();
                  Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
                  modelViewStack.pushMatrix();

                  try {
                     if (caller.restoreAndValidateDrawTarget()) {
                        renderVisibleMask(caller, depths);
                        composite(caller);
                        return;
                     }
                  } catch (Throwable var8) {
                     LOGGER.warn("[CrysisMod][Nanovision] final visible item outline failed; base spectrum kept", var8);
                     return;
                  } finally {
                     RenderSystem.disableBlend();
                     RenderSystem.depthMask(true);
                     RenderSystem.enableDepthTest();
                     modelViewStack.popMatrix();
                     RenderSystem.applyModelViewMatrix();
                     RenderSystem.restoreProjectionMatrix();
                     caller.restore();
                  }
               }
            }
         }
      }
   }

   public static void discard() {
      handPassSeen = false;
      vanillaPreHandReady = false;
   }

   public static void close() {
      discard();
      destroy(maskTarget);
      maskTarget = null;
      destroy(vanillaPreHandDepth);
      vanillaPreHandDepth = null;
      destroy(shaderOffFinalHand);
      shaderOffFinalHand = null;
   }

   private static boolean isEnabled() {
      return compositeShader != null
         && visibleMaskShader != null
         && !NanovisionConfig.thermalMarkerDebugDisableGlow()
         && NanovisionConfig.thermalHeldItemOuterGlowRatio() > 1.0E-4
         && NanovisionConfig.thermalHeldItemOuterGlowStrength() > 0.001;
   }

   private static boolean ensureMaskTarget(int width, int height) {
      if (maskTarget != null && maskTarget.width == width && maskTarget.height == height) {
         return true;
      } else {
         destroy(maskTarget);
         maskTarget = new TextureTarget(width, height, false, Minecraft.ON_OSX);
         maskTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         maskTarget.setFilterMode(9729);
         return true;
      }
   }

   private static boolean ensureVanillaPreHandDepth(int width, int height, boolean stencil) {
      if (vanillaPreHandDepth != null && vanillaPreHandDepth.width == width && vanillaPreHandDepth.height == height
         && vanillaPreHandDepth.isStencilEnabled() == stencil) {
         return true;
      } else {
         destroy(vanillaPreHandDepth);
         vanillaPreHandDepth = new TextureTarget(width, height, true, Minecraft.ON_OSX);
         if (stencil) vanillaPreHandDepth.enableStencil();
         vanillaPreHandDepth.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         vanillaPreHandDepth.setFilterMode(9728);
         vanillaPreHandReady = false;
         return true;
      }
   }

   private static boolean ensureShaderOffFinalHand(int width, int height) {
      if (shaderOffFinalHand != null && shaderOffFinalHand.width == width && shaderOffFinalHand.height == height) {
         return true;
      } else {
         destroy(shaderOffFinalHand);
         shaderOffFinalHand = new TextureTarget(width, height, true, Minecraft.ON_OSX);
         shaderOffFinalHand.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         shaderOffFinalHand.setFilterMode(9728);
         return true;
      }
   }

   private static boolean copyCallerColor(ThermalFramebufferState caller, RenderTarget target) {
      if (target != null && caller.viewportWidth() > 0 && caller.viewportHeight() > 0) {
         boolean var2;
         try {
            while (GL11.glGetError() != 0) {
            }

            GL30.glBindFramebuffer(36008, caller.drawFramebuffer());
            GL30.glBindFramebuffer(36009, target.frameBufferId);
            GL30.glBlitFramebuffer(
               caller.viewportX(),
               caller.viewportY(),
               caller.viewportX() + caller.viewportWidth(),
               caller.viewportY() + caller.viewportHeight(),
               0,
               0,
               target.width,
               target.height,
               16384,
               9728
            );
            var2 = GL11.glGetError() == 0;
         } finally {
            caller.restore();
         }

         return var2;
      } else {
         return false;
      }
   }

   private static ThermalFirstPersonItemGlowCapture.DepthPair resolveDepthPair(RenderTarget main) {
      if (NanoGeoShaderpackCompatibility.isShaderPackActive()) {
         ThermalFirstPersonItemGlowCapture.DepthPair oculus = resolveOculusDepthPair(main.getDepthTextureId());
         if (oculus != null) {
            return oculus;
         }
      }

      return vanillaPreHandReady && vanillaPreHandDepth != null
         ? new ThermalFirstPersonItemGlowCapture.DepthPair(
            vanillaPreHandDepth.getDepthTextureId(), main.getDepthTextureId(), 0, vanillaPreHandDepth.getColorTextureId(), main.getColorTextureId(), 1
         )
         : null;
   }

   private static ThermalFirstPersonItemGlowCapture.DepthPair resolveOculusDepthPair(int fallback) {
      if (oculusBridgeUnavailable) {
         return null;
      } else {
         try {
            if (!oculusBridgeResolved) {
               Class<?> bridge = Class.forName("io.github.painworks.crysisscopeadspatch.render.OculusPreHandDepth");
               oculusPreHandDepth = bridge.getMethod("textureOr", int.class);
               oculusCurrentDepth = bridge.getMethod("currentTextureOr", int.class);
               oculusBridgeResolved = true;
            }

            int beforeHands = (Integer)oculusPreHandDepth.invoke(null, fallback);
            int afterHands = (Integer)oculusCurrentDepth.invoke(null, fallback);
            return beforeHands > 0 && afterHands > 0 ? new ThermalFirstPersonItemGlowCapture.DepthPair(beforeHands, afterHands, 0, 0, 0, 0) : null;
         } catch (Throwable var3) {
            oculusBridgeUnavailable = true;
            LOGGER.warn("[CrysisMod][Nanovision] Oculus hand-depth bridge unavailable; using vanilla snapshot", var3);
            return null;
         }
      }
   }

   private static void renderVisibleMask(ThermalFramebufferState caller, ThermalFirstPersonItemGlowCapture.DepthPair depths) {
      if (visibleMaskShader != null && maskTarget != null) {
         ThermalFramebufferState state = ThermalFramebufferState.capture();

         try {
            maskTarget.bindWrite(false);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
            RenderSystem.clear(16384, Minecraft.ON_OSX);
            visibleMaskShader.setSampler("PreHandDepthSampler", depths.beforeHands());
            visibleMaskShader.setSampler("FinalHandDepthSampler", depths.afterHands());
            visibleMaskShader.setSampler("ScopeBodySampler", Minecraft.getInstance().getMainRenderTarget().getColorTextureId());
            visibleMaskShader.setSampler(
               "PreHandColorSampler",
               depths.useColorDelta() != 0 ? depths.preHandColorTexture() : Minecraft.getInstance().getMainRenderTarget().getColorTextureId()
            );
            visibleMaskShader.setSampler(
               "FinalHandColorSampler",
               depths.useColorDelta() != 0 ? depths.finalHandColorTexture() : Minecraft.getInstance().getMainRenderTarget().getColorTextureId()
            );
            setInt(visibleMaskShader, "ScopeBodyEnabled", 0);
            setInt(visibleMaskShader, "UseColorDelta", depths.useColorDelta());
            drawFullscreen(visibleMaskShader, maskTarget.width, maskTarget.height, false);
            visibleMaskShader.clear();
         } finally {
            state.restore();
            caller.restoreAndValidateDrawTarget();
         }
      }
   }

   private static void composite(ThermalFramebufferState caller) {
      if (maskTarget != null && compositeShader != null && caller.restoreAndValidateDrawTarget()) {
         int width = caller.viewportWidth();
         int height = caller.viewportHeight();
         compositeShader.setSampler("MaskSampler", maskTarget.getColorTextureId());
         set(compositeShader, "InSize", maskTarget.width, maskTarget.height);
         float radius = (float)Math.max(
            2.0, Math.min(Math.min(maskTarget.width, maskTarget.height) * NanovisionConfig.thermalHeldItemOuterGlowRatio() * 0.18, 24.0)
         );
         set(compositeShader, "GlowRadiusPx", radius);
         set(compositeShader, "GlowStrength", (float)NanovisionConfig.thermalHeldItemOuterGlowStrength());
         drawFullscreen(compositeShader, width, height, true);
         compositeShader.clear();
      }
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

   private static void setInt(ShaderInstance shader, String name, int value) {
      Uniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }

   private static void destroy(RenderTarget target) {
      if (target != null) {
         target.destroyBuffers();
      }
   }

   private record DepthPair(int beforeHands, int afterHands, int scopeBodyTexture, int preHandColorTexture, int finalHandColorTexture, int useColorDelta) {
   }
}
