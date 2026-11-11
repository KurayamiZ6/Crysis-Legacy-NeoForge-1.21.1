package io.github.painworks.crysismod.nanosuit;

import io.github.painworks.crysismod.nanosuit.rules.NanosuitAuxiliaryRules;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitCombatRules;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitControls;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitControls.Ability;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitControls.ManualMode;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitEnergyRules;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitMobilityRules;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public enum NanosuitProfile {
   LEGACY_CURRENT(
      NanosuitGeneration.LEGACY_CURRENT,
      new NanosuitControls(
         Set.of(ManualMode.ARMOR, ManualMode.CLOAK),
         Set.of(Ability.POWER_JUMP, Ability.POWER_SPRINT, Ability.POWER_SWIM, Ability.POWER_MELEE)
      ),
      20,
      NanosuitEnergyRules.LEGACY_CURRENT,
      NanosuitMobilityRules.LEGACY_CURRENT,
      NanosuitCombatRules.LEGACY_CURRENT,
      NanosuitAuxiliaryRules.LEGACY_CURRENT
   ),
   NANOSUIT_1(
      NanosuitGeneration.NANOSUIT_1,
      new NanosuitControls(
         Set.of(ManualMode.ARMOR, ManualMode.STRENGTH, ManualMode.SPEED, ManualMode.CLOAK),
         LEGACY_CURRENT.controls.contextualAbilities(),
         Map.of(
            Ability.POWER_JUMP, NanosuitMode.STRENGTH,
            Ability.POWER_MELEE, NanosuitMode.STRENGTH,
            Ability.POWER_SPRINT, NanosuitMode.SPEED,
            Ability.POWER_SWIM, NanosuitMode.SPEED
         )
      ),
      LEGACY_CURRENT
   ),
   NANOSUIT_2(NanosuitGeneration.NANOSUIT_2, LEGACY_CURRENT);

   private static final Map<NanosuitGeneration, NanosuitProfile> ACTIVE_BY_GENERATION = new EnumMap<>(NanosuitGeneration.class);

   static {
      for (NanosuitProfile profile : values()) {
         ACTIVE_BY_GENERATION.put(profile.generation, profile);
      }
   }

   private final NanosuitGeneration generation;
   private final NanosuitControls controls;
   private final int modeCooldownTicks;
   private final NanosuitEnergyRules energy;
   private final NanosuitMobilityRules mobility;
   private final NanosuitCombatRules combat;
   private final NanosuitAuxiliaryRules auxiliary;

   NanosuitProfile(NanosuitGeneration generation, NanosuitProfile rules) {
      this(generation, rules.controls, rules);
   }

   NanosuitProfile(NanosuitGeneration generation, NanosuitControls controls, NanosuitProfile rules) {
      this(generation, controls, rules.modeCooldownTicks, rules.energy, rules.mobility, rules.combat, rules.auxiliary);
   }

   NanosuitProfile(NanosuitGeneration generation, NanosuitControls controls, int modeCooldownTicks,
      NanosuitEnergyRules energy, NanosuitMobilityRules mobility, NanosuitCombatRules combat, NanosuitAuxiliaryRules auxiliary) {
      this.generation = generation;
      this.controls = controls;
      this.modeCooldownTicks = modeCooldownTicks;
      this.energy = energy;
      this.mobility = mobility;
      this.combat = combat;
      this.auxiliary = auxiliary;
   }

   public static NanosuitProfile forGeneration(NanosuitGeneration generation) {
      return ACTIVE_BY_GENERATION.get(generation);
   }

   public NanosuitGeneration generation() {
      return generation;
   }

   public NanosuitControls controls() {
      return controls;
   }

   public int modeCooldownTicks() {
      return modeCooldownTicks;
   }

   public float enhancedJumpEnergyCost() {
      return mobility.jump().energyCost();
   }

   public NanosuitEnergyRules energy() {
      return energy;
   }

   public NanosuitMobilityRules mobility() {
      return mobility;
   }

   public NanosuitCombatRules combat() {
      return combat;
   }

   public NanosuitAuxiliaryRules auxiliary() {
      return auxiliary;
   }
}
