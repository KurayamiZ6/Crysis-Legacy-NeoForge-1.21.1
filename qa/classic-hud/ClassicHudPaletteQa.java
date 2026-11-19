import io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette;
public final class ClassicHudPaletteQa {
   public static void main(String[] args){
      check(ClassicHudPalette.parse("#719671")==ClassicHudPalette.DEFAULT);
      check(ClassicHudPalette.parse("#F030B0")==0xf030b0);
      check(ClassicHudPalette.parse("#00bfff")==0x00bfff);
      check(ClassicHudPalette.parse("#000000")==0);
      check(ClassicHudPalette.parse("oops")==ClassicHudPalette.DEFAULT);
      check(ClassicHudPalette.parse(null)==ClassicHudPalette.DEFAULT);
      check(ClassicHudPalette.red(0xff0000)==1);
      check(ClassicHudPalette.green(0xff0000)==0);
      check(ClassicHudPalette.blue(0x0000ff)==1);
      System.out.println("PASS: 9 theme parsing/channel checks");
   }
   static void check(boolean ok){if(!ok)throw new AssertionError();}
}
