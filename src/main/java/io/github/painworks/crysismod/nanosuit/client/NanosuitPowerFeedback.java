package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class NanosuitPowerFeedback {
   private static final long FLASH_NANOS = 450_000_000L;
   private static final long FORCE_PULSE_NANOS = 200_000_000L;
   private static final int FLASH_ALPHA = 64;
   private static LocalPlayer player;
   private static boolean sustainedForce;
   private static long flashStart;
   private static long forceStart;

   private NanosuitPowerFeedback() {
   }

   public static void flash() {
      refreshPlayer();
      if (player != null) {
         flashStart = System.nanoTime();
      }
   }

   public static void triggerForce() {
      flash();
      forceStart = flashStart;
   }

   public static void setSustainedForce(boolean active) {
      refreshPlayer();
      if (active && !sustainedForce) {
         flash();
      }
      sustainedForce = active;
   }

   public static boolean isForceActive() {
      refreshPlayer();
      return player != null && (sustainedForce || forceStart != 0L && System.nanoTime() - forceStart < FORCE_PULSE_NANOS);
   }

   private static void refreshPlayer() {
      LocalPlayer current = Minecraft.getInstance().player;
      if (current != player) {
         player = current;
         sustainedForce = false;
         flashStart = 0L;
         forceStart = 0L;
      }
   }

   @SubscribeEvent
   public static void render(RenderGuiEvent.Pre event) {
      refreshPlayer();
      if (player == null || !player.isAlive() || Minecraft.getInstance().options.hideGui
            || !NanosuitArmorUtil.hasFullSet(player)) {
         return;
      }
      long elapsed = System.nanoTime() - flashStart;
      if (flashStart == 0L || elapsed >= FLASH_NANOS) {
         return;
      }
      float remaining = 1.0F - (float)elapsed / FLASH_NANOS;
      int alpha = Math.round(FLASH_ALPHA * remaining);
      var graphics = event.getGuiGraphics();
      graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), alpha << 24 | 0x73BAFF);
   }
}
