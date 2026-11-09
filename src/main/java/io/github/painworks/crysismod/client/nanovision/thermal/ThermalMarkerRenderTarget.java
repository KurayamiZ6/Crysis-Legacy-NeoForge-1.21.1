package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.painworks.crysismod.client.nanovision.NanovisionController;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import io.github.painworks.crysismod.client.nanovision.debug.NanovisionPerformanceProfiler;
import io.github.painworks.crysismod.client.nanovision.preview.NanovisionSettingsPreviewContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.OutputStateShard;

public final class ThermalMarkerRenderTarget extends RenderStateShard {
   private static RenderTarget markerTarget;
   private static int width;
   private static int height;
   private static boolean sourcesRenderedThisFrame;
   private static long activeBatchCpuStarted;
   private static int activeBatchGpuQuery;
   public static final OutputStateShard OUTPUT = new OutputStateShard("crysismod_nanovision_marker_target", () -> {
      activeBatchCpuStarted = NanovisionPerformanceProfiler.beginCpu();
      activeBatchGpuQuery = NanovisionPerformanceProfiler.beginGpu(NanovisionPerformanceProfiler.Section.MARKER_BATCH_GPU);
      RenderTarget target = NanovisionSettingsPreviewContext.isRendering() ? NanovisionSettingsPreviewContext.markerTarget() : getOrCreate();
      if (target != null) {
         target.bindWrite(false);
      }
   }, () -> {
      NanovisionPerformanceProfiler.endGpu(NanovisionPerformanceProfiler.Section.MARKER_BATCH_GPU, activeBatchGpuQuery);
      NanovisionPerformanceProfiler.endCpu(NanovisionPerformanceProfiler.Section.MARKER_BATCH, activeBatchCpuStarted);
      activeBatchGpuQuery = 0;
      activeBatchCpuStarted = 0L;
      Minecraft minecraft = Minecraft.getInstance();
      RenderTarget previewScene = NanovisionSettingsPreviewContext.sceneTarget();
      if (previewScene != null) {
         previewScene.bindWrite(false);
      } else if (minecraft.getMainRenderTarget() != null) {
         minecraft.getMainRenderTarget().bindWrite(false);
      }

      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
   });

   private ThermalMarkerRenderTarget() {
      super("nanovision_marker_render_target", () -> {}, () -> {});
   }

   public static void prepareFrame(Minecraft minecraft) {
      sourcesRenderedThisFrame = false;
      if (minecraft.player != null && NanovisionState.isActive(minecraft.player) && NanovisionController.shouldRenderMarkerPass()) {
         RenderTarget target = getOrCreate();
         if (target != null) {
            long cpuStarted = NanovisionPerformanceProfiler.beginCpu();
            int gpuQuery = NanovisionPerformanceProfiler.beginGpu(NanovisionPerformanceProfiler.Section.MARKER_TARGET_PREPARE_GPU);
            target.bindWrite(false);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
            RenderSystem.clear(16640, Minecraft.ON_OSX);
            minecraft.getMainRenderTarget().bindWrite(false);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            NanovisionPerformanceProfiler.endGpu(NanovisionPerformanceProfiler.Section.MARKER_TARGET_PREPARE_GPU, gpuQuery);
            NanovisionPerformanceProfiler.endCpu(NanovisionPerformanceProfiler.Section.MARKER_TARGET_PREPARE, cpuStarted);
         }
      } else {
         closeForInactiveFrame();
      }
   }

   public static RenderTarget getOrCreate() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.getWindow() != null && minecraft.getMainRenderTarget() != null) {
         int targetWidth = minecraft.getWindow().getWidth();
         int targetHeight = minecraft.getWindow().getHeight();
         if (markerTarget != null && targetWidth == width && targetHeight == height) {
            return markerTarget;
         } else {
            close();
            markerTarget = new TextureTarget(targetWidth, targetHeight, true, Minecraft.ON_OSX);
            markerTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            width = targetWidth;
            height = targetHeight;
            return markerTarget;
         }
      } else {
         return null;
      }
   }

   public static int colorTextureId() {
      RenderTarget target = getOrCreate();
      return target == null ? 0 : target.getColorTextureId();
   }

   public static int depthTextureId() {
      RenderTarget target = getOrCreate();
      return target == null ? 0 : target.getDepthTextureId();
   }

   public static int width() {
      RenderTarget target = getOrCreate();
      return target == null ? 1 : target.width;
   }

   public static int height() {
      RenderTarget target = getOrCreate();
      return target == null ? 1 : target.height;
   }

   public static void markSourceRendered() {
      if (!NanovisionSettingsPreviewContext.isRendering()) {
         sourcesRenderedThisFrame = true;
      }
   }

   public static boolean hasSourcesRenderedThisFrame() {
      return sourcesRenderedThisFrame;
   }

   public static void close() {
      if (markerTarget != null) {
         markerTarget.destroyBuffers();
         markerTarget = null;
      }

      width = 0;
      height = 0;
      sourcesRenderedThisFrame = false;
   }

   private static void closeForInactiveFrame() {
      if (markerTarget == null) {
         close();
      } else {
         ThermalFramebufferState var0 = ThermalFramebufferState.capture();
         close();
         var0.restore();
      }
   }
}
