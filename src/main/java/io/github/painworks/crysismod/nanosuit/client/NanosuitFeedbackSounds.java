package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.config.NanosuitSoundVolumes;
import io.github.painworks.crysismod.nanosuit.network.SyncSuitEnergySoundPacket;
import io.github.painworks.crysismod.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class NanosuitFeedbackSounds {
   private NanosuitFeedbackSounds() {
   }

   public static void play(SyncSuitEnergySoundPacket packet) {
      Minecraft minecraft = Minecraft.getInstance();
      float volume = packet.volume() * NanosuitSoundVolumes.energy();
      if (minecraft.player == null || !minecraft.player.isAlive() || volume <= 0.0F) {
         return;
      }
      SoundEvent sound = switch (packet.cue()) {
         case CRITICAL -> ModSounds.ENERGY_CRITICAL.get();
         case DISABLED -> ModSounds.SUIT_DISABLED_STATE_INITIAL.get();
         case WAIT -> ModSounds.SUIT_DISABLED_STATE_LOOP.get();
         case CHARGE -> ModSounds.ENERGY_CHARGE.get();
         case RESTORED -> ModSounds.ENERGY_RESTORED.get();
      };
      minecraft.getSoundManager().play(new SimpleSoundInstance(sound.getLocation(), SoundSource.PLAYERS, volume, 1.0F,
         RandomSource.create(), false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true));
   }
}
