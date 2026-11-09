package io.github.painworks.crysismod.client.minimap.cache;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class NanoMinimapRegionCache {
   private static final int REGION_TILES = 8;
   private final LinkedHashMap<NanoMinimapRegionKey, NanoMinimapRegion> regions = new LinkedHashMap<>(64, 0.75F, true);
   private int maxCachedRegions;
   private int lruEvictions;
   private int evictedTiles;
   private int cacheHits;
   private int cacheMisses;

   public NanoMinimapRegionCache(int maxCachedRegions) {
      this.maxCachedRegions = Math.max(1, maxCachedRegions);
   }

   public void setMaxCachedRegions(int value) {
      this.maxCachedRegions = Math.max(1, value);
      this.evictOverflow();
   }

   public void clear() {
      this.regions.clear();
      this.lruEvictions = 0;
      this.evictedTiles = 0;
      this.cacheHits = 0;
      this.cacheMisses = 0;
   }

   public NanoMinimapTile getTile(NanoMinimapTileKey tileKey) {
      NanoMinimapRegion region = this.regions.get(regionKey(tileKey));
      if (region == null) {
         this.cacheMisses++;
         return null;
      } else {
         NanoMinimapTile tile = region.getTile(tileKey);
         if (tile == null) {
            this.cacheMisses++;
            return null;
         } else {
            this.cacheHits++;
            return tile;
         }
      }
   }

   public NanoMinimapTile getTileNoStats(NanoMinimapTileKey tileKey) {
      NanoMinimapRegion region = this.regions.get(regionKey(tileKey));
      return region == null ? null : region.getTile(tileKey);
   }

   public NanoMinimapTile getOrCreateTile(NanoMinimapTileKey tileKey, int tileSize) {
      NanoMinimapRegionKey regionKey = regionKey(tileKey);
      NanoMinimapRegion region = this.regions.get(regionKey);
      if (region == null) {
         region = new NanoMinimapRegion(regionKey);
         this.regions.put(regionKey, region);
         this.evictOverflow();
      }

      return region.getOrCreateTile(tileKey, tileSize);
   }

   public int regionCount() {
      return this.regions.size();
   }

   public int tileCount() {
      int count = 0;

      for (NanoMinimapRegion region : this.regions.values()) {
         count += region.tileCount();
      }

      return count;
   }

   public int lruEvictions() {
      return this.lruEvictions;
   }

   public int evictedTiles() {
      return this.evictedTiles;
   }

   public int cacheHits() {
      return this.cacheHits;
   }

   public int cacheMisses() {
      return this.cacheMisses;
   }

   public static NanoMinimapRegionKey regionKey(NanoMinimapTileKey tileKey) {
      int regionX = Math.floorDiv(tileKey.tileX(), 8);
      int regionZ = Math.floorDiv(tileKey.tileZ(), 8);
      return new NanoMinimapRegionKey(tileKey.dimension(), regionX, regionZ);
   }

   public static NanoMinimapRegionKey regionKey(ResourceKey<Level> dimension, int tileX, int tileZ) {
      return new NanoMinimapRegionKey(dimension, Math.floorDiv(tileX, 8), Math.floorDiv(tileZ, 8));
   }

   private void evictOverflow() {
      while (this.regions.size() > this.maxCachedRegions) {
         Entry<NanoMinimapRegionKey, NanoMinimapRegion> eldest = this.regions.entrySet().iterator().next();
         NanoMinimapRegion removed = eldest.getValue();
         this.evictedTiles = this.evictedTiles + removed.tileCount();
         this.regions.remove(eldest.getKey());
         this.lruEvictions++;
      }
   }
}
