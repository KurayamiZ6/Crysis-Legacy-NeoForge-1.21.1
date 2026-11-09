package io.github.painworks.crysismod.nanovisor.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class NanoVisorClientConfig {
   private static final Builder BUILDER = new Builder();
   public static final boolean DEFAULT_VISOR_ENABLED = true;
   public static final NanoVisorToggleMode DEFAULT_VISOR_TOGGLE_MODE = NanoVisorToggleMode.TOGGLE;
   public static final boolean DEFAULT_VISOR_OVERLAY_ENABLED = true;
   public static final boolean DEFAULT_ZOOM_ENABLED = true;
   public static final double DEFAULT_ZOOM_MIN_FOV_MULTIPLIER = 0.35;
   public static final double DEFAULT_ZOOM_MAX_FOV_MULTIPLIER = 0.1;
   public static final double DEFAULT_ZOOM_SMOOTHNESS = 0.2;
   public static final boolean DEFAULT_ZOOM_HOLD_TO_USE = false;
   public static final double DEFAULT_ZOOM_LOOK_SENSITIVITY_MULTIPLIER = 0.6;
   public static final boolean DEFAULT_MARK_TARGET_ENABLED = true;
   public static final double DEFAULT_MARK_RANGE = 100.0;
   public static final int DEFAULT_MARK_GLOW_DURATION_TICKS = 800;
   public static final boolean DEFAULT_MARK_ONLY_LIVING_ENTITIES = true;
   public static final boolean DEFAULT_MARK_REQUIRE_LINE_OF_SIGHT = true;
   public static final int DEFAULT_MAX_MARKED_TARGETS = 8;
   public static final boolean DEFAULT_BOW_BALLISTIC_ASSIST_ENABLED = true;
   public static final boolean DEFAULT_BOW_BALLISTIC_ASSIST_HUD_INDICATOR = true;
   public static final int DEFAULT_BOW_BALLISTIC_ASSIST_SOLVE_INTERVAL_TICKS = 4;
   public static final int DEFAULT_BOW_BALLISTIC_ASSIST_MAX_FLIGHT_TICKS = 90;
   public static final double DEFAULT_BOW_BALLISTIC_ASSIST_AIM_CONE_DEGREES = 35.0;
   public static final double DEFAULT_BOW_BALLISTIC_ASSIST_LINE_OPACITY = 0.78;
   public static final boolean DEFAULT_BOW_FIRE_CONTROL_CAMERA_ASSIST_ENABLED = true;
   public static final double DEFAULT_BOW_FIRE_CONTROL_CAMERA_ASSIST_STRENGTH = 0.5;
   public static final double DEFAULT_BOW_FIRE_CONTROL_ACTIVATION_CONE_DEGREES = 4.0;
   public static final double DEFAULT_BOW_FIRE_CONTROL_MAX_TURN_RATE_DEGREES = 1.8;
   public static final boolean DEFAULT_BOW_FIRE_CONTROL_LEAD_PREDICTION = true;
   public static final double DEFAULT_BOW_FIRE_CONTROL_MARKER_RADIUS = 0.34;
   public static final boolean DEFAULT_SHOW_DISTANCE_TO_TARGET = true;
   public static final boolean DEFAULT_SHOW_DISTANCE_TO_BLOCK = true;
   public static final double DEFAULT_MAX_DISTANCE_TO_TARGET = 512.0;
   public static final double DEFAULT_MAX_DISTANCE_TO_BLOCK = 512.0;
   public static final boolean DEFAULT_SHOW_TARGET_NAME = true;
   public static final boolean DEFAULT_SHOW_ENERGY_PERCENT = false;
   public static final double DEFAULT_HUD_OPACITY = 0.95;
   public static final double DEFAULT_HUD_SCALE = 0.7;
   public static final int DEFAULT_HUD_OFFSET_X = 0;
   public static final int DEFAULT_HUD_OFFSET_Y = 0;
   public static final double DEFAULT_VISOR_ZOOM_ARROW_START_X = 702.0;
   public static final double DEFAULT_VISOR_ZOOM_ARROW_START_Y = 568.0;
   public static final double DEFAULT_VISOR_ZOOM_ARROW_END_X = 702.0;
   public static final double DEFAULT_VISOR_ZOOM_ARROW_END_Y = 493.0;
   public static final boolean DEFAULT_HUD_VIGNETTE_ENABLED = true;
   public static final double DEFAULT_HUD_VIGNETTE_INTENSITY = 1.0;
   public static final double DEFAULT_HUD_VIGNETTE_STRENGTH = 1.1;
   public static final double DEFAULT_HUD_VIGNETTE_SIZE_TO_CENTER = 0.65;
   public static final boolean DEFAULT_DEBUG_VISOR_INFO = false;
   public static final boolean DEFAULT_DEBUG_TARGETING_INFO = false;
   public static final boolean DEFAULT_DEBUG_HUD_ANCHORS = false;
   public static final double DEFAULT_CONFIG_SCREEN_BACKGROUND_OPACITY = 0.1;
   private static final BooleanValue VISOR_ENABLED = BUILDER.comment("Master switch for Nano Visor systems.").define("visorEnabled", true);
   private static final EnumValue<NanoVisorToggleMode> VISOR_TOGGLE_MODE = BUILDER.comment("How Nano Visor is toggled by input.")
      .defineEnum("visorToggleMode", DEFAULT_VISOR_TOGGLE_MODE);
   private static final BooleanValue VISOR_OVERLAY_ENABLED = BUILDER.comment("Render visor overlay when visor is active.").define("visorOverlayEnabled", true);
   private static final BooleanValue ZOOM_ENABLED = BUILDER.comment("Enable zoom subsystem.").define("zoomEnabled", true);
   private static final DoubleValue ZOOM_MIN_FOV_MULTIPLIER = BUILDER.comment("Baseline visor zoom FOV multiplier (higher = weaker zoom).")
      .defineInRange("zoomMinFovMultiplier", 0.35, 0.05, 2.0);
   private static final DoubleValue ZOOM_MAX_FOV_MULTIPLIER = BUILDER.comment(
         "Boosted visor zoom FOV multiplier while zoom key is active (lower = stronger zoom)."
      )
      .defineInRange("zoomMaxFovMultiplier", 0.1, 0.05, 2.0);
   private static final DoubleValue ZOOM_SMOOTHNESS = BUILDER.comment("Interpolation factor for zoom transition.")
      .defineInRange("zoomSmoothness", 0.2, 0.0, 1.0);
   private static final BooleanValue ZOOM_HOLD_TO_USE = BUILDER.comment("Use hold behavior for zoom key.").define("zoomHoldToUse", false);
   private static final DoubleValue ZOOM_LOOK_SENSITIVITY_MULTIPLIER = BUILDER.comment("Base mouse look sensitivity multiplier while Nano Visor is active.")
      .defineInRange("zoomLookSensitivityMultiplier", 0.6, 0.05, 1.0);
   private static final BooleanValue MARK_TARGET_ENABLED = BUILDER.comment("Enable target marking subsystem.").define("markTargetEnabled", true);
   private static final DoubleValue MARK_RANGE = BUILDER.comment("Maximum mark distance in blocks.").defineInRange("markRange", 100.0, 1.0, 256.0);
   private static final IntValue MARK_GLOW_DURATION_TICKS = BUILDER.comment("Glow duration in ticks for marked target.")
      .defineInRange("markGlowDurationTicks", 800, 1, 12000);
   private static final BooleanValue MARK_ONLY_LIVING_ENTITIES = BUILDER.comment("Allow marking only living entities.").define("markOnlyLivingEntities", true);
   private static final BooleanValue MARK_REQUIRE_LINE_OF_SIGHT = BUILDER.comment("Require direct line of sight for marking.")
      .define("markRequireLineOfSight", true);
   private static final IntValue MAX_MARKED_TARGETS = BUILDER.comment("Maximum simultaneously marked targets.").defineInRange("maxMarkedTargets", 8, 1, 64);
   private static final BooleanValue BOW_BALLISTIC_ASSIST_ENABLED = BUILDER.comment("Enable the client-side bow fire-control system for marked targets.")
      .define("bowBallisticAssistEnabled", true);
   private static final BooleanValue BOW_BALLISTIC_ASSIST_HUD_INDICATOR = BUILDER.comment("Reserved legacy bow assist HUD indicator toggle.")
      .define("bowBallisticAssistHudIndicator", true);
   private static final IntValue BOW_BALLISTIC_ASSIST_SOLVE_INTERVAL_TICKS = BUILDER.comment("Ticks between bow fire-control solver updates for slow targets.")
      .defineInRange("bowBallisticAssistSolveIntervalTicks", 4, 1, 20);
   private static final IntValue BOW_BALLISTIC_ASSIST_MAX_FLIGHT_TICKS = BUILDER.comment("Maximum simulated arrow flight ticks for bow fire-control solving.")
      .defineInRange("bowBallisticAssistMaxFlightTicks", 90, 20, 200);
   private static final DoubleValue BOW_BALLISTIC_ASSIST_AIM_CONE_DEGREES = BUILDER.comment(
         "Initial marked-target acquisition cone for bow fire-control in degrees."
      )
      .defineInRange("bowBallisticAssistAimConeDegrees", 35.0, 1.0, 90.0);
   private static final DoubleValue BOW_BALLISTIC_ASSIST_LINE_OPACITY = BUILDER.comment("Opacity of the world-space bow fire-control line and lead marker.")
      .defineInRange("bowBallisticAssistLineOpacity", 0.78, 0.05, 1.0);
   private static final BooleanValue BOW_FIRE_CONTROL_CAMERA_ASSIST_ENABLED = BUILDER.comment("Enable automatic bow fire-control aim tracking/lock assistance.")
      .define("bowFireControlCameraAssistEnabled", true);
   private static final DoubleValue BOW_FIRE_CONTROL_CAMERA_ASSIST_STRENGTH = BUILDER.comment(
         "Camera assist blend strength. 0 disables pull, 1 applies the full limited correction."
      )
      .defineInRange("bowFireControlCameraAssistStrength", 0.5, 0.0, 1.0);
   private static final DoubleValue BOW_FIRE_CONTROL_ACTIVATION_CONE_DEGREES = BUILDER.comment(
         "Camera assist activates only when current aim is this close to the computed fire-control solution."
      )
      .defineInRange("bowFireControlActivationConeDegrees", 4.0, 0.25, 20.0);
   private static final DoubleValue BOW_FIRE_CONTROL_MAX_TURN_RATE_DEGREES = BUILDER.comment(
         "Maximum camera correction per client tick in degrees before strength is applied."
      )
      .defineInRange("bowFireControlMaxTurnRateDegrees", 1.8, 0.05, 10.0);
   private static final BooleanValue BOW_FIRE_CONTROL_LEAD_PREDICTION = BUILDER.comment(
         "Predict target movement when computing the bow fire-control aiming point."
      )
      .define("bowFireControlLeadPrediction", true);
   private static final DoubleValue BOW_FIRE_CONTROL_MARKER_RADIUS = BUILDER.comment("World-space radius of the bow fire-control circle marker.")
      .defineInRange("bowFireControlMarkerRadius", 0.34, 0.05, 2.0);
   private static final BooleanValue SHOW_DISTANCE_TO_TARGET = BUILDER.comment("Show distance to selected target.").define("showDistanceToTarget", true);
   private static final BooleanValue SHOW_DISTANCE_TO_BLOCK = BUILDER.comment("Show distance to looked-at block.").define("showDistanceToBlock", true);
   private static final DoubleValue MAX_DISTANCE_TO_TARGET = BUILDER.comment("Maximum distance for showing entity distance in visor HUD.")
      .defineInRange("maxDistanceToTarget", 512.0, 1.0, 512.0);
   private static final DoubleValue MAX_DISTANCE_TO_BLOCK = BUILDER.comment("Maximum distance for showing block distance in visor HUD.")
      .defineInRange("maxDistanceToBlock", 512.0, 1.0, 512.0);
   private static final BooleanValue SHOW_TARGET_NAME = BUILDER.comment("Show selected target name.").define("showTargetName", true);
   private static final BooleanValue SHOW_ENERGY_PERCENT = BUILDER.comment("Show suit energy percentage in visor HUD.").define("showEnergyPercent", false);
   private static final DoubleValue HUD_OPACITY = BUILDER.comment("Nano Visor HUD opacity.").defineInRange("hudOpacity", 0.95, 0.0, 1.0);
   private static final DoubleValue HUD_SCALE = BUILDER.comment("Nano Visor HUD scale.").defineInRange("hudScale", 0.7, 0.25, 4.0);
   private static final IntValue HUD_OFFSET_X = BUILDER.comment("Nano Visor HUD offset X.").defineInRange("hudOffsetX", 0, -2000, 2000);
   private static final IntValue HUD_OFFSET_Y = BUILDER.comment("Nano Visor HUD offset Y.").defineInRange("hudOffsetY", 0, -2000, 2000);
   private static final DoubleValue VISOR_ZOOM_ARROW_START_X = BUILDER.comment("Reference-space X position of visor zoom arrow at minimum zoom.")
      .defineInRange("visorZoomArrowStartX", 702.0, -4000.0, 4000.0);
   private static final DoubleValue VISOR_ZOOM_ARROW_START_Y = BUILDER.comment("Reference-space Y position of visor zoom arrow at minimum zoom.")
      .defineInRange("visorZoomArrowStartY", 568.0, -4000.0, 4000.0);
   private static final DoubleValue VISOR_ZOOM_ARROW_END_X = BUILDER.comment("Reference-space X position of visor zoom arrow at maximum zoom.")
      .defineInRange("visorZoomArrowEndX", 702.0, -4000.0, 4000.0);
   private static final DoubleValue VISOR_ZOOM_ARROW_END_Y = BUILDER.comment("Reference-space Y position of visor zoom arrow at maximum zoom.")
      .defineInRange("visorZoomArrowEndY", 493.0, -4000.0, 4000.0);
   private static final BooleanValue HUD_VIGNETTE_ENABLED = BUILDER.comment("Render fullscreen Nano Visor vignette effect.").define("hudVignetteEnabled", true);
   private static final DoubleValue HUD_VIGNETTE_INTENSITY = BUILDER.comment("Nano Visor vignette intensity.")
      .defineInRange("hudVignetteIntensity", 1.0, 0.0, 1.0);
   private static final DoubleValue HUD_VIGNETTE_STRENGTH = BUILDER.comment("Additional multiplier for vignette strength.")
      .defineInRange("hudVignetteStrength", 1.1, 0.1, 3.0);
   private static final DoubleValue HUD_VIGNETTE_SIZE_TO_CENTER = BUILDER.comment("How far vignette extends toward screen center.")
      .defineInRange("hudVignetteSizeToCenter", 0.65, 0.0, 1.0);
   private static final BooleanValue DEBUG_VISOR_INFO = BUILDER.comment("Enable general visor debug info.").define("debugVisorInfo", false);
   private static final BooleanValue DEBUG_TARGETING_INFO = BUILDER.comment("Enable targeting debug info.").define("debugTargetingInfo", false);
   private static final BooleanValue DEBUG_HUD_ANCHORS = BUILDER.comment("Render debug HUD anchor points.").define("debugHudAnchors", false);
   private static final DoubleValue CONFIG_SCREEN_BACKGROUND_OPACITY = BUILDER.comment("Background opacity for Nano Visor config screen.")
      .defineInRange("configScreenBackgroundOpacity", 0.1, 0.0, 1.0);
   public static final ModConfigSpec SPEC = BUILDER.build();

   private NanoVisorClientConfig() {
   }

   public static boolean visorEnabled() {
      return safeGetBoolean(VISOR_ENABLED, true);
   }

   public static NanoVisorToggleMode visorToggleMode() {
      return safeGetEnum(VISOR_TOGGLE_MODE, DEFAULT_VISOR_TOGGLE_MODE);
   }

   public static boolean visorOverlayEnabled() {
      return safeGetBoolean(VISOR_OVERLAY_ENABLED, true);
   }

   public static boolean zoomEnabled() {
      return safeGetBoolean(ZOOM_ENABLED, true);
   }

   public static double zoomMinFovMultiplier() {
      return safeGetDouble(ZOOM_MIN_FOV_MULTIPLIER, 0.35);
   }

   public static double zoomMaxFovMultiplier() {
      return safeGetDouble(ZOOM_MAX_FOV_MULTIPLIER, 0.1);
   }

   public static double zoomSmoothness() {
      return safeGetDouble(ZOOM_SMOOTHNESS, 0.2);
   }

   public static boolean zoomHoldToUse() {
      return safeGetBoolean(ZOOM_HOLD_TO_USE, false);
   }

   public static double zoomLookSensitivityMultiplier() {
      return safeGetDouble(ZOOM_LOOK_SENSITIVITY_MULTIPLIER, 0.6);
   }

   public static boolean markTargetEnabled() {
      return safeGetBoolean(MARK_TARGET_ENABLED, true);
   }

   public static double markRange() {
      return safeGetDouble(MARK_RANGE, 100.0);
   }

   public static int markGlowDurationTicks() {
      return safeGetInt(MARK_GLOW_DURATION_TICKS, 800);
   }

   public static boolean markOnlyLivingEntities() {
      return safeGetBoolean(MARK_ONLY_LIVING_ENTITIES, true);
   }

   public static boolean markRequireLineOfSight() {
      return safeGetBoolean(MARK_REQUIRE_LINE_OF_SIGHT, true);
   }

   public static int maxMarkedTargets() {
      return safeGetInt(MAX_MARKED_TARGETS, 8);
   }

   public static boolean bowBallisticAssistEnabled() {
      return safeGetBoolean(BOW_BALLISTIC_ASSIST_ENABLED, true);
   }

   public static boolean bowBallisticAssistHudIndicator() {
      return safeGetBoolean(BOW_BALLISTIC_ASSIST_HUD_INDICATOR, true);
   }

   public static int bowBallisticAssistSolveIntervalTicks() {
      return safeGetInt(BOW_BALLISTIC_ASSIST_SOLVE_INTERVAL_TICKS, 4);
   }

   public static int bowBallisticAssistMaxFlightTicks() {
      return safeGetInt(BOW_BALLISTIC_ASSIST_MAX_FLIGHT_TICKS, 90);
   }

   public static double bowBallisticAssistAimConeDegrees() {
      return safeGetDouble(BOW_BALLISTIC_ASSIST_AIM_CONE_DEGREES, 35.0);
   }

   public static double bowBallisticAssistLineOpacity() {
      return safeGetDouble(BOW_BALLISTIC_ASSIST_LINE_OPACITY, 0.78);
   }

   public static boolean bowFireControlCameraAssistEnabled() {
      return safeGetBoolean(BOW_FIRE_CONTROL_CAMERA_ASSIST_ENABLED, true);
   }

   public static double bowFireControlCameraAssistStrength() {
      return safeGetDouble(BOW_FIRE_CONTROL_CAMERA_ASSIST_STRENGTH, 0.5);
   }

   public static double bowFireControlActivationConeDegrees() {
      return safeGetDouble(BOW_FIRE_CONTROL_ACTIVATION_CONE_DEGREES, 4.0);
   }

   public static double bowFireControlMaxTurnRateDegrees() {
      return safeGetDouble(BOW_FIRE_CONTROL_MAX_TURN_RATE_DEGREES, 1.8);
   }

   public static boolean bowFireControlLeadPrediction() {
      return safeGetBoolean(BOW_FIRE_CONTROL_LEAD_PREDICTION, true);
   }

   public static double bowFireControlMarkerRadius() {
      return safeGetDouble(BOW_FIRE_CONTROL_MARKER_RADIUS, 0.34);
   }

   public static boolean showDistanceToTarget() {
      return safeGetBoolean(SHOW_DISTANCE_TO_TARGET, true);
   }

   public static boolean showDistanceToBlock() {
      return safeGetBoolean(SHOW_DISTANCE_TO_BLOCK, true);
   }

   public static double maxDistanceToTarget() {
      return safeGetDouble(MAX_DISTANCE_TO_TARGET, 512.0);
   }

   public static double maxDistanceToBlock() {
      return safeGetDouble(MAX_DISTANCE_TO_BLOCK, 512.0);
   }

   public static boolean showTargetName() {
      return safeGetBoolean(SHOW_TARGET_NAME, true);
   }

   public static boolean showEnergyPercent() {
      return safeGetBoolean(SHOW_ENERGY_PERCENT, false);
   }

   public static double hudOpacity() {
      return safeGetDouble(HUD_OPACITY, 0.95);
   }

   public static double hudScale() {
      return safeGetDouble(HUD_SCALE, 0.7);
   }

   public static int hudOffsetX() {
      return safeGetInt(HUD_OFFSET_X, 0);
   }

   public static int hudOffsetY() {
      return safeGetInt(HUD_OFFSET_Y, 0);
   }

   public static double visorZoomArrowStartX() {
      return safeGetDouble(VISOR_ZOOM_ARROW_START_X, 702.0);
   }

   public static double visorZoomArrowStartY() {
      return safeGetDouble(VISOR_ZOOM_ARROW_START_Y, 568.0);
   }

   public static double visorZoomArrowEndX() {
      return safeGetDouble(VISOR_ZOOM_ARROW_END_X, 702.0);
   }

   public static double visorZoomArrowEndY() {
      return safeGetDouble(VISOR_ZOOM_ARROW_END_Y, 493.0);
   }

   public static boolean hudVignetteEnabled() {
      return safeGetBoolean(HUD_VIGNETTE_ENABLED, true);
   }

   public static double hudVignetteIntensity() {
      return safeGetDouble(HUD_VIGNETTE_INTENSITY, 1.0);
   }

   public static double hudVignetteStrength() {
      return safeGetDouble(HUD_VIGNETTE_STRENGTH, 1.1);
   }

   public static double hudVignetteSizeToCenter() {
      return safeGetDouble(HUD_VIGNETTE_SIZE_TO_CENTER, 0.65);
   }

   public static boolean debugVisorInfo() {
      return safeGetBoolean(DEBUG_VISOR_INFO, false);
   }

   public static boolean debugTargetingInfo() {
      return safeGetBoolean(DEBUG_TARGETING_INFO, false);
   }

   public static boolean debugHudAnchors() {
      return safeGetBoolean(DEBUG_HUD_ANCHORS, false);
   }

   public static double configScreenBackgroundOpacity() {
      return safeGetDouble(CONFIG_SCREEN_BACKGROUND_OPACITY, 0.1);
   }

   public static void setVisorEnabled(boolean value) {
      safeSetBoolean(VISOR_ENABLED, value);
   }

   public static void setVisorToggleMode(NanoVisorToggleMode value) {
      safeSetEnum(VISOR_TOGGLE_MODE, value);
   }

   public static void setVisorOverlayEnabled(boolean value) {
      safeSetBoolean(VISOR_OVERLAY_ENABLED, value);
   }

   public static void setZoomEnabled(boolean value) {
      safeSetBoolean(ZOOM_ENABLED, value);
   }

   public static void setZoomMinFovMultiplier(double value) {
      safeSetDouble(ZOOM_MIN_FOV_MULTIPLIER, value);
   }

   public static void setZoomMaxFovMultiplier(double value) {
      safeSetDouble(ZOOM_MAX_FOV_MULTIPLIER, value);
   }

   public static void setZoomSmoothness(double value) {
      safeSetDouble(ZOOM_SMOOTHNESS, value);
   }

   public static void setZoomHoldToUse(boolean value) {
      safeSetBoolean(ZOOM_HOLD_TO_USE, value);
   }

   public static void setZoomLookSensitivityMultiplier(double value) {
      safeSetDouble(ZOOM_LOOK_SENSITIVITY_MULTIPLIER, value);
   }

   public static void setMarkTargetEnabled(boolean value) {
      safeSetBoolean(MARK_TARGET_ENABLED, value);
   }

   public static void setMarkRange(double value) {
      safeSetDouble(MARK_RANGE, value);
   }

   public static void setMarkGlowDurationTicks(int value) {
      safeSetInt(MARK_GLOW_DURATION_TICKS, value);
   }

   public static void setMarkOnlyLivingEntities(boolean value) {
      safeSetBoolean(MARK_ONLY_LIVING_ENTITIES, value);
   }

   public static void setMarkRequireLineOfSight(boolean value) {
      safeSetBoolean(MARK_REQUIRE_LINE_OF_SIGHT, value);
   }

   public static void setMaxMarkedTargets(int value) {
      safeSetInt(MAX_MARKED_TARGETS, value);
   }

   public static void setBowBallisticAssistEnabled(boolean value) {
      safeSetBoolean(BOW_BALLISTIC_ASSIST_ENABLED, value);
   }

   public static void setBowBallisticAssistHudIndicator(boolean value) {
      safeSetBoolean(BOW_BALLISTIC_ASSIST_HUD_INDICATOR, value);
   }

   public static void setBowBallisticAssistSolveIntervalTicks(int value) {
      safeSetInt(BOW_BALLISTIC_ASSIST_SOLVE_INTERVAL_TICKS, value);
   }

   public static void setBowBallisticAssistMaxFlightTicks(int value) {
      safeSetInt(BOW_BALLISTIC_ASSIST_MAX_FLIGHT_TICKS, value);
   }

   public static void setBowBallisticAssistAimConeDegrees(double value) {
      safeSetDouble(BOW_BALLISTIC_ASSIST_AIM_CONE_DEGREES, value);
   }

   public static void setBowBallisticAssistLineOpacity(double value) {
      safeSetDouble(BOW_BALLISTIC_ASSIST_LINE_OPACITY, value);
   }

   public static void setBowFireControlCameraAssistEnabled(boolean value) {
      safeSetBoolean(BOW_FIRE_CONTROL_CAMERA_ASSIST_ENABLED, value);
   }

   public static void setBowFireControlCameraAssistStrength(double value) {
      safeSetDouble(BOW_FIRE_CONTROL_CAMERA_ASSIST_STRENGTH, value);
   }

   public static void setBowFireControlActivationConeDegrees(double value) {
      safeSetDouble(BOW_FIRE_CONTROL_ACTIVATION_CONE_DEGREES, value);
   }

   public static void setBowFireControlMaxTurnRateDegrees(double value) {
      safeSetDouble(BOW_FIRE_CONTROL_MAX_TURN_RATE_DEGREES, value);
   }

   public static void setBowFireControlLeadPrediction(boolean value) {
      safeSetBoolean(BOW_FIRE_CONTROL_LEAD_PREDICTION, value);
   }

   public static void setBowFireControlMarkerRadius(double value) {
      safeSetDouble(BOW_FIRE_CONTROL_MARKER_RADIUS, value);
   }

   public static void setShowDistanceToTarget(boolean value) {
      safeSetBoolean(SHOW_DISTANCE_TO_TARGET, value);
   }

   public static void setShowDistanceToBlock(boolean value) {
      safeSetBoolean(SHOW_DISTANCE_TO_BLOCK, value);
   }

   public static void setMaxDistanceToTarget(double value) {
      safeSetDouble(MAX_DISTANCE_TO_TARGET, value);
   }

   public static void setMaxDistanceToBlock(double value) {
      safeSetDouble(MAX_DISTANCE_TO_BLOCK, value);
   }

   public static void setShowTargetName(boolean value) {
      safeSetBoolean(SHOW_TARGET_NAME, value);
   }

   public static void setShowEnergyPercent(boolean value) {
      safeSetBoolean(SHOW_ENERGY_PERCENT, value);
   }

   public static void setHudOpacity(double value) {
      safeSetDouble(HUD_OPACITY, value);
   }

   public static void setHudScale(double value) {
      safeSetDouble(HUD_SCALE, value);
   }

   public static void setHudOffsetX(int value) {
      safeSetInt(HUD_OFFSET_X, value);
   }

   public static void setHudOffsetY(int value) {
      safeSetInt(HUD_OFFSET_Y, value);
   }

   public static void setVisorZoomArrowStartX(double value) {
      safeSetDouble(VISOR_ZOOM_ARROW_START_X, value);
   }

   public static void setVisorZoomArrowStartY(double value) {
      safeSetDouble(VISOR_ZOOM_ARROW_START_Y, value);
   }

   public static void setVisorZoomArrowEndX(double value) {
      safeSetDouble(VISOR_ZOOM_ARROW_END_X, value);
   }

   public static void setVisorZoomArrowEndY(double value) {
      safeSetDouble(VISOR_ZOOM_ARROW_END_Y, value);
   }

   public static void setHudVignetteEnabled(boolean value) {
      safeSetBoolean(HUD_VIGNETTE_ENABLED, value);
   }

   public static void setHudVignetteIntensity(double value) {
      safeSetDouble(HUD_VIGNETTE_INTENSITY, value);
   }

   public static void setHudVignetteStrength(double value) {
      safeSetDouble(HUD_VIGNETTE_STRENGTH, value);
   }

   public static void setHudVignetteSizeToCenter(double value) {
      safeSetDouble(HUD_VIGNETTE_SIZE_TO_CENTER, value);
   }

   public static void setDebugVisorInfo(boolean value) {
      safeSetBoolean(DEBUG_VISOR_INFO, value);
   }

   public static void setDebugTargetingInfo(boolean value) {
      safeSetBoolean(DEBUG_TARGETING_INFO, value);
   }

   public static void setDebugHudAnchors(boolean value) {
      safeSetBoolean(DEBUG_HUD_ANCHORS, value);
   }

   public static void setConfigScreenBackgroundOpacity(double value) {
      safeSetDouble(CONFIG_SCREEN_BACKGROUND_OPACITY, value);
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

   private static <T extends Enum<T>> T safeGetEnum(EnumValue<T> value, T fallback) {
      try {
         return (T)value.get();
      } catch (IllegalStateException var3) {
         return fallback;
      }
   }

   private static void safeSetBoolean(BooleanValue value, boolean target) {
      try {
         value.set(target);
         value.save();
      } catch (RuntimeException var3) {
      }
   }

   private static void safeSetInt(IntValue value, int target) {
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
      BUILDER.push("zoom");
      BUILDER.pop();
      BUILDER.push("targeting");
      BUILDER.pop();
      BUILDER.push("hud");
      BUILDER.pop();
      BUILDER.push("debug");
      BUILDER.pop();
   }
}
