package io.github.painworks.crysismod.nanovisor.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanoVisorClientMarkEvents {
   private NanoVisorClientMarkEvents() {
   }

   @SubscribeEvent
   public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      if (true) {
         NanoVisorClientMarkedTargets.tick(Minecraft.getInstance());
      }
   }

   @SubscribeEvent
   public static void onClientLogout(LoggingOut event) {
      NanoVisorClientMarkedTargets.clear();
   }
}
