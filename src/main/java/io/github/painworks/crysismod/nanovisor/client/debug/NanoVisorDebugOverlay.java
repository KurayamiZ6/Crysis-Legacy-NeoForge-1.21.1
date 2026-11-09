package io.github.painworks.crysismod.nanovisor.client.debug;

import io.github.painworks.crysismod.nanovisor.client.NanoVisorState;
import io.github.painworks.crysismod.nanovisor.client.NanoVisorZoomHandler;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorSettings;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanoVisorDebugOverlay {
   private static final int TEXT_COLOR = -6620161;
   private static final int SHADOW_COLOR = -16111058;
   private static final int CENTER_COLOR = -19372;
   private static final int HUD_ANCHOR_COLOR = -16721409;

   private NanoVisorDebugOverlay() {
   }

   @SubscribeEvent
   public static void onRenderGuiOverlay(Post event) {
      if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
         Minecraft minecraft = Minecraft.getInstance();
         if (minecraft.player != null && !minecraft.options.hideGui) {
            boolean visorInfo = NanoVisorSettings.Debug.visorInfo();
            boolean targetingInfo = NanoVisorSettings.Debug.targetingInfo();
            boolean hudAnchors = NanoVisorSettings.Debug.hudAnchors();
            if (visorInfo || targetingInfo || hudAnchors) {
               GuiGraphics guiGraphics = event.getGuiGraphics();
               int screenWidth = guiGraphics.guiWidth();
               int screenHeight = guiGraphics.guiHeight();
               int x = 6;
               int y = 6;
               if (visorInfo) {
                  y = drawLine(guiGraphics, minecraft, x, y, "Visor active: " + NanoVisorState.isVisorActive());
                  y = drawLine(guiGraphics, minecraft, x, y, "Zoom input: " + NanoVisorZoomHandler.isZoomInputActive());
                  y = drawLine(guiGraphics, minecraft, x, y, "Zoom mul: " + String.format(Locale.ROOT, "%.3f", NanoVisorZoomHandler.getCurrentZoomMultiplier()));
               }

               if (targetingInfo) {
                  HitResult hitResult = minecraft.hitResult;
                  String line = "Targeting: none";
                  if (hitResult != null) {
                     line = switch (hitResult.getType()) {
                        case ENTITY -> {
                           EntityHitResult entityHitResult = (EntityHitResult)hitResult;
                           yield "Targeting entity: id="
                              + entityHitResult.getEntity().getId()
                              + " type="
                              + entityHitResult.getEntity().getType().toShortString();
                        }
                        case BLOCK -> "Targeting block";
                        case MISS -> "Targeting miss";
                        default -> throw new IncompatibleClassChangeError();
                     };
                  }

                  y = drawLine(guiGraphics, minecraft, x, y, line);
               }

               if (hudAnchors) {
                  int centerX = screenWidth / 2;
                  int centerY = screenHeight / 2;
                  int hudAnchorX = Math.round(screenWidth * 0.5F + NanoVisorSettings.Hud.offsetX());
                  int hudAnchorY = Math.round(screenHeight * 0.5F + 22.0F + NanoVisorSettings.Hud.offsetY());
                  drawCross(guiGraphics, centerX, centerY, -19372);
                  drawCross(guiGraphics, hudAnchorX, hudAnchorY, -16721409);
                  drawLine(guiGraphics, minecraft, x, y, "HUD anchor: x=" + hudAnchorX + " y=" + hudAnchorY);
               }
            }
         }
      }
   }

   private static int drawLine(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, String text) {
      guiGraphics.drawString(minecraft.font, text, x + 1, y + 1, -16111058, false);
      guiGraphics.drawString(minecraft.font, text, x, y, -6620161, false);
      return y + 9 + 2;
   }

   private static void drawCross(GuiGraphics guiGraphics, int x, int y, int color) {
      guiGraphics.fill(x - 3, y, x + 4, y + 1, color);
      guiGraphics.fill(x, y - 3, x + 1, y + 4, color);
   }
}
