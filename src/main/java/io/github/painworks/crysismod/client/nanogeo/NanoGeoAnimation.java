package io.github.painworks.crysismod.client.nanogeo;

import java.util.Map;

public final class NanoGeoAnimation {
   private final String name;
   private final double lengthSeconds;
   private final Map<String, NanoGeoAnimationChannel> channelsByBone;

   public NanoGeoAnimation(String name, double lengthSeconds, Map<String, NanoGeoAnimationChannel> channelsByBone) {
      this.name = name;
      this.lengthSeconds = lengthSeconds;
      this.channelsByBone = channelsByBone;
   }

   public String name() {
      return this.name;
   }

   public double lengthSeconds() {
      return this.lengthSeconds;
   }

   public Map<String, NanoGeoAnimationChannel> channelsByBone() {
      return this.channelsByBone;
   }
}
