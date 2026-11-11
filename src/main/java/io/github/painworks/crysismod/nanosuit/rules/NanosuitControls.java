package io.github.painworks.crysismod.nanosuit.rules;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import java.util.Map;
import java.util.Set;

public record NanosuitControls(Set<ManualMode> manualModes, Set<Ability> contextualAbilities, Map<Ability, NanosuitMode> requiredModes) {
   public NanosuitControls(Set<ManualMode> manualModes, Set<Ability> contextualAbilities) {
      this(manualModes, contextualAbilities, Map.of());
   }

   public NanosuitControls {
      manualModes = Set.copyOf(manualModes);
      contextualAbilities = Set.copyOf(contextualAbilities);
      requiredModes = Map.copyOf(requiredModes);
   }

   public boolean allowsMode(NanosuitMode mode) {
      if (mode == null) {
         return true;
      }
      return switch (mode) {
         case NONE -> true;
         case ARMOR -> manualModes.contains(ManualMode.ARMOR);
         case CLOAK -> manualModes.contains(ManualMode.CLOAK);
         case STRENGTH -> manualModes.contains(ManualMode.STRENGTH);
         case SPEED -> manualModes.contains(ManualMode.SPEED);
      };
   }

   public boolean supports(Ability ability) {
      return contextualAbilities.contains(ability);
   }

   public boolean supports(Ability ability, NanosuitMode mode) {
      NanosuitMode required = requiredModes.get(ability);
      return supports(ability) && (required == null || required == mode);
   }

   public enum ManualMode {
      ARMOR,
      STRENGTH,
      SPEED,
      CLOAK
   }

   public enum Ability {
      POWER_JUMP,
      POWER_SPRINT,
      POWER_SWIM,
      POWER_MELEE
   }
}
