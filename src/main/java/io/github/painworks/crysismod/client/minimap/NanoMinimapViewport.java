package io.github.painworks.crysismod.client.minimap;

public record NanoMinimapViewport(int x, int y, int width, int height, float opacity) {
   public int maxX() {
      return this.x + this.width;
   }

   public int maxY() {
      return this.y + this.height;
   }
}
