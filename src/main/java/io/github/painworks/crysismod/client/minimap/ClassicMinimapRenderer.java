package io.github.painworks.crysismod.client.minimap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.client.ClassicHudText;

/** Presentation only: samples the existing uploaded terrain, never accesses a chunk. */
public final class ClassicMinimapRenderer {
   private static final ResourceLocation FRAME=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_map/frame.png");
   private static final ResourceLocation BACKGROUND=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_map/background.png");
   private static final ResourceLocation PLAYER=ResourceLocation.fromNamespaceAndPath("crysismod","textures/gui/classic_map/player.png");
   private ClassicMinimapRenderer(){}
   public static boolean isClassic(){var p=Minecraft.getInstance().player;return p!=null&&NanosuitArmorUtil.getEquippedProfile(p)==NanosuitProfile.NANOSUIT_1;}
   public static NanoMinimapViewport viewport(int width,int height){float s=height/1080f*NanosuitClientConfig.classicHudScale();return new NanoMinimapViewport(Math.round(166*s),Math.round(height-249*s),Math.max(8,Math.round(232*s)),Math.max(8,Math.round(163*s)),.9f);}
   public static double blocksPerPixel(){var mc=Minecraft.getInstance();return io.github.painworks.crysismod.client.minimap.config.NanoMinimapClientConfig.minimapRangeBlocks()*2.0/viewport(mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight()).width();}
   public static void render(GuiGraphics g,NanoMinimapViewport v,ResourceLocation terrain,boolean ready,int resolution,double offsetX,double offsetY,float yaw,double activeBpp){
      float s=v.width()/232f;int rgb=NanosuitClientConfig.classicInterfaceColor();
      g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
      if(ready){
         RenderSystem.setShader(GameRenderer::getPositionTexShader);RenderSystem.setShaderTexture(0,terrain);RenderSystem.setShaderColor(1,1,1,v.opacity());
         var b=Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
         double radians=Math.toRadians(180-yaw),cos=Math.cos(radians),sin=Math.sin(radians);
         for(int row=0;row<v.height();row++){
            double y0=row*163.0/v.height(),y1=(row+1)*163.0/v.height();
            float l0=(float)(left(y0)*v.width()/232),l1=(float)(left(y1)*v.width()/232);
            float r0=(float)(right(y0)*v.width()/232),r1=(float)(right(y1)*v.width()/232);
            vertex(b,g,v,l1,row+1,resolution,offsetX,offsetY,cos,sin);vertex(b,g,v,r1,row+1,resolution,offsetX,offsetY,cos,sin);
            vertex(b,g,v,r0,row,resolution,offsetX,offsetY,cos,sin);vertex(b,g,v,l0,row,resolution,offsetX,offsetY,cos,sin);
         }
         BufferUploader.drawWithShader(b.buildOrThrow());RenderSystem.setShaderColor(1,1,1,1);
      }
      g.pose().pushPose();g.pose().translate(v.x(),v.y(),0);g.pose().scale(s,s,1);
      RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
      g.setColor(1,1,1,1);
      g.blit(BACKGROUND,-46,-22,300,250,0,0,600,500,600,500);
      g.setColor((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,1);
      g.blit(FRAME,-46,-22,300,250,0,0,600,500,600,500);g.setColor(1,1,1,1);
      for(int i=0;i<4;i++){
         float delta=Mth.wrapDegrees(i*90-yaw);if(Math.abs(delta)>70)continue;
         float x=116+delta*1.4f;
         String cardinal=new String[]{"S",Minecraft.getInstance().getLanguageManager().getSelected().startsWith("fr")?"O":"W","N","E"}[i];
         ClassicHudText.draw(g,cardinal,x,5-x*13/232,19,rgb,1,true);
      }
      // Player looks up in this rotating map. The pale wedge and chevron are not a goal.
      RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
      g.fill(116,7,117,75,0x55000000|rgb);g.fill(116,91,117,158,0x55000000|rgb);
      g.flush();RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
      g.setColor(1,1,1,1);g.blit(PLAYER,0,0,232,163,0,0,464,326,464,326);
      renderContacts(g,yaw,activeBpp*s);
      var goal=ClassicWaypoint.current();
      if(goal!=null){
         double dx=goal.x()-Minecraft.getInstance().player.getX(),dz=goal.z()-Minecraft.getInstance().player.getZ();
         double bpp=activeBpp*s;
         float gx=(float)(116+(dx*cosFor(yaw)-dz*sinFor(yaw))/bpp),gy=(float)(81.5+(dx*sinFor(yaw)+dz*cosFor(yaw))/bpp);
         if(gy>=0&&gy<163&&gx>left(gy)+4&&gx<right(gy)-4){g.fill((int)gx-3,(int)gy-3,(int)gx+3,(int)gy+3,0xff7df65b);}
      }
      g.pose().popPose();g.setColor(1,1,1,1);RenderSystem.disableBlend();
   }
   private static double cosFor(float yaw){return Math.cos(Math.toRadians(180-yaw));}
   private static void renderContacts(GuiGraphics g,float yaw,double blocksPerPixel){
      var p=Minecraft.getInstance().player;
      for(var entity:io.github.painworks.crysismod.nanosuit.client.ClassicEnemyContacts.entities()){
         double dx=entity.getX()-p.getX(),dz=entity.getZ()-p.getZ();
         float x=(float)(116+(dx*cosFor(yaw)-dz*sinFor(yaw))/blocksPerPixel);
         float y=(float)(81.5+(dx*sinFor(yaw)+dz*cosFor(yaw))/blocksPerPixel);
         if(y<18 || y>155 || x<left(y)+6 || x>right(y)-6)continue;
         int rgb=io.github.painworks.crysismod.nanosuit.client.ClassicEnemyContacts.color(entity);
         g.flush();g.pose().pushPose();g.pose().translate(x,y,0);
         g.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(entity.getYRot()-yaw));
         RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         var b=Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES,DefaultVertexFormat.POSITION_COLOR);
         contactArrow(b,g,1.55f,0x08000000|rgb);contactArrow(b,g,1.25f,0x14000000|rgb);
         int bright=brighten(rgb>>16&255)<<16|brighten(rgb>>8&255)<<8|brighten(rgb&255);
         contactArrow(b,g,1,0x55000000|bright);
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
   private static double left(double y){return y<13?232*(1-y/13):0;}
   private static double right(double y){return y>155?(163-y)*207/8:y>133?232-(y-133)*25/22:232;}
   private static void vertex(BufferBuilder b,GuiGraphics g,NanoMinimapViewport v,float x,float y,int resolution,double ox,double oy,double cos,double sin){
      double dx=x-v.width()/2.0,dy=y-v.height()/2.0;
      float u=(float)(.5+(dx*cos+dy*sin-ox)/resolution),t=(float)(.5+(-dx*sin+dy*cos-oy)/resolution);
      b.addVertex(g.pose().last().pose(),v.x()+x,v.y()+y,0).setUv(u,t);
   }
}
