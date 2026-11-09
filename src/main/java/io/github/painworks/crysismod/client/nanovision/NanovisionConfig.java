package io.github.painworks.crysismod.client.nanovision;

import io.github.painworks.crysismod.client.nanovision.thermal.ThermalEmissionClass;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalEntityEffectMode;
import java.util.List;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class NanovisionConfig {
   private static final Builder BUILDER = new Builder();
   public static final boolean DEFAULT_NANOVISION_ENABLED = true;
   public static final boolean DEFAULT_NANOVISION_REQUIRES_HELMET = true;
   public static final boolean DEFAULT_NANOVISION_REQUIRES_FULL_SET = false;
   public static final boolean DEFAULT_NANOVISION_USE_NIGHT_VISION = true;
   public static final NanovisionMode WORKING_NANOVISION_MODE = NanovisionMode.THERMAL_MARKER;
   public static final boolean DEFAULT_NANOVISION_DEBUG = false;
   public static final boolean DEFAULT_POST_SHADER_ENABLED = true;
   public static final boolean DEFAULT_NANOVISION_MOTION_BLUR_ENABLED = true;
   public static final double DEFAULT_NANOVISION_MOTION_BLUR_STRENGTH = 0.22;
   public static final boolean DEFAULT_NANOVISION_ACTIVATION_WAVE_ENABLED = false;
   public static final double DEFAULT_NANOVISION_ACTIVATION_WORLD_FLASH_STRENGTH = 0.19789719626168223;
   public static final int DEFAULT_NANOVISION_ENABLED_TEXT_OFFSET_X = 0;
   public static final int DEFAULT_NANOVISION_ENABLED_TEXT_OFFSET_Y = 103;
   public static final boolean DEFAULT_NANOVISION_DEBUG_POST_PASSTHROUGH = false;
   public static final boolean DEFAULT_NANOVISION_DEBUG_OPAQUE_GRAY_ONLY = false;
   public static final boolean DEFAULT_THERMAL_MARKER_ENABLED = true;
   public static final ThermalEntityEffectMode WORKING_THERMAL_ENTITY_EFFECT_MODE = ThermalEntityEffectMode.SPECTRAL_MARKER;
   public static final double DEFAULT_THERMAL_MARKER_MAX_DISTANCE = 96.0;
   public static final boolean DEFAULT_THERMAL_MARKER_RENDER_PLAYERS = true;
   public static final boolean DEFAULT_THERMAL_MARKER_RENDER_ANIMALS = true;
   public static final boolean DEFAULT_THERMAL_MARKER_RENDER_MONSTERS = true;
   public static final boolean DEFAULT_THERMAL_MARKER_RENDER_INVISIBLE = false;
   public static final boolean DEFAULT_THERMAL_MARKER_SKIP_FIRST_PERSON_PLAYER = true;
   public static final List<String> DEFAULT_THERMAL_ENTITY_CLASS_OVERRIDES = List.of();
   public static final double DEFAULT_THERMAL_MARKER_INNER_RADIUS_RATIO = 0.48;
   public static final double DEFAULT_THERMAL_GEOMETRY_DEPTH_STRENGTH = 1.0;
   public static final double DEFAULT_THERMAL_GEOMETRY_DEPTH_RADIUS = 1.35;
   public static final double DEFAULT_THERMAL_GEOMETRY_DEPTH_CONTRAST = 1.15;
   public static final double DEFAULT_THERMAL_IMPROVED_SILHOUETTE_WIDTH = 0.9;
   public static final double DEFAULT_THERMAL_IMPROVED_RIM_CURVE = 0.82;
   public static final double DEFAULT_THERMAL_IMPROVED_CORE_HEAT = 0.79;
   public static final double DEFAULT_THERMAL_IMPROVED_FACING_STRENGTH = 0.08;
   public static final double DEFAULT_THERMAL_IMPROVED_SMOOTH_NORMAL_STRENGTH = 0.92;
   public static final double DEFAULT_THERMAL_IMPROVED_RELIEF_SCALE = 1.15;
   public static final double DEFAULT_THERMAL_IMPROVED_RELIEF_THRESHOLD = 1.0;
   public static final double DEFAULT_THERMAL_IMPROVED_PROTRUSION_COOLING = 0.12;
   public static final double DEFAULT_THERMAL_IMPROVED_RECESS_COOLING = 0.1;
   public static final double DEFAULT_THERMAL_IMPROVED_RELIEF_SMOOTHING = 0.88;
   public static final double DEFAULT_THERMAL_IMPROVED_V2_HOT_PEAK_AREA = 0.8660436137071651;
   public static final double DEFAULT_THERMAL_MARKER_OUTER_GLOW_RATIO = 0.06;
   public static final double DEFAULT_THERMAL_MARKER_INNER_STRENGTH = 1.0;
   public static final double DEFAULT_THERMAL_MARKER_MODEL_OPACITY = 0.9;
   public static final double DEFAULT_THERMAL_MARKER_OUTER_GLOW_STRENGTH = 0.66;
   public static final double DEFAULT_THERMAL_MARKER_HEAT_POWER = 2.46;
   public static final double DEFAULT_THERMAL_MARKER_BANDING_STRENGTH = 0.18;
   public static final double DEFAULT_THERMAL_WARM_GREEN_WIDTH = 4.0;
   public static final double DEFAULT_THERMAL_WARM_RED_WIDTH = 0.9;
   public static final double DEFAULT_THERMAL_WARM_PINK_WIDTH = 0.95;
   public static final double DEFAULT_THERMAL_WARM_WHITE_WIDTH = 0.55;
   public static final int DEFAULT_THERMAL_MARKER_MAX_SEARCH_RADIUS_PX = 80;
   public static final ThermalMarkerQuality DEFAULT_THERMAL_MARKER_QUALITY = ThermalMarkerQuality.BALANCED;
   public static final ThermalVisualPreset DEFAULT_THERMAL_VISUAL_PRESET = ThermalVisualPreset.CLASSIC_IMPROVED_V3;
   public static final double DEFAULT_THERMAL_SCANLINE_STRENGTH = 0.08;
   public static final double DEFAULT_THERMAL_SCANLINE_SPACING_PX = 4.0;
   public static final double DEFAULT_THERMAL_SCANLINE_SHARPNESS = 2.4;
   public static final double DEFAULT_THERMAL_SCANLINE_IRREGULARITY_PX = 0.55;
   public static final double DEFAULT_THERMAL_SCANLINE_PALETTE_SHIFT = 0.11;
   public static final double DEFAULT_THERMAL_SCANLINE_BREAKUP = 0.96;
   public static final double DEFAULT_THERMAL_SCANLINE_WHITE_HOT_SUPPRESSION = 0.96;
   public static final boolean DEFAULT_THERMAL_HOT_BLOCKS_ENABLED = false;
   public static final int DEFAULT_THERMAL_HOT_BLOCK_MAX_DISTANCE = 32;
   public static final double DEFAULT_THERMAL_HOT_BLOCK_OPACITY = 0.88;
   public static final boolean DEFAULT_THERMAL_HELD_ITEMS_ENABLED = true;
   public static final boolean DEFAULT_THERMAL_HELD_ITEMS_SCREEN_SPACE_EXPERIMENTAL = false;
   public static final boolean DEFAULT_THERMAL_HELD_ITEMS_SCREEN_SPACE_DEBUG_MASK = false;
   public static final double DEFAULT_THERMAL_HELD_ITEM_CONTACT_HEAT = 0.32;
   public static final double DEFAULT_THERMAL_HELD_ITEM_OUTER_GLOW_RATIO = 0.02;
   public static final double DEFAULT_THERMAL_HELD_ITEM_OUTER_GLOW_STRENGTH = 1.0;
   public static final boolean DEFAULT_THERMAL_MARKER_COMPATIBILITY_ENTITY_PASS = true;
   public static final boolean DEFAULT_THERMAL_MARKER_DEBUG_SHOW_RAW_MARKER = false;
   public static final boolean DEFAULT_THERMAL_MARKER_DEBUG_SHOW_DETECTED_MASK = false;
   public static final boolean DEFAULT_THERMAL_MARKER_DEBUG_SHOW_DISTANCE = false;
   public static final boolean DEFAULT_THERMAL_MARKER_DEBUG_DISABLE_GLOW = false;
   public static final boolean DEFAULT_THERMAL_MARKER_DEBUG_DISABLE_PALETTE = false;
   public static final boolean DEFAULT_THERMAL_GEOMETRY_RELIEF_DEBUG_ONLY = false;
   public static final boolean DEFAULT_THERMAL_MARKER_DEBUG_LOG = false;
   private static final BooleanValue NANOVISION_ENABLED = BUILDER.define("nanovisionEnabled", true);
   private static final BooleanValue NANOVISION_REQUIRES_HELMET = BUILDER.define("nanovisionRequiresHelmet", true);
   private static final BooleanValue NANOVISION_REQUIRES_FULL_SET = BUILDER.define("nanovisionRequiresFullSet", false);
   private static final BooleanValue NANOVISION_USE_NIGHT_VISION = BUILDER.define("nanovisionUseNightVision", true);
   private static final BooleanValue NANOVISION_DEBUG = BUILDER.define("nanovisionDebug", false);
   private static final BooleanValue POST_SHADER_ENABLED = BUILDER.define("nanovisionPostShaderEnabled", true);
   private static final BooleanValue NANOVISION_MOTION_BLUR_ENABLED = BUILDER.define("nanovisionMotionBlurEnabled", true);
   private static final DoubleValue NANOVISION_MOTION_BLUR_STRENGTH = BUILDER.defineInRange("nanovisionMotionBlurStrength", 0.22, 0.0, 1.0);
   private static final BooleanValue NANOVISION_ACTIVATION_WAVE_ENABLED = BUILDER.define("nanovisionActivationWaveEnabled", false);
   private static final DoubleValue NANOVISION_ACTIVATION_WORLD_FLASH_STRENGTH = BUILDER.defineInRange(
      "nanovisionActivationWorldFlashStrength", 0.19789719626168223, 0.0, 0.35
   );
   private static final IntValue NANOVISION_ENABLED_TEXT_OFFSET_X = BUILDER.defineInRange("nanovisionEnabledTextOffsetX", 0, -4096, 4096);
   private static final IntValue NANOVISION_ENABLED_TEXT_OFFSET_Y = BUILDER.defineInRange("nanovisionEnabledTextOffsetY", 103, -4096, 4096);
   private static final BooleanValue NANOVISION_DEBUG_POST_PASSTHROUGH = BUILDER.define("nanovisionDebugPostPassthrough", false);
   private static final BooleanValue NANOVISION_DEBUG_OPAQUE_GRAY_ONLY = BUILDER.define("nanovisionDebugOpaqueGrayOnly", false);
   private static final BooleanValue THERMAL_MARKER_ENABLED = BUILDER.define("thermalMarkerEnabled", true);
   private static final DoubleValue THERMAL_MARKER_MAX_DISTANCE = BUILDER.defineInRange("thermalMarkerMaxDistance", 96.0, 1.0, 256.0);
   private static final BooleanValue THERMAL_MARKER_RENDER_PLAYERS = BUILDER.define("thermalMarkerRenderPlayers", true);
   private static final BooleanValue THERMAL_MARKER_RENDER_ANIMALS = BUILDER.define("thermalMarkerRenderAnimals", true);
   private static final BooleanValue THERMAL_MARKER_RENDER_MONSTERS = BUILDER.define("thermalMarkerRenderMonsters", true);
   private static final BooleanValue THERMAL_MARKER_RENDER_INVISIBLE = BUILDER.define("thermalMarkerRenderInvisible", false);
   private static final BooleanValue THERMAL_MARKER_SKIP_FIRST_PERSON_PLAYER = BUILDER.define("thermalMarkerSkipFirstPersonPlayer", true);
   private static final ConfigValue<List<? extends String>> THERMAL_ENTITY_CLASS_OVERRIDES = BUILDER.comment(
         "Optional entity thermal-class overrides in registry_id=CLASS or namespace:*=CLASS form"
      )
      .defineListAllowEmpty("thermalEntityClassOverrides", DEFAULT_THERMAL_ENTITY_CLASS_OVERRIDES, NanovisionConfig::isValidThermalEntityClassOverride);
   private static final DoubleValue THERMAL_MARKER_INNER_RADIUS_RATIO = BUILDER.defineInRange("thermalMarkerInnerRadiusRatio", 0.48, 0.05, 1.0);
   private static final DoubleValue THERMAL_GEOMETRY_DEPTH_STRENGTH = BUILDER.defineInRange("thermalGeometryDepthStrength", 1.0, 0.0, 2.0);
   private static final DoubleValue THERMAL_GEOMETRY_DEPTH_RADIUS = BUILDER.defineInRange("thermalGeometryDepthRadius", 1.35, 0.5, 3.0);
   private static final DoubleValue THERMAL_GEOMETRY_DEPTH_CONTRAST = BUILDER.defineInRange("thermalGeometryDepthContrast", 1.15, 0.25, 3.0);
   private static final DoubleValue THERMAL_IMPROVED_SILHOUETTE_WIDTH = BUILDER.defineInRange("thermalImprovedSilhouetteWidth", 0.9, 0.4, 2.0);
   private static final DoubleValue THERMAL_IMPROVED_RIM_CURVE = BUILDER.defineInRange("thermalImprovedRimCurve", 0.82, 0.3, 1.5);
   private static final DoubleValue THERMAL_IMPROVED_CORE_HEAT = BUILDER.defineInRange("thermalImprovedCoreHeat", 0.79, 0.55, 0.98);
   private static final DoubleValue THERMAL_IMPROVED_FACING_STRENGTH = BUILDER.defineInRange("thermalImprovedFacingStrength", 0.08, 0.0, 0.55);
   private static final DoubleValue THERMAL_IMPROVED_SMOOTH_NORMAL_STRENGTH = BUILDER.defineInRange("thermalImprovedSmoothNormalStrength", 0.92, 0.0, 1.0);
   private static final DoubleValue THERMAL_IMPROVED_RELIEF_SCALE = BUILDER.defineInRange("thermalImprovedReliefScale", 1.15, 0.5, 3.0);
   private static final DoubleValue THERMAL_IMPROVED_RELIEF_THRESHOLD = BUILDER.defineInRange("thermalImprovedReliefThreshold", 1.0, 0.5, 3.0);
   private static final DoubleValue THERMAL_IMPROVED_PROTRUSION_COOLING = BUILDER.defineInRange("thermalImprovedProtrusionCooling", 0.12, 0.0, 0.5);
   private static final DoubleValue THERMAL_IMPROVED_RECESS_COOLING = BUILDER.defineInRange("thermalImprovedRecessCooling", 0.1, 0.0, 0.4);
   private static final DoubleValue THERMAL_IMPROVED_RELIEF_SMOOTHING = BUILDER.defineInRange("thermalImprovedReliefSmoothing", 0.88, 0.0, 1.0);
   private static final DoubleValue THERMAL_IMPROVED_V2_HOT_PEAK_AREA = BUILDER.defineInRange("thermalImprovedV2HotPeakArea", 0.8660436137071651, 0.0, 1.0);
   private static final DoubleValue THERMAL_MARKER_OUTER_GLOW_RATIO = BUILDER.defineInRange("thermalMarkerOuterGlowRatio", 0.06, 0.0, 0.2);
   private static final DoubleValue THERMAL_MARKER_INNER_STRENGTH = BUILDER.defineInRange("thermalMarkerInnerStrength", 1.0, 0.0, 1.0);
   private static final DoubleValue THERMAL_MARKER_MODEL_OPACITY = BUILDER.defineInRange("thermalMarkerModelOpacity", 0.9, 0.0, 1.0);
   private static final DoubleValue THERMAL_MARKER_OUTER_GLOW_STRENGTH = BUILDER.defineInRange("thermalMarkerOuterGlowStrength", 0.66, 0.0, 1.0);
   private static final DoubleValue THERMAL_MARKER_HEAT_POWER = BUILDER.defineInRange("thermalMarkerHeatPower", 2.46, 0.25, 4.0);
   private static final DoubleValue THERMAL_MARKER_BANDING_STRENGTH = BUILDER.defineInRange("thermalMarkerBandingStrength", 0.18, 0.0, 1.0);
   private static final DoubleValue THERMAL_WARM_GREEN_WIDTH = BUILDER.defineInRange("thermalWarmGreenWidth", 4.0, 0.25, 4.0);
   private static final DoubleValue THERMAL_WARM_RED_WIDTH = BUILDER.defineInRange("thermalWarmRedWidth", 0.9, 0.25, 4.0);
   private static final DoubleValue THERMAL_WARM_PINK_WIDTH = BUILDER.defineInRange("thermalWarmPinkWidth", 0.95, 0.25, 4.0);
   private static final DoubleValue THERMAL_WARM_WHITE_WIDTH = BUILDER.defineInRange("thermalWarmWhiteWidth", 0.55, 0.25, 4.0);
   private static final IntValue THERMAL_MARKER_MAX_SEARCH_RADIUS_PX = BUILDER.defineInRange("thermalMarkerMaxSearchRadiusPx", 80, 8, 96);
   private static final EnumValue<ThermalMarkerQuality> THERMAL_MARKER_QUALITY = BUILDER.defineEnum("thermalMarkerQuality", DEFAULT_THERMAL_MARKER_QUALITY);
   private static final EnumValue<ThermalVisualPreset> THERMAL_VISUAL_PRESET = BUILDER.defineEnum("thermalVisualPreset", DEFAULT_THERMAL_VISUAL_PRESET);
   private static final DoubleValue THERMAL_SCANLINE_STRENGTH = BUILDER.defineInRange("thermalScanlineStrength", 0.08, 0.0, 0.25);
   private static final DoubleValue THERMAL_SCANLINE_SPACING_PX = BUILDER.defineInRange("thermalScanlineSpacingPx", 4.0, 1.0, 8.0);
   private static final DoubleValue THERMAL_SCANLINE_SHARPNESS = BUILDER.defineInRange("thermalScanlineSharpness", 2.4, 0.5, 8.0);
   private static final DoubleValue THERMAL_SCANLINE_IRREGULARITY_PX = BUILDER.defineInRange("thermalScanlineIrregularityPx", 0.55, 0.0, 1.5);
   private static final DoubleValue THERMAL_SCANLINE_PALETTE_SHIFT = BUILDER.defineInRange("thermalScanlinePaletteShift", 0.11, 0.0, 0.18);
   private static final DoubleValue THERMAL_SCANLINE_BREAKUP = BUILDER.defineInRange("thermalScanlineBreakup", 0.96, 0.0, 1.0);
   private static final DoubleValue THERMAL_SCANLINE_WHITE_HOT_SUPPRESSION = BUILDER.defineInRange("thermalScanlineWhiteHotSuppression", 0.96, 0.0, 1.0);
   private static final BooleanValue THERMAL_HOT_BLOCKS_ENABLED = BUILDER.define("thermalHotBlocksEnabled", false);
   private static final IntValue THERMAL_HOT_BLOCK_MAX_DISTANCE = BUILDER.defineInRange("thermalHotBlockMaxDistance", 32, 8, 48);
   private static final DoubleValue THERMAL_HOT_BLOCK_OPACITY = BUILDER.defineInRange("thermalHotBlockOpacity", 0.88, 0.0, 1.0);
   private static final BooleanValue THERMAL_HELD_ITEMS_ENABLED = BUILDER.comment(
         "Full thermal spectrum drawn directly from captured first-person item geometry"
      )
      .define("thermalHeldItemsEnabled", true);
   private static final BooleanValue THERMAL_HELD_ITEMS_SCREEN_SPACE_EXPERIMENTAL = BUILDER.comment(
         "Experimental reversible final-frame held-item thermal compositor; false keeps the stable geometry path"
      )
      .define("thermalHeldItemsScreenSpaceExperimental", false);
   private static final BooleanValue THERMAL_HELD_ITEMS_SCREEN_SPACE_DEBUG_MASK = BUILDER.comment(
         "Show the raw depth-derived first-person coverage mask for diagnostics"
      )
      .define("thermalHeldItemsScreenSpaceDebugMask", false);
   private static final DoubleValue THERMAL_HELD_ITEM_CONTACT_HEAT = BUILDER.comment("How strongly nearby hand surfaces warm a cold held item")
      .defineInRange("thermalHeldItemContactHeat", 0.32, 0.0, 1.0);
   private static final DoubleValue THERMAL_HELD_ITEM_OUTER_GLOW_RATIO = BUILDER.comment("Exterior glow width for first-person held items and TACZ scopes only")
      .defineInRange("thermalHeldItemOuterGlowRatio", 0.02, 0.0, 0.2);
   private static final DoubleValue THERMAL_HELD_ITEM_OUTER_GLOW_STRENGTH = BUILDER.comment(
         "Exterior glow opacity for first-person held items and TACZ scopes only"
      )
      .defineInRange("thermalHeldItemOuterGlowStrength", 1.0, 0.0, 1.0);
   private static final BooleanValue THERMAL_MARKER_COMPATIBILITY_ENTITY_PASS = BUILDER.define("thermalMarkerCompatibilityEntityPass", true);
   private static final BooleanValue THERMAL_MARKER_DEBUG_SHOW_RAW_MARKER = BUILDER.define("thermalMarkerDebugShowRawMarker", false);
   private static final BooleanValue THERMAL_MARKER_DEBUG_SHOW_DETECTED_MASK = BUILDER.define("thermalMarkerDebugShowDetectedMask", false);
   private static final BooleanValue THERMAL_MARKER_DEBUG_SHOW_DISTANCE = BUILDER.define("thermalMarkerDebugShowDistance", false);
   private static final BooleanValue THERMAL_MARKER_DEBUG_DISABLE_GLOW = BUILDER.define("thermalMarkerDebugDisableGlow", false);
   private static final BooleanValue THERMAL_MARKER_DEBUG_DISABLE_PALETTE = BUILDER.define("thermalMarkerDebugDisablePalette", false);
   private static final BooleanValue THERMAL_GEOMETRY_RELIEF_DEBUG_ONLY = BUILDER.define("thermalGeometryReliefDebugOnly", false);
   private static final BooleanValue THERMAL_MARKER_DEBUG_LOG = BUILDER.define("thermalMarkerDebugLog", false);
   public static final ModConfigSpec SPEC = BUILDER.build();

   private NanovisionConfig() {
   }

   public static boolean nanovisionEnabled() {
      return safeGetBoolean(NANOVISION_ENABLED, true);
   }

   public static boolean nanovisionRequiresHelmet() {
      return safeGetBoolean(NANOVISION_REQUIRES_HELMET, true);
   }

   public static boolean nanovisionRequiresFullSet() {
      return safeGetBoolean(NANOVISION_REQUIRES_FULL_SET, false);
   }

   public static boolean nanovisionUseNightVision() {
      return safeGetBoolean(NANOVISION_USE_NIGHT_VISION, true);
   }

   public static NanovisionMode workingMode() {
      return WORKING_NANOVISION_MODE;
   }

   public static boolean nanovisionDebug() {
      return safeGetBoolean(NANOVISION_DEBUG, false);
   }

   public static boolean postShaderEnabled() {
      return safeGetBoolean(POST_SHADER_ENABLED, true);
   }

   public static boolean nanovisionMotionBlurEnabled() {
      return safeGetBoolean(NANOVISION_MOTION_BLUR_ENABLED, true);
   }

   public static double nanovisionMotionBlurStrength() {
      return safeGetDouble(NANOVISION_MOTION_BLUR_STRENGTH, 0.22, 0.0, 1.0);
   }

   public static boolean nanovisionActivationWaveEnabled() {
      return safeGetBoolean(NANOVISION_ACTIVATION_WAVE_ENABLED, false);
   }

   public static double nanovisionActivationWorldFlashStrength() {
      return safeGetDouble(NANOVISION_ACTIVATION_WORLD_FLASH_STRENGTH, 0.19789719626168223, 0.0, 0.35);
   }

   public static int nanovisionEnabledTextOffsetX() {
      return safeGetInt(NANOVISION_ENABLED_TEXT_OFFSET_X, 0, -4096, 4096);
   }

   public static int nanovisionEnabledTextOffsetY() {
      return safeGetInt(NANOVISION_ENABLED_TEXT_OFFSET_Y, 103, -4096, 4096);
   }

   public static boolean nanovisionDebugPostPassthrough() {
      return safeGetBoolean(NANOVISION_DEBUG_POST_PASSTHROUGH, false);
   }

   public static boolean nanovisionDebugOpaqueGrayOnly() {
      return safeGetBoolean(NANOVISION_DEBUG_OPAQUE_GRAY_ONLY, false);
   }

   public static boolean thermalMarkerEnabled() {
      return safeGetBoolean(THERMAL_MARKER_ENABLED, true);
   }

   public static ThermalEntityEffectMode workingThermalEntityEffectMode() {
      return WORKING_THERMAL_ENTITY_EFFECT_MODE;
   }

   public static double thermalMarkerMaxDistance() {
      return safeGetDouble(THERMAL_MARKER_MAX_DISTANCE, 96.0, 1.0, 256.0);
   }

   public static boolean thermalMarkerRenderPlayers() {
      return safeGetBoolean(THERMAL_MARKER_RENDER_PLAYERS, true);
   }

   public static boolean thermalMarkerRenderAnimals() {
      return safeGetBoolean(THERMAL_MARKER_RENDER_ANIMALS, true);
   }

   public static boolean thermalMarkerRenderMonsters() {
      return safeGetBoolean(THERMAL_MARKER_RENDER_MONSTERS, true);
   }

   public static boolean thermalMarkerRenderInvisible() {
      return safeGetBoolean(THERMAL_MARKER_RENDER_INVISIBLE, false);
   }

   public static boolean thermalMarkerSkipFirstPersonPlayer() {
      return safeGetBoolean(THERMAL_MARKER_SKIP_FIRST_PERSON_PLAYER, true);
   }

   public static List<? extends String> thermalEntityClassOverrides() {
      try {
         return (List<? extends String>)THERMAL_ENTITY_CLASS_OVERRIDES.get();
      } catch (RuntimeException var1) {
         return DEFAULT_THERMAL_ENTITY_CLASS_OVERRIDES;
      }
   }

   public static double thermalMarkerInnerRadiusRatio() {
      return safeGetDouble(THERMAL_MARKER_INNER_RADIUS_RATIO, 0.48, 0.05, 1.0);
   }

   public static double thermalGeometryDepthStrength() {
      return safeGetDouble(THERMAL_GEOMETRY_DEPTH_STRENGTH, 1.0, 0.0, 2.0);
   }

   public static double thermalGeometryDepthRadius() {
      return safeGetDouble(THERMAL_GEOMETRY_DEPTH_RADIUS, 1.35, 0.5, 3.0);
   }

   public static double thermalGeometryDepthContrast() {
      return safeGetDouble(THERMAL_GEOMETRY_DEPTH_CONTRAST, 1.15, 0.25, 3.0);
   }

   public static double thermalImprovedSilhouetteWidth() {
      return safeGetDouble(THERMAL_IMPROVED_SILHOUETTE_WIDTH, 0.9, 0.4, 2.0);
   }

   public static double thermalImprovedRimCurve() {
      return safeGetDouble(THERMAL_IMPROVED_RIM_CURVE, 0.82, 0.3, 1.5);
   }

   public static double thermalImprovedCoreHeat() {
      return safeGetDouble(THERMAL_IMPROVED_CORE_HEAT, 0.79, 0.55, 0.98);
   }

   public static double thermalImprovedFacingStrength() {
      return safeGetDouble(THERMAL_IMPROVED_FACING_STRENGTH, 0.08, 0.0, 0.55);
   }

   public static double thermalImprovedSmoothNormalStrength() {
      return safeGetDouble(THERMAL_IMPROVED_SMOOTH_NORMAL_STRENGTH, 0.92, 0.0, 1.0);
   }

   public static double thermalImprovedReliefScale() {
      return safeGetDouble(THERMAL_IMPROVED_RELIEF_SCALE, 1.15, 0.5, 3.0);
   }

   public static double thermalImprovedReliefThreshold() {
      return safeGetDouble(THERMAL_IMPROVED_RELIEF_THRESHOLD, 1.0, 0.5, 3.0);
   }

   public static double thermalImprovedProtrusionCooling() {
      return safeGetDouble(THERMAL_IMPROVED_PROTRUSION_COOLING, 0.12, 0.0, 0.5);
   }

   public static double thermalImprovedRecessCooling() {
      return safeGetDouble(THERMAL_IMPROVED_RECESS_COOLING, 0.1, 0.0, 0.4);
   }

   public static double thermalImprovedReliefSmoothing() {
      return safeGetDouble(THERMAL_IMPROVED_RELIEF_SMOOTHING, 0.88, 0.0, 1.0);
   }

   public static double thermalImprovedV2HotPeakArea() {
      return safeGetDouble(THERMAL_IMPROVED_V2_HOT_PEAK_AREA, 0.8660436137071651, 0.0, 1.0);
   }

   public static double thermalMarkerOuterGlowRatio() {
      return safeGetDouble(THERMAL_MARKER_OUTER_GLOW_RATIO, 0.06, 0.0, 0.2);
   }

   public static double thermalMarkerInnerStrength() {
      return safeGetDouble(THERMAL_MARKER_INNER_STRENGTH, 1.0, 0.0, 1.0);
   }

   public static double thermalMarkerModelOpacity() {
      return safeGetDouble(THERMAL_MARKER_MODEL_OPACITY, 0.9, 0.0, 1.0);
   }

   public static double thermalMarkerOuterGlowStrength() {
      return safeGetDouble(THERMAL_MARKER_OUTER_GLOW_STRENGTH, 0.66, 0.0, 1.0);
   }

   public static double thermalMarkerHeatPower() {
      return safeGetDouble(THERMAL_MARKER_HEAT_POWER, 2.46, 0.25, 4.0);
   }

   public static double thermalMarkerBandingStrength() {
      return safeGetDouble(THERMAL_MARKER_BANDING_STRENGTH, 0.18, 0.0, 1.0);
   }

   public static double thermalWarmGreenWidth() {
      return safeGetDouble(THERMAL_WARM_GREEN_WIDTH, 4.0, 0.25, 4.0);
   }

   public static double thermalWarmRedWidth() {
      return safeGetDouble(THERMAL_WARM_RED_WIDTH, 0.9, 0.25, 4.0);
   }

   public static double thermalWarmPinkWidth() {
      return safeGetDouble(THERMAL_WARM_PINK_WIDTH, 0.95, 0.25, 4.0);
   }

   public static double thermalWarmWhiteWidth() {
      return safeGetDouble(THERMAL_WARM_WHITE_WIDTH, 0.55, 0.25, 4.0);
   }

   public static int thermalMarkerMaxSearchRadiusPx() {
      return safeGetInt(THERMAL_MARKER_MAX_SEARCH_RADIUS_PX, 80, 8, 96);
   }

   public static ThermalMarkerQuality thermalMarkerQuality() {
      return safeGetEnum(THERMAL_MARKER_QUALITY, DEFAULT_THERMAL_MARKER_QUALITY);
   }

   public static ThermalVisualPreset thermalVisualPreset() {
      return safeGetEnum(THERMAL_VISUAL_PRESET, DEFAULT_THERMAL_VISUAL_PRESET);
   }

   public static double thermalScanlineStrength() {
      return safeGetDouble(THERMAL_SCANLINE_STRENGTH, 0.08, 0.0, 0.25);
   }

   public static double thermalScanlineSpacingPx() {
      return safeGetDouble(THERMAL_SCANLINE_SPACING_PX, 4.0, 1.0, 8.0);
   }

   public static double thermalScanlineSharpness() {
      return safeGetDouble(THERMAL_SCANLINE_SHARPNESS, 2.4, 0.5, 8.0);
   }

   public static double thermalScanlineIrregularityPx() {
      return safeGetDouble(THERMAL_SCANLINE_IRREGULARITY_PX, 0.55, 0.0, 1.5);
   }

   public static double thermalScanlinePaletteShift() {
      return safeGetDouble(THERMAL_SCANLINE_PALETTE_SHIFT, 0.11, 0.0, 0.18);
   }

   public static double thermalScanlineBreakup() {
      return safeGetDouble(THERMAL_SCANLINE_BREAKUP, 0.96, 0.0, 1.0);
   }

   public static double thermalScanlineWhiteHotSuppression() {
      return safeGetDouble(THERMAL_SCANLINE_WHITE_HOT_SUPPRESSION, 0.96, 0.0, 1.0);
   }

   public static boolean thermalHotBlocksEnabled() {
      return safeGetBoolean(THERMAL_HOT_BLOCKS_ENABLED, false);
   }

   public static int thermalHotBlockMaxDistance() {
      return safeGetInt(THERMAL_HOT_BLOCK_MAX_DISTANCE, 32, 8, 48);
   }

   public static double thermalHotBlockOpacity() {
      return safeGetDouble(THERMAL_HOT_BLOCK_OPACITY, 0.88, 0.0, 1.0);
   }

   public static boolean thermalHeldItemsEnabled() {
      return safeGetBoolean(THERMAL_HELD_ITEMS_ENABLED, true);
   }

   public static boolean thermalHeldItemsScreenSpaceExperimental() {
      return safeGetBoolean(THERMAL_HELD_ITEMS_SCREEN_SPACE_EXPERIMENTAL, false);
   }

   public static boolean thermalHeldItemsScreenSpaceDebugMask() {
      return safeGetBoolean(THERMAL_HELD_ITEMS_SCREEN_SPACE_DEBUG_MASK, false);
   }

   public static double thermalHeldItemContactHeat() {
      return safeGetDouble(THERMAL_HELD_ITEM_CONTACT_HEAT, 0.32, 0.0, 1.0);
   }

   public static double thermalHeldItemOuterGlowRatio() {
      return safeGetDouble(THERMAL_HELD_ITEM_OUTER_GLOW_RATIO, 0.02, 0.0, 0.2);
   }

   public static double thermalHeldItemOuterGlowStrength() {
      return safeGetDouble(THERMAL_HELD_ITEM_OUTER_GLOW_STRENGTH, 1.0, 0.0, 1.0);
   }

   public static boolean thermalMarkerCompatibilityEntityPass() {
      return safeGetBoolean(THERMAL_MARKER_COMPATIBILITY_ENTITY_PASS, true);
   }

   public static boolean thermalMarkerDebugShowRawMarker() {
      return safeGetBoolean(THERMAL_MARKER_DEBUG_SHOW_RAW_MARKER, false);
   }

   public static boolean thermalMarkerDebugShowDetectedMask() {
      return safeGetBoolean(THERMAL_MARKER_DEBUG_SHOW_DETECTED_MASK, false);
   }

   public static boolean thermalMarkerDebugShowDistance() {
      return safeGetBoolean(THERMAL_MARKER_DEBUG_SHOW_DISTANCE, false);
   }

   public static boolean thermalMarkerDebugDisableGlow() {
      return safeGetBoolean(THERMAL_MARKER_DEBUG_DISABLE_GLOW, false);
   }

   public static boolean thermalMarkerDebugDisablePalette() {
      return safeGetBoolean(THERMAL_MARKER_DEBUG_DISABLE_PALETTE, false);
   }

   public static boolean thermalGeometryReliefDebugOnly() {
      return safeGetBoolean(THERMAL_GEOMETRY_RELIEF_DEBUG_ONLY, false);
   }

   public static boolean thermalMarkerDebugLog() {
      return safeGetBoolean(THERMAL_MARKER_DEBUG_LOG, false);
   }

   public static boolean markerPipelineEnabled() {
      return thermalMarkerEnabled() && workingThermalEntityEffectMode().usesMarkerPass();
   }

   public static void setNanovisionEnabled(boolean value) {
      safeSetBoolean(NANOVISION_ENABLED, value);
   }

   public static void setNanovisionRequiresHelmet(boolean value) {
      safeSetBoolean(NANOVISION_REQUIRES_HELMET, value);
   }

   public static void setNanovisionRequiresFullSet(boolean value) {
      safeSetBoolean(NANOVISION_REQUIRES_FULL_SET, value);
   }

   public static void setNanovisionUseNightVision(boolean value) {
      safeSetBoolean(NANOVISION_USE_NIGHT_VISION, value);
   }

   public static void setNanovisionDebug(boolean value) {
      safeSetBoolean(NANOVISION_DEBUG, value);
   }

   public static void setPostShaderEnabled(boolean value) {
      safeSetBoolean(POST_SHADER_ENABLED, value);
   }

   public static void setNanovisionMotionBlurEnabled(boolean value) {
      safeSetBoolean(NANOVISION_MOTION_BLUR_ENABLED, value);
   }

   public static void setNanovisionMotionBlurStrength(double value) {
      safeSetDouble(NANOVISION_MOTION_BLUR_STRENGTH, value);
   }

   public static void setNanovisionActivationWaveEnabled(boolean value) {
      safeSetBoolean(NANOVISION_ACTIVATION_WAVE_ENABLED, value);
   }

   public static void setNanovisionActivationWorldFlashStrength(double value) {
      safeSetDouble(NANOVISION_ACTIVATION_WORLD_FLASH_STRENGTH, value);
   }

   public static void setNanovisionEnabledTextOffsetX(int value) {
      safeSetInt(NANOVISION_ENABLED_TEXT_OFFSET_X, value);
   }

   public static void setNanovisionEnabledTextOffsetY(int value) {
      safeSetInt(NANOVISION_ENABLED_TEXT_OFFSET_Y, value);
   }

   public static void setNanovisionDebugPostPassthrough(boolean value) {
      safeSetBoolean(NANOVISION_DEBUG_POST_PASSTHROUGH, value);
   }

   public static void setNanovisionDebugOpaqueGrayOnly(boolean value) {
      safeSetBoolean(NANOVISION_DEBUG_OPAQUE_GRAY_ONLY, value);
   }

   public static void setThermalMarkerEnabled(boolean value) {
      safeSetBoolean(THERMAL_MARKER_ENABLED, value);
   }

   public static void setThermalMarkerMaxDistance(double value) {
      safeSetDouble(THERMAL_MARKER_MAX_DISTANCE, value);
   }

   public static void setThermalMarkerRenderPlayers(boolean value) {
      safeSetBoolean(THERMAL_MARKER_RENDER_PLAYERS, value);
   }

   public static void setThermalMarkerRenderAnimals(boolean value) {
      safeSetBoolean(THERMAL_MARKER_RENDER_ANIMALS, value);
   }

   public static void setThermalMarkerRenderMonsters(boolean value) {
      safeSetBoolean(THERMAL_MARKER_RENDER_MONSTERS, value);
   }

   public static void setThermalMarkerRenderInvisible(boolean value) {
      safeSetBoolean(THERMAL_MARKER_RENDER_INVISIBLE, value);
   }

   public static void setThermalMarkerSkipFirstPersonPlayer(boolean value) {
      safeSetBoolean(THERMAL_MARKER_SKIP_FIRST_PERSON_PLAYER, value);
   }

   public static void setThermalEntityClassOverrides(List<String> values) {
      List<String> sanitized = values == null
         ? List.of()
         : values.stream().map(String::trim).filter(entry -> !entry.isEmpty()).filter(NanovisionConfig::isValidThermalEntityClassOverride).distinct().toList();

      try {
         THERMAL_ENTITY_CLASS_OVERRIDES.set(sanitized);
         THERMAL_ENTITY_CLASS_OVERRIDES.save();
      } catch (RuntimeException var3) {
      }
   }

   public static void setThermalMarkerInnerRadiusRatio(double value) {
      safeSetDouble(THERMAL_MARKER_INNER_RADIUS_RATIO, value);
   }

   public static void setThermalGeometryDepthStrength(double value) {
      safeSetDouble(THERMAL_GEOMETRY_DEPTH_STRENGTH, value);
   }

   public static void setThermalGeometryDepthRadius(double value) {
      safeSetDouble(THERMAL_GEOMETRY_DEPTH_RADIUS, value);
   }

   public static void setThermalGeometryDepthContrast(double value) {
      safeSetDouble(THERMAL_GEOMETRY_DEPTH_CONTRAST, value);
   }

   public static void setThermalImprovedSilhouetteWidth(double value) {
      safeSetDouble(THERMAL_IMPROVED_SILHOUETTE_WIDTH, value);
   }

   public static void setThermalImprovedRimCurve(double value) {
      safeSetDouble(THERMAL_IMPROVED_RIM_CURVE, value);
   }

   public static void setThermalImprovedCoreHeat(double value) {
      safeSetDouble(THERMAL_IMPROVED_CORE_HEAT, value);
   }

   public static void setThermalImprovedFacingStrength(double value) {
      safeSetDouble(THERMAL_IMPROVED_FACING_STRENGTH, value);
   }

   public static void setThermalImprovedSmoothNormalStrength(double value) {
      safeSetDouble(THERMAL_IMPROVED_SMOOTH_NORMAL_STRENGTH, value);
   }

   public static void setThermalImprovedReliefScale(double value) {
      safeSetDouble(THERMAL_IMPROVED_RELIEF_SCALE, value);
   }

   public static void setThermalImprovedReliefThreshold(double value) {
      safeSetDouble(THERMAL_IMPROVED_RELIEF_THRESHOLD, value);
   }

   public static void setThermalImprovedProtrusionCooling(double value) {
      safeSetDouble(THERMAL_IMPROVED_PROTRUSION_COOLING, value);
   }

   public static void setThermalImprovedRecessCooling(double value) {
      safeSetDouble(THERMAL_IMPROVED_RECESS_COOLING, value);
   }

   public static void setThermalImprovedReliefSmoothing(double value) {
      safeSetDouble(THERMAL_IMPROVED_RELIEF_SMOOTHING, value);
   }

   public static void setThermalImprovedV2HotPeakArea(double value) {
      safeSetDouble(THERMAL_IMPROVED_V2_HOT_PEAK_AREA, value);
   }

   public static void setThermalMarkerOuterGlowRatio(double value) {
      safeSetDouble(THERMAL_MARKER_OUTER_GLOW_RATIO, value);
   }

   public static void setThermalMarkerInnerStrength(double value) {
      safeSetDouble(THERMAL_MARKER_INNER_STRENGTH, value);
   }

   public static void setThermalMarkerModelOpacity(double value) {
      safeSetDouble(THERMAL_MARKER_MODEL_OPACITY, value);
   }

   public static void setThermalMarkerOuterGlowStrength(double value) {
      safeSetDouble(THERMAL_MARKER_OUTER_GLOW_STRENGTH, value);
   }

   public static void setThermalMarkerHeatPower(double value) {
      safeSetDouble(THERMAL_MARKER_HEAT_POWER, value);
   }

   public static void setThermalMarkerBandingStrength(double value) {
      safeSetDouble(THERMAL_MARKER_BANDING_STRENGTH, value);
   }

   public static void setThermalWarmGreenWidth(double value) {
      safeSetDouble(THERMAL_WARM_GREEN_WIDTH, value);
   }

   public static void setThermalWarmRedWidth(double value) {
      safeSetDouble(THERMAL_WARM_RED_WIDTH, value);
   }

   public static void setThermalWarmPinkWidth(double value) {
      safeSetDouble(THERMAL_WARM_PINK_WIDTH, value);
   }

   public static void setThermalWarmWhiteWidth(double value) {
      safeSetDouble(THERMAL_WARM_WHITE_WIDTH, value);
   }

   public static void setThermalMarkerMaxSearchRadiusPx(int value) {
      safeSetInt(THERMAL_MARKER_MAX_SEARCH_RADIUS_PX, value);
   }

   public static void setThermalMarkerQuality(ThermalMarkerQuality value) {
      safeSetEnum(THERMAL_MARKER_QUALITY, value);
   }

   public static void setThermalVisualPreset(ThermalVisualPreset value) {
      safeSetEnum(THERMAL_VISUAL_PRESET, value);
   }

   public static void setThermalScanlineStrength(double value) {
      safeSetDouble(THERMAL_SCANLINE_STRENGTH, value);
   }

   public static void setThermalScanlineSpacingPx(double value) {
      safeSetDouble(THERMAL_SCANLINE_SPACING_PX, value);
   }

   public static void setThermalScanlineSharpness(double value) {
      safeSetDouble(THERMAL_SCANLINE_SHARPNESS, value);
   }

   public static void setThermalScanlineIrregularityPx(double value) {
      safeSetDouble(THERMAL_SCANLINE_IRREGULARITY_PX, value);
   }

   public static void setThermalScanlinePaletteShift(double value) {
      safeSetDouble(THERMAL_SCANLINE_PALETTE_SHIFT, value);
   }

   public static void setThermalScanlineBreakup(double value) {
      safeSetDouble(THERMAL_SCANLINE_BREAKUP, value);
   }

   public static void setThermalScanlineWhiteHotSuppression(double value) {
      safeSetDouble(THERMAL_SCANLINE_WHITE_HOT_SUPPRESSION, value);
   }

   public static void setThermalHotBlocksEnabled(boolean value) {
      safeSetBoolean(THERMAL_HOT_BLOCKS_ENABLED, value);
   }

   public static void setThermalHotBlockMaxDistance(int value) {
      safeSetInt(THERMAL_HOT_BLOCK_MAX_DISTANCE, value);
   }

   public static void setThermalHotBlockOpacity(double value) {
      safeSetDouble(THERMAL_HOT_BLOCK_OPACITY, value);
   }

   public static void setThermalHeldItemsEnabled(boolean value) {
      safeSetBoolean(THERMAL_HELD_ITEMS_ENABLED, value);
   }

   public static void setThermalHeldItemsScreenSpaceExperimental(boolean value) {
      safeSetBoolean(THERMAL_HELD_ITEMS_SCREEN_SPACE_EXPERIMENTAL, value);
   }

   public static void setThermalHeldItemsScreenSpaceDebugMask(boolean value) {
      safeSetBoolean(THERMAL_HELD_ITEMS_SCREEN_SPACE_DEBUG_MASK, value);
   }

   public static void setThermalHeldItemContactHeat(double value) {
      safeSetDouble(THERMAL_HELD_ITEM_CONTACT_HEAT, value);
   }

   public static void setThermalHeldItemOuterGlowRatio(double value) {
      safeSetDouble(THERMAL_HELD_ITEM_OUTER_GLOW_RATIO, value);
   }

   public static void setThermalHeldItemOuterGlowStrength(double value) {
      safeSetDouble(THERMAL_HELD_ITEM_OUTER_GLOW_STRENGTH, value);
   }

   public static void setThermalMarkerCompatibilityEntityPass(boolean value) {
      safeSetBoolean(THERMAL_MARKER_COMPATIBILITY_ENTITY_PASS, value);
   }

   public static void setThermalMarkerDebugShowRawMarker(boolean value) {
      safeSetBoolean(THERMAL_MARKER_DEBUG_SHOW_RAW_MARKER, value);
   }

   public static void setThermalMarkerDebugShowDetectedMask(boolean value) {
      safeSetBoolean(THERMAL_MARKER_DEBUG_SHOW_DETECTED_MASK, value);
   }

   public static void setThermalMarkerDebugShowDistance(boolean value) {
      safeSetBoolean(THERMAL_MARKER_DEBUG_SHOW_DISTANCE, value);
   }

   public static void setThermalMarkerDebugDisableGlow(boolean value) {
      safeSetBoolean(THERMAL_MARKER_DEBUG_DISABLE_GLOW, value);
   }

   public static void setThermalMarkerDebugDisablePalette(boolean value) {
      safeSetBoolean(THERMAL_MARKER_DEBUG_DISABLE_PALETTE, value);
   }

   public static void setThermalGeometryReliefDebugOnly(boolean value) {
      safeSetBoolean(THERMAL_GEOMETRY_RELIEF_DEBUG_ONLY, value);
   }

   public static void setThermalMarkerDebugLog(boolean value) {
      safeSetBoolean(THERMAL_MARKER_DEBUG_LOG, value);
   }

   private static boolean safeGetBoolean(BooleanValue value, boolean fallback) {
      try {
         return (Boolean)value.get();
      } catch (RuntimeException var3) {
         return fallback;
      }
   }

   private static double safeGetDouble(DoubleValue value, double fallback, double min, double max) {
      try {
         return Mth.clamp((Double)value.get(), min, max);
      } catch (RuntimeException var8) {
         return fallback;
      }
   }

   private static int safeGetInt(IntValue value, int fallback, int min, int max) {
      try {
         return Mth.clamp((Integer)value.get(), min, max);
      } catch (RuntimeException var5) {
         return fallback;
      }
   }

   private static <T extends Enum<T>> T safeGetEnum(EnumValue<T> value, T fallback) {
      try {
         return (T)value.get();
      } catch (RuntimeException var3) {
         return fallback;
      }
   }

   private static boolean isValidThermalEntityClassOverride(Object value) {
      if (value instanceof String entry) {
         int separator = entry.lastIndexOf(61);
         if (separator <= 0 || separator >= entry.length() - 1) {
            return false;
         } else if (!isValidThermalEntitySelector(entry.substring(0, separator).trim())) {
            return false;
         } else {
            try {
               ThermalEmissionClass.valueOf(entry.substring(separator + 1).trim().toUpperCase(Locale.ROOT));
               return true;
            } catch (IllegalArgumentException var4) {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private static boolean isValidThermalEntitySelector(String selector) {
      if (!selector.endsWith(":*")) {
         return ResourceLocation.tryParse(selector) != null;
      } else {
         String namespace = selector.substring(0, selector.length() - 2);
         ResourceLocation probe = ResourceLocation.tryParse(namespace + ":thermal_probe");
         return probe != null && probe.getNamespace().equals(namespace);
      }
   }

   private static void safeSetBoolean(BooleanValue value, boolean target) {
      try {
         value.set(target);
         value.save();
      } catch (RuntimeException var3) {
      }
   }

   private static void safeSetDouble(DoubleValue value, double target) {
      try {
         value.set(target);
         value.save();
      } catch (RuntimeException var4) {
      }
   }

   private static void safeSetInt(IntValue value, int target) {
      try {
         value.set(target);
         value.save();
      } catch (RuntimeException var3) {
      }
   }

   private static <T extends Enum<T>> void safeSetEnum(EnumValue<T> value, T target) {
      try {
         value.set(target);
         value.save();
      } catch (RuntimeException var3) {
      }
   }

   static {
      BUILDER.push("general");
      BUILDER.pop();
      BUILDER.push("thermal_marker");
      BUILDER.pop();
      BUILDER.push("classic_improved");
      BUILDER.pop();
      BUILDER.push("classic_improved_v2");
      BUILDER.pop();
   }
}
