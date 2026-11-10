package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.NanovisionController;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import io.github.painworks.crysismod.client.nanovision.debug.NanovisionPerformanceProfiler;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import org.joml.Matrix4f;
import org.slf4j.Logger;

public final class ThermalMarkerCompatibilityRenderer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final long TRACE_INTERVAL_NANOS = 4000000000L;
   private static final int TRACE_RENDER_TYPE_LIMIT = 8;
   private static final int MAX_REPLAY_ENTITIES = 64;
   private static final int MAX_REPLAY_TEXTURES = 128;
   private static final List<ByteBufferBuilder> REPLAY_STORAGE = new ArrayList<>();
   private static final Set<String> LOGGED_RENDERERS = new HashSet<>();
   private static final Map<String, Long> LAST_TRACE_NANOS = new HashMap<>();
   private static final List<ThermalMarkerCompatibilityRenderer.CompatibilityCandidate> PREPARED_CANDIDATES = new ArrayList<>();
   private static final List<ThermalMarkerCompatibilityRenderer.CompatibilityCandidate> PREPARED_LAYER_STANDBY_CANDIDATES = new ArrayList<>();
   private static final Set<Integer> DEFERRED_ENTITY_IDS = new HashSet<>();
   private static final Set<Integer> RENDERED_LAYER_ENTITY_IDS = new HashSet<>();
   private static final Comparator<ThermalMarkerCompatibilityRenderer.CompatibilityCandidate> NEAREST_FIRST = Comparator.comparingDouble(
         ThermalMarkerCompatibilityRenderer.CompatibilityCandidate::distanceSqr
      )
      .thenComparingLong(ThermalMarkerCompatibilityRenderer.CompatibilityCandidate::stableOrder);
   private static boolean tracePreviouslyEnabled;
   private static boolean warnedReplayBatch;
   private static int entityReplayDepth;
   private static MultiBufferSource activeReplayBuffer;

   private ThermalMarkerCompatibilityRenderer() {
   }

   public static void prepareFrame(Minecraft minecraft) {
      long cpuStarted = NanovisionPerformanceProfiler.beginCpu();

      try {
         updateTraceState();
         clearPreparedFrame();
         LocalPlayer viewer = minecraft.player;
         if (!NanovisionConfig.thermalMarkerCompatibilityEntityPass()
            || minecraft.level == null
            || viewer == null
            || !NanovisionState.isActive(viewer)
            || !NanovisionController.shouldRenderMarkerPass()) {
            return;
         }

         Camera camera = minecraft.gameRenderer.getMainCamera();
         double cameraX = camera.getPosition().x;
         double cameraY = camera.getPosition().y;
         double cameraZ = camera.getPosition().z;
         ThermalCameraVisibility.View cameraVisibility = ThermalCameraVisibility.capture(minecraft, camera);
         List<ThermalMarkerCompatibilityRenderer.CompatibilityCandidate> candidates = collectCandidates(
            minecraft, viewer, cameraVisibility, cameraX, cameraY, cameraZ
         );
         candidates.sort(NEAREST_FIRST);

         for (ThermalMarkerCompatibilityRenderer.CompatibilityCandidate candidate : candidates) {
            PREPARED_CANDIDATES.add(candidate);
            if (candidate.renderer() instanceof LivingEntityRenderer<?, ?> livingRenderer
               && ThermalMarkerRenderLayer.isAttachedTo(livingRenderer)
               && ThermalMobProfileResolver.needsFullRendererFallback(candidate.entity(), livingRenderer)) {
               DEFERRED_ENTITY_IDS.add(candidate.entity().getId());
            }
         }
      } finally {
         NanovisionPerformanceProfiler.endCpu(NanovisionPerformanceProfiler.Section.COMPATIBILITY_PREPARE, cpuStarted);
      }
   }

   public static boolean shouldDeferLayer(LivingEntity entity) {
      return entity != null && DEFERRED_ENTITY_IDS.contains(entity.getId());
   }

   static void markLayerRendered(LivingEntity entity) {
      if (entity != null) {
         RENDERED_LAYER_ENTITY_IDS.add(entity.getId());
      }
   }

   public static void clear() {
      clearPreparedFrame();
      LOGGED_RENDERERS.clear();
      LAST_TRACE_NANOS.clear();
      tracePreviouslyEnabled = false;
      warnedReplayBatch = false;
      entityReplayDepth = 0;
      REPLAY_STORAGE.forEach(ByteBufferBuilder::close);
      REPLAY_STORAGE.clear();
   }

   public static boolean isEntityReplayActive() {
      return entityReplayDepth > 0;
   }

   /** Lets custom model parts keep texture overrides in the current marker pass. */
   public static MultiBufferSource resolveReplayBuffer(MultiBufferSource original) {
      return activeReplayBuffer == null ? original : activeReplayBuffer;
   }

   public static void render(Minecraft minecraft, PoseStack poseStack, float partialTick) {
      long cpuStarted = NanovisionPerformanceProfiler.beginCpu();
      int gpuQuery = 0;

      try {
         LocalPlayer viewer = minecraft.player;
         if (!NanovisionConfig.thermalMarkerCompatibilityEntityPass()
            || minecraft.level == null
            || viewer == null
            || !NanovisionState.isActive(viewer)
            || !NanovisionController.shouldRenderMarkerPass()) {
            clearPreparedFrame();
            return;
         }

         RenderTarget target = ThermalMarkerRenderTarget.getOrCreate();
         if (target == null) {
            clearPreparedFrame();
            return;
         }

         Camera camera = minecraft.gameRenderer.getMainCamera();
         double cameraX = camera.getPosition().x;
         double cameraY = camera.getPosition().y;
         double cameraZ = camera.getPosition().z;
         EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
         List<ThermalMarkerCompatibilityRenderer.CompatibilityCandidate> renderCandidates = selectRenderCandidates();
         if (!renderCandidates.isEmpty()) {
            target.bindWrite(false);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            gpuQuery = NanovisionPerformanceProfiler.beginGpu(NanovisionPerformanceProfiler.Section.COMPATIBILITY_GPU);
            boolean traceEnabled = traceEnabled();
            boolean profileMobModels = NanovisionPerformanceProfiler.isCapturingMobModels();
            int tracedEntities = 0;
            int tracedAcceptedVertices = 0;
            int tracedUnsafeVertices = 0;
            int tracedSecondaryVertices = 0;
            ReplayBuffers replayBuffers = new ReplayBuffers();

            for (ThermalMarkerCompatibilityRenderer.CompatibilityCandidate candidate : renderCandidates) {
               LivingEntity livingEntity = candidate.entity();
               EntityRenderer<? super LivingEntity> renderer = candidate.renderer();
               if (!ThermalMarkerEntityRenderer.shouldRenderMarker(viewer, livingEntity)) {
                  if (traceEnabled) {
                     traceCandidateDecision(livingEntity, renderer, "render_filter_rejected");
                  }
               } else {
                  float encodedHeight = candidate.encodedHeight();
                  ThermalMobProfile profile = ThermalMobProfileResolver.resolve(livingEntity);
                  ThermalMarkerCompatibilityRenderer.CompatibilityBufferSource markerBuffer = new ThermalMarkerCompatibilityRenderer.CompatibilityBufferSource(
                     replayBuffers, encodedHeight, profile, traceEnabled, profileMobModels
                  );
                  double x = Mth.lerp(partialTick, livingEntity.xOld, livingEntity.getX()) - cameraX;
                  double y = Mth.lerp(partialTick, livingEntity.yOld, livingEntity.getY()) - cameraY;
                  double z = Mth.lerp(partialTick, livingEntity.zOld, livingEntity.getZ()) - cameraZ;
                  float yaw = Mth.lerp(partialTick, livingEntity.yRotO, livingEntity.getYRot());
                  long entityCpuStarted = profileMobModels ? System.nanoTime() : 0L;
                  MultiBufferSource previousReplayBuffer = activeReplayBuffer;

                  try {
                     ThermalMarkerRenderTarget.markSourceRendered();
                     PoseStack isolatedPoseStack = new PoseStack();
                     isolatedPoseStack.mulPose(new Matrix4f(poseStack.last().pose()));
                     entityReplayDepth++;
                     activeReplayBuffer = markerBuffer;
                     dispatcher.render(livingEntity, x, y, z, yaw, partialTick, isolatedPoseStack, markerBuffer, 15728880);
                     logRendererOnce(livingEntity, renderer);
                     traceReplay(livingEntity, renderer, profile, markerBuffer, "completed");
                  } catch (RuntimeException var51) {
                     logRendererFailureOnce(livingEntity, renderer, var51);
                     traceReplay(livingEntity, renderer, profile, markerBuffer, "failed:" + var51.getClass().getSimpleName());
                  } finally {
                     activeReplayBuffer = previousReplayBuffer;
                     if (entityReplayDepth > 0) {
                        entityReplayDepth--;
                     }

                     if (profileMobModels) {
                        Object typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(livingEntity.getType());
                        NanovisionPerformanceProfiler.recordMobCompatibilityReplay(
                           typeKey == null ? livingEntity.getType().toString() : typeKey.toString(),
                           renderer.getClass().getName(),
                           System.nanoTime() - entityCpuStarted,
                           markerBuffer.acceptedVertices()
                        );
                     }
                  }

                  if (traceEnabled) {
                     tracedEntities++;
                     tracedAcceptedVertices += markerBuffer.acceptedVertices();
                     tracedUnsafeVertices += markerBuffer.unsafeVertices();
                     tracedSecondaryVertices += markerBuffer.secondaryVertices();
                  }
               }
            }

            replayBuffers.flush();

            traceFlush(tracedEntities, tracedAcceptedVertices, tracedUnsafeVertices, tracedSecondaryVertices);
            minecraft.getMainRenderTarget().bindWrite(false);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            clearPreparedFrame();
            return;
         }

         clearPreparedFrame();
      } finally {
         NanovisionPerformanceProfiler.endGpu(NanovisionPerformanceProfiler.Section.COMPATIBILITY_GPU, gpuQuery);
         NanovisionPerformanceProfiler.endCpu(NanovisionPerformanceProfiler.Section.COMPATIBILITY_RENDER, cpuStarted);
      }
   }

   private static final class ReplayBuffers {
      private final LinkedHashMap<RenderType, ByteBufferBuilder> fixedBuffers = new LinkedHashMap<>();
      private final BufferSource source = MultiBufferSource.immediateWithBuffers(this.fixedBuffers, null);

      private VertexConsumer getBuffer(RenderType type) {
         if (!this.fixedBuffers.containsKey(type)) {
            int index = this.fixedBuffers.size();
            if (index >= MAX_REPLAY_TEXTURES) {
               return null;
            }
            if (REPLAY_STORAGE.size() == index) {
               REPLAY_STORAGE.add(new ByteBufferBuilder(1024));
            }
            // A model can switch textures halfway through rendering, then write
            // to its first consumer again. Keep each builder alive until flush.
            this.fixedBuffers.put(type, REPLAY_STORAGE.get(index));
         }
         return this.source.getBuffer(type);
      }

      private void flush() {
         this.fixedBuffers.forEach((type, storage) -> {
            try {
               this.source.endBatch(type);
            } catch (RuntimeException exception) {
               storage.discard();
               if (!warnedReplayBatch) {
                  warnedReplayBatch = true;
                  LOGGER.warn("Skipped an unsupported nanovision geometry batch", exception);
               }
            }
         });
      }
   }

   private static void clearPreparedFrame() {
      PREPARED_CANDIDATES.clear();
      PREPARED_LAYER_STANDBY_CANDIDATES.clear();
      DEFERRED_ENTITY_IDS.clear();
      RENDERED_LAYER_ENTITY_IDS.clear();
   }

   private static List<ThermalMarkerCompatibilityRenderer.CompatibilityCandidate> selectRenderCandidates() {
      if (PREPARED_CANDIDATES.isEmpty() && PREPARED_LAYER_STANDBY_CANDIDATES.isEmpty()) {
         return List.of();
      } else {
         Map<Integer, ThermalMarkerCompatibilityRenderer.CompatibilityCandidate> selectedByEntityId = new HashMap<>();

         for (ThermalMarkerCompatibilityRenderer.CompatibilityCandidate candidate : PREPARED_CANDIDATES) {
            selectedByEntityId.put(candidate.entity().getId(), candidate);
         }

         for (ThermalMarkerCompatibilityRenderer.CompatibilityCandidate candidate : PREPARED_LAYER_STANDBY_CANDIDATES) {
            if (RENDERED_LAYER_ENTITY_IDS.contains(candidate.entity().getId())) {
               if (traceEnabled()) {
                  traceCandidateDecision(candidate.entity(), candidate.renderer(), "attached_layer_confirmed");
               }
            } else {
               if (traceEnabled()) {
                  traceCandidateDecision(candidate.entity(), candidate.renderer(), "attached_layer_missing_fallback");
               }

               selectedByEntityId.putIfAbsent(candidate.entity().getId(), candidate);
            }
         }

         List<ThermalMarkerCompatibilityRenderer.CompatibilityCandidate> selected = new ArrayList<>(selectedByEntityId.values());
         selected.sort(NEAREST_FIRST);
         return selected.subList(0, Math.min(selected.size(), MAX_REPLAY_ENTITIES));
      }
   }

   private static List<ThermalMarkerCompatibilityRenderer.CompatibilityCandidate> collectCandidates(
      Minecraft minecraft, LocalPlayer viewer, ThermalCameraVisibility.View cameraVisibility, double cameraX, double cameraY, double cameraZ
   ) {
      List<ThermalMarkerCompatibilityRenderer.CompatibilityCandidate> candidates = new ArrayList<>();
      boolean traceEnabled = traceEnabled();
      long stableOrder = 0L;

      for (Entity entity : minecraft.level.entitiesForRendering()) {
         if (entity instanceof LivingEntity livingEntity) {
            if (!ThermalMarkerEntityRenderer.shouldRenderMarker(viewer, livingEntity)) {
               if (traceEnabled) {
                  traceCandidateDecision(livingEntity, null, "marker_filter_rejected");
               }
            } else {
               EntityRenderer<? super LivingEntity> renderer = rendererFor(minecraft, livingEntity);
               if (renderer == null) {
                  if (traceEnabled) {
                     traceCandidateDecision(livingEntity, null, "renderer_missing");
                  }
               } else {
                  boolean attachedLayerStandby = shouldSkipCompatibilityRenderer(livingEntity, renderer);
                  if (!cameraVisibility.isVisible(livingEntity.getBoundingBox())) {
                     if (traceEnabled) {
                        traceCandidateDecision(livingEntity, renderer, "owned_visibility_rejected");
                     }
                  } else {
                     float encodedHeight = ThermalScreenProjection.encodedProjectedHeight(livingEntity);
                     double distanceSqr = livingEntity.distanceToSqr(cameraX, cameraY, cameraZ);
                     long candidateOrder = stableOrder++;
                     ThermalMarkerCompatibilityRenderer.CompatibilityCandidate candidate = new ThermalMarkerCompatibilityRenderer.CompatibilityCandidate(
                        livingEntity, renderer, encodedHeight, distanceSqr, candidateOrder
                     );
                     if (attachedLayerStandby) {
                        PREPARED_LAYER_STANDBY_CANDIDATES.add(candidate);
                        if (traceEnabled) {
                           traceCandidateDecision(livingEntity, renderer, "attached_layer_standby");
                        }
                     } else {
                        candidates.add(candidate);
                     }
                  }
               }
            }
         }
      }

      return candidates;
   }

   private static EntityRenderer<? super LivingEntity> rendererFor(Minecraft minecraft, LivingEntity entity) {
      return minecraft.getEntityRenderDispatcher().getRenderer(entity);
   }

   private static boolean shouldSkipCompatibilityRenderer(LivingEntity entity, EntityRenderer<? super LivingEntity> renderer) {
      return !(renderer instanceof LivingEntityRenderer<?, ?> livingRenderer)
         ? false
         : ThermalMarkerRenderLayer.isAttachedTo(livingRenderer) && !ThermalMobProfileResolver.needsFullRendererFallback(entity, livingRenderer);
   }

   private static void logRendererOnce(LivingEntity entity, EntityRenderer<? super LivingEntity> renderer) {
      if (NanovisionConfig.thermalMarkerDebugLog()) {
         String key = renderer.getClass().getName();
         if (LOGGED_RENDERERS.add(key)) {
            LOGGER.info("[CrysisMod][Nanovision] compatibility thermal marker pass active for renderer={} entityType={}", key, entity.getType());
         }
      }
   }

   private static void logRendererFailureOnce(LivingEntity entity, EntityRenderer<? super LivingEntity> renderer, RuntimeException exception) {
      if (NanovisionConfig.thermalMarkerDebugLog()) {
         String key = "failed:" + renderer.getClass().getName();
         if (LOGGED_RENDERERS.add(key)) {
            LOGGER.warn(
               "[CrysisMod][Nanovision] compatibility thermal marker pass failed for renderer={} entityType={}",
               new Object[]{renderer.getClass().getName(), entity.getType(), exception}
            );
         }
      }
   }

   private static boolean traceEnabled() {
      return NanovisionConfig.thermalMarkerDebugShowRawMarker();
   }

   private static void updateTraceState() {
      boolean enabled = traceEnabled();
      if (enabled && !tracePreviouslyEnabled) {
         LAST_TRACE_NANOS.clear();
         LOGGER.info(
            "[CrysisMod][NanovisionTrace] raw-marker compatibility tracing enabled; shaderpack={}", NanoGeoShaderpackCompatibility.isShaderPackActive()
         );
      } else if (!enabled && tracePreviouslyEnabled) {
         LAST_TRACE_NANOS.clear();
         LOGGER.info("[CrysisMod][NanovisionTrace] raw-marker compatibility tracing disabled");
      }

      tracePreviouslyEnabled = enabled;
   }

   private static void traceCandidateDecision(LivingEntity entity, EntityRenderer<? super LivingEntity> renderer, String result) {
      if (traceEnabled()) {
         String entityType = entityTypeId(entity);
         String rendererName = rendererName(renderer);
         boolean shaderpackActive = NanoGeoShaderpackCompatibility.isShaderPackActive();
         if (shouldTrace("candidate:" + shaderpackActive + ":" + entityType + ":" + rendererName + ":" + result)) {
            LOGGER.info(
               "[CrysisMod][NanovisionTrace] candidate shaderpack={} entityType={} entityId={} renderer={} result={}",
               new Object[]{shaderpackActive, entityType, entity.getId(), rendererName, result}
            );
         }
      }
   }

   private static void traceReplay(
      LivingEntity entity,
      EntityRenderer<? super LivingEntity> renderer,
      ThermalMobProfile profile,
      ThermalMarkerCompatibilityRenderer.CompatibilityBufferSource markerBuffer,
      String result
   ) {
      if (markerBuffer.traceEnabled()) {
         String entityType = entityTypeId(entity);
         String rendererName = rendererName(renderer);
         boolean shaderpackActive = NanoGeoShaderpackCompatibility.isShaderPackActive();
         if (shouldTrace("replay:" + shaderpackActive + ":" + entityType + ":" + rendererName + ":" + result)) {
            LOGGER.info(
               "[CrysisMod][NanovisionTrace] replay shaderpack={} entityType={} entityId={} renderer={} result={} profile={} primaryRenderType={} bufferRequests={} acceptedRequests={} unsafeRequests={} secondaryRequests={} acceptedVertices={} unsafeVertices={} secondaryVertices={} routes={}",
               new Object[]{
                  shaderpackActive,
                  entityType,
                  entity.getId(),
                  rendererName,
                  result,
                  profile,
                  markerBuffer.primaryRenderTypeName(),
                  markerBuffer.bufferRequests(),
                  markerBuffer.acceptedRequests(),
                  markerBuffer.unsafeRequests(),
                  markerBuffer.secondaryRequests(),
                  markerBuffer.acceptedVertices(),
                  markerBuffer.unsafeVertices(),
                  markerBuffer.secondaryVertices(),
                  markerBuffer.routes()
               }
            );
         }
      }
   }

   private static void traceFlush(int entities, int acceptedVertices, int unsafeVertices, int secondaryVertices) {
      if (traceEnabled()) {
         boolean shaderpackActive = NanoGeoShaderpackCompatibility.isShaderPackActive();
         if (shouldTrace("flush:" + shaderpackActive)) {
            LOGGER.info(
               "[CrysisMod][NanovisionTrace] flush shaderpack={} result=completed entities={} acceptedVertices={} unsafeVertices={} secondaryVertices={}",
               new Object[]{shaderpackActive, entities, acceptedVertices, unsafeVertices, secondaryVertices}
            );
         }
      }
   }

   private static boolean shouldTrace(String key) {
      long now = System.nanoTime();
      Long previous = LAST_TRACE_NANOS.get(key);
      if (previous != null && now - previous < 4000000000L) {
         return false;
      } else {
         LAST_TRACE_NANOS.put(key, now);
         return true;
      }
   }

   private static String entityTypeId(LivingEntity entity) {
      Object key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
      return key == null ? entity.getType().toString() : key.toString();
   }

   private static String rendererName(EntityRenderer<? super LivingEntity> renderer) {
      return renderer == null ? "<none>" : renderer.getClass().getName();
   }

   private static final class CompatibilityBufferSource implements MultiBufferSource {
      private final ReplayBuffers delegate;
      private final float encodedHeight;
      private final ThermalMobProfile profile;
      private final boolean traceEnabled;
      private final boolean countVertices;
      private final VertexConsumer unsafeConsumer;
      private final VertexConsumer secondaryConsumer;
      private final Set<String> routes;
      private final Map<RenderType, VertexConsumer> markerConsumers = new IdentityHashMap<>();
      private RenderType primaryRenderType;
      private int bufferRequests;
      private int acceptedRequests;
      private int unsafeRequests;
      private int secondaryRequests;
      private int acceptedVertices;
      private int unsafeVertices;
      private int secondaryVertices;

      private CompatibilityBufferSource(
         ReplayBuffers delegate,
         float encodedHeight,
         ThermalMobProfile profile,
         boolean traceEnabled,
         boolean countVertices
      ) {
         this.delegate = delegate;
         this.encodedHeight = encodedHeight;
         this.profile = profile;
         this.traceEnabled = traceEnabled;
         this.countVertices = countVertices || traceEnabled;
         if (this.traceEnabled) {
            this.unsafeConsumer = new ThermalMarkerCompatibilityRenderer.CountingNoOpVertexConsumer(
               this, ThermalMarkerCompatibilityRenderer.DroppedRoute.UNSAFE
            );
            this.secondaryConsumer = new ThermalMarkerCompatibilityRenderer.CountingNoOpVertexConsumer(
               this, ThermalMarkerCompatibilityRenderer.DroppedRoute.SECONDARY
            );
            this.routes = new LinkedHashSet<>();
         } else {
            this.unsafeConsumer = ThermalMarkerCompatibilityRenderer.NoOpVertexConsumer.INSTANCE;
            this.secondaryConsumer = ThermalMarkerCompatibilityRenderer.NoOpVertexConsumer.INSTANCE;
            this.routes = Set.of();
         }
      }

      public VertexConsumer getBuffer(RenderType sourceType) {
         RenderType renderType = ThermalExternalModelCompatibility.unwrapIrisRenderType(sourceType);
         if (this.traceEnabled) {
            this.bufferRequests++;
         }

         if (isUnsafeNonModelRenderType(renderType)) {
            if (this.traceEnabled) {
               this.unsafeRequests++;
               this.recordRoute("unsafe", renderType);
            }

            return this.unsafeConsumer;
         } else {
            RenderType markerType = markerTypeFor(renderType);
            if (markerType == null) {
               if (this.traceEnabled) {
                  this.unsafeRequests++;
                  this.recordRoute("unsupported", renderType);
               }

               return this.unsafeConsumer;
            } else {
               if (this.primaryRenderType == null) {
                  this.primaryRenderType = renderType;
               } else if (!this.primaryRenderType.equals(renderType) && this.traceEnabled) {
                  this.secondaryRequests++;
                  this.recordRoute("accepted_secondary", renderType);
               }

               if (this.traceEnabled) {
                  this.acceptedRequests++;
                  this.recordRoute("accepted", renderType);
               }

               return this.markerConsumers.computeIfAbsent(renderType, ignored -> {
                  VertexConsumer markerDestination = this.delegate.getBuffer(markerType);
                  if (markerDestination == null) {
                     return this.unsafeConsumer;
                  }
                  if (this.countVertices) {
                     markerDestination = new ThermalMarkerCompatibilityRenderer.CountingVertexConsumer(markerDestination, this);
                  }

                  return new ThermalEncodedVertexConsumer(markerDestination, renderType.format(), this.encodedHeight, this.profile);
               });
            }
         }
      }

      private void recordRoute(String route, RenderType renderType) {
         if (this.routes.size() < 8) {
            String description = String.valueOf(renderType).replace('\n', ' ').replace('\r', ' ');
            if (description.length() > 180) {
               description = description.substring(0, 177) + "...";
            }

            this.routes.add(route + "=" + description);
         }
      }

      private boolean traceEnabled() {
         return this.traceEnabled;
      }

      private String primaryRenderTypeName() {
         return this.primaryRenderType == null ? "<none>" : String.valueOf(this.primaryRenderType);
      }

      private int bufferRequests() {
         return this.bufferRequests;
      }

      private int acceptedRequests() {
         return this.acceptedRequests;
      }

      private int unsafeRequests() {
         return this.unsafeRequests;
      }

      private int secondaryRequests() {
         return this.secondaryRequests;
      }

      private int acceptedVertices() {
         return this.acceptedVertices;
      }

      private int unsafeVertices() {
         return this.unsafeVertices;
      }

      private int secondaryVertices() {
         return this.secondaryVertices;
      }

      private Set<String> routes() {
         return this.routes;
      }

      private static boolean isUnsafeNonModelRenderType(RenderType renderType) {
         String name = renderType.toString().toLowerCase(Locale.ROOT);
         return name.contains("shadow") || name.contains("leash") || name.contains("glint") || name.contains("debug") || name.contains("beam");
      }

      private static RenderType markerTypeFor(RenderType sourceType) {
         return ThermalMarkerRenderTypes.alphaAwareMarker(sourceType);
      }
   }

   private record CompatibilityCandidate(
      LivingEntity entity, EntityRenderer<? super LivingEntity> renderer, float encodedHeight, double distanceSqr, long stableOrder
   ) {
   }

   private static final class CountingNoOpVertexConsumer implements VertexConsumer {
      private final ThermalMarkerCompatibilityRenderer.CompatibilityBufferSource owner;
      private final ThermalMarkerCompatibilityRenderer.DroppedRoute route;

      private CountingNoOpVertexConsumer(
         ThermalMarkerCompatibilityRenderer.CompatibilityBufferSource owner, ThermalMarkerCompatibilityRenderer.DroppedRoute route
      ) {
         this.owner = owner;
         this.route = route;
      }

      public VertexConsumer addVertex(float x, float y, float z) {
         if (this.route == ThermalMarkerCompatibilityRenderer.DroppedRoute.UNSAFE) {
            this.owner.unsafeVertices++;
         } else {
            this.owner.secondaryVertices++;
         }
         return this;
      }

      public VertexConsumer setColor(int red, int green, int blue, int alpha) {
         return this;
      }

      public VertexConsumer setUv(float u, float v) {
         return this;
      }

      public VertexConsumer setUv1(int u, int v) {
         return this;
      }

      public VertexConsumer setUv2(int u, int v) {
         return this;
      }

      public VertexConsumer setNormal(float x, float y, float z) {
         return this;
      }
   }

   private static final class CountingVertexConsumer implements VertexConsumer {
      private final VertexConsumer delegate;
      private final ThermalMarkerCompatibilityRenderer.CompatibilityBufferSource owner;

      private CountingVertexConsumer(VertexConsumer delegate, ThermalMarkerCompatibilityRenderer.CompatibilityBufferSource owner) {
         this.delegate = delegate;
         this.owner = owner;
      }

      public VertexConsumer addVertex(float x, float y, float z) {
         this.delegate.addVertex(x, y, z);
         this.owner.acceptedVertices++;
         return this;
      }

      public VertexConsumer setColor(int red, int green, int blue, int alpha) {
         this.delegate.setColor(red, green, blue, alpha);
         return this;
      }

      public VertexConsumer setUv(float u, float v) {
         this.delegate.setUv(u, v);
         return this;
      }

      public VertexConsumer setUv1(int u, int v) {
         this.delegate.setUv1(u, v);
         return this;
      }

      public VertexConsumer setUv2(int u, int v) {
         this.delegate.setUv2(u, v);
         return this;
      }

      public VertexConsumer setNormal(float x, float y, float z) {
         this.delegate.setNormal(x, y, z);
         return this;
      }
   }

   private static enum DroppedRoute {
      UNSAFE,
      SECONDARY;
   }

   private static final class NoOpVertexConsumer implements VertexConsumer {
      private static final ThermalMarkerCompatibilityRenderer.NoOpVertexConsumer INSTANCE = new ThermalMarkerCompatibilityRenderer.NoOpVertexConsumer();

      public VertexConsumer addVertex(float x, float y, float z) {
         return this;
      }

      public VertexConsumer setColor(int red, int green, int blue, int alpha) {
         return this;
      }

      public VertexConsumer setUv(float u, float v) {
         return this;
      }

      public VertexConsumer setUv1(int u, int v) {
         return this;
      }

      public VertexConsumer setUv2(int u, int v) {
         return this;
      }

      public VertexConsumer setNormal(float x, float y, float z) {
         return this;
      }
   }
}
