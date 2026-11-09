package io.github.painworks.crysismod.client.nanogeo;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Direction;

public final class NanoGeoCube {
   private final float originX;
   private final float originY;
   private final float originZ;
   private final float sizeX;
   private final float sizeY;
   private final float sizeZ;
   private final float pivotX;
   private final float pivotY;
   private final float pivotZ;
   private final float rotationX;
   private final float rotationY;
   private final float rotationZ;
   private final float inflate;
   private final boolean mirror;
   private final NanoGeoCube.NanoGeoUvMode uvMode;
   private final float boxUvU;
   private final float boxUvV;
   private final Map<Direction, NanoGeoCube.NanoGeoFaceUv> faceUvs;

   public NanoGeoCube(
      float originX,
      float originY,
      float originZ,
      float sizeX,
      float sizeY,
      float sizeZ,
      float pivotX,
      float pivotY,
      float pivotZ,
      float rotationX,
      float rotationY,
      float rotationZ,
      float inflate,
      boolean mirror,
      NanoGeoCube.NanoGeoUvMode uvMode,
      float boxUvU,
      float boxUvV,
      Map<Direction, NanoGeoCube.NanoGeoFaceUv> faceUvs
   ) {
      this.originX = originX;
      this.originY = originY;
      this.originZ = originZ;
      this.sizeX = sizeX;
      this.sizeY = sizeY;
      this.sizeZ = sizeZ;
      this.pivotX = pivotX;
      this.pivotY = pivotY;
      this.pivotZ = pivotZ;
      this.rotationX = rotationX;
      this.rotationY = rotationY;
      this.rotationZ = rotationZ;
      this.inflate = inflate;
      this.mirror = mirror;
      this.uvMode = uvMode;
      this.boxUvU = boxUvU;
      this.boxUvV = boxUvV;
      this.faceUvs = faceUvs;
   }

   public float originX() {
      return this.originX;
   }

   public float originY() {
      return this.originY;
   }

   public float originZ() {
      return this.originZ;
   }

   public float sizeX() {
      return this.sizeX;
   }

   public float sizeY() {
      return this.sizeY;
   }

   public float sizeZ() {
      return this.sizeZ;
   }

   public float pivotX() {
      return this.pivotX;
   }

   public float pivotY() {
      return this.pivotY;
   }

   public float pivotZ() {
      return this.pivotZ;
   }

   public float rotationX() {
      return this.rotationX;
   }

   public float rotationY() {
      return this.rotationY;
   }

   public float rotationZ() {
      return this.rotationZ;
   }

   public float inflate() {
      return this.inflate;
   }

   public boolean mirror() {
      return this.mirror;
   }

   public NanoGeoCube.NanoGeoUvMode uvMode() {
      return this.uvMode;
   }

   public float boxUvU() {
      return this.boxUvU;
   }

   public float boxUvV() {
      return this.boxUvV;
   }

   public Map<Direction, NanoGeoCube.NanoGeoFaceUv> faceUvs() {
      return this.faceUvs;
   }

   public static EnumMap<Direction, NanoGeoCube.NanoGeoFaceUv> emptyFaceUvMap() {
      return new EnumMap<>(Direction.class);
   }

   public record NanoGeoFaceUv(float u, float v, float uSize, float vSize, int uvRotation) {
   }

   public static enum NanoGeoUvMode {
      BOX,
      PER_FACE;
   }
}
