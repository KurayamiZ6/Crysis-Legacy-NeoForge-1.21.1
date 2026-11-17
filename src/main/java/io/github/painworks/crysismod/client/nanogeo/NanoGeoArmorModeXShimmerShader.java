package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitArmorModeOverlayState;
import java.io.IOException;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.slf4j.Logger;

public final class NanoGeoArmorModeXShimmerShader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation SHADER_LOCATION = ResourceLocation.fromNamespaceAndPath("crysismod", "rendertype_nanogeo_armor_mode_overlay");
   private static volatile ShaderInstance shaderInstance;
   private static volatile NanoGeoArmorModeXShimmerShader.PreparedState preparedState = NanoGeoArmorModeXShimmerShader.PreparedState.DEFAULT;
   private static volatile String lastDebugSignature = "";
   private static volatile boolean warnedShaderLoadFailure;

   private NanoGeoArmorModeXShimmerShader() {
   }

   public static void register(RegisterShadersEvent event) {
      try {
         event.registerShader(new ShaderInstance(event.getResourceProvider(), SHADER_LOCATION, DefaultVertexFormat.NEW_ENTITY), shader -> {
            shaderInstance = shader;
            warnedShaderLoadFailure = false;
            if (NanoGeoClientConfig.debugArmorModePlasmaField()) {
               LOGGER.info("[CrysisMod][ArmorModePlasma] shader loaded {}", SHADER_LOCATION);
            }
         });
      } catch (IOException var2) {
         shaderInstance = null;
         if (!warnedShaderLoadFailure) {
            warnedShaderLoadFailure = true;
            LOGGER.warn("[CrysisMod][ArmorModeXShimmer] failed to register shader {}, clean overlay fallback will be used", SHADER_LOCATION, var2);
         }
      }
   }

   public static void prepare(NanosuitArmorModeOverlayState.OverlayRenderState overlayState, boolean firstPerson) {
      preparedState = NanoGeoArmorModeXShimmerShader.PreparedState.from(overlayState, firstPerson);
      maybeDebugLog();
   }

   public static boolean shouldUseShader() {
      if (shaderInstance == null) {
         return false;
      } else {
         return !NanoGeoClientConfig.armorModePulseDebugForceVisible() && !NanoGeoClientConfig.armorModePulseDebugOutputOnlyPulse() ? true : true;
      }
   }

   public static ShaderInstance resolveShader() {
      ShaderInstance shader = shaderInstance;
      if (shouldUseShader() && shader != null) {
         NanoGeoClientConfig.ArmorModeOverlayPatternMode patternMode = NanoGeoClientConfig.armorModeOverlayPatternMode();
         boolean pulseDebugForceVisible = NanoGeoClientConfig.armorModePulseDebugForceVisible();
         boolean pulseDebugOutputOnlyPulse = NanoGeoClientConfig.armorModePulseDebugOutputOnlyPulse();
         NanoGeoArmorModePulseCenterSet pulseCenterSet = NanoGeoArmorModePulseCenters.INSTANCE.centers();
         float textureSizePx = pulseCenterSet.textureWidth() > 0 ? pulseCenterSet.textureWidth() : NanoGeoClientConfig.armorModeHexTextureSizePx();
         NanoGeoArmorModeXShimmerShader.PreparedState state = preparedState;
         float overlayAlpha = pulseDebugForceVisible ? 1.0F : state.overlayAlpha();
         setInt(shader, "PatternMode", shaderPatternMode(patternMode));
         setFloat(shader, "TimeSeconds", state.timeSeconds());
         setFloat(shader, "OverlayAlpha", overlayAlpha);
         setInt(shader, "ClassicModeTint", state.classicModeTint());
         setFloat(shader, "OverlayBrightness", NanoGeoClientConfig.armorModeOverlayBrightness());
         setFloat(shader, "TextureSizePx", textureSizePx);
         setInt(shader, "TransitionEnabled", NanoGeoClientConfig.armorModeTransitionEnabled() ? 1 : 0);
         setFloat(shader, "TransitionProgress", pulseDebugForceVisible ? 1.0F : state.transitionProgress());
         setFloat(shader, "TransitionPixelBlockSizePx", NanoGeoClientConfig.armorModeTransitionPixelBlockSizePx());
         setFloat(shader, "TransitionStaggerAmount", NanoGeoClientConfig.armorModeTransitionStaggerAmount());
         setFloat(shader, "TransitionFadeWindow", NanoGeoClientConfig.armorModeTransitionFadeWindow());
         setFloat(shader, "TransitionSeed", NanoGeoClientConfig.armorModeTransitionSeed());
         setInt(shader, "TransitionAffectsPulses", NanoGeoClientConfig.armorModeTransitionAffectsPulses() ? 1 : 0);
         setFloat(shader, "PlasmaBlobInnerAlpha", NanoGeoClientConfig.armorModePlasmaBlobInnerAlpha() / 255.0F);
         setInt(shader, "PlasmaBlobCount", NanoGeoClientConfig.armorModePlasmaBlobCount());
         setFloat(shader, "PlasmaBlobMinSizePx", NanoGeoClientConfig.armorModePlasmaBlobMinSizePx());
         setFloat(shader, "PlasmaBlobMaxSizePx", NanoGeoClientConfig.armorModePlasmaBlobMaxSizePx());
         setFloat(shader, "PlasmaBlobEdgeSoftnessPx", NanoGeoClientConfig.armorModePlasmaBlobEdgeSoftnessPx());
         setFloat(shader, "PlasmaBlobRimAlpha", NanoGeoClientConfig.armorModePlasmaBlobRimAlpha() / 255.0F);
         setFloat(shader, "PlasmaBlobRimWidthPx", NanoGeoClientConfig.armorModePlasmaBlobRimWidthPx());
         setFloat(shader, "PlasmaBlobRimBrightness", NanoGeoClientConfig.armorModePlasmaBlobRimBrightness());
         setFloat(shader, "PlasmaBlobDriftSpeedPxPerSecond", NanoGeoClientConfig.armorModePlasmaBlobDriftSpeedPxPerSecond());
         setFloat(shader, "PlasmaBlobDriftRangeScale", NanoGeoClientConfig.armorModePlasmaBlobDriftRangeScale());
         setFloat(shader, "PlasmaBlobShapeDistortion", NanoGeoClientConfig.armorModePlasmaBlobShapeDistortion());
         setFloat(shader, "PatchTextureAlpha", NanoGeoClientConfig.armorModePatchTextureAlpha() / 255.0F);
         setFloat(shader, "PatchSpotAlpha", NanoGeoClientConfig.armorModePatchSpotAlpha() / 255.0F);
         setFloat(shader, "PatchMinSizePx", NanoGeoClientConfig.armorModePatchMinSizePx());
         setFloat(shader, "PatchMaxSizePx", NanoGeoClientConfig.armorModePatchMaxSizePx());
         setFloat(shader, "PatchFrequency", NanoGeoClientConfig.armorModePatchFrequency());
         setFloat(shader, "PatchDensity", NanoGeoClientConfig.armorModePatchDensity());
         setInt(shader, "PatchFlowVariant", shaderPatchFlowVariant(NanoGeoClientConfig.armorModePatchFlowVariant()));
         int pulseMaxCenters = pulseDebugForceVisible ? Math.min(8, 64) : NanoGeoClientConfig.armorModePulseMaxCenters();
         setInt(shader, "PulseEnabled", !pulseDebugForceVisible && !NanoGeoClientConfig.armorModePulseEnabled() ? 0 : 1);
         setInt(
            shader,
            "PulseVisualMode",
            !pulseDebugForceVisible && NanoGeoClientConfig.armorModePulseVisualMode() != NanoGeoClientConfig.ArmorModePulseVisualMode.CRYSIS_ELECTRIC_RING
               ? 0
               : 1
         );
         setInt(shader, "PulseCenterCount", NanoGeoArmorModePulseCenters.INSTANCE.shaderCenterCount(pulseMaxCenters));
         setFloatArray(shader, "PulseCenterX", NanoGeoArmorModePulseCenters.INSTANCE.shaderCenterXUniformValues(64));
         setFloatArray(shader, "PulseCenterY", NanoGeoArmorModePulseCenters.INSTANCE.shaderCenterYUniformValues(64));
         setFloat(shader, "PulseIntervalSeconds", pulseDebugForceVisible ? 1.0F : NanoGeoClientConfig.armorModePulseIntervalSeconds());
         setFloat(shader, "PulseDurationSeconds", pulseDebugForceVisible ? 2.0F : NanoGeoClientConfig.armorModePulseDurationSeconds());
         setFloat(shader, "PulseStartRadiusPx", pulseDebugForceVisible ? 2.0F : NanoGeoClientConfig.armorModePulseStartRadiusPx());
         setFloat(shader, "PulseEndRadiusPx", pulseDebugForceVisible ? 80.0F : NanoGeoClientConfig.armorModePulseEndRadiusPx());
         setFloat(shader, "PulseCoreWidthPx", pulseDebugForceVisible ? 1.0F : NanoGeoClientConfig.armorModePulseCoreWidthPx());
         setFloat(shader, "PulseGlowRadiusPx", pulseDebugForceVisible ? 1.0F : NanoGeoClientConfig.armorModePulseGlowRadiusPx());
         setFloat(shader, "PulseCoreSoftnessPx", pulseDebugForceVisible ? 0.25F : NanoGeoClientConfig.armorModePulseCoreSoftnessPx());
         setFloat(shader, "PulseGlowSoftnessPx", pulseDebugForceVisible ? 0.5F : NanoGeoClientConfig.armorModePulseGlowSoftnessPx());
         setFloat(shader, "PulseCoreAlpha", pulseDebugForceVisible ? 1.0F : NanoGeoClientConfig.armorModePulseCoreAlpha() / 255.0F);
         setFloat(shader, "PulseGlowAlpha", pulseDebugForceVisible ? 1.0F : NanoGeoClientConfig.armorModePulseGlowAlpha() / 255.0F);
         setFloat(shader, "PulseBrightness", pulseDebugForceVisible ? 4.0F : NanoGeoClientConfig.armorModePulseBrightness());
         setFloat(shader, "PulseFadePower", pulseDebugForceVisible ? 1.5F : NanoGeoClientConfig.armorModePulseFadePower());
         setFloat(shader, "PulseRingDistortionPx", NanoGeoClientConfig.armorModePulseRingDistortionPx());
         setFloat(shader, "PulseRingDistortionFrequency", NanoGeoClientConfig.armorModePulseRingDistortionFrequency());
         setFloat(shader, "PulseRingEnergyVariation", NanoGeoClientConfig.armorModePulseRingEnergyVariation());
         setFloat(shader, "PulseWidthVariation", NanoGeoClientConfig.armorModePulseWidthVariation());
         setFloat(shader, "PulseBrightnessVariation", NanoGeoClientConfig.armorModePulseBrightnessVariation());
         setFloat(shader, "PulseEdgeRoughness", NanoGeoClientConfig.armorModePulseEdgeRoughness());
         setFloat(shader, "PulsePatternRotationRandomness", NanoGeoClientConfig.armorModePulsePatternRotationRandomness());
         setInt(shader, "PulseRingMergingEnabled", NanoGeoClientConfig.armorModePulseMergingEnabled() ? 1 : 0);
         setInt(shader, "PulseDebugForceVisible", pulseDebugForceVisible ? 1 : 0);
         setInt(shader, "PulseDebugOutputOnlyPulse", pulseDebugOutputOnlyPulse ? 1 : 0);
         return shader;
      } else {
         return GameRenderer.getRendertypeEntityTranslucentShader();
      }
   }

   private static void maybeDebugLog() {
      if (!NanoGeoClientConfig.debugArmorModePlasmaField()
         && !NanoGeoClientConfig.armorModePulseDebugForceVisible()
         && !NanoGeoClientConfig.armorModePulseDebugOutputOnlyPulse()) {
         lastDebugSignature = "";
      } else {
         NanoGeoArmorModeXShimmerShader.PreparedState state = preparedState;
         NanoGeoClientConfig.ArmorModeOverlayPatternMode patternMode = NanoGeoClientConfig.armorModeOverlayPatternMode();
         boolean pulseDebugForceVisible = NanoGeoClientConfig.armorModePulseDebugForceVisible();
         boolean pulseDebugOutputOnlyPulse = NanoGeoClientConfig.armorModePulseDebugOutputOnlyPulse();
         NanoGeoArmorModePulseCenterSet pulseCenterSet = NanoGeoArmorModePulseCenters.INSTANCE.centers();
         float textureSizePx = pulseCenterSet.textureWidth() > 0 ? pulseCenterSet.textureWidth() : NanoGeoClientConfig.armorModeHexTextureSizePx();
         int pulseMaxCenters = NanoGeoClientConfig.armorModePulseMaxCenters();
         int pulseCenterCountUsed = NanoGeoArmorModePulseCenters.INSTANCE.shaderCenterCount(pulseMaxCenters);
         ShaderInstance shader = shaderInstance;
         String signature = "shaderActive="
            + shouldUseShader()
            + " patternMode="
            + patternMode
            + " configuredPatternMode="
            + NanoGeoClientConfig.armorModeOverlayPatternMode()
            + " texture="
            + state.texture()
            + " overlayAlpha="
            + state.overlayAlpha()
            + " overlayBrightness="
            + NanoGeoClientConfig.armorModeOverlayBrightness()
            + " transitionEnabled="
            + NanoGeoClientConfig.armorModeTransitionEnabled()
            + " transitionProgress="
            + state.transitionProgress()
            + " transitionDurationSeconds="
            + NanoGeoClientConfig.armorModeTransitionDurationSeconds()
            + " transitionPixelBlockSizePx="
            + NanoGeoClientConfig.armorModeTransitionPixelBlockSizePx()
            + " transitionStaggerAmount="
            + NanoGeoClientConfig.armorModeTransitionStaggerAmount()
            + " transitionFadeWindow="
            + NanoGeoClientConfig.armorModeTransitionFadeWindow()
            + " transitionSeed="
            + NanoGeoClientConfig.armorModeTransitionSeed()
            + " transitionAffectsPulses="
            + NanoGeoClientConfig.armorModeTransitionAffectsPulses()
            + " textureSizePx="
            + textureSizePx
            + " plasmaBlobInnerAlpha="
            + NanoGeoClientConfig.armorModePlasmaBlobInnerAlpha()
            + " plasmaBlobCount="
            + NanoGeoClientConfig.armorModePlasmaBlobCount()
            + " plasmaBlobMinSizePx="
            + NanoGeoClientConfig.armorModePlasmaBlobMinSizePx()
            + " plasmaBlobMaxSizePx="
            + NanoGeoClientConfig.armorModePlasmaBlobMaxSizePx()
            + " plasmaBlobEdgeSoftnessPx="
            + NanoGeoClientConfig.armorModePlasmaBlobEdgeSoftnessPx()
            + " plasmaBlobRimAlpha="
            + NanoGeoClientConfig.armorModePlasmaBlobRimAlpha()
            + " plasmaBlobRimWidthPx="
            + NanoGeoClientConfig.armorModePlasmaBlobRimWidthPx()
            + " plasmaBlobRimBrightness="
            + NanoGeoClientConfig.armorModePlasmaBlobRimBrightness()
            + " plasmaBlobDriftSpeed="
            + NanoGeoClientConfig.armorModePlasmaBlobDriftSpeedPxPerSecond()
            + " plasmaBlobDriftRangeScale="
            + NanoGeoClientConfig.armorModePlasmaBlobDriftRangeScale()
            + " plasmaBlobShapeDistortion="
            + NanoGeoClientConfig.armorModePlasmaBlobShapeDistortion()
            + " patchTextureAlpha="
            + NanoGeoClientConfig.armorModePatchTextureAlpha()
            + " patchSpotAlpha="
            + NanoGeoClientConfig.armorModePatchSpotAlpha()
            + " patchMinSizePx="
            + NanoGeoClientConfig.armorModePatchMinSizePx()
            + " patchMaxSizePx="
            + NanoGeoClientConfig.armorModePatchMaxSizePx()
            + " patchFrequency="
            + NanoGeoClientConfig.armorModePatchFrequency()
            + " patchDensity="
            + NanoGeoClientConfig.armorModePatchDensity()
            + " patchFlowVariant="
            + NanoGeoClientConfig.armorModePatchFlowVariant()
            + " pulseEnabled="
            + NanoGeoClientConfig.armorModePulseEnabled()
            + " pulseCenterSource="
            + NanoGeoClientConfig.armorModePulseCenterSource()
            + " pulseVisualMode="
            + NanoGeoClientConfig.armorModePulseVisualMode()
            + " pulseCentersLoaded="
            + NanoGeoArmorModePulseCenters.INSTANCE.centerCount()
            + " pulseCentersUsed="
            + pulseCenterCountUsed
            + " pulseCenterState={"
            + NanoGeoArmorModePulseCenters.INSTANCE.dumpPulseCenterState(pulseMaxCenters)
            + "} pulseFirstCenters="
            + firstPulseCentersForDebug(pulseCenterSet, 3)
            + " pulseUniformX="
            + (shader != null && shader.getUniform("PulseCenterX") != null)
            + " pulseUniformY="
            + (shader != null && shader.getUniform("PulseCenterY") != null)
            + " pulseDebugForceVisible="
            + pulseDebugForceVisible
            + " pulseDebugOutputOnlyPulse="
            + pulseDebugOutputOnlyPulse
            + " pulseMaxCenters="
            + pulseMaxCenters
            + " pulseInterval="
            + NanoGeoClientConfig.armorModePulseIntervalSeconds()
            + " pulseDuration="
            + NanoGeoClientConfig.armorModePulseDurationSeconds()
            + " pulseStartRadiusPx="
            + NanoGeoClientConfig.armorModePulseStartRadiusPx()
            + " pulseEndRadiusPx="
            + NanoGeoClientConfig.armorModePulseEndRadiusPx()
            + " pulseCoreWidthPx="
            + NanoGeoClientConfig.armorModePulseCoreWidthPx()
            + " pulseGlowRadiusPx="
            + NanoGeoClientConfig.armorModePulseGlowRadiusPx()
            + " pulseCoreAlpha="
            + NanoGeoClientConfig.armorModePulseCoreAlpha()
            + " pulseGlowAlpha="
            + NanoGeoClientConfig.armorModePulseGlowAlpha()
            + " pulseBrightness="
            + NanoGeoClientConfig.armorModePulseBrightness()
            + " pulseFadePower="
            + NanoGeoClientConfig.armorModePulseFadePower()
            + " pulseWidthVariation="
            + NanoGeoClientConfig.armorModePulseWidthVariation()
            + " pulseBrightnessVariation="
            + NanoGeoClientConfig.armorModePulseBrightnessVariation()
            + " pulseEdgeRoughness="
            + NanoGeoClientConfig.armorModePulseEdgeRoughness()
            + " pulsePatternRotationRandomness="
            + NanoGeoClientConfig.armorModePulsePatternRotationRandomness()
            + " pulseRingMergingEnabled="
            + NanoGeoClientConfig.armorModePulseMergingEnabled()
            + " pulseWarnings="
            + pulseSanityWarnings(textureSizePx, pulseCenterSet, pulseCenterCountUsed)
            + " firstPerson="
            + state.firstPerson();
         if (!signature.equals(lastDebugSignature)) {
            lastDebugSignature = signature;
            LOGGER.info("[CrysisMod][ArmorModePlasma] {}", signature);
         }
      }
   }

   private static void setFloat(ShaderInstance shader, String uniformName, float value) {
      Uniform uniform = shader.getUniform(uniformName);
      if (uniform != null) {
         uniform.set(value);
      }
   }

   private static void setInt(ShaderInstance shader, String uniformName, int value) {
      Uniform uniform = shader.getUniform(uniformName);
      if (uniform != null) {
         uniform.set(value);
      }
   }

   private static void setFloatArray(ShaderInstance shader, String uniformName, float[] value) {
      Uniform uniform = shader.getUniform(uniformName);
      if (uniform == null) {
         uniform = shader.getUniform(uniformName + "[0]");
      }

      if (uniform != null) {
         uniform.set(value);
      }
   }

   private static int shaderPatternMode(NanoGeoClientConfig.ArmorModeOverlayPatternMode patternMode) {
      return switch (patternMode) {
         case NANO_PLASMA_FIELD -> 5;
         case CHAOTIC_PATCH_FLOW -> 6;
      };
   }

   private static int shaderPatchFlowVariant(NanoGeoClientConfig.ArmorModePatchFlowVariant variant) {
      return switch (variant) {
         case DYNAMIC_RESEED -> 0;
         case FIXED_ARCHETYPE_LOOP -> 1;
         case SOFT_CLOUD_ARCHETYPES -> 1;
      };
   }

   private static String firstPulseCentersForDebug(NanoGeoArmorModePulseCenterSet centerSet, int limit) {
      int count = Math.min(Math.max(limit, 0), centerSet.centerCount());
      StringBuilder builder = new StringBuilder("[");

      for (int i = 0; i < count; i++) {
         if (i > 0) {
            builder.append(", ");
         }

         NanoGeoArmorModePulseCenterSet.Center center = centerSet.centers().get(i);
         builder.append('(').append(center.x()).append(',').append(center.y()).append(')');
      }

      return builder.append(']').toString();
   }

   private static String pulseSanityWarnings(float textureSizePx, NanoGeoArmorModePulseCenterSet centerSet, int usedCenterCount) {
      StringBuilder warnings = new StringBuilder("[");
      appendWarning(warnings, !NanoGeoClientConfig.armorModePulseEnabled() && !NanoGeoClientConfig.armorModePulseDebugForceVisible(), "pulse-disabled");
      appendWarning(warnings, centerSet.centerCount() <= 0, "centers-zero");
      appendWarning(warnings, usedCenterCount <= 0, "used-centers-zero");
      appendWarning(warnings, NanoGeoClientConfig.armorModeOverlayAlpha() <= 0 && !NanoGeoClientConfig.armorModePulseDebugForceVisible(), "overlay-alpha-zero");
      appendWarning(
         warnings,
         NanoGeoClientConfig.armorModePulseCoreAlpha() <= 0
            && NanoGeoClientConfig.armorModePulseGlowAlpha() <= 0
            && !NanoGeoClientConfig.armorModePulseDebugForceVisible(),
         "pulse-alpha-zero"
      );
      appendWarning(
         warnings, NanoGeoClientConfig.armorModePulseBrightness() <= 1.0E-4F && !NanoGeoClientConfig.armorModePulseDebugForceVisible(), "pulse-brightness-zero"
      );
      appendWarning(
         warnings,
         NanoGeoClientConfig.armorModePulseStartRadiusPx() >= NanoGeoClientConfig.armorModePulseEndRadiusPx()
            && !NanoGeoClientConfig.armorModePulseDebugForceVisible(),
         "start-radius-not-less-than-end-radius"
      );
      appendWarning(
         warnings,
         centerSet.textureWidth() > 0 && Math.abs(textureSizePx - centerSet.textureWidth()) > 0.01F,
         "texture-space-mismatch-json=" + centerSet.textureWidth() + "-runtime=" + textureSizePx
      );
      appendWarning(warnings, !NanoGeoClientConfig.armorModeOverlayRenderInThirdPerson(), "third-person-overlay-disabled");
      appendWarning(warnings, !NanoGeoClientConfig.armorModeOverlayRenderInFirstPerson(), "first-person-overlay-disabled");
      return warnings.append(']').toString();
   }

   private static void appendWarning(StringBuilder builder, boolean condition, String warning) {
      if (condition) {
         if (builder.length() > 1) {
            builder.append(", ");
         }

         builder.append(warning);
      }
   }

   private record PreparedState(ResourceLocation texture, float overlayAlpha, float timeSeconds, boolean firstPerson, float transitionProgress, int classicModeTint) {
      private static final NanoGeoArmorModeXShimmerShader.PreparedState DEFAULT = new NanoGeoArmorModeXShimmerShader.PreparedState(
         NanoGeoArmorTextures.ARMOR_MODE_OVERLAY, NanoGeoClientConfig.armorModeOverlayAlpha() / 255.0F, 0.0F, false, 0.0F, 0
      );

      private static NanoGeoArmorModeXShimmerShader.PreparedState from(NanosuitArmorModeOverlayState.OverlayRenderState overlayState, boolean firstPerson) {
         return new NanoGeoArmorModeXShimmerShader.PreparedState(
            overlayState.texture(), overlayState.alpha(), overlayState.shimmerTime() / 20.0F, firstPerson, overlayState.transitionProgress(),
            overlayState.classicMode() ? (overlayState.green() == 0.0F ? 1 : 2) : 0
         );
      }
   }
}
