package io.github.painworks.crysismod.client.nanovision.preview;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

public final class NanovisionSettingsPreviewTarget {
   private static RenderTarget sceneTarget;
   private static RenderTarget markerTarget;
   private static int width;
   private static int height;

   private NanovisionSettingsPreviewTarget() {
   }

   public static RenderTarget prepareScene() {
      Minecraft minecraft = Minecraft.getInstance();
      RenderTarget scene = sceneTarget();
      RenderTarget marker = markerTarget();
      if (scene != null && marker != null && minecraft.getMainRenderTarget() != null) {
         marker.bindWrite(true);
         RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
         RenderSystem.clear(16640, Minecraft.ON_OSX);
         scene.bindWrite(true);
         RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
         RenderSystem.clear(16640, Minecraft.ON_OSX);
         RenderSystem.colorMask(true, true, true, true);
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         return scene;
      } else {
         return null;
      }
   }

   public static RenderTarget sceneTarget() {
      ensureTargets();
      return sceneTarget;
   }

   public static RenderTarget markerTarget() {
      ensureTargets();
      return markerTarget;
   }

   private static void ensureTargets() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.getMainRenderTarget() != null) {
         int targetWidth = minecraft.getMainRenderTarget().width;
         int targetHeight = minecraft.getMainRenderTarget().height;
         if (sceneTarget == null || markerTarget == null || width != targetWidth || height != targetHeight) {
            close();
            sceneTarget = new TextureTarget(targetWidth, targetHeight, true, Minecraft.ON_OSX);
            sceneTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            sceneTarget.setFilterMode(9729);
            markerTarget = new TextureTarget(targetWidth, targetHeight, true, Minecraft.ON_OSX);
            markerTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            width = targetWidth;
            height = targetHeight;
         }
      }
   }

   public static void close() {
      if (sceneTarget != null) {
         sceneTarget.destroyBuffers();
         sceneTarget = null;
      }

      if (markerTarget != null) {
         markerTarget.destroyBuffers();
         markerTarget = null;
      }

      width = 0;
      height = 0;
   }
}
