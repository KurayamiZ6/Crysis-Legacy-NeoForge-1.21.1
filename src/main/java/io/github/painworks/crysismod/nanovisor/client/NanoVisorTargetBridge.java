package io.github.painworks.crysismod.nanovisor.client;

import io.github.painworks.crysismod.nanovisor.config.NanoVisorSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class NanoVisorTargetBridge {
   private static final double MIN_DISTANCE_LIMIT = 1.0;
   private static final double MAX_DISTANCE_LIMIT = 512.0;

   private NanoVisorTargetBridge() {
   }

   public static Entity resolveCurrentEntityTarget(Minecraft minecraft, LocalPlayer player, float partialTick) {
      if (minecraft != null && player != null) {
         double maxEntityDistance = Mth.clamp(NanoVisorSettings.Hud.maxDistanceToTarget(), 1.0, 512.0);
         double maxBlockDistance = Mth.clamp(NanoVisorSettings.Hud.maxDistanceToBlock(), 1.0, 512.0);
         Vec3 eyePos = player.getEyePosition(partialTick);
         Vec3 lookVector = player.getViewVector(partialTick);
         double blockDistance = Double.POSITIVE_INFINITY;
         Vec3 blockEnd = eyePos.add(lookVector.scale(maxBlockDistance));
         BlockHitResult blockHitResult = player.level().clip(new ClipContext(eyePos, blockEnd, Block.OUTLINE, Fluid.NONE, player));
         if (blockHitResult.getType() == Type.BLOCK) {
            blockDistance = eyePos.distanceTo(blockHitResult.getLocation());
         }

         double entityDistance = Double.POSITIVE_INFINITY;
         Entity targetEntity = null;
         Vec3 entityEnd = eyePos.add(lookVector.scale(maxEntityDistance));
         AABB searchBox = player.getBoundingBox().expandTowards(lookVector.scale(maxEntityDistance)).inflate(1.0);
         EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
            player, eyePos, entityEnd, searchBox, entity -> isValidTarget(player, entity), maxEntityDistance * maxEntityDistance
         );
         if (entityHitResult != null && entityHitResult.getType() == Type.ENTITY) {
            targetEntity = entityHitResult.getEntity();
            entityDistance = eyePos.distanceTo(entityHitResult.getLocation());
         }

         return targetEntity != null && entityDistance <= maxEntityDistance && entityDistance <= blockDistance ? targetEntity : null;
      } else {
         return null;
      }
   }

   private static boolean isValidTarget(LocalPlayer player, Entity entity) {
      return entity != null && entity.isAlive() && entity != player && !entity.isSpectator() && entity.isPickable();
   }
}
