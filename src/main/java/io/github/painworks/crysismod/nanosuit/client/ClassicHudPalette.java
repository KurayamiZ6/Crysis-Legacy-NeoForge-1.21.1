package io.github.painworks.crysismod.nanosuit.client;

/** Pure RGB helpers: usable without loading any renderer or client singleton. */
public final class ClassicHudPalette {
   public static final int DEFAULT=0x719671;
   private ClassicHudPalette() {}
   public static int parse(String text) {
      if(text==null||!text.matches("#[0-9a-fA-F]{6}"))return DEFAULT;
      return Integer.parseInt(text.substring(1),16);
   }
   public static float red(int rgb){return (rgb>>16&255)/255f;}
   public static float green(int rgb){return (rgb>>8&255)/255f;}
   public static float blue(int rgb){return (rgb&255)/255f;}
}
