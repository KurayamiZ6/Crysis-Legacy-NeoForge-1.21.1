package io.github.painworks.crysismod.client.nanogeo;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;


import net.neoforged.bus.api.SubscribeEvent;

public final class NanoGeoArmorModeCompatPrewarmHandler {
   @SubscribeEvent
   public void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      if (true) {
         if (NanoGeoShaderpackCompatibility.resolveArmorModeOverlayBackend() == NanoGeoClientConfig.ArmorModeOverlayBackend.DYNAMIC_TEXTURE_COMPAT) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null && minecraft.player != null) {
               NanoGeoArmorModeDynamicTextureCompatRenderer.prewarmSteadyBaseFrames(minecraft.player.getUUID());
            }
         }
      }
   }

   @SubscribeEvent
   public void onClientLogout(LoggingOut event) {
      NanoGeoArmorModeDynamicTextureCompatRenderer.close();
      NanoGeoShaderpackStableTextures.close();
   }
}
