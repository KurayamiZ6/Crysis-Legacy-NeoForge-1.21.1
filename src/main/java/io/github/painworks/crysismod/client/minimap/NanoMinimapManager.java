package io.github.painworks.crysismod.client.minimap;

import io.github.painworks.crysismod.client.minimap.cache.NanoMinimapTerrainTexture;
import io.github.painworks.crysismod.client.minimap.cache.NanoMinimapTileCache;
import io.github.painworks.crysismod.client.minimap.config.NanoMinimapClientConfig;
import io.github.painworks.crysismod.client.minimap.marker.NanoMinimapMarkerLayer;
import io.github.painworks.crysismod.client.minimap.render.NanoMinimapRenderer;
import io.github.painworks.crysismod.client.minimap.sampling.NanoMinimapWorldSampler;
import io.github.painworks.crysismod.nanosuit.client.NanosuitHudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public final class NanoMinimapManager {
   private static final int ANCHOR_PADDING = 12;
   private static final double PLAYER_MOVE_FORCE_REBUILD_BLOCKS = 1.0;
   private static final NanoMinimapManager INSTANCE = new NanoMinimapManager();
   private final NanoMinimapWorldSampler worldSampler = new NanoMinimapWorldSampler();
   private final NanoMinimapTileCache tileCache = new NanoMinimapTileCache();
   private final NanoMinimapTerrainTexture terrainTexture = new NanoMinimapTerrainTexture();
   private final NanoMinimapMarkerLayer markerLayer = new NanoMinimapMarkerLayer();
   private final NanoMinimapRenderer renderer = new NanoMinimapRenderer();
   private final NanoMinimapDebugOverlay debugOverlay = new NanoMinimapDebugOverlay();
   private final NanoMinimapRenderTrace renderTrace = new NanoMinimapRenderTrace();
   private long lastTerrainUpdateTick = Long.MIN_VALUE;
   private String lastUpdateReason = "idle";
   private String forcedNextUpdateReason;
   private ClientLevel lastLevel;
   private NanoMinimapViewRequest lastViewRequest;
   private int lastSampleCenterBlockX;
   private int lastSampleCenterBlockZ;
   private ResourceKey<Level> lastDimension;
   private int lastResolution;
   private int lastRangeBlocks;
   private boolean lastUseRegionCache = true;
   private int lastTileSize = 16;
   private int lastMaxCachedRegions = 64;
   private int lastTileUpdatesPerTick = 2;
   private int lastMaxQueuedTileUpdates = 512;
   private int lastMissingTileRetryCooldownTicks = 20;
   private boolean lastPrioritizeCenterTiles = true;
   private int lastMaxSamplesPerTick = 8288;
   private boolean lastTerrainEnabled = true;
   private int textureUploadCount;
   private int textureRecreatedCount;
   private long lastUploadTimeMs;
   private String lastUploadReason = "none";
   private int lastTextureResolution;
   private long lastUploadTick = Long.MIN_VALUE;
   private int framesSinceLastUpload;
   private int lastSamplesProcessedThisTick;
   private long lastRebuildTimeMs;
   private double lastPlayerExactX;
   private double lastPlayerExactZ;
   private double lastFractionalOffsetX;
   private double lastFractionalOffsetY;
   private double lastVisualOffsetPixelsX;
   private double lastVisualOffsetPixelsY;
   private float lastRenderMs;
   private int lastViewportWidth;
   private int lastViewportHeight;
   private int lastBaseViewportX;
   private int lastBaseViewportY;
   private int lastFinalViewportX;
   private int lastFinalViewportY;
   private float lastHudInertiaOffsetX;
   private float lastHudInertiaOffsetY;
   private float lastHudInertiaRollDeg;
   private int lastCoverageSize;
   private int lastOverscanPadding;
   private double lastLogicalBlocksPerPixel;
   private int lastReadyTilesInViewport;
   private int lastMissingTilesInViewport;
   private String lastComposeSkippedReason = "none";
   private int lastNeededTilesThisTick;
   private int lastNewlyEnqueuedTilesThisTick;
   private int lastEnqueueSkippedDuplicateThisTick;
   private int lastEnqueueSkippedQueueFullThisTick;
   private int lastEnqueueSkippedCooldownThisTick;
   private int lastVisibleTilesDroppedThisTick;
   private int lastPriorityCenterTileCountThisTick;
   private int lastUnloadedTilesInViewportThisTick;
   private int lastRetryCooldownTilesThisTick;
   private int lastRetryReadyTilesThisTick;
   private int lastResampledAfterChunkLoadedThisTick;
   private String lastSampledTileKey = "n/a";
   private int lastSamplesAttemptedThisTick;
   private int lastSamplesSucceededThisTick;
   private int lastSamplesFallbackThisTick;
   private int lastSampledTileValidPixelCount;
   private int lastReadyTilesUsedInLastCompose;
   private int lastMissingTilesUsedInLastCompose;
   private int lastTicksSinceLastUpload = -1;
   private double renderMsAvg;
   private double composeMsAvg;
   private double tileUpdateMsAvg;
   private double markerUpdateMsAvg;
   private double uploadsPerSecond;
   private double composesPerSecond;
   private double tilesProcessedPerSecond;
   private long perfWindowStartTick = Long.MIN_VALUE;
   private long perfWindowComposeStartCount;
   private int perfWindowUploadStartCount;
   private long perfWindowTilesProcessedAccum;
   private long renderFrameCounter;
   private int lastMapColorCount;
   private int lastFallbackColorCount;
   private int lastSpecialCaseColorCount;
   private int lastWaterColorCount;
   private int lastLavaColorCount;
   private int lastHeightShadingAppliedCount;
   private int lastBiomeTintAppliedCount;
   private String lastTerrainColorMode = "MEDIUM";

   private NanoMinimapManager() {
   }

   public static NanoMinimapManager get() {
      return INSTANCE;
   }

   public void onClientTick(Minecraft minecraft) {
      if (minecraft != null) {
         LocalPlayer player = minecraft.player;
         ClientLevel level = minecraft.level;
         if (player != null && level != null) {
            boolean debugPerformance = NanoMinimapClientConfig.debugMinimapPerformance();
            boolean debugCache = NanoMinimapClientConfig.debugMinimapCache();
            boolean debugMarkers = NanoMinimapClientConfig.debugMinimapMarkers();
            boolean debugRenderTrace = NanoMinimapClientConfig.debugMinimapRenderTrace();
            boolean debugColors = NanoMinimapClientConfig.debugMinimapColors();
            boolean collectDebugTelemetry = debugPerformance || debugCache || debugMarkers || debugRenderTrace || debugColors;
            boolean minimapVisible = NanoMinimapVisibility.shouldRender(minecraft);
            if (!minimapVisible) {
               this.markerLayer.clear();
            }

            if (this.lastLevel != null && this.lastLevel != level) {
               this.resetTerrainState("world_reload", true);
            }

            this.lastLevel = level;
            boolean terrainEnabled = NanoMinimapClientConfig.terrainEnabled();
            if (terrainEnabled != this.lastTerrainEnabled) {
               this.forcedNextUpdateReason = terrainEnabled ? "terrain_enabled_changed" : "terrain_disabled_changed";
               if (!terrainEnabled) {
                  this.resetTerrainState(this.forcedNextUpdateReason, true);
               }

               this.lastTerrainEnabled = terrainEnabled;
            }

            ResourceKey<Level> currentDimension = level.dimension();
            if (this.lastDimension != null && !this.lastDimension.equals(currentDimension)) {
               this.resetTerrainState("dimension_changed", true);
            }

            this.lastDimension = currentDimension;
            if (minimapVisible) {
               NanoMinimapViewport tickViewport = createViewport(minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
               double logicalBlocksPerPixel = logicalBlocksPerPixel();
               double activeBlocksPerPixel = this.tileCache.hasFrontData() ? this.tileCache.activeBlocksPerPixel() : logicalBlocksPerPixel;
               double tickCenterWorldX = this.tileCache.hasFrontData() ? this.tileCache.centerWorldX() : player.getX();
               double tickCenterWorldZ = this.tileCache.hasFrontData() ? this.tileCache.centerWorldZ() : player.getZ();
               NanoMinimapCoordinateMapper tickMapper = new NanoMinimapCoordinateMapper(
                  tickCenterWorldX, tickCenterWorldZ, activeBlocksPerPixel, Math.max(8, tickViewport.width()), Math.max(8, tickViewport.height())
               );
               this.markerLayer.update(minecraft, player, true, tickMapper, NanoMinimapClientConfig.rotateWithPlayer(), player.getYRot());
               if (terrainEnabled) {
                  int rangeBlocks = Math.max(32, NanoMinimapClientConfig.minimapRangeBlocks());
                  int intervalTicks = Math.max(1, NanoMinimapClientConfig.terrainUpdateIntervalTicks());
                  int rowsPerTick = Math.max(1, NanoMinimapClientConfig.terrainRowsPerTick());
                  boolean loadedChunksOnly = NanoMinimapClientConfig.terrainUseLoadedChunksOnly();
                  boolean useRegionCache = NanoMinimapClientConfig.minimapUseRegionCache() && !NanoMinimapClientConfig.debugDisableRegionCache();
                  int tileSize = Math.max(4, NanoMinimapClientConfig.minimapTileSize());
                  int maxCachedRegions = Math.max(1, NanoMinimapClientConfig.minimapMaxCachedRegions());
                  int tileUpdatesPerTick = Math.max(1, NanoMinimapClientConfig.minimapTileUpdatesPerTick());
                  int maxQueuedTileUpdates = Math.max(16, NanoMinimapClientConfig.minimapMaxQueuedTileUpdates());
                  int missingTileRetryCooldownTicks = Math.max(1, NanoMinimapClientConfig.minimapMissingTileRetryCooldownTicks());
                  boolean prioritizeCenterTiles = NanoMinimapClientConfig.minimapPrioritizeCenterTiles();
                  int maxSamplesPerTick = Math.max(256, NanoMinimapClientConfig.minimapMaxSamplesPerTick());
                  int overscanPadding = Math.max(0, NanoMinimapClientConfig.minimapRotationOverscanPadding());
                  NanoMinimapRenderCoverage coverage = NanoMinimapRenderCoverage.of(
                     tickViewport.width(), tickViewport.height(), NanoMinimapClientConfig.rotateWithPlayer(), overscanPadding
                  );
                  int resolution = coverage.coverageSizePixels();
                  int dynamicRangeBlocks = Math.max(16, (int)Math.ceil(resolution * logicalBlocksPerPixel / 2.0));
                  this.lastViewportWidth = tickViewport.width();
                  this.lastViewportHeight = tickViewport.height();
                  this.lastCoverageSize = coverage.coverageSizePixels();
                  this.lastOverscanPadding = coverage.overscanPaddingPixels();
                  this.lastLogicalBlocksPerPixel = logicalBlocksPerPixel;
                  boolean debugForceRefresh = NanoMinimapClientConfig.debugForceTerrainRefresh();
                  if (debugForceRefresh) {
                     NanoMinimapClientConfig.setDebugForceTerrainRefresh(false);
                  }

                  int centerX = Mth.floor(player.getX());
                  int centerZ = Mth.floor(player.getZ());
                  double centerWorldX = player.getX();
                  double centerWorldZ = player.getZ();
                  long nowTick = level.getGameTime();
                  NanoMinimapView view = new NanoMinimapView(
                     currentDimension,
                     centerX,
                     centerZ,
                     centerWorldX,
                     centerWorldZ,
                     dynamicRangeBlocks,
                     resolution,
                     loadedChunksOnly,
                     useRegionCache,
                     tileSize,
                     maxCachedRegions,
                     tileUpdatesPerTick,
                     maxQueuedTileUpdates,
                     missingTileRetryCooldownTicks,
                     prioritizeCenterTiles,
                     maxSamplesPerTick
                  );
                  NanoMinimapViewRequest viewRequest = view.toRequest();
                  if (!this.tileCache.isRebuilding()) {
                     String startReason = this.determineStartReason(viewRequest, debugForceRefresh, nowTick);
                     boolean bypassInterval = startReason != null && !"player_moved".equals(startReason);
                     boolean intervalSatisfied = this.lastTerrainUpdateTick == Long.MIN_VALUE || nowTick - this.lastTerrainUpdateTick >= intervalTicks;
                     if ("player_moved".equals(startReason)) {
                        double driftX = Math.abs(centerWorldX - this.tileCache.centerWorldX());
                        double driftZ = Math.abs(centerWorldZ - this.tileCache.centerWorldZ());
                        if (Math.max(driftX, driftZ) >= 1.0) {
                           intervalSatisfied = true;
                        }
                     }

                     if (startReason != null && (bypassInterval || intervalSatisfied)) {
                        this.tileCache.startRebuild(level, viewRequest, startReason);
                        this.lastViewRequest = viewRequest;
                        this.lastRangeBlocks = dynamicRangeBlocks;
                        this.lastResolution = resolution;
                        this.lastUseRegionCache = useRegionCache;
                        this.lastTileSize = tileSize;
                        this.lastMaxCachedRegions = maxCachedRegions;
                        this.lastTileUpdatesPerTick = tileUpdatesPerTick;
                        this.lastMaxQueuedTileUpdates = maxQueuedTileUpdates;
                        this.lastMissingTileRetryCooldownTicks = missingTileRetryCooldownTicks;
                        this.lastPrioritizeCenterTiles = prioritizeCenterTiles;
                        this.lastMaxSamplesPerTick = maxSamplesPerTick;
                        this.lastUpdateReason = startReason;
                     }
                  }

                  this.worldSampler.beginTick();
                  this.lastSamplesProcessedThisTick = this.tileCache
                     .processRebuildRows(
                        level,
                        this.worldSampler,
                        rowsPerTick,
                        NanoMinimapClientConfig.debugDisableTileQueue(),
                        NanoMinimapClientConfig.debugForceQueueVisibleTiles(),
                        NanoMinimapClientConfig.debugForceResampleVisibleTiles()
                     );
                  if (debugColors) {
                     NanoMinimapWorldSampler.Stats colorStats = this.worldSampler.stats();
                     this.lastMapColorCount = colorStats.mapColorCount();
                     this.lastFallbackColorCount = colorStats.fallbackColorCount();
                     this.lastSpecialCaseColorCount = colorStats.specialCaseColorCount();
                     this.lastWaterColorCount = colorStats.waterColorCount();
                     this.lastLavaColorCount = colorStats.lavaColorCount();
                     this.lastHeightShadingAppliedCount = colorStats.heightShadingAppliedCount();
                     this.lastBiomeTintAppliedCount = colorStats.biomeTintAppliedCount();
                     this.lastTerrainColorMode = colorStats.lastColorMode();
                  }

                  if (collectDebugTelemetry) {
                     this.lastRebuildTimeMs = this.tileCache.lastRebuildTimeMs();
                     this.lastReadyTilesInViewport = this.tileCache.readyTilesInViewport();
                     this.lastMissingTilesInViewport = this.tileCache.missingTilesInViewport();
                     this.lastComposeSkippedReason = this.tileCache.composeSkippedReason();
                     this.lastNeededTilesThisTick = this.tileCache.neededTilesThisTick();
                     this.lastNewlyEnqueuedTilesThisTick = this.tileCache.newlyEnqueuedTilesThisTick();
                     this.lastEnqueueSkippedDuplicateThisTick = this.tileCache.enqueueSkippedDuplicateThisTick();
                     this.lastEnqueueSkippedQueueFullThisTick = this.tileCache.enqueueSkippedQueueFullThisTick();
                     this.lastEnqueueSkippedCooldownThisTick = this.tileCache.enqueueSkippedCooldownThisTick();
                     this.lastVisibleTilesDroppedThisTick = this.tileCache.visibleTilesDroppedThisTick();
                     this.lastPriorityCenterTileCountThisTick = this.tileCache.priorityCenterTileCountThisTick();
                     this.lastUnloadedTilesInViewportThisTick = this.tileCache.unloadedTilesInViewportThisTick();
                     this.lastRetryCooldownTilesThisTick = this.tileCache.retryCooldownTilesThisTick();
                     this.lastRetryReadyTilesThisTick = this.tileCache.retryReadyTilesThisTick();
                     this.lastResampledAfterChunkLoadedThisTick = this.tileCache.resampledAfterChunkLoadedThisTick();
                     this.lastSampledTileKey = this.tileCache.lastSampledTileKey();
                     this.lastSamplesAttemptedThisTick = this.tileCache.samplesAttemptedThisTick();
                     this.lastSamplesSucceededThisTick = this.tileCache.samplesSucceededThisTick();
                     this.lastSamplesFallbackThisTick = this.tileCache.samplesFallbackThisTick();
                     this.lastSampledTileValidPixelCount = this.tileCache.sampledTileValidPixelCount();
                     this.lastReadyTilesUsedInLastCompose = this.tileCache.readyTilesUsedInLastCompose();
                     this.lastMissingTilesUsedInLastCompose = this.tileCache.missingTilesUsedInLastCompose();
                     this.composeMsAvg = ema(this.composeMsAvg, this.tileCache.lastComposeTimeMs());
                     this.tileUpdateMsAvg = ema(this.tileUpdateMsAvg, this.tileCache.lastTileUpdateMs());
                     this.markerUpdateMsAvg = ema(this.markerUpdateMsAvg, this.markerLayer.stats().lastMarkerUpdateMs());
                  }

                  if (this.tileCache.consumeUploadReady()) {
                     boolean recreated = this.terrainTexture.ensureCapacity(this.tileCache.resolution());
                     if (recreated) {
                        this.textureRecreatedCount++;
                     }

                     this.terrainTexture.upload(this.tileCache.frontPixels());
                     this.textureUploadCount++;
                     this.lastUploadTimeMs = System.currentTimeMillis();
                     this.lastUploadReason = this.tileCache.viewportComposeReason();
                     this.lastTextureResolution = this.tileCache.resolution();
                     this.lastTerrainUpdateTick = nowTick;
                     this.lastUploadTick = nowTick;
                     this.framesSinceLastUpload = 0;
                     this.lastSampleCenterBlockX = this.tileCache.centerBlockX();
                     this.lastSampleCenterBlockZ = this.tileCache.centerBlockZ();
                     this.lastResolution = this.tileCache.resolution();
                     this.lastRangeBlocks = dynamicRangeBlocks;
                     this.lastUseRegionCache = useRegionCache;
                     this.lastTileSize = tileSize;
                     this.lastMaxCachedRegions = maxCachedRegions;
                     this.lastTileUpdatesPerTick = tileUpdatesPerTick;
                     this.lastMaxQueuedTileUpdates = maxQueuedTileUpdates;
                     this.lastMissingTileRetryCooldownTicks = missingTileRetryCooldownTicks;
                     this.lastPrioritizeCenterTiles = prioritizeCenterTiles;
                     this.lastMaxSamplesPerTick = maxSamplesPerTick;
                     this.forcedNextUpdateReason = null;
                  }

                  if (collectDebugTelemetry) {
                     this.lastTicksSinceLastUpload = this.lastUploadTick == Long.MIN_VALUE ? -1 : (int)(nowTick - this.lastUploadTick);
                  }

                  if (debugPerformance || debugCache) {
                     this.updatePerfRates(nowTick);
                  }
               }
            }
         }
      }
   }

   public void render(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float partialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      if (NanoMinimapVisibility.shouldRender(minecraft)) {
         LocalPlayer player = minecraft.player;
         if (player != null) {
            NanoMinimapViewport viewport = createViewport(screenWidth, screenHeight);
            if (viewport.width() > 0 && viewport.height() > 0) {
               boolean debugPerformance = NanoMinimapClientConfig.debugMinimapPerformance();
               boolean debugMarkers = NanoMinimapClientConfig.debugMinimapMarkers();
               boolean debugCache = NanoMinimapClientConfig.debugMinimapCache();
               boolean debugRenderTrace = NanoMinimapClientConfig.debugMinimapRenderTrace();
               boolean debugLogRenderTrace = NanoMinimapClientConfig.debugLogMinimapTraceOncePerSecond();
               boolean debugColors = NanoMinimapClientConfig.debugMinimapColors();
               NanoMinimapRenderTrace activeTrace = !debugRenderTrace && !debugLogRenderTrace ? null : this.renderTrace;
               if (activeTrace != null) {
                  activeTrace.reset();
                  activeTrace.frameIndex = ++this.renderFrameCounter;
                  activeTrace.partialTick = partialTick;
                  activeTrace.screenWidth = screenWidth;
                  activeTrace.screenHeight = screenHeight;
                  activeTrace.minimapScale = NanoMinimapClientConfig.minimapScale();
                  activeTrace.minimapUseHudInertia = NanoMinimapClientConfig.minimapUseHudInertia();
                  activeTrace.rotateWithPlayer = NanoMinimapClientConfig.rotateWithPlayer();
               }

               this.lastBaseViewportX = viewport.x();
               this.lastBaseViewportY = viewport.y();
               boolean debugFreezeHudInertia = NanoMinimapClientConfig.debugFreezeHudInertia();
               boolean debugFreezeMapWorldMovement = NanoMinimapClientConfig.debugFreezeMapWorldMovement();
               boolean debugFreezeTerrainSourceOffset = NanoMinimapClientConfig.debugFreezeTerrainSourceOffset();
               boolean debugDisablePoseStackInMinimap = NanoMinimapClientConfig.debugDisablePoseStackInMinimap();
               boolean debugForceManualViewportCoordinates = NanoMinimapClientConfig.debugForceManualViewportCoordinates();
               boolean debugDrawTerrainBounds = NanoMinimapClientConfig.debugDrawTerrainBounds();
               boolean debugDrawFrameBounds = NanoMinimapClientConfig.debugDrawFrameBounds();
               boolean debugDrawMarkerBounds = NanoMinimapClientConfig.debugDrawMarkerBounds();
               boolean debugDrawScissorBounds = NanoMinimapClientConfig.debugDrawScissorBounds();
               boolean perspectiveEnabled = NanoMinimapClientConfig.minimapPerspectiveEnabled();
               float perspectiveRightSide = (float)NanoMinimapClientConfig.minimapPerspectiveRightSide();
               float perspectiveAxisX = (float)NanoMinimapClientConfig.minimapPerspectiveAxisX();
               float perspectiveAxisY = (float)NanoMinimapClientConfig.minimapPerspectiveAxisY();
               float edgeArcTopBottom = (float)NanoMinimapClientConfig.minimapEdgeArcTopBottom();
               float perspectiveStrength = perspectiveEnabled ? 1.0F : 0.0F;
               float perspectiveTopScale = 1.0F;
               float perspectiveBottomScale = 1.0F;
               float perspectiveXSkew = Mth.clamp(perspectiveRightSide + perspectiveAxisX, -1.5F, 1.5F);
               float perspectiveTilt = 0.0F;
               int perspectiveOffsetX = 0;
               int perspectiveOffsetY = 0;
               boolean perspectiveDebugBounds = NanoMinimapClientConfig.minimapPerspectiveDebugBounds();
               NanosuitHudOverlay.HudInertiaSnapshot rawInertia = NanoMinimapClientConfig.minimapUseHudInertia()
                  ? NanosuitHudOverlay.sharedRightHudInertiaSnapshot()
                  : NanosuitHudOverlay.HudInertiaSnapshot.ZERO;
               NanosuitHudOverlay.HudInertiaSnapshot inertia = debugFreezeHudInertia ? NanosuitHudOverlay.HudInertiaSnapshot.ZERO : rawInertia;
               this.lastHudInertiaOffsetX = inertia.offsetX();
               this.lastHudInertiaOffsetY = inertia.offsetY();
               this.lastHudInertiaRollDeg = inertia.rollDeg();
               float[] finalBounds = NanoMinimapHudTransform.transformedBounds(
                  viewport,
                  screenWidth / 2,
                  screenHeight / 2,
                  inertia.offsetX(),
                  inertia.offsetY(),
                  inertia.rollDeg(),
                  true,
                  perspectiveEnabled ? perspectiveStrength : 0.0F,
                  perspectiveTopScale,
                  perspectiveBottomScale,
                  perspectiveXSkew,
                  perspectiveAxisY,
                  perspectiveTilt,
                  perspectiveOffsetX,
                  perspectiveOffsetY
               );
               this.lastFinalViewportX = Math.round(finalBounds[0]);
               this.lastFinalViewportY = Math.round(finalBounds[1]);
               double renderBlocksPerPixel = this.tileCache.hasFrontData() ? this.tileCache.activeBlocksPerPixel() : logicalBlocksPerPixel();
               if (activeTrace != null) {
                  activeTrace.blocksPerPixel = renderBlocksPerPixel;
               }

               double renderCenterWorldX = this.tileCache.hasFrontData() ? this.tileCache.centerWorldX() : player.getX();
               double renderCenterWorldZ = this.tileCache.hasFrontData() ? this.tileCache.centerWorldZ() : player.getZ();
               NanoMinimapCoordinateMapper mapper = new NanoMinimapCoordinateMapper(
                  renderCenterWorldX, renderCenterWorldZ, renderBlocksPerPixel, viewport.width(), viewport.height()
               );
               double renderPlayerX = Mth.lerp(partialTick, player.xo, player.getX());
               double renderPlayerZ = Mth.lerp(partialTick, player.zo, player.getZ());
               NanoMinimapCoordinateMapper.Point rawFractionalOffset = mapper.playerFractionalOffset(renderPlayerX, renderPlayerZ);
               double terrainOffsetX = rawFractionalOffset.x();
               double terrainOffsetY = rawFractionalOffset.y();
               double markerOffsetX = rawFractionalOffset.x();
               double markerOffsetY = rawFractionalOffset.y();
               if (debugFreezeMapWorldMovement) {
                  terrainOffsetX = 0.0;
                  terrainOffsetY = 0.0;
                  markerOffsetX = 0.0;
                  markerOffsetY = 0.0;
               } else if (debugFreezeTerrainSourceOffset) {
                  terrainOffsetX = 0.0;
                  terrainOffsetY = 0.0;
               }

               this.lastPlayerExactX = renderPlayerX;
               this.lastPlayerExactZ = renderPlayerZ;
               this.lastFractionalOffsetX = renderPlayerX - Mth.floor(renderPlayerX);
               this.lastFractionalOffsetY = renderPlayerZ - Mth.floor(renderPlayerZ);
               this.lastVisualOffsetPixelsX = terrainOffsetX;
               this.lastVisualOffsetPixelsY = terrainOffsetY;
               if (activeTrace != null) {
                  activeTrace.debugFreezeHudInertia = debugFreezeHudInertia;
                  activeTrace.debugFreezeMapWorldMovement = debugFreezeMapWorldMovement;
                  activeTrace.debugFreezeTerrainSourceOffset = debugFreezeTerrainSourceOffset;
                  activeTrace.debugDisablePoseStackInMinimap = debugDisablePoseStackInMinimap;
                  activeTrace.debugForceManualViewportCoordinates = debugForceManualViewportCoordinates;
               }

               long renderStartNanos = System.nanoTime();
               boolean debugSolid = NanoMinimapClientConfig.debugForceSolidTerrain();
               boolean debugChecker = NanoMinimapClientConfig.debugForceCheckerboardTerrain();
               boolean debugTerrainOnly = NanoMinimapClientConfig.debugShowTerrainOnly();
               boolean debugDisableMinimapMask = NanoMinimapClientConfig.debugDisableMinimapMask();
               boolean debugDrawMinimapMaskBounds = NanoMinimapClientConfig.debugDrawMinimapMaskBounds();
               boolean debugTextureOnly = NanoMinimapClientConfig.debugShowOnlyFrontTexture() || debugTerrainOnly;
               this.renderer
                  .render(
                     guiGraphics,
                     viewport,
                     this.terrainTexture.textureId(),
                     NanoMinimapClientConfig.terrainEnabled() && this.terrainTexture.hasTexture() && this.tileCache.hasValidFrontTexture(),
                     this.lastCoverageSize > 0 ? this.lastCoverageSize : Math.max(viewport.width(), viewport.height()),
                     Math.max(16, this.terrainTexture.resolution()),
                     mapper,
                     terrainOffsetX,
                     terrainOffsetY,
                     markerOffsetX,
                     markerOffsetY,
                     NanoMinimapClientConfig.rotateWithPlayer(),
                     player.getYRot(),
                     this.markerLayer.markers(),
                     NanoMinimapClientConfig.minimapClampMarkersToBounds(),
                     (float)NanoMinimapClientConfig.minimapMarkerScale(),
                     (float)NanoMinimapClientConfig.minimapMarkerOpacity(),
                     NanoMinimapClientConfig.minimapUseEntityIcons(),
                     NanoMinimapClientConfig.minimapEntityIconSize(),
                     (float)NanoMinimapClientConfig.minimapEntityIconOpacity(),
                     NanoMinimapClientConfig.minimapFallbackEntityMarkers(),
                     partialTick,
                     NanoMinimapClientConfig.minimapFrameEnabled(),
                     (float)NanoMinimapClientConfig.minimapFrameOpacity(),
                     perspectiveEnabled,
                     perspectiveStrength,
                     perspectiveTopScale,
                     perspectiveBottomScale,
                     perspectiveXSkew,
                     perspectiveAxisY,
                     perspectiveTilt,
                     perspectiveOffsetX,
                     perspectiveOffsetY,
                     edgeArcTopBottom,
                     perspectiveDebugBounds,
                     NanoMinimapClientConfig.debugMinimapBounds(),
                     debugSolid,
                     debugChecker,
                     debugTextureOnly,
                     debugTerrainOnly,
                     NanoMinimapClientConfig.minimapUseHudInertia(),
                     inertia.offsetX(),
                     inertia.offsetY(),
                     inertia.rollDeg(),
                     screenWidth / 2,
                     screenHeight / 2,
                     debugDisablePoseStackInMinimap,
                     debugForceManualViewportCoordinates,
                     debugDrawTerrainBounds,
                     debugDrawFrameBounds,
                     debugDrawMarkerBounds,
                     debugDrawScissorBounds,
                     debugDisableMinimapMask,
                     debugDrawMinimapMaskBounds,
                     activeTrace
                  );
               this.lastRenderMs = (float)(System.nanoTime() - renderStartNanos) / 1000000.0F;
               this.renderMsAvg = ema(this.renderMsAvg, this.lastRenderMs);
               if (this.lastUploadTick != Long.MIN_VALUE) {
                  this.framesSinceLastUpload++;
               }

               if (debugPerformance || debugMarkers || debugCache || debugRenderTrace || debugColors) {
                  NanoMinimapMarkerLayer.Stats markerStats = this.markerLayer.stats();
                  long debugTick = minecraft.level != null ? minecraft.level.getGameTime() : 0L;
                  this.debugOverlay
                     .render(
                        guiGraphics,
                        viewport,
                        debugTick,
                        this.lastUpdateReason,
                        this.tileCache.updateState(),
                        this.tileCache.rebuildRow(),
                        this.tileCache.totalRows(),
                        this.lastSamplesProcessedThisTick,
                        this.lastRebuildTimeMs,
                        this.lastTerrainUpdateTick,
                        this.textureUploadCount,
                        this.textureRecreatedCount,
                        this.lastUploadTimeMs,
                        this.lastTextureResolution,
                        this.lastRangeBlocks,
                        this.tileCache.centerBlockX(),
                        this.tileCache.centerBlockZ(),
                        String.valueOf(this.lastDimension),
                        this.lastPlayerExactX,
                        this.lastPlayerExactZ,
                        this.lastFractionalOffsetX,
                        this.lastFractionalOffsetY,
                        this.lastVisualOffsetPixelsX,
                        this.lastVisualOffsetPixelsY,
                        this.lastRenderMs,
                        markerStats.totalMarkerCount(),
                        markerStats.playerMarkerVisible(),
                        markerStats.targetMarkerVisible(),
                        markerStats.markedEntityCount(),
                        markerStats.nearbyEntityCount(),
                        markerStats.nearbyScanIntervalTicks(),
                        markerStats.lastMarkerUpdateMs(),
                        markerStats.lastNearbyScanCount(),
                        markerStats.rotateWithPlayer(),
                        markerStats.sampleTransformedMarker(),
                        this.tileCache.cacheMode(),
                        this.tileCache.loadedRegionCount(),
                        this.tileCache.loadedTileCount(),
                        this.tileCache.dirtyTileCount(),
                        this.tileCache.queuedTileUpdates(),
                        this.tileCache.tilesProcessedThisTick(),
                        this.tileCache.cacheHits(),
                        this.tileCache.cacheMisses(),
                        this.tileCache.lruEvictions(),
                        this.tileCache.viewportComposeCount(),
                        this.tileCache.viewportComposeReason(),
                        this.tileCache.lastComposeTimeMs(),
                        this.lastUploadReason,
                        this.tileCache.missingChunkSamplesThisTick(),
                        this.tileCache.missingTilesThisTick(),
                        this.tileCache.lastTileUpdateMs(),
                        this.tileCache.playerTileKey(),
                        this.tileCache.playerRegionKey(),
                        this.lastViewportWidth,
                        this.lastViewportHeight,
                        this.lastBaseViewportX,
                        this.lastBaseViewportY,
                        this.lastFinalViewportX,
                        this.lastFinalViewportY,
                        this.lastHudInertiaOffsetX,
                        this.lastHudInertiaOffsetY,
                        this.lastHudInertiaRollDeg,
                        this.lastCoverageSize,
                        this.lastOverscanPadding,
                        this.lastLogicalBlocksPerPixel,
                        this.lastReadyTilesInViewport,
                        this.lastMissingTilesInViewport,
                        this.lastComposeSkippedReason,
                        this.framesSinceLastUpload,
                        this.lastTicksSinceLastUpload,
                        this.lastNeededTilesThisTick,
                        this.lastNewlyEnqueuedTilesThisTick,
                        this.lastEnqueueSkippedDuplicateThisTick,
                        this.lastEnqueueSkippedQueueFullThisTick,
                        this.lastEnqueueSkippedCooldownThisTick,
                        this.lastVisibleTilesDroppedThisTick,
                        this.lastPriorityCenterTileCountThisTick,
                        this.lastUnloadedTilesInViewportThisTick,
                        this.lastRetryCooldownTilesThisTick,
                        this.lastRetryReadyTilesThisTick,
                        this.lastResampledAfterChunkLoadedThisTick,
                        this.lastSampledTileKey,
                        this.lastSamplesAttemptedThisTick,
                        this.lastSamplesSucceededThisTick,
                        this.lastSamplesFallbackThisTick,
                        this.lastSampledTileValidPixelCount,
                        this.lastReadyTilesUsedInLastCompose,
                        this.lastMissingTilesUsedInLastCompose,
                        this.tileCache.composeTriggeredByTileReady(),
                        this.tileCache.composeTriggeredByPlayerMove(),
                        this.tileCache.composeSkippedNoReadyTiles(),
                        this.tileCache.composeSkippedNoChange(),
                        this.renderMsAvg,
                        this.composeMsAvg,
                        this.tileUpdateMsAvg,
                        this.markerUpdateMsAvg,
                        this.uploadsPerSecond,
                        this.composesPerSecond,
                        this.tilesProcessedPerSecond,
                        activeTrace,
                        debugRenderTrace,
                        debugLogRenderTrace,
                        this.lastMapColorCount,
                        this.lastFallbackColorCount,
                        this.lastSpecialCaseColorCount,
                        this.lastWaterColorCount,
                        this.lastLavaColorCount,
                        this.lastHeightShadingAppliedCount,
                        this.lastBiomeTintAppliedCount,
                        this.lastTerrainColorMode,
                        debugColors
                     );
               }
            }
         }
      }
   }

   public void onWorldUnload() {
      this.resetTerrainState("world_unload", true);
      this.lastLevel = null;
      this.lastDimension = null;
      this.lastViewRequest = null;
   }

   public void onClientDisconnect() {
      this.resetTerrainState("client_disconnect", true);
      this.lastLevel = null;
      this.lastDimension = null;
      this.lastViewRequest = null;
   }

   public void onResourceReload() {
      this.renderer.onResourceReload();
      this.resetTerrainState("resource_reload", true);
   }

   private String determineStartReason(NanoMinimapViewRequest viewRequest, boolean debugForceRefresh, long nowTick) {
      ResourceKey<Level> currentDimension = viewRequest.dimension();
      if (this.forcedNextUpdateReason != null) {
         return this.forcedNextUpdateReason;
      } else if (!this.tileCache.hasFrontData()) {
         return "initial_load";
      } else if (this.tileCache.hasPendingTileUpdates(nowTick)) {
         return "tile_updates_pending";
      } else if (debugForceRefresh) {
         return "debug_force_refresh";
      } else if (this.lastDimension != null && this.lastDimension.equals(currentDimension)) {
         if (this.lastResolution != viewRequest.resolution()) {
            return "resolution_changed";
         } else if (this.lastRangeBlocks != viewRequest.rangeBlocks()) {
            return "range_changed";
         } else if (this.lastUseRegionCache != viewRequest.useRegionCache()) {
            return "cache_mode_changed";
         } else if (this.lastTileSize != viewRequest.tileSize()) {
            return "tile_size_changed";
         } else if (this.lastMaxCachedRegions != viewRequest.maxCachedRegions()) {
            return "max_cached_regions_changed";
         } else if (this.lastTileUpdatesPerTick != viewRequest.tileUpdatesPerTick()) {
            return "tile_update_budget_changed";
         } else if (this.lastMaxQueuedTileUpdates != viewRequest.maxQueuedTileUpdates()) {
            return "tile_queue_capacity_changed";
         } else if (this.lastMissingTileRetryCooldownTicks != viewRequest.missingTileRetryCooldownTicks()) {
            return "tile_retry_cooldown_changed";
         } else if (this.lastPrioritizeCenterTiles != viewRequest.prioritizeCenterTiles()) {
            return "tile_priority_mode_changed";
         } else if (this.lastMaxSamplesPerTick != viewRequest.maxSamplesPerTick()) {
            return "tile_samples_budget_changed";
         } else {
            return !viewRequest.sameCoverage(this.lastViewRequest) ? "player_moved" : null;
         }
      } else {
         return "dimension_changed";
      }
   }

   private void resetTerrainState(String reason, boolean closeTexture) {
      if (closeTexture) {
         this.terrainTexture.close();
      }

      this.tileCache.clear();
      this.markerLayer.clear();
      this.lastTerrainUpdateTick = Long.MIN_VALUE;
      this.lastUpdateReason = reason;
      this.forcedNextUpdateReason = reason;
      this.lastSampleCenterBlockX = 0;
      this.lastSampleCenterBlockZ = 0;
      this.lastResolution = 0;
      this.lastRangeBlocks = 0;
      this.lastUseRegionCache = true;
      this.lastTileSize = 16;
      this.lastMaxCachedRegions = 64;
      this.lastTileUpdatesPerTick = 2;
      this.lastMaxQueuedTileUpdates = 512;
      this.lastMissingTileRetryCooldownTicks = 20;
      this.lastPrioritizeCenterTiles = true;
      this.lastMaxSamplesPerTick = 8288;
      this.lastViewRequest = null;
      this.lastUploadReason = "none";
      this.lastUploadTick = Long.MIN_VALUE;
      this.framesSinceLastUpload = 0;
      this.lastSamplesProcessedThisTick = 0;
      this.lastRebuildTimeMs = 0L;
      this.lastPlayerExactX = 0.0;
      this.lastPlayerExactZ = 0.0;
      this.lastFractionalOffsetX = 0.0;
      this.lastFractionalOffsetY = 0.0;
      this.lastVisualOffsetPixelsX = 0.0;
      this.lastVisualOffsetPixelsY = 0.0;
      this.lastRenderMs = 0.0F;
      this.lastViewportWidth = 0;
      this.lastViewportHeight = 0;
      this.lastBaseViewportX = 0;
      this.lastBaseViewportY = 0;
      this.lastFinalViewportX = 0;
      this.lastFinalViewportY = 0;
      this.lastHudInertiaOffsetX = 0.0F;
      this.lastHudInertiaOffsetY = 0.0F;
      this.lastHudInertiaRollDeg = 0.0F;
      this.lastCoverageSize = 0;
      this.lastOverscanPadding = 0;
      this.lastLogicalBlocksPerPixel = 0.0;
      this.lastReadyTilesInViewport = 0;
      this.lastMissingTilesInViewport = 0;
      this.lastComposeSkippedReason = "none";
      this.lastNeededTilesThisTick = 0;
      this.lastNewlyEnqueuedTilesThisTick = 0;
      this.lastEnqueueSkippedDuplicateThisTick = 0;
      this.lastEnqueueSkippedQueueFullThisTick = 0;
      this.lastEnqueueSkippedCooldownThisTick = 0;
      this.lastVisibleTilesDroppedThisTick = 0;
      this.lastPriorityCenterTileCountThisTick = 0;
      this.lastUnloadedTilesInViewportThisTick = 0;
      this.lastRetryCooldownTilesThisTick = 0;
      this.lastRetryReadyTilesThisTick = 0;
      this.lastResampledAfterChunkLoadedThisTick = 0;
      this.lastSampledTileKey = "n/a";
      this.lastSamplesAttemptedThisTick = 0;
      this.lastSamplesSucceededThisTick = 0;
      this.lastSamplesFallbackThisTick = 0;
      this.lastSampledTileValidPixelCount = 0;
      this.lastReadyTilesUsedInLastCompose = 0;
      this.lastMissingTilesUsedInLastCompose = 0;
      this.lastTicksSinceLastUpload = -1;
      this.renderMsAvg = 0.0;
      this.composeMsAvg = 0.0;
      this.tileUpdateMsAvg = 0.0;
      this.markerUpdateMsAvg = 0.0;
      this.uploadsPerSecond = 0.0;
      this.composesPerSecond = 0.0;
      this.tilesProcessedPerSecond = 0.0;
      this.perfWindowStartTick = Long.MIN_VALUE;
      this.perfWindowComposeStartCount = 0L;
      this.perfWindowUploadStartCount = 0;
      this.perfWindowTilesProcessedAccum = 0L;
      this.renderFrameCounter = 0L;
      this.lastMapColorCount = 0;
      this.lastFallbackColorCount = 0;
      this.lastSpecialCaseColorCount = 0;
      this.lastWaterColorCount = 0;
      this.lastLavaColorCount = 0;
      this.lastHeightShadingAppliedCount = 0;
      this.lastBiomeTintAppliedCount = 0;
      this.lastTerrainColorMode = "MEDIUM";
      this.renderTrace.reset();
   }

   private void updatePerfRates(long nowTick) {
      if (this.perfWindowStartTick == Long.MIN_VALUE) {
         this.perfWindowStartTick = nowTick;
         this.perfWindowComposeStartCount = this.tileCache.viewportComposeCount();
         this.perfWindowUploadStartCount = this.textureUploadCount;
         this.perfWindowTilesProcessedAccum = 0L;
      } else {
         this.perfWindowTilesProcessedAccum = this.perfWindowTilesProcessedAccum + this.tileCache.tilesProcessedThisTick();
         long elapsedTicks = nowTick - this.perfWindowStartTick;
         if (elapsedTicks >= 20L) {
            double elapsedSeconds = elapsedTicks / 20.0;
            long composeDelta = this.tileCache.viewportComposeCount() - this.perfWindowComposeStartCount;
            int uploadDelta = this.textureUploadCount - this.perfWindowUploadStartCount;
            this.uploadsPerSecond = uploadDelta / elapsedSeconds;
            this.composesPerSecond = composeDelta / elapsedSeconds;
            this.tilesProcessedPerSecond = this.perfWindowTilesProcessedAccum / elapsedSeconds;
            this.perfWindowStartTick = nowTick;
            this.perfWindowComposeStartCount = this.tileCache.viewportComposeCount();
            this.perfWindowUploadStartCount = this.textureUploadCount;
            this.perfWindowTilesProcessedAccum = 0L;
         }
      }
   }

   private static double ema(double current, double sample) {
      double alpha = 0.2;
      return current <= 0.0 ? sample : current * (1.0 - alpha) + sample * alpha;
   }

   private static double logicalBlocksPerPixel() {
      int baseRangeBlocks = Math.max(16, NanoMinimapClientConfig.minimapRangeBlocks());
      int baseResolution = Math.max(16, NanoMinimapClientConfig.terrainInternalResolution());
      return baseRangeBlocks * 2.0 / baseResolution;
   }

   private static NanoMinimapViewport createViewport(int screenWidth, int screenHeight) {
      float scale = (float)Mth.clamp(NanoMinimapClientConfig.minimapScale(), 0.25, 4.0);
      int width = Math.max(8, Math.round(NanoMinimapClientConfig.minimapWidth() * scale));
      int height = Math.max(8, Math.round(NanoMinimapClientConfig.minimapHeight() * scale));
      int x = 12 + NanoMinimapClientConfig.minimapOffsetX();
      int y = screenHeight - 12 - height - NanoMinimapClientConfig.minimapOffsetY();
      x = Mth.clamp(x, 0, Math.max(0, screenWidth - width));
      y = Mth.clamp(y, 0, Math.max(0, screenHeight - height));
      float opacity = (float)Mth.clamp(NanoMinimapClientConfig.minimapOpacity(), 0.0, 1.0);
      return new NanoMinimapViewport(x, y, width, height, opacity);
   }
}
