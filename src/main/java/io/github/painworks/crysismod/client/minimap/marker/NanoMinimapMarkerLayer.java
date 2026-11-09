package io.github.painworks.crysismod.client.minimap.marker;

import io.github.painworks.crysismod.client.minimap.NanoMinimapCoordinateMapper;
import io.github.painworks.crysismod.client.minimap.config.NanoMinimapClientConfig;
import io.github.painworks.crysismod.nanovisor.client.NanoVisorClientMarkedTargets;
import io.github.painworks.crysismod.nanovisor.client.NanoVisorState;
import io.github.painworks.crysismod.nanovisor.client.NanoVisorTargetBridge;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;

public final class NanoMinimapMarkerLayer {
   private static final long MARKED_SCAN_INTERVAL_TICKS = 5L;
   private static final double SURFACE_HEIGHT_TOLERANCE_BLOCKS = 8.0;
   private static final float MIN_TARGET_VERTICAL_ALPHA = 0.85F;
   private static final float MIN_MARKED_VERTICAL_ALPHA = 0.6F;
   private final ArrayList<NanoMinimapMarker> working = new ArrayList<>();
   private final ArrayList<NanoMinimapMarker> markedOutBuffer = new ArrayList<>();
   private final ArrayList<NanoMinimapMarkerLayer.NearbyCandidate> nearbyCandidatesBuffer = new ArrayList<>();
   private final ArrayList<NanoMinimapMarker> nearbyOutBuffer = new ArrayList<>();
   private final HashSet<Integer> nearbyStickyEntityIds = new HashSet<>();
   private final PriorityQueue<NanoMinimapMarkerLayer.MarkedCandidate> markedNearestHeap = new PriorityQueue<>((a, b) -> {
      int byDist = Double.compare(b.distanceSqr(), a.distanceSqr());
      return byDist != 0 ? byDist : Integer.compare(b.entity().getId(), a.entity().getId());
   });
   private List<NanoMinimapMarker> renderMarkers = List.of();
   private List<NanoMinimapMarker> cachedMarkedMarkers = List.of();
   private List<NanoMinimapMarker> cachedNearbyMarkers = List.of();
   private long lastMarkedScanTick = Long.MIN_VALUE;
   private long lastNearbyScanTick = Long.MIN_VALUE;
   private long lastMarkerUpdateMs;
   private int lastNearbyScanCount;
   private int lastMarkedScanCount;
   private NanoMinimapMarkerLayer.Stats stats = NanoMinimapMarkerLayer.Stats.empty();

   public void update(
      Minecraft minecraft, LocalPlayer player, boolean minimapVisible, NanoMinimapCoordinateMapper mapper, boolean rotateWithPlayer, float playerYawDeg
   ) {
      long startNanos = System.nanoTime();
      this.working.clear();
      if (minecraft != null && player != null) {
         ClientLevel level = minecraft.level;
         if (level == null) {
            this.renderMarkers = List.of();
            this.stats = NanoMinimapMarkerLayer.Stats.empty();
         } else if (!minimapVisible) {
            this.renderMarkers = List.of();
            this.stats = NanoMinimapMarkerLayer.Stats.empty();
            this.lastMarkerUpdateMs = (System.nanoTime() - startNanos) / 1000000L;
         } else {
            long gameTick = level.getGameTime();
            NanoMinimapMarkerLayer.MarkerVisibilityContext visibilityContext = NanoMinimapMarkerLayer.MarkerVisibilityContext.create(level, player);
            if (NanoMinimapClientConfig.minimapShowPlayerMarker()) {
               this.working.add(NanoMinimapMarker.player(player.getX(), player.getY(), player.getZ(), player.getYRot(), 8321023, gameTick));
            }

            boolean targetVisible = false;
            if (minimapVisible && NanoMinimapClientConfig.minimapShowTargetMarker() && NanoVisorState.isVisorActive()) {
               Entity target = NanoVisorTargetBridge.resolveCurrentEntityTarget(minecraft, player, 1.0F);
               if (target != null && target.level() == player.level() && target.isAlive()) {
                  double distance = player.distanceTo(target);
                  float targetAlpha = computeContextualMarkerAlpha(
                     visibilityContext, target.getX(), target.getY(), target.getZ(), NanoMinimapClientConfig.minimapTargetIgnoreVerticalLimit(), 0.85F
                  );
                  this.working
                     .add(
                        NanoMinimapMarker.entity(
                           NanoMinimapMarker.Type.TARGET,
                           target.getX(),
                           target.getY(),
                           target.getZ(),
                           target.getYRot(),
                           16729413,
                           target.getType().toShortString(),
                           entityTypeId(target),
                           target.getId(),
                           distance,
                           gameTick,
                           targetAlpha
                        )
                     );
                  targetVisible = true;
               }
            }

            int markedCount = 0;
            if (minimapVisible && NanoMinimapClientConfig.minimapShowMarkedEntities()) {
               if (this.lastMarkedScanTick == Long.MIN_VALUE || gameTick - this.lastMarkedScanTick >= 5L) {
                  this.cachedMarkedMarkers = this.scanMarkedEntities(player, gameTick, visibilityContext);
                  this.lastMarkedScanTick = gameTick;
               }

               markedCount = this.cachedMarkedMarkers.size();
               this.working.addAll(this.cachedMarkedMarkers);
            }

            int nearbyCount = 0;
            if (minimapVisible && NanoMinimapClientConfig.minimapShowNearbyEntities()) {
               int intervalTicks = Math.max(1, NanoMinimapClientConfig.minimapNearbyEntityUpdateIntervalTicks());
               if (this.lastNearbyScanTick == Long.MIN_VALUE || gameTick - this.lastNearbyScanTick >= intervalTicks) {
                  this.cachedNearbyMarkers = this.scanNearbyEntities(player, gameTick, visibilityContext);
                  this.lastNearbyScanTick = gameTick;
               }

               nearbyCount = this.cachedNearbyMarkers.size();
               this.working.addAll(this.cachedNearbyMarkers);
            } else {
               this.cachedNearbyMarkers = List.of();
               this.nearbyStickyEntityIds.clear();
               this.lastNearbyScanCount = 0;
            }

            this.renderMarkers = List.copyOf(this.working);
            this.lastMarkerUpdateMs = (System.nanoTime() - startNanos) / 1000000L;
            String sample = this.buildSampleTransformedMarker(player, mapper, rotateWithPlayer, playerYawDeg);
            this.stats = new NanoMinimapMarkerLayer.Stats(
               this.renderMarkers.size(),
               NanoMinimapClientConfig.minimapShowPlayerMarker(),
               targetVisible,
               markedCount,
               nearbyCount,
               Math.max(1, NanoMinimapClientConfig.minimapNearbyEntityUpdateIntervalTicks()),
               this.lastMarkerUpdateMs,
               this.lastNearbyScanCount,
               rotateWithPlayer,
               sample
            );
         }
      } else {
         this.renderMarkers = List.of();
         this.stats = NanoMinimapMarkerLayer.Stats.empty();
      }
   }

   public List<NanoMinimapMarker> markers() {
      return this.renderMarkers;
   }

   public NanoMinimapMarkerLayer.Stats stats() {
      return this.stats;
   }

   public void clear() {
      this.working.clear();
      this.renderMarkers = List.of();
      this.cachedMarkedMarkers = List.of();
      this.cachedNearbyMarkers = List.of();
      this.markedOutBuffer.clear();
      this.nearbyCandidatesBuffer.clear();
      this.nearbyOutBuffer.clear();
      this.nearbyStickyEntityIds.clear();
      this.markedNearestHeap.clear();
      this.lastMarkedScanTick = Long.MIN_VALUE;
      this.lastNearbyScanTick = Long.MIN_VALUE;
      this.lastMarkerUpdateMs = 0L;
      this.lastNearbyScanCount = 0;
      this.lastMarkedScanCount = 0;
      this.stats = NanoMinimapMarkerLayer.Stats.empty();
   }

   private List<NanoMinimapMarker> scanMarkedEntities(LocalPlayer player, long gameTick, NanoMinimapMarkerLayer.MarkerVisibilityContext visibilityContext) {
      ClientLevel level = (ClientLevel)player.level();
      int range = Math.max(16, NanoMinimapClientConfig.minimapRangeBlocks());
      int maxMarkers = Math.max(1, NanoMinimapClientConfig.minimapMaxNearbyEntityMarkers());
      double maxRangeSq = (double)range * range;
      boolean ignoreVerticalLimit = NanoMinimapClientConfig.minimapMarkedIgnoreVerticalLimit();
      this.markedNearestHeap.clear();
      NanoVisorClientMarkedTargets.forEachActiveMarkedEntity(level, entityx -> {
         if (entityx != player && entityx.isAlive()) {
            double distanceSqr = horizontalDistanceSqr(player, entityx);
            if (!(distanceSqr > maxRangeSq)) {
               float markedAlpha = computeContextualMarkerAlpha(visibilityContext, entityx.getX(), entityx.getY(), entityx.getZ(), ignoreVerticalLimit, 0.6F);
               if (!(markedAlpha <= 0.001F)) {
                  NanoMinimapMarkerLayer.MarkedCandidate candidatex = new NanoMinimapMarkerLayer.MarkedCandidate(entityx, distanceSqr, markedAlpha);
                  if (this.markedNearestHeap.size() < maxMarkers) {
                     this.markedNearestHeap.offer(candidatex);
                  } else {
                     NanoMinimapMarkerLayer.MarkedCandidate worst = this.markedNearestHeap.peek();
                     if (worst != null) {
                        boolean closer = distanceSqr < worst.distanceSqr();
                        boolean sameDistBetterId = distanceSqr == worst.distanceSqr() && entityx.getId() < worst.entity().getId();
                        if (closer || sameDistBetterId) {
                           this.markedNearestHeap.poll();
                           this.markedNearestHeap.offer(candidatex);
                        }
                     }
                  }
               }
            }
         }
      });
      if (this.markedNearestHeap.isEmpty()) {
         this.lastMarkedScanCount = 0;
         return List.of();
      } else {
         this.markedOutBuffer.clear();
         ArrayList<NanoMinimapMarkerLayer.MarkedCandidate> selected = new ArrayList<>(this.markedNearestHeap);
         selected.sort(
            Comparator.comparingDouble(NanoMinimapMarkerLayer.MarkedCandidate::distanceSqr).thenComparingInt(candidatex -> candidatex.entity().getId())
         );

         for (NanoMinimapMarkerLayer.MarkedCandidate candidate : selected) {
            Entity entity = candidate.entity();
            this.markedOutBuffer
               .add(
                  NanoMinimapMarker.entity(
                     NanoMinimapMarker.Type.MARKED_ENTITY,
                     entity.getX(),
                     entity.getY(),
                     entity.getZ(),
                     entity.getYRot(),
                     16767075,
                     entity.getType().toShortString(),
                     entityTypeId(entity),
                     entity.getId(),
                     Math.sqrt(candidate.distanceSqr()),
                     gameTick,
                     candidate.alpha()
                  )
               );
         }

         this.lastMarkedScanCount = this.markedOutBuffer.size();
         return List.copyOf(this.markedOutBuffer);
      }
   }

   private List<NanoMinimapMarker> scanNearbyEntities(LocalPlayer player, long gameTick, NanoMinimapMarkerLayer.MarkerVisibilityContext visibilityContext) {
      ClientLevel level = (ClientLevel)player.level();
      int range = Math.max(8, NanoMinimapClientConfig.minimapNearbyEntityRange());
      int maxMarkers = Math.max(1, NanoMinimapClientConfig.minimapMaxNearbyEntityMarkers());
      double maxRangeSq = (double)range * range;
      boolean playerNearSurface = visibilityContext.playerNearSurface();
      int verticalLimit = visibilityContext.verticalLimit();
      int fadeStart = visibilityContext.fadeStart();
      float fadeStrength = visibilityContext.fadeStrength();
      float undergroundFadeStrength = visibilityContext.undergroundFadeStrength();
      boolean surfaceAware = visibilityContext.surfaceAware();
      AABB box = new AABB(
         player.getX() - range, level.getMinBuildHeight(), player.getZ() - range, player.getX() + range, level.getMaxBuildHeight(), player.getZ() + range
      );
      List<LivingEntity> entities = level.getEntitiesOfClass(
         LivingEntity.class, box, entityx -> entityx != null && entityx.isAlive() && entityx != player && !entityx.isSpectator()
      );
      if (entities.isEmpty()) {
         this.lastNearbyScanCount = 0;
         return List.of();
      } else {
         this.nearbyCandidatesBuffer.clear();

         for (LivingEntity entity : entities) {
            double distSq = horizontalDistanceSqr(player, entity);
            if (!(distSq > maxRangeSq)) {
               double absDy = Math.abs(entity.getY() - player.getY());
               float alpha = computeVerticalFadeAlpha(absDy, verticalLimit, fadeStart, fadeStrength);
               if (!(alpha <= 0.001F)) {
                  boolean entityNearSurface = visibilityContext.entityNearSurface(entity.getX(), entity.getY(), entity.getZ());
                  if (!entityNearSurface) {
                     alpha *= 1.0F - undergroundFadeStrength;
                  }

                  if (surfaceAware && playerNearSurface && !entityNearSurface) {
                     alpha *= 1.0F - undergroundFadeStrength;
                  }

                  if (!(alpha <= 0.001F)) {
                     this.nearbyCandidatesBuffer.add(new NanoMinimapMarkerLayer.NearbyCandidate(entity, distSq, alpha));
                  }
               }
            }
         }

         if (this.nearbyCandidatesBuffer.isEmpty()) {
            this.lastNearbyScanCount = 0;
            this.nearbyStickyEntityIds.clear();
            return List.of();
         } else {
            List<NanoMinimapMarkerLayer.NearbyCandidate> selected;
            if (this.nearbyCandidatesBuffer.size() <= maxMarkers) {
               selected = this.nearbyCandidatesBuffer;
            } else {
               this.nearbyCandidatesBuffer
                  .sort(
                     Comparator.comparingDouble(NanoMinimapMarkerLayer.NearbyCandidate::distanceSqr)
                        .thenComparingInt(candidatex -> candidatex.entity().getId())
                  );
               selected = selectAcrossDistanceStable(this.nearbyCandidatesBuffer, maxMarkers, this.nearbyStickyEntityIds);
            }

            this.nearbyOutBuffer.clear();
            this.nearbyStickyEntityIds.clear();

            for (NanoMinimapMarkerLayer.NearbyCandidate candidate : selected) {
               LivingEntity entityx = candidate.entity();
               this.nearbyStickyEntityIds.add(entityx.getId());
               this.nearbyOutBuffer
                  .add(
                     NanoMinimapMarker.entity(
                        NanoMinimapMarker.Type.NEARBY_ENTITY,
                        entityx.getX(),
                        entityx.getY(),
                        entityx.getZ(),
                        entityx.getYRot(),
                        8833023,
                        entityx.getType().toShortString(),
                        entityTypeId(entityx),
                        entityx.getId(),
                        player.distanceTo(entityx),
                        gameTick,
                        candidate.alpha()
                     )
                  );
            }

            this.lastNearbyScanCount = this.nearbyOutBuffer.size();
            return List.copyOf(this.nearbyOutBuffer);
         }
      }
   }

   private static double horizontalDistanceSqr(LocalPlayer player, Entity entity) {
      double dx = entity.getX() - player.getX();
      double dz = entity.getZ() - player.getZ();
      return dx * dx + dz * dz;
   }

   private static boolean isNearSurface(ClientLevel level, double worldX, double worldY, double worldZ, double tolerance) {
      int x = Mth.floor(worldX);
      int z = Mth.floor(worldZ);
      int surfaceY = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
      return Math.abs(worldY - surfaceY) <= tolerance;
   }

   private static ResourceLocation entityTypeId(Entity entity) {
      return entity != null && entity.getType() != null ? BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()) : null;
   }

   private static List<NanoMinimapMarkerLayer.NearbyCandidate> selectAcrossDistanceStable(
      List<NanoMinimapMarkerLayer.NearbyCandidate> sortedByDistance, int maxMarkers, Set<Integer> stickyEntityIds
   ) {
      if (sortedByDistance.size() <= maxMarkers) {
         return sortedByDistance;
      } else {
         int nearQuota = Math.max(1, Math.min(maxMarkers / 3, 12));
         nearQuota = Math.min(nearQuota, maxMarkers);
         ArrayList<NanoMinimapMarkerLayer.NearbyCandidate> selected = new ArrayList<>(maxMarkers);
         HashSet<Integer> selectedIds = new HashSet<>(maxMarkers * 2);

         for (int i = 0; i < nearQuota; i++) {
            NanoMinimapMarkerLayer.NearbyCandidate candidate = sortedByDistance.get(i);
            selected.add(candidate);
            selectedIds.add(candidate.entity().getId());
         }

         int remainingSlots = maxMarkers - selected.size();
         int tailStart = nearQuota;
         int tailCount = sortedByDistance.size() - nearQuota;
         if (remainingSlots > 0 && tailCount > 0) {
            for (int i = nearQuota; i < sortedByDistance.size() && remainingSlots > 0; i++) {
               NanoMinimapMarkerLayer.NearbyCandidate candidate = sortedByDistance.get(i);
               int entityId = candidate.entity().getId();
               if (stickyEntityIds.contains(entityId) && selectedIds.add(entityId)) {
                  selected.add(candidate);
                  remainingSlots--;
               }
            }

            if (remainingSlots > 0) {
               double step = (double)tailCount / remainingSlots;

               for (int ix = 0; ix < remainingSlots; ix++) {
                  int preferredIdx = tailStart + Math.min(tailCount - 1, (int)Math.floor(ix * step));
                  int chosenIdx = findNearestUnselectedIndex(sortedByDistance, selectedIds, preferredIdx, tailStart);
                  if (chosenIdx < 0) {
                     break;
                  }

                  NanoMinimapMarkerLayer.NearbyCandidate candidate = sortedByDistance.get(chosenIdx);
                  selectedIds.add(candidate.entity().getId());
                  selected.add(candidate);
               }
            }

            return (List<NanoMinimapMarkerLayer.NearbyCandidate>)(selected.size() > maxMarkers ? selected.subList(0, maxMarkers) : selected);
         } else {
            return selected;
         }
      }
   }

   private static int findNearestUnselectedIndex(
      List<NanoMinimapMarkerLayer.NearbyCandidate> sortedByDistance, Set<Integer> selectedIds, int preferredIdx, int minIdx
   ) {
      int clampedPreferred = Math.max(minIdx, Math.min(preferredIdx, sortedByDistance.size() - 1));
      NanoMinimapMarkerLayer.NearbyCandidate preferred = sortedByDistance.get(clampedPreferred);
      if (!selectedIds.contains(preferred.entity().getId())) {
         return clampedPreferred;
      } else {
         int left = clampedPreferred - 1;
         int right = clampedPreferred + 1;
         int max = sortedByDistance.size() - 1;

         while (left >= minIdx || right <= max) {
            if (right <= max) {
               NanoMinimapMarkerLayer.NearbyCandidate c = sortedByDistance.get(right);
               if (!selectedIds.contains(c.entity().getId())) {
                  return right;
               }

               right++;
            }

            if (left >= minIdx) {
               NanoMinimapMarkerLayer.NearbyCandidate c = sortedByDistance.get(left);
               if (!selectedIds.contains(c.entity().getId())) {
                  return left;
               }

               left--;
            }
         }

         return -1;
      }
   }

   private static float computeContextualMarkerAlpha(
      NanoMinimapMarkerLayer.MarkerVisibilityContext visibilityContext,
      double worldX,
      double worldY,
      double worldZ,
      boolean ignoreVerticalLimit,
      float minAlpha
   ) {
      int verticalLimit = visibilityContext.verticalLimit();
      int fadeStart = visibilityContext.fadeStart();
      float fadeStrength = visibilityContext.fadeStrength();
      float undergroundFadeStrength = visibilityContext.undergroundFadeStrength();
      boolean playerNearSurface = visibilityContext.playerNearSurface();
      boolean entityNearSurface = visibilityContext.entityNearSurface(worldX, worldY, worldZ);
      float alpha = 1.0F;
      if (!ignoreVerticalLimit) {
         double absDy = Math.abs(worldY - visibilityContext.playerY());
         alpha = computeVerticalFadeAlpha(absDy, verticalLimit, fadeStart, fadeStrength);
      }

      if (!entityNearSurface) {
         alpha *= 1.0F - undergroundFadeStrength;
      }

      if (visibilityContext.surfaceAware() && playerNearSurface && !entityNearSurface) {
         alpha *= 1.0F - undergroundFadeStrength;
      }

      return alpha <= 0.001F ? 0.0F : Mth.clamp(Math.max(minAlpha, alpha), 0.0F, 1.0F);
   }

   private static float computeVerticalFadeAlpha(double absDy, int verticalLimit, int fadeStart, float fadeStrength) {
      if (absDy >= verticalLimit) {
         return 0.0F;
      } else if (!(absDy <= fadeStart) && verticalLimit > fadeStart) {
         double denom = Math.max(1.0, (double)(verticalLimit - fadeStart));
         float t = (float)((absDy - fadeStart) / denom);
         t = Mth.clamp(t, 0.0F, 1.0F);
         return Mth.clamp(1.0F - t * fadeStrength, 0.0F, 1.0F);
      } else {
         return 1.0F;
      }
   }

   private String buildSampleTransformedMarker(LocalPlayer player, NanoMinimapCoordinateMapper mapper, boolean rotateWithPlayer, float playerYawDeg) {
      for (NanoMinimapMarker marker : this.renderMarkers) {
         if (marker.type() != NanoMinimapMarker.Type.PLAYER) {
            NanoMinimapCoordinateMapper.Point p = mapper.worldToViewport(marker.worldX(), marker.worldZ(), 0.0, 0.0, rotateWithPlayer, playerYawDeg);
            return marker.type() + " " + String.format(Locale.ROOT, "%.1f,%.1f", p.x(), p.y());
         }
      }

      NanoMinimapCoordinateMapper.Point self = mapper.worldToViewport(player.getX(), player.getZ(), 0.0, 0.0, rotateWithPlayer, playerYawDeg);
      return "PLAYER " + String.format(Locale.ROOT, "%.1f,%.1f", self.x(), self.y());
   }

   private record MarkedCandidate(Entity entity, double distanceSqr, float alpha) {
   }

   private record MarkerVisibilityContext(
      ClientLevel level,
      double playerY,
      boolean playerNearSurface,
      int verticalLimit,
      int fadeStart,
      float fadeStrength,
      float undergroundFadeStrength,
      boolean surfaceAware
   ) {
      static NanoMinimapMarkerLayer.MarkerVisibilityContext create(ClientLevel level, LocalPlayer player) {
         int verticalLimit = Math.max(1, NanoMinimapClientConfig.minimapEntityVerticalLimit());
         int fadeStart = Mth.clamp(NanoMinimapClientConfig.minimapEntityVerticalFadeStart(), 0, verticalLimit - 1);
         float fadeStrength = (float)Mth.clamp(NanoMinimapClientConfig.minimapEntityVerticalFadeStrength(), 0.0, 1.0);
         float undergroundFadeStrength = (float)Mth.clamp(NanoMinimapClientConfig.minimapEntityUndergroundFade(), 0.0, 1.0);
         boolean surfaceAware = NanoMinimapClientConfig.minimapSurfaceAwareNearby();
         boolean needsSurfaceChecks = undergroundFadeStrength > 0.0F || surfaceAware;
         boolean playerNearSurface = !needsSurfaceChecks || NanoMinimapMarkerLayer.isNearSurface(level, player.getX(), player.getY(), player.getZ(), 8.0);
         return new NanoMinimapMarkerLayer.MarkerVisibilityContext(
            level, player.getY(), playerNearSurface, verticalLimit, fadeStart, fadeStrength, undergroundFadeStrength, surfaceAware
         );
      }

      boolean entityNearSurface(double x, double y, double z) {
         return this.undergroundFadeStrength <= 0.0F && !this.surfaceAware ? true : NanoMinimapMarkerLayer.isNearSurface(this.level, x, y, z, 8.0);
      }
   }

   private record NearbyCandidate(LivingEntity entity, double distanceSqr, float alpha) {
   }

   public record Stats(
      int totalMarkerCount,
      boolean playerMarkerVisible,
      boolean targetMarkerVisible,
      int markedEntityCount,
      int nearbyEntityCount,
      int nearbyScanIntervalTicks,
      long lastMarkerUpdateMs,
      int lastNearbyScanCount,
      boolean rotateWithPlayer,
      String sampleTransformedMarker
   ) {
      public static NanoMinimapMarkerLayer.Stats empty() {
         return new NanoMinimapMarkerLayer.Stats(0, false, false, 0, 0, 0, 0L, 0, false, "none");
      }
   }
}
