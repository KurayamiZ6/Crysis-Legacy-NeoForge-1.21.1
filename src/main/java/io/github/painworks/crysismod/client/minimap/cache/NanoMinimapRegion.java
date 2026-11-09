package io.github.painworks.crysismod.client.minimap.cache;

import java.util.HashMap;
import java.util.Map;

public final class NanoMinimapRegion {
   private final NanoMinimapRegionKey key;
   private final Map<NanoMinimapTileKey, NanoMinimapTile> tiles = new HashMap<>();

   public NanoMinimapRegion(NanoMinimapRegionKey key) {
      this.key = key;
   }

   public NanoMinimapRegionKey key() {
      return this.key;
   }

   public NanoMinimapTile getTile(NanoMinimapTileKey tileKey) {
      return this.tiles.get(tileKey);
   }

   public NanoMinimapTile getOrCreateTile(NanoMinimapTileKey tileKey, int tileSize) {
      NanoMinimapTile tile = this.tiles.get(tileKey);
      if (tile != null) {
         return tile;
      } else {
         NanoMinimapTile created = new NanoMinimapTile(tileKey, tileSize);
         this.tiles.put(tileKey, created);
         return created;
      }
   }

   public int tileCount() {
      return this.tiles.size();
   }
}
