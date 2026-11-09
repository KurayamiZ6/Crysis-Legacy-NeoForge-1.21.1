package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.SequencedMap;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.slf4j.Logger;

public final class ThermalFirstPersonItemCapture {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int BUFFER_SIZE = 32768;
   private static final SequencedMap<RenderType, ByteBufferBuilder> FIXED_BUFFERS = new LinkedHashMap<>();
   private static final Set<String> LOGGED_UNSUPPORTED_TYPES = new LinkedHashSet<>();
   private static ByteBufferBuilder fallbackBuffer = new ByteBufferBuilder(32768);
   private static ThermalDeferredBufferSource captureSource = createSource(FIXED_BUFFERS, fallbackBuffer);
   private static boolean handPassActive;
   private static boolean wrapping;
   private static int suppressionDepth;
   private static int taczScopeRenderDepth;
   private static int scopeBodyCaptureDepth;
   private static int sourceDelegationDepth;
   private static boolean pending;
   private static Matrix4f handProjection;
   private static Matrix4f handModelView;
   private static BufferSource lastDelegate;
   private static BufferSource lastWrapper;
   private static boolean loggedScope;
   private static boolean loggedAccepted;
   private static boolean loggedCapture;
   private static boolean loggedReplay;
   private static boolean loggedNativeProjection;
   private static boolean loggedCanonicalProjection;
   private static boolean loggedShaderOffRendererBridge;
   private static boolean loggedImmediateTaczGunBridge;
   private static boolean loggedScopeBodyCapture;
   private static boolean warnedReplayFailure;
   private static int debugScopeBodyPasses;
   private static long lastScopeDiagnosticNanos;

   private ThermalFirstPersonItemCapture() {
   }

   public static void beginFrame() {
      RenderSystem.assertOnRenderThread();
      handPassActive = false;
      wrapping = false;
      suppressionDepth = 0;
      taczScopeRenderDepth = 0;
      scopeBodyCaptureDepth = 0;
      sourceDelegationDepth = 0;
      debugScopeBodyPasses = 0;
      if (pending) {
         resetStorage();
      }
   }

   public static void beginHandPass(Minecraft minecraft) {
      RenderSystem.assertOnRenderThread();
      handPassActive = canCapture(minecraft);
      suppressionDepth = 0;
      taczScopeRenderDepth = 0;
      scopeBodyCaptureDepth = 0;
      if (handPassActive && !loggedScope) {
         loggedScope = true;
         LOGGER.info("[CrysisMod][Nanovision][HeldItemGeometry] renderer-owned item capture scope active");
      }
   }

   public static void endHandPass() {
      RenderSystem.assertOnRenderThread();
      handPassActive = false;
      suppressionDepth = 0;
      taczScopeRenderDepth = 0;
      scopeBodyCaptureDepth = 0;
      if (pending && !loggedCapture) {
         loggedCapture = true;
         LOGGER.info("[CrysisMod][Nanovision] captured isolated first-person item overlay geometry");
      }
   }

   public static void pushArmSuppression() {
      if (handPassActive) {
         suppressionDepth++;
      }
   }

   public static void popArmSuppression() {
      if (suppressionDepth > 0) {
         suppressionDepth--;
      }
   }

   public static void pushOpticsSuppression() {
      if (handPassActive) {
         suppressionDepth++;
      }
   }

   public static void popOpticsSuppression() {
      if (suppressionDepth > 0) {
         suppressionDepth--;
      }
   }

   public static void pushTaczScopeRender() {
      if (handPassActive) {
         taczScopeRenderDepth++;
      }
   }

   public static void popTaczScopeRender() {
      if (taczScopeRenderDepth > 0) {
         taczScopeRenderDepth--;
      }
   }

   public static void pushScopeBodyStencilCapture() {
      if (handPassActive) {
         scopeBodyCaptureDepth++;
         debugScopeBodyPasses++;
         if (!loggedScopeBodyCapture) {
            loggedScopeBodyCapture = true;
            LOGGER.info("[CrysisMod][Nanovision] routing TACZ scope housing through its native ocular-stencil pass");
         }
      }
   }

   public static void popScopeBodyStencilCapture() {
      if (scopeBodyCaptureDepth > 0) {
         scopeBodyCaptureDepth--;
      }
   }

   public static VertexConsumer wrap(RenderType originalType, VertexConsumer originalConsumer) {
      return wrap(currentChannel(), originalType, originalConsumer);
   }

   static VertexConsumer wrap(CaptureChannel channel, RenderType originalType, VertexConsumer originalConsumer) {
      if (handPassActive && channel != CaptureChannel.NONE && currentChannel() != CaptureChannel.NONE && !wrapping && originalConsumer != null) {
         RenderType captureType = ThermalFirstPersonItemRenderTypes.capture(originalType);
         if (captureType == null) {
            if (channel == CaptureChannel.DEFAULT) {
               logUnsupported(originalType, originalConsumer);
            }

            return originalConsumer;
         } else {
            wrapping = true;

            VertexConsumer var5;
            try {
               FIXED_BUFFERS.computeIfAbsent(captureType, ignored -> new ByteBufferBuilder(Math.max(32768, captureType.bufferSize())));
               VertexConsumer thermalConsumer = captureSource.getBuffer(captureType);
               beginPendingCapture();
               if (!loggedAccepted) {
                  loggedAccepted = true;
                  LOGGER.info(
                     "[CrysisMod][Nanovision][HeldItemGeometry] accepted renderType={} format={} consumer={}",
                     new Object[]{originalType, originalType.format(), originalConsumer.getClass().getName()}
                  );
               }

               var5 = VertexMultiConsumer.create(originalConsumer, thermalConsumer);
            } finally {
               wrapping = false;
            }

            return var5;
         }
      } else {
         return originalConsumer;
      }
   }

   public static VertexConsumer wrapBackend(RenderType type, VertexConsumer consumer) {
      return sourceDelegationDepth > 0 ? consumer : wrap(type, consumer);
   }

   static void beginSourceDelegation() {
      sourceDelegationDepth++;
   }

   static void endSourceDelegation() {
      if (sourceDelegationDepth > 0) {
         sourceDelegationDepth--;
      }
   }

   public static BufferSource wrapSource(BufferSource original) {
      if (handPassActive && original != null && !(original instanceof ThermalCapturingBufferSource)) {
         if (lastDelegate != original || lastWrapper == null) {
            lastDelegate = original;
            lastWrapper = new ThermalCapturingBufferSource(original, CaptureChannel.DEFAULT);
         }

         return lastWrapper;
      } else {
         return original;
      }
   }

   public static BufferSource wrapTaczAttachmentSource(BufferSource original) {
      if (handPassActive && original != null) {
         if (currentChannel() == CaptureChannel.NONE) {
            // TaCZ must see Iris's real buffer to flush while the ocular stencil is still active.
            return physicalSource(original);
         }
         if (!NanoGeoShaderpackCompatibility.isShaderPackActive()) {
            return wrapShaderOffRendererSource(original);
         }
      }
      return original;
   }

   public static RenderType prepareTaczScopeBodyThermalPass(RenderType originalType) {
      return prepareImmediateTaczThermalPass(originalType);
   }

   static RenderType prepareImmediateTaczGunThermalPass(RenderType originalType) {
      return prepareImmediateTaczThermalPass(originalType);
   }

   private static RenderType prepareImmediateTaczThermalPass(RenderType originalType) {
      if (handPassActive && originalType != null) {
         RenderType thermalType = ThermalFirstPersonItemRenderTypes.capture(originalType);
         if (thermalType == null) {
            return null;
         } else {
            ThermalFirstPersonItemShaders.prepareInlineGeometryOverlay(Minecraft.getInstance().getMainRenderTarget());
            return thermalType;
         }
      } else {
         return null;
      }
   }

   public static BufferSource wrapShaderOffTaczBedrockModelSource(BufferSource original, Object model) {
      if (NanoGeoShaderpackCompatibility.isShaderPackActive() || currentChannel() == CaptureChannel.NONE || original == null) {
         return original;
      } else if (model != null && "com.tacz.guns.client.model.BedrockGunModel".equals(model.getClass().getName())) {
         if (!loggedImmediateTaczGunBridge) {
            loggedImmediateTaczGunBridge = true;
            LOGGER.info("[CrysisMod][Nanovision] rendering TACZ parent gun thermal geometry inline before deferred replay");
         }

         return new ThermalImmediateTaczGunBufferSource(physicalSource(original));
      } else {
         return wrapShaderOffRendererSource(original);
      }
   }

   private static BufferSource physicalSource(BufferSource source) {
      return source instanceof ThermalCapturingBufferSource capture ? capture.physicalSource() : source;
   }

   public static BufferSource wrapShaderOffRendererSource(BufferSource original) {
      if (!NanoGeoShaderpackCompatibility.isShaderPackActive() && currentChannel() != CaptureChannel.NONE) {
         BufferSource wrapped = wrapSource(original);
         if (wrapped != original && !loggedShaderOffRendererBridge) {
            loggedShaderOffRendererBridge = true;
            LOGGER.info(
               "[CrysisMod][Nanovision][HeldItemGeometry] bridged final shader-off renderer source for held-item geometry class={}",
               original.getClass().getName()
            );
         }

         return wrapped;
      } else {
         return original;
      }
   }

   public static boolean hasPending() {
      return pending;
   }

   public static boolean shouldPreserveArmMasks() {
      return handPassActive || pending;
   }

   public static boolean flushDeferred() {
      RenderSystem.assertOnRenderThread();
      if (!pending || handProjection == null || handModelView == null) {
         return false;
      } else if (!hasSubmittedVertices()) {
         resetStorage();
         return false;
      } else {
         Matrix4f projection = handProjection;
         Matrix4f modelView = handModelView;
         pending = false;
         handProjection = null;
         handModelView = null;
         Minecraft minecraft = Minecraft.getInstance();
         RenderTarget destination = minecraft.getMainRenderTarget();
         if (destination == null) {
            resetStorage();
            return false;
         } else {
            ThermalFramebufferState callerState = ThermalFramebufferState.capture();
            ShaderInstance callerShader = RenderSystem.getShader();
            RenderSystem.backupProjectionMatrix();
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();

            boolean var8;
            try {
               destination.bindWrite(true);
               RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);
               modelViewStack.set(modelView);
               RenderSystem.applyModelViewMatrix();
               ThermalFirstPersonItemShaders.prepareGeometryOverlay(destination);
               captureSource.endBatch();
               logScopeDiagnosticsIfEnabled();
               if (!loggedReplay) {
                  loggedReplay = true;
                  LOGGER.info("[CrysisMod][Nanovision] rendered full held-item spectrum directly from captured geometry");
               }

               return true;
            } catch (Throwable var12) {
               if (!warnedReplayFailure) {
                  warnedReplayFailure = true;
                  LOGGER.warn("[CrysisMod][Nanovision] held-item geometry overlay failed; keeping physical item", var12);
               }

               var8 = false;
            } finally {
               RenderSystem.setShader(() -> callerShader);
               RenderSystem.disableBlend();
               RenderSystem.colorMask(true, true, true, true);
               RenderSystem.depthMask(true);
               RenderSystem.enableDepthTest();
               modelViewStack.popMatrix();
               RenderSystem.applyModelViewMatrix();
               RenderSystem.restoreProjectionMatrix();
               callerState.restore();
               resetStorage();
            }

            return var8;
         }
      }
   }

   public static void discardDeferred() {
      RenderSystem.assertOnRenderThread();
      resetStorage();
   }

   private static void logScopeDiagnosticsIfEnabled() {
      if (NanovisionConfig.nanovisionDebug() && debugScopeBodyPasses != 0) {
         long now = System.nanoTime();
         if (now - lastScopeDiagnosticNanos >= 1000000000L) {
            lastScopeDiagnosticNanos = now;
            LOGGER.info(
               "[CrysisMod][Nanovision][ScopeDebug] directBodyPasses={} deferredTypes={} deferredVertices={} shaderpack={}",
               new Object[]{
                  debugScopeBodyPasses, FIXED_BUFFERS.size(), captureSource.hasPendingBatches(), NanoGeoShaderpackCompatibility.isShaderPackActive()
               }
            );
         }
      }
   }

   private static CaptureChannel currentChannel() {
      if (suppressionDepth > 0) {
         return CaptureChannel.NONE;
      } else if (taczScopeRenderDepth > 0) {
         return CaptureChannel.NONE;
      } else {
         return scopeBodyCaptureDepth > 0 ? CaptureChannel.NONE : CaptureChannel.DEFAULT;
      }
   }

   private static void beginPendingCapture() {
      if (!pending) {
         handProjection = captureReplayProjection();
         handModelView = new Matrix4f(RenderSystem.getModelViewMatrix());
      }

      pending = true;
   }

   private static boolean hasSubmittedVertices() {
      return captureSource.hasPendingBatches();
   }

   private static boolean canCapture(Minecraft minecraft) {
      return minecraft != null
         && minecraft.player != null
         && !io.github.painworks.crysismod.nanosuit.client.render.NanosuitCloakRender.isDrawing()
         && minecraft.options.getCameraType().isFirstPerson()
         && NanovisionState.isActive(minecraft.player)
         && NanovisionConfig.thermalHeldItemsEnabled()
         && ThermalFirstPersonItemShaders.isAvailable()
         && (!minecraft.player.getMainHandItem().isEmpty() || !minecraft.player.getOffhandItem().isEmpty());
   }

   private static ThermalDeferredBufferSource createSource(SequencedMap<RenderType, ByteBufferBuilder> buffers, ByteBufferBuilder fallback) {
      return new ThermalDeferredBufferSource(buffers, fallback);
   }

   private static Matrix4f captureReplayProjection() {
      Matrix4f activeProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
      if (!NanoGeoShaderpackCompatibility.isShaderPackActive()) {
         if (!loggedNativeProjection) {
            loggedNativeProjection = true;
            LOGGER.info("[CrysisMod][Nanovision][HeldItemGeometry] using exact native hand projection for shader-off replay");
         }

         return activeProjection;
      } else {
         if (!loggedCanonicalProjection) {
            loggedCanonicalProjection = true;
            LOGGER.info("[CrysisMod][Nanovision][HeldItemGeometry] normalizing depth projection for shaderpack replay");
         }

         return canonicalizeDepthProjection(activeProjection);
      }
   }

   private static Matrix4f canonicalizeDepthProjection(Matrix4f activeProjection) {
      Matrix4f vanillaProjection = Minecraft.getInstance().gameRenderer.getProjectionMatrix(70.0);
      activeProjection.m02(vanillaProjection.m02()).m12(vanillaProjection.m12()).m22(vanillaProjection.m22()).m32(vanillaProjection.m32());
      return activeProjection;
   }

   private static void logUnsupported(RenderType type, VertexConsumer consumer) {
      if (LOGGED_UNSUPPORTED_TYPES.size() < 12) {
         String key = type.getClass().getName() + "|" + type.format() + "|" + type.mode();
         if (LOGGED_UNSUPPORTED_TYPES.add(key)) {
            LOGGER.info(
               "[CrysisMod][Nanovision][HeldItemGeometry] skipped unsupported renderType={} class={} format={} mode={} consumer={}",
               new Object[]{type, type.getClass().getName(), type.format(), type.mode(), consumer.getClass().getName()}
            );
         }
      }
   }

   private static void resetStorage() {
      discard(FIXED_BUFFERS, fallbackBuffer);
      fallbackBuffer = new ByteBufferBuilder(32768);
      captureSource = createSource(FIXED_BUFFERS, fallbackBuffer);
      handPassActive = false;
      wrapping = false;
      suppressionDepth = 0;
      taczScopeRenderDepth = 0;
      scopeBodyCaptureDepth = 0;
      sourceDelegationDepth = 0;
      debugScopeBodyPasses = 0;
      pending = false;
      handProjection = null;
      handModelView = null;
      lastDelegate = null;
      lastWrapper = null;
   }

   private static void discard(SequencedMap<RenderType, ByteBufferBuilder> buffers, ByteBufferBuilder fallback) {
      for (ByteBufferBuilder buffer : buffers.values()) {
         buffer.close();
      }

      buffers.clear();
      fallback.close();
   }
}
