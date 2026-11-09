package io.github.painworks.crysismod.client.minimap;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record NanoMinimapView(
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
   int maxSamplesPerTick
) {
   public double blocksPerPixel() {
      return Math.max(1.0E-6, this.rangeBlocks * 2.0 / Math.max(1, this.resolution));
   }

   public NanoMinimapViewRequest toRequest() {
      double bpp = this.blocksPerPixel();
      int centerGlobalPixelX = (int)Math.floor(this.centerWorldX / bpp);
      int centerGlobalPixelZ = (int)Math.floor(this.centerWorldZ / bpp);
      int viewportStartGlobalPixelX = centerGlobalPixelX - this.resolution / 2;
      int viewportStartGlobalPixelZ = centerGlobalPixelZ - this.resolution / 2;
      int viewportEndGlobalPixelX = viewportStartGlobalPixelX + this.resolution - 1;
      int viewportEndGlobalPixelZ = viewportStartGlobalPixelZ + this.resolution - 1;
      int startTileX = Math.floorDiv(viewportStartGlobalPixelX, this.tileSize);
      int endTileX = Math.floorDiv(viewportEndGlobalPixelX, this.tileSize);
      int startTileZ = Math.floorDiv(viewportStartGlobalPixelZ, this.tileSize);
      int endTileZ = Math.floorDiv(viewportEndGlobalPixelZ, this.tileSize);
      return new NanoMinimapViewRequest(
         this.dimension,
         this.centerBlockX,
         this.centerBlockZ,
         this.centerWorldX,
         this.centerWorldZ,
         this.rangeBlocks,
         this.resolution,
         this.loadedChunksOnly,
         this.useRegionCache,
         this.tileSize,
         this.maxCachedRegions,
         this.tileUpdatesPerTick,
         this.maxQueuedTileUpdates,
         this.missingTileRetryCooldownTicks,
         this.prioritizeCenterTiles,
         this.maxSamplesPerTick,
         bpp,
         viewportStartGlobalPixelX,
         viewportStartGlobalPixelZ,
         new NanoMinimapTileRange(startTileX, endTileX, startTileZ, endTileZ)
      );
   }
}
