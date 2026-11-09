package io.github.painworks.crysismod.nanosuit.client.render;

import io.github.painworks.crysismod.client.nanogeo.NanoGeoClientConfig;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;

final class NanosuitArmorModeOverlayTransitionState {
   private static final Map<UUID, NanosuitArmorModeOverlayTransitionState.VisualState> STATES = new ConcurrentHashMap<>();

   private NanosuitArmorModeOverlayTransitionState() {
   }

   static float resolveProgress(Player player, boolean armorModeActive) {
      if (player == null) {
         return armorModeActive ? 1.0F : 0.0F;
      } else if (!NanoGeoClientConfig.armorModeTransitionEnabled()) {
         UUID playerId = player.getUUID();
         if (armorModeActive) {
            STATES.remove(playerId);
            return 1.0F;
         } else {
            STATES.remove(playerId);
            return 0.0F;
         }
      } else {
         double nowSeconds = Util.getMillis() / 1000.0;
         float durationSeconds = Math.max(NanoGeoClientConfig.armorModeTransitionDurationSeconds(), 0.1F);
         float target = armorModeActive ? 1.0F : 0.0F;
         NanosuitArmorModeOverlayTransitionState.VisualState state = STATES.computeIfAbsent(
            player.getUUID(), ignored -> new NanosuitArmorModeOverlayTransitionState.VisualState(armorModeActive ? 0.0F : 0.0F, target, nowSeconds)
         );
         float progress = state.advance(nowSeconds, target, durationSeconds);
         if (!armorModeActive && progress <= 1.0E-4F) {
            STATES.remove(player.getUUID());
            return 0.0F;
         } else {
            return progress;
         }
      }
   }

   private static final class VisualState {
      private float progress;
      private float target;
      private double lastTimeSeconds;

      private VisualState(float initialProgress, float initialTarget, double nowSeconds) {
         this.progress = initialProgress;
         this.target = initialTarget;
         this.lastTimeSeconds = nowSeconds;
      }

      private float advance(double nowSeconds, float nextTarget, float durationSeconds) {
         double deltaSeconds = Math.max(0.0, Math.min(nowSeconds - this.lastTimeSeconds, 0.25));
         this.lastTimeSeconds = nowSeconds;
         this.target = nextTarget;
         float rate = 1.0F / durationSeconds;
         if (this.progress < this.target) {
            this.progress = Math.min(this.target, this.progress + (float)deltaSeconds * rate);
         } else if (this.progress > this.target) {
            this.progress = Math.max(this.target, this.progress - (float)deltaSeconds * rate);
         }

         return this.progress;
      }
   }
}
