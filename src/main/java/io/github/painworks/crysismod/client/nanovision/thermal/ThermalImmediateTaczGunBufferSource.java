package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;

final class ThermalImmediateTaczGunBufferSource extends BufferSource {
   private static final ByteBufferBuilder UNUSED_BUFFER = new ByteBufferBuilder(256);
   // The physical and thermal consumers must stay writable at the same time.
   private static final BufferSource THERMAL_SOURCE = MultiBufferSource.immediate(new ByteBufferBuilder(32768));
   private final BufferSource delegate;
   private final Map<RenderType, RenderType> thermalTypes = new LinkedHashMap<>();

   ThermalImmediateTaczGunBufferSource(BufferSource delegate) {
      super(UNUSED_BUFFER, new LinkedHashMap<>());
      this.delegate = delegate;
   }

   public VertexConsumer getBuffer(RenderType renderType) {
      ThermalFirstPersonItemCapture.beginSourceDelegation();

      VertexConsumer physical;
      try {
         physical = this.delegate.getBuffer(renderType);
      } finally {
         ThermalFirstPersonItemCapture.endSourceDelegation();
      }

      VertexConsumer outline = ThermalShaderOffItemOutlineCapture.capture(renderType);
      RenderType thermalType = ThermalFirstPersonItemCapture.prepareImmediateTaczGunThermalPass(renderType);
      if (thermalType == null) {
         return outline != null && outline != physical ? VertexMultiConsumer.create(physical, outline) : physical;
      } else {
         VertexConsumer thermal;
         ThermalFirstPersonItemCapture.beginSourceDelegation();
         try {
            thermal = THERMAL_SOURCE.getBuffer(thermalType);
         } finally {
            ThermalFirstPersonItemCapture.endSourceDelegation();
         }

         this.thermalTypes.put(renderType, thermalType);
         return outline != null && outline != physical && outline != thermal
            ? VertexMultiConsumer.create(new VertexConsumer[]{physical, thermal, outline})
            : VertexMultiConsumer.create(physical, thermal);
      }
   }

   public void endLastBatch() {
      this.delegate.endLastBatch();
      THERMAL_SOURCE.endLastBatch();
   }

   public void endBatch(RenderType renderType) {
      RenderType thermalType = this.thermalTypes.remove(renderType);
      this.delegate.endBatch(renderType);
      if (thermalType != null) {
         THERMAL_SOURCE.endBatch(thermalType);
      }
   }

   public void endBatch() {
      Map<RenderType, RenderType> submitted = new LinkedHashMap<>(this.thermalTypes);
      this.thermalTypes.clear();

      for (RenderType physicalType : submitted.keySet()) {
         this.delegate.endBatch(physicalType);
      }

      for (RenderType thermalType : submitted.values()) {
         THERMAL_SOURCE.endBatch(thermalType);
      }

      this.delegate.endBatch();
   }
}
