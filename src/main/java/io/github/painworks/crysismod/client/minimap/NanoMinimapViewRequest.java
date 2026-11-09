package io.github.painworks.crysismod.client.minimap;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record NanoMinimapViewRequest(
   ResourceKey<Level> dimension,
   int centerBlockX,
   int centerBlockZ,
   double centerWorldX,
   double centerWorldZ,
   int rangeBlocks,
   int resolution,
   boolean loadedChunksOnly,
   boolean useRegionCache,
   int tileSize,
   int maxCachedRegions,
   int tileUpdatesPerTick,
   int maxQueuedTileUpdates,
   int missingTileRetryCooldownTicks,
   boolean prioritizeCenterTiles,
   int maxSamplesPerTick,
   double blocksPerPixel,
   int viewportStartGlobalPixelX,
   int viewportStartGlobalPixelZ,
   NanoMinimapTileRange tileRange
) {
   public boolean sameCoverage(NanoMinimapViewRequest other) {
      return other != null
         && this.dimension.equals(other.dimension)
         && this.resolution == other.resolution
         && this.viewportStartGlobalPixelX == other.viewportStartGlobalPixelX
         && this.viewportStartGlobalPixelZ == other.viewportStartGlobalPixelZ;
   }
}
