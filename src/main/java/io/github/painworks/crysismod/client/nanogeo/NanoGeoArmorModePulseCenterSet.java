package io.github.painworks.crysismod.client.nanogeo;

import java.util.List;

public record NanoGeoArmorModePulseCenterSet(
   String texture, String markerTexture, int textureWidth, int textureHeight, List<NanoGeoArmorModePulseCenterSet.Center> centers
) {
   public static final NanoGeoArmorModePulseCenterSet EMPTY = new NanoGeoArmorModePulseCenterSet("", "", 512, 512, List.of());

   public int centerCount() {
      return this.centers.size();
   }

   public record Center(float x, float y, int pixelCount) {
   }
}
