package io.github.painworks.crysismod.nanosuit.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.config.ModConfigEvent.Loading;
import net.neoforged.fml.event.config.ModConfigEvent.Reloading;

@EventBusSubscriber(modid = "crysismod", bus = Bus.MOD)
public final class NanosuitServerConfig {
   private static final Builder BUILDER = new Builder();
   private static final double DEFAULT_ENERGY_REGEN_PER_TICK = 1.2;
   private static final double DEFAULT_STANDING_DRAIN_PER_TICK = 0.05;
   private static final double DEFAULT_WALKING_DRAIN_PER_TICK = 0.25;
   private static final double DEFAULT_SPRINTING_DRAIN_PER_TICK = 0.75;
   private static final double DEFAULT_ACTIVE_MODE_DRAIN_PER_TICK = 0.5;
   private static final double DEFAULT_NANOVISION_DRAIN_PER_TICK = 0.08333333333333333;
   private static final int DEFAULT_REGEN_DELAY_AFTER_DRAIN_TICKS = 30;
   private static final double DEFAULT_PASSIVE_SPEED_BONUS = 0.15;
   private static final double DEFAULT_ARMOR_DAMAGE_MULTIPLIER = 0.6;
   private static final int DEFAULT_EFFECT_DURATION_TICKS = 10;
   private static final int DEFAULT_EFFECT_REFRESH_THRESHOLD_TICKS = 4;
   private static final int DEFAULT_ARMOR_ABSORPTION_AMPLIFIER = 3;
   private static final int DEFAULT_SPRINT_SPEED_AMPLIFIER = 1;
   private static final double DEFAULT_ENERGY_SYNC_STEP = 1.26;
   private static final int DEFAULT_ENERGY_SYNC_INTERVAL_TICKS = 13;
   private static final double DEFAULT_ARMOR_MODE_FULL_ENERGY_BLOCK_CAPACITY_HP = 200.0;
   private static final boolean DEFAULT_DEBUG_COMMANDS_ENABLED = true;
   private static final boolean DEFAULT_DEBUG_ARMOR_DAMAGE_ABSORPTION = false;
   private static final double DEFAULT_NANOSUIT_1_WALK_BONUS = 0.10;
   private static final double DEFAULT_NANOSUIT_1_SPRINT_DRAIN_PER_TICK = 0.75;
   private static final int DEFAULT_NANOSUIT_1_SPRINT_SPEED_AMPLIFIER = 1;
   public static final DoubleValue ENERGY_REGEN_PER_TICK = BUILDER.comment("Energy regenerated per tick when regeneration is allowed.")
      .defineInRange("energy.regenPerTick", 1.2, 0.0, 100.0);
   public static final DoubleValue STANDING_DRAIN_PER_TICK = BUILDER.comment("Base energy drain per tick while standing.")
      .defineInRange("energy.drainStandingPerTick", 0.05, 0.0, 100.0);
   public static final DoubleValue WALKING_DRAIN_PER_TICK = BUILDER.comment("Base energy drain per tick while walking.")
      .defineInRange("energy.drainWalkingPerTick", 0.25, 0.0, 100.0);
   public static final DoubleValue SPRINTING_DRAIN_PER_TICK = BUILDER.comment("Base energy drain per tick while enhanced sprint is active.")
      .defineInRange("energy.drainSprintingPerTick", 0.75, 0.0, 100.0);
   public static final DoubleValue ACTIVE_MODE_DRAIN_PER_TICK = BUILDER.comment("Additional drain per tick while ARMOR or CLOAK mode is active.")
      .defineInRange("energy.drainActiveModePerTick", 0.5, 0.0, 100.0);
   public static final DoubleValue NANOVISION_DRAIN_PER_TICK = BUILDER.comment(
         "Energy drained per tick while Nanovision is active. Default is one 5-energy HUD division per 3 seconds."
      )
      .defineInRange("energy.nanovisionDrainPerTick", 0.08333333333333333, 0.0, 100.0);
   public static final IntValue REGEN_DELAY_AFTER_DRAIN_TICKS = BUILDER.comment("Delay before energy regeneration starts after any active drain.")
      .defineInRange("energy.regenDelayAfterDrainTicks", 30, 0, 600);
   public static final DoubleValue PASSIVE_SPEED_BONUS = BUILDER.comment("Permanent nanosuit movement speed bonus multiplier.")
      .defineInRange("effects.passiveSpeedBonus", 0.15, 0.0, 10.0);
   public static final DoubleValue ARMOR_DAMAGE_MULTIPLIER = BUILDER.comment("Incoming damage multiplier in ARMOR mode (lower = stronger protection).")
      .defineInRange("effects.armorDamageMultiplier", 0.6, 0.0, 10.0);
   public static final IntValue EFFECT_DURATION_TICKS = BUILDER.comment("Duration for nanosuit effects, refreshed while conditions are met.")
      .defineInRange("effects.durationTicks", 10, 1, 600);
   public static final IntValue EFFECT_REFRESH_THRESHOLD_TICKS = BUILDER.comment("Refresh effect when remaining duration is at or below this threshold.")
      .defineInRange("effects.refreshThresholdTicks", 4, 0, 600);
   public static final IntValue ARMOR_ABSORPTION_AMPLIFIER = BUILDER.comment("ARMOR mode absorption amplifier. Vanilla level shown to player is value + 1.")
      .defineInRange("effects.armorAbsorptionAmplifier", 3, 0, 255);
   public static final IntValue SPRINT_SPEED_AMPLIFIER = BUILDER.comment("Enhanced sprint speed amplifier. Vanilla level shown to player is value + 1.")
      .defineInRange("effects.sprintSpeedAmplifier", 1, 0, 255);
   public static final DoubleValue NANOSUIT_1_WALK_BONUS = BUILDER.comment(
         "Additional movement multiplier in Nanosuit 1 Speed mode while walking. 0.10 adds ten percent."
      )
      .defineInRange("nanosuit1.speed.walkBonus", DEFAULT_NANOSUIT_1_WALK_BONUS, 0.0, 1.0);
   public static final DoubleValue NANOSUIT_1_SPRINT_DRAIN_PER_TICK = BUILDER.comment(
         "Nanosuit 1 energy spent per tick of enhanced sprint or swim in Speed mode."
      )
      .defineInRange("nanosuit1.speed.sprintDrainPerTick", DEFAULT_NANOSUIT_1_SPRINT_DRAIN_PER_TICK, 0.0, 100.0);
   public static final IntValue NANOSUIT_1_SPRINT_SPEED_AMPLIFIER = BUILDER.comment(
         "Nanosuit 1 enhanced sprint speed amplifier. Vanilla level shown to player is value + 1."
      )
      .defineInRange("nanosuit1.speed.sprintSpeedAmplifier", DEFAULT_NANOSUIT_1_SPRINT_SPEED_AMPLIFIER, 0, 255);
   public static final DoubleValue ENERGY_SYNC_STEP = BUILDER.comment("Minimum energy delta to trigger immediate server->client sync.")
      .defineInRange("sync.energyStep", 1.26, 0.01, 100.0);
   public static final IntValue ENERGY_SYNC_INTERVAL_TICKS = BUILDER.comment("Heartbeat sync interval in ticks while energy changes.")
      .defineInRange("sync.energyIntervalTicks", 13, 1, 200);
   public static final DoubleValue ARMOR_MODE_FULL_ENERGY_BLOCK_CAPACITY_HP = BUILDER.comment(
         "How much incoming HP can be blocked by full nanosuit energy in ARMOR mode."
      )
      .defineInRange("effects.armorModeFullEnergyBlockCapacityHp", 200.0, 1.0, 10000.0);
   public static final BooleanValue DEBUG_COMMANDS_ENABLED = BUILDER.comment("Enable /nanosuit debug commands.").define("debug.commandsEnabled", true);
   public static final BooleanValue DEBUG_ARMOR_DAMAGE_ABSORPTION = BUILDER.comment("Log armor-mode damage absorption calculations on hit events.")
      .define("debug.armorDamageAbsorption", false);
   public static final ModConfigSpec SPEC = BUILDER.build();
   private static volatile NanosuitServerConfig.CachedValues CACHED_VALUES = NanosuitServerConfig.CachedValues.defaults();

   private NanosuitServerConfig() {
   }

   @SubscribeEvent
   public static void onConfigLoading(Loading event) {
      refreshCache(event);
   }

   @SubscribeEvent
   public static void onConfigReloading(Reloading event) {
      refreshCache(event);
   }

   private static void refreshCache(ModConfigEvent event) {
      if (event.getConfig().getSpec() == SPEC) {
         CACHED_VALUES = new NanosuitServerConfig.CachedValues(
            safeGetDouble(ENERGY_REGEN_PER_TICK, 1.2).floatValue(),
            safeGetDouble(STANDING_DRAIN_PER_TICK, 0.05).floatValue(),
            safeGetDouble(WALKING_DRAIN_PER_TICK, 0.25).floatValue(),
            safeGetDouble(SPRINTING_DRAIN_PER_TICK, 0.75).floatValue(),
            safeGetDouble(ACTIVE_MODE_DRAIN_PER_TICK, 0.5).floatValue(),
            safeGetDouble(NANOVISION_DRAIN_PER_TICK, 0.08333333333333333).floatValue(),
            safeGetInt(REGEN_DELAY_AFTER_DRAIN_TICKS, 30),
            safeGetDouble(PASSIVE_SPEED_BONUS, 0.15),
            safeGetDouble(ARMOR_DAMAGE_MULTIPLIER, 0.6).floatValue(),
            safeGetInt(EFFECT_DURATION_TICKS, 10),
            safeGetInt(EFFECT_REFRESH_THRESHOLD_TICKS, 4),
            safeGetInt(ARMOR_ABSORPTION_AMPLIFIER, 3),
            safeGetInt(SPRINT_SPEED_AMPLIFIER, 1),
            safeGetDouble(NANOSUIT_1_WALK_BONUS, DEFAULT_NANOSUIT_1_WALK_BONUS),
            safeGetDouble(NANOSUIT_1_SPRINT_DRAIN_PER_TICK, DEFAULT_NANOSUIT_1_SPRINT_DRAIN_PER_TICK).floatValue(),
            safeGetInt(NANOSUIT_1_SPRINT_SPEED_AMPLIFIER, DEFAULT_NANOSUIT_1_SPRINT_SPEED_AMPLIFIER),
            safeGetDouble(ENERGY_SYNC_STEP, 1.26).floatValue(),
            safeGetInt(ENERGY_SYNC_INTERVAL_TICKS, 13),
            safeGetDouble(ARMOR_MODE_FULL_ENERGY_BLOCK_CAPACITY_HP, 200.0).floatValue(),
            safeGetBoolean(DEBUG_COMMANDS_ENABLED, true),
            safeGetBoolean(DEBUG_ARMOR_DAMAGE_ABSORPTION, false)
         );
      }
   }

   public static float energyRegenPerTick() {
      return CACHED_VALUES.energyRegenPerTick();
   }

   public static float standingDrainPerTick() {
      return CACHED_VALUES.standingDrainPerTick();
   }

   public static float walkingDrainPerTick() {
      return CACHED_VALUES.walkingDrainPerTick();
   }

   public static float sprintingDrainPerTick() {
      return CACHED_VALUES.sprintingDrainPerTick();
   }

   public static float activeModeDrainPerTick() {
      return CACHED_VALUES.activeModeDrainPerTick();
   }

   public static float nanovisionDrainPerTick() {
      return CACHED_VALUES.nanovisionDrainPerTick();
   }

   public static int regenDelayAfterDrainTicks() {
      return CACHED_VALUES.regenDelayAfterDrainTicks();
   }

   public static double passiveSpeedBonus() {
      return CACHED_VALUES.passiveSpeedBonus();
   }

   public static float armorDamageMultiplier() {
      return CACHED_VALUES.armorDamageMultiplier();
   }

   public static int effectDurationTicks() {
      return CACHED_VALUES.effectDurationTicks();
   }

   public static int effectRefreshThresholdTicks() {
      return CACHED_VALUES.effectRefreshThresholdTicks();
   }

   public static int armorAbsorptionAmplifier() {
      return CACHED_VALUES.armorAbsorptionAmplifier();
   }

   public static int sprintSpeedAmplifier() {
      return CACHED_VALUES.sprintSpeedAmplifier();
   }

   public static double nanosuit1WalkBonus() {
      return CACHED_VALUES.nanosuit1WalkBonus();
   }

   public static float nanosuit1SprintDrainPerTick() {
      return CACHED_VALUES.nanosuit1SprintDrainPerTick();
   }

   public static int nanosuit1SprintSpeedAmplifier() {
      return CACHED_VALUES.nanosuit1SprintSpeedAmplifier();
   }

   public static float energySyncStep() {
      return CACHED_VALUES.energySyncStep();
   }

   public static int energySyncIntervalTicks() {
      return CACHED_VALUES.energySyncIntervalTicks();
   }

   public static float armorModeFullEnergyBlockCapacityHp() {
      return CACHED_VALUES.armorModeFullEnergyBlockCapacityHp();
   }

   public static boolean debugCommandsEnabled() {
      return CACHED_VALUES.debugCommandsEnabled();
   }

   public static boolean debugArmorDamageAbsorption() {
      return CACHED_VALUES.debugArmorDamageAbsorption();
   }

   public static void resetCachedValuesToDefaults() {
      CACHED_VALUES = NanosuitServerConfig.CachedValues.defaults();
   }

   private static Double safeGetDouble(DoubleValue value, double fallback) {
      try {
         return (Double)value.get();
      } catch (IllegalStateException var4) {
         return fallback;
      }
   }

   private static int safeGetInt(IntValue value, int fallback) {
      try {
         return (Integer)value.get();
      } catch (IllegalStateException var3) {
         return fallback;
      }
   }

   private static boolean safeGetBoolean(BooleanValue value, boolean fallback) {
      try {
         return (Boolean)value.get();
      } catch (IllegalStateException var3) {
         return fallback;
      }
   }

   private record CachedValues(
      float energyRegenPerTick,
      float standingDrainPerTick,
      float walkingDrainPerTick,
      float sprintingDrainPerTick,
      float activeModeDrainPerTick,
      float nanovisionDrainPerTick,
      int regenDelayAfterDrainTicks,
      double passiveSpeedBonus,
      float armorDamageMultiplier,
      int effectDurationTicks,
      int effectRefreshThresholdTicks,
      int armorAbsorptionAmplifier,
      int sprintSpeedAmplifier,
      double nanosuit1WalkBonus,
      float nanosuit1SprintDrainPerTick,
      int nanosuit1SprintSpeedAmplifier,
      float energySyncStep,
      int energySyncIntervalTicks,
      float armorModeFullEnergyBlockCapacityHp,
      boolean debugCommandsEnabled,
      boolean debugArmorDamageAbsorption
   ) {
      private static NanosuitServerConfig.CachedValues defaults() {
         return new NanosuitServerConfig.CachedValues(
            1.2F, 0.05F, 0.25F, 0.75F, 0.5F, 0.083333336F, 30, 0.15, 0.6F, 10, 4, 3, 1,
            DEFAULT_NANOSUIT_1_WALK_BONUS, (float)DEFAULT_NANOSUIT_1_SPRINT_DRAIN_PER_TICK, DEFAULT_NANOSUIT_1_SPRINT_SPEED_AMPLIFIER,
            1.26F, 13, 200.0F, true, false
         );
      }
   }
}
