package io.github.painworks.crysismod.client.minimap.cache;

import io.github.painworks.crysismod.client.minimap.NanoMinimapUpdateState;
import io.github.painworks.crysismod.client.minimap.NanoMinimapViewRequest;
import io.github.painworks.crysismod.client.minimap.sampling.NanoMinimapWorldSampler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class NanoMinimapTileCache {
   private static final int FALLBACK_PIXEL = -15724008;
   private static final Comparator<NanoMinimapTileCache.TileCandidate> TILE_CANDIDATE_COMPARATOR = Comparator.comparingInt(
      NanoMinimapTileCache.TileCandidate::distance
   );
   private static final Comparator<NanoMinimapTileCache.TileCandidate> TILE_CANDIDATE_REVERSE_COMPARATOR = (a, b) -> Integer.compare(b.distance(), a.distance());
   private int resolution;
   private int rangeBlocks;
   private int centerBlockX;
   private int centerBlockZ;
   private double centerWorldX;
   private double centerWorldZ;
   private ResourceKey<Level> dimension;
   private int[] frontPixels = new int[0];
   private int[] stagingPixels = new int[0];
   private boolean hasData;
   private boolean uploadReady;
   private boolean rebuilding;
   private NanoMinimapViewRequest pendingRequest;
   private NanoMinimapViewRequest currentRequest;
   private int composeRow;
   private int lastSamplesProcessedThisTick;
   private long rebuildStartNanos;
   private long lastRebuildTimeMs;
   private final NanoMinimapTileUpdateQueue updateQueue = new NanoMinimapTileUpdateQueue();
   private NanoMinimapRegionCache regionCache = new NanoMinimapRegionCache(64);
   private double blocksPerPixel = 1.0;
   private int viewportStartGlobalPixelX;
   private int viewportStartGlobalPixelZ;
   private int lastTilesProcessedThisTick;
   private int lastDirtyTilesCount;
   private long lastComposedGameTick;
   private long viewportComposeCount;
   private String viewportComposeReason = "none";
   private long lastComposeTimeMs;
   private boolean composeChangedAnyPixel;
   private int lastMissingChunkSamplesThisTick;
   private int lastMissingTilesThisTick;
   private long lastTileUpdateMs;
   private int lastReadyTilesInViewport;
   private int lastMissingTilesInViewport;
   private String composeSkippedReason = "none";
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
   private int composeTriggeredByTileReady;
   private int composeTriggeredByPlayerMove;
   private int composeSkippedNoReadyTiles;
   private int composeSkippedNoChange;
   private int pendingDirtyRetryTilesInView;
   private long nextDirtyRetryTickInView = Long.MAX_VALUE;
   private String cacheMode = "REGION_TILE";
   private final ArrayList<NanoMinimapTileCache.TileCandidate> candidateBuffer = new ArrayList<>(512);
   private final PriorityQueue<NanoMinimapTileCache.TileCandidate> nearestCandidateHeap = new PriorityQueue<>(TILE_CANDIDATE_REVERSE_COMPARATOR);
   private int[] worldXByPixel = new int[0];
   private int[] worldZByPixel = new int[0];
   private int[] chunkXByPixel = new int[0];
   private int[] chunkZByPixel = new int[0];
   private int[] heightRowA = new int[0];
   private int[] heightRowB = new int[0];
   private boolean[] loadedChunkFlags = new boolean[0];

   public void clear() {
      this.resolution = 0;
      this.rangeBlocks = 0;
      this.centerBlockX = 0;
      this.centerBlockZ = 0;
      this.centerWorldX = 0.0;
      this.centerWorldZ = 0.0;
      this.dimension = null;
      this.frontPixels = new int[0];
      this.stagingPixels = new int[0];
      this.hasData = false;
      this.uploadReady = false;
      this.rebuilding = false;
      this.pendingRequest = null;
      this.currentRequest = null;
      this.composeRow = 0;
      this.lastSamplesProcessedThisTick = 0;
      this.rebuildStartNanos = 0L;
      this.lastRebuildTimeMs = 0L;
      this.lastTilesProcessedThisTick = 0;
      this.lastDirtyTilesCount = 0;
      this.lastComposedGameTick = 0L;
      this.viewportComposeCount = 0L;
      this.viewportComposeReason = "none";
      this.lastComposeTimeMs = 0L;
      this.composeChangedAnyPixel = false;
      this.lastMissingChunkSamplesThisTick = 0;
      this.lastMissingTilesThisTick = 0;
      this.lastTileUpdateMs = 0L;
      this.lastReadyTilesInViewport = 0;
      this.lastMissingTilesInViewport = 0;
      this.composeSkippedReason = "none";
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
      this.composeTriggeredByTileReady = 0;
      this.composeTriggeredByPlayerMove = 0;
      this.composeSkippedNoReadyTiles = 0;
      this.composeSkippedNoChange = 0;
      this.pendingDirtyRetryTilesInView = 0;
      this.nextDirtyRetryTickInView = Long.MAX_VALUE;
      this.cacheMode = "REGION_TILE";
      this.blocksPerPixel = 1.0;
      this.viewportStartGlobalPixelX = 0;
      this.viewportStartGlobalPixelZ = 0;
      this.updateQueue.clear();
      this.regionCache.clear();
      this.candidateBuffer.clear();
      this.nearestCandidateHeap.clear();
   }

   public void startRebuild(ClientLevel level, NanoMinimapViewRequest request, String composeReason) {
      if (request != null) {
         int clampedResolution = Math.max(16, request.resolution());
         int requiredPixels = clampedResolution * clampedResolution;
         if (this.frontPixels.length != requiredPixels) {
            this.frontPixels = new int[requiredPixels];
         }

         if (this.stagingPixels.length != requiredPixels) {
            this.stagingPixels = new int[requiredPixels];
         }

         boolean modeChanged = this.currentRequest != null && this.currentRequest.useRegionCache() != request.useRegionCache();
         boolean tileSizeChanged = this.currentRequest != null && this.currentRequest.tileSize() != request.tileSize();
         this.pendingRequest = request;
         this.cacheMode = request.useRegionCache() ? "REGION_TILE" : "WINDOW";
         this.viewportComposeReason = composeReason == null ? "unknown" : composeReason;
         if (!request.useRegionCache() || modeChanged || tileSizeChanged) {
            this.regionCache.clear();
         }

         this.regionCache.setMaxCachedRegions(Math.max(1, request.maxCachedRegions()));
         this.updateQueue.clear();
         this.blocksPerPixel = Math.max(1.0E-6, request.blocksPerPixel());
         this.viewportStartGlobalPixelX = request.viewportStartGlobalPixelX();
         this.viewportStartGlobalPixelZ = request.viewportStartGlobalPixelZ();
         this.composeRow = 0;
         this.lastSamplesProcessedThisTick = 0;
         this.lastTilesProcessedThisTick = 0;
         this.lastDirtyTilesCount = 0;
         this.lastMissingChunkSamplesThisTick = 0;
         this.lastMissingTilesThisTick = 0;
         this.lastTileUpdateMs = 0L;
         this.pendingDirtyRetryTilesInView = 0;
         this.nextDirtyRetryTickInView = Long.MAX_VALUE;
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
         this.uploadReady = false;
         this.rebuilding = true;
         this.composeChangedAnyPixel = false;
         this.composeSkippedReason = "none";
         this.rebuildStartNanos = System.nanoTime();
      }
   }

   public int processRebuildRows(
      ClientLevel level,
      NanoMinimapWorldSampler sampler,
      int rowsPerTick,
      boolean disableTileQueue,
      boolean forceQueueVisibleTiles,
      boolean forceResampleVisibleTiles
   ) {
      this.lastSamplesProcessedThisTick = 0;
      this.lastTilesProcessedThisTick = 0;
      this.lastDirtyTilesCount = 0;
      this.lastMissingChunkSamplesThisTick = 0;
      this.lastMissingTilesThisTick = 0;
      this.lastTileUpdateMs = 0L;
      this.lastReadyTilesInViewport = 0;
      this.lastMissingTilesInViewport = 0;
      this.composeSkippedReason = "none";
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
      if (this.rebuilding && this.pendingRequest != null && rowsPerTick > 0) {
         long tick = level.getGameTime();
         this.collectNeededTiles(level, tick, forceQueueVisibleTiles, forceResampleVisibleTiles);
         if (!disableTileQueue) {
            this.processTileUpdateQueue(level, sampler, tick);
         } else {
            this.composeSkippedReason = "tile_queue_disabled";
         }

         boolean viewChanged = this.currentRequest == null || !this.pendingRequest.sameCoverage(this.currentRequest);
         boolean composeInProgress = this.composeRow > 0 && this.composeRow < this.pendingRequest.resolution();
         if (!viewChanged && this.hasData && this.lastTilesProcessedThisTick <= 0 && !composeInProgress) {
            this.rebuilding = false;
            this.lastRebuildTimeMs = (System.nanoTime() - this.rebuildStartNanos) / 1000000L;
            this.composeSkippedReason = "coalesced_no_tile_updates";
            this.composeSkippedNoChange++;
            return this.lastSamplesProcessedThisTick;
         } else {
            this.composeViewportRows(Math.max(1, rowsPerTick));
            if (this.composeRow >= this.pendingRequest.resolution()) {
               this.viewportComposeCount++;
               this.lastComposeTimeMs = (System.nanoTime() - this.rebuildStartNanos) / 1000000L;
               this.lastComposedGameTick = tick;
               if ("player_moved".equals(this.viewportComposeReason)) {
                  this.composeTriggeredByPlayerMove++;
               }

               if (this.lastTilesProcessedThisTick > 0) {
                  this.composeTriggeredByTileReady++;
               }

               if (this.lastReadyTilesUsedInLastCompose == 0 && this.lastMissingTilesUsedInLastCompose > 0) {
                  this.composeSkippedNoReadyTiles++;
               }

               if (!this.composeChangedAnyPixel && this.hasData) {
                  this.uploadReady = false;
                  this.composeSkippedReason = this.composeChangedAnyPixel ? "none" : "unchanged";
                  this.composeSkippedNoChange++;
               } else {
                  int[] tmp = this.frontPixels;
                  this.frontPixels = this.stagingPixels;
                  this.stagingPixels = tmp;
                  double snappedCenterWorldX = requestSnappedCenterWorldX(this.pendingRequest);
                  double snappedCenterWorldZ = requestSnappedCenterWorldZ(this.pendingRequest);
                  this.resolution = this.pendingRequest.resolution();
                  this.rangeBlocks = this.pendingRequest.rangeBlocks();
                  this.centerWorldX = snappedCenterWorldX;
                  this.centerWorldZ = snappedCenterWorldZ;
                  this.centerBlockX = (int)Math.floor(snappedCenterWorldX);
                  this.centerBlockZ = (int)Math.floor(snappedCenterWorldZ);
                  this.dimension = this.pendingRequest.dimension();
                  this.currentRequest = this.pendingRequest;
                  this.uploadReady = true;
                  this.hasData = true;
                  this.composeSkippedReason = "none";
               }

               this.rebuilding = false;
               this.lastRebuildTimeMs = this.lastComposeTimeMs;
            }

            return this.lastSamplesProcessedThisTick;
         }
      } else {
         return 0;
      }
   }

   private void collectNeededTiles(ClientLevel level, long tick, boolean forceQueueVisibleTiles, boolean forceResampleVisibleTiles) {
      if (this.pendingRequest != null) {
         int tileSize = Math.max(1, this.pendingRequest.tileSize());
         int startTileX = this.pendingRequest.tileRange().startTileX();
         int endTileX = this.pendingRequest.tileRange().endTileX();
         int startTileZ = this.pendingRequest.tileRange().startTileZ();
         int endTileZ = this.pendingRequest.tileRange().endTileZ();
         int centerTileX = Math.floorDiv(this.worldToGlobalPixel(this.pendingRequest.centerWorldX()), tileSize);
         int centerTileZ = Math.floorDiv(this.worldToGlobalPixel(this.pendingRequest.centerWorldZ()), tileSize);
         int maxQueue = Math.max(16, this.pendingRequest.maxQueuedTileUpdates());
         long retryCooldown = Math.max(1L, (long)this.pendingRequest.missingTileRetryCooldownTicks());
         this.pendingDirtyRetryTilesInView = 0;
         this.nextDirtyRetryTickInView = Long.MAX_VALUE;
         this.updateQueue.clear();
         this.candidateBuffer.clear();
         this.nearestCandidateHeap.clear();

         for (int tileZ = startTileZ; tileZ <= endTileZ; tileZ++) {
            for (int tileX = startTileX; tileX <= endTileX; tileX++) {
               this.lastNeededTilesThisTick++;
               NanoMinimapTileKey key = new NanoMinimapTileKey(this.pendingRequest.dimension(), tileX, tileZ);
               NanoMinimapTile tile = this.regionCache.getTile(key);
               if (forceResampleVisibleTiles && tile != null && tile.ready()) {
                  tile.setDirty(true);
                  tile.setNextRetryTick(tick);
               }

               boolean enqueue = false;
               if (tile == null || !tile.ready()) {
                  enqueue = true;
                  this.lastMissingTilesInViewport++;
                  if (this.pendingRequest.loadedChunksOnly() && !this.isTileFullyLoaded(level, key, tileSize)) {
                     this.lastUnloadedTilesInViewportThisTick++;
                  }
               } else if (tile.dirty()) {
                  boolean retryReady = tick >= tile.nextRetryTick();
                  if (!retryReady) {
                     this.lastRetryCooldownTilesThisTick++;
                     if (!forceQueueVisibleTiles && !this.isTileFullyLoaded(level, key, tileSize)) {
                        this.lastEnqueueSkippedCooldownThisTick++;
                     } else {
                        retryReady = true;
                        this.lastResampledAfterChunkLoadedThisTick++;
                     }
                  }

                  if (retryReady) {
                     enqueue = true;
                     this.lastRetryReadyTilesThisTick++;
                  } else {
                     this.pendingDirtyRetryTilesInView++;
                     if (tile.nextRetryTick() < this.nextDirtyRetryTickInView) {
                        this.nextDirtyRetryTickInView = tile.nextRetryTick();
                     }
                  }
               } else {
                  this.lastReadyTilesInViewport++;
               }

               if (enqueue) {
                  int distance = Math.abs(tileX - centerTileX) + Math.abs(tileZ - centerTileZ);
                  if (distance <= 1) {
                     this.lastPriorityCenterTileCountThisTick++;
                  }

                  this.candidateBuffer.add(new NanoMinimapTileCache.TileCandidate(key, distance));
               }
            }
         }

         int totalCandidates = this.candidateBuffer.size();
         int limit = forceQueueVisibleTiles ? totalCandidates : Math.min(maxQueue, totalCandidates);
         if (!forceQueueVisibleTiles && this.pendingRequest.prioritizeCenterTiles() && totalCandidates > limit && limit > 0) {
            this.enqueueClosestCandidatesWithBoundedHeap(limit);
         } else {
            if (this.pendingRequest.prioritizeCenterTiles() && this.candidateBuffer.size() > 1) {
               this.candidateBuffer.sort(TILE_CANDIDATE_COMPARATOR);
            }

            this.enqueueCandidatesLinear(limit);
         }

         this.lastDirtyTilesCount = totalCandidates + this.pendingDirtyRetryTilesInView;
         if (this.nextDirtyRetryTickInView == Long.MAX_VALUE && this.pendingDirtyRetryTilesInView > 0) {
            this.nextDirtyRetryTickInView = tick + retryCooldown;
         }
      }
   }

   private void enqueueCandidatesLinear(int limit) {
      int i = 0;

      for (int size = this.candidateBuffer.size(); i < size; i++) {
         NanoMinimapTileCache.TileCandidate candidate = this.candidateBuffer.get(i);
         if (i >= limit) {
            this.lastVisibleTilesDroppedThisTick++;
            this.lastEnqueueSkippedQueueFullThisTick++;
         } else if (this.updateQueue.enqueue(candidate.key())) {
            this.lastNewlyEnqueuedTilesThisTick++;
         } else {
            this.lastEnqueueSkippedDuplicateThisTick++;
         }
      }
   }

   private void enqueueClosestCandidatesWithBoundedHeap(int limit) {
      this.nearestCandidateHeap.clear();
      int dropped = 0;
      int i = 0;

      for (int size = this.candidateBuffer.size(); i < size; i++) {
         NanoMinimapTileCache.TileCandidate candidate = this.candidateBuffer.get(i);
         if (this.nearestCandidateHeap.size() < limit) {
            this.nearestCandidateHeap.offer(candidate);
         } else {
            NanoMinimapTileCache.TileCandidate worstKept = this.nearestCandidateHeap.peek();
            if (worstKept != null && candidate.distance() < worstKept.distance()) {
               this.nearestCandidateHeap.poll();
               this.nearestCandidateHeap.offer(candidate);
               dropped++;
            } else {
               dropped++;
            }
         }
      }

      this.candidateBuffer.clear();
      this.candidateBuffer.addAll(this.nearestCandidateHeap);
      if (this.candidateBuffer.size() > 1) {
         this.candidateBuffer.sort(TILE_CANDIDATE_COMPARATOR);
      }

      i = 0;

      for (int sizex = this.candidateBuffer.size(); i < sizex; i++) {
         NanoMinimapTileCache.TileCandidate candidate = this.candidateBuffer.get(i);
         if (this.updateQueue.enqueue(candidate.key())) {
            this.lastNewlyEnqueuedTilesThisTick++;
         } else {
            this.lastEnqueueSkippedDuplicateThisTick++;
         }
      }

      this.lastVisibleTilesDroppedThisTick += dropped;
      this.lastEnqueueSkippedQueueFullThisTick += dropped;
      this.nearestCandidateHeap.clear();
   }

   private void processTileUpdateQueue(ClientLevel level, NanoMinimapWorldSampler sampler, long tick) {
      if (this.pendingRequest != null) {
         long startNanos = System.nanoTime();
         int budget = Math.max(1, this.pendingRequest.tileUpdatesPerTick());
         if (this.lastMissingTilesInViewport > budget) {
            int burstBoost = Math.max(1, Math.min(8, this.lastMissingTilesInViewport / Math.max(1, this.pendingRequest.tileSize())));
            budget = Math.min(64, budget + burstBoost);
         }

         int tileCost = Math.max(1, this.pendingRequest.tileSize() * this.pendingRequest.tileSize());
         int samplesBudget = Math.max(tileCost, this.pendingRequest.maxSamplesPerTick());

         for (int i = 0; i < budget && samplesBudget >= tileCost; i++) {
            NanoMinimapTileKey key = this.updateQueue.poll();
            if (key == null) {
               break;
            }

            NanoMinimapTile tile = this.regionCache.getOrCreateTile(key, this.pendingRequest.tileSize());
            int sampled = this.sampleTile(level, sampler, tile, tick);
            samplesBudget -= Math.max(0, sampled);
            tile.touch(tick);
            this.lastTilesProcessedThisTick++;
         }

         this.lastTileUpdateMs = (System.nanoTime() - startNanos) / 1000000L;
      }
   }

   private int sampleTile(ClientLevel level, NanoMinimapWorldSampler sampler, NanoMinimapTile tile, long tick) {
      if (this.pendingRequest == null) {
         return 0;
      } else {
         this.lastSampledTileKey = tile.key().tileX() + "," + tile.key().tileZ();
         int tileSize = this.pendingRequest.tileSize();
         this.ensureSampleBuffers(tileSize);
         int baseGlobalX = tile.key().tileX() * tileSize;
         int baseGlobalZ = tile.key().tileZ() * tileSize;
         MutableBlockPos pos = new MutableBlockPos();
         int minY = level.getMinBuildHeight();
         int maxY = level.getMaxBuildHeight() - 1;
         boolean wasDirty = tile.dirty();
         boolean missingChunks = false;
         int sampledColumns = 0;
         int attemptedColumns = 0;
         int fallbackColumns = 0;
         int validPixelCount = 0;

         for (int px = 0; px < tileSize; px++) {
            int globalPixelX = baseGlobalX + px;
            int worldX = this.globalToWorldBlock(globalPixelX);
            this.worldXByPixel[px] = worldX;
            this.chunkXByPixel[px] = Math.floorDiv(worldX, 16);
         }

         for (int py = 0; py < tileSize; py++) {
            int globalPixelZ = baseGlobalZ + py;
            int worldZ = this.globalToWorldBlock(globalPixelZ);
            this.worldZByPixel[py] = worldZ;
            this.chunkZByPixel[py] = Math.floorDiv(worldZ, 16);
         }

         int chunkMinX = Integer.MAX_VALUE;
         int chunkMaxX = Integer.MIN_VALUE;
         int chunkMinZ = Integer.MAX_VALUE;
         int chunkMaxZ = Integer.MIN_VALUE;

         for (int i = 0; i < tileSize; i++) {
            int cx = this.chunkXByPixel[i];
            int cz = this.chunkZByPixel[i];
            if (cx < chunkMinX) {
               chunkMinX = cx;
            }

            if (cx > chunkMaxX) {
               chunkMaxX = cx;
            }

            if (cz < chunkMinZ) {
               chunkMinZ = cz;
            }

            if (cz > chunkMaxZ) {
               chunkMaxZ = cz;
            }
         }

         int chunkSpanX = Math.max(1, chunkMaxX - chunkMinX + 1);
         int chunkSpanZ = Math.max(1, chunkMaxZ - chunkMinZ + 1);
         int chunkFlagCount = chunkSpanX * chunkSpanZ;
         this.ensureLoadedChunkFlagCapacity(chunkFlagCount);

         for (int z = 0; z < chunkSpanZ; z++) {
            int chunkZ = chunkMinZ + z;
            int rowOffset = z * chunkSpanX;

            for (int x = 0; x < chunkSpanX; x++) {
               int chunkX = chunkMinX + x;
               this.loadedChunkFlags[rowOffset + x] = this.isChunkLoaded(level, chunkX, chunkZ);
            }
         }

         int[] pixels = tile.pixels();
         int[] prevHeights = this.heightRowA;
         int[] currHeights = this.heightRowB;

         for (int py = 0; py < tileSize; py++) {
            int row = py * tileSize;
            int worldZ = this.worldZByPixel[py];
            int chunkZ = this.chunkZByPixel[py];
            int chunkZIndex = chunkZ - chunkMinZ;
            int loadedChunkRowOffset = chunkZIndex * chunkSpanX;

            for (int px = 0; px < tileSize; px++) {
               attemptedColumns++;
               int worldX = this.worldXByPixel[px];
               int chunkX = this.chunkXByPixel[px];
               int chunkXIndex = chunkX - chunkMinX;
               boolean chunkLoaded = this.loadedChunkFlags[loadedChunkRowOffset + chunkXIndex];
               int idx = row + px;
               int prev = pixels[idx];
               int next;
               if (this.pendingRequest.loadedChunksOnly() && !chunkLoaded) {
                  next = tile.ready() && prev != 0 ? prev : -15724008;
                  currHeights[px] = py > 0 ? prevHeights[px] : minY;
                  missingChunks = true;
                  fallbackColumns++;
                  this.lastMissingChunkSamplesThisTick++;
               } else {
                  int topY = level.getHeight(Types.WORLD_SURFACE, worldX, worldZ) - 1;
                  if (topY < minY) {
                     topY = minY;
                  }

                  if (topY > maxY) {
                     topY = maxY;
                  }

                  pos.set(worldX, topY, worldZ);
                  BlockState state = level.getBlockState(pos);
                  int leftHeight = px > 0 ? currHeights[px - 1] : topY;
                  int upHeight = py > 0 ? prevHeights[px] : topY;
                  next = sampler.sampleArgb(level, pos, state, topY, leftHeight, upHeight);
                  currHeights[px] = topY;
                  this.lastSamplesProcessedThisTick++;
                  sampledColumns++;
               }

               pixels[idx] = next;
               if (next != 0 && next != -15724008) {
                  validPixelCount++;
               }

               if (prev != next) {
                  this.composeChangedAnyPixel = true;
               }
            }

            int[] swap = prevHeights;
            prevHeights = currHeights;
            currHeights = swap;
         }

         this.lastSamplesAttemptedThisTick += attemptedColumns;
         this.lastSamplesSucceededThisTick += sampledColumns;
         this.lastSamplesFallbackThisTick += fallbackColumns;
         this.lastSampledTileValidPixelCount = validPixelCount;
         tile.setReady(true);
         tile.setDirty(missingChunks);
         long cooldown = Math.max(1L, (long)this.pendingRequest.missingTileRetryCooldownTicks());
         tile.setNextRetryTick(missingChunks ? tick + cooldown : tick);
         if (!missingChunks && wasDirty) {
            this.lastResampledAfterChunkLoadedThisTick++;
         }

         if (missingChunks) {
            this.lastMissingTilesThisTick++;
         }

         return sampledColumns;
      }
   }

   private void composeViewportRows(int rowsPerTick) {
      if (this.pendingRequest != null) {
         int resolution = this.pendingRequest.resolution();
         int toRow = Math.min(resolution, this.composeRow + rowsPerTick);
         int tileSize = this.pendingRequest.tileSize();
         ResourceKey<Level> dimensionKey = this.pendingRequest.dimension();
         int readyUsed = 0;
         int missingUsed = 0;

         for (int py = this.composeRow; py < toRow; py++) {
            int globalPixelZ = this.viewportStartGlobalPixelZ + py;
            int tileZ = Math.floorDiv(globalPixelZ, tileSize);
            int localZ = Math.floorMod(globalPixelZ, tileSize);
            int outRow = py * resolution;
            int px = 0;
            int globalPixelX = this.viewportStartGlobalPixelX;

            while (px < resolution) {
               int tileX = Math.floorDiv(globalPixelX, tileSize);
               int localX = Math.floorMod(globalPixelX, tileSize);
               int span = Math.min(tileSize - localX, resolution - px);
               NanoMinimapTileKey key = new NanoMinimapTileKey(dimensionKey, tileX, tileZ);
               NanoMinimapTile tile = this.regionCache.getTileNoStats(key);
               if (tile == null) {
                  for (int s = 0; s < span; s++) {
                     int outIdx = outRow + px + s;
                     int next = this.hasData ? this.frontPixels[outIdx] : -15724008;
                     this.stagingPixels[outIdx] = next;
                     if (!this.hasData || this.frontPixels[outIdx] != next) {
                        this.composeChangedAnyPixel = true;
                     }
                  }

                  missingUsed += span;
               } else if (tile.ready()) {
                  int[] tilePixels = tile.pixels();
                  int tileRowOffset = localZ * tileSize + localX;
                  int outOffset = outRow + px;
                  System.arraycopy(tilePixels, tileRowOffset, this.stagingPixels, outOffset, span);
                  if (!this.hasData) {
                     this.composeChangedAnyPixel = true;
                  } else {
                     for (int sx = 0; sx < span; sx++) {
                        int outIdx = outOffset + sx;
                        if (this.frontPixels[outIdx] != this.stagingPixels[outIdx]) {
                           this.composeChangedAnyPixel = true;
                           break;
                        }
                     }
                  }

                  readyUsed += span;
               } else {
                  int[] tilePixels = tile.pixels();
                  int tileRowOffset = localZ * tileSize;

                  for (int sxx = 0; sxx < span; sxx++) {
                     int outIdx = outRow + px + sxx;
                     int tilePixel = tilePixels[tileRowOffset + localX + sxx];
                     int next = tilePixel != 0 ? tilePixel : (this.hasData ? this.frontPixels[outIdx] : -15724008);
                     this.stagingPixels[outIdx] = next;
                     if (!this.hasData || this.frontPixels[outIdx] != next) {
                        this.composeChangedAnyPixel = true;
                     }
                  }

                  missingUsed += span;
               }

               px += span;
               globalPixelX += span;
            }
         }

         this.lastReadyTilesUsedInLastCompose = readyUsed;
         this.lastMissingTilesUsedInLastCompose = missingUsed;
         this.composeRow = toRow;
      }
   }

   private boolean isTileFullyLoaded(ClientLevel level, NanoMinimapTileKey key, int tileSize) {
      int startGlobalX = key.tileX() * tileSize;
      int startGlobalZ = key.tileZ() * tileSize;
      int endGlobalX = startGlobalX + tileSize - 1;
      int endGlobalZ = startGlobalZ + tileSize - 1;
      int worldMinX = this.globalToWorldBlock(startGlobalX);
      int worldMinZ = this.globalToWorldBlock(startGlobalZ);
      int worldMaxX = this.globalToWorldBlock(endGlobalX);
      int worldMaxZ = this.globalToWorldBlock(endGlobalZ);
      int chunkMinX = Math.floorDiv(worldMinX, 16);
      int chunkMinZ = Math.floorDiv(worldMinZ, 16);
      int chunkMaxX = Math.floorDiv(worldMaxX, 16);
      int chunkMaxZ = Math.floorDiv(worldMaxZ, 16);

      for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
         for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
            if (!this.isChunkLoaded(level, chunkX, chunkZ)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean isChunkLoaded(ClientLevel level, int chunkX, int chunkZ) {
      return level.getChunkSource().getChunkNow(chunkX, chunkZ) != null;
   }

   private void ensureSampleBuffers(int tileSize) {
      if (this.worldXByPixel.length < tileSize
         || this.worldZByPixel.length < tileSize
         || this.chunkXByPixel.length < tileSize
         || this.chunkZByPixel.length < tileSize
         || this.heightRowA.length < tileSize
         || this.heightRowB.length < tileSize) {
         this.worldXByPixel = new int[tileSize];
         this.worldZByPixel = new int[tileSize];
         this.chunkXByPixel = new int[tileSize];
         this.chunkZByPixel = new int[tileSize];
         this.heightRowA = new int[tileSize];
         this.heightRowB = new int[tileSize];
      }
   }

   private void ensureLoadedChunkFlagCapacity(int count) {
      if (this.loadedChunkFlags.length < count) {
         this.loadedChunkFlags = new boolean[count];
      }
   }

   private int worldToGlobalPixel(int worldBlock) {
      return (int)Math.floor(worldBlock / this.blocksPerPixel);
   }

   private int worldToGlobalPixel(double worldBlock) {
      return (int)Math.floor(worldBlock / this.blocksPerPixel);
   }

   private int globalToWorldBlock(int globalPixel) {
      return (int)Math.floor(globalPixel * this.blocksPerPixel);
   }

   private static double requestSnappedCenterWorldX(NanoMinimapViewRequest request) {
      if (request == null) {
         return 0.0;
      } else {
         int centerGlobalPixelX = request.viewportStartGlobalPixelX() + request.resolution() / 2;
         return centerGlobalPixelX * request.blocksPerPixel();
      }
   }

   private static double requestSnappedCenterWorldZ(NanoMinimapViewRequest request) {
      if (request == null) {
         return 0.0;
      } else {
         int centerGlobalPixelZ = request.viewportStartGlobalPixelZ() + request.resolution() / 2;
         return centerGlobalPixelZ * request.blocksPerPixel();
      }
   }

   private NanoMinimapViewRequest activeRequest() {
      return this.rebuilding && this.pendingRequest != null ? this.pendingRequest : this.currentRequest;
   }

   public boolean hasFrontData() {
      return this.hasData;
   }

   public boolean hasValidFrontTexture() {
      return this.hasData;
   }

   public boolean isUploadReady() {
      return this.uploadReady;
   }

   public boolean consumeUploadReady() {
      if (!this.uploadReady) {
         return false;
      } else {
         this.uploadReady = false;
         return true;
      }
   }

   public boolean isRebuilding() {
      return this.rebuilding;
   }

   public NanoMinimapUpdateState updateState() {
      if (this.uploadReady) {
         return NanoMinimapUpdateState.UPLOAD_READY;
      } else {
         return this.rebuilding ? NanoMinimapUpdateState.REBUILDING : NanoMinimapUpdateState.IDLE;
      }
   }

   public int rebuildRow() {
      return this.composeRow;
   }

   public int totalRows() {
      return this.pendingRequest != null ? this.pendingRequest.resolution() : 0;
   }

   public int lastSamplesProcessedThisTick() {
      return this.lastSamplesProcessedThisTick;
   }

   public long lastRebuildTimeMs() {
      return this.lastRebuildTimeMs;
   }

   public int[] frontPixels() {
      return this.frontPixels;
   }

   public int resolution() {
      return this.resolution;
   }

   public int centerBlockX() {
      return this.centerBlockX;
   }

   public int centerBlockZ() {
      return this.centerBlockZ;
   }

   public double centerWorldX() {
      return this.centerWorldX;
   }

   public double centerWorldZ() {
      return this.centerWorldZ;
   }

   public double activeBlocksPerPixel() {
      NanoMinimapViewRequest request = this.activeRequest();
      return request != null ? Math.max(1.0E-6, request.blocksPerPixel()) : Math.max(1.0E-6, this.blocksPerPixel);
   }

   public boolean hasPendingTileUpdates(long currentTick) {
      return this.updateQueue.size() > 0 ? true : this.pendingDirtyRetryTilesInView > 0 && currentTick >= this.nextDirtyRetryTickInView;
   }

   public int loadedRegionCount() {
      return this.regionCache.regionCount();
   }

   public int loadedTileCount() {
      return this.regionCache.tileCount();
   }

   public int dirtyTileCount() {
      return this.lastDirtyTilesCount + this.pendingDirtyRetryTilesInView;
   }

   public int queuedTileUpdates() {
      return this.updateQueue.size();
   }

   public int tilesProcessedThisTick() {
      return this.lastTilesProcessedThisTick;
   }

   public int cacheHits() {
      return this.regionCache.cacheHits();
   }

   public int cacheMisses() {
      return this.regionCache.cacheMisses();
   }

   public int lruEvictions() {
      return this.regionCache.lruEvictions();
   }

   public long viewportComposeCount() {
      return this.viewportComposeCount;
   }

   public String viewportComposeReason() {
      return this.viewportComposeReason;
   }

   public long lastComposeTimeMs() {
      return this.lastComposeTimeMs;
   }

   public String cacheMode() {
      return this.cacheMode;
   }

   public String playerTileKey() {
      NanoMinimapViewRequest request = this.activeRequest();
      if (request == null) {
         return "n/a";
      } else {
         int tileX = Math.floorDiv(this.worldToGlobalPixel(request.centerWorldX()), Math.max(1, request.tileSize()));
         int tileZ = Math.floorDiv(this.worldToGlobalPixel(request.centerWorldZ()), Math.max(1, request.tileSize()));
         return tileX + "," + tileZ;
      }
   }

   public String playerRegionKey() {
      NanoMinimapViewRequest request = this.activeRequest();
      if (request != null && request.dimension() != null) {
         int tileX = Math.floorDiv(this.worldToGlobalPixel(request.centerWorldX()), Math.max(1, request.tileSize()));
         int tileZ = Math.floorDiv(this.worldToGlobalPixel(request.centerWorldZ()), Math.max(1, request.tileSize()));
         NanoMinimapRegionKey key = NanoMinimapRegionCache.regionKey(request.dimension(), tileX, tileZ);
         return key.regionX() + "," + key.regionZ();
      } else {
         return "n/a";
      }
   }

   public int missingChunkSamplesThisTick() {
      return this.lastMissingChunkSamplesThisTick;
   }

   public int missingTilesThisTick() {
      return this.lastMissingTilesThisTick;
   }

   public long lastTileUpdateMs() {
      return this.lastTileUpdateMs;
   }

   public int readyTilesInViewport() {
      return this.lastReadyTilesInViewport;
   }

   public int missingTilesInViewport() {
      return this.lastMissingTilesInViewport;
   }

   public String composeSkippedReason() {
      return this.composeSkippedReason;
   }

   public int neededTilesThisTick() {
      return this.lastNeededTilesThisTick;
   }

   public int newlyEnqueuedTilesThisTick() {
      return this.lastNewlyEnqueuedTilesThisTick;
   }

   public int enqueueSkippedDuplicateThisTick() {
      return this.lastEnqueueSkippedDuplicateThisTick;
   }

   public int enqueueSkippedQueueFullThisTick() {
      return this.lastEnqueueSkippedQueueFullThisTick;
   }

   public int enqueueSkippedCooldownThisTick() {
      return this.lastEnqueueSkippedCooldownThisTick;
   }

   public int visibleTilesDroppedThisTick() {
      return this.lastVisibleTilesDroppedThisTick;
   }

   public int priorityCenterTileCountThisTick() {
      return this.lastPriorityCenterTileCountThisTick;
   }

   public int unloadedTilesInViewportThisTick() {
      return this.lastUnloadedTilesInViewportThisTick;
   }

   public int retryCooldownTilesThisTick() {
      return this.lastRetryCooldownTilesThisTick;
   }

   public int retryReadyTilesThisTick() {
      return this.lastRetryReadyTilesThisTick;
   }

   public int resampledAfterChunkLoadedThisTick() {
      return this.lastResampledAfterChunkLoadedThisTick;
   }

   public String lastSampledTileKey() {
      return this.lastSampledTileKey;
   }

   public int samplesAttemptedThisTick() {
      return this.lastSamplesAttemptedThisTick;
   }

   public int samplesSucceededThisTick() {
      return this.lastSamplesSucceededThisTick;
   }

   public int samplesFallbackThisTick() {
      return this.lastSamplesFallbackThisTick;
   }

   public int sampledTileValidPixelCount() {
      return this.lastSampledTileValidPixelCount;
   }

   public int readyTilesUsedInLastCompose() {
      return this.lastReadyTilesUsedInLastCompose;
   }

   public int missingTilesUsedInLastCompose() {
      return this.lastMissingTilesUsedInLastCompose;
   }

   public int composeTriggeredByTileReady() {
      return this.composeTriggeredByTileReady;
   }

   public int composeTriggeredByPlayerMove() {
      return this.composeTriggeredByPlayerMove;
   }

   public int composeSkippedNoReadyTiles() {
      return this.composeSkippedNoReadyTiles;
   }

   public int composeSkippedNoChange() {
      return this.composeSkippedNoChange;
   }

   private record TileCandidate(NanoMinimapTileKey key, int distance) {
   }
}
