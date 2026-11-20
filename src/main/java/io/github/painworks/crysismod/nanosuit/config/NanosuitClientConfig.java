package io.github.painworks.crysismod.nanosuit.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class NanosuitClientConfig {
   private static final Builder BUILDER = new Builder();
   private static final boolean DEFAULT_HUD_ENABLED = true;
   private static final int DEFAULT_HUD_OFFSET_X = -28;
   private static final int DEFAULT_HUD_OFFSET_Y = -8;
   private static final int DEFAULT_PERSPECTIVE_DEBUG_OFFSET_X = -56;
   private static final int DEFAULT_PERSPECTIVE_DEBUG_OFFSET_Y = -8;
   private static final double DEFAULT_PERSPECTIVE_DEBUG_SCALE_X = 0.89533126F;
   private static final double DEFAULT_PERSPECTIVE_DEBUG_SCALE_Y = 0.9F;
   private static final double DEFAULT_PERSPECTIVE_DEBUG_ROTATION = 0.0;
   private static final double DEFAULT_PERSPECTIVE_DEBUG_TILT = 0.8F;
   private static final double DEFAULT_PERSPECTIVE_DEBUG_VERTICAL_SKEW_DOWN = 0.16084337F;
   private static final double DEFAULT_PERSPECTIVE_DEBUG_TEXT_SIZE = 1.1667922F;
   private static final double DEFAULT_PERSPECTIVE_DEBUG_LAYER_SPACING = 0.0;
   private static final double DEFAULT_PERSPECTIVE_DEBUG_ENERGY_PERCENT_COLOR_R = 0.9019608F;
   private static final double DEFAULT_PERSPECTIVE_DEBUG_ENERGY_PERCENT_COLOR_G = 0.98039216F;
   private static final double DEFAULT_PERSPECTIVE_DEBUG_ENERGY_PERCENT_COLOR_B = 1.0;
   private static final double DEFAULT_PERSPECTIVE_DEBUG_ENERGY_PERCENT_ALPHA = 1.0;
   private static final double DEFAULT_HUD_SCALE = 0.200000002980232;
   private static final double DEFAULT_ARMOR_MODE_OVERLAY_SCALE = 1.04F;
   private static final boolean DEFAULT_ARMOR_OVERLAY_INTRO_ENABLED = true;
   private static final int DEFAULT_ARMOR_OVERLAY_INTRO_DURATION_TICKS = 18;
   private static final double DEFAULT_ARMOR_OVERLAY_INTRO_ELASTIC_STRENGTH = 0.35F;
   private static final int DEFAULT_ARMOR_OVERLAY_INTRO_SEGMENT_COUNT = 48;
   private static final double DEFAULT_ARMOR_OVERLAY_INTRO_OVERSHOOT = 0.08F;
   private static final int DEFAULT_ARMOR_OVERLAY_INTRO_START_OUTSIDE_DISTANCE = 260;
   private static final double DEFAULT_ARMOR_OVERLAY_INTRO_CENTER_DELAY_BIAS = 0.35F;
   private static final boolean DEFAULT_ARMOR_OVERLAY_INTRO_ALPHA_FADE = true;
   private static final boolean DEFAULT_ARMOR_OVERLAY_INTRO_GLOW_PULSE = true;
   private static final boolean DEFAULT_ARMOR_OVERLAY_MOTION_BLUR_ENABLED = true;
   private static final double DEFAULT_ARMOR_OVERLAY_MOTION_BLUR_STRENGTH = 0.35F;
   private static final int DEFAULT_ARMOR_OVERLAY_MOTION_BLUR_SAMPLES = 3;
   private static final boolean DEFAULT_ARMOR_OVERLAY_INTRO_DEBUG = false;
   private static final double DEFAULT_OFF_LAYER_ALPHA = 1.0;
   private static final boolean DEFAULT_TEXTURE_SMOOTH = false;
   private static final boolean DEFAULT_TEXTURE_MIPMAP = false;
   private static final boolean DEFAULT_INERTIA_ENABLED = true;
   private static final double DEFAULT_INERTIA_INTENSITY = 1.0;
   private static final double DEFAULT_YAW_INERTIA_STRENGTH = 0.015;
   private static final double DEFAULT_PITCH_INERTIA_STRENGTH = 0.01;
   private static final double DEFAULT_MAX_VIEW_OFFSET = 6.0;
   private static final double DEFAULT_MAX_ROLL_DEGREES = 1.2;
   private static final double DEFAULT_DAMPING_STRENGTH = 12.0;
   private static final double DEFAULT_RETURN_SPEED = 8.0;
   private static final double DEFAULT_WALK_SIDE_AMPLITUDE = 2.4;
   private static final double DEFAULT_WALK_VERTICAL_AMPLITUDE = 2.8;
   private static final double DEFAULT_WALK_ROLL_AMPLITUDE = 0.95;
   private static final double DEFAULT_WALK_STEP_DURATION = 0.34;
   private static final double DEFAULT_WALK_SPRING_STRENGTH = 28.0;
   private static final double DEFAULT_WALK_DAMPING = 11.5;
   private static final double DEFAULT_WALK_RETURN_TO_CENTER_SPEED = 8.5;
   private static final double DEFAULT_SPRINT_WALK_MULTIPLIER = 1.3;
   private static final double DEFAULT_MAX_WALK_OFFSET_X = 55.377357F;
   private static final double DEFAULT_MAX_WALK_OFFSET_Y = 64.245285F;
   private static final double DEFAULT_MAX_WALK_ROLL = 2.0;
   private static final boolean DEFAULT_NANOSUIT_FIRST_PERSON_ARMS_ENABLED = true;
   private static final boolean DEFAULT_NANOSUIT_FIRST_PERSON_ARMS_USE_NANOGEO = true;
   private static final boolean DEFAULT_DEBUG_ENERGY_DRAIN_INFO = false;
   private static final boolean DEFAULT_HUD_MASTER_COMPOSITOR_ENABLED = true;
   private static final boolean DEFAULT_HUD_MASTER_COMPOSITOR_ROUTE_NANOSUIT_HUD = true;
   private static final boolean DEFAULT_HUD_MASTER_COMPOSITOR_ROUTE_TACZ_HUD = true;
   private static final boolean DEFAULT_HUD_MASTER_COMPOSITOR_ROUTE_NANOVISOR_HUD = true;
   private static final boolean DEFAULT_HUD_MASTER_COMPOSITOR_ROUTE_MINIMAP_HUD = true;
   private static final boolean DEFAULT_TACZ_HUD_OVERRIDE_ENABLED = true;
   private static final boolean DEFAULT_TACZ_HIDE_ORIGINAL_HUD = true;
   private static final int DEFAULT_TACZ_HUD_GROUP_OFFSET_X = -42;
   private static final int DEFAULT_TACZ_HUD_GROUP_OFFSET_Y = -30;
   private static final double DEFAULT_TACZ_HUD_GROUP_SCALE_X = 0.82F;
   private static final double DEFAULT_TACZ_HUD_GROUP_SCALE_Y = 0.82F;
   private static final double DEFAULT_TACZ_HUD_GROUP_ROTATION = 0.0;
   private static final double DEFAULT_TACZ_HUD_GROUP_VERTICAL_SKEW_DOWN = 0.17F;
   private static final int DEFAULT_TACZ_HUD_ICON_OFFSET_X = 0;
   private static final int DEFAULT_TACZ_HUD_ICON_OFFSET_Y = 1;
   private static final double DEFAULT_TACZ_HUD_ICON_SCALE = 1.0;
   private static final int DEFAULT_TACZ_HUD_AMMO_GUN_OFFSET_X = 47;
   private static final int DEFAULT_TACZ_HUD_AMMO_GUN_OFFSET_Y = 1;
   private static final double DEFAULT_TACZ_HUD_AMMO_GUN_SCALE = 1.69F;
   private static final int DEFAULT_TACZ_HUD_AMMO_INV_OFFSET_X = 79;
   private static final int DEFAULT_TACZ_HUD_AMMO_INV_OFFSET_Y = 1;
   private static final double DEFAULT_TACZ_HUD_AMMO_INV_SCALE = 1.0;
   private static final int DEFAULT_TACZ_HUD_MODE_OFFSET_X = 0;
   private static final int DEFAULT_TACZ_HUD_MODE_OFFSET_Y = 32;
   private static final double DEFAULT_TACZ_HUD_MODE_SCALE = 1.0;
   private static final int DEFAULT_TACZ_HUD_ICON_COLOR = -1;
   private static final int DEFAULT_TACZ_HUD_DIVIDER_COLOR = -1639681;
   private static final int DEFAULT_TACZ_HUD_AMMO_GUN_COLOR = -1;
   private static final int DEFAULT_TACZ_HUD_AMMO_INV_COLOR = -1094795572;
   private static final BooleanValue HUD_ENABLED = BUILDER.comment("Enable/disable nanosuit HUD rendering.").define("hud.enabled", true);
   private static final IntValue HUD_OFFSET_X = BUILDER.comment("Nanosuit HUD X offset.").defineInRange("hud.offsetX", -28, -2000, 2000);
   private static final IntValue HUD_OFFSET_Y = BUILDER.comment("Nanosuit HUD Y offset.").defineInRange("hud.offsetY", -8, -2000, 2000);
   private static final IntValue PERSPECTIVE_DEBUG_OFFSET_X = BUILDER.comment("Perspective debug percent window X offset relative to RightHud group center.")
      .defineInRange("hud.perspectiveDebug.offsetX", -56, -500, 500);
   private static final IntValue PERSPECTIVE_DEBUG_OFFSET_Y = BUILDER.comment("Perspective debug percent window Y offset relative to RightHud group center.")
      .defineInRange("hud.perspectiveDebug.offsetY", -8, -500, 500);
   private static final DoubleValue PERSPECTIVE_DEBUG_SCALE_X = BUILDER.comment("Perspective debug percent window scale on X.")
      .defineInRange("hud.perspectiveDebug.scaleX", 0.89533126F, 0.2, 4.0);
   private static final DoubleValue PERSPECTIVE_DEBUG_SCALE_Y = BUILDER.comment("Perspective debug percent window scale on Y.")
      .defineInRange("hud.perspectiveDebug.scaleY", 0.9F, 0.2, 4.0);
   private static final DoubleValue PERSPECTIVE_DEBUG_ROTATION = BUILDER.comment("Perspective debug percent window rotation in degrees.")
      .defineInRange("hud.perspectiveDebug.rotation", 0.0, -80.0, 80.0);
   private static final DoubleValue PERSPECTIVE_DEBUG_TILT = BUILDER.comment("Perspective debug percent window depth tilt factor.")
      .defineInRange("hud.perspectiveDebug.tilt", 0.8F, -3.0, 3.0);
   private static final DoubleValue PERSPECTIVE_DEBUG_VERTICAL_SKEW_DOWN = BUILDER.comment("Perspective debug percent window vertical skew down factor.")
      .defineInRange("hud.perspectiveDebug.verticalSkewDown", 0.16084337F, -1.2, 1.2);
   private static final DoubleValue PERSPECTIVE_DEBUG_TEXT_SIZE = BUILDER.comment("Perspective debug percent window text size.")
      .defineInRange("hud.perspectiveDebug.textSize", 1.1667922F, 0.5, 6.0);
   private static final DoubleValue PERSPECTIVE_DEBUG_LAYER_SPACING = BUILDER.comment("Perspective debug percent window depth layer spacing.")
      .defineInRange("hud.perspectiveDebug.layerSpacing", 0.0, 0.0, 6.0);
   private static final DoubleValue PERSPECTIVE_DEBUG_ENERGY_PERCENT_COLOR_R = BUILDER.comment("Perspective debug percent text color red channel.")
      .defineInRange("hud.perspectiveDebug.energyPercentColorR", 0.9019608F, 0.0, 1.0);
   private static final DoubleValue PERSPECTIVE_DEBUG_ENERGY_PERCENT_COLOR_G = BUILDER.comment("Perspective debug percent text color green channel.")
      .defineInRange("hud.perspectiveDebug.energyPercentColorG", 0.98039216F, 0.0, 1.0);
   private static final DoubleValue PERSPECTIVE_DEBUG_ENERGY_PERCENT_COLOR_B = BUILDER.comment("Perspective debug percent text color blue channel.")
      .defineInRange("hud.perspectiveDebug.energyPercentColorB", 1.0, 0.0, 1.0);
   private static final DoubleValue PERSPECTIVE_DEBUG_ENERGY_PERCENT_ALPHA = BUILDER.comment("Perspective debug percent text alpha.")
      .defineInRange("hud.perspectiveDebug.energyPercentAlpha", 1.0, 0.0, 1.0);
   private static final DoubleValue HUD_SCALE = BUILDER.comment("Nanosuit HUD scale.").defineInRange("hud.scale", 0.200000002980232, 0.05, 5.0);
   private static final DoubleValue CLASSIC_HUD_SCALE = BUILDER.comment("Nanosuit 1 HUD size relative to screen height; independent of GUI scale and Nanosuit 2.")
      .defineInRange("hud.classic.scale", 1.0, 0.5, 2.0);
   private static final ModConfigSpec.ConfigValue<String> CLASSIC_INTERFACE_COLOR = BUILDER
      .comment("Nanosuit 1 interface theme, #RRGGBB. Default preserves the original palette; backgrounds follow a darker shade. Does not change health green, energy blue, warnings or selection amber.")
      .define("hud.classic.color", "#719671", value -> value instanceof String s && s.matches("#[0-9a-fA-F]{6}"));
   private static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> CLASSIC_COLOR_PRESETS = BUILDER
      .comment("Named N1 theme presets, saved by the in-game color editor. Format: name|#RRGGBB.")
      .defineListAllowEmpty("hud.classic.presets", java.util.List.of(), value -> value instanceof String s && s.matches("[^|]{1,24}\\|#[0-9a-fA-F]{6}"));
   public static java.util.List<String> classicColorPresets() {
      try { return java.util.List.copyOf(CLASSIC_COLOR_PRESETS.get()); }
      catch(IllegalStateException exception) { return java.util.List.of(); }
   }
   public static void setClassicColorPresets(java.util.List<String> presets) {
      if(presets.size()>16 || presets.stream().anyMatch(s->!s.matches("[^|]{1,24}\\|#[0-9a-fA-F]{6}")))
         throw new IllegalArgumentException("Invalid N1 color presets");
      CLASSIC_COLOR_PRESETS.set(java.util.List.copyOf(presets));CLASSIC_COLOR_PRESETS.save();
   }
   public static void setClassicInterfaceColor(int rgb) {
      CLASSIC_INTERFACE_COLOR.set(String.format(java.util.Locale.ROOT,"#%06X",rgb&0xffffff));CLASSIC_INTERFACE_COLOR.save();
   }
   private static final BooleanValue CLASSIC_MINIMAP_ENABLED = BUILDER.comment("Show Crysis N1 minimap. Disable to choose an external minimap; external configs are never changed.")
      .define("hud.classic.minimapEnabled",true);
   private static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> CLASSIC_AMMO_ICONS = BUILDER
      .comment("Exact ammo IDs and HUD icon families: id|shell, id|rocket or id|grenade_round. Unknown ammo keeps the generic bullet icon.")
      .defineListAllowEmpty("hud.classic.ammoIcons",java.util.List.of("tacz:12g|shell","tacz:rpg_rocket|rocket","tacz:40mm|grenade_round"),
         value->value instanceof String s&&s.matches("[a-z0-9_.-]+:[a-z0-9_./-]+\\|(shell|rocket|grenade_round|bullet)"));
   private static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> CLASSIC_THROWABLE_ITEMS = BUILDER
      .comment("Optional exact item IDs for the N1 throwable row: id|frag, id|smoke, id|flash or id|incendiary. Empty by default; item tags are also supported.")
      .defineListAllowEmpty("hud.classic.throwableItems",java.util.List.of(),
         value->value instanceof String s&&s.matches("[a-z0-9_.-]+:[a-z0-9_./-]+\\|(frag|smoke|flash|incendiary)"));
   public static java.util.List<? extends String> classicAmmoIcons(){try{return CLASSIC_AMMO_ICONS.get();}catch(IllegalStateException e){return java.util.List.of();}}
   public static java.util.List<? extends String> classicThrowableItems(){try{return CLASSIC_THROWABLE_ITEMS.get();}catch(IllegalStateException e){return java.util.List.of();}}
   public static boolean classicMinimapEnabled(){return safeGetBoolean(CLASSIC_MINIMAP_ENABLED,true);}
    private static final BooleanValue CLASSIC_HIDE_DIORAMA = BUILDER.comment("Temporarily hide only Diorama 0.10.2 minimap layer when the N1 terrain is ready. Waypoints and full map are preserved.")
       .define("hud.classic.hideDioramaMinimap",true);
    public static boolean classicHideDiorama(){return safeGetBoolean(CLASSIC_HIDE_DIORAMA,true);}

   public static int classicInterfaceColor() {
      try {
         return io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.parse(CLASSIC_INTERFACE_COLOR.get());
      } catch (IllegalStateException exception) {
         return io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.DEFAULT;
      }
   }

   public static float classicHudScale() {
      return safeGetDouble(CLASSIC_HUD_SCALE, 1.0).floatValue();
   }
   private static final DoubleValue ARMOR_MODE_OVERLAY_SCALE = BUILDER.comment("Fullscreen armor-mode overlay scale multiplier.")
      .defineInRange("hud.armorModeOverlayScale", 1.04F, 0.25, 2.0);
   private static final BooleanValue ARMOR_OVERLAY_INTRO_ENABLED = BUILDER.comment("Enable elastic intro animation when armor mode turns on.")
      .define("hud.armorOverlayIntro.enabled", true);
   private static final IntValue ARMOR_OVERLAY_INTRO_DURATION_TICKS = BUILDER.comment("Armor overlay intro animation duration in ticks.")
      .defineInRange("hud.armorOverlayIntro.durationTicks", 18, 4, 60);
   private static final DoubleValue ARMOR_OVERLAY_INTRO_ELASTIC_STRENGTH = BUILDER.comment("Armor overlay intro elastic blend strength.")
      .defineInRange("hud.armorOverlayIntro.elasticStrength", 0.35F, 0.0, 1.0);
   private static final IntValue ARMOR_OVERLAY_INTRO_SEGMENT_COUNT = BUILDER.comment("Armor overlay intro segment count.")
      .defineInRange("hud.armorOverlayIntro.segmentCount", 48, 8, 128);
   private static final DoubleValue ARMOR_OVERLAY_INTRO_OVERSHOOT = BUILDER.comment("Armor overlay intro overshoot amount for back easing.")
      .defineInRange("hud.armorOverlayIntro.overshoot", 0.08F, 0.0, 0.3);
   private static final IntValue ARMOR_OVERLAY_INTRO_START_OUTSIDE_DISTANCE = BUILDER.comment(
         "Armor overlay intro start distance outside final position in pixels."
      )
      .defineInRange("hud.armorOverlayIntro.startOutsideDistance", 260, 0, 600);
   private static final DoubleValue ARMOR_OVERLAY_INTRO_CENTER_DELAY_BIAS = BUILDER.comment("Armor overlay intro center-to-edge delay bias.")
      .defineInRange("hud.armorOverlayIntro.centerDelayBias", 0.35F, 0.0, 1.0);
   private static final BooleanValue ARMOR_OVERLAY_INTRO_ALPHA_FADE = BUILDER.comment("Apply alpha fade during armor overlay intro animation.")
      .define("hud.armorOverlayIntro.alphaFade", true);
   private static final BooleanValue ARMOR_OVERLAY_INTRO_GLOW_PULSE = BUILDER.comment("Apply subtle glow pulse during armor overlay intro animation.")
      .define("hud.armorOverlayIntro.glowPulse", true);
   private static final BooleanValue ARMOR_OVERLAY_MOTION_BLUR_ENABLED = BUILDER.comment("Enable motion blur trail for armor overlay intro/outro animation.")
      .define("hud.armorOverlayIntro.motionBlurEnabled", true);
   private static final DoubleValue ARMOR_OVERLAY_MOTION_BLUR_STRENGTH = BUILDER.comment("Motion blur strength for armor overlay intro/outro animation.")
      .defineInRange("hud.armorOverlayIntro.motionBlurStrength", 0.35F, 0.0, 1.0);
   private static final IntValue ARMOR_OVERLAY_MOTION_BLUR_SAMPLES = BUILDER.comment("Motion blur sample count for armor overlay intro/outro animation.")
      .defineInRange("hud.armorOverlayIntro.motionBlurSamples", 3, 1, 8);
   private static final BooleanValue ARMOR_OVERLAY_INTRO_DEBUG = BUILDER.comment("Show armor overlay intro debug text.")
      .define("hud.armorOverlayIntro.debug", false);
   private static final DoubleValue OFF_LAYER_ALPHA = BUILDER.comment("Nanosuit HUD off layer alpha.").defineInRange("hud.offLayerAlpha", 1.0, 0.0, 1.0);
   private static final BooleanValue TEXTURE_SMOOTH = BUILDER.comment("Use linear filtering for HUD textures.").define("hud.textureSmooth", false);
   private static final BooleanValue TEXTURE_MIPMAP = BUILDER.comment("Use mipmaps for HUD textures.").define("hud.textureMipmap", false);
   private static final BooleanValue INERTIA_ENABLED = BUILDER.comment("Enable visor-like HUD inertia.").define("hud.inertia.enabled", true);
   private static final DoubleValue INERTIA_INTENSITY = BUILDER.comment("Global inertia intensity multiplier.")
      .defineInRange("hud.inertia.intensity", 1.0, 0.0, 3.0);
   private static final DoubleValue YAW_INERTIA_STRENGTH = BUILDER.comment("Camera yaw inertia strength (opposite screen-space drift).")
      .defineInRange("hud.inertia.yawStrength", 0.015, 0.0, 0.2);
   private static final DoubleValue PITCH_INERTIA_STRENGTH = BUILDER.comment("Camera pitch inertia strength (opposite screen-space drift).")
      .defineInRange("hud.inertia.pitchStrength", 0.01, 0.0, 0.2);
   private static final DoubleValue MAX_VIEW_OFFSET = BUILDER.comment("Maximum absolute inertia offset in pixels.")
      .defineInRange("hud.inertia.maxOffset", 6.0, 0.0, 40.0);
   private static final DoubleValue MAX_ROLL_DEGREES = BUILDER.comment("Maximum visor roll in degrees.").defineInRange("hud.inertia.maxRoll", 1.2, 0.0, 10.0);
   private static final DoubleValue DAMPING_STRENGTH = BUILDER.comment("Active damping strength (larger = snappier).")
      .defineInRange("hud.inertia.damping", 12.0, 0.1, 60.0);
   private static final DoubleValue RETURN_SPEED = BUILDER.comment("Return-to-neutral speed when input fades.")
      .defineInRange("hud.inertia.returnSpeed", 8.0, 0.1, 60.0);
   private static final DoubleValue WALK_SIDE_AMPLITUDE = BUILDER.comment("Step-driven walk sway amplitude on X.")
      .defineInRange("hud.inertia.walkSideAmplitude", 2.4, 0.0, 16.0);
   private static final DoubleValue WALK_VERTICAL_AMPLITUDE = BUILDER.comment("Step-driven walk arc amplitude on Y.")
      .defineInRange("hud.inertia.walkVerticalAmplitude", 2.8, 0.0, 16.0);
   private static final DoubleValue WALK_ROLL_AMPLITUDE = BUILDER.comment("Step-driven roll amplitude in degrees.")
      .defineInRange("hud.inertia.walkRollAmplitude", 0.95, 0.0, 8.0);
   private static final DoubleValue WALK_STEP_DURATION = BUILDER.comment("Base duration of one walk step in seconds.")
      .defineInRange("hud.inertia.walkStepDuration", 0.34, 0.12, 1.0);
   private static final DoubleValue WALK_SPRING_STRENGTH = BUILDER.comment("Spring strength for step target following.")
      .defineInRange("hud.inertia.walkSpringStrength", 28.0, 1.0, 80.0);
   private static final DoubleValue WALK_DAMPING = BUILDER.comment("Damping for step spring motion.").defineInRange("hud.inertia.walkDamping", 11.5, 0.1, 60.0);
   private static final DoubleValue WALK_RETURN_TO_CENTER_SPEED = BUILDER.comment("Speed to settle walk sway back to center.")
      .defineInRange("hud.inertia.walkReturnToCenterSpeed", 8.5, 0.1, 60.0);
   private static final DoubleValue SPRINT_WALK_MULTIPLIER = BUILDER.comment("Walk sway multiplier while sprinting.")
      .defineInRange("hud.inertia.sprintWalkMultiplier", 1.3, 1.0, 3.0);
   private static final DoubleValue MAX_WALK_OFFSET_X = BUILDER.comment("Maximum walk-driven X offset in pixels.")
      .defineInRange("hud.inertia.maxWalkOffsetX", 55.377357F, 0.0, 80.0);
   private static final DoubleValue MAX_WALK_OFFSET_Y = BUILDER.comment("Maximum walk-driven Y offset in pixels.")
      .defineInRange("hud.inertia.maxWalkOffsetY", 64.245285F, 0.0, 80.0);
   private static final DoubleValue MAX_WALK_ROLL = BUILDER.comment("Maximum walk-driven roll in degrees.")
      .defineInRange("hud.inertia.maxWalkRoll", 2.0, 0.0, 8.0);
   private static final BooleanValue NANOSUIT_FIRST_PERSON_ARMS_ENABLED = BUILDER.comment("Enable nanosuit first-person armor arms renderer.")
      .define("render.firstPersonArms.enabled", true);
   private static final BooleanValue NANOSUIT_FIRST_PERSON_ARMS_USE_NANOGEO = BUILDER.comment(
         "Allow NanoGeo first-person arm renderer for NanoGeo nanosuit armor."
      )
      .define("render.firstPersonArms.useNanoGeo", true);
   private static final BooleanValue DEBUG_ENERGY_DRAIN_INFO = BUILDER.comment("Show on-screen nanosuit energy drain debug information.")
      .define("debug.energyDrainInfo", false);
   private static final BooleanValue HUD_MASTER_COMPOSITOR_ENABLED = BUILDER.comment(
         "Enable unified master HUD compositor for nanosuit/TaCZ/nanovisor/minimap overlays."
      )
      .define("hud.masterCompositor.enabled", true);
   private static final BooleanValue HUD_MASTER_COMPOSITOR_ROUTE_NANOSUIT_HUD = BUILDER.comment("Route nanosuit right HUD through the master compositor.")
      .define("hud.masterCompositor.routeNanosuitHud", true);
   private static final BooleanValue HUD_MASTER_COMPOSITOR_ROUTE_TACZ_HUD = BUILDER.comment("Route TaCZ HUD replacement through the master compositor.")
      .define("hud.masterCompositor.routeTaczHud", true);
   private static final BooleanValue HUD_MASTER_COMPOSITOR_ROUTE_NANOVISOR_HUD = BUILDER.comment("Route NanoVisor text HUD through the master compositor.")
      .define("hud.masterCompositor.routeNanoVisorHud", true);
   private static final BooleanValue HUD_MASTER_COMPOSITOR_ROUTE_MINIMAP_HUD = BUILDER.comment("Route minimap HUD overlay through the master compositor.")
      .define("hud.masterCompositor.routeMinimapHud", true);
   private static final BooleanValue TACZ_HUD_OVERRIDE_ENABLED = BUILDER.comment(
         "Enable custom nanosuit TaCZ HUD integration when full nanosuit set is equipped."
      )
      .define("hud.taczOverlay.enabled", true);
   private static final BooleanValue TACZ_HIDE_ORIGINAL_HUD = BUILDER.comment("Hide original TaCZ overlay layers when nanosuit TaCZ HUD integration is active.")
      .define("hud.taczOverlay.hideOriginalHud", true);
   private static final IntValue TACZ_HUD_GROUP_OFFSET_X = BUILDER.comment("TaCZ HUD group X offset relative to nanosuit RightHud center.")
      .defineInRange("hud.taczOverlay.groupOffsetX", -42, -2000, 2000);
   private static final IntValue TACZ_HUD_GROUP_OFFSET_Y = BUILDER.comment("TaCZ HUD group Y offset relative to nanosuit RightHud center.")
      .defineInRange("hud.taczOverlay.groupOffsetY", -30, -2000, 2000);
   private static final DoubleValue TACZ_HUD_GROUP_SCALE_X = BUILDER.comment("TaCZ HUD group scale X.")
      .defineInRange("hud.taczOverlay.groupScaleX", 0.82F, 0.1, 4.0);
   private static final DoubleValue TACZ_HUD_GROUP_SCALE_Y = BUILDER.comment("TaCZ HUD group scale Y.")
      .defineInRange("hud.taczOverlay.groupScaleY", 0.82F, 0.1, 4.0);
   private static final DoubleValue TACZ_HUD_GROUP_ROTATION = BUILDER.comment("TaCZ HUD group rotation in degrees.")
      .defineInRange("hud.taczOverlay.groupRotation", 0.0, -90.0, 90.0);
   private static final DoubleValue TACZ_HUD_GROUP_VERTICAL_SKEW_DOWN = BUILDER.comment("TaCZ HUD group vertical skew down factor.")
      .defineInRange("hud.taczOverlay.groupVerticalSkewDown", 0.17F, -1.2, 1.2);
   private static final IntValue TACZ_HUD_ICON_OFFSET_X = BUILDER.comment("TaCZ HUD weapon icon X offset inside group.")
      .defineInRange("hud.taczOverlay.iconOffsetX", 0, -100, 100);
   private static final IntValue TACZ_HUD_ICON_OFFSET_Y = BUILDER.comment("TaCZ HUD weapon icon Y offset inside group.")
      .defineInRange("hud.taczOverlay.iconOffsetY", 1, -100, 100);
   private static final DoubleValue TACZ_HUD_ICON_SCALE = BUILDER.comment("TaCZ HUD weapon icon scale.")
      .defineInRange("hud.taczOverlay.iconScale", 1.0, 0.2, 5.0);
   private static final IntValue TACZ_HUD_AMMO_GUN_OFFSET_X = BUILDER.comment("TaCZ HUD in-mag ammo block X offset inside group.")
      .defineInRange("hud.taczOverlay.ammoGunOffsetX", 47, -100, 100);
   private static final IntValue TACZ_HUD_AMMO_GUN_OFFSET_Y = BUILDER.comment("TaCZ HUD in-mag ammo block Y offset inside group.")
      .defineInRange("hud.taczOverlay.ammoGunOffsetY", 1, -100, 100);
   private static final DoubleValue TACZ_HUD_AMMO_GUN_SCALE = BUILDER.comment("TaCZ HUD in-mag ammo block scale.")
      .defineInRange("hud.taczOverlay.ammoGunScale", 1.69F, 0.2, 5.0);
   private static final IntValue TACZ_HUD_AMMO_INV_OFFSET_X = BUILDER.comment("TaCZ HUD reserve ammo block X offset inside group.")
      .defineInRange("hud.taczOverlay.ammoInvOffsetX", 79, -100, 100);
   private static final IntValue TACZ_HUD_AMMO_INV_OFFSET_Y = BUILDER.comment("TaCZ HUD reserve ammo block Y offset inside group.")
      .defineInRange("hud.taczOverlay.ammoInvOffsetY", 1, -100, 100);
   private static final DoubleValue TACZ_HUD_AMMO_INV_SCALE = BUILDER.comment("TaCZ HUD reserve ammo block scale.")
      .defineInRange("hud.taczOverlay.ammoInvScale", 1.0, 0.2, 5.0);
   private static final IntValue TACZ_HUD_MODE_OFFSET_X = BUILDER.comment("TaCZ HUD fire mode block X offset inside group.")
      .defineInRange("hud.taczOverlay.modeOffsetX", 0, -100, 100);
   private static final IntValue TACZ_HUD_MODE_OFFSET_Y = BUILDER.comment("TaCZ HUD fire mode block Y offset inside group.")
      .defineInRange("hud.taczOverlay.modeOffsetY", 32, -100, 100);
   private static final DoubleValue TACZ_HUD_MODE_SCALE = BUILDER.comment("TaCZ HUD fire mode block scale.")
      .defineInRange("hud.taczOverlay.modeScale", 1.0, 0.2, 5.0);
   private static final IntValue TACZ_HUD_ICON_COLOR = BUILDER.comment("TaCZ HUD weapon icon tint color (ARGB).")
      .defineInRange("hud.taczOverlay.iconColor", -1, Integer.MIN_VALUE, Integer.MAX_VALUE);
   private static final IntValue TACZ_HUD_DIVIDER_COLOR = BUILDER.comment("TaCZ HUD divider color (ARGB).")
      .defineInRange("hud.taczOverlay.dividerColor", -1639681, Integer.MIN_VALUE, Integer.MAX_VALUE);
   private static final IntValue TACZ_HUD_AMMO_GUN_COLOR = BUILDER.comment("TaCZ HUD in-mag ammo text color (ARGB).")
      .defineInRange("hud.taczOverlay.ammoGunColor", -1, Integer.MIN_VALUE, Integer.MAX_VALUE);
   private static final IntValue TACZ_HUD_AMMO_INV_COLOR = BUILDER.comment("TaCZ HUD reserve ammo text color (ARGB).")
      .defineInRange("hud.taczOverlay.ammoInvColor", -1094795572, Integer.MIN_VALUE, Integer.MAX_VALUE);
   static final DoubleValue SOUND_AMBIENCE = volume("ambience");
   static final DoubleValue SOUND_MODES = volume("mode_transitions");
   static final DoubleValue SOUND_ENERGY = volume("energy");
   static final DoubleValue SOUND_VISOR = volume("visor");
   static final DoubleValue SOUND_MOVEMENT = volume("movement");
   static final DoubleValue SOUND_MELEE = volume("melee");
   public static final ModConfigSpec SPEC = BUILDER.build();

   private static DoubleValue volume(String category) {
      return BUILDER.defineInRange("suit_audio." + category, 1.0, 0.0, 1.0);
   }

   private NanosuitClientConfig() {
   }

   public static boolean hudEnabled() {
      return safeGetBoolean(HUD_ENABLED, true);
   }

   public static int hudOffsetX() {
      return safeGetInt(HUD_OFFSET_X, -28);
   }

   public static int hudOffsetY() {
      return safeGetInt(HUD_OFFSET_Y, -8);
   }

   public static int perspectiveDebugOffsetX() {
      return safeGetInt(PERSPECTIVE_DEBUG_OFFSET_X, -56);
   }

   public static int perspectiveDebugOffsetY() {
      return safeGetInt(PERSPECTIVE_DEBUG_OFFSET_Y, -8);
   }

   public static float perspectiveDebugScaleX() {
      return safeGetDouble(PERSPECTIVE_DEBUG_SCALE_X, 0.89533126F).floatValue();
   }

   public static float perspectiveDebugScaleY() {
      return safeGetDouble(PERSPECTIVE_DEBUG_SCALE_Y, 0.9F).floatValue();
   }

   public static float perspectiveDebugRotation() {
      return safeGetDouble(PERSPECTIVE_DEBUG_ROTATION, 0.0).floatValue();
   }

   public static float perspectiveDebugTilt() {
      return safeGetDouble(PERSPECTIVE_DEBUG_TILT, 0.8F).floatValue();
   }

   public static float perspectiveDebugVerticalSkewDown() {
      return safeGetDouble(PERSPECTIVE_DEBUG_VERTICAL_SKEW_DOWN, 0.16084337F).floatValue();
   }

   public static float perspectiveDebugTextSize() {
      return safeGetDouble(PERSPECTIVE_DEBUG_TEXT_SIZE, 1.1667922F).floatValue();
   }

   public static float perspectiveDebugLayerSpacing() {
      return safeGetDouble(PERSPECTIVE_DEBUG_LAYER_SPACING, 0.0).floatValue();
   }

   public static float perspectiveDebugEnergyPercentColorR() {
      return safeGetDouble(PERSPECTIVE_DEBUG_ENERGY_PERCENT_COLOR_R, 0.9019608F).floatValue();
   }

   public static float perspectiveDebugEnergyPercentColorG() {
      return safeGetDouble(PERSPECTIVE_DEBUG_ENERGY_PERCENT_COLOR_G, 0.98039216F).floatValue();
   }

   public static float perspectiveDebugEnergyPercentColorB() {
      return safeGetDouble(PERSPECTIVE_DEBUG_ENERGY_PERCENT_COLOR_B, 1.0).floatValue();
   }

   public static float perspectiveDebugEnergyPercentAlpha() {
      return safeGetDouble(PERSPECTIVE_DEBUG_ENERGY_PERCENT_ALPHA, 1.0).floatValue();
   }

   public static double defaultPerspectiveDebugEnergyPercentColorR() {
      return 0.9019608F;
   }

   public static double defaultPerspectiveDebugEnergyPercentColorG() {
      return 0.98039216F;
   }

   public static double defaultPerspectiveDebugEnergyPercentColorB() {
      return 1.0;
   }

   public static double defaultPerspectiveDebugEnergyPercentAlpha() {
      return 1.0;
   }

   public static float hudScale() {
      return safeGetDouble(HUD_SCALE, 0.200000002980232).floatValue();
   }

   public static float armorModeOverlayScale() {
      return safeGetDouble(ARMOR_MODE_OVERLAY_SCALE, 1.04F).floatValue();
   }

   public static boolean armorOverlayIntroEnabled() {
      return safeGetBoolean(ARMOR_OVERLAY_INTRO_ENABLED, true);
   }

   public static int armorOverlayIntroDurationTicks() {
      return safeGetInt(ARMOR_OVERLAY_INTRO_DURATION_TICKS, 18);
   }

   public static float armorOverlayIntroElasticStrength() {
      return safeGetDouble(ARMOR_OVERLAY_INTRO_ELASTIC_STRENGTH, 0.35F).floatValue();
   }

   public static int armorOverlayIntroSegmentCount() {
      return safeGetInt(ARMOR_OVERLAY_INTRO_SEGMENT_COUNT, 48);
   }

   public static float armorOverlayIntroOvershoot() {
      return safeGetDouble(ARMOR_OVERLAY_INTRO_OVERSHOOT, 0.08F).floatValue();
   }

   public static int armorOverlayIntroStartOutsideDistance() {
      return safeGetInt(ARMOR_OVERLAY_INTRO_START_OUTSIDE_DISTANCE, 260);
   }

   public static float armorOverlayIntroCenterDelayBias() {
      return safeGetDouble(ARMOR_OVERLAY_INTRO_CENTER_DELAY_BIAS, 0.35F).floatValue();
   }

   public static boolean armorOverlayIntroAlphaFade() {
      return safeGetBoolean(ARMOR_OVERLAY_INTRO_ALPHA_FADE, true);
   }

   public static boolean armorOverlayIntroGlowPulse() {
      return safeGetBoolean(ARMOR_OVERLAY_INTRO_GLOW_PULSE, true);
   }

   public static boolean armorOverlayMotionBlurEnabled() {
      return safeGetBoolean(ARMOR_OVERLAY_MOTION_BLUR_ENABLED, true);
   }

   public static float armorOverlayMotionBlurStrength() {
      return safeGetDouble(ARMOR_OVERLAY_MOTION_BLUR_STRENGTH, 0.35F).floatValue();
   }

   public static int armorOverlayMotionBlurSamples() {
      return safeGetInt(ARMOR_OVERLAY_MOTION_BLUR_SAMPLES, 3);
   }

   public static boolean armorOverlayIntroDebug() {
      return safeGetBoolean(ARMOR_OVERLAY_INTRO_DEBUG, false);
   }

   public static float offLayerAlpha() {
      return safeGetDouble(OFF_LAYER_ALPHA, 1.0).floatValue();
   }

   public static boolean textureSmooth() {
      return safeGetBoolean(TEXTURE_SMOOTH, false);
   }

   public static boolean textureMipmap() {
      return safeGetBoolean(TEXTURE_MIPMAP, false);
   }

   public static boolean inertiaEnabled() {
      return safeGetBoolean(INERTIA_ENABLED, true);
   }

   public static float inertiaIntensity() {
      return safeGetDouble(INERTIA_INTENSITY, 1.0).floatValue();
   }

   public static float yawInertiaStrength() {
      return safeGetDouble(YAW_INERTIA_STRENGTH, 0.015).floatValue();
   }

   public static float pitchInertiaStrength() {
      return safeGetDouble(PITCH_INERTIA_STRENGTH, 0.01).floatValue();
   }

   public static float maxViewOffset() {
      return safeGetDouble(MAX_VIEW_OFFSET, 6.0).floatValue();
   }

   public static float maxRollDegrees() {
      return safeGetDouble(MAX_ROLL_DEGREES, 1.2).floatValue();
   }

   public static float dampingStrength() {
      return safeGetDouble(DAMPING_STRENGTH, 12.0).floatValue();
   }

   public static float returnSpeed() {
      return safeGetDouble(RETURN_SPEED, 8.0).floatValue();
   }

   public static float walkSideAmplitude() {
      return safeGetDouble(WALK_SIDE_AMPLITUDE, 2.4).floatValue();
   }

   public static float walkVerticalAmplitude() {
      return safeGetDouble(WALK_VERTICAL_AMPLITUDE, 2.8).floatValue();
   }

   public static float walkRollAmplitude() {
      return safeGetDouble(WALK_ROLL_AMPLITUDE, 0.95).floatValue();
   }

   public static float walkStepDuration() {
      return safeGetDouble(WALK_STEP_DURATION, 0.34).floatValue();
   }

   public static float walkSpringStrength() {
      return safeGetDouble(WALK_SPRING_STRENGTH, 28.0).floatValue();
   }

   public static float walkDamping() {
      return safeGetDouble(WALK_DAMPING, 11.5).floatValue();
   }

   public static float walkReturnToCenterSpeed() {
      return safeGetDouble(WALK_RETURN_TO_CENTER_SPEED, 8.5).floatValue();
   }

   public static float sprintWalkMultiplier() {
      return safeGetDouble(SPRINT_WALK_MULTIPLIER, 1.3).floatValue();
   }

   public static float maxWalkOffsetX() {
      return safeGetDouble(MAX_WALK_OFFSET_X, 55.377357F).floatValue();
   }

   public static float maxWalkOffsetY() {
      return safeGetDouble(MAX_WALK_OFFSET_Y, 64.245285F).floatValue();
   }

   public static float maxWalkRoll() {
      return safeGetDouble(MAX_WALK_ROLL, 2.0).floatValue();
   }

   public static boolean nanosuitFirstPersonArmsEnabled() {
      return safeGetBoolean(NANOSUIT_FIRST_PERSON_ARMS_ENABLED, true);
   }

   public static boolean defaultNanosuitFirstPersonArmsEnabled() {
      return true;
   }

   public static boolean nanosuitFirstPersonArmsUseNanoGeo() {
      return safeGetBoolean(NANOSUIT_FIRST_PERSON_ARMS_USE_NANOGEO, true);
   }

   public static boolean defaultNanosuitFirstPersonArmsUseNanoGeo() {
      return true;
   }

   public static boolean debugEnergyDrainInfo() {
      return safeGetBoolean(DEBUG_ENERGY_DRAIN_INFO, false);
   }

   public static boolean hudMasterCompositorEnabled() {
      return safeGetBoolean(HUD_MASTER_COMPOSITOR_ENABLED, true);
   }

   public static boolean defaultHudMasterCompositorEnabled() {
      return true;
   }

   public static boolean hudMasterCompositorRouteNanosuitHud() {
      return safeGetBoolean(HUD_MASTER_COMPOSITOR_ROUTE_NANOSUIT_HUD, true);
   }

   public static boolean defaultHudMasterCompositorRouteNanosuitHud() {
      return true;
   }

   public static boolean hudMasterCompositorRouteTaczHud() {
      return safeGetBoolean(HUD_MASTER_COMPOSITOR_ROUTE_TACZ_HUD, true);
   }

   public static boolean defaultHudMasterCompositorRouteTaczHud() {
      return true;
   }

   public static boolean hudMasterCompositorRouteNanoVisorHud() {
      return safeGetBoolean(HUD_MASTER_COMPOSITOR_ROUTE_NANOVISOR_HUD, true);
   }

   public static boolean defaultHudMasterCompositorRouteNanoVisorHud() {
      return true;
   }

   public static boolean hudMasterCompositorRouteMinimapHud() {
      return safeGetBoolean(HUD_MASTER_COMPOSITOR_ROUTE_MINIMAP_HUD, true);
   }

   public static boolean defaultHudMasterCompositorRouteMinimapHud() {
      return true;
   }

   public static boolean taczHudOverrideEnabled() {
      return safeGetBoolean(TACZ_HUD_OVERRIDE_ENABLED, true);
   }

   public static boolean defaultTaczHudOverrideEnabled() {
      return true;
   }

   public static boolean taczHideOriginalHud() {
      return safeGetBoolean(TACZ_HIDE_ORIGINAL_HUD, true);
   }

   public static boolean defaultTaczHideOriginalHud() {
      return true;
   }

   public static int taczHudGroupOffsetX() {
      return safeGetInt(TACZ_HUD_GROUP_OFFSET_X, -42);
   }

   public static int defaultTaczHudGroupOffsetX() {
      return -42;
   }

   public static int taczHudGroupOffsetY() {
      return safeGetInt(TACZ_HUD_GROUP_OFFSET_Y, -30);
   }

   public static int defaultTaczHudGroupOffsetY() {
      return -30;
   }

   public static float taczHudGroupScaleX() {
      return safeGetDouble(TACZ_HUD_GROUP_SCALE_X, 0.82F).floatValue();
   }

   public static double defaultTaczHudGroupScaleX() {
      return 0.82F;
   }

   public static float taczHudGroupScaleY() {
      return safeGetDouble(TACZ_HUD_GROUP_SCALE_Y, 0.82F).floatValue();
   }

   public static double defaultTaczHudGroupScaleY() {
      return 0.82F;
   }

   public static float taczHudGroupRotation() {
      return safeGetDouble(TACZ_HUD_GROUP_ROTATION, 0.0).floatValue();
   }

   public static double defaultTaczHudGroupRotation() {
      return 0.0;
   }

   public static float taczHudGroupVerticalSkewDown() {
      return safeGetDouble(TACZ_HUD_GROUP_VERTICAL_SKEW_DOWN, 0.17F).floatValue();
   }

   public static double defaultTaczHudGroupVerticalSkewDown() {
      return 0.17F;
   }

   public static int taczHudIconOffsetX() {
      return safeGetInt(TACZ_HUD_ICON_OFFSET_X, 0);
   }

   public static int defaultTaczHudIconOffsetX() {
      return 0;
   }

   public static int taczHudIconOffsetY() {
      return safeGetInt(TACZ_HUD_ICON_OFFSET_Y, 1);
   }

   public static int defaultTaczHudIconOffsetY() {
      return 1;
   }

   public static float taczHudIconScale() {
      return safeGetDouble(TACZ_HUD_ICON_SCALE, 1.0).floatValue();
   }

   public static double defaultTaczHudIconScale() {
      return 1.0;
   }

   public static int taczHudAmmoGunOffsetX() {
      return safeGetInt(TACZ_HUD_AMMO_GUN_OFFSET_X, 47);
   }

   public static int defaultTaczHudAmmoGunOffsetX() {
      return 47;
   }

   public static int taczHudAmmoGunOffsetY() {
      return safeGetInt(TACZ_HUD_AMMO_GUN_OFFSET_Y, 1);
   }

   public static int defaultTaczHudAmmoGunOffsetY() {
      return 1;
   }

   public static float taczHudAmmoGunScale() {
      return safeGetDouble(TACZ_HUD_AMMO_GUN_SCALE, 1.69F).floatValue();
   }

   public static double defaultTaczHudAmmoGunScale() {
      return 1.69F;
   }

   public static int taczHudAmmoInvOffsetX() {
      return safeGetInt(TACZ_HUD_AMMO_INV_OFFSET_X, 79);
   }

   public static int defaultTaczHudAmmoInvOffsetX() {
      return 79;
   }

   public static int taczHudAmmoInvOffsetY() {
      return safeGetInt(TACZ_HUD_AMMO_INV_OFFSET_Y, 1);
   }

   public static int defaultTaczHudAmmoInvOffsetY() {
      return 1;
   }

   public static float taczHudAmmoInvScale() {
      return safeGetDouble(TACZ_HUD_AMMO_INV_SCALE, 1.0).floatValue();
   }

   public static double defaultTaczHudAmmoInvScale() {
      return 1.0;
   }

   public static int taczHudModeOffsetX() {
      return safeGetInt(TACZ_HUD_MODE_OFFSET_X, 0);
   }

   public static int defaultTaczHudModeOffsetX() {
      return 0;
   }

   public static int taczHudModeOffsetY() {
      return safeGetInt(TACZ_HUD_MODE_OFFSET_Y, 32);
   }

   public static int defaultTaczHudModeOffsetY() {
      return 32;
   }

   public static float taczHudModeScale() {
      return safeGetDouble(TACZ_HUD_MODE_SCALE, 1.0).floatValue();
   }

   public static double defaultTaczHudModeScale() {
      return 1.0;
   }

   public static int taczHudIconColor() {
      return safeGetInt(TACZ_HUD_ICON_COLOR, -1);
   }

   public static int defaultTaczHudIconColor() {
      return -1;
   }

   public static int taczHudDividerColor() {
      return safeGetInt(TACZ_HUD_DIVIDER_COLOR, -1639681);
   }

   public static int defaultTaczHudDividerColor() {
      return -1639681;
   }

   public static int taczHudAmmoGunColor() {
      return safeGetInt(TACZ_HUD_AMMO_GUN_COLOR, -1);
   }

   public static int defaultTaczHudAmmoGunColor() {
      return -1;
   }

   public static int taczHudAmmoInvColor() {
      return safeGetInt(TACZ_HUD_AMMO_INV_COLOR, -1094795572);
   }

   public static int defaultTaczHudAmmoInvColor() {
      return -1094795572;
   }

   public static void setHudEnabled(boolean value) {
      safeSetBoolean(HUD_ENABLED, value);
   }

   public static void setHudOffsetX(int value) {
      safeSetInt(HUD_OFFSET_X, value);
   }

   public static void setHudOffsetY(int value) {
      safeSetInt(HUD_OFFSET_Y, value);
   }

   public static void setPerspectiveDebugOffsetX(int value) {
      safeSetInt(PERSPECTIVE_DEBUG_OFFSET_X, value);
   }

   public static void setPerspectiveDebugOffsetY(int value) {
      safeSetInt(PERSPECTIVE_DEBUG_OFFSET_Y, value);
   }

   public static void setPerspectiveDebugScaleX(float value) {
      safeSetDouble(PERSPECTIVE_DEBUG_SCALE_X, value);
   }

   public static void setPerspectiveDebugScaleY(float value) {
      safeSetDouble(PERSPECTIVE_DEBUG_SCALE_Y, value);
   }

   public static void setPerspectiveDebugRotation(float value) {
      safeSetDouble(PERSPECTIVE_DEBUG_ROTATION, value);
   }

   public static void setPerspectiveDebugTilt(float value) {
      safeSetDouble(PERSPECTIVE_DEBUG_TILT, value);
   }

   public static void setPerspectiveDebugVerticalSkewDown(float value) {
      safeSetDouble(PERSPECTIVE_DEBUG_VERTICAL_SKEW_DOWN, value);
   }

   public static void setPerspectiveDebugTextSize(float value) {
      safeSetDouble(PERSPECTIVE_DEBUG_TEXT_SIZE, value);
   }

   public static void setPerspectiveDebugLayerSpacing(float value) {
      safeSetDouble(PERSPECTIVE_DEBUG_LAYER_SPACING, value);
   }

   public static void setPerspectiveDebugEnergyPercentColorR(float value) {
      safeSetDouble(PERSPECTIVE_DEBUG_ENERGY_PERCENT_COLOR_R, value);
   }

   public static void setPerspectiveDebugEnergyPercentColorG(float value) {
      safeSetDouble(PERSPECTIVE_DEBUG_ENERGY_PERCENT_COLOR_G, value);
   }

   public static void setPerspectiveDebugEnergyPercentColorB(float value) {
      safeSetDouble(PERSPECTIVE_DEBUG_ENERGY_PERCENT_COLOR_B, value);
   }

   public static void setPerspectiveDebugEnergyPercentAlpha(float value) {
      safeSetDouble(PERSPECTIVE_DEBUG_ENERGY_PERCENT_ALPHA, value);
   }

   public static void setHudScale(float value) {
      safeSetDouble(HUD_SCALE, value);
   }

   public static void setArmorModeOverlayScale(float value) {
      safeSetDouble(ARMOR_MODE_OVERLAY_SCALE, value);
   }

   public static void setArmorOverlayIntroEnabled(boolean value) {
      safeSetBoolean(ARMOR_OVERLAY_INTRO_ENABLED, value);
   }

   public static void setArmorOverlayIntroDurationTicks(int value) {
      safeSetInt(ARMOR_OVERLAY_INTRO_DURATION_TICKS, value);
   }

   public static void setArmorOverlayIntroElasticStrength(float value) {
      safeSetDouble(ARMOR_OVERLAY_INTRO_ELASTIC_STRENGTH, value);
   }

   public static void setArmorOverlayIntroSegmentCount(int value) {
      safeSetInt(ARMOR_OVERLAY_INTRO_SEGMENT_COUNT, value);
   }

   public static void setArmorOverlayIntroOvershoot(float value) {
      safeSetDouble(ARMOR_OVERLAY_INTRO_OVERSHOOT, value);
   }

   public static void setArmorOverlayIntroStartOutsideDistance(int value) {
      safeSetInt(ARMOR_OVERLAY_INTRO_START_OUTSIDE_DISTANCE, value);
   }

   public static void setArmorOverlayIntroCenterDelayBias(float value) {
      safeSetDouble(ARMOR_OVERLAY_INTRO_CENTER_DELAY_BIAS, value);
   }

   public static void setArmorOverlayIntroAlphaFade(boolean value) {
      safeSetBoolean(ARMOR_OVERLAY_INTRO_ALPHA_FADE, value);
   }

   public static void setArmorOverlayIntroGlowPulse(boolean value) {
      safeSetBoolean(ARMOR_OVERLAY_INTRO_GLOW_PULSE, value);
   }

   public static void setArmorOverlayMotionBlurEnabled(boolean value) {
      safeSetBoolean(ARMOR_OVERLAY_MOTION_BLUR_ENABLED, value);
   }

   public static void setArmorOverlayMotionBlurStrength(float value) {
      safeSetDouble(ARMOR_OVERLAY_MOTION_BLUR_STRENGTH, value);
   }

   public static void setArmorOverlayMotionBlurSamples(int value) {
      safeSetInt(ARMOR_OVERLAY_MOTION_BLUR_SAMPLES, value);
   }

   public static void setArmorOverlayIntroDebug(boolean value) {
      safeSetBoolean(ARMOR_OVERLAY_INTRO_DEBUG, value);
   }

   public static void setOffLayerAlpha(float value) {
      safeSetDouble(OFF_LAYER_ALPHA, value);
   }

   public static void setTextureSmooth(boolean value) {
      safeSetBoolean(TEXTURE_SMOOTH, value);
   }

   public static void setTextureMipmap(boolean value) {
      safeSetBoolean(TEXTURE_MIPMAP, value);
   }

   public static void setMaxWalkOffsetX(float value) {
      safeSetDouble(MAX_WALK_OFFSET_X, value);
   }

   public static void setMaxWalkOffsetY(float value) {
      safeSetDouble(MAX_WALK_OFFSET_Y, value);
   }

   public static void setNanosuitFirstPersonArmsEnabled(boolean value) {
      safeSetBoolean(NANOSUIT_FIRST_PERSON_ARMS_ENABLED, value);
   }

   public static void setNanosuitFirstPersonArmsUseNanoGeo(boolean value) {
      safeSetBoolean(NANOSUIT_FIRST_PERSON_ARMS_USE_NANOGEO, value);
   }

   public static void setDebugEnergyDrainInfo(boolean value) {
      safeSetBoolean(DEBUG_ENERGY_DRAIN_INFO, value);
   }

   public static void setHudMasterCompositorEnabled(boolean value) {
      safeSetBoolean(HUD_MASTER_COMPOSITOR_ENABLED, value);
   }

   public static void setHudMasterCompositorRouteNanosuitHud(boolean value) {
      safeSetBoolean(HUD_MASTER_COMPOSITOR_ROUTE_NANOSUIT_HUD, value);
   }

   public static void setHudMasterCompositorRouteTaczHud(boolean value) {
      safeSetBoolean(HUD_MASTER_COMPOSITOR_ROUTE_TACZ_HUD, value);
   }

   public static void setHudMasterCompositorRouteNanoVisorHud(boolean value) {
      safeSetBoolean(HUD_MASTER_COMPOSITOR_ROUTE_NANOVISOR_HUD, value);
   }

   public static void setHudMasterCompositorRouteMinimapHud(boolean value) {
      safeSetBoolean(HUD_MASTER_COMPOSITOR_ROUTE_MINIMAP_HUD, value);
   }

   public static void setTaczHudOverrideEnabled(boolean value) {
      safeSetBoolean(TACZ_HUD_OVERRIDE_ENABLED, value);
   }

   public static void setTaczHideOriginalHud(boolean value) {
      safeSetBoolean(TACZ_HIDE_ORIGINAL_HUD, value);
   }

   public static void setTaczHudGroupOffsetX(int value) {
      safeSetInt(TACZ_HUD_GROUP_OFFSET_X, value);
   }

   public static void setTaczHudGroupOffsetY(int value) {
      safeSetInt(TACZ_HUD_GROUP_OFFSET_Y, value);
   }

   public static void setTaczHudGroupScaleX(float value) {
      safeSetDouble(TACZ_HUD_GROUP_SCALE_X, value);
   }

   public static void setTaczHudGroupScaleY(float value) {
      safeSetDouble(TACZ_HUD_GROUP_SCALE_Y, value);
   }

   public static void setTaczHudGroupRotation(float value) {
      safeSetDouble(TACZ_HUD_GROUP_ROTATION, value);
   }

   public static void setTaczHudGroupVerticalSkewDown(float value) {
      safeSetDouble(TACZ_HUD_GROUP_VERTICAL_SKEW_DOWN, value);
   }

   public static void setTaczHudIconOffsetX(int value) {
      safeSetInt(TACZ_HUD_ICON_OFFSET_X, value);
   }

   public static void setTaczHudIconOffsetY(int value) {
      safeSetInt(TACZ_HUD_ICON_OFFSET_Y, value);
   }

   public static void setTaczHudIconScale(float value) {
      safeSetDouble(TACZ_HUD_ICON_SCALE, value);
   }

   public static void setTaczHudAmmoGunOffsetX(int value) {
      safeSetInt(TACZ_HUD_AMMO_GUN_OFFSET_X, value);
   }

   public static void setTaczHudAmmoGunOffsetY(int value) {
      safeSetInt(TACZ_HUD_AMMO_GUN_OFFSET_Y, value);
   }

   public static void setTaczHudAmmoGunScale(float value) {
      safeSetDouble(TACZ_HUD_AMMO_GUN_SCALE, value);
   }

   public static void setTaczHudAmmoInvOffsetX(int value) {
      safeSetInt(TACZ_HUD_AMMO_INV_OFFSET_X, value);
   }

   public static void setTaczHudAmmoInvOffsetY(int value) {
      safeSetInt(TACZ_HUD_AMMO_INV_OFFSET_Y, value);
   }

   public static void setTaczHudAmmoInvScale(float value) {
      safeSetDouble(TACZ_HUD_AMMO_INV_SCALE, value);
   }

   public static void setTaczHudModeOffsetX(int value) {
      safeSetInt(TACZ_HUD_MODE_OFFSET_X, value);
   }

   public static void setTaczHudModeOffsetY(int value) {
      safeSetInt(TACZ_HUD_MODE_OFFSET_Y, value);
   }

   public static void setTaczHudModeScale(float value) {
      safeSetDouble(TACZ_HUD_MODE_SCALE, value);
   }

   public static void setTaczHudIconColor(int value) {
      safeSetInt(TACZ_HUD_ICON_COLOR, value);
   }

   public static void setTaczHudDividerColor(int value) {
      safeSetInt(TACZ_HUD_DIVIDER_COLOR, value);
   }

   public static void setTaczHudAmmoGunColor(int value) {
      safeSetInt(TACZ_HUD_AMMO_GUN_COLOR, value);
   }

   public static void setTaczHudAmmoInvColor(int value) {
      safeSetInt(TACZ_HUD_AMMO_INV_COLOR, value);
   }

   private static Double safeGetDouble(DoubleValue value, double fallback) {
      try {
         return (Double)value.get();
      } catch (IllegalStateException var4) {
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

   private static boolean safeGetBoolean(BooleanValue value, boolean fallback) {
      try {
         return (Boolean)value.get();
      } catch (IllegalStateException var3) {
         return fallback;
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

   private static void safeSetBoolean(BooleanValue value, boolean target) {
      try {
         value.set(target);
         value.save();
      } catch (RuntimeException var3) {
      }
   }
}
