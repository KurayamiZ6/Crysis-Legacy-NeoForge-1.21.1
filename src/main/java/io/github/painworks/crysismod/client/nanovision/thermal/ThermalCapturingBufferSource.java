package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import java.util.LinkedHashMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;

final class ThermalCapturingBufferSource extends BufferSource {
   private static final ByteBufferBuilder UNUSED_BUFFER = new ByteBufferBuilder(256);
   private final BufferSource delegate;
   private final CaptureChannel channel;

   ThermalCapturingBufferSource(BufferSource delegate, CaptureChannel channel) {
      super(UNUSED_BUFFER, new LinkedHashMap<>());
      this.delegate = delegate;
      this.channel = channel;
   }

   BufferSource physicalSource() {
      return this.delegate;
   }

   public VertexConsumer getBuffer(RenderType renderType) {
      ThermalFirstPersonItemCapture.beginSourceDelegation();

      VertexConsumer original;
      try {
         original = this.delegate.getBuffer(renderType);
      } finally {
         ThermalFirstPersonItemCapture.endSourceDelegation();
      }

      VertexConsumer accepted = ThermalFirstPersonItemCapture.wrap(this.channel, renderType, original);
      if (this.channel != CaptureChannel.DEFAULT) {
         return accepted;
      } else {
         VertexConsumer halo = accepted != original ? ThermalShaderOffItemOutlineCapture.capture(renderType) : null;
         return halo != null && halo != accepted ? VertexMultiConsumer.create(accepted, halo) : accepted;
      }
   }

   public void endLastBatch() {
      this.delegate.endLastBatch();
   }

   public void endBatch() {
      this.delegate.endBatch();
   }

   public void endBatch(RenderType renderType) {
      this.delegate.endBatch(renderType);
   }
}
