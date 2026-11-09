package io.github.painworks.crysismod.nanosuit.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public final class NanosuitArmorIdentityConfig {
   private static final Builder BUILDER = new Builder();
   private static final boolean DEFAULT_NANOSUIT_USE_NANOGEO_ARMOR_AS_PRIMARY = true;
   private static final boolean DEFAULT_NANOSUIT_REQUIRE_SAME_ARMOR_VARIANT_FOR_FULL_SET = true;
   private static final boolean DEFAULT_DEBUG_NANOSUIT_ARMOR_IDENTITY = false;
   private static final BooleanValue NANOSUIT_USE_NANOGEO_ARMOR_AS_PRIMARY = BUILDER.comment("Treat NanoGeo nanosuit armor as primary gameplay variant.")
      .define("nanosuitUseNanoGeoArmorAsPrimary", true);
   private static final BooleanValue NANOSUIT_REQUIRE_SAME_ARMOR_VARIANT_FOR_FULL_SET = BUILDER.comment(
         "Require all four equipped nanosuit pieces to belong to the same armor variant."
      )
      .define("nanosuitRequireSameArmorVariantForFullSet", true);
   private static final BooleanValue DEBUG_NANOSUIT_ARMOR_IDENTITY = BUILDER.comment(
         "Log nanosuit armor identity resolution when equipped pieces or full-set state changes."
      )
      .define("debugNanosuitArmorIdentity", false);
   public static final ModConfigSpec SPEC = BUILDER.build();

   private NanosuitArmorIdentityConfig() {
   }

   public static boolean nanosuitUseNanoGeoArmorAsPrimary() {
      return safeGetBoolean(NANOSUIT_USE_NANOGEO_ARMOR_AS_PRIMARY, true);
   }

   public static boolean defaultNanosuitUseNanoGeoArmorAsPrimary() {
      return true;
   }

   public static boolean nanosuitRequireSameArmorVariantForFullSet() {
      return safeGetBoolean(NANOSUIT_REQUIRE_SAME_ARMOR_VARIANT_FOR_FULL_SET, true);
   }

   public static boolean defaultNanosuitRequireSameArmorVariantForFullSet() {
      return true;
   }

   public static boolean debugNanosuitArmorIdentity() {
      return safeGetBoolean(DEBUG_NANOSUIT_ARMOR_IDENTITY, false);
   }

   public static boolean defaultDebugNanosuitArmorIdentity() {
      return false;
   }

   public static void setNanosuitUseNanoGeoArmorAsPrimary(boolean value) {
      safeSetBoolean(NANOSUIT_USE_NANOGEO_ARMOR_AS_PRIMARY, value);
   }

   public static void setNanosuitRequireSameArmorVariantForFullSet(boolean value) {
      safeSetBoolean(NANOSUIT_REQUIRE_SAME_ARMOR_VARIANT_FOR_FULL_SET, value);
   }

   public static void setDebugNanosuitArmorIdentity(boolean value) {
      safeSetBoolean(DEBUG_NANOSUIT_ARMOR_IDENTITY, value);
   }

   private static boolean safeGetBoolean(BooleanValue value, boolean fallback) {
      try {
         return (Boolean)value.get();
      } catch (IllegalStateException var3) {
         return fallback;
      }
   }

   private static void safeSetBoolean(BooleanValue value, boolean target) {
      try {
         value.set(target);
         value.save();
      } catch (RuntimeException var3) {
      }
   }
}
