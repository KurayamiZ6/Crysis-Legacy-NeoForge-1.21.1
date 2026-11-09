package io.github.painworks.crysismod.client.nanovision;

public enum NanovisionMode {
   OFF,
   GRAYSCALE_NIGHT,
   THERMAL_MARKER,
   DEBUG_MARKER,
   DEBUG_DISTANCE;

   public boolean isActive() {
      return this != OFF;
   }

   public boolean usesMarkerPass() {
      return this == THERMAL_MARKER || this == DEBUG_MARKER || this == DEBUG_DISTANCE;
   }
}
