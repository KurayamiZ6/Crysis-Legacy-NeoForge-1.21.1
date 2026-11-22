package io.github.painworks.crysismod.nanovisor.client;

import io.github.painworks.crysismod.nanosuit.client.ClassicEnemyContacts;
import io.github.painworks.crysismod.nanosuit.client.Nanosuit2Hud;
import io.github.painworks.crysismod.nanosuit.client.Nanosuit2Art;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorSettings;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

final class Nanosuit2Optic {
   private static final double RANGE=100;
   private static LocalPlayer owner;
   private static List<LivingEntity> candidates=List.of();
   private static int sampledTick=Integer.MIN_VALUE;
   private static UUID focus;
   private static long focusedAt;
   private static boolean markedAtEntry,requested,confirmedWhileFocused;
   private static float spread;
   private static long lastFrame;

   private Nanosuit2Optic() {}

   static void resetFocus() {
      focus=null;
      spread=0;
      lastFrame=0;
   }

   static void render(GuiGraphics g,LocalPlayer player,float partial,float alpha) {
      var camera=Minecraft.getInstance().gameRenderer.getMainCamera();
      long now=Util.getMillis();
      if(owner!=player) {
         owner=player;
         sampledTick=Integer.MIN_VALUE;
         resetFocus();
      }
      if(sampledTick==Integer.MIN_VALUE||player.tickCount-sampledTick>=5) {
         sampledTick=player.tickCount;
         candidates=player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(RANGE),
            e->e.isAlive() && (e instanceof Enemy || NanoVisorClientMarkedTargets.isMarkedActive(e)));
      }
      float scale=g.guiHeight()/540f;
      float halfWidth=g.guiWidth()*.5f/scale;
      float focal=270f/(float)Math.tan(Math.toRadians(NanoVisorZoomHandler.worldFov()*.5));
      var inverse=new Quaternionf(camera.rotation()).conjugate();
      LivingEntity centered=null;
      float closest=Float.POSITIVE_INFINITY,lockedX=0,lockedY=0;
      float frameX=41,frameY=29;
      g.pose().pushPose();
      try {
         g.pose().translate(g.guiWidth()*.5f,g.guiHeight()*.5f,0);
         g.pose().scale(scale,scale,1);
         for(LivingEntity entity:candidates) {
            if(!entity.isAlive()||player.distanceToSqr(entity)>RANGE*RANGE) continue;
            Vec3 point=entity.getPosition(partial).add(0,entity.getBbHeight()+.22,0).subtract(camera.getPosition());
            Vector3f relative=inverse.transform(new Vector3f((float)point.x,(float)point.y,(float)point.z));
            if(relative.z>=-.01f) continue;
            float x=-relative.x*focal/relative.z,y=relative.y*focal/relative.z;
            if(Math.abs(x)>halfWidth||Math.abs(y)>270) continue;
            boolean marked=NanoVisorClientMarkedTargets.isMarkedActive(entity);
            if(marked) {
               int threat=ClassicEnemyContacts.nanosuit2Threat(entity).ordinal();
               Nanosuit2Hud.shape(g,tint(threat<2?0xff79ff92:Nanosuit2Hud.threatColor(threat),alpha),x-3,y-1,x+3,y-1,x,y+4);
            }
            line(g,x-5,y-3,x+5,y-3,tint(marked?0xffff3932:0xffffffff,alpha),.9f);
            line(g,x+5,y-3,x,y+6,tint(marked?0xffff3932:0xffffffff,alpha),.9f);
            line(g,x,y+6,x-5,y-3,tint(marked?0xffff3932:0xffffffff,alpha),.9f);
            float distance=x*x+y*y;
            if(Math.abs(x)<frameX&&Math.abs(y)<frameY&&distance<closest) {
               centered=entity;
               closest=distance;
               lockedX=x;
               lockedY=y;
            }
         }
         UUID target=centered==null?null:centered.getUUID();
         if(!java.util.Objects.equals(focus,target)) {
            focus=target;
            focusedAt=now;
            requested=false;
            markedAtEntry=centered!=null&&NanoVisorClientMarkedTargets.isMarkedActive(centered);
            confirmedWhileFocused=markedAtEntry;
         }
         long delta=lastFrame==0?0:Math.min(60,now-lastFrame);
         lastFrame=now;
         spread+=( (centered==null?0:1)-spread)*(1-(float)Math.exp(-delta/100.0));
         int rail=tint(0xb889afa2,alpha),yellow=tint(0xffffe44d,alpha);
         int moving=Math.round(71*spread);
         int imageTint=tint(-1,alpha);
         Nanosuit2Art.sprite(g,"optic-left",-265-moving,-95,105,190,imageTint);
         Nanosuit2Art.sprite(g,"optic-right",160+moving,-95,105,190,imageTint);
         Nanosuit2Art.sprite(g,"optic-center",-50,-95,100,190,imageTint);
         for(int side:new int[]{-1,1}) {
            float x=side*(193+71*spread);
            for(int i=-36;i<=36;i++) {
               float y=(i*3+player.getViewXRot(partial))*1.8f;
               if(Math.abs(y)>63) continue;
               int color=i==0?yellow:i%10==0?tint(0xffb7e1cf,alpha):rail;
               line(g,x+side*3,y,x+side*(i%10==0?14:9),y,color,1.7f);
            }
         }
         double distance=NanoVisorHudOverlay.classicDistance(player,partial);
         String range=distance<0?"— m":String.format(Locale.ROOT,"%.0f m",distance);
         opticText(g,range,-155,-65,ink(alpha),true);
         opticText(g,NanoVisorZoomHandler.isZoomInputActive()?"×3":"×1",155,-65,ink(alpha),true);
         if(centered!=null) renderFocus(g,centered,lockedX,lockedY,halfWidth,now,alpha);
      } finally {g.pose().popPose();}
   }

   private static void renderFocus(GuiGraphics g,LivingEntity target,float x,float y,float halfWidth,long now,float alpha) {
      long elapsed=now-focusedAt;
      confirmedWhileFocused|=NanoVisorClientMarkedTargets.isMarkedActive(target);
      boolean marked=confirmedWhileFocused;
      if(elapsed>=1000&&!marked&&!requested) {
         requested=true;
         NanoVisorTargetingHandler.requestTarget(target,Minecraft.getInstance().player);
      }
      long feedback=elapsed-(markedAtEntry?500:1000);
      int ink=tint(Nanosuit2Hud.INK,alpha);
      ring(g,x,y,30,6,6,ink);
      if(feedback<0||feedback<1000&&feedback/125%2==0) {
         Nanosuit2Hud.shape(g,ink,x,y+3,x+6,y+9,x,y+15,x-6,y+9);
         Nanosuit2Hud.shape(g,tint(0xff142423,alpha),x,y+4.4f,x+4.6f,y+9,x,y+13.6f,x-4.6f,y+9);
      }
      if(!markedAtEntry&&!marked) ring(g,x,y+8,10,30,Math.min(30,(int)(elapsed*30/1000)),ink);
      if(!marked||feedback<0) return;
      int fx=Math.min(198,Math.round(halfWidth-246));
      Nanosuit2Art.sprite(g,"optic-info",fx,-153,239,146,tint(-1,alpha));
      g.pose().pushPose();
      try {
         g.pose().translate(fx+1,-133,0);
         Nanosuit2Art.shear(g,0,0,-.075f,1);
         opticText(g,target.getDisplayName().getString().toUpperCase(Locale.ROOT),12,5,ink,false);
         opticText(g,net.minecraft.network.chat.Component.translatable("hud.crysismod.n2.hostile").getString(),12,29,ink,false);
         if(!target.getMainHandItem().isEmpty())
            opticText(g,"›  "+target.getMainHandItem().getHoverName().getString(),12,47,ink,false);
         opticText(g,"›  "+Math.round(Minecraft.getInstance().player.distanceTo(target))+" m",12,65,ink,false);
         int threat=ClassicEnemyContacts.nanosuit2Threat(target).ordinal();
         for(int i=0;i<6;i++) {
            float bar=164+i*11;
            Nanosuit2Hud.shape(g,tint(i==0?0xffeffff8:i<=threat+1?Nanosuit2Hud.threatColor(threat):0xff254840,alpha),
               bar,117,bar+8,117,bar+10,120,bar+8,123,bar,123,bar-2,120);
         }
      } finally {g.pose().popPose();}
      if(feedback<3400) {
         float hx=x+80,hy=y+61;
         Nanosuit2Hud.shape(g,tint(0xc51b382f,alpha),hx-63,hy,hx-31.5f,hy-54.6f,hx+31.5f,hy-54.6f,hx+63,hy,hx+31.5f,hy+54.6f,hx-31.5f,hy+54.6f);
         Nanosuit2Hologram.render(g,target,hx,hy,feedback,alpha);
         ring(g,hx,hy,63,6,6,ink);
      }
   }

   private static int ink(float alpha) {return tint(Nanosuit2Hud.INK,alpha);}

   private static void opticText(GuiGraphics g,String text,int x,int y,int color,boolean centered) {
      int width=Minecraft.getInstance().font.width(text);
      float scale=Math.min(1.26f,210f/Math.max(1,width));
      float left=centered?x-width*scale*.5f:x;
      Nanosuit2Hud.label(g,text,left+1,y+1,scale,color&0x00ffffff|0x36000000);
      Nanosuit2Hud.label(g,text,left,y,scale,color);
   }

   private static int tint(int color,float opacity) {
      return color&0xffffff|Math.round((color>>>24)*opacity)<<24;
   }

   private static void ring(GuiGraphics g,float x,float y,float radius,int sides,int amount,int color) {
      for(int i=0;i<amount;i++) {
         double start=i*Math.PI*2/sides,end=(i+1)*Math.PI*2/sides;
         float ax=x+(float)Math.cos(start)*radius,ay=y+(float)Math.sin(start)*radius;
         float bx=x+(float)Math.cos(end)*radius,by=y+(float)Math.sin(end)*radius;
         if(sides==6) dashed(g,ax,ay,bx,by,color);
         else line(g,ax,ay,bx,by,color,.9f);
      }
   }

   private static void dashed(GuiGraphics g,float ax,float ay,float bx,float by,int color) {
      float dx=bx-ax,dy=by-ay,length=Mth.sqrt(dx*dx+dy*dy);
      for(float start=0;start<length;start+=6) {
         float end=Math.min(length,start+3);
         line(g,ax+dx*start/length,ay+dy*start/length,ax+dx*end/length,ay+dy*end/length,color,.9f);
      }
   }

   private static void line(GuiGraphics g,float x,float y,float endX,float endY,int color,float width) {
      float length=Mth.sqrt((endX-x)*(endX-x)+(endY-y)*(endY-y));
      if(length<.001f) return;
      float dx=(endY-y)*width*.5f/length,dy=(x-endX)*width*.5f/length;
      Nanosuit2Hud.shape(g,color,x-dx,y-dy,endX-dx,endY-dy,endX+dx,endY+dy,x+dx,y+dy);
   }
}
