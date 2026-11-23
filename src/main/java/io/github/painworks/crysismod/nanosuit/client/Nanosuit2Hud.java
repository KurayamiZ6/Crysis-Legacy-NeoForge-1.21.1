package io.github.painworks.crysismod.nanosuit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanovisor.client.NanoVisorState;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;

/** Presentation for the N2 profile. Energy and mode decisions stay with the suit capability. */
@EventBusSubscriber(modid="crysismod",value=Dist.CLIENT)
public final class Nanosuit2Hud {
   public static final int INK=0xffd2ffea;
   public static final int PANEL=0xa5273f39;
   private static final int RED=0xffff3932;
   private static final ResourceLocation PREDATOR=ResourceLocation.parse("sfms:tb23");
   private static LocalPlayer owner;
   private static boolean belowThreshold;
   private static long lowSince=Long.MIN_VALUE;
   private static long deathSince=-1;
   private static long lastEquippedRender;
   private static ClassicTaczData.State currentGun,previousGun;
   private static int radarX,radarY,radarWidth,radarHeight;
   private static float panelCenterX,panelCenterY,panelScale;

   private Nanosuit2Hud() {}

   public static boolean equipped() {
      var player=Minecraft.getInstance().player;
      return player!=null && NanosuitArmorUtil.getEquippedProfile(player)==NanosuitProfile.NANOSUIT_2;
   }

   public static int threatColor(int level) {
      return switch(level) { case 2 -> 0xffffdf32; case 3 -> RED; default -> INK; };
   }

   static void render(GuiGraphics g,LocalPlayer player,float energy,int filled,boolean armor,boolean cloak,
                      float centerX,float centerY,float scale,float inactiveAlpha) {
      long now=Util.getMillis();
      if(owner!=player) {
         owner=player;
         belowThreshold=false;
         lowSince=Long.MIN_VALUE;
         deathSince=-1;
         currentGun=previousGun=null;
         radarWidth=0;
      }
      if(player.isAlive()) lastEquippedRender=now;
      float reference=Nanosuit2Art.scale(g);
      panelScale=reference*scale/.2f;
      panelCenterX=g.guiWidth()-325*reference+(NanosuitHudOverlay.getHudOffsetX()+28);
      panelCenterY=g.guiHeight()-189*reference+(NanosuitHudOverlay.getHudOffsetY()+8);
      boolean low=energy<20;
      if(low&&!belowThreshold) lowSince=now;
      belowThreshold=low;
      if(!player.isAlive()&&deathSince<0) deathSince=now;
      if(player.isAlive()) deathSince=-1;
      if(panelDropped(false)) return;
      boolean redPulse=now%333<166;
      var ammo=NanosuitClientConfig.taczHudOverrideEnabled()&&NanosuitClientConfig.taczHideOriginalHud()?ClassicTaczData.readNanosuit2(player):null;
      if(ammo!=null) {
         if(currentGun!=null&&!currentGun.gunId().equals(ammo.gunId())) previousGun=currentGun;
         currentGun=ammo;
      }
      g.pose().pushPose();
      try (var crt=Nanosuit2Crt.capture(g,panelCenterY-96.5F*panelScale,193F*panelScale)) {
         NanosuitHudOverlay.applySharedRightHudInertiaTransform(g,g.guiWidth()/2,g.guiHeight()/2);
         g.pose().translate(panelCenterX+crtShift(),panelCenterY,0);
         g.pose().scale(panelScale,panelScale,1);
         g.pose().mulPose(new Matrix4f().m01(.122785f));
         g.pose().translate(-218.5f,-96.5f,0);
         Nanosuit2Art.sprite(g,"panel",0,0,437,193,-1);
         String empty=low?"energy-low-off":"energy-off";
         String full=low?(redPulse?"energy-low-red":"energy-low-on"):"energy-on";
         // Preserve the capability's 20 quantized steps and lay them out across the 30 authored cells.
         int lit=Math.round(filled*30/20f);
         int cut=Math.round(lit*405/30f);
         int offTint=Math.round(255*inactiveAlpha)<<24|0xffffff;
         Nanosuit2Art.region(g,full,21,115,cut,41,0,0,cut,41,405,41,-1);
         Nanosuit2Art.region(g,empty,21+cut,115,405-cut,41,cut,0,405-cut,41,405,41,offTint);
         Nanosuit2Art.sprite(g,armor?"mode-armor":cloak?"mode-stealth":"mode-neutral",0,0,437,193,-1);
         if(ammo!=null) {
            if(!NanoVisorState.isVisorActive()) {
               boolean lowAmmo=ammo.inventoryFed()?ammo.loaded()==0:ammo.capacity()>0&&ammo.loaded()*5<=ammo.capacity();
               if(ammo.inventoryFed()&&ammo.unlimited()) glowingLabel(g,"∞",290,12,4.3f,0xfff3fffb,0xffcdffea);
               else Nanosuit2Art.counter(g,ammo.loaded(),320,8,31,50,lowAmmo?0xffff493c:0xfff3fffb,
                  lowAmmo?0xa0ff463a:0x75cdffea);
               if(!ammo.inventoryFed()) {
                  Nanosuit2Art.sprite(g,"slash",321,1,32,64,-1);
                  if(ammo.unlimited()) glowingLabel(g,"∞",395,23,3.2f,INK,0xff93e7bb);
                  else Nanosuit2Art.counter(g,ammo.reserve(),422,16,21,34,INK,0x6493e7bb);
               }
            }
            int count=switch(ammo.fireMode()) {case "AUTO" -> 4; case "BURST" -> 3; case "SEMI" -> 1; default -> 0;};
            if(count>0) Nanosuit2Art.sprite(g,"shots-"+count,0,0,437,193,-1);
         }
         if(previousGun!=null&&previousGun.icon()!=null) {
            Nanosuit2Art.guiColor(g,INK);
            try {g.blit(previousGun.icon(),54,79,0,0,96,27,96,27);}
            finally {g.setColor(1,1,1,1);}
         }
         if(hasPredator(player)) Nanosuit2Art.sprite(g,"bow",0,0,437,193,-1);
      } finally {g.pose().popPose();}
   }

   private static boolean hasPredator(LocalPlayer player) {
      for(ItemStack stack:player.getInventory().items) {
         if(PREDATOR.equals(ClassicTaczData.gunId(stack))) return true;
      }
      return PREDATOR.equals(ClassicTaczData.gunId(player.getOffhandItem()));
   }

   public static float crtShift() {
      long t=Util.getMillis();
      long cycle=t/1100;
      long phase=t%1100;
      long start=cycle*137%440;
      return phase<start||phase>=start+90?0:phase<start+45?.25f:-.25f;
   }

   public static int crt(int color,float row) {
      float phase=(Util.getMillis()%1460)/1460f;
      float rollback=Math.max(0,1-Math.abs(phase-.72f)/.048f);
      float main=7*phase-.53f*rollback;
      float echo=.62f+6*phase;
      float y=row/77;
      int alpha=color>>>24;
      if(circularDistance(y,main)<.135f) alpha=alpha*91/100;
      if(circularDistance(y,echo)<.09f) alpha=alpha*96/100;
      return color&0xffffff|alpha<<24;
   }

   /** Low-opacity scanlines during play, horizontal cyan dropouts during shutdown. */
   public static void crtLines(GuiGraphics g,int x,int y,int w,int h,boolean polygonalMap) {
      long now=Util.getMillis(),age=deathSince<0?-1:now-deathSince;
      for(int row=1;row<h;row+=2) {
         int left=x,right=x+w;
         if(polygonalMap) {
            if(row<h*.083f) left+=Math.round((.083f-row/(float)h)*w*.54f);
            if(row>h*.741f) right-=Math.round((row/(float)h-.741f)*w*.61f);
            if(row>h*.925f) break;
         } else if(row<5) {left+=6-row;right-=6-row;}
         int rgb;
         if(age>=0) {
            int band=Math.floorMod((int)(row*5-age/7),53);
            int grain=Math.floorMod(row*97^(int)(age/26)*761,139);
            if(band>=26&&grain<90) continue;
            rgb=((band<26?grain+34:27)<<24)|0x33d9ec;
            int noiseShift=(grain-70)*w/1000;
            left+=Math.max(0,noiseShift);
            right+=Math.min(0,noiseShift);
         } else {
            int shade=crt(0xff000000,row*77f/h)>>>24;
            rgb=(10+(255-shade)*2)<<24;
         }
         if(right>left)g.fill(left,y+row,right,y+row+1,rgb);
      }
   }

   private static float circularDistance(float a,float b) {
      float offset=Math.abs(a-b)%1;
      return Math.min(offset,1-offset);
   }

   public static boolean panelDropped(boolean radar) {
      if(deathSince<0) return false;
      long age=Util.getMillis()-deathSince;
      return age>=2000 || age>1000 && (age+(radar?137:0))%251<(age-1000)/9;
   }

   public static void rememberRadar(int x,int y,int width,int height) {
      radarX=x;radarY=y;radarWidth=width;radarHeight=height;
   }

   private static void shutdownPanels(GuiGraphics g) {
      if(radarWidth>0&&!panelDropped(true)) {
         g.pose().pushPose();
         try {
            g.pose().translate(radarX,radarY,0);
            Nanosuit2Art.shear(g,radarWidth*231f/340,radarHeight*67f/184,-.122785f,1);
            g.pose().pushPose();
            g.pose().scale(radarWidth/340f,radarHeight/184f,1);
            Nanosuit2Art.sprite(g,"radar-0",0,-59,462,251,0xb95ce1f6);
            Nanosuit2Art.sprite(g,"radar-threat-base",0,-59,462,251,0xb95ce1f6);
            g.pose().popPose();
            shape(g,0x66225158,0,6,8,0,radarWidth,0,radarWidth,radarHeight*.72f,radarWidth*.84f,radarHeight,0,radarHeight);
            crtLines(g,0,0,radarWidth,radarHeight,true);
         } finally {g.pose().popPose();}
      }
      if(!panelDropped(false)) {
         g.pose().pushPose();
         try {
            g.pose().translate(panelCenterX,panelCenterY,0);
            g.pose().scale(panelScale,panelScale,1);
            g.pose().mulPose(new Matrix4f().m01(.122785f));
            g.pose().translate(-218.5f,-96.5f,0);
            Nanosuit2Art.sprite(g,"panel",0,0,437,193,0xa75cdef1);
            Nanosuit2Art.sprite(g,"mode-neutral",0,0,437,193,0xad5cf4ff);
            crtLines(g,21,5,404,181,false);
         } finally {g.pose().popPose();}
      }
   }

   @SubscribeEvent
   public static void alert(RenderGuiEvent.Post event) {
      Minecraft mc=Minecraft.getInstance();
      if(owner==null||owner!=mc.player) {
         owner=null;
         deathSince=-1;
         return;
      }
      if(!NanosuitHudOverlay.isHudEnabled()||mc.options.hideGui) return;
      long now=Util.getMillis();
      if(mc.player.isAlive()) {
         deathSince=-1;
         if(!equipped()) return;
      } else if(deathSince<0 && now-lastEquippedRender<=1500) deathSince=now;
      boolean critical=deathSince>=0;
      if(!critical&&(!belowThreshold||lowSince==Long.MIN_VALUE||now-lowSince>=2000)) return;
      GuiGraphics g=event.getGuiGraphics();
      if(critical&&!equipped()) shutdownPanels(g);
      g.pose().pushPose();
      try {
         g.pose().translate(g.guiWidth()*.5f,g.guiHeight()*.60f,0);
         float size=Nanosuit2Art.scale(g);
         g.pose().scale(size,size,1);
         Nanosuit2Art.sprite(g,"alert-brackets",-317,-24,634,48,-1);
         if((now-(critical?deathSince:lowSince))%333<166) {
            String language=mc.getLanguageManager().getSelected().equals("fr_fr")?"fr":"en";
            Nanosuit2Art.sprite(g,"alert-"+(critical?"critical-":"low-")+language,-317,-24,634,48,-1);
         }
      } finally {g.pose().popPose();}
   }

   public static void glowingLabel(GuiGraphics g,String value,float x,float y,float scale,int color,int halo) {
      int haloAlpha=(color>>>24)*28/255;
      int glow=halo&0xffffff|haloAlpha<<24;
      for(int i=0;i<8;i++) {
         double angle=i*Math.PI/4;
         label(g,value,x+2.4f*(float)Math.cos(angle),y+2.4f*(float)Math.sin(angle),scale,glow);
      }
      label(g,value,x,y,scale,color);
   }

   public static void label(GuiGraphics g,String value,float x,float y,float scale,int color) {
      g.pose().pushPose();
      g.pose().translate(x,y,0);
      g.pose().scale(scale,scale,1);
      g.drawString(Minecraft.getInstance().font,value,0,0,color,false);
      g.pose().popPose();
   }

   public static void shape(GuiGraphics g,int color,float... xy) {
      g.flush();
      Nanosuit2Crt.blendLayer();
      RenderSystem.disableCull();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      var buffer=Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN,DefaultVertexFormat.POSITION_COLOR);
      var matrix=g.pose().last().pose();
      for(int i=0;i<xy.length;i+=2) buffer.addVertex(matrix,xy[i],xy[i+1],0).setColor(color);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }
}
