package io.github.painworks.crysismod.nanosuit.handler;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class NanosuitCloak {
   private static final double QUIET_RANGE = 1.5;
   private static final double WALKING_RANGE = 4.0;
   private static final double SPRINTING_RANGE = 9.0;
   private static final double COMBAT_RANGE = 12.0;

   public static boolean isActive(ServerPlayer player) {
      var suit = NanosuitCapabilities.get(player);
      return suit.getMode() == NanosuitMode.CLOAK && suit.getEnergy() > 0.0F && NanosuitArmorUtil.hasFullSet(player);
   }

   public static boolean mufflesSteps(Player player) {
      if (!player.isCrouching() || player.isSprinting()) {
         return false;
      }
      if (player instanceof ServerPlayer serverPlayer) {
         return isActive(serverPlayer);
      }
      // The invisible flag is synchronized to observers, including clients tracking a remote player.
      return player.isInvisible() && !player.hasEffect(MobEffects.INVISIBILITY) && NanosuitArmorUtil.hasFullSet(player);
   }

   private static double detectionRange(ServerPlayer player) {
      if (NanosuitPlayerHandler.hasCloakCombatNoise(player)) {
         return COMBAT_RANGE;
      }
      if (player.isSprinting()) {
         return SPRINTING_RANGE;
      }
      if (!player.isCrouching() && NanosuitPlayerHandler.isMovingForCloak(player)) {
         return WALKING_RANGE;
      }
      return QUIET_RANGE;
   }

   public static boolean hidesFrom(LivingEntity observer, LivingEntity candidate) {
      if (!(candidate instanceof ServerPlayer player) || !isActive(player)) {
         return false;
      }
      double range = detectionRange(player);
      return observer.distanceToSqr(player) > range * range;
   }

   @SubscribeEvent
   public void onTargetAcquisition(LivingChangeTargetEvent event) {
      if (event.getEntity() instanceof Mob mob && hidesFrom(mob, event.getNewAboutToBeSetTarget())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onCombatNoise(LivingDamageEvent.Post event) {
      if (event.getNewDamage() <= 0.0F || !(event.getSource().getEntity() instanceof LivingEntity attacker)) {
         return;
      }
      if (event.getEntity() instanceof ServerPlayer victim && attacker != victim && !victim.isAlliedTo(attacker)) {
         NanosuitPlayerHandler.applyCloakCombatCost(victim, 100.0F, true);
      }
      if (attacker instanceof ServerPlayer player && isActive(player)) {
         NanosuitPlayerHandler.recordCloakCombatNoise(player);
      }
   }

   @SubscribeEvent
   public void onMobTick(EntityTickEvent.Pre event) {
      if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide() || mob.tickCount % 5 != 0) {
         return;
      }

      Brain<?> brain = mob.getBrain();
      LivingEntity currentTarget = mob.getTarget();
      if (currentTarget instanceof ServerPlayer player && hidesFrom(mob, player)) {
         forgetTarget(mob, brain, player);
      }

      if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
         LivingEntity remembered = brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
         if (remembered instanceof ServerPlayer player && hidesFrom(mob, player)) {
            forgetTarget(mob, brain, player);
         }
      }

      if (mob.getLastHurtByMob() instanceof ServerPlayer player && hidesFrom(mob, player)) {
         forgetTarget(mob, brain, player);
      }
   }

   private static void forgetTarget(Mob mob, Brain<?> brain, ServerPlayer player) {
      if (mob.getTarget() == player) {
         mob.setTarget(null);
         mob.getNavigation().stop();
      }
      if (mob.getLastHurtByMob() == player) {
         mob.setLastHurtByMob(null);
      }
      if (mob instanceof NeutralMob neutral && player.getUUID().equals(neutral.getPersistentAngerTarget())) {
         neutral.setPersistentAngerTarget(null);
         neutral.setRemainingPersistentAngerTime(0);
      }

      eraseMatchingMemory(brain, MemoryModuleType.ATTACK_TARGET, player);
      eraseMatchingMemory(brain, MemoryModuleType.HURT_BY_ENTITY, player);
      eraseMatchingMemory(brain, MemoryModuleType.ANGRY_AT, player.getUUID());
      if (brain.hasMemoryValue(MemoryModuleType.HURT_BY)
            && brain.getMemory(MemoryModuleType.HURT_BY).orElseThrow().getEntity() == player) {
         brain.eraseMemory(MemoryModuleType.HURT_BY);
      }
      if (brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)
            && tracksPlayer(brain.getMemory(MemoryModuleType.WALK_TARGET).orElseThrow().getTarget(), player)) {
         brain.eraseMemory(MemoryModuleType.WALK_TARGET);
         mob.getNavigation().stop();
      }
      if (brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)
            && tracksPlayer(brain.getMemory(MemoryModuleType.LOOK_TARGET).orElseThrow(), player)) {
         brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
      }
   }

   private static <T> void eraseMatchingMemory(Brain<?> brain, MemoryModuleType<T> type, T value) {
      if (brain.isMemoryValue(type, value)) {
         brain.eraseMemory(type);
      }
   }

   private static boolean tracksPlayer(PositionTracker tracker, ServerPlayer player) {
      return tracker instanceof EntityTracker entityTracker && entityTracker.getEntity() == player;
   }
}
