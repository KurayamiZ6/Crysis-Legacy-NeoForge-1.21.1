package io.github.painworks.crysismod.nanosuit.rules;

public record NanosuitMobilityRules(Jump jump, int sprintCooldownTicks, double swimSpeedBonus) {
   public static final int LEGACY_JUMP_HOLD_TICKS = 6;
   public static final NanosuitMobilityRules LEGACY_CURRENT = new NanosuitMobilityRules(
      new Jump(10.0F, LEGACY_JUMP_HOLD_TICKS, 40, 8, 0.685F),
      20,
      0.40
   );
   public static final NanosuitMobilityRules CLASSIC = new NanosuitMobilityRules(
      new Jump(15.0F, 0, LEGACY_CURRENT.jump.chargeTimeoutTicks(), LEGACY_CURRENT.jump.cooldownTicks(),
         LEGACY_CURRENT.jump.verticalVelocity()),
      LEGACY_CURRENT.sprintCooldownTicks, LEGACY_CURRENT.swimSpeedBonus
   );

   public record Jump(float energyCost, int holdTicks, int chargeTimeoutTicks, int cooldownTicks, float verticalVelocity) {
   }
}
