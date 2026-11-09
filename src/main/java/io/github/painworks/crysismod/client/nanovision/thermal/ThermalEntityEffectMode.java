package io.github.painworks.crysismod.client.nanovision.thermal;

public enum ThermalEntityEffectMode {
   NONE,
   SPECTRAL_MARKER;

   public boolean usesMarkerPass() {
      return this == SPECTRAL_MARKER;
   }
}
