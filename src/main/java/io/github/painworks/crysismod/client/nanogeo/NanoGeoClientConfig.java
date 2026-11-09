package io.github.painworks.crysismod.client.nanogeo;

import java.util.Locale;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class NanoGeoClientConfig {
   private static final Builder BUILDER = new Builder();
   private static final boolean DEFAULT_NANO_GEO_DEBUG_RENDER_ENABLED = false;
   private static final boolean DEFAULT_NANO_GEO_LOG_MODEL_LOAD = true;
   private static final boolean DEFAULT_NANO_GEO_USE_EXPERIMENTAL_ARMOR_RENDERER = true;
   private static final boolean DEFAULT_NANO_GEO_EXPERIMENTAL_ARMOR_ITEMS_ENABLED = true;
   private static final boolean DEFAULT_NANO_GEO_EXPERIMENTAL_ARMOR_COUNTS_AS_NANOSUIT = false;
   private static final boolean DEFAULT_NANO_GEO_FPV_ARMS_ENABLED = true;
   private static final boolean DEFAULT_NANO_GEO_FPV_ARMS_RENDER_BOTH_HANDS = true;
   private static final boolean DEFAULT_NANO_GEO_FPV_ARMS_DEBUG = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_FPV_VISIBLE_BONES = false;
   private static final boolean DEFAULT_ARMOR_MODE_OVERLAY_ENABLED = true;
   private static final NanoGeoClientConfig.ArmorModeOverlayBackend DEFAULT_ARMOR_MODE_OVERLAY_BACKEND = NanoGeoClientConfig.ArmorModeOverlayBackend.DYNAMIC_TEXTURE_COMPAT;
   private static final String DEFAULT_ARMOR_MODE_OVERLAY_TEXTURE = "crysismod:textures/armor/nanosuit_armor_mode.png";
   private static final int DEFAULT_ARMOR_MODE_OVERLAY_ALPHA = 205;
   private static final double DEFAULT_ARMOR_MODE_OVERLAY_BRIGHTNESS = 1.5;
   private static final boolean DEFAULT_ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_SHELL_ENABLED = true;
   private static final double DEFAULT_ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_SHELL_SCALE = 0.004F;
   private static final int DEFAULT_ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_MAX_RESOLUTION = 256;
   private static final int DEFAULT_ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_UPDATE_INTERVAL_MILLIS = 50;
   private static final double DEFAULT_ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_GLOW_STRENGTH = 0.3F;
   private static final boolean DEFAULT_ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_UV_COVERAGE_MASK_ENABLED = true;
   private static final NanoGeoClientConfig.ArmorModeCompatPerformancePreset DEFAULT_ARMOR_MODE_COMPAT_PERFORMANCE_PRESET = NanoGeoClientConfig.ArmorModeCompatPerformancePreset.UNSHADED_ARMOR_LAYER;
   private static final double DEFAULT_ARMOR_MODE_OVERLAY_BASE_ALPHA = 0.35;
   private static final double DEFAULT_ARMOR_MODE_OVERLAY_PULSE_AMOUNT = 0.0;
   private static final double DEFAULT_ARMOR_MODE_OVERLAY_PULSE_SPEED = 0.0;
   private static final boolean DEFAULT_ARMOR_MODE_OVERLAY_ACTIVATION_PULSE_ENABLED = false;
   private static final int DEFAULT_ARMOR_MODE_OVERLAY_ACTIVATION_DURATION_TICKS = 10;
   private static final double DEFAULT_ARMOR_MODE_OVERLAY_ACTIVATION_STRENGTH = 0.35;
   private static final boolean DEFAULT_ARMOR_MODE_OVERLAY_FULLBRIGHT = true;
   private static final double DEFAULT_ARMOR_MODE_OVERLAY_TINT_R = 1.0;
   private static final double DEFAULT_ARMOR_MODE_OVERLAY_TINT_G = 0.9F;
   private static final double DEFAULT_ARMOR_MODE_OVERLAY_TINT_B = 1.0;
   private static final double DEFAULT_ARMOR_MODE_OVERLAY_GLINT_INTENSITY = 0.45;
   private static final boolean DEFAULT_ARMOR_MODE_OVERLAY_WAVE_ENABLED = false;
   private static final double DEFAULT_ARMOR_MODE_OVERLAY_WAVE_INTENSITY = 0.3;
   private static final double DEFAULT_ARMOR_MODE_OVERLAY_WAVE_WIDTH = 0.24;
   private static final double DEFAULT_ARMOR_MODE_OVERLAY_WAVE_SPEED = 0.85;
   private static final double DEFAULT_ARMOR_MODE_OVERLAY_WAVE_FREQUENCY = 2.2;
   private static final boolean DEFAULT_ARMOR_MODE_DIAMOND_SHIMMER_ENABLED = false;
   private static final double DEFAULT_ARMOR_MODE_DIAMOND_SHIMMER_SPEED_PIXELS_PER_SECOND = 1.0;
   private static final int DEFAULT_ARMOR_MODE_DIAMOND_WIDTH_PX = 3;
   private static final int DEFAULT_ARMOR_MODE_DIAMOND_HEIGHT_PX = 5;
   private static final int DEFAULT_ARMOR_MODE_DIAMOND_ALPHA = 0;
   private static final NanoGeoClientConfig.ArmorModeDiamondShimmerDirection DEFAULT_ARMOR_MODE_DIAMOND_SHIMMER_DIRECTION = NanoGeoClientConfig.ArmorModeDiamondShimmerDirection.DOWN_RIGHT;
   private static final boolean DEFAULT_DEBUG_ARMOR_MODE_DIAMOND_SHIMMER = false;
   private static final NanoGeoClientConfig.ArmorModeOverlayPatternMode DEFAULT_ARMOR_MODE_OVERLAY_PATTERN_MODE = NanoGeoClientConfig.ArmorModeOverlayPatternMode.CHAOTIC_PATCH_FLOW;
   private static final boolean DEFAULT_ARMOR_MODE_X_SHIMMER_ENABLED = true;
   private static final int DEFAULT_ARMOR_MODE_X_SHIMMER_ALPHA = 90;
   private static final int DEFAULT_ARMOR_MODE_X_SHIMMER_SIZE_PX = 5;
   private static final int DEFAULT_ARMOR_MODE_X_SHIMMER_SPACING_PX = 16;
   private static final double DEFAULT_ARMOR_MODE_X_SHIMMER_LINE_WIDTH_PX = 1.0;
   private static final double DEFAULT_ARMOR_MODE_X_SHIMMER_EDGE_SOFTNESS_PX = 1.0;
   private static final double DEFAULT_ARMOR_MODE_X_SHIMMER_SPEED_PX_PER_SECOND = 1.0;
   private static final double DEFAULT_ARMOR_MODE_X_SHIMMER_DIRECTION_X = 1.0;
   private static final double DEFAULT_ARMOR_MODE_X_SHIMMER_DIRECTION_Y = 1.0;
   private static final boolean DEFAULT_ARMOR_MODE_X_SHIMMER_AS_CUTOUT = false;
   private static final boolean DEFAULT_DEBUG_ARMOR_MODE_X_SHIMMER = false;
   private static final double DEFAULT_ARMOR_MODE_HEX_CELL_SIZE_PX = 18.0;
   private static final double DEFAULT_ARMOR_MODE_HEX_LINE_WIDTH_PX = 2.0;
   private static final double DEFAULT_ARMOR_MODE_HEX_EDGE_SOFTNESS_PX = 1.0;
   private static final int DEFAULT_ARMOR_MODE_HEX_INNER_ALPHA = 0;
   private static final int DEFAULT_ARMOR_MODE_HEX_GLOW_ALPHA = 180;
   private static final double DEFAULT_ARMOR_MODE_HEX_FREQUENCY = 1.0;
   private static final double DEFAULT_ARMOR_MODE_HEX_SCROLL_SPEED_PX_PER_SECOND = 0.5;
   private static final double DEFAULT_ARMOR_MODE_HEX_DIRECTION_X = 1.0;
   private static final double DEFAULT_ARMOR_MODE_HEX_DIRECTION_Y = 0.5;
   private static final double DEFAULT_ARMOR_MODE_HEX_TEXTURE_SIZE_PX = 512.0;
   private static final double DEFAULT_ARMOR_MODE_HEX_PATTERN_SCALE = 1.0;
   private static final double DEFAULT_ARMOR_MODE_HEX_GLOW_BOOST = 0.25;
   private static final double DEFAULT_ARMOR_MODE_SHEEN_BREATHING_AMOUNT = 0.12;
   private static final double DEFAULT_ARMOR_MODE_SHEEN_BREATHING_SPEED = 0.07;
   private static final double DEFAULT_ARMOR_MODE_SHEEN_ALPHA = 0.45;
   private static final double DEFAULT_ARMOR_MODE_SHEEN_TRANSPARENCY = 0.45;
   private static final double DEFAULT_ARMOR_MODE_SHEEN_WIDTH_PX = 64.0;
   private static final double DEFAULT_ARMOR_MODE_SHEEN_SOFTNESS_PX = 48.0;
   private static final double DEFAULT_ARMOR_MODE_SHEEN_REPEAT_PX = 220.0;
   private static final double DEFAULT_ARMOR_MODE_SHEEN_SPEED_PX_PER_SECOND = 6.0;
   private static final double DEFAULT_ARMOR_MODE_SHEEN_DIRECTION_X = 1.0;
   private static final double DEFAULT_ARMOR_MODE_SHEEN_DIRECTION_Y = 0.35;
   private static final double DEFAULT_ARMOR_MODE_SHEEN_COLOR_BOOST = 0.25;
   private static final boolean DEFAULT_ARMOR_MODE_FIBER_FLOW_ENABLED = true;
   private static final double DEFAULT_ARMOR_MODE_FIBER_FLOW_STRENGTH = 0.08;
   private static final double DEFAULT_ARMOR_MODE_FIBER_FLOW_SCALE = 0.045;
   private static final double DEFAULT_ARMOR_MODE_FIBER_FLOW_SPEED = 0.8;
   private static final boolean DEFAULT_DEBUG_ARMOR_MODE_UNIVERSAL_SHEEN = false;
   private static final int DEFAULT_ARMOR_MODE_PLASMA_BLOB_INNER_ALPHA = 25;
   private static final int DEFAULT_ARMOR_MODE_PLASMA_BLOB_COUNT = 100;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_BLOB_MIN_SIZE_PX = 29.156998F;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_BLOB_MAX_SIZE_PX = 32.17406F;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_BLOB_EDGE_SOFTNESS_PX = 0.0;
   private static final int DEFAULT_ARMOR_MODE_PLASMA_BLOB_RIM_ALPHA = 27;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_BLOB_RIM_WIDTH_PX = 2.3481228F;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_BLOB_RIM_BRIGHTNESS = 0.29692832F;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_BLOB_DRIFT_SPEED_PX_PER_SECOND = 6.552901F;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_BLOB_DRIFT_RANGE_SCALE = 2.0;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_BLOB_SHAPE_DISTORTION = 0.35F;
   private static final int DEFAULT_ARMOR_MODE_PATCH_TEXTURE_ALPHA = 254;
   private static final int DEFAULT_ARMOR_MODE_PATCH_SPOT_ALPHA = 245;
   private static final double DEFAULT_ARMOR_MODE_PATCH_MIN_SIZE_PX = 10.1F;
   private static final double DEFAULT_ARMOR_MODE_PATCH_MAX_SIZE_PX = 16.0;
   private static final double DEFAULT_ARMOR_MODE_PATCH_FREQUENCY = 0.5;
   private static final double DEFAULT_ARMOR_MODE_PATCH_DENSITY = 0.88F;
   private static final NanoGeoClientConfig.ArmorModePatchFlowVariant DEFAULT_ARMOR_MODE_PATCH_FLOW_VARIANT = NanoGeoClientConfig.ArmorModePatchFlowVariant.SOFT_CLOUD_ARCHETYPES;
   private static final double DEFAULT_ARMOR_MODE_PATCH_CLOUD_EDGE_SOFTNESS = 0.25;
   private static final boolean DEFAULT_ARMOR_MODE_PLASMA_PULSE_ENABLED = true;
   private static final int DEFAULT_ARMOR_MODE_PLASMA_PULSE_COUNT = 5;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_PULSE_INTERVAL_SECONDS = 1.0;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_PULSE_DURATION_SECONDS = 2.0;
   private static final int DEFAULT_ARMOR_MODE_PLASMA_PULSE_ALPHA = 190;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_PULSE_START_RADIUS_PX = 0.0;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_PULSE_END_RADIUS_PX = 96.0;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_PULSE_SPREAD_SPEED_PX_PER_SECOND = 1.0;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_PULSE_LINE_WIDTH_PX = 1.0;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_PULSE_EDGE_SOFTNESS_PX = 1.0;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_PULSE_BRIGHTNESS = 1.25;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_PULSE_ARC_COVERAGE = 0.45;
   private static final double DEFAULT_ARMOR_MODE_PLASMA_PULSE_ARC_DISTORTION = 0.25;
   private static final boolean DEFAULT_ARMOR_MODE_PULSE_ENABLED = true;
   private static final NanoGeoClientConfig.ArmorModePulseCenterSource DEFAULT_ARMOR_MODE_PULSE_CENTER_SOURCE = NanoGeoClientConfig.ArmorModePulseCenterSource.RED_PIXEL_JSON;
   private static final NanoGeoClientConfig.ArmorModePulseVisualMode DEFAULT_ARMOR_MODE_PULSE_VISUAL_MODE = NanoGeoClientConfig.ArmorModePulseVisualMode.CRYSIS_ELECTRIC_RING;
   private static final NanoGeoClientConfig.ArmorModeCompatPulsePreset DEFAULT_ARMOR_MODE_COMPAT_PULSE_PRESET = NanoGeoClientConfig.ArmorModeCompatPulsePreset.CRISP_MASKED;
   private static final int DEFAULT_ARMOR_MODE_PULSE_MAX_CENTERS = 53;
   private static final int DEFAULT_ARMOR_MODE_PULSE_CENTER_MARKER_ALPHA = 0;
   private static final double DEFAULT_ARMOR_MODE_PULSE_INTERVAL_SECONDS = 0.81F;
   private static final double DEFAULT_ARMOR_MODE_PULSE_DURATION_SECONDS = 1.51F;
   private static final double DEFAULT_ARMOR_MODE_PULSE_START_RADIUS_PX = 0.0;
   private static final double DEFAULT_ARMOR_MODE_PULSE_END_RADIUS_PX = 19.07F;
   private static final double DEFAULT_ARMOR_MODE_PULSE_CORE_WIDTH_PX = 0.3F;
   private static final double DEFAULT_ARMOR_MODE_PULSE_GLOW_RADIUS_PX = 0.29F;
   private static final double DEFAULT_ARMOR_MODE_PULSE_CORE_SOFTNESS_PX = 0.25;
   private static final double DEFAULT_ARMOR_MODE_PULSE_GLOW_SOFTNESS_PX = 0.31F;
   private static final int DEFAULT_ARMOR_MODE_PULSE_CORE_ALPHA = 100;
   private static final int DEFAULT_ARMOR_MODE_PULSE_GLOW_ALPHA = 68;
   private static final String DEFAULT_ARMOR_MODE_PULSE_CORE_COLOR = "#7EC6FC";
   private static final String DEFAULT_ARMOR_MODE_PULSE_GLOW_COLOR = "#ADD1ED";
   private static final double DEFAULT_ARMOR_MODE_PULSE_BRIGHTNESS = 0.83F;
   private static final double DEFAULT_ARMOR_MODE_PULSE_FADE_POWER = 2.0;
   private static final double DEFAULT_ARMOR_MODE_PULSE_RING_DISTORTION_PX = 1.32F;
   private static final double DEFAULT_ARMOR_MODE_PULSE_RING_DISTORTION_FREQUENCY = 1.0;
   private static final double DEFAULT_ARMOR_MODE_PULSE_RING_ENERGY_VARIATION = 0.2F;
   private static final double DEFAULT_ARMOR_MODE_PULSE_WIDTH_VARIATION = 0.45F;
   private static final double DEFAULT_ARMOR_MODE_PULSE_BRIGHTNESS_VARIATION = 0.39419794F;
   private static final double DEFAULT_ARMOR_MODE_PULSE_EDGE_ROUGHNESS = 0.24061434F;
   private static final double DEFAULT_ARMOR_MODE_PULSE_PATTERN_ROTATION_RANDOMNESS = 1.0;
   private static final boolean DEFAULT_ARMOR_MODE_PULSE_MERGING_ENABLED = true;
   private static final double DEFAULT_ARMOR_MODE_PULSE_MERGE_STRENGTH = 0.25;
   private static final double DEFAULT_ARMOR_MODE_PULSE_MERGE_SOFTNESS_PX = 0.75;
   private static final double DEFAULT_ARMOR_MODE_PULSE_MERGE_BRIGHTNESS_BOOST = 0.1F;
   private static final boolean DEFAULT_ARMOR_MODE_PULSE_DEBUG_FORCE_VISIBLE = false;
   private static final boolean DEFAULT_ARMOR_MODE_PULSE_DEBUG_OUTPUT_ONLY_PULSE = false;
   private static final boolean DEFAULT_DEBUG_ARMOR_MODE_PLASMA_FIELD = false;
   private static final boolean DEFAULT_DEBUG_ARMOR_MODE_HEX_PATTERN = false;
   private static final boolean DEFAULT_DEBUG_ARMOR_MODE_SETTINGS_SCREEN = false;
   private static final boolean DEFAULT_ARMOR_MODE_OVERLAY_RENDER_IN_FIRST_PERSON = true;
   private static final boolean DEFAULT_ARMOR_MODE_OVERLAY_RENDER_IN_THIRD_PERSON = true;
   private static final boolean DEFAULT_ARMOR_MODE_TRANSITION_ENABLED = true;
   private static final double DEFAULT_ARMOR_MODE_TRANSITION_DURATION_SECONDS = 1.0072526F;
   private static final double DEFAULT_ARMOR_MODE_TRANSITION_PIXEL_BLOCK_SIZE_PX = 2.0;
   private static final double DEFAULT_ARMOR_MODE_TRANSITION_STAGGER_AMOUNT = 0.65F;
   private static final double DEFAULT_ARMOR_MODE_TRANSITION_FADE_WINDOW = 0.25;
   private static final double DEFAULT_ARMOR_MODE_TRANSITION_SEED = 17.0;
   private static final boolean DEFAULT_ARMOR_MODE_TRANSITION_AFFECTS_PULSES = true;
   private static final boolean DEFAULT_DEBUG_ARMOR_MODE_OVERLAY = true;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_UV = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_UV_LOG_CUBES = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_UV_RENDER_CHECKER = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_UV_FACE_COLORS = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_VERTEX_COLORS = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_LOG_RESOURCES = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_LOG_VISIBLE_BONES = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_LOG_BONE_TRANSFORMS = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_PERF_ENABLED = false;
   private static final int DEFAULT_NANO_GEO_DEBUG_PERF_LOG_INTERVAL = 120;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_PERF_LOG_STATIC = false;
   private static final boolean DEFAULT_NANO_GEO_PERF_PROFILING_ENABLED = false;
   private static final int DEFAULT_NANO_GEO_PERF_LOG_EVERY_SECONDS = 5;
   private static final boolean DEFAULT_NANO_GEO_PERF_COMPARE_MODE = false;
   private static final boolean DEFAULT_NANO_GEO_PERF_TRACK_ALLOCATIONS = false;
   private static final boolean DEFAULT_NANO_GEO_PERF_SHOW_OVERLAY = false;
   private static final boolean DEFAULT_NANO_GEO_SHOW_ADVANCED_DEBUG_OPTIONS = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_RENDER_AXES = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_SHOW_BONE_PIVOTS = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_SHOW_BONE_AXES = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_TINT_LEFT_RIGHT = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_HIDE_CHILDREN = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_FREEZE_PLAYER_POSE = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_ARMOR_CHEST_ONLY_FULL_MODEL = false;
   private static final boolean DEFAULT_NANO_GEO_DEBUG_USE_GRADIENT_ARMOR_TEXTURE = false;
   private static final String DEFAULT_NANO_GEO_DEBUG_BONE_NAME = "";
   private static final String DEFAULT_NANO_GEO_DEBUG_RENDER_ONLY_BONE = "";
   private static final int DEFAULT_NANO_GEO_DEBUG_RENDER_ONLY_CUBE_INDEX = -1;
   private static final String DEFAULT_NANO_GEO_DEBUG_RENDER_ONLY_FACE = "";
   private static final String DEFAULT_NANO_GEO_DEBUG_LOG_UV_FOR_BONE = "";
   private static final double DEFAULT_NANO_GEO_DEBUG_FORCE_BONE_ROTATION_X = 0.0;
   private static final double DEFAULT_NANO_GEO_DEBUG_FORCE_BONE_ROTATION_Y = 0.0;
   private static final double DEFAULT_NANO_GEO_DEBUG_FORCE_BONE_ROTATION_Z = 0.0;
   private static final NanoGeoClientConfig.NanoGeoDebugRenderType DEFAULT_NANO_GEO_DEBUG_RENDER_TYPE = NanoGeoClientConfig.NanoGeoDebugRenderType.ARMOR_CUTOUT_NO_CULL;
   private static final BooleanValue NANO_GEO_DEBUG_RENDER_ENABLED = BUILDER.comment("Enable NanoGeo static debug model rendering.")
      .define("nanoGeoDebugRenderEnabled", false);
   private static final BooleanValue NANO_GEO_LOG_MODEL_LOAD = BUILDER.comment("Log NanoGeo model loading and baking.").define("nanoGeoLogModelLoad", true);
   private static final BooleanValue NANO_GEO_USE_EXPERIMENTAL_ARMOR_RENDERER = BUILDER.comment("Reserved flag for future NanoGeo armor renderer switch.")
      .define("nanoGeoUseExperimentalArmorRenderer", true);
   private static final BooleanValue NANO_GEO_EXPERIMENTAL_ARMOR_ITEMS_ENABLED = BUILDER.comment(
         "Enable exposure of NanoGeo experimental armor items in creative tab."
      )
      .define("nanoGeoExperimentalArmorItemsEnabled", true);
   private static final BooleanValue NANO_GEO_EXPERIMENTAL_ARMOR_COUNTS_AS_NANOSUIT = BUILDER.comment(
         "Allow experimental NanoGeo armor set to count as full nanosuit set for gameplay checks."
      )
      .define("nanoGeoExperimentalArmorCountsAsNanosuit", false);
   private static final BooleanValue NANO_GEO_FPV_ARMS_ENABLED = BUILDER.comment("Enable NanoGeo first-person armor arm renderer for NanoGeo nanosuit armor.")
      .define("nanoGeoFpvArmsEnabled", true);
   private static final BooleanValue NANO_GEO_FPV_ARMS_RENDER_BOTH_HANDS = BUILDER.comment(
         "Render both first-person arms for NanoGeo nanosuit (false = render only main arm)."
      )
      .define("nanoGeoFpvArmsRenderBothHands", true);
   private static final BooleanValue NANO_GEO_FPV_ARMS_DEBUG = BUILDER.comment("Enable NanoGeo first-person arm renderer debug logs.")
      .define("nanoGeoFpvArmsDebug", false);
   private static final BooleanValue NANO_GEO_DEBUG_FPV_VISIBLE_BONES = BUILDER.comment("Log NanoGeo first-person visible arm bones when building masks.")
      .define("nanoGeoDebugFpvVisibleBones", false);
   private static final BooleanValue ARMOR_MODE_OVERLAY_ENABLED = BUILDER.comment("Enable armor-mode texture overlay pass for NanoGeo armor.")
      .define("armorModeOverlayEnabled", true);
   private static final EnumValue<NanoGeoClientConfig.ArmorModeOverlayBackend> ARMOR_MODE_OVERLAY_BACKEND = BUILDER.comment(
         "Armor-mode overlay backend. AUTO = custom shader normally, texture fallback when shaderpacks are active. DynamicTexture compat remains manual experimental."
      )
      .defineEnum("armorModeOverlayBackend", DEFAULT_ARMOR_MODE_OVERLAY_BACKEND);
   private static final ConfigValue<String> ARMOR_MODE_OVERLAY_TEXTURE = BUILDER.comment("Resource location of armor-mode overlay texture.")
      .define("armorModeOverlayTexture", "crysismod:textures/armor/nanosuit_armor_mode.png");
   private static final IntValue ARMOR_MODE_OVERLAY_ALPHA = BUILDER.comment("Whole armor-mode overlay alpha in 0..255.")
      .defineInRange("armorModeOverlayAlpha", 205, 0, 255);
   private static final DoubleValue ARMOR_MODE_OVERLAY_BRIGHTNESS = BUILDER.comment("Brightness multiplier for the armor-mode overlay RGB layer.")
      .defineInRange("armorModeOverlayBrightness", 1.5, 0.0, 4.0);
   private static final BooleanValue ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_SHELL_ENABLED = BUILDER.comment(
         "Experimental: slightly inflate the shaderpack-compatible DynamicTexture overlay shell."
      )
      .define("armorModeDynamicTextureCompatShellEnabled", true);
   private static final DoubleValue ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_SHELL_SCALE = BUILDER.comment(
         "Experimental: shell scale delta for the shaderpack-compatible DynamicTexture overlay."
      )
      .defineInRange("armorModeDynamicTextureCompatShellScale", 0.004F, 0.0, 0.02);
   private static final IntValue ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_MAX_RESOLUTION = BUILDER.comment(
         "Max internal texture resolution used by the shaderpack-compatible DynamicTexture overlay backend."
      )
      .defineInRange("armorModeDynamicTextureCompatMaxResolution", 256, 64, 1024);
   private static final IntValue ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_UPDATE_INTERVAL_MILLIS = BUILDER.comment(
         "Minimum interval between DynamicTexture compat backend updates in milliseconds."
      )
      .defineInRange("armorModeDynamicTextureCompatUpdateIntervalMillis", 50, 16, 250);
   private static final DoubleValue ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_GLOW_STRENGTH = BUILDER.comment(
         "Strength of bright-area glow bias for the shaderpack-compatible DynamicTexture base flow."
      )
      .defineInRange("armorModeDynamicTextureCompatGlowStrength", 0.3F, 0.0, 1.5);
   private static final BooleanValue ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_UV_COVERAGE_MASK_ENABLED = BUILDER.comment(
         "Restrict the DynamicTexture base flow to UV regions used by the NanoGeo armor model."
      )
      .define("armorModeDynamicTextureCompatUvCoverageMaskEnabled", true);
   private static final EnumValue<NanoGeoClientConfig.ArmorModeCompatPerformancePreset> ARMOR_MODE_COMPAT_PERFORMANCE_PRESET = BUILDER.comment(
         "Shaderpack-compatible DynamicTexture performance preset. BASE_COMPOSITE_FAST replaces the steady third-person nanosuit+Armor Mode base with one precomposited pass while keeping pulses unshaded. UNSHADED_ARMOR_LAYER renders only the Armor Mode alpha layer through the emissive path to preserve its source colors under shaderpacks."
      )
      .defineEnum("armorModeCompatPerformancePreset", DEFAULT_ARMOR_MODE_COMPAT_PERFORMANCE_PRESET);
   private static final DoubleValue ARMOR_MODE_OVERLAY_BASE_ALPHA = BUILDER.comment("Base alpha for armor-mode overlay pass.")
      .defineInRange("armorModeOverlayBaseAlpha", 0.35, 0.0, 1.0);
   private static final DoubleValue ARMOR_MODE_OVERLAY_PULSE_AMOUNT = BUILDER.comment("Sin pulse alpha amplitude for armor-mode overlay.")
      .defineInRange("armorModeOverlayPulseAmount", 0.0, 0.0, 1.0);
   private static final DoubleValue ARMOR_MODE_OVERLAY_PULSE_SPEED = BUILDER.comment("Sin pulse speed multiplier for armor-mode overlay.")
      .defineInRange("armorModeOverlayPulseSpeed", 0.0, 0.0, 2.0);
   private static final BooleanValue ARMOR_MODE_OVERLAY_ACTIVATION_PULSE_ENABLED = BUILDER.comment("Enable short activation pulse when armor mode turns on.")
      .define("armorModeOverlayActivationPulseEnabled", false);
   private static final IntValue ARMOR_MODE_OVERLAY_ACTIVATION_DURATION_TICKS = BUILDER.comment("Activation pulse duration in ticks.")
      .defineInRange("armorModeOverlayActivationDurationTicks", 10, 1, 200);
   private static final DoubleValue ARMOR_MODE_OVERLAY_ACTIVATION_STRENGTH = BUILDER.comment("Activation pulse alpha boost strength.")
      .defineInRange("armorModeOverlayActivationStrength", 0.35, 0.0, 1.0);
   private static final BooleanValue ARMOR_MODE_OVERLAY_FULLBRIGHT = BUILDER.comment("Use full-bright light for armor-mode overlay pass.")
      .define("armorModeOverlayFullbright", true);
   private static final DoubleValue ARMOR_MODE_OVERLAY_TINT_R = BUILDER.comment("Armor-mode overlay color filter R multiplier.")
      .defineInRange("armorModeOverlayTintR", 1.0, 0.0, 2.0);
   private static final DoubleValue ARMOR_MODE_OVERLAY_TINT_G = BUILDER.comment("Armor-mode overlay color filter G multiplier.")
      .defineInRange("armorModeOverlayTintG", 0.9F, 0.0, 2.0);
   private static final DoubleValue ARMOR_MODE_OVERLAY_TINT_B = BUILDER.comment("Armor-mode overlay color filter B multiplier.")
      .defineInRange("armorModeOverlayTintB", 1.0, 0.0, 2.0);
   private static final DoubleValue ARMOR_MODE_OVERLAY_GLINT_INTENSITY = BUILDER.comment("Vanilla enchant-glint pass intensity for armor-mode overlay.")
      .defineInRange("armorModeOverlayGlintIntensity", 0.45, 0.0, 1.0);
   private static final BooleanValue ARMOR_MODE_OVERLAY_WAVE_ENABLED = BUILDER.comment("Enable moving UV wave alpha modulation for armor-mode overlay.")
      .define("armorModeOverlayWaveEnabled", false);
   private static final DoubleValue ARMOR_MODE_OVERLAY_WAVE_INTENSITY = BUILDER.comment(
         "How strongly wave lowers local overlay alpha (0 = off, 1 = full removal)."
      )
      .defineInRange("armorModeOverlayWaveIntensity", 0.3, 0.0, 1.0);
   private static final DoubleValue ARMOR_MODE_OVERLAY_WAVE_WIDTH = BUILDER.comment("Normalized wave band width in UV space.")
      .defineInRange("armorModeOverlayWaveWidth", 0.24, 0.01, 1.0);
   private static final DoubleValue ARMOR_MODE_OVERLAY_WAVE_SPEED = BUILDER.comment("Wave travel speed multiplier.")
      .defineInRange("armorModeOverlayWaveSpeed", 0.85, 0.0, 3.0);
   private static final DoubleValue ARMOR_MODE_OVERLAY_WAVE_FREQUENCY = BUILDER.comment("Wave repetition frequency across UV space.")
      .defineInRange("armorModeOverlayWaveFrequency", 2.2, 0.2, 8.0);
   private static final BooleanValue ARMOR_MODE_DIAMOND_SHIMMER_ENABLED = BUILDER.comment("Enable moving diamond alpha-cut pattern for armor-mode overlay.")
      .define("armorModeDiamondShimmerEnabled", false);
   private static final DoubleValue ARMOR_MODE_DIAMOND_SHIMMER_SPEED_PIXELS_PER_SECOND = BUILDER.comment(
         "Diamond shimmer travel speed in texture pixels per second."
      )
      .defineInRange("armorModeDiamondShimmerSpeedPixelsPerSecond", 1.0, 0.0, 32.0);
   private static final IntValue ARMOR_MODE_DIAMOND_WIDTH_PX = BUILDER.comment("Diamond cutout width in texture pixels.")
      .defineInRange("armorModeDiamondWidthPx", 3, 1, 64);
   private static final IntValue ARMOR_MODE_DIAMOND_HEIGHT_PX = BUILDER.comment("Diamond cutout height in texture pixels.")
      .defineInRange("armorModeDiamondHeightPx", 5, 1, 64);
   private static final IntValue ARMOR_MODE_DIAMOND_ALPHA = BUILDER.comment("Overlay alpha inside diamond cutouts in 0..255.")
      .defineInRange("armorModeDiamondAlpha", 0, 0, 255);
   private static final EnumValue<NanoGeoClientConfig.ArmorModeDiamondShimmerDirection> ARMOR_MODE_DIAMOND_SHIMMER_DIRECTION = BUILDER.comment(
         "Diagonal travel direction for armor-mode diamond shimmer."
      )
      .defineEnum("armorModeDiamondShimmerDirection", DEFAULT_ARMOR_MODE_DIAMOND_SHIMMER_DIRECTION);
   private static final EnumValue<NanoGeoClientConfig.ArmorModeOverlayPatternMode> ARMOR_MODE_OVERLAY_PATTERN_MODE = BUILDER.comment(
         "Procedural armor-mode overlay pattern mode."
      )
      .defineEnum("armorModeOverlayPatternMode", DEFAULT_ARMOR_MODE_OVERLAY_PATTERN_MODE);
   private static final BooleanValue ARMOR_MODE_X_SHIMMER_ENABLED = BUILDER.comment("Enable shader-based moving X shimmer on the armor-mode overlay.")
      .define("armorModeXShimmerEnabled", true);
   private static final IntValue ARMOR_MODE_X_SHIMMER_ALPHA = BUILDER.comment("X shimmer highlight strength in 0..255.")
      .defineInRange("armorModeXShimmerAlpha", 90, 0, 255);
   private static final IntValue ARMOR_MODE_X_SHIMMER_SIZE_PX = BUILDER.comment("X shimmer box size in texture pixels.")
      .defineInRange("armorModeXShimmerSizePx", 5, 1, 32);
   private static final IntValue ARMOR_MODE_X_SHIMMER_SPACING_PX = BUILDER.comment("X shimmer grid spacing in texture pixels.")
      .defineInRange("armorModeXShimmerSpacingPx", 16, 5, 64);
   private static final DoubleValue ARMOR_MODE_X_SHIMMER_LINE_WIDTH_PX = BUILDER.comment("X shimmer diagonal line width in texture pixels.")
      .defineInRange("armorModeXShimmerLineWidthPx", 1.0, 0.25, 8.0);
   private static final DoubleValue ARMOR_MODE_X_SHIMMER_EDGE_SOFTNESS_PX = BUILDER.comment("X shimmer edge softness in texture pixels.")
      .defineInRange("armorModeXShimmerEdgeSoftnessPx", 1.0, 0.0, 8.0);
   private static final DoubleValue ARMOR_MODE_X_SHIMMER_SPEED_PX_PER_SECOND = BUILDER.comment("X shimmer travel speed in texture pixels per second.")
      .defineInRange("armorModeXShimmerSpeedPxPerSecond", 1.0, 0.0, 64.0);
   private static final DoubleValue ARMOR_MODE_X_SHIMMER_DIRECTION_X = BUILDER.comment("X shimmer travel direction X component.")
      .defineInRange("armorModeXShimmerDirectionX", 1.0, -1.0, 1.0);
   private static final DoubleValue ARMOR_MODE_X_SHIMMER_DIRECTION_Y = BUILDER.comment("X shimmer travel direction Y component.")
      .defineInRange("armorModeXShimmerDirectionY", 1.0, -1.0, 1.0);
   private static final BooleanValue ARMOR_MODE_X_SHIMMER_AS_CUTOUT = BUILDER.comment("Use X shimmer as alpha cutout instead of highlight boost.")
      .define("armorModeXShimmerAsCutout", false);
   private static final BooleanValue DEBUG_ARMOR_MODE_X_SHIMMER = BUILDER.comment("Enable armor-mode X shimmer debug logs.")
      .define("debugArmorModeXShimmer", false);
   private static final DoubleValue ARMOR_MODE_HEX_CELL_SIZE_PX = BUILDER.comment("Hex overlay cell size in texture pixels.")
      .defineInRange("armorModeHexCellSizePx", 18.0, 4.0, 128.0);
   private static final DoubleValue ARMOR_MODE_HEX_LINE_WIDTH_PX = BUILDER.comment("Hex overlay border width in texture pixels.")
      .defineInRange("armorModeHexLineWidthPx", 2.0, 0.25, 16.0);
   private static final DoubleValue ARMOR_MODE_HEX_EDGE_SOFTNESS_PX = BUILDER.comment("Hex overlay edge softness in texture pixels.")
      .defineInRange("armorModeHexEdgeSoftnessPx", 1.0, 0.0, 8.0);
   private static final IntValue ARMOR_MODE_HEX_INNER_ALPHA = BUILDER.comment("Overlay alpha inside hex cells in 0..255.")
      .defineInRange("armorModeHexInnerAlpha", 0, 0, 255);
   private static final IntValue ARMOR_MODE_HEX_GLOW_ALPHA = BUILDER.comment("Overlay alpha on hex borders and between hex cells in 0..255.")
      .defineInRange("armorModeHexGlowAlpha", 180, 0, 255);
   private static final DoubleValue ARMOR_MODE_HEX_FREQUENCY = BUILDER.comment("Hex overlay frequency multiplier.")
      .defineInRange("armorModeHexFrequency", 1.0, 0.25, 8.0);
   private static final DoubleValue ARMOR_MODE_HEX_SCROLL_SPEED_PX_PER_SECOND = BUILDER.comment("Hex overlay scroll speed in texture pixels per second.")
      .defineInRange("armorModeHexScrollSpeedPxPerSecond", 0.5, 0.0, 64.0);
   private static final DoubleValue ARMOR_MODE_HEX_DIRECTION_X = BUILDER.comment("Hex overlay scroll direction X component.")
      .defineInRange("armorModeHexDirectionX", 1.0, -1.0, 1.0);
   private static final DoubleValue ARMOR_MODE_HEX_DIRECTION_Y = BUILDER.comment("Hex overlay scroll direction Y component.")
      .defineInRange("armorModeHexDirectionY", 0.5, -1.0, 1.0);
   private static final DoubleValue ARMOR_MODE_HEX_TEXTURE_SIZE_PX = BUILDER.comment("Texture size assumption for procedural hex overlay UV space.")
      .defineInRange("armorModeHexTextureSizePx", 512.0, 64.0, 4096.0);
   private static final DoubleValue ARMOR_MODE_HEX_PATTERN_SCALE = BUILDER.comment(
         "Scales the overall visual size/detail of the hex pattern and border glow together."
      )
      .defineInRange("armorModeHexPatternScale", 1.0, 0.25, 8.0);
   private static final DoubleValue ARMOR_MODE_HEX_GLOW_BOOST = BUILDER.comment("Additional glow boost on hex borders.")
      .defineInRange("armorModeHexGlowBoost", 0.25, 0.0, 2.0);
   private static final DoubleValue ARMOR_MODE_SHEEN_BREATHING_AMOUNT = BUILDER.comment("Global breathing emissive amount for the universal nano sheen mode.")
      .defineInRange("armorModeSheenBreathingAmount", 0.12, 0.0, 1.0);
   private static final DoubleValue ARMOR_MODE_SHEEN_BREATHING_SPEED = BUILDER.comment("Global breathing emissive speed for the universal nano sheen mode.")
      .defineInRange("armorModeSheenBreathingSpeed", 0.07, 0.0, 2.0);
   private static final DoubleValue ARMOR_MODE_SHEEN_ALPHA = BUILDER.comment("Strength of the wide directional sheen band.")
      .defineInRange("armorModeSheenAlpha", 0.45, 0.0, 2.0);
   private static final DoubleValue ARMOR_MODE_SHEEN_TRANSPARENCY = BUILDER.comment("How strongly the directional sheen band cuts overlay alpha.")
      .defineInRange("armorModeSheenTransparency", 0.45, 0.0, 1.0);
   private static final DoubleValue ARMOR_MODE_SHEEN_WIDTH_PX = BUILDER.comment("Width of the directional sheen band in texture pixels.")
      .defineInRange("armorModeSheenWidthPx", 64.0, 4.0, 256.0);
   private static final DoubleValue ARMOR_MODE_SHEEN_SOFTNESS_PX = BUILDER.comment("Softness of the directional sheen band in texture pixels.")
      .defineInRange("armorModeSheenSoftnessPx", 48.0, 0.0, 256.0);
   private static final DoubleValue ARMOR_MODE_SHEEN_REPEAT_PX = BUILDER.comment("Repeat period of the directional sheen in texture pixels.")
      .defineInRange("armorModeSheenRepeatPx", 220.0, 32.0, 1024.0);
   private static final DoubleValue ARMOR_MODE_SHEEN_SPEED_PX_PER_SECOND = BUILDER.comment(
         "Travel speed of the directional sheen in texture pixels per second."
      )
      .defineInRange("armorModeSheenSpeedPxPerSecond", 6.0, 0.0, 128.0);
   private static final DoubleValue ARMOR_MODE_SHEEN_DIRECTION_X = BUILDER.comment("Directional sheen X axis component.")
      .defineInRange("armorModeSheenDirectionX", 1.0, -1.0, 1.0);
   private static final DoubleValue ARMOR_MODE_SHEEN_DIRECTION_Y = BUILDER.comment("Directional sheen Y axis component.")
      .defineInRange("armorModeSheenDirectionY", 0.35, -1.0, 1.0);
   private static final DoubleValue ARMOR_MODE_SHEEN_COLOR_BOOST = BUILDER.comment("Cyan-white color boost inside the directional sheen band.")
      .defineInRange("armorModeSheenColorBoost", 0.25, 0.0, 2.0);
   private static final BooleanValue ARMOR_MODE_FIBER_FLOW_ENABLED = BUILDER.comment("Enable subtle fiber-flow shimmer for the universal nano sheen mode.")
      .define("armorModeFiberFlowEnabled", true);
   private static final DoubleValue ARMOR_MODE_FIBER_FLOW_STRENGTH = BUILDER.comment("Strength of the subtle fiber-flow shimmer.")
      .defineInRange("armorModeFiberFlowStrength", 0.08, 0.0, 1.0);
   private static final DoubleValue ARMOR_MODE_FIBER_FLOW_SCALE = BUILDER.comment("Spatial scale of the fiber-flow shimmer.")
      .defineInRange("armorModeFiberFlowScale", 0.045, 0.001, 0.25);
   private static final DoubleValue ARMOR_MODE_FIBER_FLOW_SPEED = BUILDER.comment("Temporal speed of the fiber-flow shimmer.")
      .defineInRange("armorModeFiberFlowSpeed", 0.8, 0.0, 8.0);
   private static final BooleanValue DEBUG_ARMOR_MODE_UNIVERSAL_SHEEN = BUILDER.comment("Enable armor-mode universal sheen debug logs.")
      .define("debugArmorModeUniversalSheen", false);
   private static final IntValue ARMOR_MODE_PLASMA_BLOB_INNER_ALPHA = BUILDER.comment("Overlay alpha inside plasma blobs in 0..255.")
      .defineInRange("armorModePlasmaBlobInnerAlpha", 25, 0, 255);
   private static final IntValue ARMOR_MODE_PLASMA_BLOB_COUNT = BUILDER.comment("Number of moving plasma blobs used by the plasma field mode.")
      .defineInRange("armorModePlasmaBlobCount", 100, 1, 512);
   private static final DoubleValue ARMOR_MODE_PLASMA_BLOB_MIN_SIZE_PX = BUILDER.comment("Minimum plasma blob radius in texture pixels.")
      .defineInRange("armorModePlasmaBlobMinSizePx", 29.156998F, 4.0, 256.0);
   private static final DoubleValue ARMOR_MODE_PLASMA_BLOB_MAX_SIZE_PX = BUILDER.comment("Maximum plasma blob radius in texture pixels.")
      .defineInRange("armorModePlasmaBlobMaxSizePx", 32.17406F, 4.0, 512.0);
   private static final DoubleValue ARMOR_MODE_PLASMA_BLOB_EDGE_SOFTNESS_PX = BUILDER.comment("Softness of the plasma blob falloff in texture pixels.")
      .defineInRange("armorModePlasmaBlobEdgeSoftnessPx", 0.0, 0.0, 128.0);
   private static final IntValue ARMOR_MODE_PLASMA_BLOB_RIM_ALPHA = BUILDER.comment("Alpha of the plasma blob bright rim in 0..255.")
      .defineInRange("armorModePlasmaBlobRimAlpha", 27, 0, 255);
   private static final DoubleValue ARMOR_MODE_PLASMA_BLOB_RIM_WIDTH_PX = BUILDER.comment("Width of the plasma blob rim in texture pixels.")
      .defineInRange("armorModePlasmaBlobRimWidthPx", 2.3481228F, 0.0, 64.0);
   private static final DoubleValue ARMOR_MODE_PLASMA_BLOB_RIM_BRIGHTNESS = BUILDER.comment("Brightness multiplier of the plasma blob rim.")
      .defineInRange("armorModePlasmaBlobRimBrightness", 0.29692832F, 0.0, 4.0);
   private static final DoubleValue ARMOR_MODE_PLASMA_BLOB_DRIFT_SPEED_PX_PER_SECOND = BUILDER.comment(
         "Texture-space drift speed of plasma blobs in pixels per second."
      )
      .defineInRange("armorModePlasmaBlobDriftSpeedPxPerSecond", 6.552901F, 0.0, 128.0);
   private static final DoubleValue ARMOR_MODE_PLASMA_BLOB_DRIFT_RANGE_SCALE = BUILDER.comment(
         "Multiplier for how far plasma blobs can drift away from their base cell center. Values above 1 may let blobs cross into neighboring cells."
      )
      .defineInRange("armorModePlasmaBlobDriftRangeScale", 2.0, 0.0, 2.0);
   private static final DoubleValue ARMOR_MODE_PLASMA_BLOB_SHAPE_DISTORTION = BUILDER.comment("Shape distortion amount for plasma blobs.")
      .defineInRange("armorModePlasmaBlobShapeDistortion", 0.35F, 0.0, 1.0);
   private static final IntValue ARMOR_MODE_PATCH_TEXTURE_ALPHA = BUILDER.comment(
         "Base alpha of the overlay texture for the chaotic patch flow mode, excluding pulse rings."
      )
      .defineInRange("armorModePatchTextureAlpha", 254, 0, 255);
   private static final IntValue ARMOR_MODE_PATCH_SPOT_ALPHA = BUILDER.comment("Transparency strength inside chaotic patch-flow spots in 0..255.")
      .defineInRange("armorModePatchSpotAlpha", 245, 0, 255);
   private static final DoubleValue ARMOR_MODE_PATCH_MIN_SIZE_PX = BUILDER.comment("Minimum block size in texture pixels for chaotic patch-flow spots.")
      .defineInRange("armorModePatchMinSizePx", 10.1F, 1.0, 16.0);
   private static final DoubleValue ARMOR_MODE_PATCH_MAX_SIZE_PX = BUILDER.comment("Maximum block size in texture pixels for chaotic patch-flow spots.")
      .defineInRange("armorModePatchMaxSizePx", 16.0, 1.0, 16.0);
   private static final DoubleValue ARMOR_MODE_PATCH_FREQUENCY = BUILDER.comment("How quickly chaotic patch-flow spots fade in and out.")
      .defineInRange("armorModePatchFrequency", 0.5, 0.1, 6.0);
   private static final DoubleValue ARMOR_MODE_PATCH_DENSITY = BUILDER.comment("Fraction of texture area that can participate in chaotic patch-flow spots.")
      .defineInRange("armorModePatchDensity", 0.88F, 0.0, 1.0);
   private static final EnumValue<NanoGeoClientConfig.ArmorModePatchFlowVariant> ARMOR_MODE_PATCH_FLOW_VARIANT = BUILDER.comment(
         "Select which chaotic patch-flow animation variant is used."
      )
      .defineEnum("armorModePatchFlowVariant", DEFAULT_ARMOR_MODE_PATCH_FLOW_VARIANT);
   private static final DoubleValue ARMOR_MODE_PATCH_CLOUD_EDGE_SOFTNESS = BUILDER.comment(
         "Edge softness multiplier used only by the soft cloud chaotic patch-flow variant."
      )
      .defineInRange("armorModePatchCloudEdgeSoftness", 0.25, 0.25, 3.0);
   private static final BooleanValue ARMOR_MODE_PLASMA_PULSE_ENABLED = BUILDER.comment("Enable expanding plasma pulse arcs.")
      .define("armorModePlasmaPulseEnabled", true);
   private static final IntValue ARMOR_MODE_PLASMA_PULSE_COUNT = BUILDER.comment("Number of plasma pulse centers.")
      .defineInRange("armorModePlasmaPulseCount", 5, 1, 512);
   private static final DoubleValue ARMOR_MODE_PLASMA_PULSE_INTERVAL_SECONDS = BUILDER.comment(
         "Interval between synchronized plasma pulse generations in seconds."
      )
      .defineInRange("armorModePlasmaPulseIntervalSeconds", 1.0, 0.25, 10.0);
   private static final DoubleValue ARMOR_MODE_PLASMA_PULSE_DURATION_SECONDS = BUILDER.comment("Lifetime of one plasma pulse generation in seconds.")
      .defineInRange("armorModePlasmaPulseDurationSeconds", 2.0, 0.25, 5.0);
   private static final IntValue ARMOR_MODE_PLASMA_PULSE_ALPHA = BUILDER.comment("Base alpha of plasma pulse arcs in 0..255.")
      .defineInRange("armorModePlasmaPulseAlpha", 190, 0, 255);
   private static final DoubleValue ARMOR_MODE_PLASMA_PULSE_START_RADIUS_PX = BUILDER.comment("Start radius of plasma pulse arcs in texture pixels.")
      .defineInRange("armorModePlasmaPulseStartRadiusPx", 0.0, 0.0, 256.0);
   private static final DoubleValue ARMOR_MODE_PLASMA_PULSE_END_RADIUS_PX = BUILDER.comment("End radius of plasma pulse arcs in texture pixels.")
      .defineInRange("armorModePlasmaPulseEndRadiusPx", 96.0, 4.0, 512.0);
   private static final DoubleValue ARMOR_MODE_PLASMA_PULSE_SPREAD_SPEED_PX_PER_SECOND = BUILDER.comment(
         "Spread speed of plasma pulse rings in texture pixels per second."
      )
      .defineInRange("armorModePlasmaPulseSpreadSpeedPxPerSecond", 1.0, 0.0, 64.0);
   private static final DoubleValue ARMOR_MODE_PLASMA_PULSE_LINE_WIDTH_PX = BUILDER.comment("Line width of plasma pulse arcs in texture pixels.")
      .defineInRange("armorModePlasmaPulseLineWidthPx", 1.0, 0.25, 32.0);
   private static final DoubleValue ARMOR_MODE_PLASMA_PULSE_EDGE_SOFTNESS_PX = BUILDER.comment("Edge softness of plasma pulse arcs in texture pixels.")
      .defineInRange("armorModePlasmaPulseEdgeSoftnessPx", 1.0, 0.0, 64.0);
   private static final DoubleValue ARMOR_MODE_PLASMA_PULSE_BRIGHTNESS = BUILDER.comment("Brightness multiplier of plasma pulse arcs.")
      .defineInRange("armorModePlasmaPulseBrightness", 1.25, 0.0, 8.0);
   private static final DoubleValue ARMOR_MODE_PLASMA_PULSE_ARC_COVERAGE = BUILDER.comment("Arc coverage of plasma pulse rings (1.0 = full ring).")
      .defineInRange("armorModePlasmaPulseArcCoverage", 0.45, 0.0, 1.0);
   private static final DoubleValue ARMOR_MODE_PLASMA_PULSE_ARC_DISTORTION = BUILDER.comment("Angular/radius distortion of plasma pulse arcs.")
      .defineInRange("armorModePlasmaPulseArcDistortion", 0.25, 0.0, 1.0);
   private static final BooleanValue ARMOR_MODE_PULSE_ENABLED = BUILDER.comment("Enable new closed armor-mode pulse rings from cached marker centers.")
      .define("armorModePulseEnabled", true);
   private static final EnumValue<NanoGeoClientConfig.ArmorModePulseCenterSource> ARMOR_MODE_PULSE_CENTER_SOURCE = BUILDER.comment(
         "Source used by the new armor-mode pulse ring system."
      )
      .defineEnum("armorModePulseCenterSource", DEFAULT_ARMOR_MODE_PULSE_CENTER_SOURCE);
   private static final EnumValue<NanoGeoClientConfig.ArmorModePulseVisualMode> ARMOR_MODE_PULSE_VISUAL_MODE = BUILDER.comment(
         "Visual style used by the armor-mode pulse ring shader."
      )
      .defineEnum("armorModePulseVisualMode", DEFAULT_ARMOR_MODE_PULSE_VISUAL_MODE);
   private static final EnumValue<NanoGeoClientConfig.ArmorModeCompatPulsePreset> ARMOR_MODE_COMPAT_PULSE_PRESET = BUILDER.comment(
         "Shaderpack-compatible DynamicTexture pulse preset. CRISP_MASKED is the new strict UV-masked default, LEGACY_SMOOTH keeps the old smoother pulse sampling."
      )
      .defineEnum("armorModeCompatPulsePreset", DEFAULT_ARMOR_MODE_COMPAT_PULSE_PRESET);
   private static final IntValue ARMOR_MODE_PULSE_MAX_CENTERS = BUILDER.comment("Maximum cached marker centers sent to the pulse shader.")
      .defineInRange("armorModePulseMaxCenters", 53, 1, 64);
   private static final IntValue ARMOR_MODE_PULSE_CENTER_MARKER_ALPHA = BUILDER.comment(
         "Opacity for baked red pulse-center marker pixels in 0..255. Set to 0 to hide the markers from the Armor Mode layer and its preview without disabling pulse rings."
      )
      .defineInRange("armorModePulseCenterMarkerAlpha", 0, 0, 255);
   private static final DoubleValue ARMOR_MODE_PULSE_INTERVAL_SECONDS = BUILDER.comment(
         "Interval between synchronized closed-ring pulse generations in seconds."
      )
      .defineInRange("armorModePulseIntervalSeconds", 0.81F, 0.25, 10.0);
   private static final DoubleValue ARMOR_MODE_PULSE_DURATION_SECONDS = BUILDER.comment("Lifetime of one closed-ring pulse generation in seconds.")
      .defineInRange("armorModePulseDurationSeconds", 1.51F, 0.25, 5.0);
   private static final DoubleValue ARMOR_MODE_PULSE_START_RADIUS_PX = BUILDER.comment("Start radius of each closed-ring pulse in texture pixels.")
      .defineInRange("armorModePulseStartRadiusPx", 0.0, 0.0, 128.0);
   private static final DoubleValue ARMOR_MODE_PULSE_END_RADIUS_PX = BUILDER.comment("End radius of each closed-ring pulse in texture pixels.")
      .defineInRange("armorModePulseEndRadiusPx", 19.07F, 4.0, 512.0);
   private static final DoubleValue ARMOR_MODE_PULSE_CORE_WIDTH_PX = BUILDER.comment("Core line width of each closed-ring pulse in texture pixels.")
      .defineInRange("armorModePulseCoreWidthPx", 0.3F, 0.01, 4.0);
   private static final DoubleValue ARMOR_MODE_PULSE_GLOW_RADIUS_PX = BUILDER.comment("Glow radius around the pulse core in texture pixels.")
      .defineInRange("armorModePulseGlowRadiusPx", 0.29F, 0.0, 8.0);
   private static final DoubleValue ARMOR_MODE_PULSE_CORE_SOFTNESS_PX = BUILDER.comment("Softness of the closed-ring pulse core edge in texture pixels.")
      .defineInRange("armorModePulseCoreSoftnessPx", 0.25, 0.0, 4.0);
   private static final DoubleValue ARMOR_MODE_PULSE_GLOW_SOFTNESS_PX = BUILDER.comment("Softness of the closed-ring pulse glow edge in texture pixels.")
      .defineInRange("armorModePulseGlowSoftnessPx", 0.31F, 0.0, 8.0);
   private static final IntValue ARMOR_MODE_PULSE_CORE_ALPHA = BUILDER.comment("Core alpha of the closed-ring pulse in 0..255.")
      .defineInRange("armorModePulseCoreAlpha", 100, 0, 255);
   private static final IntValue ARMOR_MODE_PULSE_GLOW_ALPHA = BUILDER.comment("Glow alpha of the closed-ring pulse in 0..255.")
      .defineInRange("armorModePulseGlowAlpha", 68, 0, 255);
   private static final ConfigValue<String> ARMOR_MODE_PULSE_CORE_COLOR = BUILDER.comment("Core color of the closed-ring pulse in #RRGGBB format.")
      .define("armorModePulseCoreColor", "#7EC6FC");
   private static final ConfigValue<String> ARMOR_MODE_PULSE_GLOW_COLOR = BUILDER.comment("Glow color of the closed-ring pulse in #RRGGBB format.")
      .define("armorModePulseGlowColor", "#ADD1ED");
   private static final DoubleValue ARMOR_MODE_PULSE_BRIGHTNESS = BUILDER.comment("Brightness multiplier of closed-ring pulses.")
      .defineInRange("armorModePulseBrightness", 0.83F, 0.0, 4.0);
   private static final DoubleValue ARMOR_MODE_PULSE_FADE_POWER = BUILDER.comment("Fade curve power for closed-ring pulses.")
      .defineInRange("armorModePulseFadePower", 2.0, 0.25, 6.0);
   private static final DoubleValue ARMOR_MODE_PULSE_RING_DISTORTION_PX = BUILDER.comment(
         "Subtle radius distortion of closed rings in texture pixels. Default 0 keeps exact circles."
      )
      .defineInRange("armorModePulseRingDistortionPx", 1.32F, 0.0, 16.0);
   private static final DoubleValue ARMOR_MODE_PULSE_RING_DISTORTION_FREQUENCY = BUILDER.comment("Angular frequency of optional closed-ring radius distortion.")
      .defineInRange("armorModePulseRingDistortionFrequency", 1.0, 0.0, 32.0);
   private static final DoubleValue ARMOR_MODE_PULSE_RING_ENERGY_VARIATION = BUILDER.comment("Subtle brightness variation around closed rings.")
      .defineInRange("armorModePulseRingEnergyVariation", 0.2F, 0.0, 1.0);
   private static final DoubleValue ARMOR_MODE_PULSE_WIDTH_VARIATION = BUILDER.comment("How much the closed-ring pulse width varies around the circumference.")
      .defineInRange("armorModePulseWidthVariation", 0.45F, 0.0, 1.0);
   private static final DoubleValue ARMOR_MODE_PULSE_BRIGHTNESS_VARIATION = BUILDER.comment(
         "How much the closed-ring pulse brightness varies around the circumference."
      )
      .defineInRange("armorModePulseBrightnessVariation", 0.39419794F, 0.0, 1.0);
   private static final DoubleValue ARMOR_MODE_PULSE_EDGE_ROUGHNESS = BUILDER.comment(
         "Subtle electric roughness applied to the pulse edge without breaking the ring."
      )
      .defineInRange("armorModePulseEdgeRoughness", 0.24061434F, 0.0, 1.0);
   private static final DoubleValue ARMOR_MODE_PULSE_PATTERN_ROTATION_RANDOMNESS = BUILDER.comment(
         "Per-center phase/rotation randomness applied to the electric closed-ring profile."
      )
      .defineInRange("armorModePulsePatternRotationRandomness", 1.0, 0.0, 1.0);
   private static final BooleanValue ARMOR_MODE_PULSE_MERGING_ENABLED = BUILDER.comment(
         "When enabled, nearby pulse rings softly merge like shared energy fields instead of only crossing."
      )
      .define("armorModePulseMergingEnabled", true);
   private static final DoubleValue ARMOR_MODE_PULSE_MERGE_STRENGTH = BUILDER.comment("How strongly nearby pulse rings pull into a shared merged shape.")
      .defineInRange("armorModePulseMergeStrength", 0.25, 0.0, 1.0);
   private static final DoubleValue ARMOR_MODE_PULSE_MERGE_SOFTNESS_PX = BUILDER.comment("Softness of the pulse-ring merge field falloff.")
      .defineInRange("armorModePulseMergeSoftnessPx", 0.75, 0.0, 16.0);
   private static final DoubleValue ARMOR_MODE_PULSE_MERGE_BRIGHTNESS_BOOST = BUILDER.comment(
         "Extra brightness added to the soft bridge where pulse rings merge."
      )
      .defineInRange("armorModePulseMergeBrightnessBoost", 0.1F, 0.0, 2.0);
   private static final BooleanValue ARMOR_MODE_PULSE_DEBUG_FORCE_VISIBLE = BUILDER.comment(
         "Debug: force closed pulse rings visible by overriding pulse uniforms. Do not leave enabled for normal visuals."
      )
      .define("armorModePulseDebugForceVisible", false);
   private static final BooleanValue ARMOR_MODE_PULSE_DEBUG_OUTPUT_ONLY_PULSE = BUILDER.comment(
         "Debug: shader outputs only pulse contribution, ignoring the base overlay texture."
      )
      .define("armorModePulseDebugOutputOnlyPulse", false);
   private static final BooleanValue DEBUG_ARMOR_MODE_PLASMA_FIELD = BUILDER.comment("Enable armor-mode plasma field debug logs.")
      .define("debugArmorModePlasmaField", false);
   private static final BooleanValue DEBUG_ARMOR_MODE_HEX_PATTERN = BUILDER.comment("Enable armor-mode hex pattern debug logs.")
      .define("debugArmorModeHexPattern", false);
   private static final BooleanValue DEBUG_ARMOR_MODE_SETTINGS_SCREEN = BUILDER.comment("Enable Armor Mode settings screen debug overlay.")
      .define("debugArmorModeSettingsScreen", false);
   private static final BooleanValue ARMOR_MODE_OVERLAY_RENDER_IN_FIRST_PERSON = BUILDER.comment("Render armor-mode overlay in first-person NanoGeo arm pass.")
      .define("armorModeOverlayRenderInFirstPerson", true);
   private static final BooleanValue ARMOR_MODE_OVERLAY_RENDER_IN_THIRD_PERSON = BUILDER.comment(
         "Render armor-mode overlay in third-person NanoGeo armor pass."
      )
      .define("armorModeOverlayRenderInThirdPerson", true);
   private static final BooleanValue ARMOR_MODE_TRANSITION_ENABLED = BUILDER.comment(
         "Enable pixel UV materialization/dissolve transition for the armor-mode overlay."
      )
      .define("armorModeTransitionEnabled", true);
   private static final DoubleValue ARMOR_MODE_TRANSITION_DURATION_SECONDS = BUILDER.comment(
         "Duration of armor-mode overlay activation/deactivation transition in seconds."
      )
      .defineInRange("armorModeTransitionDurationSeconds", 1.0072526F, 0.1, 5.0);
   private static final DoubleValue ARMOR_MODE_TRANSITION_PIXEL_BLOCK_SIZE_PX = BUILDER.comment("Transition pixel block size in overlay texture-space pixels.")
      .defineInRange("armorModeTransitionPixelBlockSizePx", 2.0, 1.0, 2.0);
   private static final DoubleValue ARMOR_MODE_TRANSITION_STAGGER_AMOUNT = BUILDER.comment("How much per-block random delay is used during overlay transition.")
      .defineInRange("armorModeTransitionStaggerAmount", 0.65F, 0.0, 1.0);
   private static final DoubleValue ARMOR_MODE_TRANSITION_FADE_WINDOW = BUILDER.comment("How wide the per-block fade window is during overlay transition.")
      .defineInRange("armorModeTransitionFadeWindow", 0.25, 0.05, 1.0);
   private static final DoubleValue ARMOR_MODE_TRANSITION_SEED = BUILDER.comment("Deterministic seed used by the armor-mode transition pixel mask.")
      .defineInRange("armorModeTransitionSeed", 17.0, -10000.0, 10000.0);
   private static final BooleanValue ARMOR_MODE_TRANSITION_AFFECTS_PULSES = BUILDER.comment("Apply the overlay transition mask to pulse rings as well.")
      .define("armorModeTransitionAffectsPulses", true);
   private static final BooleanValue DEBUG_ARMOR_MODE_OVERLAY = BUILDER.comment("Enable armor-mode overlay debug logs.").define("debugArmorModeOverlay", true);
   private static final BooleanValue DEBUG_ARMOR_MODE_DIAMOND_SHIMMER = BUILDER.comment("Enable armor-mode diamond shimmer debug logs.")
      .define("debugArmorModeDiamondShimmer", false);
   private static final BooleanValue NANO_GEO_DEBUG_UV = BUILDER.comment("Enable NanoGeo UV diagnostics.").define("nanoGeoDebugUv", false);
   private static final BooleanValue NANO_GEO_DEBUG_UV_LOG_CUBES = BUILDER.comment("Log UV cube diagnostics when models are loaded.")
      .define("nanoGeoDebugUvLogCubes", false);
   private static final BooleanValue NANO_GEO_DEBUG_UV_RENDER_CHECKER = BUILDER.comment(
         "Use checker/missing texture in NanoGeo debug render path for UV validation."
      )
      .define("nanoGeoDebugUvRenderChecker", false);
   private static final BooleanValue NANO_GEO_DEBUG_UV_FACE_COLORS = BUILDER.comment("Tint NanoGeo faces by direction for UV orientation debugging.")
      .define("nanoGeoDebugUvFaceColors", false);
   private static final BooleanValue NANO_GEO_DEBUG_VERTEX_COLORS = BUILDER.comment(
         "Tint each vertex corner differently to debug quad vertex order/orientation."
      )
      .define("nanoGeoDebugVertexColors", false);
   private static final BooleanValue NANO_GEO_DEBUG_LOG_RESOURCES = BUILDER.comment("Log NanoGeo model/texture resource details on load/reload.")
      .define("nanoGeoDebugLogResources", false);
   private static final BooleanValue NANO_GEO_DEBUG_LOG_VISIBLE_BONES = BUILDER.comment("Log visible bones used for slot filtering.")
      .define("nanoGeoDebugLogVisibleBones", false);
   private static final BooleanValue NANO_GEO_DEBUG_LOG_BONE_TRANSFORMS = BUILDER.comment("Log selected bone transforms during armor pose mapping.")
      .define("nanoGeoDebugLogBoneTransforms", false);
   private static final BooleanValue NANO_GEO_DEBUG_PERF_ENABLED = BUILDER.comment("Enable lightweight NanoGeo render performance logging.")
      .define("nanoGeoDebugPerfEnabled", false);
   private static final IntValue NANO_GEO_DEBUG_PERF_LOG_INTERVAL = BUILDER.comment("NanoGeo perf logs are printed every N render passes.")
      .defineInRange("nanoGeoDebugPerfLogInterval", 120, 1, Integer.MAX_VALUE);
   private static final BooleanValue NANO_GEO_DEBUG_PERF_LOG_STATIC = BUILDER.comment("Include static debug renderer in NanoGeo perf logging.")
      .define("nanoGeoDebugPerfLogStatic", false);
   private static final BooleanValue NANO_GEO_PERF_PROFILING_ENABLED = BUILDER.comment("Enable detailed staged NanoGeo performance profiler.")
      .define("nanoGeoPerfProfilingEnabled", false);
   private static final IntValue NANO_GEO_PERF_LOG_EVERY_SECONDS = BUILDER.comment("Detailed NanoGeo profiler logs are printed every N seconds.")
      .defineInRange("nanoGeoPerfLogEverySeconds", 5, 1, Integer.MAX_VALUE);
   private static final BooleanValue NANO_GEO_PERF_COMPARE_MODE = BUILDER.comment("Tag NanoGeo perf logs for controlled performance comparison runs.")
      .define("nanoGeoPerfCompareMode", false);
   private static final BooleanValue NANO_GEO_PERF_TRACK_ALLOCATIONS = BUILDER.comment("Track allocation-suspected counters in NanoGeo profiler.")
      .define("nanoGeoPerfTrackAllocations", false);
   private static final BooleanValue NANO_GEO_PERF_SHOW_OVERLAY = BUILDER.comment("Reserved toggle for in-game NanoGeo performance overlay.")
      .define("nanoGeoPerfShowOverlay", false);
   private static final BooleanValue NANO_GEO_SHOW_ADVANCED_DEBUG_OPTIONS = BUILDER.comment("Expose advanced NanoGeo forensic/debug options in Cloth config.")
      .define("nanoGeoShowAdvancedDebugOptions", false);
   private static final BooleanValue NANO_GEO_DEBUG_RENDER_AXES = BUILDER.comment("Reserved: draw debug axes for NanoGeo bones.")
      .define("nanoGeoDebugRenderAxes", false);
   private static final BooleanValue NANO_GEO_DEBUG_SHOW_BONE_PIVOTS = BUILDER.comment("Reserved: draw debug pivots for NanoGeo bones.")
      .define("nanoGeoDebugShowBonePivots", false);
   private static final BooleanValue NANO_GEO_DEBUG_SHOW_BONE_AXES = BUILDER.comment("Draw local bone axes for selected debug bones.")
      .define("nanoGeoDebugShowBoneAxes", false);
   private static final BooleanValue NANO_GEO_DEBUG_TINT_LEFT_RIGHT = BUILDER.comment(
         "Tint left-side bones blue and right-side bones red for side diagnostics."
      )
      .define("nanoGeoDebugTintLeftRight", false);
   private static final BooleanValue NANO_GEO_DEBUG_HIDE_CHILDREN = BUILDER.comment("When render-only-bone is active, hide descendant child bones.")
      .define("nanoGeoDebugHideChildren", false);
   private static final BooleanValue NANO_GEO_DEBUG_FREEZE_PLAYER_POSE = BUILDER.comment("Do not apply live HumanoidModel pose to NanoGeo armor bones.")
      .define("nanoGeoDebugFreezePlayerPose", false);
   private static final ConfigValue<String> NANO_GEO_DEBUG_BONE_NAME = BUILDER.comment("Optional target bone name used by focused NanoGeo debug logs.")
      .define("nanoGeoDebugBoneName", "");
   private static final ConfigValue<String> NANO_GEO_DEBUG_RENDER_ONLY_BONE = BUILDER.comment("If not empty, render only this bone.")
      .define("nanoGeoDebugRenderOnlyBone", "");
   private static final IntValue NANO_GEO_DEBUG_RENDER_ONLY_CUBE_INDEX = BUILDER.comment("If >= 0, render only cube with this index within each rendered bone.")
      .defineInRange("nanoGeoDebugRenderOnlyCubeIndex", -1, -1, Integer.MAX_VALUE);
   private static final ConfigValue<String> NANO_GEO_DEBUG_RENDER_ONLY_FACE = BUILDER.comment("If set to north/south/east/west/up/down, render only that face.")
      .define("nanoGeoDebugRenderOnlyFace", "");
   private static final ConfigValue<String> NANO_GEO_DEBUG_LOG_UV_FOR_BONE = BUILDER.comment("If not empty, UV cube diagnostics are limited to this bone name.")
      .define("nanoGeoDebugLogUvForBone", "");
   private static final DoubleValue NANO_GEO_DEBUG_FORCE_BONE_ROTATION_X = BUILDER.comment("Force-add rotation X (degrees) for focused debug bone.")
      .defineInRange("nanoGeoDebugForceBoneRotationX", 0.0, -360.0, 360.0);
   private static final DoubleValue NANO_GEO_DEBUG_FORCE_BONE_ROTATION_Y = BUILDER.comment("Force-add rotation Y (degrees) for focused debug bone.")
      .defineInRange("nanoGeoDebugForceBoneRotationY", 0.0, -360.0, 360.0);
   private static final DoubleValue NANO_GEO_DEBUG_FORCE_BONE_ROTATION_Z = BUILDER.comment("Force-add rotation Z (degrees) for focused debug bone.")
      .defineInRange("nanoGeoDebugForceBoneRotationZ", 0.0, -360.0, 360.0);
   private static final EnumValue<NanoGeoClientConfig.NanoGeoDebugRenderType> NANO_GEO_DEBUG_RENDER_TYPE = BUILDER.comment(
         "Debug static render type selection."
      )
      .defineEnum("nanoGeoDebugRenderType", DEFAULT_NANO_GEO_DEBUG_RENDER_TYPE);
   private static final BooleanValue NANO_GEO_DEBUG_ARMOR_CHEST_ONLY_FULL_MODEL = BUILDER.comment(
         "Debug mode: render full NanoGeo armor model only in CHEST slot pass and skip other armor slots."
      )
      .define("nanoGeoDebugArmorChestOnlyFullModel", false);
   private static final BooleanValue NANO_GEO_DEBUG_USE_GRADIENT_ARMOR_TEXTURE = BUILDER.comment(
         "Debug mode: experimental NanoGeo armor uses gradient test texture for UV diagnostics."
      )
      .define("nanoGeoDebugUseGradientArmorTexture", false);
   public static final ModConfigSpec SPEC = BUILDER.build();

   private NanoGeoClientConfig() {
   }

   public static boolean nanoGeoDebugRenderEnabled() {
      return safeGetBoolean(NANO_GEO_DEBUG_RENDER_ENABLED, false);
   }

   public static boolean defaultNanoGeoDebugRenderEnabled() {
      return false;
   }

   public static boolean nanoGeoLogModelLoad() {
      return safeGetBoolean(NANO_GEO_LOG_MODEL_LOAD, true);
   }

   public static boolean defaultNanoGeoLogModelLoad() {
      return true;
   }

   public static boolean nanoGeoUseExperimentalArmorRenderer() {
      return safeGetBoolean(NANO_GEO_USE_EXPERIMENTAL_ARMOR_RENDERER, true);
   }

   public static boolean defaultNanoGeoUseExperimentalArmorRenderer() {
      return true;
   }

   public static boolean nanoGeoExperimentalArmorItemsEnabled() {
      return safeGetBoolean(NANO_GEO_EXPERIMENTAL_ARMOR_ITEMS_ENABLED, true);
   }

   public static boolean defaultNanoGeoExperimentalArmorItemsEnabled() {
      return true;
   }

   public static boolean nanoGeoExperimentalArmorCountsAsNanosuit() {
      return safeGetBoolean(NANO_GEO_EXPERIMENTAL_ARMOR_COUNTS_AS_NANOSUIT, false);
   }

   public static boolean defaultNanoGeoExperimentalArmorCountsAsNanosuit() {
      return false;
   }

   public static boolean nanoGeoFpvArmsEnabled() {
      return safeGetBoolean(NANO_GEO_FPV_ARMS_ENABLED, true);
   }

   public static boolean defaultNanoGeoFpvArmsEnabled() {
      return true;
   }

   public static boolean nanoGeoFpvArmsRenderBothHands() {
      return safeGetBoolean(NANO_GEO_FPV_ARMS_RENDER_BOTH_HANDS, true);
   }

   public static boolean defaultNanoGeoFpvArmsRenderBothHands() {
      return true;
   }

   public static boolean nanoGeoFpvArmsDebug() {
      return safeGetBoolean(NANO_GEO_FPV_ARMS_DEBUG, false);
   }

   public static boolean defaultNanoGeoFpvArmsDebug() {
      return false;
   }

   public static boolean nanoGeoDebugFpvVisibleBones() {
      return safeGetBoolean(NANO_GEO_DEBUG_FPV_VISIBLE_BONES, false);
   }

   public static boolean defaultNanoGeoDebugFpvVisibleBones() {
      return false;
   }

   public static boolean armorModeOverlayEnabled() {
      return safeGetBoolean(ARMOR_MODE_OVERLAY_ENABLED, true);
   }

   public static boolean defaultArmorModeOverlayEnabled() {
      return true;
   }

   public static NanoGeoClientConfig.ArmorModeOverlayBackend armorModeOverlayBackend() {
      try {
         NanoGeoClientConfig.ArmorModeOverlayBackend backend = (NanoGeoClientConfig.ArmorModeOverlayBackend)ARMOR_MODE_OVERLAY_BACKEND.get();
         return backend == null ? DEFAULT_ARMOR_MODE_OVERLAY_BACKEND : backend;
      } catch (IllegalStateException var1) {
         return DEFAULT_ARMOR_MODE_OVERLAY_BACKEND;
      }
   }

   public static NanoGeoClientConfig.ArmorModeOverlayBackend defaultArmorModeOverlayBackend() {
      return DEFAULT_ARMOR_MODE_OVERLAY_BACKEND;
   }

   public static String armorModeOverlayTexture() {
      return safeGetString(ARMOR_MODE_OVERLAY_TEXTURE, "crysismod:textures/armor/nanosuit_armor_mode.png");
   }

   public static String defaultArmorModeOverlayTexture() {
      return "crysismod:textures/armor/nanosuit_armor_mode.png";
   }

   public static int armorModeOverlayAlpha() {
      return safeGetInt(ARMOR_MODE_OVERLAY_ALPHA, 205);
   }

   public static int defaultArmorModeOverlayAlpha() {
      return 205;
   }

   public static float armorModeOverlayBrightness() {
      return (float)safeGetDouble(ARMOR_MODE_OVERLAY_BRIGHTNESS, 1.5);
   }

   public static float defaultArmorModeOverlayBrightness() {
      return 1.5F;
   }

   public static boolean armorModeDynamicTextureCompatShellEnabled() {
      return safeGetBoolean(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_SHELL_ENABLED, true);
   }

   public static boolean defaultArmorModeDynamicTextureCompatShellEnabled() {
      return true;
   }

   public static float armorModeDynamicTextureCompatShellScale() {
      return (float)safeGetDouble(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_SHELL_SCALE, 0.004F);
   }

   public static float defaultArmorModeDynamicTextureCompatShellScale() {
      return 0.004F;
   }

   public static int armorModeDynamicTextureCompatMaxResolution() {
      return safeGetInt(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_MAX_RESOLUTION, 256);
   }

   public static int defaultArmorModeDynamicTextureCompatMaxResolution() {
      return 256;
   }

   public static int armorModeDynamicTextureCompatUpdateIntervalMillis() {
      return safeGetInt(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_UPDATE_INTERVAL_MILLIS, 50);
   }

   public static int defaultArmorModeDynamicTextureCompatUpdateIntervalMillis() {
      return 50;
   }

   public static float armorModeDynamicTextureCompatGlowStrength() {
      return (float)safeGetDouble(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_GLOW_STRENGTH, 0.3F);
   }

   public static float defaultArmorModeDynamicTextureCompatGlowStrength() {
      return 0.3F;
   }

   public static boolean armorModeDynamicTextureCompatUvCoverageMaskEnabled() {
      return safeGetBoolean(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_UV_COVERAGE_MASK_ENABLED, true);
   }

   public static boolean defaultArmorModeDynamicTextureCompatUvCoverageMaskEnabled() {
      return true;
   }

   public static NanoGeoClientConfig.ArmorModeCompatPerformancePreset armorModeCompatPerformancePreset() {
      try {
         NanoGeoClientConfig.ArmorModeCompatPerformancePreset preset = (NanoGeoClientConfig.ArmorModeCompatPerformancePreset)ARMOR_MODE_COMPAT_PERFORMANCE_PRESET.get();
         return preset == null ? DEFAULT_ARMOR_MODE_COMPAT_PERFORMANCE_PRESET : preset;
      } catch (RuntimeException var1) {
         return DEFAULT_ARMOR_MODE_COMPAT_PERFORMANCE_PRESET;
      }
   }

   public static NanoGeoClientConfig.ArmorModeCompatPerformancePreset defaultArmorModeCompatPerformancePreset() {
      return DEFAULT_ARMOR_MODE_COMPAT_PERFORMANCE_PRESET;
   }

   public static float armorModeOverlayBaseAlpha() {
      return (float)safeGetDouble(ARMOR_MODE_OVERLAY_BASE_ALPHA, 0.35);
   }

   public static float defaultArmorModeOverlayBaseAlpha() {
      return 0.35F;
   }

   public static float armorModeOverlayPulseAmount() {
      return (float)safeGetDouble(ARMOR_MODE_OVERLAY_PULSE_AMOUNT, 0.0);
   }

   public static float defaultArmorModeOverlayPulseAmount() {
      return 0.0F;
   }

   public static float armorModeOverlayPulseSpeed() {
      return (float)safeGetDouble(ARMOR_MODE_OVERLAY_PULSE_SPEED, 0.0);
   }

   public static float defaultArmorModeOverlayPulseSpeed() {
      return 0.0F;
   }

   public static boolean armorModeOverlayActivationPulseEnabled() {
      return safeGetBoolean(ARMOR_MODE_OVERLAY_ACTIVATION_PULSE_ENABLED, false);
   }

   public static boolean defaultArmorModeOverlayActivationPulseEnabled() {
      return false;
   }

   public static int armorModeOverlayActivationDurationTicks() {
      return safeGetInt(ARMOR_MODE_OVERLAY_ACTIVATION_DURATION_TICKS, 10);
   }

   public static int defaultArmorModeOverlayActivationDurationTicks() {
      return 10;
   }

   public static float armorModeOverlayActivationStrength() {
      return (float)safeGetDouble(ARMOR_MODE_OVERLAY_ACTIVATION_STRENGTH, 0.35);
   }

   public static float defaultArmorModeOverlayActivationStrength() {
      return 0.35F;
   }

   public static boolean armorModeOverlayFullbright() {
      return safeGetBoolean(ARMOR_MODE_OVERLAY_FULLBRIGHT, true);
   }

   public static boolean defaultArmorModeOverlayFullbright() {
      return true;
   }

   public static float armorModeOverlayTintR() {
      return (float)safeGetDouble(ARMOR_MODE_OVERLAY_TINT_R, 1.0);
   }

   public static float defaultArmorModeOverlayTintR() {
      return 1.0F;
   }

   public static float armorModeOverlayTintG() {
      return (float)safeGetDouble(ARMOR_MODE_OVERLAY_TINT_G, 0.9F);
   }

   public static float defaultArmorModeOverlayTintG() {
      return 0.9F;
   }

   public static float armorModeOverlayTintB() {
      return (float)safeGetDouble(ARMOR_MODE_OVERLAY_TINT_B, 1.0);
   }

   public static float defaultArmorModeOverlayTintB() {
      return 1.0F;
   }

   public static float armorModeOverlayGlintIntensity() {
      return (float)safeGetDouble(ARMOR_MODE_OVERLAY_GLINT_INTENSITY, 0.45);
   }

   public static float defaultArmorModeOverlayGlintIntensity() {
      return 0.45F;
   }

   public static boolean armorModeOverlayWaveEnabled() {
      return safeGetBoolean(ARMOR_MODE_OVERLAY_WAVE_ENABLED, false);
   }

   public static boolean defaultArmorModeOverlayWaveEnabled() {
      return false;
   }

   public static float armorModeOverlayWaveIntensity() {
      return (float)safeGetDouble(ARMOR_MODE_OVERLAY_WAVE_INTENSITY, 0.3);
   }

   public static float defaultArmorModeOverlayWaveIntensity() {
      return 0.3F;
   }

   public static float armorModeOverlayWaveWidth() {
      return (float)safeGetDouble(ARMOR_MODE_OVERLAY_WAVE_WIDTH, 0.24);
   }

   public static float defaultArmorModeOverlayWaveWidth() {
      return 0.24F;
   }

   public static float armorModeOverlayWaveSpeed() {
      return (float)safeGetDouble(ARMOR_MODE_OVERLAY_WAVE_SPEED, 0.85);
   }

   public static float defaultArmorModeOverlayWaveSpeed() {
      return 0.85F;
   }

   public static float armorModeOverlayWaveFrequency() {
      return (float)safeGetDouble(ARMOR_MODE_OVERLAY_WAVE_FREQUENCY, 2.2);
   }

   public static float defaultArmorModeOverlayWaveFrequency() {
      return 2.2F;
   }

   public static boolean armorModeDiamondShimmerEnabled() {
      return safeGetBoolean(ARMOR_MODE_DIAMOND_SHIMMER_ENABLED, false);
   }

   public static boolean defaultArmorModeDiamondShimmerEnabled() {
      return false;
   }

   public static float armorModeDiamondShimmerSpeedPixelsPerSecond() {
      return (float)safeGetDouble(ARMOR_MODE_DIAMOND_SHIMMER_SPEED_PIXELS_PER_SECOND, 1.0);
   }

   public static float defaultArmorModeDiamondShimmerSpeedPixelsPerSecond() {
      return 1.0F;
   }

   public static int armorModeDiamondWidthPx() {
      return safeGetInt(ARMOR_MODE_DIAMOND_WIDTH_PX, 3);
   }

   public static int defaultArmorModeDiamondWidthPx() {
      return 3;
   }

   public static int armorModeDiamondHeightPx() {
      return safeGetInt(ARMOR_MODE_DIAMOND_HEIGHT_PX, 5);
   }

   public static int defaultArmorModeDiamondHeightPx() {
      return 5;
   }

   public static int armorModeDiamondAlpha() {
      return safeGetInt(ARMOR_MODE_DIAMOND_ALPHA, 0);
   }

   public static int defaultArmorModeDiamondAlpha() {
      return 0;
   }

   public static NanoGeoClientConfig.ArmorModeDiamondShimmerDirection armorModeDiamondShimmerDirection() {
      try {
         return (NanoGeoClientConfig.ArmorModeDiamondShimmerDirection)ARMOR_MODE_DIAMOND_SHIMMER_DIRECTION.get();
      } catch (IllegalStateException var1) {
         return DEFAULT_ARMOR_MODE_DIAMOND_SHIMMER_DIRECTION;
      }
   }

   public static NanoGeoClientConfig.ArmorModeDiamondShimmerDirection defaultArmorModeDiamondShimmerDirection() {
      return DEFAULT_ARMOR_MODE_DIAMOND_SHIMMER_DIRECTION;
   }

   public static NanoGeoClientConfig.ArmorModeOverlayPatternMode armorModeOverlayPatternMode() {
      return NanoGeoClientConfig.ArmorModeOverlayPatternMode.CHAOTIC_PATCH_FLOW;
   }

   public static NanoGeoClientConfig.ArmorModeOverlayPatternMode defaultArmorModeOverlayPatternMode() {
      return DEFAULT_ARMOR_MODE_OVERLAY_PATTERN_MODE;
   }

   public static boolean armorModeXShimmerEnabled() {
      return safeGetBoolean(ARMOR_MODE_X_SHIMMER_ENABLED, true);
   }

   public static boolean defaultArmorModeXShimmerEnabled() {
      return true;
   }

   public static int armorModeXShimmerAlpha() {
      return safeGetInt(ARMOR_MODE_X_SHIMMER_ALPHA, 90);
   }

   public static int defaultArmorModeXShimmerAlpha() {
      return 90;
   }

   public static int armorModeXShimmerSizePx() {
      return safeGetInt(ARMOR_MODE_X_SHIMMER_SIZE_PX, 5);
   }

   public static int defaultArmorModeXShimmerSizePx() {
      return 5;
   }

   public static int armorModeXShimmerSpacingPx() {
      return safeGetInt(ARMOR_MODE_X_SHIMMER_SPACING_PX, 16);
   }

   public static int defaultArmorModeXShimmerSpacingPx() {
      return 16;
   }

   public static float armorModeXShimmerLineWidthPx() {
      return (float)safeGetDouble(ARMOR_MODE_X_SHIMMER_LINE_WIDTH_PX, 1.0);
   }

   public static float defaultArmorModeXShimmerLineWidthPx() {
      return 1.0F;
   }

   public static float armorModeXShimmerEdgeSoftnessPx() {
      return (float)safeGetDouble(ARMOR_MODE_X_SHIMMER_EDGE_SOFTNESS_PX, 1.0);
   }

   public static float defaultArmorModeXShimmerEdgeSoftnessPx() {
      return 1.0F;
   }

   public static float armorModeXShimmerSpeedPxPerSecond() {
      return (float)safeGetDouble(ARMOR_MODE_X_SHIMMER_SPEED_PX_PER_SECOND, 1.0);
   }

   public static float defaultArmorModeXShimmerSpeedPxPerSecond() {
      return 1.0F;
   }

   public static float armorModeXShimmerDirectionX() {
      return (float)safeGetDouble(ARMOR_MODE_X_SHIMMER_DIRECTION_X, 1.0);
   }

   public static float defaultArmorModeXShimmerDirectionX() {
      return 1.0F;
   }

   public static float armorModeXShimmerDirectionY() {
      return (float)safeGetDouble(ARMOR_MODE_X_SHIMMER_DIRECTION_Y, 1.0);
   }

   public static float defaultArmorModeXShimmerDirectionY() {
      return 1.0F;
   }

   public static boolean armorModeXShimmerAsCutout() {
      return safeGetBoolean(ARMOR_MODE_X_SHIMMER_AS_CUTOUT, false);
   }

   public static boolean defaultArmorModeXShimmerAsCutout() {
      return false;
   }

   public static boolean debugArmorModeXShimmer() {
      return safeGetBoolean(DEBUG_ARMOR_MODE_X_SHIMMER, false);
   }

   public static boolean defaultDebugArmorModeXShimmer() {
      return false;
   }

   public static float armorModeHexCellSizePx() {
      return (float)safeGetDouble(ARMOR_MODE_HEX_CELL_SIZE_PX, 18.0);
   }

   public static float defaultArmorModeHexCellSizePx() {
      return 18.0F;
   }

   public static float armorModeHexLineWidthPx() {
      return (float)safeGetDouble(ARMOR_MODE_HEX_LINE_WIDTH_PX, 2.0);
   }

   public static float defaultArmorModeHexLineWidthPx() {
      return 2.0F;
   }

   public static float armorModeHexEdgeSoftnessPx() {
      return (float)safeGetDouble(ARMOR_MODE_HEX_EDGE_SOFTNESS_PX, 1.0);
   }

   public static float defaultArmorModeHexEdgeSoftnessPx() {
      return 1.0F;
   }

   public static int armorModeHexInnerAlpha() {
      return safeGetInt(ARMOR_MODE_HEX_INNER_ALPHA, 0);
   }

   public static int defaultArmorModeHexInnerAlpha() {
      return 0;
   }

   public static int armorModeHexGlowAlpha() {
      return safeGetInt(ARMOR_MODE_HEX_GLOW_ALPHA, 180);
   }

   public static int defaultArmorModeHexGlowAlpha() {
      return 180;
   }

   public static float armorModeHexFrequency() {
      return (float)safeGetDouble(ARMOR_MODE_HEX_FREQUENCY, 1.0);
   }

   public static float defaultArmorModeHexFrequency() {
      return 1.0F;
   }

   public static float armorModeHexScrollSpeedPxPerSecond() {
      return (float)safeGetDouble(ARMOR_MODE_HEX_SCROLL_SPEED_PX_PER_SECOND, 0.5);
   }

   public static float defaultArmorModeHexScrollSpeedPxPerSecond() {
      return 0.5F;
   }

   public static float armorModeHexDirectionX() {
      return (float)safeGetDouble(ARMOR_MODE_HEX_DIRECTION_X, 1.0);
   }

   public static float defaultArmorModeHexDirectionX() {
      return 1.0F;
   }

   public static float armorModeHexDirectionY() {
      return (float)safeGetDouble(ARMOR_MODE_HEX_DIRECTION_Y, 0.5);
   }

   public static float defaultArmorModeHexDirectionY() {
      return 0.5F;
   }

   public static float armorModeHexTextureSizePx() {
      return (float)safeGetDouble(ARMOR_MODE_HEX_TEXTURE_SIZE_PX, 512.0);
   }

   public static float defaultArmorModeHexTextureSizePx() {
      return 512.0F;
   }

   public static NanoGeoClientConfig.ArmorModeHexTexturePreset armorModeHexTexturePreset() {
      return NanoGeoClientConfig.ArmorModeHexTexturePreset.closestTo(armorModeHexTextureSizePx());
   }

   public static NanoGeoClientConfig.ArmorModeHexTexturePreset defaultArmorModeHexTexturePreset() {
      return NanoGeoClientConfig.ArmorModeHexTexturePreset.closestTo(defaultArmorModeHexTextureSizePx());
   }

   public static float armorModeHexPatternScale() {
      return (float)safeGetDouble(ARMOR_MODE_HEX_PATTERN_SCALE, 1.0);
   }

   public static float defaultArmorModeHexPatternScale() {
      return 1.0F;
   }

   public static float armorModeHexGlowBoost() {
      return (float)safeGetDouble(ARMOR_MODE_HEX_GLOW_BOOST, 0.25);
   }

   public static float defaultArmorModeHexGlowBoost() {
      return 0.25F;
   }

   public static float armorModeSheenBreathingAmount() {
      return (float)safeGetDouble(ARMOR_MODE_SHEEN_BREATHING_AMOUNT, 0.12);
   }

   public static float defaultArmorModeSheenBreathingAmount() {
      return 0.12F;
   }

   public static float armorModeSheenBreathingSpeed() {
      return (float)safeGetDouble(ARMOR_MODE_SHEEN_BREATHING_SPEED, 0.07);
   }

   public static float defaultArmorModeSheenBreathingSpeed() {
      return 0.07F;
   }

   public static float armorModeSheenAlpha() {
      return (float)safeGetDouble(ARMOR_MODE_SHEEN_ALPHA, 0.45);
   }

   public static float defaultArmorModeSheenAlpha() {
      return 0.45F;
   }

   public static float armorModeSheenTransparency() {
      return (float)safeGetDouble(ARMOR_MODE_SHEEN_TRANSPARENCY, 0.45);
   }

   public static float defaultArmorModeSheenTransparency() {
      return 0.45F;
   }

   public static float armorModeSheenWidthPx() {
      return (float)safeGetDouble(ARMOR_MODE_SHEEN_WIDTH_PX, 64.0);
   }

   public static float defaultArmorModeSheenWidthPx() {
      return 64.0F;
   }

   public static float armorModeSheenSoftnessPx() {
      return (float)safeGetDouble(ARMOR_MODE_SHEEN_SOFTNESS_PX, 48.0);
   }

   public static float defaultArmorModeSheenSoftnessPx() {
      return 48.0F;
   }

   public static float armorModeSheenRepeatPx() {
      return (float)safeGetDouble(ARMOR_MODE_SHEEN_REPEAT_PX, 220.0);
   }

   public static float defaultArmorModeSheenRepeatPx() {
      return 220.0F;
   }

   public static float armorModeSheenSpeedPxPerSecond() {
      return (float)safeGetDouble(ARMOR_MODE_SHEEN_SPEED_PX_PER_SECOND, 6.0);
   }

   public static float defaultArmorModeSheenSpeedPxPerSecond() {
      return 6.0F;
   }

   public static float armorModeSheenDirectionX() {
      return (float)safeGetDouble(ARMOR_MODE_SHEEN_DIRECTION_X, 1.0);
   }

   public static float defaultArmorModeSheenDirectionX() {
      return 1.0F;
   }

   public static float armorModeSheenDirectionY() {
      return (float)safeGetDouble(ARMOR_MODE_SHEEN_DIRECTION_Y, 0.35);
   }

   public static float defaultArmorModeSheenDirectionY() {
      return 0.35F;
   }

   public static float armorModeSheenColorBoost() {
      return (float)safeGetDouble(ARMOR_MODE_SHEEN_COLOR_BOOST, 0.25);
   }

   public static float defaultArmorModeSheenColorBoost() {
      return 0.25F;
   }

   public static boolean armorModeFiberFlowEnabled() {
      return safeGetBoolean(ARMOR_MODE_FIBER_FLOW_ENABLED, true);
   }

   public static boolean defaultArmorModeFiberFlowEnabled() {
      return true;
   }

   public static float armorModeFiberFlowStrength() {
      return (float)safeGetDouble(ARMOR_MODE_FIBER_FLOW_STRENGTH, 0.08);
   }

   public static float defaultArmorModeFiberFlowStrength() {
      return 0.08F;
   }

   public static float armorModeFiberFlowScale() {
      return (float)safeGetDouble(ARMOR_MODE_FIBER_FLOW_SCALE, 0.045);
   }

   public static float defaultArmorModeFiberFlowScale() {
      return 0.045F;
   }

   public static float armorModeFiberFlowSpeed() {
      return (float)safeGetDouble(ARMOR_MODE_FIBER_FLOW_SPEED, 0.8);
   }

   public static float defaultArmorModeFiberFlowSpeed() {
      return 0.8F;
   }

   public static boolean debugArmorModeUniversalSheen() {
      return safeGetBoolean(DEBUG_ARMOR_MODE_UNIVERSAL_SHEEN, false);
   }

   public static boolean defaultDebugArmorModeUniversalSheen() {
      return false;
   }

   public static int armorModePlasmaBlobInnerAlpha() {
      return safeGetInt(ARMOR_MODE_PLASMA_BLOB_INNER_ALPHA, 25);
   }

   public static int defaultArmorModePlasmaBlobInnerAlpha() {
      return 25;
   }

   public static int armorModePlasmaBlobCount() {
      return safeGetInt(ARMOR_MODE_PLASMA_BLOB_COUNT, 100);
   }

   public static int defaultArmorModePlasmaBlobCount() {
      return 100;
   }

   public static float armorModePlasmaBlobMinSizePx() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_BLOB_MIN_SIZE_PX, 29.156998F);
   }

   public static float defaultArmorModePlasmaBlobMinSizePx() {
      return 29.156998F;
   }

   public static float armorModePlasmaBlobMaxSizePx() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_BLOB_MAX_SIZE_PX, 32.17406F);
   }

   public static float defaultArmorModePlasmaBlobMaxSizePx() {
      return 32.17406F;
   }

   public static float armorModePlasmaBlobEdgeSoftnessPx() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_BLOB_EDGE_SOFTNESS_PX, 0.0);
   }

   public static float defaultArmorModePlasmaBlobEdgeSoftnessPx() {
      return 0.0F;
   }

   public static int armorModePlasmaBlobRimAlpha() {
      return safeGetInt(ARMOR_MODE_PLASMA_BLOB_RIM_ALPHA, 27);
   }

   public static int defaultArmorModePlasmaBlobRimAlpha() {
      return 27;
   }

   public static float armorModePlasmaBlobRimWidthPx() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_BLOB_RIM_WIDTH_PX, 2.3481228F);
   }

   public static float defaultArmorModePlasmaBlobRimWidthPx() {
      return 2.3481228F;
   }

   public static float armorModePlasmaBlobRimBrightness() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_BLOB_RIM_BRIGHTNESS, 0.29692832F);
   }

   public static float defaultArmorModePlasmaBlobRimBrightness() {
      return 0.29692832F;
   }

   public static float armorModePlasmaBlobDriftSpeedPxPerSecond() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_BLOB_DRIFT_SPEED_PX_PER_SECOND, 6.552901F);
   }

   public static float defaultArmorModePlasmaBlobDriftSpeedPxPerSecond() {
      return 6.552901F;
   }

   public static float armorModePlasmaBlobDriftRangeScale() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_BLOB_DRIFT_RANGE_SCALE, 2.0);
   }

   public static float defaultArmorModePlasmaBlobDriftRangeScale() {
      return 2.0F;
   }

   public static float armorModePlasmaBlobShapeDistortion() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_BLOB_SHAPE_DISTORTION, 0.35F);
   }

   public static float defaultArmorModePlasmaBlobShapeDistortion() {
      return 0.35F;
   }

   public static int armorModePatchTextureAlpha() {
      return safeGetInt(ARMOR_MODE_PATCH_TEXTURE_ALPHA, 254);
   }

   public static int defaultArmorModePatchTextureAlpha() {
      return 254;
   }

   public static int armorModePatchSpotAlpha() {
      return safeGetInt(ARMOR_MODE_PATCH_SPOT_ALPHA, 245);
   }

   public static int defaultArmorModePatchSpotAlpha() {
      return 245;
   }

   public static float armorModePatchMinSizePx() {
      return (float)safeGetDouble(ARMOR_MODE_PATCH_MIN_SIZE_PX, 10.1F);
   }

   public static float defaultArmorModePatchMinSizePx() {
      return 10.1F;
   }

   public static float armorModePatchMaxSizePx() {
      return (float)safeGetDouble(ARMOR_MODE_PATCH_MAX_SIZE_PX, 16.0);
   }

   public static float defaultArmorModePatchMaxSizePx() {
      return 16.0F;
   }

   public static float armorModePatchFrequency() {
      return (float)safeGetDouble(ARMOR_MODE_PATCH_FREQUENCY, 0.5);
   }

   public static float defaultArmorModePatchFrequency() {
      return 0.5F;
   }

   public static float armorModePatchDensity() {
      return (float)safeGetDouble(ARMOR_MODE_PATCH_DENSITY, 0.88F);
   }

   public static float defaultArmorModePatchDensity() {
      return 0.88F;
   }

   public static NanoGeoClientConfig.ArmorModePatchFlowVariant armorModePatchFlowVariant() {
      try {
         return (NanoGeoClientConfig.ArmorModePatchFlowVariant)ARMOR_MODE_PATCH_FLOW_VARIANT.get();
      } catch (IllegalStateException var1) {
         return DEFAULT_ARMOR_MODE_PATCH_FLOW_VARIANT;
      }
   }

   public static NanoGeoClientConfig.ArmorModePatchFlowVariant defaultArmorModePatchFlowVariant() {
      return DEFAULT_ARMOR_MODE_PATCH_FLOW_VARIANT;
   }

   public static float armorModePatchCloudEdgeSoftness() {
      return (float)safeGetDouble(ARMOR_MODE_PATCH_CLOUD_EDGE_SOFTNESS, 0.25);
   }

   public static float defaultArmorModePatchCloudEdgeSoftness() {
      return 0.25F;
   }

   public static boolean armorModePlasmaPulseEnabled() {
      return safeGetBoolean(ARMOR_MODE_PLASMA_PULSE_ENABLED, true);
   }

   public static boolean defaultArmorModePlasmaPulseEnabled() {
      return true;
   }

   public static int armorModePlasmaPulseCount() {
      return safeGetInt(ARMOR_MODE_PLASMA_PULSE_COUNT, 5);
   }

   public static int defaultArmorModePlasmaPulseCount() {
      return 5;
   }

   public static float armorModePlasmaPulseIntervalSeconds() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_PULSE_INTERVAL_SECONDS, 1.0);
   }

   public static float defaultArmorModePlasmaPulseIntervalSeconds() {
      return 1.0F;
   }

   public static float armorModePlasmaPulseDurationSeconds() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_PULSE_DURATION_SECONDS, 2.0);
   }

   public static float defaultArmorModePlasmaPulseDurationSeconds() {
      return 2.0F;
   }

   public static int armorModePlasmaPulseAlpha() {
      return safeGetInt(ARMOR_MODE_PLASMA_PULSE_ALPHA, 190);
   }

   public static int defaultArmorModePlasmaPulseAlpha() {
      return 190;
   }

   public static float armorModePlasmaPulseStartRadiusPx() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_PULSE_START_RADIUS_PX, 0.0);
   }

   public static float defaultArmorModePlasmaPulseStartRadiusPx() {
      return 0.0F;
   }

   public static float armorModePlasmaPulseEndRadiusPx() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_PULSE_END_RADIUS_PX, 96.0);
   }

   public static float defaultArmorModePlasmaPulseEndRadiusPx() {
      return 96.0F;
   }

   public static float armorModePlasmaPulseSpreadSpeedPxPerSecond() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_PULSE_SPREAD_SPEED_PX_PER_SECOND, 1.0);
   }

   public static float defaultArmorModePlasmaPulseSpreadSpeedPxPerSecond() {
      return 1.0F;
   }

   public static float armorModePlasmaPulseLineWidthPx() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_PULSE_LINE_WIDTH_PX, 1.0);
   }

   public static float defaultArmorModePlasmaPulseLineWidthPx() {
      return 1.0F;
   }

   public static float armorModePlasmaPulseEdgeSoftnessPx() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_PULSE_EDGE_SOFTNESS_PX, 1.0);
   }

   public static float defaultArmorModePlasmaPulseEdgeSoftnessPx() {
      return 1.0F;
   }

   public static float armorModePlasmaPulseBrightness() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_PULSE_BRIGHTNESS, 1.25);
   }

   public static float defaultArmorModePlasmaPulseBrightness() {
      return 1.25F;
   }

   public static float armorModePlasmaPulseArcCoverage() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_PULSE_ARC_COVERAGE, 0.45);
   }

   public static float defaultArmorModePlasmaPulseArcCoverage() {
      return 0.45F;
   }

   public static float armorModePlasmaPulseArcDistortion() {
      return (float)safeGetDouble(ARMOR_MODE_PLASMA_PULSE_ARC_DISTORTION, 0.25);
   }

   public static float defaultArmorModePlasmaPulseArcDistortion() {
      return 0.25F;
   }

   public static boolean armorModePulseEnabled() {
      return safeGetBoolean(ARMOR_MODE_PULSE_ENABLED, true);
   }

   public static boolean defaultArmorModePulseEnabled() {
      return true;
   }

   public static NanoGeoClientConfig.ArmorModePulseCenterSource armorModePulseCenterSource() {
      try {
         return (NanoGeoClientConfig.ArmorModePulseCenterSource)ARMOR_MODE_PULSE_CENTER_SOURCE.get();
      } catch (RuntimeException var1) {
         return DEFAULT_ARMOR_MODE_PULSE_CENTER_SOURCE;
      }
   }

   public static NanoGeoClientConfig.ArmorModePulseCenterSource defaultArmorModePulseCenterSource() {
      return DEFAULT_ARMOR_MODE_PULSE_CENTER_SOURCE;
   }

   public static NanoGeoClientConfig.ArmorModePulseVisualMode armorModePulseVisualMode() {
      try {
         return (NanoGeoClientConfig.ArmorModePulseVisualMode)ARMOR_MODE_PULSE_VISUAL_MODE.get();
      } catch (RuntimeException var1) {
         return DEFAULT_ARMOR_MODE_PULSE_VISUAL_MODE;
      }
   }

   public static NanoGeoClientConfig.ArmorModePulseVisualMode defaultArmorModePulseVisualMode() {
      return DEFAULT_ARMOR_MODE_PULSE_VISUAL_MODE;
   }

   public static NanoGeoClientConfig.ArmorModeCompatPulsePreset armorModeCompatPulsePreset() {
      try {
         return (NanoGeoClientConfig.ArmorModeCompatPulsePreset)ARMOR_MODE_COMPAT_PULSE_PRESET.get();
      } catch (RuntimeException var1) {
         return DEFAULT_ARMOR_MODE_COMPAT_PULSE_PRESET;
      }
   }

   public static NanoGeoClientConfig.ArmorModeCompatPulsePreset defaultArmorModeCompatPulsePreset() {
      return DEFAULT_ARMOR_MODE_COMPAT_PULSE_PRESET;
   }

   public static int armorModePulseMaxCenters() {
      return safeGetInt(ARMOR_MODE_PULSE_MAX_CENTERS, 53);
   }

   public static int defaultArmorModePulseMaxCenters() {
      return 53;
   }

   public static int armorModePulseCenterMarkerAlpha() {
      return safeGetInt(ARMOR_MODE_PULSE_CENTER_MARKER_ALPHA, 0);
   }

   public static int defaultArmorModePulseCenterMarkerAlpha() {
      return 0;
   }

   public static float armorModePulseIntervalSeconds() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_INTERVAL_SECONDS, 0.81F);
   }

   public static float defaultArmorModePulseIntervalSeconds() {
      return 0.81F;
   }

   public static float armorModePulseDurationSeconds() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_DURATION_SECONDS, 1.51F);
   }

   public static float defaultArmorModePulseDurationSeconds() {
      return 1.51F;
   }

   public static float armorModePulseStartRadiusPx() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_START_RADIUS_PX, 0.0);
   }

   public static float defaultArmorModePulseStartRadiusPx() {
      return 0.0F;
   }

   public static float armorModePulseEndRadiusPx() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_END_RADIUS_PX, 19.07F);
   }

   public static float defaultArmorModePulseEndRadiusPx() {
      return 19.07F;
   }

   public static float armorModePulseCoreWidthPx() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_CORE_WIDTH_PX, 0.3F);
   }

   public static float defaultArmorModePulseCoreWidthPx() {
      return 0.3F;
   }

   public static float armorModePulseGlowRadiusPx() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_GLOW_RADIUS_PX, 0.29F);
   }

   public static float defaultArmorModePulseGlowRadiusPx() {
      return 0.29F;
   }

   public static float armorModePulseCoreSoftnessPx() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_CORE_SOFTNESS_PX, 0.25);
   }

   public static float defaultArmorModePulseCoreSoftnessPx() {
      return 0.25F;
   }

   public static float armorModePulseGlowSoftnessPx() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_GLOW_SOFTNESS_PX, 0.31F);
   }

   public static float defaultArmorModePulseGlowSoftnessPx() {
      return 0.31F;
   }

   public static int armorModePulseCoreAlpha() {
      return safeGetInt(ARMOR_MODE_PULSE_CORE_ALPHA, 100);
   }

   public static int defaultArmorModePulseCoreAlpha() {
      return 100;
   }

   public static int armorModePulseGlowAlpha() {
      return safeGetInt(ARMOR_MODE_PULSE_GLOW_ALPHA, 68);
   }

   public static int defaultArmorModePulseGlowAlpha() {
      return 68;
   }

   public static String armorModePulseCoreColor() {
      return normalizeRgbHexColor(safeGetString(ARMOR_MODE_PULSE_CORE_COLOR, "#7EC6FC"), "#7EC6FC");
   }

   public static String defaultArmorModePulseCoreColor() {
      return "#7EC6FC";
   }

   public static int armorModePulseCoreColorRgb() {
      return parseRgbHexColor(armorModePulseCoreColor(), "#7EC6FC");
   }

   public static String armorModePulseGlowColor() {
      return normalizeRgbHexColor(safeGetString(ARMOR_MODE_PULSE_GLOW_COLOR, "#ADD1ED"), "#ADD1ED");
   }

   public static String defaultArmorModePulseGlowColor() {
      return "#ADD1ED";
   }

   public static int armorModePulseGlowColorRgb() {
      return parseRgbHexColor(armorModePulseGlowColor(), "#ADD1ED");
   }

   public static float armorModePulseBrightness() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_BRIGHTNESS, 0.83F);
   }

   public static float defaultArmorModePulseBrightness() {
      return 0.83F;
   }

   public static float armorModePulseFadePower() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_FADE_POWER, 2.0);
   }

   public static float defaultArmorModePulseFadePower() {
      return 2.0F;
   }

   public static float armorModePulseRingDistortionPx() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_RING_DISTORTION_PX, 1.32F);
   }

   public static float defaultArmorModePulseRingDistortionPx() {
      return 1.32F;
   }

   public static float armorModePulseRingDistortionFrequency() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_RING_DISTORTION_FREQUENCY, 1.0);
   }

   public static float defaultArmorModePulseRingDistortionFrequency() {
      return 1.0F;
   }

   public static float armorModePulseRingEnergyVariation() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_RING_ENERGY_VARIATION, 0.2F);
   }

   public static float defaultArmorModePulseRingEnergyVariation() {
      return 0.2F;
   }

   public static float armorModePulseWidthVariation() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_WIDTH_VARIATION, 0.45F);
   }

   public static float defaultArmorModePulseWidthVariation() {
      return 0.45F;
   }

   public static float armorModePulseBrightnessVariation() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_BRIGHTNESS_VARIATION, 0.39419794F);
   }

   public static float defaultArmorModePulseBrightnessVariation() {
      return 0.39419794F;
   }

   public static float armorModePulseEdgeRoughness() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_EDGE_ROUGHNESS, 0.24061434F);
   }

   public static float defaultArmorModePulseEdgeRoughness() {
      return 0.24061434F;
   }

   public static float armorModePulsePatternRotationRandomness() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_PATTERN_ROTATION_RANDOMNESS, 1.0);
   }

   public static float defaultArmorModePulsePatternRotationRandomness() {
      return 1.0F;
   }

   public static boolean armorModePulseMergingEnabled() {
      return safeGetBoolean(ARMOR_MODE_PULSE_MERGING_ENABLED, true);
   }

   public static boolean defaultArmorModePulseMergingEnabled() {
      return true;
   }

   public static float armorModePulseMergeStrength() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_MERGE_STRENGTH, 0.25);
   }

   public static float defaultArmorModePulseMergeStrength() {
      return 0.25F;
   }

   public static float armorModePulseMergeSoftnessPx() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_MERGE_SOFTNESS_PX, 0.75);
   }

   public static float defaultArmorModePulseMergeSoftnessPx() {
      return 0.75F;
   }

   public static float armorModePulseMergeBrightnessBoost() {
      return (float)safeGetDouble(ARMOR_MODE_PULSE_MERGE_BRIGHTNESS_BOOST, 0.1F);
   }

   public static float defaultArmorModePulseMergeBrightnessBoost() {
      return 0.1F;
   }

   public static boolean armorModePulseDebugForceVisible() {
      return safeGetBoolean(ARMOR_MODE_PULSE_DEBUG_FORCE_VISIBLE, false);
   }

   public static boolean defaultArmorModePulseDebugForceVisible() {
      return false;
   }

   public static boolean armorModePulseDebugOutputOnlyPulse() {
      return safeGetBoolean(ARMOR_MODE_PULSE_DEBUG_OUTPUT_ONLY_PULSE, false);
   }

   public static boolean defaultArmorModePulseDebugOutputOnlyPulse() {
      return false;
   }

   public static boolean debugArmorModePlasmaField() {
      return safeGetBoolean(DEBUG_ARMOR_MODE_PLASMA_FIELD, false);
   }

   public static boolean defaultDebugArmorModePlasmaField() {
      return false;
   }

   public static boolean debugArmorModeHexPattern() {
      return safeGetBoolean(DEBUG_ARMOR_MODE_HEX_PATTERN, false);
   }

   public static boolean defaultDebugArmorModeHexPattern() {
      return false;
   }

   public static boolean debugArmorModeSettingsScreen() {
      return safeGetBoolean(DEBUG_ARMOR_MODE_SETTINGS_SCREEN, false);
   }

   public static boolean defaultDebugArmorModeSettingsScreen() {
      return false;
   }

   public static boolean armorModeOverlayRenderInFirstPerson() {
      return safeGetBoolean(ARMOR_MODE_OVERLAY_RENDER_IN_FIRST_PERSON, true);
   }

   public static boolean defaultArmorModeOverlayRenderInFirstPerson() {
      return true;
   }

   public static boolean armorModeOverlayRenderInThirdPerson() {
      return safeGetBoolean(ARMOR_MODE_OVERLAY_RENDER_IN_THIRD_PERSON, true);
   }

   public static boolean defaultArmorModeOverlayRenderInThirdPerson() {
      return true;
   }

   public static boolean armorModeTransitionEnabled() {
      return safeGetBoolean(ARMOR_MODE_TRANSITION_ENABLED, true);
   }

   public static boolean defaultArmorModeTransitionEnabled() {
      return true;
   }

   public static float armorModeTransitionDurationSeconds() {
      return (float)safeGetDouble(ARMOR_MODE_TRANSITION_DURATION_SECONDS, 1.0072526F);
   }

   public static float defaultArmorModeTransitionDurationSeconds() {
      return 1.0072526F;
   }

   public static float armorModeTransitionPixelBlockSizePx() {
      return (float)safeGetDouble(ARMOR_MODE_TRANSITION_PIXEL_BLOCK_SIZE_PX, 2.0);
   }

   public static float defaultArmorModeTransitionPixelBlockSizePx() {
      return 2.0F;
   }

   public static float armorModeTransitionStaggerAmount() {
      return (float)safeGetDouble(ARMOR_MODE_TRANSITION_STAGGER_AMOUNT, 0.65F);
   }

   public static float defaultArmorModeTransitionStaggerAmount() {
      return 0.65F;
   }

   public static float armorModeTransitionFadeWindow() {
      return (float)safeGetDouble(ARMOR_MODE_TRANSITION_FADE_WINDOW, 0.25);
   }

   public static float defaultArmorModeTransitionFadeWindow() {
      return 0.25F;
   }

   public static float armorModeTransitionSeed() {
      return (float)safeGetDouble(ARMOR_MODE_TRANSITION_SEED, 17.0);
   }

   public static float defaultArmorModeTransitionSeed() {
      return 17.0F;
   }

   public static boolean armorModeTransitionAffectsPulses() {
      return safeGetBoolean(ARMOR_MODE_TRANSITION_AFFECTS_PULSES, true);
   }

   public static boolean defaultArmorModeTransitionAffectsPulses() {
      return true;
   }

   public static boolean debugArmorModeOverlay() {
      return safeGetBoolean(DEBUG_ARMOR_MODE_OVERLAY, true);
   }

   public static boolean defaultDebugArmorModeOverlay() {
      return true;
   }

   public static boolean debugArmorModeDiamondShimmer() {
      return safeGetBoolean(DEBUG_ARMOR_MODE_DIAMOND_SHIMMER, false);
   }

   public static boolean defaultDebugArmorModeDiamondShimmer() {
      return false;
   }

   public static boolean nanoGeoDebugUv() {
      return safeGetBoolean(NANO_GEO_DEBUG_UV, false);
   }

   public static boolean defaultNanoGeoDebugUv() {
      return false;
   }

   public static boolean nanoGeoDebugUvLogCubes() {
      return safeGetBoolean(NANO_GEO_DEBUG_UV_LOG_CUBES, false);
   }

   public static boolean defaultNanoGeoDebugUvLogCubes() {
      return false;
   }

   public static boolean nanoGeoDebugUvRenderChecker() {
      return safeGetBoolean(NANO_GEO_DEBUG_UV_RENDER_CHECKER, false);
   }

   public static boolean defaultNanoGeoDebugUvRenderChecker() {
      return false;
   }

   public static boolean nanoGeoDebugUvFaceColors() {
      return safeGetBoolean(NANO_GEO_DEBUG_UV_FACE_COLORS, false);
   }

   public static boolean defaultNanoGeoDebugUvFaceColors() {
      return false;
   }

   public static boolean nanoGeoDebugVertexColors() {
      return safeGetBoolean(NANO_GEO_DEBUG_VERTEX_COLORS, false);
   }

   public static boolean defaultNanoGeoDebugVertexColors() {
      return false;
   }

   public static boolean nanoGeoDebugLogResources() {
      return safeGetBoolean(NANO_GEO_DEBUG_LOG_RESOURCES, false);
   }

   public static boolean defaultNanoGeoDebugLogResources() {
      return false;
   }

   public static boolean nanoGeoDebugLogVisibleBones() {
      return safeGetBoolean(NANO_GEO_DEBUG_LOG_VISIBLE_BONES, false);
   }

   public static boolean defaultNanoGeoDebugLogVisibleBones() {
      return false;
   }

   public static boolean nanoGeoDebugLogBoneTransforms() {
      return safeGetBoolean(NANO_GEO_DEBUG_LOG_BONE_TRANSFORMS, false);
   }

   public static boolean defaultNanoGeoDebugLogBoneTransforms() {
      return false;
   }

   public static boolean nanoGeoDebugPerfEnabled() {
      return safeGetBoolean(NANO_GEO_DEBUG_PERF_ENABLED, false);
   }

   public static boolean defaultNanoGeoDebugPerfEnabled() {
      return false;
   }

   public static int nanoGeoDebugPerfLogInterval() {
      return safeGetInt(NANO_GEO_DEBUG_PERF_LOG_INTERVAL, 120);
   }

   public static int defaultNanoGeoDebugPerfLogInterval() {
      return 120;
   }

   public static boolean nanoGeoDebugPerfLogStatic() {
      return safeGetBoolean(NANO_GEO_DEBUG_PERF_LOG_STATIC, false);
   }

   public static boolean defaultNanoGeoDebugPerfLogStatic() {
      return false;
   }

   public static boolean nanoGeoPerfProfilingEnabled() {
      return safeGetBoolean(NANO_GEO_PERF_PROFILING_ENABLED, false);
   }

   public static boolean defaultNanoGeoPerfProfilingEnabled() {
      return false;
   }

   public static int nanoGeoPerfLogEverySeconds() {
      return safeGetInt(NANO_GEO_PERF_LOG_EVERY_SECONDS, 5);
   }

   public static int defaultNanoGeoPerfLogEverySeconds() {
      return 5;
   }

   public static boolean nanoGeoPerfCompareMode() {
      return safeGetBoolean(NANO_GEO_PERF_COMPARE_MODE, false);
   }

   public static boolean defaultNanoGeoPerfCompareMode() {
      return false;
   }

   public static boolean nanoGeoPerfTrackAllocations() {
      return safeGetBoolean(NANO_GEO_PERF_TRACK_ALLOCATIONS, false);
   }

   public static boolean defaultNanoGeoPerfTrackAllocations() {
      return false;
   }

   public static boolean nanoGeoPerfShowOverlay() {
      return safeGetBoolean(NANO_GEO_PERF_SHOW_OVERLAY, false);
   }

   public static boolean defaultNanoGeoPerfShowOverlay() {
      return false;
   }

   public static boolean nanoGeoShowAdvancedDebugOptions() {
      return safeGetBoolean(NANO_GEO_SHOW_ADVANCED_DEBUG_OPTIONS, false);
   }

   public static boolean defaultNanoGeoShowAdvancedDebugOptions() {
      return false;
   }

   public static boolean nanoGeoDebugRenderAxes() {
      return safeGetBoolean(NANO_GEO_DEBUG_RENDER_AXES, false);
   }

   public static boolean defaultNanoGeoDebugRenderAxes() {
      return false;
   }

   public static boolean nanoGeoDebugShowBonePivots() {
      return safeGetBoolean(NANO_GEO_DEBUG_SHOW_BONE_PIVOTS, false);
   }

   public static boolean defaultNanoGeoDebugShowBonePivots() {
      return false;
   }

   public static boolean nanoGeoDebugShowBoneAxes() {
      return safeGetBoolean(NANO_GEO_DEBUG_SHOW_BONE_AXES, false);
   }

   public static boolean defaultNanoGeoDebugShowBoneAxes() {
      return false;
   }

   public static boolean nanoGeoDebugTintLeftRight() {
      return safeGetBoolean(NANO_GEO_DEBUG_TINT_LEFT_RIGHT, false);
   }

   public static boolean defaultNanoGeoDebugTintLeftRight() {
      return false;
   }

   public static boolean nanoGeoDebugHideChildren() {
      return safeGetBoolean(NANO_GEO_DEBUG_HIDE_CHILDREN, false);
   }

   public static boolean defaultNanoGeoDebugHideChildren() {
      return false;
   }

   public static boolean nanoGeoDebugFreezePlayerPose() {
      return safeGetBoolean(NANO_GEO_DEBUG_FREEZE_PLAYER_POSE, false);
   }

   public static boolean defaultNanoGeoDebugFreezePlayerPose() {
      return false;
   }

   public static String nanoGeoDebugBoneName() {
      return safeGetString(NANO_GEO_DEBUG_BONE_NAME, "");
   }

   public static String defaultNanoGeoDebugBoneName() {
      return "";
   }

   public static String nanoGeoDebugRenderOnlyBone() {
      return safeGetString(NANO_GEO_DEBUG_RENDER_ONLY_BONE, "");
   }

   public static String defaultNanoGeoDebugRenderOnlyBone() {
      return "";
   }

   public static int nanoGeoDebugRenderOnlyCubeIndex() {
      return safeGetInt(NANO_GEO_DEBUG_RENDER_ONLY_CUBE_INDEX, -1);
   }

   public static int defaultNanoGeoDebugRenderOnlyCubeIndex() {
      return -1;
   }

   public static String nanoGeoDebugRenderOnlyFace() {
      return safeGetString(NANO_GEO_DEBUG_RENDER_ONLY_FACE, "");
   }

   public static String defaultNanoGeoDebugRenderOnlyFace() {
      return "";
   }

   public static String nanoGeoDebugLogUvForBone() {
      return safeGetString(NANO_GEO_DEBUG_LOG_UV_FOR_BONE, "");
   }

   public static String defaultNanoGeoDebugLogUvForBone() {
      return "";
   }

   public static float nanoGeoDebugForceBoneRotationX() {
      return (float)safeGetDouble(NANO_GEO_DEBUG_FORCE_BONE_ROTATION_X, 0.0);
   }

   public static float defaultNanoGeoDebugForceBoneRotationX() {
      return 0.0F;
   }

   public static float nanoGeoDebugForceBoneRotationY() {
      return (float)safeGetDouble(NANO_GEO_DEBUG_FORCE_BONE_ROTATION_Y, 0.0);
   }

   public static float defaultNanoGeoDebugForceBoneRotationY() {
      return 0.0F;
   }

   public static float nanoGeoDebugForceBoneRotationZ() {
      return (float)safeGetDouble(NANO_GEO_DEBUG_FORCE_BONE_ROTATION_Z, 0.0);
   }

   public static float defaultNanoGeoDebugForceBoneRotationZ() {
      return 0.0F;
   }

   public static NanoGeoClientConfig.NanoGeoDebugRenderType nanoGeoDebugRenderType() {
      try {
         return (NanoGeoClientConfig.NanoGeoDebugRenderType)NANO_GEO_DEBUG_RENDER_TYPE.get();
      } catch (IllegalStateException var1) {
         return DEFAULT_NANO_GEO_DEBUG_RENDER_TYPE;
      }
   }

   public static NanoGeoClientConfig.NanoGeoDebugRenderType defaultNanoGeoDebugRenderType() {
      return DEFAULT_NANO_GEO_DEBUG_RENDER_TYPE;
   }

   public static boolean nanoGeoDebugArmorChestOnlyFullModel() {
      return safeGetBoolean(NANO_GEO_DEBUG_ARMOR_CHEST_ONLY_FULL_MODEL, false);
   }

   public static boolean defaultNanoGeoDebugArmorChestOnlyFullModel() {
      return false;
   }

   public static boolean nanoGeoDebugUseGradientArmorTexture() {
      return safeGetBoolean(NANO_GEO_DEBUG_USE_GRADIENT_ARMOR_TEXTURE, false);
   }

   public static boolean defaultNanoGeoDebugUseGradientArmorTexture() {
      return false;
   }

   public static void setNanoGeoDebugRenderEnabled(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_RENDER_ENABLED, value);
   }

   public static void setNanoGeoLogModelLoad(boolean value) {
      safeSetBoolean(NANO_GEO_LOG_MODEL_LOAD, value);
   }

   public static void setNanoGeoUseExperimentalArmorRenderer(boolean value) {
      safeSetBoolean(NANO_GEO_USE_EXPERIMENTAL_ARMOR_RENDERER, value);
   }

   public static void setNanoGeoExperimentalArmorItemsEnabled(boolean value) {
      safeSetBoolean(NANO_GEO_EXPERIMENTAL_ARMOR_ITEMS_ENABLED, value);
   }

   public static void setNanoGeoExperimentalArmorCountsAsNanosuit(boolean value) {
      safeSetBoolean(NANO_GEO_EXPERIMENTAL_ARMOR_COUNTS_AS_NANOSUIT, value);
   }

   public static void setNanoGeoFpvArmsEnabled(boolean value) {
      safeSetBoolean(NANO_GEO_FPV_ARMS_ENABLED, value);
   }

   public static void setNanoGeoFpvArmsRenderBothHands(boolean value) {
      safeSetBoolean(NANO_GEO_FPV_ARMS_RENDER_BOTH_HANDS, value);
   }

   public static void setNanoGeoFpvArmsDebug(boolean value) {
      safeSetBoolean(NANO_GEO_FPV_ARMS_DEBUG, value);
   }

   public static void setNanoGeoDebugFpvVisibleBones(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_FPV_VISIBLE_BONES, value);
   }

   public static void setArmorModeOverlayEnabled(boolean value) {
      safeSetBoolean(ARMOR_MODE_OVERLAY_ENABLED, value);
   }

   public static void setArmorModeOverlayBackend(NanoGeoClientConfig.ArmorModeOverlayBackend value) {
      setArmorModeOverlayBackend(value, true);
   }

   public static void setArmorModeOverlayBackend(NanoGeoClientConfig.ArmorModeOverlayBackend value, boolean persist) {
      try {
         ARMOR_MODE_OVERLAY_BACKEND.set(value == null ? DEFAULT_ARMOR_MODE_OVERLAY_BACKEND : value);
         if (persist) {
            ARMOR_MODE_OVERLAY_BACKEND.save();
         }
      } catch (RuntimeException var3) {
      }
   }

   public static void setArmorModeOverlayTexture(String value) {
      safeSetString(ARMOR_MODE_OVERLAY_TEXTURE, value, "crysismod:textures/armor/nanosuit_armor_mode.png");
   }

   public static void setArmorModeOverlayAlpha(int value) {
      safeSetInt(ARMOR_MODE_OVERLAY_ALPHA, value);
   }

   public static void setArmorModeOverlayAlpha(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_OVERLAY_ALPHA, value, persist);
   }

   public static void setArmorModeOverlayBrightness(float value) {
      safeSetDouble(ARMOR_MODE_OVERLAY_BRIGHTNESS, value);
   }

   public static void setArmorModeOverlayBrightness(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_OVERLAY_BRIGHTNESS, value, persist);
   }

   public static void setArmorModeDynamicTextureCompatShellEnabled(boolean value) {
      safeSetBoolean(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_SHELL_ENABLED, value);
   }

   public static void setArmorModeDynamicTextureCompatShellEnabled(boolean value, boolean persist) {
      safeSetBoolean(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_SHELL_ENABLED, value, persist);
   }

   public static void setArmorModeDynamicTextureCompatShellScale(float value) {
      safeSetDouble(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_SHELL_SCALE, value);
   }

   public static void setArmorModeDynamicTextureCompatShellScale(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_SHELL_SCALE, value, persist);
   }

   public static void setArmorModeDynamicTextureCompatMaxResolution(int value) {
      safeSetInt(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_MAX_RESOLUTION, value);
   }

   public static void setArmorModeDynamicTextureCompatMaxResolution(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_MAX_RESOLUTION, value, persist);
   }

   public static void setArmorModeDynamicTextureCompatUpdateIntervalMillis(int value) {
      safeSetInt(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_UPDATE_INTERVAL_MILLIS, value);
   }

   public static void setArmorModeDynamicTextureCompatUpdateIntervalMillis(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_UPDATE_INTERVAL_MILLIS, value, persist);
   }

   public static void setArmorModeDynamicTextureCompatGlowStrength(float value) {
      safeSetDouble(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_GLOW_STRENGTH, value);
   }

   public static void setArmorModeDynamicTextureCompatGlowStrength(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_GLOW_STRENGTH, value, persist);
   }

   public static void setArmorModeDynamicTextureCompatUvCoverageMaskEnabled(boolean value) {
      safeSetBoolean(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_UV_COVERAGE_MASK_ENABLED, value);
   }

   public static void setArmorModeDynamicTextureCompatUvCoverageMaskEnabled(boolean value, boolean persist) {
      safeSetBoolean(ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_UV_COVERAGE_MASK_ENABLED, value, persist);
   }

   public static void setArmorModeCompatPerformancePreset(NanoGeoClientConfig.ArmorModeCompatPerformancePreset value) {
      setArmorModeCompatPerformancePreset(value, true);
   }

   public static void setArmorModeCompatPerformancePreset(NanoGeoClientConfig.ArmorModeCompatPerformancePreset value, boolean persist) {
      try {
         ARMOR_MODE_COMPAT_PERFORMANCE_PRESET.set(value == null ? DEFAULT_ARMOR_MODE_COMPAT_PERFORMANCE_PRESET : value);
         if (persist) {
            ARMOR_MODE_COMPAT_PERFORMANCE_PRESET.save();
         }
      } catch (RuntimeException var3) {
      }
   }

   public static void setArmorModeOverlayBaseAlpha(float value) {
      safeSetDouble(ARMOR_MODE_OVERLAY_BASE_ALPHA, value);
   }

   public static void setArmorModeOverlayPulseAmount(float value) {
      safeSetDouble(ARMOR_MODE_OVERLAY_PULSE_AMOUNT, value);
   }

   public static void setArmorModeOverlayPulseSpeed(float value) {
      safeSetDouble(ARMOR_MODE_OVERLAY_PULSE_SPEED, value);
   }

   public static void setArmorModeOverlayActivationPulseEnabled(boolean value) {
      safeSetBoolean(ARMOR_MODE_OVERLAY_ACTIVATION_PULSE_ENABLED, value);
   }

   public static void setArmorModeOverlayActivationDurationTicks(int value) {
      safeSetInt(ARMOR_MODE_OVERLAY_ACTIVATION_DURATION_TICKS, value);
   }

   public static void setArmorModeOverlayActivationStrength(float value) {
      safeSetDouble(ARMOR_MODE_OVERLAY_ACTIVATION_STRENGTH, value);
   }

   public static void setArmorModeOverlayFullbright(boolean value) {
      safeSetBoolean(ARMOR_MODE_OVERLAY_FULLBRIGHT, value);
   }

   public static void setArmorModeOverlayFullbright(boolean value, boolean persist) {
      safeSetBoolean(ARMOR_MODE_OVERLAY_FULLBRIGHT, value, persist);
   }

   public static void setArmorModeOverlayTintR(float value) {
      safeSetDouble(ARMOR_MODE_OVERLAY_TINT_R, value);
   }

   public static void setArmorModeOverlayTintR(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_OVERLAY_TINT_R, value, persist);
   }

   public static void setArmorModeOverlayTintG(float value) {
      safeSetDouble(ARMOR_MODE_OVERLAY_TINT_G, value);
   }

   public static void setArmorModeOverlayTintG(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_OVERLAY_TINT_G, value, persist);
   }

   public static void setArmorModeOverlayTintB(float value) {
      safeSetDouble(ARMOR_MODE_OVERLAY_TINT_B, value);
   }

   public static void setArmorModeOverlayTintB(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_OVERLAY_TINT_B, value, persist);
   }

   public static void setArmorModeOverlayGlintIntensity(float value) {
      safeSetDouble(ARMOR_MODE_OVERLAY_GLINT_INTENSITY, value);
   }

   public static void setArmorModeOverlayWaveEnabled(boolean value) {
      safeSetBoolean(ARMOR_MODE_OVERLAY_WAVE_ENABLED, value);
   }

   public static void setArmorModeOverlayWaveIntensity(float value) {
      safeSetDouble(ARMOR_MODE_OVERLAY_WAVE_INTENSITY, value);
   }

   public static void setArmorModeOverlayWaveWidth(float value) {
      safeSetDouble(ARMOR_MODE_OVERLAY_WAVE_WIDTH, value);
   }

   public static void setArmorModeOverlayWaveSpeed(float value) {
      safeSetDouble(ARMOR_MODE_OVERLAY_WAVE_SPEED, value);
   }

   public static void setArmorModeOverlayWaveFrequency(float value) {
      safeSetDouble(ARMOR_MODE_OVERLAY_WAVE_FREQUENCY, value);
   }

   public static void setArmorModeDiamondShimmerEnabled(boolean value) {
      safeSetBoolean(ARMOR_MODE_DIAMOND_SHIMMER_ENABLED, value);
   }

   public static void setArmorModeDiamondShimmerSpeedPixelsPerSecond(float value) {
      safeSetDouble(ARMOR_MODE_DIAMOND_SHIMMER_SPEED_PIXELS_PER_SECOND, value);
   }

   public static void setArmorModeDiamondWidthPx(int value) {
      safeSetInt(ARMOR_MODE_DIAMOND_WIDTH_PX, value);
   }

   public static void setArmorModeDiamondHeightPx(int value) {
      safeSetInt(ARMOR_MODE_DIAMOND_HEIGHT_PX, value);
   }

   public static void setArmorModeDiamondAlpha(int value) {
      safeSetInt(ARMOR_MODE_DIAMOND_ALPHA, value);
   }

   public static void setArmorModeDiamondShimmerDirection(NanoGeoClientConfig.ArmorModeDiamondShimmerDirection value) {
      try {
         ARMOR_MODE_DIAMOND_SHIMMER_DIRECTION.set(value == null ? DEFAULT_ARMOR_MODE_DIAMOND_SHIMMER_DIRECTION : value);
         ARMOR_MODE_DIAMOND_SHIMMER_DIRECTION.save();
      } catch (RuntimeException var2) {
      }
   }

   public static void setArmorModeOverlayPatternMode(NanoGeoClientConfig.ArmorModeOverlayPatternMode value) {
      try {
         ARMOR_MODE_OVERLAY_PATTERN_MODE.set(NanoGeoClientConfig.ArmorModeOverlayPatternMode.CHAOTIC_PATCH_FLOW);
         ARMOR_MODE_OVERLAY_PATTERN_MODE.save();
      } catch (RuntimeException var2) {
      }
   }

   public static void setArmorModeOverlayPatternMode(NanoGeoClientConfig.ArmorModeOverlayPatternMode value, boolean persist) {
      try {
         ARMOR_MODE_OVERLAY_PATTERN_MODE.set(NanoGeoClientConfig.ArmorModeOverlayPatternMode.CHAOTIC_PATCH_FLOW);
         if (persist) {
            ARMOR_MODE_OVERLAY_PATTERN_MODE.save();
         }
      } catch (RuntimeException var3) {
      }
   }

   public static void setArmorModeXShimmerEnabled(boolean value) {
      safeSetBoolean(ARMOR_MODE_X_SHIMMER_ENABLED, value);
   }

   public static void setArmorModeXShimmerAlpha(int value) {
      safeSetInt(ARMOR_MODE_X_SHIMMER_ALPHA, value);
   }

   public static void setArmorModeXShimmerAlpha(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_X_SHIMMER_ALPHA, value, persist);
   }

   public static void setArmorModeXShimmerSizePx(int value) {
      safeSetInt(ARMOR_MODE_X_SHIMMER_SIZE_PX, value);
   }

   public static void setArmorModeXShimmerSizePx(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_X_SHIMMER_SIZE_PX, value, persist);
   }

   public static void setArmorModeXShimmerSpacingPx(int value) {
      safeSetInt(ARMOR_MODE_X_SHIMMER_SPACING_PX, value);
   }

   public static void setArmorModeXShimmerSpacingPx(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_X_SHIMMER_SPACING_PX, value, persist);
   }

   public static void setArmorModeXShimmerLineWidthPx(float value) {
      safeSetDouble(ARMOR_MODE_X_SHIMMER_LINE_WIDTH_PX, value);
   }

   public static void setArmorModeXShimmerLineWidthPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_X_SHIMMER_LINE_WIDTH_PX, value, persist);
   }

   public static void setArmorModeXShimmerEdgeSoftnessPx(float value) {
      safeSetDouble(ARMOR_MODE_X_SHIMMER_EDGE_SOFTNESS_PX, value);
   }

   public static void setArmorModeXShimmerEdgeSoftnessPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_X_SHIMMER_EDGE_SOFTNESS_PX, value, persist);
   }

   public static void setArmorModeXShimmerSpeedPxPerSecond(float value) {
      safeSetDouble(ARMOR_MODE_X_SHIMMER_SPEED_PX_PER_SECOND, value);
   }

   public static void setArmorModeXShimmerSpeedPxPerSecond(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_X_SHIMMER_SPEED_PX_PER_SECOND, value, persist);
   }

   public static void setArmorModeXShimmerDirectionX(float value) {
      safeSetDouble(ARMOR_MODE_X_SHIMMER_DIRECTION_X, value);
   }

   public static void setArmorModeXShimmerDirectionX(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_X_SHIMMER_DIRECTION_X, value, persist);
   }

   public static void setArmorModeXShimmerDirectionY(float value) {
      safeSetDouble(ARMOR_MODE_X_SHIMMER_DIRECTION_Y, value);
   }

   public static void setArmorModeXShimmerDirectionY(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_X_SHIMMER_DIRECTION_Y, value, persist);
   }

   public static void setArmorModeXShimmerAsCutout(boolean value) {
      safeSetBoolean(ARMOR_MODE_X_SHIMMER_AS_CUTOUT, value);
   }

   public static void setArmorModeXShimmerAsCutout(boolean value, boolean persist) {
      safeSetBoolean(ARMOR_MODE_X_SHIMMER_AS_CUTOUT, value, persist);
   }

   public static void setDebugArmorModeXShimmer(boolean value) {
      safeSetBoolean(DEBUG_ARMOR_MODE_X_SHIMMER, value);
   }

   public static void setDebugArmorModeXShimmer(boolean value, boolean persist) {
      safeSetBoolean(DEBUG_ARMOR_MODE_X_SHIMMER, value, persist);
   }

   public static void setArmorModeHexCellSizePx(float value) {
      safeSetDouble(ARMOR_MODE_HEX_CELL_SIZE_PX, value);
   }

   public static void setArmorModeHexCellSizePx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_HEX_CELL_SIZE_PX, value, persist);
   }

   public static void setArmorModeHexLineWidthPx(float value) {
      safeSetDouble(ARMOR_MODE_HEX_LINE_WIDTH_PX, value);
   }

   public static void setArmorModeHexLineWidthPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_HEX_LINE_WIDTH_PX, value, persist);
   }

   public static void setArmorModeHexEdgeSoftnessPx(float value) {
      safeSetDouble(ARMOR_MODE_HEX_EDGE_SOFTNESS_PX, value);
   }

   public static void setArmorModeHexEdgeSoftnessPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_HEX_EDGE_SOFTNESS_PX, value, persist);
   }

   public static void setArmorModeHexInnerAlpha(int value) {
      safeSetInt(ARMOR_MODE_HEX_INNER_ALPHA, value);
   }

   public static void setArmorModeHexInnerAlpha(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_HEX_INNER_ALPHA, value, persist);
   }

   public static void setArmorModeHexGlowAlpha(int value) {
      safeSetInt(ARMOR_MODE_HEX_GLOW_ALPHA, value);
   }

   public static void setArmorModeHexGlowAlpha(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_HEX_GLOW_ALPHA, value, persist);
   }

   public static void setArmorModeHexFrequency(float value) {
      safeSetDouble(ARMOR_MODE_HEX_FREQUENCY, value);
   }

   public static void setArmorModeHexFrequency(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_HEX_FREQUENCY, value, persist);
   }

   public static void setArmorModeHexScrollSpeedPxPerSecond(float value) {
      safeSetDouble(ARMOR_MODE_HEX_SCROLL_SPEED_PX_PER_SECOND, value);
   }

   public static void setArmorModeHexScrollSpeedPxPerSecond(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_HEX_SCROLL_SPEED_PX_PER_SECOND, value, persist);
   }

   public static void setArmorModeHexDirectionX(float value) {
      safeSetDouble(ARMOR_MODE_HEX_DIRECTION_X, value);
   }

   public static void setArmorModeHexDirectionX(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_HEX_DIRECTION_X, value, persist);
   }

   public static void setArmorModeHexDirectionY(float value) {
      safeSetDouble(ARMOR_MODE_HEX_DIRECTION_Y, value);
   }

   public static void setArmorModeHexDirectionY(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_HEX_DIRECTION_Y, value, persist);
   }

   public static void setArmorModeHexTextureSizePx(float value) {
      safeSetDouble(ARMOR_MODE_HEX_TEXTURE_SIZE_PX, value);
   }

   public static void setArmorModeHexTextureSizePx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_HEX_TEXTURE_SIZE_PX, value, persist);
   }

   public static void setArmorModeHexTexturePreset(NanoGeoClientConfig.ArmorModeHexTexturePreset preset) {
      setArmorModeHexTextureSizePx((preset == null ? defaultArmorModeHexTexturePreset() : preset).textureSizePx());
   }

   public static void setArmorModeHexTexturePreset(NanoGeoClientConfig.ArmorModeHexTexturePreset preset, boolean persist) {
      setArmorModeHexTextureSizePx((preset == null ? defaultArmorModeHexTexturePreset() : preset).textureSizePx(), persist);
   }

   public static void setArmorModeHexPatternScale(float value) {
      safeSetDouble(ARMOR_MODE_HEX_PATTERN_SCALE, value);
   }

   public static void setArmorModeHexPatternScale(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_HEX_PATTERN_SCALE, value, persist);
   }

   public static void setArmorModeHexGlowBoost(float value) {
      safeSetDouble(ARMOR_MODE_HEX_GLOW_BOOST, value);
   }

   public static void setArmorModeHexGlowBoost(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_HEX_GLOW_BOOST, value, persist);
   }

   public static void setArmorModeSheenBreathingAmount(float value) {
      safeSetDouble(ARMOR_MODE_SHEEN_BREATHING_AMOUNT, value);
   }

   public static void setArmorModeSheenBreathingAmount(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_SHEEN_BREATHING_AMOUNT, value, persist);
   }

   public static void setArmorModeSheenBreathingSpeed(float value) {
      safeSetDouble(ARMOR_MODE_SHEEN_BREATHING_SPEED, value);
   }

   public static void setArmorModeSheenBreathingSpeed(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_SHEEN_BREATHING_SPEED, value, persist);
   }

   public static void setArmorModeSheenAlpha(float value) {
      safeSetDouble(ARMOR_MODE_SHEEN_ALPHA, value);
   }

   public static void setArmorModeSheenAlpha(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_SHEEN_ALPHA, value, persist);
   }

   public static void setArmorModeSheenTransparency(float value) {
      safeSetDouble(ARMOR_MODE_SHEEN_TRANSPARENCY, value);
   }

   public static void setArmorModeSheenTransparency(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_SHEEN_TRANSPARENCY, value, persist);
   }

   public static void setArmorModeSheenWidthPx(float value) {
      safeSetDouble(ARMOR_MODE_SHEEN_WIDTH_PX, value);
   }

   public static void setArmorModeSheenWidthPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_SHEEN_WIDTH_PX, value, persist);
   }

   public static void setArmorModeSheenSoftnessPx(float value) {
      safeSetDouble(ARMOR_MODE_SHEEN_SOFTNESS_PX, value);
   }

   public static void setArmorModeSheenSoftnessPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_SHEEN_SOFTNESS_PX, value, persist);
   }

   public static void setArmorModeSheenRepeatPx(float value) {
      safeSetDouble(ARMOR_MODE_SHEEN_REPEAT_PX, value);
   }

   public static void setArmorModeSheenRepeatPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_SHEEN_REPEAT_PX, value, persist);
   }

   public static void setArmorModeSheenSpeedPxPerSecond(float value) {
      safeSetDouble(ARMOR_MODE_SHEEN_SPEED_PX_PER_SECOND, value);
   }

   public static void setArmorModeSheenSpeedPxPerSecond(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_SHEEN_SPEED_PX_PER_SECOND, value, persist);
   }

   public static void setArmorModeSheenDirectionX(float value) {
      safeSetDouble(ARMOR_MODE_SHEEN_DIRECTION_X, value);
   }

   public static void setArmorModeSheenDirectionX(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_SHEEN_DIRECTION_X, value, persist);
   }

   public static void setArmorModeSheenDirectionY(float value) {
      safeSetDouble(ARMOR_MODE_SHEEN_DIRECTION_Y, value);
   }

   public static void setArmorModeSheenDirectionY(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_SHEEN_DIRECTION_Y, value, persist);
   }

   public static void setArmorModeSheenColorBoost(float value) {
      safeSetDouble(ARMOR_MODE_SHEEN_COLOR_BOOST, value);
   }

   public static void setArmorModeSheenColorBoost(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_SHEEN_COLOR_BOOST, value, persist);
   }

   public static void setArmorModeFiberFlowEnabled(boolean value) {
      safeSetBoolean(ARMOR_MODE_FIBER_FLOW_ENABLED, value);
   }

   public static void setArmorModeFiberFlowEnabled(boolean value, boolean persist) {
      safeSetBoolean(ARMOR_MODE_FIBER_FLOW_ENABLED, value, persist);
   }

   public static void setArmorModeFiberFlowStrength(float value) {
      safeSetDouble(ARMOR_MODE_FIBER_FLOW_STRENGTH, value);
   }

   public static void setArmorModeFiberFlowStrength(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_FIBER_FLOW_STRENGTH, value, persist);
   }

   public static void setArmorModeFiberFlowScale(float value) {
      safeSetDouble(ARMOR_MODE_FIBER_FLOW_SCALE, value);
   }

   public static void setArmorModeFiberFlowScale(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_FIBER_FLOW_SCALE, value, persist);
   }

   public static void setArmorModeFiberFlowSpeed(float value) {
      safeSetDouble(ARMOR_MODE_FIBER_FLOW_SPEED, value);
   }

   public static void setArmorModeFiberFlowSpeed(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_FIBER_FLOW_SPEED, value, persist);
   }

   public static void setDebugArmorModeUniversalSheen(boolean value) {
      safeSetBoolean(DEBUG_ARMOR_MODE_UNIVERSAL_SHEEN, value);
   }

   public static void setDebugArmorModeUniversalSheen(boolean value, boolean persist) {
      safeSetBoolean(DEBUG_ARMOR_MODE_UNIVERSAL_SHEEN, value, persist);
   }

   public static void setArmorModePlasmaBlobInnerAlpha(int value) {
      safeSetInt(ARMOR_MODE_PLASMA_BLOB_INNER_ALPHA, value);
   }

   public static void setArmorModePlasmaBlobInnerAlpha(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_PLASMA_BLOB_INNER_ALPHA, value, persist);
   }

   public static void setArmorModePlasmaBlobCount(int value) {
      safeSetInt(ARMOR_MODE_PLASMA_BLOB_COUNT, value);
   }

   public static void setArmorModePlasmaBlobCount(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_PLASMA_BLOB_COUNT, value, persist);
   }

   public static void setArmorModePlasmaBlobMinSizePx(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_MIN_SIZE_PX, value);
   }

   public static void setArmorModePlasmaBlobMinSizePx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_MIN_SIZE_PX, value, persist);
   }

   public static void setArmorModePlasmaBlobMaxSizePx(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_MAX_SIZE_PX, value);
   }

   public static void setArmorModePlasmaBlobMaxSizePx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_MAX_SIZE_PX, value, persist);
   }

   public static void setArmorModePlasmaBlobEdgeSoftnessPx(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_EDGE_SOFTNESS_PX, value);
   }

   public static void setArmorModePlasmaBlobEdgeSoftnessPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_EDGE_SOFTNESS_PX, value, persist);
   }

   public static void setArmorModePlasmaBlobRimAlpha(int value) {
      safeSetInt(ARMOR_MODE_PLASMA_BLOB_RIM_ALPHA, value);
   }

   public static void setArmorModePlasmaBlobRimAlpha(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_PLASMA_BLOB_RIM_ALPHA, value, persist);
   }

   public static void setArmorModePlasmaBlobRimWidthPx(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_RIM_WIDTH_PX, value);
   }

   public static void setArmorModePlasmaBlobRimWidthPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_RIM_WIDTH_PX, value, persist);
   }

   public static void setArmorModePlasmaBlobRimBrightness(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_RIM_BRIGHTNESS, value);
   }

   public static void setArmorModePlasmaBlobRimBrightness(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_RIM_BRIGHTNESS, value, persist);
   }

   public static void setArmorModePlasmaBlobDriftSpeedPxPerSecond(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_DRIFT_SPEED_PX_PER_SECOND, value);
   }

   public static void setArmorModePlasmaBlobDriftSpeedPxPerSecond(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_DRIFT_SPEED_PX_PER_SECOND, value, persist);
   }

   public static void setArmorModePlasmaBlobDriftRangeScale(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_DRIFT_RANGE_SCALE, value);
   }

   public static void setArmorModePlasmaBlobDriftRangeScale(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_DRIFT_RANGE_SCALE, value, persist);
   }

   public static void setArmorModePlasmaBlobShapeDistortion(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_SHAPE_DISTORTION, value);
   }

   public static void setArmorModePlasmaBlobShapeDistortion(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_BLOB_SHAPE_DISTORTION, value, persist);
   }

   public static void setArmorModePatchTextureAlpha(int value) {
      safeSetInt(ARMOR_MODE_PATCH_TEXTURE_ALPHA, value);
   }

   public static void setArmorModePatchTextureAlpha(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_PATCH_TEXTURE_ALPHA, value, persist);
   }

   public static void setArmorModePatchSpotAlpha(int value) {
      safeSetInt(ARMOR_MODE_PATCH_SPOT_ALPHA, value);
   }

   public static void setArmorModePatchSpotAlpha(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_PATCH_SPOT_ALPHA, value, persist);
   }

   public static void setArmorModePatchMinSizePx(float value) {
      safeSetDouble(ARMOR_MODE_PATCH_MIN_SIZE_PX, value);
   }

   public static void setArmorModePatchMinSizePx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PATCH_MIN_SIZE_PX, value, persist);
   }

   public static void setArmorModePatchMaxSizePx(float value) {
      safeSetDouble(ARMOR_MODE_PATCH_MAX_SIZE_PX, value);
   }

   public static void setArmorModePatchMaxSizePx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PATCH_MAX_SIZE_PX, value, persist);
   }

   public static void setArmorModePatchFrequency(float value) {
      safeSetDouble(ARMOR_MODE_PATCH_FREQUENCY, value);
   }

   public static void setArmorModePatchFrequency(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PATCH_FREQUENCY, value, persist);
   }

   public static void setArmorModePatchDensity(float value) {
      safeSetDouble(ARMOR_MODE_PATCH_DENSITY, value);
   }

   public static void setArmorModePatchDensity(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PATCH_DENSITY, value, persist);
   }

   public static void setArmorModePatchFlowVariant(NanoGeoClientConfig.ArmorModePatchFlowVariant value) {
      try {
         ARMOR_MODE_PATCH_FLOW_VARIANT.set(value == null ? DEFAULT_ARMOR_MODE_PATCH_FLOW_VARIANT : value);
         ARMOR_MODE_PATCH_FLOW_VARIANT.save();
      } catch (RuntimeException var2) {
      }
   }

   public static void setArmorModePatchFlowVariant(NanoGeoClientConfig.ArmorModePatchFlowVariant value, boolean persist) {
      try {
         ARMOR_MODE_PATCH_FLOW_VARIANT.set(value == null ? DEFAULT_ARMOR_MODE_PATCH_FLOW_VARIANT : value);
         if (persist) {
            ARMOR_MODE_PATCH_FLOW_VARIANT.save();
         }
      } catch (RuntimeException var3) {
      }
   }

   public static void setArmorModePatchCloudEdgeSoftness(float value) {
      safeSetDouble(ARMOR_MODE_PATCH_CLOUD_EDGE_SOFTNESS, value);
   }

   public static void setArmorModePatchCloudEdgeSoftness(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PATCH_CLOUD_EDGE_SOFTNESS, value, persist);
   }

   public static void setArmorModePlasmaPulseEnabled(boolean value) {
      safeSetBoolean(ARMOR_MODE_PLASMA_PULSE_ENABLED, value);
   }

   public static void setArmorModePlasmaPulseEnabled(boolean value, boolean persist) {
      safeSetBoolean(ARMOR_MODE_PLASMA_PULSE_ENABLED, value, persist);
   }

   public static void setArmorModePlasmaPulseCount(int value) {
      safeSetInt(ARMOR_MODE_PLASMA_PULSE_COUNT, value);
   }

   public static void setArmorModePlasmaPulseCount(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_PLASMA_PULSE_COUNT, value, persist);
   }

   public static void setArmorModePlasmaPulseIntervalSeconds(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_INTERVAL_SECONDS, value);
   }

   public static void setArmorModePlasmaPulseIntervalSeconds(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_INTERVAL_SECONDS, value, persist);
   }

   public static void setArmorModePlasmaPulseDurationSeconds(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_DURATION_SECONDS, value);
   }

   public static void setArmorModePlasmaPulseDurationSeconds(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_DURATION_SECONDS, value, persist);
   }

   public static void setArmorModePlasmaPulseAlpha(int value) {
      safeSetInt(ARMOR_MODE_PLASMA_PULSE_ALPHA, value);
   }

   public static void setArmorModePlasmaPulseAlpha(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_PLASMA_PULSE_ALPHA, value, persist);
   }

   public static void setArmorModePlasmaPulseStartRadiusPx(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_START_RADIUS_PX, value);
   }

   public static void setArmorModePlasmaPulseStartRadiusPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_START_RADIUS_PX, value, persist);
   }

   public static void setArmorModePlasmaPulseEndRadiusPx(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_END_RADIUS_PX, value);
   }

   public static void setArmorModePlasmaPulseEndRadiusPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_END_RADIUS_PX, value, persist);
   }

   public static void setArmorModePlasmaPulseSpreadSpeedPxPerSecond(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_SPREAD_SPEED_PX_PER_SECOND, value);
   }

   public static void setArmorModePlasmaPulseSpreadSpeedPxPerSecond(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_SPREAD_SPEED_PX_PER_SECOND, value, persist);
   }

   public static void setArmorModePlasmaPulseLineWidthPx(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_LINE_WIDTH_PX, value);
   }

   public static void setArmorModePlasmaPulseLineWidthPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_LINE_WIDTH_PX, value, persist);
   }

   public static void setArmorModePlasmaPulseEdgeSoftnessPx(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_EDGE_SOFTNESS_PX, value);
   }

   public static void setArmorModePlasmaPulseEdgeSoftnessPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_EDGE_SOFTNESS_PX, value, persist);
   }

   public static void setArmorModePlasmaPulseBrightness(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_BRIGHTNESS, value);
   }

   public static void setArmorModePlasmaPulseBrightness(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_BRIGHTNESS, value, persist);
   }

   public static void setArmorModePlasmaPulseArcCoverage(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_ARC_COVERAGE, value);
   }

   public static void setArmorModePlasmaPulseArcCoverage(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_ARC_COVERAGE, value, persist);
   }

   public static void setArmorModePlasmaPulseArcDistortion(float value) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_ARC_DISTORTION, value);
   }

   public static void setArmorModePlasmaPulseArcDistortion(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PLASMA_PULSE_ARC_DISTORTION, value, persist);
   }

   public static void setArmorModePulseEnabled(boolean value) {
      safeSetBoolean(ARMOR_MODE_PULSE_ENABLED, value);
   }

   public static void setArmorModePulseEnabled(boolean value, boolean persist) {
      safeSetBoolean(ARMOR_MODE_PULSE_ENABLED, value, persist);
   }

   public static void setArmorModePulseCenterSource(NanoGeoClientConfig.ArmorModePulseCenterSource value) {
      setArmorModePulseCenterSource(value, true);
   }

   public static void setArmorModePulseCenterSource(NanoGeoClientConfig.ArmorModePulseCenterSource value, boolean persist) {
      try {
         ARMOR_MODE_PULSE_CENTER_SOURCE.set(value == null ? DEFAULT_ARMOR_MODE_PULSE_CENTER_SOURCE : value);
         if (persist) {
            ARMOR_MODE_PULSE_CENTER_SOURCE.save();
         }
      } catch (RuntimeException var3) {
      }
   }

   public static void setArmorModePulseVisualMode(NanoGeoClientConfig.ArmorModePulseVisualMode value) {
      setArmorModePulseVisualMode(value, true);
   }

   public static void setArmorModePulseVisualMode(NanoGeoClientConfig.ArmorModePulseVisualMode value, boolean persist) {
      try {
         ARMOR_MODE_PULSE_VISUAL_MODE.set(value == null ? DEFAULT_ARMOR_MODE_PULSE_VISUAL_MODE : value);
         if (persist) {
            ARMOR_MODE_PULSE_VISUAL_MODE.save();
         }
      } catch (RuntimeException var3) {
      }
   }

   public static void setArmorModeCompatPulsePreset(NanoGeoClientConfig.ArmorModeCompatPulsePreset value) {
      setArmorModeCompatPulsePreset(value, true);
   }

   public static void setArmorModeCompatPulsePreset(NanoGeoClientConfig.ArmorModeCompatPulsePreset value, boolean persist) {
      try {
         ARMOR_MODE_COMPAT_PULSE_PRESET.set(value == null ? DEFAULT_ARMOR_MODE_COMPAT_PULSE_PRESET : value);
         if (persist) {
            ARMOR_MODE_COMPAT_PULSE_PRESET.save();
         }
      } catch (RuntimeException var3) {
      }
   }

   public static void setArmorModePulseMaxCenters(int value) {
      safeSetInt(ARMOR_MODE_PULSE_MAX_CENTERS, value);
   }

   public static void setArmorModePulseMaxCenters(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_PULSE_MAX_CENTERS, value, persist);
   }

   public static void setArmorModePulseCenterMarkerAlpha(int value) {
      safeSetInt(ARMOR_MODE_PULSE_CENTER_MARKER_ALPHA, value);
   }

   public static void setArmorModePulseCenterMarkerAlpha(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_PULSE_CENTER_MARKER_ALPHA, value, persist);
   }

   public static void setArmorModePulseIntervalSeconds(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_INTERVAL_SECONDS, value);
   }

   public static void setArmorModePulseIntervalSeconds(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_INTERVAL_SECONDS, value, persist);
   }

   public static void setArmorModePulseDurationSeconds(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_DURATION_SECONDS, value);
   }

   public static void setArmorModePulseDurationSeconds(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_DURATION_SECONDS, value, persist);
   }

   public static void setArmorModePulseStartRadiusPx(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_START_RADIUS_PX, value);
   }

   public static void setArmorModePulseStartRadiusPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_START_RADIUS_PX, value, persist);
   }

   public static void setArmorModePulseEndRadiusPx(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_END_RADIUS_PX, value);
   }

   public static void setArmorModePulseEndRadiusPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_END_RADIUS_PX, value, persist);
   }

   public static void setArmorModePulseCoreWidthPx(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_CORE_WIDTH_PX, value);
   }

   public static void setArmorModePulseCoreWidthPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_CORE_WIDTH_PX, value, persist);
   }

   public static void setArmorModePulseGlowRadiusPx(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_GLOW_RADIUS_PX, value);
   }

   public static void setArmorModePulseGlowRadiusPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_GLOW_RADIUS_PX, value, persist);
   }

   public static void setArmorModePulseCoreSoftnessPx(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_CORE_SOFTNESS_PX, value);
   }

   public static void setArmorModePulseCoreSoftnessPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_CORE_SOFTNESS_PX, value, persist);
   }

   public static void setArmorModePulseGlowSoftnessPx(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_GLOW_SOFTNESS_PX, value);
   }

   public static void setArmorModePulseGlowSoftnessPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_GLOW_SOFTNESS_PX, value, persist);
   }

   public static void setArmorModePulseCoreAlpha(int value) {
      safeSetInt(ARMOR_MODE_PULSE_CORE_ALPHA, value);
   }

   public static void setArmorModePulseCoreAlpha(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_PULSE_CORE_ALPHA, value, persist);
   }

   public static void setArmorModePulseGlowAlpha(int value) {
      safeSetInt(ARMOR_MODE_PULSE_GLOW_ALPHA, value);
   }

   public static void setArmorModePulseGlowAlpha(int value, boolean persist) {
      safeSetInt(ARMOR_MODE_PULSE_GLOW_ALPHA, value, persist);
   }

   public static void setArmorModePulseCoreColor(String value) {
      safeSetString(ARMOR_MODE_PULSE_CORE_COLOR, normalizeRgbHexColor(value, "#7EC6FC"), "#7EC6FC");
   }

   public static void setArmorModePulseCoreColor(String value, boolean persist) {
      safeSetString(ARMOR_MODE_PULSE_CORE_COLOR, normalizeRgbHexColor(value, "#7EC6FC"), "#7EC6FC", persist);
   }

   public static void setArmorModePulseGlowColor(String value) {
      safeSetString(ARMOR_MODE_PULSE_GLOW_COLOR, normalizeRgbHexColor(value, "#ADD1ED"), "#ADD1ED");
   }

   public static void setArmorModePulseGlowColor(String value, boolean persist) {
      safeSetString(ARMOR_MODE_PULSE_GLOW_COLOR, normalizeRgbHexColor(value, "#ADD1ED"), "#ADD1ED", persist);
   }

   public static void setArmorModePulseBrightness(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_BRIGHTNESS, value);
   }

   public static void setArmorModePulseBrightness(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_BRIGHTNESS, value, persist);
   }

   public static void setArmorModePulseFadePower(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_FADE_POWER, value);
   }

   public static void setArmorModePulseFadePower(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_FADE_POWER, value, persist);
   }

   public static void setArmorModePulseRingDistortionPx(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_RING_DISTORTION_PX, value);
   }

   public static void setArmorModePulseRingDistortionPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_RING_DISTORTION_PX, value, persist);
   }

   public static void setArmorModePulseRingDistortionFrequency(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_RING_DISTORTION_FREQUENCY, value);
   }

   public static void setArmorModePulseRingDistortionFrequency(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_RING_DISTORTION_FREQUENCY, value, persist);
   }

   public static void setArmorModePulseRingEnergyVariation(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_RING_ENERGY_VARIATION, value);
   }

   public static void setArmorModePulseRingEnergyVariation(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_RING_ENERGY_VARIATION, value, persist);
   }

   public static void setArmorModePulseWidthVariation(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_WIDTH_VARIATION, value);
   }

   public static void setArmorModePulseWidthVariation(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_WIDTH_VARIATION, value, persist);
   }

   public static void setArmorModePulseBrightnessVariation(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_BRIGHTNESS_VARIATION, value);
   }

   public static void setArmorModePulseBrightnessVariation(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_BRIGHTNESS_VARIATION, value, persist);
   }

   public static void setArmorModePulseEdgeRoughness(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_EDGE_ROUGHNESS, value);
   }

   public static void setArmorModePulseEdgeRoughness(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_EDGE_ROUGHNESS, value, persist);
   }

   public static void setArmorModePulsePatternRotationRandomness(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_PATTERN_ROTATION_RANDOMNESS, value);
   }

   public static void setArmorModePulsePatternRotationRandomness(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_PATTERN_ROTATION_RANDOMNESS, value, persist);
   }

   public static void setArmorModePulseMergingEnabled(boolean value) {
      safeSetBoolean(ARMOR_MODE_PULSE_MERGING_ENABLED, value);
   }

   public static void setArmorModePulseMergingEnabled(boolean value, boolean persist) {
      safeSetBoolean(ARMOR_MODE_PULSE_MERGING_ENABLED, value, persist);
   }

   public static void setArmorModePulseMergeStrength(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_MERGE_STRENGTH, value);
   }

   public static void setArmorModePulseMergeStrength(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_MERGE_STRENGTH, value, persist);
   }

   public static void setArmorModePulseMergeSoftnessPx(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_MERGE_SOFTNESS_PX, value);
   }

   public static void setArmorModePulseMergeSoftnessPx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_MERGE_SOFTNESS_PX, value, persist);
   }

   public static void setArmorModePulseMergeBrightnessBoost(float value) {
      safeSetDouble(ARMOR_MODE_PULSE_MERGE_BRIGHTNESS_BOOST, value);
   }

   public static void setArmorModePulseMergeBrightnessBoost(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_PULSE_MERGE_BRIGHTNESS_BOOST, value, persist);
   }

   public static void setArmorModePulseDebugForceVisible(boolean value) {
      safeSetBoolean(ARMOR_MODE_PULSE_DEBUG_FORCE_VISIBLE, value);
   }

   public static void setArmorModePulseDebugForceVisible(boolean value, boolean persist) {
      safeSetBoolean(ARMOR_MODE_PULSE_DEBUG_FORCE_VISIBLE, value, persist);
   }

   public static void setArmorModePulseDebugOutputOnlyPulse(boolean value) {
      safeSetBoolean(ARMOR_MODE_PULSE_DEBUG_OUTPUT_ONLY_PULSE, value);
   }

   public static void setArmorModePulseDebugOutputOnlyPulse(boolean value, boolean persist) {
      safeSetBoolean(ARMOR_MODE_PULSE_DEBUG_OUTPUT_ONLY_PULSE, value, persist);
   }

   public static void setDebugArmorModePlasmaField(boolean value) {
      safeSetBoolean(DEBUG_ARMOR_MODE_PLASMA_FIELD, value);
   }

   public static void setDebugArmorModePlasmaField(boolean value, boolean persist) {
      safeSetBoolean(DEBUG_ARMOR_MODE_PLASMA_FIELD, value, persist);
   }

   public static void setDebugArmorModeHexPattern(boolean value) {
      safeSetBoolean(DEBUG_ARMOR_MODE_HEX_PATTERN, value);
   }

   public static void setDebugArmorModeHexPattern(boolean value, boolean persist) {
      safeSetBoolean(DEBUG_ARMOR_MODE_HEX_PATTERN, value, persist);
   }

   public static void setDebugArmorModeSettingsScreen(boolean value) {
      safeSetBoolean(DEBUG_ARMOR_MODE_SETTINGS_SCREEN, value);
   }

   public static void setDebugArmorModeSettingsScreen(boolean value, boolean persist) {
      safeSetBoolean(DEBUG_ARMOR_MODE_SETTINGS_SCREEN, value, persist);
   }

   public static void setArmorModeOverlayRenderInFirstPerson(boolean value) {
      safeSetBoolean(ARMOR_MODE_OVERLAY_RENDER_IN_FIRST_PERSON, value);
   }

   public static void setArmorModeOverlayRenderInFirstPerson(boolean value, boolean persist) {
      safeSetBoolean(ARMOR_MODE_OVERLAY_RENDER_IN_FIRST_PERSON, value, persist);
   }

   public static void setArmorModeOverlayRenderInThirdPerson(boolean value) {
      safeSetBoolean(ARMOR_MODE_OVERLAY_RENDER_IN_THIRD_PERSON, value);
   }

   public static void setArmorModeOverlayRenderInThirdPerson(boolean value, boolean persist) {
      safeSetBoolean(ARMOR_MODE_OVERLAY_RENDER_IN_THIRD_PERSON, value, persist);
   }

   public static void setArmorModeTransitionEnabled(boolean value) {
      safeSetBoolean(ARMOR_MODE_TRANSITION_ENABLED, value);
   }

   public static void setArmorModeTransitionEnabled(boolean value, boolean persist) {
      safeSetBoolean(ARMOR_MODE_TRANSITION_ENABLED, value, persist);
   }

   public static void setArmorModeTransitionDurationSeconds(float value) {
      safeSetDouble(ARMOR_MODE_TRANSITION_DURATION_SECONDS, value);
   }

   public static void setArmorModeTransitionDurationSeconds(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_TRANSITION_DURATION_SECONDS, value, persist);
   }

   public static void setArmorModeTransitionPixelBlockSizePx(float value) {
      safeSetDouble(ARMOR_MODE_TRANSITION_PIXEL_BLOCK_SIZE_PX, value);
   }

   public static void setArmorModeTransitionPixelBlockSizePx(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_TRANSITION_PIXEL_BLOCK_SIZE_PX, value, persist);
   }

   public static void setArmorModeTransitionStaggerAmount(float value) {
      safeSetDouble(ARMOR_MODE_TRANSITION_STAGGER_AMOUNT, value);
   }

   public static void setArmorModeTransitionStaggerAmount(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_TRANSITION_STAGGER_AMOUNT, value, persist);
   }

   public static void setArmorModeTransitionFadeWindow(float value) {
      safeSetDouble(ARMOR_MODE_TRANSITION_FADE_WINDOW, value);
   }

   public static void setArmorModeTransitionFadeWindow(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_TRANSITION_FADE_WINDOW, value, persist);
   }

   public static void setArmorModeTransitionSeed(float value) {
      safeSetDouble(ARMOR_MODE_TRANSITION_SEED, value);
   }

   public static void setArmorModeTransitionSeed(float value, boolean persist) {
      safeSetDouble(ARMOR_MODE_TRANSITION_SEED, value, persist);
   }

   public static void setArmorModeTransitionAffectsPulses(boolean value) {
      safeSetBoolean(ARMOR_MODE_TRANSITION_AFFECTS_PULSES, value);
   }

   public static void setArmorModeTransitionAffectsPulses(boolean value, boolean persist) {
      safeSetBoolean(ARMOR_MODE_TRANSITION_AFFECTS_PULSES, value, persist);
   }

   public static void saveArmorModeOverlayEditorConfig() {
      try {
         ARMOR_MODE_OVERLAY_ALPHA.save();
         ARMOR_MODE_OVERLAY_BRIGHTNESS.save();
         ARMOR_MODE_OVERLAY_BACKEND.save();
         ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_SHELL_ENABLED.save();
         ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_SHELL_SCALE.save();
         ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_MAX_RESOLUTION.save();
         ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_UPDATE_INTERVAL_MILLIS.save();
         ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_GLOW_STRENGTH.save();
         ARMOR_MODE_DYNAMIC_TEXTURE_COMPAT_UV_COVERAGE_MASK_ENABLED.save();
         ARMOR_MODE_OVERLAY_PATTERN_MODE.save();
         ARMOR_MODE_OVERLAY_FULLBRIGHT.save();
         ARMOR_MODE_OVERLAY_RENDER_IN_FIRST_PERSON.save();
         ARMOR_MODE_OVERLAY_RENDER_IN_THIRD_PERSON.save();
         ARMOR_MODE_TRANSITION_ENABLED.save();
         ARMOR_MODE_TRANSITION_DURATION_SECONDS.save();
         ARMOR_MODE_TRANSITION_PIXEL_BLOCK_SIZE_PX.save();
         ARMOR_MODE_TRANSITION_STAGGER_AMOUNT.save();
         ARMOR_MODE_TRANSITION_FADE_WINDOW.save();
         ARMOR_MODE_TRANSITION_SEED.save();
         ARMOR_MODE_TRANSITION_AFFECTS_PULSES.save();
         DEBUG_ARMOR_MODE_SETTINGS_SCREEN.save();
         ARMOR_MODE_HEX_TEXTURE_SIZE_PX.save();
         ARMOR_MODE_PLASMA_BLOB_INNER_ALPHA.save();
         ARMOR_MODE_PLASMA_BLOB_COUNT.save();
         ARMOR_MODE_PLASMA_BLOB_MIN_SIZE_PX.save();
         ARMOR_MODE_PLASMA_BLOB_MAX_SIZE_PX.save();
         ARMOR_MODE_PLASMA_BLOB_EDGE_SOFTNESS_PX.save();
         ARMOR_MODE_PLASMA_BLOB_RIM_ALPHA.save();
         ARMOR_MODE_PLASMA_BLOB_RIM_WIDTH_PX.save();
         ARMOR_MODE_PLASMA_BLOB_RIM_BRIGHTNESS.save();
         ARMOR_MODE_PLASMA_BLOB_DRIFT_SPEED_PX_PER_SECOND.save();
         ARMOR_MODE_PLASMA_BLOB_DRIFT_RANGE_SCALE.save();
         ARMOR_MODE_PLASMA_BLOB_SHAPE_DISTORTION.save();
         ARMOR_MODE_PATCH_TEXTURE_ALPHA.save();
         ARMOR_MODE_PATCH_SPOT_ALPHA.save();
         ARMOR_MODE_PATCH_MIN_SIZE_PX.save();
         ARMOR_MODE_PATCH_MAX_SIZE_PX.save();
         ARMOR_MODE_PATCH_FREQUENCY.save();
         ARMOR_MODE_PATCH_DENSITY.save();
         ARMOR_MODE_PATCH_FLOW_VARIANT.save();
         ARMOR_MODE_PATCH_CLOUD_EDGE_SOFTNESS.save();
         ARMOR_MODE_PULSE_ENABLED.save();
         ARMOR_MODE_PULSE_CENTER_SOURCE.save();
         ARMOR_MODE_PULSE_VISUAL_MODE.save();
         ARMOR_MODE_PULSE_MAX_CENTERS.save();
         ARMOR_MODE_PULSE_CENTER_MARKER_ALPHA.save();
         ARMOR_MODE_PULSE_INTERVAL_SECONDS.save();
         ARMOR_MODE_PULSE_DURATION_SECONDS.save();
         ARMOR_MODE_PULSE_START_RADIUS_PX.save();
         ARMOR_MODE_PULSE_END_RADIUS_PX.save();
         ARMOR_MODE_PULSE_CORE_WIDTH_PX.save();
         ARMOR_MODE_PULSE_GLOW_RADIUS_PX.save();
         ARMOR_MODE_PULSE_CORE_SOFTNESS_PX.save();
         ARMOR_MODE_PULSE_GLOW_SOFTNESS_PX.save();
         ARMOR_MODE_PULSE_CORE_ALPHA.save();
         ARMOR_MODE_PULSE_GLOW_ALPHA.save();
         ARMOR_MODE_PULSE_CORE_COLOR.save();
         ARMOR_MODE_PULSE_GLOW_COLOR.save();
         ARMOR_MODE_PULSE_BRIGHTNESS.save();
         ARMOR_MODE_PULSE_FADE_POWER.save();
         ARMOR_MODE_PULSE_RING_DISTORTION_PX.save();
         ARMOR_MODE_PULSE_RING_DISTORTION_FREQUENCY.save();
         ARMOR_MODE_PULSE_RING_ENERGY_VARIATION.save();
         ARMOR_MODE_PULSE_WIDTH_VARIATION.save();
         ARMOR_MODE_PULSE_BRIGHTNESS_VARIATION.save();
         ARMOR_MODE_PULSE_EDGE_ROUGHNESS.save();
         ARMOR_MODE_PULSE_PATTERN_ROTATION_RANDOMNESS.save();
         ARMOR_MODE_PULSE_MERGING_ENABLED.save();
         ARMOR_MODE_PULSE_MERGE_STRENGTH.save();
         ARMOR_MODE_PULSE_MERGE_SOFTNESS_PX.save();
         ARMOR_MODE_PULSE_MERGE_BRIGHTNESS_BOOST.save();
         ARMOR_MODE_PULSE_DEBUG_FORCE_VISIBLE.save();
         ARMOR_MODE_PULSE_DEBUG_OUTPUT_ONLY_PULSE.save();
         DEBUG_ARMOR_MODE_PLASMA_FIELD.save();
      } catch (RuntimeException var1) {
      }
   }

   public static void setDebugArmorModeOverlay(boolean value) {
      safeSetBoolean(DEBUG_ARMOR_MODE_OVERLAY, value);
   }

   public static void setDebugArmorModeDiamondShimmer(boolean value) {
      safeSetBoolean(DEBUG_ARMOR_MODE_DIAMOND_SHIMMER, value);
   }

   public static void setNanoGeoDebugUv(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_UV, value);
   }

   public static void setNanoGeoDebugUvLogCubes(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_UV_LOG_CUBES, value);
   }

   public static void setNanoGeoDebugUvRenderChecker(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_UV_RENDER_CHECKER, value);
   }

   public static void setNanoGeoDebugUvFaceColors(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_UV_FACE_COLORS, value);
   }

   public static void setNanoGeoDebugVertexColors(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_VERTEX_COLORS, value);
   }

   public static void setNanoGeoDebugLogResources(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_LOG_RESOURCES, value);
   }

   public static void setNanoGeoDebugLogVisibleBones(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_LOG_VISIBLE_BONES, value);
   }

   public static void setNanoGeoDebugLogBoneTransforms(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_LOG_BONE_TRANSFORMS, value);
   }

   public static void setNanoGeoDebugPerfEnabled(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_PERF_ENABLED, value);
   }

   public static void setNanoGeoDebugPerfLogInterval(int value) {
      safeSetInt(NANO_GEO_DEBUG_PERF_LOG_INTERVAL, value);
   }

   public static void setNanoGeoDebugPerfLogStatic(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_PERF_LOG_STATIC, value);
   }

   public static void setNanoGeoPerfProfilingEnabled(boolean value) {
      safeSetBoolean(NANO_GEO_PERF_PROFILING_ENABLED, value);
   }

   public static void setNanoGeoPerfLogEverySeconds(int value) {
      safeSetInt(NANO_GEO_PERF_LOG_EVERY_SECONDS, value);
   }

   public static void setNanoGeoPerfCompareMode(boolean value) {
      safeSetBoolean(NANO_GEO_PERF_COMPARE_MODE, value);
   }

   public static void setNanoGeoPerfTrackAllocations(boolean value) {
      safeSetBoolean(NANO_GEO_PERF_TRACK_ALLOCATIONS, value);
   }

   public static void setNanoGeoPerfShowOverlay(boolean value) {
      safeSetBoolean(NANO_GEO_PERF_SHOW_OVERLAY, value);
   }

   public static void setNanoGeoShowAdvancedDebugOptions(boolean value) {
      safeSetBoolean(NANO_GEO_SHOW_ADVANCED_DEBUG_OPTIONS, value);
   }

   public static void setNanoGeoDebugRenderAxes(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_RENDER_AXES, value);
   }

   public static void setNanoGeoDebugShowBonePivots(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_SHOW_BONE_PIVOTS, value);
   }

   public static void setNanoGeoDebugShowBoneAxes(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_SHOW_BONE_AXES, value);
   }

   public static void setNanoGeoDebugTintLeftRight(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_TINT_LEFT_RIGHT, value);
   }

   public static void setNanoGeoDebugHideChildren(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_HIDE_CHILDREN, value);
   }

   public static void setNanoGeoDebugFreezePlayerPose(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_FREEZE_PLAYER_POSE, value);
   }

   public static void setNanoGeoDebugBoneName(String value) {
      safeSetString(NANO_GEO_DEBUG_BONE_NAME, value, "");
   }

   public static void setNanoGeoDebugRenderOnlyBone(String value) {
      safeSetString(NANO_GEO_DEBUG_RENDER_ONLY_BONE, value, "");
   }

   public static void setNanoGeoDebugRenderOnlyCubeIndex(int value) {
      safeSetInt(NANO_GEO_DEBUG_RENDER_ONLY_CUBE_INDEX, value);
   }

   public static void setNanoGeoDebugRenderOnlyFace(String value) {
      safeSetString(NANO_GEO_DEBUG_RENDER_ONLY_FACE, value, "");
   }

   public static void setNanoGeoDebugLogUvForBone(String value) {
      safeSetString(NANO_GEO_DEBUG_LOG_UV_FOR_BONE, value, "");
   }

   public static void setNanoGeoDebugForceBoneRotationX(float value) {
      safeSetDouble(NANO_GEO_DEBUG_FORCE_BONE_ROTATION_X, value);
   }

   public static void setNanoGeoDebugForceBoneRotationY(float value) {
      safeSetDouble(NANO_GEO_DEBUG_FORCE_BONE_ROTATION_Y, value);
   }

   public static void setNanoGeoDebugForceBoneRotationZ(float value) {
      safeSetDouble(NANO_GEO_DEBUG_FORCE_BONE_ROTATION_Z, value);
   }

   public static void setNanoGeoDebugRenderType(NanoGeoClientConfig.NanoGeoDebugRenderType value) {
      try {
         NANO_GEO_DEBUG_RENDER_TYPE.set(value == null ? DEFAULT_NANO_GEO_DEBUG_RENDER_TYPE : value);
         NANO_GEO_DEBUG_RENDER_TYPE.save();
      } catch (RuntimeException var2) {
      }
   }

   public static void setNanoGeoDebugArmorChestOnlyFullModel(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_ARMOR_CHEST_ONLY_FULL_MODEL, value);
   }

   public static void setNanoGeoDebugUseGradientArmorTexture(boolean value) {
      safeSetBoolean(NANO_GEO_DEBUG_USE_GRADIENT_ARMOR_TEXTURE, value);
   }

   private static boolean safeGetBoolean(BooleanValue value, boolean fallback) {
      try {
         return (Boolean)value.get();
      } catch (IllegalStateException var3) {
         return fallback;
      }
   }

   private static int safeGetInt(IntValue value, int fallback) {
      try {
         return (Integer)value.get();
      } catch (IllegalStateException var3) {
         return fallback;
      }
   }

   private static double safeGetDouble(DoubleValue value, double fallback) {
      try {
         return (Double)value.get();
      } catch (IllegalStateException var4) {
         return fallback;
      }
   }

   private static String safeGetString(ConfigValue<String> value, String fallback) {
      try {
         String resolved = (String)value.get();
         return resolved == null ? fallback : resolved;
      } catch (IllegalStateException var3) {
         return fallback;
      }
   }

   private static void safeSetBoolean(BooleanValue value, boolean target) {
      safeSetBoolean(value, target, true);
   }

   private static void safeSetBoolean(BooleanValue value, boolean target, boolean persist) {
      try {
         value.set(target);
         if (persist) {
            value.save();
         }
      } catch (RuntimeException var4) {
      }
   }

   private static void safeSetInt(IntValue value, int target) {
      safeSetInt(value, target, true);
   }

   private static void safeSetInt(IntValue value, int target, boolean persist) {
      try {
         value.set(target);
         if (persist) {
            value.save();
         }
      } catch (RuntimeException var4) {
      }
   }

   private static void safeSetDouble(DoubleValue value, double target) {
      safeSetDouble(value, target, true);
   }

   private static void safeSetDouble(DoubleValue value, double target, boolean persist) {
      try {
         value.set(target);
         if (persist) {
            value.save();
         }
      } catch (RuntimeException var5) {
      }
   }

   private static void safeSetString(ConfigValue<String> value, String target, String fallback) {
      safeSetString(value, target, fallback, true);
   }

   private static void safeSetString(ConfigValue<String> value, String target, String fallback, boolean persist) {
      try {
         value.set(target == null ? fallback : target);
         if (persist) {
            value.save();
         }
      } catch (RuntimeException var5) {
      }
   }

   private static String normalizeRgbHexColor(String value, String fallback) {
      String fallbackNormalized = normalizeRgbHexColorInternal(fallback);
      String normalized = normalizeRgbHexColorInternal(value);
      return normalized == null ? fallbackNormalized : normalized;
   }

   private static int parseRgbHexColor(String value, String fallback) {
      String normalized = normalizeRgbHexColor(value, fallback);

      try {
         return Integer.parseInt(normalized.substring(1), 16) & 16777215;
      } catch (RuntimeException var4) {
         return Integer.parseInt(normalizeRgbHexColor(fallback, "#FFFFFF").substring(1), 16) & 16777215;
      }
   }

   private static String normalizeRgbHexColorInternal(String value) {
      if (value == null) {
         return null;
      } else {
         String trimmed = value.trim();
         if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
         }

         if (trimmed.length() != 6) {
            return null;
         } else {
            for (int i = 0; i < trimmed.length(); i++) {
               char c = trimmed.charAt(i);
               boolean hex = c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
               if (!hex) {
                  return null;
               }
            }

            return "#" + trimmed.toUpperCase(Locale.ROOT);
         }
      }
   }

   public static enum ArmorModeCompatPerformancePreset {
      QUALITY,
      QUALITY_OPTIMIZED,
      THIRD_PERSON_FAST,
      COMPOSITE_FAST,
      BASE_COMPOSITE_FAST,
      UNSHADED_ARMOR_LAYER;
   }

   public static enum ArmorModeCompatPulsePreset {
      CRISP_MASKED,
      LEGACY_SMOOTH;
   }

   public static enum ArmorModeDiamondShimmerDirection {
      DOWN_RIGHT(1.0F, 1.0F),
      UP_LEFT(-1.0F, -1.0F),
      DOWN_LEFT(-1.0F, 1.0F),
      UP_RIGHT(1.0F, -1.0F);

      private final float xSign;
      private final float ySign;

      private ArmorModeDiamondShimmerDirection(float xSign, float ySign) {
         this.xSign = xSign;
         this.ySign = ySign;
      }

      public float xSign() {
         return this.xSign;
      }

      public float ySign() {
         return this.ySign;
      }
   }

   public static enum ArmorModeHexTexturePreset {
      UV_512(512.0F),
      UV_1024(1024.0F);

      private final float textureSizePx;

      private ArmorModeHexTexturePreset(float textureSizePx) {
         this.textureSizePx = textureSizePx;
      }

      public float textureSizePx() {
         return this.textureSizePx;
      }

      public static NanoGeoClientConfig.ArmorModeHexTexturePreset closestTo(float textureSizePx) {
         return Math.abs(textureSizePx - UV_1024.textureSizePx) < Math.abs(textureSizePx - UV_512.textureSizePx) ? UV_1024 : UV_512;
      }
   }

   public static enum ArmorModeOverlayBackend {
      AUTO,
      CUSTOM_SHADER,
      TEXTURE_FALLBACK,
      DYNAMIC_TEXTURE_COMPAT,
      DISABLED;
   }

   public static enum ArmorModeOverlayPatternMode {
      NANO_PLASMA_FIELD,
      CHAOTIC_PATCH_FLOW;
   }

   public static enum ArmorModePatchFlowVariant {
      DYNAMIC_RESEED,
      FIXED_ARCHETYPE_LOOP,
      SOFT_CLOUD_ARCHETYPES;
   }

   public static enum ArmorModePulseCenterSource {
      RED_PIXEL_JSON;
   }

   public static enum ArmorModePulseVisualMode {
      SIMPLE_RING,
      CRYSIS_ELECTRIC_RING;
   }

   public static enum NanoGeoDebugRenderType {
      ARMOR_CUTOUT_NO_CULL,
      ENTITY_CUTOUT_NO_CULL,
      ENTITY_TRANSLUCENT;
   }
}
