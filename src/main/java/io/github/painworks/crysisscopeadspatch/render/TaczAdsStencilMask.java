package io.github.painworks.crysisscopeadspatch.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import java.nio.ByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.slf4j.Logger;

public final class TaczAdsStencilMask {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int INVERTED_ADS_BIT = 128;
   private static RenderTarget adsMask;
   private static boolean opticalStencilReady;
   private static boolean captured;
   private static boolean maskValid;
   private static boolean scopeAdsSeen;
   private static TaczAdsStencilMask.GlState stableExclusionState;
   private static boolean captureLogged;
   private static boolean eraseLogged;
   private static boolean stableExclusionLogged;
   private static boolean warned;

   private TaczAdsStencilMask() {
   }

   public static void beginFrame() {
      RenderSystem.assertOnRenderThread();
      opticalStencilReady = false;
      captured = false;
      maskValid = false;
      scopeAdsSeen = false;
      stableExclusionState = null;
   }

   public static void markOpticalStencilReady() {
      RenderSystem.assertOnRenderThread();
      opticalStencilReady = true;
      scopeAdsSeen = true;
   }

   public static void captureBeforeStencilClear() {
      RenderSystem.assertOnRenderThread();
      if (opticalStencilReady && !captured && shaderpackActive()) {
         TaczAdsStencilMask.GlState caller = TaczAdsStencilMask.GlState.capture();
         TaczAdsStencilMask.ProjectionState projection = TaczAdsStencilMask.ProjectionState.capture();
         Matrix4fStack modelView = RenderSystem.getModelViewStack();
         modelView.pushMatrix();
         boolean attachmentInstalled = false;

         try {
            try {
               int width = caller.viewportWidth;
               int height = caller.viewportHeight;
               if (width <= 0 || height <= 0 || caller.drawFramebuffer == 0 || !ensureMask(width, height)) {
                  return;
               }

               GL30.glBindFramebuffer(36009, caller.drawFramebuffer);
               int objectType = GL30.glGetFramebufferAttachmentParameteri(36009, 36128, 36048);
               int objectName = GL30.glGetFramebufferAttachmentParameteri(36009, 36128, 36049);
               if (objectType == 0 || objectName == 0) {
                  warnOnce("ADS framebuffer has no live TACZ stencil attachment", null);
                  return;
               }

               int textureLevel = objectType == 5890 ? GL30.glGetFramebufferAttachmentParameteri(36009, 36128, 36050) : 0;
               adsMask.bindWrite(false);
               adsMask.clear(Minecraft.ON_OSX);
               adsMask.bindWrite(false);
               if (objectType == 5890) {
                  GL32.glFramebufferTexture(36009, 36128, objectName, textureLevel);
               } else {
                  if (objectType != 36161) {
                     warnOnce("unsupported TACZ stencil attachment type " + objectType, null);
                     return;
                  }

                  GL30.glFramebufferRenderbuffer(36009, 36128, 36161, objectName);
               }

               attachmentInstalled = true;
               int status = GL30.glCheckFramebufferStatus(36009);
               if (status != 36053) {
                  warnOnce("shared TACZ stencil mask framebuffer incomplete: " + status, null);
                  return;
               }

               setFullscreenMatrices(width, height);
               GL11.glEnable(2960);
               GL11.glStencilMask(0);
               GL11.glStencilFunc(514, 128, 128);
               GL11.glStencilOp(7680, 7680, 7680);
               RenderSystem.disableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.disableBlend();
               RenderSystem.disableCull();
               RenderSystem.colorMask(true, true, true, true);
               RenderSystem.setShader(GameRenderer::getPositionColorShader);
               drawWhiteMask(width, height);
               captured = true;
               maskValid = true;
               if (!captureLogged) {
                  captureLogged = true;
                  LOGGER.info(
                     "[CrysisScopeFix] captured TACZ inverted ADS aperture before stencil clear (framebuffer={}, stencil={}, viewport={}x{})",
                     new Object[]{caller.drawFramebuffer, objectName, width, height}
                  );
                  return;
               }
            } catch (Throwable var13) {
               warnOnce("failed to capture TACZ inverted ADS aperture", var13);
            }
         } finally {
            if (attachmentInstalled) {
               GL30.glBindFramebuffer(36009, adsMask.frameBufferId);
               GL32.glFramebufferTexture(36009, 36128, 0, 0);
               GL30.glFramebufferRenderbuffer(36009, 36128, 36161, 0);
            }

            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
            projection.restore();
            caller.restore();
            opticalStencilReady = false;
         }
      }
   }

   public static boolean eraseFromThermalOverlay(RenderTarget thermalTarget) {
      RenderSystem.assertOnRenderThread();
      if (shaderpackActive()
         && maskValid
         && scopeAdsSeen
         && adsMask != null
         && thermalTarget != null
         && thermalTarget.width == adsMask.width
         && thermalTarget.height == adsMask.height) {
         TaczAdsStencilMask.GlState caller = TaczAdsStencilMask.GlState.capture();
         TaczAdsStencilMask.ProjectionState projection = TaczAdsStencilMask.ProjectionState.capture();
         Matrix4fStack modelView = RenderSystem.getModelViewStack();
         modelView.pushMatrix();

         boolean var5;
         try {
            thermalTarget.bindWrite(false);
            RenderSystem.viewport(0, 0, thermalTarget.width, thermalTarget.height);
            setFullscreenMatrices(thermalTarget.width, thermalTarget.height);
            GL11.glDisable(2960);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            GL14.glBlendFuncSeparate(0, 771, 0, 771);
            RenderSystem.disableCull();
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, adsMask.getColorTextureId());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            drawTexture(thermalTarget.width, thermalTarget.height);
            if (!eraseLogged) {
               eraseLogged = true;
               LOGGER.info("[CrysisScopeFix] removed thermal overlay only from TACZ's active ADS aperture");
            }

            return true;
         } catch (Throwable var9) {
            warnOnce("failed to erase ADS aperture from scope thermal overlay", var9);
            var5 = false;
         } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
            projection.restore();
            caller.restore();
         }

         return var5;
      } else {
         return false;
      }
   }

   public static boolean beginStableThermalExclusion() {
      RenderSystem.assertOnRenderThread();
      stableExclusionState = null;
      ShaderInstance maskShader = TaczAdsMaskShaders.maskReplayShader();
      if (shaderpackActive() && maskValid && scopeAdsSeen && adsMask != null && maskShader != null) {
         TaczAdsStencilMask.GlState caller = TaczAdsStencilMask.GlState.capture();
         TaczAdsStencilMask.ProjectionState projection = TaczAdsStencilMask.ProjectionState.capture();
         Matrix4fStack modelView = RenderSystem.getModelViewStack();
         modelView.pushMatrix();
         ShaderInstance callerShader = RenderSystem.getShader();
         boolean matricesRestored = false;

         boolean var9;
         try {
            int width = caller.viewportWidth;
            int height = caller.viewportHeight;
            if (width <= 0 || height <= 0 || caller.drawFramebuffer == 0 || adsMask.width != width || adsMask.height != height) {
               return false;
            }

            caller.restoreFramebuffer();
            int stencilType = GL30.glGetFramebufferAttachmentParameteri(36009, 36128, 36048);
            if (stencilType != 0) {
               GL11.glDisable(3089);
               GL11.glEnable(2960);
               GL11.glStencilMask(128);
               GL11.glClearStencil(0);
               RenderSystem.clear(1024, Minecraft.ON_OSX);
               setFullscreenMatrices(width, height);
               RenderSystem.disableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.disableBlend();
               RenderSystem.disableCull();
               RenderSystem.colorMask(false, false, false, false);
               GL11.glStencilMask(128);
               GL11.glStencilFunc(519, 128, 128);
               GL11.glStencilOp(7680, 7680, 7681);
               maskShader.setSampler("MaskSampler", adsMask.getColorTextureId());
               RenderSystem.setShader(TaczAdsMaskShaders::maskReplayShader);
               RenderSystem.setShaderTexture(0, adsMask.getColorTextureId());
               drawTexture(width, height);
               maskShader.clear();
               modelView.popMatrix();
               RenderSystem.applyModelViewMatrix();
               projection.restore();
               matricesRestored = true;
               caller.restore();
               GL11.glEnable(2960);
               GL11.glStencilMask(0);
               GL11.glStencilFunc(514, 0, 128);
               GL11.glStencilOp(7680, 7680, 7680);
               stableExclusionState = caller;
               if (!stableExclusionLogged) {
                  stableExclusionLogged = true;
                  LOGGER.info("[CrysisScopeFix] stable held-item thermal draw is stencil-excluded from the active TACZ ADS aperture");
               }

               return true;
            }

            warnOnce("late held-item framebuffer has no stencil attachment", null);
            var9 = false;
         } catch (Throwable var13) {
            warnOnce("failed to begin TACZ ADS thermal exclusion", var13);
            return false;
         } finally {
            if (stableExclusionState == null) {
               RenderSystem.setShader(() -> callerShader);
               if (!matricesRestored) {
                  modelView.popMatrix();
                  RenderSystem.applyModelViewMatrix();
                  projection.restore();
               }

               caller.restore();
            }
         }

         return var9;
      } else {
         return false;
      }
   }

   public static void endStableThermalExclusion() {
      RenderSystem.assertOnRenderThread();
      TaczAdsStencilMask.GlState caller = stableExclusionState;
      stableExclusionState = null;
      if (caller != null) {
         try {
            caller.restoreFramebuffer();
            GL11.glDisable(3089);
            GL11.glEnable(2960);
            GL11.glStencilMask(128);
            GL11.glClearStencil(0);
            RenderSystem.clear(1024, Minecraft.ON_OSX);
         } finally {
            caller.restore();
         }
      }
   }

   public static void discard() {
      RenderSystem.assertOnRenderThread();
      opticalStencilReady = false;
      captured = false;
      maskValid = false;
      stableExclusionState = null;
   }

   private static boolean ensureMask(int width, int height) {
      if (adsMask == null || adsMask.width != width || adsMask.height != height) {
         if (adsMask != null) {
            adsMask.destroyBuffers();
         }

         adsMask = new TextureTarget(width, height, false, Minecraft.ON_OSX);
         adsMask.setFilterMode(9728);
         adsMask.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      }

      return true;
   }

   private static boolean shaderpackActive() {
      return NanoGeoShaderpackCompatibility.isShaderPackActive();
   }

   private static void setFullscreenMatrices(int width, int height) {
      RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, width, height, 0.0F, 1000.0F, 3000.0F), VertexSorting.ORTHOGRAPHIC_Z);
      Matrix4fStack modelView = RenderSystem.getModelViewStack();
      modelView.identity();
      modelView.translate(0.0F, 0.0F, -2000.0F);
      RenderSystem.applyModelViewMatrix();
   }

   private static void drawWhiteMask(int width, int height) {
      BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      buffer.addVertex(0.0F, height, 0.0F).setColor(255, 255, 255, 255);
      buffer.addVertex(width, height, 0.0F).setColor(255, 255, 255, 255);
      buffer.addVertex(width, 0.0F, 0.0F).setColor(255, 255, 255, 255);
      buffer.addVertex(0.0F, 0.0F, 0.0F).setColor(255, 255, 255, 255);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
   }

   private static void drawTexture(int width, int height) {
      drawTextureOffset(width, height, 0, 0);
   }

   private static void drawTextureOffset(int width, int height, int offsetX, int offsetY) {
      BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      buffer.addVertex(offsetX, height + offsetY, 0.0F).setUv(0.0F, 0.0F);
      buffer.addVertex(width + offsetX, height + offsetY, 0.0F).setUv(1.0F, 0.0F);
      buffer.addVertex(width + offsetX, offsetY, 0.0F).setUv(1.0F, 1.0F);
      buffer.addVertex(offsetX, offsetY, 0.0F).setUv(0.0F, 1.0F);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
   }

   private static void warnOnce(String message, Throwable error) {
      if (!warned) {
         warned = true;
         if (error == null) {
            LOGGER.warn("[CrysisScopeFix] {}", message);
         } else {
            LOGGER.warn("[CrysisScopeFix] {}", message, error);
         }
      }
   }

   private static final class GlState {
      private final int readFramebuffer;
      private final int drawFramebuffer;
      private final int viewportX;
      private final int viewportY;
      private final int viewportWidth;
      private final int viewportHeight;
      private final boolean framebufferSrgb;
      private final boolean stencil;
      private final boolean depth;
      private final boolean blend;
      private final boolean cull;
      private final boolean scissor;
      private final boolean depthMask;
      private final boolean[] colorMask;
      private final int stencilWriteMask;
      private final int stencilFunc;
      private final int stencilRef;
      private final int stencilValueMask;
      private final int stencilFail;
      private final int stencilDepthFail;
      private final int stencilDepthPass;
      private final int blendSrcRgb;
      private final int blendDstRgb;
      private final int blendSrcAlpha;
      private final int blendDstAlpha;

      private GlState() {
         int[] viewport = new int[4];
         GL11.glGetIntegerv(2978, viewport);
         ByteBuffer colors = BufferUtils.createByteBuffer(4);
         GL11.glGetBooleanv(3107, colors);
         this.readFramebuffer = GL11.glGetInteger(36010);
         this.drawFramebuffer = GL11.glGetInteger(36006);
         this.viewportX = viewport[0];
         this.viewportY = viewport[1];
         this.viewportWidth = viewport[2];
         this.viewportHeight = viewport[3];
         this.framebufferSrgb = GL11.glIsEnabled(36281);
         this.stencil = GL11.glIsEnabled(2960);
         this.depth = GL11.glIsEnabled(2929);
         this.blend = GL11.glIsEnabled(3042);
         this.cull = GL11.glIsEnabled(2884);
         this.scissor = GL11.glIsEnabled(3089);
         this.depthMask = GL11.glGetBoolean(2930);
         this.colorMask = new boolean[]{colors.get(0) != 0, colors.get(1) != 0, colors.get(2) != 0, colors.get(3) != 0};
         this.stencilWriteMask = GL11.glGetInteger(2968);
         this.stencilFunc = GL11.glGetInteger(2962);
         this.stencilRef = GL11.glGetInteger(2967);
         this.stencilValueMask = GL11.glGetInteger(2963);
         this.stencilFail = GL11.glGetInteger(2964);
         this.stencilDepthFail = GL11.glGetInteger(2965);
         this.stencilDepthPass = GL11.glGetInteger(2966);
         this.blendSrcRgb = GL11.glGetInteger(32969);
         this.blendDstRgb = GL11.glGetInteger(32968);
         this.blendSrcAlpha = GL11.glGetInteger(32971);
         this.blendDstAlpha = GL11.glGetInteger(32970);
      }

      private static TaczAdsStencilMask.GlState capture() {
         return new TaczAdsStencilMask.GlState();
      }

      private void restoreFramebuffer() {
         GL30.glBindFramebuffer(36008, this.readFramebuffer);
         GL30.glBindFramebuffer(36009, this.drawFramebuffer);
         RenderSystem.viewport(this.viewportX, this.viewportY, this.viewportWidth, this.viewportHeight);
         set(36281, this.framebufferSrgb);
      }

      private void restore() {
         this.restoreFramebuffer();
         set(2960, this.stencil);
         set(2929, this.depth);
         set(3042, this.blend);
         set(2884, this.cull);
         set(3089, this.scissor);
         GL11.glDepthMask(this.depthMask);
         GL11.glColorMask(this.colorMask[0], this.colorMask[1], this.colorMask[2], this.colorMask[3]);
         GL11.glStencilMask(this.stencilWriteMask);
         GL11.glStencilFunc(this.stencilFunc, this.stencilRef, this.stencilValueMask);
         GL11.glStencilOp(this.stencilFail, this.stencilDepthFail, this.stencilDepthPass);
         GL14.glBlendFuncSeparate(this.blendSrcRgb, this.blendDstRgb, this.blendSrcAlpha, this.blendDstAlpha);
      }

      private static void set(int capability, boolean enabled) {
         if (enabled) {
            GL11.glEnable(capability);
         } else {
            GL11.glDisable(capability);
         }
      }
   }

   private static final class ProjectionState {
      private final Matrix4f matrix;
      private final VertexSorting sorting;

      private ProjectionState(Matrix4f matrix, VertexSorting sorting) {
         this.matrix = matrix;
         this.sorting = sorting;
      }

      private static TaczAdsStencilMask.ProjectionState capture() {
         return new TaczAdsStencilMask.ProjectionState(new Matrix4f(RenderSystem.getProjectionMatrix()), RenderSystem.getVertexSorting());
      }

      private void restore() {
         RenderSystem.setProjectionMatrix(this.matrix, this.sorting);
      }
   }
}
