package io.github.painworks.crysismod.client.nanovision;

import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import io.github.painworks.crysismod.nanosuit.client.NanosuitPowerFeedback;
import io.github.painworks.crysismod.nanosuit.config.NanosuitSoundVolumes;
import io.github.painworks.crysismod.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

public final class NanovisionController {
   private static NanovisionMode mode = NanovisionMode.OFF;

   private NanovisionController() {
   }

   public static void tick(Minecraft minecraft) {
      LocalPlayer player = minecraft.player;
      if (player != null && NanovisionConfig.nanovisionEnabled() && NanovisionState.canUseNanovision(player)) {
         if (mode != NanovisionMode.OFF && !isModeAvailable(mode)) {
            setMode(NanovisionConfig.workingMode());
         }
      } else {
         setMode(NanovisionMode.OFF);
      }
   }

   public static void toggle(Minecraft minecraft) {
      if (minecraft.player != null && NanovisionState.canUseNanovision(minecraft.player)) {
         setMode(mode.isActive() ? NanovisionMode.OFF : NanovisionConfig.workingMode());
      } else {
         setMode(NanovisionMode.OFF);
      }
   }

   public static void cycleMode(Minecraft minecraft) {
      if (minecraft.player != null && NanovisionState.canUseNanovision(minecraft.player)) {
         setMode(NanovisionConfig.workingMode());
      }
   }

   public static boolean isActive() {
      return mode.isActive();
   }

   public static NanovisionMode getMode() {
      return mode;
   }

   public static void setMode(NanovisionMode targetMode) {
      NanovisionMode previous = mode;
      mode = normalizeMode(targetMode);
      if (!previous.isActive() && mode.isActive()) {
         // The late thermal pass can leave shader packs with blending unavailable to the GUI.
         if (!NanoGeoShaderpackCompatibility.isShaderPackActive()) {
            NanosuitPowerFeedback.flash();
         }
         NanovisionActivationOverlay.begin();
         playUiSound((SoundEvent)ModSounds.NANOVISION_ACTIVATION.get());
         playUiSound((SoundEvent)ModSounds.NANOVISION_ENABLED.get());
      } else if (previous.isActive() && !mode.isActive()) {
         NanovisionActivationOverlay.reset();
         playUiSound((SoundEvent)ModSounds.NANOVISION_DEACTIVATION.get());
      }
   }

   public static void reset() {
      mode = NanovisionMode.OFF;
      NanovisionActivationOverlay.reset();
   }

   public static boolean shouldRenderMarkerPass() {
      return mode.usesMarkerPass() && NanovisionConfig.markerPipelineEnabled();
   }

   public static boolean shouldRunPostProcessor() {
      return mode.isActive() && (NanovisionConfig.postShaderEnabled() || shouldRenderMarkerPass());
   }

   public static boolean isDebugMarkerMode() {
      return mode == NanovisionMode.DEBUG_MARKER || NanovisionConfig.thermalMarkerDebugShowRawMarker();
   }

   public static boolean isDebugDistanceMode() {
      return mode == NanovisionMode.DEBUG_DISTANCE || NanovisionConfig.thermalMarkerDebugShowDistance();
   }

   private static NanovisionMode normalizeMode(NanovisionMode requested) {
      if (requested != null && requested != NanovisionMode.OFF) {
         return isModeAvailable(requested) ? requested : NanovisionConfig.workingMode();
      } else {
         return NanovisionMode.OFF;
      }
   }

   private static boolean isModeAvailable(NanovisionMode candidate) {
      return switch (candidate) {
         case OFF -> true;
         case THERMAL_MARKER, DEBUG_MARKER, DEBUG_DISTANCE -> NanovisionConfig.markerPipelineEnabled();
         case GRAYSCALE_NIGHT -> false;
      };
   }

   private static void playUiSound(SoundEvent soundEvent) {
      Minecraft minecraft = Minecraft.getInstance();
      float volume = 0.55F * NanosuitSoundVolumes.visor();
      if (minecraft.getSoundManager() != null && volume > 0.0F) {
         minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, 1.0F, volume));
      }
   }
}
