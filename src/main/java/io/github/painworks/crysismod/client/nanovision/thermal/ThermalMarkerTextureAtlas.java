package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

public final class ThermalMarkerTextureAtlas implements ResourceManagerReloadListener {
   public static final ThermalMarkerTextureAtlas INSTANCE = new ThermalMarkerTextureAtlas();
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int PAGE_SIZE = 1024;
   private static final int PADDING = 1;
   private static final int MAX_PAGES = 4;
   private final Map<ResourceLocation, ThermalMarkerTextureAtlas.Region> regions = new HashMap<>();
   private final Set<ResourceLocation> fallbackTextures = new HashSet<>();
   private final List<ThermalMarkerTextureAtlas.Page> pages = new ArrayList<>();

   private ThermalMarkerTextureAtlas() {
   }

   public synchronized ThermalMarkerTextureAtlas.Region resolve(ResourceLocation textureLocation) {
      ThermalMarkerTextureAtlas.Region cached = this.regions.get(textureLocation);
      if (cached != null) {
         return cached;
      } else if (!this.fallbackTextures.contains(textureLocation) && RenderSystem.isOnRenderThreadOrInit()) {
         ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
         ResourceLocation metadataLocation = ResourceLocation.fromNamespaceAndPath(textureLocation.getNamespace(), textureLocation.getPath() + ".mcmeta");
         if (resourceManager.getResource(metadataLocation).isPresent()) {
            return this.reject(textureLocation, "texture has metadata/animation");
         } else {
            Optional<Resource> resource = resourceManager.getResource(textureLocation);
            if (resource.isEmpty()) {
               return this.reject(textureLocation, "texture is dynamic or not resource-backed");
            } else {
               try {
                  ThermalMarkerTextureAtlas.Region var19;
                  try (InputStream stream = resource.get().open()) {
                     NativeImage source = NativeImage.read(stream);

                     label172: {
                        ThermalMarkerTextureAtlas.Region var22;
                        label173: {
                           ThermalMarkerTextureAtlas.Region region;
                           try {
                              int width = source.getWidth();
                              int height = source.getHeight();
                              if (width <= 0 || height <= 0 || width + 2 > 1024 || height + 2 > 1024) {
                                 var19 = this.reject(textureLocation, "texture does not fit an atlas page");
                                 break label172;
                              }

                              ThermalMarkerTextureAtlas.Placement placement = null;
                              ThermalMarkerTextureAtlas.Page page = null;

                              for (ThermalMarkerTextureAtlas.Page candidate : this.pages) {
                                 placement = candidate.allocate(width, height);
                                 if (placement != null) {
                                    page = candidate;
                                    break;
                                 }
                              }

                              if (page == null && this.pages.size() < 4) {
                                 page = new ThermalMarkerTextureAtlas.Page(this.pages.size());
                                 this.pages.add(page);
                                 placement = page.allocate(width, height);
                              }

                              if (page != null && placement != null) {
                                 page.upload(source, placement);
                                 region = new ThermalMarkerTextureAtlas.Region(page.location, placement.innerX, placement.innerY, width, height);
                                 this.regions.put(textureLocation, region);
                                 if (NanovisionConfig.nanovisionDebug()) {
                                    LOGGER.info(
                                       "[CrysisMod][NanovisionAtlas] packed {} ({}x{}) into page {} at {},{}",
                                       new Object[]{textureLocation, width, height, page.index, placement.innerX, placement.innerY}
                                    );
                                 }

                                 var22 = region;
                                 break label173;
                              }

                              region = this.reject(textureLocation, "all atlas pages are full");
                           } catch (Throwable var16) {
                              if (source != null) {
                                 try {
                                    source.close();
                                 } catch (Throwable var15) {
                                    var16.addSuppressed(var15);
                                 }
                              }

                              throw var16;
                           }

                           if (source != null) {
                              source.close();
                           }

                           return region;
                        }

                        if (source != null) {
                           source.close();
                        }

                        return var22;
                     }

                     if (source != null) {
                        source.close();
                     }
                  }

                  return var19;
               } catch (RuntimeException | IOException var18) {
                  this.fallbackTextures.add(textureLocation);
                  LOGGER.debug("[CrysisMod][NanovisionAtlas] keeping {} on per-texture fallback", textureLocation, var18);
                  return null;
               }
            }
         }
      } else {
         return null;
      }
   }

   public synchronized int pageCount() {
      return this.pages.size();
   }

   public synchronized int regionCount() {
      return this.regions.size();
   }

   public synchronized int fallbackTextureCount() {
      return this.fallbackTextures.size();
   }

   public synchronized void onResourceManagerReload(ResourceManager resourceManager) {
      ThermalMarkerBufferSource.reset();
      TextureManager textureManager = Minecraft.getInstance().getTextureManager();

      for (ThermalMarkerTextureAtlas.Page page : this.pages) {
         textureManager.release(page.location);
      }

      this.pages.clear();
      this.regions.clear();
      this.fallbackTextures.clear();
   }

   private ThermalMarkerTextureAtlas.Region reject(ResourceLocation textureLocation, String reason) {
      this.fallbackTextures.add(textureLocation);
      if (NanovisionConfig.nanovisionDebug()) {
         LOGGER.info("[CrysisMod][NanovisionAtlas] {} uses per-texture fallback: {}", textureLocation, reason);
      }

      return null;
   }

   private static final class Page {
      private final int index;
      private final ResourceLocation location;
      private final DynamicTexture texture;
      private int cursorX;
      private int cursorY;
      private int rowHeight;

      private Page(int index) {
         this.index = index;
         this.location = ResourceLocation.fromNamespaceAndPath("crysismod", "dynamic/nanovision_thermal_atlas_" + index);
         this.texture = new DynamicTexture(1024, 1024, true);
         this.texture.setFilter(false, false);
         Minecraft.getInstance().getTextureManager().register(this.location, this.texture);
      }

      private ThermalMarkerTextureAtlas.Placement allocate(int width, int height) {
         int paddedWidth = width + 2;
         int paddedHeight = height + 2;
         if (this.cursorX + paddedWidth > 1024) {
            this.cursorX = 0;
            this.cursorY = this.cursorY + this.rowHeight;
            this.rowHeight = 0;
         }

         if (this.cursorY + paddedHeight > 1024) {
            return null;
         } else {
            ThermalMarkerTextureAtlas.Placement placement = new ThermalMarkerTextureAtlas.Placement(
               this.cursorX, this.cursorY, this.cursorX + 1, this.cursorY + 1, paddedWidth, paddedHeight, width, height
            );
            this.cursorX += paddedWidth;
            this.rowHeight = Math.max(this.rowHeight, paddedHeight);
            return placement;
         }
      }

      private void upload(NativeImage source, ThermalMarkerTextureAtlas.Placement placement) {
         NativeImage padded = new NativeImage(placement.paddedWidth, placement.paddedHeight, true);

         try {
            for (int y = 0; y < placement.paddedHeight; y++) {
               int sourceY = Math.floorMod(y - 1, placement.sourceHeight);

               for (int x = 0; x < placement.paddedWidth; x++) {
                  int sourceX = Math.floorMod(x - 1, placement.sourceWidth);
                  padded.setPixelRGBA(x, y, source.getPixelRGBA(sourceX, sourceY));
               }
            }

            int previousTexture = GL11.glGetInteger(32873);

            try {
               this.texture.bind();
               padded.upload(0, placement.paddedX, placement.paddedY, 0, 0, placement.paddedWidth, placement.paddedHeight, false, true, false, false);
            } finally {
               GlStateManager._bindTexture(previousTexture);
            }
         } catch (Throwable var14) {
            try {
               padded.close();
            } catch (Throwable var12) {
               var14.addSuppressed(var12);
            }

            throw var14;
         }

         padded.close();
      }
   }

   private record Placement(int paddedX, int paddedY, int innerX, int innerY, int paddedWidth, int paddedHeight, int sourceWidth, int sourceHeight) {
   }

   public record Region(ResourceLocation atlasTexture, int pixelX, int pixelY, int pixelWidth, int pixelHeight) {
      public float remapU(float sourceU) {
         return (this.pixelX + sourceU * this.pixelWidth) / 1024.0F;
      }

      public float remapV(float sourceV) {
         return (this.pixelY + sourceV * this.pixelHeight) / 1024.0F;
      }
   }
}
