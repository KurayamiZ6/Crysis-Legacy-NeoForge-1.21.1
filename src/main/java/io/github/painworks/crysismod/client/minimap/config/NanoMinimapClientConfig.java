package io.github.painworks.crysismod.client.minimap.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class NanoMinimapClientConfig {
   private static final Builder BUILDER = new Builder();
   public static final boolean DEFAULT_MINIMAP_ENABLED = true;
   public static final int DEFAULT_MINIMAP_RANGE_BLOCKS = 150;
   public static final int DEFAULT_MINIMAP_WIDTH = 150;
   public static final int DEFAULT_MINIMAP_HEIGHT = 100;
   public static final int DEFAULT_MINIMAP_OFFSET_X = 40;
   public static final int DEFAULT_MINIMAP_OFFSET_Y = 16;
   public static final double DEFAULT_MINIMAP_OPACITY = 0.6;
   public static final double DEFAULT_MINIMAP_SCALE = 0.7;
   public static final boolean DEFAULT_MINIMAP_USE_HUD_INERTIA = true;
   public static final boolean DEFAULT_ROTATE_WITH_PLAYER = true;
   public static final boolean DEFAULT_TERRAIN_ENABLED = true;
   public static final MinimapTerrainQuality DEFAULT_MINIMAP_TERRAIN_QUALITY = MinimapTerrainQuality.HIGH;
   public static final boolean DEFAULT_MINIMAP_HEIGHT_SHADING_ENABLED = true;
   public static final MinimapHeightShadingMode DEFAULT_MINIMAP_HEIGHT_SHADING_MODE = MinimapHeightShadingMode.SIMPLE;
   public static final double DEFAULT_MINIMAP_HEIGHT_SHADING_STRENGTH = 0.5;
   public static final double DEFAULT_MINIMAP_TERRAIN_BRIGHTNESS = 1.0;
   public static final double DEFAULT_MINIMAP_TERRAIN_CONTRAST = 1.1;
   public static final double DEFAULT_MINIMAP_TERRAIN_SATURATION = 1.08;
   public static final boolean DEFAULT_MINIMAP_WATER_TINT_ENABLED = true;
   public static final boolean DEFAULT_MINIMAP_BIOME_TINT_ENABLED = true;
   public static final boolean DEFAULT_MINIMAP_USE_REGION_CACHE = true;
   public static final int DEFAULT_MINIMAP_TILE_SIZE = 16;
   public static final int DEFAULT_MINIMAP_MAX_CACHED_REGIONS = 64;
   public static final int DEFAULT_MINIMAP_TILE_UPDATES_PER_TICK = 12;
   public static final int DEFAULT_MINIMAP_MAX_QUEUED_TILE_UPDATES = 512;
   public static final int DEFAULT_MINIMAP_MISSING_TILE_RETRY_COOLDOWN_TICKS = 20;
   public static final boolean DEFAULT_MINIMAP_PRIORITIZE_CENTER_TILES = true;
   public static final int DEFAULT_MINIMAP_MAX_SAMPLES_PER_TICK = 8288;
   public static final int DEFAULT_TERRAIN_INTERNAL_RESOLUTION = 128;
   public static final int DEFAULT_TERRAIN_UPDATE_INTERVAL_TICKS = 1;
   public static final int DEFAULT_TERRAIN_ROWS_PER_TICK = 48;
   public static final int DEFAULT_MINIMAP_ROTATION_OVERSCAN_PADDING = 8;
   public static final boolean DEFAULT_TERRAIN_USE_LOADED_CHUNKS_ONLY = true;
   public static final boolean DEFAULT_MINIMAP_SHOW_PLAYER_MARKER = true;
   public static final boolean DEFAULT_MINIMAP_SHOW_TARGET_MARKER = true;
   public static final boolean DEFAULT_MINIMAP_SHOW_MARKED_ENTITIES = true;
   public static final boolean DEFAULT_MINIMAP_SHOW_NEARBY_ENTITIES = true;
   public static final int DEFAULT_MINIMAP_NEARBY_ENTITY_RANGE = 150;
   public static final int DEFAULT_MINIMAP_NEARBY_ENTITY_UPDATE_INTERVAL_TICKS = 20;
   public static final int DEFAULT_MINIMAP_MAX_NEARBY_ENTITY_MARKERS = 60;
   public static final int DEFAULT_MINIMAP_ENTITY_VERTICAL_LIMIT = 20;
   public static final int DEFAULT_MINIMAP_ENTITY_VERTICAL_FADE_START = 8;
   public static final double DEFAULT_MINIMAP_ENTITY_VERTICAL_FADE_STRENGTH = 0.75;
   public static final double DEFAULT_MINIMAP_ENTITY_UNDERGROUND_FADE = 0.3;
   public static final boolean DEFAULT_MINIMAP_TARGET_IGNORE_VERTICAL_LIMIT = true;
   public static final boolean DEFAULT_MINIMAP_MARKED_IGNORE_VERTICAL_LIMIT = true;
   public static final boolean DEFAULT_MINIMAP_SURFACE_AWARE_NEARBY = true;
   public static final boolean DEFAULT_MINIMAP_CLAMP_MARKERS_TO_BOUNDS = true;
   public static final double DEFAULT_MINIMAP_MARKER_SCALE = 1.0;
   public static final double DEFAULT_MINIMAP_MARKER_OPACITY = 1.0;
   public static final double DEFAULT_MINIMAP_MARKED_HIGHLIGHT_OPACITY = 1.0;
   public static final boolean DEFAULT_MINIMAP_USE_ENTITY_ICONS = true;
   public static final int DEFAULT_MINIMAP_ENTITY_ICON_SIZE = 8;
   public static final double DEFAULT_MINIMAP_ENTITY_ICON_OPACITY = 1.0;
   public static final boolean DEFAULT_MINIMAP_FALLBACK_ENTITY_MARKERS = true;
   public static final boolean DEFAULT_MINIMAP_FRAME_ENABLED = true;
   public static final double DEFAULT_MINIMAP_FRAME_OPACITY = 0.7;
   public static final boolean DEFAULT_MINIMAP_PERSPECTIVE_ENABLED = true;
   public static final double DEFAULT_MINIMAP_PERSPECTIVE_RIGHT_SIDE = -0.19;
   public static final double DEFAULT_MINIMAP_PERSPECTIVE_AXIS_X = 0.07;
   public static final double DEFAULT_MINIMAP_PERSPECTIVE_AXIS_Y = 0.0;
   public static final double DEFAULT_MINIMAP_EDGE_ARC_TOP_BOTTOM = -0.09;
   public static final boolean DEFAULT_MINIMAP_PERSPECTIVE_DEBUG_BOUNDS = false;
   public static final boolean DEFAULT_DEBUG_MINIMAP_BOUNDS = false;
   public static final boolean DEFAULT_DEBUG_MINIMAP_PERFORMANCE = false;
   public static final boolean DEFAULT_DEBUG_MINIMAP_CACHE = false;
   public static final boolean DEFAULT_DEBUG_MINIMAP_MARKERS = false;
   public static final boolean DEFAULT_DEBUG_FORCE_TERRAIN_REFRESH = false;
   public static final boolean DEFAULT_DEBUG_FORCE_SOLID_TERRAIN = false;
   public static final boolean DEFAULT_DEBUG_FORCE_CHECKERBOARD_TERRAIN = false;
   public static final boolean DEFAULT_DEBUG_DISABLE_REGION_CACHE = false;
   public static final boolean DEFAULT_DEBUG_DISABLE_TILE_QUEUE = false;
   public static final boolean DEFAULT_DEBUG_SHOW_ONLY_FRONT_TEXTURE = false;
   public static final boolean DEFAULT_DEBUG_SHOW_TERRAIN_ONLY = false;
   public static final boolean DEFAULT_DEBUG_DISABLE_MINIMAP_MASK = false;
   public static final boolean DEFAULT_DEBUG_DRAW_MINIMAP_MASK_BOUNDS = false;
   public static final boolean DEFAULT_DEBUG_FORCE_QUEUE_VISIBLE_TILES = false;
   public static final boolean DEFAULT_DEBUG_FORCE_RESAMPLE_VISIBLE_TILES = false;
   public static final boolean DEFAULT_DEBUG_MINIMAP_RENDER_TRACE = false;
   public static final boolean DEFAULT_DEBUG_FREEZE_HUD_INERTIA = false;
   public static final boolean DEFAULT_DEBUG_FREEZE_MAP_WORLD_MOVEMENT = false;
   public static final boolean DEFAULT_DEBUG_FREEZE_TERRAIN_SOURCE_OFFSET = false;
   public static final boolean DEFAULT_DEBUG_DISABLE_POSE_STACK_IN_MINIMAP = false;
   public static final boolean DEFAULT_DEBUG_FORCE_MANUAL_VIEWPORT_COORDINATES = false;
   public static final boolean DEFAULT_DEBUG_DRAW_TERRAIN_BOUNDS = false;
   public static final boolean DEFAULT_DEBUG_DRAW_FRAME_BOUNDS = false;
   public static final boolean DEFAULT_DEBUG_DRAW_MARKER_BOUNDS = false;
   public static final boolean DEFAULT_DEBUG_DRAW_SCISSOR_BOUNDS = false;
   public static final boolean DEFAULT_DEBUG_LOG_MINIMAP_TRACE_ONCE_PER_SECOND = false;
   public static final boolean DEFAULT_DEBUG_MINIMAP_COLORS = false;
   private static final BooleanValue MINIMAP_ENABLED = BUILDER.comment("Master switch for the custom nanosuit minimap.").define("minimapEnabled", true);
   private static final IntValue MINIMAP_RANGE_BLOCKS = BUILDER.comment("Half-range in world blocks represented by minimap.")
      .defineInRange("minimapRangeBlocks", 150, 32, 2048);
   private static final IntValue MINIMAP_WIDTH = BUILDER.comment("Minimap viewport width in UI pixels before scale.")
      .defineInRange("minimapWidth", 150, 64, 512);
   private static final IntValue MINIMAP_HEIGHT = BUILDER.comment("Minimap viewport height in UI pixels before scale.")
      .defineInRange("minimapHeight", 100, 64, 512);
   private static final IntValue MINIMAP_OFFSET_X = BUILDER.comment("Minimap offset X from bottom-left anchor.")
      .defineInRange("minimapOffsetX", 40, -3000, 3000);
   private static final IntValue MINIMAP_OFFSET_Y = BUILDER.comment("Minimap offset Y from bottom-left anchor.")
      .defineInRange("minimapOffsetY", 16, -3000, 3000);
   private static final DoubleValue MINIMAP_OPACITY = BUILDER.comment("Minimap panel opacity.").defineInRange("minimapOpacity", 0.6, 0.05, 1.0);
   private static final DoubleValue MINIMAP_SCALE = BUILDER.comment("Minimap scale factor.").defineInRange("minimapScale", 0.7, 0.25, 4.0);
   private static final BooleanValue MINIMAP_USE_HUD_INERTIA = BUILDER.comment("Apply nanosuit HUD inertia offset to minimap anchor.")
      .define("minimapUseHudInertia", true);
   private static final BooleanValue ROTATE_WITH_PLAYER = BUILDER.comment("Heading-up mode: rotate map content by player yaw.")
      .define("rotateWithPlayer", true);
   private static final BooleanValue TERRAIN_ENABLED = BUILDER.comment("Enable terrain sampling layer.").define("terrainEnabled", true);
   private static final EnumValue<MinimapTerrainQuality> MINIMAP_TERRAIN_QUALITY = BUILDER.comment("Terrain color pipeline quality preset.")
      .defineEnum("minimapTerrainQuality", DEFAULT_MINIMAP_TERRAIN_QUALITY);
   private static final BooleanValue MINIMAP_HEIGHT_SHADING_ENABLED = BUILDER.comment("Enable terrain height shading in sampling pipeline.")
      .define("minimapHeightShadingEnabled", true);
   private static final EnumValue<MinimapHeightShadingMode> MINIMAP_HEIGHT_SHADING_MODE = BUILDER.comment("Height shading mode for minimap terrain.")
      .defineEnum("minimapHeightShadingMode", DEFAULT_MINIMAP_HEIGHT_SHADING_MODE);
   private static final DoubleValue MINIMAP_HEIGHT_SHADING_STRENGTH = BUILDER.comment("Height shading intensity multiplier.")
      .defineInRange("minimapHeightShadingStrength", 0.5, 0.0, 1.0);
   private static final DoubleValue MINIMAP_TERRAIN_BRIGHTNESS = BUILDER.comment("Terrain color brightness multiplier.")
      .defineInRange("minimapTerrainBrightness", 1.0, 0.25, 2.0);
   private static final DoubleValue MINIMAP_TERRAIN_CONTRAST = BUILDER.comment("Terrain color contrast multiplier.")
      .defineInRange("minimapTerrainContrast", 1.1, 0.25, 2.0);
   private static final DoubleValue MINIMAP_TERRAIN_SATURATION = BUILDER.comment("Terrain color saturation multiplier.")
      .defineInRange("minimapTerrainSaturation", 1.08, 0.0, 2.0);
   private static final BooleanValue MINIMAP_WATER_TINT_ENABLED = BUILDER.comment("Apply dedicated water tinting for minimap terrain colors.")
      .define("minimapWaterTintEnabled", true);
   private static final BooleanValue MINIMAP_BIOME_TINT_ENABLED = BUILDER.comment("Apply biome tinting for grass/foliage/water when supported.")
      .define("minimapBiomeTintEnabled", true);
   private static final BooleanValue MINIMAP_USE_REGION_CACHE = BUILDER.comment("Use in-memory region/tile cache for minimap terrain.")
      .define("minimapUseRegionCache", true);
   private static final IntValue MINIMAP_TILE_SIZE = BUILDER.comment("Tile size in minimap pixels for region cache.")
      .defineInRange("minimapTileSize", 16, 8, 64);
   private static final IntValue MINIMAP_MAX_CACHED_REGIONS = BUILDER.comment("Maximum in-memory cached minimap regions.")
      .defineInRange("minimapMaxCachedRegions", 64, 1, 512);
   private static final IntValue MINIMAP_TILE_UPDATES_PER_TICK = BUILDER.comment("How many minimap tiles are sampled per client tick.")
      .defineInRange("minimapTileUpdatesPerTick", 12, 1, 64);
   private static final IntValue MINIMAP_MAX_QUEUED_TILE_UPDATES = BUILDER.comment("Maximum queued tile update requests per rebuild cycle.")
      .defineInRange("minimapMaxQueuedTileUpdates", 512, 16, 8192);
   private static final IntValue MINIMAP_MISSING_TILE_RETRY_COOLDOWN_TICKS = BUILDER.comment("Retry cooldown for missing/unloaded tile chunks.")
      .defineInRange("minimapMissingTileRetryCooldownTicks", 20, 1, 600);
   private static final BooleanValue MINIMAP_PRIORITIZE_CENTER_TILES = BUILDER.comment("Prioritize tile updates close to minimap center.")
      .define("minimapPrioritizeCenterTiles", true);
   private static final IntValue MINIMAP_MAX_SAMPLES_PER_TICK = BUILDER.comment("Hard cap for terrain samples per client tick.")
      .defineInRange("minimapMaxSamplesPerTick", 8288, 256, 131072);
   private static final IntValue TERRAIN_INTERNAL_RESOLUTION = BUILDER.comment("Internal terrain texture resolution.")
      .defineInRange("terrainInternalResolution", 128, 64, 512);
   private static final IntValue TERRAIN_UPDATE_INTERVAL_TICKS = BUILDER.comment("Minimum ticks between terrain updates.")
      .defineInRange("terrainUpdateIntervalTicks", 1, 1, 200);
   private static final IntValue TERRAIN_ROWS_PER_TICK = BUILDER.comment("How many terrain rows are sampled each client tick.")
      .defineInRange("terrainRowsPerTick", 48, 1, 512);
   private static final IntValue MINIMAP_ROTATION_OVERSCAN_PADDING = BUILDER.comment(
         "Extra minimap terrain source padding (pixels) to avoid corner holes when rotating."
      )
      .defineInRange("minimapRotationOverscanPadding", 8, 0, 128);
   private static final BooleanValue TERRAIN_USE_LOADED_CHUNKS_ONLY = BUILDER.comment("Use only already loaded chunks for sampling.")
      .define("terrainUseLoadedChunksOnly", true);
   private static final BooleanValue MINIMAP_SHOW_PLAYER_MARKER = BUILDER.comment("Render player marker on minimap.").define("minimapShowPlayerMarker", true);
   private static final BooleanValue MINIMAP_SHOW_TARGET_MARKER = BUILDER.comment("Render current Nano Visor target marker on minimap.")
      .define("minimapShowTargetMarker", true);
   private static final BooleanValue MINIMAP_SHOW_MARKED_ENTITIES = BUILDER.comment("Render glowing/marked entities on minimap.")
      .define("minimapShowMarkedEntities", true);
   private static final BooleanValue MINIMAP_SHOW_NEARBY_ENTITIES = BUILDER.comment("Render nearby living entities on minimap.")
      .define("minimapShowNearbyEntities", true);
   private static final IntValue MINIMAP_NEARBY_ENTITY_RANGE = BUILDER.comment("Nearby marker scan range in blocks.")
      .defineInRange("minimapNearbyEntityRange", 150, 8, 512);
   private static final IntValue MINIMAP_NEARBY_ENTITY_UPDATE_INTERVAL_TICKS = BUILDER.comment("Nearby marker scan interval in ticks.")
      .defineInRange("minimapNearbyEntityUpdateIntervalTicks", 20, 1, 200);
   private static final IntValue MINIMAP_MAX_NEARBY_ENTITY_MARKERS = BUILDER.comment("Maximum nearby entity markers.")
      .defineInRange("minimapMaxNearbyEntityMarkers", 60, 1, 256);
   private static final IntValue MINIMAP_ENTITY_VERTICAL_LIMIT = BUILDER.comment("Max Y-distance for nearby entity markers.")
      .defineInRange("minimapEntityVerticalLimit", 20, 1, 256);
   private static final IntValue MINIMAP_ENTITY_VERTICAL_FADE_START = BUILDER.comment("Start Y-distance where nearby marker depth fade begins.")
      .defineInRange("minimapEntityVerticalFadeStart", 8, 0, 255);
   private static final DoubleValue MINIMAP_ENTITY_VERTICAL_FADE_STRENGTH = BUILDER.comment("Strength of Y-depth fade for nearby entities.")
      .defineInRange("minimapEntityVerticalFadeStrength", 0.75, 0.0, 1.0);
   private static final DoubleValue MINIMAP_ENTITY_UNDERGROUND_FADE = BUILDER.comment("Extra fade for entities below surface.")
      .defineInRange("minimapEntityUndergroundFade", 0.3, 0.0, 1.0);
   private static final BooleanValue MINIMAP_TARGET_IGNORE_VERTICAL_LIMIT = BUILDER.comment("Ignore Y-limit for current target marker.")
      .define("minimapTargetIgnoreVerticalLimit", true);
   private static final BooleanValue MINIMAP_MARKED_IGNORE_VERTICAL_LIMIT = BUILDER.comment("Ignore Y-limit for marked entity markers.")
      .define("minimapMarkedIgnoreVerticalLimit", true);
   private static final BooleanValue MINIMAP_SURFACE_AWARE_NEARBY = BUILDER.comment("When player is near surface, underground nearby entities get extra fade.")
      .define("minimapSurfaceAwareNearby", true);
   private static final BooleanValue MINIMAP_CLAMP_MARKERS_TO_BOUNDS = BUILDER.comment("Clamp out-of-range markers to minimap bounds.")
      .define("minimapClampMarkersToBounds", true);
   private static final DoubleValue MINIMAP_MARKER_SCALE = BUILDER.comment("Global minimap marker scale.").defineInRange("minimapMarkerScale", 1.0, 0.25, 4.0);
   private static final DoubleValue MINIMAP_MARKER_OPACITY = BUILDER.comment("Global minimap marker opacity.")
      .defineInRange("minimapMarkerOpacity", 1.0, 0.05, 1.0);
   private static final DoubleValue MINIMAP_MARKED_HIGHLIGHT_OPACITY = BUILDER.comment("Opacity multiplier for marked-entity minimap frame.")
      .defineInRange("minimapMarkedHighlightOpacity", 1.0, 0.0, 1.0);
   private static final BooleanValue MINIMAP_USE_ENTITY_ICONS = BUILDER.comment("Use entity icon atlas for minimap entity markers.")
      .define("minimapUseEntityIcons", true);
   private static final IntValue MINIMAP_ENTITY_ICON_SIZE = BUILDER.comment("Draw size of minimap entity icons.")
      .defineInRange("minimapEntityIconSize", 8, 8, 16);
   private static final DoubleValue MINIMAP_ENTITY_ICON_OPACITY = BUILDER.comment("Opacity multiplier for minimap entity icons.")
      .defineInRange("minimapEntityIconOpacity", 1.0, 0.05, 1.0);
   private static final BooleanValue MINIMAP_FALLBACK_ENTITY_MARKERS = BUILDER.comment("Draw primitive fallback marker when entity icon is missing.")
      .define("minimapFallbackEntityMarkers", true);
   private static final BooleanValue MINIMAP_FRAME_ENABLED = BUILDER.comment("Render minimap frame/border layer.").define("minimapFrameEnabled", true);
   private static final DoubleValue MINIMAP_FRAME_OPACITY = BUILDER.comment("Opacity multiplier for minimap frame/border layer.")
      .defineInRange("minimapFrameOpacity", 0.7, 0.0, 1.0);
   private static final BooleanValue MINIMAP_PERSPECTIVE_ENABLED = BUILDER.comment("Enable visual post-transform perspective for minimap group.")
      .define("minimapPerspectiveEnabled", true);
   private static final DoubleValue MINIMAP_PERSPECTIVE_RIGHT_SIDE = BUILDER.comment("Perspective right side amount (-1..1).")
      .defineInRange("minimapPerspectiveRightSide", -0.19, -1.0, 1.0);
   private static final DoubleValue MINIMAP_PERSPECTIVE_AXIS_X = BUILDER.comment("Perspective X-axis amount (-1..1).")
      .defineInRange("minimapPerspectiveAxisX", 0.07, -1.0, 1.0);
   private static final DoubleValue MINIMAP_PERSPECTIVE_AXIS_Y = BUILDER.comment("Perspective Y-axis amount (-1..1).")
      .defineInRange("minimapPerspectiveAxisY", 0.0, -1.0, 1.0);
   private static final DoubleValue MINIMAP_EDGE_ARC_TOP_BOTTOM = BUILDER.comment(
         "Top/bottom edge arc amount applied symmetrically around edge centers (-1..1)."
      )
      .defineInRange("minimapEdgeArcTopBottom", -0.09, -1.0, 1.0);
   private static final BooleanValue MINIMAP_PERSPECTIVE_DEBUG_BOUNDS = BUILDER.comment("Draw perspective-transformed minimap debug bounds.")
      .define("minimapPerspectiveDebugBounds", false);
   private static final BooleanValue DEBUG_MINIMAP_BOUNDS = BUILDER.comment("Draw minimap viewport debug bounds.").define("debugMinimapBounds", false);
   private static final BooleanValue DEBUG_MINIMAP_PERFORMANCE = BUILDER.comment("Draw minimap performance/debug text.")
      .define("debugMinimapPerformance", false);
   private static final BooleanValue DEBUG_MINIMAP_CACHE = BUILDER.comment("Draw minimap region/tile cache diagnostics.").define("debugMinimapCache", false);
   private static final BooleanValue DEBUG_MINIMAP_MARKERS = BUILDER.comment("Draw minimap marker diagnostics.").define("debugMinimapMarkers", false);
   private static final BooleanValue DEBUG_FORCE_TERRAIN_REFRESH = BUILDER.comment("Force a one-time terrain refresh on next client tick.")
      .define("debugForceTerrainRefresh", false);
   private static final BooleanValue DEBUG_FORCE_SOLID_TERRAIN = BUILDER.comment("Debug: render solid terrain instead of sampled texture.")
      .define("debugForceSolidTerrain", false);
   private static final BooleanValue DEBUG_FORCE_CHECKERBOARD_TERRAIN = BUILDER.comment("Debug: render checkerboard terrain instead of sampled texture.")
      .define("debugForceCheckerboardTerrain", false);
   private static final BooleanValue DEBUG_DISABLE_REGION_CACHE = BUILDER.comment("Debug: disable region/tile cache and force window mode.")
      .define("debugDisableRegionCache", false);
   private static final BooleanValue DEBUG_DISABLE_TILE_QUEUE = BUILDER.comment("Debug: disable tile queue processing (render front texture only).")
      .define("debugDisableTileQueue", false);
   private static final BooleanValue DEBUG_SHOW_ONLY_FRONT_TEXTURE = BUILDER.comment("Debug: render only terrain front texture (no markers/frame).")
      .define("debugShowOnlyFrontTexture", false);
   private static final BooleanValue DEBUG_SHOW_TERRAIN_ONLY = BUILDER.comment("Debug: render terrain layer only (no frame/markers/background/scissor).")
      .define("debugShowTerrainOnly", false);
   private static final BooleanValue DEBUG_DISABLE_MINIMAP_MASK = BUILDER.comment("Debug: disable minimap clipping mask/scissor.")
      .define("debugDisableMinimapMask", false);
   private static final BooleanValue DEBUG_DRAW_MINIMAP_MASK_BOUNDS = BUILDER.comment("Debug: draw minimap clipping mask/scissor bounds.")
      .define("debugDrawMinimapMaskBounds", false);
   private static final BooleanValue DEBUG_FORCE_QUEUE_VISIBLE_TILES = BUILDER.comment(
         "Debug: ignore queue cap/cooldown and enqueue all currently visible missing tiles."
      )
      .define("debugForceQueueVisibleTiles", false);
   private static final BooleanValue DEBUG_FORCE_RESAMPLE_VISIBLE_TILES = BUILDER.comment("Debug: force visible tiles to dirty each update cycle.")
      .define("debugForceResampleVisibleTiles", false);
   private static final BooleanValue DEBUG_MINIMAP_RENDER_TRACE = BUILDER.comment("Debug: collect minimap render trace (layer coordinates/transforms).")
      .define("debugMinimapRenderTrace", false);
   private static final BooleanValue DEBUG_FREEZE_HUD_INERTIA = BUILDER.comment("Debug: freeze HUD inertia offsets for minimap render.")
      .define("debugFreezeHudInertia", false);
   private static final BooleanValue DEBUG_FREEZE_MAP_WORLD_MOVEMENT = BUILDER.comment("Debug: freeze map world movement offsets for terrain/markers.")
      .define("debugFreezeMapWorldMovement", false);
   private static final BooleanValue DEBUG_FREEZE_TERRAIN_SOURCE_OFFSET = BUILDER.comment(
         "Debug: freeze terrain source offset only (markers still use map movement)."
      )
      .define("debugFreezeTerrainSourceOffset", false);
   private static final BooleanValue DEBUG_DISABLE_POSE_STACK_IN_MINIMAP = BUILDER.comment(
         "Debug: disable minimap pose-stack inertia transform and use manual viewport offsets."
      )
      .define("debugDisablePoseStackInMinimap", false);
   private static final BooleanValue DEBUG_FORCE_MANUAL_VIEWPORT_COORDINATES = BUILDER.comment(
         "Debug: force manual final viewport coordinates for all minimap layers."
      )
      .define("debugForceManualViewportCoordinates", false);
   private static final BooleanValue DEBUG_DRAW_TERRAIN_BOUNDS = BUILDER.comment("Debug: draw terrain destination bounds.")
      .define("debugDrawTerrainBounds", false);
   private static final BooleanValue DEBUG_DRAW_FRAME_BOUNDS = BUILDER.comment("Debug: draw frame bounds.").define("debugDrawFrameBounds", false);
   private static final BooleanValue DEBUG_DRAW_MARKER_BOUNDS = BUILDER.comment("Debug: draw marker viewport bounds.").define("debugDrawMarkerBounds", false);
   private static final BooleanValue DEBUG_DRAW_SCISSOR_BOUNDS = BUILDER.comment("Debug: draw scissor bounds.").define("debugDrawScissorBounds", false);
   private static final BooleanValue DEBUG_LOG_MINIMAP_TRACE_ONCE_PER_SECOND = BUILDER.comment("Debug: print minimap render trace to log once per second.")
      .define("debugLogMinimapTraceOncePerSecond", false);
   private static final BooleanValue DEBUG_MINIMAP_COLORS = BUILDER.comment("Draw minimap terrain color pipeline counters.")
      .define("debugMinimapColors", false);
   public static final ModConfigSpec SPEC = BUILDER.build();

   private NanoMinimapClientConfig() {
   }

   public static boolean minimapEnabled() {
      return safeGetBoolean(MINIMAP_ENABLED, true);
   }

   public static int minimapRangeBlocks() {
      return safeGetInt(MINIMAP_RANGE_BLOCKS, 150);
   }

   public static int minimapWidth() {
      return safeGetInt(MINIMAP_WIDTH, 150);
   }

   public static int minimapHeight() {
      return safeGetInt(MINIMAP_HEIGHT, 100);
   }

   public static int minimapOffsetX() {
      return safeGetInt(MINIMAP_OFFSET_X, 40);
   }

   public static int minimapOffsetY() {
      return safeGetInt(MINIMAP_OFFSET_Y, 16);
   }

   public static double minimapOpacity() {
      return safeGetDouble(MINIMAP_OPACITY, 0.6);
   }

   public static double minimapScale() {
      return safeGetDouble(MINIMAP_SCALE, 0.7);
   }

   public static boolean minimapUseHudInertia() {
      return safeGetBoolean(MINIMAP_USE_HUD_INERTIA, true);
   }

   public static boolean rotateWithPlayer() {
      return safeGetBoolean(ROTATE_WITH_PLAYER, true);
   }

   public static boolean terrainEnabled() {
      return safeGetBoolean(TERRAIN_ENABLED, true);
   }

   public static MinimapTerrainQuality minimapTerrainQuality() {
      return safeGetEnum(MINIMAP_TERRAIN_QUALITY, DEFAULT_MINIMAP_TERRAIN_QUALITY);
   }

   public static boolean minimapHeightShadingEnabled() {
      return safeGetBoolean(MINIMAP_HEIGHT_SHADING_ENABLED, true);
   }

   public static MinimapHeightShadingMode minimapHeightShadingMode() {
      return safeGetEnum(MINIMAP_HEIGHT_SHADING_MODE, DEFAULT_MINIMAP_HEIGHT_SHADING_MODE);
   }

   public static double minimapHeightShadingStrength() {
      return safeGetDouble(MINIMAP_HEIGHT_SHADING_STRENGTH, 0.5);
   }

   public static double minimapTerrainBrightness() {
      return safeGetDouble(MINIMAP_TERRAIN_BRIGHTNESS, 1.0);
   }

   public static double minimapTerrainContrast() {
      return safeGetDouble(MINIMAP_TERRAIN_CONTRAST, 1.1);
   }

   public static double minimapTerrainSaturation() {
      return safeGetDouble(MINIMAP_TERRAIN_SATURATION, 1.08);
   }

   public static boolean minimapWaterTintEnabled() {
      return safeGetBoolean(MINIMAP_WATER_TINT_ENABLED, true);
   }

   public static boolean minimapBiomeTintEnabled() {
      return safeGetBoolean(MINIMAP_BIOME_TINT_ENABLED, true);
   }

   public static boolean minimapUseRegionCache() {
      return safeGetBoolean(MINIMAP_USE_REGION_CACHE, true);
   }

   public static int minimapTileSize() {
      return safeGetInt(MINIMAP_TILE_SIZE, 16);
   }

   public static int minimapMaxCachedRegions() {
      return safeGetInt(MINIMAP_MAX_CACHED_REGIONS, 64);
   }

   public static int minimapTileUpdatesPerTick() {
      return safeGetInt(MINIMAP_TILE_UPDATES_PER_TICK, 12);
   }

   public static int minimapMaxQueuedTileUpdates() {
      return safeGetInt(MINIMAP_MAX_QUEUED_TILE_UPDATES, 512);
   }

   public static int minimapMissingTileRetryCooldownTicks() {
      return safeGetInt(MINIMAP_MISSING_TILE_RETRY_COOLDOWN_TICKS, 20);
   }

   public static boolean minimapPrioritizeCenterTiles() {
      return safeGetBoolean(MINIMAP_PRIORITIZE_CENTER_TILES, true);
   }

   public static int minimapMaxSamplesPerTick() {
      return safeGetInt(MINIMAP_MAX_SAMPLES_PER_TICK, 8288);
   }

   public static int terrainInternalResolution() {
      return safeGetInt(TERRAIN_INTERNAL_RESOLUTION, 128);
   }

   public static int terrainUpdateIntervalTicks() {
      return safeGetInt(TERRAIN_UPDATE_INTERVAL_TICKS, 1);
   }

   public static int terrainRowsPerTick() {
      return safeGetInt(TERRAIN_ROWS_PER_TICK, 48);
   }

   public static int minimapRotationOverscanPadding() {
      return safeGetInt(MINIMAP_ROTATION_OVERSCAN_PADDING, 8);
   }

   public static boolean terrainUseLoadedChunksOnly() {
      return safeGetBoolean(TERRAIN_USE_LOADED_CHUNKS_ONLY, true);
   }

   public static boolean minimapShowPlayerMarker() {
      return safeGetBoolean(MINIMAP_SHOW_PLAYER_MARKER, true);
   }

   public static boolean minimapShowTargetMarker() {
      return safeGetBoolean(MINIMAP_SHOW_TARGET_MARKER, true);
   }

   public static boolean minimapShowMarkedEntities() {
      return safeGetBoolean(MINIMAP_SHOW_MARKED_ENTITIES, true);
   }

   public static boolean minimapShowNearbyEntities() {
      return safeGetBoolean(MINIMAP_SHOW_NEARBY_ENTITIES, true);
   }

   public static int minimapNearbyEntityRange() {
      return safeGetInt(MINIMAP_NEARBY_ENTITY_RANGE, 150);
   }

   public static int minimapNearbyEntityUpdateIntervalTicks() {
      return safeGetInt(MINIMAP_NEARBY_ENTITY_UPDATE_INTERVAL_TICKS, 20);
   }

   public static int minimapMaxNearbyEntityMarkers() {
      return safeGetInt(MINIMAP_MAX_NEARBY_ENTITY_MARKERS, 60);
   }

   public static int minimapEntityVerticalLimit() {
      return safeGetInt(MINIMAP_ENTITY_VERTICAL_LIMIT, 20);
   }

   public static int minimapEntityVerticalFadeStart() {
      return safeGetInt(MINIMAP_ENTITY_VERTICAL_FADE_START, 8);
   }

   public static double minimapEntityVerticalFadeStrength() {
      return safeGetDouble(MINIMAP_ENTITY_VERTICAL_FADE_STRENGTH, 0.75);
   }

   public static double minimapEntityUndergroundFade() {
      return safeGetDouble(MINIMAP_ENTITY_UNDERGROUND_FADE, 0.3);
   }

   public static boolean minimapTargetIgnoreVerticalLimit() {
      return safeGetBoolean(MINIMAP_TARGET_IGNORE_VERTICAL_LIMIT, true);
   }

   public static boolean minimapMarkedIgnoreVerticalLimit() {
      return safeGetBoolean(MINIMAP_MARKED_IGNORE_VERTICAL_LIMIT, true);
   }

   public static boolean minimapSurfaceAwareNearby() {
      return safeGetBoolean(MINIMAP_SURFACE_AWARE_NEARBY, true);
   }

   public static boolean minimapClampMarkersToBounds() {
      return safeGetBoolean(MINIMAP_CLAMP_MARKERS_TO_BOUNDS, true);
   }

   public static double minimapMarkerScale() {
      return safeGetDouble(MINIMAP_MARKER_SCALE, 1.0);
   }

   public static double minimapMarkerOpacity() {
      return safeGetDouble(MINIMAP_MARKER_OPACITY, 1.0);
   }

   public static double minimapMarkedHighlightOpacity() {
      return safeGetDouble(MINIMAP_MARKED_HIGHLIGHT_OPACITY, 1.0);
   }

   public static boolean minimapUseEntityIcons() {
      return safeGetBoolean(MINIMAP_USE_ENTITY_ICONS, true);
   }

   public static int minimapEntityIconSize() {
      return safeGetInt(MINIMAP_ENTITY_ICON_SIZE, 8);
   }

   public static double minimapEntityIconOpacity() {
      return safeGetDouble(MINIMAP_ENTITY_ICON_OPACITY, 1.0);
   }

   public static boolean minimapFallbackEntityMarkers() {
      return safeGetBoolean(MINIMAP_FALLBACK_ENTITY_MARKERS, true);
   }

   public static boolean minimapFrameEnabled() {
      return safeGetBoolean(MINIMAP_FRAME_ENABLED, true);
   }

   public static double minimapFrameOpacity() {
      return safeGetDouble(MINIMAP_FRAME_OPACITY, 0.7);
   }

   public static boolean minimapPerspectiveEnabled() {
      return safeGetBoolean(MINIMAP_PERSPECTIVE_ENABLED, true);
   }

   public static double minimapPerspectiveRightSide() {
      return safeGetDouble(MINIMAP_PERSPECTIVE_RIGHT_SIDE, -0.19);
   }

   public static double minimapPerspectiveAxisX() {
      return safeGetDouble(MINIMAP_PERSPECTIVE_AXIS_X, 0.07);
   }

   public static double minimapPerspectiveAxisY() {
      return safeGetDouble(MINIMAP_PERSPECTIVE_AXIS_Y, 0.0);
   }

   public static double minimapEdgeArcTopBottom() {
      return safeGetDouble(MINIMAP_EDGE_ARC_TOP_BOTTOM, -0.09);
   }

   public static boolean minimapPerspectiveDebugBounds() {
      return safeGetBoolean(MINIMAP_PERSPECTIVE_DEBUG_BOUNDS, false);
   }

   public static boolean debugMinimapBounds() {
      return safeGetBoolean(DEBUG_MINIMAP_BOUNDS, false);
   }

   public static boolean debugMinimapPerformance() {
      return safeGetBoolean(DEBUG_MINIMAP_PERFORMANCE, false);
   }

   public static boolean debugMinimapCache() {
      return safeGetBoolean(DEBUG_MINIMAP_CACHE, false);
   }

   public static boolean debugMinimapMarkers() {
      return safeGetBoolean(DEBUG_MINIMAP_MARKERS, false);
   }

   public static boolean debugForceTerrainRefresh() {
      return safeGetBoolean(DEBUG_FORCE_TERRAIN_REFRESH, false);
   }

   public static boolean debugForceSolidTerrain() {
      return safeGetBoolean(DEBUG_FORCE_SOLID_TERRAIN, false);
   }

   public static boolean debugForceCheckerboardTerrain() {
      return safeGetBoolean(DEBUG_FORCE_CHECKERBOARD_TERRAIN, false);
   }

   public static boolean debugDisableRegionCache() {
      return safeGetBoolean(DEBUG_DISABLE_REGION_CACHE, false);
   }

   public static boolean debugDisableTileQueue() {
      return safeGetBoolean(DEBUG_DISABLE_TILE_QUEUE, false);
   }

   public static boolean debugShowOnlyFrontTexture() {
      return safeGetBoolean(DEBUG_SHOW_ONLY_FRONT_TEXTURE, false);
   }

   public static boolean debugShowTerrainOnly() {
      return safeGetBoolean(DEBUG_SHOW_TERRAIN_ONLY, false);
   }

   public static boolean debugDisableMinimapMask() {
      return safeGetBoolean(DEBUG_DISABLE_MINIMAP_MASK, false);
   }

   public static boolean debugDrawMinimapMaskBounds() {
      return safeGetBoolean(DEBUG_DRAW_MINIMAP_MASK_BOUNDS, false);
   }

   public static boolean debugForceQueueVisibleTiles() {
      return safeGetBoolean(DEBUG_FORCE_QUEUE_VISIBLE_TILES, false);
   }

   public static boolean debugForceResampleVisibleTiles() {
      return safeGetBoolean(DEBUG_FORCE_RESAMPLE_VISIBLE_TILES, false);
   }

   public static boolean debugMinimapRenderTrace() {
      return safeGetBoolean(DEBUG_MINIMAP_RENDER_TRACE, false);
   }

   public static boolean debugFreezeHudInertia() {
      return safeGetBoolean(DEBUG_FREEZE_HUD_INERTIA, false);
   }

   public static boolean debugFreezeMapWorldMovement() {
      return safeGetBoolean(DEBUG_FREEZE_MAP_WORLD_MOVEMENT, false);
   }

   public static boolean debugFreezeTerrainSourceOffset() {
      return safeGetBoolean(DEBUG_FREEZE_TERRAIN_SOURCE_OFFSET, false);
   }

   public static boolean debugDisablePoseStackInMinimap() {
      return safeGetBoolean(DEBUG_DISABLE_POSE_STACK_IN_MINIMAP, false);
   }

   public static boolean debugForceManualViewportCoordinates() {
      return safeGetBoolean(DEBUG_FORCE_MANUAL_VIEWPORT_COORDINATES, false);
   }

   public static boolean debugDrawTerrainBounds() {
      return safeGetBoolean(DEBUG_DRAW_TERRAIN_BOUNDS, false);
   }

   public static boolean debugDrawFrameBounds() {
      return safeGetBoolean(DEBUG_DRAW_FRAME_BOUNDS, false);
   }

   public static boolean debugDrawMarkerBounds() {
      return safeGetBoolean(DEBUG_DRAW_MARKER_BOUNDS, false);
   }

   public static boolean debugDrawScissorBounds() {
      return safeGetBoolean(DEBUG_DRAW_SCISSOR_BOUNDS, false);
   }

   public static boolean debugLogMinimapTraceOncePerSecond() {
      return safeGetBoolean(DEBUG_LOG_MINIMAP_TRACE_ONCE_PER_SECOND, false);
   }

   public static boolean debugMinimapColors() {
      return safeGetBoolean(DEBUG_MINIMAP_COLORS, false);
   }

   public static boolean defaultMinimapEnabled() {
      return true;
   }

   public static int defaultMinimapRangeBlocks() {
      return 150;
   }

   public static int defaultMinimapWidth() {
      return 150;
   }

   public static int defaultMinimapHeight() {
      return 100;
   }

   public static int defaultMinimapOffsetX() {
      return 40;
   }

   public static int defaultMinimapOffsetY() {
      return 16;
   }

   public static double defaultMinimapOpacity() {
      return 0.6;
   }

   public static double defaultMinimapScale() {
      return 0.7;
   }

   public static boolean defaultMinimapUseHudInertia() {
      return true;
   }

   public static boolean defaultRotateWithPlayer() {
      return true;
   }

   public static boolean defaultTerrainEnabled() {
      return true;
   }

   public static MinimapTerrainQuality defaultMinimapTerrainQuality() {
      return DEFAULT_MINIMAP_TERRAIN_QUALITY;
   }

   public static boolean defaultMinimapHeightShadingEnabled() {
      return true;
   }

   public static MinimapHeightShadingMode defaultMinimapHeightShadingMode() {
      return DEFAULT_MINIMAP_HEIGHT_SHADING_MODE;
   }

   public static double defaultMinimapHeightShadingStrength() {
      return 0.5;
   }

   public static double defaultMinimapTerrainBrightness() {
      return 1.0;
   }

   public static double defaultMinimapTerrainContrast() {
      return 1.1;
   }

   public static double defaultMinimapTerrainSaturation() {
      return 1.08;
   }

   public static boolean defaultMinimapWaterTintEnabled() {
      return true;
   }

   public static boolean defaultMinimapBiomeTintEnabled() {
      return true;
   }

   public static boolean defaultMinimapUseRegionCache() {
      return true;
   }

   public static int defaultMinimapTileSize() {
      return 16;
   }

   public static int defaultMinimapMaxCachedRegions() {
      return 64;
   }

   public static int defaultMinimapTileUpdatesPerTick() {
      return 12;
   }

   public static int defaultMinimapMaxQueuedTileUpdates() {
      return 512;
   }

   public static int defaultMinimapMissingTileRetryCooldownTicks() {
      return 20;
   }

   public static boolean defaultMinimapPrioritizeCenterTiles() {
      return true;
   }

   public static int defaultMinimapMaxSamplesPerTick() {
      return 8288;
   }

   public static int defaultTerrainInternalResolution() {
      return 128;
   }

   public static int defaultTerrainUpdateIntervalTicks() {
      return 1;
   }

   public static int defaultTerrainRowsPerTick() {
      return 48;
   }

   public static int defaultMinimapRotationOverscanPadding() {
      return 8;
   }

   public static boolean defaultTerrainUseLoadedChunksOnly() {
      return true;
   }

   public static boolean defaultMinimapShowPlayerMarker() {
      return true;
   }

   public static boolean defaultMinimapShowTargetMarker() {
      return true;
   }

   public static boolean defaultMinimapShowMarkedEntities() {
      return true;
   }

   public static boolean defaultMinimapShowNearbyEntities() {
      return true;
   }

   public static int defaultMinimapNearbyEntityRange() {
      return 150;
   }

   public static int defaultMinimapNearbyEntityUpdateIntervalTicks() {
      return 20;
   }

   public static int defaultMinimapMaxNearbyEntityMarkers() {
      return 60;
   }

   public static int defaultMinimapEntityVerticalLimit() {
      return 20;
   }

   public static int defaultMinimapEntityVerticalFadeStart() {
      return 8;
   }

   public static double defaultMinimapEntityVerticalFadeStrength() {
      return 0.75;
   }

   public static double defaultMinimapEntityUndergroundFade() {
      return 0.3;
   }

   public static boolean defaultMinimapTargetIgnoreVerticalLimit() {
      return true;
   }

   public static boolean defaultMinimapMarkedIgnoreVerticalLimit() {
      return true;
   }

   public static boolean defaultMinimapSurfaceAwareNearby() {
      return true;
   }

   public static boolean defaultMinimapClampMarkersToBounds() {
      return true;
   }

   public static double defaultMinimapMarkerScale() {
      return 1.0;
   }

   public static double defaultMinimapMarkerOpacity() {
      return 1.0;
   }

   public static double defaultMinimapMarkedHighlightOpacity() {
      return 1.0;
   }

   public static boolean defaultMinimapUseEntityIcons() {
      return true;
   }

   public static int defaultMinimapEntityIconSize() {
      return 8;
   }

   public static double defaultMinimapEntityIconOpacity() {
      return 1.0;
   }

   public static boolean defaultMinimapFallbackEntityMarkers() {
      return true;
   }

   public static boolean defaultMinimapFrameEnabled() {
      return true;
   }

   public static double defaultMinimapFrameOpacity() {
      return 0.7;
   }

   public static boolean defaultMinimapPerspectiveEnabled() {
      return true;
   }

   public static double defaultMinimapPerspectiveRightSide() {
      return -0.19;
   }

   public static double defaultMinimapPerspectiveAxisX() {
      return 0.07;
   }

   public static double defaultMinimapPerspectiveAxisY() {
      return 0.0;
   }

   public static double defaultMinimapEdgeArcTopBottom() {
      return -0.09;
   }

   public static boolean defaultMinimapPerspectiveDebugBounds() {
      return false;
   }

   public static boolean defaultDebugMinimapBounds() {
      return false;
   }

   public static boolean defaultDebugMinimapPerformance() {
      return false;
   }

   public static boolean defaultDebugMinimapCache() {
      return false;
   }

   public static boolean defaultDebugMinimapMarkers() {
      return false;
   }

   public static boolean defaultDebugForceTerrainRefresh() {
      return false;
   }

   public static boolean defaultDebugForceSolidTerrain() {
      return false;
   }

   public static boolean defaultDebugForceCheckerboardTerrain() {
      return false;
   }

   public static boolean defaultDebugDisableRegionCache() {
      return false;
   }

   public static boolean defaultDebugDisableTileQueue() {
      return false;
   }

   public static boolean defaultDebugShowOnlyFrontTexture() {
      return false;
   }

   public static boolean defaultDebugShowTerrainOnly() {
      return false;
   }

   public static boolean defaultDebugDisableMinimapMask() {
      return false;
   }

   public static boolean defaultDebugDrawMinimapMaskBounds() {
      return false;
   }

   public static boolean defaultDebugForceQueueVisibleTiles() {
      return false;
   }

   public static boolean defaultDebugForceResampleVisibleTiles() {
      return false;
   }

   public static boolean defaultDebugMinimapRenderTrace() {
      return false;
   }

   public static boolean defaultDebugFreezeHudInertia() {
      return false;
   }

   public static boolean defaultDebugFreezeMapWorldMovement() {
      return false;
   }

   public static boolean defaultDebugFreezeTerrainSourceOffset() {
      return false;
   }

   public static boolean defaultDebugDisablePoseStackInMinimap() {
      return false;
   }

   public static boolean defaultDebugForceManualViewportCoordinates() {
      return false;
   }

   public static boolean defaultDebugDrawTerrainBounds() {
      return false;
   }

   public static boolean defaultDebugDrawFrameBounds() {
      return false;
   }

   public static boolean defaultDebugDrawMarkerBounds() {
      return false;
   }

   public static boolean defaultDebugDrawScissorBounds() {
      return false;
   }

   public static boolean defaultDebugLogMinimapTraceOncePerSecond() {
      return false;
   }

   public static boolean defaultDebugMinimapColors() {
      return false;
   }

   public static void setMinimapEnabled(boolean value) {
      safeSetBoolean(MINIMAP_ENABLED, value);
   }

   public static void setMinimapRangeBlocks(int value) {
      safeSetInt(MINIMAP_RANGE_BLOCKS, value);
   }

   public static void setMinimapWidth(int value) {
      safeSetInt(MINIMAP_WIDTH, value);
   }

   public static void setMinimapHeight(int value) {
      safeSetInt(MINIMAP_HEIGHT, value);
   }

   public static void setMinimapOffsetX(int value) {
      safeSetInt(MINIMAP_OFFSET_X, value);
   }

   public static void setMinimapOffsetY(int value) {
      safeSetInt(MINIMAP_OFFSET_Y, value);
   }

   public static void setMinimapOpacity(double value) {
      safeSetDouble(MINIMAP_OPACITY, value);
   }

   public static void setMinimapScale(double value) {
      safeSetDouble(MINIMAP_SCALE, value);
   }

   public static void setMinimapUseHudInertia(boolean value) {
      safeSetBoolean(MINIMAP_USE_HUD_INERTIA, value);
   }

   public static void setRotateWithPlayer(boolean value) {
      safeSetBoolean(ROTATE_WITH_PLAYER, value);
   }

   public static void setTerrainEnabled(boolean value) {
      safeSetBoolean(TERRAIN_ENABLED, value);
   }

   public static void setMinimapTerrainQuality(MinimapTerrainQuality value) {
      safeSetEnum(MINIMAP_TERRAIN_QUALITY, value);
   }

   public static void setMinimapHeightShadingEnabled(boolean value) {
      safeSetBoolean(MINIMAP_HEIGHT_SHADING_ENABLED, value);
   }

   public static void setMinimapHeightShadingMode(MinimapHeightShadingMode value) {
      safeSetEnum(MINIMAP_HEIGHT_SHADING_MODE, value);
   }

   public static void setMinimapHeightShadingStrength(double value) {
      safeSetDouble(MINIMAP_HEIGHT_SHADING_STRENGTH, value);
   }

   public static void setMinimapTerrainBrightness(double value) {
      safeSetDouble(MINIMAP_TERRAIN_BRIGHTNESS, value);
   }

   public static void setMinimapTerrainContrast(double value) {
      safeSetDouble(MINIMAP_TERRAIN_CONTRAST, value);
   }

   public static void setMinimapTerrainSaturation(double value) {
      safeSetDouble(MINIMAP_TERRAIN_SATURATION, value);
   }

   public static void setMinimapWaterTintEnabled(boolean value) {
      safeSetBoolean(MINIMAP_WATER_TINT_ENABLED, value);
   }

   public static void setMinimapBiomeTintEnabled(boolean value) {
      safeSetBoolean(MINIMAP_BIOME_TINT_ENABLED, value);
   }

   public static void setMinimapUseRegionCache(boolean value) {
      safeSetBoolean(MINIMAP_USE_REGION_CACHE, value);
   }

   public static void setMinimapTileSize(int value) {
      safeSetInt(MINIMAP_TILE_SIZE, value);
   }

   public static void setMinimapMaxCachedRegions(int value) {
      safeSetInt(MINIMAP_MAX_CACHED_REGIONS, value);
   }

   public static void setMinimapTileUpdatesPerTick(int value) {
      safeSetInt(MINIMAP_TILE_UPDATES_PER_TICK, value);
   }

   public static void setMinimapMaxQueuedTileUpdates(int value) {
      safeSetInt(MINIMAP_MAX_QUEUED_TILE_UPDATES, value);
   }

   public static void setMinimapMissingTileRetryCooldownTicks(int value) {
      safeSetInt(MINIMAP_MISSING_TILE_RETRY_COOLDOWN_TICKS, value);
   }

   public static void setMinimapPrioritizeCenterTiles(boolean value) {
      safeSetBoolean(MINIMAP_PRIORITIZE_CENTER_TILES, value);
   }

   public static void setMinimapMaxSamplesPerTick(int value) {
      safeSetInt(MINIMAP_MAX_SAMPLES_PER_TICK, value);
   }

   public static void setTerrainInternalResolution(int value) {
      safeSetInt(TERRAIN_INTERNAL_RESOLUTION, value);
   }

   public static void setTerrainUpdateIntervalTicks(int value) {
      safeSetInt(TERRAIN_UPDATE_INTERVAL_TICKS, value);
   }

   public static void setTerrainRowsPerTick(int value) {
      safeSetInt(TERRAIN_ROWS_PER_TICK, value);
   }

   public static void setMinimapRotationOverscanPadding(int value) {
      safeSetInt(MINIMAP_ROTATION_OVERSCAN_PADDING, value);
   }

   public static void setTerrainUseLoadedChunksOnly(boolean value) {
      safeSetBoolean(TERRAIN_USE_LOADED_CHUNKS_ONLY, value);
   }

   public static void setMinimapShowPlayerMarker(boolean value) {
      safeSetBoolean(MINIMAP_SHOW_PLAYER_MARKER, value);
   }

   public static void setMinimapShowTargetMarker(boolean value) {
      safeSetBoolean(MINIMAP_SHOW_TARGET_MARKER, value);
   }

   public static void setMinimapShowMarkedEntities(boolean value) {
      safeSetBoolean(MINIMAP_SHOW_MARKED_ENTITIES, value);
   }

   public static void setMinimapShowNearbyEntities(boolean value) {
      safeSetBoolean(MINIMAP_SHOW_NEARBY_ENTITIES, value);
   }

   public static void setMinimapNearbyEntityRange(int value) {
      safeSetInt(MINIMAP_NEARBY_ENTITY_RANGE, value);
   }

   public static void setMinimapNearbyEntityUpdateIntervalTicks(int value) {
      safeSetInt(MINIMAP_NEARBY_ENTITY_UPDATE_INTERVAL_TICKS, value);
   }

   public static void setMinimapMaxNearbyEntityMarkers(int value) {
      safeSetInt(MINIMAP_MAX_NEARBY_ENTITY_MARKERS, value);
   }

   public static void setMinimapEntityVerticalLimit(int value) {
      safeSetInt(MINIMAP_ENTITY_VERTICAL_LIMIT, value);
   }

   public static void setMinimapEntityVerticalFadeStart(int value) {
      safeSetInt(MINIMAP_ENTITY_VERTICAL_FADE_START, value);
   }

   public static void setMinimapEntityVerticalFadeStrength(double value) {
      safeSetDouble(MINIMAP_ENTITY_VERTICAL_FADE_STRENGTH, value);
   }

   public static void setMinimapEntityUndergroundFade(double value) {
      safeSetDouble(MINIMAP_ENTITY_UNDERGROUND_FADE, value);
   }

   public static void setMinimapTargetIgnoreVerticalLimit(boolean value) {
      safeSetBoolean(MINIMAP_TARGET_IGNORE_VERTICAL_LIMIT, value);
   }

   public static void setMinimapMarkedIgnoreVerticalLimit(boolean value) {
      safeSetBoolean(MINIMAP_MARKED_IGNORE_VERTICAL_LIMIT, value);
   }

   public static void setMinimapSurfaceAwareNearby(boolean value) {
      safeSetBoolean(MINIMAP_SURFACE_AWARE_NEARBY, value);
   }

   public static void setMinimapClampMarkersToBounds(boolean value) {
      safeSetBoolean(MINIMAP_CLAMP_MARKERS_TO_BOUNDS, value);
   }

   public static void setMinimapMarkerScale(double value) {
      safeSetDouble(MINIMAP_MARKER_SCALE, value);
   }

   public static void setMinimapMarkerOpacity(double value) {
      safeSetDouble(MINIMAP_MARKER_OPACITY, value);
   }

   public static void setMinimapMarkedHighlightOpacity(double value) {
      safeSetDouble(MINIMAP_MARKED_HIGHLIGHT_OPACITY, value);
   }

   public static void setMinimapUseEntityIcons(boolean value) {
      safeSetBoolean(MINIMAP_USE_ENTITY_ICONS, value);
   }

   public static void setMinimapEntityIconSize(int value) {
      safeSetInt(MINIMAP_ENTITY_ICON_SIZE, value);
   }

   public static void setMinimapEntityIconOpacity(double value) {
      safeSetDouble(MINIMAP_ENTITY_ICON_OPACITY, value);
   }

   public static void setMinimapFallbackEntityMarkers(boolean value) {
      safeSetBoolean(MINIMAP_FALLBACK_ENTITY_MARKERS, value);
   }

   public static void setMinimapFrameEnabled(boolean value) {
      safeSetBoolean(MINIMAP_FRAME_ENABLED, value);
   }

   public static void setMinimapFrameOpacity(double value) {
      safeSetDouble(MINIMAP_FRAME_OPACITY, value);
   }

   public static void setMinimapPerspectiveEnabled(boolean value) {
      safeSetBoolean(MINIMAP_PERSPECTIVE_ENABLED, value);
   }

   public static void setMinimapPerspectiveRightSide(double value) {
      safeSetDouble(MINIMAP_PERSPECTIVE_RIGHT_SIDE, value);
   }

   public static void setMinimapPerspectiveAxisX(double value) {
      safeSetDouble(MINIMAP_PERSPECTIVE_AXIS_X, value);
   }

   public static void setMinimapPerspectiveAxisY(double value) {
      safeSetDouble(MINIMAP_PERSPECTIVE_AXIS_Y, value);
   }

   public static void setMinimapEdgeArcTopBottom(double value) {
      safeSetDouble(MINIMAP_EDGE_ARC_TOP_BOTTOM, value);
   }

   public static void setMinimapPerspectiveDebugBounds(boolean value) {
      safeSetBoolean(MINIMAP_PERSPECTIVE_DEBUG_BOUNDS, value);
   }

   public static void setDebugMinimapBounds(boolean value) {
      safeSetBoolean(DEBUG_MINIMAP_BOUNDS, value);
   }

   public static void setDebugMinimapPerformance(boolean value) {
      safeSetBoolean(DEBUG_MINIMAP_PERFORMANCE, value);
   }

   public static void setDebugMinimapCache(boolean value) {
      safeSetBoolean(DEBUG_MINIMAP_CACHE, value);
   }

   public static void setDebugMinimapMarkers(boolean value) {
      safeSetBoolean(DEBUG_MINIMAP_MARKERS, value);
   }

   public static void setDebugForceTerrainRefresh(boolean value) {
      safeSetBoolean(DEBUG_FORCE_TERRAIN_REFRESH, value);
   }

   public static void setDebugForceSolidTerrain(boolean value) {
      safeSetBoolean(DEBUG_FORCE_SOLID_TERRAIN, value);
   }

   public static void setDebugForceCheckerboardTerrain(boolean value) {
      safeSetBoolean(DEBUG_FORCE_CHECKERBOARD_TERRAIN, value);
   }

   public static void setDebugDisableRegionCache(boolean value) {
      safeSetBoolean(DEBUG_DISABLE_REGION_CACHE, value);
   }

   public static void setDebugDisableTileQueue(boolean value) {
      safeSetBoolean(DEBUG_DISABLE_TILE_QUEUE, value);
   }

   public static void setDebugShowOnlyFrontTexture(boolean value) {
      safeSetBoolean(DEBUG_SHOW_ONLY_FRONT_TEXTURE, value);
   }

   public static void setDebugShowTerrainOnly(boolean value) {
      safeSetBoolean(DEBUG_SHOW_TERRAIN_ONLY, value);
   }

   public static void setDebugDisableMinimapMask(boolean value) {
      safeSetBoolean(DEBUG_DISABLE_MINIMAP_MASK, value);
   }

   public static void setDebugDrawMinimapMaskBounds(boolean value) {
      safeSetBoolean(DEBUG_DRAW_MINIMAP_MASK_BOUNDS, value);
   }

   public static void setDebugForceQueueVisibleTiles(boolean value) {
      safeSetBoolean(DEBUG_FORCE_QUEUE_VISIBLE_TILES, value);
   }

   public static void setDebugForceResampleVisibleTiles(boolean value) {
      safeSetBoolean(DEBUG_FORCE_RESAMPLE_VISIBLE_TILES, value);
   }

   public static void setDebugMinimapRenderTrace(boolean value) {
      safeSetBoolean(DEBUG_MINIMAP_RENDER_TRACE, value);
   }

   public static void setDebugFreezeHudInertia(boolean value) {
      safeSetBoolean(DEBUG_FREEZE_HUD_INERTIA, value);
   }

   public static void setDebugFreezeMapWorldMovement(boolean value) {
      safeSetBoolean(DEBUG_FREEZE_MAP_WORLD_MOVEMENT, value);
   }

   public static void setDebugFreezeTerrainSourceOffset(boolean value) {
      safeSetBoolean(DEBUG_FREEZE_TERRAIN_SOURCE_OFFSET, value);
   }

   public static void setDebugDisablePoseStackInMinimap(boolean value) {
      safeSetBoolean(DEBUG_DISABLE_POSE_STACK_IN_MINIMAP, value);
   }

   public static void setDebugForceManualViewportCoordinates(boolean value) {
      safeSetBoolean(DEBUG_FORCE_MANUAL_VIEWPORT_COORDINATES, value);
   }

   public static void setDebugDrawTerrainBounds(boolean value) {
      safeSetBoolean(DEBUG_DRAW_TERRAIN_BOUNDS, value);
   }

   public static void setDebugDrawFrameBounds(boolean value) {
      safeSetBoolean(DEBUG_DRAW_FRAME_BOUNDS, value);
   }

   public static void setDebugDrawMarkerBounds(boolean value) {
      safeSetBoolean(DEBUG_DRAW_MARKER_BOUNDS, value);
   }

   public static void setDebugDrawScissorBounds(boolean value) {
      safeSetBoolean(DEBUG_DRAW_SCISSOR_BOUNDS, value);
   }

   public static void setDebugLogMinimapTraceOncePerSecond(boolean value) {
      safeSetBoolean(DEBUG_LOG_MINIMAP_TRACE_ONCE_PER_SECOND, value);
   }

   public static void setDebugMinimapColors(boolean value) {
      safeSetBoolean(DEBUG_MINIMAP_COLORS, value);
   }

   private static boolean safeGetBoolean(BooleanValue value, boolean fallback) {
      try {
         return (Boolean)value.get();
      } catch (RuntimeException var3) {
         return fallback;
      }
   }

   private static int safeGetInt(IntValue value, int fallback) {
      try {
         return (Integer)value.get();
      } catch (RuntimeException var3) {
         return fallback;
      }
   }

   private static double safeGetDouble(DoubleValue value, double fallback) {
      try {
         return (Double)value.get();
      } catch (RuntimeException var4) {
         return fallback;
      }
   }

   private static <E extends Enum<E>> E safeGetEnum(EnumValue<E> value, E fallback) {
      try {
         return (E)value.get();
      } catch (RuntimeException var3) {
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

   private static <E extends Enum<E>> void safeSetEnum(EnumValue<E> value, E target) {
      try {
         value.set(target);
         value.save();
      } catch (RuntimeException var3) {
      }
   }

   static {
      BUILDER.push("minimap");
      BUILDER.pop();
   }
}
