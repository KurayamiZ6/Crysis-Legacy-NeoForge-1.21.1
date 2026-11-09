package io.github.painworks.crysismod.client.nanovision;

public enum ThermalMarkerQuality {
   PERFORMANCE(1, 0.72F, 0.84F, 0.6F, 0.95F),
   BALANCED(2, 1.0F, 1.15F, 0.82F, 1.28F),
   QUALITY(3, 1.28F, 1.48F, 0.92F, 1.52F);

   private final int shaderQuality;
   private final float glowRadiusMultiplier;
   private final float glowStrengthMultiplier;
   private final float diffusionStrength;
   private final float diffusionTapSpacing;

   private ThermalMarkerQuality(int shaderQuality, float glowRadiusMultiplier, float glowStrengthMultiplier, float diffusionStrength, float diffusionTapSpacing) {
      this.shaderQuality = shaderQuality;
      this.glowRadiusMultiplier = glowRadiusMultiplier;
      this.glowStrengthMultiplier = glowStrengthMultiplier;
      this.diffusionStrength = diffusionStrength;
      this.diffusionTapSpacing = diffusionTapSpacing;
   }

   public int shaderQuality() {
      return this.shaderQuality;
   }

   public float glowRadiusMultiplier() {
      return this.glowRadiusMultiplier;
   }

   public float glowStrengthMultiplier() {
      return this.glowStrengthMultiplier;
   }

   public float diffusionStrength() {
      return this.diffusionStrength;
   }

   public float diffusionTapSpacing() {
      return this.diffusionTapSpacing;
   }

   public int hotBlockScanBudget() {
      return switch (this) {
         case PERFORMANCE -> 4096;
         case BALANCED -> 8192;
         case QUALITY -> 12288;
      };
   }

   public int hotBlockRenderCap() {
      return switch (this) {
         case PERFORMANCE -> 256;
         case BALANCED -> 512;
         case QUALITY -> 768;
      };
   }
}
