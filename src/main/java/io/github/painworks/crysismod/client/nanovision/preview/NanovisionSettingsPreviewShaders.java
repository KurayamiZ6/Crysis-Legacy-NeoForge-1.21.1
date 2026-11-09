package io.github.painworks.crysismod.client.nanovision.preview;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.ThermalVisualPreset;
import java.io.IOException;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.slf4j.Logger;

public final class NanovisionSettingsPreviewShaders {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation PREVIEW_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "nanovision_settings_preview");
   private static volatile ShaderInstance shader;

   private NanovisionSettingsPreviewShaders() {
   }

   public static void register(RegisterShadersEvent event) {
      try {
         event.registerShader(new ShaderInstance(event.getResourceProvider(), PREVIEW_SHADER, DefaultVertexFormat.POSITION_TEX), loaded -> shader = loaded);
      } catch (IOException var2) {
         shader = null;
         LOGGER.error("[CrysisMod][Nanovision] failed to register settings preview shader {}", PREVIEW_SHADER, var2);
      }
   }

   public static ShaderInstance shader() {
      return shader;
   }

   public static void prepare(RenderTarget source) {
      ShaderInstance current = shader;
      if (current != null && source != null) {
         current.setSampler("SourceSampler", source.getColorTextureId());
         current.setSampler("DepthSampler", source.getDepthTextureId());
         setFloat2(current, "InSize", source.width, source.height);
         setInt(current, "MarkerEnabled", NanovisionConfig.thermalMarkerEnabled() ? 1 : 0);
         setFloat(current, "InnerRadiusRatio", (float)NanovisionConfig.thermalMarkerInnerRadiusRatio());
         setFloat(current, "OuterGlowRatio", (float)NanovisionConfig.thermalMarkerOuterGlowRatio());
         setFloat(current, "InnerStrength", (float)NanovisionConfig.thermalMarkerInnerStrength());
         setFloat(current, "ModelOpacity", (float)NanovisionConfig.thermalMarkerModelOpacity());
         setFloat(
            current, "OuterGlowStrength", NanovisionConfig.thermalMarkerDebugDisableGlow() ? 0.0F : (float)NanovisionConfig.thermalMarkerOuterGlowStrength()
         );
         setFloat(current, "HeatPower", (float)NanovisionConfig.thermalMarkerHeatPower());
         setFloat(current, "BandingStrength", (float)NanovisionConfig.thermalMarkerBandingStrength());
         setFloat(current, "WarmGreenWidth", (float)NanovisionConfig.thermalWarmGreenWidth());
         setFloat(current, "WarmRedWidth", (float)NanovisionConfig.thermalWarmRedWidth());
         setFloat(current, "WarmPinkWidth", (float)NanovisionConfig.thermalWarmPinkWidth());
         setFloat(current, "WarmWhiteWidth", (float)NanovisionConfig.thermalWarmWhiteWidth());
         setFloat(current, "MaxSearchRadiusPx", NanovisionConfig.thermalMarkerMaxSearchRadiusPx());
         setInt(current, "ThermalQuality", NanovisionConfig.thermalMarkerQuality().shaderQuality());
         setInt(current, "CinematicPreset", NanovisionConfig.thermalVisualPreset() == ThermalVisualPreset.CINEMATIC ? 1 : 0);
         setInt(current, "ThermalPresetMode", NanovisionConfig.thermalVisualPreset().usesImprovedV2Model() ? 2 : 0);
         setFloat(current, "ImprovedV2HotPeakArea", (float)NanovisionConfig.thermalImprovedV2HotPeakArea());
         setFloat(current, "ScanlineStrength", (float)NanovisionConfig.thermalScanlineStrength());
         setFloat(current, "ScanlineSpacingPx", (float)NanovisionConfig.thermalScanlineSpacingPx());
         setFloat(current, "ScanlineSharpness", (float)NanovisionConfig.thermalScanlineSharpness());
         setFloat(current, "ScanlineIrregularityPx", (float)NanovisionConfig.thermalScanlineIrregularityPx());
         setFloat(current, "ScanlinePaletteShift", (float)NanovisionConfig.thermalScanlinePaletteShift());
         setFloat(current, "ScanlineBreakup", (float)NanovisionConfig.thermalScanlineBreakup());
         setFloat(current, "ScanlineWhiteHotSuppression", (float)NanovisionConfig.thermalScanlineWhiteHotSuppression());
         setInt(current, "DebugRawMarker", NanovisionConfig.thermalMarkerDebugShowRawMarker() ? 1 : 0);
         setInt(current, "DebugMask", NanovisionConfig.thermalMarkerDebugShowDetectedMask() ? 1 : 0);
         setInt(current, "DebugDistance", NanovisionConfig.thermalMarkerDebugShowDistance() ? 1 : 0);
         setInt(current, "DisablePalette", NanovisionConfig.thermalMarkerDebugDisablePalette() ? 1 : 0);
      }
   }

   private static void setFloat(ShaderInstance target, String name, float value) {
      Uniform uniform = target.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }

   private static void setFloat2(ShaderInstance target, String name, float x, float y) {
      Uniform uniform = target.getUniform(name);
      if (uniform != null) {
         uniform.set(x, y);
      }
   }

   private static void setInt(ShaderInstance target, String name, int value) {
      Uniform uniform = target.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }
}
