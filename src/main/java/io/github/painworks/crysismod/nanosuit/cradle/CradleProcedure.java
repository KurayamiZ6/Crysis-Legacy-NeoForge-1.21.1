package io.github.painworks.crysismod.nanosuit.cradle;

import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.config.NanosuitServerConfig;
import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlockEntity.MachineState;
import io.github.painworks.crysismod.nanosuit.cradle.RoboticArmAssemblyBlockEntity.State;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.integration.BiologicalSuitIntegration;
import io.github.painworks.crysismod.nanosuit.integration.BiologicalSuitIntegration.Result;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

public final class CradleProcedure {
   public enum Operation {
      INTEGRATION(200), EXTRACTION(150);
      private final int cost;
      Operation(int cost) { this.cost = cost; }
      public int cost() { return cost; }
      public String key() { return name().toLowerCase(Locale.ROOT); }
   }

   public enum Issue {
      READY, NO_CRADLE, POWER_OFF, BUSY, ARMS, BATTERY, CHARGE, STORAGE;
      public String key() { return name().toLowerCase(Locale.ROOT); }
   }

   private static final int DURATION_TICKS = 100;
   private static final int CONFIRM_WINDOW = 200;
   private static final int CONFIRM_DELAY = 10;
   private static final ResourceKey<DamageType> EXTRACTION = ResourceKey.create(Registries.DAMAGE_TYPE,
      ResourceLocation.fromNamespaceAndPath("crysismod", "cradle_extraction"));

   private final NanosuitCradleBlockEntity cradle;
   private final Operation operation;
   private final RoboticArmAssemblyBlockEntity arms;
   private UUID asked;
   private long askedAt;
   private NanosuitProfile askedProfile;
   private double warnedRisk;
   private ServerPlayer wearer;
   private ServerLevel level;
   private Vec3 entry;
   private Vec3 anchor;
   private Pose entryPose;
   private long readyAt;
   private NanosuitProfile profile;
   private UUID extractionIdentity;

   private CradleProcedure(NanosuitCradleBlockEntity cradle, Operation operation, RoboticArmAssemblyBlockEntity arms) {
      this.cradle = cradle;
      this.operation = operation;
      this.arms = arms;
   }

   public Operation operation() { return operation; }
   boolean running() { return wearer != null; }
   boolean wornBy(ServerPlayer player) { return wearer == player; }

   static Issue issue(NanosuitCradleBlockEntity cradle, Operation operation) {
      if (cradle == null) return Issue.NO_CRADLE;
      if (cradle.procedure() != null || cradle.machineState() == MachineState.RUNNING) return Issue.BUSY;
      if (cradle.machineState() == MachineState.OFF) return Issue.POWER_OFF;
      var arms = cradle.roboticArms().assembly();
      if (arms == null || arms.armState() != State.FOLDED) return Issue.ARMS;
      return resourceIssue(cradle, operation);
   }

   private static Issue resourceIssue(NanosuitCradleBlockEntity cradle, Operation operation) {
      if (cradle.batteryKind() == null) return Issue.BATTERY;
      if (!cradle.canDebitEnergy(operation.cost())) return Issue.CHARGE;
      boolean storageReady = operation == Operation.INTEGRATION ? cradle.storedProfile() != null : cradle.occupiedSlots() == 0;
      return storageReady ? Issue.READY : Issue.STORAGE;
   }

   static boolean program(NanosuitCradleBlockEntity cradle, Operation operation) {
      if (issue(cradle, operation) != Issue.READY) return false;
      var arms = cradle.roboticArms().assembly();
      if (arms == null || !arms.transitionTo(State.DEPLOYED)) return false;
      cradle.setProcedure(new CradleProcedure(cradle, operation, arms));
      return true;
   }

   void use(ServerPlayer player) {
      if (running()) {
         if (wornBy(player)) cancel();
         else message(player, "reserved");
         return;
      }
      if (!intact(State.DEPLOYED)) {
         cancel();
         message(player, "interrupted");
         return;
      }
      if (CradlePatient.active(player) || player.isPassenger() || player.isSleeping() || player.isFallFlying()) {
         message(player, "invalid_pose");
         return;
      }
      Result eligibility = validate(player);
      if (eligibility != Result.SUCCESS) {
         player.displayClientMessage(Component.translatable("command.crysismod.biological." + lower(eligibility)), true);
         return;
      }
      var nextProfile = playerProfile(player);
      var world = player.serverLevel();
      if (nextProfile == NanosuitProfile.NANOSUIT_2) {
         long time = world.getGameTime();
         double risk = fatalChance(world);
         if (!player.getUUID().equals(asked) || askedProfile != nextProfile || time - askedAt > CONFIRM_WINDOW || warnedRisk != risk) {
            asked = player.getUUID();
            askedAt = time;
            askedProfile = nextProfile;
            warnedRisk = risk;
            var warning = Component.translatable("crysismod.cradle.procedure.warning_" + operation.key(),
               Math.round(risk * 100));
            player.sendSystemMessage(warning);
            player.displayClientMessage(Component.translatable("crysismod.cradle.procedure.confirm"), true);
            return;
         }
         if (time - askedAt < CONFIRM_DELAY) {
            message(player, "confirm");
            return;
         }
      }

      var pos = cradle.getBlockPos();
      var direction = cradle.getBlockState().getValue(NanosuitCradleBlock.FACING);
      var start = Vec3.atBottomCenterOf(pos.above()).add(direction.getStepX() * .5, 0, direction.getStepZ() * .5);
      if (!world.noCollision(player, player.getDimensions(Pose.SWIMMING).makeBoundingBox(start))
         || !arms.transitionTo(State.OPERATING)) {
         message(player, "invalid_pose");
         return;
      }
      wearer = player;
      level = world;
      entry = player.position();
      entryPose = player.getPose();
      anchor = start;
      readyAt = world.getGameTime() + DURATION_TICKS;
      profile = nextProfile;
      extractionIdentity = operation == Operation.EXTRACTION ? BiologicalSuitIntegration.linkedIdentity(player) : null;
      cradle.setMachineState(MachineState.RUNNING);
      player.closeContainer();
      player.setData(NanosuitCapabilities.CRADLE_PATIENT, new CradlePatient(this, world.dimension().location(), entry));
      hold(player);
      player.connection.teleport(anchor.x, anchor.y, anchor.z, direction.toYRot(), player.getXRot());
      message(player, "started");
   }

   boolean cancel() {
      return finish(MachineState.ABORTED, "interrupted");
   }

   void tick() {
      if (!running()) {
         if (!intact(State.DEPLOYED)) cancel();
         return;
      }
      var player = wearer;
      if (!connected(player) || !player.isAlive() || player.isSpectator() || player.isPassenger()
         || player.level() != level || player.position().distanceToSqr(anchor) > 16 || !intact(State.OPERATING)) {
         cancel();
         return;
      }
      hold(player);
      if (player.position().distanceToSqr(anchor) > 0.0001) {
         player.connection.teleport(anchor.x, anchor.y, anchor.z, player.getYRot(), player.getXRot());
      }
      if (level.getGameTime() < readyAt) return;
      if (validate(player) != Result.SUCCESS || playerProfile(player) != profile
         || operation == Operation.EXTRACTION && !java.util.Objects.equals(extractionIdentity, BiologicalSuitIntegration.linkedIdentity(player))) {
         cancel();
         return;
      }
      commit(player);
   }

   private boolean intact(State expected) {
      return !cradle.isRemoved() && cradle.roboticArms().assembly() == arms && arms.armState() == expected
         && cradle.machineState() != MachineState.OFF && resourceIssue(cradle, operation) == Issue.READY;
   }

   private Result validate(ServerPlayer player) {
      return operation == Operation.INTEGRATION ? BiologicalSuitIntegration.validateIntegration(player, cradle.piecesForProcedure())
         : BiologicalSuitIntegration.validateExtraction(player, cradle.piecesForProcedure());
   }

   private NanosuitProfile playerProfile(ServerPlayer player) {
      return operation == Operation.INTEGRATION ? cradle.storedProfile() : NanosuitArmorUtil.getEquippedProfile(player);
   }

   private void commit(ServerPlayer player) {
      boolean n2Extraction = operation == Operation.EXTRACTION && profile == NanosuitProfile.NANOSUIT_2;
      // Only one server roll. A fatal outcome never touches the armor or biological link.
      boolean fatal = n2Extraction && level.getRandom().nextDouble() < warnedRisk;
      Path journal = null;
      if (!fatal) {
         try {
            journal = CradleTransferRecovery.prepare(cradle, player, releasePosition(player));
         } catch (IOException exception) {
            CradleTransferRecovery.reportFailure(exception);
            cancel();
            return;
         }
      }
      if (cradle.procedure() != this || !intact(State.OPERATING) || validate(player) != Result.SUCCESS) {
         cancel();
         if (journal != null) CradleTransferRecovery.complete(level.getServer(), journal);
         return;
      }
      if (!cradle.tryDebitEnergy(operation.cost())) {
         cancel();
         if (journal != null) CradleTransferRecovery.complete(level.getServer(), journal);
         return;
      }
      if (fatal) {
         finish(MachineState.STANDBY, "failed_extraction");
         level.getChunkSource().save(true);
         player.hurt(player.damageSources().source(EXTRACTION), 10000.0F);
         return;
      }
      var pieces = cradle.piecesForProcedure();
      var result = operation == Operation.INTEGRATION ? BiologicalSuitIntegration.integrate(player, pieces)
         : BiologicalSuitIntegration.extract(player, pieces);
      if (result != Result.SUCCESS) {
         cradle.addEnergy(operation.cost());
         cancel();
         CradleTransferRecovery.complete(level.getServer(), journal);
         return;
      }
      cradle.publishProcedureInventory();
      if (n2Extraction) player.setHealth(Math.min(player.getHealth(), 1.0F));
      finish(MachineState.STANDBY, "completed");
      CradleTransferRecovery.complete(level.getServer(), journal);
   }

   private boolean finish(MachineState next, String status) {
      if (cradle.procedure() != this) return false;
      cradle.setProcedure(null);
      cradle.setMachineState(next);
      arms.transitionTo(State.FOLDED);
      var player = wearer;
      wearer = null;
      if (player != null) {
         player.removeData(NanosuitCapabilities.CRADLE_PATIENT);
         player.setPose(entryPose);
         player.setDeltaMovement(Vec3.ZERO);
         if (player.isAlive() && player.level() == level && player.position().distanceToSqr(anchor) <= 16) {
            var safe = releasePosition(player);
            if (!connected(player)) player.moveTo(safe.x, safe.y, safe.z, player.getYRot(), player.getXRot());
            else player.connection.teleport(safe.x, safe.y, safe.z, player.getYRot(), player.getXRot());
         }
         if (connected(player)) message(player, status);
      }
      return true;
   }

   private Vec3 releasePosition(ServerPlayer player) {
      var dimensions = player.getDimensions(Pose.STANDING);
      if (level.noCollision(player, dimensions.makeBoundingBox(entry))) return entry;
      var pos = cradle.getBlockPos();
      for (var direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
         Vec3 side = Vec3.atBottomCenterOf(pos.relative(direction, 2));
         if (level.hasChunkAt(BlockPos.containing(side)) && level.noCollision(player, dimensions.makeBoundingBox(side))) return side;
      }
      return Vec3.atBottomCenterOf(level.getSharedSpawnPos());
   }

   private static void hold(ServerPlayer player) {
      player.setPose(Pose.SWIMMING);
      player.setDeltaMovement(Vec3.ZERO);
      player.fallDistance = 0;
      player.setSprinting(false);
   }

   private static boolean connected(ServerPlayer player) {
      return player.connection != null && player.connection.getConnection().isConnected();
   }

   private static double fatalChance(ServerLevel world) {
      if (world.getServer().isHardcore()) return .5;
      return NanosuitServerConfig.CRADLE_N2_FATAL_RISK.get() ? NanosuitServerConfig.CRADLE_N2_FATAL_CHANCE.get() : 0;
   }

   private static String lower(Enum<?> value) { return value.name().toLowerCase(Locale.ROOT); }

   private static void message(ServerPlayer player, String key) {
      player.displayClientMessage(Component.translatable("crysismod.cradle.procedure." + key), true);
   }
}
