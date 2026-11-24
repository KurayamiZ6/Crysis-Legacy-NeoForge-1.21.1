package io.github.painworks.crysismod.nanovisor.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import java.util.Locale;

@EventBusSubscriber(modid="crysismod",value=Dist.CLIENT)
public final class ClassicBinoculars {
   private static final ClassicBinocularAnimation ANIMATION=new ClassicBinocularAnimation();
   private static final ResourceLocation FRAME=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_binoculars/frame.png");
   private static final ResourceLocation LABEL_BACKGROUND=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_binoculars/label-background.png");
   private static final ResourceLocation GLYPHS=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_binoculars/glyphs.png");
   private static final ResourceLocation BRACKETS=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_binoculars/brackets.png");
   private static final ResourceLocation VIGNETTE=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_binoculars/vignette.png");
   private static int zoom=2;
   private static boolean hadContext;
   private static java.lang.reflect.Method clientOperator,isAim,aimProgress;
   private static boolean taczChecked;
   private static double lastBaseFov,lastWorldFov;
   private ClassicBinoculars(){}
   public static boolean isN1Context(){var player=Minecraft.getInstance().player;return player!=null&&NanosuitArmorUtil.getEquippedProfile(player)==NanosuitProfile.NANOSUIT_1;}
   public static boolean running(){return hadContext&&ANIMATION.state()!=ClassicBinocularAnimation.State.CLOSED;}
   public static boolean eligible(){
      var mc=Minecraft.getInstance();
      return isN1Context()&&mc.player!=null&&mc.level!=null&&mc.player.isAlive()&&!mc.player.isSpectator()
         &&mc.screen==null&&NanoVisorSettings.General.visorEnabled()&&!mc.options.keyAttack.isDown()&&!mc.options.keyUse.isDown()&&!taczAiming();
   }
   private static boolean taczAiming(){
      if(!taczChecked){taczChecked=true;try{
         var type=Class.forName("com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator");
         clientOperator=type.getMethod("fromLocalPlayer",net.minecraft.client.player.LocalPlayer.class);
         isAim=type.getMethod("isAim");aimProgress=type.getMethod("getClientAimingProgress",float.class);
      }catch(ReflectiveOperationException|LinkageError unavailable){clientOperator=null;}}
      if(clientOperator==null)return false;
      try {var operator=clientOperator.invoke(null,Minecraft.getInstance().player);
         return operator!=null&&((boolean)isAim.invoke(operator)||((Number)aimProgress.invoke(operator,1f)).floatValue()>.001f);
      }catch(ReflectiveOperationException|LinkageError unavailable){return false;}
   }
   public static boolean updateFromInput(KeyMapping key){
      boolean previous=NanoVisorState.isVisorActive();
      if(!eligible()){reset();while(key.consumeClick()){};return previous;}
      hadContext=true;
      while(key.consumeClick())ANIMATION.toggle(System.nanoTime());
      ANIMATION.sample(System.nanoTime());
      NanoVisorState.setVisorActive(ANIMATION.requestedOpen());
      return previous!=NanoVisorState.isVisorActive();
   }
   public static void reset(){ANIMATION.reset();zoom=2;hadContext=false;NanoVisorState.setVisorActive(false);}
   public static void updateZoomInput(KeyMapping key){while(key.consumeClick())if(ANIMATION.requestedOpen())zoom=zoom==2?4:2;}
   public static int zoom(){return zoom;}
   @SubscribeEvent(priority=EventPriority.HIGHEST)
   public static void scroll(net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent event){
      if(eligible()&&ANIMATION.requestedOpen()&&event.getScrollDeltaY()!=0){
         zoom=event.getScrollDeltaY()>0?4:2;event.setCanceled(true);
      }
   }
   public static float progress(){return eligible()?ANIMATION.sample(System.nanoTime()):0;}
   public static String animationState(){return ANIMATION.state().name();}
   public static double lastWorldFov(){return lastWorldFov;}
   public static double lastBaseFov(){return lastBaseFov;}
   public static double targetDistance(float partial){var player=Minecraft.getInstance().player;return player==null?-1:NanoVisorHudOverlay.classicDistance(player,partial);}
   @SubscribeEvent(priority=EventPriority.LOWEST)
   public static void fov(ViewportEvent.ComputeFov event){
      if(isN1Context()&&eligible()&&event.usedConfiguredFov()){
         float p=progress();lastBaseFov=event.getFOV();
         lastWorldFov=ClassicBinocularAnimation.zoomedFov(lastBaseFov,1+(zoom-1)*p);
         event.setFOV(lastWorldFov);
      }
   }
   @SubscribeEvent(priority=EventPriority.HIGHEST)
   public static void hands(RenderHandEvent event){if(isN1Context()&&running()&&eligible())event.setCanceled(true);}
   public static void render(GuiGraphics g,float partial){
      float p=progress();if(p<=0||!NanoVisorSettings.General.visorOverlayEnabled())return;
      var mc=Minecraft.getInstance();float sx=g.guiWidth()/1920f,sy=g.guiHeight()/1080f;
      int rgb=io.github.painworks.crysismod.nanosuit.client.ClassicMalfunction.hudColor();
      int textRgb=rgb==io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette.DEFAULT?0xc0efa6:rgb;
      boolean depth=org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
      g.pose().pushPose();
      try{
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();
         g.pose().scale(sx,sy,1);
         // Night vision supplies its own scene vignette: blend ownership instead of doubling it.
         float vignetteAlpha=p*(1-io.github.painworks.crysismod.client.nanovision.ClassicNightVisionClient.visualIntensity());
         g.setColor(1,1,1,vignetteAlpha);
         g.blit(VIGNETTE,0,0,1920,1080,0,0,1920,1080,1920,1080);g.setColor(1,1,1,1);
         g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
         g.setColor(1,1,1,p);
         g.blit(LABEL_BACKGROUND,0,0,1920,1080,0,0,1920,1080,1920,1080);
         g.setColor((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,p);
         // Vanilla positions its 15-pixel crosshair on integer GUI coordinates.
         boolean firstPerson=mc.options.getCameraType().isFirstPerson();
         float centerOffsetX=firstPerson && g.guiWidth()%2==0 ? -.5f/sx : 0;
         float centerOffsetY=firstPerson && g.guiHeight()%2==0 ? -.5f/sy : 0;
         g.pose().pushPose();
         g.pose().translate(centerOffsetX,centerOffsetY,0);
         g.blit(FRAME,0,0,1920,1080,0,0,1920,1080,1920,1080);
         g.pose().popPose();
         g.setColor(1,1,1,1);
         text(g,String.format(Locale.ROOT,"X %.1f   Z %.1f",mc.player.getX(),mc.player.getZ()),885,280,21,textRgb,p,true);
         text(g,String.format(Locale.ROOT,"Y %.1f",mc.player.getY()),1040,280,21,textRgb,p,true);
         text(g,"ZOOM",463,542,19,textRgb,p,true);text(g,"X0"+zoom,463,569,20,textRgb,p,true);
         text(g,"MODE",1458,542,19,textRgb,p,true);text(g,"VIS",1458,569,20,textRgb,p,true);
         double distance=targetDistance(partial);
         text(g,distance<0?"— M":String.format(Locale.ROOT,"%.2f M",distance),960,859,29,textRgb,p,true);
         int top=(int)(550-280*p),bottom=(int)(550+280*p);
         g.enableScissor((int)(510*sx),(int)(top*sy),(int)Math.ceil(1410*sx),(int)Math.ceil(bottom*sy));
         float angle=-mc.player.getViewXRot(partial);
         for(int a=-180;a<=180;a+=2){float y=550+(angle-a)*6;if(y<290||y>837)continue;
            int color=((int)(p*200)<<24)|rgb;
            g.fill(558,(int)y,558+(a%10==0?16:9),(int)y+2,color);
            g.fill(1363,(int)y,1363+(a%10==0?16:9),(int)y+2,color);
            if(a%10==0){int folded=(int)Math.round(Math.toDegrees(Math.asin(Math.sin(Math.toRadians(a)))));
               String label=folded==0?"00":Integer.toString(folded);
               text(g,label,578,y+6,18,textRgb,p,false);text(g,label,1351,y+6,18,textRgb,p,true);}
         }
         g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
         g.setColor((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,p);
         g.blit(BRACKETS,0,0,1920,1080,0,0,1920,1080,1920,1080);
         g.disableScissor();
      }finally{g.setColor(1,1,1,1);g.pose().popPose();RenderSystem.disableBlend();if(depth)RenderSystem.enableDepthTest();else RenderSystem.disableDepthTest();}
   }
   private static void text(GuiGraphics g,String value,float x,float y,float height,int rgb,float alpha,boolean center){
      float w=height*.5f,h=height*1.25f;
      g.setColor((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,alpha);
      RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
      g.pose().pushPose();g.pose().translate(center?x-value.length()*w/2:x,y-height,0);g.pose().scale(w/16,h/40,1);
      for(int i=0;i<value.length();i++){
         int c=value.charAt(i);if(c<32||c>90)c='-';
         g.blit(GLYPHS,i*16,0,16,40,(c-32)*32,0,32,80,1888,80);
      }
      g.pose().popPose();g.setColor(1,1,1,1);
   }
}
