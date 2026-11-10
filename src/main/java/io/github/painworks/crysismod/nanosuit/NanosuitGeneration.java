package io.github.painworks.crysismod.nanosuit;

public enum NanosuitGeneration {
   LEGACY_CURRENT,
   NANOSUIT_1,
   NANOSUIT_2;

   public static NanosuitGeneration fullSet(
      NanosuitGeneration head, NanosuitGeneration chest, NanosuitGeneration legs, NanosuitGeneration feet
   ) {
      return head != null && head == chest && head == legs && head == feet ? head : null;
   }
}
