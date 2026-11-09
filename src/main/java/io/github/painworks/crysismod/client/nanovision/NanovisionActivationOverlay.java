package io.github.painworks.crysismod.client.nanovision;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanovisionActivationOverlay {
   private static final long TEXT_DURATION_MS = 2000L;
   private static final long TEXT_FADE_MS = 300L;
   private static final long PULSE_DURATION_MS = 950L;
   private static final long FLASH_START_MS = 100L;
   private static final long FLASH_PEAK_MS = 400L;
   private static final long FLASH_END_MS = 800L;
   private static final int PULSE_SEGMENTS = 160;
   private static final float PULSE_HALF_WIDTH = 34.0F;
   private static final int PULSE_BASE_ALPHA = 120;
   private static final float PULSE_RED = 0.09411765F;
   private static final float PULSE_GREEN = 0.5372549F;
   private static final float PULSE_BLUE = 1.0F;
   private static long activationStartMs = Long.MIN_VALUE;

   private NanovisionActivationOverlay() {
   }

   public static void begin() {
      activationStartMs = Util.getMillis();
   }

   public static void reset() {
      activationStartMs = Long.MIN_VALUE;
   }

   public static float thermalFlashIntensity() {
      if (activationStartMs == Long.MIN_VALUE) {
         return 0.0F;
      } else {
         long elapsedMs = Math.max(0L, Util.getMillis() - activationStartMs);
         if (elapsedMs < 100L || elapsedMs >= 800L) {
            return 0.0F;
         } else {
            return elapsedMs <= 400L
               ? smoothstep(0.0F, 1.0F, (float)(elapsedMs - 100L) / 300.0F)
               : 1.0F - smoothstep(0.0F, 1.0F, (float)(elapsedMs - 400L) / 400.0F);
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onRenderGuiOverlay(Post event) {
      if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
         Minecraft minecraft = Minecraft.getInstance();
         if (minecraft.player != null && !minecraft.options.hideGui && activationStartMs != Long.MIN_VALUE) {
            long elapsedMs = Math.max(0L, Util.getMillis() - activationStartMs);
            if (elapsedMs >= 2000L) {
               reset();
            } else {
               int width = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth();
               int height = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight();
               if (NanovisionConfig.nanovisionActivationWaveEnabled() && elapsedMs < 950L) {
                  renderPulse(event.getGuiGraphics(), width, height, (float)elapsedMs / 950.0F);
               }

               renderEnabledText(event.getGuiGraphics(), minecraft, width, height, elapsedMs);
            }
         }
      }
   }

   private static void renderEnabledText(GuiGraphics guiGraphics, Minecraft minecraft, int screenWidth, int screenHeight, long elapsedMs) {
      float alpha = elapsedMs <= 1700L ? 1.0F : 1.0F - (float)(elapsedMs - 1700L) / 300.0F;
      int argb = Mth.clamp(Math.round(alpha * 255.0F), 0, 255) << 24 | 16777215;
      Component text = Component.translatable("message.crysismod.nanovision_enabled");
      int x = screenWidth / 2 - minecraft.font.width(text) / 2 + NanovisionConfig.nanovisionEnabledTextOffsetX();
      int y = screenHeight / 2 + NanovisionConfig.nanovisionEnabledTextOffsetY();
      guiGraphics.drawString(minecraft.font, text, x, y, argb, false);
   }

   private static void renderPulse(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float progress) {
      float eased = 1.0F - (1.0F - progress) * (1.0F - progress);
      float maxRadius = (float)Math.hypot(screenWidth * 0.5F, screenHeight * 0.5F) + 34.0F;
      float centerRadius = maxRadius * eased;
      float fadeIn = Mth.clamp(progress / 0.1F, 0.0F, 1.0F);
      float fadeOut = 1.0F - smoothstep(0.62F, 1.0F, progress);
      float centerAlpha = 0.47058824F * fadeIn * fadeOut;
      if (!(centerAlpha <= 0.001F)) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         Matrix4f matrix = guiGraphics.pose().last().pose();
         BufferBuilder buffer = Tesselator.getInstance().begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
         float centerX = screenWidth * 0.5F;
         float centerY = screenHeight * 0.5F;

         for (int segment = 0; segment < 160; segment++) {
            float angle0 = (float)((Math.PI * 2) * segment / 160.0);
            float angle1 = (float)((Math.PI * 2) * (segment + 1) / 160.0);
            float wave0 = waveOffset(angle0, progress);
            float wave1 = waveOffset(angle1, progress);
            addPulseStrip(
               buffer,
               matrix,
               centerX,
               centerY,
               angle0,
               angle1,
               Math.max(0.0F, centerRadius - 34.0F + wave0),
               Math.max(0.0F, centerRadius - 34.0F + wave1),
               Math.max(0.0F, centerRadius + wave0),
               Math.max(0.0F, centerRadius + wave1),
               0.0F,
               centerAlpha
            );
            addPulseStrip(
               buffer,
               matrix,
               centerX,
               centerY,
               angle0,
               angle1,
               Math.max(0.0F, centerRadius + wave0),
               Math.max(0.0F, centerRadius + wave1),
               Math.max(0.0F, centerRadius + 34.0F + wave0),
               Math.max(0.0F, centerRadius + 34.0F + wave1),
               centerAlpha,
               0.0F
            );
         }

         BufferUploader.drawWithShader(buffer.buildOrThrow());
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
      }
   }

   private static float waveOffset(float angle, float progress) {
      return (float)(Math.sin(angle * 7.0F + progress * 9.0F) * 7.0 + Math.sin(angle * 13.0F - progress * 6.0F) * 3.0);
   }

   private static void addPulseStrip(
      BufferBuilder buffer,
      Matrix4f matrix,
      float centerX,
      float centerY,
      float angle0,
      float angle1,
      float innerRadius0,
      float innerRadius1,
      float outerRadius0,
      float outerRadius1,
      float innerAlpha,
      float outerAlpha
   ) {
      float ix0 = centerX + (float)Math.cos(angle0) * innerRadius0;
      float iy0 = centerY + (float)Math.sin(angle0) * innerRadius0;
      float ix1 = centerX + (float)Math.cos(angle1) * innerRadius1;
      float iy1 = centerY + (float)Math.sin(angle1) * innerRadius1;
      float ox0 = centerX + (float)Math.cos(angle0) * outerRadius0;
      float oy0 = centerY + (float)Math.sin(angle0) * outerRadius0;
      float ox1 = centerX + (float)Math.cos(angle1) * outerRadius1;
      float oy1 = centerY + (float)Math.sin(angle1) * outerRadius1;
      vertex(buffer, matrix, ix0, iy0, innerAlpha);
      vertex(buffer, matrix, ox0, oy0, outerAlpha);
      vertex(buffer, matrix, ox1, oy1, outerAlpha);
      vertex(buffer, matrix, ix0, iy0, innerAlpha);
      vertex(buffer, matrix, ox1, oy1, outerAlpha);
      vertex(buffer, matrix, ix1, iy1, innerAlpha);
   }

   private static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, float alpha) {
      buffer.addVertex(matrix, x, y, 0.0F).setColor(0.09411765F, 0.5372549F, 1.0F, alpha);
   }

   private static float smoothstep(float edge0, float edge1, float value) {
      float t = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }
}
