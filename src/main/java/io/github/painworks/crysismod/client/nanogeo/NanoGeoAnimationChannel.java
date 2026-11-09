package io.github.painworks.crysismod.client.nanogeo;

import java.util.List;

public final class NanoGeoAnimationChannel {
   private final List<NanoGeoKeyframe> rotationKeyframes;
   private final List<NanoGeoKeyframe> positionKeyframes;
   private final List<NanoGeoKeyframe> scaleKeyframes;

   public NanoGeoAnimationChannel(List<NanoGeoKeyframe> rotationKeyframes, List<NanoGeoKeyframe> positionKeyframes, List<NanoGeoKeyframe> scaleKeyframes) {
      this.rotationKeyframes = rotationKeyframes;
      this.positionKeyframes = positionKeyframes;
      this.scaleKeyframes = scaleKeyframes;
   }

   public List<NanoGeoKeyframe> rotationKeyframes() {
      return this.rotationKeyframes;
   }

   public List<NanoGeoKeyframe> positionKeyframes() {
      return this.positionKeyframes;
   }

   public List<NanoGeoKeyframe> scaleKeyframes() {
      return this.scaleKeyframes;
   }
}
