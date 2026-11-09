package io.github.painworks.crysismod.nanovisor.client;

import com.mojang.blaze3d.platform.InputConstants.Type;
import io.github.painworks.crysismod.nanosuit.config.NanosuitSoundVolumes;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanovisor.network.SetNanoVisorActivePacket;
import io.github.painworks.crysismod.registry.ModSounds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanoVisorKeyMappings {
   private static final String CATEGORY = "key.categories.crysismod";
   private static final int VISOR_STATE_HEARTBEAT_INTERVAL_TICKS = 20;
   public static final KeyMapping TOGGLE_NANO_VISOR = new KeyMapping("key.crysismod.toggle_nano_visor", Type.KEYSYM, 86, "key.categories.crysismod");
   public static final KeyMapping MARK_NANO_VISOR_TARGET = new KeyMapping("key.crysismod.mark_nano_visor_target", Type.KEYSYM, 71, "key.categories.crysismod");
   public static final KeyMapping OPEN_NANO_VISOR_SETTINGS = new KeyMapping(
      "key.crysismod.open_nano_visor_settings", Type.KEYSYM, 78, "key.categories.crysismod"
   );
   public static final KeyMapping USE_NANO_VISOR_ZOOM = new KeyMapping("key.crysismod.use_nano_visor_zoom", Type.KEYSYM, 67, "key.categories.crysismod");
   private static int visorStateHeartbeatTicks = 20;

   private NanoVisorKeyMappings() {
   }

   public static void register(RegisterKeyMappingsEvent event) {
      event.register(TOGGLE_NANO_VISOR);
      event.register(MARK_NANO_VISOR_TARGET);
      event.register(OPEN_NANO_VISOR_SETTINGS);
      event.register(USE_NANO_VISOR_ZOOM);
   }

   @SubscribeEvent
   public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      if (true) {
         Minecraft minecraft = Minecraft.getInstance();
         boolean visorChanged = NanoVisorState.updateFromInput(minecraft, TOGGLE_NANO_VISOR);
         if (minecraft.player != null) {
            if (visorChanged) {
               NanosuitNetwork.sendToServer(new SetNanoVisorActivePacket(NanoVisorState.isVisorActive()));
               playSound(minecraft, NanoVisorState.isVisorActive() ? ModSounds.BINOCULAR_ON.get() : ModSounds.BINOCULAR_OFF.get(), 0.55F);
               visorStateHeartbeatTicks = 20;
            } else if (--visorStateHeartbeatTicks <= 0) {
               NanosuitNetwork.sendToServer(new SetNanoVisorActivePacket(NanoVisorState.isVisorActive()));
               visorStateHeartbeatTicks = 20;
            }
         }

         boolean previousZoom = NanoVisorZoomHandler.isZoomInputActive();
         NanoVisorZoomHandler.updateZoomInput(USE_NANO_VISOR_ZOOM);
         if (minecraft.player != null && NanoVisorState.isVisorActive() && previousZoom != NanoVisorZoomHandler.isZoomInputActive()) {
            playSound(minecraft, NanoVisorZoomHandler.isZoomInputActive()
               ? ModSounds.BINOCULAR_ZOOM_IN.get() : ModSounds.BINOCULAR_ZOOM_OUT.get(), 0.45F);
         }
         if (minecraft.player != null) {
            while (MARK_NANO_VISOR_TARGET.consumeClick()) {
               NanoVisorTargetingHandler.requestMarkCurrentTarget(minecraft);
            }

            while (OPEN_NANO_VISOR_SETTINGS.consumeClick()) {
               minecraft.setScreen(NanoVisorConfigScreen.create(minecraft.screen));
            }
         }
      }
   }

   private static void playSound(Minecraft minecraft, SoundEvent event, float gain) {
      float volume = gain * NanosuitSoundVolumes.visor();
      if (volume > 0.0F) {
         minecraft.getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0F, volume));
      }
   }

   public static boolean isVisorToggleKeyDown() {
      return TOGGLE_NANO_VISOR.isDown();
   }

   public static boolean isZoomKeyDown() {
      return USE_NANO_VISOR_ZOOM.isDown();
   }
}
