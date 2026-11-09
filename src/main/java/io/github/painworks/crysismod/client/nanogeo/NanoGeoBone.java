package io.github.painworks.crysismod.client.nanogeo;

import java.util.List;

public final class NanoGeoBone {
   private final String name;
   private final String parentName;
   private final float pivotX;
   private final float pivotY;
   private final float pivotZ;
   private final float rotationX;
   private final float rotationY;
   private final float rotationZ;
   private final List<NanoGeoCube> cubes;
   private final List<NanoGeoBone> children;

   public NanoGeoBone(
      String name,
      String parentName,
      float pivotX,
      float pivotY,
      float pivotZ,
      float rotationX,
      float rotationY,
      float rotationZ,
      List<NanoGeoCube> cubes,
      List<NanoGeoBone> children
   ) {
      this.name = name;
      this.parentName = parentName;
      this.pivotX = pivotX;
      this.pivotY = pivotY;
      this.pivotZ = pivotZ;
      this.rotationX = rotationX;
      this.rotationY = rotationY;
      this.rotationZ = rotationZ;
      this.cubes = cubes;
      this.children = children;
   }

   public String name() {
      return this.name;
   }

   public String parentName() {
      return this.parentName;
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

   public List<NanoGeoCube> cubes() {
      return this.cubes;
   }

   public List<NanoGeoBone> children() {
      return this.children;
   }
}
