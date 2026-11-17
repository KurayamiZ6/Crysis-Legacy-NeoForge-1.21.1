package io.github.painworks.crysismod.nanovision;

public final class ClassicNightVisionBattery {
   public static final int FULL_CHARGE = 60 * 20;
   private static final int CHARGE_PER_TICK = FULL_CHARGE / (15 * 20);

   private int charge = FULL_CHARGE;
   private boolean active;

   public boolean active() {
      return active;
   }

   public int charge() {
      return charge;
   }

   public void toggle() {
      active = !active && charge > 0;
   }

   public void deactivate() {
      active = false;
   }

   public void tick(boolean eligible) {
      if (active && eligible) {
         active = --charge > 0;
      } else {
         deactivate();
         charge = Math.min(FULL_CHARGE, charge + CHARGE_PER_TICK);
      }
   }
}
