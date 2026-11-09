package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.config.ModConfigEvent.Loading;
import net.neoforged.fml.event.config.ModConfigEvent.Reloading;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.MOD)
public final class NanosuitClientConfigSync {
   private NanosuitClientConfigSync() {
   }

   @SubscribeEvent
   public static void onConfigLoading(Loading event) {
      syncHudSettings(event);
   }

   @SubscribeEvent
   public static void onConfigReloading(Reloading event) {
      syncHudSettings(event);
   }

   private static void syncHudSettings(ModConfigEvent event) {
      if (event.getConfig().getSpec() == NanosuitClientConfig.SPEC) {
         NanosuitHudOverlay.loadFromConfig();
      }
   }
}
