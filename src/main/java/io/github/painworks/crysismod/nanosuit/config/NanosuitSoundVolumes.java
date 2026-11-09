package io.github.painworks.crysismod.nanosuit.config;

import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;

public final class NanosuitSoundVolumes {
   private NanosuitSoundVolumes() {
   }

   public static float ambience() { return Category.AMBIENCE.gain(); }
   public static float modes() { return Category.MODES.gain(); }
   public static float energy() { return Category.ENERGY.gain(); }
   public static float visor() { return Category.VISOR.gain(); }
   public static float movement() { return Category.MOVEMENT.gain(); }
   public static float melee() { return Category.MELEE.gain(); }

   public enum Category {
      AMBIENCE("Ambiances", NanosuitClientConfig.SOUND_AMBIENCE),
      MODES("Mode transitions", NanosuitClientConfig.SOUND_MODES),
      ENERGY("Energy alerts", NanosuitClientConfig.SOUND_ENERGY),
      VISOR("Visor and zoom", NanosuitClientConfig.SOUND_VISOR),
      MOVEMENT("Movement", NanosuitClientConfig.SOUND_MOVEMENT),
      MELEE("Power melee", NanosuitClientConfig.SOUND_MELEE);

      private final String title;
      private final DoubleValue setting;

      Category(String title, DoubleValue setting) {
         this.title = title;
         this.setting = setting;
      }

      public String title() {
         return title;
      }

      public float gain() {
         return this.setting.get().floatValue();
      }

      public void setPercent(int percent) {
         this.setting.set(percent / 100.0);
      }
   }
}
