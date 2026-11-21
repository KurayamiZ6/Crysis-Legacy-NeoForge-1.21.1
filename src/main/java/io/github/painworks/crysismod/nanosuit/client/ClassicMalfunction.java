package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Temporary N1 display state. The equipped suit and the saved theme remain independent. */
@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT)
public final class ClassicMalfunction {
   public static final int RED = 0xeb6670;
   private static final int DISPLAY_TICKS = 56;
   private static LocalPlayer player;
   private static ClientLevel level;
   private static int expiresAt;
   private static long startedAt;

   private ClassicMalfunction() {}

   public static void trigger(Player recipient) {
      var client = Minecraft.getInstance();
      if (recipient != client.player || !eligible(client.player)) return;
      if (!active()) startedAt = System.nanoTime();
      player = client.player;
      level = client.level;
      expiresAt = player.tickCount + DISPLAY_TICKS;
   }

   public static boolean active() {
      if (player == null) return false;
      var client = Minecraft.getInstance();
      return client.player == player && client.level == level && player.tickCount < expiresAt && eligible(player);
   }

   private static boolean eligible(LocalPlayer candidate) {
      return candidate != null && candidate.isAlive() && !candidate.isSpectator()
         && NanosuitArmorUtil.getEquippedProfile(candidate) == NanosuitProfile.NANOSUIT_1;
   }

   @SubscribeEvent
   public static void tick(ClientTickEvent.Post event) {
      if (!active()) {
         player = null;
         level = null;
      }
   }

   public static int hudColor() {
      return active() ? RED : NanosuitClientConfig.classicInterfaceColor();
   }

   public static long elapsedMillis() {
      return (System.nanoTime() - startedAt) / 1_000_000L;
   }

   static int labelColor(int phaseMillis) {
      float phase = ((elapsedMillis() + phaseMillis) % 1200L) / 1200f;
      float weight = 1 - Math.abs(2 * phase - 1);
      weight = weight * weight * (3 - 2 * weight);
      return (Mth.lerpInt(weight, 255, 235) << 16) | (Mth.lerpInt(weight, 242, 102) << 8)
         | Mth.lerpInt(weight, 243, 112);
   }
}
