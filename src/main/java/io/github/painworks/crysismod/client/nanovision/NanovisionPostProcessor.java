package io.github.painworks.crysismod.client.nanovision;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanovision.debug.NanovisionPerformanceProfiler;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerRenderTarget;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalScreenProjection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.util.Mth;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;

public final class NanovisionPostProcessor {
   private static final Logger LOGGER = LogUtils.getLogger();
   private PostChain chain;
   private PostPass heatExtractPass;
   private PostPass diffuseVerticalPass;
   private PostPass fieldPass;
   private PostPass glowHorizontalPass;
   private PostPass compositePass;
   private PostPass rawMarkerDebugPass;
   private PostPass motionBlurPass;
   private PostPass cinematicGeometryPass;
   private PostPass cinematicGeometryHorizontalPass;
   private PostPass cinematicGeometryVerticalPass;
   private RenderTarget classicThermalFieldTarget;
   private RenderTarget classicFinalTarget;
   private ThermalVisualPreset requestedPreset;
   private ThermalVisualPreset activePreset;
   private int width;
   private int height;
   private boolean warnedLoadFailure;
   private boolean cameraStateInitialized;
   private float lastCameraYaw;
   private float lastCameraPitch;
   private float smoothedMotionX;
   private float smoothedMotionY;
   private float smoothedRadialMotion;
   private float currentMotionBlurAmount;
   private int v3ClassicUniformSignature = Integer.MIN_VALUE;

   public void renderAfterLevel(Minecraft minecraft, float partialTick) {
      NanovisionPerformanceProfiler.pollGpuQueries();
      if (minecraft.player == null || minecraft.level == null || !NanovisionState.isActive(minecraft.player)) {
         this.closeForInactiveFrame();
      } else if (NanovisionController.shouldRunPostProcessor()) {
         PostChain activeChain = this.getOrCreateChain(minecraft);
         if (activeChain == null) {
            minecraft.getMainRenderTarget().bindWrite(false);
            NanovisionController.setMode(NanovisionMode.OFF);
         } else {
            NanovisionPerformanceProfiler.beginFrame(
               this.activePreset,
               minecraft.getMainRenderTarget().width,
               minecraft.getMainRenderTarget().height,
               ThermalMarkerRenderTarget.hasSourcesRenderedThisFrame()
            );
            long postCpuStarted = NanovisionPerformanceProfiler.beginCpu();

            try {
               this.updateMotionBlur(minecraft);
               this.applyUniforms();
               if (this.activePreset.usesOptimizedV3Pipeline()) {
                  this.processV3Frame(minecraft, partialTick);
               } else if (NanovisionPerformanceProfiler.isCapturing()) {
                  this.processProfiledFrame(partialTick);
               } else {
                  RenderTarget mainTarget = minecraft.getMainRenderTarget();
                  int previousFilter = mainTarget.filterMode;
                  try {
                     activeChain.process(partialTick);
                  } finally {
                     if (mainTarget.filterMode != previousFilter) {
                        mainTarget.setFilterMode(previousFilter);
                     }
                  }
               }
            } finally {
               NanovisionPerformanceProfiler.endCpu(NanovisionPerformanceProfiler.Section.POST_TOTAL, postCpuStarted);
               NanovisionPerformanceProfiler.endFrame();
               minecraft.getMainRenderTarget().bindWrite(false);
            }
         }
      }
   }

   public void close() {
      if (this.chain != null) {
         this.chain.close();
         this.chain = null;
         this.heatExtractPass = null;
         this.diffuseVerticalPass = null;
         this.fieldPass = null;
         this.glowHorizontalPass = null;
         this.compositePass = null;
         this.rawMarkerDebugPass = null;
         this.motionBlurPass = null;
         this.cinematicGeometryPass = null;
         this.cinematicGeometryHorizontalPass = null;
         this.cinematicGeometryVerticalPass = null;
         this.classicThermalFieldTarget = null;
         this.classicFinalTarget = null;
         this.requestedPreset = null;
         this.activePreset = null;
         this.width = 0;
         this.height = 0;
         this.cameraStateInitialized = false;
         this.smoothedMotionX = 0.0F;
         this.smoothedMotionY = 0.0F;
         this.smoothedRadialMotion = 0.0F;
         this.currentMotionBlurAmount = 0.0F;
         this.v3ClassicUniformSignature = Integer.MIN_VALUE;
      }
   }

   private PostChain getOrCreateChain(Minecraft minecraft) {
      int windowWidth = minecraft.getWindow().getWidth();
      int windowHeight = minecraft.getWindow().getHeight();
      ThermalVisualPreset configuredPreset = NanovisionConfig.thermalVisualPreset();
      if (this.chain != null && (windowWidth != this.width || windowHeight != this.height || configuredPreset != this.requestedPreset)) {
         this.close();
      }

      if (this.chain != null) {
         return this.chain;
      } else {
         try {
            this.createChain(minecraft, configuredPreset, windowWidth, windowHeight);
            this.requestedPreset = configuredPreset;
            this.activePreset = configuredPreset;
            this.warnedLoadFailure = false;
            LOGGER.info("[CrysisMod][Nanovision] active thermal visual preset: {}", this.activePreset);
            return this.chain;
         } catch (Exception var8) {
            Exception exception = var8;
            this.close();
            if (configuredPreset != ThermalVisualPreset.CLASSIC) {
               try {
                  this.createChain(minecraft, ThermalVisualPreset.CLASSIC, windowWidth, windowHeight);
                  this.requestedPreset = configuredPreset;
                  this.activePreset = ThermalVisualPreset.CLASSIC;
                  if (!this.warnedLoadFailure) {
                     this.warnedLoadFailure = true;
                     LOGGER.warn("[CrysisMod][Nanovision] {} post chain failed; using classic fallback", configuredPreset, exception);
                  }

                  return this.chain;
               } catch (Exception var7) {
                  var8.addSuppressed(var7);
                  this.close();
               }
            }

            if (!this.warnedLoadFailure) {
               this.warnedLoadFailure = true;
               LOGGER.warn("[CrysisMod][Nanovision] failed to load {} post chain", configuredPreset, var8);
            }

            return null;
         }
      }
   }

   private void createChain(Minecraft minecraft, ThermalVisualPreset preset, int windowWidth, int windowHeight) throws Exception {
      this.chain = new PostChain(
         minecraft.getTextureManager(),
         minecraft.getResourceManager(),
         minecraft.getMainRenderTarget(),
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
         this.createCinematicPasses(minecraft);
      } else {
         this.createClassicPasses(minecraft, preset);
      }

      this.chain.resize(windowWidth, windowHeight);
      this.width = windowWidth;
      this.height = windowHeight;
   }

   private void createClassicPasses(Minecraft minecraft, ThermalVisualPreset preset) throws Exception {
      RenderTarget heatA = this.chain.getTempTarget("nanovision_heat_a");
      RenderTarget heatB = this.chain.getTempTarget("nanovision_heat_b");
      RenderTarget finalTarget = this.chain.getTempTarget("nanovision_final");
      this.classicThermalFieldTarget = heatB;
      this.classicFinalTarget = finalTarget;
      this.heatExtractPass = this.chain
         .addPass(
            "crysismod" + (preset.usesOptimizedV3Pipeline() ? ":thermal_heat_extract_v3" : ":thermal_heat_extract"),
            minecraft.getMainRenderTarget(),
            heatB,
            false
         );
      this.heatExtractPass
         .addAuxAsset(
            "SceneDepthSampler",
            () -> minecraft.getMainRenderTarget().getDepthTextureId(),
            minecraft.getMainRenderTarget().width,
            minecraft.getMainRenderTarget().height
         );
      this.heatExtractPass
         .addAuxAsset("MarkerSampler", ThermalMarkerRenderTarget::colorTextureId, ThermalMarkerRenderTarget.width(), ThermalMarkerRenderTarget.height());
      this.heatExtractPass
         .addAuxAsset("MarkerDepthSampler", ThermalMarkerRenderTarget::depthTextureId, ThermalMarkerRenderTarget.width(), ThermalMarkerRenderTarget.height());
      this.fieldPass = this.chain.addPass("crysismod" + (preset.usesImprovedRelief() ? ":thermal_field_improved" : ":thermal_field"), heatB, heatA, false);
      this.fieldPass
         .addAuxAsset("MarkerDepthSampler", ThermalMarkerRenderTarget::depthTextureId, ThermalMarkerRenderTarget.width(), ThermalMarkerRenderTarget.height());
      this.glowHorizontalPass = this.chain
         .addPass("crysismod" + (preset.usesOptimizedV3Pipeline() ? ":thermal_glow_diffuse_v3" : ":thermal_glow_diffuse"), heatA, heatB, false);
      this.compositePass = this.chain.addPass("crysismod:nanovision_composite", minecraft.getMainRenderTarget(), finalTarget, false);
      this.compositePass.addAuxAsset("ThermalFieldSampler", heatB::getColorTextureId, heatB.width, heatB.height);
      if (preset.usesOptimizedV3Pipeline()) {
         this.rawMarkerDebugPass = this.chain.addPass("crysismod:nanovision_raw_marker_debug", minecraft.getMainRenderTarget(), finalTarget, false);
         this.rawMarkerDebugPass
            .addAuxAsset("RawMarkerSampler", ThermalMarkerRenderTarget::colorTextureId, ThermalMarkerRenderTarget.width(), ThermalMarkerRenderTarget.height());
      }

      this.motionBlurPass = this.chain.addPass("crysismod:nanovision_motion_blur", finalTarget, minecraft.getMainRenderTarget(), false);
   }

   private void createCinematicPasses(Minecraft minecraft) throws Exception {
      RenderTarget heatA = this.chain.getTempTarget("cinematic_heat_a");
      RenderTarget heatB = this.chain.getTempTarget("cinematic_heat_b");
      RenderTarget geometryA = this.chain.getTempTarget("cinematic_geometry_a");
      RenderTarget geometryB = this.chain.getTempTarget("cinematic_geometry_b");
      RenderTarget finalTarget = this.chain.getTempTarget("cinematic_final");
      this.heatExtractPass = this.chain.addPass("crysismod:cinematic_heat_extract", minecraft.getMainRenderTarget(), heatB, false);
      this.heatExtractPass
         .addAuxAsset(
            "SceneDepthSampler",
            () -> minecraft.getMainRenderTarget().getDepthTextureId(),
            minecraft.getMainRenderTarget().width,
            minecraft.getMainRenderTarget().height
         );
      this.heatExtractPass
         .addAuxAsset("MarkerSampler", ThermalMarkerRenderTarget::colorTextureId, ThermalMarkerRenderTarget.width(), ThermalMarkerRenderTarget.height());
      this.heatExtractPass
         .addAuxAsset("MarkerDepthSampler", ThermalMarkerRenderTarget::depthTextureId, ThermalMarkerRenderTarget.width(), ThermalMarkerRenderTarget.height());
      this.diffuseVerticalPass = this.chain.addPass("crysismod:cinematic_heat_diffuse", heatB, heatA, false);
      this.cinematicGeometryPass = this.chain.addPass("crysismod:cinematic_geometry", heatA, geometryA, false);
      this.cinematicGeometryPass
         .addAuxAsset("MarkerDepthSampler", ThermalMarkerRenderTarget::depthTextureId, ThermalMarkerRenderTarget.width(), ThermalMarkerRenderTarget.height());
      this.cinematicGeometryHorizontalPass = this.chain.addPass("crysismod:cinematic_geometry_diffuse", geometryA, geometryB, false);
      this.cinematicGeometryVerticalPass = this.chain.addPass("crysismod:cinematic_geometry_diffuse", geometryB, geometryA, false);
      this.compositePass = this.chain.addPass("crysismod:nanovision_cinematic_composite", minecraft.getMainRenderTarget(), finalTarget, false);
      this.compositePass.addAuxAsset("HeatSampler", heatA::getColorTextureId, heatA.width, heatA.height);
      this.compositePass.addAuxAsset("GeometrySampler", geometryA::getColorTextureId, geometryA.width, geometryA.height);
      this.motionBlurPass = this.chain.addPass("crysismod:nanovision_motion_blur", finalTarget, minecraft.getMainRenderTarget(), false);
   }

   private void processV3Frame(Minecraft minecraft, float partialTick) {
      if (this.classicThermalFieldTarget != null
         && this.classicFinalTarget != null
         && this.heatExtractPass != null
         && this.fieldPass != null
         && this.glowHorizontalPass != null
         && this.compositePass != null
         && this.motionBlurPass != null) {
         if (NanovisionConfig.thermalMarkerDebugShowRawMarker()) {
            if (this.rawMarkerDebugPass != null) {
               processPass(NanovisionPerformanceProfiler.Section.COMPOSITE, this.rawMarkerDebugPass, partialTick);
               blitColor(this.classicFinalTarget, minecraft.getMainRenderTarget());
            }
         } else {
            if (ThermalMarkerRenderTarget.hasSourcesRenderedThisFrame()) {
               processPass(NanovisionPerformanceProfiler.Section.HEAT_EXTRACT, this.heatExtractPass, partialTick);
               processPass(NanovisionPerformanceProfiler.Section.THERMAL_FIELD, this.fieldPass, partialTick);
               processPass(NanovisionPerformanceProfiler.Section.GLOW_HORIZONTAL, this.glowHorizontalPass, partialTick);
            } else {
               long clearCpuStarted = NanovisionPerformanceProfiler.beginCpu();
               int clearGpuQuery = NanovisionPerformanceProfiler.beginGpu(NanovisionPerformanceProfiler.Section.EMPTY_FIELD_CLEAR);
               this.classicThermalFieldTarget.bindWrite(false);
               RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
               RenderSystem.clear(16384, Minecraft.ON_OSX);
               NanovisionPerformanceProfiler.endGpu(NanovisionPerformanceProfiler.Section.EMPTY_FIELD_CLEAR, clearGpuQuery);
               NanovisionPerformanceProfiler.endCpu(NanovisionPerformanceProfiler.Section.EMPTY_FIELD_CLEAR, clearCpuStarted);
            }

            processPass(NanovisionPerformanceProfiler.Section.COMPOSITE, this.compositePass, partialTick);
            if (this.currentMotionBlurAmount < 0.001F) {
               long blitCpuStarted = NanovisionPerformanceProfiler.beginCpu();
               int blitGpuQuery = NanovisionPerformanceProfiler.beginGpu(NanovisionPerformanceProfiler.Section.FRAMEBUFFER_BLIT);
               blitColor(this.classicFinalTarget, minecraft.getMainRenderTarget());
               NanovisionPerformanceProfiler.endGpu(NanovisionPerformanceProfiler.Section.FRAMEBUFFER_BLIT, blitGpuQuery);
               NanovisionPerformanceProfiler.endCpu(NanovisionPerformanceProfiler.Section.FRAMEBUFFER_BLIT, blitCpuStarted);
            } else {
               processPass(NanovisionPerformanceProfiler.Section.MOTION_BLUR, this.motionBlurPass, partialTick);
            }
         }
      }
   }

   private void processProfiledFrame(float partialTick) {
      if (this.activePreset == ThermalVisualPreset.CINEMATIC) {
         processPass(NanovisionPerformanceProfiler.Section.HEAT_EXTRACT, this.heatExtractPass, partialTick);
         processPass(NanovisionPerformanceProfiler.Section.HEAT_DIFFUSE_VERTICAL, this.diffuseVerticalPass, partialTick);
         processPass(NanovisionPerformanceProfiler.Section.CINEMATIC_GEOMETRY, this.cinematicGeometryPass, partialTick);
         processPass(NanovisionPerformanceProfiler.Section.CINEMATIC_GEOMETRY_HORIZONTAL, this.cinematicGeometryHorizontalPass, partialTick);
         processPass(NanovisionPerformanceProfiler.Section.CINEMATIC_GEOMETRY_VERTICAL, this.cinematicGeometryVerticalPass, partialTick);
         processPass(NanovisionPerformanceProfiler.Section.COMPOSITE, this.compositePass, partialTick);
         processPass(NanovisionPerformanceProfiler.Section.MOTION_BLUR, this.motionBlurPass, partialTick);
      } else {
         processPass(NanovisionPerformanceProfiler.Section.HEAT_EXTRACT, this.heatExtractPass, partialTick);
         processPass(NanovisionPerformanceProfiler.Section.THERMAL_FIELD, this.fieldPass, partialTick);
         processPass(NanovisionPerformanceProfiler.Section.GLOW_HORIZONTAL, this.glowHorizontalPass, partialTick);
         processPass(NanovisionPerformanceProfiler.Section.COMPOSITE, this.compositePass, partialTick);
         processPass(NanovisionPerformanceProfiler.Section.MOTION_BLUR, this.motionBlurPass, partialTick);
      }
   }

   private static void processPass(NanovisionPerformanceProfiler.Section section, PostPass pass, float partialTick) {
      if (pass != null) {
         long cpuStarted = NanovisionPerformanceProfiler.beginCpu();
         int gpuQuery = NanovisionPerformanceProfiler.beginGpu(section);

         try {
            pass.process(partialTick);
         } finally {
            NanovisionPerformanceProfiler.endGpu(section, gpuQuery);
            NanovisionPerformanceProfiler.endCpu(section, cpuStarted);
         }
      }
   }

   private static void blitColor(RenderTarget source, RenderTarget destination) {
      GlStateManager._glBindFramebuffer(36008, source.frameBufferId);
      GlStateManager._glBindFramebuffer(36009, destination.frameBufferId);
      GL30.glBlitFramebuffer(0, 0, source.width, source.height, 0, 0, destination.width, destination.height, 16384, 9728);
      destination.bindWrite(false);
   }

   private void applyUniforms() {
      this.applyMotionBlurUniforms();
      if (this.compositePass != null) {
         applyActivationUniforms(this.compositePass.getEffect());
      }

      if (this.activePreset == ThermalVisualPreset.CINEMATIC) {
         this.applyCinematicUniforms();
      } else if (this.heatExtractPass != null && this.fieldPass != null && this.glowHorizontalPass != null && this.compositePass != null) {
         if (this.activePreset.usesOptimizedV3Pipeline()) {
            int signature = this.classicUniformSignature();
            if (signature == this.v3ClassicUniformSignature) {
               return;
            }

            this.v3ClassicUniformSignature = signature;
         }

         ThermalMarkerQuality quality = NanovisionConfig.thermalMarkerQuality();
         boolean reliefDebug = this.activePreset.usesImprovedRelief() && NanovisionConfig.thermalGeometryReliefDebugOnly();
         EffectInstance extract = this.heatExtractPass.getEffect();
         setUniform(extract, "HeatPower", (float)NanovisionConfig.thermalMarkerHeatPower());
         configureDiffusionPass(extract, quality, 1.0F, 0.0F);
         EffectInstance field = this.fieldPass.getEffect();
         setUniform(field, "EncodedHeightRangePx", ThermalScreenProjection.encodedHeightRangePx(this.activePreset));
         setUniform(field, "InnerRadiusRatio", (float)NanovisionConfig.thermalMarkerInnerRadiusRatio());
         setUniform(field, "MaxSearchRadiusPx", NanovisionConfig.thermalMarkerMaxSearchRadiusPx());
         setIntUniform(field, "ThermalQuality", quality.shaderQuality());
         setUniform(field, "DiffusionStrength", quality.diffusionStrength());
         setUniform(field, "TapSpacing", quality.diffusionTapSpacing());
         setUniform(field, "GeometryDepthStrength", (float)NanovisionConfig.thermalGeometryDepthStrength());
         setUniform(field, "GeometryDepthRadius", (float)NanovisionConfig.thermalGeometryDepthRadius());
         setUniform(field, "GeometryDepthContrast", (float)NanovisionConfig.thermalGeometryDepthContrast());
         setIntUniform(field, "ImprovedReliefMode", this.activePreset.improvedReliefMode());
         setIntUniform(field, "DebugGeometryReliefOnly", reliefDebug ? 1 : 0);
         setUniform(field, "ImprovedSilhouetteWidth", (float)NanovisionConfig.thermalImprovedSilhouetteWidth());
         setUniform(field, "ImprovedRimCurve", (float)NanovisionConfig.thermalImprovedRimCurve());
         setUniform(field, "ImprovedCoreHeat", (float)NanovisionConfig.thermalImprovedCoreHeat());
         setUniform(field, "ImprovedFacingStrength", (float)NanovisionConfig.thermalImprovedFacingStrength());
         setUniform(field, "ImprovedSmoothNormalStrength", (float)NanovisionConfig.thermalImprovedSmoothNormalStrength());
         setUniform(field, "ImprovedReliefScale", (float)NanovisionConfig.thermalImprovedReliefScale());
         setUniform(field, "ImprovedReliefThreshold", (float)NanovisionConfig.thermalImprovedReliefThreshold());
         setUniform(field, "ImprovedProtrusionCooling", (float)NanovisionConfig.thermalImprovedProtrusionCooling());
         setUniform(field, "ImprovedRecessCooling", (float)NanovisionConfig.thermalImprovedRecessCooling());
         setUniform(field, "ImprovedReliefSmoothing", (float)NanovisionConfig.thermalImprovedReliefSmoothing());
         setUniform(field, "ImprovedV2HotPeakArea", (float)NanovisionConfig.thermalImprovedV2HotPeakArea());
         configureGlowPass(this.glowHorizontalPass.getEffect(), quality, 1.0F, 0.0F);
         EffectInstance composite = this.compositePass.getEffect();
         setUniform(composite, "EncodedHeightRangePx", ThermalScreenProjection.encodedHeightRangePx(this.activePreset));
         setUniform(composite, "OuterGlowRatio", (float)NanovisionConfig.thermalMarkerOuterGlowRatio());
         setUniform(composite, "GlowRadiusMultiplier", quality.glowRadiusMultiplier());
         setIntUniform(composite, "ThermalQuality", quality.shaderQuality());
         setIntUniform(composite, "NanovisionMode", NanovisionController.getMode().ordinal());
         setIntUniform(composite, "ThermalPresetMode", this.activePreset.usesImprovedV2Model() ? 2 : 0);
         setUniform(composite, "InnerStrength", (float)NanovisionConfig.thermalMarkerInnerStrength());
         setUniform(composite, "ThermalModelOpacity", (float)NanovisionConfig.thermalMarkerModelOpacity());
         setUniform(composite, "HotBlockOpacity", (float)NanovisionConfig.thermalHotBlockOpacity());
         setUniform(composite, "OuterGlowStrength", (float)NanovisionConfig.thermalMarkerOuterGlowStrength());
         setUniform(composite, "GlowStrengthMultiplier", quality.glowStrengthMultiplier());
         setUniform(composite, "BandingStrength", (float)NanovisionConfig.thermalMarkerBandingStrength());
         setUniform(composite, "WarmGreenWidth", (float)NanovisionConfig.thermalWarmGreenWidth());
         setUniform(composite, "WarmRedWidth", (float)NanovisionConfig.thermalWarmRedWidth());
         setUniform(composite, "WarmPinkWidth", (float)NanovisionConfig.thermalWarmPinkWidth());
         setUniform(composite, "WarmWhiteWidth", (float)NanovisionConfig.thermalWarmWhiteWidth());
         setUniform(composite, "ScanlineStrength", (float)NanovisionConfig.thermalScanlineStrength());
         setUniform(composite, "ScanlineSpacingPx", (float)NanovisionConfig.thermalScanlineSpacingPx());
         setUniform(composite, "ScanlineSharpness", (float)NanovisionConfig.thermalScanlineSharpness());
         setUniform(composite, "ScanlineIrregularityPx", (float)NanovisionConfig.thermalScanlineIrregularityPx());
         setUniform(composite, "ScanlinePaletteShift", (float)NanovisionConfig.thermalScanlinePaletteShift());
         setUniform(composite, "ScanlineBreakup", (float)NanovisionConfig.thermalScanlineBreakup());
         setUniform(composite, "ScanlineWhiteHotSuppression", (float)NanovisionConfig.thermalScanlineWhiteHotSuppression());
         setIntUniform(composite, "DebugRawMarker", NanovisionController.isDebugMarkerMode() ? 1 : 0);
         setIntUniform(composite, "DebugDetectedMask", NanovisionConfig.thermalMarkerDebugShowDetectedMask() ? 1 : 0);
         setIntUniform(composite, "DebugDistance", NanovisionController.isDebugDistanceMode() ? 1 : 0);
         setIntUniform(composite, "DisablePalette", NanovisionConfig.thermalMarkerDebugDisablePalette() ? 1 : 0);
         setIntUniform(composite, "DisableGlow", NanovisionConfig.thermalMarkerDebugDisableGlow() ? 1 : 0);
         setIntUniform(composite, "DebugPassthrough", NanovisionConfig.nanovisionDebugPostPassthrough() ? 1 : 0);
         setIntUniform(composite, "DebugOpaqueGrayOnly", NanovisionConfig.nanovisionDebugOpaqueGrayOnly() ? 1 : 0);
         setIntUniform(composite, "DebugGeometryReliefOnly", reliefDebug ? 1 : 0);
      }
   }

   private int classicUniformSignature() {
      int hash = this.activePreset == null ? 0 : this.activePreset.ordinal();
      ThermalMarkerQuality quality = NanovisionConfig.thermalMarkerQuality();
      hash = mixSignature(hash, quality.ordinal());
      hash = mixSignature(hash, NanovisionController.getMode().ordinal());
      hash = mixSignature(hash, floatBits(ThermalScreenProjection.encodedHeightRangePx(this.activePreset)));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalMarkerHeatPower()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalMarkerInnerRadiusRatio()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalMarkerMaxSearchRadiusPx()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalGeometryDepthStrength()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalGeometryDepthRadius()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalGeometryDepthContrast()));
      hash = mixSignature(hash, NanovisionConfig.thermalGeometryReliefDebugOnly() ? 1 : 0);
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalImprovedSilhouetteWidth()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalImprovedRimCurve()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalImprovedCoreHeat()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalImprovedFacingStrength()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalImprovedSmoothNormalStrength()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalImprovedReliefScale()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalImprovedReliefThreshold()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalImprovedProtrusionCooling()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalImprovedRecessCooling()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalImprovedReliefSmoothing()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalImprovedV2HotPeakArea()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalMarkerOuterGlowRatio()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalMarkerInnerStrength()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalMarkerModelOpacity()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalHotBlockOpacity()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalMarkerOuterGlowStrength()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalMarkerBandingStrength()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalWarmGreenWidth()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalWarmRedWidth()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalWarmPinkWidth()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalWarmWhiteWidth()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalScanlineStrength()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalScanlineSpacingPx()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalScanlineSharpness()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalScanlineIrregularityPx()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalScanlinePaletteShift()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalScanlineBreakup()));
      hash = mixSignature(hash, floatBits(NanovisionConfig.thermalScanlineWhiteHotSuppression()));
      hash = mixSignature(hash, NanovisionController.isDebugMarkerMode() ? 1 : 0);
      hash = mixSignature(hash, NanovisionConfig.thermalMarkerDebugShowDetectedMask() ? 1 : 0);
      hash = mixSignature(hash, NanovisionController.isDebugDistanceMode() ? 1 : 0);
      hash = mixSignature(hash, NanovisionConfig.thermalMarkerDebugDisablePalette() ? 1 : 0);
      hash = mixSignature(hash, NanovisionConfig.thermalMarkerDebugDisableGlow() ? 1 : 0);
      hash = mixSignature(hash, NanovisionConfig.nanovisionDebugPostPassthrough() ? 1 : 0);
      return mixSignature(hash, NanovisionConfig.nanovisionDebugOpaqueGrayOnly() ? 1 : 0);
   }

   private static int floatBits(double value) {
      return Float.floatToIntBits((float)value);
   }

   private static int mixSignature(int hash, int value) {
      return hash * 31 + value;
   }

   private void updateMotionBlur(Minecraft minecraft) {
      float yaw = minecraft.gameRenderer.getMainCamera().getYRot();
      float pitch = minecraft.gameRenderer.getMainCamera().getXRot();
      if (!this.cameraStateInitialized) {
         this.lastCameraYaw = yaw;
         this.lastCameraPitch = pitch;
         this.cameraStateInitialized = true;
      }

      float yawDelta = Mth.wrapDegrees(yaw - this.lastCameraYaw);
      float pitchDelta = Mth.wrapDegrees(pitch - this.lastCameraPitch);
      this.lastCameraYaw = yaw;
      this.lastCameraPitch = pitch;
      float targetX = Mth.clamp(-yawDelta * 0.22F, -3.5F, 3.5F);
      float targetY = Mth.clamp(pitchDelta * 0.22F, -3.5F, 3.5F);
      float horizontalSpeed = minecraft.player == null ? 0.0F : (float)minecraft.player.getDeltaMovement().horizontalDistance();
      float targetRadial = Mth.clamp(horizontalSpeed * 5.0F, 0.0F, 2.25F);
      this.smoothedMotionX = Mth.lerp(0.42F, this.smoothedMotionX, targetX);
      this.smoothedMotionY = Mth.lerp(0.42F, this.smoothedMotionY, targetY);
      this.smoothedRadialMotion = Mth.lerp(0.3F, this.smoothedRadialMotion, targetRadial);
   }

   private void applyMotionBlurUniforms() {
      if (this.motionBlurPass != null) {
         EffectInstance effect = this.motionBlurPass.getEffect();
         boolean enabled = NanovisionConfig.nanovisionMotionBlurEnabled()
            && (this.activePreset == null || !this.activePreset.usesImprovedRelief() || !NanovisionConfig.thermalGeometryReliefDebugOnly());
         float configuredStrength = enabled ? (float)NanovisionConfig.nanovisionMotionBlurStrength() : 0.0F;
         float motionMagnitude = (float)Math.sqrt(this.smoothedMotionX * this.smoothedMotionX + this.smoothedMotionY * this.smoothedMotionY)
            + this.smoothedRadialMotion * 0.45F;
         float amount = Mth.clamp(motionMagnitude * 0.34F, 0.0F, 1.0F) * configuredStrength;
         this.currentMotionBlurAmount = amount;
         setVec2Uniform(effect, "MotionDirectionPx", this.smoothedMotionX, this.smoothedMotionY);
         setUniform(effect, "RadialMotionPx", this.smoothedRadialMotion);
         setUniform(effect, "MotionAmount", amount);
      }
   }

   private static void applyActivationUniforms(EffectInstance effect) {
      setUniform(effect, "ActivationFlash", NanovisionActivationOverlay.thermalFlashIntensity());
      setUniform(effect, "ActivationWorldFlashStrength", (float)NanovisionConfig.nanovisionActivationWorldFlashStrength());
   }

   private void applyCinematicUniforms() {
      if (this.heatExtractPass != null
         && this.diffuseVerticalPass != null
         && this.cinematicGeometryPass != null
         && this.cinematicGeometryHorizontalPass != null
         && this.cinematicGeometryVerticalPass != null
         && this.compositePass != null) {
         ThermalMarkerQuality quality = NanovisionConfig.thermalMarkerQuality();
         setUniform(this.heatExtractPass.getEffect(), "HeatPower", (float)NanovisionConfig.thermalMarkerHeatPower());
         configureDiffusionPass(this.heatExtractPass.getEffect(), quality, 1.0F, 0.0F);
         configureDiffusionPass(this.diffuseVerticalPass.getEffect(), quality, 0.0F, 1.0F);
         EffectInstance geometry = this.cinematicGeometryPass.getEffect();
         setUniform(geometry, "EncodedHeightRangePx", ThermalScreenProjection.encodedHeightRangePx(this.activePreset));
         setIntUniform(geometry, "ThermalQuality", quality.shaderQuality());
         configureCinematicGeometryPass(this.cinematicGeometryHorizontalPass.getEffect(), quality, 1.0F, 0.0F);
         configureCinematicGeometryPass(this.cinematicGeometryVerticalPass.getEffect(), quality, 0.0F, 1.0F);
         EffectInstance composite = this.compositePass.getEffect();
         setIntUniform(composite, "NanovisionMode", NanovisionController.getMode().ordinal());
         setUniform(composite, "InnerStrength", (float)NanovisionConfig.thermalMarkerInnerStrength());
         setUniform(composite, "ThermalModelOpacity", (float)NanovisionConfig.thermalMarkerModelOpacity());
         setUniform(composite, "HotBlockOpacity", (float)NanovisionConfig.thermalHotBlockOpacity());
         setUniform(composite, "OuterGlowStrength", (float)NanovisionConfig.thermalMarkerOuterGlowStrength());
         setUniform(composite, "GlowStrengthMultiplier", quality.glowStrengthMultiplier());
         setUniform(composite, "BandingStrength", (float)NanovisionConfig.thermalMarkerBandingStrength());
         setUniform(composite, "WarmGreenWidth", (float)NanovisionConfig.thermalWarmGreenWidth());
         setUniform(composite, "WarmRedWidth", (float)NanovisionConfig.thermalWarmRedWidth());
         setUniform(composite, "WarmPinkWidth", (float)NanovisionConfig.thermalWarmPinkWidth());
         setUniform(composite, "WarmWhiteWidth", (float)NanovisionConfig.thermalWarmWhiteWidth());
         setUniform(composite, "ScanlineStrength", (float)NanovisionConfig.thermalScanlineStrength());
         setUniform(composite, "ScanlineSpacingPx", (float)NanovisionConfig.thermalScanlineSpacingPx());
         setUniform(composite, "ScanlineSharpness", (float)NanovisionConfig.thermalScanlineSharpness());
         setUniform(composite, "ScanlineIrregularityPx", (float)NanovisionConfig.thermalScanlineIrregularityPx());
         setUniform(composite, "ScanlinePaletteShift", (float)NanovisionConfig.thermalScanlinePaletteShift());
         setUniform(composite, "ScanlineBreakup", (float)NanovisionConfig.thermalScanlineBreakup());
         setUniform(composite, "ScanlineWhiteHotSuppression", (float)NanovisionConfig.thermalScanlineWhiteHotSuppression());
         setIntUniform(composite, "DebugRawMarker", NanovisionController.isDebugMarkerMode() ? 1 : 0);
         setIntUniform(composite, "DebugDetectedMask", NanovisionConfig.thermalMarkerDebugShowDetectedMask() ? 1 : 0);
         setIntUniform(composite, "DebugDistance", NanovisionController.isDebugDistanceMode() ? 1 : 0);
         setIntUniform(composite, "DisablePalette", NanovisionConfig.thermalMarkerDebugDisablePalette() ? 1 : 0);
         setIntUniform(composite, "DisableGlow", NanovisionConfig.thermalMarkerDebugDisableGlow() ? 1 : 0);
         setIntUniform(composite, "DebugPassthrough", NanovisionConfig.nanovisionDebugPostPassthrough() ? 1 : 0);
         setIntUniform(composite, "DebugOpaqueGrayOnly", NanovisionConfig.nanovisionDebugOpaqueGrayOnly() ? 1 : 0);
      }
   }

   private static void configureCinematicGeometryPass(EffectInstance effect, ThermalMarkerQuality quality, float directionX, float directionY) {
      setVec2Uniform(effect, "Direction", directionX, directionY);
      setUniform(effect, "EncodedHeightRangePx", ThermalScreenProjection.encodedHeightRangePx());
      setUniform(effect, "EdgeWidthRatio", (float)NanovisionConfig.thermalMarkerInnerRadiusRatio());
      setUniform(effect, "OuterGlowRatio", (float)NanovisionConfig.thermalMarkerOuterGlowRatio());
      setUniform(effect, "GlowRadiusMultiplier", quality.glowRadiusMultiplier());
      setIntUniform(effect, "ThermalQuality", quality.shaderQuality());
   }

   private static void configureDiffusionPass(EffectInstance effect, ThermalMarkerQuality quality, float directionX, float directionY) {
      setVec2Uniform(effect, "Direction", directionX, directionY);
      setIntUniform(effect, "ThermalQuality", quality.shaderQuality());
      setUniform(effect, "DiffusionStrength", quality.diffusionStrength());
      setUniform(effect, "TapSpacing", quality.diffusionTapSpacing());
   }

   private static void configureGlowPass(EffectInstance effect, ThermalMarkerQuality quality, float directionX, float directionY) {
      setVec2Uniform(effect, "Direction", directionX, directionY);
      setUniform(effect, "EncodedHeightRangePx", ThermalScreenProjection.encodedHeightRangePx());
      setUniform(effect, "OuterGlowRatio", (float)NanovisionConfig.thermalMarkerOuterGlowRatio());
      setUniform(effect, "GlowRadiusMultiplier", quality.glowRadiusMultiplier());
      setIntUniform(effect, "ThermalQuality", quality.shaderQuality());
   }

   private static void setUniform(EffectInstance effect, String name, float value) {
      if (effect.getUniform(name) != null) {
         effect.getUniform(name).set(value);
      }
   }

   private static void setIntUniform(EffectInstance effect, String name, int value) {
      if (effect.getUniform(name) != null) {
         effect.getUniform(name).set(value);
      }
   }

   private static void setVec2Uniform(EffectInstance effect, String name, float x, float y) {
      if (effect.getUniform(name) != null) {
         effect.getUniform(name).set(x, y);
      }
   }

   private void closeForInactiveFrame() {
      if (this.chain != null) {
         int var1 = GL11.glGetInteger(36010);
         int var2 = GL11.glGetInteger(36006);
         this.close();
         GlStateManager._glBindFramebuffer(36008, var1);
         GlStateManager._glBindFramebuffer(36009, var2);
      }
   }
}
