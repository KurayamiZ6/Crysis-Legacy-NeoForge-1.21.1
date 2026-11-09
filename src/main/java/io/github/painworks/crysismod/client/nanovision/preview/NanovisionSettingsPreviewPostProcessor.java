package io.github.painworks.crysismod.client.nanovision.preview;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.NanovisionMode;
import io.github.painworks.crysismod.client.nanovision.NanovisionShaders;
import io.github.painworks.crysismod.client.nanovision.ThermalMarkerQuality;
import io.github.painworks.crysismod.client.nanovision.ThermalVisualPreset;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalScreenProjection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.slf4j.Logger;

public final class NanovisionSettingsPreviewPostProcessor implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private PostChain chain;
   private PostPass heatExtractPass;
   private PostPass diffuseVerticalPass;
   private PostPass fieldPass;
   private PostPass glowHorizontalPass;
   private PostPass compositePass;
   private PostPass finalBlitPass;
   private PostPass cinematicGeometryPass;
   private PostPass cinematicGeometryHorizontalPass;
   private PostPass cinematicGeometryVerticalPass;
   private RenderTarget boundScene;
   private RenderTarget boundMarker;
   private ThermalVisualPreset activePreset;
   private int width;
   private int height;
   private boolean warnedFailure;

   public boolean process(RenderTarget scene, RenderTarget marker, float partialTick) {
      if (scene != null && marker != null) {
         PostChain activeChain = this.getOrCreate(scene, marker);
         if (activeChain == null) {
            return false;
         } else {
            int previousFilter = scene.filterMode;
            try {
               this.applyUniforms();
               activeChain.process(partialTick);
               scene.bindWrite(false);
               return true;
            } catch (RuntimeException var6) {
               if (!this.warnedFailure) {
                  this.warnedFailure = true;
                  LOGGER.error("[CrysisMod][Nanovision] exact settings preview post-process failed", var6);
               }

               this.close();
               return false;
            } finally {
               if (scene.filterMode != previousFilter) {
                  scene.setFilterMode(previousFilter);
               }
            }
         }
      } else {
         return false;
      }
   }

   private PostChain getOrCreate(RenderTarget scene, RenderTarget marker) {
      ThermalVisualPreset requestedPreset = NanovisionConfig.thermalVisualPreset();
      if (this.chain != null
         && (
            scene != this.boundScene
               || marker != this.boundMarker
               || scene.width != this.width
               || scene.height != this.height
               || requestedPreset != this.activePreset
         )) {
         this.close();
      }

      if (this.chain != null) {
         return this.chain;
      } else {
         try {
            this.createChain(scene, marker, requestedPreset);
            this.activePreset = requestedPreset;
            this.warnedFailure = false;
            return this.chain;
         } catch (Exception var7) {
            Exception exception = var7;
            this.close();
            if (requestedPreset != ThermalVisualPreset.CLASSIC) {
               try {
                  this.createChain(scene, marker, ThermalVisualPreset.CLASSIC);
                  this.activePreset = ThermalVisualPreset.CLASSIC;
                  LOGGER.warn("[CrysisMod][Nanovision] {} settings preview failed; using Classic", requestedPreset, exception);
                  return this.chain;
               } catch (Exception var6) {
                  var7.addSuppressed(var6);
                  this.close();
               }
            }

            if (!this.warnedFailure) {
               this.warnedFailure = true;
               LOGGER.error("[CrysisMod][Nanovision] failed to create exact settings preview post-chain", var7);
            }

            return null;
         }
      }
   }

   private void createChain(RenderTarget scene, RenderTarget marker, ThermalVisualPreset preset) throws Exception {
      Minecraft minecraft = Minecraft.getInstance();
      this.chain = new PostChain(
         minecraft.getTextureManager(),
         minecraft.getResourceManager(),
         scene,
         preset == ThermalVisualPreset.CINEMATIC
            ? NanovisionShaders.CINEMATIC_POST_CHAIN
            : (
               preset == ThermalVisualPreset.CLASSIC_IMPROVED_V3
                  ? NanovisionShaders.CLASSIC_IMPROVED_V3_POST_CHAIN
                  : (
                     preset == ThermalVisualPreset.CLASSIC_IMPROVED_V2
                        ? NanovisionShaders.CLASSIC_IMPROVED_V2_POST_CHAIN
                        : (
                           preset == ThermalVisualPreset.CLASSIC_IMPROVED
                              ? NanovisionShaders.CLASSIC_IMPROVED_POST_CHAIN
                              : NanovisionShaders.CLASSIC_POST_CHAIN
                        )
                  )
            )
      );
      if (preset == ThermalVisualPreset.CINEMATIC) {
         this.createCinematicPasses(scene, marker);
      } else {
         this.createClassicPasses(scene, marker, preset);
      }

      this.chain.resize(scene.width, scene.height);
      this.boundScene = scene;
      this.boundMarker = marker;
      this.width = scene.width;
      this.height = scene.height;
   }

   private void createClassicPasses(RenderTarget scene, RenderTarget marker, ThermalVisualPreset preset) throws Exception {
      RenderTarget heatA = this.chain.getTempTarget("nanovision_heat_a");
      RenderTarget heatB = this.chain.getTempTarget("nanovision_heat_b");
      RenderTarget finalTarget = this.chain.getTempTarget("nanovision_final");
      this.heatExtractPass = this.chain
         .addPass("crysismod" + (preset.usesOptimizedV3Pipeline() ? ":thermal_heat_extract_v3" : ":thermal_heat_extract"), scene, heatB, false);
      addSceneAndMarkerDepthAssets(this.heatExtractPass, scene, marker, true);
      this.fieldPass = this.chain.addPass("crysismod" + (preset.usesImprovedRelief() ? ":thermal_field_improved" : ":thermal_field"), heatB, heatA, false);
      this.fieldPass.addAuxAsset("MarkerDepthSampler", marker::getDepthTextureId, marker.width, marker.height);
      this.glowHorizontalPass = this.chain
         .addPass("crysismod" + (preset.usesOptimizedV3Pipeline() ? ":thermal_glow_diffuse_v3" : ":thermal_glow_diffuse"), heatA, heatB, false);
      this.compositePass = this.chain.addPass("crysismod:nanovision_composite", scene, finalTarget, false);
      this.compositePass.addAuxAsset("ThermalFieldSampler", heatB::getColorTextureId, heatB.width, heatB.height);
      this.finalBlitPass = this.chain.addPass("crysismod:nanovision_motion_blur", finalTarget, scene, false);
   }

   private void createCinematicPasses(RenderTarget scene, RenderTarget marker) throws Exception {
      RenderTarget heatA = this.chain.getTempTarget("cinematic_heat_a");
      RenderTarget heatB = this.chain.getTempTarget("cinematic_heat_b");
      RenderTarget geometryA = this.chain.getTempTarget("cinematic_geometry_a");
      RenderTarget geometryB = this.chain.getTempTarget("cinematic_geometry_b");
      RenderTarget finalTarget = this.chain.getTempTarget("cinematic_final");
      this.heatExtractPass = this.chain.addPass("crysismod:cinematic_heat_extract", scene, heatB, false);
      addSceneAndMarkerDepthAssets(this.heatExtractPass, scene, marker, true);
      this.diffuseVerticalPass = this.chain.addPass("crysismod:cinematic_heat_diffuse", heatB, heatA, false);
      this.cinematicGeometryPass = this.chain.addPass("crysismod:cinematic_geometry", heatA, geometryA, false);
      this.cinematicGeometryPass.addAuxAsset("MarkerDepthSampler", marker::getDepthTextureId, marker.width, marker.height);
      this.cinematicGeometryHorizontalPass = this.chain.addPass("crysismod:cinematic_geometry_diffuse", geometryA, geometryB, false);
      this.cinematicGeometryVerticalPass = this.chain.addPass("crysismod:cinematic_geometry_diffuse", geometryB, geometryA, false);
      this.compositePass = this.chain.addPass("crysismod:nanovision_cinematic_composite", scene, finalTarget, false);
      this.compositePass.addAuxAsset("HeatSampler", heatA::getColorTextureId, heatA.width, heatA.height);
      this.compositePass.addAuxAsset("GeometrySampler", geometryA::getColorTextureId, geometryA.width, geometryA.height);
      this.finalBlitPass = this.chain.addPass("crysismod:nanovision_motion_blur", finalTarget, scene, false);
   }

   private static void addSceneAndMarkerDepthAssets(PostPass pass, RenderTarget scene, RenderTarget marker, boolean includeMarkerColor) {
      pass.addAuxAsset("SceneDepthSampler", scene::getDepthTextureId, scene.width, scene.height);
      if (includeMarkerColor) {
         pass.addAuxAsset("MarkerSampler", marker::getColorTextureId, marker.width, marker.height);
      }

      pass.addAuxAsset("MarkerDepthSampler", marker::getDepthTextureId, marker.width, marker.height);
   }

   private void applyUniforms() {
      ThermalMarkerQuality quality = NanovisionConfig.thermalMarkerQuality();
      this.setFinalBlitToDirectCopy();
      if (this.activePreset == ThermalVisualPreset.CINEMATIC) {
         this.applyCinematicUniforms(quality);
      } else {
         this.applyClassicUniforms(quality);
      }
   }

   private void applyClassicUniforms(ThermalMarkerQuality quality) {
      boolean reliefDebug = this.activePreset.usesImprovedRelief() && NanovisionConfig.thermalGeometryReliefDebugOnly();
      EffectInstance extract = this.heatExtractPass.getEffect();
      setFloat(extract, "HeatPower", (float)NanovisionConfig.thermalMarkerHeatPower());
      configureDiffusion(extract, quality, 1.0F, 0.0F);
      EffectInstance field = this.fieldPass.getEffect();
      setFloat(field, "EncodedHeightRangePx", ThermalScreenProjection.encodedHeightRangePx(this.activePreset));
      setFloat(field, "InnerRadiusRatio", (float)NanovisionConfig.thermalMarkerInnerRadiusRatio());
      setFloat(field, "MaxSearchRadiusPx", NanovisionConfig.thermalMarkerMaxSearchRadiusPx());
      setInt(field, "ThermalQuality", quality.shaderQuality());
      setFloat(field, "DiffusionStrength", quality.diffusionStrength());
      setFloat(field, "TapSpacing", quality.diffusionTapSpacing());
      setFloat(field, "GeometryDepthStrength", (float)NanovisionConfig.thermalGeometryDepthStrength());
      setFloat(field, "GeometryDepthRadius", (float)NanovisionConfig.thermalGeometryDepthRadius());
      setFloat(field, "GeometryDepthContrast", (float)NanovisionConfig.thermalGeometryDepthContrast());
      setInt(field, "ImprovedReliefMode", this.activePreset.improvedReliefMode());
      setInt(field, "DebugGeometryReliefOnly", reliefDebug ? 1 : 0);
      setFloat(field, "ImprovedSilhouetteWidth", (float)NanovisionConfig.thermalImprovedSilhouetteWidth());
      setFloat(field, "ImprovedRimCurve", (float)NanovisionConfig.thermalImprovedRimCurve());
      setFloat(field, "ImprovedCoreHeat", (float)NanovisionConfig.thermalImprovedCoreHeat());
      setFloat(field, "ImprovedFacingStrength", (float)NanovisionConfig.thermalImprovedFacingStrength());
      setFloat(field, "ImprovedSmoothNormalStrength", (float)NanovisionConfig.thermalImprovedSmoothNormalStrength());
      setFloat(field, "ImprovedReliefScale", (float)NanovisionConfig.thermalImprovedReliefScale());
      setFloat(field, "ImprovedReliefThreshold", (float)NanovisionConfig.thermalImprovedReliefThreshold());
      setFloat(field, "ImprovedProtrusionCooling", (float)NanovisionConfig.thermalImprovedProtrusionCooling());
      setFloat(field, "ImprovedRecessCooling", (float)NanovisionConfig.thermalImprovedRecessCooling());
      setFloat(field, "ImprovedReliefSmoothing", (float)NanovisionConfig.thermalImprovedReliefSmoothing());
      setFloat(field, "ImprovedV2HotPeakArea", (float)NanovisionConfig.thermalImprovedV2HotPeakArea());
      EffectInstance glow = this.glowHorizontalPass.getEffect();
      setVec2(glow, "Direction", 1.0F, 0.0F);
      setFloat(glow, "EncodedHeightRangePx", ThermalScreenProjection.encodedHeightRangePx(this.activePreset));
      setFloat(glow, "OuterGlowRatio", (float)NanovisionConfig.thermalMarkerOuterGlowRatio());
      setFloat(glow, "GlowRadiusMultiplier", quality.glowRadiusMultiplier());
      setInt(glow, "ThermalQuality", quality.shaderQuality());
      applyCompositeUniforms(this.compositePass.getEffect(), quality);
      setInt(this.compositePass.getEffect(), "ThermalPresetMode", this.activePreset.usesImprovedV2Model() ? 2 : 0);
      setInt(this.compositePass.getEffect(), "DebugGeometryReliefOnly", reliefDebug ? 1 : 0);
   }

   private void applyCinematicUniforms(ThermalMarkerQuality quality) {
      EffectInstance extract = this.heatExtractPass.getEffect();
      setFloat(extract, "HeatPower", (float)NanovisionConfig.thermalMarkerHeatPower());
      configureDiffusion(extract, quality, 1.0F, 0.0F);
      configureDiffusion(this.diffuseVerticalPass.getEffect(), quality, 0.0F, 1.0F);
      EffectInstance geometry = this.cinematicGeometryPass.getEffect();
      setFloat(geometry, "EncodedHeightRangePx", ThermalScreenProjection.encodedHeightRangePx(this.activePreset));
      setInt(geometry, "ThermalQuality", quality.shaderQuality());
      configureGeometry(this.cinematicGeometryHorizontalPass.getEffect(), quality, 1.0F, 0.0F);
      configureGeometry(this.cinematicGeometryVerticalPass.getEffect(), quality, 0.0F, 1.0F);
      applyCompositeUniforms(this.compositePass.getEffect(), quality);
   }

   private static void configureDiffusion(EffectInstance effect, ThermalMarkerQuality quality, float directionX, float directionY) {
      setVec2(effect, "Direction", directionX, directionY);
      setInt(effect, "ThermalQuality", quality.shaderQuality());
      setFloat(effect, "DiffusionStrength", quality.diffusionStrength());
      setFloat(effect, "TapSpacing", quality.diffusionTapSpacing());
   }

   private static void configureGeometry(EffectInstance effect, ThermalMarkerQuality quality, float directionX, float directionY) {
      setVec2(effect, "Direction", directionX, directionY);
      setFloat(effect, "EncodedHeightRangePx", ThermalScreenProjection.encodedHeightRangePx());
      setFloat(effect, "EdgeWidthRatio", (float)NanovisionConfig.thermalMarkerInnerRadiusRatio());
      setFloat(effect, "OuterGlowRatio", (float)NanovisionConfig.thermalMarkerOuterGlowRatio());
      setFloat(effect, "GlowRadiusMultiplier", quality.glowRadiusMultiplier());
      setInt(effect, "ThermalQuality", quality.shaderQuality());
   }

   private static void applyCompositeUniforms(EffectInstance effect, ThermalMarkerQuality quality) {
      setInt(effect, "NanovisionMode", NanovisionMode.THERMAL_MARKER.ordinal());
      setFloat(effect, "InnerStrength", (float)NanovisionConfig.thermalMarkerInnerStrength());
      setFloat(effect, "ThermalModelOpacity", (float)NanovisionConfig.thermalMarkerModelOpacity());
      setFloat(effect, "HotBlockOpacity", (float)NanovisionConfig.thermalHotBlockOpacity());
      setFloat(effect, "OuterGlowStrength", (float)NanovisionConfig.thermalMarkerOuterGlowStrength());
      setFloat(effect, "GlowStrengthMultiplier", quality.glowStrengthMultiplier());
      setFloat(effect, "BandingStrength", (float)NanovisionConfig.thermalMarkerBandingStrength());
      setFloat(effect, "WarmGreenWidth", (float)NanovisionConfig.thermalWarmGreenWidth());
      setFloat(effect, "WarmRedWidth", (float)NanovisionConfig.thermalWarmRedWidth());
      setFloat(effect, "WarmPinkWidth", (float)NanovisionConfig.thermalWarmPinkWidth());
      setFloat(effect, "WarmWhiteWidth", (float)NanovisionConfig.thermalWarmWhiteWidth());
      setFloat(effect, "ScanlineStrength", (float)NanovisionConfig.thermalScanlineStrength());
      setFloat(effect, "ScanlineSpacingPx", (float)NanovisionConfig.thermalScanlineSpacingPx());
      setFloat(effect, "ScanlineSharpness", (float)NanovisionConfig.thermalScanlineSharpness());
      setFloat(effect, "ScanlineIrregularityPx", (float)NanovisionConfig.thermalScanlineIrregularityPx());
      setFloat(effect, "ScanlinePaletteShift", (float)NanovisionConfig.thermalScanlinePaletteShift());
      setFloat(effect, "ScanlineBreakup", (float)NanovisionConfig.thermalScanlineBreakup());
      setFloat(effect, "ScanlineWhiteHotSuppression", (float)NanovisionConfig.thermalScanlineWhiteHotSuppression());
      setInt(effect, "DebugRawMarker", NanovisionConfig.thermalMarkerDebugShowRawMarker() ? 1 : 0);
      setInt(effect, "DebugDetectedMask", NanovisionConfig.thermalMarkerDebugShowDetectedMask() ? 1 : 0);
      setInt(effect, "DebugDistance", NanovisionConfig.thermalMarkerDebugShowDistance() ? 1 : 0);
      setInt(effect, "DisablePalette", NanovisionConfig.thermalMarkerDebugDisablePalette() ? 1 : 0);
      setInt(effect, "DisableGlow", NanovisionConfig.thermalMarkerDebugDisableGlow() ? 1 : 0);
      setInt(effect, "DebugPassthrough", NanovisionConfig.nanovisionDebugPostPassthrough() ? 1 : 0);
      setInt(effect, "DebugOpaqueGrayOnly", NanovisionConfig.nanovisionDebugOpaqueGrayOnly() ? 1 : 0);
   }

   private void setFinalBlitToDirectCopy() {
      if (this.finalBlitPass != null) {
         EffectInstance effect = this.finalBlitPass.getEffect();
         setVec2(effect, "MotionDirectionPx", 0.0F, 0.0F);
         setFloat(effect, "RadialMotionPx", 0.0F);
         setFloat(effect, "MotionAmount", 0.0F);
      }
   }

   private static void setFloat(EffectInstance effect, String name, float value) {
      if (effect.getUniform(name) != null) {
         effect.getUniform(name).set(value);
      }
   }

   private static void setInt(EffectInstance effect, String name, int value) {
      if (effect.getUniform(name) != null) {
         effect.getUniform(name).set(value);
      }
   }

   private static void setVec2(EffectInstance effect, String name, float x, float y) {
      if (effect.getUniform(name) != null) {
         effect.getUniform(name).set(x, y);
      }
   }

   @Override
   public void close() {
      if (this.chain != null) {
         this.chain.close();
      }

      this.chain = null;
      this.heatExtractPass = null;
      this.diffuseVerticalPass = null;
      this.fieldPass = null;
      this.glowHorizontalPass = null;
      this.compositePass = null;
      this.finalBlitPass = null;
      this.cinematicGeometryPass = null;
      this.cinematicGeometryHorizontalPass = null;
      this.cinematicGeometryVerticalPass = null;
      this.boundScene = null;
      this.boundMarker = null;
      this.activePreset = null;
      this.width = 0;
      this.height = 0;
   }
}
