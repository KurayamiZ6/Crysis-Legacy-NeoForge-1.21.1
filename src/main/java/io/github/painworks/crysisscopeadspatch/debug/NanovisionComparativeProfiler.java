package io.github.painworks.crysisscopeadspatch.debug;

import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import io.github.painworks.crysismod.client.nanovision.debug.NanovisionPerformanceProfiler;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GLCapabilities;
import org.slf4j.Logger;

public final class NanovisionComparativeProfiler {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int DEFAULT_FRAMES = 300;
   private static final int MIN_FRAMES = 60;
   private static final int MAX_FRAMES = 1800;
   private static final int WARMUP_FRAMES = 60;
   private static final long DRAIN_TIMEOUT_NANOS = 8000000000L;
   private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
   private static final Pattern PROFILE_LINE = Pattern.compile("^(\\S+).*?avg_frame_ms=([0-9.]+).*$");
   private static final EnumMap<NanovisionComparativeProfiler.Phase, EnumMap<NanovisionComparativeProfiler.Section, NanovisionComparativeProfiler.TimingStats>> CPU = new EnumMap<>(
      NanovisionComparativeProfiler.Phase.class
   );
   private static final EnumMap<NanovisionComparativeProfiler.Phase, EnumMap<NanovisionComparativeProfiler.Section, NanovisionComparativeProfiler.TimingStats>> GPU = new EnumMap<>(
      NanovisionComparativeProfiler.Phase.class
   );
   private static final ArrayDeque<NanovisionComparativeProfiler.PendingGpuQuery> PENDING = new ArrayDeque<>();
   private static final Set<String> SHADERPACK_NAMES = new LinkedHashSet<>();
   private static boolean running;
   private static boolean collecting;
   private static boolean gpuChecked;
   private static boolean gpuSupported;
   private static boolean gpuFailed;
   private static int targetFrames = 300;
   private static int offWarmup;
   private static int onWarmup;
   private static int noWorldWarmup;
   private static int noHeldItemWarmup;
   private static int offFrames;
   private static int onFrames;
   private static int noWorldFrames;
   private static int noHeldItemFrames;
   private static int rejectedShaderFrames;
   private static int nextOffProgress;
   private static int nextOnProgress;
   private static int nextNoWorldProgress;
   private static int nextNoHeldItemProgress;
   private static long drainStarted;
   private static NanovisionComparativeProfiler.Phase framePhase = NanovisionComparativeProfiler.Phase.NONE;
   private static NanovisionComparativeProfiler.Phase gatePhase = NanovisionComparativeProfiler.Phase.NONE;
   private static String internalReportBefore = "";
   private static boolean internalProfilerStarted;
   private static String gpuFailure = "none";
   private static String gpuVendor = "unknown";
   private static String gpuRenderer = "unknown";
   private static String gpuVersion = "unknown";
   private static String lastReport = "No comparative NanoVision profile captured.";

   private NanovisionComparativeProfiler() {
   }

   public static String start(int requestedFrames) {
      if (running) {
         return statusV3();
      } else if (NanovisionPerformanceProfiler.isCapturing()) {
         return "Stop the existing /crysismod nanovision_profile capture first.";
      } else {
         clearQueries();
         CPU.clear();
         GPU.clear();
         SHADERPACK_NAMES.clear();
         targetFrames = Math.max(60, Math.min(1800, requestedFrames));
         offWarmup = 0;
         onWarmup = 0;
         noWorldWarmup = 0;
         noHeldItemWarmup = 0;
         offFrames = 0;
         onFrames = 0;
         noWorldFrames = 0;
         noHeldItemFrames = 0;
         rejectedShaderFrames = 0;
         nextOffProgress = 10;
         nextOnProgress = 10;
         nextNoWorldProgress = 10;
         nextNoHeldItemProgress = 10;
         drainStarted = 0L;
         framePhase = NanovisionComparativeProfiler.Phase.NONE;
         gatePhase = NanovisionComparativeProfiler.Phase.NONE;
         gpuChecked = false;
         gpuSupported = false;
         gpuFailed = false;
         gpuFailure = "none";
         internalReportBefore = NanovisionPerformanceProfiler.lastReport();
         internalProfilerStarted = false;
         running = true;
         collecting = true;
         return "Профилировщик NanoVision запущен: по "
            + targetFrames
            + " кадров OFF и ON после 60 кадров прогрева. Не выключай шейдерпак; собери один режим, переключи NanoVision и собери второй.";
      }
   }

   public static String stop() {
      if (!running) {
         return "NanoVision A/B profiler is not running.";
      } else {
         collecting = false;
         framePhase = NanovisionComparativeProfiler.Phase.NONE;
         gatePhase = NanovisionComparativeProfiler.Phase.NONE;
         if (NanovisionPerformanceProfiler.isCapturing()) {
            NanovisionPerformanceProfiler.requestStop();
         }

         beginDrain();
         finishIfReady();
         return "Профилирование остановлено; сохраняются готовые CPU/GPU-замеры.";
      }
   }

   public static String startV3(int requestedFrames) {
      if (running) {
         return statusV3();
      } else {
         String result = start(requestedFrames);
         return !running
            ? result
            : "NanoVision profiler v3 started: "
               + targetFrames
               + " frames per phase. Collect OFF once, then enable NanoVision and leave it enabled while FULL, NO_WORLD_CAPTURE and NO_HELD_ITEM_CAPTURE run automatically.";
      }
   }

   public static String stopV3() {
      boolean wasRunning = running;
      String result = stop();
      return wasRunning ? "NanoVision profiler v3 stopped; completed samples are being saved." : result;
   }

   public static String status() {
      return !running
         ? "Профилировщик NanoVision не запущен. Последний отчёт: " + reportPath("nanovision-compare-latest.txt")
         : "Профилирование: OFF "
            + offFrames
            + "/"
            + targetFrames
            + " (прогрев "
            + Math.min(offWarmup, 60)
            + "/60), ON "
            + onFrames
            + "/"
            + targetFrames
            + " (прогрев "
            + Math.min(onWarmup, 60)
            + "/60), ожидают GPU="
            + PENDING.size()
            + ", пропущено без шейдерпака="
            + rejectedShaderFrames
            + ".";
   }

   public static boolean running() {
      return running;
   }

   public static String statusV3() {
      return !running
         ? "NanoVision profiler v3 is not running. Latest report: " + reportPath("nanovision-compare-latest.txt")
         : "NanoVision profiler v3: OFF "
            + offFrames
            + "/"
            + targetFrames
            + " (warmup "
            + Math.min(offWarmup, 60)
            + "/60), FULL "
            + onFrames
            + "/"
            + targetFrames
            + " (warmup "
            + Math.min(onWarmup, 60)
            + "/60), NO_WORLD_CAPTURE "
            + noWorldFrames
            + "/"
            + targetFrames
            + " (warmup "
            + Math.min(noWorldWarmup, 60)
            + "/60), NO_HELD_ITEM_CAPTURE "
            + noHeldItemFrames
            + "/"
            + targetFrames
            + " (warmup "
            + Math.min(noHeldItemWarmup, 60)
            + "/60), pending GPU="
            + PENDING.size()
            + ", rejected without shaderpack="
            + rejectedShaderFrames
            + ".";
   }

   public static NanovisionComparativeProfiler.Token beginFrame() {
      poll();
      if (!running || !collecting) {
         framePhase = NanovisionComparativeProfiler.Phase.NONE;
         gatePhase = NanovisionComparativeProfiler.Phase.NONE;
         return null;
      } else if (!ShaderpackStatus.enabled()) {
         rejectedShaderFrames++;
         framePhase = NanovisionComparativeProfiler.Phase.NONE;
         gatePhase = NanovisionComparativeProfiler.Phase.NONE;
         return null;
      } else {
         SHADERPACK_NAMES.add(ShaderpackStatus.name());
         boolean active = NanovisionState.isActive();
         if (active) {
            if (onWarmup < 60) {
               onWarmup++;
               if (onWarmup == 60) {
                  notifyChat("[NanoVision profiler] ON: прогрев завершён, начинается замер.", ChatFormatting.AQUA);
               }

               framePhase = NanovisionComparativeProfiler.Phase.NONE;
               return null;
            }

            if (onFrames >= targetFrames) {
               return beginCausalOnFrame();
            }

            framePhase = NanovisionComparativeProfiler.Phase.ON;
            gatePhase = NanovisionComparativeProfiler.Phase.NONE;
            startInternalProfilerIfNeeded();
         } else {
            if (offWarmup < 60) {
               offWarmup++;
               if (offWarmup == 60) {
                  notifyChat("[NanoVision profiler] OFF: прогрев завершён, начинается замер.", ChatFormatting.YELLOW);
               }

               framePhase = NanovisionComparativeProfiler.Phase.NONE;
               return null;
            }

            if (offFrames >= targetFrames) {
               framePhase = NanovisionComparativeProfiler.Phase.NONE;
               return null;
            }

            framePhase = NanovisionComparativeProfiler.Phase.OFF;
            gatePhase = NanovisionComparativeProfiler.Phase.NONE;
         }

         return beginSection(NanovisionComparativeProfiler.Section.FRAME_TOTAL);
      }
   }

   private static NanovisionComparativeProfiler.Token beginCausalOnFrame() {
      if (noWorldFrames < targetFrames) {
         if (noWorldWarmup < 60) {
            noWorldWarmup++;
            if (noWorldWarmup == 60) {
               notifyChat("[NanoVision profiler] NO_WORLD_CAPTURE: warmup complete; measurement starts.", ChatFormatting.LIGHT_PURPLE);
            }

            framePhase = NanovisionComparativeProfiler.Phase.NONE;
            gatePhase = NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE;
            return null;
         } else {
            framePhase = NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE;
            gatePhase = NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE;
            return beginSection(NanovisionComparativeProfiler.Section.FRAME_TOTAL);
         }
      } else if (noHeldItemFrames < targetFrames) {
         if (noHeldItemWarmup < 60) {
            noHeldItemWarmup++;
            if (noHeldItemWarmup == 60) {
               notifyChat("[NanoVision profiler] NO_HELD_ITEM_CAPTURE: warmup complete; measurement starts.", ChatFormatting.GREEN);
            }

            framePhase = NanovisionComparativeProfiler.Phase.NONE;
            gatePhase = NanovisionComparativeProfiler.Phase.ON_NO_HELD_ITEM_CAPTURE;
            return null;
         } else {
            framePhase = NanovisionComparativeProfiler.Phase.ON_NO_HELD_ITEM_CAPTURE;
            gatePhase = NanovisionComparativeProfiler.Phase.ON_NO_HELD_ITEM_CAPTURE;
            return beginSection(NanovisionComparativeProfiler.Section.FRAME_TOTAL);
         }
      } else {
         framePhase = NanovisionComparativeProfiler.Phase.NONE;
         gatePhase = NanovisionComparativeProfiler.Phase.NONE;
         return null;
      }
   }

   public static void endFrame(NanovisionComparativeProfiler.Token token) {
      if (token != null) {
         endSection(token, true);
         if (token.phase == NanovisionComparativeProfiler.Phase.ON) {
            onFrames++;
            nextOnProgress = notifyProgress(NanovisionComparativeProfiler.Phase.ON, onFrames, nextOnProgress);
         } else if (token.phase == NanovisionComparativeProfiler.Phase.OFF) {
            offFrames++;
            nextOffProgress = notifyProgress(NanovisionComparativeProfiler.Phase.OFF, offFrames, nextOffProgress);
         } else if (token.phase == NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE) {
            noWorldFrames++;
            nextNoWorldProgress = notifyProgress(NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE, noWorldFrames, nextNoWorldProgress);
         } else if (token.phase == NanovisionComparativeProfiler.Phase.ON_NO_HELD_ITEM_CAPTURE) {
            noHeldItemFrames++;
            nextNoHeldItemProgress = notifyProgress(NanovisionComparativeProfiler.Phase.ON_NO_HELD_ITEM_CAPTURE, noHeldItemFrames, nextNoHeldItemProgress);
         }
      }

      framePhase = NanovisionComparativeProfiler.Phase.NONE;
      gatePhase = NanovisionComparativeProfiler.Phase.NONE;
      if (collecting && offFrames >= targetFrames && onFrames >= targetFrames && noWorldFrames >= targetFrames && noHeldItemFrames >= targetFrames) {
         collecting = false;
         if (NanovisionPerformanceProfiler.isCapturing()) {
            NanovisionPerformanceProfiler.requestStop();
         }

         beginDrain();
      }

      poll();
   }

   public static boolean suppressWorldThermalCapture() {
      return running && gatePhase == NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE;
   }

   public static boolean suppressHeldItemCapture() {
      return running && gatePhase == NanovisionComparativeProfiler.Phase.ON_NO_HELD_ITEM_CAPTURE;
   }

   public static NanovisionComparativeProfiler.Token beginSection(NanovisionComparativeProfiler.Section section) {
      if (running && collecting && framePhase != NanovisionComparativeProfiler.Phase.NONE) {
         int query = beginGpu();
         return new NanovisionComparativeProfiler.Token(section, framePhase, System.nanoTime(), query);
      } else {
         return null;
      }
   }

   public static void endSection(NanovisionComparativeProfiler.Token token, boolean meaningful) {
      if (token != null) {
         if (!meaningful) {
            deleteQuery(token.startQuery);
         } else {
            long elapsed = Math.max(0L, System.nanoTime() - token.cpuStarted);
            stats(CPU, token.phase, token.section).add(elapsed);
            endGpu(token);
         }
      }
   }

   public static void poll() {
      if (gpuChecked && gpuSupported) {
         int count = PENDING.size();

         for (int i = 0; i < count; i++) {
            NanovisionComparativeProfiler.PendingGpuQuery pending = PENDING.removeFirst();
            if (!GL15.glIsQuery(pending.startQuery) || !GL15.glIsQuery(pending.endQuery)) {
               deleteQuery(pending.startQuery);
               deleteQuery(pending.endQuery);
               failGpu("OpenGL invalidated a pending timestamp.");
            } else if (GL15.glGetQueryObjecti(pending.endQuery, 34919) == 0) {
               PENDING.addLast(pending);
            } else {
               long start = GL33.glGetQueryObjectui64(pending.startQuery, 34918);
               long end = GL33.glGetQueryObjectui64(pending.endQuery, 34918);
               deleteQuery(pending.startQuery);
               deleteQuery(pending.endQuery);
               stats(GPU, pending.phase, pending.section).add(Math.max(0L, end - start));
            }
         }
      }

      NanovisionPerformanceProfiler.pollGpuQueries();
      if (!collecting && drainStarted != 0L && System.nanoTime() - drainStarted >= 8000000000L && !PENDING.isEmpty()) {
         failGpu("Comparative GPU timestamps did not drain within 8 seconds.");
         clearQueries();
      }

      finishIfReady();
   }

   private static void startInternalProfilerIfNeeded() {
      if (!internalProfilerStarted) {
         internalProfilerStarted = true;
         internalReportBefore = NanovisionPerformanceProfiler.lastReport();
         NanovisionPerformanceProfiler.start(targetFrames);
      }
   }

   private static int beginGpu() {
      if (!gpuFailed && ensureGpuSupport()) {
         int query = 0;

         try {
            query = GL15.glGenQueries();
            GL33.glQueryCounter(query, 36392);
            return query;
         } catch (RuntimeException var2) {
            deleteQuery(query);
            failGpu("Failed to submit a GPU start timestamp: " + var2.getMessage());
            return 0;
         }
      } else {
         return 0;
      }
   }

   private static void endGpu(NanovisionComparativeProfiler.Token token) {
      if (token.startQuery != 0) {
         int endQuery = 0;

         try {
            endQuery = GL15.glGenQueries();
            GL33.glQueryCounter(endQuery, 36392);
            PENDING.addLast(new NanovisionComparativeProfiler.PendingGpuQuery(token.section, token.phase, token.startQuery, endQuery));
         } catch (RuntimeException var3) {
            deleteQuery(token.startQuery);
            deleteQuery(endQuery);
            failGpu("Failed to submit a GPU end timestamp: " + var3.getMessage());
         }
      }
   }

   private static boolean ensureGpuSupport() {
      if (gpuChecked) {
         return gpuSupported;
      } else {
         gpuChecked = true;

         try {
            GLCapabilities capabilities = GL.getCapabilities();
            gpuSupported = capabilities.OpenGL33 || capabilities.GL_ARB_timer_query;
            gpuVendor = safeGlString(7936);
            gpuRenderer = safeGlString(7937);
            gpuVersion = safeGlString(7938);
         } catch (RuntimeException var1) {
            gpuSupported = false;
            failGpu("GPU timestamp capability check failed: " + var1.getMessage());
         }

         return gpuSupported;
      }
   }

   private static String safeGlString(int name) {
      String value = GL11.glGetString(name);
      return value == null ? "unknown" : value;
   }

   private static void beginDrain() {
      if (drainStarted == 0L) {
         drainStarted = System.nanoTime();
      }
   }

   private static void finishIfReady() {
      if (running && !collecting && PENDING.isEmpty()) {
         String internal = NanovisionPerformanceProfiler.lastReport();
         boolean internalReady = !internalProfilerStarted || !internal.equals(internalReportBefore);
         boolean timedOut = drainStarted != 0L && System.nanoTime() - drainStarted >= 8000000000L;
         if (internalReady || timedOut) {
            running = false;
            lastReport = buildReport(internalReady ? internal : "");
            Path latest = reportPath("nanovision-compare-latest.txt");
            Path archive = reportPath("nanovision-compare-" + LocalDateTime.now().format(FILE_TIME) + ".txt");

            try {
               Files.writeString(latest, lastReport, StandardCharsets.UTF_8);
               Files.writeString(archive, lastReport, StandardCharsets.UTF_8);
            } catch (IOException var6) {
               LOGGER.warn("[CrysisScopeFix][NanoVisionAB] failed to write report", var6);
            }

            LOGGER.info("\n{}", lastReport);
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.gui != null) {
               minecraft.gui.getChat().addMessage(Component.literal("[NanoVision profiler] Отчёт сохранён: " + latest).withStyle(ChatFormatting.GREEN));
               minecraft.gui.getChat().addMessage(Component.literal(completionSummary()).withStyle(ChatFormatting.GREEN));
               minecraft.gui.getChat().addMessage(Component.literal(causalCompletionSummary()).withStyle(ChatFormatting.GREEN));
            }
         }
      }
   }

   private static int notifyProgress(NanovisionComparativeProfiler.Phase phase, int frames, int nextPercent) {
      if (phase != NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE && phase != NanovisionComparativeProfiler.Phase.ON_NO_HELD_ITEM_CAPTURE) {
         int percent;
         for (percent = nextPercent; percent <= 100 && frames >= (int)Math.ceil(targetFrames * percent / 100.0); percent += 10) {
            String mode = phase == NanovisionComparativeProfiler.Phase.ON ? "ON" : "OFF";
            ChatFormatting color = phase == NanovisionComparativeProfiler.Phase.ON ? ChatFormatting.AQUA : ChatFormatting.YELLOW;
            String suffix = percent == 100
               ? (
                  phase == NanovisionComparativeProfiler.Phase.ON
                     ? " Готово; если OFF ещё не завершён — выключи NanoVision."
                     : " Готово; если ON ещё не завершён — включи NanoVision."
               )
               : "";
            notifyChat("[NanoVision profiler] " + mode + ": " + percent + "% (" + frames + "/" + targetFrames + ")." + suffix, color);
         }

         return percent;
      } else {
         return notifyCausalProgress(phase, frames, nextPercent);
      }
   }

   private static int notifyCausalProgress(NanovisionComparativeProfiler.Phase phase, int frames, int nextPercent) {
      int percent = nextPercent;
      String mode = phase == NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE ? "NO_WORLD_CAPTURE" : "NO_HELD_ITEM_CAPTURE";

      for (ChatFormatting color = phase == NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GREEN;
         percent <= 100 && frames >= (int)Math.ceil(targetFrames * percent / 100.0);
         percent += 10
      ) {
         String suffix = percent == 100
            ? (
               phase == NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE
                  ? " Complete; switching automatically to NO_HELD_ITEM_CAPTURE."
                  : " Complete; causal ON phases are finished."
            )
            : "";
         notifyChat("[NanoVision profiler] " + mode + ": " + percent + "% (" + frames + "/" + targetFrames + ")." + suffix, color);
      }

      return percent;
   }

   private static void notifyChat(String message, ChatFormatting color) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.gui != null) {
         minecraft.gui.getChat().addMessage(Component.literal(message).withStyle(color));
      }
   }

   private static String completionSummary() {
      double offCpu = get(CPU, NanovisionComparativeProfiler.Phase.OFF, NanovisionComparativeProfiler.Section.FRAME_TOTAL).averagePerFrameMillis(offFrames);
      double onCpu = get(CPU, NanovisionComparativeProfiler.Phase.ON, NanovisionComparativeProfiler.Section.FRAME_TOTAL).averagePerFrameMillis(onFrames);
      double offGpu = get(GPU, NanovisionComparativeProfiler.Phase.OFF, NanovisionComparativeProfiler.Section.FRAME_TOTAL).averagePerFrameMillis(offFrames);
      double onGpu = get(GPU, NanovisionComparativeProfiler.Phase.ON, NanovisionComparativeProfiler.Section.FRAME_TOTAL).averagePerFrameMillis(onFrames);
      return String.format(
         Locale.ROOT,
         "[NanoVision profiler] Готово: CPU %.2f -> %.2f ms (%+.2f), GPU %.2f -> %.2f ms (%+.2f).",
         offCpu,
         onCpu,
         onCpu - offCpu,
         offGpu,
         onGpu,
         onGpu - offGpu
      );
   }

   private static String causalCompletionSummary() {
      double fullCpu = average(CPU, NanovisionComparativeProfiler.Phase.ON, NanovisionComparativeProfiler.Section.FRAME_TOTAL, onFrames);
      double noWorldCpu = average(
         CPU, NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE, NanovisionComparativeProfiler.Section.FRAME_TOTAL, noWorldFrames
      );
      double noItemCpu = average(
         CPU, NanovisionComparativeProfiler.Phase.ON_NO_HELD_ITEM_CAPTURE, NanovisionComparativeProfiler.Section.FRAME_TOTAL, noHeldItemFrames
      );
      double fullGpu = average(GPU, NanovisionComparativeProfiler.Phase.ON, NanovisionComparativeProfiler.Section.FRAME_TOTAL, onFrames);
      double noWorldGpu = average(
         GPU, NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE, NanovisionComparativeProfiler.Section.FRAME_TOTAL, noWorldFrames
      );
      double noItemGpu = average(
         GPU, NanovisionComparativeProfiler.Phase.ON_NO_HELD_ITEM_CAPTURE, NanovisionComparativeProfiler.Section.FRAME_TOTAL, noHeldItemFrames
      );
      return String.format(
         Locale.ROOT,
         "[NanoVision profiler] Causal savings: world CPU %+.2f / GPU %+.2f ms; held-item CPU %+.2f / GPU %+.2f ms.",
         fullCpu - noWorldCpu,
         fullGpu - noWorldGpu,
         fullCpu - noItemCpu,
         fullGpu - noItemGpu
      );
   }

   private static String buildReport(String internalReport) {
      StringBuilder report = new StringBuilder(12000);
      report.append("NanoVision shaderpack causal performance profile v3\n");
      report.append("shaderpacks=").append(SHADERPACK_NAMES.isEmpty() ? "unresolved" : String.join(" | ", SHADERPACK_NAMES)).append('\n');
      report.append("resolution=")
         .append(Minecraft.getInstance().getMainRenderTarget().width)
         .append('x')
         .append(Minecraft.getInstance().getMainRenderTarget().height)
         .append('\n');
      report.append("target_frames_per_state=").append(targetFrames).append('\n');
      report.append("off_frames=").append(offFrames).append('\n');
      report.append("on_frames=").append(onFrames).append('\n');
      report.append("on_no_world_capture_frames=").append(noWorldFrames).append('\n');
      report.append("on_no_held_item_capture_frames=").append(noHeldItemFrames).append('\n');
      report.append("warmup_frames_per_state=").append(60).append('\n');
      report.append("rejected_shader_off_frames=").append(rejectedShaderFrames).append('\n');
      report.append("gpu_timer_supported=").append(gpuSupported).append('\n');
      report.append("gpu_timer_failed=").append(gpuFailed).append('\n');
      report.append("gpu_failure=").append(gpuFailure).append('\n');
      report.append("gpu_vendor=").append(gpuVendor).append('\n');
      report.append("gpu_renderer=").append(gpuRenderer).append('\n');
      report.append("gpu_version=").append(gpuVersion).append('\n');
      report.append("measurement_note=Section timings overlap and must not be added together. ")
         .append("GPU timestamps are asynchronous and do not call glFinish.\n\n");
      appendComparison(report, "CPU", CPU);
      appendComparison(report, "GPU", GPU);
      appendCausalComparison(report, "CPU", CPU);
      appendCausalComparison(report, "GPU", GPU);
      appendConclusions(report, internalReport);
      if (!internalReport.isBlank()) {
         report.append("\nDetailed built-in NanoVision post profile\n");
         report.append(internalReport);
      } else {
         report.append("\nDetailed built-in post profile was unavailable or timed out.\n");
      }

      return report.toString();
   }

   private static void appendComparison(
      StringBuilder report,
      String heading,
      EnumMap<NanovisionComparativeProfiler.Phase, EnumMap<NanovisionComparativeProfiler.Section, NanovisionComparativeProfiler.TimingStats>> source
   ) {
      report.append(heading).append(" section comparison\n");

      for (NanovisionComparativeProfiler.Section section : NanovisionComparativeProfiler.Section.values()) {
         NanovisionComparativeProfiler.TimingStats off = get(source, NanovisionComparativeProfiler.Phase.OFF, section);
         NanovisionComparativeProfiler.TimingStats on = get(source, NanovisionComparativeProfiler.Phase.ON, section);
         if (off.count != 0 || on.count != 0) {
            double offAverage = off.averagePerFrameMillis(offFrames);
            double onAverage = on.averagePerFrameMillis(onFrames);
            report.append(
               String.format(
                  Locale.ROOT,
                  "%s off_avg_frame_ms=%.4f on_avg_frame_ms=%.4f delta_ms=%+.4f off_avg_call_ms=%.4f on_avg_call_ms=%.4f off_p95_ms=%.4f on_p95_ms=%.4f off_max_ms=%.4f on_max_ms=%.4f off_calls=%d on_calls=%d%n",
                  section.label,
                  offAverage,
                  onAverage,
                  onAverage - offAverage,
                  off.averageMillis(),
                  on.averageMillis(),
                  off.percentileMillis(0.95),
                  on.percentileMillis(0.95),
                  off.maxMillis(),
                  on.maxMillis(),
                  off.count,
                  on.count
               )
            );
         }
      }

      report.append('\n');
   }

   private static void appendCausalComparison(
      StringBuilder report,
      String heading,
      EnumMap<NanovisionComparativeProfiler.Phase, EnumMap<NanovisionComparativeProfiler.Section, NanovisionComparativeProfiler.TimingStats>> source
   ) {
      report.append(heading).append(" causal phase comparison\n");

      for (NanovisionComparativeProfiler.Section section : NanovisionComparativeProfiler.Section.values()) {
         NanovisionComparativeProfiler.TimingStats full = get(source, NanovisionComparativeProfiler.Phase.ON, section);
         NanovisionComparativeProfiler.TimingStats noWorld = get(source, NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE, section);
         NanovisionComparativeProfiler.TimingStats noItem = get(source, NanovisionComparativeProfiler.Phase.ON_NO_HELD_ITEM_CAPTURE, section);
         if (full.count != 0 || noWorld.count != 0 || noItem.count != 0) {
            double fullAverage = full.averagePerFrameMillis(onFrames);
            double noWorldAverage = noWorld.averagePerFrameMillis(noWorldFrames);
            double noItemAverage = noItem.averagePerFrameMillis(noHeldItemFrames);
            report.append(
               String.format(
                  Locale.ROOT,
                  "%s full_avg_frame_ms=%.4f no_world_avg_frame_ms=%.4f world_capture_cost_ms=%+.4f no_held_item_avg_frame_ms=%.4f held_item_capture_cost_ms=%+.4f full_p95_ms=%.4f no_world_p95_ms=%.4f no_held_item_p95_ms=%.4f full_calls=%d no_world_calls=%d no_held_item_calls=%d%n",
                  section.label,
                  fullAverage,
                  noWorldAverage,
                  fullAverage - noWorldAverage,
                  noItemAverage,
                  fullAverage - noItemAverage,
                  full.percentileMillis(0.95),
                  noWorld.percentileMillis(0.95),
                  noItem.percentileMillis(0.95),
                  full.count,
                  noWorld.count,
                  noItem.count
               )
            );
         }
      }

      report.append('\n');
   }

   private static void appendConclusions(StringBuilder report, String internalReport) {
      NanovisionComparativeProfiler.TimingStats offCpu = get(CPU, NanovisionComparativeProfiler.Phase.OFF, NanovisionComparativeProfiler.Section.FRAME_TOTAL);
      NanovisionComparativeProfiler.TimingStats onCpu = get(CPU, NanovisionComparativeProfiler.Phase.ON, NanovisionComparativeProfiler.Section.FRAME_TOTAL);
      NanovisionComparativeProfiler.TimingStats offGpu = get(GPU, NanovisionComparativeProfiler.Phase.OFF, NanovisionComparativeProfiler.Section.FRAME_TOTAL);
      NanovisionComparativeProfiler.TimingStats onGpu = get(GPU, NanovisionComparativeProfiler.Phase.ON, NanovisionComparativeProfiler.Section.FRAME_TOTAL);
      report.append("Automatic bottleneck summary\n");
      appendDelta(report, "full_frame_cpu", offCpu.averagePerFrameMillis(offFrames), onCpu.averagePerFrameMillis(onFrames));
      appendDelta(report, "full_frame_gpu", offGpu.averagePerFrameMillis(offFrames), onGpu.averagePerFrameMillis(onFrames));
      appendTopSections(report, "top_on_cpu_sections", CPU);
      appendTopSections(report, "top_on_gpu_sections", GPU);
      appendCausalSavings(report, "world_capture_cpu", CPU, NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE);
      appendCausalSavings(report, "world_capture_gpu", GPU, NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE);
      appendCausalSavings(report, "held_item_capture_cpu", CPU, NanovisionComparativeProfiler.Phase.ON_NO_HELD_ITEM_CAPTURE);
      appendCausalSavings(report, "held_item_capture_gpu", GPU, NanovisionComparativeProfiler.Phase.ON_NO_HELD_ITEM_CAPTURE);
      if (!internalReport.isBlank()) {
         report.append("top_internal_post_cpu=").append(topInternal(internalReport, "CPU timings", "GPU timings")).append('\n');
         report.append("top_internal_post_gpu=").append(topInternal(internalReport, "GPU timings", null)).append('\n');
      }
   }

   private static void appendCausalSavings(
      StringBuilder report,
      String label,
      EnumMap<NanovisionComparativeProfiler.Phase, EnumMap<NanovisionComparativeProfiler.Section, NanovisionComparativeProfiler.TimingStats>> source,
      NanovisionComparativeProfiler.Phase bypassPhase
   ) {
      double off = average(source, NanovisionComparativeProfiler.Phase.OFF, NanovisionComparativeProfiler.Section.FRAME_TOTAL, offFrames);
      double full = average(source, NanovisionComparativeProfiler.Phase.ON, NanovisionComparativeProfiler.Section.FRAME_TOTAL, onFrames);
      int bypassFrames = bypassPhase == NanovisionComparativeProfiler.Phase.ON_NO_WORLD_CAPTURE ? noWorldFrames : noHeldItemFrames;
      double bypass = average(source, bypassPhase, NanovisionComparativeProfiler.Section.FRAME_TOTAL, bypassFrames);
      double saved = full - bypass;
      double fullOverhead = full - off;
      double share = fullOverhead > 0.0 ? saved * 100.0 / fullOverhead : 0.0;
      report.append(
         String.format(Locale.ROOT, "%s full_ms=%.4f bypass_ms=%.4f saved_ms=%+.4f share_of_full_overhead=%+.2f%%%n", label, full, bypass, saved, share)
      );
   }

   private static void appendDelta(StringBuilder report, String label, double off, double on) {
      double delta = on - off;
      double percent = off > 0.0 ? delta * 100.0 / off : 0.0;
      report.append(String.format(Locale.ROOT, "%s off_ms=%.4f on_ms=%.4f delta_ms=%+.4f delta_percent=%+.2f%%%n", label, off, on, delta, percent));
   }

   private static void appendTopSections(
      StringBuilder report,
      String label,
      EnumMap<NanovisionComparativeProfiler.Phase, EnumMap<NanovisionComparativeProfiler.Section, NanovisionComparativeProfiler.TimingStats>> source
   ) {
      List<Entry<NanovisionComparativeProfiler.Section, NanovisionComparativeProfiler.TimingStats>> entries = new ArrayList<>(
         source.getOrDefault(NanovisionComparativeProfiler.Phase.ON, new EnumMap<>(NanovisionComparativeProfiler.Section.class)).entrySet()
      );
      entries.removeIf(entryx -> entryx.getKey() == NanovisionComparativeProfiler.Section.FRAME_TOTAL);
      entries.sort(Comparator.comparingDouble(entryx -> -((NanovisionComparativeProfiler.TimingStats)entryx.getValue()).averagePerFrameMillis(onFrames)));
      report.append(label).append('=');
      int count = Math.min(4, entries.size());

      for (int i = 0; i < count; i++) {
         if (i > 0) {
            report.append(", ");
         }

         Entry<NanovisionComparativeProfiler.Section, NanovisionComparativeProfiler.TimingStats> entry = entries.get(i);
         report.append(entry.getKey().label).append(':').append(String.format(Locale.ROOT, "%.4fms/frame", entry.getValue().averagePerFrameMillis(onFrames)));
      }

      report.append('\n');
   }

   private static String topInternal(String report, String start, String end) {
      int startIndex = report.indexOf(start);
      if (startIndex < 0) {
         return "unavailable";
      } else {
         int endIndex = end == null ? report.length() : report.indexOf(end, startIndex + start.length());
         if (endIndex < 0) {
            endIndex = report.length();
         }

         String bestName = "unavailable";
         double best = -1.0;

         for (String line : report.substring(startIndex, endIndex).split("\\R")) {
            Matcher matcher = PROFILE_LINE.matcher(line);
            if (matcher.matches()) {
               double value = Double.parseDouble(matcher.group(2));
               if (value > best) {
                  best = value;
                  bestName = matcher.group(1) + ":" + String.format(Locale.ROOT, "%.4fms", value);
               }
            }
         }

         return bestName;
      }
   }

   private static NanovisionComparativeProfiler.TimingStats stats(
      EnumMap<NanovisionComparativeProfiler.Phase, EnumMap<NanovisionComparativeProfiler.Section, NanovisionComparativeProfiler.TimingStats>> source,
      NanovisionComparativeProfiler.Phase phase,
      NanovisionComparativeProfiler.Section section
   ) {
      return source.computeIfAbsent(phase, ignored -> new EnumMap<>(NanovisionComparativeProfiler.Section.class))
         .computeIfAbsent(section, ignored -> new NanovisionComparativeProfiler.TimingStats());
   }

   private static NanovisionComparativeProfiler.TimingStats get(
      EnumMap<NanovisionComparativeProfiler.Phase, EnumMap<NanovisionComparativeProfiler.Section, NanovisionComparativeProfiler.TimingStats>> source,
      NanovisionComparativeProfiler.Phase phase,
      NanovisionComparativeProfiler.Section section
   ) {
      EnumMap<NanovisionComparativeProfiler.Section, NanovisionComparativeProfiler.TimingStats> phaseStats = source.get(phase);
      return phaseStats == null
         ? NanovisionComparativeProfiler.TimingStats.EMPTY
         : phaseStats.getOrDefault(section, NanovisionComparativeProfiler.TimingStats.EMPTY);
   }

   private static double average(
      EnumMap<NanovisionComparativeProfiler.Phase, EnumMap<NanovisionComparativeProfiler.Section, NanovisionComparativeProfiler.TimingStats>> source,
      NanovisionComparativeProfiler.Phase phase,
      NanovisionComparativeProfiler.Section section,
      int frames
   ) {
      return get(source, phase, section).averagePerFrameMillis(frames);
   }

   private static void failGpu(String reason) {
      gpuFailed = true;
      if ("none".equals(gpuFailure)) {
         gpuFailure = reason == null ? "unknown" : reason;
      }
   }

   private static void clearQueries() {
      while (!PENDING.isEmpty()) {
         NanovisionComparativeProfiler.PendingGpuQuery pending = PENDING.removeFirst();
         deleteQuery(pending.startQuery);
         deleteQuery(pending.endQuery);
      }
   }

   private static void deleteQuery(int query) {
      if (query != 0) {
         try {
            GL15.glDeleteQueries(query);
         } catch (RuntimeException var2) {
         }
      }
   }

   private static Path reportPath(String name) {
      return Minecraft.getInstance().gameDirectory.toPath().resolve(name);
   }

   private record PendingGpuQuery(NanovisionComparativeProfiler.Section section, NanovisionComparativeProfiler.Phase phase, int startQuery, int endQuery) {
   }

   public static enum Phase {
      NONE,
      OFF,
      ON,
      ON_NO_WORLD_CAPTURE,
      ON_NO_HELD_ITEM_CAPTURE;
   }

   public static enum Section {
      FRAME_TOTAL("frame.total"),
      WORLD_TOTAL("world.total"),
      MARKER_PREPARE("world.marker_prepare"),
      MARKER_ENTITIES("world.marker_entities_hot_blocks"),
      AFTER_LEVEL("world.after_level"),
      HANDS_TOTAL("first_person.hands_total"),
      TACZ_GUN("first_person.tacz_gun"),
      ARM_OCCLUSION_DEPTH("first_person.arm_occlusion_depth"),
      ARM_SNAPSHOT("first_person.arm_snapshot"),
      POST_PROCESSOR("nanovision.post_processor"),
      FIRST_PERSON_ARM("nanovision.first_person_arm"),
      HELD_ITEM("nanovision.held_item"),
      SCOPE_DEFERRED("nanovision.scope_deferred");

      private final String label;

      private Section(String label) {
         this.label = label;
      }
   }

   private static final class TimingStats {
      private static final NanovisionComparativeProfiler.TimingStats EMPTY = new NanovisionComparativeProfiler.TimingStats();
      private long[] samples = new long[256];
      private int count;
      private long sum;
      private long max;

      private void add(long nanos) {
         long safe = Math.max(0L, nanos);
         if (this.count >= this.samples.length) {
            this.samples = Arrays.copyOf(this.samples, this.samples.length * 2);
         }

         this.samples[this.count++] = safe;
         this.sum += safe;
         this.max = Math.max(this.max, safe);
      }

      private double averageMillis() {
         return this.count == 0 ? 0.0 : this.sum / 1000000.0 / this.count;
      }

      private double averagePerFrameMillis(int frames) {
         return frames <= 0 ? 0.0 : this.sum / 1000000.0 / frames;
      }

      private double maxMillis() {
         return this.max / 1000000.0;
      }

      private double percentileMillis(double percentile) {
         if (this.count == 0) {
            return 0.0;
         } else {
            long[] sorted = Arrays.copyOf(this.samples, this.count);
            Arrays.sort(sorted);
            int index = Math.max(0, Math.min(this.count - 1, (int)Math.ceil(this.count * percentile) - 1));
            return sorted[index] / 1000000.0;
         }
      }
   }

   public static final class Token {
      private final NanovisionComparativeProfiler.Section section;
      private final NanovisionComparativeProfiler.Phase phase;
      private final long cpuStarted;
      private final int startQuery;

      private Token(NanovisionComparativeProfiler.Section section, NanovisionComparativeProfiler.Phase phase, long cpuStarted, int startQuery) {
         this.section = section;
         this.phase = phase;
         this.cpuStarted = cpuStarted;
         this.startQuery = startQuery;
      }
   }
}
