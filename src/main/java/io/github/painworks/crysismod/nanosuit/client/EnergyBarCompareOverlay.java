package io.github.painworks.crysismod.nanosuit.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class EnergyBarCompareOverlay {
   private static final ResourceLocation ENERGY_BAR_LIGHTING = ResourceLocation.fromNamespaceAndPath("crysismod", "textures/gui/energy_bar_active_lighting.png");
   private static final int SRC_W = 420;
   private static final int SRC_H = 151;
   private static final float SMALL_SCALE = 0.1F;
   private static final int GAP = 16;
   private static boolean enabled;

   private EnergyBarCompareOverlay() {
   }

   @SubscribeEvent
   public static void onRenderGuiOverlay(Post event) {
      if (enabled) {
         if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            Minecraft minecraft = Minecraft.getInstance();
            if (!minecraft.options.hideGui) {
               GuiGraphics guiGraphics = event.getGuiGraphics();
               int screenW = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth();
               int screenH = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight();
               int centerX = (screenW - 420) / 2;
               int centerY = (screenH - 151) / 2;
               RenderSystem.enableBlend();
               RenderSystem.defaultBlendFunc();
               guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
               guiGraphics.blit(ENERGY_BAR_LIGHTING, centerX, centerY, 0.0F, 0.0F, 420, 151, 420, 151);
               int smallX = centerX + 420 + 16;
               guiGraphics.pose().pushPose();
               guiGraphics.pose().translate(smallX, centerY, 0.0F);
               guiGraphics.pose().scale(0.1F, 0.1F, 1.0F);
               guiGraphics.blit(ENERGY_BAR_LIGHTING, 0, 0, 0.0F, 0.0F, 420, 151, 420, 151);
               guiGraphics.pose().popPose();
               guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
               RenderSystem.disableBlend();
            }
         }
      }
   }

   public static void setEnabled(boolean value) {
      enabled = value;
   }

   public static boolean isEnabled() {
      return enabled;
   }

   public static boolean toggle() {
      enabled = !enabled;
      return enabled;
   }
}
