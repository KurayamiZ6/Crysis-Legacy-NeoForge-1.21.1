package io.github.painworks.crysismod.nanosuit;

public enum NanosuitProfile {
   LEGACY_CURRENT(20, 10.0F);

   private final int modeCooldownTicks;
   private final float enhancedJumpEnergyCost;

   NanosuitProfile(int modeCooldownTicks, float enhancedJumpEnergyCost) {
      this.modeCooldownTicks = modeCooldownTicks;
      this.enhancedJumpEnergyCost = enhancedJumpEnergyCost;
   }

   public int modeCooldownTicks() {
      return modeCooldownTicks;
   }

   public float enhancedJumpEnergyCost() {
      return enhancedJumpEnergyCost;
   }
}
