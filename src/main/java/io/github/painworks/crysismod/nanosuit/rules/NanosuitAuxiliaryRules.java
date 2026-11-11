package io.github.painworks.crysismod.nanosuit.rules;

public record NanosuitAuxiliaryRules(boolean nanovision, boolean nanoVisorMarking) {
   public static final NanosuitAuxiliaryRules LEGACY_CURRENT = new NanosuitAuxiliaryRules(true, true);
}
