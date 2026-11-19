package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
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
   private static boolean classicModeFlash;
   private static int flashColor = 0x73BAFF;

   private NanosuitPowerFeedback() {
   }

   public static void clearTransientEffects() {
      sustainedForce=false;flashStart=0;forceStart=0;classicModeFlash=false;
   }

   public static void flash() {
      refreshPlayer();
      if (player != null && NanosuitArmorUtil.getEquippedProfile(player) != NanosuitProfile.NANOSUIT_1) {
         classicModeFlash = false;
         flashColor = 0x73BAFF;
         flashStart = System.nanoTime();
      }
   }

   /** Called only for a changed mode received in the existing server sync. */
   public static void flashClassicMode(NanosuitMode mode) {
      refreshPlayer();
      if (player == null || NanosuitArmorUtil.getEquippedProfile(player) != NanosuitProfile.NANOSUIT_1) return;
      if (mode == NanosuitMode.CLOAK || mode == NanosuitMode.NONE) {
         flashStart = 0L;
         return;
      }
      classicModeFlash = true;
      flashColor = switch (mode) {
         case STRENGTH -> 0xFF4B42;
         case SPEED -> 0xFFD44A;
         default -> 0x73BAFF;
      };
      flashStart = System.nanoTime();
   }

   public static void triggerForce() {
      flash();
      // Keep the existing HUD force activity even when N1 has no expenditure flash.
      forceStart = player != null && NanosuitArmorUtil.getEquippedProfile(player) == NanosuitProfile.NANOSUIT_1
         ? System.nanoTime() : flashStart;
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
         classicModeFlash = false;
         flashColor = 0x73BAFF;
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
      boolean classic = NanosuitArmorUtil.getEquippedProfile(player) == NanosuitProfile.NANOSUIT_1;
      long duration = classicModeFlash ? 500_000_000L : FLASH_NANOS;
      if (classic != classicModeFlash || flashStart == 0L || elapsed >= duration) {
         return;
      }
      float remaining = 1.0F - (float)elapsed / duration;
      int alpha = Math.round((classicModeFlash ? 32 : FLASH_ALPHA) * remaining);
      var graphics = event.getGuiGraphics();
      graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), alpha << 24 | flashColor);
   }
}
