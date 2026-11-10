package io.github.painworks.crysismod.nanosuit.rules;

public record NanosuitEnergyRules(Recovery recovery, Healing healing, CriticalWarning criticalWarning) {
   // Crysis 3: shutdown 1.556 s, waiting pulse 1.007 s, charge 1.521 s.
   public static final NanosuitEnergyRules LEGACY_CURRENT = new NanosuitEnergyRules(
      new Recovery(21, 10, 5, 20, 41),
      new Healing(80, 60, 1.0F),
      new CriticalWarning(10.0F, 20.0F)
   );

   public record Recovery(int firstWaitTick, int waitIntervalTicks, int waitPulseCount, int chargeDelayAfterLastPulse, int chargeTicks) {
      public int chargeStartTick() {
         return firstWaitTick + (waitPulseCount - 1) * waitIntervalTicks + chargeDelayAfterLastPulse;
      }
   }

   public record Healing(int delayTicks, int intervalTicks, float amountHp) {
   }

   public record CriticalWarning(float threshold, float rearmThreshold) {
   }
}
