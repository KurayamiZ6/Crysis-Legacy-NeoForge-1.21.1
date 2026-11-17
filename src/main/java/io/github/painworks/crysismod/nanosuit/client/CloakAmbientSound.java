package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import io.github.painworks.crysismod.nanosuit.config.NanosuitSoundVolumes;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitSwimBoost;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class CloakAmbientSound {
   private static final AmbientLoop CLOAK = new AmbientLoop();
   private static final AmbientLoop ARMOR = new AmbientLoop();
   private static final AmbientLoop THRUSTER = new AmbientLoop();
   private static final AmbientLoop POWER_SPRINT = new AmbientLoop();
   private static final AmbientLoop NANOVISION = new AmbientLoop();

   private CloakAmbientSound() {
   }

   @SubscribeEvent
   public static void update(ClientTickEvent.Post event) {
      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer player = minecraft.player;
      SoundManager sounds = minecraft.getSoundManager();
      float volume = NanosuitSoundVolumes.ambience();
      boolean audible = minecraft.options.getSoundSourceVolume(SoundSource.MASTER) > 0.0F
         && minecraft.options.getSoundSourceVolume(SoundSource.PLAYERS) > 0.0F && volume > 0.0F;
      NanosuitProfile profile = audible && player != null && player.isAlive() ? NanosuitArmorUtil.getEquippedProfile(player) : null;
      NanosuitMode activeMode = NanosuitMode.NONE;
      if (profile != null) {
         var data = NanosuitCapabilities.get(player);
         if (data.getEnergy() > 0.0F && !data.isRecoveryLocked()) {
            activeMode = data.getMode();
         }
      }

      CLOAK.update(sounds, activeMode == NanosuitMode.CLOAK, ModSounds.CLOAK_AMBIENCE.get(), 0.192F * volume);
      ARMOR.update(sounds, activeMode == NanosuitMode.ARMOR && profile != NanosuitProfile.NANOSUIT_1, ModSounds.ARMOR_AMBIENCE.get(), 0.232F * volume);
      THRUSTER.update(sounds, audible && player != null && NanosuitSwimBoost.isActive(player), ModSounds.HYDRO_THRUSTER.get(), 0.298F * volume);
      POWER_SPRINT.update(sounds, audible && player != null && NanosuitPlayerHandler.isLandPowerSprintActive(player),
         ModSounds.POWER_SPRINT_AMBIENCE.get(), 0.42F * volume);
      NANOVISION.update(sounds, audible && player != null && player.isAlive() && NanovisionState.isActive(player),
         ModSounds.NANOVISION_AMBIENCE.get(), 0.24F * volume);
   }

   private static final class AmbientLoop {
      private SoundInstance instance;
      private float gain;

      void update(SoundManager manager, boolean active, SoundEvent event, float gain) {
         if (this.instance != null && (!active || this.gain != gain)) {
            manager.stop(this.instance);
            this.instance = null;
         }
         if (active && (this.instance == null || !manager.isActive(this.instance))) {
            this.gain = gain;
            this.instance = new SimpleSoundInstance(event.getLocation(), SoundSource.PLAYERS, gain, 1.0F,
               RandomSource.create(), true, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
            manager.play(this.instance);
         }
      }
   }
}
