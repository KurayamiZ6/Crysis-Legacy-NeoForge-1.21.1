package io.github.painworks.crysismod.client.minimap.cache;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class NanoMinimapTileUpdateQueue {
   private final ArrayDeque<NanoMinimapTileKey> deque = new ArrayDeque<>();
   private final Set<NanoMinimapTileKey> inQueue = new HashSet<>();

   public void clear() {
      this.deque.clear();
      this.inQueue.clear();
   }

   public boolean enqueue(NanoMinimapTileKey key) {
      if (!this.inQueue.add(key)) {
         return false;
      } else {
         this.deque.addLast(key);
         return true;
      }
   }

   public NanoMinimapTileKey poll() {
      NanoMinimapTileKey key = this.deque.pollFirst();
      if (key != null) {
         this.inQueue.remove(key);
      }

      return key;
   }

   public int size() {
      return this.deque.size();
   }
}
