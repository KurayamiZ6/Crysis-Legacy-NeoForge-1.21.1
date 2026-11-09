package io.github.painworks.crysismod.client.minimap.marker;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class NanoMinimapEntityIconAtlas {
   public static final NanoMinimapEntityIconAtlas EMPTY = new NanoMinimapEntityIconAtlas(
      ResourceLocation.fromNamespaceAndPath("minecraft", "textures/missing.png"), 16, 16, Map.of()
   );
   private final ResourceLocation textureId;
   private final int textureWidth;
   private final int textureHeight;
   private final Map<ResourceLocation, NanoMinimapEntityIcon> iconsByEntityType;

   public NanoMinimapEntityIconAtlas(
      ResourceLocation textureId, int textureWidth, int textureHeight, Map<ResourceLocation, NanoMinimapEntityIcon> iconsByEntityType
   ) {
      this.textureId = textureId;
      this.textureWidth = Math.max(1, textureWidth);
      this.textureHeight = Math.max(1, textureHeight);
      this.iconsByEntityType = Map.copyOf(iconsByEntityType);
   }

   public ResourceLocation textureId() {
      return this.textureId;
   }

   public int textureWidth() {
      return this.textureWidth;
   }

   public int textureHeight() {
      return this.textureHeight;
   }

   public NanoMinimapEntityIcon resolve(ResourceLocation entityTypeId) {
      return entityTypeId == null ? null : this.iconsByEntityType.get(entityTypeId);
   }

   public int iconCount() {
      return this.iconsByEntityType.size();
   }
}
