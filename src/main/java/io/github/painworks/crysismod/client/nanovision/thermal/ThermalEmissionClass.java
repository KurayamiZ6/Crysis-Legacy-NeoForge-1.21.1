package io.github.painworks.crysismod.client.nanovision.thermal;

public enum ThermalEmissionClass {
   COLD(0, 0.3F),
   COOL(1, 0.42F),
   UNKNOWN(2, 0.62F),
   WARM(3, 1.0F),
   HOT(4, 0.3F),
   HOT_EMITTER(5, 0.22F);

   private final int packedId;
   private final float glowResponse;

   private ThermalEmissionClass(int packedId, float glowResponse) {
      this.packedId = packedId;
      this.glowResponse = glowResponse;
   }

   public float encoded() {
      return this.packedId / 7.0F;
   }

   public int encodedByte() {
      return Math.round(this.encoded() * 255.0F);
   }

   public float glowResponse() {
      return this.glowResponse;
   }
}
