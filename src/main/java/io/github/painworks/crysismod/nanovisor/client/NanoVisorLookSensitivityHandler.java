package io.github.painworks.crysismod.nanovisor.client;

import io.github.painworks.crysismod.nanovisor.config.NanoVisorSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanoVisorLookSensitivityHandler {
   private static final double MIN_MULTIPLIER = 0.05;
   private static final double MAX_MULTIPLIER = 1.0;
   private static final double EPSILON = 1.0E-6;
   private static boolean overrideActive;
   private static double baseSensitivity = 0.5;
   private static double lastAppliedSensitivity = Double.NaN;

   private NanoVisorLookSensitivityHandler() {
   }

   @SubscribeEvent
   public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      if (true) {
         Minecraft minecraft = Minecraft.getInstance();
         if (minecraft.player == null) {
            clearOverride(minecraft);
         } else if (!shouldApply()) {
            clearOverride(minecraft);
         } else {
            double baseMultiplier = Mth.clamp(NanoVisorSettings.Zoom.lookSensitivityMultiplier(), 0.05, 1.0);
            double zoomFactor = NanoVisorZoomHandler.getSensitivityZoomFactor();
            double effectiveMultiplier = Mth.clamp(baseMultiplier * zoomFactor, 0.05, 1.0);
            OptionInstance<Double> sensitivityOption = minecraft.options.sensitivity();
            double currentSensitivity = (Double)sensitivityOption.get();
            if (!overrideActive) {
               baseSensitivity = currentSensitivity;
               overrideActive = true;
            } else if (!Double.isNaN(lastAppliedSensitivity) && Math.abs(currentSensitivity - lastAppliedSensitivity) > 1.0E-6) {
               baseSensitivity = currentSensitivity / Math.max(effectiveMultiplier, 0.05);
            }

            double targetSensitivity = Mth.clamp(baseSensitivity * effectiveMultiplier, 0.0, 1.0);
            if (Math.abs(currentSensitivity - targetSensitivity) > 1.0E-6) {
               sensitivityOption.set(targetSensitivity);
            }

            lastAppliedSensitivity = targetSensitivity;
         }
      }
   }

   private static boolean shouldApply() {
      return NanoVisorState.isVisorActive() && NanoVisorSettings.Zoom.enabled();
   }

   private static void clearOverride(Minecraft minecraft) {
      if (overrideActive) {
         OptionInstance<Double> sensitivityOption = minecraft.options.sensitivity();
         sensitivityOption.set(Mth.clamp(baseSensitivity, 0.0, 1.0));
         overrideActive = false;
         lastAppliedSensitivity = Double.NaN;
      }
   }
}
