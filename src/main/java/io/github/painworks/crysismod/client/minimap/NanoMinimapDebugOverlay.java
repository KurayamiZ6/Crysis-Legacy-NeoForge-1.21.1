package io.github.painworks.crysismod.client.minimap;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class NanoMinimapDebugOverlay {
   private static final int REFRESH_INTERVAL_TICKS = 5;
   private long lastRefreshTick = Long.MIN_VALUE;
   private long lastTraceLogTick = Long.MIN_VALUE;
   private List<String> cachedLines = List.of();

   public void render(
      GuiGraphics guiGraphics,
      NanoMinimapViewport viewport,
      long currentTick,
      String lastUpdateReason,
      NanoMinimapUpdateState updateState,
      int rebuildRow,
      int rebuildTotalRows,
      int samplesProcessedThisTick,
      long totalRebuildTimeMs,
      long lastUpdateTick,
      int textureUploadCount,
      int textureRecreatedCount,
      long lastUploadTimeMs,
      int lastTextureResolution,
      int currentRangeBlocks,
      int cacheCenterX,
      int cacheCenterZ,
      String dimensionKey,
      double playerExactX,
      double playerExactZ,
      double fractionalOffsetX,
      double fractionalOffsetZ,
      double visualOffsetPixelsX,
      double visualOffsetPixelsY,
      float renderMs,
      int markerCountTotal,
      boolean playerMarkerVisible,
      boolean targetMarkerVisible,
      int markedEntityCount,
      int nearbyEntityCount,
      int nearbyScanIntervalTicks,
      long lastMarkerUpdateMs,
      int lastNearbyScanCount,
      boolean rotateWithPlayerMarkers,
      String sampleTransformedMarker,
      String cacheMode,
      int loadedRegionsCount,
      int loadedTilesCount,
      int dirtyTilesCount,
      int queuedTileUpdates,
      int tilesProcessedThisTick,
      int cacheHits,
      int cacheMisses,
      int lruEvictions,
      long viewportComposeCount,
      String viewportComposeReason,
      long lastComposeTimeMs,
      String lastUploadReason,
      int missingChunkSamplesThisTick,
      int missingTilesThisTick,
      long lastTileUpdateMs,
      String playerTileKey,
      String playerRegionKey,
      int viewportWidth,
      int viewportHeight,
      int baseViewportX,
      int baseViewportY,
      int finalViewportX,
      int finalViewportY,
      float inertiaOffsetX,
      float inertiaOffsetY,
      float inertiaRollDeg,
      int terrainCoverageSize,
      int overscanPadding,
      double blocksPerPixel,
      int readyTilesInViewport,
      int missingTilesInViewport,
      String composeSkippedReason,
      int framesSinceLastUpload,
      int ticksSinceLastUpload,
      int neededTilesThisTick,
      int newlyEnqueuedTilesThisTick,
      int enqueueSkippedDuplicateThisTick,
      int enqueueSkippedQueueFullThisTick,
      int enqueueSkippedCooldownThisTick,
      int visibleTilesDroppedThisTick,
      int priorityCenterTileCountThisTick,
      int unloadedTilesInViewportThisTick,
      int retryCooldownTilesThisTick,
      int retryReadyTilesThisTick,
      int resampledAfterChunkLoadedThisTick,
      String lastSampledTileKey,
      int samplesAttemptedThisTick,
      int samplesSucceededThisTick,
      int samplesFallbackThisTick,
      int sampledTileValidPixelCount,
      int readyTilesUsedInLastCompose,
      int missingTilesUsedInLastCompose,
      int composeTriggeredByTileReady,
      int composeTriggeredByPlayerMove,
      int composeSkippedNoReadyTiles,
      int composeSkippedNoChange,
      double renderMsAvg,
      double composeMsAvg,
      double tileUpdateMsAvg,
      double markerUpdateMsAvg,
      double uploadsPerSecond,
      double composesPerSecond,
      double tilesProcessedPerSecond,
      NanoMinimapRenderTrace renderTrace,
      boolean showRenderTrace,
      boolean logRenderTraceOncePerSecond,
      int mapColorCount,
      int fallbackColorCount,
      int specialCaseColorCount,
      int waterColorCount,
      int lavaColorCount,
      int heightShadingAppliedCount,
      int biomeTintAppliedCount,
      String terrainColorMode,
      boolean showColorDebug
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      if (this.cachedLines.isEmpty() || this.lastRefreshTick == Long.MIN_VALUE || currentTick - this.lastRefreshTick >= 5L) {
         ArrayList<String> lines = new ArrayList<>();
         lines.add("MM state: " + updateState + " rows " + rebuildRow + "/" + rebuildTotalRows);
         lines.add("MM samples/tick: " + samplesProcessedThisTick + " rebuildMs: " + totalRebuildTimeMs);
         lines.add("MM last reason: " + lastUpdateReason + " lastTick: " + lastUpdateTick);
         lines.add("MM tex uploads: " + textureUploadCount + " recreates: " + textureRecreatedCount);
         lines.add("MM tex res: " + lastTextureResolution + " lastUploadMs: " + lastUploadTimeMs);
         lines.add("MM range: " + currentRangeBlocks + " center: " + cacheCenterX + ", " + cacheCenterZ);
         lines.add("MM dim: " + dimensionKey);
         lines.add(String.format(Locale.ROOT, "MM player: %.3f, %.3f frac: %.3f, %.3f", playerExactX, playerExactZ, fractionalOffsetX, fractionalOffsetZ));
         lines.add(String.format(Locale.ROOT, "MM visual px: %.3f, %.3f renderMs: %.3f", visualOffsetPixelsX, visualOffsetPixelsY, renderMs));
         lines.add("MM markers total: " + markerCountTotal + " player: " + playerMarkerVisible + " target: " + targetMarkerVisible);
         lines.add("MM marked: " + markedEntityCount + " nearby: " + nearbyEntityCount + " nearbyInt: " + nearbyScanIntervalTicks);
         lines.add("MM markerUpdateMs: " + lastMarkerUpdateMs + " lastNearbyScan: " + lastNearbyScanCount);
         lines.add("MM markerRotate: " + rotateWithPlayerMarkers + " sample: " + sampleTransformedMarker);
         lines.add("MM cache mode: " + cacheMode + " regions: " + loadedRegionsCount + " tiles: " + loadedTilesCount);
         lines.add("MM dirty/queue: " + dirtyTilesCount + "/" + queuedTileUpdates + " tilesTick: " + tilesProcessedThisTick);
         lines.add("MM hits/miss/evict: " + cacheHits + "/" + cacheMisses + "/" + lruEvictions);
         lines.add("MM compose: " + viewportComposeCount + " reason: " + viewportComposeReason + " composeMs: " + lastComposeTimeMs);
         lines.add("MM compose skip: " + composeSkippedReason + " framesSinceUpload: " + framesSinceLastUpload + " ticksSinceUpload: " + ticksSinceLastUpload);
         lines.add("MM upload reason: " + lastUploadReason + " missing samples/tiles: " + missingChunkSamplesThisTick + "/" + missingTilesThisTick);
         lines.add(
            String.format(
               Locale.ROOT,
               "MM viewport: %dx%d coverage: %d pad: %d bpp: %.4f",
               viewportWidth,
               viewportHeight,
               terrainCoverageSize,
               overscanPadding,
               blocksPerPixel
            )
         );
         lines.add(
            String.format(
               Locale.ROOT,
               "MM base/final vp: %d,%d -> %d,%d inertia: %.2f,%.2f roll: %.2f",
               baseViewportX,
               baseViewportY,
               finalViewportX,
               finalViewportY,
               inertiaOffsetX,
               inertiaOffsetY,
               inertiaRollDeg
            )
         );
         lines.add("MM viewport tiles ready/missing: " + readyTilesInViewport + "/" + missingTilesInViewport);
         lines.add("MM tileUpdateMs: " + lastTileUpdateMs + " key T:" + playerTileKey + " R:" + playerRegionKey);
         lines.add(
            "MM needed/enq: "
               + neededTilesThisTick
               + "/"
               + newlyEnqueuedTilesThisTick
               + " drop:"
               + visibleTilesDroppedThisTick
               + " prioC:"
               + priorityCenterTileCountThisTick
         );
         lines.add(
            "MM enq skip dup/full/cool: " + enqueueSkippedDuplicateThisTick + "/" + enqueueSkippedQueueFullThisTick + "/" + enqueueSkippedCooldownThisTick
         );
         lines.add("MM unloaded/retryCd/retryReady: " + unloadedTilesInViewportThisTick + "/" + retryCooldownTilesThisTick + "/" + retryReadyTilesThisTick);
         lines.add("MM retry->resample loaded: " + resampledAfterChunkLoadedThisTick);
         lines.add(
            "MM sampled tile: "
               + lastSampledTileKey
               + " att/succ/fallback: "
               + samplesAttemptedThisTick
               + "/"
               + samplesSucceededThisTick
               + "/"
               + samplesFallbackThisTick
               + " validPx:"
               + sampledTileValidPixelCount
         );
         lines.add("MM compose tiles ready/missing: " + readyTilesUsedInLastCompose + "/" + missingTilesUsedInLastCompose);
         lines.add("MM compose triggers tileReady/playerMove: " + composeTriggeredByTileReady + "/" + composeTriggeredByPlayerMove);
         lines.add("MM compose skips noReady/noChange: " + composeSkippedNoReadyTiles + "/" + composeSkippedNoChange);
         lines.add(
            String.format(
               Locale.ROOT, "MM avg render/compose/tile/marker ms: %.3f/%.3f/%.3f/%.3f", renderMsAvg, composeMsAvg, tileUpdateMsAvg, markerUpdateMsAvg
            )
         );
         lines.add(String.format(Locale.ROOT, "MM rate up/s comp/s tiles/s: %.2f/%.2f/%.2f", uploadsPerSecond, composesPerSecond, tilesProcessedPerSecond));
         if (showColorDebug) {
            lines.add("MM colors mode: " + terrainColorMode);
            lines.add("MM colors map/fallback/special: " + mapColorCount + "/" + fallbackColorCount + "/" + specialCaseColorCount);
            lines.add("MM colors water/lava: " + waterColorCount + "/" + lavaColorCount);
            lines.add("MM colors shade/biomeTint: " + heightShadingAppliedCount + "/" + biomeTintAppliedCount);
         }

         if (showRenderTrace && renderTrace != null) {
            lines.addAll(renderTrace.briefLines());
         }

         this.cachedLines = lines;
         this.lastRefreshTick = currentTick;
      }

      if (showRenderTrace
         && logRenderTraceOncePerSecond
         && renderTrace != null
         && (this.lastTraceLogTick == Long.MIN_VALUE || currentTick - this.lastTraceLogTick >= 20L)) {
         this.lastTraceLogTick = currentTick;

         for (String line : renderTrace.briefLines()) {
            System.out.println(line);
         }
      }

      List<String> linesx = this.cachedLines;
      int x = viewport.x();
      int y = viewport.y() - linesx.size() * (9 + 1) - 4;

      for (String line : linesx) {
         guiGraphics.drawString(minecraft.font, line, x, y, -6620161, false);
         y += 9 + 1;
      }
   }
}
