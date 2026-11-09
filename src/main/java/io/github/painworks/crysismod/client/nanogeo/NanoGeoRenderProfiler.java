package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.logging.LogUtils;
import java.util.Locale;
import org.slf4j.Logger;

public final class NanoGeoRenderProfiler {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String ARMOR_CHANNEL = "armor";
   private static final String STATIC_CHANNEL = "static";
   private static final NanoGeoRenderProfiler.ChannelStats ARMOR_STATS = new NanoGeoRenderProfiler.ChannelStats("armor");
   private static final NanoGeoRenderProfiler.ChannelStats STATIC_STATS = new NanoGeoRenderProfiler.ChannelStats("static");

   private NanoGeoRenderProfiler() {
   }

   public static NanoGeoRenderProfiler.RenderPass beginArmorPass() {
      return beginPass(ARMOR_STATS);
   }

   public static NanoGeoRenderProfiler.RenderPass beginStaticPass() {
      return !NanoGeoClientConfig.nanoGeoDebugPerfLogStatic() ? NanoGeoRenderProfiler.RenderPass.DISABLED : beginPass(STATIC_STATS);
   }

   private static NanoGeoRenderProfiler.RenderPass beginPass(NanoGeoRenderProfiler.ChannelStats channel) {
      return !NanoGeoClientConfig.nanoGeoDebugPerfEnabled()
         ? NanoGeoRenderProfiler.RenderPass.DISABLED
         : new NanoGeoRenderProfiler.RenderPass(System.nanoTime(), channel);
   }

   public static void endPass(NanoGeoRenderProfiler.RenderPass pass, NanoGeoRenderProfiler.NanoGeoRenderMetrics metrics) {
      if (pass.enabled()) {
         long elapsedNs = System.nanoTime() - pass.startNs();
         pass.channel().record(elapsedNs, metrics);
      }
   }

   private static final class ChannelStats {
      private final String channelName;
      private long passCount;
      private long totalNs;
      private long totalVisitedBones;
      private long totalRenderedBones;
      private long totalRenderedCubes;
      private long totalRotatedCubes;
      private long totalRenderedQuads;
      private long totalRenderedVertices;

      private ChannelStats(String channelName) {
         this.channelName = channelName;
      }

      private void record(long elapsedNs, NanoGeoRenderProfiler.NanoGeoRenderMetrics metrics) {
         this.passCount++;
         this.totalNs += elapsedNs;
         this.totalVisitedBones = this.totalVisitedBones + metrics.visitedBones;
         this.totalRenderedBones = this.totalRenderedBones + metrics.renderedBones;
         this.totalRenderedCubes = this.totalRenderedCubes + metrics.renderedCubes;
         this.totalRotatedCubes = this.totalRotatedCubes + metrics.rotatedCubes;
         this.totalRenderedQuads = this.totalRenderedQuads + metrics.renderedQuads;
         this.totalRenderedVertices = this.totalRenderedVertices + metrics.renderedVertices;
         int interval = Math.max(1, NanoGeoClientConfig.nanoGeoDebugPerfLogInterval());
         if (this.passCount % interval == 0L) {
            double avgMs = this.totalNs / 1000000.0 / this.passCount;
            double avgVisitedBones = (double)this.totalVisitedBones / this.passCount;
            double avgRenderedBones = (double)this.totalRenderedBones / this.passCount;
            double avgCubes = (double)this.totalRenderedCubes / this.passCount;
            double avgRotatedCubes = (double)this.totalRotatedCubes / this.passCount;
            double avgQuads = (double)this.totalRenderedQuads / this.passCount;
            double avgVertices = (double)this.totalRenderedVertices / this.passCount;
            NanoGeoRenderProfiler.LOGGER
               .info(
                  "[CrysisMod][NanoGeo][Perf:{}] passes={} avgMs={}; avgVisitedBones={}; avgRenderedBones={}; avgCubes={}; avgRotCubes={}; avgQuads={}; avgVerts={}",
                  new Object[]{
                     this.channelName,
                     this.passCount,
                     this.format(avgMs),
                     this.format(avgVisitedBones),
                     this.format(avgRenderedBones),
                     this.format(avgCubes),
                     this.format(avgRotatedCubes),
                     this.format(avgQuads),
                     this.format(avgVertices)
                  }
               );
         }
      }

      private String format(double value) {
         return String.format(Locale.ROOT, "%.3f", value);
      }
   }

   public static final class NanoGeoRenderMetrics {
      int visitedBones;
      int renderedBones;
      int renderedCubes;
      int rotatedCubes;
      int renderedQuads;
      int renderedVertices;

      void reset() {
         this.visitedBones = 0;
         this.renderedBones = 0;
         this.renderedCubes = 0;
         this.rotatedCubes = 0;
         this.renderedQuads = 0;
         this.renderedVertices = 0;
      }
   }

   public record RenderPass(long startNs, NanoGeoRenderProfiler.ChannelStats channel, boolean enabled) {
      private static final NanoGeoRenderProfiler.RenderPass DISABLED = new NanoGeoRenderProfiler.RenderPass(0L, null, false);

      private RenderPass(long startNs, NanoGeoRenderProfiler.ChannelStats channel) {
         this(startNs, channel, true);
      }
   }
}
