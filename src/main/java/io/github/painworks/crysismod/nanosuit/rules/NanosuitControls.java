package io.github.painworks.crysismod.nanosuit.rules;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import java.util.Set;

public record NanosuitControls(Set<ManualMode> manualModes, Set<Ability> contextualAbilities) {
   public NanosuitControls {
      manualModes = Set.copyOf(manualModes);
      contextualAbilities = Set.copyOf(contextualAbilities);
   }

   public boolean allowsMode(NanosuitMode mode) {
      if (mode == null) {
         return true;
      }
      return switch (mode) {
         case NONE -> true;
         case ARMOR -> manualModes.contains(ManualMode.ARMOR);
         case CLOAK -> manualModes.contains(ManualMode.CLOAK);
      };
   }

   public boolean supports(Ability ability) {
      return contextualAbilities.contains(ability);
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
