package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.VertexConsumer;

final class ThermalAtlasUvVertexConsumer implements VertexConsumer {
   private final VertexConsumer delegate;
   private final ThermalMarkerTextureAtlas.Region region;

   ThermalAtlasUvVertexConsumer(VertexConsumer delegate, ThermalMarkerTextureAtlas.Region region) {
      this.delegate = delegate;
      this.region = region;
   }

   public VertexConsumer addVertex(float x, float y, float z) {
      this.delegate.addVertex(x, y, z);
      return this;
   }

   public VertexConsumer setColor(int red, int green, int blue, int alpha) {
      this.delegate.setColor(red, green, blue, alpha);
      return this;
   }

   public VertexConsumer setUv(float u, float v) {
      this.delegate.setUv(this.region.remapU(u), this.region.remapV(v));
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
