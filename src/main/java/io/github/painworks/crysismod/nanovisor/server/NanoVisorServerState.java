package io.github.painworks.crysismod.nanovisor.server;

import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.energy.INanosuitEnergy;

import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.server.level.ServerPlayer;

public final class NanoVisorServerState {
   private static final ConcurrentMap<UUID, Boolean> ACTIVE_BY_PLAYER = new ConcurrentHashMap<>();

   private NanoVisorServerState() {
   }

   public static void setActiveFromClient(ServerPlayer player, boolean requestedActive) {
      if (!requestedActive) {
         ACTIVE_BY_PLAYER.remove(player.getUUID());
      } else {
         if (canActivate(player)) {
            ACTIVE_BY_PLAYER.put(player.getUUID(), true);
         } else {
            ACTIVE_BY_PLAYER.remove(player.getUUID());
         }
      }
   }

   public static boolean isActive(ServerPlayer player) {
      if (!ACTIVE_BY_PLAYER.getOrDefault(player.getUUID(), false)) {
         return false;
      } else if (!canActivate(player)) {
         ACTIVE_BY_PLAYER.remove(player.getUUID());
         return false;
      } else {
         return true;
      }
   }

   public static void clear(UUID playerId) {
      ACTIVE_BY_PLAYER.remove(playerId);
   }

   private static boolean canActivate(ServerPlayer player) {
      NanosuitProfile profile = NanosuitArmorUtil.getEquippedProfile(player);
      if (profile == null || !profile.auxiliary().nanoVisorMarking()) {
         return false;
      }
      INanosuitEnergy data = NanosuitCapabilities.get(player);
      return !data.isRecoveryLocked() && data.getEnergy() > 0.0F;
   }
}
