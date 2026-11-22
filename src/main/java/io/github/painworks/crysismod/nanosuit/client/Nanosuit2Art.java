package io.github.painworks.crysismod.nanosuit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;
import org.joml.Matrix4f;

/** Sprite coordinates come from the approved 1920×1080 HTML layout. */
public final class Nanosuit2Art {
   private static final Map<String,ResourceLocation> SPRITES=new HashMap<>();
   public static final int PANEL_W=437,PANEL_H=193;
   public static final int RADAR_W=462,RADAR_H=251;
   private Nanosuit2Art() {}

   public static float scale(GuiGraphics g) {return g.guiWidth()/1920f;}

   public static void shear(GuiGraphics g,float pivotX,float pivotY,float slope,float scaleX) {
      g.pose().translate(pivotX,pivotY,0);
      g.pose().mulPose(new Matrix4f().m01(slope*scaleX));
      g.pose().translate(-pivotX,-pivotY,0);
   }

   public static void sprite(GuiGraphics g,String name,int x,int y,int width,int height,int color) {
      region(g,name,x,y,width,height,0,0,width,height,width,height,color);
   }

   public static void region(GuiGraphics g,String name,int x,int y,int width,int height,
                             int u,int v,int sourceWidth,int sourceHeight,int texWidth,int texHeight,int color) {
      if(width<=0||height<=0) return;
      ResourceLocation id=SPRITES.computeIfAbsent(name,key->ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/nanosuit2/"+key+".png"));
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      guiColor(g,color);
      try {
         g.blit(id,x,y,width,height,u,v,sourceWidth,sourceHeight,texWidth,texHeight);
      }
      finally {g.setColor(1,1,1,1);}
   }

   public static void guiColor(GuiGraphics g,int color) {
      g.setColor((color>>>16&255)/255f,(color>>>8&255)/255f,(color&255)/255f,(color>>>24)/255f);
   }

   static void counter(GuiGraphics g,int amount,int right,int y,int glyphWidth,int glyphHeight,int color,int bloom) {
      String value=Integer.toString(Math.max(0,amount));
      int count=Math.max(3,value.length());
      int pitch=glyphWidth+2;
      int x=right-count*pitch+2;
      int placeholders=count-value.length();
      int paddingX=Math.round(glyphWidth*24/64f);
      int paddingY=Math.round(glyphHeight*24/96f);
      for(int i=0;i<value.length();i++) {
         int digit=value.charAt(i)-'0';
         int left=x+(placeholders+i)*pitch;
         region(g,"digits-bloom",left-paddingX,y-paddingY,glyphWidth+2*paddingX,glyphHeight+2*paddingY,
            digit*112,0,112,144,1120,144,bloom);
      }
      for(int i=0;i<count;i++) {
         int digit=i<placeholders?0:value.charAt(i-placeholders)-'0';
         region(g,"digits",x+i*pitch,y,glyphWidth,glyphHeight,digit*64,0,64,96,640,96,i<placeholders?0x38a2c0b5:color);
      }
   }
}
