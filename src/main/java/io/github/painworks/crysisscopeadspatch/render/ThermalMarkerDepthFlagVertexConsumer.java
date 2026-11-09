package io.github.painworks.crysisscopeadspatch.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

public final class ThermalMarkerDepthFlagVertexConsumer implements VertexConsumer {
   private final VertexConsumer delegate;
   private final int flags;
   public ThermalMarkerDepthFlagVertexConsumer(VertexConsumer delegate, int flags) { this.delegate=delegate; this.flags=flags; }
   public VertexConsumer addVertex(float x, float y, float z) { delegate.addVertex(x,y,z); return this; }
   public VertexConsumer setColor(int r, int g, int b, int a) { delegate.setColor(r,g,b,a); return this; }
   public VertexConsumer setUv(float u, float v) { delegate.setUv(u,v); return this; }
   public VertexConsumer setUv1(int u, int v) { delegate.setUv1(flags,0); return this; }
   public VertexConsumer setUv2(int u, int v) { delegate.setUv2(u,v); return this; }
   public VertexConsumer setNormal(float x, float y, float z) { delegate.setNormal(x,y,z); return this; }
}
