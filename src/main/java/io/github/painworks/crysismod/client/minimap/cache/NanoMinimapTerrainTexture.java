package io.github.painworks.crysismod.client.minimap.cache;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette;
import io.github.painworks.crysismod.nanosuit.client.ClassicMalfunction;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;

public final class NanoMinimapTerrainTexture {
   private static final ResourceLocation TEXTURE_ID = ResourceLocation.fromNamespaceAndPath("crysismod", "dynamic/nano_minimap_terrain");
   private DynamicTexture dynamicTexture;
   private NativeImage pixels;
   private int resolution;
   private boolean hasTexture;
   private boolean classicPalette;
   private boolean nanosuit2Palette;
   private int classicColor=ClassicHudPalette.DEFAULT;
   private int[] sourcePixels;

   public void setPalette(boolean enabled,boolean nanosuit2){
      int theme=classicColor;
      if(enabled) theme=ClassicMalfunction.active()?0xeb9c9e:NanosuitClientConfig.classicInterfaceColor();
      if(classicPalette!=enabled || nanosuit2Palette!=nanosuit2 || enabled&&classicColor!=theme){
         classicPalette=enabled;
         nanosuit2Palette=nanosuit2;
         classicColor=theme;
         if(sourcePixels!=null)upload(sourcePixels);
      }
   }

   public boolean ensureCapacity(int targetResolution) {
      int clampedResolution = Math.max(16, targetResolution);
      if (this.dynamicTexture != null && this.resolution == clampedResolution && this.pixels != null) {
         return false;
      } else {
         this.close();
         this.pixels = new NativeImage(clampedResolution, clampedResolution, false);
         this.dynamicTexture = new DynamicTexture(this.pixels);
         this.dynamicTexture.setFilter(true, false);
         this.resolution = clampedResolution;
         TextureManager textureManager = Minecraft.getInstance().getTextureManager();
         textureManager.register(TEXTURE_ID, this.dynamicTexture);
         this.hasTexture = false;
         return true;
      }
   }

   public void upload(int[] argbPixels) {
      if (this.pixels != null && this.dynamicTexture != null && argbPixels != null) {
         int side = this.resolution;
         if (argbPixels.length >= side * side) {
            sourcePixels=argbPixels.clone();
            for (int y = 0; y < side; y++) {
               int row = y * side;

               for (int x = 0; x < side; x++) {
                  int color=argbPixels[row+x];
                  if(classicPalette)color=ClassicHudPalette.terrainColor(color,classicColor);
                  else if(nanosuit2Palette) color=nanosuit2Color(color,argbPixels[row+Math.min(x+1,side-1)],argbPixels[Math.min(y+1,side-1)*side+x]);
                  this.pixels.setPixelRGBA(x, y, argbToAbgr(color));
               }
            }

            this.dynamicTexture.upload();
            this.hasTexture = true;
         }
      }
   }

   public ResourceLocation textureId() {
      return TEXTURE_ID;
   }

   public boolean hasTexture() {
      return this.hasTexture;
   }

   public int resolution() {
      return this.resolution;
   }

   public void close() {
      sourcePixels=null;
      this.hasTexture = false;
      this.resolution = 0;
      if (this.dynamicTexture != null) {
         this.dynamicTexture.close();
         this.dynamicTexture = null;
      }

      if (this.pixels != null) {
         this.pixels.close();
         this.pixels = null;
      }
   }

   private static int nanosuit2Color(int argb,int right,int below) {
      int luminance=((argb>>>16&255)*54+(argb>>>8&255)*183+(argb&255)*19)>>8;
      int edge=Math.max(Math.abs(luminance-luminance(right)),Math.abs(luminance-luminance(below)));
      int green=Math.min(205,34+luminance*55/255+Math.max(0,edge-14)*2);
      return argb&0xff000000|(green*82/100)<<16|green<<8|green*93/100;
   }

   private static int luminance(int argb) {
      return ((argb>>>16&255)*54+(argb>>>8&255)*183+(argb&255)*19)>>8;
   }

   private static int argbToAbgr(int argb) {
      int a = argb >>> 24 & 0xFF;
      int r = argb >>> 16 & 0xFF;
      int g = argb >>> 8 & 0xFF;
      int b = argb & 0xFF;
      return a << 24 | b << 16 | g << 8 | r;
   }
}
