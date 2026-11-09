package io.github.painworks.crysismod.nanosuit.client.render;

import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import org.slf4j.Logger;

public final class NanosuitArmRenderHandler {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int WARMUP_FRAMES_AFTER_LOGIN = 12;
   private static final int WARMUP_FRAMES_AFTER_SET_EQUIP = 4;
   private static boolean firstEventLogged;
   private int warmupFrames;
   private boolean hadFullSetLastFrame;

   @SubscribeEvent
   public void onClientLoggingIn(LoggingIn event) {
      this.warmupFrames = 12;
      this.hadFullSetLastFrame = false;
      NanosuitFirstPersonArmRenderDispatcher.resetTransientRenderState();
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void onRenderArm(RenderArmEvent event) {
      AbstractClientPlayer player = event.getPlayer();
      boolean fullSet = player != null && NanosuitArmorUtil.hasFullSet(player);
      if (!firstEventLogged) {
         firstEventLogged = true;
         LOGGER.info("[CrysisMod] Nanosuit RenderArmEvent active (arm={}, fullSet={})", event.getArm(), fullSet);
      }

      if (!fullSet) {
         this.hadFullSetLastFrame = false;
         this.warmupFrames = 0;
         NanosuitFirstPersonArmRenderDispatcher.resetTransientRenderState();
      } else {
         if (!this.hadFullSetLastFrame) {
            this.hadFullSetLastFrame = true;
            this.warmupFrames = Math.max(this.warmupFrames, 4);
            NanosuitFirstPersonArmRenderDispatcher.resetTransientRenderState();
         }

         if (this.warmupFrames > 0) {
            this.warmupFrames--;
            NanosuitFirstPersonArmRenderDispatcher.resetTransientRenderState();
         } else {
            boolean rendered = false;

            try {
               rendered = NanosuitFirstPersonArmRenderDispatcher.renderArm(event);
            } catch (Throwable var9) {
               LOGGER.warn("[CrysisMod] Nanosuit arm render failed, fallback to vanilla hand", var9);
            } finally {
               NanosuitFirstPersonArmRenderDispatcher.resetTransientRenderState();
            }

            if (rendered) {
               event.setCanceled(true);
            }
         }
      }
   }
}
