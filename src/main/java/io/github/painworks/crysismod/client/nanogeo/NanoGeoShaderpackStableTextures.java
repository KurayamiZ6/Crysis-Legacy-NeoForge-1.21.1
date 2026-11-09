package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.slf4j.Logger;

public final class NanoGeoShaderpackStableTextures {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String DYNAMIC_TEXTURE_PATH_PREFIX = "dynamic/shaderpack_stable/";
   private static final Map<ResourceLocation, NanoGeoShaderpackStableTextures.CachedTexture> SANITIZED_TEXTURES = new ConcurrentHashMap<>();

   private NanoGeoShaderpackStableTextures() {
   }

   public static String resolveArmorTexturePath(String fallbackTexturePath) {
      if (fallbackTexturePath != null && !fallbackTexturePath.isBlank()) {
         ResourceLocation sourceTextureId = ResourceLocation.tryParse(fallbackTexturePath);
         return sourceTextureId != null && NanoGeoShaderpackCompatibility.shouldUseArmorModeCompatibilityFallback()
            ? resolveSanitizedTexture(sourceTextureId).toString()
            : fallbackTexturePath;
      } else {
         return fallbackTexturePath;
      }
   }

   public static ResourceLocation resolveBaseArmorTexture() {
      return !NanoGeoShaderpackCompatibility.shouldUseArmorModeCompatibilityFallback()
         ? NanoGeoArmorTextures.BASE_ARMOR
         : resolveSanitizedTexture(NanoGeoArmorTextures.BASE_ARMOR);
   }

   public static int sanitizeTransparentPixel(int pixel) {
      return (pixel >>> 24 & 0xFF) == 0 ? 0 : pixel;
   }

   public static void sanitizeTransparentPixels(NativeImage image) {
      if (image != null) {
         int width = image.getWidth();
         int height = image.getHeight();

         for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
               if ((image.getPixelRGBA(x, y) >>> 24 & 0xFF) == 0) {
                  image.setPixelRGBA(x, y, 0);
               }
            }
         }
      }
   }

   public static void close() {
      for (NanoGeoShaderpackStableTextures.CachedTexture texture : SANITIZED_TEXTURES.values()) {
         texture.close();
      }

      SANITIZED_TEXTURES.clear();
   }

   private static ResourceLocation resolveSanitizedTexture(ResourceLocation sourceTextureId) {
      NanoGeoShaderpackStableTextures.CachedTexture cachedTexture = SANITIZED_TEXTURES.computeIfAbsent(
         sourceTextureId, NanoGeoShaderpackStableTextures::createSanitizedTexture
      );
      return cachedTexture != null ? cachedTexture.textureId() : sourceTextureId;
   }

   private static NanoGeoShaderpackStableTextures.CachedTexture createSanitizedTexture(ResourceLocation sourceTextureId) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft == null) {
         return new NanoGeoShaderpackStableTextures.CachedTexture(sourceTextureId, null);
      } else {
         Resource resource = (Resource)minecraft.getResourceManager().getResource(sourceTextureId).orElse(null);
         if (resource == null) {
            return new NanoGeoShaderpackStableTextures.CachedTexture(sourceTextureId, null);
         } else {
            try {
               NanoGeoShaderpackStableTextures.CachedTexture var8;
               try (InputStream inputStream = resource.open()) {
                  NativeImage pixels = NativeImage.read(inputStream);
                  sanitizeTransparentPixels(pixels);
                  ResourceLocation dynamicTextureId = dynamicTextureId(sourceTextureId);
                  DynamicTexture dynamicTexture = new DynamicTexture(pixels);
                  TextureManager textureManager = minecraft.getTextureManager();
                  textureManager.register(dynamicTextureId, dynamicTexture);
                  var8 = new NanoGeoShaderpackStableTextures.CachedTexture(dynamicTextureId, dynamicTexture);
               }

               return var8;
            } catch (RuntimeException | IOException var11) {
               LOGGER.warn("[CrysisMod][NanoGeo] Failed to create shaderpack-stable texture copy for {}", sourceTextureId, var11);
               return new NanoGeoShaderpackStableTextures.CachedTexture(sourceTextureId, null);
            }
         }
      }
   }

   private static ResourceLocation dynamicTextureId(ResourceLocation sourceTextureId) {
      String namespace = sourceTextureId.getNamespace();
      String path = sourceTextureId.getPath().replace('/', '_').replace('\\', '_').replace('.', '_');
      return ResourceLocation.fromNamespaceAndPath("crysismod", "dynamic/shaderpack_stable/" + namespace + "_" + path);
   }

   private record CachedTexture(ResourceLocation textureId, DynamicTexture texture) {
      private void close() {
         if (this.texture != null) {
            this.texture.close();
         }
      }
   }
}
