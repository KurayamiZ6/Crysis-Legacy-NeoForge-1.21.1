package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import java.util.SequencedMap;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;

final class ThermalDeferredBufferSource extends BufferSource {
   ThermalDeferredBufferSource(SequencedMap<RenderType, ByteBufferBuilder> buffers, ByteBufferBuilder fallback) {
      super(fallback, buffers);
   }

   boolean hasPendingBatches() {
      return !this.startedBuilders.isEmpty();
   }
}
