package io.github.painworks.crysismod.client.nanovision;

public enum ThermalVisualPreset {
   CLASSIC,
   CLASSIC_IMPROVED,
   CLASSIC_IMPROVED_V2,
   CLASSIC_IMPROVED_V3,
   CINEMATIC;

   public boolean usesImprovedRelief() {
      return this == CLASSIC_IMPROVED || this == CLASSIC_IMPROVED_V2 || this == CLASSIC_IMPROVED_V3;
   }

   public int improvedReliefMode() {
      if (this.usesImprovedV2Model()) {
         return 2;
      } else {
         return this == CLASSIC_IMPROVED ? 1 : 0;
      }
   }

   public boolean usesImprovedV2Model() {
      return this == CLASSIC_IMPROVED_V2 || this == CLASSIC_IMPROVED_V3;
   }

   public boolean usesOptimizedV3Pipeline() {
      return this == CLASSIC_IMPROVED_V3;
   }
}
