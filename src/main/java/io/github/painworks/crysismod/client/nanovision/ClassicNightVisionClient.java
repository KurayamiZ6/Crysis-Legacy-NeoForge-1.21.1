package io.github.painworks.crysismod.client.nanovision;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.nanovision.ClassicNightVision;
import io.github.painworks.crysismod.nanovision.ClassicNightVisionBattery;
import io.github.painworks.crysismod.nanosuit.config.NanosuitSoundVolumes;
import io.github.painworks.crysismod.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;

public final class ClassicNightVisionClient {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String TARGET = "classic_night_vision_color";
   private static final ResourceLocation POST_CHAIN =
      ResourceLocation.fromNamespaceAndPath("crysismod", "shaders/post/classic_night_vision.json");
   private static boolean active;
   private static int batteryCharge = ClassicNightVisionBattery.FULL_CHARGE;
   private static PostChain chain;
   private static RenderTarget lastMainTarget;
   private static int width;
   private static int height;
   private static boolean loadFailed;
   private static final ClassicVisionTransition TRANSITION=new ClassicVisionTransition();

   public static float visualIntensity() { return TRANSITION.displayed(); }

   private ClassicNightVisionClient() {}

   public static void sync(boolean enabled, int charge) {
      boolean wasActive = active;
      batteryCharge = Mth.clamp(charge, 0, ClassicNightVisionBattery.FULL_CHARGE);
      active = enabled && batteryCharge > 0;
      if (active != wasActive) {
         Minecraft minecraft = Minecraft.getInstance();
         float volume = 0.55F * NanosuitSoundVolumes.visor();
         if (minecraft.player != null && volume > 0.0F) {
            var sound = active ? ModSounds.CLASSIC_NIGHT_VISION_ON : ModSounds.CLASSIC_NIGHT_VISION_OFF;
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound.get(), 1.0F, volume));
         }
      }
   }

   public static boolean isActive() {
      return active && ClassicNightVision.canUse(Minecraft.getInstance().player);
   }

   public static int batteryCharge() {
      return batteryCharge;
   }

   static void renderScene(Minecraft minecraft, float partialTick) {
      if (minecraft.level == null || !ClassicNightVision.canUse(minecraft.player)) {
         TRANSITION.reset();
         closeChain();
         return;
      }
      float intensity=TRANSITION.sample(System.nanoTime(),isActive());
      if(intensity<=0 && !isActive()) { closeChain(); return; }
      if (loadFailed) {
         return;
      }

      RenderTarget main = minecraft.getMainRenderTarget();
      if (chain == null || lastMainTarget != main) {
         closeChain();
         try {
            chain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), main, POST_CHAIN);
            lastMainTarget = main;
         } catch (Exception exception) {
            loadFailed = true;
            LOGGER.warn("Could not load classic night vision color pass", exception);
            main.bindWrite(true);
            return;
         }
      }
      int previousFilter = main.filterMode;
      try {
         if (width != main.width || height != main.height) {
            width = main.width;
            height = main.height;
            chain.resize(width, height);
         }
         chain.setUniform("Intensity",intensity);
         chain.process(partialTick);
         RenderTarget color = chain.getTempTarget(TARGET);
         GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, color.frameBufferId);
         GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, main.frameBufferId);
         GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL30.GL_COLOR_BUFFER_BIT, GL30.GL_NEAREST);
      } finally {
         if (main.filterMode != previousFilter) {
            main.setFilterMode(previousFilter);
         }
         main.bindWrite(true);
      }
   }

   static void renderBattery(Minecraft minecraft, GuiGraphics graphics) {
      if (minecraft.options.hideGui || !active && batteryCharge == ClassicNightVisionBattery.FULL_CHARGE
         || !ClassicNightVision.canUse(minecraft.player)
         || io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil.getEquippedProfile(minecraft.player)
            != io.github.painworks.crysismod.nanosuit.NanosuitProfile.NANOSUIT_1) {
         return;
      }
      if (!io.github.painworks.crysismod.nanosuit.client.NanosuitHudOverlay.isHudEnabled()) return;
      io.github.painworks.crysismod.nanosuit.client.ClassicNanosuitHud.renderBattery(
         graphics, batteryCharge, ClassicNightVisionBattery.FULL_CHARGE);
   }

   static void reset() {
      TRANSITION.reset();
      active = false;
      batteryCharge = ClassicNightVisionBattery.FULL_CHARGE;
      reload();
   }

   static void reload() {
      loadFailed = false;
      closeChain();
   }

   private static void closeChain() {
      if (chain != null) {
         chain.close();
         chain = null;
         lastMainTarget = null;
         width = 0;
         height = 0;
      }
   }
}
