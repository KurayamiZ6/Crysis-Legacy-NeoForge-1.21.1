package io.github.painworks.crysismod.nanosuit.util;

import io.github.painworks.crysismod.nanosuit.NanosuitGeneration;

public enum NanosuitArmorVariant {
   NANOGEO(NanosuitGeneration.LEGACY_CURRENT);

   private final NanosuitGeneration generation;

   NanosuitArmorVariant(NanosuitGeneration generation) {
      this.generation = generation;
   }

   NanosuitGeneration generation() {
      return generation;
   }
}
