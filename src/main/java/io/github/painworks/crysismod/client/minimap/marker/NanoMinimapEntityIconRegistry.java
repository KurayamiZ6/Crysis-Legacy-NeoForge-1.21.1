package io.github.painworks.crysismod.client.minimap.marker;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

public final class NanoMinimapEntityIconRegistry {
   private static final int DEFAULT_ICON_SIZE = 16;
   private static final ResourceLocation ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath("crysismod", "textures/gui/minimap/entity_icons_16.png");
   private static final ResourceLocation ICON_MAPPING = ResourceLocation.fromNamespaceAndPath("crysismod", "minimap/entity_icons_16.json");
   private NanoMinimapEntityIconAtlas atlas = NanoMinimapEntityIconAtlas.EMPTY;
   private boolean loaded;

   public synchronized void invalidate() {
      this.loaded = false;
      this.atlas = NanoMinimapEntityIconAtlas.EMPTY;
   }

   public synchronized void ensureLoaded(ResourceManager resourceManager) {
      if (!this.loaded && resourceManager != null) {
         this.loaded = true;
         this.atlas = loadAtlas(resourceManager);
      }
   }

   public NanoMinimapEntityIconAtlas atlas() {
      return this.atlas;
   }

   public NanoMinimapEntityIcon resolve(ResourceLocation entityTypeId) {
      return this.atlas.resolve(entityTypeId);
   }

   private static NanoMinimapEntityIconAtlas loadAtlas(ResourceManager resourceManager) {
      try {
         Optional<Resource> resourceOptional = resourceManager.getResource(ICON_MAPPING);
         if (resourceOptional.isEmpty()) {
            return NanoMinimapEntityIconAtlas.EMPTY;
         } else {
            JsonObject root;
            try (InputStreamReader reader = new InputStreamReader(resourceOptional.get().open(), StandardCharsets.UTF_8)) {
               root = GsonHelper.parse(reader);
            }

            JsonObject entities = GsonHelper.getAsJsonObject(root, "entities");
            int width = GsonHelper.getAsInt(entities, "width", 256);
            int height = GsonHelper.getAsInt(entities, "height", 256);
            int rows = GsonHelper.getAsInt(entities, "rows", 0);
            HashMap icons = new HashMap();

            for (int rowIndex = 1; rowIndex <= rows; rowIndex++) {
               String rowKey = "row_" + rowIndex;
               if (entities.has(rowKey)) {
                  JsonArray row = GsonHelper.getAsJsonArray(entities, rowKey);
                  int y = (rowIndex - 1) * 16;

                  for (int column = 0; column < row.size(); column++) {
                     JsonElement rowElement = row.get(column);
                     if (rowElement.isJsonObject()) {
                        JsonObject cell = rowElement.getAsJsonObject();
                        String entityTypeId = GsonHelper.getAsString(cell, "entity", "");
                        ResourceLocation entityType = ResourceLocation.tryParse(entityTypeId);
                        if (entityType != null) {
                           int x = column * 16;
                           icons.putIfAbsent(entityType, new NanoMinimapEntityIcon(x, y, 16, 16));
                        }
                     }
                  }
               }
            }

            return new NanoMinimapEntityIconAtlas(ICON_TEXTURE, width, height, icons);
         }
      } catch (Exception var20) {
         return NanoMinimapEntityIconAtlas.EMPTY;
      }
   }
}
