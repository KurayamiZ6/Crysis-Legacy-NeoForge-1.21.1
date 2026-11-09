package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.client.compat.GnetumHudCompat;
import io.github.painworks.crysismod.client.minimap.NanoMinimapHudOverlay;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanovisor.client.NanoVisorHudOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanosuitHudMasterCompositor {
   private static Post lastHudFrameEvent;
   private static long lastHudFrameId;

   private NanosuitHudMasterCompositor() {
   }

   public static boolean isEnabled() {
      return NanosuitClientConfig.hudMasterCompositorEnabled();
   }

   public static boolean shouldLegacyRenderNanosuitHud() {
      return !isEnabled() || !NanosuitClientConfig.hudMasterCompositorRouteNanosuitHud();
   }

   public static boolean shouldLegacyRenderTaczHud() {
      return !isEnabled() || !NanosuitClientConfig.hudMasterCompositorRouteTaczHud();
   }

   public static boolean shouldLegacyRenderNanoVisorHud() {
      return !isEnabled() || !NanosuitClientConfig.hudMasterCompositorRouteNanoVisorHud();
   }

   public static boolean shouldLegacyRenderMinimapHud() {
      return !isEnabled() || !NanosuitClientConfig.hudMasterCompositorRouteMinimapHud();
   }

   public static long captureHudFrame(Post event) {
      if (event == null) {
         return lastHudFrameId;
      } else {
         if (lastHudFrameEvent != event) {
            lastHudFrameEvent = event;
            lastHudFrameId++;
         }

         return lastHudFrameId;
      }
   }

   public static void prepareSharedHudFrame(Post event) {
      if (event != null && event.getName().equals(VanillaGuiLayers.HOTBAR)) {
         GnetumHudCompat.disableCachingForCurrentHudElement();
         NanosuitHudOverlay.beginSharedHudFrame(captureHudFrame(event), event.getPartialTick().getGameTimeDeltaPartialTick(false));
      }
   }

   @SubscribeEvent
   public static void onRenderGuiOverlay(Post event) {
      if (isEnabled() && event.getName().equals(VanillaGuiLayers.HOTBAR)) {
         prepareSharedHudFrame(event);
         if (NanosuitClientConfig.hudMasterCompositorRouteNanosuitHud()) {
            NanosuitHudOverlay.renderFromMaster(event);
         }

         if (NanosuitClientConfig.hudMasterCompositorRouteTaczHud()) {
            NanosuitTaczHudOverlay.renderFromMaster(event);
         }

         if (NanosuitClientConfig.hudMasterCompositorRouteNanoVisorHud()) {
            NanoVisorHudOverlay.renderFromMaster(event);
         }

         if (NanosuitClientConfig.hudMasterCompositorRouteMinimapHud()) {
            NanoMinimapHudOverlay.renderFromMaster(event);
         }
      }
   }
}
