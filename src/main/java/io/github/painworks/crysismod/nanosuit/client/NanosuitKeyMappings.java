package io.github.painworks.crysismod.nanosuit.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanosuit.network.RequestSprintBoostPacket;
import io.github.painworks.crysismod.nanosuit.network.ToggleNanosuitModePacket;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanosuitKeyMappings {
   public static final KeyMapping TOGGLE_ARMOR = new KeyMapping("key.crysismod.toggle_armor", Type.KEYSYM, 90, "key.categories.crysismod");
   public static final KeyMapping TOGGLE_CLOAK = new KeyMapping("key.crysismod.toggle_cloak", Type.KEYSYM, 88, "key.categories.crysismod");
   public static final KeyMapping CYCLE_NANOSUIT_1_MODE = new KeyMapping(
      "key.crysismod.cycle_nanosuit1_mode", Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.crysismod"
   );
   public static final KeyMapping OPEN_PERSPECTIVE_DEBUG_SETTINGS = new KeyMapping(
      "key.crysismod.open_perspective_debug_settings", Type.KEYSYM, 75, "key.categories.crysismod"
   );
   public static final KeyMapping TOGGLE_ENHANCED_JUMP_DEBUG = new KeyMapping(
      "key.crysismod.toggle_enhanced_jump_debug", Type.KEYSYM, 297, "key.categories.crysismod"
   );
   private static boolean enhancedJumpDebugEnabled;

   private NanosuitKeyMappings() {
   }

   public static void register(RegisterKeyMappingsEvent event) {
      event.register(TOGGLE_ARMOR);
      event.register(TOGGLE_CLOAK);
      event.register(CYCLE_NANOSUIT_1_MODE);
      event.register(OPEN_PERSPECTIVE_DEBUG_SETTINGS);
      event.register(TOGGLE_ENHANCED_JUMP_DEBUG);
   }

   @SubscribeEvent
   public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
      if (true) {
         while (TOGGLE_ARMOR.consumeClick()) {
            NanosuitNetwork.sendToServer(new ToggleNanosuitModePacket(NanosuitMode.ARMOR));
         }

         while (TOGGLE_CLOAK.consumeClick()) {
            NanosuitNetwork.sendToServer(new ToggleNanosuitModePacket(NanosuitMode.CLOAK));
         }

         Minecraft minecraft = Minecraft.getInstance();
         while (CYCLE_NANOSUIT_1_MODE.consumeClick()) {
            requestNextNanosuit1Mode(minecraft);
         }
         if (minecraft.player != null) {
            EnhancedJumpClientState.captureUnshiftedHorizontalVelocity();
            EnhancedJumpClientState.logTestTick();

            while (OPEN_PERSPECTIVE_DEBUG_SETTINGS.consumeClick()) {
               if (!(minecraft.screen instanceof NanosuitPerspectiveDebugSettingsScreen)) {
                  minecraft.setScreen(new NanosuitPerspectiveDebugSettingsScreen(minecraft.screen));
               }
            }

            while (TOGGLE_ENHANCED_JUMP_DEBUG.consumeClick()) {
               enhancedJumpDebugEnabled = !enhancedJumpDebugEnabled;
            }
         }
      }
   }

   private static void requestNextNanosuit1Mode(Minecraft minecraft) {
      if (minecraft.screen != null || minecraft.getOverlay() != null || minecraft.player == null
         || NanosuitArmorUtil.getEquippedProfile(minecraft.player) != NanosuitProfile.NANOSUIT_1) {
         return;
      }
      var data = NanosuitCapabilities.get(minecraft.player);
      if (data.isRecoveryLocked()) {
         return;
      }
      NanosuitMode nextMode = switch (data.getMode()) {
         case ARMOR -> NanosuitMode.STRENGTH;
         case STRENGTH -> NanosuitMode.SPEED;
         case SPEED -> NanosuitMode.CLOAK;
         case CLOAK, NONE -> NanosuitMode.ARMOR;
      };
      NanosuitNetwork.sendToServer(new ToggleNanosuitModePacket(nextMode));
   }

   @SubscribeEvent
   public static void onSprintKey(InputEvent.Key event) {
      if (event.getAction() == GLFW.GLFW_PRESS) {
         requestPowerSprintOnPress(InputConstants.getKey(event.getKey(), event.getScanCode()));
      }
   }

   @SubscribeEvent
   public static void onSprintMouseButton(InputEvent.MouseButton.Post event) {
      if (event.getAction() == GLFW.GLFW_PRESS) {
         requestPowerSprintOnPress(Type.MOUSE.getOrCreate(event.getButton()));
      }
   }

   private static void requestPowerSprintOnPress(InputConstants.Key key) {
      Minecraft minecraft = Minecraft.getInstance();
      // Evaluate before the next movement tick can start vanilla sprinting from this press.
      if (minecraft.screen == null && minecraft.getOverlay() == null && minecraft.player != null
         && minecraft.player.isSprinting() && !minecraft.player.isInWaterOrBubble()
         && minecraft.options.keySprint.isActiveAndMatches(key)
         && NanosuitArmorUtil.getEquippedProfile(minecraft.player) != NanosuitProfile.NANOSUIT_1) {
         NanosuitNetwork.sendToServer(new RequestSprintBoostPacket());
      }
   }

   public static boolean isEnhancedJumpDebugEnabled() {
      return enhancedJumpDebugEnabled;
   }
}
