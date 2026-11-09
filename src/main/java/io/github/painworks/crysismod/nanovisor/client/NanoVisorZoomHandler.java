package io.github.painworks.crysismod.nanovisor.client;

import io.github.painworks.crysismod.nanovisor.config.NanoVisorSettings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanoVisorZoomHandler {
   private static final float MIN_ZOOM_MULTIPLIER = 0.05F;
   private static final float MAX_ZOOM_MULTIPLIER = 2.0F;
   private static final float MIN_SMOOTHNESS = 0.0F;
   private static final float MAX_SMOOTHNESS = 1.0F;
   private static final float EPSILON = 1.0E-4F;
   private static float currentZoomMultiplier = 1.0F;
   private static boolean zoomInputActive;
   private static Boolean lastHoldMode;

   private NanoVisorZoomHandler() {
   }

   @SubscribeEvent
   public static void onComputeFovModifier(ComputeFovModifierEvent event) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player == null) {
         currentZoomMultiplier = 1.0F;
         zoomInputActive = false;
      } else {
         float targetMultiplier = resolveTargetZoomMultiplier();
         float smoothness = (float)Mth.clamp(NanoVisorSettings.Zoom.smoothness(), 0.0, 1.0);
         float interpolation = smoothness <= 1.0E-4F ? 1.0F : smoothness;
         currentZoomMultiplier = currentZoomMultiplier + (targetMultiplier - currentZoomMultiplier) * interpolation;
         if (Math.abs(currentZoomMultiplier - targetMultiplier) <= 1.0E-4F) {
            currentZoomMultiplier = targetMultiplier;
         }

         event.setNewFovModifier(event.getNewFovModifier() * currentZoomMultiplier);
      }
   }

   public static void updateZoomInput(KeyMapping zoomKey) {
      boolean holdToUse = NanoVisorSettings.Zoom.holdToUse();
      if (lastHoldMode == null || lastHoldMode != holdToUse) {
         while (zoomKey.consumeClick()) {
         }

         zoomInputActive = holdToUse && zoomKey.isDown();
         lastHoldMode = holdToUse;
      }

      if (NanoVisorState.isVisorActive() && NanoVisorSettings.Zoom.enabled()) {
         if (!holdToUse) {
            while (zoomKey.consumeClick()) {
               zoomInputActive = !zoomInputActive;
            }
         } else {
            while (zoomKey.consumeClick()) {
            }

            zoomInputActive = zoomKey.isDown();
         }
      } else {
         while (zoomKey.consumeClick()) {
         }

         zoomInputActive = false;
      }
   }

   public static float getNormalizedZoom() {
      float baseMultiplier = getBaseZoomMultiplier();
      float boostedMultiplier = getBoostedZoomMultiplier();
      float denominator = baseMultiplier - boostedMultiplier;
      if (Math.abs(denominator) <= 1.0E-4F) {
         return 0.0F;
      } else {
         float normalized = (baseMultiplier - currentZoomMultiplier) / denominator;
         return Mth.clamp(normalized, 0.0F, 1.0F);
      }
   }

   public static boolean isZoomInputActive() {
      return zoomInputActive;
   }

   public static float getCurrentZoomMultiplier() {
      return currentZoomMultiplier;
   }

   public static float getSensitivityZoomFactor() {
      if (NanoVisorState.isVisorActive() && NanoVisorSettings.Zoom.enabled()) {
         float baseMultiplier = getBaseZoomMultiplier();
         if (baseMultiplier <= 1.0E-4F) {
            return 1.0F;
         } else {
            float ratio = currentZoomMultiplier / baseMultiplier;
            return Mth.clamp(ratio, 0.05F, 1.0F);
         }
      } else {
         return 1.0F;
      }
   }

   private static float getBaseZoomMultiplier() {
      float configuredMin = (float)Mth.clamp(NanoVisorSettings.Zoom.minFovMultiplier(), 0.05F, 2.0);
      float configuredMax = (float)Mth.clamp(NanoVisorSettings.Zoom.maxFovMultiplier(), 0.05F, 2.0);
      return Math.max(configuredMin, configuredMax);
   }

   private static float getBoostedZoomMultiplier() {
      float configuredMin = (float)Mth.clamp(NanoVisorSettings.Zoom.minFovMultiplier(), 0.05F, 2.0);
      float configuredMax = (float)Mth.clamp(NanoVisorSettings.Zoom.maxFovMultiplier(), 0.05F, 2.0);
      return Math.min(configuredMin, configuredMax);
   }

   private static float resolveTargetZoomMultiplier() {
      if (!NanoVisorState.isVisorActive() || !NanoVisorSettings.Zoom.enabled()) {
         return 1.0F;
      } else {
         return !zoomInputActive ? getBaseZoomMultiplier() : getBoostedZoomMultiplier();
      }
   }
}
