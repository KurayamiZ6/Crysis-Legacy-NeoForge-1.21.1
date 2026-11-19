package io.github.painworks.crysismod.nanosuit.threat;

import io.github.painworks.crysismod.nanosuit.handler.NanosuitCloak;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanosuit.network.SyncThreatLevelPacket;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "crysismod")
public final class NanosuitThreatTracker {
   private static final int SAMPLE_INTERVAL = 10;
   private static final int RESEND_INTERVAL = 40;
   private static final int SEARCH_LINGER = 60;
   private static final double RANGE = 32.0;
   private static final double SUSPICION_RANGE_SQUARED = 8.0 * 8.0;
   private static final double SECTOR_ALERT_RANGE_SQUARED = 12.0 * 12.0;
   private static final Map<ServerPlayer, State> TRACKED = new WeakHashMap<>();

   private NanosuitThreatTracker() {
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent.Post event) {
      if (!(event.getEntity() instanceof ServerPlayer player)) {
         return;
      }
      if (!player.isAlive() || player.isSpectator() || !NanosuitArmorUtil.hasFullSet(player)) {
         if (TRACKED.remove(player) != null) {
            NanosuitNetwork.sendToPlayer(player, new SyncThreatLevelPacket(ThreatLevel.CALM));
         }
         return;
      }
      if (Math.floorMod(player.tickCount + player.getId(), SAMPLE_INTERVAL) != 0) {
         return;
      }

      ServerLevel level = player.serverLevel();
      long now = level.getGameTime();
      State state = TRACKED.computeIfAbsent(player, key -> new State());
      NanosuitProfile profile = NanosuitArmorUtil.getEquippedProfile(player);
      if (state.level != level || state.profile != profile) {
         state.profile = profile;
         state.level = level;
         state.lastSent = null;
         state.searchUntil = now;
      }
      ThreatLevel threat = profile == NanosuitProfile.NANOSUIT_1 ? assessClassic(player, level) : assess(player, level, now);
      if (threat.ordinal() >= ThreatLevel.SEARCHING.ordinal()) {
         state.searchUntil = now + SEARCH_LINGER;
      } else if (now < state.searchUntil) {
         threat = ThreatLevel.SEARCHING;
      }
      if (state.lastSent != threat || now - state.lastSentAt >= RESEND_INTERVAL) {
         NanosuitNetwork.sendToPlayer(player, new SyncThreatLevelPacket(threat));
         state.lastSent = threat;
         state.lastSentAt = now;
      }
   }

   @SubscribeEvent
   public static void onLogout(PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         TRACKED.remove(player);
      }
   }

   private static ThreatLevel assessClassic(ServerPlayer player, ServerLevel level) {
      ThreatLevel threat = ThreatLevel.CALM;
      for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RANGE, 16.0, RANGE))) {
         if (!mob.isAlive() || mob.isAlliedTo(player) || mob.distanceToSqr(player) > RANGE * RANGE) continue;
         var brain = mob.getBrain();
         boolean tracksPlayer = mob.getTarget() == player || brain.isMemoryValue(MemoryModuleType.ATTACK_TARGET, player);
         if (tracksPlayer) {
            if (!NanosuitCloak.hidesFrom(mob, player) && mob.hasLineOfSight(player)) return ThreatLevel.DETECTED;
            threat = ThreatLevel.SEARCHING;
         } else if (brain.isMemoryValue(MemoryModuleType.ANGRY_AT, player.getUUID())
               || mob.getLastHurtByMob() == player && mob.tickCount - mob.getLastHurtByMobTimestamp() <= SEARCH_LINGER) {
            threat = ThreatLevel.SEARCHING;
         }
         // A nearby idle enemy or unattributed ZA investigation is not proof of detecting this player.
      }
      return threat;
   }

   private static ThreatLevel assess(ServerPlayer player, ServerLevel level, long now) {
      ThreatLevel threat = ThreatLevel.CALM;
      var bounds = player.getBoundingBox().inflate(RANGE, 16.0, RANGE);
      for (Mob mob : level.getEntitiesOfClass(Mob.class, bounds)) {
         double distance = mob.distanceToSqr(player);
         if (distance > RANGE * RANGE || !mob.isAlive() || mob.isAlliedTo(player)) {
            continue;
         }

         LivingEntity target = mob.getTarget();
         var brain = mob.getBrain();
         boolean hasAttackMemory = brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET);
         boolean tracksPlayer = target == player || hasAttackMemory && brain.isMemoryValue(MemoryModuleType.ATTACK_TARGET, player);
         boolean rememberedAttacker = mob.getLastHurtByMob() == player
            && mob.tickCount - mob.getLastHurtByMobTimestamp() <= SEARCH_LINGER;
         if (tracksPlayer) {
            if (!NanosuitCloak.hidesFrom(mob, player) && mob.hasLineOfSight(player)) {
               return ThreatLevel.DETECTED;
            }
            threat = ThreatLevel.SEARCHING;
         } else if (rememberedAttacker || brain.isMemoryValue(MemoryModuleType.ANGRY_AT, player.getUUID())) {
            threat = ThreatLevel.SEARCHING;
         } else if (mob instanceof Enemy && target == null && !hasAttackMemory) {
            if (distance <= SECTOR_ALERT_RANGE_SQUARED && ZombieAwarenessThreat.isInvestigating(mob, now)) {
               threat = ThreatLevel.SEARCHING;
            } else if (threat == ThreatLevel.CALM && distance <= SUSPICION_RANGE_SQUARED && !player.isInvisible()
                  && mob.hasLineOfSight(player)) {
               threat = ThreatLevel.SUSPICIOUS;
            }
         }
      }
      return threat;
   }

   private static final class State {
      private NanosuitProfile profile;
      private ServerLevel level;
      private ThreatLevel lastSent;
      private long lastSentAt;
      private long searchUntil;
   }
}
