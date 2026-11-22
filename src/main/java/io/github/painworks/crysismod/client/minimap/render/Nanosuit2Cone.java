package io.github.painworks.crysismod.client.minimap.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import io.github.painworks.crysismod.client.minimap.NanoMinimapViewport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** Projection and easing from the N2 design canvas, checked by export-view-cone.cjs. */
final class Nanosuit2Cone {
   private static final float CANVAS_WIDTH = 440;
   private static final float CANVAS_HEIGHT = 258;
   private static final float PIVOT_X = 0.49F;
   private static final float PIVOT_Y = 0.61F;
   private static final float FIRST_DOT = 17;
   private static final float DOT_SPACING = 9.5F;
   private static final long TRANSITION_MS = 240;
   private static final ResourceLocation DOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
      "crysismod", "textures/gui/nanosuit2/radar-cone-dot.png");

   private LocalPlayer viewer;
   private float aimFrom;
   private float aimTo;
   private long aimChangedAt;

   static float offsetX(NanoMinimapViewport viewport) {
      return viewport.width() * (PIVOT_X - 0.5F);
   }

   static float offsetY(NanoMinimapViewport viewport) {
      return viewport.height() * (PIVOT_Y - 0.5F);
   }

   void draw(GuiGraphics graphics, NanoMinimapViewport viewport, Minecraft minecraft,
             float x, float y, float heading) {
      float aim = aimFraction(minecraft);
      float angle = Mth.lerp(aim, 27F, 12F) * Mth.DEG_TO_RAD;
      float sin = Mth.sin(angle);
      float cos = Mth.cos(angle);
      int tint = Math.round(viewport.opacity() * 255) << 24 | 0xFFFFFF;

      var pose = graphics.pose();
      Matrix4f matrix = new Matrix4f(pose.last().pose())
         .translate(x, y, 0)
         .rotate(Axis.ZP.rotationDegrees(heading))
         .scale(viewport.width() / CANVAS_WIDTH, viewport.height() / CANVAS_HEIGHT, 1);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.setShaderTexture(0, DOT_TEXTURE);
      BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
      for (float distance = FIRST_DOT; distance * cos < CANVAS_HEIGHT * PIVOT_Y; distance += DOT_SPACING) {
         addDot(buffer, matrix, distance * sin, -distance * cos, tint);
         addDot(buffer, matrix, -distance * sin, -distance * cos, tint);
      }
      BufferUploader.drawWithShader(buffer.buildOrThrow());
      RenderSystem.disableBlend();
   }

   private static void addDot(BufferBuilder buffer, Matrix4f matrix, float x, float y, int tint) {
      buffer.addVertex(matrix, x - 2, y + 2, 0).setUv(0, 1).setColor(tint);
      buffer.addVertex(matrix, x + 2, y + 2, 0).setUv(1, 1).setColor(tint);
      buffer.addVertex(matrix, x + 2, y - 2, 0).setUv(1, 0).setColor(tint);
      buffer.addVertex(matrix, x - 2, y - 2, 0).setUv(0, 0).setColor(tint);
   }

   private float aimFraction(Minecraft minecraft) {
      long now = Util.getMillis();
      float target = minecraft.options.keyUse.isDown() && minecraft.screen == null ? 1F : 0F;
      if (viewer != minecraft.player) {
         viewer = minecraft.player;
         aimFrom = aimTo = 0;
         aimChangedAt = now;
      }
      float progress = Mth.clamp((now - aimChangedAt) / (float) TRANSITION_MS, 0F, 1F);
      float current = Mth.lerp(progress * progress * (3 - 2 * progress), aimFrom, aimTo);
      if (target != aimTo) {
         aimFrom = current;
         aimTo = target;
         aimChangedAt = now;
      }
      return current;
   }
}
