package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

final class ThermalEncodedVertexConsumer implements VertexConsumer {
   private final VertexConsumer delegate;
   private final int encodedHeight;
   private final int baseHeat;
   private final int coldInsulation;
   private final int emissionClass;
   private final boolean sourceHasUv;
   private final boolean sourceHasNormal;

   ThermalEncodedVertexConsumer(VertexConsumer delegate, VertexFormat sourceFormat, float encodedHeight, ThermalMobProfile profile) {
      this.delegate = delegate;
      this.encodedHeight = toByte(encodedHeight);
      this.baseHeat = toByte(profile.baseHeat());
      this.coldInsulation = toByte(profile.coldInsulation());
      this.emissionClass = profile.emissionClass().encodedByte();
      this.sourceHasUv = sourceFormat.hasUV(0);
      this.sourceHasNormal = sourceFormat.contains(VertexFormatElement.NORMAL);
   }

   @Override
   public VertexConsumer addVertex(float x, float y, float z) {
      this.delegate.addVertex(x, y, z)
         .setColor(this.encodedHeight, this.baseHeat, this.coldInsulation, this.emissionClass)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880);
      if (!this.sourceHasUv) {
         this.delegate.setUv(0.0F, 0.0F);
      }
      if (!this.sourceHasNormal) {
         this.delegate.setNormal(0.0F, 1.0F, 0.0F);
      }
      return this;
   }

   @Override
   public VertexConsumer setColor(int red, int green, int blue, int alpha) {
      return this;
   }

   @Override
   public VertexConsumer setUv(float u, float v) {
      this.delegate.setUv(u, v);
      return this;
   }

   @Override
   public VertexConsumer setUv1(int u, int v) {
      return this;
   }

   @Override
   public VertexConsumer setUv2(int u, int v) {
      return this;
   }

   @Override
   public VertexConsumer setNormal(float x, float y, float z) {
      this.delegate.setNormal(x, y, z);
      return this;
   }

   private static int toByte(float value) {
      return Mth.clamp((int)(value * 255.0F), 0, 255);
   }
}
