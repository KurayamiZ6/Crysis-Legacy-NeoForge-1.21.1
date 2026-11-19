package io.github.painworks.crysismod.nanovisor.client;

import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorSettings;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorToggleMode;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class NanoVisorState {
   private static boolean visorActive;

   private NanoVisorState() {
   }

   public static boolean updateFromInput(Minecraft minecraft, KeyMapping visorKey) {
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
      return visorActive && NanoVisorSettings.General.visorEnabled();
   }

   public static boolean shouldRenderOverlay() {
      return isVisorActive() && NanoVisorSettings.General.visorOverlayEnabled();
   }

   public static void setVisorActive(boolean active) {
      visorActive = active && NanoVisorSettings.General.visorEnabled();
   }
}
