package io.github.painworks.crysismod.nanosuit;

import io.github.painworks.crysismod.nanosuit.rules.NanosuitControls;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitControls.Ability;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitControls.ManualMode;
import java.util.Set;

public enum NanosuitProfile {
   LEGACY_CURRENT(
      NanosuitGeneration.LEGACY_CURRENT,
      new NanosuitControls(
         Set.of(ManualMode.ARMOR, ManualMode.CLOAK),
         Set.of(Ability.POWER_JUMP, Ability.POWER_SPRINT, Ability.POWER_SWIM, Ability.POWER_MELEE)
      ),
      20,
      10.0F
   );

   private final NanosuitGeneration generation;
   private final NanosuitControls controls;
   private final int modeCooldownTicks;
   private final float enhancedJumpEnergyCost;

   NanosuitProfile(NanosuitGeneration generation, NanosuitControls controls, int modeCooldownTicks, float enhancedJumpEnergyCost) {
      this.generation = generation;
      this.controls = controls;
      this.modeCooldownTicks = modeCooldownTicks;
      this.enhancedJumpEnergyCost = enhancedJumpEnergyCost;
   }

   public static NanosuitProfile forGeneration(NanosuitGeneration generation) {
      return generation == LEGACY_CURRENT.generation ? LEGACY_CURRENT : null;
   }

   public NanosuitControls controls() {
      return controls;
   }

   public int modeCooldownTicks() {
      return modeCooldownTicks;
   }

   public float enhancedJumpEnergyCost() {
      return enhancedJumpEnergyCost;
   }
}
