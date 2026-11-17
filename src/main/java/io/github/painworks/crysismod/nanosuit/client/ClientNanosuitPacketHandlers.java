package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.config.NanosuitSoundVolumes;
import io.github.painworks.crysismod.nanosuit.energy.INanosuitEnergy;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.SyncNanosuitDataPacket;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

public final class ClientNanosuitPacketHandlers {
   private ClientNanosuitPacketHandlers() {
   }

   public static void handleSyncNanosuitData(SyncNanosuitDataPacket packet) {
      Player player = Minecraft.getInstance().player;
      if (player != null) {
         INanosuitEnergy data = NanosuitCapabilities.get(player);
         NanosuitMode previousMode = data.getMode();
         data.setRecoveryTicks(-1);
         data.setEnergyWithoutStartingRecovery(packet.getEnergy());
         data.setRecoveryTicks(packet.isRecoveryLocked() ? 0 : -1);
         data.setMode(packet.getMode());
         data.setSprintBoostReady(packet.isSprintBoostReady());
         playModeSoundIfNeeded(previousMode, packet.getMode(),
            NanosuitArmorUtil.getEquippedProfile(player) == NanosuitProfile.NANOSUIT_1);
      }
   }

   private static void playModeSoundIfNeeded(NanosuitMode previousMode, NanosuitMode nextMode, boolean classic) {
      if (previousMode == nextMode) {
         return;
      }

      if (classic) {
         switch (nextMode) {
            case ARMOR -> playUiSound(ModSounds.CLASSIC_MAXIMUM_ARMOR.get());
            case STRENGTH -> playUiSound(ModSounds.CLASSIC_MAXIMUM_STRENGTH.get());
            case SPEED -> playUiSound(ModSounds.CLASSIC_MAXIMUM_SPEED.get());
            case CLOAK -> {
               NanosuitPowerFeedback.flash();
               playUiSound(ModSounds.CLASSIC_CLOAK_ON.get());
            }
            case NONE -> {
               if (previousMode == NanosuitMode.CLOAK) {
                  playUiSound(ModSounds.CLASSIC_CLOAK_OFF.get());
               }
            }
         }
      } else if (nextMode == NanosuitMode.CLOAK) {
         NanosuitPowerFeedback.flash();
         playUiSound(ModSounds.CLOAK_ACTIVATION.get());
         playUiSound(ModSounds.CLOAK_ENGAGED.get());
      } else if (previousMode == NanosuitMode.CLOAK) {
         playUiSound(ModSounds.CLOAK_DEACTIVATION.get());
      }
   }

   private static void playUiSound(SoundEvent soundEvent) {
      Minecraft minecraft = Minecraft.getInstance();
      float volume = 0.75F * NanosuitSoundVolumes.modes();
      if (volume > 0.0F && minecraft.getSoundManager() != null) {
         minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, 1.0F, volume));
      }
   }
}
