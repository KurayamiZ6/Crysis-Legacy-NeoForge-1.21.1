package io.github.painworks.crysismod.nanovisor.client;

import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorSettings;
import io.github.painworks.crysismod.nanovisor.network.RequestNanoVisorMarkTargetPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class NanoVisorTargetingHandler {
   private static final double MIN_MARK_RANGE = 1.0;
   private static final double MAX_MARK_RANGE = 256.0;
   private static final int MIN_GLOW_TICKS = 1;
   private static final int MAX_GLOW_TICKS = 12000;
   private static final int MIN_MAX_TARGETS = 1;
   private static final int MAX_MAX_TARGETS = 64;

   private NanoVisorTargetingHandler() {
   }

   public static void requestMarkCurrentTarget(Minecraft minecraft) {
      LocalPlayer player = minecraft.player;
      if (player != null && NanoVisorState.isVisorActive() && NanoVisorSettings.Targeting.markTargetEnabled()) {
         double range = Mth.clamp(NanoVisorSettings.Targeting.markRange(), 1.0, 256.0);
         int glowDurationTicks = Mth.clamp(NanoVisorSettings.Targeting.markGlowDurationTicks(), 1, 12000);
         int maxMarkedTargets = Mth.clamp(NanoVisorSettings.Targeting.maxMarkedTargets(), 1, 64);
         boolean onlyLiving = NanoVisorSettings.Targeting.markOnlyLivingEntities();
         boolean requireLineOfSight = NanoVisorSettings.Targeting.markRequireLineOfSight();
         Entity target = resolveTarget(minecraft, player, range, onlyLiving, requireLineOfSight);
         if (target != null) {
            NanosuitNetwork.sendToServer(
               new RequestNanoVisorMarkTargetPacket(target.getId(), range, glowDurationTicks, onlyLiving, requireLineOfSight, maxMarkedTargets)
            );
         }
      }
   }

   private static Entity resolveTarget(Minecraft minecraft, LocalPlayer player, double range, boolean onlyLiving, boolean requireLineOfSight) {
      if (minecraft.hitResult instanceof EntityHitResult entityHitResult) {
         Entity directTarget = entityHitResult.getEntity();
         if (isValidTarget(player, directTarget, range, onlyLiving, requireLineOfSight)) {
            return directTarget;
         }
      }

      Vec3 eyePos = player.getEyePosition(1.0F);
      Vec3 look = player.getViewVector(1.0F);
      Vec3 reachEnd = eyePos.add(look.scale(range));
      AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
      EntityHitResult fallbackHit = ProjectileUtil.getEntityHitResult(
         player, eyePos, reachEnd, searchBox, entity -> isValidTarget(player, entity, range, onlyLiving, requireLineOfSight), range * range
      );
      return fallbackHit != null ? fallbackHit.getEntity() : null;
   }

   private static boolean isValidTarget(LocalPlayer player, Entity entity, double range, boolean onlyLiving, boolean requireLineOfSight) {
      if (entity == null || !entity.isAlive() || entity == player || entity.isSpectator() || !entity.isPickable()) {
         return false;
      } else if (onlyLiving && !(entity instanceof LivingEntity)) {
         return false;
      } else {
         return player.distanceToSqr(entity) > range * range ? false : !requireLineOfSight || player.hasLineOfSight(entity);
      }
   }
}
