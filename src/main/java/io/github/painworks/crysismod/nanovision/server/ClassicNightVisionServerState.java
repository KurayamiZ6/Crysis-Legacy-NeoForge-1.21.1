package io.github.painworks.crysismod.nanovision.server;

import io.github.painworks.crysismod.nanovision.ClassicNightVision;
import io.github.painworks.crysismod.nanovision.ClassicNightVisionBattery;
import io.github.painworks.crysismod.nanovision.network.SyncClassicNightVisionPacket;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "crysismod")
public final class ClassicNightVisionServerState {
   private static final int SYNC_INTERVAL_TICKS = 10;
   private static final Map<UUID, ClassicNightVisionBattery> BATTERIES = new HashMap<>();

   private ClassicNightVisionServerState() {}

   public static void toggle(ServerPlayer player) {
      if (!ClassicNightVision.canUse(player)) {
         return;
      }
      ClassicNightVisionBattery battery = BATTERIES.computeIfAbsent(player.getUUID(), ignored -> new ClassicNightVisionBattery());
      battery.toggle();
      sync(player, battery);
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent.Post event) {
      if (!(event.getEntity() instanceof ServerPlayer player)) {
         return;
      }
      ClassicNightVisionBattery battery = BATTERIES.get(player.getUUID());
      if (battery == null) {
         return;
      }
      boolean wasActive = battery.active();
      battery.tick(wasActive && ClassicNightVision.canUse(player));

      boolean full = battery.charge() == ClassicNightVisionBattery.FULL_CHARGE;
      if (wasActive != battery.active() || full || player.tickCount % SYNC_INTERVAL_TICKS == 0) {
         sync(player, battery);
      }
      if (full && !battery.active()) {
         BATTERIES.remove(player.getUUID());
      }
   }

   @SubscribeEvent
   public static void onRespawn(PlayerRespawnEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         ClassicNightVisionBattery battery = BATTERIES.get(player.getUUID());
         if (battery != null) {
            battery.deactivate();
            sync(player, battery);
         }
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         BATTERIES.remove(player.getUUID());
      }
   }

   private static void sync(ServerPlayer player, ClassicNightVisionBattery battery) {
      NanosuitNetwork.sendToPlayer(player, new SyncClassicNightVisionPacket(battery.active(), battery.charge()));
   }
}
