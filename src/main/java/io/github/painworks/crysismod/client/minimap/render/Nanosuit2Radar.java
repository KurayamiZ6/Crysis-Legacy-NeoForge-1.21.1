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
   private static int lit,target;
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
         Nanosuit2Hud.shape(g,-1,13,0,340,0,340,137,11,137,11,3);
         Nanosuit2Hud.shape(g,-1,11,137,86,137,86,147,63,175,11,175);
         Nanosuit2Hud.shape(g,-1,86,137,274,137,274,147,86,147);
         Nanosuit2Hud.shape(g,-1,274,137,340,137,313,169,296,169,274,147);
      } finally {g.pose().popPose();}
   }

   static void frame(GuiGraphics g,NanoMinimapViewport v,LocalPlayer player,float yaw,boolean clipped,boolean border) {
      long now=Util.getMillis();
      int level=ThreatGauge.current(player).ordinal();
      if(owner!=player) {
         owner=player;
         target=lit=BARS[level];
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
         stepAt+=step*150L;
         if(lit==20) maxAt=now;
      }
      boolean blink=maxAt>=0 && now-maxAt<880 && (now-maxAt)%220>=110;
      float opacity=v.opacity();
      int rail=alpha(0xffa6d4c6,opacity*.69f);
      int glow=alpha(Nanosuit2Hud.threatColor(level),opacity*(blink?.40f:1f));
      int untinted=alpha(-1,opacity);
      g.pose().pushPose();
      try {
         g.pose().translate(v.x(),v.y(),0);
         g.pose().scale(v.width()/340f,v.height()/184f,1);
         int palette=level>=2?level:0;
         if(border) Nanosuit2Art.sprite(g,"radar-"+palette,0,-59,462,251,untinted);
         int height=Math.round(141*lit/20f);
         Nanosuit2Art.region(g,"threat-segments",345,110-height,35,height,0,141-height,35,height,35,141,glow);
         Nanosuit2Art.sprite(g,"threat-label",301,-60,79,25,glow);
         float offset=Mth.wrapDegrees(yaw)/90;
         for(int i=-2;i<=2;i++) {
            int heading=Mth.floor(offset)+i;
            float mid=170+(heading-offset)*158;
            if(mid>40&&mid<325) {
               int glyph=(heading+2)&3;
               Nanosuit2Art.region(g,"compass",Math.round(mid)-16,-36,32,32,glyph*32,0,32,32,128,32,untinted);
            }
            if(mid-34>20&&mid-34<339) g.fill(Math.round(mid-34),-24,Math.round(mid-34)+2,-12,rail);
            if(mid+34>20&&mid+34<339) g.fill(Math.round(mid+34),-24,Math.round(mid+34)+2,-12,rail);
         }
         String pose=player.isVisuallySwimming()?"prone":player.isCrouching()?"crouching":"standing";
         g.pose().pushPose();
         try {
            Nanosuit2Art.shear(g,383,165,.122785f,1);
            Nanosuit2Art.sprite(g,"stance-"+pose,383,93,77,72,untinted);
         } finally {g.pose().popPose();}
      } finally {g.pose().popPose();}
   }

   static void waypoint(GuiGraphics g,NanoMinimapViewport v,NanoMinimapCoordinateMapper mapper,double dx,double dy,
                        boolean rotate,float yaw,boolean clipped) {
      var goal=ClassicWaypoint.current();
      if(goal==null) return;
      var point=new NanoMinimapCoordinateMapper.MutablePoint();
      mapper.worldToViewport(goal.x(),goal.z(),dx,dy,rotate,yaw,point);
      float cx=v.width()*.5f,cy=v.height()*.5f;
      double x=point.x-cx,y=point.y-cy;
      float left=cx-5,right=cx-6,top=cy-5,bottom=cy-(clipped?v.height()*.15f:5);
      double ratio=Math.min(1,Math.min((x<0?left:right)/Math.abs(x),(y<0?top:bottom)/Math.abs(y)));
      float drawX=v.x()+cx+(float)(x*ratio),drawY=v.y()+cy+(float)(y*ratio);
      if(ratio<1) arrow(g,drawX,drawY,(float)Math.toDegrees(Math.atan2(y,x))+90,WAYPOINT,3);
      else Nanosuit2Hud.shape(g,WAYPOINT,drawX,drawY-4,drawX+4,drawY,drawX,drawY+4,drawX-4,drawY);
   }

   static void contact(GuiGraphics g,Entity entity,LocalPlayer viewer,float x,float y,float yaw,float scale) {
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
      arrow(g,x,y,yaw,shooting&&age<115?Nanosuit2Hud.threatColor(3):Nanosuit2Hud.threatColor(level),scale);
   }

   static void arrow(GuiGraphics g,float x,float y,float yaw,int color,float size) {
      g.pose().pushPose();
      try {
         g.pose().translate(x,y,0);
         g.pose().mulPose(Axis.ZP.rotationDegrees(yaw));
         g.pose().scale(size,size,1);
         Nanosuit2Hud.shape(g,color,0,-1.25f,1,.7f,0,.35f,-1,.7f);
      } finally {g.pose().popPose();}
   }
}
