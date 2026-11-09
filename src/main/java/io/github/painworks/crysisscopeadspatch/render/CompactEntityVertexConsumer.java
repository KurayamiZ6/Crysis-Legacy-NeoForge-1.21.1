package io.github.painworks.crysisscopeadspatch.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

/** Adapts entity vertices to the compact position/uv/color/normal capture format. */
public final class CompactEntityVertexConsumer implements VertexConsumer {
   private final VertexConsumer delegate;
   private int red = 255, green = 255, blue = 255, alpha = 255;
   public CompactEntityVertexConsumer(VertexConsumer delegate) { this.delegate = delegate; }
   public VertexConsumer addVertex(float x, float y, float z) {
      delegate.addVertex(x, y, z); red = green = blue = alpha = 255; return this;
   }
   public VertexConsumer setColor(int r, int g, int b, int a) { red=r; green=g; blue=b; alpha=a; return this; }
   public VertexConsumer setUv(float u, float v) { delegate.setUv(u, v); delegate.setColor(red, green, blue, alpha); return this; }
   public VertexConsumer setUv1(int u, int v) { return this; }
   public VertexConsumer setUv2(int u, int v) { return this; }
   public VertexConsumer setNormal(float x, float y, float z) { delegate.setNormal(x, y, z); return this; }
}
