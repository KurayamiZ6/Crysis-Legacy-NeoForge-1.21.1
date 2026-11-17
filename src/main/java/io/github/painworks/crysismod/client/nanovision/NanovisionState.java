package io.github.painworks.crysismod.client.nanovision;

import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.energy.INanosuitEnergy;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class NanovisionState {
   private static final int INFINITE_NIGHT_VISION_DURATION_TICKS = -1;
   private static boolean appliedNightVision;

   private NanovisionState() {
   }

   public static boolean isActive() {
      return isActive(Minecraft.getInstance().player);
   }

   public static boolean isActive(LocalPlayer player) {
      return player != null && NanovisionConfig.nanovisionEnabled() && NanovisionController.isActive() && canUseNanovision(player);
   }

   public static boolean canUseNanovision(LocalPlayer player) {
      if (player == null) {
         return false;
      }
      INanosuitEnergy data = NanosuitCapabilities.get(player);
      if (data.isRecoveryLocked() || !(data.getEnergy() > 0.0F)) {
         return false;
      }
      NanosuitProfile profile = NanosuitArmorUtil.getNanovisionProfile(
         player, NanovisionConfig.nanovisionRequiresFullSet(), NanovisionConfig.nanovisionRequiresHelmet()
      );
      return profile != null && profile != NanosuitProfile.NANOSUIT_1 && profile.auxiliary().nanovision();
   }

   public static void tickClientEffects(Minecraft minecraft) {
      LocalPlayer player = minecraft.player;
      if (player == null) {
         appliedNightVision = false;
      } else if (isActive(player) && NanovisionConfig.nanovisionUseNightVision()) {
         MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
         if (current == null || appliedNightVision && current.getDuration() != -1) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 0, false, false, false));
            appliedNightVision = true;
         }
      } else {
         removeNightVision(player);
      }
   }

   public static void clearClientEffects(LocalPlayer player) {
      removeNightVision(player);
   }

   private static void removeNightVision(LocalPlayer player) {
      if (player == null) {
         appliedNightVision = false;
      } else {
         MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
         if (appliedNightVision && current != null) {
            player.removeEffect(MobEffects.NIGHT_VISION);
         }

         appliedNightVision = false;
      }
   }
}
