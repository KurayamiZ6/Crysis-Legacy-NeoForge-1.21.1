package io.github.painworks.crysismod.nanosuit.integration;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.level.GameType;

@EventBusSubscriber(modid = "crysismod", bus = EventBusSubscriber.Bus.GAME)
public final class BiologicalSuitEvents {
   private BiologicalSuitEvents() {}

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void onClone(PlayerEvent.Clone event) {
      if (event.getEntity() instanceof ServerPlayer player) BiologicalSuitIntegration.transferOnClone(event.getOriginal(), player);
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void onTick(PlayerTickEvent.Post event) {
      if (event.getEntity() instanceof ServerPlayer player) BiologicalSuitIntegration.reconcile(player);
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void onGameModeChange(PlayerEvent.PlayerChangeGameModeEvent event) {
      if (event.getCurrentGameMode() == GameType.CREATIVE && event.getEntity() instanceof ServerPlayer player) {
         BiologicalSuitIntegration.reconcile(player);
      }
   }
}
