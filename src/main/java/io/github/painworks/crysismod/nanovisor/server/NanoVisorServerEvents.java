package io.github.painworks.crysismod.nanovisor.server;

import io.github.painworks.crysismod.nanovision.server.NanovisionServerState;
import net.minecraft.server.level.ServerPlayer;


import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", bus = Bus.GAME)
public final class NanoVisorServerEvents {
   private NanoVisorServerEvents() {
   }

   @SubscribeEvent
   public static void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
      if (true) {
         NanoVisorTargetMarkService.tick(event.getServer());
      }
   }

   @SubscribeEvent
   public static void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer) {
         NanoVisorServerState.clear(serverPlayer.getUUID());
         NanovisionServerState.clear(serverPlayer.getUUID());
         NanoVisorTargetMarkService.clearPlayerMarks(serverPlayer.server, serverPlayer.getUUID());
      }
   }

   @SubscribeEvent
   public static void onPlayerRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer) {
         NanoVisorServerState.clear(serverPlayer.getUUID());
         NanovisionServerState.clear(serverPlayer.getUUID());
         NanoVisorTargetMarkService.clearPlayerMarks(serverPlayer.server, serverPlayer.getUUID());
      }
   }

   @SubscribeEvent
   public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer) {
         NanoVisorServerState.clear(serverPlayer.getUUID());
         NanovisionServerState.clear(serverPlayer.getUUID());
         NanoVisorTargetMarkService.clearPlayerMarks(serverPlayer.server, serverPlayer.getUUID());
      }
   }
}
