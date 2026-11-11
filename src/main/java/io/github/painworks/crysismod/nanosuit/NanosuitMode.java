package io.github.painworks.crysismod.nanosuit;

public enum NanosuitMode {
   NONE(0),
   ARMOR(1),
   CLOAK(2),
   STRENGTH(3),
   SPEED(4);

   private final int wireId;

   NanosuitMode(int wireId) {
      this.wireId = wireId;
   }

   public int wireId() {
      return this.wireId;
   }

   public static NanosuitMode fromWireId(int wireId) {
      return switch (wireId) {
         case 0 -> NONE;
         case 1 -> ARMOR;
         case 2 -> CLOAK;
         case 3 -> STRENGTH;
         case 4 -> SPEED;
         default -> throw new IllegalArgumentException("Unknown Nanosuit mode ID: " + wireId);
      };
   }
}
