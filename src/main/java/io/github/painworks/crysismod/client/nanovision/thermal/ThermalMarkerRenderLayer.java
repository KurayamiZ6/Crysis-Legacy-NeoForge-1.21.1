package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.preview.NanovisionSettingsPreviewContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;

public final class ThermalMarkerRenderLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final long TRACE_INTERVAL_NANOS = 4000000000L;
   private static final Set<LivingEntityRenderer<?, ?>> ATTACHED_RENDERERS = Collections.newSetFromMap(new WeakHashMap<>());
   private static final Map<String, Long> LAST_ZOMBIE_TRACE_NANOS = new HashMap<>();
   private final LivingEntityRenderer<T, M> parentRenderer;

   public ThermalMarkerRenderLayer(RenderLayerParent<T, M> parent, LivingEntityRenderer<T, M> parentRenderer) {
      super(parent);
      this.parentRenderer = parentRenderer;
      synchronized (ATTACHED_RENDERERS) {
         ATTACHED_RENDERERS.add(parentRenderer);
      }
   }

   public static boolean isAttachedTo(LivingEntityRenderer<?, ?> renderer) {
      synchronized (ATTACHED_RENDERERS) {
         return ATTACHED_RENDERERS.contains(renderer);
      }
   }

   public void render(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      T entity,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      boolean settingsPreview = NanovisionSettingsPreviewContext.isRendering();
      boolean traceZombie = !settingsPreview && shouldTraceZombie(entity);
      LocalPlayer viewer = Minecraft.getInstance().player;
      if (settingsPreview) {
         if (!NanovisionConfig.thermalMarkerEnabled() || !NanovisionConfig.thermalMarkerRenderPlayers()) {
            return;
         }
      } else if (!ThermalMarkerEntityRenderer.shouldRenderMarker(viewer, entity)) {
         this.traceZombieLayer(entity, "render_filter_rejected", null, null);
         return;
      }

      if (ThermalMarkerCompatibilityRenderer.shouldDeferLayer(entity)) {
         this.traceZombieLayer(entity, "deferred_to_compatibility", null, null);
      } else {
         ResourceLocation texture = this.parentRenderer.getTextureLocation(entity);
         if (texture == null) {
            this.traceZombieLayer(entity, "texture_missing", null, null);
         } else {
            ThermalMobProfile profile = ThermalMobProfileResolver.resolve(entity);
            float encodedHeight = settingsPreview
               ? NanovisionSettingsPreviewContext.encodedProjectedHeight()
               : ThermalScreenProjection.encodedProjectedHeight(entity);
            if (traceZombie) {
               ThermalMarkerModelRenderer.TraceResult result = ThermalMarkerModelRenderer.renderTexturedMarkerWithTrace(
                  this.getParentModel(), entity, poseStack, bufferSource, texture, ThermalLayerProfile.of(profile), encodedHeight
               );
               ThermalMarkerCompatibilityRenderer.markLayerRendered(entity);
               this.traceZombieLayer(entity, "submitted", texture, result);
            } else {
               ThermalMarkerModelRenderer.renderTexturedMarker(
                  this.getParentModel(), entity, poseStack, bufferSource, texture, ThermalLayerProfile.of(profile), encodedHeight
               );
               if (!settingsPreview) {
                  ThermalMarkerCompatibilityRenderer.markLayerRendered(entity);
               }
            }
         }
      }
   }

   private static boolean shouldTraceZombie(LivingEntity entity) {
      if (!NanovisionConfig.thermalMarkerDebugShowRawMarker()) {
         return false;
      } else {
         ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
         return key != null && "minecraft".equals(key.getNamespace()) && "zombie".equals(key.getPath());
      }
   }

   private void traceZombieLayer(T entity, String result, ResourceLocation texture, ThermalMarkerModelRenderer.TraceResult submission) {
      if (shouldTraceZombie(entity)) {
         long now = System.nanoTime();
         Long previous = LAST_ZOMBIE_TRACE_NANOS.get(result);
         if (previous == null || now - previous >= 4000000000L) {
            LAST_ZOMBIE_TRACE_NANOS.put(result, now);
            M model = (M)this.getParentModel();
            LOGGER.info(
               "[CrysisMod][NanovisionTrace] attached_layer entityType=minecraft:zombie entityId={} renderer={} model={} emfCustom={} texture={} result={} vertices={} route={}",
               new Object[]{
                  entity.getId(),
                  this.parentRenderer.getClass().getName(),
                  model.getClass().getName(),
                  ThermalExternalModelCompatibility.requiresFullRendererFallback(model),
                  texture == null ? "<none>" : texture,
                  result,
                  submission == null ? 0 : submission.vertexCount(),
                  submission == null ? "<none>" : submission.route()
               }
            );
         }
      }
   }
}
