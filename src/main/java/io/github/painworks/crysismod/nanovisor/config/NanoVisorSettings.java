package io.github.painworks.crysismod.nanovisor.config;

public final class NanoVisorSettings {
   private NanoVisorSettings() {
   }

   public static final class Debug {
      private Debug() {
      }

      public static boolean visorInfo() {
         return NanoVisorClientConfig.debugVisorInfo();
      }

      public static boolean targetingInfo() {
         return NanoVisorClientConfig.debugTargetingInfo();
      }

      public static boolean hudAnchors() {
         return NanoVisorClientConfig.debugHudAnchors();
      }

      public static double configScreenBackgroundOpacity() {
         return NanoVisorClientConfig.configScreenBackgroundOpacity();
      }
   }

   public static final class General {
      private General() {
      }

      public static boolean visorEnabled() {
         return NanoVisorClientConfig.visorEnabled();
      }

      public static NanoVisorToggleMode visorToggleMode() {
         return NanoVisorClientConfig.visorToggleMode();
      }

      public static boolean visorOverlayEnabled() {
         return NanoVisorClientConfig.visorOverlayEnabled();
      }
   }

   public static final class Hud {
      private Hud() {
      }

      public static boolean showDistanceToTarget() {
         return NanoVisorClientConfig.showDistanceToTarget();
      }

      public static boolean showDistanceToBlock() {
         return NanoVisorClientConfig.showDistanceToBlock();
      }

      public static boolean showTargetName() {
         return NanoVisorClientConfig.showTargetName();
      }

      public static double maxDistanceToTarget() {
         return NanoVisorClientConfig.maxDistanceToTarget();
      }

      public static double maxDistanceToBlock() {
         return NanoVisorClientConfig.maxDistanceToBlock();
      }

      public static boolean showEnergyPercent() {
         return NanoVisorClientConfig.showEnergyPercent();
      }

      public static double opacity() {
         return NanoVisorClientConfig.hudOpacity();
      }

      public static double scale() {
         return NanoVisorClientConfig.hudScale();
      }

      public static int offsetX() {
         return NanoVisorClientConfig.hudOffsetX();
      }

      public static int offsetY() {
         return NanoVisorClientConfig.hudOffsetY();
      }

      public static double visorZoomArrowStartX() {
         return NanoVisorClientConfig.visorZoomArrowStartX();
      }

      public static double visorZoomArrowStartY() {
         return NanoVisorClientConfig.visorZoomArrowStartY();
      }

      public static double visorZoomArrowEndX() {
         return NanoVisorClientConfig.visorZoomArrowEndX();
      }

      public static double visorZoomArrowEndY() {
         return NanoVisorClientConfig.visorZoomArrowEndY();
      }

      public static boolean vignetteEnabled() {
         return NanoVisorClientConfig.hudVignetteEnabled();
      }

      public static double vignetteIntensity() {
         return NanoVisorClientConfig.hudVignetteIntensity();
      }

      public static double vignetteStrength() {
         return NanoVisorClientConfig.hudVignetteStrength();
      }

      public static double vignetteSizeToCenter() {
         return NanoVisorClientConfig.hudVignetteSizeToCenter();
      }
   }

   public static final class Targeting {
      private Targeting() {
      }

      public static boolean markTargetEnabled() {
         return NanoVisorClientConfig.markTargetEnabled();
      }

      public static double markRange() {
         return NanoVisorClientConfig.markRange();
      }

      public static int markGlowDurationTicks() {
         return NanoVisorClientConfig.markGlowDurationTicks();
      }

      public static boolean markOnlyLivingEntities() {
         return NanoVisorClientConfig.markOnlyLivingEntities();
      }

      public static boolean markRequireLineOfSight() {
         return NanoVisorClientConfig.markRequireLineOfSight();
      }

      public static int maxMarkedTargets() {
         return NanoVisorClientConfig.maxMarkedTargets();
      }
   }

   public static final class Zoom {
      private Zoom() {
      }

      public static boolean enabled() {
         return NanoVisorClientConfig.zoomEnabled();
      }

      public static double minFovMultiplier() {
         return NanoVisorClientConfig.zoomMinFovMultiplier();
      }

      public static double maxFovMultiplier() {
         return NanoVisorClientConfig.zoomMaxFovMultiplier();
      }

      public static double smoothness() {
         return NanoVisorClientConfig.zoomSmoothness();
      }

      public static boolean holdToUse() {
         return NanoVisorClientConfig.zoomHoldToUse();
      }

      public static double lookSensitivityMultiplier() {
         return NanoVisorClientConfig.zoomLookSensitivityMultiplier();
      }
   }
}
