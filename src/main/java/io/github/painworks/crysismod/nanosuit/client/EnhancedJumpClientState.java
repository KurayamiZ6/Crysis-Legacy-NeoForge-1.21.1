package io.github.painworks.crysismod.nanosuit.client;

import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanosuit.network.RequestEnhancedJumpPacket;
import io.github.painworks.crysismod.nanosuit.network.RequestEnhancedJumpPacket.Phase;
import io.github.painworks.crysismod.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public final class EnhancedJumpClientState {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static int requestUntilClientTick;
   private static int activeUntilClientTick;
   private static int activationClientTick;
   private static boolean serverConfirmed;
   private static boolean lastVanillaSlowdown;
   private static boolean lastSlowdownBypassApplied;
   private static double lastUnshiftedVelocityX;
   private static double lastUnshiftedVelocityZ;
   private static double requestedVelocityX;
   private static double requestedVelocityZ;
   private static LocalPlayer chargePlayer;
   private static int chargeTicks;
   private static boolean jumpHeldLastTick;
   private static boolean launchedWhileHeld;
   private static boolean testLogging;
   private static int testSample;

   private EnhancedJumpClientState() {
   }

   private static void beginRequest() {
      Minecraft minecraft = Minecraft.getInstance();
      int clientTick = minecraft.player == null ? 0 : minecraft.player.tickCount;
      requestUntilClientTick = clientTick + 2;
      serverConfirmed = false;
      requestedVelocityX = lastUnshiftedVelocityX;
      requestedVelocityZ = lastUnshiftedVelocityZ;
      logTestState("REQUEST", minecraft.player);
   }

   public static void activate(double verticalVelocity) {
      Minecraft minecraft = Minecraft.getInstance();
      activationClientTick = minecraft.player == null ? 0 : minecraft.player.tickCount;
      activeUntilClientTick = activationClientTick + 40;
      serverConfirmed = true;
      if (minecraft.player != null) {
         logTestState("CONFIRM_BEFORE", minecraft.player);
         minecraft.player.setDeltaMovement(requestedVelocityX, verticalVelocity, requestedVelocityZ);
         minecraft.player.hasImpulse = true;
         minecraft.player.playSound((SoundEvent)ModSounds.POWER_JUMP.get(), 1.0F, 1.0F);
         NanosuitPowerFeedback.triggerForce();
         logTestState("CONFIRM_AFTER", minecraft.player);
      }
   }

   public static void captureUnshiftedHorizontalVelocity() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null && !minecraft.options.keyShift.isDown()) {
         lastUnshiftedVelocityX = minecraft.player.getDeltaMovement().x;
         lastUnshiftedVelocityZ = minecraft.player.getDeltaMovement().z;
      }
   }

   public static boolean shouldBypassSneakSlowdown() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player == null) {
         return false;
      } else {
         int clientTick = minecraft.player.tickCount;
         if (!serverConfirmed) {
            return clientTick <= requestUntilClientTick;
         } else if (clientTick < activeUntilClientTick && (clientTick <= activationClientTick + 1 || !minecraft.player.onGround())) {
            return true;
         } else {
            activeUntilClientTick = 0;
            serverConfirmed = false;
            return false;
         }
      }
   }

   public static boolean shouldSuppressCrouchInput() {
      return shouldBypassSneakSlowdown();
   }

   public static boolean handleJumpInput(boolean held) {
      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer player = minecraft.player;
      if (chargePlayer != player) {
         chargePlayer = player;
         chargeTicks = 0;
         jumpHeldLastTick = false;
         launchedWhileHeld = false;
      }
      if (player == null || minecraft.screen != null || !minecraft.isWindowActive()) {
         if (chargeTicks > 0 && player != null) {
            sendChargePhase(Phase.RELEASE);
         }
         chargeTicks = 0;
         launchedWhileHeld = false;
         jumpHeldLastTick = held;
         return held;
      }

      boolean pressed = held && !jumpHeldLastTick;
      jumpHeldLastTick = held;
      if (!held) {
         launchedWhileHeld = false;
         if (chargeTicks == 0) {
            return false;
         }
         // A short tap becomes one ordinary jump on release.
         chargeTicks = 0;
         sendChargePhase(Phase.RELEASE);
         return player.onGround();
      }
      if (launchedWhileHeld) {
         return false;
      }
      var jump = NanosuitPlayerHandler.availablePowerJump(player);
      if (chargeTicks > 0 && jump == null) {
         chargeTicks = 0;
         sendChargePhase(Phase.RELEASE);
         return true;
      }
      if (chargeTicks == 0) {
         if (!pressed || jump == null) {
            return true;
         }
         sendChargePhase(Phase.BEGIN);
      }
      if (++chargeTicks >= jump.holdTicks()) {
         chargeTicks = 0;
         launchedWhileHeld = true;
         beginRequest();
         sendChargePhase(Phase.LAUNCH);
      }
      return false;
   }

   private static void sendChargePhase(Phase phase) {
      NanosuitNetwork.sendToServer(new RequestEnhancedJumpPacket(phase));
   }

   public static void recordSlowdownDecision(boolean vanillaSlowdown, boolean bypassApplied) {
      lastVanillaSlowdown = vanillaSlowdown;
      lastSlowdownBypassApplied = bypassApplied;
   }

   public static boolean isServerConfirmed() {
      return serverConfirmed;
   }

   public static boolean isRequestPending() {
      Minecraft minecraft = Minecraft.getInstance();
      return !serverConfirmed && minecraft.player != null && minecraft.player.tickCount <= requestUntilClientTick;
   }

   public static boolean lastVanillaSlowdown() {
      return lastVanillaSlowdown;
   }

   public static boolean lastSlowdownBypassApplied() {
      return lastSlowdownBypassApplied;
   }

   public static int activeTicksRemaining() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player == null) {
         return 0;
      } else {
         return serverConfirmed
            ? Math.max(0, activeUntilClientTick - minecraft.player.tickCount)
            : Math.max(0, requestUntilClientTick - minecraft.player.tickCount);
      }
   }

   public static void setTestLogging(boolean enabled) {
      testLogging = enabled;
      testSample = 0;
      LOGGER.info("[PowerJumpClientTest] {}", enabled ? "START" : "STOP");
      if (enabled) {
         logTestState("INITIAL", Minecraft.getInstance().player);
      }
   }

   public static void logTestTick() {
      if (testLogging) {
         logTestState("CLIENT_TICK sample=" + ++testSample, Minecraft.getInstance().player);
      }
   }

   private static void logTestState(String event, LocalPlayer player) {
      if (testLogging && player != null) {
         Minecraft minecraft = Minecraft.getInstance();
         Vec3 velocity = player.getDeltaMovement();
         double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
         LOGGER.info(
            "[PowerJumpClientTest] {} tick={} pending={} confirmed={} remain={} rawShift={} inputShift={} crouch={} slowVanilla={} bypass={} input=[left={},forward={}] velocity=[x={},y={},z={},h={}] cached=[x={},z={}] requested=[x={},z={}] ground={} sprint={} fallDistance={}",
            new Object[]{
               event,
               player.tickCount,
               isRequestPending(),
               serverConfirmed,
               activeTicksRemaining(),
               minecraft.options.keyShift.isDown(),
               player.input.shiftKeyDown,
               player.isCrouching(),
               lastVanillaSlowdown,
               lastSlowdownBypassApplied,
               player.input.leftImpulse,
               player.input.forwardImpulse,
               velocity.x,
               velocity.y,
               velocity.z,
               horizontalSpeed,
               lastUnshiftedVelocityX,
               lastUnshiftedVelocityZ,
               requestedVelocityX,
               requestedVelocityZ,
               player.onGround(),
               player.isSprinting(),
               player.fallDistance
            }
         );
      }
   }
}
