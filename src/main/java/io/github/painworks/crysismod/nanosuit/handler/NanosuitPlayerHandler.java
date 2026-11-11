package io.github.painworks.crysismod.nanosuit.handler;

import io.github.painworks.crysismod.nanosuit.network.SyncPowerFeedbackPacket;

import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.config.NanosuitServerConfig;
import io.github.painworks.crysismod.nanosuit.energy.INanosuitEnergy;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanosuit.network.NanosuitSyncManager;
import io.github.painworks.crysismod.nanosuit.network.SyncEnhancedJumpPacket;
import io.github.painworks.crysismod.nanosuit.network.SyncSuitEnergySoundPacket;
import io.github.painworks.crysismod.nanosuit.network.SyncSuitEnergySoundPacket.Cue;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitCombatRules;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitControls.Ability;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitEnergyRules;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitMobilityRules;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanovision.server.NanovisionServerState;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;


import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.slf4j.Logger;

public class NanosuitPlayerHandler {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final float ENERGY_PER_DIVISION = 5.0F;
   public static final int ENHANCED_JUMP_HOLD_TICKS = NanosuitMobilityRules.LEGACY_JUMP_HOLD_TICKS;
   private static final int ACTIVE_DRAIN_SYNC_INTERVAL_TICKS = 2;
   private static final double MOVEMENT_POSITION_EPSILON_SQR = 6.25E-4;
   private static final float MOVEMENT_INPUT_EPSILON = 0.01F;
   private static final float MOVEMENT_WALKDIST_EPSILON = 0.001F;
   private static final double MOVEMENT_VELOCITY_EPSILON_SQR = 1.0E-4;
   private static final int MOVING_HOLD_TICKS = 4;
   private static final Map<UUID, NanosuitPlayerHandler.PlayerRuntimeState> PLAYER_RUNTIME_STATE = new HashMap<>();
   private static final Map<UUID, NanosuitPlayerHandler.PowerJumpDebugSession> POWER_JUMP_DEBUG_SESSIONS = new HashMap<>();
   private static final net.minecraft.resources.ResourceLocation PASSIVE_SPEED_MODIFIER_ID = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("crysismod", "nanosuit_passive_speed");
   private static final String PASSIVE_SPEED_MODIFIER_NAME = "nanosuit_passive_speed";
   private static final float ENERGY_BOUND_EPSILON = 0.001F;
   private static boolean profilingEnabled = false;
   private static long profilingTickCalls = 0L;
   private static long profilingTickTotalNanos = 0L;
   private static long profilingTickMaxNanos = 0L;
   private static long profilingMovingCalls = 0L;
   private static long profilingMovingTrueCalls = 0L;
   private static long profilingMovingTotalNanos = 0L;
   private static long profilingMovingMaxNanos = 0L;

   @SubscribeEvent
   public void onPlayerClone(Clone event) {
      UUID playerId = event.getEntity().getUUID();
      resetRuntimeStateForPlayer(playerId);
      NanosuitCapabilities.get(event.getEntity()).setSprintBoostReady(false);
      this.syncToClient(event.getEntity());
   }

   @SubscribeEvent
   public void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
      if (!event.getEntity().level().isClientSide()) {
         Player player = event.getEntity();
         if (player instanceof ServerPlayer serverPlayer) {
            tickEnhancedJumpCharge(serverPlayer);
            tickEnhancedJumpMantle(serverPlayer);
         }
         if (!profilingEnabled) {
            java.util.Optional.<INanosuitEnergy>of(NanosuitCapabilities.get(player)).ifPresent(data -> {
               this.tickPlayer(player, data);
               tracePowerJumpTick(player, data);
            });
         } else {
            long start = System.nanoTime();
            java.util.Optional.<INanosuitEnergy>of(NanosuitCapabilities.get(player)).ifPresent(data -> {
               this.tickPlayer(player, data);
               tracePowerJumpTick(player, data);
            });
            long elapsed = System.nanoTime() - start;
            profilingTickCalls++;
            profilingTickTotalNanos += elapsed;
            if (elapsed > profilingTickMaxNanos) {
               profilingTickMaxNanos = elapsed;
            }
         }
      }
   }

   @SubscribeEvent
   public void onBreathe(LivingBreatheEvent event) {
      if (event.getEntity() instanceof Player player && player.isEyeInFluid(FluidTags.WATER)
            && NanosuitArmorUtil.hasFullSet(player)) {
         event.setCanBreathe(true);
         event.setRefillAirAmount(player.getMaxAirSupply() - player.getAirSupply());
      }
   }

   @SubscribeEvent
   public void onHealthDamage(LivingDamageEvent.Post event) {
      if (event.getEntity() instanceof ServerPlayer player && event.getNewDamage() > 0.0F) {
         NanosuitProfile profile = NanosuitArmorUtil.getEquippedProfile(player);
         NanosuitEnergyRules energy = profile == null ? NanosuitEnergyRules.LEGACY_CURRENT : profile.energy();
         runtimeState(player.getUUID()).nextHealthRecoveryTick = player.tickCount + energy.healing().delayTicks();
      }
   }

   @SubscribeEvent
   public void onLivingHurt(LivingIncomingDamageEvent event) {
      if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
         if (event.getSource().is(DamageTypes.FALL) && consumeEnhancedJumpFallProtection(player)) {
            event.setCanceled(true);
            event.setAmount(0.0F);
         } else {
            java.util.Optional.<INanosuitEnergy>of(NanosuitCapabilities.get(player))
               .ifPresent(
                  data -> {
                     if (NanosuitArmorUtil.hasFullSet(player)) {
                        if (data.getMode() == NanosuitMode.ARMOR && !(data.getEnergy() <= 0.0F)) {
                           if (this.isArmorDamageBlockable(event.getSource())) {
                              if (hasInfiniteCreativeEnergy(player)) {
                                 data.setEnergy(100.0F);
                                 this.syncToClient(player);
                              } else {
                                 float incomingDamageHp = Math.max(0.0F, event.getAmount());
                                 if (!(incomingDamageHp <= 0.0F)) {
                                    float maxEnergy = 100.0F;
                                    float currentEnergyBefore = data.getEnergy();
                                    float fullEnergyBlockCapacityHp = Math.max(1.0F, NanosuitServerConfig.armorModeFullEnergyBlockCapacityHp());
                                    float hpPerEnergy = fullEnergyBlockCapacityHp / maxEnergy;
                                    float availableBlockHp = currentEnergyBefore * hpPerEnergy;
                                    float absorbedHp = Math.min(incomingDamageHp, availableBlockHp);
                                    float remainingDamageHp = incomingDamageHp - absorbedHp;
                                    float energyCost = absorbedHp / hpPerEnergy;
                                    float currentEnergyAfter = Math.max(0.0F, currentEnergyBefore - energyCost);
                                    data.setEnergy(currentEnergyAfter);
                                    if (currentEnergyAfter <= 0.0F) {
                                       data.setMode(NanosuitMode.NONE);
                                       data.setSprintBoostReady(false);
                                    }

                                    if (remainingDamageHp <= 0.0F) {
                                       event.setCanceled(true);
                                       event.setAmount(0.0F);
                                    } else {
                                       event.setAmount(remainingDamageHp);
                                    }

                                    if (NanosuitServerConfig.debugArmorDamageAbsorption()) {
                                       LOGGER.info(
                                          "[NanoSuit][ArmorAbsorb] src={} incomingHp={} energyBefore={} maxEnergy={} hpPerEnergy={} absorbedHp={} remainingHp={} energyCost={} energyAfter={} armorModeActive={}",
                                          new Object[]{
                                             event.getSource().typeHolder().unwrapKey().map(Object::toString).orElse("unknown"),
                                             incomingDamageHp,
                                             currentEnergyBefore,
                                             maxEnergy,
                                             hpPerEnergy,
                                             absorbedHp,
                                             remainingDamageHp,
                                             energyCost,
                                             currentEnergyAfter,
                                             data.getMode() == NanosuitMode.ARMOR
                                          }
                                       );
                                    }

                                    this.syncToClient(player);
                                 }
                              }
                           }
                        }
                     }
                  }
               );
         }
      }
   }

   @SubscribeEvent
   public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
      UUID playerId = event.getEntity().getUUID();
      resetRuntimeStateForPlayer(playerId);
      NanosuitCapabilities.get(event.getEntity()).setSprintBoostReady(false);
      this.ensurePassiveSpeed(event.getEntity());
      this.syncToClient(event.getEntity());
   }

   @SubscribeEvent
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      UUID playerId = event.getEntity().getUUID();
      resetRuntimeStateForPlayer(playerId);
      NanosuitCapabilities.get(event.getEntity()).setSprintBoostReady(false);
      this.ensurePassiveSpeed(event.getEntity());
      this.syncToClient(event.getEntity());
   }

   @SubscribeEvent
   public void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      UUID playerId = event.getEntity().getUUID();
      PlayerRuntimeState runtime = PLAYER_RUNTIME_STATE.get(playerId);
      if (runtime != null) {
         updateCloakVisibility(event.getEntity(), runtime, false);
      }
      resetRuntimeStateForPlayer(playerId);
      NanosuitCapabilities.get(event.getEntity()).setSprintBoostReady(false);
      this.ensurePassiveSpeed(event.getEntity());
      this.syncToClient(event.getEntity());
   }

   @SubscribeEvent
   public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
      UUID playerId = event.getEntity().getUUID();
      PLAYER_RUNTIME_STATE.remove(playerId);
      POWER_JUMP_DEBUG_SESSIONS.remove(playerId);
      NanosuitSyncManager.clear(playerId);
   }

   private void tickPlayer(Player player, INanosuitEnergy data) {
      UUID playerId = player.getUUID();
      NanosuitPlayerHandler.PlayerRuntimeState runtime = runtimeState(playerId);
      if (runtime.enhancedJumpFallProtectionActive && player.onGround()) {
         runtime.enhancedJumpFallProtectionActive = false;
      }

      float previousEnergy = data.getEnergy();
      NanosuitMode previousMode = data.getMode();
      boolean previousSprintBoostReady = data.isSprintBoostReady();
      NanosuitProfile equippedProfile = NanosuitArmorUtil.getEquippedProfile(player);
      boolean hasFullSet = equippedProfile != null;
      // Recovery and the end of a boost still advance after unequipping the suit.
      NanosuitProfile rulesProfile = hasFullSet ? equippedProfile : NanosuitProfile.LEGACY_CURRENT;
      boolean landSprinting = isLandSprinting(player);
      runtime.landSprintingLastTick = landSprinting;
      if (!landSprinting || !hasFullSet) {
         data.setSprintBoostReady(false);
      }
      boolean creativeInfiniteEnergy = hasInfiniteCreativeEnergy(player);
      if (data.isRecoveryLocked()) {
         if (creativeInfiniteEnergy) {
            data.setEnergy(100.0F);
            data.setRegenDelayTicks(0);
            this.syncToClient(player);
         } else if (player instanceof ServerPlayer serverPlayer) {
            this.tickRecovery(serverPlayer, data, runtime, rulesProfile, hasFullSet);
            return;
         }
      }
      boolean nanovisionActive = player instanceof ServerPlayer serverPlayer && NanovisionServerState.isActive(serverPlayer);
      this.ensurePassiveSpeed(player);
      if (!hasFullSet) {
         updatePowerSprint(player, data, runtime, rulesProfile, false);
         updateSustainedPower(player, runtime, false);
         if (data.getMode() != NanosuitMode.NONE) {
            data.setMode(NanosuitMode.NONE);
         }

         if (creativeInfiniteEnergy) {
            data.setEnergy(100.0F);
            data.setRegenDelayTicks(0);
         } else if (nanovisionActive) {
            data.consumeEnergy(NanosuitServerConfig.nanovisionDrainPerTick());
            data.setRegenDelayTicks(NanosuitServerConfig.regenDelayAfterDrainTicks());
         } else {
            int regenDelayTicks = data.getRegenDelayTicks();
            if (regenDelayTicks > 0) {
               data.setRegenDelayTicks(regenDelayTicks - 1);
            } else {
               data.addEnergy(NanosuitServerConfig.energyRegenPerTick());
            }
         }

         runtime.movingHoldTicks = 0;
         updateCloakVisibility(player, runtime, false);
         this.removeEffectIfPresent(player, MobEffects.ABSORPTION);
         this.removeEffectIfPresent(player, MobEffects.MOVEMENT_SPEED);
         boolean energyChanged = Math.abs(previousEnergy - data.getEnergy()) > 0.001F;
         boolean energySyncDue = energyChanged
            && (
               Math.abs(previousEnergy - data.getEnergy()) >= NanosuitServerConfig.energySyncStep()
                  || player.tickCount % NanosuitServerConfig.energySyncIntervalTicks() == 0
                  || data.getEnergy() <= 0.0F
                  || data.getEnergy() >= 99.999F
            );
         if (previousMode != data.getMode() || previousSprintBoostReady != data.isSprintBoostReady() || energySyncDue) {
            this.syncToClient(player);
         }
      } else {
         float standingDrain = NanosuitServerConfig.standingDrainPerTick();
         float walkingDrain = NanosuitServerConfig.walkingDrainPerTick();
         float sprintingDrain = NanosuitServerConfig.sprintingDrainPerTick();
         int regenDelayAfterDrainTicks = NanosuitServerConfig.regenDelayAfterDrainTicks();
         float regenPerTick = NanosuitServerConfig.energyRegenPerTick();
         int effectDurationTicks = NanosuitServerConfig.effectDurationTicks();
         int effectRefreshThresholdTicks = NanosuitServerConfig.effectRefreshThresholdTicks();
         int armorAbsorptionAmplifier = NanosuitServerConfig.armorAbsorptionAmplifier();
         int sprintSpeedAmplifier = NanosuitServerConfig.sprintSpeedAmplifier();
         float energySyncStep = NanosuitServerConfig.energySyncStep();
         int energySyncInterval = NanosuitServerConfig.energySyncIntervalTicks();
         NanosuitMode mode = data.getMode();
         boolean sprintDrainRequested = data.getEnergy() > 0.0F && player.isAlive() && !player.isSpectator()
            && player.tickCount >= runtime.powerSprintCooldownUntilTick
            && (landSprinting && data.isSprintBoostReady() && rulesProfile.controls().supports(Ability.POWER_SPRINT)
               || NanosuitSwimBoost.isSwimmingSprint(player) && rulesProfile.controls().supports(Ability.POWER_SWIM));
         boolean hasActiveMode = mode != NanosuitMode.NONE;
         NanosuitPlayerHandler.DrainState drainState = hasActiveMode
            ? NanosuitPlayerHandler.DrainState.MODE_ACTIVE
            : (sprintDrainRequested ? NanosuitPlayerHandler.DrainState.BASE_SPRINT : NanosuitPlayerHandler.DrainState.NONE);
         boolean activeDrainSource = drainState != NanosuitPlayerHandler.DrainState.NONE || nanovisionActive;
         boolean movingNow = this.isMoving(player, runtime);
         int sprintSpeedAmplifierToApply = hasActiveMode ? 0 : sprintSpeedAmplifier;
         float baselineDrain = hasActiveMode
            ? this.getModeDrainPerTick(rulesProfile.combat(), mode, sprintDrainRequested, movingNow)
            : this.getBaseDrainPerTick(sprintDrainRequested, movingNow, standingDrain, walkingDrain, sprintingDrain);
         float drain = (drainState != NanosuitPlayerHandler.DrainState.NONE ? baselineDrain : 0.0F)
            + (nanovisionActive ? NanosuitServerConfig.nanovisionDrainPerTick() : 0.0F);
         if (creativeInfiniteEnergy) {
            data.setEnergy(100.0F);
            data.setRegenDelayTicks(0);
         } else if (drainState != NanosuitPlayerHandler.DrainState.NONE) {
            data.consumeEnergy(drain);
            data.setRegenDelayTicks(regenDelayAfterDrainTicks);
         } else {
            int regenDelayTicks = data.getRegenDelayTicks();
            if (regenDelayTicks > 0) {
               data.setRegenDelayTicks(regenDelayTicks - 1);
            } else if (mode == NanosuitMode.NONE) {
               data.addEnergy(regenPerTick - baselineDrain);
            }
         }

         if (data.getEnergy() <= 0.0F) {
            data.setMode(NanosuitMode.NONE);
            data.setSprintBoostReady(false);
            mode = NanosuitMode.NONE;
            runtime.movingHoldTicks = 0;
         }

         boolean powerSprintActive = sprintDrainRequested && data.getEnergy() > 0.0F && !data.isRecoveryLocked();
         updatePowerSprint(player, data, runtime, rulesProfile, powerSprintActive);
         updateSustainedPower(player, runtime, powerSprintActive && baselineDrain > 0.0F && !creativeInfiniteEnergy);

         updateCloakVisibility(player, runtime, mode == NanosuitMode.CLOAK && data.getEnergy() > 0.0F);

         if (mode == NanosuitMode.ARMOR && data.getEnergy() > 0.0F) {
            this.applyOrRefreshEffect(player, MobEffects.ABSORPTION, effectDurationTicks, armorAbsorptionAmplifier, effectRefreshThresholdTicks);
         } else {
            this.removeEffectIfPresent(player, MobEffects.ABSORPTION);
         }

         if (powerSprintActive) {
            this.applyOrRefreshEffect(player, MobEffects.MOVEMENT_SPEED, effectDurationTicks, sprintSpeedAmplifierToApply, effectRefreshThresholdTicks);
         } else {
            this.removeEffectIfPresent(player, MobEffects.MOVEMENT_SPEED);
         }

         boolean shouldSyncByMode = previousMode != data.getMode() || previousSprintBoostReady != data.isSprintBoostReady();
         boolean reachedFullThisTick = previousEnergy < 99.999F && data.getEnergy() >= 99.999F;
         float energyDeltaFromPrevious = Math.abs(previousEnergy - data.getEnergy());
         NanosuitSyncManager.SyncedState lastState = NanosuitSyncManager.getLastSyncedState(player.getUUID());
         float energyFromLastSync = lastState == null ? previousEnergy : lastState.energy();
         boolean shouldSyncByEnergyDelta = Math.abs(energyFromLastSync - data.getEnergy()) >= energySyncStep;
         boolean shouldSyncByHeartbeat = player.tickCount % energySyncInterval == 0 && energyDeltaFromPrevious > 0.01F;
         boolean shouldSyncByActiveDrainCadence = activeDrainSource && energyDeltaFromPrevious > 0.0F && player.tickCount % 2 == 0;
         if (shouldSyncByMode || reachedFullThisTick || shouldSyncByEnergyDelta || shouldSyncByHeartbeat || shouldSyncByActiveDrainCadence) {
            this.syncToClient(player);
         }
      }
      tickHealthRecovery(player, data, runtime, rulesProfile.energy().healing(), hasFullSet);
      if (player instanceof ServerPlayer serverPlayer && hasFullSet) {
         NanosuitEnergyRules.CriticalWarning warning = rulesProfile.energy().criticalWarning();
         if (!runtime.energyCriticalWarned && data.getEnergy() > 0.0F && data.getEnergy() <= warning.threshold()) {
            runtime.energyCriticalWarned = true;
            sendEnergyCue(serverPlayer, Cue.CRITICAL, 0.72F);
         } else if (data.getEnergy() > warning.rearmThreshold()) {
            runtime.energyCriticalWarned = false;
         }
      }
   }

   private void tickRecovery(ServerPlayer player, INanosuitEnergy data, PlayerRuntimeState runtime, NanosuitProfile profile, boolean hasFullSet) {
      updatePowerSprint(player, data, runtime, profile, false);
      updateSustainedPower(player, runtime, false);
      int tick = data.getRecoveryTicks();
      runtime.movingHoldTicks = 0;
      runtime.nextHealthRecoveryTick = -1;
      runtime.energyCriticalWarned = true;
      updateCloakVisibility(player, runtime, false);
      this.removeEffectIfPresent(player, MobEffects.ABSORPTION);
      this.removeEffectIfPresent(player, MobEffects.MOVEMENT_SPEED);

      NanosuitEnergyRules.Recovery recovery = profile.energy().recovery();
      int chargeStartTick = recovery.chargeStartTick();
      int waitTime = tick - recovery.firstWaitTick();
      if (hasFullSet) {
         if (tick == 0) {
            sendEnergyCue(player, Cue.DISABLED, 0.8F);
         } else if (waitTime >= 0 && waitTime < recovery.waitPulseCount() * recovery.waitIntervalTicks()
               && waitTime % recovery.waitIntervalTicks() == 0) {
            int pulse = waitTime / recovery.waitIntervalTicks();
            sendEnergyCue(player, Cue.WAIT, 0.7F - pulse * 0.1F);
         } else if (tick == chargeStartTick) {
            sendEnergyCue(player, Cue.CHARGE, 0.65F);
         }
      }

      if (tick >= chargeStartTick + recovery.chargeTicks()) {
         data.setEnergy(100.0F);
         data.setRegenDelayTicks(0);
         this.syncToClient(player);
         if (hasFullSet) {
            sendEnergyCue(player, Cue.RESTORED, 0.72F);
         }
      } else {
         if (tick >= chargeStartTick) {
            data.setEnergy(100.0F * (tick - chargeStartTick) / recovery.chargeTicks());
         }
         data.setRecoveryTicks(tick + 1);
         if (tick == 0 || tick >= chargeStartTick) {
            this.syncToClient(player);
         }
      }
   }

   private static void updatePowerSprint(Player player, INanosuitEnergy data, PlayerRuntimeState runtime, NanosuitProfile profile, boolean active) {
      if (runtime.powerSprintActive && !active) {
         runtime.powerSprintCooldownUntilTick = player.tickCount + profile.mobility().sprintCooldownTicks();
         data.setSprintBoostReady(false);
      }
      runtime.powerSprintActive = active;
      if (player instanceof ServerPlayer serverPlayer) {
         NanosuitSwimBoost.update(serverPlayer, active && profile.controls().supports(Ability.POWER_SWIM), profile.mobility().swimSpeedBonus());
      }
   }

   public static void requestPowerSprint(ServerPlayer player) {
      INanosuitEnergy data = NanosuitCapabilities.get(player);
      PlayerRuntimeState runtime = runtimeState(player.getUUID());
      if (runtime.landSprintingLastTick && isLandSprinting(player)
         && player.tickCount >= runtime.powerSprintCooldownUntilTick && !data.isSprintBoostReady()
         && !data.isRecoveryLocked() && data.getEnergy() > 0.0F && hasPowerSprintProfile(player)) {
         data.setSprintBoostReady(true);
         NanosuitSyncManager.syncToClient(player);
      }
   }

   public static boolean isLandPowerSprintActive(Player player) {
      if (!isLandSprinting(player) || !hasPowerSprintProfile(player)) {
         return false;
      }
      INanosuitEnergy data = NanosuitCapabilities.get(player);
      return data.isSprintBoostReady() && data.getEnergy() > 0.0F && !data.isRecoveryLocked();
   }

   private static boolean hasPowerSprintProfile(Player player) {
      NanosuitProfile profile = NanosuitArmorUtil.getEquippedProfile(player);
      return profile != null && profile.controls().supports(Ability.POWER_SPRINT);
   }

   private static boolean isLandSprinting(Player player) {
      return player.isSprinting() && player.isAlive() && !player.isSpectator() && !player.isInWaterOrBubble()
         && !player.getAbilities().flying && !player.isFallFlying() && !player.isPassenger();
   }

   private static void tickHealthRecovery(
      Player player, INanosuitEnergy data, PlayerRuntimeState runtime, NanosuitEnergyRules.Healing healing, boolean hasFullSet
   ) {
      if (!hasFullSet || data.getEnergy() <= 0.0F || data.isRecoveryLocked() || !player.isAlive()
         || player.isSpectator() || player.isCreative() || player.getHealth() >= player.getMaxHealth()) {
         runtime.nextHealthRecoveryTick = -1;
      } else if (runtime.nextHealthRecoveryTick < 0) {
         runtime.nextHealthRecoveryTick = player.tickCount + healing.delayTicks();
      } else if (player.tickCount >= runtime.nextHealthRecoveryTick) {
         player.heal(healing.amountHp());
         runtime.nextHealthRecoveryTick = player.tickCount + healing.intervalTicks();
      }
   }

   private static void updateSustainedPower(Player player, PlayerRuntimeState runtime, boolean active) {
      if (player instanceof ServerPlayer serverPlayer && runtime.sustainedPower != active) {
         runtime.sustainedPower = active;
         NanosuitNetwork.sendToPlayer(serverPlayer, new SyncPowerFeedbackPacket(active
            ? SyncPowerFeedbackPacket.Feedback.SUSTAINED_START : SyncPowerFeedbackPacket.Feedback.SUSTAINED_STOP));
      }
   }

   private static void sendEnergyCue(ServerPlayer player, Cue cue, float volume) {
      NanosuitNetwork.sendToPlayer(player, new SyncSuitEnergySoundPacket(cue, volume));
   }

   public static boolean canSpendSuitEnergy(ServerPlayer player, float amount) {
      INanosuitEnergy data = NanosuitCapabilities.get(player);
      return Float.isFinite(amount) && amount > 0.0F && NanosuitArmorUtil.hasFullSet(player)
         && !data.isRecoveryLocked() && data.getEnergy() >= amount;
   }

   public static boolean trySpendSuitEnergy(ServerPlayer player, float amount) {
      if (!canSpendSuitEnergy(player, amount)) {
         return false;
      }
      if (!hasInfiniteCreativeEnergy(player)) {
         INanosuitEnergy data = NanosuitCapabilities.get(player);
         data.consumeEnergy(amount);
         data.setRegenDelayTicks(NanosuitServerConfig.regenDelayAfterDrainTicks());
         NanosuitSyncManager.syncToClient(player);
      }
      return true;
   }

   public static void applyCloakCombatCost(ServerPlayer player, float amount, boolean breakCloak) {
      if (NanosuitCloak.isActive(player)) {
         applyActiveCloakCombatCost(player, amount, breakCloak);
      }
   }

   static void applyActiveCloakCombatCost(ServerPlayer player, float amount, boolean breakCloak) {
      INanosuitEnergy data = NanosuitCapabilities.get(player);
      data.consumeEnergy(amount);
      data.setRegenDelayTicks(NanosuitServerConfig.regenDelayAfterDrainTicks());
      if (breakCloak || data.isRecoveryLocked()) {
         data.setMode(NanosuitMode.NONE);
         updateCloakVisibility(player, runtimeState(player.getUUID()), false);
      }
      NanosuitSyncManager.syncToClient(player);
   }

   public static boolean tryToggleMode(ServerPlayer player, NanosuitMode requestedMode) {
      INanosuitEnergy data = NanosuitCapabilities.get(player);
      PlayerRuntimeState runtime = runtimeState(player.getUUID());
      NanosuitProfile profile = NanosuitArmorUtil.getEquippedProfile(player);
      if (profile == null || data.isRecoveryLocked()
            || player.tickCount < runtime.modeCooldownUntilTick || !profile.controls().allowsMode(requestedMode)) {
         return false;
      }
      NanosuitMode nextMode = data.getMode() == requestedMode ? NanosuitMode.NONE : requestedMode;
      if (nextMode == data.getMode()) {
         return false;
      }
      data.setMode(nextMode);
      runtime.modeCooldownUntilTick = player.tickCount + profile.modeCooldownTicks();
      NanosuitSyncManager.syncToClient(player);
      return true;
   }

   private static boolean hasInfiniteCreativeEnergy(Player player) {
      return player.getAbilities().instabuild;
   }

   private static void updateCloakVisibility(Player player, PlayerRuntimeState runtime, boolean cloaked) {
      if (cloaked) {
         player.setInvisible(true);
      } else if (runtime.cloakVisibilityApplied) {
         player.setInvisible(player.hasEffect(MobEffects.INVISIBILITY));
      }
      runtime.cloakVisibilityApplied = cloaked;
   }

   static boolean isMovingForCloak(ServerPlayer player) {
      PlayerRuntimeState runtime = PLAYER_RUNTIME_STATE.get(player.getUUID());
      return runtime != null && runtime.movingHoldTicks > 0;
   }

   static void recordCloakCombatNoise(ServerPlayer player, int durationTicks) {
      runtimeState(player.getUUID()).cloakCombatNoiseUntilTick = player.tickCount + durationTicks;
   }

   static boolean hasCloakCombatNoise(ServerPlayer player) {
      PlayerRuntimeState runtime = PLAYER_RUNTIME_STATE.get(player.getUUID());
      return runtime != null && player.tickCount < runtime.cloakCombatNoiseUntilTick;
   }

   public static boolean canChargeEnhancedJump(Player player) {
      return availablePowerJump(player) != null;
   }

   public static NanosuitMobilityRules.Jump availablePowerJump(Player player) {
      if (!player.isAlive() || !player.onGround() || player.isSpectator() || player.getAbilities().flying
            || player.isFallFlying() || player.isSwimming() || player.isInWaterOrBubble()
            || player.onClimbable() || player.isPassenger()) {
         return null;
      }
      NanosuitProfile profile = NanosuitArmorUtil.getEquippedProfile(player);
      if (profile == null || !profile.controls().supports(Ability.POWER_JUMP)) {
         return null;
      }
      INanosuitEnergy data = NanosuitCapabilities.get(player);
      NanosuitMobilityRules.Jump jump = profile.mobility().jump();
      return !data.isRecoveryLocked() && data.getEnergy() >= jump.energyCost() ? jump : null;
   }

   public static void beginEnhancedJumpCharge(ServerPlayer player) {
      PlayerRuntimeState runtime = runtimeState(player.getUUID());
      if (runtime.enhancedJumpChargeTick < 0 && player.tickCount >= runtime.enhancedJumpCooldownUntilTick) {
         NanosuitMobilityRules.Jump jump = availablePowerJump(player);
         if (jump != null) {
            runtime.enhancedJumpChargeTick = player.tickCount;
            runtime.enhancedJumpChargeRules = jump;
         }
      }
   }

   public static void cancelEnhancedJumpCharge(ServerPlayer player) {
      PlayerRuntimeState runtime = runtimeState(player.getUUID());
      runtime.enhancedJumpChargeTick = -1;
      runtime.enhancedJumpChargeRules = null;
      runtime.enhancedJumpRequested = false;
   }

   public static void requestChargedEnhancedJump(ServerPlayer player) {
      PlayerRuntimeState runtime = runtimeState(player.getUUID());
      if (runtime.enhancedJumpChargeTick >= 0) {
         runtime.enhancedJumpRequested = true;
      }
   }

   private static void tickEnhancedJumpCharge(ServerPlayer player) {
      PlayerRuntimeState runtime = runtimeState(player.getUUID());
      if (runtime.enhancedJumpChargeTick < 0) {
         return;
      }
      int heldTicks = player.tickCount - runtime.enhancedJumpChargeTick;
      NanosuitMobilityRules.Jump jump = runtime.enhancedJumpChargeRules;
      if (heldTicks > jump.chargeTimeoutTicks() || availablePowerJump(player) != jump) {
         cancelEnhancedJumpCharge(player);
      } else if (runtime.enhancedJumpRequested && heldTicks >= jump.holdTicks()) {
         cancelEnhancedJumpCharge(player);
         tryPerformEnhancedJump(player);
      }
   }

   private static void tickEnhancedJumpMantle(ServerPlayer player) {
      PlayerRuntimeState runtime = runtimeState(player.getUUID());
      if (runtime.enhancedJumpMantleUntilTick <= player.tickCount && runtime.enhancedJumpMantlePlan == null) {
         return;
      }
      Vec3 position = player.position();
      Vec3 movement = position.subtract(runtime.enhancedJumpLastPosition);
      runtime.enhancedJumpLastPosition = position;
      if ((player.onGround() && player.tickCount >= runtime.enhancedJumpCooldownUntilTick)
            || !player.isAlive() || player.isSpectator() || player.getAbilities().flying
            || player.isFallFlying() || player.isInWaterOrBubble() || player.onClimbable() || player.isPassenger()
            || !NanosuitArmorUtil.hasFullSet(player) || NanosuitCapabilities.get(player).isRecoveryLocked()) {
         runtime.enhancedJumpMantleUntilTick = 0;
         runtime.enhancedJumpMantlePlan = null;
         return;
      }
      if (runtime.enhancedJumpMantlePlan != null) {
         if (!EnhancedJumpMantle.guideToLanding(player, runtime.enhancedJumpMantlePlan)) {
            runtime.enhancedJumpMantlePlan = null;
         }
         return;
      }
      if (movement.horizontalDistanceSqr() > 0.0025) {
         runtime.enhancedJumpMantleDirection = new Vec3(movement.x, 0.0, movement.z).normalize();
         runtime.enhancedJumpMotionUntilTick = player.tickCount + 4;
      }
      if (player.onGround() || position.y - runtime.enhancedJumpStartY < 0.9 || player.fallDistance >= 0.5F) {
         return;
      }
      // Facing still provides a usable direction when the player is already pressed against the wall.
      Vec3 facing = Vec3.directionFromRotation(0.0F, player.getYRot());
      EnhancedJumpMantle.Plan plan = null;
      if (player.tickCount < runtime.enhancedJumpMotionUntilTick && runtime.enhancedJumpMantleDirection.dot(facing) > 0.25) {
         plan = EnhancedJumpMantle.tryLift(player, runtime.enhancedJumpMantleDirection);
      }
      if (plan == null) {
         plan = EnhancedJumpMantle.tryLift(player, facing);
      }
      if (plan != null) {
         runtime.enhancedJumpMantleUntilTick = 0;
         runtime.enhancedJumpMantlePlan = plan;
      }
   }

   private static boolean tryPerformEnhancedJump(ServerPlayer player) {
      if (!player.onGround()) {
         tracePowerJumpRejection(player, "not_on_ground");
         return false;
      }
      NanosuitProfile profile = NanosuitArmorUtil.getEquippedProfile(player);
      if (profile == null || !profile.controls().supports(Ability.POWER_JUMP)) {
         tracePowerJumpRejection(player, "no_full_suit");
         return false;
      } else if (player.getAbilities().flying) {
         tracePowerJumpRejection(player, "flying");
         return false;
      } else if (player.isFallFlying()) {
         tracePowerJumpRejection(player, "elytra");
         return false;
      } else if (player.isSwimming()) {
         tracePowerJumpRejection(player, "swimming");
         return false;
      } else if (player.onClimbable()) {
         tracePowerJumpRejection(player, "climbing");
         return false;
      } else if (player.isPassenger()) {
         tracePowerJumpRejection(player, "passenger");
         return false;
      } else {
         NanosuitPlayerHandler.PlayerRuntimeState runtime = runtimeState(player.getUUID());
         if (player.tickCount < runtime.enhancedJumpCooldownUntilTick) {
            tracePowerJumpRejection(player, "cooldown");
            return false;
         } else {
            NanosuitMobilityRules.Jump jump = profile.mobility().jump();
            float energyCost = jump.energyCost();
            return java.util.Optional.<INanosuitEnergy>of(NanosuitCapabilities.get(player)).map(data -> {
               if (data.isRecoveryLocked() || data.getEnergy() < energyCost) {
                  tracePowerJumpRejection(player, "insufficient_energy");
                  return false;
               } else {
                  if (hasInfiniteCreativeEnergy(player)) {
                     data.setEnergy(100.0F);
                  } else {
                     data.consumeEnergy(energyCost);
                     data.setRegenDelayTicks(NanosuitServerConfig.regenDelayAfterDrainTicks());
                  }

                  player.setDeltaMovement(player.getDeltaMovement().x, jump.verticalVelocity(), player.getDeltaMovement().z);
                  player.setOnGround(false);
                  player.hasImpulse = true;
                  runtime.enhancedJumpCooldownUntilTick = player.tickCount + jump.cooldownTicks();
                  runtime.enhancedJumpFallProtectionActive = true;
                  runtime.enhancedJumpMantleUntilTick = player.tickCount + 24;
                  runtime.enhancedJumpMantlePlan = null;
                  runtime.enhancedJumpMotionUntilTick = 0;
                  runtime.enhancedJumpStartY = player.getY();
                  runtime.enhancedJumpLastPosition = player.position();
                  NanosuitSyncManager.syncToClient(player);
                  NanosuitNetwork.sendToPlayer(player, new SyncEnhancedJumpPacket(jump.verticalVelocity()));
                  tracePowerJumpAccepted(player, data, energyCost);
                  return true;
               }
            }).orElse(false);
         }
      }
   }

   public static void startPowerJumpDebug(ServerPlayer player) {
      POWER_JUMP_DEBUG_SESSIONS.put(player.getUUID(), new NanosuitPlayerHandler.PowerJumpDebugSession());
      LOGGER.info("[PowerJumpTest] START player={}", player.getGameProfile().getName());
   }

   public static boolean stopPowerJumpDebug(ServerPlayer player) {
      boolean removed = POWER_JUMP_DEBUG_SESSIONS.remove(player.getUUID()) != null;
      LOGGER.info("[PowerJumpTest] STOP player={} active={}", player.getGameProfile().getName(), removed);
      return removed;
   }

   private static void tracePowerJumpRejection(ServerPlayer player, String reason) {
      if (POWER_JUMP_DEBUG_SESSIONS.containsKey(player.getUUID())) {
         LOGGER.info("[PowerJumpTest] REQUEST_REJECTED reason={} {}", reason, describePowerJumpState(player, null));
      }
   }

   private static void tracePowerJumpAccepted(ServerPlayer player, INanosuitEnergy data, float energyCost) {
      NanosuitPlayerHandler.PowerJumpDebugSession session = POWER_JUMP_DEBUG_SESSIONS.get(player.getUUID());
      if (session != null) {
         session.jumpSamplesRemaining = 40;
         LOGGER.info("[PowerJumpTest] REQUEST_ACCEPTED cost={} {}", energyCost, describePowerJumpState(player, data));
      }
   }

   private static void tracePowerJumpTick(Player player, INanosuitEnergy data) {
      NanosuitPlayerHandler.PowerJumpDebugSession session = POWER_JUMP_DEBUG_SESSIONS.get(player.getUUID());
      if (session != null && session.jumpSamplesRemaining > 0) {
         int sample = 41 - session.jumpSamplesRemaining--;
         LOGGER.info("[PowerJumpTest] AIR_TICK sample={} {}", sample, describePowerJumpState(player, data));
      }
   }

   private static String describePowerJumpState(Player player, INanosuitEnergy data) {
      return String.format(
         Locale.ROOT,
         "player=%s tick=%d ground=%s crouch=%s shift=%s sprint=%s input=[xxa=%.3f,zza=%.3f] velocity=[x=%.4f,y=%.4f,z=%.4f,h=%.4f] fall=%.3f energy=%s",
         player.getGameProfile().getName(),
         player.tickCount,
         player.onGround(),
         player.isCrouching(),
         player.isShiftKeyDown(),
         player.isSprinting(),
         player.xxa,
         player.zza,
         player.getDeltaMovement().x,
         player.getDeltaMovement().y,
         player.getDeltaMovement().z,
         player.getDeltaMovement().horizontalDistance(),
         player.fallDistance,
         data == null ? "n/a" : String.format(Locale.ROOT, "%.3f", data.getEnergy())
      );
   }

   public static float energyPerHudSegment() {
      return 5.0F;
   }

   public static float enhancedJumpEnergyCost() {
      return NanosuitProfile.LEGACY_CURRENT.enhancedJumpEnergyCost();
   }

   private float getModeDrainPerTick(NanosuitCombatRules combat, NanosuitMode mode, boolean sprintDrainRequested, boolean movingNow) {
      if (mode == NanosuitMode.CLOAK) {
         return combat.cloak().drain().perTick(sprintDrainRequested, movingNow);
      }
      if (mode == NanosuitMode.ARMOR) {
         return combat.armorDrain().perTick(sprintDrainRequested, movingNow);
      }
      return combat.armorDrain().standing();
   }

   private float getBaseDrainPerTick(boolean sprintDrainRequested, boolean movingNow, float standingDrain, float walkingDrain, float sprintingDrain) {
      if (sprintDrainRequested) {
         return sprintingDrain;
      } else {
         return movingNow ? walkingDrain : standingDrain;
      }
   }

   private boolean isMoving(Player player, NanosuitPlayerHandler.PlayerRuntimeState runtime) {
      long start = profilingEnabled ? System.nanoTime() : 0L;
      boolean hasTrackedPositionDelta = false;
      if (runtime.hasLastPos) {
         double trackedDx = player.getX() - runtime.lastX;
         double trackedDz = player.getZ() - runtime.lastZ;
         hasTrackedPositionDelta = trackedDx * trackedDx + trackedDz * trackedDz > 6.25E-4;
      }

      runtime.lastX = player.getX();
      runtime.lastZ = player.getZ();
      runtime.hasLastPos = true;
      boolean hasWalkStepDelta = Math.abs(player.walkDist - player.walkDistO) > 0.001F;
      boolean hasMoveInput = Math.abs(player.xxa) > 0.01F || Math.abs(player.zza) > 0.01F;
      double dx = player.getX() - player.xo;
      double dz = player.getZ() - player.zo;
      boolean hasPositionDelta = dx * dx + dz * dz > 6.25E-4;
      boolean hasVelocityDelta = player.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4;
      boolean movingRaw = hasWalkStepDelta || hasMoveInput || hasTrackedPositionDelta || hasPositionDelta || hasVelocityDelta;
      boolean result;
      if (movingRaw) {
         runtime.movingHoldTicks = 4;
         result = true;
      } else {
         int holdTicks = runtime.movingHoldTicks;
         if (holdTicks <= 0) {
            result = false;
         } else {
            int next = holdTicks - 1;
            if (next <= 0) {
               runtime.movingHoldTicks = 0;
               result = false;
            } else {
               runtime.movingHoldTicks = next;
               result = true;
            }
         }
      }

      if (profilingEnabled) {
         long elapsed = System.nanoTime() - start;
         profilingMovingCalls++;
         profilingMovingTotalNanos += elapsed;
         if (result) {
            profilingMovingTrueCalls++;
         }

         if (elapsed > profilingMovingMaxNanos) {
            profilingMovingMaxNanos = elapsed;
         }
      }

      return result;
   }

   private boolean isArmorDamageBlockable(DamageSource source) {
      if (source == null) {
         return false;
      } else if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         return false;
      } else {
         return source.is(DamageTypes.FELL_OUT_OF_WORLD) ? false : !source.is(DamageTypes.DROWN) && !source.is(DamageTypes.STARVE);
      }
   }

   private void applyOrRefreshEffect(Player player, net.minecraft.core.Holder<MobEffect> effect, int duration, int amplifier, int refreshThresholdTicks) {
      MobEffectInstance current = player.getEffect(effect);
      if (current == null || current.getAmplifier() != amplifier || current.getDuration() <= refreshThresholdTicks) {
         player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, false, false));
      }
   }

   private void removeEffectIfPresent(Player player, net.minecraft.core.Holder<MobEffect> effect) {
      if (player.hasEffect(effect)) {
         player.removeEffect(effect);
      }
   }

   private void ensurePassiveSpeed(Player player) {
      AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
      if (movementSpeed != null && movementSpeed.getModifier(PASSIVE_SPEED_MODIFIER_ID) == null) {
         movementSpeed.addTransientModifier(
            new AttributeModifier(PASSIVE_SPEED_MODIFIER_ID, NanosuitServerConfig.passiveSpeedBonus(), Operation.ADD_MULTIPLIED_TOTAL)
         );
      }
   }

   private void syncToClient(Player player) {
      if (player instanceof ServerPlayer serverPlayer) {
         NanosuitSyncManager.syncToClient(serverPlayer);
      }
   }

   private static NanosuitPlayerHandler.PlayerRuntimeState runtimeState(UUID playerId) {
      return PLAYER_RUNTIME_STATE.computeIfAbsent(playerId, id -> new NanosuitPlayerHandler.PlayerRuntimeState());
   }

   private static void resetRuntimeStateForPlayer(UUID playerId) {
      NanosuitPlayerHandler.PlayerRuntimeState runtime = runtimeState(playerId);
      runtime.hasLastPos = false;
      runtime.lastX = 0.0;
      runtime.lastZ = 0.0;
      runtime.movingHoldTicks = 0;
      runtime.powerSprintActive = false;
      runtime.powerSprintCooldownUntilTick = 0;
      runtime.nextHealthRecoveryTick = -1;
      runtime.landSprintingLastTick = false;
      runtime.enhancedJumpCooldownUntilTick = 0;
      runtime.enhancedJumpChargeTick = -1;
      runtime.enhancedJumpChargeRules = null;
      runtime.enhancedJumpRequested = false;
      runtime.enhancedJumpMantleUntilTick = 0;
      runtime.enhancedJumpMantlePlan = null;
      runtime.modeCooldownUntilTick = 0;
      runtime.enhancedJumpFallProtectionActive = false;
   }

   private static boolean consumeEnhancedJumpFallProtection(Player player) {
      NanosuitPlayerHandler.PlayerRuntimeState runtime = PLAYER_RUNTIME_STATE.get(player.getUUID());
      if (runtime != null && runtime.enhancedJumpFallProtectionActive) {
         runtime.enhancedJumpFallProtectionActive = false;
         return true;
      } else {
         return false;
      }
   }

   public static void setProfilingEnabled(boolean enabled) {
      profilingEnabled = enabled;
   }

   public static boolean isProfilingEnabled() {
      return profilingEnabled;
   }

   public static void resetProfiling() {
      profilingTickCalls = 0L;
      profilingTickTotalNanos = 0L;
      profilingTickMaxNanos = 0L;
      profilingMovingCalls = 0L;
      profilingMovingTrueCalls = 0L;
      profilingMovingTotalNanos = 0L;
      profilingMovingMaxNanos = 0L;
   }

   public static NanosuitPlayerHandler.ProfilingSnapshot profilingSnapshot() {
      double avgTickMicros = profilingTickCalls == 0L ? 0.0 : (double)profilingTickTotalNanos / profilingTickCalls / 1000.0;
      double avgMovingMicros = profilingMovingCalls == 0L ? 0.0 : (double)profilingMovingTotalNanos / profilingMovingCalls / 1000.0;
      double movingTrueRatio = profilingMovingCalls == 0L ? 0.0 : profilingMovingTrueCalls * 100.0 / profilingMovingCalls;
      return new NanosuitPlayerHandler.ProfilingSnapshot(
         profilingEnabled,
         profilingTickCalls,
         avgTickMicros,
         profilingTickMaxNanos / 1000.0,
         profilingMovingCalls,
         avgMovingMicros,
         profilingMovingMaxNanos / 1000.0,
         movingTrueRatio
      );
   }

   private static enum DrainState {
      NONE,
      BASE_SPRINT,
      MODE_ACTIVE;
   }

   private static final class PlayerRuntimeState {
      private double lastX;
      private double lastZ;
      private boolean hasLastPos;
      private int movingHoldTicks;
      private boolean powerSprintActive;
      private boolean landSprintingLastTick;
      private int powerSprintCooldownUntilTick;
      private int nextHealthRecoveryTick = -1;
      private boolean sustainedPower;
      private int enhancedJumpCooldownUntilTick;
      private int enhancedJumpChargeTick = -1;
      private NanosuitMobilityRules.Jump enhancedJumpChargeRules;
      private boolean enhancedJumpRequested;
      private int enhancedJumpMantleUntilTick;
      private EnhancedJumpMantle.Plan enhancedJumpMantlePlan;
      private int enhancedJumpMotionUntilTick;
      private double enhancedJumpStartY;
      private Vec3 enhancedJumpLastPosition = Vec3.ZERO;
      private Vec3 enhancedJumpMantleDirection = Vec3.ZERO;
      private boolean enhancedJumpFallProtectionActive;
      private boolean cloakVisibilityApplied;
      private int cloakCombatNoiseUntilTick;
      private int modeCooldownUntilTick;
      private boolean energyCriticalWarned;
   }

   private static final class PowerJumpDebugSession {
      private int jumpSamplesRemaining;
   }

   public record ProfilingSnapshot(
      boolean enabled,
      long tickCalls,
      double tickAvgMicros,
      double tickMaxMicros,
      long isMovingCalls,
      double isMovingAvgMicros,
      double isMovingMaxMicros,
      double movingTrueRatioPercent
   ) {
   }
}
