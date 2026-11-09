package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.debug.NanovisionPerformanceProfiler;
import io.github.painworks.crysismod.client.nanovision.preview.NanovisionSettingsPreviewContext;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ThermalMarkerModelRenderer {
   private ThermalMarkerModelRenderer() {
   }

   public static <T extends LivingEntity> void renderTexturedMarker(
      EntityModel<T> model,
      T entity,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      ResourceLocation texture,
      ThermalLayerProfile profile,
      float encodedHeight
   ) {
      renderTexturedMarkerInternal(model, entity, poseStack, bufferSource, texture, profile, encodedHeight, false);
   }

   static <T extends LivingEntity> ThermalMarkerModelRenderer.TraceResult renderTexturedMarkerWithTrace(
      EntityModel<T> model,
      T entity,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      ResourceLocation texture,
      ThermalLayerProfile profile,
      float encodedHeight
   ) {
      return renderTexturedMarkerInternal(model, entity, poseStack, bufferSource, texture, profile, encodedHeight, true);
   }

   private static <T extends LivingEntity> ThermalMarkerModelRenderer.TraceResult renderTexturedMarkerInternal(
      EntityModel<T> model,
      T entity,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      ResourceLocation texture,
      ThermalLayerProfile profile,
      float encodedHeight,
      boolean trace
   ) {
      ThermalMarkerRenderTarget.markSourceRendered();
      long cpuStarted = NanovisionPerformanceProfiler.beginCpu();
      boolean optimizedWorldV3 = NanovisionConfig.thermalVisualPreset().usesOptimizedV3Pipeline() && !NanovisionSettingsPreviewContext.isRendering();
      VertexConsumer consumer;
      String route;
      if (optimizedWorldV3) {
         ThermalMarkerTextureAtlas.Region atlasRegion = ThermalMarkerTextureAtlas.INSTANCE.resolve(texture);
         if (atlasRegion != null) {
            RenderType atlasRenderType = ThermalMarkerRenderTypes.markerAtlas(atlasRegion.atlasTexture());
            consumer = new ThermalAtlasUvVertexConsumer(ThermalMarkerBufferSource.atlas(atlasRenderType), atlasRegion);
            route = "v3_atlas";
         } else {
            consumer = ThermalMarkerBufferSource.fallback(ThermalMarkerRenderTypes.markerV3Fallback(texture));
            route = "v3_texture_fallback";
         }
      } else if (NanovisionConfig.thermalVisualPreset().usesOptimizedV3Pipeline()) {
         consumer = bufferSource.getBuffer(ThermalMarkerRenderTypes.markerV3Fallback(texture));
         route = "v3_preview_fallback";
      } else {
         consumer = bufferSource.getBuffer(ThermalMarkerRenderTypes.marker(texture));
         route = "legacy_texture";
      }

      boolean profileMobModels = NanovisionPerformanceProfiler.isCapturingMobModels();
      ThermalMarkerModelRenderer.CountingVertexConsumer countingConsumer = null;
      if (trace || profileMobModels) {
         countingConsumer = new ThermalMarkerModelRenderer.CountingVertexConsumer(consumer);
         consumer = countingConsumer;
      }

      long mobModelStarted = profileMobModels ? System.nanoTime() : 0L;

      try {
         model.renderToBuffer(
            poseStack,
            consumer,
            15728880,
            OverlayTexture.NO_OVERLAY,
            FastColor.ARGB32.colorFromFloat(profile.emissionClass().encoded(), encodedHeight, profile.baseHeat(), profile.coldInsulation())
         );
      } finally {
         NanovisionPerformanceProfiler.endCpu(NanovisionPerformanceProfiler.Section.MARKER_MODEL, cpuStarted);
         if (profileMobModels && countingConsumer != null) {
            ResourceLocation typeKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            NanovisionPerformanceProfiler.recordMobLayerModel(
               typeKey == null ? entity.getType().toString() : typeKey.toString(),
               model.getClass().getName(),
               route,
               System.nanoTime() - mobModelStarted,
               countingConsumer.vertexCount()
            );
         }
      }

      return trace ? new ThermalMarkerModelRenderer.TraceResult(countingConsumer.vertexCount(), route) : null;
   }

   private static final class CountingVertexConsumer implements VertexConsumer {
      private final VertexConsumer delegate;
      private int vertexCount;

      private CountingVertexConsumer(VertexConsumer delegate) {
         this.delegate = delegate;
      }

      public VertexConsumer addVertex(float x, float y, float z) {
         this.delegate.addVertex(x, y, z);
         this.vertexCount++;
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

      private int vertexCount() {
         return this.vertexCount;
      }
   }

   record TraceResult(int vertexCount, String route) {
   }
}
