package io.github.painworks.crysismod.nanosuit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.energy.INanosuitEnergy;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/** N1 presentation using the approved SVG layers. Caller enforces complete N1 equipment. */
public final class ClassicNanosuitHud {
   private static final ResourceLocation PANEL=asset("panel"), ENERGY=asset("energy"), CRITICAL=asset("critical"), HEALTH=asset("health"), DIGITS=asset("digits");
   private static final ResourceLocation ARMOR=asset("armor"), STRENGTH=asset("strength"), SPEED=asset("speed"), CLOAK=asset("cloak");
   private static final ResourceLocation BATTERY_OFF=asset("battery-off"), BATTERY_ON=asset("battery-on");
   private static final ResourceLocation PANEL_FIXED=asset("panel-fixed");
   private ClassicNanosuitHud() {}
   private static ResourceLocation asset(String name) {
      return ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_hud/"+name+".png");
   }
   public static void render(GuiGraphics g, Player player, INanosuitEnergy data) {
      float base=g.guiHeight()/1080f;
      float scale=base*.7f*NanosuitClientConfig.classicHudScale();
      float x=g.guiWidth()-129*base-440*scale, y=g.guiHeight()-24*base-155*scale;
      g.pose().pushPose();
      try {
         RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
         g.pose().translate(x,y,0);g.pose().scale(scale,scale,1);
         layer(g,PANEL);
         clippedFill(g,ENERGY,x,y,scale,ClassicHudValues.fraction(data.getEnergy(),100));
         if (ClassicHudValues.alert(data.getEnergy())==2) {
            // Red icon remains visible even at zero; the fill always follows actual energy.
            g.enableScissor((int)(x+195*scale),(int)y,(int)Math.ceil(x+239*scale),(int)Math.ceil(y+155*scale));
            layer(g,CRITICAL);g.disableScissor();
            clippedFill(g,CRITICAL,x,y,scale,ClassicHudValues.fraction(data.getEnergy(),100));
         }
         clippedFill(g,HEALTH,x,y,scale,ClassicHudValues.fraction(player.getHealth(),player.getMaxHealth()));
         number(g,ClassicHudValues.percent(data.getEnergy(),100),256,15);
         number(g,ClassicHudValues.percent(player.getHealth(),player.getMaxHealth()),256,44);
         ResourceLocation icon=switch(data.getMode()) {
            case ARMOR -> ARMOR; case STRENGTH -> STRENGTH; case SPEED -> SPEED; case CLOAK -> CLOAK;
            default -> null;
         };
         if(icon!=null)layer(g,icon);
      } finally {g.setColor(1,1,1,1);g.pose().popPose();RenderSystem.disableBlend();}
   }
   private static void layer(GuiGraphics g,ResourceLocation texture) {
      int rgb=NanosuitClientConfig.classicInterfaceColor();
      boolean themed=texture!=ENERGY&&texture!=CRITICAL&&rgb!=ClassicHudPalette.DEFAULT;
      if(themed)g.setColor(ClassicHudPalette.red(rgb),ClassicHudPalette.green(rgb),ClassicHudPalette.blue(rgb),1);
      else g.setColor(1,1,1,1);
      g.blit(themed?neutral(texture):texture,0,0,440,155,0,0,880,310,880,310);
      g.setColor(1,1,1,1);
      if(themed&&texture==PANEL)g.blit(PANEL_FIXED,0,0,440,155,0,0,880,310,880,310);
   }
   private static ResourceLocation neutral(ResourceLocation texture) {
      return texture.withPath(path->path.replace("/classic_hud/","/classic_hud/themed/"));
   }
   private static void clippedFill(GuiGraphics g,ResourceLocation texture,float x,float y,float s,float fraction) {
      if(fraction<=0)return;
      g.enableScissor((int)(x+24*s),(int)y,(int)Math.ceil(x+(24+165*fraction)*s),(int)Math.ceil(y+155*s));
      layer(g,texture);g.disableScissor();
   }
   private static void number(GuiGraphics g,int value,int x,int y) {
      int rgb=NanosuitClientConfig.classicInterfaceColor();
      boolean custom=rgb!=ClassicHudPalette.DEFAULT;
      if(custom)g.setColor(ClassicHudPalette.red(rgb),ClassicHudPalette.green(rgb),ClassicHudPalette.blue(rgb),1);
      String digits=Integer.toString(value);
      x+=33-digits.length()*11;
      for(int i=0;i<digits.length();i++) {
         int digit=digits.charAt(i)-'0';
         g.blit(custom?neutral(DIGITS):DIGITS,x+i*11,y,11,40,digit*32,0,32,80,320,80);
      }
      g.setColor(1,1,1,1);
   }
   public static void renderBattery(GuiGraphics g,int charge,int maximum) {
      float base=g.guiHeight()/1080f, s=base*NanosuitClientConfig.classicHudScale();
      float x=g.guiWidth()-198*base-232*s,y=77*base;
      g.pose().pushPose();
      try {
         RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
         g.pose().translate(x,y,0);g.pose().scale(s,s,1);
         g.blit(BATTERY_OFF,0,0,232,14,0,0,464,28,464,28);
         float fraction=ClassicHudValues.fraction(charge,maximum);
         if(fraction>0) {
            g.enableScissor((int)Math.ceil(x+232*(1-fraction)*s),(int)y,(int)Math.ceil(x+232*s),(int)Math.ceil(y+14*s));
            g.blit(BATTERY_ON,0,0,232,14,0,0,464,28,464,28);g.disableScissor();
         }
      } finally {g.pose().popPose();RenderSystem.disableBlend();}
   }
}
