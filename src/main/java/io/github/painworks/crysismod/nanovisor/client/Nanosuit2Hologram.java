package io.github.painworks.crysismod.nanovisor.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/** Composites the live entity model as a monochrome hologram inside the optic. */
@EventBusSubscriber(modid="crysismod",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class Nanosuit2Hologram {
   private static final int SIZE=192;
   private static TextureTarget modelTarget;
   private static ShaderInstance shader;

   private Nanosuit2Hologram() {}

   @SubscribeEvent
   public static void register(RegisterShadersEvent event) throws IOException {
      if(modelTarget!=null) {
         modelTarget.destroyBuffers();
         modelTarget=null;
      }
      event.registerShader(new ShaderInstance(event.getResourceProvider(),
         ResourceLocation.fromNamespaceAndPath("crysismod","nanosuit2_hologram"),DefaultVertexFormat.POSITION_TEX),loaded->shader=loaded);
   }

   static void render(GuiGraphics graphics,LivingEntity entity,float x,float y,long age,float opacity) {
      if(shader==null) return;
      Minecraft minecraft=Minecraft.getInstance();
      graphics.flush();
      int drawFramebuffer=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
      int readFramebuffer=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
      int[] viewport=new int[4];
      GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);
      boolean depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
      boolean blend=GL11.glIsEnabled(GL11.GL_BLEND);
      boolean cull=GL11.glIsEnabled(GL11.GL_CULL_FACE);
      var savedShader=RenderSystem.getShader();
      var view=RenderSystem.getModelViewStack();
      view.pushMatrix();
      RenderSystem.backupProjectionMatrix();
      try {
         if(modelTarget==null) {
            modelTarget=new TextureTarget(SIZE,SIZE,true,Minecraft.ON_OSX);
            modelTarget.setClearColor(0,0,0,0);
         }
         modelTarget.clear(Minecraft.ON_OSX);
         modelTarget.bindWrite(true);
         RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0,SIZE,SIZE,0,1000,3000),VertexSorting.ORTHOGRAPHIC_Z);
         view.identity().translate(0,0,-2000);
         RenderSystem.applyModelViewMatrix();
         var preview=new GuiGraphics(minecraft,minecraft.renderBuffers().bufferSource());
         float size=132/Math.max(1,Math.max(entity.getBbHeight(),entity.getBbWidth()*1.25f));
         var rotation=new Quaternionf().rotateZ((float)Math.PI).rotateY(age*(float)(Math.PI*2/3400));
         InventoryScreen.renderEntityInInventory(preview,SIZE*.5f,157,size,new Vector3f(),rotation,null,entity);
      } finally {
         GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,drawFramebuffer);
         GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,readFramebuffer);
         RenderSystem.viewport(viewport[0],viewport[1],viewport[2],viewport[3]);
         view.popMatrix();
         RenderSystem.applyModelViewMatrix();
         RenderSystem.restoreProjectionMatrix();
      }
      try {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         shader.setSampler("ModelSampler",modelTarget.getColorTextureId());
         shader.safeGetUniform("Opacity").set(opacity);
         shader.safeGetUniform("Phase").set(age/1000f);
         RenderSystem.setShader(()->shader);
         var pose=graphics.pose().last().pose();
         var quad=Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
         quad.addVertex(pose,x-63,y-54.6f,0).setUv(0,1);
         quad.addVertex(pose,x-63,y+54.6f,0).setUv(0,0);
         quad.addVertex(pose,x+63,y+54.6f,0).setUv(1,0);
         quad.addVertex(pose,x+63,y-54.6f,0).setUv(1,1);
         BufferUploader.drawWithShader(quad.buildOrThrow());
      } finally {
         if(depth) RenderSystem.enableDepthTest(); else RenderSystem.disableDepthTest();
         if(blend) RenderSystem.enableBlend(); else RenderSystem.disableBlend();
         if(cull) RenderSystem.enableCull(); else RenderSystem.disableCull();
         RenderSystem.setShader(()->savedShader);
      }
   }
}
