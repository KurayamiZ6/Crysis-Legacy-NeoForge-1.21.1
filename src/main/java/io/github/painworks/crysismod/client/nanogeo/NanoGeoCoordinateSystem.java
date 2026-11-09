package io.github.painworks.crysismod.client.nanogeo;

public final class NanoGeoCoordinateSystem {
   public static final float GEO_UNIT = 0.0625F;

   private NanoGeoCoordinateSystem() {
   }

   public static float modelUnits(float geoUnits) {
      return geoUnits * 0.0625F;
   }

   public static float geoPivotX(float pivotX) {
      return -pivotX;
   }

   public static float geoPivotY(float pivotY) {
      return pivotY;
   }

   public static float geoPivotZ(float pivotZ) {
      return pivotZ;
   }

   public static float geoBoneRotationXRad(float rotationXDeg) {
      return (float)Math.toRadians(-rotationXDeg);
   }

   public static float geoBoneRotationYRad(float rotationYDeg) {
      return (float)Math.toRadians(-rotationYDeg);
   }

   public static float geoBoneRotationZRad(float rotationZDeg) {
      return (float)Math.toRadians(rotationZDeg);
   }

   public static float geoPoseTranslationX(float modelPartX) {
      return -modelUnits(modelPartX);
   }

   public static float geoPoseTranslationY(float modelPartY) {
      return modelUnits(modelPartY);
   }

   public static float geoPoseTranslationZ(float modelPartZ) {
      return modelUnits(modelPartZ);
   }
}
