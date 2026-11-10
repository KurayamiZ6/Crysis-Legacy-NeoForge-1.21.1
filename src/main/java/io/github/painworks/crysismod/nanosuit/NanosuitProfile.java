package io.github.painworks.crysismod.nanosuit;

public enum NanosuitProfile {
   LEGACY_CURRENT(20);

   private final int modeCooldownTicks;

   NanosuitProfile(int modeCooldownTicks) {
      this.modeCooldownTicks = modeCooldownTicks;
   }

   public int modeCooldownTicks() {
      return modeCooldownTicks;
   }
}
