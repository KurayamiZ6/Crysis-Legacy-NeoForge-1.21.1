package io.github.painworks.crysismod.nanovision.server;

import io.github.painworks.crysismod.nanosuit.energy.INanosuitEnergy;

import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;

public final class NanovisionServerState {
   private static final ConcurrentMap<UUID, Boolean> ACTIVE_BY_PLAYER = new ConcurrentHashMap<>();

   private NanovisionServerState() {
   }

   public static void setActiveFromClient(ServerPlayer player, boolean requestedActive) {
      if (requestedActive && canUse(player)) {
         ACTIVE_BY_PLAYER.put(player.getUUID(), true);
      } else {
         ACTIVE_BY_PLAYER.remove(player.getUUID());
      }
   }

   public static boolean isActive(ServerPlayer player) {
      if (!ACTIVE_BY_PLAYER.getOrDefault(player.getUUID(), false)) {
         return false;
      } else if (!canUse(player)) {
         ACTIVE_BY_PLAYER.remove(player.getUUID());
         return false;
      } else {
         return true;
      }
   }

   public static void clear(UUID playerId) {
      ACTIVE_BY_PLAYER.remove(playerId);
   }

   private static boolean canUse(ServerPlayer player) {
      if (!NanovisionConfig.nanovisionEnabled()) {
         return false;
      } else if (NanovisionConfig.nanovisionRequiresFullSet() && !NanosuitArmorUtil.hasFullSet(player)) {
         return false;
      } else {
         return !NanovisionConfig.nanovisionRequiresFullSet()
               && NanovisionConfig.nanovisionRequiresHelmet()
               && !NanosuitArmorUtil.isNanosuitHelmet(player.getItemBySlot(EquipmentSlot.HEAD))
            ? false
            : java.util.Optional.<INanosuitEnergy>of(NanosuitCapabilities.get(player)).map(data -> !data.isRecoveryLocked() && data.getEnergy() > 0.0F).orElse(false);
      }
   }
}
