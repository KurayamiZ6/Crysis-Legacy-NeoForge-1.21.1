package io.github.painworks.crysismod.client.minimap.render;

import io.github.painworks.crysismod.client.minimap.ClassicWaypoint;
import io.github.painworks.crysismod.client.minimap.NanoMinimapCoordinateMapper;
import io.github.painworks.crysismod.client.minimap.NanoMinimapViewport;
import io.github.painworks.crysismod.nanosuit.client.Nanosuit2Hud;
import io.github.painworks.crysismod.nanosuit.client.Nanosuit2Art;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import io.github.painworks.crysismod.nanosuit.client.ClassicEnemyContacts;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid="crysismod",value=Dist.CLIENT)
final class Nanosuit2Radar {
   private static final int[] BARS={2,7,13,20};
   private static final int WAYPOINT=0xff3bddff;
   private static final int PLAYER_MARKER = 0xffffe582;
   private static final int PASSIVE_MARKER = 0xff52eb68;
   private static final int MARKER_TILE_SIZE = 40;
   // The compass band is centered at atlas row 43, and the glyph ink at tile row 15.
   private static final int COMPASS_CENTER_Y = -59 + 43;
   private static final int COMPASS_GLYPH_CENTER = 15;
   private static int lit,target,displayedLevel;
   private static long stepAt,maxAt=-1;
   private static LocalPlayer owner;
   private static Entity shotOwner;
   private static long shotAt=-1;

   private Nanosuit2Radar() {}

   @SubscribeEvent
   public static void onProjectile(EntityJoinLevelEvent event) {
      if(event.getLevel().isClientSide() && event.getEntity() instanceof Projectile projectile
         && projectile.getOwner() instanceof Enemy enemy && enemy instanceof Entity shooter) {
         shotOwner=shooter;
         shotAt=Util.getMillis();
      }
   }

   private static int alpha(int color,float opacity) {
      return color&0xffffff|Mth.clamp(Math.round((color>>>24)*opacity),0,255)<<24;
   }

   static void mask(GuiGraphics g,NanoMinimapViewport v) {
      g.pose().pushPose();
      try {
         g.pose().translate(v.x(),v.y(),0);
         g.pose().scale(v.width()/340f,v.height()/184f,1);
         // Convex strips of the authored terrain clip, in the radar sprite's viewport coordinates.
         float left = 11.81F, right = 340.65F;
         float rightBevel = 137.37F, notch = 147.19F, rightAtNotch = 332.42F;
         Nanosuit2Hud.shape(g, -1, 13.73F, 0, right, 0, right, rightBevel, left, rightBevel, left, 2.33F);
         Nanosuit2Hud.shape(g, -1, left, rightBevel, right, rightBevel, rightAtNotch, notch, left, notch);
         Nanosuit2Hud.shape(g, -1, left, notch, 86.83F, notch, 64.08F, 175.80F, left, 175.80F);
         Nanosuit2Hud.shape(g, -1, 274.37F, notch, rightAtNotch, notch, 313.51F, 169.78F, 297.12F, 169.78F);
      } finally {g.pose().popPose();}
   }

   static void frame(GuiGraphics g,NanoMinimapViewport v,LocalPlayer player,float yaw,boolean clipped,boolean border) {
      long now=Util.getMillis();
      int level=ThreatGauge.current(player).ordinal();
      if(owner!=player) {
         owner=player;
         target=lit=BARS[level];
         displayedLevel=level;
         stepAt=now;
         maxAt=-1;
      }
      if(target!=BARS[level]) {
         target=BARS[level];
         maxAt=-1;
         stepAt=now;
      }
      if(lit!=target && now-stepAt>=150) {
         int step=(int)Math.min(20,(now-stepAt)/150);
         lit+=Integer.signum(target-lit)*Math.min(step,Math.abs(target-lit));
         while(displayedLevel+1<BARS.length && lit>=BARS[displayedLevel+1]) displayedLevel++;
         while(displayedLevel>0 && lit<=BARS[displayedLevel-1]) displayedLevel--;
         stepAt+=step*150L;
         if(lit==20) maxAt=now;
      }
      boolean blink=maxAt>=0 && now-maxAt<880 && (now-maxAt)%220>=110;
      float opacity=v.opacity();
      int rail=alpha(0xffa6d4c6,opacity*.69f);
      int glow=alpha(Nanosuit2Hud.threatColor(displayedLevel),blink?.40f:1f);
      int untinted=alpha(-1,opacity);
      g.pose().pushPose();
      try {
         g.pose().translate(v.x(),v.y(),0);
         g.pose().scale(v.width()/340f,v.height()/184f,1);
         if(border) {
            Nanosuit2Art.sprite(g,"radar-0",0,-59,462,251,untinted);
            Nanosuit2Art.sprite(g,"radar-threat-base",0,-59,462,251,glow);
         }
         int height=Math.round(141*lit/20f);
         Nanosuit2Art.region(g,"threat-segments",345,110-height,35,height,0,141-height,35,height,35,141,glow);
         Nanosuit2Art.sprite(g,"threat-label",301,-60,79,25,glow);
         float offset=Mth.wrapDegrees(yaw)/90;
         for(int i=-2;i<=2;i++) {
            int heading=Mth.floor(offset)+i;
            float mid=170+(heading-offset)*158;
            if(mid>40&&mid<325) {
               int glyph=(heading+2)&3;
               Nanosuit2Art.region(g,"compass",Math.round(mid)-16,COMPASS_CENTER_Y-COMPASS_GLYPH_CENTER,
                  32,32,glyph*32,0,32,32,128,32,-1);
            }
            if(mid-34>20&&mid-34<339)
               g.fill(Math.round(mid-34),COMPASS_CENTER_Y-6,Math.round(mid-34)+2,COMPASS_CENTER_Y+6,rail);
            if(mid+34>20&&mid+34<339)
               g.fill(Math.round(mid+34),COMPASS_CENTER_Y-6,Math.round(mid+34)+2,COMPASS_CENTER_Y+6,rail);
         }
         String pose=player.isVisuallySwimming()?"prone":player.isCrouching()?"crouching":"standing";
         g.pose().pushPose();
         try {
            Nanosuit2Art.shear(g,383,165,.122785f,1);
            Nanosuit2Art.sprite(g,"stance-"+pose,383,93,77,72,-1);
         } finally {g.pose().popPose();}
      } finally {g.pose().popPose();}
   }

   static void waypoint(GuiGraphics g,NanoMinimapViewport v,NanoMinimapCoordinateMapper mapper,double dx,double dy,
                        boolean rotate,float yaw,boolean clipped) {
      var goal=ClassicWaypoint.current();
      if(goal==null) return;
      var point=new NanoMinimapCoordinateMapper.MutablePoint();
      mapper.worldToViewport(goal.x(),goal.z(),dx,dy,rotate,yaw,point);
      float cx = v.width() * .5f + Nanosuit2Cone.offsetX(v);
      float cy = v.height() * .5f + Nanosuit2Cone.offsetY(v);
      double x = point.x - v.width() * .5, y = point.y - v.height() * .5;
      float left = cx - 5, right = v.width() - cx - 6;
      float top = cy - 5, bottom = v.height() - cy - (clipped ? v.height() * .15f : 5);
      double ratio=Math.min(1,Math.min((x<0?left:right)/Math.abs(x),(y<0?top:bottom)/Math.abs(y)));
      float drawX=v.x()+cx+(float)(x*ratio),drawY=v.y()+cy+(float)(y*ratio);
      if(ratio<1) arrow(g,drawX,drawY,(float)Math.toDegrees(Math.atan2(y,x))+90,WAYPOINT,3);
      else Nanosuit2Hud.shape(g,WAYPOINT,drawX,drawY-4,drawX+4,drawY,drawX,drawY+4,drawX-4,drawY);
   }

   static void contact(GuiGraphics g, NanoMinimapViewport viewport, Entity entity, float x, float y, float yaw) {
      int level=ClassicEnemyContacts.nanosuit2Threat(entity).ordinal();
      long age=Util.getMillis()-shotAt;
      boolean shooting=entity==shotOwner&&age>=0&&age<620;
      if(shooting) {
         float t=1-(float)Math.pow(1-age/620f,3);
         float radius=2+11*t;
         int color=alpha(0xffffffff,1-t);
         for(int i=0;i<16;i++) {
            double a=i*Math.PI/8,b=(i+1)*Math.PI/8;
            float aX=(float)Math.cos(a),aY=(float)Math.sin(a),bX=(float)Math.cos(b),bY=(float)Math.sin(b);
            Nanosuit2Hud.shape(g,color,x+aX*radius,y+aY*radius,x+bX*radius,y+bY*radius,
               x+bX*(radius-.7f),y+bY*(radius-.7f),x+aX*(radius-.7f),y+aY*(radius-.7f));
         }
      }
      int color = Nanosuit2Hud.threatColor(shooting && age < 115 ? 3 : level);
      marker(g, viewport, x, y, yaw, color, 0);
   }

   static void player(GuiGraphics g, NanoMinimapViewport viewport, float x, float y, float yaw) {
      marker(g, viewport, x, y, yaw, PLAYER_MARKER, 0);
   }

   static void passive(GuiGraphics g, NanoMinimapViewport viewport, float x, float y) {
      marker(g, viewport, x, y, 0, PASSIVE_MARKER, MARKER_TILE_SIZE);
   }

   private static void marker(GuiGraphics g, NanoMinimapViewport viewport, float x, float y, float yaw,
                              int color, int sourceX) {
      var pose = g.pose();
      pose.pushPose();
      try {
         pose.translate(x, y, 0);
         pose.scale(viewport.width() / 440F, viewport.height() / 258F, 1);
         pose.mulPose(Axis.ZP.rotationDegrees(yaw));
         int half = MARKER_TILE_SIZE / 2;
         Nanosuit2Art.region(g, "radar-markers", -half, -half, MARKER_TILE_SIZE, MARKER_TILE_SIZE,
            sourceX, 0, MARKER_TILE_SIZE, MARKER_TILE_SIZE, MARKER_TILE_SIZE * 2, MARKER_TILE_SIZE, color);
      } finally {
         pose.popPose();
      }
   }

   private static void arrow(GuiGraphics g,float x,float y,float yaw,int color,float size) {
      g.pose().pushPose();
      try {
         g.pose().translate(x,y,0);
         g.pose().mulPose(Axis.ZP.rotationDegrees(yaw));
         g.pose().scale(size,size,1);
         Nanosuit2Hud.shape(g,color,0,-1.25f,1,.7f,0,.35f,-1,.7f);
      } finally {g.pose().popPose();}
   }
}
