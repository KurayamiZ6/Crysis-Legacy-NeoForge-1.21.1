package io.github.painworks.crysismod.client.nanovision.thermal;

public enum ThermalMobProfile {
   GENERIC_COLD(0.34F, 0.74F, ThermalEmissionClass.COLD),
   GENERIC_COOL(0.4F, 0.64F, ThermalEmissionClass.COOL),
   WARM_BLOODED(0.8F, 0.1F, ThermalEmissionClass.WARM),
   CLOTHED_WARM(0.86F, 0.12F, ThermalEmissionClass.WARM),
   UNDEAD_COOL(0.4F, 0.64F, ThermalEmissionClass.COOL),
   SKELETAL_COLD(0.28F, 0.82F, ThermalEmissionClass.COLD),
   SLIME_GEL(0.44F, 0.68F, ThermalEmissionClass.COOL),
   ARTHROPOD_COOL(0.42F, 0.58F, ThermalEmissionClass.COOL),
   GENERIC_HOT(0.88F, 0.08F, ThermalEmissionClass.HOT),
   MAGMATIC_HOT(0.96F, 0.04F, ThermalEmissionClass.HOT),
   BLAZING_HOT(0.98F, 0.02F, ThermalEmissionClass.HOT_EMITTER),
   UNKNOWN_MEDIUM(0.58F, 0.36F, ThermalEmissionClass.UNKNOWN);

   private final float baseHeat;
   private final float coldInsulation;
   private final ThermalEmissionClass emissionClass;

   private ThermalMobProfile(float baseHeat, float coldInsulation, ThermalEmissionClass emissionClass) {
      this.baseHeat = baseHeat;
      this.coldInsulation = coldInsulation;
      this.emissionClass = emissionClass;
   }

   public float baseHeat() {
      return this.baseHeat;
   }

   public float coldInsulation() {
      return this.coldInsulation;
   }

   public ThermalEmissionClass emissionClass() {
      return this.emissionClass;
   }

   public static ThermalMobProfile defaultFor(ThermalEmissionClass emissionClass) {
      return switch (emissionClass) {
         case COLD -> GENERIC_COLD;
         case COOL -> GENERIC_COOL;
         case WARM -> WARM_BLOODED;
         case HOT -> GENERIC_HOT;
         case HOT_EMITTER -> BLAZING_HOT;
         case UNKNOWN -> UNKNOWN_MEDIUM;
      };
   }
}
