package io.github.painworks.crysismod.client.nanovision.thermal;

public record ThermalLayerProfile(float baseHeat, float coldInsulation, ThermalEmissionClass emissionClass) {
   public static ThermalLayerProfile of(ThermalMobProfile profile) {
      return new ThermalLayerProfile(profile.baseHeat(), profile.coldInsulation(), profile.emissionClass());
   }

   public static ThermalLayerProfile insulatedOuter(ThermalMobProfile profile, float heatDrop, float coldBoost) {
      return new ThermalLayerProfile(clamp(profile.baseHeat() - heatDrop), clamp(profile.coldInsulation() + coldBoost), profile.emissionClass());
   }

   private static float clamp(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }
}
