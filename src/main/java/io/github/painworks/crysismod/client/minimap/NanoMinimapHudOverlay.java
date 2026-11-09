package io.github.painworks.crysismod.client.minimap;

import io.github.painworks.crysismod.nanosuit.client.NanosuitHudMasterCompositor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;


import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanoMinimapHudOverlay {
   private NanoMinimapHudOverlay() {
   }

   @SubscribeEvent
   public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      if (true) {
         NanoMinimapManager.get().onClientTick(Minecraft.getInstance());
      }
   }

   @SubscribeEvent
   public static void onRenderGuiOverlay(Post event) {
      if (NanosuitHudMasterCompositor.shouldLegacyRenderMinimapHud()) {
         renderFromMaster(event);
      }
   }

   public static void renderFromMaster(Post event) {
      if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
         NanosuitHudMasterCompositor.prepareSharedHudFrame(event);
         NanoMinimapManager.get()
            .render(event.getGuiGraphics(), Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
      }
   }

   @SubscribeEvent
   public static void onLevelUnload(Unload event) {
      if (event.getLevel() instanceof ClientLevel) {
         NanoMinimapManager.get().onWorldUnload();
      }
   }

   @SubscribeEvent
   public static void onClientDisconnect(LoggingOut event) {
      NanoMinimapManager.get().onClientDisconnect();
   }
}
