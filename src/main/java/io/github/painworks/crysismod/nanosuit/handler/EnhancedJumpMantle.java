package io.github.painworks.crysismod.nanosuit.handler;

import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanosuit.network.SyncMantleMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Raises the player to a reachable ledge, then guides ordinary movement over it. */
final class EnhancedJumpMantle {
   private static final double MAX_RISE = 2.10;
   private static final double SURFACE_CLEARANCE = 0.04;
   private static final double FORWARD_REACH = 0.30;
   private static final double LANDING_INSET = 0.78;
   private static final double MAX_PULL_SPEED = 0.24;
   private static final double CLEARANCE_MARGIN = 0.80;

   private EnhancedJumpMantle() {
   }

   static Plan tryLift(ServerPlayer player, Vec3 direction) {
      AABB body = player.getBoundingBox();
      AABB forward = body.move(direction.x * FORWARD_REACH, 0.0, direction.z * FORWARD_REACH);
      AABB probe = new AABB(forward.minX, body.minY + 0.12, forward.minZ,
            forward.maxX, body.minY + MAX_RISE, forward.maxZ);
      double rise = -1.0;
      for (VoxelShape obstacle : player.level().getBlockCollisions(player, probe)) {
         for (AABB part : obstacle.toAabbs()) {
            double height = part.maxY - body.minY;
            if (height >= 0.12 && height <= MAX_RISE && part.intersects(probe)) {
               rise = Math.max(rise, height);
            }
         }
      }
      if (rise < 0.0) {
         return null;
      }

      double lift = rise + SURFACE_CLEARANCE;
      AABB overhead = body.expandTowards(0.0, lift, 0.0);
      AABB overLedge = body.move(0.0, lift, 0.0);
      double reachX = direction.x * LANDING_INSET;
      double reachZ = direction.z * LANDING_INSET;
      double top = body.minY + rise;
      Vec3 landing = new Vec3(player.getX() + reachX, top, player.getZ() + reachZ);
      if (!player.level().noCollision(player, overhead)
            || !player.level().noCollision(player, overLedge.expandTowards(reachX, 0.0, reachZ))
            || !hasLandingSupport(player, landing)) {
         return null;
      }

      // Allow for the first gravity update after the server packet and for crossing the ledge before descent.
      double velocityY = Math.sqrt(0.16 * (lift + CLEARANCE_MARGIN)) - 0.015;
      Vec3 velocity = player.getDeltaMovement();
      sendVelocity(player, new Vec3(velocity.x, Math.max(velocity.y, velocityY), velocity.z));
      return new Plan(landing, player.tickCount + 16);
   }

   static boolean guideToLanding(ServerPlayer player, Plan plan) {
      if (player.tickCount >= plan.expiresAt() || !hasLandingSupport(player, plan.landing())) {
         return false;
      }
      if (player.getY() < plan.landing().y + SURFACE_CLEARANCE) {
         return !player.onGround();
      }
      Vec3 offset = new Vec3(plan.landing().x - player.getX(), 0.0, plan.landing().z - player.getZ());
      double distance = offset.horizontalDistance();
      if (distance <= 0.12 || distance > LANDING_INSET + 0.5) {
         return false;
      }

      Vec3 step = offset.scale(Math.min(MAX_PULL_SPEED / distance, 1.0));
      AABB sweep = player.getBoundingBox().expandTowards(step.x, 0.0, step.z);
      if (!player.level().noCollision(player, sweep)) {
         return false;
      }
      player.setDeltaMovement(step.x, player.getDeltaMovement().y, step.z);
      player.hasImpulse = true;
      NanosuitNetwork.sendToPlayer(player, new SyncMantleMotionPacket(step.x, step.z));
      return distance > MAX_PULL_SPEED + 0.12;
   }

   private static boolean hasLandingSupport(ServerPlayer player, Vec3 landing) {
      double halfSupportWidth = 0.16;
      AABB support = new AABB(landing.x - halfSupportWidth, landing.y - 0.09, landing.z - halfSupportWidth,
            landing.x + halfSupportWidth, landing.y + 0.001, landing.z + halfSupportWidth);
      return player.level().getBlockCollisions(player, support).iterator().hasNext();
   }

   private static void sendVelocity(ServerPlayer player, Vec3 velocity) {
      player.setDeltaMovement(velocity);
      player.hasImpulse = true;
      player.connection.send(new ClientboundSetEntityMotionPacket(player));
   }

   record Plan(Vec3 landing, int expiresAt) {
   }
}
