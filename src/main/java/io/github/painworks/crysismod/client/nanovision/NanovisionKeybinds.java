package io.github.painworks.crysismod.client.nanovision;

import com.mojang.blaze3d.platform.InputConstants.Type;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanovision.network.SetNanovisionActivePacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanovisionKeybinds {
   private static final String CATEGORY = "key.categories.crysismod";
   private static final int SERVER_STATE_HEARTBEAT_TICKS = 20;
   private static int serverStateHeartbeat = 20;
   public static final KeyMapping TOGGLE_NANOVISION = new KeyMapping("key.crysismod.toggle_nanovision", Type.KEYSYM, 66, "key.categories.crysismod");
   public static final KeyMapping CYCLE_NANOVISION_MODE = new KeyMapping("key.crysismod.cycle_nanovision_mode", Type.KEYSYM, 72, "key.categories.crysismod");

   private NanovisionKeybinds() {
   }

   public static void register(RegisterKeyMappingsEvent event) {
      event.register(TOGGLE_NANOVISION);
      event.register(CYCLE_NANOVISION_MODE);
   }

   @SubscribeEvent
   public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      if (true) {
         Minecraft minecraft = Minecraft.getInstance();
         boolean wasActive = NanovisionController.isActive();

         while (TOGGLE_NANOVISION.consumeClick()) {
            NanovisionController.toggle(minecraft);
         }

         while (CYCLE_NANOVISION_MODE.consumeClick()) {
            NanovisionController.cycleMode(minecraft);
         }

         NanovisionController.tick(minecraft);
         NanovisionState.tickClientEffects(minecraft);
         boolean active = NanovisionController.isActive();
         if (minecraft.player != null) {
            if (active != wasActive) {
               NanosuitNetwork.sendToServer(new SetNanovisionActivePacket(active));
               serverStateHeartbeat = 20;
            } else if (--serverStateHeartbeat <= 0) {
               NanosuitNetwork.sendToServer(new SetNanovisionActivePacket(active));
               serverStateHeartbeat = 20;
            }
         } else {
            serverStateHeartbeat = 20;
         }
      }
   }
}
