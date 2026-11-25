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
   private static final IntValue CLASSIC_MODE_COOLDOWN = BUILDER.comment("Technical cooldown between Nanosuit 1 selections.")
      .defineInRange("nanosuit1.modeCooldownTicks", 2, 0, 20);
   private static final IntValue CLASSIC_REGEN_DELAY = BUILDER.comment("Provisional Nanosuit 1 recharge delay after spending energy.")
      .defineInRange("nanosuit1.energy.rechargeDelayTicks", 30, 0, 600);
   private static final DoubleValue CLASSIC_REGEN = classicRate("energy.rechargePerTick", 1.2);
   private static final DoubleValue CLASSIC_WALK = classicRate("speed.walkMultiplier", 2.0);
   private static final DoubleValue CLASSIC_CROUCH = classicRate("speed.crouchMultiplier", 2.0);
   private static final DoubleValue CLASSIC_CRAWL = classicRate("speed.crawlMultiplier", 2.0);
   private static final DoubleValue CLASSIC_SPRINT = classicRate("speed.sprintMultiplier", 2.0);
   private static final DoubleValue CLASSIC_SPRINT_DRAIN = classicRate("speed.landSprintDrainPerTick", 2.25);
   private static final DoubleValue CLASSIC_SWIM_DRAIN = classicRate("speed.swimDrainPerTick", 0.75);
   private static final DoubleValue CLASSIC_CLOAK_STANDING = classicRate("cloak.standingDrainPerTick", 0.025);
   private static final DoubleValue CLASSIC_CLOAK_PRONE = classicRate("cloak.proneDrainPerTick", 0.05625);
   private static final DoubleValue CLASSIC_CLOAK_CROUCH = classicRate("cloak.crouchDrainPerTick", 0.15);
   private static final DoubleValue CLASSIC_CLOAK_WALK = classicRate("cloak.walkDrainPerTick", 0.375);
   private static final DoubleValue CLASSIC_CLOAK_RUN = classicRate("cloak.runDrainPerTick", 0.75);
   private static final DoubleValue CLASSIC_ARMOR_CAPACITY = BUILDER.comment("Provisional incoming HP absorbed by 100 Nanosuit 1 energy.")
      .defineInRange("nanosuit1.armor.fullEnergyBlockCapacityHp", 200.0, 1.0, 10000.0);
   private static final BooleanValue CLASSIC_ARMOR_ENVIRONMENT = BUILDER.comment("Also protect against fall, drowning and other legacy-exempt sources where damage is absorbable.")
      .define("nanosuit1.armor.protectEnvironmental", false);
   private static final DoubleValue POWER_MELEE_KNOCKBACK = BUILDER.comment("Extra vanilla knockback strength on a successful powered melee hit for either suit.")
      .defineInRange("effects.powerMeleeKnockback", 0.30, 0.0, 2.0);
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
   public static final BooleanValue CRADLE_N2_FATAL_RISK = BUILDER.comment("Enable the N2 extraction death roll outside Hardcore.")
      .define("cradle.n2FatalRisk", true);
   public static final DoubleValue CRADLE_N2_FATAL_CHANCE = BUILDER.comment("Chance of a fatal N2 extraction outside Hardcore.")
      .defineInRange("cradle.n2FatalChance", 0.5, 0.0, 1.0);
   public static final BooleanValue CRADLE_RESTRICT_MANUAL_ARMOR = BUILDER.comment("Require the Cradle to equip Nanosuit pieces in Survival. Off by default.")
      .define("cradle.restrictManualArmor", false);
   public static final ModConfigSpec SPEC = BUILDER.build();
   private static volatile NanosuitServerConfig.CachedValues CACHED_VALUES = NanosuitServerConfig.CachedValues.defaults();

   private static DoubleValue classicRate(String key, double value) {
      boolean multiplier = key.endsWith("Multiplier");
      return BUILDER.defineInRange("nanosuit1." + key, value, multiplier ? 1.0 : 0.0, multiplier ? 5.0 : 100.0);
   }

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
            safeGetDouble(ENERGY_SYNC_STEP, 1.26).floatValue(),
            safeGetInt(ENERGY_SYNC_INTERVAL_TICKS, 13),
            safeGetDouble(ARMOR_MODE_FULL_ENERGY_BLOCK_CAPACITY_HP, 200.0).floatValue(),
            safeGetBoolean(DEBUG_COMMANDS_ENABLED, true),
            safeGetBoolean(DEBUG_ARMOR_DAMAGE_ABSORPTION, false)
         );
         classic = ClassicValues.load();
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

   private static volatile ClassicValues classic = ClassicValues.defaults();

   public static ClassicValues classic() {
      return classic;
   }

   public static float nanosuit1SprintDrainPerTick() {
      return classic.sprintDrain();
   }

   public static double powerMeleeKnockback() {
      return classic.meleeKnockback();
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
      classic = ClassicValues.defaults();
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

   public record ClassicValues(
      int modeCooldown, int rechargeDelay, float recharge, double walkMultiplier,
      double crouchMultiplier, double crawlMultiplier, double sprintMultiplier, float sprintDrain,
      float swimDrain, float cloakStanding, float cloakProne, float cloakCrouch,
      float cloakWalk, float cloakRun, float armorCapacity, boolean protectEnvironmental, double meleeKnockback
   ) {
      private static ClassicValues defaults() {
         return new ClassicValues(2, 30, 1.2F, 2.0, 2.0, 2.0, 2.0, 2.25F, 0.75F,
            0.025F, 0.05625F, 0.15F, 0.375F, 0.75F, 200.0F, false, 0.30);
      }

      private static ClassicValues load() {
         ClassicValues d = defaults();
         return new ClassicValues(
            safeGetInt(CLASSIC_MODE_COOLDOWN, d.modeCooldown), safeGetInt(CLASSIC_REGEN_DELAY, d.rechargeDelay),
            safeGetDouble(CLASSIC_REGEN, d.recharge).floatValue(), safeGetDouble(CLASSIC_WALK, d.walkMultiplier),
            safeGetDouble(CLASSIC_CROUCH, d.crouchMultiplier), safeGetDouble(CLASSIC_CRAWL, d.crawlMultiplier),
            safeGetDouble(CLASSIC_SPRINT, d.sprintMultiplier), safeGetDouble(CLASSIC_SPRINT_DRAIN, d.sprintDrain).floatValue(),
            safeGetDouble(CLASSIC_SWIM_DRAIN, d.swimDrain).floatValue(), safeGetDouble(CLASSIC_CLOAK_STANDING, d.cloakStanding).floatValue(),
            safeGetDouble(CLASSIC_CLOAK_PRONE, d.cloakProne).floatValue(), safeGetDouble(CLASSIC_CLOAK_CROUCH, d.cloakCrouch).floatValue(),
            safeGetDouble(CLASSIC_CLOAK_WALK, d.cloakWalk).floatValue(), safeGetDouble(CLASSIC_CLOAK_RUN, d.cloakRun).floatValue(),
            safeGetDouble(CLASSIC_ARMOR_CAPACITY, d.armorCapacity).floatValue(),
            safeGetBoolean(CLASSIC_ARMOR_ENVIRONMENT, d.protectEnvironmental), safeGetDouble(POWER_MELEE_KNOCKBACK, d.meleeKnockback)
         );
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
      float energySyncStep,
      int energySyncIntervalTicks,
      float armorModeFullEnergyBlockCapacityHp,
      boolean debugCommandsEnabled,
      boolean debugArmorDamageAbsorption
   ) {
      private static NanosuitServerConfig.CachedValues defaults() {
         return new NanosuitServerConfig.CachedValues(
            1.2F, 0.05F, 0.25F, 0.75F, 0.5F, 0.083333336F, 30, 0.15, 0.6F, 10, 4, 3, 1,
            1.26F, 13, 200.0F, true, false
         );
      }
   }
}
