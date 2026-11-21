package io.github.painworks.crysismod.client.minimap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.client.ClassicHudText;
import io.github.painworks.crysismod.nanosuit.client.ClassicMalfunction;
import io.github.painworks.crysismod.nanosuit.client.ClassicHudPalette;

/** Presentation only: samples the existing uploaded terrain, never accesses a chunk. */
public final class ClassicMinimapRenderer {
   private static final ResourceLocation FRAME=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_map/frame.png");
   private static final ResourceLocation BACKGROUND=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_map/background.png");
   private static final ResourceLocation PLAYER=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_map/player.png");
   private static final ResourceLocation PLAYER_ARROW=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_map/player-arrow.png");
   private static final String[] ENGLISH_HEADINGS={"S","SW","W","NW","N","NE","E","SE"};
   private static final String[] FRENCH_HEADINGS={"S","SO","O","NO","N","NE","E","SE"};
   // The badge's upper-right edge rises 21 units over 36. Keep a 6-unit perpendicular gap.
   private static final double BADGE_EDGE_SLOPE=21.0/36;
   private static final double NOTCH_START=109+BADGE_EDGE_SLOPE-6*Math.hypot(1,BADGE_EDGE_SLOPE);
   private ClassicMinimapRenderer(){}
   public static boolean isClassic(){var p=Minecraft.getInstance().player;return p!=null&&NanosuitArmorUtil.getEquippedProfile(p)==NanosuitProfile.NANOSUIT_1;}
   public static NanoMinimapViewport viewport(int width,int height){float s=height/1080f*NanosuitClientConfig.classicHudScale();return new NanoMinimapViewport(Math.round(166*s),Math.round(height-249*s),Math.max(8,Math.round(232*s)),Math.max(8,Math.round(163*s)),.9f);}
   public static double blocksPerPixel(){var mc=Minecraft.getInstance();return io.github.painworks.crysismod.client.minimap.config.NanoMinimapClientConfig.minimapRangeBlocks()*2.0/viewport(mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight()).width();}
   public static void render(GuiGraphics g,NanoMinimapViewport v,ResourceLocation terrain,boolean ready,int resolution,double offsetX,double offsetY,float yaw,double activeBpp){
      float s=v.width()/232f;
      boolean jammed=ClassicMalfunction.active();
      int rgb=ClassicMalfunction.hudColor();
      long sweep=jammed?ClassicMalfunction.elapsedMillis():0;
      g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
      if(ready){
         RenderSystem.setShader(GameRenderer::getPositionTexShader);RenderSystem.setShaderTexture(0,terrain);RenderSystem.setShaderColor(1,1,1,v.opacity());
         var b=Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
         double radians=Math.toRadians(180-yaw),cos=Math.cos(radians),sin=Math.sin(radians);
         for(int row=0;row<v.height();row++){
            double y0=row*163.0/v.height(),y1=(row+1)*163.0/v.height();
            float l0=(float)(left(y0)*v.width()/232),l1=(float)(left(y1)*v.width()/232);
            float r0=(float)(right(y0)*v.width()/232),r1=(float)(right(y1)*v.width()/232);
            // Shift texture coordinates inside short bands, while the terrain silhouette stays fixed.
            float drift=jammed?signalDrift(y0,sweep)*s:0;
            vertex(b,g,v,l1,row+1,resolution,offsetX,offsetY,cos,sin,drift);
            vertex(b,g,v,r1,row+1,resolution,offsetX,offsetY,cos,sin,drift);
            vertex(b,g,v,r0,row,resolution,offsetX,offsetY,cos,sin,drift);
            vertex(b,g,v,l0,row,resolution,offsetX,offsetY,cos,sin,drift);
         }
         BufferUploader.drawWithShader(b.buildOrThrow());RenderSystem.setShaderColor(1,1,1,1);
      }
      g.pose().pushPose();g.pose().translate(v.x(),v.y(),0);g.pose().scale(s,s,1);
      RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
      if(jammed)drawInterference(g,sweep);
      g.setColor(1,1,1,1);
      g.blit(BACKGROUND,-46,-22,300,250,0,0,600,500,600,500);
      g.setColor((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,1);
      g.blit(FRAME,-46,-22,300,250,0,0,600,500,600,500);g.setColor(1,1,1,1);
      boolean french=Minecraft.getInstance().getLanguageManager().getSelected().startsWith("fr");
      String[] headings=french?FRENCH_HEADINGS:ENGLISH_HEADINGS;
      for(int i=0;i<headings.length;i++){
         float x=116+Mth.wrapDegrees(i*45-yaw)*1.4f;
         String heading=headings[i];
         float half=heading.length()*4.75f;
         if(x-half>=9 && x+half<=223) ClassicHudText.draw(g,heading,x,5-x*13/232,19,rgb,1,true);
      }
      // Player looks up in this rotating map. The pale wedge and chevron are not a goal.
      RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
      g.fill(116,7,117,75,0x55000000|rgb);g.fill(116,91,117,158,0x55000000|rgb);
      g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
      if(jammed)g.setColor((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,1);
      g.blit(PLAYER,0,0,232,163,0,0,464,326,464,326);
      int arrow=ClassicHudPalette.loadedAmmoColor(rgb);
      g.setColor(ClassicHudPalette.red(arrow),ClassicHudPalette.green(arrow),ClassicHudPalette.blue(arrow),1);
      g.blit(PLAYER_ARROW,0,0,232,163,0,0,464,326,464,326);
      g.setColor(1,1,1,1);
      if(!jammed)renderMarkers(g,yaw,activeBpp*s);
      g.pose().popPose();g.setColor(1,1,1,1);RenderSystem.disableBlend();
   }
   private static void renderMarkers(GuiGraphics g,float yaw,double bpp){
      renderContacts(g,yaw,bpp);
      var goal=ClassicWaypoint.current();
      if(goal!=null){
         double dx=goal.x()-Minecraft.getInstance().player.getX(),dz=goal.z()-Minecraft.getInstance().player.getZ();
         float gx=(float)(116+(dx*cosFor(yaw)-dz*sinFor(yaw))/bpp),gy=(float)(81.5+(dx*sinFor(yaw)+dz*cosFor(yaw))/bpp);
         if(fitsInside(gx,gy,7)){
            int x=Math.round(gx),y=Math.round(gy);
            g.fill(x-6,y-4,x+6,y+4,0x287df65b);
            g.fill(x-4,y-6,x+4,y+6,0x287df65b);
            g.fill(x-4,y-3,x+4,y+3,0x687df65b);
            g.fill(x-2,y-2,x+2,y+2,0xf27df65b);
         }
      }
   }
   private static double cosFor(float yaw){return Math.cos(Math.toRadians(180-yaw));}
   private static void renderContacts(GuiGraphics g,float yaw,double blocksPerPixel){
      var p=Minecraft.getInstance().player;
      for(var entity:io.github.painworks.crysismod.nanosuit.client.ClassicEnemyContacts.entities()){
         double dx=entity.getX()-p.getX(),dz=entity.getZ()-p.getZ();
         float x=(float)(116+(dx*cosFor(yaw)-dz*sinFor(yaw))/blocksPerPixel);
         float y=(float)(81.5+(dx*sinFor(yaw)+dz*cosFor(yaw))/blocksPerPixel);
         if(!fitsInside(x,y,11))continue;
         int rgb=io.github.painworks.crysismod.nanosuit.client.ClassicEnemyContacts.color(entity);
         g.flush();g.pose().pushPose();g.pose().translate(x,y,0);
         g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(entity.getYRot()-yaw));
         RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         var b=Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES,DefaultVertexFormat.POSITION_COLOR);
         contactArrow(b,g,1.7f,0x08000000|rgb);contactArrow(b,g,1.4f,0x14000000|rgb);
         int bright=brighten(rgb>>16&255)<<16|brighten(rgb>>8&255)<<8|brighten(rgb&255);
         contactArrow(b,g,1.2f,0xb5000000|bright);
         BufferUploader.drawWithShader(b.buildOrThrow());g.pose().popPose();
      }
   }
   private static int brighten(int channel){return channel+(255-channel)/9;}
   private static void contactArrow(BufferBuilder b,GuiGraphics g,float s,int color){
      contactVertex(b,g,0,-6*s,color);contactVertex(b,g,-5.5f*s,4*s,color);contactVertex(b,g,0,s,color);
      contactVertex(b,g,0,-6*s,color);contactVertex(b,g,0,s,color);contactVertex(b,g,5.5f*s,4*s,color);
   }
   private static void contactVertex(BufferBuilder b,GuiGraphics g,float x,float y,int color){
      b.addVertex(g.pose().last().pose(),x,y,0).setColor(color);
   }
   private static double sinFor(float yaw){return Math.sin(Math.toRadians(180-yaw));}
   // Polygon follows the compass slope, the notch alongside the warning badge and the chamfer at the right.
   private static double left(double y){
      if(y<13)return 232*(1-y/13);
      return Mth.clamp((y-NOTCH_START)/BADGE_EDGE_SLOPE,0,41);
   }
   private static double right(double y){
      if(y<=133)return 232;
      return y<=155?232-(y-133)*25/22:207-(y-155)*166/8;
   }
   public static void drawAlertBar(GuiGraphics g,int color){
      for(int segment=0;segment<4;segment++){
         int top=17+segment*24,bottom=top+21;
         if(left(bottom)<=3){
            g.fill(RenderType.guiOverlay(),3,top,15,bottom,color);
            continue;
         }
         for(int y=top;y<bottom;y++){
            int x=Math.max(3,(int)Math.ceil(left(y+1)));
            if(x<15)g.fill(RenderType.guiOverlay(),x,y,15,y+1,color);
         }
      }
   }
   private static boolean fitsInside(float x,float y,int radius){
      return y>=radius && y<=163-radius
         && x>=Math.max(left(y-radius),left(y+radius))+radius
         && x<=Math.min(right(y-radius),right(y+radius))-radius;
   }
   private static float signalDrift(double y,long millis){
      int band=(int)y/11;
      int seed=noise(band*19+(int)(millis/65));
      return (seed&7)==0 ? ((seed>>>8)&15)-7 : 0;
   }
   private static void drawInterference(GuiGraphics g,long millis){
      g.flush();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      var b=Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR);
      int frame=(int)(millis/67);
      float beam=millis%950*175/950f-6;
      for(int y=0;y<163;y++){
         int sample=noise(y+frame*163);
         int redAlpha=34+((sample>>>10)&15)+(Math.abs(y-beam)<4?26:0);
         interferenceQuad(b,g,y,0,232,ClassicMalfunction.RED|(redAlpha<<24));
         if(y%3==0)interferenceQuad(b,g,y,0,232,0x291b1115);
         if((sample&3)==0){
            int x=(sample>>>8)%232;
            int span=12+((sample>>>18)&63);
            interferenceQuad(b,g,y,x,x+span,0x42ffd9dd);
         }
      }
      BufferUploader.drawWithShader(b.buildOrThrow());
   }
   private static int noise(int seed){
      int mixed=(seed^(seed>>>16))*0x45d9f3b;
      return (mixed^(mixed>>>16))*0x45d9f3b;
   }
   private static void interferenceQuad(BufferBuilder b,GuiGraphics g,int y,int start,int end,int rgb){
      float leftTop=(float)Mth.clamp(start,left(y),right(y));
      float rightTop=(float)Mth.clamp(end,left(y),right(y));
      float leftBottom=(float)Mth.clamp(start,left(y+1),right(y+1));
      float rightBottom=(float)Mth.clamp(end,left(y+1),right(y+1));
      contactVertex(b,g,leftBottom,y+1,rgb);contactVertex(b,g,rightBottom,y+1,rgb);
      contactVertex(b,g,rightTop,y,rgb);contactVertex(b,g,leftTop,y,rgb);
   }
   private static void vertex(BufferBuilder b,GuiGraphics g,NanoMinimapViewport v,float x,float y,int resolution,double ox,double oy,double cos,double sin,float drift){
      double dx=x-v.width()/2.0+drift,dy=y-v.height()/2.0;
      float u=(float)(.5+(dx*cos+dy*sin-ox)/resolution),t=(float)(.5+(-dx*sin+dy*cos-oy)/resolution);
      b.addVertex(g.pose().last().pose(),v.x()+x,v.y()+y,0).setUv(u,t);
   }
}
