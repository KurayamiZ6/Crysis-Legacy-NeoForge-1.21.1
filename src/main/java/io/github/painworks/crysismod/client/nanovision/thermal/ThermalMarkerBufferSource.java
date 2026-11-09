package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.LinkedHashMap;
import java.util.SequencedMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;

public final class ThermalMarkerBufferSource {
   private static final int FALLBACK_BUFFER_SIZE = 65536;
   private static final int ATLAS_BUFFER_SIZE = 262144;
   private static final SequencedMap<RenderType, ByteBufferBuilder> FIXED_MARKER_BUFFERS = new LinkedHashMap<>();
   private static final ByteBufferBuilder FALLBACK_BUFFER = new ByteBufferBuilder(65536);
   private static final BufferSource SOURCE = MultiBufferSource.immediateWithBuffers(FIXED_MARKER_BUFFERS, FALLBACK_BUFFER);

   private ThermalMarkerBufferSource() {
   }

   public static VertexConsumer atlas(RenderType renderType) {
      return fixed(renderType, 262144);
   }

   public static VertexConsumer fallback(RenderType renderType) {
      return fixed(renderType, 65536);
   }

   private static VertexConsumer fixed(RenderType renderType, int minimumSize) {
      FIXED_MARKER_BUFFERS.computeIfAbsent(renderType, ignored -> new ByteBufferBuilder(Math.max(minimumSize, renderType.bufferSize())));
      return SOURCE.getBuffer(renderType);
   }

   public static void flush() {
      SOURCE.endBatch();
   }

   public static void reset() {
      SOURCE.endBatch();

      for (ByteBufferBuilder buffer : FIXED_MARKER_BUFFERS.values()) {
         buffer.close();
      }

      FIXED_MARKER_BUFFERS.clear();
      FALLBACK_BUFFER.discard();
   }
}
