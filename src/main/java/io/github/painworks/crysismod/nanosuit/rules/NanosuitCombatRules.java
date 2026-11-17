package io.github.painworks.crysismod.nanosuit.rules;

public record NanosuitCombatRules(Melee melee, Cloak cloak, ModeDrain armorDrain, float predatorDamageMultiplier) {
   public static final NanosuitCombatRules LEGACY_CURRENT = new NanosuitCombatRules(
      new Melee(15.0F, 2.0F),
      new Cloak(
         new ModeDrain(0.0625F, 0.25F, 0.5F),
         new Detection(1.5, 4.0, 9.0, 12.0, 40),
         new CloakCost(100.0F, true),
         new CloakCost(7.0F, false),
         new CloakCost(20.0F, true)
      ),
      new ModeDrain(0.0625F, 0.083333336F, 0.25F),
      1.5F
   );
   public static final NanosuitCombatRules CLASSIC = new NanosuitCombatRules(
      new Melee(10.0F, LEGACY_CURRENT.melee.damageMultiplier()), LEGACY_CURRENT.cloak, LEGACY_CURRENT.armorDrain, 1.0F
   );

   public record Melee(float energyCost, float damageMultiplier) {
   }

   public record Cloak(ModeDrain drain, Detection detection, CloakCost receivedHit, CloakCost silencedShot, CloakCost loudShot) {
      public CloakCost shot(boolean silenced) {
         return silenced ? silencedShot : loudShot;
      }
   }

   public record CloakCost(float energyCost, boolean breaksCloak) {
   }

   public record Detection(double quietRange, double walkingRange, double sprintingRange, double combatRange, int combatNoiseTicks) {
   }

   public record ModeDrain(float standing, float walking, float powered) {
      public float perTick(boolean powerMovement, boolean moving) {
         return powerMovement ? powered : moving ? walking : standing;
      }
   }
}
