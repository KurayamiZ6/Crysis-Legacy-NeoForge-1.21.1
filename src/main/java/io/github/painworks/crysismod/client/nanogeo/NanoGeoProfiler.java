package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import org.slf4j.Logger;

public final class NanoGeoProfiler {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final NanoGeoProfiler INSTANCE = new NanoGeoProfiler();
   private final EnumMap<NanoGeoProfiler.PassType, NanoGeoProfiler.WindowStats> windows = new EnumMap<>(NanoGeoProfiler.PassType.class);
   private final ThreadLocal<NanoGeoProfiler.FrameState> frameState = ThreadLocal.withInitial(NanoGeoProfiler.FrameState::new);

   private NanoGeoProfiler() {
      for (NanoGeoProfiler.PassType passType : NanoGeoProfiler.PassType.values()) {
         this.windows.put(passType, new NanoGeoProfiler.WindowStats(passType));
      }
   }

   public boolean beginFrameIfNeeded(NanoGeoProfiler.PassType passType) {
      if (!NanoGeoClientConfig.nanoGeoPerfProfilingEnabled()) {
         return false;
      } else {
         NanoGeoProfiler.FrameState state = this.frameState.get();
         if (state.active) {
            return false;
         } else {
            state.resetForFrame(passType);
            return true;
         }
      }
   }

   public boolean isActive() {
      return this.frameState.get().active;
   }

   public void endFrameIfOwned(boolean owned) {
      if (owned && NanoGeoClientConfig.nanoGeoPerfProfilingEnabled()) {
         NanoGeoProfiler.FrameState state = this.frameState.get();
         if (state.active) {
            long elapsedNs = System.nanoTime() - state.frameStartNs;
            state.sectionTotalsNs[NanoGeoProfilerSection.TOTAL_RENDER.ordinal()] = elapsedNs;
            this.windows.get(state.passType).recordFrame(state);
            state.active = false;
         }
      }
   }

   public long beginSection(NanoGeoProfilerSection section) {
      NanoGeoProfiler.FrameState state = this.frameState.get();
      if (!state.active) {
         return 0L;
      } else {
         long startNs = System.nanoTime();
         state.sectionStartNs[section.ordinal()] = startNs;
         return startNs;
      }
   }

   public void endSection(NanoGeoProfilerSection section, long startNs) {
      NanoGeoProfiler.FrameState state = this.frameState.get();
      if (state.active && startNs != 0L) {
         state.sectionTotalsNs[section.ordinal()] += System.nanoTime() - startNs;
      }
   }

   public void incrementCounter(NanoGeoProfiler.Counter counter) {
      this.incrementCounter(counter, 1L);
   }

   public void incrementCounter(NanoGeoProfiler.Counter counter, long amount) {
      NanoGeoProfiler.FrameState state = this.frameState.get();
      if (state.active && amount != 0L) {
         state.counters[counter.ordinal()] += amount;
      }
   }

   public static enum Counter {
      RENDERED_BONES,
      SKIPPED_BONES,
      RENDERED_CUBES,
      SKIPPED_CUBES,
      RENDERED_QUADS,
      EMITTED_VERTICES,
      POSESTACK_PUSH,
      POSESTACK_POP,
      MATRIX_MULTIPLY,
      VISIBLE_BONE_LOOKUPS,
      STRING_BONE_LOOKUPS,
      MAP_LOOKUPS,
      RENDERTYPE_LOOKUPS,
      TEXTURE_LOOKUPS,
      MODEL_CACHE_HITS,
      MODEL_CACHE_MISSES,
      ALLOCATIONS_SUSPECTED;
   }

   private static final class FrameState {
      private boolean active;
      private NanoGeoProfiler.PassType passType;
      private long frameStartNs;
      private final long[] sectionStartNs = new long[NanoGeoProfilerSection.values().length];
      private final long[] sectionTotalsNs = new long[NanoGeoProfilerSection.values().length];
      private final long[] counters = new long[NanoGeoProfiler.Counter.values().length];

      private void resetForFrame(NanoGeoProfiler.PassType passType) {
         this.active = true;
         this.passType = passType;
         this.frameStartNs = System.nanoTime();
         Arrays.fill(this.sectionStartNs, 0L);
         Arrays.fill(this.sectionTotalsNs, 0L);
         Arrays.fill(this.counters, 0L);
      }
   }

   public static enum PassType {
      ARMOR,
      STATIC;
   }

   private static final class WindowStats {
      private final NanoGeoProfiler.PassType passType;
      private final EnumMap<NanoGeoProfilerSection, NanoGeoPerfStats> sectionStats = new EnumMap<>(NanoGeoProfilerSection.class);
      private final long[] counterTotals = new long[NanoGeoProfiler.Counter.values().length];
      private long lastLogNs;
      private long frameCount;

      private WindowStats(NanoGeoProfiler.PassType passType) {
         this.passType = passType;

         for (NanoGeoProfilerSection section : NanoGeoProfilerSection.values()) {
            this.sectionStats.put(section, new NanoGeoPerfStats());
         }

         this.lastLogNs = System.nanoTime();
      }

      private void recordFrame(NanoGeoProfiler.FrameState frame) {
         this.frameCount++;

         for (NanoGeoProfilerSection section : NanoGeoProfilerSection.values()) {
            this.sectionStats.get(section).addSample(frame.sectionTotalsNs[section.ordinal()]);
         }

         for (NanoGeoProfiler.Counter counter : NanoGeoProfiler.Counter.values()) {
            this.counterTotals[counter.ordinal()] += frame.counters[counter.ordinal()];
         }

         long now = System.nanoTime();
         long intervalNs = Math.max(1, NanoGeoClientConfig.nanoGeoPerfLogEverySeconds()) * 1000000000L;
         if (now - this.lastLogNs >= intervalNs) {
            this.lastLogNs = now;
            this.logSnapshot();
         }
      }

      private void logSnapshot() {
         NanoGeoPerfStats total = this.sectionStats.get(NanoGeoProfilerSection.TOTAL_RENDER);
         NanoGeoPerfStats pose = this.sectionStats.get(NanoGeoProfilerSection.POSE_MAPPING);
         NanoGeoPerfStats visibility = this.sectionStats.get(NanoGeoProfilerSection.SLOT_VISIBILITY);
         NanoGeoPerfStats traversal = this.sectionStats.get(NanoGeoProfilerSection.BONE_TRAVERSAL);
         NanoGeoPerfStats cube = this.sectionStats.get(NanoGeoProfilerSection.CUBE_RENDER);
         NanoGeoPerfStats vertex = this.sectionStats.get(NanoGeoProfilerSection.VERTEX_EMISSION);
         NanoGeoPerfStats modelCache = this.sectionStats.get(NanoGeoProfilerSection.MODEL_CACHE_LOOKUP);
         NanoGeoPerfStats bufferLookup = this.sectionStats.get(NanoGeoProfilerSection.BUFFER_LOOKUP);
         String prefix = NanoGeoClientConfig.nanoGeoPerfCompareMode() ? "[COMPARE]" : "";
         NanoGeoProfiler.LOGGER
            .info(
               "[CrysisMod][NanoGeo][Perf{}:{}] total({}) modelCache({}) visibility({}) pose({}) traversal({}) cubes({}) verts({}) buffer({}); counters={} ",
               new Object[]{
                  prefix,
                  this.passType.name().toLowerCase(Locale.ROOT),
                  total.formatMsLine(),
                  modelCache.formatMsLine(),
                  visibility.formatMsLine(),
                  pose.formatMsLine(),
                  traversal.formatMsLine(),
                  cube.formatMsLine(),
                  vertex.formatMsLine(),
                  bufferLookup.formatMsLine(),
                  this.formatCounters()
               }
            );
      }

      private String formatCounters() {
         long sampleCount = Math.max(1L, this.frameCount);
         return "bones="
            + this.counterTotals[NanoGeoProfiler.Counter.RENDERED_BONES.ordinal()]
            + ", skippedBones="
            + this.counterTotals[NanoGeoProfiler.Counter.SKIPPED_BONES.ordinal()]
            + ", cubes="
            + this.counterTotals[NanoGeoProfiler.Counter.RENDERED_CUBES.ordinal()]
            + ", skippedCubes="
            + this.counterTotals[NanoGeoProfiler.Counter.SKIPPED_CUBES.ordinal()]
            + ", quads="
            + this.counterTotals[NanoGeoProfiler.Counter.RENDERED_QUADS.ordinal()]
            + ", verts="
            + this.counterTotals[NanoGeoProfiler.Counter.EMITTED_VERTICES.ordinal()]
            + ", push="
            + this.counterTotals[NanoGeoProfiler.Counter.POSESTACK_PUSH.ordinal()]
            + ", pop="
            + this.counterTotals[NanoGeoProfiler.Counter.POSESTACK_POP.ordinal()]
            + ", mapLookups/frame="
            + String.format(Locale.ROOT, "%.2f", (double)this.counterTotals[NanoGeoProfiler.Counter.MAP_LOOKUPS.ordinal()] / sampleCount)
            + ", stringLookups/frame="
            + String.format(Locale.ROOT, "%.2f", (double)this.counterTotals[NanoGeoProfiler.Counter.STRING_BONE_LOOKUPS.ordinal()] / sampleCount)
            + ", visibleLookups/frame="
            + String.format(Locale.ROOT, "%.2f", (double)this.counterTotals[NanoGeoProfiler.Counter.VISIBLE_BONE_LOOKUPS.ordinal()] / sampleCount)
            + ", renderTypeLookups/frame="
            + String.format(Locale.ROOT, "%.2f", (double)this.counterTotals[NanoGeoProfiler.Counter.RENDERTYPE_LOOKUPS.ordinal()] / sampleCount)
            + ", textureLookups/frame="
            + String.format(Locale.ROOT, "%.2f", (double)this.counterTotals[NanoGeoProfiler.Counter.TEXTURE_LOOKUPS.ordinal()] / sampleCount)
            + ", cacheHits="
            + this.counterTotals[NanoGeoProfiler.Counter.MODEL_CACHE_HITS.ordinal()]
            + ", cacheMisses="
            + this.counterTotals[NanoGeoProfiler.Counter.MODEL_CACHE_MISSES.ordinal()]
            + ", allocSuspected="
            + this.counterTotals[NanoGeoProfiler.Counter.ALLOCATIONS_SUSPECTED.ordinal()];
      }
   }
}
