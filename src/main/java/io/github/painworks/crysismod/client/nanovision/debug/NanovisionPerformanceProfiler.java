package io.github.painworks.crysismod.client.nanovision.debug;

import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.ThermalVisualPreset;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerTextureAtlas;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GLCapabilities;
import org.slf4j.Logger;

public final class NanovisionPerformanceProfiler {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int DEFAULT_FRAMES = 300;
   private static final int MIN_FRAMES = 60;
   private static final int MAX_FRAMES = 3600;
   private static final long GPU_DRAIN_TIMEOUT_NANOS = 5000000000L;
   private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
   private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
   private static final EnumMap<NanovisionPerformanceProfiler.Section, NanovisionPerformanceProfiler.TimingStats> CPU_STATS = new EnumMap<>(
      NanovisionPerformanceProfiler.Section.class
   );
   private static final EnumMap<NanovisionPerformanceProfiler.Section, NanovisionPerformanceProfiler.TimingStats> GPU_STATS = new EnumMap<>(
      NanovisionPerformanceProfiler.Section.class
   );
   private static final NanovisionPerformanceProfiler.TimingStats FRAME_INTERVALS = new NanovisionPerformanceProfiler.TimingStats();
   private static final ArrayDeque<NanovisionPerformanceProfiler.PendingGpuQuery> PENDING_GPU_QUERIES = new ArrayDeque<>();
   private static final Map<String, NanovisionPerformanceProfiler.MobModelLoadStats> MOB_MODEL_LOADS = new LinkedHashMap<>();
   private static boolean capturing;
   private static boolean draining;
   private static boolean gpuSupportChecked;
   private static boolean gpuTimerSupported;
   private static boolean gpuTimerFailed;
   private static NanovisionPerformanceProfiler.CaptureMode captureMode = NanovisionPerformanceProfiler.CaptureMode.FULL_NANOVISION;
   private static int targetFrames = 300;
   private static int capturedFrames;
   private static int thermalSourceFrames;
   private static int nextProgressPercent;
   private static boolean firstPersonFramePending;
   private static long lastFrameStartNanos;
   private static long drainStartedNanos;
   private static ThermalVisualPreset preset;
   private static int width;
   private static int height;
   private static String gpuVendor = "unknown";
   private static String gpuRenderer = "unknown";
   private static String gpuVersion = "unknown";
   private static String gpuFailureReason = "none";
   private static String lastReport = "No NanoVision profile has been captured yet.";

   private NanovisionPerformanceProfiler() {
   }

   public static int defaultFrames() {
      return 300;
   }

   public static String start(int requestedFrames) {
      return start(requestedFrames, NanovisionPerformanceProfiler.CaptureMode.FULL_NANOVISION);
   }

   public static String startMobModels(int requestedFrames) {
      return start(requestedFrames, NanovisionPerformanceProfiler.CaptureMode.MOB_MODELS_ONLY);
   }

   public static String startFirstPerson(int requestedFrames) {
      return start(requestedFrames, NanovisionPerformanceProfiler.CaptureMode.FIRST_PERSON_ONLY);
   }

   private static String start(int requestedFrames, NanovisionPerformanceProfiler.CaptureMode requestedMode) {
      clearPendingQueries();
      CPU_STATS.clear();
      GPU_STATS.clear();
      FRAME_INTERVALS.clear();
      MOB_MODEL_LOADS.clear();
      captureMode = requestedMode;
      targetFrames = Math.max(60, Math.min(3600, requestedFrames));
      capturedFrames = 0;
      thermalSourceFrames = 0;
      nextProgressPercent = 10;
      firstPersonFramePending = false;
      lastFrameStartNanos = 0L;
      preset = null;
      width = 0;
      height = 0;
      capturing = true;
      draining = false;
      gpuSupportChecked = false;
      gpuTimerSupported = false;
      gpuTimerFailed = false;
      drainStartedNanos = 0L;
      gpuFailureReason = "none";
      if (requestedMode == NanovisionPerformanceProfiler.CaptureMode.MOB_MODELS_ONLY) {
         return "NanoVision mob-model profiler started for " + targetFrames + " frames. It measures only entity thermal models and their marker batches.";
      } else {
         return requestedMode == NanovisionPerformanceProfiler.CaptureMode.FIRST_PERSON_ONLY
            ? "NanoVision first-person profiler started for "
               + targetFrames
               + " hand frames. Hold the same item still; run once with NanoVision off and once with it on."
            : "NanoVision profiler started for " + targetFrames + " rendered NanoVision frames. Keep NanoVision enabled until the report appears.";
      }
   }

   public static String requestStop() {
      if (!capturing && !draining) {
         return "NanoVision profiler is not running.";
      } else {
         capturing = false;
         draining = true;
         beginDrain();
         finishIfDrained();
         return "NanoVision profiler stopped at " + capturedFrames + " frames; draining asynchronous GPU queries.";
      }
   }

   public static String status() {
      if (capturing) {
         return "NanoVision profiler: capturing " + capturedFrames + "/" + targetFrames + " frames, pending GPU queries=" + PENDING_GPU_QUERIES.size() + ".";
      } else {
         return draining
            ? "NanoVision profiler: capture complete, pending GPU queries=" + PENDING_GPU_QUERIES.size() + "."
            : "NanoVision profiler idle. Last report: " + Minecraft.getInstance().gameDirectory.toPath().resolve("nanovision-profile-latest.txt");
      }
   }

   public static String lastReport() {
      return lastReport;
   }

   public static boolean isCapturing() {
      return capturing;
   }

   public static boolean isCapturingMobModels() {
      return capturing && captureMode == NanovisionPerformanceProfiler.CaptureMode.MOB_MODELS_ONLY;
   }

   public static void recordMobLayerModel(String entityType, String modelName, String route, long cpuNanos, int vertices) {
      if (isCapturingMobModels()) {
         CPU_STATS.computeIfAbsent(NanovisionPerformanceProfiler.Section.MOB_LAYER_MODEL, ignored -> new NanovisionPerformanceProfiler.TimingStats())
            .add(cpuNanos);
         recordMobLoad("layer", entityType, modelName, route, cpuNanos, vertices);
      }
   }

   public static void recordMobCompatibilityReplay(String entityType, String rendererName, long cpuNanos, int vertices) {
      if (isCapturingMobModels()) {
         CPU_STATS.computeIfAbsent(NanovisionPerformanceProfiler.Section.MOB_COMPATIBILITY_ENTITY, ignored -> new NanovisionPerformanceProfiler.TimingStats())
            .add(cpuNanos);
         recordMobLoad("compatibility", entityType, rendererName, "full_renderer_replay", cpuNanos, vertices);
      }
   }

   public static void beginFrame(ThermalVisualPreset activePreset, int frameWidth, int frameHeight, boolean hasThermalSources) {
      pollGpuQueries();
      if (!capturing) {
         finishIfDrained();
      } else if (captureMode != NanovisionPerformanceProfiler.CaptureMode.FIRST_PERSON_ONLY) {
         beginCapturedFrame(activePreset, frameWidth, frameHeight, hasThermalSources);
      }
   }

   public static void beginFirstPersonFrame(int frameWidth, int frameHeight, boolean thermalActive) {
      pollGpuQueries();
      if (!capturing) {
         finishIfDrained();
      } else if (captureMode == NanovisionPerformanceProfiler.CaptureMode.FIRST_PERSON_ONLY) {
         beginCapturedFrame(NanovisionConfig.thermalVisualPreset(), frameWidth, frameHeight, thermalActive);
         firstPersonFramePending = true;
      }
   }

   public static void endFirstPersonFrame() {
      if (capturing && captureMode == NanovisionPerformanceProfiler.CaptureMode.FIRST_PERSON_ONLY && firstPersonFramePending) {
         firstPersonFramePending = false;
         finishCapturedFrame();
      }
   }

   private static void beginCapturedFrame(ThermalVisualPreset activePreset, int frameWidth, int frameHeight, boolean hasThermalSources) {
      long now = System.nanoTime();
      if (lastFrameStartNanos != 0L) {
         FRAME_INTERVALS.add(now - lastFrameStartNanos);
      }

      lastFrameStartNanos = now;
      preset = activePreset;
      width = frameWidth;
      height = frameHeight;
      if (hasThermalSources) {
         thermalSourceFrames++;
      }
   }

   public static void endFrame() {
      if (capturing && captureMode != NanovisionPerformanceProfiler.CaptureMode.FIRST_PERSON_ONLY) {
         finishCapturedFrame();
      }
   }

   private static void finishCapturedFrame() {
      capturedFrames++;
      reportProgressIfNeeded();
      if (capturedFrames >= targetFrames) {
         capturing = false;
         draining = true;
         beginDrain();
         finishIfDrained();
      }
   }

   public static long beginCpu() {
      return capturing ? System.nanoTime() : 0L;
   }

   public static void endCpu(NanovisionPerformanceProfiler.Section section, long startedNanos) {
      if (startedNanos != 0L && shouldRecordSection(section)) {
         CPU_STATS.computeIfAbsent(section, ignored -> new NanovisionPerformanceProfiler.TimingStats()).add(System.nanoTime() - startedNanos);
      }
   }

   public static int beginGpu(NanovisionPerformanceProfiler.Section section) {
      if (capturing && shouldRecordSection(section) && !gpuTimerFailed && ensureGpuTimerSupport()) {
         int queryId = 0;

         try {
            queryId = GL15.glGenQueries();
            GL33.glQueryCounter(queryId, 36392);
            if (!GL15.glIsQuery(queryId)) {
               markGpuFailure("OpenGL rejected the start timestamp query.");
               deleteQuery(queryId);
               return 0;
            } else {
               return queryId;
            }
         } catch (RuntimeException var3) {
            deleteQuery(queryId);
            markGpuFailure("Failed to submit the start timestamp query: " + var3.getMessage());
            LOGGER.warn("[CrysisMod][NanovisionProfile] GPU start timestamp failed", var3);
            return 0;
         }
      } else {
         return 0;
      }
   }

   public static void endGpu(NanovisionPerformanceProfiler.Section section, int queryId) {
      if (queryId != 0) {
         int endQueryId = 0;

         try {
            endQueryId = GL15.glGenQueries();
            GL33.glQueryCounter(endQueryId, 36392);
            if (!GL15.glIsQuery(endQueryId)) {
               deleteQuery(queryId);
               deleteQuery(endQueryId);
               markGpuFailure("OpenGL rejected the end timestamp query.");
               return;
            }

            PENDING_GPU_QUERIES.addLast(new NanovisionPerformanceProfiler.PendingGpuQuery(queryId, endQueryId, section));
         } catch (RuntimeException var4) {
            deleteQuery(queryId);
            deleteQuery(endQueryId);
            markGpuFailure("Failed to submit the end timestamp query: " + var4.getMessage());
            LOGGER.warn("[CrysisMod][NanovisionProfile] GPU end timestamp failed", var4);
         }
      }
   }

   public static void pollGpuQueries() {
      if (gpuSupportChecked && gpuTimerSupported) {
         int queriesToCheck = PENDING_GPU_QUERIES.size();

         for (int i = 0; i < queriesToCheck; i++) {
            NanovisionPerformanceProfiler.PendingGpuQuery pending = PENDING_GPU_QUERIES.removeFirst();
            if (GL15.glIsQuery(pending.startQueryId()) && GL15.glIsQuery(pending.endQueryId())) {
               if (GL15.glGetQueryObjecti(pending.endQueryId(), 34919) == 0) {
                  PENDING_GPU_QUERIES.addLast(pending);
               } else {
                  long startedNanos = GL33.glGetQueryObjectui64(pending.startQueryId(), 34918);
                  long endedNanos = GL33.glGetQueryObjectui64(pending.endQueryId(), 34918);
                  deleteQuery(pending.startQueryId());
                  deleteQuery(pending.endQueryId());
                  long elapsedNanos = Math.max(0L, endedNanos - startedNanos);
                  GPU_STATS.computeIfAbsent(pending.section(), ignored -> new NanovisionPerformanceProfiler.TimingStats()).add(elapsedNanos);
               }
            } else {
               deleteQuery(pending.startQueryId());
               deleteQuery(pending.endQueryId());
               markGpuFailure("OpenGL invalidated a pending timestamp query.");
            }
         }

         if (draining && !PENDING_GPU_QUERIES.isEmpty() && drainStartedNanos != 0L && System.nanoTime() - drainStartedNanos >= 5000000000L) {
            markGpuFailure("GPU timestamp queries did not drain within 5 seconds; saved available GPU and CPU timings.");
            clearPendingQueries();
         }

         finishIfDrained();
      }
   }

   private static boolean ensureGpuTimerSupport() {
      if (gpuSupportChecked) {
         return gpuTimerSupported;
      } else {
         gpuSupportChecked = true;

         try {
            GLCapabilities capabilities = GL.getCapabilities();
            gpuTimerSupported = capabilities.OpenGL33 || capabilities.GL_ARB_timer_query;
            gpuVendor = safeGlString(7936);
            gpuRenderer = safeGlString(7937);
            gpuVersion = safeGlString(7938);
         } catch (RuntimeException var1) {
            gpuTimerSupported = false;
            LOGGER.warn("[CrysisMod][NanovisionProfile] GPU timer query detection failed", var1);
         }

         return gpuTimerSupported;
      }
   }

   private static String safeGlString(int name) {
      String value = GL11.glGetString(name);
      return value == null ? "unknown" : value;
   }

   private static void finishIfDrained() {
      if (draining && PENDING_GPU_QUERIES.isEmpty()) {
         draining = false;
         lastReport = buildReport();
         LOGGER.info("\n{}", lastReport);
         Path reportPath = Minecraft.getInstance().gameDirectory.toPath().resolve("nanovision-profile-latest.txt");
         String presetName = preset == null ? "unknown" : preset.name().toLowerCase(Locale.ROOT);
         Path archivedReportPath = Minecraft.getInstance()
            .gameDirectory
            .toPath()
            .resolve("nanovision-profile-" + presetName + "-" + LocalDateTime.now().format(FILE_TIME_FORMAT) + ".txt");

         try {
            Files.writeString(reportPath, lastReport, StandardCharsets.UTF_8);
            Files.writeString(archivedReportPath, lastReport, StandardCharsets.UTF_8);
         } catch (IOException var4) {
            LOGGER.warn("[CrysisMod][NanovisionProfile] failed to write report", var4);
         }

         if (Minecraft.getInstance().gui != null) {
            Minecraft.getInstance()
               .gui
               .getChat()
               .addMessage(Component.literal("NanoVision profile complete: " + capturedFrames + " frames. Report: " + reportPath));
         }
      }
   }

   private static void reportProgressIfNeeded() {
      if (targetFrames > 0 && nextProgressPercent <= 100) {
         int progress = (int)(capturedFrames * 100L / targetFrames);
         if (progress >= nextProgressPercent) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.gui != null) {
               String title = switch (captureMode) {
                  case MOB_MODELS_ONLY -> "NanoVision mob-model profile";
                  case FIRST_PERSON_ONLY -> "NanoVision first-person profile";
                  case FULL_NANOVISION -> "NanoVision profile";
               };
               minecraft.gui.getChat().addMessage(Component.literal(title + ": " + nextProgressPercent + "%"));
            }

            nextProgressPercent += 10;
         }
      }
   }

   private static String buildReport() {
      StringBuilder report = new StringBuilder(4096);
      report.append("NanoVision performance profile\n");
      report.append("captured_at=").append(LocalDateTime.now().format(TIME_FORMAT)).append('\n');
      report.append("preset=").append(preset).append('\n');
      report.append("capture_mode=").append(captureMode).append('\n');
      report.append("shaderpack_active=").append(NanoGeoShaderpackCompatibility.isShaderPackActive()).append('\n');
      report.append("quality=").append(NanovisionConfig.thermalMarkerQuality()).append('\n');
      report.append("resolution=").append(width).append('x').append(height).append('\n');
      report.append("frames=").append(capturedFrames).append('\n');
      report.append("thermal_source_frames=").append(thermalSourceFrames).append('/').append(capturedFrames).append('\n');
      report.append("gpu_timer_supported=").append(gpuTimerSupported).append('\n');
      report.append("gpu_timer_backend=timestamp_pairs\n");
      report.append("gpu_timer_failed=").append(gpuTimerFailed).append('\n');
      report.append("gpu_failure_reason=").append(gpuFailureReason).append('\n');
      report.append("gpu_vendor=").append(gpuVendor).append('\n');
      report.append("gpu_renderer=").append(gpuRenderer).append('\n');
      report.append("gpu_version=").append(gpuVersion).append('\n');
      report.append("marker_atlas_pages=").append(ThermalMarkerTextureAtlas.INSTANCE.pageCount()).append('\n');
      report.append("marker_atlas_regions=").append(ThermalMarkerTextureAtlas.INSTANCE.regionCount()).append('\n');
      report.append("marker_atlas_fallback_textures=").append(ThermalMarkerTextureAtlas.INSTANCE.fallbackTextureCount()).append('\n');
      report.append("profiling_note=GPU queries are asynchronous; CPU pass timings include profiling submission overhead.\n");
      if (FRAME_INTERVALS.count() > 0) {
         double frameMs = FRAME_INTERVALS.averageMillis();
         report.append(
            String.format(
               Locale.ROOT,
               "observed_frame_interval_ms=%.4f observed_fps=%.2f frame_p95_ms=%.4f%n",
               frameMs,
               frameMs > 0.0 ? 1000.0 / frameMs : 0.0,
               FRAME_INTERVALS.percentileMillis(0.95)
            )
         );
      }

      appendStats(report, "CPU", CPU_STATS);
      appendStats(report, "GPU", GPU_STATS);
      if (captureMode == NanovisionPerformanceProfiler.CaptureMode.MOB_MODELS_ONLY) {
         appendMobModelLoads(report);
      }

      return report.toString();
   }

   private static boolean shouldRecordSection(NanovisionPerformanceProfiler.Section section) {
      if (captureMode != NanovisionPerformanceProfiler.CaptureMode.MOB_MODELS_ONLY) {
         return captureMode != NanovisionPerformanceProfiler.CaptureMode.FIRST_PERSON_ONLY
            ? true
            : section == NanovisionPerformanceProfiler.Section.FIRST_PERSON_HAND_RENDER
               || section == NanovisionPerformanceProfiler.Section.FIRST_PERSON_HAND_RENDER_GPU
               || section == NanovisionPerformanceProfiler.Section.FIRST_PERSON_ITEM_DEFERRED
               || section == NanovisionPerformanceProfiler.Section.FIRST_PERSON_ITEM_DEFERRED_GPU
               || section == NanovisionPerformanceProfiler.Section.FIRST_PERSON_ARM_TOTAL
               || section == NanovisionPerformanceProfiler.Section.FIRST_PERSON_ARM_COMPOSITE
               || section == NanovisionPerformanceProfiler.Section.FIRST_PERSON_ARM_COMPOSITE_GPU;
      } else {
         return section == NanovisionPerformanceProfiler.Section.MARKER_BATCH
            || section == NanovisionPerformanceProfiler.Section.MARKER_BATCH_GPU
            || section == NanovisionPerformanceProfiler.Section.COMPATIBILITY_PREPARE
            || section == NanovisionPerformanceProfiler.Section.COMPATIBILITY_RENDER
            || section == NanovisionPerformanceProfiler.Section.COMPATIBILITY_GPU;
      }
   }

   private static void recordMobLoad(String path, String entityType, String rendererOrModel, String route, long cpuNanos, int vertices) {
      String key = path + "|" + entityType + "|" + rendererOrModel + "|" + route;
      MOB_MODEL_LOADS.computeIfAbsent(key, ignored -> new NanovisionPerformanceProfiler.MobModelLoadStats(path, entityType, rendererOrModel, route))
         .add(cpuNanos, vertices);
   }

   private static void appendMobModelLoads(StringBuilder report) {
      report.append("\nMob thermal-model load breakdown (top 20 by CPU)\n");
      if (MOB_MODEL_LOADS.isEmpty()) {
         report.append("no eligible mob thermal models were submitted during capture\n");
      } else {
         List<NanovisionPerformanceProfiler.MobModelLoadStats> loads = new ArrayList<>(MOB_MODEL_LOADS.values());
         loads.sort(Comparator.comparingLong(NanovisionPerformanceProfiler.MobModelLoadStats::cpuNanos).reversed());

         for (int index = 0; index < Math.min(20, loads.size()); index++) {
            NanovisionPerformanceProfiler.MobModelLoadStats load = loads.get(index);
            report.append(
               String.format(
                  Locale.ROOT,
                  "%02d path=%s entity=%s renderer_or_model=%s route=%s calls=%d cpu_total_ms=%.4f cpu_avg_call_ms=%.4f vertices=%d vertices_per_call=%.1f%n",
                  index + 1,
                  load.path,
                  load.entityType,
                  load.rendererOrModel,
                  load.route,
                  load.calls,
                  load.cpuNanos / 1000000.0,
                  load.calls == 0 ? 0.0 : load.cpuNanos / 1000000.0 / load.calls,
                  load.vertices,
                  load.calls == 0 ? 0.0 : (double)load.vertices / load.calls
               )
            );
         }
      }
   }

   private static void appendStats(
      StringBuilder report, String heading, Map<NanovisionPerformanceProfiler.Section, NanovisionPerformanceProfiler.TimingStats> stats
   ) {
      report.append('\n').append(heading).append(" timings\n");
      List<Entry<NanovisionPerformanceProfiler.Section, NanovisionPerformanceProfiler.TimingStats>> entries = new ArrayList<>(stats.entrySet());
      entries.sort(Comparator.comparingDouble(entryx -> -((NanovisionPerformanceProfiler.TimingStats)entryx.getValue()).sumMillis()));

      for (Entry<NanovisionPerformanceProfiler.Section, NanovisionPerformanceProfiler.TimingStats> entry : entries) {
         NanovisionPerformanceProfiler.TimingStats value = entry.getValue();
         report.append(
            String.format(
               Locale.ROOT,
               "%s total_ms=%.4f avg_frame_ms=%.4f avg_call_ms=%.4f p95_call_ms=%.4f max_call_ms=%.4f calls=%d calls_per_frame=%.3f%n",
               entry.getKey().label(),
               value.sumMillis(),
               capturedFrames > 0 ? value.sumMillis() / capturedFrames : 0.0,
               value.averageMillis(),
               value.percentileMillis(0.95),
               value.maxMillis(),
               value.count(),
               capturedFrames > 0 ? (double)value.count() / capturedFrames : 0.0
            )
         );
      }
   }

   private static void clearPendingQueries() {
      while (!PENDING_GPU_QUERIES.isEmpty()) {
         NanovisionPerformanceProfiler.PendingGpuQuery pending = PENDING_GPU_QUERIES.removeFirst();
         deleteQuery(pending.startQueryId());
         deleteQuery(pending.endQueryId());
      }
   }

   private static void beginDrain() {
      if (drainStartedNanos == 0L) {
         drainStartedNanos = System.nanoTime();
      }
   }

   private static void markGpuFailure(String reason) {
      gpuTimerFailed = true;
      if ("none".equals(gpuFailureReason)) {
         gpuFailureReason = reason != null && !reason.isBlank() ? reason : "unknown GPU timer failure";
      }
   }

   private static void deleteQuery(int queryId) {
      if (queryId != 0) {
         try {
            GL15.glDeleteQueries(queryId);
         } catch (RuntimeException var2) {
         }
      }
   }

   private static enum CaptureMode {
      FULL_NANOVISION,
      MOB_MODELS_ONLY,
      FIRST_PERSON_ONLY;
   }

   private static final class MobModelLoadStats {
      private final String path;
      private final String entityType;
      private final String rendererOrModel;
      private final String route;
      private long cpuNanos;
      private long vertices;
      private int calls;

      private MobModelLoadStats(String path, String entityType, String rendererOrModel, String route) {
         this.path = path;
         this.entityType = entityType;
         this.rendererOrModel = rendererOrModel;
         this.route = route;
      }

      private void add(long nanos, int submittedVertices) {
         this.cpuNanos = this.cpuNanos + Math.max(0L, nanos);
         this.vertices = this.vertices + Math.max(0, submittedVertices);
         this.calls++;
      }

      private long cpuNanos() {
         return this.cpuNanos;
      }
   }

   private record PendingGpuQuery(int startQueryId, int endQueryId, NanovisionPerformanceProfiler.Section section) {
   }

   public static enum Section {
      POST_TOTAL("post.total"),
      HEAT_EXTRACT("post.heat_extract"),
      HEAT_DIFFUSE_VERTICAL("post.heat_diffuse_vertical"),
      THERMAL_FIELD("post.thermal_field"),
      GLOW_HORIZONTAL("post.glow_horizontal"),
      COMPOSITE("post.composite"),
      MOTION_BLUR("post.motion_blur"),
      FRAMEBUFFER_BLIT("post.framebuffer_blit"),
      EMPTY_FIELD_CLEAR("post.empty_field_clear"),
      CINEMATIC_GEOMETRY("post.cinematic_geometry"),
      CINEMATIC_GEOMETRY_HORIZONTAL("post.cinematic_geometry_horizontal"),
      CINEMATIC_GEOMETRY_VERTICAL("post.cinematic_geometry_vertical"),
      MARKER_MODEL("marker.model_submit"),
      MARKER_TARGET_PREPARE("marker.target_prepare_cpu"),
      MARKER_TARGET_PREPARE_GPU("marker.target_prepare_gpu"),
      MARKER_BATCH("marker.batch_flush_cpu"),
      MARKER_BATCH_GPU("marker.batch_gpu"),
      COMPATIBILITY_PREPARE("marker.compatibility_prepare"),
      COMPATIBILITY_RENDER("marker.compatibility_render"),
      COMPATIBILITY_GPU("marker.compatibility_gpu"),
      HOT_BLOCK_RENDER("marker.hot_blocks_cpu"),
      HOT_BLOCK_GPU("marker.hot_blocks_gpu"),
      FIRST_PERSON_ARM_TOTAL("arm.total_cpu"),
      FIRST_PERSON_ARM_COMPOSITE("arm.composite_cpu"),
      FIRST_PERSON_ARM_COMPOSITE_GPU("arm.composite_gpu"),
      FIRST_PERSON_HAND_RENDER("first_person.hand_render_cpu"),
      FIRST_PERSON_HAND_RENDER_GPU("first_person.hand_render_gpu"),
      FIRST_PERSON_ITEM_DEFERRED("first_person.item_deferred_cpu"),
      FIRST_PERSON_ITEM_DEFERRED_GPU("first_person.item_deferred_gpu"),
      MOB_LAYER_MODEL("mob.layer_model_cpu"),
      MOB_COMPATIBILITY_ENTITY("mob.compatibility_entity_cpu");

      private final String label;

      private Section(String label) {
         this.label = label;
      }

      public String label() {
         return this.label;
      }
   }

   private static final class TimingStats {
      private long[] samples = new long[256];
      private int size;
      private long sum;
      private long max;

      private void add(long nanos) {
         long safeNanos = Math.max(0L, nanos);
         if (this.size >= this.samples.length) {
            this.samples = Arrays.copyOf(this.samples, this.samples.length * 2);
         }

         this.samples[this.size++] = safeNanos;
         this.sum += safeNanos;
         this.max = Math.max(this.max, safeNanos);
      }

      private void clear() {
         this.size = 0;
         this.sum = 0L;
         this.max = 0L;
      }

      private int count() {
         return this.size;
      }

      private double sumMillis() {
         return this.sum / 1000000.0;
      }

      private double averageMillis() {
         return this.size == 0 ? 0.0 : this.sumMillis() / this.size;
      }

      private double maxMillis() {
         return this.max / 1000000.0;
      }

      private double percentileMillis(double percentile) {
         if (this.size == 0) {
            return 0.0;
         } else {
            long[] sorted = Arrays.copyOf(this.samples, this.size);
            Arrays.sort(sorted);
            int index = Math.max(0, Math.min(this.size - 1, (int)Math.ceil(this.size * percentile) - 1));
            return sorted[index] / 1000000.0;
         }
      }
   }
}
