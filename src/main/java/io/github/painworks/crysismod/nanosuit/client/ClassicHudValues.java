package io.github.painworks.crysismod.nanosuit.client;

/** Presentation math only; never writes health, energy or battery state. */
public final class ClassicHudValues {
   private ClassicHudValues() {}
   public static float fraction(float value, float maximum) {
      if (!Float.isFinite(value) || !Float.isFinite(maximum) || maximum <= 0) return 0;
      return Math.max(0, Math.min(1, value / maximum));
   }
   public static int percent(float value, float maximum) {
      return Math.round(100 * fraction(value, maximum));
   }
   public static int alert(float energy) {
      return energy <= 15 ? 2 : energy <= 25 ? 1 : 0;
   }
}
