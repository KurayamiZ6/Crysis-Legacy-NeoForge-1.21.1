package io.github.painworks.crysismod.nanosuit.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public final class NanosuitHudInertia {
   private static final float CAMERA_TO_ROLL_FACTOR = 0.28F;
   private static final float WALK_ROLL_WEIGHT = 0.6F;
   private static final float MOVE_WEIGHT_SCALE = 0.38F;
   private static final float BASE_WALK_RANGE_X = 9.0F;
   private static final float BASE_WALK_RANGE_Y = 8.0F;
   private static final float WALK_PARABOLA_A = -0.01F;
   private static final float WALK_PARABOLA_H = 0.0F;
   private static final float WALK_PARABOLA_K = 10.0F;
   private static final float WALK_PARABOLA_EDGE_X = (float)Math.sqrt(1000.0);
   private static final float WALK_PARABOLA_LEFT_X = 0.0F - WALK_PARABOLA_EDGE_X;
   private static final float WALK_PARABOLA_RIGHT_X = 0.0F + WALK_PARABOLA_EDGE_X;
   private static final float MIN_DT = 0.001F;
   private static final float MAX_DT = 0.05F;
   private float offsetX;
   private float offsetY;
   private float rollDeg;
   private float walkStateX;
   private float walkStateY;
   private float walkStateRoll;
   private float walkVelX;
   private float walkVelY;
   private float walkVelRoll;
   private float stepTimer;
   private int stepSideSign = 1;
   private boolean walkingLastFrame;
   private float lastYaw;
   private float lastPitch;
   private long lastTimeNs;
   private boolean initialized;

   public void update(LocalPlayer player, float partialTick, NanosuitHudInertia.Params params) {
      long now = System.nanoTime();
      float dt = this.initialized && this.lastTimeNs != 0L ? Mth.clamp((float)(now - this.lastTimeNs) / 1.0E9F, 0.001F, 0.05F) : 0.016666668F;
      this.lastTimeNs = now;
      if (player == null) {
         this.initialized = false;
         this.approachNeutral(dt, params.returnSpeed, params.walkReturnToCenterSpeed, params.walkDamping);
      } else {
         float yaw = resolveCurrentCameraYaw(player);
         float pitch = resolveCurrentCameraPitch(player);
         if (!this.initialized) {
            this.initialized = true;
            this.lastYaw = yaw;
            this.lastPitch = pitch;
         } else {
            float yawDelta = Mth.wrapDegrees(yaw - this.lastYaw);
            float pitchDelta = pitch - this.lastPitch;
            this.lastYaw = yaw;
            this.lastPitch = pitch;
            if (params.enabled && !(params.intensity <= 0.0F)) {
               float yawVelocity = yawDelta / dt;
               float pitchVelocity = pitchDelta / dt;
               float cameraTargetX = Mth.clamp(-yawVelocity * params.yawStrength, -params.maxOffset, params.maxOffset);
               float cameraTargetY = Mth.clamp(pitchVelocity * params.pitchStrength, -params.maxOffset, params.maxOffset);
               NanosuitHudInertia.WalkTargets walkTargets = this.resolveWalkTargets(player, dt, params);
               float spring = walkTargets.active ? params.walkSpringStrength : params.walkReturnToCenterSpeed;
               this.walkVelX = this.walkVelX + (walkTargets.targetX - this.walkStateX) * spring * dt;
               this.walkVelY = this.walkVelY + (walkTargets.targetY - this.walkStateY) * spring * dt;
               this.walkVelRoll = this.walkVelRoll + (walkTargets.targetRoll - this.walkStateRoll) * spring * dt;
               float velDamp = (float)Math.exp(-params.walkDamping * dt);
               this.walkVelX *= velDamp;
               this.walkVelY *= velDamp;
               this.walkVelRoll *= velDamp;
               this.walkStateX = this.walkStateX + this.walkVelX * dt;
               this.walkStateY = this.walkStateY + this.walkVelY * dt;
               this.walkStateRoll = this.walkStateRoll + this.walkVelRoll * dt;
               this.walkStateX = Mth.clamp(this.walkStateX, -params.maxWalkOffsetX, params.maxWalkOffsetX);
               this.walkStateY = Mth.clamp(this.walkStateY, -params.maxWalkOffsetY, params.maxWalkOffsetY);
               this.walkStateRoll = Mth.clamp(this.walkStateRoll, -params.maxWalkRoll, params.maxWalkRoll);
               float targetX = Mth.clamp((cameraTargetX + this.walkStateX) * params.intensity, -params.maxOffset, params.maxOffset);
               float targetY = Mth.clamp((cameraTargetY + this.walkStateY) * params.intensity, -params.maxOffset, params.maxOffset);
               float targetRoll = Mth.clamp((-cameraTargetX * 0.28F + this.walkStateRoll * 0.6F) * params.intensity, -params.maxRoll, params.maxRoll);
               boolean activeInput = Math.abs(yawDelta) > 0.02F
                  || Math.abs(pitchDelta) > 0.02F
                  || Math.abs(this.walkStateX) > 0.01F
                  || Math.abs(this.walkStateY) > 0.01F
                  || Math.abs(this.walkStateRoll) > 0.01F;
               float response = activeInput ? params.damping : params.returnSpeed;
               float alpha = 1.0F - (float)Math.exp(-response * dt);
               this.offsetX = this.offsetX + (targetX - this.offsetX) * alpha;
               this.offsetY = this.offsetY + (targetY - this.offsetY) * alpha;
               this.rollDeg = this.rollDeg + (targetRoll - this.rollDeg) * alpha;
            } else {
               this.approachNeutral(dt, params.returnSpeed, params.walkReturnToCenterSpeed, params.walkDamping);
            }
         }
      }
   }

   private static float resolveCurrentCameraYaw(LocalPlayer player) {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft.gameRenderer != null && minecraft.gameRenderer.getMainCamera() != null
         ? minecraft.gameRenderer.getMainCamera().getYRot()
         : player.getYRot();
   }

   private static float resolveCurrentCameraPitch(LocalPlayer player) {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft.gameRenderer != null && minecraft.gameRenderer.getMainCamera() != null
         ? minecraft.gameRenderer.getMainCamera().getXRot()
         : player.getXRot();
   }

   private NanosuitHudInertia.WalkTargets resolveWalkTargets(LocalPlayer player, float dt, NanosuitHudInertia.Params params) {
      float speedBlocksPerSec = (float)(player.getDeltaMovement().horizontalDistance() * 20.0);
      float moveWeight = Mth.clamp(speedBlocksPerSec * 0.38F, 0.0F, 1.0F);
      boolean disableStepBobbing = !player.onGround() || player.getAbilities().flying || player.isFallFlying() || player.isSwimming() || player.isPassenger();
      boolean walkingActive = !disableStepBobbing && moveWeight > 0.01F;
      if (!walkingActive) {
         this.walkingLastFrame = false;
         this.stepTimer = 0.0F;
         return new NanosuitHudInertia.WalkTargets(false, 0.0F, 0.0F, 0.0F);
      } else {
         float sprintMul = player.isSprinting() ? params.sprintWalkMultiplier : 1.0F;
         float crouchMul = player.isCrouching() ? 0.55F : 1.0F;
         float stepDuration = Mth.clamp(params.walkStepDuration / sprintMul, 0.12F, 1.0F);
         if (!this.walkingLastFrame) {
            this.stepTimer = 0.0F;
            this.stepSideSign = 1;
            this.walkingLastFrame = true;
         }

         for (this.stepTimer += dt; this.stepTimer >= stepDuration; this.stepSideSign = -this.stepSideSign) {
            this.stepTimer -= stepDuration;
         }

         float t = Mth.clamp(this.stepTimer / stepDuration, 0.0F, 1.0F);
         float blend = smoothstep(t);
         float xNorm = Mth.lerp(blend, this.stepSideSign, -this.stepSideSign);
         float parabolaX = Mth.lerp((xNorm + 1.0F) * 0.5F, WALK_PARABOLA_LEFT_X, WALK_PARABOLA_RIGHT_X);
         float parabolaY = -0.01F * (parabolaX - 0.0F) * (parabolaX - 0.0F) + 10.0F;
         float parabola01 = Mth.clamp(parabolaY / 10.0F, 0.0F, 1.0F);
         float arcSigned = parabola01 * 2.0F - 1.0F;
         float baseSideX = xNorm * params.walkSideAmplitude;
         float baseY = arcSigned * params.walkVerticalAmplitude;
         float baseRoll = -xNorm * params.walkRollAmplitude;
         float strength = moveWeight * sprintMul * crouchMul;
         float rangeScaleX = params.maxWalkOffsetX / 9.0F;
         float rangeScaleY = params.maxWalkOffsetY / 8.0F;
         float targetX = Mth.clamp(baseSideX * strength * rangeScaleX, -params.maxWalkOffsetX, params.maxWalkOffsetX);
         float targetY = Mth.clamp(baseY * strength * rangeScaleY, -params.maxWalkOffsetY, params.maxWalkOffsetY);
         float targetRoll = Mth.clamp(baseRoll * strength, -params.maxWalkRoll, params.maxWalkRoll);
         return new NanosuitHudInertia.WalkTargets(true, targetX, targetY, targetRoll);
      }
   }

   private void approachNeutral(float dt, float returnSpeed, float walkReturnSpeed, float walkDamping) {
      float alpha = 1.0F - (float)Math.exp(-returnSpeed * dt);
      this.offsetX = this.offsetX + (0.0F - this.offsetX) * alpha;
      this.offsetY = this.offsetY + (0.0F - this.offsetY) * alpha;
      this.rollDeg = this.rollDeg + (0.0F - this.rollDeg) * alpha;
      float walkAlpha = 1.0F - (float)Math.exp(-walkReturnSpeed * dt);
      this.walkStateX = this.walkStateX + (0.0F - this.walkStateX) * walkAlpha;
      this.walkStateY = this.walkStateY + (0.0F - this.walkStateY) * walkAlpha;
      this.walkStateRoll = this.walkStateRoll + (0.0F - this.walkStateRoll) * walkAlpha;
      float velDamp = (float)Math.exp(-walkDamping * dt);
      this.walkVelX *= velDamp;
      this.walkVelY *= velDamp;
      this.walkVelRoll *= velDamp;
   }

   public float getOffsetX() {
      return this.offsetX;
   }

   public float getOffsetY() {
      return this.offsetY;
   }

   public float getRollDeg() {
      return this.rollDeg;
   }

   private static float smoothstep(float x) {
      float t = Mth.clamp(x, 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   public static final class Params {
      private boolean enabled;
      private float intensity;
      private float yawStrength;
      private float pitchStrength;
      private float maxOffset;
      private float maxRoll;
      private float damping;
      private float returnSpeed;
      private float walkSideAmplitude;
      private float walkVerticalAmplitude;
      private float walkRollAmplitude;
      private float walkStepDuration;
      private float walkSpringStrength;
      private float walkDamping;
      private float walkReturnToCenterSpeed;
      private float sprintWalkMultiplier;
      private float maxWalkOffsetX;
      private float maxWalkOffsetY;
      private float maxWalkRoll;

      public NanosuitHudInertia.Params set(
         boolean enabled,
         float intensity,
         float yawStrength,
         float pitchStrength,
         float maxOffset,
         float maxRoll,
         float damping,
         float returnSpeed,
         float walkSideAmplitude,
         float walkVerticalAmplitude,
         float walkRollAmplitude,
         float walkStepDuration,
         float walkSpringStrength,
         float walkDamping,
         float walkReturnToCenterSpeed,
         float sprintWalkMultiplier,
         float maxWalkOffsetX,
         float maxWalkOffsetY,
         float maxWalkRoll
      ) {
         this.enabled = enabled;
         this.intensity = intensity;
         this.yawStrength = yawStrength;
         this.pitchStrength = pitchStrength;
         this.maxOffset = maxOffset;
         this.maxRoll = maxRoll;
         this.damping = damping;
         this.returnSpeed = returnSpeed;
         this.walkSideAmplitude = walkSideAmplitude;
         this.walkVerticalAmplitude = walkVerticalAmplitude;
         this.walkRollAmplitude = walkRollAmplitude;
         this.walkStepDuration = walkStepDuration;
         this.walkSpringStrength = walkSpringStrength;
         this.walkDamping = walkDamping;
         this.walkReturnToCenterSpeed = walkReturnToCenterSpeed;
         this.sprintWalkMultiplier = sprintWalkMultiplier;
         this.maxWalkOffsetX = maxWalkOffsetX;
         this.maxWalkOffsetY = maxWalkOffsetY;
         this.maxWalkRoll = maxWalkRoll;
         return this;
      }
   }

   private record WalkTargets(boolean active, float targetX, float targetY, float targetRoll) {
   }
}
