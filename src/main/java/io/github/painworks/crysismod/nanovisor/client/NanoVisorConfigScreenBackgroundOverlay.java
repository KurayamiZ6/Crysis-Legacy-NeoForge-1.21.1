package io.github.painworks.crysismod.nanovisor.client;

import io.github.painworks.crysismod.nanovisor.config.NanoVisorSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ScreenEvent.Render.Pre;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanoVisorConfigScreenBackgroundOverlay {
   private NanoVisorConfigScreenBackgroundOverlay() {
   }

   @SubscribeEvent
   public static void onScreenRenderPre(Pre event) {
      if (NanoVisorConfigScreen.isNanoVisorConfigScreen(event.getScreen())) {
         Minecraft minecraft = Minecraft.getInstance();
         int width = minecraft.getWindow().getGuiScaledWidth();
         int height = minecraft.getWindow().getGuiScaledHeight();
         float opacity = (float)Mth.clamp(NanoVisorSettings.Debug.configScreenBackgroundOpacity(), 0.0, 1.0);
         int alpha = Mth.clamp(Math.round(opacity * 255.0F), 0, 255);
         int color = alpha << 24;
         event.getGuiGraphics().fill(0, 0, width, height, color);
      }
   }
}
