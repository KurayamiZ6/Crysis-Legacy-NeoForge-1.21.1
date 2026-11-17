package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.mixin.client.nanosuit.AbstractSoundVolumeAccessor;
import io.github.painworks.crysismod.nanosuit.config.NanosuitSoundVolumes;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class NanosuitPositionalSoundGain {
   private NanosuitPositionalSoundGain() {
   }

   @SubscribeEvent
   public static void adjust(PlaySoundEvent event) {
      SoundInstance sound = event.getSound();
      if (!(sound instanceof SimpleSoundInstance) || !sound.getLocation().getNamespace().equals("crysismod")) {
         return;
      }

      String name = sound.getLocation().getPath();
      float gain;
      if (name.equals("power_melee") || name.equals("power_melee_stock") || name.equals("classic_power_melee")) {
         gain = NanosuitSoundVolumes.melee();
      } else if (name.startsWith("step_") || name.equals("nanosuit_landing") || name.equals("power_jump")) {
         gain = NanosuitSoundVolumes.movement();
      } else {
         return;
      }
      if (gain == 0.0F) {
         event.setSound(null);
      } else if (gain != 1.0F && sound instanceof AbstractSoundVolumeAccessor volume) {
         // PlaySoundEvent runs before the engine resolves the sample used by getVolume().
         volume.crysismod$setBaseVolume(volume.crysismod$getBaseVolume() * gain);
      }
   }
}
