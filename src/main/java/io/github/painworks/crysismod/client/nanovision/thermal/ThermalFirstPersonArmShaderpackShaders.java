package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanovision.NanovisionActivationOverlay;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import java.io.IOException;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.slf4j.Logger;

public final class ThermalFirstPersonArmShaderpackShaders {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation ARM_SHADERPACK_COMPOSITE_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_first_person_arm_shaderpack_composite");
   private static volatile ShaderInstance armShaderpackCompositeShader;

   private ThermalFirstPersonArmShaderpackShaders() {
   }

   public static void register(RegisterShadersEvent event) {
      try {
         event.registerShader(
            new ShaderInstance(event.getResourceProvider(), ARM_SHADERPACK_COMPOSITE_SHADER, DefaultVertexFormat.POSITION_TEX),
            shader -> armShaderpackCompositeShader = shader
         );
      } catch (IOException var2) {
         armShaderpackCompositeShader = null;
         LOGGER.error("[CrysisMod][Nanovision] failed to register shaderpack-only arm composite {}", ARM_SHADERPACK_COMPOSITE_SHADER, var2);
      }
   }

   public static ShaderInstance armCompositeShader() {
      return armShaderpackCompositeShader;
   }

   public static boolean isAvailable() {
      return armShaderpackCompositeShader != null;
   }

   public static void prepareComposite(int maskTextureId, int depthTextureId, int width, int height, float armProjectionScale) {
      ShaderInstance shader = armShaderpackCompositeShader;
      if (shader != null) {
         shader.setSampler("MaskSampler", maskTextureId);
         shader.setSampler("ArmDepthSampler", depthTextureId);
         ThermalFirstPersonArmOcclusionDepth.prepareComposite(shader, depthTextureId);
         set(shader, "InSize", width, height);
         set(shader, "ActivationFlash", NanovisionActivationOverlay.thermalFlashIntensity());
         setInt(shader, "ThermalQuality", NanovisionConfig.thermalMarkerQuality().shaderQuality());
         set(shader, "ArmProjectionScale", sanitizeArmProjectionScale(armProjectionScale));
         set(shader, "ThermalModelOpacity", (float)NanovisionConfig.thermalMarkerModelOpacity());
         set(shader, "InnerStrength", (float)NanovisionConfig.thermalMarkerInnerStrength());
         set(shader, "InnerRadiusRatio", (float)NanovisionConfig.thermalMarkerInnerRadiusRatio());
         set(shader, "OuterGlowRatio", (float)NanovisionConfig.thermalMarkerOuterGlowRatio());
         set(shader, "OuterGlowStrength", NanovisionConfig.thermalMarkerDebugDisableGlow() ? 0.0F : (float)NanovisionConfig.thermalMarkerOuterGlowStrength());
         set(shader, "HeatPower", (float)NanovisionConfig.thermalMarkerHeatPower());
         set(shader, "BandingStrength", (float)NanovisionConfig.thermalMarkerBandingStrength());
         set(shader, "WarmGreenWidth", (float)NanovisionConfig.thermalWarmGreenWidth());
         set(shader, "WarmRedWidth", (float)NanovisionConfig.thermalWarmRedWidth());
         set(shader, "WarmPinkWidth", (float)NanovisionConfig.thermalWarmPinkWidth());
         set(shader, "WarmWhiteWidth", (float)NanovisionConfig.thermalWarmWhiteWidth());
         set(shader, "ScanlineStrength", (float)NanovisionConfig.thermalScanlineStrength());
         set(shader, "ScanlineSpacingPx", (float)NanovisionConfig.thermalScanlineSpacingPx());
         set(shader, "ScanlineSharpness", (float)NanovisionConfig.thermalScanlineSharpness());
         set(shader, "ScanlineIrregularityPx", (float)NanovisionConfig.thermalScanlineIrregularityPx());
         set(shader, "ScanlinePaletteShift", (float)NanovisionConfig.thermalScanlinePaletteShift());
         set(shader, "ScanlineBreakup", (float)NanovisionConfig.thermalScanlineBreakup());
         set(shader, "ScanlineWhiteHotSuppression", (float)NanovisionConfig.thermalScanlineWhiteHotSuppression());
         setInt(shader, "ThermalPresetMode", NanovisionConfig.thermalVisualPreset().usesImprovedV2Model() ? 2 : 0);
         set(shader, "GeometryDepthStrength", (float)NanovisionConfig.thermalGeometryDepthStrength());
         set(shader, "ImprovedSilhouetteWidth", (float)NanovisionConfig.thermalImprovedSilhouetteWidth());
         set(shader, "ImprovedRimCurve", (float)NanovisionConfig.thermalImprovedRimCurve());
         set(shader, "ImprovedCoreHeat", (float)NanovisionConfig.thermalImprovedCoreHeat());
         set(shader, "ImprovedFacingStrength", (float)NanovisionConfig.thermalImprovedFacingStrength());
         set(shader, "ImprovedSmoothNormalStrength", (float)NanovisionConfig.thermalImprovedSmoothNormalStrength());
         set(shader, "ImprovedReliefScale", (float)NanovisionConfig.thermalImprovedReliefScale());
         set(shader, "ImprovedReliefThreshold", (float)NanovisionConfig.thermalImprovedReliefThreshold());
         set(shader, "ImprovedProtrusionCooling", (float)NanovisionConfig.thermalImprovedProtrusionCooling());
         set(shader, "ImprovedRecessCooling", (float)NanovisionConfig.thermalImprovedRecessCooling());
         set(shader, "ImprovedReliefSmoothing", (float)NanovisionConfig.thermalImprovedReliefSmoothing());
         set(shader, "ImprovedV2HotPeakArea", (float)NanovisionConfig.thermalImprovedV2HotPeakArea());
      }
   }

   private static float sanitizeArmProjectionScale(float value) {
      return !Float.isFinite(value) ? 1.0F : Math.max(0.45F, Math.min(3.5F, value));
   }

   private static void set(ShaderInstance shader, String name, float value) {
      Uniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }

   private static void set(ShaderInstance shader, String name, float x, float y) {
      Uniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(x, y);
      }
   }

   private static void setInt(ShaderInstance shader, String name, int value) {
      Uniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }
}
