package io.github.painworks.crysismod.nanosuit.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/** Shared CRT pass for the two N2 panels. Each panel is captured separately over transparent pixels. */
@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT)
public final class Nanosuit2Crt implements AutoCloseable {
   private static ShaderInstance shader;
   private static TextureTarget target;
   private static long frameTime;

   private final GuiGraphics graphics;
   private final int drawFramebuffer;
   private final int readFramebuffer;
   private final int[] viewport = new int[4];
   private final float panelTop;
   private final float panelHeight;

   @SubscribeEvent
   public static void registerShader(RegisterShadersEvent event) throws IOException {
      event.registerShader(new ShaderInstance(event.getResourceProvider(),
         ResourceLocation.fromNamespaceAndPath("crysismod", "nanosuit2_crt"), DefaultVertexFormat.POSITION_TEX),
         loaded -> shader = loaded);
   }

   public static Nanosuit2Crt capture(GuiGraphics graphics, float panelTop, float panelHeight) {
      if (shader == null || panelHeight <= 0 || GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)) return null;
      return new Nanosuit2Crt(graphics, panelTop / graphics.guiHeight(), panelHeight / graphics.guiHeight());
   }

   private Nanosuit2Crt(GuiGraphics graphics, float panelTop, float panelHeight) {
      this.graphics = graphics;
      this.panelTop = panelTop;
      this.panelHeight = panelHeight;
      graphics.flush();
      drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
      readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
      GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
      var main = Minecraft.getInstance().getMainRenderTarget();
      if (target == null || target.width != main.width || target.height != main.height) {
         if (target != null) target.destroyBuffers();
         target = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
         target.enableStencil();
         target.setFilterMode(GL11.GL_LINEAR);
         target.setClearColor(0, 0, 0, 0);
      }
      target.clear(Minecraft.ON_OSX);
      target.bindWrite(true);
   }

   @SubscribeEvent
   public static void beginFrame(RenderGuiEvent.Pre event) {
      frameTime = Util.getMillis();
   }

   @Override
   public void close() {
      graphics.flush();
      GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
      GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
      RenderSystem.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
      boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
      boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
      var oldShader = RenderSystem.getShader();
      try {
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
         shader.setSampler("HudSampler", target.getColorTextureId());
         shader.safeGetUniform("Time").set((frameTime % 1460000L) / 1000F);
         shader.safeGetUniform("Panel").set(panelTop, panelHeight, 1F / target.width);
         RenderSystem.setShader(() -> shader);
         var vertices = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
         vertices.addVertex(-1, -1, 0).setUv(0, 0);
         vertices.addVertex(1, -1, 0).setUv(1, 0);
         vertices.addVertex(1, 1, 0).setUv(1, 1);
         vertices.addVertex(-1, 1, 0).setUv(0, 1);
         BufferUploader.drawWithShader(vertices.buildOrThrow());
      } finally {
         RenderSystem.defaultBlendFunc();
         if (!blend) RenderSystem.disableBlend();
         if (depth) RenderSystem.enableDepthTest();
         RenderSystem.setShader(() -> oldShader);
      }
   }
}
