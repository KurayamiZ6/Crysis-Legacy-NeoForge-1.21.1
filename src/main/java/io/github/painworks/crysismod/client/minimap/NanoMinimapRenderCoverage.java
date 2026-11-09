package io.github.painworks.crysismod.client.minimap;

public record NanoMinimapRenderCoverage(int viewportWidth, int viewportHeight, int coverageSizePixels, int overscanPaddingPixels, boolean rotateWithPlayer) {
   public static NanoMinimapRenderCoverage of(int viewportWidth, int viewportHeight, boolean rotateWithPlayer, int overscanPaddingPixels) {
      int vw = Math.max(8, viewportWidth);
      int vh = Math.max(8, viewportHeight);
      int pad = Math.max(0, overscanPaddingPixels);
      int base = Math.max(vw, vh);
      int diagonal = (int)Math.ceil(Math.hypot(vw, vh));
      int coverage = (rotateWithPlayer ? diagonal : base) + pad * 2;
      coverage = Math.max(base, coverage);
      return new NanoMinimapRenderCoverage(vw, vh, coverage, pad, rotateWithPlayer);
   }
}
