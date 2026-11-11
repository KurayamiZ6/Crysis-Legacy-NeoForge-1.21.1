import io.github.painworks.crysismod.nanosuit.NanosuitGeneration;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitCombatRules.CloakCost;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitCombatRules.Melee;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitControls;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitControls.Ability;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitControls.ManualMode;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitMobilityRules.Jump;
import java.util.EnumSet;
import java.util.Set;

// Run with Java 21 after compiling the mod:
// java -cp build/classes/java/main qa/NanosuitArchitectureQa.java
public final class NanosuitArchitectureQa {
   public static void main(String[] args) {
      checkProfileSelection();
      checkControls();
      checkLegacyRules();
      checkModeWireOrdinals();
      System.out.println("NANOSUIT_ARCHITECTURE_QA_PASS");
   }

   private static void checkProfileSelection() {
      var legacy = NanosuitGeneration.LEGACY_CURRENT;
      var first = NanosuitGeneration.NANOSUIT_1;
      var second = NanosuitGeneration.NANOSUIT_2;

      check(NanosuitGeneration.fullSet(legacy, legacy, legacy, legacy) == legacy, "complete legacy set");
      check(NanosuitGeneration.fullSet(first, first, first, first) == first, "complete Nanosuit 1 prototype");
      check(NanosuitGeneration.fullSet(second, second, second, second) == second, "complete Nanosuit 2 prototype");
      check(NanosuitGeneration.fullSet(legacy, legacy, legacy, null) == null, "missing boots");
      check(NanosuitGeneration.fullSet(null, legacy, legacy, legacy) == null, "missing helmet");
      check(NanosuitGeneration.fullSet(first, first, second, first) == null, "mixed generations");
      check(NanosuitGeneration.fullSet(null, null, null, null) == null, "empty equipment");
      check(NanosuitProfile.forGeneration(legacy) == NanosuitProfile.LEGACY_CURRENT, "active legacy profile");
      check(NanosuitProfile.LEGACY_CURRENT.generation() == legacy, "legacy profile identity");
      check(NanosuitProfile.forGeneration(first) == null, "inactive Nanosuit 1 profile");
      check(NanosuitProfile.forGeneration(second) == null, "inactive Nanosuit 2 profile");
      check(NanosuitProfile.forGeneration(null) == null, "no equipped profile");
   }

   private static void checkControls() {
      var legacyControls = NanosuitProfile.LEGACY_CURRENT.controls();
      check(legacyControls.manualModes().equals(Set.of(ManualMode.ARMOR, ManualMode.CLOAK)), "legacy manual modes");
      check(legacyControls.contextualAbilities().equals(EnumSet.allOf(Ability.class)), "legacy contextual abilities");
      for (NanosuitMode mode : NanosuitMode.values()) {
         check(legacyControls.allowsMode(mode), "legacy mode gate for " + mode);
      }
      check(legacyControls.allowsMode(null), "legacy internal null mode");

      var manualModes = EnumSet.allOf(ManualMode.class);
      var manualPrototype = new NanosuitControls(manualModes, Set.of());
      manualModes.clear();
      check(manualPrototype.manualModes().size() == 4 && !manualPrototype.supports(Ability.POWER_JUMP),
         "manual prototype holds an independent copy and denies contextual jump");

      var contextualPrototype = new NanosuitControls(Set.of(ManualMode.ARMOR, ManualMode.CLOAK), EnumSet.allOf(Ability.class));
      check(!contextualPrototype.manualModes().contains(ManualMode.STRENGTH)
         && contextualPrototype.supports(Ability.POWER_JUMP), "contextual jump does not require manual strength");
   }

   private static void checkLegacyRules() {
      var profile = NanosuitProfile.LEGACY_CURRENT;
      check(profile.modeCooldownTicks() == 20, "legacy mode cooldown");
      check(profile.mobility().jump().equals(new Jump(10.0F, 4, 40, 8, 0.685F))
         && profile.enhancedJumpEnergyCost() == 10.0F, "legacy jump cost, charge, cooldown and velocity");

      var combat = profile.combat();
      check(combat.melee().equals(new Melee(15.0F, 2.0F)), "legacy melee cost and multiplier");
      check(combat.cloak().shot(true).equals(new CloakCost(7.0F, false))
         && combat.cloak().shot(false).equals(new CloakCost(20.0F, true)), "legacy cloak shot costs and breaks");
      check(combat.predatorDamageMultiplier() == 1.5F, "legacy Predator Bow multiplier");
   }

   private static void checkModeWireOrdinals() {
      NanosuitMode[] expected = {NanosuitMode.NONE, NanosuitMode.ARMOR, NanosuitMode.CLOAK};
      NanosuitMode[] decodedByOrdinal = NanosuitMode.values();
      check(decodedByOrdinal.length == expected.length, "protocol v4 mode count");
      for (int wireOrdinal = 0; wireOrdinal < expected.length; wireOrdinal++) {
         NanosuitMode mode = expected[wireOrdinal];
         check(mode.ordinal() == wireOrdinal && decodedByOrdinal[mode.ordinal()] == mode,
            "protocol v4 mode ordinal for " + mode);
      }
   }

   private static void check(boolean condition, String label) {
      if (!condition) {
         throw new AssertionError(label);
      }
   }
}
