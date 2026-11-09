package io.github.painworks.crysismod.client.nanovision.thermal.block;

public enum ThermalBlockHeatProfile {
   NONE(0, 0.0F, 1.0F),
   WARM(1, 0.7F, 0.18F),
   HOT(2, 0.87F, 0.07F),
   EXTREME(3, 0.98F, 0.01F);

   private static final ThermalBlockHeatProfile[] VALUES = values();
   private final byte id;
   private final float heat;
   private final float coldMaterial;

   private ThermalBlockHeatProfile(int id, float heat, float coldMaterial) {
      this.id = (byte)id;
      this.heat = heat;
      this.coldMaterial = coldMaterial;
   }

   public byte id() {
      return this.id;
   }

   public float heat() {
      return this.heat;
   }

   public float coldMaterial() {
      return this.coldMaterial;
   }

   public static ThermalBlockHeatProfile fromId(byte id) {
      int index = Byte.toUnsignedInt(id);
      return index < VALUES.length ? VALUES[index] : NONE;
   }
}
