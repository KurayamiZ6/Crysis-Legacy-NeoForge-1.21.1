package io.github.painworks.crysismod.nanovisor.client;

import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorSettings;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorToggleMode;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class NanoVisorState {
   private static boolean visorActive;
   private static boolean waitForRelease;

   private NanoVisorState() {
   }

   public static boolean updateFromInput(Minecraft minecraft, KeyMapping visorKey) {
      boolean wasActive = visorActive;
      if (!canUse(minecraft) || waitForRelease && visorKey.isDown()) {
         invalidate();
         while (visorKey.consumeClick()) {}
         if (!visorKey.isDown()) waitForRelease = false;
         return wasActive;
      }
      waitForRelease = false;
      if (ClassicBinoculars.isN1Context() || ClassicBinoculars.running()) return ClassicBinoculars.updateFromInput(visorKey);
      boolean previous = visorActive;
      if (minecraft.player != null && NanoVisorSettings.General.visorEnabled()
         && !NanosuitCapabilities.get(minecraft.player).isRecoveryLocked()) {
         NanoVisorToggleMode toggleMode = NanoVisorSettings.General.visorToggleMode();
         if (toggleMode != NanoVisorToggleMode.HOLD) {
            while (visorKey.consumeClick()) {
               visorActive = !visorActive;
            }

            return previous != visorActive;
         } else {
            while (visorKey.consumeClick()) {
            }

            visorActive = visorKey.isDown();
            return previous != visorActive;
         }
      } else {
         while (visorKey.consumeClick()) {
         }

         visorActive = false;
         return previous != visorActive;
      }
   }

   public static boolean isVisorActive() {
      return visorActive && canUse(Minecraft.getInstance());
   }

   public static boolean shouldRenderOverlay() {
      return isVisorActive() && NanoVisorSettings.General.visorOverlayEnabled();
   }

   public static void setVisorActive(boolean active) {
      visorActive = active && canUse(Minecraft.getInstance());
   }

   public static boolean canUse(Minecraft minecraft) {
      var profile=io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil.getUsableProfile(minecraft.player);
      if(profile==null || minecraft.screen!=null || minecraft.getOverlay()!=null || !NanoVisorSettings.General.visorEnabled())return false;
      if(profile==io.github.painworks.crysismod.nanosuit.NanosuitProfile.NANOSUIT_1)return true;
      var data=NanosuitCapabilities.get(minecraft.player);
      return profile.auxiliary().nanoVisorMarking() && !data.isRecoveryLocked() && data.getEnergy()>0;
   }

   public static void invalidate() {
      waitForRelease |= NanoVisorKeyMappings.TOGGLE_NANO_VISOR.isDown();
      ClassicBinoculars.reset();
      NanoVisorZoomHandler.reset();
   }
}
