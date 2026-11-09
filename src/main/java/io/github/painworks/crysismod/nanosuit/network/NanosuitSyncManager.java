package io.github.painworks.crysismod.nanosuit.network;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitEnergy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public final class NanosuitSyncManager {
   private static final Map<UUID, NanosuitSyncManager.SyncedState> LAST_SYNCED_STATE = new HashMap<>();

   private NanosuitSyncManager() {
   }

   public static NanosuitSyncManager.SyncedState getLastSyncedState(UUID playerId) {
      return LAST_SYNCED_STATE.get(playerId);
   }

   public static void clear(UUID playerId) {
      LAST_SYNCED_STATE.remove(playerId);
   }

   public static void syncToClient(ServerPlayer player) {
      NanosuitEnergy data = NanosuitCapabilities.get(player);
      NanosuitNetwork.sendToPlayer(player, new SyncNanosuitDataPacket(data.getEnergy(), data.getMode(), data.isSprintBoostReady(), data.isRecoveryLocked()));
      LAST_SYNCED_STATE.put(player.getUUID(), new SyncedState(data.getEnergy(), data.getMode(), data.isSprintBoostReady(), data.isRecoveryLocked()));
   }

   public record SyncedState(float energy, NanosuitMode mode, boolean sprintBoostReady, boolean recoveryLocked) {
   }
}
