package io.github.painworks.crysismod.client.nanogeo;

import io.github.painworks.crysismod.nanosuit.client.render.NanosuitArmorModeOverlayState.OverlayRenderState;
import net.minecraft.resources.ResourceLocation;

public final class OverlayTintQa {
   public static void main(String[] args) {
      int[] samples = {0, 0x00FFFFFF, 0x001122CC, 0x00CC2211, 0x0000FF00, 0x00ADD1ED, 0x007EC6FC};
      int checks = 0;
      for (int alpha = 0; alpha <= 255; alpha++) {
         for (int sample : samples) {
            int pixel = alpha << 24 | sample;
            int red = NanoGeoClassicOverlayTint.recolor(pixel, 0xFF0000);
            int yellow = NanoGeoClassicOverlayTint.recolor(pixel, 0xFFFF00);
            int intensity = Math.max(sample & 255, Math.max(sample >>> 8 & 255, sample >>> 16 & 255));
            if (red >>> 24 != alpha || yellow >>> 24 != alpha
               || (red & 0xFFFFFF) != intensity || (yellow & 0xFFFFFF) != (intensity << 8 | intensity)) {
               throw new AssertionError("Alpha, intensity or hue changed incorrectly");
            }
            checks++;
         }
      }
      var source = ResourceLocation.fromNamespaceAndPath("crysismod", "test/unchanged");
      var n2 = new OverlayRenderState(true, true, source, 1, .9F, 1, .5F, 0, 0, 15728880, 20, 1);
      if (NanoGeoClassicOverlayTint.resolve(source, 1, n2, "base") != source) {
         throw new AssertionError("N2 texture was replaced");
      }
      System.out.println("OVERLAY_TINT_QA_PASS pixelCases=" + checks + " N2TextureIdentity=true");
   }
}
