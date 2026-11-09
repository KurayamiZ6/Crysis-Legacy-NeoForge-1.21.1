package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanovision.NanovisionActivationOverlay;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.slf4j.Logger;

public final class ThermalFirstPersonArmShaders {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation WARM_ARM_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_first_person_arm");
   private static final ResourceLocation ARM_COMPOSITE_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_first_person_arm_composite");
   private static volatile ShaderInstance warmArmShader;
   private static volatile ShaderInstance armCompositeShader;

   private ThermalFirstPersonArmShaders() {
   }

   public static void register(RegisterShadersEvent event) {
      try {
         event.registerShader(
            new ShaderInstance(event.getResourceProvider(), WARM_ARM_SHADER, DefaultVertexFormat.NEW_ENTITY), shader -> warmArmShader = shader
         );
      } catch (IOException var3) {
         warmArmShader = null;
         LOGGER.error("[CrysisMod][Nanovision] failed to register first-person thermal arm shader {}", WARM_ARM_SHADER, var3);
      }

      try {
         event.registerShader(
            new ShaderInstance(event.getResourceProvider(), ARM_COMPOSITE_SHADER, DefaultVertexFormat.POSITION_TEX), shader -> armCompositeShader = shader
         );
      } catch (IOException var2) {
         armCompositeShader = null;
         LOGGER.error("[CrysisMod][Nanovision] failed to register first-person thermal arm composite shader {}", ARM_COMPOSITE_SHADER, var2);
      }
   }

   public static ShaderInstance warmArmShader() {
      return warmArmShader;
   }

   public static ShaderInstance armCompositeShader() {
      return armCompositeShader;
   }

   public static boolean isAvailable() {
      return warmArmShader != null && armCompositeShader != null;
   }

   public static void prepareComposite(int maskTextureId, int depthTextureId, int width, int height, float armProjectionScale) {
      ShaderInstance shader = armCompositeShader;
      if (shader != null) {
         shader.setSampler("MaskSampler", maskTextureId);
         shader.setSampler("ArmDepthSampler", depthTextureId);
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

   public static float captureCurrentArmProjectionScale() {
      Minecraft minecraft = Minecraft.getInstance();
      double baseFov = ((Integer)minecraft.options.fov().get()).intValue();
      double baseProjectionScale = 1.0 / Math.tan(Math.toRadians(baseFov) * 0.5);
      if (RenderSystem.isOnRenderThread() && !(baseProjectionScale <= 1.0E-4)) {
         float activeProjectionScale = Math.abs(RenderSystem.getProjectionMatrix().m11());
         return Float.isFinite(activeProjectionScale) && !(activeProjectionScale < 0.05F) && !(activeProjectionScale > 100.0F)
            ? (float)Math.max(0.45, Math.min(3.5, activeProjectionScale / baseProjectionScale))
            : 1.0F;
      } else {
         return 1.0F;
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
