package io.github.painworks.crysismod.client.minimap.cache;

public final class NanoMinimapTile {
   private final NanoMinimapTileKey key;
   private final int[] pixels;
   private boolean ready;
   private boolean dirty;
   private long nextRetryTick;
   private long lastTouchedTick;

   public NanoMinimapTile(NanoMinimapTileKey key, int tileSize) {
      this.key = key;
      this.pixels = new int[tileSize * tileSize];
      this.ready = false;
      this.dirty = true;
      this.nextRetryTick = 0L;
      this.lastTouchedTick = 0L;
   }

   public NanoMinimapTileKey key() {
      return this.key;
   }

   public int[] pixels() {
      return this.pixels;
   }

   public boolean ready() {
      return this.ready;
   }

   public void setReady(boolean value) {
      this.ready = value;
   }

   public boolean dirty() {
      return this.dirty;
   }

   public void setDirty(boolean value) {
      this.dirty = value;
   }

   public long nextRetryTick() {
      return this.nextRetryTick;
   }

   public void setNextRetryTick(long tick) {
      this.nextRetryTick = tick;
   }

   public long lastTouchedTick() {
      return this.lastTouchedTick;
   }

   public void touch(long tick) {
      this.lastTouchedTick = tick;
   }
}
