package io.github.painworks.crysisscopefix.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.logging.LogUtils;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;

public final class ScopeDeferredCapture {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int APERTURE_STENCIL_VALUE = 1;
   private static final List<ScopeDeferredCapture.Segment> SEGMENTS = new ArrayList<>();
   private static Matrix4f projection;
   private static Matrix4f modelView;
   private static RenderTarget target;
   private static RenderType armedOrdinaryThermalType;
   private static BufferBuilder ordinaryBuilder;
   private static ByteBufferBuilder ordinaryStorage;
   private static RenderType ordinaryThermalType;
   private static boolean warned;
   private static boolean logged;
   private static boolean aperturePending;

   private ScopeDeferredCapture() {
   }

   public static void beginFrame() {
      RenderSystem.assertOnRenderThread();
      discardOrdinaryCapture();
      armedOrdinaryThermalType = null;
      discardSegments();
   }

   public static void armOrdinaryModelCapture(RenderType thermalType) {
      if (thermalType != null) {
         armedOrdinaryThermalType = thermalType;
      }
   }

   public static VertexConsumer wrapOrdinaryModelConsumer(VertexConsumer physical) {
      RenderSystem.assertOnRenderThread();
      if (physical != null && armedOrdinaryThermalType != null) {
         finishOrdinaryModelCapture();
         RenderType thermalType = armedOrdinaryThermalType;
         ensureMatrices();
         ordinaryStorage = new ByteBufferBuilder(Math.max(32768, thermalType.bufferSize()));
         ordinaryBuilder = new BufferBuilder(ordinaryStorage, thermalType.mode(), thermalType.format());
         ordinaryThermalType = thermalType;
         return VertexMultiConsumer.create(physical, ordinaryBuilder);
      } else {
         return physical;
      }
   }

   public static void finishOrdinaryModelCapture() {
      RenderSystem.assertOnRenderThread();
      if (ordinaryBuilder != null && ordinaryThermalType != null) {
         BufferBuilder builder = ordinaryBuilder;
         ByteBufferBuilder storage = ordinaryStorage;
         RenderType thermalType = ordinaryThermalType;
         ordinaryBuilder = null;
         ordinaryStorage = null;
         ordinaryThermalType = null;

         boolean retained = false;
         try {
            retained = retainSegment(thermalType, builder, storage, SegmentKind.THERMAL);
         } catch (Throwable var3) {
            warnOnce("failed to retain ordinary TACZ scope housing vertices", var3);
         } finally {
            if (!retained) {
               storage.close();
            }
         }
      }
   }

   public static void endScopeModelCapture() {
      finishOrdinaryModelCapture();
      armedOrdinaryThermalType = null;
   }

   public static void captureSightAperture(PoseStack poseStack, ItemDisplayContext context, RenderType originalType, int light, int overlay, List<?> path) {
      capture(poseStack, context, originalType, light, overlay, path, SegmentKind.APERTURE);
   }

   public static void captureThermal(PoseStack poseStack, ItemDisplayContext context, RenderType thermalType, int light, int overlay, List<?> path) {
      if (thermalType != null) {
         capture(poseStack, context, thermalType, light, overlay, path, ScopeDeferredCapture.SegmentKind.THERMAL);
      }
   }

   private static void capture(
      PoseStack poseStack, ItemDisplayContext context, RenderType renderType, int light, int overlay, List<?> rawPath, ScopeDeferredCapture.SegmentKind kind
   ) {
      RenderSystem.assertOnRenderThread();
      if (renderType != null && rawPath != null && !rawPath.isEmpty()) {
         ensureMatrices();
         ByteBufferBuilder storage = new ByteBufferBuilder(Math.max(32768, renderType.bufferSize()));
         boolean retained = false;
         BedrockPart part = null;
         boolean oldVisible = false;
         poseStack.pushPose();

         try {
            for (int i = 0; i < rawPath.size() - 1; i++) {
               ((BedrockPart)rawPath.get(i)).translateAndRotateAndScale(poseStack);
            }

            part = (BedrockPart)rawPath.get(rawPath.size() - 1);
            oldVisible = part.visible;
            part.visible = true;
            BufferBuilder builder = new BufferBuilder(storage, renderType.mode(), renderType.format());
            part.render(poseStack, context, builder, light, overlay);
            retained = retainSegment(renderType, builder, storage, kind);
         } catch (Throwable var15) {
            warnOnce("failed to retain TACZ scope vertices", var15);
         } finally {
            if (!retained) {
               storage.close();
            }
            if (part != null) {
               part.visible = oldVisible;
            }

            poseStack.popPose();
         }
      }
   }

   private static boolean retainSegment(RenderType renderType, BufferBuilder builder, ByteBufferBuilder storage, SegmentKind kind) {
      MeshData mesh = builder.build();
      if (mesh == null) {
         return false;
      }
      SEGMENTS.add(new Segment(renderType, mesh, storage, kind));
      aperturePending |= kind == SegmentKind.APERTURE;
      return true;
   }

   public static boolean flushDeferred() {
      RenderSystem.assertOnRenderThread();
      if (!SEGMENTS.isEmpty() && projection != null && modelView != null) {
         ScopeDeferredCapture.GlGlobalState caller = ScopeDeferredCapture.GlGlobalState.capture();
         StencilState stencilState = aperturePending ? StencilState.capture() : null;
         RenderSystem.backupProjectionMatrix();
         Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
         modelViewStack.pushMatrix();

         boolean var6;
         try {
            int width = caller.viewportWidth;
            int height = caller.viewportHeight;
            if (width <= 0 || height <= 0 || !beginTarget(width, height)) {
               return false;
            }

            RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);
            modelViewStack.set(modelView);
            RenderSystem.applyModelViewMatrix();
            boolean hasAperture = renderSegments(ScopeDeferredCapture.SegmentKind.APERTURE);
            boolean hasThermal = renderSegments(ScopeDeferredCapture.SegmentKind.THERMAL);
            if (hasThermal) {
               if (hasAperture) {
                  eraseAperture(width, height);
               }

               composite(caller);
               if (!logged) {
                  logged = true;
                  LOGGER.info("[CrysisScopeFix] scope-only thermal overlay composited; exact TACZ ocular aperture preserved");
               }

               return true;
            }

            var6 = false;
         } catch (Throwable var10) {
            warnOnce("scope-only thermal composite failed; physical scope was preserved", var10);
            return false;
         } finally {
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
            caller.restore();
            if (stencilState != null) {
               stencilState.restore();
            }
            discardSegments();
         }

         return var6;
      } else {
         return false;
      }
   }

   public static void discard() {
      RenderSystem.assertOnRenderThread();
      discardOrdinaryCapture();
      armedOrdinaryThermalType = null;
      discardSegments();
   }

   private static boolean renderSegments(ScopeDeferredCapture.SegmentKind requestedKind) {
      boolean rendered = false;

      for (ScopeDeferredCapture.Segment segment : SEGMENTS) {
         if (segment.kind == requestedKind) {
            segment.renderType.setupRenderState();
            try {
               target.bindWrite(false);
               if (requestedKind == ScopeDeferredCapture.SegmentKind.APERTURE) {
                  applyApertureWriteState();
               } else {
                  applyThermalState();
               }
               BufferUploader.drawWithShader(segment.buffer);
               rendered = true;
            } finally {
               segment.renderType.clearRenderState();
            }
         }
      }

      return rendered;
   }

   private static void applyApertureWriteState() {
      GL11.glEnable(2960);
      GL11.glStencilMask(255);
      GL11.glStencilFunc(519, 1, 255);
      GL11.glStencilOp(7680, 7680, 7681);
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.colorMask(false, false, false, false);
   }

   private static void applyThermalState() {
      GL11.glDisable(2960);
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
   }

   private static void eraseAperture(int width, int height) {
      target.bindWrite(false);
      RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, width, height, 0.0F, 1000.0F, 3000.0F), VertexSorting.ORTHOGRAPHIC_Z);
      Matrix4fStack stack = RenderSystem.getModelViewStack();
      stack.identity();
      stack.translate(0.0F, 0.0F, -2000.0F);
      RenderSystem.applyModelViewMatrix();
      GL11.glEnable(2960);
      GL11.glStencilMask(0);
      GL11.glStencilFunc(514, 1, 255);
      GL11.glStencilOp(7680, 7680, 7680);
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.disableBlend();
      RenderSystem.disableCull();
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      buffer.addVertex(0.0F, height, 0.0F).setColor(0, 0, 0, 0);
      buffer.addVertex(width, height, 0.0F).setColor(0, 0, 0, 0);
      buffer.addVertex(width, 0.0F, 0.0F).setColor(0, 0, 0, 0);
      buffer.addVertex(0.0F, 0.0F, 0.0F).setColor(0, 0, 0, 0);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
   }

   private static void composite(ScopeDeferredCapture.GlGlobalState destination) {
      destination.restoreFramebuffer();
      RenderSystem.setProjectionMatrix(
         new Matrix4f().setOrtho(0.0F, destination.viewportWidth, destination.viewportHeight, 0.0F, 1000.0F, 3000.0F), VertexSorting.ORTHOGRAPHIC_Z
      );
      Matrix4fStack stack = RenderSystem.getModelViewStack();
      stack.identity();
      stack.translate(0.0F, 0.0F, -2000.0F);
      RenderSystem.applyModelViewMatrix();
      GL11.glDisable(2960);
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, target.getColorTextureId());
      BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      buffer.addVertex(0.0F, destination.viewportHeight, 0.0F).setUv(0.0F, 0.0F);
      buffer.addVertex(destination.viewportWidth, destination.viewportHeight, 0.0F).setUv(1.0F, 0.0F);
      buffer.addVertex(destination.viewportWidth, 0.0F, 0.0F).setUv(1.0F, 1.0F);
      buffer.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, 1.0F);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
   }

   private static void ensureMatrices() {
      if (projection == null) {
         projection = new Matrix4f(RenderSystem.getProjectionMatrix());
         modelView = new Matrix4f(RenderSystem.getModelViewMatrix());
      }
   }

   private static boolean beginTarget(int width, int height) {
      if (target == null || target.width != width || target.height != height) {
         if (target != null) {
            target.destroyBuffers();
         }

         target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
         target.enableStencil();
         target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         target.setFilterMode(9729);
      }

      if (!target.isStencilEnabled()) {
         target.enableStencil();
      }

      target.bindWrite(true);
      target.clear(Minecraft.ON_OSX);
      target.bindWrite(true);
      if (aperturePending) {
         GL11.glStencilMask(0xFF);
         GL11.glClearStencil(0);
         GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
      }
      return target.isStencilEnabled() && GL30.glCheckFramebufferStatus(36009) == 36053;
   }

   private static void discardOrdinaryCapture() {
      if (ordinaryStorage != null) {
         ordinaryStorage.close();
      }

      ordinaryBuilder = null;
      ordinaryStorage = null;
      ordinaryThermalType = null;
   }

   private static void discardSegments() {
      for (ScopeDeferredCapture.Segment segment : SEGMENTS) {
         segment.buffer.close();
         segment.storage.close();
      }

      SEGMENTS.clear();
      projection = null;
      modelView = null;
      aperturePending = false;
   }

   private record StencilState(int clearValue, StencilFace front, StencilFace back) {
      static StencilState capture() {
         return new StencilState(GL11.glGetInteger(GL11.GL_STENCIL_CLEAR_VALUE),
            new StencilFace(GL11.glGetInteger(GL11.GL_STENCIL_FUNC), GL11.glGetInteger(GL11.GL_STENCIL_REF),
               GL11.glGetInteger(GL11.GL_STENCIL_VALUE_MASK), GL11.glGetInteger(GL11.GL_STENCIL_WRITEMASK),
               GL11.glGetInteger(GL11.GL_STENCIL_FAIL), GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_FAIL),
               GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_PASS)),
            new StencilFace(GL11.glGetInteger(GL20.GL_STENCIL_BACK_FUNC), GL11.glGetInteger(GL20.GL_STENCIL_BACK_REF),
               GL11.glGetInteger(GL20.GL_STENCIL_BACK_VALUE_MASK), GL11.glGetInteger(GL20.GL_STENCIL_BACK_WRITEMASK),
               GL11.glGetInteger(GL20.GL_STENCIL_BACK_FAIL), GL11.glGetInteger(GL20.GL_STENCIL_BACK_PASS_DEPTH_FAIL),
               GL11.glGetInteger(GL20.GL_STENCIL_BACK_PASS_DEPTH_PASS)));
      }

      void restore() {
         GL11.glClearStencil(clearValue);
         front.restore(GL11.GL_FRONT);
         back.restore(GL11.GL_BACK);
      }
   }

   private record StencilFace(int function, int reference, int valueMask, int writeMask, int fail, int depthFail, int pass) {
      void restore(int face) {
         GL20.glStencilFuncSeparate(face, function, reference, valueMask);
         GL20.glStencilMaskSeparate(face, writeMask);
         GL20.glStencilOpSeparate(face, fail, depthFail, pass);
      }
   }

   private static void warnOnce(String message, Throwable error) {
      if (!warned) {
         warned = true;
         LOGGER.warn("[CrysisScopeFix] {}", message, error);
      }
   }

   private static final class GlGlobalState {
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
      private final boolean depthMask;
      private final boolean[] colorMask;
      private final int blendSrcRgb;
      private final int blendDstRgb;
      private final int blendSrcAlpha;
      private final int blendDstAlpha;

      private GlGlobalState() {
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
         this.depthMask = GL11.glGetBoolean(2930);
         this.colorMask = new boolean[]{colors.get(0) != 0, colors.get(1) != 0, colors.get(2) != 0, colors.get(3) != 0};
         this.blendSrcRgb = GL11.glGetInteger(32969);
         this.blendDstRgb = GL11.glGetInteger(32968);
         this.blendSrcAlpha = GL11.glGetInteger(32971);
         this.blendDstAlpha = GL11.glGetInteger(32970);
      }

      private static ScopeDeferredCapture.GlGlobalState capture() {
         return new ScopeDeferredCapture.GlGlobalState();
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
         GL11.glDepthMask(this.depthMask);
         GL11.glColorMask(this.colorMask[0], this.colorMask[1], this.colorMask[2], this.colorMask[3]);
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

   private static final class Segment {
      private final RenderType renderType;
      private final MeshData buffer;
      private final ByteBufferBuilder storage;
      private final ScopeDeferredCapture.SegmentKind kind;

      private Segment(RenderType renderType, MeshData buffer, ByteBufferBuilder storage, ScopeDeferredCapture.SegmentKind kind) {
         this.renderType = renderType;
         this.buffer = buffer;
         this.storage = storage;
         this.kind = kind;
      }
   }

   private static enum SegmentKind {
      APERTURE,
      THERMAL;
   }
}
