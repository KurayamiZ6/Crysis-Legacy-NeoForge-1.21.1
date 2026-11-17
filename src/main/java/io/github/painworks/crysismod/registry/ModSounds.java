package io.github.painworks.crysismod.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModSounds {
   public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "crysismod");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLOAK_ENGAGED = register("cloak_engaged");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLOAK_ACTIVATION = register("cloak_activation");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLOAK_DEACTIVATION = register("cloak_deactivation");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLOAK_AMBIENCE = register("cloak_ambience");
   public static final DeferredHolder<SoundEvent, SoundEvent> MAXIMUM_ARMOR = register("maximum_armor");
   public static final DeferredHolder<SoundEvent, SoundEvent> ARMOR_MODE_ACTIVATION = register("armor_mode_activation");
   public static final DeferredHolder<SoundEvent, SoundEvent> ARMOR_MODE_DISABLE = register("armor_mode_disable");
   public static final DeferredHolder<SoundEvent, SoundEvent> NANOVISION_ACTIVATION = register("nanovision_activation");
   public static final DeferredHolder<SoundEvent, SoundEvent> NANOVISION_DEACTIVATION = register("nanovision_deactivation");
   public static final DeferredHolder<SoundEvent, SoundEvent> NANOVISION_ENABLED = register("nanovision_enabled");
   public static final DeferredHolder<SoundEvent, SoundEvent> NANOVISION_AMBIENCE = register("nanovision_ambience");
   public static final DeferredHolder<SoundEvent, SoundEvent> POWER_JUMP = register("power_jump");
   public static final StepSounds STONE_STEPS = registerSteps("stone");
   public static final StepSounds DIRT_STEPS = registerSteps("dirt");
   public static final StepSounds GRASS_STEPS = registerSteps("grass");
   public static final StepSounds GRAVEL_STEPS = registerSteps("gravel");
   public static final StepSounds METAL_STEPS = registerSteps("metal");
   public static final StepSounds SAND_STEPS = registerSteps("sand");
   public static final StepSounds WOOD_STEPS = registerSteps("wood");
   public static final DeferredHolder<SoundEvent, SoundEvent> FABRIC_STEP = register("step_fabric");
   public static final DeferredHolder<SoundEvent, SoundEvent> ARMOR_RUN_STEP = register("step_armor_run");
   public static final DeferredHolder<SoundEvent, SoundEvent> NANOSUIT_LANDING = register("nanosuit_landing");
   public static final DeferredHolder<SoundEvent, SoundEvent> ENERGY_CRITICAL = register("energy_critical");
   public static final DeferredHolder<SoundEvent, SoundEvent> SUIT_DISABLED_STATE_INITIAL = register("suit_disabled_state_initial");
   public static final DeferredHolder<SoundEvent, SoundEvent> SUIT_DISABLED_STATE_LOOP = register("suit_disabled_state_loop");
   public static final DeferredHolder<SoundEvent, SoundEvent> ENERGY_CHARGE = register("energy_charge");
   public static final DeferredHolder<SoundEvent, SoundEvent> ENERGY_RESTORED = register("energy_restored");
   public static final DeferredHolder<SoundEvent, SoundEvent> ARMOR_AMBIENCE = register("armor_ambience");
   public static final DeferredHolder<SoundEvent, SoundEvent> HYDRO_THRUSTER = register("hydro_thruster");
   public static final DeferredHolder<SoundEvent, SoundEvent> POWER_SPRINT_AMBIENCE = register("power_sprint_ambience");
   public static final DeferredHolder<SoundEvent, SoundEvent> POWER_MELEE = register("power_melee");
   public static final DeferredHolder<SoundEvent, SoundEvent> POWER_MELEE_STOCK = register("power_melee_stock");
   public static final DeferredHolder<SoundEvent, SoundEvent> BINOCULAR_ON = register("binocular_on");
   public static final DeferredHolder<SoundEvent, SoundEvent> BINOCULAR_OFF = register("binocular_off");
   public static final DeferredHolder<SoundEvent, SoundEvent> BINOCULAR_ZOOM_IN = register("binocular_zoom_in");
   public static final DeferredHolder<SoundEvent, SoundEvent> BINOCULAR_ZOOM_OUT = register("binocular_zoom_out");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_MAXIMUM_ARMOR = register("classic_maximum_armor");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_MAXIMUM_STRENGTH = register("classic_maximum_strength");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_MAXIMUM_SPEED = register("classic_maximum_speed");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_CLOAK_ON = register("classic_cloak_on");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_CLOAK_OFF = register("classic_cloak_off");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_ENERGY_CRITICAL = register("classic_energy_critical");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_ENERGY_LOW = register("classic_energy_low");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_POWER_MELEE = register("classic_power_melee");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_NIGHT_VISION_ON = register("classic_night_vision_on");
   public static final DeferredHolder<SoundEvent, SoundEvent> CLASSIC_NIGHT_VISION_OFF = register("classic_night_vision_off");

   private ModSounds() {
   }

   private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
      ResourceLocation id = ResourceLocation.fromNamespaceAndPath("crysismod", name);
      return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
   }

   private static StepSounds registerSteps(String surface) {
      return new StepSounds(register("step_" + surface + "_walk"), register("step_" + surface + "_run"));
   }

   public record StepSounds(DeferredHolder<SoundEvent, SoundEvent> walk, DeferredHolder<SoundEvent, SoundEvent> run) {
      public SoundEvent forPace(boolean sprinting) {
         return (sprinting ? run : walk).get();
      }
   }
}
