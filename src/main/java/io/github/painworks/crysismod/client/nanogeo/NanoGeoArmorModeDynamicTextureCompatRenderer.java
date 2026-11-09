package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitArmorModeOverlayState;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.slf4j.Logger;

public final class NanoGeoArmorModeDynamicTextureCompatRenderer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation SHARED_BASE_TEXTURE_ID = ResourceLocation.fromNamespaceAndPath("crysismod", "dynamic/armor_mode_overlay_compat/base_shared");
   private static final ResourceLocation SHARED_PULSE_CORE_TEXTURE_ID = ResourceLocation.fromNamespaceAndPath("crysismod", "dynamic/armor_mode_overlay_compat/pulse_core_shared");
   private static final ResourceLocation SHARED_COMPOSITE_TEXTURE_ID = ResourceLocation.fromNamespaceAndPath("crysismod", "dynamic/armor_mode_overlay_compat/composite_shared");
   private static final ResourceLocation PULSE_ATLAS_BUILD_CORE_TEXTURE_ID = ResourceLocation.fromNamespaceAndPath(
      "crysismod", "dynamic/armor_mode_overlay_compat/pulse_atlas_build_core"
   );
   private static final String STEADY_BASE_TEXTURE_PATH_PREFIX = "dynamic/armor_mode_overlay_compat/base_steady/";
   private static final String STEADY_BASE_COMPOSITE_TEXTURE_PATH_PREFIX = "dynamic/armor_mode_overlay_compat/base_composite_steady/";
   private static final String PULSE_ATLAS_CORE_TEXTURE_PATH_PREFIX = "dynamic/armor_mode_overlay_compat/pulse_atlas_core/";
   private static final String TRANSIENT_BASE_TEXTURE_PATH_PREFIX = "dynamic/armor_mode_overlay_compat/base/";
   private static final String TRANSIENT_BASE_COMPOSITE_TEXTURE_PATH_PREFIX = "dynamic/armor_mode_overlay_compat/base_composite/";
   private static final String TRANSIENT_PULSE_CORE_TEXTURE_PATH_PREFIX = "dynamic/armor_mode_overlay_compat/pulse_core/";
   private static final String TRANSIENT_COMPOSITE_TEXTURE_PATH_PREFIX = "dynamic/armor_mode_overlay_compat/composite/";
   private static final long STALE_TRANSIENT_MILLIS = 300000L;
   private static final int MIN_TARGET_RESOLUTION = 16;
   private static final int MAX_COMPAT_OVERLAPPING_WAVES = 3;
   private static final int BASE_STEADY_FRAME_COUNT = 96;
   private static final int PULSE_ATLAS_FRAME_COUNT = 64;
   private static final int PULSE_STAMP_FRAME_COUNT = 48;
   private static final int PULSE_STAMP_VARIANT_COUNT = 4;
   private static final int PREWARM_BASE_FRAME_BUDGET_PER_TICK = 1;
   private static final int PREWARM_PULSE_FRAME_BUDGET_PER_TICK = 1;
   private static final int PREWARM_PULSE_STAMP_FRAME_BUDGET_PER_TICK = 1;
   private static final float MAX_COMPAT_PULSE_RADIUS_PX = 96.0F;
   private static final int TRANSITION_FRAME_COUNT = 48;
   private static final int LOOPED_LAYER_A_CYCLES = 5;
   private static final int LOOPED_LAYER_B_CYCLES = 4;
   private static final int LOOPED_LAYER_C_CYCLES = 3;
   private static final float BASE_SOURCE_ALPHA_THRESHOLD = 0.1F;
   private static final float PULSE_SOURCE_ALPHA_THRESHOLD = 0.0039215684F;
   private static final float COMPAT_PULSE_MIN_ALPHA = 0.0039215684F;
   private static final float COMPAT_PULSE_STAMP_SCREEN_FOOTPRINT = 0.22F;
   private static final float COMPAT_PULSE_ALPHA_CORE_WEIGHT = 1.0F;
   private static final float COMPAT_PULSE_ALPHA_GLOW_WEIGHT = 1.0F;
   private static final float COMPAT_PULSE_COLOR_BOOST = 1.0F;
   private static final float COMPAT_PULSE_GLOW_BOOST = 1.0F;
   private static final float COMPAT_PULSE_SHADERPACK_GLOW_ALPHA_FLOOR = 0.32F;
   private static final float COMPAT_PULSE_SHADERPACK_GLOW_WEIGHT_FLOOR = 0.42F;
   private static final float TAU = (float) (Math.PI * 2);
   private static final float COMPAT_ALPHA_PRESERVATION = 1.0F;
   private static final float BASE_GLOW_LUMA_START = 0.42F;
   private static final float BASE_GLOW_LUMA_END = 0.78F;
   private static final float BASE_GLOW_BOOST = 0.3F;
   private static final float BASE_GLOW_WHITE_MIX = 0.18F;
   private static final long PERF_LOG_THRESHOLD_NS = 1000000L;
   private static final NanoGeoArmorModeDynamicTextureCompatRenderer.CloudPatchArchetype[] CLOUD_PATCH_ARCHETYPES = createCloudPatchArchetypes();
   private static final NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState SHARED_BASE_STATE = new NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState(
      SHARED_BASE_TEXTURE_ID
   );
   private static final NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState SHARED_PULSE_STATE = new NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState(
      SHARED_PULSE_CORE_TEXTURE_ID, false
   );
   private static final NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState SHARED_COMPOSITE_STATE = new NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState(
      SHARED_COMPOSITE_TEXTURE_ID
   );
   private static final NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState PULSE_ATLAS_BUILD_STATE = new NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState(
      PULSE_ATLAS_BUILD_CORE_TEXTURE_ID, false
   );
   private static final Map<UUID, NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState> TRANSIENT_BASE_STATES = new ConcurrentHashMap<>();
   private static final Map<UUID, NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState> TRANSIENT_BASE_COMPOSITE_STATES = new ConcurrentHashMap<>();
   private static final Map<UUID, NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState> TRANSIENT_PULSE_STATES = new ConcurrentHashMap<>();
   private static final Map<UUID, NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState> TRANSIENT_COMPOSITE_STATES = new ConcurrentHashMap<>();
   private static final Map<UUID, NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTimingState> PULSE_TIMING_STATES = new ConcurrentHashMap<>();
   private static NativeImage sourceTextureImage;
   private static ResourceLocation sourceTextureId;
   private static NativeImage baseArmorTextureImage;
   private static int[] baseArmorSampledPixels = new int[0];
   private static boolean warnedBaseArmorTextureLoadFailure;
   private static int sourceWidth = 256;
   private static int sourceHeight = 256;
   private static int baseTargetWidth = 256;
   private static int baseTargetHeight = 256;
   private static int baseFlowMaskWidth = 256;
   private static int baseFlowMaskHeight = 256;
   private static int pulseTargetWidth = 256;
   private static int pulseTargetHeight = 256;
   private static int lastConfiguredMaxResolution = -1;
   private static NanoGeoClientConfig.ArmorModeCompatPerformancePreset lastConfiguredPerformancePreset;
   private static int lastConfiguredPulseCenterMarkerAlpha = -1;
   private static boolean warnedSourceLoadFailure;
   private static int[] baseSampledSourcePixels = new int[0];
   private static float[] baseSampledSourceAlpha = new float[0];
   private static int[] basePreparedSourceRgb = new int[0];
   private static int[] baseActivePixelIndices = new int[0];
   private static int baseActivePixelCount;
   private static NanoGeoBakedModel baseUvCoverageModel;
   private static byte[] baseUvCoverageMask = new byte[0];
   private static int baseUvCoverageWidth;
   private static int baseUvCoverageHeight;
   private static int baseUvCoveredPixelCount;
   private static int baseUvCoveredQuadCount;
   private static boolean baseUvCoverageEnabled;
   private static float[] pulseSampledSourceAlpha = new float[0];
   private static byte[] baseTransitionDelayBytes = new byte[0];
   private static byte[] pulseTransitionDelayBytes = new byte[0];
   private static float lastPreparedOverlayBrightness = Float.NaN;
   private static float lastPreparedGlowStrength = Float.NaN;
   private static NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture[] steadyBaseFrames = new NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture[0];
   private static String steadyBaseFramesSignature = "";
   private static int steadyBaseBuildCursor;
   private static NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture[] steadyBaseCompositeFrames = new NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture[0];
   private static String steadyBaseCompositeFramesSignature = "";
   private static int steadyBaseCompositeBuildCursor;
   private static NanoGeoArmorModeDynamicTextureCompatRenderer.PulseFrameTexture[] pulseAtlasFrames = new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseFrameTexture[0];
   private static String pulseAtlasSignature = "";
   private static int pulseAtlasBuildCursor;
   private static String frameCacheSignature = "";
   private static String lastDebugSignature = "";
   private static NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame[][] pulseStampFrames = new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame[0][];
   private static String pulseStampSignature = "";
   private static int pulseStampBuildCursor;
   private static int pulseStampFrameSize;
   private static float pulseStampFrameCenter;

   private NanoGeoArmorModeDynamicTextureCompatRenderer() {
   }

   public static ResourceLocation prepareAndGetBaseFlowTexture(UUID playerId, NanosuitArmorModeOverlayState.OverlayRenderState overlayState) {
      if (overlayState == null || !overlayState.enabled()) {
         return null;
      } else if (NanoGeoClientConfig.armorModePulseDebugOutputOnlyPulse()) {
         return null;
      } else {
         NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch = patchSnapshot();
         float timeSeconds = overlayState.shimmerTime() / 20.0F;
         int steadyFrameIndex = resolveSteadyBaseFrameIndex(timeSeconds, patch);
         ensureSourceTexture(overlayState.texture());
         ensureTargetResolutionUpToDate();
         ensureFrameCachesCurrent();
         ensurePreparedBaseRgbCache(patch);
         ensureSteadyBaseFramesCurrent(patch, steadyFrameIndex);
         cleanupStaleTransientStates();
         boolean transitionActive = isTransitionActive(overlayState);
         if (!transitionActive) {
            ResourceLocation steadyTextureId = steadyBaseFrameTextureId(steadyFrameIndex);
            maybeDebugLog(overlayState, false, steadyTextureId);
            return steadyTextureId;
         } else {
            NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state = playerId != null
               ? TRANSIENT_BASE_STATES.computeIfAbsent(playerId, NanoGeoArmorModeDynamicTextureCompatRenderer::createTransientBaseState)
               : SHARED_BASE_STATE;
            state.ensureCapacity(baseTargetWidth, baseTargetHeight);
            state.lastUseMillis = Util.getMillis();
            updateTransitionBaseIfNeeded(state, overlayState, steadyFrameIndex);
            maybeDebugLog(overlayState, true, state.textureId);
            return state.textureId;
         }
      }
   }

   public static NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet prepareAndGetPulseTextures(
      UUID playerId, NanosuitArmorModeOverlayState.OverlayRenderState overlayState
   ) {
      if (overlayState == null || !overlayState.enabled()) {
         return NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet.empty();
      } else if (!NanoGeoClientConfig.armorModePulseEnabled()
         && !NanoGeoClientConfig.armorModePulseDebugForceVisible()
         && !NanoGeoClientConfig.armorModePulseDebugOutputOnlyPulse()) {
         return NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet.empty();
      } else {
         long perfStart = perfStart();
         StringBuilder perf = perfBuilder("pulseTextures");
         ensureSourceTexture(overlayState.texture());
         long perfSection = perfAppend(perf, "source", perfStart);
         ensureTargetResolutionUpToDate();
         perfSection = perfAppend(perf, "target", perfSection);
         ensureFrameCachesCurrent();
         perfSection = perfAppend(perf, "frameCaches", perfSection);
         cleanupStaleTransientStates();
         perfSection = perfAppend(perf, "cleanup", perfSection);
         float timeSeconds = resolvePulseTimeSeconds(playerId, overlayState, overlayState.shimmerTime() / 20.0F);
         boolean transitionActive = isTransitionActive(overlayState);
         boolean transitionSensitive = transitionActive && NanoGeoClientConfig.armorModeTransitionAffectsPulses();
         NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state = playerId != null
            ? TRANSIENT_PULSE_STATES.computeIfAbsent(playerId, NanoGeoArmorModeDynamicTextureCompatRenderer::createTransientPulseState)
            : SHARED_PULSE_STATE;
         state.ensureCapacity(pulseTargetWidth, pulseTargetHeight);
         perfSection = perfAppend(perf, "ensureCapacity", perfSection);
         state.lastUseMillis = Util.getMillis();
         updatePulseIfNeeded(state, overlayState, transitionSensitive, timeSeconds);
         perfSection = perfAppend(perf, "updatePulse", perfSection);
         perfLog(perf, perfStart, "dynamic=true transitionSensitive=" + transitionSensitive + " visible=" + hasVisiblePulseContent(state));
         return hasVisiblePulseContent(state)
            ? new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet(state.textureId, null)
            : NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet.empty();
      }
   }

   public static ResourceLocation prepareAndGetCompositeTexture(UUID playerId, NanosuitArmorModeOverlayState.OverlayRenderState overlayState) {
      if (overlayState != null && overlayState.enabled()) {
         NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch = patchSnapshot();
         float timeSeconds = overlayState.shimmerTime() / 20.0F;
         float pulseTimeSeconds = resolvePulseTimeSeconds(playerId, overlayState, timeSeconds);
         int steadyFrameIndex = resolveSteadyBaseFrameIndex(timeSeconds, patch);
         ensureSourceTexture(overlayState.texture());
         ensureTargetResolutionUpToDate();
         ensureFrameCachesCurrent();
         ensurePreparedBaseRgbCache(patch);
         ensureSteadyBaseFramesCurrent(patch, steadyFrameIndex);
         cleanupStaleTransientStates();
         boolean outputOnlyPulse = NanoGeoClientConfig.armorModePulseDebugOutputOnlyPulse();
         boolean transitionActive = isTransitionActive(overlayState);
         boolean pulsesVisible = compatPulsesVisible(pulseTimeSeconds) || outputOnlyPulse;
         boolean transitionSensitive = transitionActive && NanoGeoClientConfig.armorModeTransitionAffectsPulses();
         if (!outputOnlyPulse && !transitionActive && !pulsesVisible) {
            return steadyBaseFrameTextureId(steadyFrameIndex);
         } else {
            int pulseFrameIndex = -1;
            NanoGeoArmorModeDynamicTextureCompatRenderer.PulseFrameTexture pulseFrame = null;
            NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState pulseState = null;
            if (pulsesVisible) {
               if (transitionSensitive) {
                  pulseState = playerId != null
                     ? TRANSIENT_PULSE_STATES.computeIfAbsent(playerId, NanoGeoArmorModeDynamicTextureCompatRenderer::createTransientPulseState)
                     : SHARED_PULSE_STATE;
                  pulseState.ensureCapacity(pulseTargetWidth, pulseTargetHeight);
                  pulseState.lastUseMillis = Util.getMillis();
                  updatePulseIfNeeded(pulseState, overlayState, true, pulseTimeSeconds);
                  if (!hasVisiblePulseContent(pulseState)) {
                     pulseState = null;
                  }
               } else {
                  pulseState = playerId != null
                     ? TRANSIENT_PULSE_STATES.computeIfAbsent(playerId, NanoGeoArmorModeDynamicTextureCompatRenderer::createTransientPulseState)
                     : SHARED_PULSE_STATE;
                  pulseState.ensureCapacity(pulseTargetWidth, pulseTargetHeight);
                  pulseState.lastUseMillis = Util.getMillis();
                  updatePulseIfNeeded(pulseState, overlayState, false, pulseTimeSeconds);
                  if (!hasVisiblePulseContent(pulseState)) {
                     pulseState = null;
                  }
               }
            }

            if (outputOnlyPulse && pulseFrame == null && pulseState == null) {
               return null;
            } else {
               NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state = playerId != null
                  ? TRANSIENT_COMPOSITE_STATES.computeIfAbsent(playerId, NanoGeoArmorModeDynamicTextureCompatRenderer::createTransientCompositeState)
                  : SHARED_COMPOSITE_STATE;
               state.ensureCapacity(baseTargetWidth, baseTargetHeight);
               state.lastUseMillis = Util.getMillis();
               updateCompositeIfNeeded(state, overlayState, steadyFrameIndex, pulseFrameIndex, transitionActive, outputOnlyPulse, pulseFrame, pulseState);
               maybeDebugLog(overlayState, transitionActive, state.textureId);
               return state.textureId;
            }
         }
      } else {
         return null;
      }
   }

   public static ResourceLocation prepareAndGetPulseTexture(UUID playerId, NanosuitArmorModeOverlayState.OverlayRenderState overlayState) {
      return prepareAndGetPulseTextures(playerId, overlayState).coreTexture();
   }

   public static ResourceLocation prepareAndGetTexture(UUID playerId, NanosuitArmorModeOverlayState.OverlayRenderState overlayState) {
      return prepareAndGetBaseFlowTexture(playerId, overlayState);
   }

   public static ResourceLocation prepareAndGetPreviewBaseFlowTexture(float shimmerTimeTicks) {
      NanosuitArmorModeOverlayState.OverlayRenderState previewState = new NanosuitArmorModeOverlayState.OverlayRenderState(
         true, true, NanosuitArmorModeOverlayState.overlayTexture(), 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, -1, shimmerTimeTicks, 1.0F
      );
      return prepareAndGetBaseFlowTexture(null, previewState);
   }

   public static ResourceLocation prepareAndGetPreviewBaseCompositeTexture(float shimmerTimeTicks) {
      NanosuitArmorModeOverlayState.OverlayRenderState previewState = new NanosuitArmorModeOverlayState.OverlayRenderState(
         true,
         true,
         NanosuitArmorModeOverlayState.overlayTexture(),
         NanoGeoClientConfig.armorModeOverlayTintR(),
         NanoGeoClientConfig.armorModeOverlayTintG(),
         NanoGeoClientConfig.armorModeOverlayTintB(),
         NanoGeoClientConfig.armorModeOverlayAlpha() / 255.0F,
         0.0F,
         0.0F,
         -1,
         shimmerTimeTicks,
         1.0F
      );
      return prepareAndGetBaseCompositeTexture(null, previewState);
   }

   public static ResourceLocation prepareAndGetBaseCompositeTexture(NanosuitArmorModeOverlayState.OverlayRenderState overlayState) {
      return prepareAndGetBaseCompositeTexture(null, overlayState);
   }

   public static ResourceLocation prepareAndGetBaseCompositeTexture(UUID playerId, NanosuitArmorModeOverlayState.OverlayRenderState overlayState) {
      if (overlayState != null && overlayState.enabled()) {
         long perfStart = perfStart();
         StringBuilder perf = perfBuilder("baseComposite");
         NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch = patchSnapshot();
         float timeSeconds = overlayState.shimmerTime() / 20.0F;
         int steadyFrameIndex = resolveSteadyBaseFrameIndex(timeSeconds, patch);
         long perfSection = perfAppend(perf, "snapshot", perfStart);
         ensureSourceTexture(overlayState.texture());
         perfSection = perfAppend(perf, "source", perfSection);
         ensureTargetResolutionUpToDate();
         perfSection = perfAppend(perf, "target", perfSection);
         ensureFrameCachesCurrent();
         perfSection = perfAppend(perf, "frameCaches", perfSection);
         ensurePreparedBaseRgbCache(patch);
         perfSection = perfAppend(perf, "preparedRgb", perfSection);
         if (!ensureBaseArmorTexture()) {
            perfLog(perf, perfStart, "fallbackBaseArmor=true");
            return NanoGeoShaderpackStableTextures.resolveBaseArmorTexture();
         } else {
            perfSection = perfAppend(perf, "baseArmor", perfSection);
            cleanupStaleTransientStates();
            perfSection = perfAppend(perf, "cleanup", perfSection);
            boolean transitionActive = isTransitionActive(overlayState);
            if (transitionActive) {
               NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state = playerId != null
                  ? TRANSIENT_BASE_COMPOSITE_STATES.computeIfAbsent(playerId, NanoGeoArmorModeDynamicTextureCompatRenderer::createTransientBaseCompositeState)
                  : SHARED_COMPOSITE_STATE;
               state.ensureCapacity(baseTargetWidth, baseTargetHeight);
               perfSection = perfAppend(perf, "ensureCapacity", perfSection);
               state.lastUseMillis = Util.getMillis();
               updateBaseCompositeTransitionIfNeeded(state, overlayState, steadyFrameIndex, patch);
               perfSection = perfAppend(perf, "transitionUpdate", perfSection);
               maybeDebugLog(overlayState, true, state.textureId);
               perfLog(perf, perfStart, "transition=true frame=" + steadyFrameIndex + " activePixels=" + baseActivePixelCount);
               return state.textureId;
            } else {
               ensureSteadyBaseCompositeFramesCurrent(patch, overlayState, steadyFrameIndex);
               perfSection = perfAppend(perf, "steadyFrames", perfSection);
               NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture frame = steadyBaseCompositeFrame(steadyFrameIndex);
               perfLog(perf, perfStart, "transition=false frame=" + steadyFrameIndex + " frameReady=" + (frame != null));
               return frame != null ? frame.textureId : NanoGeoShaderpackStableTextures.resolveBaseArmorTexture();
            }
         }
      } else {
         return null;
      }
   }

   public static void prewarmSteadyBaseFrames(UUID playerId) {
      NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch = patchSnapshot();
      ensureSourceTexture(NanosuitArmorModeOverlayState.overlayTexture());
      ensureTargetResolutionUpToDate();
      ensureFrameCachesCurrent();
      ensurePreparedBaseRgbCache(patch);
      prewarmTransientStates(playerId);
      if (NanoGeoArmorModeBaseCompositeCompat.replacementEligible() && ensureBaseArmorTexture()) {
         NanosuitArmorModeOverlayState.OverlayRenderState prewarmState = new NanosuitArmorModeOverlayState.OverlayRenderState(
            true,
            true,
            NanosuitArmorModeOverlayState.overlayTexture(),
            NanoGeoClientConfig.armorModeOverlayTintR(),
            NanoGeoClientConfig.armorModeOverlayTintG(),
            NanoGeoClientConfig.armorModeOverlayTintB(),
            NanoGeoClientConfig.armorModeOverlayAlpha() / 255.0F,
            0.0F,
            0.0F,
            -1,
            0.0F,
            1.0F
         );
         ensureSteadyBaseCompositeFramesCurrent(patch, prewarmState, clampInt(steadyBaseCompositeBuildCursor, 0, 95));
         buildAdditionalSteadyBaseCompositeFrames(patch, prewarmState, 1);
      } else {
         ensureSteadyBaseFramesCurrent(patch, clampInt(steadyBaseBuildCursor, 0, 95));
         buildAdditionalSteadyBaseFrames(patch, 1);
      }

      prewarmPulseStampFrames();
   }

   public static void prewarmPulseAtlasFrames() {
      prewarmPulseStampFrames();
   }

   private static void prewarmPulseStampFrames() {
      if (NanoGeoClientConfig.armorModePulseEnabled() || NanoGeoClientConfig.armorModePulseDebugForceVisible()) {
         ensureSourceTexture(NanosuitArmorModeOverlayState.overlayTexture());
         ensureTargetResolutionUpToDate();
         ensureFrameCachesCurrent();
         PULSE_ATLAS_BUILD_STATE.ensureCapacity(pulseTargetWidth, pulseTargetHeight);
         NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse = pulseRuntimeConfig(PULSE_ATLAS_BUILD_STATE);
         if (pulse.enabled && pulse.centerCount > 0) {
            ensurePulseStampFramesCurrent(pulse);
            buildAdditionalPulseStampFrames(pulse, 1);
         }
      }
   }

   public static void close() {
      SHARED_BASE_STATE.close();
      SHARED_PULSE_STATE.close();
      SHARED_COMPOSITE_STATE.close();
      PULSE_ATLAS_BUILD_STATE.close();
      closeStateMap(TRANSIENT_BASE_STATES);
      closeStateMap(TRANSIENT_BASE_COMPOSITE_STATES);
      closeStateMap(TRANSIENT_PULSE_STATES);
      closeStateMap(TRANSIENT_COMPOSITE_STATES);
      closePulseAtlasFrames();
      closeSourceTexture();
      baseTransitionDelayBytes = new byte[0];
      pulseTransitionDelayBytes = new byte[0];
      frameCacheSignature = "";
      lastDebugSignature = "";
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState createTransientBaseState(UUID playerId) {
      String safeId = playerId.toString().replace("-", "");
      return new NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState(
         ResourceLocation.fromNamespaceAndPath("crysismod", "dynamic/armor_mode_overlay_compat/base/" + safeId)
      );
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState createTransientBaseCompositeState(UUID playerId) {
      String safeId = playerId.toString().replace("-", "");
      return new NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState(
         ResourceLocation.fromNamespaceAndPath("crysismod", "dynamic/armor_mode_overlay_compat/base_composite/" + safeId)
      );
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState createTransientPulseState(UUID playerId) {
      String safeId = playerId.toString().replace("-", "");
      return new NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState(
         ResourceLocation.fromNamespaceAndPath("crysismod", "dynamic/armor_mode_overlay_compat/pulse_core/" + safeId), false
      );
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState createTransientCompositeState(UUID playerId) {
      String safeId = playerId.toString().replace("-", "");
      return new NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState(
         ResourceLocation.fromNamespaceAndPath("crysismod", "dynamic/armor_mode_overlay_compat/composite/" + safeId)
      );
   }

   private static void cleanupStaleTransientStates() {
      long now = Util.getMillis();
      cleanupStateMap(TRANSIENT_BASE_STATES, now);
      cleanupStateMap(TRANSIENT_BASE_COMPOSITE_STATES, now);
      cleanupStateMap(TRANSIENT_PULSE_STATES, now);
      cleanupStateMap(TRANSIENT_COMPOSITE_STATES, now);
      cleanupPulseTimingStates(now);
   }

   private static float resolvePulseTimeSeconds(UUID playerId, NanosuitArmorModeOverlayState.OverlayRenderState overlayState, float sourceTimeSeconds) {
      if (playerId != null && overlayState != null) {
         NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTimingState timing = PULSE_TIMING_STATES.computeIfAbsent(
            playerId, ignored -> new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTimingState()
         );
         timing.lastUseMillis = Util.getMillis();
         if (overlayState.armorModeActive()) {
            if (!timing.active || Float.isNaN(timing.activationSourceTimeSeconds)) {
               timing.activationSourceTimeSeconds = sourceTimeSeconds;
            }

            timing.active = true;
         } else if (timing.active) {
            timing.active = false;
         }

         return Float.isNaN(timing.activationSourceTimeSeconds) ? 0.0F : max(0.0F, sourceTimeSeconds - timing.activationSourceTimeSeconds);
      } else {
         return sourceTimeSeconds;
      }
   }

   private static void cleanupPulseTimingStates(long now) {
      PULSE_TIMING_STATES.entrySet().removeIf(entry -> now - entry.getValue().lastUseMillis > 300000L);
   }

   private static void cleanupStateMap(Map<UUID, NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState> stateMap, long now) {
      stateMap.entrySet().removeIf(entry -> {
         NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state = entry.getValue();
         if (now - state.lastUseMillis <= 300000L) {
            return false;
         } else {
            state.close();
            return true;
         }
      });
   }

   private static void closeStateMap(Map<UUID, NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState> stateMap) {
      for (NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state : stateMap.values()) {
         state.close();
      }

      stateMap.clear();
   }

   private static void prewarmTransientStates(UUID playerId) {
      if (playerId != null) {
         NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState baseState = TRANSIENT_BASE_STATES.computeIfAbsent(
            playerId, NanoGeoArmorModeDynamicTextureCompatRenderer::createTransientBaseState
         );
         prewarmTransientState(baseState, baseTargetWidth, baseTargetHeight);
         if (NanoGeoClientConfig.armorModePulseEnabled() || NanoGeoClientConfig.armorModePulseDebugForceVisible()) {
            NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState pulseState = TRANSIENT_PULSE_STATES.computeIfAbsent(
               playerId, NanoGeoArmorModeDynamicTextureCompatRenderer::createTransientPulseState
            );
            prewarmTransientState(pulseState, pulseTargetWidth, pulseTargetHeight);
         }

         if (NanoGeoClientConfig.armorModeCompatPerformancePreset() == NanoGeoClientConfig.ArmorModeCompatPerformancePreset.COMPOSITE_FAST) {
            NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState compositeState = TRANSIENT_COMPOSITE_STATES.computeIfAbsent(
               playerId, NanoGeoArmorModeDynamicTextureCompatRenderer::createTransientCompositeState
            );
            prewarmTransientState(compositeState, baseTargetWidth, baseTargetHeight);
         }

         if (NanoGeoClientConfig.armorModeCompatPerformancePreset() == NanoGeoClientConfig.ArmorModeCompatPerformancePreset.BASE_COMPOSITE_FAST) {
            NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState baseCompositeState = TRANSIENT_BASE_COMPOSITE_STATES.computeIfAbsent(
               playerId, NanoGeoArmorModeDynamicTextureCompatRenderer::createTransientBaseCompositeState
            );
            prewarmTransientState(baseCompositeState, baseTargetWidth, baseTargetHeight);
         }
      }
   }

   private static void prewarmTransientState(NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state, int targetWidth, int targetHeight) {
      if (state != null) {
         state.ensureCapacity(targetWidth, targetHeight);
         state.lastUseMillis = Util.getMillis();
         state.ensureUploaded();
      }
   }

   private static void ensureSourceTexture(ResourceLocation textureId) {
      if (textureId != null) {
         if (!textureId.equals(sourceTextureId) || sourceTextureImage == null) {
            closeSourceTexture();
            sourceTextureId = textureId;
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null) {
               try {
                  Resource resource = (Resource)minecraft.getResourceManager().getResource(textureId).orElse(null);
                  if (resource == null) {
                     return;
                  }

                  try (InputStream inputStream = resource.open()) {
                     sourceTextureImage = NativeImage.read(inputStream);
                     NanoGeoShaderpackStableTextures.sanitizeTransparentPixels(sourceTextureImage);
                     sourceWidth = Math.max(16, sourceTextureImage.getWidth());
                     sourceHeight = Math.max(16, sourceTextureImage.getHeight());
                     recalculateTargetDimensions();
                     rebuildSourceSamplingCache();
                     warnedSourceLoadFailure = false;
                     resetDynamicStatesAndCaches();
                  }
               } catch (RuntimeException | IOException var8) {
                  sourceTextureImage = null;
                  sourceWidth = 256;
                  sourceHeight = 256;
                  recalculateTargetDimensions();
                  rebuildSourceSamplingCache();
                  resetDynamicStatesAndCaches();
                  if (!warnedSourceLoadFailure) {
                     warnedSourceLoadFailure = true;
                     LOGGER.warn(
                        "[CrysisMod][ArmorModeOverlay] failed to load source overlay texture {}, DynamicTexture compat backend will use fallback mask",
                        textureId,
                        var8
                     );
                  }
               }
            }
         }
      }
   }

   private static boolean ensureBaseArmorTexture() {
      int pixelCount = baseTargetWidth * baseTargetHeight;
      if (baseArmorTextureImage != null && baseArmorSampledPixels.length == pixelCount) {
         return true;
      } else {
         if (baseArmorTextureImage == null) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null) {
               return false;
            }

            try {
               Resource resource = (Resource)minecraft.getResourceManager().getResource(NanoGeoArmorTextures.BASE_ARMOR).orElse(null);
               if (resource == null) {
                  return false;
               }

               try (InputStream inputStream = resource.open()) {
                  baseArmorTextureImage = NativeImage.read(inputStream);
                  NanoGeoShaderpackStableTextures.sanitizeTransparentPixels(baseArmorTextureImage);
               }

               warnedBaseArmorTextureLoadFailure = false;
            } catch (RuntimeException | IOException var9) {
               closeBaseArmorTexture();
               if (!warnedBaseArmorTextureLoadFailure) {
                  warnedBaseArmorTextureLoadFailure = true;
                  LOGGER.warn(
                     "[CrysisMod][ArmorModeOverlay] failed to load base armor texture {}, base-composite preset will use the normal base texture",
                     NanoGeoArmorTextures.BASE_ARMOR,
                     var9
                  );
               }

               return false;
            }
         }

         int sourceBaseWidth = baseArmorTextureImage.getWidth();
         int sourceBaseHeight = baseArmorTextureImage.getHeight();
         baseArmorSampledPixels = new int[pixelCount];

         for (int y = 0; y < baseTargetHeight; y++) {
            int sourceY = sampleSourceCoordinateForTarget(y, baseTargetHeight, sourceBaseHeight);

            for (int x = 0; x < baseTargetWidth; x++) {
               int sourceX = sampleSourceCoordinateForTarget(x, baseTargetWidth, sourceBaseWidth);
               baseArmorSampledPixels[y * baseTargetWidth + x] = NanoGeoShaderpackStableTextures.sanitizeTransparentPixel(
                  baseArmorTextureImage.getPixelRGBA(sourceX, sourceY)
               );
            }
         }

         closeSteadyBaseCompositeFrames();
         return true;
      }
   }

   private static void ensureTargetResolutionUpToDate() {
      int configuredMaxResolution = Math.max(64, NanoGeoClientConfig.armorModeDynamicTextureCompatMaxResolution());
      NanoGeoClientConfig.ArmorModeCompatPerformancePreset configuredPerformancePreset = NanoGeoClientConfig.armorModeCompatPerformancePreset();
      int configuredPulseCenterMarkerAlpha = NanoGeoClientConfig.armorModePulseCenterMarkerAlpha();
      boolean targetConfigurationChanged = configuredMaxResolution != lastConfiguredMaxResolution
         || configuredPerformancePreset != lastConfiguredPerformancePreset
         || configuredPulseCenterMarkerAlpha != lastConfiguredPulseCenterMarkerAlpha;
      if (targetConfigurationChanged) {
         lastConfiguredMaxResolution = configuredMaxResolution;
         lastConfiguredPerformancePreset = configuredPerformancePreset;
         lastConfiguredPulseCenterMarkerAlpha = configuredPulseCenterMarkerAlpha;
         recalculateTargetDimensions();
      }

      boolean uvCoverageChanged = refreshBaseUvCoverageMask();
      if (targetConfigurationChanged || uvCoverageChanged || baseSampledSourcePixels.length <= 0 || pulseSampledSourceAlpha.length <= 0) {
         rebuildSourceSamplingCache();
         resetDynamicStatesAndCaches();
      }
   }

   private static void recalculateTargetDimensions() {
      baseTargetWidth = Math.max(16, sourceWidth);
      baseTargetHeight = Math.max(16, sourceHeight);
      int maxResolution = Math.max(64, NanoGeoClientConfig.armorModeDynamicTextureCompatMaxResolution());
      int baseMaxDimension = Math.max(baseTargetWidth, baseTargetHeight);
      if (baseMaxDimension <= maxResolution) {
         baseFlowMaskWidth = baseTargetWidth;
         baseFlowMaskHeight = baseTargetHeight;
      } else {
         float baseMaskScale = (float)maxResolution / baseMaxDimension;
         baseFlowMaskWidth = Math.max(16, Math.round(baseTargetWidth * baseMaskScale));
         baseFlowMaskHeight = Math.max(16, Math.round(baseTargetHeight * baseMaskScale));
      }

      int pulseMaxResolution = resolvePulseMaxResolution(maxResolution);
      int maxDimension = Math.max(sourceWidth, sourceHeight);
      if (maxDimension <= pulseMaxResolution) {
         pulseTargetWidth = Math.max(16, sourceWidth);
         pulseTargetHeight = Math.max(16, sourceHeight);
      } else {
         float scale = (float)pulseMaxResolution / maxDimension;
         pulseTargetWidth = Math.max(16, Math.round(sourceWidth * scale));
         pulseTargetHeight = Math.max(16, Math.round(sourceHeight * scale));
      }
   }

   private static int resolvePulseMaxResolution(int maxResolution) {
      return Math.max(maxResolution, 512);
   }

   private static boolean refreshBaseUvCoverageMask() {
      boolean enabled = NanoGeoClientConfig.armorModeDynamicTextureCompatUvCoverageMaskEnabled();
      if (enabled) {
         baseUvCoverageEnabled = true;
         NanoGeoBakedModel model = NanoGeoArmorModeUvCoverageMask.currentModel();
         if (model == null) {
            return false;
         } else if (model == baseUvCoverageModel
            && baseUvCoverageWidth == baseTargetWidth
            && baseUvCoverageHeight == baseTargetHeight
            && baseUvCoverageMask.length == baseTargetWidth * baseTargetHeight) {
            return false;
         } else {
            NanoGeoArmorModeUvCoverageMask.Coverage coverage = NanoGeoArmorModeUvCoverageMask.build(model, baseTargetWidth, baseTargetHeight);
            baseUvCoverageModel = model;
            baseUvCoverageMask = coverage.mask();
            baseUvCoverageWidth = baseTargetWidth;
            baseUvCoverageHeight = baseTargetHeight;
            baseUvCoveredPixelCount = coverage.coveredPixelCount();
            baseUvCoveredQuadCount = coverage.quadCount();
            return true;
         }
      } else {
         boolean changed = baseUvCoverageEnabled || baseUvCoverageMask.length > 0;
         baseUvCoverageEnabled = false;
         baseUvCoverageModel = null;
         baseUvCoverageMask = new byte[0];
         baseUvCoverageWidth = 0;
         baseUvCoverageHeight = 0;
         baseUvCoveredPixelCount = 0;
         baseUvCoveredQuadCount = 0;
         return changed;
      }
   }

   private static void rebuildSourceSamplingCache() {
      int basePixelCount = baseTargetWidth * baseTargetHeight;
      boolean useUvCoverageMask = baseUvCoverageMask.length == basePixelCount;
      baseSampledSourcePixels = new int[basePixelCount];
      baseSampledSourceAlpha = new float[basePixelCount];
      basePreparedSourceRgb = new int[basePixelCount];
      baseActivePixelIndices = new int[basePixelCount];
      baseActivePixelCount = 0;

      for (int y = 0; y < baseTargetHeight; y++) {
         for (int x = 0; x < baseTargetWidth; x++) {
            int index = y * baseTargetWidth + x;
            if (!useUvCoverageMask || baseUvCoverageMask[index] != 0) {
               int pixel = NanoGeoShaderpackStableTextures.sanitizeTransparentPixel(sampleSourcePixelForTarget(x, y, baseTargetWidth, baseTargetHeight));
               pixel = applyPulseCenterMarkerAlpha(pixel);
               baseSampledSourcePixels[index] = pixel;
               float alpha = (pixel >>> 24 & 0xFF) / 255.0F;
               baseSampledSourceAlpha[index] = alpha;
               if (alpha >= 0.1F) {
                  baseActivePixelIndices[baseActivePixelCount++] = index;
               }
            }
         }
      }

      int pulsePixelCount = pulseTargetWidth * pulseTargetHeight;
      pulseSampledSourceAlpha = new float[pulsePixelCount];

      for (int y = 0; y < pulseTargetHeight; y++) {
         for (int xx = 0; xx < pulseTargetWidth; xx++) {
            int index = y * pulseTargetWidth + xx;
            int pixel = NanoGeoShaderpackStableTextures.sanitizeTransparentPixel(sampleSourcePixelForTarget(xx, y, pulseTargetWidth, pulseTargetHeight));
            float alpha = (pixel >>> 24 & 0xFF) / 255.0F;
            pulseSampledSourceAlpha[index] = alpha;
         }
      }

      lastPreparedOverlayBrightness = Float.NaN;
      lastPreparedGlowStrength = Float.NaN;
      closeSteadyBaseFrames();
      closeSteadyBaseCompositeFrames();
      closePulseStampFrames();
      closePulseAtlasFrames();
      steadyBaseFramesSignature = "";
   }

   private static void ensureFrameCachesCurrent() {
      NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch = patchSnapshot();
      NanoGeoArmorModeDynamicTextureCompatRenderer.TransitionSnapshot transition = transitionSnapshot();
      String signature = buildFrameCacheSignature(patch, transition);
      if (!signature.equals(frameCacheSignature)
         || baseTransitionDelayBytes.length != baseTargetWidth * baseTargetHeight
         || pulseTransitionDelayBytes.length != pulseTargetWidth * pulseTargetHeight) {
         buildTransitionDelayMaps(transition);
         frameCacheSignature = signature;
      }
   }

   private static String buildFrameCacheSignature(
      NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch, NanoGeoArmorModeDynamicTextureCompatRenderer.TransitionSnapshot transition
   ) {
      return sourceTextureId
         + "|baseTarget="
         + baseTargetWidth
         + "x"
         + baseTargetHeight
         + "|pulseTarget="
         + pulseTargetWidth
         + "x"
         + pulseTargetHeight
         + "|compatPerformancePreset="
         + NanoGeoClientConfig.armorModeCompatPerformancePreset()
         + "|overlayBrightness="
         + patch.overlayBrightness
         + "|patchTextureAlpha="
         + patch.textureAlpha
         + "|patchSpotAlpha="
         + patch.spotAlpha
         + "|patchMin="
         + patch.minSizePx
         + "|patchMax="
         + patch.maxSizePx
         + "|patchFreq="
         + patch.frequency
         + "|patchDensity="
         + patch.density
         + "|patchVariant="
         + patch.variant
         + "|transitionEnabled="
         + transition.enabled
         + "|transitionBlock="
         + transition.blockSizePx
         + "|transitionStagger="
         + transition.staggerAmount
         + "|transitionFade="
         + transition.fadeWindow
         + "|transitionSeed="
         + transition.seed;
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patchSnapshot() {
      return new NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot(
         clamp(NanoGeoClientConfig.armorModeOverlayBrightness(), 0.0F, 4.0F),
         clamp(NanoGeoClientConfig.armorModeDynamicTextureCompatGlowStrength(), 0.0F, 1.5F),
         NanoGeoClientConfig.armorModePatchTextureAlpha() / 255.0F,
         NanoGeoClientConfig.armorModePatchSpotAlpha() / 255.0F,
         max(Math.min(NanoGeoClientConfig.armorModePatchMinSizePx(), NanoGeoClientConfig.armorModePatchMaxSizePx()), 1.0F),
         max(Math.max(NanoGeoClientConfig.armorModePatchMinSizePx(), NanoGeoClientConfig.armorModePatchMaxSizePx()), 1.0F),
         max(NanoGeoClientConfig.armorModePatchFrequency(), 0.1F),
         clamp01(NanoGeoClientConfig.armorModePatchDensity()),
         NanoGeoClientConfig.armorModePatchFlowVariant(),
         patchLoopDurationSeconds()
      );
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.TransitionSnapshot transitionSnapshot() {
      float fadeWindow = clamp(NanoGeoClientConfig.armorModeTransitionFadeWindow(), 1.0E-4F, 1.0F);
      float staggerAmount = clamp01(NanoGeoClientConfig.armorModeTransitionStaggerAmount());
      float maxDelay = Math.min(staggerAmount, max(1.0F - fadeWindow, 0.0F));
      return new NanoGeoArmorModeDynamicTextureCompatRenderer.TransitionSnapshot(
         NanoGeoClientConfig.armorModeTransitionEnabled(),
         clamp(Math.round(NanoGeoClientConfig.armorModeTransitionPixelBlockSizePx()), 1.0F, 2.0F),
         fadeWindow,
         staggerAmount,
         maxDelay,
         NanoGeoClientConfig.armorModeTransitionSeed(),
         NanoGeoClientConfig.armorModeTransitionAffectsPulses()
      );
   }

   private static void buildTransitionDelayMaps(NanoGeoArmorModeDynamicTextureCompatRenderer.TransitionSnapshot transition) {
      baseTransitionDelayBytes = buildTransitionDelayMap(baseTargetWidth, baseTargetHeight, transition, baseSampledSourceAlpha);
      pulseTransitionDelayBytes = buildTransitionDelayMap(pulseTargetWidth, pulseTargetHeight, transition, pulseSampledSourceAlpha);
   }

   private static byte[] buildTransitionDelayMap(
      int width, int height, NanoGeoArmorModeDynamicTextureCompatRenderer.TransitionSnapshot transition, float[] sourceAlpha
   ) {
      byte[] delayBytes = new byte[width * height];
      if (!transition.enabled) {
         return delayBytes;
      } else {
         for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
               int index = y * width + x;
               if (index < sourceAlpha.length && !(sourceAlpha[index] < 0.1F)) {
                  int blockX = (int)Math.floor((x + 0.5F) / transition.blockSizePx);
                  int blockY = (int)Math.floor((y + 0.5F) / transition.blockSizePx);
                  float delay = hash21(blockX + transition.seed, blockY + transition.seed * 0.6180339F) * transition.maxDelay;
                  delayBytes[index] = (byte)clamp255(Math.round(delay * 255.0F));
               }
            }
         }

         return delayBytes;
      }
   }

   private static void updateTransitionBaseIfNeeded(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state,
      NanosuitArmorModeOverlayState.OverlayRenderState overlayState,
      int steadyFrameIndex
   ) {
      long now = Util.getMillis();
      float transitionProgress = clamp01(overlayState.transitionProgress());
      boolean targetChanged = state.lastArmorModeActive != overlayState.armorModeActive();
      boolean enteringTransition = !state.lastTransitionActive || targetChanged || Float.isNaN(state.lastTransitionProgress);
      if (shouldUpdateTransitionBase(state, now, steadyFrameIndex, transitionProgress, overlayState.armorModeActive())) {
         if (enteringTransition || transitionProgress <= 1.0E-4F || transitionProgress < state.lastTransitionProgress) {
            clearTexture(state.pixels, state.width, state.height);
            state.previousBasePixelCount = 0;
         }

         fillTransitionBaseTexture(state, transitionProgress, steadyFrameIndex);
         state.dynamicTexture.upload();
         state.lastUpdateMillis = now;
         state.lastTimeSeconds = transitionProgress;
         state.lastBaseFrameIndex = steadyFrameIndex;
         state.lastTransitionActive = true;
         state.lastArmorModeActive = overlayState.armorModeActive();
         state.lastTransitionProgress = transitionProgress;
      }
   }

   private static void updatePulseIfNeeded(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state,
      NanosuitArmorModeOverlayState.OverlayRenderState overlayState,
      boolean transitionSensitive,
      float timeSeconds
   ) {
      long now = Util.getMillis();
      boolean pulsesVisible = compatPulsesVisible(timeSeconds) || NanoGeoClientConfig.armorModePulseDebugOutputOnlyPulse();
      float transitionProgress = clamp01(overlayState.transitionProgress());
      float pulseSpawnCutoffTimeSeconds = resolvePulseSpawnCutoffTimeSeconds(state, overlayState, timeSeconds);
      if (shouldUpdate(state, now, timeSeconds, -1, transitionSensitive, pulsesVisible, overlayState.armorModeActive(), transitionProgress)) {
         long perfStart = perfStart();
         StringBuilder perf = perfBuilder("pulseUpdate");
         fillPulseTexture(state, overlayState, timeSeconds, transitionSensitive, pulsesVisible, pulseSpawnCutoffTimeSeconds);
         long perfSection = perfAppend(perf, "fill", perfStart);
         state.upload();
         perfSection = perfAppend(perf, "upload", perfSection);
         state.lastUpdateMillis = now;
         state.lastTimeSeconds = timeSeconds;
         state.lastTransitionActive = transitionSensitive;
         state.lastBaseFrameIndex = -1;
         state.lastArmorModeActive = overlayState.armorModeActive();
         state.lastTransitionProgress = transitionProgress;
         perfLog(
            perf,
            perfStart,
            "transitionSensitive="
               + transitionSensitive
               + " visible="
               + pulsesVisible
               + " touched="
               + state.previousPulsePixelCount
               + " finalTouched="
               + state.finalTouchedCount
         );
      }
   }

   private static float resolvePulseSpawnCutoffTimeSeconds(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state, NanosuitArmorModeOverlayState.OverlayRenderState overlayState, float timeSeconds
   ) {
      if (overlayState != null && !overlayState.armorModeActive()) {
         if (Float.isNaN(state.deactivationPulseSpawnCutoffTimeSeconds) || state.lastArmorModeActive) {
            state.deactivationPulseSpawnCutoffTimeSeconds = timeSeconds;
         }

         return state.deactivationPulseSpawnCutoffTimeSeconds;
      } else {
         state.deactivationPulseSpawnCutoffTimeSeconds = Float.NaN;
         return Float.POSITIVE_INFINITY;
      }
   }

   private static void updateCompositeIfNeeded(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state,
      NanosuitArmorModeOverlayState.OverlayRenderState overlayState,
      int steadyFrameIndex,
      int pulseFrameIndex,
      boolean transitionActive,
      boolean outputOnlyPulse,
      NanoGeoArmorModeDynamicTextureCompatRenderer.PulseFrameTexture pulseFrame,
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState pulseState
   ) {
      long now = Util.getMillis();
      float transitionProgress = overlayState == null ? 1.0F : clamp01(overlayState.transitionProgress());
      boolean armorModeActive = overlayState != null && overlayState.armorModeActive();
      if (shouldUpdateComposite(
         state, now, steadyFrameIndex, pulseFrameIndex, transitionActive, pulseFrame != null || pulseState != null, armorModeActive, transitionProgress
      )) {
         clearTexture(state.pixels, state.width, state.height);
         if (!outputOnlyPulse) {
            composeBaseIntoTexture(state, overlayState, steadyFrameIndex, transitionActive);
         }

         if (pulseFrame != null) {
            composePulseImageIntoTexture(state, pulseFrame.corePixels, pulseFrame.corePixels.getWidth(), pulseFrame.corePixels.getHeight());
         } else if (pulseState != null && pulseState.pixels != null) {
            composePulseImageIntoTexture(state, pulseState.pixels, pulseState.width, pulseState.height);
         }

         state.upload();
         state.lastUpdateMillis = now;
         state.lastBaseFrameIndex = steadyFrameIndex;
         state.lastTransitionFrameIndex = pulseFrameIndex;
         state.lastTransitionActive = transitionActive;
         state.hasVisiblePulseContent = pulseFrame != null || pulseState != null;
         state.lastArmorModeActive = armorModeActive;
         state.lastTransitionProgress = transitionProgress;
      }
   }

   private static void updateBaseCompositeTransitionIfNeeded(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state,
      NanosuitArmorModeOverlayState.OverlayRenderState overlayState,
      int steadyFrameIndex,
      NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch
   ) {
      long now = Util.getMillis();
      float transitionProgress = clamp01(overlayState.transitionProgress());
      boolean armorModeActive = overlayState.armorModeActive();
      if (shouldUpdateBaseCompositeTransition(state, now, steadyFrameIndex, armorModeActive, transitionProgress)) {
         long perfStart = perfStart();
         StringBuilder perf = perfBuilder("baseCompositeTransitionUpdate");
         fillBaseCompositeTransitionTexture(state, steadyFrameIndex, patch, overlayState);
         long perfSection = perfAppend(perf, "fill", perfStart);
         state.upload();
         perfSection = perfAppend(perf, "upload", perfSection);
         state.lastUpdateMillis = now;
         state.lastBaseFrameIndex = steadyFrameIndex;
         state.lastTransitionActive = true;
         state.lastArmorModeActive = armorModeActive;
         state.lastTransitionProgress = transitionProgress;
         state.hasVisiblePulseContent = false;
         perfLog(
            perf,
            perfStart,
            "frame="
               + steadyFrameIndex
               + " progress="
               + formatPerfFloat(transitionProgress)
               + " overlayPixels="
               + state.previousBasePixelCount
               + " initialized="
               + state.baseCompositeInitialized
         );
      }
   }

   private static boolean shouldUpdateBaseCompositeTransition(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state, long now, int steadyFrameIndex, boolean armorModeActive, float transitionProgress
   ) {
      if (state.dynamicTexture == null || state.pixels == null) {
         return false;
      } else if (state.lastUpdateMillis == 0L) {
         return true;
      } else if (!state.lastTransitionActive || state.lastBaseFrameIndex != steadyFrameIndex) {
         return true;
      } else if (state.lastArmorModeActive == armorModeActive && !(transitionProgress < state.lastTransitionProgress)) {
         int updateIntervalMillis = Math.max(16, NanoGeoClientConfig.armorModeDynamicTextureCompatUpdateIntervalMillis() / 2);
         return now - state.lastUpdateMillis >= updateIntervalMillis;
      } else {
         return true;
      }
   }

   private static boolean shouldUpdateComposite(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state,
      long now,
      int steadyFrameIndex,
      int pulseFrameIndex,
      boolean transitionActive,
      boolean pulsesVisible,
      boolean armorModeActive,
      float transitionProgress
   ) {
      if (state.dynamicTexture == null || state.pixels == null) {
         return false;
      } else if (state.lastUpdateMillis == 0L) {
         return true;
      } else if (state.lastBaseFrameIndex != steadyFrameIndex || state.lastTransitionActive != transitionActive) {
         return true;
      } else if (state.lastArmorModeActive != armorModeActive || transitionProgress < state.lastTransitionProgress) {
         return true;
      } else if (!pulsesVisible && state.hasVisiblePulseContent) {
         return true;
      } else {
         int updateIntervalMillis = Math.max(16, NanoGeoClientConfig.armorModeDynamicTextureCompatUpdateIntervalMillis());
         return transitionActive
            ? now - state.lastUpdateMillis >= Math.max(16, updateIntervalMillis / 2)
            : pulsesVisible && state.lastTransitionFrameIndex != pulseFrameIndex && now - state.lastUpdateMillis >= updateIntervalMillis;
      }
   }

   private static void composeBaseIntoTexture(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state,
      NanosuitArmorModeOverlayState.OverlayRenderState overlayState,
      int steadyFrameIndex,
      boolean transitionActive
   ) {
      NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture frame = steadyBaseFrame(steadyFrameIndex);
      if (frame != null && frame.pixels != null) {
         NanoGeoArmorModeDynamicTextureCompatRenderer.TransitionSnapshot transition = transitionActive ? transitionSnapshot() : null;
         float transitionProgress = overlayState == null ? 1.0F : clamp01(overlayState.transitionProgress());

         for (int activeIndex = 0; activeIndex < baseActivePixelCount; activeIndex++) {
            int index = baseActivePixelIndices[activeIndex];
            int x = index % baseTargetWidth;
            int y = index / baseTargetWidth;
            int framePixel = frame.pixels.getPixelRGBA(x, y);
            float frameAlpha = (framePixel >>> 24 & 0xFF) / 255.0F;
            if (!(frameAlpha <= 0.0F)) {
               if (transition != null && transition.enabled) {
                  frameAlpha *= transitionMaskForDelayByte(baseTransitionDelayBytes[index] & 255, transitionProgress, transition.fadeWindow);
                  if (frameAlpha <= 0.0F) {
                     continue;
                  }
               }

               state.pixels.setPixelRGBA(x, y, packAlphaIntoPreparedAbgr(framePixel & 16777215, frameAlpha));
            }
         }
      }
   }

   private static void composePulseImageIntoTexture(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state, NativeImage pulsePixels, int pulseWidth, int pulseHeight
   ) {
      if (pulsePixels != null && pulseWidth > 0 && pulseHeight > 0) {
         for (int y = 0; y < state.height; y++) {
            int pulseY = sampleSourceCoordinateForTarget(y, state.height, pulseHeight);

            for (int x = 0; x < state.width; x++) {
               int pulseX = sampleSourceCoordinateForTarget(x, state.width, pulseWidth);
               int pulsePixel = pulsePixels.getPixelRGBA(pulseX, pulseY);
               float pulseAlpha = (pulsePixel >>> 24 & 0xFF) / 255.0F;
               if (!(pulseAlpha < 0.0039215684F)) {
                  int basePixel = state.pixels.getPixelRGBA(x, y);
                  state.pixels.setPixelRGBA(x, y, compositePulseOverBase(basePixel, pulsePixel, pulseAlpha));
               }
            }
         }
      }
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet prepareAndGetPulseAtlasTextures(
      NanosuitArmorModeOverlayState.OverlayRenderState overlayState
   ) {
      float timeSeconds = overlayState.shimmerTime() / 20.0F;
      if (!compatPulsesVisible(timeSeconds) && !NanoGeoClientConfig.armorModePulseDebugOutputOnlyPulse()) {
         return NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet.empty();
      } else {
         PULSE_ATLAS_BUILD_STATE.ensureCapacity(pulseTargetWidth, pulseTargetHeight);
         NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse = pulseRuntimeConfig(PULSE_ATLAS_BUILD_STATE);
         if (pulse.enabled && pulse.centerCount > 0) {
            int frameIndex = resolvePulseAtlasFrameIndex(timeSeconds, pulse);
            ensurePulseAtlasFramesCurrent(pulse, frameIndex);
            NanoGeoArmorModeDynamicTextureCompatRenderer.PulseFrameTexture frame = pulseAtlasFrame(frameIndex);
            return frame != null && frame.hasVisibleContent
               ? new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet(frame.coreTextureId, null)
               : NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet.empty();
         } else {
            closePulseAtlasFrames();
            return NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet.empty();
         }
      }
   }

   private static int resolvePulseAtlasFrameIndex(float timeSeconds, NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse) {
      float interval = max(pulse.intervalSeconds, 1.0E-4F);
      float phase = fract(timeSeconds / interval);
      return clampInt((int)Math.floor(phase * 64.0F), 0, 63);
   }

   private static void ensurePulseAtlasFramesCurrent(NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse, int preferredFrameIndex) {
      String signature = buildPulseAtlasSignature(pulse);
      ensurePulseStampFramesCurrent(pulse);
      if (!pulseStampFramesComplete()) {
         buildAdditionalPulseStampFrames(pulse, 1);
      } else if (signature.equals(pulseAtlasSignature) && pulseAtlasFrames.length == 64) {
         buildPulseAtlasFrameIfNeeded(preferredFrameIndex, pulse);
         buildAdditionalPulseAtlasFrames(pulse, 1);
      } else {
         closePulseAtlasFrames();
         pulseAtlasFrames = new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseFrameTexture[64];
         pulseAtlasSignature = signature;
         pulseAtlasBuildCursor = 0;
         buildPulseAtlasFrameIfNeeded(preferredFrameIndex, pulse);
         buildAdditionalPulseAtlasFrames(pulse, 1);
      }
   }

   private static String buildPulseAtlasSignature(NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse) {
      StringBuilder builder = new StringBuilder(buildPulseStampSignature(pulse))
         .append("|source=")
         .append(sourceTextureId)
         .append("|interval=")
         .append(pulse.intervalSeconds)
         .append("|duration=")
         .append(pulse.durationSeconds)
         .append("|centerCount=")
         .append(pulse.centerCount);

      for (int i = 0; i < pulse.centerCount; i++) {
         builder.append("|c")
            .append(i)
            .append('=')
            .append(PULSE_ATLAS_BUILD_STATE.scaledCenterX[i])
            .append(',')
            .append(PULSE_ATLAS_BUILD_STATE.scaledCenterY[i]);
      }

      return builder.toString();
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.PulseFrameTexture pulseAtlasFrame(int frameIndex) {
      return pulseAtlasFrames.length == 0 ? null : pulseAtlasFrames[clampInt(frameIndex, 0, pulseAtlasFrames.length - 1)];
   }

   private static void buildPulseAtlasFrameIfNeeded(int frameIndex, NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse) {
      if (pulseAtlasFrames.length != 0) {
         int safeIndex = clampInt(frameIndex, 0, pulseAtlasFrames.length - 1);
         if (pulseAtlasFrames[safeIndex] == null) {
            float framePhase = safeIndex / 64.0F;
            float frameTimeSeconds = framePhase * max(pulse.intervalSeconds, 1.0E-4F);
            ResourceLocation coreTextureId = ResourceLocation.fromNamespaceAndPath("crysismod", "dynamic/armor_mode_overlay_compat/pulse_atlas_core/" + safeIndex);
            long perfStart = perfStart();
            StringBuilder perf = perfBuilder("pulseAtlasFrameBuild");
            NanoGeoArmorModeDynamicTextureCompatRenderer.PulseFrameTexture frame = new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseFrameTexture(
               coreTextureId, pulseTargetWidth, pulseTargetHeight
            );
            long perfSection = perfAppend(perf, "allocate", perfStart);
            fillPulseTexture(PULSE_ATLAS_BUILD_STATE, null, frameTimeSeconds, false, true, Float.POSITIVE_INFINITY);
            perfSection = perfAppend(perf, "fillPulse", perfSection);
            frame.copyFrom(PULSE_ATLAS_BUILD_STATE);
            perfSection = perfAppend(perf, "copy", perfSection);
            frame.upload();
            perfSection = perfAppend(perf, "upload", perfSection);
            pulseAtlasFrames[safeIndex] = frame;
            perfLog(perf, perfStart, "frame=" + safeIndex + " visible=" + frame.hasVisibleContent);
         }
      }
   }

   private static void buildAdditionalPulseAtlasFrames(NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse, int budget) {
      if (pulseAtlasFrames.length != 0) {
         int remaining = Math.max(budget, 0);

         while (remaining > 0 && pulseAtlasBuildCursor < pulseAtlasFrames.length) {
            int frameIndex = pulseAtlasBuildCursor++;
            if (pulseAtlasFrames[frameIndex] == null) {
               buildPulseAtlasFrameIfNeeded(frameIndex, pulse);
               remaining--;
            }
         }
      }
   }

   private static void closePulseAtlasFrames() {
      for (NanoGeoArmorModeDynamicTextureCompatRenderer.PulseFrameTexture frame : pulseAtlasFrames) {
         if (frame != null) {
            frame.close();
         }
      }

      pulseAtlasFrames = new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseFrameTexture[0];
      pulseAtlasSignature = "";
      pulseAtlasBuildCursor = 0;
   }

   private static boolean shouldUpdate(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state,
      long now,
      float timeSeconds,
      int baseFrameIndex,
      boolean transitionActive,
      boolean pulsesVisible,
      boolean armorModeActive,
      float transitionProgress
   ) {
      if (state.dynamicTexture == null || state.pixels == null) {
         return false;
      } else if (state.lastUpdateMillis == 0L) {
         return true;
      } else if (state.lastBaseFrameIndex != baseFrameIndex || state.lastTransitionActive != transitionActive) {
         return true;
      } else if (state.lastArmorModeActive != armorModeActive || transitionProgress < state.lastTransitionProgress) {
         return true;
      } else if (!pulsesVisible && state.hasVisiblePulseContent) {
         return true;
      } else {
         int updateIntervalMillis = Math.max(16, NanoGeoClientConfig.armorModeDynamicTextureCompatUpdateIntervalMillis());
         return transitionActive && now - state.lastUpdateMillis >= Math.max(16, updateIntervalMillis / 2)
            ? true
            : pulsesVisible && now - state.lastUpdateMillis >= updateIntervalMillis;
      }
   }

   private static boolean shouldUpdateTransitionBase(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state, long now, int steadyFrameIndex, float transitionProgress, boolean armorModeActive
   ) {
      if (state.dynamicTexture == null || state.pixels == null) {
         return false;
      } else if (state.lastUpdateMillis == 0L) {
         return true;
      } else if (!state.lastTransitionActive || state.lastBaseFrameIndex != steadyFrameIndex) {
         return true;
      } else if (state.lastArmorModeActive == armorModeActive && !(transitionProgress < state.lastTransitionProgress)) {
         int updateIntervalMillis = Math.max(16, NanoGeoClientConfig.armorModeDynamicTextureCompatUpdateIntervalMillis() / 2);
         return now - state.lastUpdateMillis >= updateIntervalMillis;
      } else {
         return true;
      }
   }

   private static void fillTransitionBaseTexture(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state, float transitionProgress, int steadyFrameIndex
   ) {
      NanoGeoArmorModeDynamicTextureCompatRenderer.TransitionSnapshot transition = transitionSnapshot();
      if (transitionProgress <= 1.0E-4F) {
         state.previousBasePixelCount = 0;
      } else {
         NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture frame = steadyBaseFrame(steadyFrameIndex);
         if (frame != null && frame.pixels != null) {
            clearPreviousBasePixels(state);

            for (int activeIndex = 0; activeIndex < baseActivePixelCount; activeIndex++) {
               int index = baseActivePixelIndices[activeIndex];
               int x = index % state.width;
               int y = index / state.width;
               float transitionMaskValue = transition.enabled
                  ? transitionMaskForDelayByte(baseTransitionDelayBytes[index] & 255, transitionProgress, transition.fadeWindow)
                  : 1.0F;
               if (transitionMaskValue <= 0.0F) {
                  state.pixels.setPixelRGBA(x, y, 0);
               } else {
                  int framePixel = frame.pixels.getPixelRGBA(x, y);
                  float frameAlpha = (framePixel >>> 24 & 0xFF) / 255.0F;
                  if (frameAlpha <= 0.0F) {
                     state.pixels.setPixelRGBA(x, y, 0);
                  } else {
                     state.pixels.setPixelRGBA(x, y, packAlphaIntoPreparedAbgr(framePixel & 16777215, frameAlpha * transitionMaskValue));
                     state.previousBasePixelIndices[state.previousBasePixelCount++] = index;
                  }
               }
            }
         } else {
            state.previousBasePixelCount = 0;
         }
      }
   }

   private static void clearPreviousBasePixels(NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state) {
      for (int i = 0; i < state.previousBasePixelCount; i++) {
         int index = state.previousBasePixelIndices[i];
         state.pixels.setPixelRGBA(index % state.width, index / state.width, 0);
      }

      state.previousBasePixelCount = 0;
   }

   private static void ensurePreparedBaseRgbCache(NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch) {
      if (basePreparedSourceRgb.length != baseSampledSourcePixels.length) {
         basePreparedSourceRgb = new int[baseSampledSourcePixels.length];
         lastPreparedOverlayBrightness = Float.NaN;
         lastPreparedGlowStrength = Float.NaN;
      }

      if (patch.overlayBrightness != lastPreparedOverlayBrightness || patch.glowStrength != lastPreparedGlowStrength) {
         for (int i = 0; i < baseSampledSourcePixels.length; i++) {
            int sourcePixel = baseSampledSourcePixels[i];
            float sourceAlpha = (sourcePixel >>> 24 & 0xFF) / 255.0F;
            float sourceRed = abgrRed(sourcePixel);
            float sourceGreen = abgrGreen(sourcePixel);
            float sourceBlue = abgrBlue(sourcePixel);
            float glowMask = sourceAlpha * smoothstep(0.42F, 0.78F, luminance(sourceRed, sourceGreen, sourceBlue));
            float red = clamp01(sourceRed * patch.overlayBrightness);
            float green = clamp01(sourceGreen * patch.overlayBrightness);
            float blue = clamp01(sourceBlue * patch.overlayBrightness);
            if (glowMask > 0.0F && patch.glowStrength > 0.0F) {
               red = clamp01(red * (1.0F + 0.3F * glowMask * patch.glowStrength));
               green = clamp01(green * (1.0F + 0.3F * glowMask * patch.glowStrength));
               blue = clamp01(blue * (1.0F + 0.3F * glowMask * patch.glowStrength));
               float whiteMix = 0.18F * glowMask * patch.glowStrength;
               red = lerp(red, 1.0F, whiteMix);
               green = lerp(green, 1.0F, whiteMix);
               blue = lerp(blue, 1.0F, whiteMix);
            }

            basePreparedSourceRgb[i] = packRgbAbgr(red, green, blue);
         }

         lastPreparedOverlayBrightness = patch.overlayBrightness;
         lastPreparedGlowStrength = patch.glowStrength;
      }
   }

   private static void ensureSteadyBaseFramesCurrent(NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch, int preferredFrameIndex) {
      String signature = buildSteadyBaseFramesSignature(patch);
      if (signature.equals(steadyBaseFramesSignature) && steadyBaseFrames.length == 96) {
         buildSteadyBaseFrameIfNeeded(preferredFrameIndex, patch);
         buildAdditionalSteadyBaseFrames(patch, 2);
      } else {
         closeSteadyBaseFrames();
         steadyBaseFrames = new NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture[96];
         steadyBaseFramesSignature = signature;
         steadyBaseBuildCursor = 0;
         buildSteadyBaseFrameIfNeeded(preferredFrameIndex, patch);
         buildAdditionalSteadyBaseFrames(patch, 2);
      }
   }

   private static String buildSteadyBaseFramesSignature(NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch) {
      return sourceTextureId
         + "|baseTarget="
         + baseTargetWidth
         + "x"
         + baseTargetHeight
         + "|baseFlowMask="
         + baseFlowMaskWidth
         + "x"
         + baseFlowMaskHeight
         + "|overlayBrightness="
         + patch.overlayBrightness
         + "|glowStrength="
         + patch.glowStrength
         + "|patchTextureAlpha="
         + patch.textureAlpha
         + "|patchSpotAlpha="
         + patch.spotAlpha
         + "|patchMin="
         + patch.minSizePx
         + "|patchMax="
         + patch.maxSizePx
         + "|patchFreq="
         + patch.frequency
         + "|patchDensity="
         + patch.density
         + "|patchVariant="
         + patch.variant
         + "|loopDuration="
         + patch.loopDurationSeconds;
   }

   private static void fillSteadyBaseFrame(NativeImage pixels, float framePhase, NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch) {
      clearTexture(pixels, baseTargetWidth, baseTargetHeight);

      for (int activeIndex = 0; activeIndex < baseActivePixelCount; activeIndex++) {
         int index = baseActivePixelIndices[activeIndex];
         int x = index % baseTargetWidth;
         int y = index / baseTargetWidth;
         float sourceAlpha = baseSampledSourceAlpha[index];
         float spotMask = chaoticPatchSpotMaskLooped(x + 0.5F, y + 0.5F, framePhase, patch);
         float localAlphaFactor = 1.0F - spotMask * patch.spotAlpha;
         float alpha = clamp01(sourceAlpha * patch.textureAlpha * localAlphaFactor * 1.0F);
         pixels.setPixelRGBA(x, y, packAlphaIntoPreparedAbgr(basePreparedSourceRgb[index], alpha));
      }
   }

   private static int resolveSteadyBaseFrameIndex(float timeSeconds, NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch) {
      float loopDuration = max(patch.loopDurationSeconds, 1.0E-4F);
      float phase = fract(timeSeconds / loopDuration);
      return clampInt((int)Math.floor(phase * 96.0F), 0, 95);
   }

   private static ResourceLocation steadyBaseFrameTextureId(int frameIndex) {
      NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture frame = steadyBaseFrame(frameIndex);
      return frame != null ? frame.textureId : null;
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture steadyBaseFrame(int frameIndex) {
      if (steadyBaseFrames.length == 0) {
         return null;
      } else {
         int safeIndex = clampInt(frameIndex, 0, steadyBaseFrames.length - 1);
         return steadyBaseFrames[safeIndex];
      }
   }

   private static void buildSteadyBaseFrameIfNeeded(int frameIndex, NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch) {
      if (steadyBaseFrames.length != 0) {
         int safeIndex = clampInt(frameIndex, 0, steadyBaseFrames.length - 1);
         if (steadyBaseFrames[safeIndex] == null) {
            float framePhase = safeIndex / 96.0F;
            ResourceLocation frameTextureId = ResourceLocation.fromNamespaceAndPath("crysismod", "dynamic/armor_mode_overlay_compat/base_steady/" + safeIndex);
            NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture frame = new NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture(
               frameTextureId, baseTargetWidth, baseTargetHeight
            );
            fillSteadyBaseFrame(frame.pixels, framePhase, patch);
            frame.upload();
            steadyBaseFrames[safeIndex] = frame;
         }
      }
   }

   private static void buildAdditionalSteadyBaseFrames(NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch, int budget) {
      if (steadyBaseFrames.length != 0) {
         int remaining = Math.max(budget, 0);

         while (remaining > 0 && steadyBaseBuildCursor < steadyBaseFrames.length) {
            int frameIndex = steadyBaseBuildCursor++;
            if (steadyBaseFrames[frameIndex] == null) {
               buildSteadyBaseFrameIfNeeded(frameIndex, patch);
               remaining--;
            }
         }
      }
   }

   private static void closeSteadyBaseFrames() {
      for (NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture frame : steadyBaseFrames) {
         if (frame != null) {
            frame.close();
         }
      }

      steadyBaseFrames = new NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture[0];
      steadyBaseFramesSignature = "";
      steadyBaseBuildCursor = 0;
   }

   private static void ensureSteadyBaseCompositeFramesCurrent(
      NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch, NanosuitArmorModeOverlayState.OverlayRenderState overlayState, int preferredFrameIndex
   ) {
      String signature = buildSteadyBaseCompositeFramesSignature(patch, overlayState);
      if (signature.equals(steadyBaseCompositeFramesSignature) && steadyBaseCompositeFrames.length == 96) {
         buildSteadyBaseCompositeFrameIfNeeded(preferredFrameIndex, patch, overlayState);
         buildAdditionalSteadyBaseCompositeFrames(patch, overlayState, 1);
      } else {
         closeSteadyBaseCompositeFrames();
         steadyBaseCompositeFrames = new NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture[96];
         steadyBaseCompositeFramesSignature = signature;
         steadyBaseCompositeBuildCursor = 0;
         buildSteadyBaseCompositeFrameIfNeeded(preferredFrameIndex, patch, overlayState);
         buildAdditionalSteadyBaseCompositeFrames(patch, overlayState, 1);
      }
   }

   private static String buildSteadyBaseCompositeFramesSignature(
      NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch, NanosuitArmorModeOverlayState.OverlayRenderState overlayState
   ) {
      return buildSteadyBaseFramesSignature(patch)
         + "|baseArmor="
         + NanoGeoArmorTextures.BASE_ARMOR
         + "|baseArmorSamples="
         + baseArmorSampledPixels.length
         + "|overlayTint="
         + overlayState.red()
         + ","
         + overlayState.green()
         + ","
         + overlayState.blue()
         + "|overlayAlpha="
         + overlayState.alpha()
         + "|uvCoverage="
         + NanoGeoClientConfig.armorModeDynamicTextureCompatUvCoverageMaskEnabled()
         + ":"
         + baseUvCoveredPixelCount
         + ":"
         + baseUvCoveredQuadCount;
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture steadyBaseCompositeFrame(int frameIndex) {
      return steadyBaseCompositeFrames.length == 0 ? null : steadyBaseCompositeFrames[clampInt(frameIndex, 0, steadyBaseCompositeFrames.length - 1)];
   }

   private static void buildSteadyBaseCompositeFrameIfNeeded(
      int frameIndex, NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch, NanosuitArmorModeOverlayState.OverlayRenderState overlayState
   ) {
      if (steadyBaseCompositeFrames.length != 0 && baseArmorSampledPixels.length == baseTargetWidth * baseTargetHeight) {
         int safeIndex = clampInt(frameIndex, 0, steadyBaseCompositeFrames.length - 1);
         if (steadyBaseCompositeFrames[safeIndex] == null) {
            ResourceLocation textureId = ResourceLocation.fromNamespaceAndPath("crysismod", "dynamic/armor_mode_overlay_compat/base_composite_steady/" + safeIndex);
            NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture frame = new NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture(
               textureId, baseTargetWidth, baseTargetHeight
            );
            fillSteadyBaseCompositeFrame(frame.pixels, safeIndex / 96.0F, patch, overlayState);
            frame.upload();
            steadyBaseCompositeFrames[safeIndex] = frame;
         }
      }
   }

   private static void fillSteadyBaseCompositeFrame(
      NativeImage pixels,
      float framePhase,
      NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch,
      NanosuitArmorModeOverlayState.OverlayRenderState overlayState
   ) {
      fillBaseCompositeTexture(pixels, framePhase, patch, overlayState, false);
   }

   private static void fillBaseCompositeTransitionTexture(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state,
      int steadyFrameIndex,
      NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch,
      NanosuitArmorModeOverlayState.OverlayRenderState overlayState
   ) {
      if (state.pixels != null && baseArmorSampledPixels.length == state.width * state.height) {
         if (!state.baseCompositeInitialized) {
            for (int index = 0; index < baseArmorSampledPixels.length; index++) {
               state.pixels.setPixelRGBA(index % state.width, index / state.width, baseArmorSampledPixels[index]);
            }

            state.baseCompositeInitialized = true;
            state.previousBasePixelCount = 0;
         } else {
            restorePreviousBaseCompositeOverlayPixels(state);
         }

         NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture steadyCompositeFrame = steadyBaseCompositeFrame(steadyFrameIndex);
         if (steadyCompositeFrame != null && steadyCompositeFrame.pixels != null) {
            fillBaseCompositeTransitionFromSteadyFrame(state, steadyCompositeFrame, overlayState);
         } else {
            float framePhase = steadyFrameIndex / 96.0F;
            float globalOverlayAlpha = clamp01(overlayState.alpha());
            float tintR = clamp(overlayState.red(), 0.0F, 2.0F);
            float tintG = clamp(overlayState.green(), 0.0F, 2.0F);
            float tintB = clamp(overlayState.blue(), 0.0F, 2.0F);
            NanoGeoArmorModeDynamicTextureCompatRenderer.TransitionSnapshot transition = transitionSnapshot();
            float transitionProgress = clamp01(overlayState.transitionProgress());

            for (int activeIndex = 0; activeIndex < baseActivePixelCount; activeIndex++) {
               int index = baseActivePixelIndices[activeIndex];
               int x = index % state.width;
               int y = index / state.width;
               float sourceAlpha = baseSampledSourceAlpha[index];
               float spotMask = chaoticPatchSpotMaskLooped(x + 0.5F, y + 0.5F, framePhase, patch);
               float localAlphaFactor = 1.0F - spotMask * patch.spotAlpha;
               float transitionMask = transition.enabled
                  ? transitionMaskForDelayByte(baseTransitionDelayBytes[index] & 255, transitionProgress, transition.fadeWindow)
                  : 1.0F;
               float overlayAlpha = clamp01(sourceAlpha * patch.textureAlpha * localAlphaFactor * 1.0F * globalOverlayAlpha * transitionMask);
               if (!(overlayAlpha <= 0.0F)) {
                  int preparedRgb = basePreparedSourceRgb[index];
                  int tintedOverlay = packAbgr(abgrRed(preparedRgb) * tintR, abgrGreen(preparedRgb) * tintG, abgrBlue(preparedRgb) * tintB, overlayAlpha);
                  state.pixels.setPixelRGBA(x, y, compositeSourceOver(baseArmorSampledPixels[index], tintedOverlay));
                  state.previousBasePixelIndices[state.previousBasePixelCount++] = index;
               }
            }
         }
      }
   }

   private static void fillBaseCompositeTransitionFromSteadyFrame(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state,
      NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture steadyCompositeFrame,
      NanosuitArmorModeOverlayState.OverlayRenderState overlayState
   ) {
      NanoGeoArmorModeDynamicTextureCompatRenderer.TransitionSnapshot transition = transitionSnapshot();
      float transitionProgress = clamp01(overlayState.transitionProgress());

      for (int activeIndex = 0; activeIndex < baseActivePixelCount; activeIndex++) {
         int index = baseActivePixelIndices[activeIndex];
         float transitionMask = transition.enabled
            ? transitionMaskForDelayByte(baseTransitionDelayBytes[index] & 255, transitionProgress, transition.fadeWindow)
            : 1.0F;
         if (!(transitionMask <= 0.0F)) {
            int x = index % state.width;
            int y = index / state.width;
            int basePixel = baseArmorSampledPixels[index];
            int fullPixel = steadyCompositeFrame.pixels.getPixelRGBA(x, y);
            int compositedPixel = transitionMask >= 0.9999F ? fullPixel : lerpAbgr(basePixel, fullPixel, transitionMask);
            if (compositedPixel != basePixel) {
               state.pixels.setPixelRGBA(x, y, compositedPixel);
               state.previousBasePixelIndices[state.previousBasePixelCount++] = index;
            }
         }
      }
   }

   private static void restorePreviousBaseCompositeOverlayPixels(NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state) {
      for (int i = 0; i < state.previousBasePixelCount; i++) {
         int index = state.previousBasePixelIndices[i];
         state.pixels.setPixelRGBA(index % state.width, index / state.width, baseArmorSampledPixels[index]);
      }

      state.previousBasePixelCount = 0;
   }

   private static void fillBaseCompositeTexture(
      NativeImage pixels,
      float framePhase,
      NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch,
      NanosuitArmorModeOverlayState.OverlayRenderState overlayState,
      boolean transitionActive
   ) {
      for (int index = 0; index < baseArmorSampledPixels.length; index++) {
         pixels.setPixelRGBA(index % baseTargetWidth, index / baseTargetWidth, baseArmorSampledPixels[index]);
      }

      float globalOverlayAlpha = clamp01(overlayState.alpha());
      float tintR = clamp(overlayState.red(), 0.0F, 2.0F);
      float tintG = clamp(overlayState.green(), 0.0F, 2.0F);
      float tintB = clamp(overlayState.blue(), 0.0F, 2.0F);
      NanoGeoArmorModeDynamicTextureCompatRenderer.TransitionSnapshot transition = transitionActive ? transitionSnapshot() : null;
      float transitionProgress = clamp01(overlayState.transitionProgress());

      for (int activeIndex = 0; activeIndex < baseActivePixelCount; activeIndex++) {
         int index = baseActivePixelIndices[activeIndex];
         int x = index % baseTargetWidth;
         int y = index / baseTargetWidth;
         float sourceAlpha = baseSampledSourceAlpha[index];
         float spotMask = chaoticPatchSpotMaskLooped(x + 0.5F, y + 0.5F, framePhase, patch);
         float localAlphaFactor = 1.0F - spotMask * patch.spotAlpha;
         float transitionMask = transition != null && transition.enabled
            ? transitionMaskForDelayByte(baseTransitionDelayBytes[index] & 255, transitionProgress, transition.fadeWindow)
            : 1.0F;
         float overlayAlpha = clamp01(sourceAlpha * patch.textureAlpha * localAlphaFactor * 1.0F * globalOverlayAlpha * transitionMask);
         if (!(overlayAlpha <= 0.0F)) {
            int preparedRgb = basePreparedSourceRgb[index];
            int tintedOverlay = packAbgr(abgrRed(preparedRgb) * tintR, abgrGreen(preparedRgb) * tintG, abgrBlue(preparedRgb) * tintB, overlayAlpha);
            int basePixel = pixels.getPixelRGBA(x, y);
            pixels.setPixelRGBA(x, y, compositeSourceOver(basePixel, tintedOverlay));
         }
      }
   }

   private static void buildAdditionalSteadyBaseCompositeFrames(
      NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch, NanosuitArmorModeOverlayState.OverlayRenderState overlayState, int budget
   ) {
      int remaining = Math.max(0, budget);

      while (remaining > 0 && steadyBaseCompositeBuildCursor < steadyBaseCompositeFrames.length) {
         int frameIndex = steadyBaseCompositeBuildCursor++;
         if (steadyBaseCompositeFrames[frameIndex] == null) {
            buildSteadyBaseCompositeFrameIfNeeded(frameIndex, patch, overlayState);
            remaining--;
         }
      }
   }

   private static void closeSteadyBaseCompositeFrames() {
      for (NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture frame : steadyBaseCompositeFrames) {
         if (frame != null) {
            frame.close();
         }
      }

      steadyBaseCompositeFrames = new NanoGeoArmorModeDynamicTextureCompatRenderer.StaticFrameTexture[0];
      steadyBaseCompositeFramesSignature = "";
      steadyBaseCompositeBuildCursor = 0;
   }

   private static void fillPulseTexture(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state,
      NanosuitArmorModeOverlayState.OverlayRenderState overlayState,
      float timeSeconds,
      boolean transitionSensitive,
      boolean pulsesVisible,
      float pulseSpawnCutoffTimeSeconds
   ) {
      clearPreviousPulsePixels(state);
      if (!pulsesVisible) {
         state.hasVisiblePulseContent = false;
      } else {
         NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse = pulseRuntimeConfig(state);
         if (pulse.enabled && pulse.centerCount > 0) {
            ensurePulseStampFramesCurrent(pulse);
            beginPulseFrame(state);
            renderCompatPulseBuffers(state, pulse, timeSeconds, pulseSpawnCutoffTimeSeconds);
            NanoGeoArmorModeDynamicTextureCompatRenderer.TransitionSnapshot transition = transitionSensitive ? transitionSnapshot() : null;
            float transitionProgress = overlayState == null ? 1.0F : clamp01(overlayState.transitionProgress());
            state.hasVisiblePulseContent = false;

            for (int i = 0; i < state.finalTouchedCount; i++) {
               int index = state.finalTouchedIndices[i];
               int x = index % state.width;
               int y = index / state.width;
               float sourceAlpha = clamp01(pulseSampledSourceAlpha[index]);
               if (sourceAlpha < 0.0039215684F) {
                  state.pixels.setPixelRGBA(x, y, 0);
                  if (state.secondaryPixels != null) {
                     state.secondaryPixels.setPixelRGBA(x, y, 0);
                  }
               } else {
                  float pulseCore = state.coreBuffer[index];
                  float pulseGlow = state.glowBuffer[index];
                  if (transitionSensitive && transition != null && transition.enabled && transition.affectsPulses) {
                     float transitionMaskValue = transitionMaskForDelayByte(pulseTransitionDelayBytes[index] & 255, transitionProgress, transition.fadeWindow);
                     pulseCore *= transitionMaskValue;
                     pulseGlow *= transitionMaskValue;
                  }

                  float coreAlpha = clamp01(pulseCore * 1.0F * sourceAlpha);
                  float glowAlpha = clamp01(pulseGlow * 1.0F * sourceAlpha);
                  float combinedAlpha = max(coreAlpha, glowAlpha);
                  if (combinedAlpha < 0.0039215684F) {
                     state.pixels.setPixelRGBA(x, y, 0);
                     if (state.secondaryPixels != null) {
                        state.secondaryPixels.setPixelRGBA(x, y, 0);
                     }
                  } else if (combinedAlpha <= 1.0E-4F) {
                     state.pixels.setPixelRGBA(x, y, 0);
                     if (state.secondaryPixels != null) {
                        state.secondaryPixels.setPixelRGBA(x, y, 0);
                     }
                  } else {
                     state.pixels.setPixelRGBA(x, y, packCombinedPulseAbgr(pulse, coreAlpha, glowAlpha));
                     if (state.secondaryPixels != null) {
                        state.secondaryPixels.setPixelRGBA(x, y, 0);
                     }

                     state.previousPulsePixelIndices[state.previousPulsePixelCount++] = index;
                     state.hasVisiblePulseContent = true;
                  }
               }
            }
         } else {
            state.hasVisiblePulseContent = false;
         }
      }
   }

   private static void renderCompatPulseBuffers(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state,
      NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse,
      float timeSeconds,
      float pulseSpawnCutoffTimeSeconds
   ) {
      float baseGeneration = (float)Math.floor(timeSeconds / pulse.intervalSeconds);

      for (int waveIndex = 0; waveIndex < 3; waveIndex++) {
         float generation = baseGeneration - waveIndex;
         float triggerTime = generation * pulse.intervalSeconds;
         if (!(triggerTime < 0.0F) && !(triggerTime > pulseSpawnCutoffTimeSeconds)) {
            float age = timeSeconds - triggerTime;
            if (!(age < 0.0F) && !(age > pulse.durationSeconds)) {
               float progress = clamp01(age / pulse.durationSeconds);
               int frameIndex = clampInt((int)Math.floor(progress * 47.0F), 0, 47);
               if (pulse.mergingEnabled) {
                  beginPulseWave(state);
               }

               for (int centerIndex = 0; centerIndex < pulse.centerCount; centerIndex++) {
                  int variantIndex = (centerIndex + waveIndex) % 4;
                  NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame frame = pulseStampFrame(variantIndex, frameIndex);
                  if (frame != null) {
                     stampPulseFrame(state, pulse, frame, state.scaledCenterX[centerIndex], state.scaledCenterY[centerIndex]);
                  }
               }

               if (pulse.mergingEnabled) {
                  finalizeMergedPulseWave(state, pulse);
               }
            }
         }
      }
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulseRuntimeConfig(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state
   ) {
      boolean debugForceVisible = NanoGeoClientConfig.armorModePulseDebugForceVisible();
      boolean enabled = debugForceVisible || NanoGeoClientConfig.armorModePulseEnabled();
      NanoGeoArmorModePulseCenterSet centerSet = NanoGeoArmorModePulseCenters.INSTANCE.centers();
      int configuredMaxCenters = debugForceVisible ? Math.min(8, 64) : NanoGeoClientConfig.armorModePulseMaxCenters();
      int centerCount = Math.min(NanoGeoArmorModePulseCenters.INSTANCE.shaderCenterCount(configuredMaxCenters), centerSet.centerCount());
      if (enabled && centerCount > 0) {
         float centerTextureWidth = centerSet.textureWidth() > 0 ? centerSet.textureWidth() : state.width;
         float centerTextureHeight = centerSet.textureHeight() > 0 ? centerSet.textureHeight() : state.height;
         float scaleX = state.width / centerTextureWidth;
         float scaleY = state.height / centerTextureHeight;
         float radialScale = max((scaleX + scaleY) * 0.5F, 1.0E-4F);

         for (int i = 0; i < centerCount; i++) {
            NanoGeoArmorModePulseCenterSet.Center center = centerSet.centers().get(i);
            state.scaledCenterX[i] = scalePulseCenterCoordinate(center.x(), scaleX);
            state.scaledCenterY[i] = scalePulseCenterCoordinate(center.y(), scaleY);
         }

         boolean electric = debugForceVisible
            || NanoGeoClientConfig.armorModePulseVisualMode() == NanoGeoClientConfig.ArmorModePulseVisualMode.CRYSIS_ELECTRIC_RING;
         boolean mergingEnabled = !debugForceVisible && NanoGeoClientConfig.armorModePulseMergingEnabled();
         return new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig(
            true,
            centerCount,
            electric,
            mergingEnabled,
            debugForceVisible ? 1.0F : max(NanoGeoClientConfig.armorModePulseIntervalSeconds(), 1.0E-4F),
            debugForceVisible ? 2.0F : max(NanoGeoClientConfig.armorModePulseDurationSeconds(), 1.0E-4F),
            clampCompatPulseRadius(debugForceVisible ? 2.0F : NanoGeoClientConfig.armorModePulseStartRadiusPx(), radialScale),
            clampCompatPulseRadius(debugForceVisible ? 80.0F : NanoGeoClientConfig.armorModePulseEndRadiusPx(), radialScale),
            debugForceVisible ? 1.0F * radialScale : NanoGeoClientConfig.armorModePulseCoreWidthPx() * radialScale,
            debugForceVisible ? 1.0F * radialScale : NanoGeoClientConfig.armorModePulseGlowRadiusPx() * radialScale,
            debugForceVisible ? 0.25F * radialScale : NanoGeoClientConfig.armorModePulseCoreSoftnessPx() * radialScale,
            debugForceVisible ? 0.5F * radialScale : NanoGeoClientConfig.armorModePulseGlowSoftnessPx() * radialScale,
            debugForceVisible ? 1.0F : NanoGeoClientConfig.armorModePulseCoreAlpha() / 255.0F,
            debugForceVisible ? 1.0F : NanoGeoClientConfig.armorModePulseGlowAlpha() / 255.0F,
            debugForceVisible ? 14285567 : NanoGeoClientConfig.armorModePulseCoreColorRgb(),
            debugForceVisible ? 5888511 : NanoGeoClientConfig.armorModePulseGlowColorRgb(),
            debugForceVisible ? 4.0F : NanoGeoClientConfig.armorModePulseBrightness(),
            debugForceVisible ? 1.5F : max(NanoGeoClientConfig.armorModePulseFadePower(), 1.0E-4F),
            NanoGeoClientConfig.armorModePulseRingDistortionPx() * radialScale,
            NanoGeoClientConfig.armorModePulseRingDistortionFrequency(),
            clamp01(NanoGeoClientConfig.armorModePulseRingEnergyVariation()),
            clamp01(NanoGeoClientConfig.armorModePulseWidthVariation()),
            clamp01(NanoGeoClientConfig.armorModePulseBrightnessVariation()),
            clamp01(NanoGeoClientConfig.armorModePulseEdgeRoughness()),
            clamp01(NanoGeoClientConfig.armorModePulsePatternRotationRandomness()),
            clamp01(NanoGeoClientConfig.armorModePulseMergeStrength()),
            max(NanoGeoClientConfig.armorModePulseMergeSoftnessPx(), 0.0F) * radialScale,
            max(NanoGeoClientConfig.armorModePulseMergeBrightnessBoost(), 0.0F)
         );
      } else {
         return NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig.disabled();
      }
   }

   private static float clampCompatPulseRadius(float radiusPx, float radialScale) {
      return clamp(radiusPx, 0.0F, 96.0F) * radialScale;
   }

   private static float scalePulseCenterCoordinate(float coordinate, float scale) {
      return (coordinate - 0.5F) * scale + 0.5F;
   }

   private static void ensurePulseStampFramesCurrent(NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse) {
      String signature = buildPulseStampSignature(pulse);
      if (!signature.equals(pulseStampSignature) || pulseStampFrames.length != 4) {
         closePulseStampFrames();
         pulseStampFrames = new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame[4][48];
         pulseStampSignature = signature;
         float maxReach = pulseMaxReach(pulse, pulse.endRadiusPx);
         pulseStampFrameSize = Math.max(8, (int)Math.ceil(maxReach) * 2 + 6);
         pulseStampFrameCenter = pulseStampFrameSize * 0.5F - 0.5F;
         pulseStampBuildCursor = 0;
         if (NanoGeoClientConfig.debugArmorModeOverlay()) {
            LOGGER.info(
               "[CrysisMod][ArmorModePerf] pulseStampCacheReset frameSize={} variants={} frames={} centerCount={} merging={} electric={}",
               new Object[]{pulseStampFrameSize, 4, 48, pulse.centerCount, pulse.mergingEnabled, pulse.electric}
            );
         }
      }
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame buildPulseStampFrame(
      int variantIndex, int frameIndex, NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse
   ) {
      if (pulseStampFrames.length != 4 || pulseStampFrameSize <= 0) {
         ensurePulseStampFramesCurrent(pulse);
      }

      int safeVariant = clampInt(variantIndex, 0, 3);
      int safeFrame = clampInt(frameIndex, 0, 47);
      NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame[] variantFrames = pulseStampFrames[safeVariant];
      if (variantFrames == null || variantFrames.length != 48) {
         variantFrames = new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame[48];
         pulseStampFrames[safeVariant] = variantFrames;
      }

      if (variantFrames[safeFrame] != null) {
         return variantFrames[safeFrame];
      } else {
         long perfStart = perfStart();
         StringBuilder perf = perfBuilder("pulseStampFrameBuild");
         int frameSize = pulseStampFrameSize;
         float center = pulseStampFrameCenter;
         NanoGeoArmorModeDynamicTextureCompatRenderer.PulseProfile profile = new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseProfile();
         float variantSeed = safeVariant * 17.417F + 3.14159F;
         float progress = (float)safeFrame / Math.max(47, 1);
         float fade = (float)Math.pow(max(1.0F - progress, 0.0F), pulse.fadePower);
         float radius = lerp(pulse.startRadiusPx, pulse.endRadiusPx, progress);
         NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame frame = new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame(
            frameSize, frameSize, center, center
         );

         for (int y = 0; y < frameSize; y++) {
            for (int x = 0; x < frameSize; x++) {
               float core = 0.0F;
               float glow = 0.0F;
               boolean profileActive = false;

               for (int sampleY = 0; sampleY < 2; sampleY++) {
                  float dy = y + (sampleY == 0 ? 0.25F : 0.75F) - center;

                  for (int sampleX = 0; sampleX < 2; sampleX++) {
                     float dx = x + (sampleX == 0 ? 0.25F : 0.75F) - center;
                     float angle = (float)Math.atan2(dy, dx);
                     boolean active = pulse.electric
                        ? electricRingProfile(dx, dy, radius, fade, angle, variantSeed, 0.22F, pulse, profile)
                        : simpleRingProfile(dx, dy, radius, fade, 0.22F, pulse, profile);
                     boolean mergeFieldActive = pulse.mergingEnabled && profile.signedDistance <= profile.glowLimit + profile.glowSoftness;
                     if (active || mergeFieldActive) {
                        profileActive = true;
                        if (active) {
                           float ringDistance = Math.abs(profile.signedDistance);
                           core += (1.0F - smoothstep(profile.coreHalfWidth, profile.coreHalfWidth + profile.coreSoftness, ringDistance))
                              * pulse.coreAlpha
                              * profile.brightnessFactor;
                           glow += (1.0F - smoothstep(profile.glowLimit, profile.glowLimit + profile.glowSoftness, ringDistance))
                              * pulse.glowAlpha
                              * profile.brightnessFactor;
                        }
                     }
                  }
               }

               core *= 0.25F;
               glow *= 0.25F;
               if (profileActive && (!(core <= 1.0E-4F) || !(glow <= 1.0E-4F) || pulse.mergingEnabled)) {
                  int index = y * frameSize + x;
                  frame.core[index] = core;
                  frame.glow[index] = glow;
                  frame.signedDistance[index] = profile.signedDistance;
                  frame.coreHalfWidth[index] = profile.coreHalfWidth;
                  frame.glowLimit[index] = profile.glowLimit;
                  frame.coreSoftness[index] = profile.coreSoftness;
                  frame.glowSoftness[index] = profile.glowSoftness;
                  frame.brightnessFactor[index] = profile.brightnessFactor;
                  frame.activeIndices[frame.activeCount++] = index;
               }
            }
         }

         variantFrames[safeFrame] = frame;
         perfLog(perf, perfStart, "variant=" + safeVariant + " frame=" + safeFrame + " frameSize=" + frameSize + " active=" + frame.activeCount);
         return frame;
      }
   }

   private static void buildAdditionalPulseStampFrames(NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse, int budget) {
      if (pulseStampFrames.length == 4) {
         int totalFrames = 192;
         int remaining = Math.max(0, budget);

         while (remaining > 0 && pulseStampBuildCursor < totalFrames) {
            int cursor = pulseStampBuildCursor++;
            int variantIndex = cursor / 48;
            int frameIndex = cursor % 48;
            NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame[] variantFrames = pulseStampFrames[variantIndex];
            if (variantFrames == null || variantFrames[frameIndex] == null) {
               buildPulseStampFrame(variantIndex, frameIndex, pulse);
               remaining--;
            }
         }
      }
   }

   private static boolean pulseStampFramesComplete() {
      if (pulseStampFrames.length != 4) {
         return false;
      } else {
         for (int variantIndex = 0; variantIndex < 4; variantIndex++) {
            NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame[] variantFrames = pulseStampFrames[variantIndex];
            if (variantFrames == null || variantFrames.length != 48) {
               return false;
            }

            for (int frameIndex = 0; frameIndex < 48; frameIndex++) {
               if (variantFrames[frameIndex] == null) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   private static String buildPulseStampSignature(NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse) {
      return "pulseTarget="
         + pulseTargetWidth
         + "x"
         + pulseTargetHeight
         + "|electric="
         + pulse.electric
         + "|startRadius="
         + pulse.startRadiusPx
         + "|endRadius="
         + pulse.endRadiusPx
         + "|coreWidth="
         + pulse.coreWidthPx
         + "|glowRadius="
         + pulse.glowRadiusPx
         + "|coreSoftness="
         + pulse.coreSoftnessPx
         + "|glowSoftness="
         + pulse.glowSoftnessPx
         + "|coreAlpha="
         + pulse.coreAlpha
         + "|glowAlpha="
         + pulse.glowAlpha
         + "|brightness="
         + pulse.brightness
         + "|fadePower="
         + pulse.fadePower
         + "|distortionPx="
         + pulse.distortionPx
         + "|distortionFrequency="
         + pulse.distortionFrequency
         + "|energyVariation="
         + pulse.energyVariation
         + "|widthVariation="
         + pulse.widthVariation
         + "|brightnessVariation="
         + pulse.brightnessVariation
         + "|edgeRoughness="
         + pulse.edgeRoughness
         + "|rotationRandomness="
         + pulse.patternRotationRandomness
         + "|mergeModel=sdf_union_v3|merging="
         + pulse.mergingEnabled
         + "|mergeStrength="
         + pulse.mergeStrength
         + "|mergeSoftness="
         + pulse.mergeSoftnessPx
         + "|mergeBrightness="
         + pulse.mergeBrightnessBoost;
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame pulseStampFrame(int variantIndex, int frameIndex) {
      if (pulseStampFrames.length == 0) {
         return null;
      } else {
         int safeVariant = clampInt(variantIndex, 0, pulseStampFrames.length - 1);
         NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame[] variantFrames = pulseStampFrames[safeVariant];
         if (variantFrames != null && variantFrames.length != 0) {
            int safeFrame = clampInt(frameIndex, 0, variantFrames.length - 1);
            return variantFrames[safeFrame];
         } else {
            return null;
         }
      }
   }

   private static void closePulseStampFrames() {
      pulseStampFrames = new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame[0][];
      pulseStampSignature = "";
      pulseStampBuildCursor = 0;
      pulseStampFrameSize = 0;
      pulseStampFrameCenter = 0.0F;
   }

   private static void stampPulseFrame(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state,
      NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse,
      NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame frame,
      float centerX,
      float centerY
   ) {
      int startX = (int)Math.floor(centerX - frame.centerX);
      int startY = (int)Math.floor(centerY - frame.centerY);

      for (int activeIndex = 0; activeIndex < frame.activeCount; activeIndex++) {
         int localIndex = frame.activeIndices[activeIndex];
         int localX = localIndex % frame.width;
         int localY = localIndex / frame.width;
         int targetY = startY + localY;
         if (targetY >= 0 && targetY < state.height) {
            int targetX = startX + localX;
            if (targetX >= 0 && targetX < state.width) {
               float core = frame.core[localIndex];
               float glow = frame.glow[localIndex];
               if (!(core <= 1.0E-4F) || !(glow <= 1.0E-4F) || pulse.mergingEnabled) {
                  int targetIndex = targetY * state.width + targetX;
                  if (!(pulseSampledSourceAlpha[targetIndex] < 0.0039215684F)) {
                     if (pulse.mergingEnabled) {
                        writeMergedPulseProfile(state, targetIndex, frame, localIndex);
                     } else {
                        writeFinalPulseContribution(state, targetIndex, core, glow, false);
                     }
                  }
               }
            }
         }
      }
   }

   private static void clearPreviousPulsePixels(NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state) {
      for (int i = 0; i < state.previousPulsePixelCount; i++) {
         int index = state.previousPulsePixelIndices[i];
         state.pixels.setPixelRGBA(index % state.width, index / state.width, 0);
         if (state.secondaryPixels != null) {
            state.secondaryPixels.setPixelRGBA(index % state.width, index / state.width, 0);
         }
      }

      state.previousPulsePixelCount = 0;
   }

   private static void beginPulseFrame(NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state) {
      for (int i = 0; i < state.finalTouchedCount; i++) {
         int index = state.finalTouchedIndices[i];
         state.coreBuffer[index] = 0.0F;
         state.glowBuffer[index] = 0.0F;
      }

      state.finalTouchedCount = 0;
      state.hasVisiblePulseContent = false;
      state.frameStamp++;
      if (state.frameStamp == Integer.MAX_VALUE) {
         for (int i = 0; i < state.finalStamp.length; i++) {
            state.finalStamp[i] = 0;
         }

         state.frameStamp = 1;
      }
   }

   private static void beginPulseWave(NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state) {
      for (int i = 0; i < state.waveTouchedCount; i++) {
         int index = state.waveTouchedIndices[i];
         state.mergeOverlapCount[index] = 0;
         state.unionDistance[index] = 0.0F;
         state.unionCoreHalfWidth[index] = 0.0F;
         state.unionGlowLimit[index] = 0.0F;
         state.unionCoreSoftness[index] = 0.0F;
         state.unionGlowSoftness[index] = 0.0F;
         state.unionBrightnessFactor[index] = 0.0F;
      }

      state.waveTouchedCount = 0;
      state.waveFrameStamp++;
      if (state.waveFrameStamp == Integer.MAX_VALUE) {
         for (int i = 0; i < state.waveStamp.length; i++) {
            state.waveStamp[i] = 0;
         }

         state.waveFrameStamp = 1;
      }
   }

   private static boolean hasVisiblePulseContent(NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state) {
      return state.hasVisiblePulseContent;
   }

   private static void writeFinalPulseContribution(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state, int index, float core, float glow, boolean mergingEnabled
   ) {
      if (state.finalStamp[index] != state.frameStamp) {
         state.finalStamp[index] = state.frameStamp;
         state.finalTouchedIndices[state.finalTouchedCount++] = index;
         state.coreBuffer[index] = 0.0F;
         state.glowBuffer[index] = 0.0F;
         state.mergeOverlapCount[index] = 1;
      } else if (mergingEnabled && (core > 1.0E-4F || glow > 1.0E-4F)) {
         state.mergeOverlapCount[index]++;
      }

      state.coreBuffer[index] = max(state.coreBuffer[index], core);
      state.glowBuffer[index] = max(state.glowBuffer[index], glow);
   }

   private static void writeMergedPulseProfile(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state,
      int targetIndex,
      NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame frame,
      int localIndex
   ) {
      float signedDistance = frame.signedDistance[localIndex];
      if (state.waveStamp[targetIndex] != state.waveFrameStamp) {
         state.waveStamp[targetIndex] = state.waveFrameStamp;
         state.waveTouchedIndices[state.waveTouchedCount++] = targetIndex;
         state.mergeOverlapCount[targetIndex] = 1;
         copyUnionProfile(state, targetIndex, frame, localIndex);
      } else {
         state.mergeOverlapCount[targetIndex]++;
         if (signedDistance < state.unionDistance[targetIndex]) {
            copyUnionProfile(state, targetIndex, frame, localIndex);
         }
      }
   }

   private static void finalizeMergedPulseWave(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state, NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse
   ) {
      for (int i = 0; i < state.waveTouchedCount; i++) {
         int index = state.waveTouchedIndices[i];
         if (state.mergeOverlapCount[index] > 0) {
            float ringDistance = Math.abs(state.unionDistance[index]);
            int overlapCount = state.mergeOverlapCount[index];
            float overlapFactor = clamp01((overlapCount - 1) / 2.0F);
            float brightnessFactor = state.unionBrightnessFactor[index] * (1.0F + pulse.mergeBrightnessBoost * overlapFactor);
            float core = 1.0F - smoothstep(state.unionCoreHalfWidth[index], state.unionCoreHalfWidth[index] + state.unionCoreSoftness[index], ringDistance);
            float glow = 1.0F - smoothstep(state.unionGlowLimit[index], state.unionGlowLimit[index] + state.unionGlowSoftness[index], ringDistance);
            if (overlapCount > 1 && pulse.mergeStrength > 0.0F) {
               glow = min(1.0F, glow * (1.0F + pulse.mergeStrength * overlapFactor));
            }

            writeFinalPulseContribution(
               state, index, clamp01(core * pulse.coreAlpha * brightnessFactor), clamp01(glow * pulse.glowAlpha * brightnessFactor), false
            );
         }
      }
   }

   private static void copyUnionProfile(
      NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state,
      int targetIndex,
      NanoGeoArmorModeDynamicTextureCompatRenderer.PulseStampFrame frame,
      int localIndex
   ) {
      state.unionDistance[targetIndex] = frame.signedDistance[localIndex];
      state.unionCoreHalfWidth[targetIndex] = frame.coreHalfWidth[localIndex];
      state.unionGlowLimit[targetIndex] = frame.glowLimit[localIndex];
      state.unionCoreSoftness[targetIndex] = frame.coreSoftness[localIndex];
      state.unionGlowSoftness[targetIndex] = frame.glowSoftness[localIndex];
      state.unionBrightnessFactor[targetIndex] = frame.brightnessFactor[localIndex];
   }

   private static boolean simpleRingProfile(
      float dx,
      float dy,
      float radius,
      float fade,
      float screenFootprint,
      NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse,
      NanoGeoArmorModeDynamicTextureCompatRenderer.PulseProfile out
   ) {
      float authoredCoreHalfWidth = max(pulse.coreWidthPx, 1.0E-4F) * 0.5F;
      float authoredGlowLimit = authoredCoreHalfWidth + max(pulse.glowRadiusPx, 0.0F);
      float authoredCoreSoftness = max(pulse.coreSoftnessPx, 1.0E-4F);
      float authoredGlowSoftness = max(pulse.glowSoftnessPx, 1.0E-4F);
      float signedDistance = (float)Math.sqrt(dx * dx + dy * dy) - radius;
      float minVisibleCoreHalfWidth = screenFootprint * 0.18F;
      float minVisibleGlowLimit = screenFootprint * 0.35F;
      out.signedDistance = signedDistance;
      out.coreHalfWidth = max(authoredCoreHalfWidth, minVisibleCoreHalfWidth);
      out.glowLimit = max(authoredGlowLimit, out.coreHalfWidth + minVisibleGlowLimit);
      out.coreSoftness = max(authoredCoreSoftness, screenFootprint * 0.25F);
      out.glowSoftness = max(authoredGlowSoftness, screenFootprint * 0.4F);
      out.brightnessFactor = fade * max(pulse.brightness, 0.0F);
      return Math.abs(signedDistance) <= out.glowLimit + out.glowSoftness;
   }

   private static boolean electricRingProfile(
      float dx,
      float dy,
      float radius,
      float fade,
      float angle,
      float centerSeed,
      float screenFootprint,
      NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse,
      NanoGeoArmorModeDynamicTextureCompatRenderer.PulseProfile out
   ) {
      float authoredCoreHalfWidth = max(pulse.coreWidthPx, 1.0E-4F) * 0.5F;
      float authoredGlowLimit = authoredCoreHalfWidth + max(pulse.glowRadiusPx, 0.0F);
      float authoredCoreSoftness = max(pulse.coreSoftnessPx, 1.0E-4F);
      float authoredGlowSoftness = max(pulse.glowSoftnessPx, 1.0E-4F);
      float ringRotation = fract((float)Math.sin(centerSeed) * 43758.547F) * (float) (Math.PI * 2);
      float profileAngle = angle + ringRotation * pulse.patternRotationRandomness;
      float frequencyControl = max(pulse.distortionFrequency, 0.0F);
      float radiusNoise = pulsePeriodicElectricNoise(profileAngle, centerSeed, frequencyControl);
      float widthNoise = pulsePeriodicElectricNoise01(profileAngle + 1.7F, centerSeed + 3.1F, frequencyControl);
      float brightnessNoise = pulsePeriodicElectricNoise01(profileAngle + 3.1F, centerSeed + 5.7F, frequencyControl);
      float edgeNoise = pulsePeriodicElectricNoise01(profileAngle + 5.3F, centerSeed + 7.9F, frequencyControl);
      float shapeStability = pulseShapeStability(radius, max(pulse.distortionPx, 0.0F), pulse.coreWidthPx, pulse.glowRadiusPx);
      float distortionStrength = lerp(0.1F, 1.0F, shapeStability);
      float widthVariationStrength = pulse.widthVariation * lerp(0.25F, 1.0F, shapeStability);
      float brightnessVariationStrength = pulse.brightnessVariation * lerp(0.55F, 1.0F, shapeStability);
      float energyVariationStrength = pulse.energyVariation * lerp(0.6F, 1.0F, shapeStability);
      float roughness = pulse.edgeRoughness * lerp(0.2F, 1.0F, shapeStability);
      float distortion = radiusNoise * max(pulse.distortionPx, 0.0F) * distortionStrength;
      float distortedRadius = radius + distortion;
      float signedDistance = (float)Math.sqrt(dx * dx + dy * dy) - distortedRadius;
      float localWidthMul = lerp(1.0F - widthVariationStrength * 0.45F, 1.0F + widthVariationStrength * 0.85F, widthNoise);
      localWidthMul = max(localWidthMul, 0.55F);
      float localBrightness = lerp(1.0F - brightnessVariationStrength, 1.0F + brightnessVariationStrength, brightnessNoise);
      localBrightness = max(localBrightness, 0.35F);
      float energyNoise = pulsePeriodicElectricNoise01(profileAngle + 7.1F, centerSeed + 11.3F, frequencyControl);
      float localEnergy = lerp(1.0F - energyVariationStrength * 0.45F, 1.0F + energyVariationStrength * 0.55F, energyNoise);
      localEnergy = max(localEnergy, 0.55F);
      float minVisibleCoreHalfWidth = screenFootprint * 0.18F;
      float minVisibleGlowLimit = screenFootprint * 0.35F;
      float authoredCoreHalfWidthLocal = authoredCoreHalfWidth * localWidthMul;
      float authoredGlowLimitLocal = authoredGlowLimit * localWidthMul;
      out.signedDistance = signedDistance;
      out.coreHalfWidth = max(authoredCoreHalfWidthLocal, minVisibleCoreHalfWidth);
      out.glowLimit = max(authoredGlowLimitLocal, out.coreHalfWidth + minVisibleGlowLimit);
      float edgeMix = edgeNoise * roughness + (1.0F - roughness) * 0.5F;
      out.coreSoftness = max(authoredCoreSoftness * lerp(0.75F, 1.5F, edgeMix), screenFootprint * 0.25F);
      out.glowSoftness = max(authoredGlowSoftness * lerp(0.8F, 1.8F, edgeMix), screenFootprint * 0.4F);
      out.brightnessFactor = fade * max(pulse.brightness, 0.0F) * localBrightness * localEnergy;
      return Math.abs(out.signedDistance) <= out.glowLimit + out.glowSoftness;
   }

   private static float pulseMaxReach(NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse, float radius) {
      float authoredCoreHalfWidth = max(pulse.coreWidthPx, 1.0E-4F) * 0.5F;
      float authoredGlowLimit = authoredCoreHalfWidth + max(pulse.glowRadiusPx, 0.0F);
      float authoredGlowSoftness = max(pulse.glowSoftnessPx, 1.0E-4F);
      float distortion = pulse.electric ? max(pulse.distortionPx, 0.0F) : 0.0F;
      float widthVariation = pulse.electric ? max(pulse.widthVariation, 0.0F) * authoredGlowLimit : 0.0F;
      return radius + distortion + authoredGlowLimit + authoredGlowSoftness + widthVariation + 3.0F;
   }

   private static boolean compatPulsesVisible(float timeSeconds) {
      if (!NanoGeoClientConfig.armorModePulseEnabled() && !NanoGeoClientConfig.armorModePulseDebugForceVisible()) {
         return false;
      } else if (NanoGeoArmorModePulseCenters.INSTANCE.centerCount() <= 0) {
         return false;
      } else {
         float intervalSeconds = NanoGeoClientConfig.armorModePulseDebugForceVisible()
            ? 1.0F
            : max(NanoGeoClientConfig.armorModePulseIntervalSeconds(), 1.0E-4F);
         float durationSeconds = NanoGeoClientConfig.armorModePulseDebugForceVisible()
            ? 2.0F
            : max(NanoGeoClientConfig.armorModePulseDurationSeconds(), 1.0E-4F);
         float baseGeneration = (float)Math.floor(timeSeconds / intervalSeconds);

         for (int waveIndex = 0; waveIndex < 3; waveIndex++) {
            float generation = baseGeneration - waveIndex;
            float triggerTime = generation * intervalSeconds;
            if (!(triggerTime < 0.0F)) {
               float age = timeSeconds - triggerTime;
               if (age >= 0.0F && age <= durationSeconds) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private static boolean isTransitionActive(NanosuitArmorModeOverlayState.OverlayRenderState overlayState) {
      if (overlayState != null && overlayState.enabled()) {
         float progress = clamp01(overlayState.transitionProgress());
         return overlayState.armorModeActive() ? progress < 0.9999F : progress > 1.0E-4F;
      } else {
         return false;
      }
   }

   private static int resolveBaseUpdateStep(long nowMillis) {
      int baseUpdateIntervalMillis = Math.max(16, NanoGeoClientConfig.armorModeDynamicTextureCompatUpdateIntervalMillis());
      return (int)(nowMillis / baseUpdateIntervalMillis);
   }

   private static float patchLoopDurationSeconds() {
      return max(0.75F, 4.0F / max(NanoGeoClientConfig.armorModePatchFrequency(), 0.1F));
   }

   private static float transitionMaskForDelayByte(int delayByte, float progress, float fadeWindow) {
      float delay = (delayByte & 0xFF) / 255.0F;
      return smoothstep(delay, delay + fadeWindow, clamp01(progress));
   }

   public static float samplePatchLayerPreviewMask(
      float pixelX,
      float pixelY,
      float timeSeconds,
      float blockSizePx,
      float speed,
      float seed,
      float threshold,
      NanoGeoClientConfig.ArmorModePatchFlowVariant variant
   ) {
      return patchLayerActivity(pixelX, pixelY, blockSizePx, speed, seed, threshold, timeSeconds, variant);
   }

   private static float chaoticPatchSpotMask(float pixelX, float pixelY, float timeSeconds, NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch) {
      float midSize = lerp(patch.minSizePx, patch.maxSizePx, 0.5F);
      float threshold = 1.0F - patch.density;
      float layerA = patchLayerActivity(pixelX, pixelY, patch.minSizePx, patch.frequency * 1.18F, 19.0F, threshold, timeSeconds, patch.variant);
      float layerB = patchLayerActivity(pixelX, pixelY, midSize, patch.frequency * 0.97F, 43.0F, threshold, timeSeconds, patch.variant);
      float layerC = patchLayerActivity(pixelX, pixelY, patch.maxSizePx, patch.frequency * 0.78F, 71.0F, threshold, timeSeconds, patch.variant);
      return clamp01(max(layerC, max(layerB, layerA)));
   }

   private static float chaoticPatchSpotMaskLooped(
      float pixelX, float pixelY, float framePhase, NanoGeoArmorModeDynamicTextureCompatRenderer.PatchSnapshot patch
   ) {
      float midSize = lerp(patch.minSizePx, patch.maxSizePx, 0.5F);
      float threshold = 1.0F - patch.density;
      float layerA = patchLayerActivityLooped(pixelX, pixelY, patch.minSizePx, 19.0F, threshold, framePhase, 5, patch.variant);
      float layerB = patchLayerActivityLooped(pixelX, pixelY, midSize, 43.0F, threshold, framePhase, 4, patch.variant);
      float layerC = patchLayerActivityLooped(pixelX, pixelY, patch.maxSizePx, 71.0F, threshold, framePhase, 3, patch.variant);
      return clamp01(max(layerC, max(layerB, layerA)));
   }

   private static float patchLayerActivity(
      float pixelX,
      float pixelY,
      float blockSizePx,
      float speed,
      float seed,
      float threshold,
      float timeSeconds,
      NanoGeoClientConfig.ArmorModePatchFlowVariant variant
   ) {
      return switch (variant) {
         case DYNAMIC_RESEED -> patchLayerActivityDynamic(pixelX, pixelY, blockSizePx, speed, seed, threshold, timeSeconds);
         case FIXED_ARCHETYPE_LOOP -> patchLayerActivityFixedArchetype(pixelX, pixelY, blockSizePx, speed, seed, threshold, timeSeconds);
         case SOFT_CLOUD_ARCHETYPES -> patchLayerActivitySoftCloud(pixelX, pixelY, blockSizePx, speed, seed, threshold, timeSeconds);
      };
   }

   private static float patchLayerActivityLooped(
      float pixelX,
      float pixelY,
      float blockSizePx,
      float seed,
      float threshold,
      float framePhase,
      int loopCycleCount,
      NanoGeoClientConfig.ArmorModePatchFlowVariant variant
   ) {
      return switch (variant) {
         case DYNAMIC_RESEED -> patchLayerActivityDynamicLooped(pixelX, pixelY, blockSizePx, seed, threshold, framePhase, loopCycleCount);
         case FIXED_ARCHETYPE_LOOP -> patchLayerActivityFixedArchetypeLooped(pixelX, pixelY, blockSizePx, seed, threshold, framePhase, loopCycleCount);
         case SOFT_CLOUD_ARCHETYPES -> patchLayerActivitySoftCloudLooped(pixelX, pixelY, blockSizePx, seed, threshold, framePhase, loopCycleCount);
      };
   }

   private static float patchLayerActivityDynamic(float pixelX, float pixelY, float blockSizePx, float speed, float seed, float threshold, float timeSeconds) {
      float shiftedX = pixelX + seed * 0.37F;
      float shiftedY = pixelY + seed * 0.19F;
      float cellX = (float)Math.floor(shiftedX / blockSizePx);
      float cellY = (float)Math.floor(shiftedY / blockSizePx);
      float phaseOffset = hash21(cellX + seed * 1.31F, cellY + seed * 2.17F) * 4.0F;
      float timeline = timeSeconds * speed + phaseOffset;
      float cycleIndex = (float)Math.floor(timeline);
      float cyclePhase = fract(timeline);
      float gate = hash21(cellX + seed + cycleIndex * 0.6180339F, cellY + seed * 0.73F + cycleIndex * 0.113F) >= threshold ? 1.0F : 0.0F;
      float strength = lerp(0.72F, 1.0F, hash21(cellX + seed * 3.11F, cellY + cycleIndex + 17.0F));
      return gate * patchCycleEnvelope(cyclePhase) * strength;
   }

   private static float patchLayerActivityFixedArchetype(
      float pixelX, float pixelY, float blockSizePx, float speed, float seed, float threshold, float timeSeconds
   ) {
      float shiftedX = pixelX + seed * 0.37F;
      float shiftedY = pixelY + seed * 0.19F;
      float cellX = (float)Math.floor(shiftedX / blockSizePx);
      float cellY = (float)Math.floor(shiftedY / blockSizePx);
      float archetypeIndex = (float)Math.floor(hash21(cellX + seed * 2.47F, cellY + seed * 0.61F) * 10.0F);
      float archetypeNormalized = (archetypeIndex + 0.5F) / 10.0F;
      float phaseOffset = hash21(cellX + seed * 1.31F, cellY + seed * 2.17F);
      float timeline = timeSeconds * speed + phaseOffset;
      float cycleIndex = (float)Math.floor(timeline);
      float cyclePhase = fract(timeline);
      float density = clamp01(1.0F - threshold);
      int activeCycleCount = clampInt((int)Math.floor(density * 10.0F + 0.5F), 0, 10);
      if (activeCycleCount <= 0) {
         return 0.0F;
      } else {
         int cycleSlot = (int)mod(cycleIndex + archetypeIndex, 10.0F);
         float cycleMask = cycleSlot < activeCycleCount ? 1.0F : 0.0F;
         float strength = lerp(0.72F, 1.0F, archetypeNormalized);
         return patchCycleEnvelope(cyclePhase) * cycleMask * strength;
      }
   }

   private static float patchLayerActivityDynamicLooped(
      float pixelX, float pixelY, float blockSizePx, float seed, float threshold, float framePhase, int loopCycleCount
   ) {
      float shiftedX = pixelX + seed * 0.37F;
      float shiftedY = pixelY + seed * 0.19F;
      float cellX = (float)Math.floor(shiftedX / blockSizePx);
      float cellY = (float)Math.floor(shiftedY / blockSizePx);
      float phaseOffset = hash21(cellX + seed * 1.31F, cellY + seed * 2.17F);
      float timeline = framePhase * loopCycleCount + phaseOffset;
      int cycleSlot = clampInt((int)Math.floor(mod(timeline, loopCycleCount)), 0, Math.max(loopCycleCount - 1, 0));
      float cyclePhase = fract(timeline);
      float gate = hash21(cellX + seed + cycleSlot * 0.6180339F, cellY + seed * 0.73F + cycleSlot * 0.113F) >= threshold ? 1.0F : 0.0F;
      float strength = lerp(0.72F, 1.0F, hash21(cellX + seed * 3.11F, cellY + cycleSlot + 17.0F));
      return gate * patchCycleEnvelope(cyclePhase) * strength;
   }

   private static float patchLayerActivityFixedArchetypeLooped(
      float pixelX, float pixelY, float blockSizePx, float seed, float threshold, float framePhase, int loopCycleCount
   ) {
      float shiftedX = pixelX + seed * 0.37F;
      float shiftedY = pixelY + seed * 0.19F;
      float cellX = (float)Math.floor(shiftedX / blockSizePx);
      float cellY = (float)Math.floor(shiftedY / blockSizePx);
      float archetypeIndex = (float)Math.floor(hash21(cellX + seed * 2.47F, cellY + seed * 0.61F) * 10.0F);
      float archetypeNormalized = (archetypeIndex + 0.5F) / 10.0F;
      float phaseOffset = hash21(cellX + seed * 1.31F, cellY + seed * 2.17F);
      float timeline = framePhase * loopCycleCount + phaseOffset;
      int localCycleIndex = clampInt((int)Math.floor(mod(timeline, loopCycleCount)), 0, Math.max(loopCycleCount - 1, 0));
      float cyclePhase = fract(timeline);
      float density = clamp01(1.0F - threshold);
      int activeCycleCount = clampInt((int)Math.floor(density * 10.0F + 0.5F), 0, 10);
      if (activeCycleCount <= 0) {
         return 0.0F;
      } else {
         int cycleSlot = (localCycleIndex + (int)archetypeIndex) % 10;
         float cycleMask = cycleSlot < activeCycleCount ? 1.0F : 0.0F;
         float strength = lerp(0.72F, 1.0F, archetypeNormalized);
         return patchCycleEnvelope(cyclePhase) * cycleMask * strength;
      }
   }

   private static float patchLayerActivitySoftCloud(float pixelX, float pixelY, float blockSizePx, float speed, float seed, float threshold, float timeSeconds) {
      float safeBlockSize = max(blockSizePx, 1.0E-4F);
      float shiftedX = pixelX + seed * 0.37F;
      float shiftedY = pixelY + seed * 0.19F;
      float blockCoordX = shiftedX / safeBlockSize;
      float blockCoordY = shiftedY / safeBlockSize;
      float cellX = (float)Math.floor(blockCoordX);
      float cellY = (float)Math.floor(blockCoordY);
      float localX = fract(blockCoordX);
      float localY = fract(blockCoordY);
      float phaseOffset = hash21(cellX + seed * 1.31F, cellY + seed * 2.17F) * 4.0F;
      float timeline = timeSeconds * speed + phaseOffset;
      float cycleIndex = (float)Math.floor(timeline);
      float cyclePhase = fract(timeline);
      float gate = hash21(cellX + seed + cycleIndex * 0.6180339F, cellY + seed * 0.73F + cycleIndex * 0.113F) >= threshold ? 1.0F : 0.0F;
      if (gate <= 0.0F) {
         return 0.0F;
      } else {
         float strength = lerp(0.78F, 1.0F, hash21(cellX + seed * 3.11F, cellY + cycleIndex + 17.0F));
         float shape = cloudPatchShapeAlpha(localX, localY, blockSizePx, cellX, cellY, seed);
         return gate * patchCycleEnvelope(cyclePhase) * strength * shape;
      }
   }

   private static float patchLayerActivitySoftCloudLooped(
      float pixelX, float pixelY, float blockSizePx, float seed, float threshold, float framePhase, int loopCycleCount
   ) {
      float safeBlockSize = max(blockSizePx, 1.0E-4F);
      float shiftedX = pixelX + seed * 0.37F;
      float shiftedY = pixelY + seed * 0.19F;
      float blockCoordX = shiftedX / safeBlockSize;
      float blockCoordY = shiftedY / safeBlockSize;
      float cellX = (float)Math.floor(blockCoordX);
      float cellY = (float)Math.floor(blockCoordY);
      float localX = fract(blockCoordX);
      float localY = fract(blockCoordY);
      float phaseOffset = hash21(cellX + seed * 1.31F, cellY + seed * 2.17F);
      float timeline = framePhase * loopCycleCount + phaseOffset;
      int cycleSlot = clampInt((int)Math.floor(mod(timeline, loopCycleCount)), 0, Math.max(loopCycleCount - 1, 0));
      float cyclePhase = fract(timeline);
      float gate = hash21(cellX + seed + cycleSlot * 0.6180339F, cellY + seed * 0.73F + cycleSlot * 0.113F) >= threshold ? 1.0F : 0.0F;
      if (gate <= 0.0F) {
         return 0.0F;
      } else {
         float strength = lerp(0.78F, 1.0F, hash21(cellX + seed * 3.11F, cellY + cycleSlot + 17.0F));
         float shape = cloudPatchShapeAlpha(localX, localY, blockSizePx, cellX, cellY, seed);
         return gate * patchCycleEnvelope(cyclePhase) * strength * shape;
      }
   }

   private static float cloudPatchShapeAlpha(float localX, float localY, float blockSizePx, float cellX, float cellY, float seed) {
      int archetypeIndex = cloudPatchArchetypeIndex(cellX, cellY, seed);
      NanoGeoArmorModeDynamicTextureCompatRenderer.CloudPatchArchetype archetype = CLOUD_PATCH_ARCHETYPES[archetypeIndex];
      float contentScale = cloudPatchContentScale(blockSizePx);
      float edgeSoftnessMultiplier = max(NanoGeoClientConfig.armorModePatchCloudEdgeSoftness(), 0.01F);
      localX = (localX - 0.5F) / contentScale + 0.5F;
      localY = (localY - 0.5F) / contentScale + 0.5F;
      float transformHash = hash21(cellX + seed * 5.13F, cellY + seed * 1.79F);
      int transformBits = clampInt((int)Math.floor(transformHash * 8.0F), 0, 7);
      if ((transformBits & 1) != 0) {
         localX = 1.0F - localX;
      }

      if ((transformBits & 2) != 0) {
         localY = 1.0F - localY;
      }

      if ((transformBits & 4) != 0) {
         float swap = localX;
         localX = localY;
         localY = swap;
      }

      float alpha = 0.0F;

      for (int i = 0; i < archetype.centersX().length; i++) {
         float dx = (localX - archetype.centersX()[i]) / archetype.stretchX()[i];
         float dy = (localY - archetype.centersY()[i]) / archetype.stretchY()[i];
         float distance = (float)Math.sqrt(dx * dx + dy * dy);
         float edgeSoftness = archetype.edgeSoftness() * edgeSoftnessMultiplier;
         float lobe = 1.0F - smoothstep(archetype.radii()[i] - edgeSoftness, archetype.radii()[i] + edgeSoftness, distance);
         alpha = max(alpha, lobe);
      }

      return smoothstep(0.04F, 0.96F, clamp01(alpha));
   }

   private static float cloudPatchContentScale(float blockSizePx) {
      return clamp(0.2F + max(blockSizePx, 0.0F) * 0.12F, 0.36F, 1.05F);
   }

   private static int cloudPatchArchetypeIndex(float cellX, float cellY, float seed) {
      int count = CLOUD_PATCH_ARCHETYPES.length;
      if (count <= 1) {
         return 0;
      } else {
         float archetypeNoise = hash21(cellX + seed * 2.47F, cellY + seed * 0.61F);
         return clampInt((int)Math.floor(archetypeNoise * count), 0, count - 1);
      }
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.CloudPatchArchetype[] createCloudPatchArchetypes() {
      return new NanoGeoArmorModeDynamicTextureCompatRenderer.CloudPatchArchetype[]{
         cloudPatch(
            0.16F,
            new float[]{0.28F, 0.45F, 0.63F, 0.52F},
            new float[]{0.54F, 0.34F, 0.56F, 0.72F},
            new float[]{0.2F, 0.21F, 0.18F, 0.17F},
            new float[]{1.1F, 1.0F, 1.08F, 0.96F},
            new float[]{0.92F, 1.12F, 0.95F, 1.08F}
         ),
         cloudPatch(
            0.15F,
            new float[]{0.24F, 0.4F, 0.58F, 0.74F},
            new float[]{0.46F, 0.3F, 0.5F, 0.62F},
            new float[]{0.16F, 0.2F, 0.22F, 0.17F},
            new float[]{1.18F, 0.94F, 1.05F, 1.12F},
            new float[]{0.86F, 1.16F, 0.96F, 0.92F}
         ),
         cloudPatch(
            0.14F,
            new float[]{0.34F, 0.54F, 0.71F, 0.5F, 0.25F},
            new float[]{0.27F, 0.38F, 0.56F, 0.68F, 0.59F},
            new float[]{0.17F, 0.21F, 0.17F, 0.18F, 0.14F},
            new float[]{0.9F, 1.08F, 1.14F, 1.0F, 1.06F},
            new float[]{1.12F, 0.94F, 0.88F, 1.08F, 0.96F}
         ),
         cloudPatch(
            0.15F,
            new float[]{0.22F, 0.36F, 0.55F, 0.68F, 0.54F},
            new float[]{0.58F, 0.36F, 0.27F, 0.44F, 0.7F},
            new float[]{0.18F, 0.16F, 0.18F, 0.17F, 0.19F},
            new float[]{1.2F, 0.92F, 1.12F, 0.96F, 1.08F},
            new float[]{0.88F, 1.18F, 0.92F, 1.1F, 0.9F}
         ),
         cloudPatch(
            0.16F,
            new float[]{0.3F, 0.47F, 0.62F, 0.79F},
            new float[]{0.34F, 0.48F, 0.28F, 0.52F},
            new float[]{0.18F, 0.23F, 0.16F, 0.15F},
            new float[]{1.02F, 1.18F, 0.88F, 1.12F},
            new float[]{1.08F, 0.9F, 1.18F, 0.94F}
         ),
         cloudPatch(
            0.15F,
            new float[]{0.18F, 0.34F, 0.5F, 0.66F, 0.79F},
            new float[]{0.44F, 0.28F, 0.42F, 0.63F, 0.46F},
            new float[]{0.14F, 0.17F, 0.19F, 0.18F, 0.13F},
            new float[]{1.1F, 0.95F, 1.05F, 1.12F, 0.92F},
            new float[]{0.92F, 1.16F, 0.98F, 0.9F, 1.14F}
         ),
         cloudPatch(
            0.14F,
            new float[]{0.29F, 0.47F, 0.59F, 0.72F, 0.45F},
            new float[]{0.26F, 0.25F, 0.44F, 0.6F, 0.67F},
            new float[]{0.16F, 0.16F, 0.18F, 0.16F, 0.18F},
            new float[]{1.16F, 0.9F, 1.08F, 1.04F, 0.96F},
            new float[]{0.88F, 1.22F, 0.94F, 1.08F, 1.02F}
         ),
         cloudPatch(
            0.15F,
            new float[]{0.23F, 0.39F, 0.58F, 0.71F},
            new float[]{0.33F, 0.55F, 0.4F, 0.61F},
            new float[]{0.17F, 0.22F, 0.19F, 0.16F},
            new float[]{1.08F, 0.92F, 1.16F, 1.0F},
            new float[]{0.94F, 1.12F, 0.88F, 1.1F}
         ),
         cloudPatch(
            0.16F,
            new float[]{0.27F, 0.42F, 0.56F, 0.69F, 0.54F},
            new float[]{0.6F, 0.39F, 0.24F, 0.41F, 0.71F},
            new float[]{0.19F, 0.18F, 0.15F, 0.17F, 0.18F},
            new float[]{1.14F, 0.96F, 1.06F, 1.1F, 0.92F},
            new float[]{0.9F, 1.14F, 1.2F, 0.96F, 1.04F}
         ),
         cloudPatch(
            0.15F,
            new float[]{0.21F, 0.37F, 0.52F, 0.66F, 0.81F},
            new float[]{0.53F, 0.32F, 0.47F, 0.29F, 0.48F},
            new float[]{0.15F, 0.18F, 0.21F, 0.16F, 0.13F},
            new float[]{1.12F, 0.94F, 1.02F, 1.18F, 0.9F},
            new float[]{0.9F, 1.18F, 0.96F, 0.86F, 1.1F}
         )
      };
   }

   private static NanoGeoArmorModeDynamicTextureCompatRenderer.CloudPatchArchetype cloudPatch(
      float edgeSoftness, float[] centersX, float[] centersY, float[] radii, float[] stretchX, float[] stretchY
   ) {
      return new NanoGeoArmorModeDynamicTextureCompatRenderer.CloudPatchArchetype(centersX, centersY, radii, stretchX, stretchY, edgeSoftness);
   }

   private static float patchCycleEnvelope(float cyclePhase) {
      return smoothstep(0.05F, 0.28F, cyclePhase) * (1.0F - smoothstep(0.56F, 0.96F, cyclePhase));
   }

   private static float pulseElectricNoise(float angle, float seed, float frequencyScale) {
      float a = angle * frequencyScale;
      float n1 = (float)Math.sin(a * 5.0F + seed * 1.17F);
      float n2 = (float)Math.sin(a * 11.0F + seed * 2.31F);
      float n3 = (float)Math.sin(a * 19.0F - seed * 0.73F);
      return n1 * 0.5F + n2 * 0.3F + n3 * 0.2F;
   }

   private static float pulseElectricNoise01(float angle, float seed, float frequencyScale) {
      return clamp01(pulseElectricNoise(angle, seed, frequencyScale) * 0.5F + 0.5F);
   }

   private static float pulsePeriodicElectricNoise(float angle, float seed, float frequencyControl) {
      float safeControl = clamp(max(frequencyControl, 0.0F), 0.0F, 32.0F);
      float harmonicScale = 1.0F + safeControl / 8.0F;
      float lowScale = max((float)Math.floor(harmonicScale), 1.0F);
      float highScale = max(min(lowScale + 1.0F, 5.0F), lowScale);
      float blend = clamp(harmonicScale - lowScale, 0.0F, 1.0F);
      float lowNoise = pulseElectricNoise(angle, seed, lowScale);
      float highNoise = pulseElectricNoise(angle, seed, highScale);
      return lerp(lowNoise, highNoise, blend);
   }

   private static float pulsePeriodicElectricNoise01(float angle, float seed, float frequencyControl) {
      return clamp01(pulsePeriodicElectricNoise(angle, seed, frequencyControl) * 0.5F + 0.5F);
   }

   private static float pulseShapeStability(float radius, float distortionPx, float coreWidthPx, float glowRadiusPx) {
      float targetRadius = max(distortionPx * 6.0F + coreWidthPx * 4.0F + glowRadiusPx * 3.0F, 2.0F);
      return smoothstep(0.0F, targetRadius, max(radius, 0.0F));
   }

   private static float luminance(float red, float green, float blue) {
      return clamp01(red * 0.2126F + green * 0.7152F + blue * 0.0722F);
   }

   private static int sampleSourcePixelForTarget(int x, int y, int desiredWidth, int desiredHeight) {
      if (sourceTextureImage == null) {
         return packAbgr(1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         int sampleX = sampleSourceCoordinateForTarget(x, desiredWidth, sourceTextureImage.getWidth());
         int sampleY = sampleSourceCoordinateForTarget(y, desiredHeight, sourceTextureImage.getHeight());
         return sourceTextureImage.getPixelRGBA(sampleX, sampleY);
      }
   }

   private static int applyPulseCenterMarkerAlpha(int abgr) {
      int configuredAlpha = NanoGeoClientConfig.armorModePulseCenterMarkerAlpha();
      if (configuredAlpha < 255 && isPulseCenterMarker(abgr)) {
         int sourceAlpha = abgr >>> 24 & 0xFF;
         int scaledAlpha = Math.round(sourceAlpha * (configuredAlpha / 255.0F));
         return abgr & 16777215 | scaledAlpha << 24;
      } else {
         return abgr;
      }
   }

   private static boolean isPulseCenterMarker(int abgr) {
      return (abgr >>> 24 & 0xFF) >= 128 && abgrRed(abgr) >= 0.9411765F && abgrGreen(abgr) <= 0.15686275F && abgrBlue(abgr) <= 0.15686275F;
   }

   private static int sampleSourceCoordinateForTarget(int coordinate, int desiredSize, int sourceSize) {
      return clampInt(coordinate * sourceSize / Math.max(desiredSize, 1), 0, sourceSize - 1);
   }

   private static void maybeDebugLog(NanosuitArmorModeOverlayState.OverlayRenderState overlayState, boolean transitionActive, ResourceLocation textureId) {
      if (!NanoGeoClientConfig.debugArmorModeOverlay()) {
         lastDebugSignature = "";
      } else {
         String signature = "dynamicTexture="
            + textureId
            + " sourceSize="
            + sourceWidth
            + "x"
            + sourceHeight
            + " baseTargetSize="
            + baseTargetWidth
            + "x"
            + baseTargetHeight
            + " baseFlowMaskSize="
            + baseFlowMaskWidth
            + "x"
            + baseFlowMaskHeight
            + " pulseTargetSize="
            + pulseTargetWidth
            + "x"
            + pulseTargetHeight
            + " activePixels="
            + baseActivePixelCount
            + " uvMask="
            + (baseUvCoverageMask.length == baseTargetWidth * baseTargetHeight)
            + " uvCoveredPixels="
            + baseUvCoveredPixelCount
            + " uvQuads="
            + baseUvCoveredQuadCount
            + " updateIntervalMs="
            + NanoGeoClientConfig.armorModeDynamicTextureCompatUpdateIntervalMillis()
            + " transitionActive="
            + transitionActive
            + " transitionProgress="
            + overlayState.transitionProgress()
            + " pulseEnabled="
            + NanoGeoClientConfig.armorModePulseEnabled()
            + " pulseCenters="
            + NanoGeoArmorModePulseCenters.INSTANCE.centerCount();
         if (!signature.equals(lastDebugSignature)) {
            lastDebugSignature = signature;
            LOGGER.info("[CrysisMod][ArmorModeOverlay] {}", signature);
         }
      }
   }

   private static long perfStart() {
      return NanoGeoClientConfig.debugArmorModeOverlay() ? System.nanoTime() : 0L;
   }

   private static StringBuilder perfBuilder(String label) {
      return !NanoGeoClientConfig.debugArmorModeOverlay() ? null : new StringBuilder(label);
   }

   private static long perfAppend(StringBuilder builder, String label, long sectionStartNs) {
      if (builder == null) {
         return 0L;
      } else {
         long now = System.nanoTime();
         builder.append(' ').append(label).append('=').append(formatPerfMillis(now - sectionStartNs)).append("ms");
         return now;
      }
   }

   private static void perfLog(StringBuilder builder, long startNs, String details) {
      if (builder != null) {
         long elapsedNs = System.nanoTime() - startNs;
         if (elapsedNs >= 1000000L) {
            LOGGER.info("[CrysisMod][ArmorModePerf] total={}ms {} {}", new Object[]{formatPerfMillis(elapsedNs), builder, details});
         }
      }
   }

   private static String formatPerfMillis(long nanos) {
      return String.format(Locale.ROOT, "%.3f", nanos / 1000000.0);
   }

   private static String formatPerfFloat(float value) {
      return String.format(Locale.ROOT, "%.3f", value);
   }

   private static void resetDynamicStatesAndCaches() {
      SHARED_BASE_STATE.close();
      SHARED_PULSE_STATE.close();
      SHARED_COMPOSITE_STATE.close();
      PULSE_ATLAS_BUILD_STATE.close();
      closeStateMap(TRANSIENT_BASE_STATES);
      closeStateMap(TRANSIENT_BASE_COMPOSITE_STATES);
      closeStateMap(TRANSIENT_PULSE_STATES);
      closeStateMap(TRANSIENT_COMPOSITE_STATES);
      PULSE_TIMING_STATES.clear();
      closeSteadyBaseFrames();
      closeSteadyBaseCompositeFrames();
      closePulseStampFrames();
      closePulseAtlasFrames();
      baseTransitionDelayBytes = new byte[0];
      pulseTransitionDelayBytes = new byte[0];
      lastPreparedOverlayBrightness = Float.NaN;
      lastPreparedGlowStrength = Float.NaN;
      frameCacheSignature = "";
   }

   private static void closeSourceTexture() {
      if (sourceTextureImage != null) {
         sourceTextureImage.close();
         sourceTextureImage = null;
      }

      sourceTextureId = null;
      closeBaseArmorTexture();
      sourceWidth = 256;
      sourceHeight = 256;
      baseTargetWidth = 256;
      baseTargetHeight = 256;
      baseFlowMaskWidth = 256;
      baseFlowMaskHeight = 256;
      pulseTargetWidth = 256;
      pulseTargetHeight = 256;
      baseSampledSourcePixels = new int[0];
      baseSampledSourceAlpha = new float[0];
      basePreparedSourceRgb = new int[0];
      baseActivePixelIndices = new int[0];
      baseActivePixelCount = 0;
      baseUvCoverageModel = null;
      baseUvCoverageMask = new byte[0];
      baseUvCoverageWidth = 0;
      baseUvCoverageHeight = 0;
      baseUvCoveredPixelCount = 0;
      baseUvCoveredQuadCount = 0;
      baseUvCoverageEnabled = false;
      pulseSampledSourceAlpha = new float[0];
      closeSteadyBaseFrames();
      closeSteadyBaseCompositeFrames();
      closePulseStampFrames();
      closePulseAtlasFrames();
      lastPreparedOverlayBrightness = Float.NaN;
      lastPreparedGlowStrength = Float.NaN;
      lastConfiguredMaxResolution = -1;
      lastConfiguredPerformancePreset = null;
   }

   private static void closeBaseArmorTexture() {
      if (baseArmorTextureImage != null) {
         baseArmorTextureImage.close();
         baseArmorTextureImage = null;
      }

      baseArmorSampledPixels = new int[0];
      closeSteadyBaseCompositeFrames();
   }

   private static void clearTexture(NativeImage pixels, int width, int height) {
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            pixels.setPixelRGBA(x, y, 0);
         }
      }
   }

   private static int packRgbAbgr(float red, float green, float blue) {
      int r = clamp255(Math.round(red * 255.0F));
      int g = clamp255(Math.round(green * 255.0F));
      int b = clamp255(Math.round(blue * 255.0F));
      return b << 16 | g << 8 | r;
   }

   private static int packAlphaIntoPreparedAbgr(int preparedRgbAbgr, float alpha) {
      int a = clamp255(Math.round(clamp01(alpha) * 255.0F));
      return a << 24 | preparedRgbAbgr;
   }

   private static float abgrRed(int abgr) {
      return (abgr & 0xFF) / 255.0F;
   }

   private static float abgrGreen(int abgr) {
      return (abgr >>> 8 & 0xFF) / 255.0F;
   }

   private static float abgrBlue(int abgr) {
      return (abgr >>> 16 & 0xFF) / 255.0F;
   }

   private static int packAbgr(float red, float green, float blue, float alpha) {
      int a = clamp255(Math.round(alpha * 255.0F));
      int r = clamp255(Math.round(red * 255.0F));
      int g = clamp255(Math.round(green * 255.0F));
      int b = clamp255(Math.round(blue * 255.0F));
      return a << 24 | b << 16 | g << 8 | r;
   }

   private static int packCombinedPulseAbgr(NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig pulse, float coreAlpha, float glowAlpha) {
      float coreWeight = coreAlpha * 1.0F;
      float glowWeight = glowAlpha * 1.0F;
      boolean shaderpackSafeGlow = NanoGeoShaderpackCompatibility.shouldUseArmorModeCompatibilityFallback() && glowAlpha > 1.0E-4F;
      float glowPresence = shaderpackSafeGlow ? smoothstep(0.0039215684F, 0.12F, glowAlpha) : 0.0F;
      if (shaderpackSafeGlow) {
         glowWeight = max(glowWeight, glowPresence * 0.42F);
      }

      float combinedAlpha = clamp01(coreWeight + glowWeight);
      if (shaderpackSafeGlow) {
         combinedAlpha = max(combinedAlpha, glowPresence * 0.32F);
      }

      if (combinedAlpha <= 1.0E-4F) {
         return 0;
      } else {
         float colorWeight = max(coreWeight + glowWeight, 1.0E-4F);
         float red = (rgbChannel(pulse.coreColorRgb, 16) * coreWeight + rgbChannel(pulse.glowColorRgb, 16) * glowWeight) / colorWeight;
         float green = (rgbChannel(pulse.coreColorRgb, 8) * coreWeight + rgbChannel(pulse.glowColorRgb, 8) * glowWeight) / colorWeight;
         float blue = (rgbChannel(pulse.coreColorRgb, 0) * coreWeight + rgbChannel(pulse.glowColorRgb, 0) * glowWeight) / colorWeight;
         return packAbgr(red, green, blue, combinedAlpha);
      }
   }

   private static int compositePulseOverBase(int basePixel, int pulsePixel, float pulseAlpha) {
      float baseAlpha = (basePixel >>> 24 & 0xFF) / 255.0F;
      float pulseRed = abgrRed(pulsePixel);
      float pulseGreen = abgrGreen(pulsePixel);
      float pulseBlue = abgrBlue(pulsePixel);
      if (baseAlpha <= 1.0E-4F) {
         return packAbgr(pulseRed, pulseGreen, pulseBlue, pulseAlpha);
      } else {
         float baseRed = abgrRed(basePixel);
         float baseGreen = abgrGreen(basePixel);
         float baseBlue = abgrBlue(basePixel);
         float pulseStrength = clamp01(pulseAlpha * 1.25F);
         float red = screenBlend(baseRed, pulseRed * pulseStrength);
         float green = screenBlend(baseGreen, pulseGreen * pulseStrength);
         float blue = screenBlend(baseBlue, pulseBlue * pulseStrength);
         float alpha = max(baseAlpha, pulseAlpha);
         return packAbgr(red, green, blue, alpha);
      }
   }

   private static int compositeSourceOver(int basePixel, int sourcePixel) {
      float sourceAlpha = (sourcePixel >>> 24 & 0xFF) / 255.0F;
      if (sourceAlpha <= 0.0F) {
         return basePixel;
      } else {
         float baseAlpha = (basePixel >>> 24 & 0xFF) / 255.0F;
         float inverseSourceAlpha = 1.0F - sourceAlpha;
         float outputAlpha = sourceAlpha + baseAlpha * inverseSourceAlpha;
         if (outputAlpha <= 0.0F) {
            return 0;
         } else {
            float sourceWeight = sourceAlpha / outputAlpha;
            float baseWeight = baseAlpha * inverseSourceAlpha / outputAlpha;
            return packAbgr(
               abgrRed(sourcePixel) * sourceWeight + abgrRed(basePixel) * baseWeight,
               abgrGreen(sourcePixel) * sourceWeight + abgrGreen(basePixel) * baseWeight,
               abgrBlue(sourcePixel) * sourceWeight + abgrBlue(basePixel) * baseWeight,
               outputAlpha
            );
         }
      }
   }

   private static int lerpAbgr(int startPixel, int endPixel, float delta) {
      float t = clamp01(delta);
      int startA = startPixel >>> 24 & 0xFF;
      int startB = startPixel >>> 16 & 0xFF;
      int startG = startPixel >>> 8 & 0xFF;
      int startR = startPixel & 0xFF;
      int endA = endPixel >>> 24 & 0xFF;
      int endB = endPixel >>> 16 & 0xFF;
      int endG = endPixel >>> 8 & 0xFF;
      int endR = endPixel & 0xFF;
      int a = clamp255(Math.round(lerp(startA, endA, t)));
      int b = clamp255(Math.round(lerp(startB, endB, t)));
      int g = clamp255(Math.round(lerp(startG, endG, t)));
      int r = clamp255(Math.round(lerp(startR, endR, t)));
      return a << 24 | b << 16 | g << 8 | r;
   }

   private static float screenBlend(float base, float add) {
      return clamp01(1.0F - (1.0F - clamp01(base)) * (1.0F - clamp01(add)));
   }

   private static float rgbChannel(int rgb, int shift) {
      return (rgb >>> shift & 0xFF) / 255.0F;
   }

   private static float fract(float value) {
      return value - (float)Math.floor(value);
   }

   private static float hash21(float x, float y) {
      float px = fract(x * 123.34F);
      float py = fract(y * 456.21F);
      float sum = px * (px + 45.32F) + py * (py + 45.32F);
      px += sum;
      py += sum;
      return fract(px * py);
   }

   private static float smoothstep(float edge0, float edge1, float value) {
      if (edge0 >= edge1) {
         return value >= edge1 ? 1.0F : 0.0F;
      } else {
         float t = clamp01((value - edge0) / (edge1 - edge0));
         return t * t * (3.0F - 2.0F * t);
      }
   }

   private static float lerp(float start, float end, float delta) {
      return start + (end - start) * delta;
   }

   private static float mod(float value, float mod) {
      return value - mod * (float)Math.floor(value / mod);
   }

   private static float clamp01(float value) {
      return clamp(value, 0.0F, 1.0F);
   }

   private static float clamp(float value, float min, float max) {
      if (value < min) {
         return min;
      } else {
         return value > max ? max : value;
      }
   }

   private static float max(float left, float right) {
      return Math.max(left, right);
   }

   private static float min(float left, float right) {
      return Math.min(left, right);
   }

   private static int clampInt(int value, int min, int max) {
      if (value < min) {
         return min;
      } else {
         return value > max ? max : value;
      }
   }

   private static int clamp255(int value) {
      return clampInt(value, 0, 255);
   }

   private record CloudPatchArchetype(float[] centersX, float[] centersY, float[] radii, float[] stretchX, float[] stretchY, float edgeSoftness) {
   }

   private static final class DynamicTextureState {
      private final ResourceLocation textureId;
      private final ResourceLocation secondaryTextureId;
      private final boolean smoothFiltering;
      private DynamicTexture dynamicTexture;
      private DynamicTexture secondaryDynamicTexture;
      private NativeImage pixels;
      private NativeImage secondaryPixels;
      private int width;
      private int height;
      private long lastUpdateMillis;
      private long lastUseMillis;
      private float lastTimeSeconds = Float.NaN;
      private int lastBaseFrameIndex = -1;
      private int lastTransitionFrameIndex = -1;
      private boolean lastTransitionActive;
      private boolean lastArmorModeActive;
      private float lastTransitionProgress = Float.NaN;
      private float deactivationPulseSpawnCutoffTimeSeconds = Float.NaN;
      private float[] coreBuffer = new float[0];
      private float[] glowBuffer = new float[0];
      private float[] unionDistance = new float[0];
      private float[] unionCoreHalfWidth = new float[0];
      private float[] unionGlowLimit = new float[0];
      private float[] unionCoreSoftness = new float[0];
      private float[] unionGlowSoftness = new float[0];
      private float[] unionBrightnessFactor = new float[0];
      private int[] finalTouchedIndices = new int[0];
      private int[] previousBasePixelIndices = new int[0];
      private int previousBasePixelCount;
      private int finalTouchedCount;
      private int[] previousPulsePixelIndices = new int[0];
      private int previousPulsePixelCount;
      private int[] waveTouchedIndices = new int[0];
      private int[] mergeOverlapCount = new int[0];
      private int waveTouchedCount;
      private int[] finalStamp = new int[0];
      private int[] waveStamp = new int[0];
      private int frameStamp = 1;
      private int waveFrameStamp = 1;
      private boolean hasVisiblePulseContent;
      private boolean baseCompositeInitialized;
      private boolean uploadedOnce;
      private final float[] scaledCenterX = new float[64];
      private final float[] scaledCenterY = new float[64];

      private DynamicTextureState(ResourceLocation textureId) {
         this(textureId, null, false);
      }

      private DynamicTextureState(ResourceLocation textureId, boolean smoothFiltering) {
         this(textureId, null, smoothFiltering);
      }

      private DynamicTextureState(ResourceLocation textureId, ResourceLocation secondaryTextureId, boolean smoothFiltering) {
         this.textureId = textureId;
         this.secondaryTextureId = secondaryTextureId;
         this.smoothFiltering = smoothFiltering;
      }

      private void ensureCapacity(int targetWidth, int targetHeight) {
         int safeWidth = Math.max(16, targetWidth);
         int safeHeight = Math.max(16, targetHeight);
         if (this.dynamicTexture == null || this.pixels == null || this.width != safeWidth || this.height != safeHeight) {
            this.close();
            this.pixels = new NativeImage(safeWidth, safeHeight, false);
            this.dynamicTexture = new DynamicTexture(this.pixels);
            this.dynamicTexture.setFilter(this.smoothFiltering, false);
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            textureManager.register(this.textureId, this.dynamicTexture);
            NanoGeoArmorModeDynamicTextureCompatRenderer.clearTexture(this.pixels, safeWidth, safeHeight);
            if (this.secondaryTextureId != null) {
               this.secondaryPixels = new NativeImage(safeWidth, safeHeight, false);
               this.secondaryDynamicTexture = new DynamicTexture(this.secondaryPixels);
               this.secondaryDynamicTexture.setFilter(this.smoothFiltering, false);
               textureManager.register(this.secondaryTextureId, this.secondaryDynamicTexture);
               NanoGeoArmorModeDynamicTextureCompatRenderer.clearTexture(this.secondaryPixels, safeWidth, safeHeight);
            }

            this.width = safeWidth;
            this.height = safeHeight;
            int pixelCount = this.width * this.height;
            this.coreBuffer = new float[pixelCount];
            this.glowBuffer = new float[pixelCount];
            this.unionDistance = new float[pixelCount];
            this.unionCoreHalfWidth = new float[pixelCount];
            this.unionGlowLimit = new float[pixelCount];
            this.unionCoreSoftness = new float[pixelCount];
            this.unionGlowSoftness = new float[pixelCount];
            this.unionBrightnessFactor = new float[pixelCount];
            this.finalTouchedIndices = new int[pixelCount];
            this.previousBasePixelIndices = new int[pixelCount];
            this.previousPulsePixelIndices = new int[pixelCount];
            this.waveTouchedIndices = new int[pixelCount];
            this.mergeOverlapCount = new int[pixelCount];
            this.finalStamp = new int[pixelCount];
            this.waveStamp = new int[pixelCount];
            this.previousBasePixelCount = 0;
            this.finalTouchedCount = 0;
            this.previousPulsePixelCount = 0;
            this.waveTouchedCount = 0;
            this.frameStamp = 1;
            this.waveFrameStamp = 1;
            this.hasVisiblePulseContent = false;
            this.baseCompositeInitialized = false;
            this.lastUpdateMillis = 0L;
            this.lastTimeSeconds = Float.NaN;
            this.lastBaseFrameIndex = -1;
            this.lastTransitionFrameIndex = -1;
            this.lastTransitionActive = false;
            this.lastArmorModeActive = false;
            this.lastTransitionProgress = Float.NaN;
            this.deactivationPulseSpawnCutoffTimeSeconds = Float.NaN;
            this.uploadedOnce = false;
         }
      }

      private void upload() {
         if (this.dynamicTexture != null) {
            this.dynamicTexture.upload();
         }

         if (this.secondaryDynamicTexture != null) {
            this.secondaryDynamicTexture.upload();
         }

         this.uploadedOnce = true;
      }

      private void ensureUploaded() {
         if (!this.uploadedOnce) {
            this.upload();
         }
      }

      private void close() {
         if (this.dynamicTexture != null) {
            this.dynamicTexture.close();
            this.dynamicTexture = null;
         }

         if (this.secondaryDynamicTexture != null) {
            this.secondaryDynamicTexture.close();
            this.secondaryDynamicTexture = null;
         }

         if (this.pixels != null) {
            this.pixels.close();
            this.pixels = null;
         }

         if (this.secondaryPixels != null) {
            this.secondaryPixels.close();
            this.secondaryPixels = null;
         }

         this.width = 0;
         this.height = 0;
         this.coreBuffer = new float[0];
         this.glowBuffer = new float[0];
         this.unionDistance = new float[0];
         this.unionCoreHalfWidth = new float[0];
         this.unionGlowLimit = new float[0];
         this.unionCoreSoftness = new float[0];
         this.unionGlowSoftness = new float[0];
         this.unionBrightnessFactor = new float[0];
         this.finalTouchedIndices = new int[0];
         this.previousBasePixelIndices = new int[0];
         this.previousPulsePixelIndices = new int[0];
         this.waveTouchedIndices = new int[0];
         this.mergeOverlapCount = new int[0];
         this.finalStamp = new int[0];
         this.waveStamp = new int[0];
         this.previousBasePixelCount = 0;
         this.finalTouchedCount = 0;
         this.previousPulsePixelCount = 0;
         this.waveTouchedCount = 0;
         this.frameStamp = 1;
         this.waveFrameStamp = 1;
         this.hasVisiblePulseContent = false;
         this.baseCompositeInitialized = false;
         this.lastUpdateMillis = 0L;
         this.lastTimeSeconds = Float.NaN;
         this.lastBaseFrameIndex = -1;
         this.lastTransitionFrameIndex = -1;
         this.lastTransitionActive = false;
         this.lastArmorModeActive = false;
         this.lastTransitionProgress = Float.NaN;
         this.deactivationPulseSpawnCutoffTimeSeconds = Float.NaN;
         this.uploadedOnce = false;
      }
   }

   private record PatchSnapshot(
      float overlayBrightness,
      float glowStrength,
      float textureAlpha,
      float spotAlpha,
      float minSizePx,
      float maxSizePx,
      float frequency,
      float density,
      NanoGeoClientConfig.ArmorModePatchFlowVariant variant,
      float loopDurationSeconds
   ) {
   }

   private static final class PulseFrameTexture {
      private final ResourceLocation coreTextureId;
      private final NativeImage corePixels;
      private final DynamicTexture coreTexture;
      private boolean hasVisibleContent;

      private PulseFrameTexture(ResourceLocation coreTextureId, int width, int height) {
         this.coreTextureId = coreTextureId;
         this.corePixels = new NativeImage(width, height, false);
         this.coreTexture = new DynamicTexture(this.corePixels);
         this.coreTexture.setFilter(false, false);
         TextureManager textureManager = Minecraft.getInstance().getTextureManager();
         textureManager.register(coreTextureId, this.coreTexture);
         NanoGeoArmorModeDynamicTextureCompatRenderer.clearTexture(this.corePixels, width, height);
      }

      private void copyFrom(NanoGeoArmorModeDynamicTextureCompatRenderer.DynamicTextureState state) {
         this.hasVisibleContent = state.hasVisiblePulseContent;

         for (int i = 0; i < state.previousPulsePixelCount; i++) {
            int index = state.previousPulsePixelIndices[i];
            int x = index % state.width;
            int y = index / state.width;
            this.corePixels.setPixelRGBA(x, y, state.pixels.getPixelRGBA(x, y));
         }
      }

      private void upload() {
         this.coreTexture.upload();
      }

      private void close() {
         this.coreTexture.close();
         this.corePixels.close();
      }
   }

   private static final class PulseProfile {
      private float signedDistance;
      private float coreHalfWidth;
      private float glowLimit;
      private float coreSoftness;
      private float glowSoftness;
      private float brightnessFactor;
   }

   private record PulseRuntimeConfig(
      boolean enabled,
      int centerCount,
      boolean electric,
      boolean mergingEnabled,
      float intervalSeconds,
      float durationSeconds,
      float startRadiusPx,
      float endRadiusPx,
      float coreWidthPx,
      float glowRadiusPx,
      float coreSoftnessPx,
      float glowSoftnessPx,
      float coreAlpha,
      float glowAlpha,
      int coreColorRgb,
      int glowColorRgb,
      float brightness,
      float fadePower,
      float distortionPx,
      float distortionFrequency,
      float energyVariation,
      float widthVariation,
      float brightnessVariation,
      float edgeRoughness,
      float patternRotationRandomness,
      float mergeStrength,
      float mergeSoftnessPx,
      float mergeBrightnessBoost
   ) {
      private static NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig disabled() {
         return new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseRuntimeConfig(
            false,
            0,
            false,
            false,
            1.0F,
            1.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            14285567,
            5888511,
            0.0F,
            1.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            0.0F
         );
      }
   }

   private static final class PulseStampFrame {
      private final int width;
      private final int height;
      private final float centerX;
      private final float centerY;
      private final float[] core;
      private final float[] glow;
      private final float[] signedDistance;
      private final float[] coreHalfWidth;
      private final float[] glowLimit;
      private final float[] coreSoftness;
      private final float[] glowSoftness;
      private final float[] brightnessFactor;
      private final int[] activeIndices;
      private int activeCount;

      private PulseStampFrame(int width, int height, float centerX, float centerY) {
         this.width = width;
         this.height = height;
         this.centerX = centerX;
         this.centerY = centerY;
         this.core = new float[width * height];
         this.glow = new float[width * height];
         this.signedDistance = new float[width * height];
         this.coreHalfWidth = new float[width * height];
         this.glowLimit = new float[width * height];
         this.coreSoftness = new float[width * height];
         this.glowSoftness = new float[width * height];
         this.brightnessFactor = new float[width * height];
         this.activeIndices = new int[width * height];
         this.activeCount = 0;
      }
   }

   public record PulseTextureSet(ResourceLocation coreTexture, ResourceLocation glowTexture) {
      private static final NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet EMPTY = new NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet(
         null, null
      );

      public static NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet empty() {
         return EMPTY;
      }
   }

   private static final class PulseTimingState {
      private boolean active;
      private float activationSourceTimeSeconds = Float.NaN;
      private long lastUseMillis;
   }

   private static final class StaticFrameTexture {
      private final ResourceLocation textureId;
      private final NativeImage pixels;
      private final DynamicTexture dynamicTexture;

      private StaticFrameTexture(ResourceLocation textureId, int width, int height) {
         this.textureId = textureId;
         this.pixels = new NativeImage(width, height, false);
         this.dynamicTexture = new DynamicTexture(this.pixels);
         this.dynamicTexture.setFilter(false, false);
         Minecraft.getInstance().getTextureManager().register(textureId, this.dynamicTexture);
      }

      private void upload() {
         this.dynamicTexture.upload();
      }

      private void close() {
         this.dynamicTexture.close();
         this.pixels.close();
      }
   }

   private record TransitionSnapshot(
      boolean enabled, float blockSizePx, float fadeWindow, float staggerAmount, float maxDelay, float seed, boolean affectsPulses
   ) {
   }
}
