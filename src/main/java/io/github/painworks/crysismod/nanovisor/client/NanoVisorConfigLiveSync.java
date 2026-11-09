package io.github.painworks.crysismod.nanovisor.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ScreenEvent.Render.Post;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanoVisorConfigLiveSync {
   private NanoVisorConfigLiveSync() {
   }

   @SubscribeEvent
   public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      if (true) {
         Minecraft minecraft = Minecraft.getInstance();
         NanoVisorConfigScreen.applyLiveBindingsIfOpen(minecraft.screen);
      }
   }

   @SubscribeEvent
   public static void onScreenRender(Post event) {
      NanoVisorConfigScreen.applyLiveBindingsIfOpen(event.getScreen());
   }
}
