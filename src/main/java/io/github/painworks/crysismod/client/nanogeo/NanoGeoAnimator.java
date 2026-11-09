package io.github.painworks.crysismod.client.nanogeo;

import java.util.Optional;

public final class NanoGeoAnimator {
   private NanoGeoAnimation activeAnimation;
   private double localTimeSeconds;

   public void setAnimation(NanoGeoAnimation animation) {
      this.activeAnimation = animation;
      this.localTimeSeconds = 0.0;
   }

   public void clearAnimation() {
      this.activeAnimation = null;
      this.localTimeSeconds = 0.0;
   }

   public Optional<NanoGeoAnimation> activeAnimation() {
      return Optional.ofNullable(this.activeAnimation);
   }

   public double localTimeSeconds() {
      return this.localTimeSeconds;
   }

   public void tick(double deltaSeconds) {
      if (this.activeAnimation != null) {
         this.localTimeSeconds += deltaSeconds;
         double len = Math.max(1.0E-4, this.activeAnimation.lengthSeconds());

         while (this.localTimeSeconds >= len) {
            this.localTimeSeconds -= len;
         }
      }
   }
}
