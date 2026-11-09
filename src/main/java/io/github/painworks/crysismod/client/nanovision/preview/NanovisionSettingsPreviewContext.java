package io.github.painworks.crysismod.client.nanovision.preview;

import com.mojang.blaze3d.pipeline.RenderTarget;

public final class NanovisionSettingsPreviewContext {
   private static int renderDepth;
   private static RenderTarget sceneTarget;
   private static RenderTarget markerTarget;
   private static float encodedProjectedHeight;

   private NanovisionSettingsPreviewContext() {
   }

   public static void begin(RenderTarget scene, RenderTarget marker, float projectedHeight) {
      if (renderDepth == 0) {
         sceneTarget = scene;
         markerTarget = marker;
         encodedProjectedHeight = projectedHeight;
      }

      renderDepth++;
   }

   public static void end() {
      renderDepth = Math.max(0, renderDepth - 1);
      if (renderDepth == 0) {
         sceneTarget = null;
         markerTarget = null;
         encodedProjectedHeight = 0.0F;
      }
   }

   public static boolean isRendering() {
      return renderDepth > 0;
   }

   public static RenderTarget sceneTarget() {
      return isRendering() ? sceneTarget : null;
   }

   public static RenderTarget markerTarget() {
      return isRendering() ? markerTarget : null;
   }

   public static float encodedProjectedHeight() {
      return Math.max(0.01F, encodedProjectedHeight);
   }
}
