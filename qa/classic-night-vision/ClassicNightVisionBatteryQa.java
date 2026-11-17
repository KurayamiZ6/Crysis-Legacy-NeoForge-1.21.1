import io.github.painworks.crysismod.nanovision.ClassicNightVisionBattery;

public final class ClassicNightVisionBatteryQa {
   public static void main(String[] args) {
      var battery = new ClassicNightVisionBattery();
      check(!battery.active() && battery.charge() == 1200, "initial charge");
      battery.toggle();
      for (int tick = 1; tick <= 1200; tick++) {
         battery.tick(true);
         check(battery.charge() == 1200 - tick, "depletion tick " + tick);
         check(battery.active() == (tick < 1200), "automatic stop tick " + tick);
      }
      battery.toggle();
      check(!battery.active(), "refuses activation at zero");
      for (int tick = 1; tick <= 300; tick++) {
         battery.tick(true);
         check(battery.charge() == tick * 4 && !battery.active(), "idle recharge tick " + tick);
      }
      battery.tick(true);
      check(battery.charge() == 1200, "capacity cap");

      battery.toggle();
      for (int tick = 1; tick <= 30; tick++) {
         battery.tick(true);
      }
      for (int i = 0; i < 20; i++) {
         battery.toggle();
      }
      check(battery.active() && battery.charge() == 1170, "rapid toggles do not create charge");
      battery.tick(false);
      check(!battery.active() && battery.charge() == 1174, "unequip shuts off and recharges normally");
      System.out.println("CLASSIC_NIGHT_VISION_BATTERY_QA_PASS");
   }

   private static void check(boolean condition, String test) {
      if (!condition) {
         throw new AssertionError(test);
      }
   }
}
