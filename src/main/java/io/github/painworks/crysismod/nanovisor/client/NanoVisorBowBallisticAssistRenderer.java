package io.github.painworks.crysismod.nanovisor.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorClientConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanoVisorBowBallisticAssistRenderer {
   private static final double AIR_DRAG = 0.99;
   private static final double ARROW_GRAVITY = 0.05;
   private static final double MAX_TARGET_DISTANCE_SQ = 65536.0;
   private static final double MAX_TRACKED_TARGET_SPEED = 1.6;
   private static final double MAX_TRACKED_TARGET_ACCELERATION = 0.45;
   private static final double LOCK_RETENTION_CONE_DEGREES = 120.0;
   private static final int MOTION_SAMPLE_TTL_TICKS = 80;
   private static final int INTERCEPT_ITERATIONS = 6;
   private static final int MAX_CACHED_POINTS = 128;
   private static final int LOCK_GRACE_TICKS = 6;
   private static final double SOLUTION_SMOOTHING_MIN = 0.4;
   private static final double SOLUTION_SMOOTHING_MAX = 0.86;
   private static final float COARSE_PITCH_STEP_DEG = 1.5F;
   private static final float FINE_PITCH_STEP_DEG = 0.2F;
   private static final float FINE_PITCH_WINDOW_DEG = 2.5F;
   private static final float YAW_REFINE_WINDOW_DEG = 7.0F;
   private static final float YAW_REFINE_STEP_DEG = 0.7F;
   private static final float PITCH_REFINE_WINDOW_DEG = 2.8F;
   private static final float PITCH_REFINE_STEP_DEG = 0.35F;
   private static final NanoVisorBowBallisticAssistRenderer.FireControlSolution EMPTY = NanoVisorBowBallisticAssistRenderer.FireControlSolution.empty();
   private static final Map<Integer, NanoVisorBowBallisticAssistRenderer.MotionSample> MOTION_SAMPLES = new HashMap<>();
   private static NanoVisorBowBallisticAssistRenderer.FireControlSolution cachedSolution = EMPTY;
   private static NanoVisorBowBallisticAssistRenderer.FireControlSolution previousRenderSolution = EMPTY;
   private static NanoVisorBowBallisticAssistRenderer.FireControlSolution renderSolution = EMPTY;
   private static int lockedTargetId = Integer.MIN_VALUE;
   private static int lastSolvedUseTicks = Integer.MIN_VALUE;
   private static int lastSolvedTargetId = Integer.MIN_VALUE;
   private static long lastSolveTick = Long.MIN_VALUE;
   private static long lastControlTick = Long.MIN_VALUE;
   private static long lastMotionPruneTick = Long.MIN_VALUE;
   private static long lockGraceUntilTick = Long.MIN_VALUE;

   private NanoVisorBowBallisticAssistRenderer() {
   }

   @SubscribeEvent
   public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      if (true) {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer player = minecraft.player;
         if (!canUseFireControl(minecraft, player)) {
            resetFireControl();
         } else {
            updateFireControl(player, 1.0F);
            if (cachedSolution != EMPTY && cachedSolution.valid() && NanoVisorClientConfig.bowFireControlCameraAssistEnabled()) {
               applyCameraAssist(player, cachedSolution);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onRenderLevelStage(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer player = minecraft.player;
         if (!canUseFireControl(minecraft, player)) {
            resetFireControl();
         } else {
            renderFireControl(event, interpolateRenderSolution(event.getPartialTick().getGameTimeDeltaPartialTick(false)));
         }
      }
   }

   private static void updateFireControl(LocalPlayer player, float partialTick) {
      int useTicks = player.getTicksUsingItem();
      float power = BowItem.getPowerForTime(useTicks);
      if (power < 0.08F) {
         cachedSolution = EMPTY;
         previousRenderSolution = EMPTY;
         renderSolution = EMPTY;
      } else {
         long gameTick = player.level().getGameTime();
         if (gameTick != lastControlTick) {
            lastControlTick = gameTick;
            pruneMotionSamples(gameTick);
            NanoVisorBowBallisticAssistRenderer.TargetSnapshot target = selectFireControlTarget(player, partialTick, gameTick);
            if (target == null) {
               if (cachedSolution == EMPTY || !cachedSolution.valid() || gameTick > lockGraceUntilTick) {
                  cachedSolution = EMPTY;
                  previousRenderSolution = EMPTY;
                  renderSolution = EMPTY;
                  lockedTargetId = Integer.MIN_VALUE;
               }
            } else {
               lockGraceUntilTick = gameTick + 6L;
               int solveInterval = target.isMovingFast() ? 1 : Math.max(1, NanoVisorClientConfig.bowBallisticAssistSolveIntervalTicks());
               if (shouldResolve(gameTick, useTicks, target.entity().getId(), solveInterval)) {
                  NanoVisorBowBallisticAssistRenderer.FireControlSolution rawSolution = solve(player, target, partialTick, power, gameTick);
                  cachedSolution = smoothSolution(cachedSolution, rawSolution);
                  previousRenderSolution = renderSolution == EMPTY ? cachedSolution : renderSolution;
                  renderSolution = cachedSolution;
                  lastSolveTick = gameTick;
                  lastSolvedUseTicks = useTicks;
                  lastSolvedTargetId = target.entity().getId();
               }
            }
         }
      }
   }

   private static void resetFireControl() {
      cachedSolution = EMPTY;
      previousRenderSolution = EMPTY;
      renderSolution = EMPTY;
      lockedTargetId = Integer.MIN_VALUE;
      lastSolvedUseTicks = Integer.MIN_VALUE;
      lastSolvedTargetId = Integer.MIN_VALUE;
      lastSolveTick = Long.MIN_VALUE;
      lastControlTick = Long.MIN_VALUE;
      lockGraceUntilTick = Long.MIN_VALUE;
   }

   private static boolean canUseFireControl(Minecraft minecraft, LocalPlayer player) {
      return minecraft != null
         && player != null
         && player.level() != null
         && NanoVisorClientConfig.bowBallisticAssistEnabled()
         && player.isUsingItem()
         && player.getUseItem().is(Items.BOW);
   }

   private static boolean shouldResolve(long gameTick, int useTicks, int targetId, int intervalTicks) {
      return cachedSolution == EMPTY
         || targetId != lastSolvedTargetId
         || Math.abs(useTicks - lastSolvedUseTicks) >= 2
         || lastSolveTick == Long.MIN_VALUE
         || gameTick - lastSolveTick >= intervalTicks;
   }

   private static NanoVisorBowBallisticAssistRenderer.TargetSnapshot selectFireControlTarget(LocalPlayer player, float partialTick, long gameTick) {
      NanoVisorBowBallisticAssistRenderer.TargetSnapshot retained = findLockedTarget(player, partialTick, gameTick);
      if (retained != null) {
         return retained;
      } else {
         Vec3 eye = player.getEyePosition(partialTick);
         Vec3 look = player.getViewVector(partialTick).normalize();
         double minDot = Math.cos(Math.toRadians(Mth.clamp(NanoVisorClientConfig.bowBallisticAssistAimConeDegrees(), 1.0, 90.0)));
         NanoVisorBowBallisticAssistRenderer.TargetSelection selection = new NanoVisorBowBallisticAssistRenderer.TargetSelection();
         NanoVisorClientMarkedTargets.forEachActiveMarkedEntity((ClientLevel)player.level(), entity -> {
            if (entity != player && entity.isAlive()) {
               NanoVisorBowBallisticAssistRenderer.TargetSnapshot snapshot = snapshotTarget(entity, partialTick, gameTick, false);
               Vec3 toTarget = snapshot.aimCenter().subtract(eye);
               double distanceSq = toTarget.lengthSqr();
               if (!(distanceSq <= 1.0E-4) && !(distanceSq > 65536.0)) {
                  double dot = look.dot(toTarget.normalize());
                  if (!(dot < minDot)) {
                     double speedBonus = Math.min(0.08, snapshot.velocity().length() * 0.04);
                     double score = dot + speedBonus - distanceSq / 65536.0 * 0.15;
                     if (selection.snapshot == null || score > selection.score) {
                        selection.snapshot = snapshot;
                        selection.score = score;
                     }
                  }
               }
            }
         });
         if (selection.snapshot == null) {
            return null;
         } else {
            lockedTargetId = selection.snapshot.entity().getId();
            return selection.snapshot;
         }
      }
   }

   private static NanoVisorBowBallisticAssistRenderer.TargetSnapshot findLockedTarget(LocalPlayer player, float partialTick, long gameTick) {
      if (lockedTargetId == Integer.MIN_VALUE) {
         return null;
      } else {
         Vec3 eye = player.getEyePosition(partialTick);
         Vec3 look = player.getViewVector(partialTick).normalize();
         NanoVisorBowBallisticAssistRenderer.TargetSelection locked = new NanoVisorBowBallisticAssistRenderer.TargetSelection();
         NanoVisorClientMarkedTargets.forEachActiveMarkedEntity((ClientLevel)player.level(), entity -> {
            if (entity.getId() == lockedTargetId && entity != player && entity.isAlive()) {
               NanoVisorBowBallisticAssistRenderer.TargetSnapshot snapshot = snapshotTarget(entity, partialTick, gameTick, true);
               Vec3 toTarget = snapshot.aimCenter().subtract(eye);
               double distanceSq = toTarget.lengthSqr();
               if (!(distanceSq <= 1.0E-4) && !(distanceSq > 65536.0)) {
                  double dot = look.dot(toTarget.normalize());
                  double retentionDot = Math.cos(Math.toRadians(120.0));
                  if (!(dot < retentionDot)) {
                     locked.snapshot = snapshot;
                     locked.score = dot;
                  }
               }
            }
         });
         if (locked.snapshot == null) {
            lockedTargetId = Integer.MIN_VALUE;
         }

         return locked.snapshot;
      }
   }

   private static NanoVisorBowBallisticAssistRenderer.TargetSnapshot snapshotTarget(Entity entity, float partialTick, long gameTick, boolean retainedLock) {
      AABB rawBox = entity.getBoundingBox();
      Vec3 rawCenter = rawBox.getCenter();
      Vec3 renderCenter = interpolatedEntityCenter(entity, rawCenter, partialTick);
      AABB box = rawBox.move(renderCenter.subtract(rawCenter));
      Vec3 center = box.getCenter();
      Vec3 aimCenter = new Vec3(center.x, box.minY + box.getYsize() * 0.58, center.z);
      NanoVisorBowBallisticAssistRenderer.MotionState motion = sampleTargetMotion(entity, center, gameTick);
      Vec3 velocity = motion.velocity();
      Vec3 acceleration = motion.acceleration();
      if (!NanoVisorClientConfig.bowFireControlLeadPrediction()) {
         velocity = Vec3.ZERO;
         acceleration = Vec3.ZERO;
      }

      double horizontalRadius = Math.max(box.getXsize(), box.getZsize()) * 0.5;
      double verticalRadius = Math.max(0.15, box.getYsize() * 0.5);
      double boundingRadius = Math.sqrt(horizontalRadius * horizontalRadius + verticalRadius * verticalRadius);
      return new NanoVisorBowBallisticAssistRenderer.TargetSnapshot(
         entity, box, center, aimCenter, velocity, acceleration, retainedLock, horizontalRadius, verticalRadius, boundingRadius
      );
   }

   private static Vec3 interpolatedEntityCenter(Entity entity, Vec3 fallbackCenter, float partialTick) {
      double x = Mth.lerp(partialTick, entity.xo, entity.getX());
      double y = Mth.lerp(partialTick, entity.yo, entity.getY());
      double z = Mth.lerp(partialTick, entity.zo, entity.getZ());
      Vec3 currentPosition = new Vec3(entity.getX(), entity.getY(), entity.getZ());
      return fallbackCenter.add(new Vec3(x, y, z).subtract(currentPosition));
   }

   private static NanoVisorBowBallisticAssistRenderer.MotionState sampleTargetMotion(Entity entity, Vec3 center, long gameTick) {
      NanoVisorBowBallisticAssistRenderer.MotionSample previous = MOTION_SAMPLES.get(entity.getId());
      Vec3 fallback = clampVelocity(entity.getDeltaMovement());
      if (previous != null && gameTick == previous.tick()) {
         return new NanoVisorBowBallisticAssistRenderer.MotionState(previous.velocity(), previous.acceleration());
      } else if (previous != null && gameTick >= previous.tick()) {
         long dt = Math.min(5L, Math.max(1L, gameTick - previous.tick()));
         Vec3 measured = center.subtract(previous.center()).scale(1.0 / dt);
         if (measured.lengthSqr() <= 1.0E-6 && fallback.lengthSqr() > 1.0E-6) {
            measured = fallback;
         }

         measured = clampVelocity(measured);
         Vec3 smoothed = previous.velocity().scale(0.62).add(measured.scale(0.38));
         if (measured.lengthSqr() > previous.velocity().lengthSqr() * 2.25) {
            smoothed = previous.velocity().scale(0.35).add(measured.scale(0.65));
         }

         smoothed = clampVelocity(smoothed);
         Vec3 measuredAcceleration = smoothed.subtract(previous.velocity()).scale(1.0 / dt);
         Vec3 smoothedAcceleration = clampAcceleration(previous.acceleration().scale(0.55).add(measuredAcceleration.scale(0.45)));
         MOTION_SAMPLES.put(entity.getId(), new NanoVisorBowBallisticAssistRenderer.MotionSample(center, smoothed, smoothedAcceleration, gameTick));
         return new NanoVisorBowBallisticAssistRenderer.MotionState(smoothed, smoothedAcceleration);
      } else {
         MOTION_SAMPLES.put(entity.getId(), new NanoVisorBowBallisticAssistRenderer.MotionSample(center, fallback, Vec3.ZERO, gameTick));
         return new NanoVisorBowBallisticAssistRenderer.MotionState(fallback, Vec3.ZERO);
      }
   }

   private static Vec3 clampVelocity(Vec3 velocity) {
      return velocity.lengthSqr() > 2.5600000000000005 ? velocity.normalize().scale(1.6) : velocity;
   }

   private static Vec3 clampAcceleration(Vec3 acceleration) {
      return acceleration.lengthSqr() > 0.2025 ? acceleration.normalize().scale(0.45) : acceleration;
   }

   private static void pruneMotionSamples(long gameTick) {
      if (lastMotionPruneTick == Long.MIN_VALUE || gameTick - lastMotionPruneTick >= 40L) {
         lastMotionPruneTick = gameTick;
         Iterator<Entry<Integer, NanoVisorBowBallisticAssistRenderer.MotionSample>> iterator = MOTION_SAMPLES.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<Integer, NanoVisorBowBallisticAssistRenderer.MotionSample> entry = iterator.next();
            if (gameTick - entry.getValue().tick() > 80L) {
               iterator.remove();
            }
         }
      }
   }

   private static NanoVisorBowBallisticAssistRenderer.FireControlSolution solve(
      LocalPlayer player, NanoVisorBowBallisticAssistRenderer.TargetSnapshot target, float partialTick, float power, long gameTick
   ) {
      int maxTicks = Mth.clamp(NanoVisorClientConfig.bowBallisticAssistMaxFlightTicks(), 20, 200);
      Vec3 eye = player.getEyePosition(partialTick);
      Vec3 projectileStart = projectileStart(player, partialTick);
      float currentPitch = player.getXRot();
      float currentYaw = player.getYRot();
      NanoVisorBowBallisticAssistRenderer.AimSeed seed = buildInterceptSeed(projectileStart, target, power, maxTicks);
      NanoVisorBowBallisticAssistRenderer.TrajectoryResult current = simulate(
         player, target.box(), target.velocity(), target.acceleration(), projectileStart, currentYaw, currentPitch, power, maxTicks, false
      );
      NanoVisorBowBallisticAssistRenderer.AimSearch pitchOnly = searchPitchForYaw(
         player, target.box(), target.velocity(), target.acceleration(), projectileStart, seed.yaw(), power, maxTicks, -85.0F, 85.0F, 1.5F
      );
      NanoVisorBowBallisticAssistRenderer.AimSearch finePitch = pitchOnly.valid()
         ? searchPitchForYaw(
            player,
            target.box(),
            target.velocity(),
            target.acceleration(),
            projectileStart,
            seed.yaw(),
            power,
            maxTicks,
            Math.max(-85.0F, pitchOnly.pitchDeg() - 2.5F),
            Math.min(85.0F, pitchOnly.pitchDeg() + 2.5F),
            0.2F
         )
         : pitchOnly;
      NanoVisorBowBallisticAssistRenderer.AimSearch refined = finePitch.valid()
         ? refineYawPitch(
            player, target.box(), target.velocity(), target.acceleration(), projectileStart, finePitch.yawDeg(), finePitch.pitchDeg(), power, maxTicks
         )
         : refineYawPitch(player, target.box(), target.velocity(), target.acceleration(), projectileStart, seed.yaw(), seed.pitch(), power, maxTicks);
      float solutionYaw = refined.valid() ? refined.yawDeg() : seed.yaw();
      float solutionPitch = refined.valid() ? refined.pitchDeg() : seed.pitch();
      NanoVisorBowBallisticAssistRenderer.TrajectoryResult solved = simulate(
         player, target.box(), target.velocity(), target.acceleration(), projectileStart, solutionYaw, solutionPitch, power, maxTicks, false
      );
      Vec3 aimPoint = computeAimPoint(projectileStart, target, solutionYaw, solutionPitch, solved.flightTicks(), seed.predictedCenter());
      return new NanoVisorBowBallisticAssistRenderer.FireControlSolution(
         target.entity().getId(),
         target.aimCenter(),
         seed.predictedCenter(),
         aimPoint,
         List.of(),
         current.impactPoint(),
         current.hitTarget(),
         solutionYaw,
         solutionPitch,
         currentYaw,
         currentPitch,
         gameTick,
         target.retainedLock(),
         target.velocity().length(),
         target.acceleration().length(),
         target.horizontalRadius(),
         target.verticalRadius(),
         computeMarkerHalfSize(target),
         computeYawToleranceDegrees(projectileStart, target, seed.predictedCenter()),
         computePitchToleranceDegrees(projectileStart, target, seed.predictedCenter()),
         refined.valid() || solved.hitTarget()
      );
   }

   private static Vec3 projectileStart(LocalPlayer player, float partialTick) {
      return player.getEyePosition(partialTick).add(0.0, -0.1, 0.0);
   }

   private static NanoVisorBowBallisticAssistRenderer.AimSeed buildInterceptSeed(
      Vec3 eye, NanoVisorBowBallisticAssistRenderer.TargetSnapshot target, float power, int maxTicks
   ) {
      Vec3 predicted = target.aimCenter();
      double arrowSpeed = Math.max(0.1, power * 3.0);
      double estimatedTicks = eye.distanceTo(predicted) / arrowSpeed;

      for (int i = 0; i < 6; i++) {
         predicted = predictTargetCenter(target, Mth.clamp(estimatedTicks, 0.0, maxTicks));
         double gravityCompensation = 0.025 * estimatedTicks * estimatedTicks * 0.18;
         Vec3 compensated = predicted.add(0.0, gravityCompensation, 0.0);
         estimatedTicks = eye.distanceTo(compensated) / arrowSpeed;
      }

      predicted = predictTargetCenter(target, Mth.clamp(estimatedTicks, 0.0, maxTicks));
      float yaw = yawTo(eye, predicted);
      float pitch = pitchTo(eye, predicted);
      return new NanoVisorBowBallisticAssistRenderer.AimSeed(predicted, yaw, pitch, estimatedTicks);
   }

   private static Vec3 predictTargetCenter(NanoVisorBowBallisticAssistRenderer.TargetSnapshot target, double ticks) {
      double clampedTicks = Mth.clamp(ticks, 0.0, 200.0);
      return target.aimCenter().add(target.velocity().scale(clampedTicks)).add(target.acceleration().scale(0.5 * clampedTicks * clampedTicks));
   }

   private static NanoVisorBowBallisticAssistRenderer.AimSearch searchPitchForYaw(
      LocalPlayer player,
      AABB targetBox,
      Vec3 targetVelocity,
      Vec3 targetAcceleration,
      Vec3 eye,
      float yaw,
      float power,
      int maxTicks,
      float minPitch,
      float maxPitch,
      float step
   ) {
      NanoVisorBowBallisticAssistRenderer.AimSearch best = NanoVisorBowBallisticAssistRenderer.AimSearch.invalid(yaw, Float.NaN);
      float pitch = minPitch;

      while (pitch <= maxPitch + 0.001F) {
         NanoVisorBowBallisticAssistRenderer.TrajectoryResult result = simulate(
            player, targetBox, targetVelocity, targetAcceleration, eye, yaw, pitch, power, maxTicks, false
         );
         double miss = result.hitTarget() ? 0.0 : result.closestDistanceSq();
         if (!best.valid() || miss < best.missDistanceSq()) {
            best = new NanoVisorBowBallisticAssistRenderer.AimSearch(yaw, pitch, miss, result.hitTarget(), result.hitTarget() || miss < 0.45);
         }

         if (result.hitTarget()) {
            break;
         }

         pitch += step;
      }

      return best;
   }

   private static NanoVisorBowBallisticAssistRenderer.AimSearch refineYawPitch(
      LocalPlayer player, AABB targetBox, Vec3 targetVelocity, Vec3 targetAcceleration, Vec3 eye, float baseYaw, float basePitch, float power, int maxTicks
   ) {
      NanoVisorBowBallisticAssistRenderer.AimSearch best = NanoVisorBowBallisticAssistRenderer.AimSearch.invalid(baseYaw, basePitch);
      float yawWindow = (float)Math.max(7.0, Math.min(18.0, targetVelocity.length() * 12.0 + targetAcceleration.length() * 18.0));

      for (float yawOffset = -yawWindow; yawOffset <= yawWindow + 0.001F; yawOffset += 0.7F) {
         float yaw = baseYaw + yawOffset;

         for (float pitchOffset = -2.8F; pitchOffset <= 2.8009999F; pitchOffset += 0.35F) {
            float pitch = Mth.clamp(basePitch + pitchOffset, -85.0F, 85.0F);
            NanoVisorBowBallisticAssistRenderer.TrajectoryResult result = simulate(
               player, targetBox, targetVelocity, targetAcceleration, eye, yaw, pitch, power, maxTicks, false
            );
            double miss = result.hitTarget() ? 0.0 : result.closestDistanceSq();
            if (!best.valid() || miss < best.missDistanceSq()) {
               best = new NanoVisorBowBallisticAssistRenderer.AimSearch(yaw, pitch, miss, result.hitTarget(), result.hitTarget() || miss < 0.45);
            }

            if (result.hitTarget()) {
               return best;
            }
         }
      }

      return best;
   }

   private static NanoVisorBowBallisticAssistRenderer.TrajectoryResult simulate(
      LocalPlayer player,
      AABB targetBox,
      Vec3 targetVelocity,
      Vec3 targetAcceleration,
      Vec3 start,
      float yaw,
      float pitch,
      float power,
      int maxTicks,
      boolean keepPoints
   ) {
      Vec3 pos = start;
      Vec3 velocity = Vec3.directionFromRotation(pitch, yaw).normalize().scale(power * 3.0);
      ArrayList<Vec3> points = keepPoints ? new ArrayList<>(Math.min(maxTicks + 1, 128)) : null;
      if (keepPoints) {
         points.add(start);
      }

      double closestDistanceSq = distanceToAabbSqr(start, targetBox);
      Vec3 impactPoint = null;

      for (int tick = 0; tick < maxTicks; tick++) {
         Vec3 next = pos.add(velocity);
         BlockHitResult blockHit = player.level().clip(new ClipContext(pos, next, Block.COLLIDER, Fluid.NONE, player));
         Vec3 segmentEnd = blockHit.getType() == Type.BLOCK ? blockHit.getLocation() : next;
         AABB movedTargetBox = targetBox.move(targetDisplacement(targetVelocity, targetAcceleration, tick + 1.0)).inflate(0.28);
         if (movedTargetBox.clip(pos, segmentEnd).isPresent()) {
            if (keepPoints && points.size() < 128) {
               points.add(segmentEnd);
            }

            return new NanoVisorBowBallisticAssistRenderer.TrajectoryResult(copyPoints(points), segmentEnd, true, 0.0, tick + 1);
         }

         closestDistanceSq = Math.min(closestDistanceSq, distanceToAabbSqr(segmentEnd, movedTargetBox));
         if (keepPoints && points.size() < 128) {
            points.add(segmentEnd);
         }

         if (blockHit.getType() == Type.BLOCK) {
            impactPoint = blockHit.getLocation();
         } else {
            pos = next;
            velocity = velocity.scale(0.99).add(0.0, -0.05, 0.0);
            if (!(next.y < player.level().getMinBuildHeight() - 16.0)) {
               continue;
            }
         }
         break;
      }

      return new NanoVisorBowBallisticAssistRenderer.TrajectoryResult(copyPoints(points), impactPoint, false, closestDistanceSq, maxTicks);
   }

   private static Vec3 targetDisplacement(Vec3 velocity, Vec3 acceleration, double ticks) {
      return velocity.scale(ticks).add(acceleration.scale(0.5 * ticks * ticks));
   }

   private static Vec3 computeAimPoint(
      Vec3 eye, NanoVisorBowBallisticAssistRenderer.TargetSnapshot target, float yaw, float pitch, int flightTicks, Vec3 fallbackPredictedCenter
   ) {
      Vec3 predicted = predictTargetCenter(target, Mth.clamp(flightTicks, 0, 200));
      if (predicted.distanceToSqr(target.aimCenter()) < 1.0E-4) {
         predicted = fallbackPredictedCenter;
      }

      double distance = Math.max(4.0, eye.distanceTo(predicted));
      return eye.add(Vec3.directionFromRotation(pitch, yaw).normalize().scale(distance));
   }

   private static NanoVisorBowBallisticAssistRenderer.FireControlSolution smoothSolution(
      NanoVisorBowBallisticAssistRenderer.FireControlSolution previous, NanoVisorBowBallisticAssistRenderer.FireControlSolution current
   ) {
      if (previous != EMPTY && previous.targetId() == current.targetId() && previous.valid()) {
         double alpha = Mth.clamp(0.4 + current.targetSpeed() * 0.18 + current.targetAcceleration() * 0.28, 0.4, 0.86);
         float yaw = previous.solutionYaw() + wrapDegrees(current.solutionYaw() - previous.solutionYaw()) * (float)alpha;
         float pitch = (float)Mth.lerp(alpha, previous.solutionPitch(), current.solutionPitch());
         return new NanoVisorBowBallisticAssistRenderer.FireControlSolution(
            current.targetId(),
            lerp(previous.targetCenter(), current.targetCenter(), alpha),
            lerp(previous.predictedCenter(), current.predictedCenter(), alpha),
            lerp(previous.aimPoint(), current.aimPoint(), alpha),
            List.of(),
            current.impactPoint(),
            current.currentAimHits(),
            yaw,
            pitch,
            current.sourceYaw(),
            current.sourcePitch(),
            current.solvedAtTick(),
            current.retainedLock(),
            current.targetSpeed(),
            current.targetAcceleration(),
            current.targetHorizontalRadius(),
            current.targetVerticalRadius(),
            current.markerHalfSize(),
            current.yawToleranceDegrees(),
            current.pitchToleranceDegrees(),
            current.valid()
         );
      } else {
         return current;
      }
   }

   private static NanoVisorBowBallisticAssistRenderer.FireControlSolution interpolateRenderSolution(float partialTick) {
      if (renderSolution != EMPTY && renderSolution.valid()) {
         if (previousRenderSolution != EMPTY && previousRenderSolution.valid() && previousRenderSolution.targetId() == renderSolution.targetId()) {
            double alpha = Mth.clamp(partialTick, 0.0F, 1.0F);
            float yaw = previousRenderSolution.solutionYaw() + wrapDegrees(renderSolution.solutionYaw() - previousRenderSolution.solutionYaw()) * (float)alpha;
            float pitch = (float)Mth.lerp(alpha, previousRenderSolution.solutionPitch(), renderSolution.solutionPitch());
            return new NanoVisorBowBallisticAssistRenderer.FireControlSolution(
               renderSolution.targetId(),
               lerp(previousRenderSolution.targetCenter(), renderSolution.targetCenter(), alpha),
               lerp(previousRenderSolution.predictedCenter(), renderSolution.predictedCenter(), alpha),
               lerp(previousRenderSolution.aimPoint(), renderSolution.aimPoint(), alpha),
               List.of(),
               renderSolution.impactPoint(),
               renderSolution.currentAimHits(),
               yaw,
               pitch,
               renderSolution.sourceYaw(),
               renderSolution.sourcePitch(),
               renderSolution.solvedAtTick(),
               renderSolution.retainedLock(),
               renderSolution.targetSpeed(),
               renderSolution.targetAcceleration(),
               renderSolution.targetHorizontalRadius(),
               renderSolution.targetVerticalRadius(),
               renderSolution.markerHalfSize(),
               renderSolution.yawToleranceDegrees(),
               renderSolution.pitchToleranceDegrees(),
               renderSolution.valid()
            );
         } else {
            return renderSolution;
         }
      } else {
         return EMPTY;
      }
   }

   private static Vec3 lerp(Vec3 from, Vec3 to, double alpha) {
      return new Vec3(Mth.lerp(alpha, from.x, to.x), Mth.lerp(alpha, from.y, to.y), Mth.lerp(alpha, from.z, to.z));
   }

   private static double computeMarkerHalfSize(NanoVisorBowBallisticAssistRenderer.TargetSnapshot target) {
      double configured = Mth.clamp(NanoVisorClientConfig.bowFireControlMarkerRadius(), 0.05, 2.0);
      return Mth.clamp(configured * Math.max(0.45, target.boundingRadius() * 0.72), 0.12, 2.25);
   }

   private static double computeYawToleranceDegrees(Vec3 eye, NanoVisorBowBallisticAssistRenderer.TargetSnapshot target, Vec3 predictedCenter) {
      double distance = Math.max(1.0, eye.distanceTo(predictedCenter));
      return Mth.clamp(Math.toDegrees(Math.atan2(target.horizontalRadius() * 0.72, distance)) + 0.08, 0.08, 4.5);
   }

   private static double computePitchToleranceDegrees(Vec3 eye, NanoVisorBowBallisticAssistRenderer.TargetSnapshot target, Vec3 predictedCenter) {
      double distance = Math.max(1.0, eye.distanceTo(predictedCenter));
      return Mth.clamp(Math.toDegrees(Math.atan2(target.verticalRadius() * 0.58, distance)) + 0.08, 0.08, 4.5);
   }

   private static void applyCameraAssist(LocalPlayer player, NanoVisorBowBallisticAssistRenderer.FireControlSolution solution) {
      double strength = Mth.clamp(NanoVisorClientConfig.bowFireControlCameraAssistStrength(), 0.0, 1.0);
      if (!(strength <= 1.0E-4)) {
         float yawDelta = applyAngularDeadzone(wrapDegrees(solution.solutionYaw() - player.getYRot()), solution.yawToleranceDegrees());
         float pitchDelta = applyAngularDeadzone(Mth.clamp(solution.solutionPitch() - player.getXRot(), -90.0F, 90.0F), solution.pitchToleranceDegrees());
         double error = Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
         if (!(error <= 1.0E-4)) {
            double activationCone = Mth.clamp(NanoVisorClientConfig.bowFireControlActivationConeDegrees(), 0.25, 20.0);
            double trackingCone = solution.retainedLock()
               ? Math.max(activationCone, Math.min(120.0, NanoVisorClientConfig.bowBallisticAssistAimConeDegrees() * 2.0))
               : activationCone;
            if (!(error > trackingCone)) {
               float configuredMaxTurn = (float)Mth.clamp(NanoVisorClientConfig.bowFireControlMaxTurnRateDegrees(), 0.05, 10.0);
               float dynamicMaxTurn = (float)Mth.clamp(
                  configuredMaxTurn + solution.targetSpeed() * 4.5 + solution.targetAcceleration() * 5.5 + Math.min(4.0, error * 0.1), configuredMaxTurn, 18.0
               );
               double sensitivityFactor = currentMouseSensitivityFactor();
               float distanceFactor = error > activationCone ? 0.72F : 1.0F;
               float yawStep = Mth.clamp(yawDelta, -dynamicMaxTurn, dynamicMaxTurn) * (float)strength * distanceFactor * (float)sensitivityFactor;
               float pitchStep = Mth.clamp(pitchDelta, -dynamicMaxTurn, dynamicMaxTurn) * (float)strength * distanceFactor * (float)sensitivityFactor;
               if (!(Math.abs(yawStep) < 5.0E-4F) || !(Math.abs(pitchStep) < 5.0E-4F)) {
                  player.setYRot(player.getYRot() + yawStep);
                  player.setXRot(Mth.clamp(player.getXRot() + pitchStep, -90.0F, 90.0F));
               }
            }
         }
      }
   }

   private static float applyAngularDeadzone(float delta, double toleranceDegrees) {
      double tolerance = Mth.clamp(toleranceDegrees, 0.0, 6.0);
      double abs = Math.abs(delta);
      return abs <= tolerance ? 0.0F : (float)Math.copySign(abs - tolerance * 0.65, (double)delta);
   }

   private static double currentMouseSensitivityFactor() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft != null && minecraft.options != null) {
         double sensitivity = Mth.clamp((Double)minecraft.options.sensitivity().get(), 0.0, 1.0);
         return Mth.clamp(0.72 + sensitivity * 0.56, 0.72, 1.28);
      } else {
         return 1.0;
      }
   }

   private static void renderFireControl(RenderLevelStageEvent event, NanoVisorBowBallisticAssistRenderer.FireControlSolution solution) {
      if (solution.valid()) {
         Minecraft minecraft = Minecraft.getInstance();
         Camera camera = minecraft.gameRenderer.getMainCamera();
         Vec3 cameraPos = camera.getPosition();
         PoseStack poseStack = event.getPoseStack();
         poseStack.pushPose();
         poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
         BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
         VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
         Matrix4f pose = poseStack.last().pose();
         float opacity = (float)Mth.clamp(NanoVisorClientConfig.bowBallisticAssistLineOpacity(), 0.05, 1.0);
         float markerR = solution.currentAimHits() ? 0.62F : 1.0F;
         float markerG = solution.currentAimHits() ? 1.0F : 0.72F;
         float markerB = solution.currentAimHits() ? 1.0F : 0.2F;
         drawLine(consumer, pose, solution.targetCenter(), solution.aimPoint(), markerR, markerG, markerB, opacity);
         drawBillboardSquare(consumer, pose, cameraPos, solution.aimPoint(), solution.markerHalfSize(), markerR, markerG, markerB, opacity);
         bufferSource.endBatch(RenderType.lines());
         poseStack.popPose();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private static void drawBillboardSquare(
      VertexConsumer consumer, Matrix4f pose, Vec3 cameraPos, Vec3 center, double halfSize, float r, float g, float b, float alpha
   ) {
      Vec3 normal = center.subtract(cameraPos).normalize();
      Vec3 upSeed = Math.abs(normal.y) > 0.94 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
      Vec3 right = normal.cross(upSeed).normalize();
      Vec3 up = right.cross(normal).normalize();
      double size = Mth.clamp(halfSize, 0.05, 2.5);
      Vec3 topLeft = center.add(right.scale(-size)).add(up.scale(size));
      Vec3 topRight = center.add(right.scale(size)).add(up.scale(size));
      Vec3 bottomRight = center.add(right.scale(size)).add(up.scale(-size));
      Vec3 bottomLeft = center.add(right.scale(-size)).add(up.scale(-size));
      drawLine(consumer, pose, topLeft, topRight, r, g, b, alpha);
      drawLine(consumer, pose, topRight, bottomRight, r, g, b, alpha);
      drawLine(consumer, pose, bottomRight, bottomLeft, r, g, b, alpha);
      drawLine(consumer, pose, bottomLeft, topLeft, r, g, b, alpha);
      double tick = size * 0.34;
      drawLine(consumer, pose, topLeft, topLeft.add(right.scale(tick)), r, g, b, alpha * 0.75F);
      drawLine(consumer, pose, topLeft, topLeft.add(up.scale(-tick)), r, g, b, alpha * 0.75F);
      drawLine(consumer, pose, topRight, topRight.add(right.scale(-tick)), r, g, b, alpha * 0.75F);
      drawLine(consumer, pose, topRight, topRight.add(up.scale(-tick)), r, g, b, alpha * 0.75F);
      drawLine(consumer, pose, bottomRight, bottomRight.add(right.scale(-tick)), r, g, b, alpha * 0.75F);
      drawLine(consumer, pose, bottomRight, bottomRight.add(up.scale(tick)), r, g, b, alpha * 0.75F);
      drawLine(consumer, pose, bottomLeft, bottomLeft.add(right.scale(tick)), r, g, b, alpha * 0.75F);
      drawLine(consumer, pose, bottomLeft, bottomLeft.add(up.scale(tick)), r, g, b, alpha * 0.75F);
   }

   private static void drawLine(VertexConsumer consumer, Matrix4f pose, Vec3 from, Vec3 to, float r, float g, float b, float alpha) {
      consumer.addVertex(pose, (float)from.x, (float)from.y, (float)from.z).setColor(r, g, b, alpha).setNormal(0.0F, 1.0F, 0.0F);
      consumer.addVertex(pose, (float)to.x, (float)to.y, (float)to.z).setColor(r, g, b, alpha).setNormal(0.0F, 1.0F, 0.0F);
   }

   private static List<Vec3> copyPoints(ArrayList<Vec3> points) {
      return points != null && !points.isEmpty() ? List.copyOf(points) : List.of();
   }

   private static float yawTo(Vec3 from, Vec3 to) {
      Vec3 delta = to.subtract(from);
      return (float)Math.toDegrees(Math.atan2(-delta.x, delta.z));
   }

   private static float pitchTo(Vec3 from, Vec3 to) {
      Vec3 delta = to.subtract(from);
      double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
      return (float)Math.toDegrees(Math.atan2(-delta.y, horizontal));
   }

   private static float wrapDegrees(float value) {
      return Mth.wrapDegrees(value);
   }

   private static double distanceToAabbSqr(Vec3 point, AABB box) {
      double dx = axisDistance(point.x, box.minX, box.maxX);
      double dy = axisDistance(point.y, box.minY, box.maxY);
      double dz = axisDistance(point.z, box.minZ, box.maxZ);
      return dx * dx + dy * dy + dz * dz;
   }

   private static double axisDistance(double value, double min, double max) {
      if (value < min) {
         return min - value;
      } else {
         return value > max ? value - max : 0.0;
      }
   }

   private record AimSearch(float yawDeg, float pitchDeg, double missDistanceSq, boolean hitTarget, boolean valid) {
      static NanoVisorBowBallisticAssistRenderer.AimSearch invalid(float yawDeg, float pitchDeg) {
         return new NanoVisorBowBallisticAssistRenderer.AimSearch(yawDeg, pitchDeg, Double.POSITIVE_INFINITY, false, false);
      }
   }

   private record AimSeed(Vec3 predictedCenter, float yaw, float pitch, double estimatedTicks) {
   }

   private record FireControlSolution(
      int targetId,
      Vec3 targetCenter,
      Vec3 predictedCenter,
      Vec3 aimPoint,
      List<Vec3> points,
      Vec3 impactPoint,
      boolean currentAimHits,
      float solutionYaw,
      float solutionPitch,
      float sourceYaw,
      float sourcePitch,
      long solvedAtTick,
      boolean retainedLock,
      double targetSpeed,
      double targetAcceleration,
      double targetHorizontalRadius,
      double targetVerticalRadius,
      double markerHalfSize,
      double yawToleranceDegrees,
      double pitchToleranceDegrees,
      boolean valid
   ) {
      static NanoVisorBowBallisticAssistRenderer.FireControlSolution empty() {
         return new NanoVisorBowBallisticAssistRenderer.FireControlSolution(
            Integer.MIN_VALUE,
            Vec3.ZERO,
            Vec3.ZERO,
            Vec3.ZERO,
            List.of(),
            null,
            false,
            Float.NaN,
            Float.NaN,
            Float.NaN,
            Float.NaN,
            Long.MIN_VALUE,
            false,
            0.0,
            0.0,
            0.0,
            0.0,
            0.22,
            0.0,
            0.0,
            false
         );
      }
   }

   private record MotionSample(Vec3 center, Vec3 velocity, Vec3 acceleration, long tick) {
   }

   private record MotionState(Vec3 velocity, Vec3 acceleration) {
   }

   private static final class TargetSelection {
      private NanoVisorBowBallisticAssistRenderer.TargetSnapshot snapshot;
      private double score = Double.NEGATIVE_INFINITY;
   }

   private record TargetSnapshot(
      Entity entity,
      AABB box,
      Vec3 center,
      Vec3 aimCenter,
      Vec3 velocity,
      Vec3 acceleration,
      boolean retainedLock,
      double horizontalRadius,
      double verticalRadius,
      double boundingRadius
   ) {
      boolean isMovingFast() {
         return this.velocity.lengthSqr() > 0.04 || this.acceleration.lengthSqr() > 0.01;
      }
   }

   private record TrajectoryResult(List<Vec3> points, Vec3 impactPoint, boolean hitTarget, double closestDistanceSq, int flightTicks) {
   }
}
