package local.crysisqa;
import io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette;
public final class ClassicPaletteQa {
 public static void main(String[] args){var random=new java.util.Random(1201);int count=8192;
  for(int i=0;i<count;i++){int color=random.nextInt(),gray=((color>>16&255)*54+(color>>8&255)*183+(color&255)*19)/256;
   int old=(color&0xff000000)|((int)(gray*.88)<<16)|((int)(gray*.96)<<8)|(int)(gray*.83);
   if(ClassicHudPalette.terrainColor(color,ClassicHudPalette.DEFAULT)!=old)throw new AssertionError("Default palette changed");
   for(int theme:new int[]{0,0x3388cc,0xc59a64,0xffffff})if((ClassicHudPalette.terrainColor(color,theme)&0xff000000)!=(color&0xff000000))throw new AssertionError("Alpha changed");
  }System.out.println("CLASSIC_PALETTE_PASS samples="+count);
 }
}
