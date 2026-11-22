package io.github.painworks.crysismod.nanovisor.server;

import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanovisor.network.SyncNanoVisorMarkedTargetsPacket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class NanoVisorTargetMarkService {
   private static final Map<UUID, LinkedHashMap<UUID, NanoVisorTargetMarkService.MarkEntry>> MARKS_BY_PLAYER = new HashMap<>();

   private NanoVisorTargetMarkService() {
   }

   public static boolean tryApplyMark(ServerPlayer player, Entity target, int durationTicks, int maxMarkedTargets) {
      if (maxMarkedTargets > 0 && durationTicks >= 0) {
         MinecraftServer server = player.server;
         long now = getNow(server);
         LinkedHashMap<UUID, NanoVisorTargetMarkService.MarkEntry> marks = MARKS_BY_PLAYER.computeIfAbsent(player.getUUID(), id -> new LinkedHashMap<>());
         cleanupExpiredEntries(server, player, marks, now);
         UUID targetId = target.getUUID();
         if (!marks.containsKey(targetId) && marks.size() >= maxMarkedTargets) {
            return false;
         }
         long expiresAt = durationTicks == SyncNanoVisorMarkedTargetsPacket.PERSISTENT_SCAN ? Long.MAX_VALUE : now + durationTicks;
         marks.put(targetId, new MarkEntry(expiresAt, target.getId()));
         syncUpsertToClient(player, target, durationTicks);
         return true;
      } else {
         return false;
      }
   }

   static void forgetDestroyedTarget(MinecraftServer server, UUID targetId) {
      MARKS_BY_PLAYER.forEach((owner, marks) -> {
         MarkEntry entry = marks.get(targetId);
         if (entry != null && entry.persistent()) {
            marks.remove(targetId);
            ServerPlayer player = server.getPlayerList().getPlayer(owner);
            if (player != null) syncRemoveToClient(player, targetId, entry.targetEntityId());
         }
      });
   }

   public static void tick(MinecraftServer server) {
      long now = getNow(server);
      Iterator<Entry<UUID, LinkedHashMap<UUID, NanoVisorTargetMarkService.MarkEntry>>> playerIt = MARKS_BY_PLAYER.entrySet().iterator();

      while (playerIt.hasNext()) {
         Entry<UUID, LinkedHashMap<UUID, NanoVisorTargetMarkService.MarkEntry>> playerEntry = playerIt.next();
         UUID playerId = playerEntry.getKey();
         LinkedHashMap<UUID, NanoVisorTargetMarkService.MarkEntry> marks = playerEntry.getValue();
         ServerPlayer player = server.getPlayerList().getPlayer(playerId);
         if (player == null) {
            playerIt.remove();
         } else {
            cleanupExpiredEntries(server, player, marks, now);
            if (marks.isEmpty()) {
               playerIt.remove();
            }
         }
      }
   }

   public static void clearPlayerMarks(MinecraftServer server, UUID playerId) {
      LinkedHashMap<UUID, NanoVisorTargetMarkService.MarkEntry> marks = MARKS_BY_PLAYER.remove(playerId);
      if (marks != null) {
         ServerPlayer player = server.getPlayerList().getPlayer(playerId);
         if (player != null) {
            NanosuitNetwork.sendToPlayer(player, SyncNanoVisorMarkedTargetsPacket.clearAll());
         }
      }
   }

   private static void cleanupExpiredEntries(
      MinecraftServer server, ServerPlayer owner, LinkedHashMap<UUID, NanoVisorTargetMarkService.MarkEntry> marks, long now
   ) {
      Iterator<Entry<UUID, NanoVisorTargetMarkService.MarkEntry>> it = marks.entrySet().iterator();

      while (it.hasNext()) {
         Entry<UUID, NanoVisorTargetMarkService.MarkEntry> markEntry = it.next();
         UUID targetId = markEntry.getKey();
         NanoVisorTargetMarkService.MarkEntry entry = markEntry.getValue();
         Entity target = entry.persistent() ? owner.serverLevel().getEntity(targetId) : findEntityByUuid(server, targetId);
         boolean expired = entry.expiresAt() <= now;
         boolean invalid = target == null ? !entry.persistent() : !target.isAlive();
         if (expired || invalid) {
            syncRemoveToClient(owner, targetId, entry.targetEntityId());
            it.remove();
         } else if (entry.persistent() && target != null && target.getId() != entry.targetEntityId()) {
            markEntry.setValue(new MarkEntry(entry.expiresAt(), target.getId()));
            syncUpsertToClient(owner, target, SyncNanoVisorMarkedTargetsPacket.PERSISTENT_SCAN);
         }
      }
   }

   private static void syncUpsertToClient(ServerPlayer player, Entity target, int glowDurationTicks) {
      NanosuitNetwork.sendToPlayer(player, SyncNanoVisorMarkedTargetsPacket.upsert(target.getUUID(), target.getId(), glowDurationTicks));
   }

   private static void syncRemoveToClient(ServerPlayer player, UUID targetId, int targetEntityId) {
      NanosuitNetwork.sendToPlayer(player, SyncNanoVisorMarkedTargetsPacket.remove(targetId, targetEntityId));
   }

   private static Entity findEntityByUuid(MinecraftServer server, UUID targetId) {
      for (ServerLevel level : server.getAllLevels()) {
         Entity entity = level.getEntity(targetId);
         if (entity != null) {
            return entity;
         }
      }

      return null;
   }

   private static long getNow(MinecraftServer server) {
      return server.overworld().getGameTime();
   }

   private record MarkEntry(long expiresAt, int targetEntityId) {
      boolean persistent() {
         return expiresAt == Long.MAX_VALUE;
      }
   }
}
