package io.github.painworks.crysismod.nanosuit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Shared narrow glyph atlas from the approved editable HUD typography. */
public final class ClassicHudText {
   private static final ResourceLocation GLYPHS=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_binoculars/glyphs.png");
   private ClassicHudText(){}
   public static void draw(GuiGraphics g,String text,float x,float baseline,float size,int rgb,float alpha,boolean centered){
      float w=size*.5f,h=size*1.25f;
      g.setColor((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,alpha);
      RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
      g.pose().pushPose();g.pose().translate(centered?x-text.length()*w/2:x,baseline-size,0);g.pose().scale(w/16,h/40,1);
      for(int i=0;i<text.length();i++){int c=text.charAt(i);if(c<32||c>90)c='-';g.blit(GLYPHS,i*16,0,16,40,(c-32)*32,0,32,80,1888,80);}
      g.pose().popPose();g.setColor(1,1,1,1);
   }
}
