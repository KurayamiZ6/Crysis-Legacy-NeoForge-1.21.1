package io.github.painworks.crysismod.nanosuit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Shared narrow glyph atlas from the approved editable HUD typography. */
public final class ClassicHudText {
   private static final ResourceLocation GLYPHS=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_binoculars/glyphs.png");
   private static final ResourceLocation DIGITS=ResourceLocation.fromNamespaceAndPath(
      "crysismod","textures/gui/classic_hud/digits.png");
   private ClassicHudText(){}

   static void alignWithPanel(GuiGraphics g,float artworkY) {
      g.pose().mulPose(new org.joml.Matrix4f().m01(.045f).m31(artworkY-6.75f));
   }

   /** Draw the approved digits in a right-aligned field, dimming only optional leading zeros. */
   static void digits(GuiGraphics g,int value,float right,float top,float height,float maxWidth,
                      int color,int minimumDigits) {
      String text=Integer.toString(Math.max(0,value));
      int leading=Math.max(0,minimumDigits-text.length());
      int length=leading+text.length();
      height=Math.min(height,maxWidth/(length*.49f));
      float advance=height*.49f;
      g.pose().pushPose();
      try {
         g.pose().translate(right-length*advance,top,0);
         g.pose().scale(advance/16,height/24,1);
         for(int i=0;i<length;i++){
            boolean placeholder=i<leading;
            int digit=placeholder?0:text.charAt(i-leading)-'0';
            g.setColor(ClassicHudPalette.red(color),ClassicHudPalette.green(color),
               ClassicHudPalette.blue(color),placeholder?.30f:1);
            g.blit(DIGITS,i*16,0,16,24,digit*32,0,32,48,320,48);
         }
      } finally {g.setColor(1,1,1,1);g.pose().popPose();}
   }
   public static void draw(GuiGraphics g,String text,float x,float baseline,float size,int rgb,float alpha,boolean centered){
      float w=size*.5f,h=size*1.25f;
      g.setColor((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,alpha);
      RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
      g.pose().pushPose();g.pose().translate(centered?x-text.length()*w/2:x,baseline-size,0);g.pose().scale(w/16,h/40,1);
      for(int i=0;i<text.length();i++){int c=text.charAt(i);if(c<32||c>90)c='-';g.blit(GLYPHS,i*16,0,16,40,(c-32)*32,0,32,80,1888,80);}
      g.pose().popPose();g.setColor(1,1,1,1);
   }
}
