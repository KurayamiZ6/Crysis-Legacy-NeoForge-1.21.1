package io.github.painworks.crysismod.client.minimap;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.MOD)
public final class NanoMinimapClientModEvents {
   private NanoMinimapClientModEvents() {
   }

   @SubscribeEvent
   public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
      event.registerReloadListener(new ResourceManagerReloadListener() {
         public void onResourceManagerReload(ResourceManager resourceManager) {
            NanoMinimapManager.get().onResourceReload();
         }
      });
   }
}
