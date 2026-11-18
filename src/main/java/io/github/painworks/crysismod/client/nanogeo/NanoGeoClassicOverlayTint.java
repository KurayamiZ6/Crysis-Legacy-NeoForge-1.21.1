package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitArmorModeOverlayState.OverlayRenderState;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

/** Recolors the local N1 overlay after animation and composition, preserving its alpha. */
final class NanoGeoClassicOverlayTint {
   private static final Map<String, TintedTexture> TEXTURES = new HashMap<>();

   private NanoGeoClassicOverlayTint() {}

   static ResourceLocation resolve(ResourceLocation source, long revision, OverlayRenderState overlay, String layer) {
      if (source == null || !overlay.classicMode()) {
         return source;
      }
      var manager = Minecraft.getInstance().getTextureManager();
      if (!(manager.getTexture(source) instanceof DynamicTexture dynamic) || dynamic.getPixels() == null) {
         return source;
      }
      NativeImage pixels = dynamic.getPixels();
      TintedTexture tinted = TEXTURES.get(layer);
      if (tinted == null || tinted.pixels.getWidth() != pixels.getWidth() || tinted.pixels.getHeight() != pixels.getHeight()) {
         if (tinted != null) {
            manager.release(tinted.location);
         }
         tinted = new TintedTexture(layer, pixels.getWidth(), pixels.getHeight());
         TEXTURES.put(layer, tinted);
      }
      int rgb = overlay.pulseGlowColorRgb();
      if (tinted.source != pixels || tinted.revision != revision || tinted.rgb != rgb) {
         for (int y = 0; y < pixels.getHeight(); y++) {
            for (int x = 0; x < pixels.getWidth(); x++) {
               tinted.pixels.setPixelRGBA(x, y, recolor(pixels.getPixelRGBA(x, y), rgb));
            }
         }
         tinted.texture.upload();
         tinted.source = pixels;
         tinted.revision = revision;
         tinted.rgb = rgb;
      }
      return tinted.location;
   }

   // NativeImage uses ABGR. Retain the strongest emitted channel and the exact alpha.
   static int recolor(int abgr, int rgb) {
      int intensity = Math.max(abgr & 255, Math.max(abgr >>> 8 & 255, abgr >>> 16 & 255));
      int red = intensity * (rgb >>> 16 & 255) / 255;
      int green = intensity * (rgb >>> 8 & 255) / 255;
      int blue = intensity * (rgb & 255) / 255;
      return abgr & 0xFF000000 | blue << 16 | green << 8 | red;
   }

   static void clear() {
      var manager = Minecraft.getInstance().getTextureManager();
      for (TintedTexture tinted : TEXTURES.values()) {
         manager.release(tinted.location);
      }
      TEXTURES.clear();
   }

   private static final class TintedTexture {
      private final ResourceLocation location;
      private final NativeImage pixels;
      private final DynamicTexture texture;
      private NativeImage source;
      private long revision;
      private int rgb;

      private TintedTexture(String layer, int width, int height) {
         location = ResourceLocation.fromNamespaceAndPath("crysismod", "dynamic/classic_overlay_tint/" + layer);
         pixels = new NativeImage(width, height, false);
         texture = new DynamicTexture(pixels);
         texture.setFilter(false, false);
         Minecraft.getInstance().getTextureManager().register(location, texture);
      }
   }
}
