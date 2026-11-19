package io.github.painworks.crysismod.nanosuit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.energy.INanosuitEnergy;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/** N1 presentation using the approved SVG layers. Caller enforces complete N1 equipment. */
@net.neoforged.fml.common.EventBusSubscriber(modid="crysismod", value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class ClassicNanosuitHud {
   private static final ResourceLocation PANEL=asset("panel"), ENERGY=asset("energy"), CRITICAL=asset("critical"), HEALTH=asset("health"), DIGITS=asset("digits");
   private static final ResourceLocation ARMOR=asset("armor"), STRENGTH=asset("strength"), SPEED=asset("speed"), CLOAK=asset("cloak");
   private static final ResourceLocation BATTERY_OFF=asset("battery-off"), BATTERY_ON=asset("battery-on");
   private static final ResourceLocation PANEL_FIXED=asset("panel-fixed");
   private static final ResourceLocation FRAME_LEFT=asset("frame-left"), FRAME_RIGHT=asset("frame-right");
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
   /** One permanent GUI pass, after conditional overlays so visor vignette cannot dim the chrome. */
   @net.neoforged.bus.api.SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOW)
   public static void onPermanentFrame(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event) {
      var minecraft=net.minecraft.client.Minecraft.getInstance();
      if(!minecraft.options.hideGui && NanosuitHudOverlay.isHudEnabled()
         && minecraft.player!=null
         && io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil.getEquippedProfile(minecraft.player)
            ==io.github.painworks.crysismod.nanosuit.NanosuitProfile.NANOSUIT_1) {
         renderPermanentFrame(event.getGuiGraphics());
      }
   }
   private static void renderPermanentFrame(GuiGraphics g) {
      float base=g.guiHeight()/1080f, s=base*NanosuitClientConfig.classicHudScale();
      int rgb=NanosuitClientConfig.classicInterfaceColor();
      boolean custom=rgb!=ClassicHudPalette.DEFAULT;
      RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
      if(custom)g.setColor(ClassicHudPalette.red(rgb),ClassicHudPalette.green(rgb),ClassicHudPalette.blue(rgb),1);
      else g.setColor(1,1,1,1);
      for(int side=0;side<2;side++) {
         ResourceLocation texture=side==0?FRAME_LEFT:FRAME_RIGHT;
         float x=side==0?125*base:g.guiWidth()-128*base-57*s;
         g.pose().pushPose();
         try {
            g.pose().translate(x-2*s,73*base-2*s,0);g.pose().scale(s,s,1);
            g.blit(custom?neutral(texture):texture,0,0,62,18,0,0,124,36,124,36);
         } finally {g.pose().popPose();}
      }
      g.setColor(1,1,1,1);RenderSystem.disableBlend();
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
