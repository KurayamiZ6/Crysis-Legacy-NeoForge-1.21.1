package io.github.painworks.crysismod.client.minimap.marker;

import net.minecraft.resources.ResourceLocation;

public record NanoMinimapMarker(
   NanoMinimapMarker.Type type,
   double worldX,
   double worldY,
   double worldZ,
   float yawDeg,
   int rgbColor,
   String label,
   ResourceLocation entityTypeId,
   int entityId,
   double distance,
   float opacityMultiplier,
   boolean clamped,
   long lastSeenTick
) {
   public static NanoMinimapMarker player(double worldX, double worldY, double worldZ, float yawDeg, int rgbColor, long tick) {
      return new NanoMinimapMarker(NanoMinimapMarker.Type.PLAYER, worldX, worldY, worldZ, yawDeg, rgbColor, "player", null, -1, 0.0, 1.0F, false, tick);
   }

   public static NanoMinimapMarker entity(
      NanoMinimapMarker.Type type,
      double worldX,
      double worldY,
      double worldZ,
      float yawDeg,
      int rgbColor,
      String label,
      ResourceLocation entityTypeId,
      int entityId,
      double distance,
      long tick
   ) {
      return entity(type, worldX, worldY, worldZ, yawDeg, rgbColor, label, entityTypeId, entityId, distance, tick, 1.0F);
   }

   public static NanoMinimapMarker entity(
      NanoMinimapMarker.Type type,
      double worldX,
      double worldY,
      double worldZ,
      float yawDeg,
      int rgbColor,
      String label,
      ResourceLocation entityTypeId,
      int entityId,
      double distance,
      long tick,
      float opacityMultiplier
   ) {
      return new NanoMinimapMarker(type, worldX, worldY, worldZ, yawDeg, rgbColor, label, entityTypeId, entityId, distance, opacityMultiplier, false, tick);
   }

   public NanoMinimapMarker withClamped(boolean value) {
      return new NanoMinimapMarker(
         this.type,
         this.worldX,
         this.worldY,
         this.worldZ,
         this.yawDeg,
         this.rgbColor,
         this.label,
         this.entityTypeId,
         this.entityId,
         this.distance,
         this.opacityMultiplier,
         value,
         this.lastSeenTick
      );
   }

   public NanoMinimapMarker withOpacityMultiplier(float value) {
      return new NanoMinimapMarker(
         this.type,
         this.worldX,
         this.worldY,
         this.worldZ,
         this.yawDeg,
         this.rgbColor,
         this.label,
         this.entityTypeId,
         this.entityId,
         this.distance,
         value,
         this.clamped,
         this.lastSeenTick
      );
   }

   public static enum Type {
      PLAYER,
      TARGET,
      MARKED_ENTITY,
      NEARBY_ENTITY;
   }
}
