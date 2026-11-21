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
   static int textColor(int theme){return theme==DEFAULT?0xc0efa6:theme;}
   public static int loadedAmmoColor(int theme){
      int text=textColor(theme);
      return (lift(text>>16&255)<<16)|(lift(text>>8&255)<<8)|lift(text&255);
   }
   private static int lift(int channel){return channel+Math.round((255-channel)*.23f);}
   /** Tint real terrain luminance; the default branch preserves the approved original palette exactly. */
   public static int terrainColor(int color,int theme){
      int gray=((color>>16&255)*54+(color>>8&255)*183+(color&255)*19)/256;
      int alpha=color&0xff000000;
      if(theme==DEFAULT)return alpha|((int)(gray*.88)<<16)|((int)(gray*.96)<<8)|(int)(gray*.83);
      int r=theme>>16&255,g=theme>>8&255,b=theme&255,max=Math.max(r,Math.max(g,b));
      if(max==0)return alpha;
      double shade=gray*.96/max;
      return alpha|((int)(r*shade)<<16)|((int)(g*shade)<<8)|(int)(b*shade);
   }
}
