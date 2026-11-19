package io.github.painworks.crysismod.nanosuit.util;

import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.nanosuit.NanosuitGeneration;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.config.NanosuitArmorIdentityConfig;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public final class NanosuitArmorUtil {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<UUID, String> DEBUG_SIGNATURE_BY_PLAYER = new ConcurrentHashMap<>();

   private NanosuitArmorUtil() {
   }

   public static boolean hasFullSet(Player player) {
      return getEquippedProfile(player) != null;
   }

   /** Active abilities require a living player and a complete, homogeneous suit. */
   public static NanosuitProfile getUsableProfile(Player player) {
      return player == null || !player.isAlive() || player.isSpectator() ? null : getEquippedProfile(player);
   }

   public static NanosuitProfile getEquippedProfile(Player player) {
      NanosuitArmorUtil.Evaluation evaluation = evaluate(player);
      maybeLogIdentity(player, evaluation);
      return evaluation.profile();
   }

   public static NanosuitProfile getPieceProfile(ItemStack stack, EquipmentSlot slot) {
      return NanosuitProfile.forGeneration(getPieceGeneration(stack, slot));
   }

   public static NanosuitProfile getNanovisionProfile(Player player, boolean requiresFullSet, boolean requiresHelmet) {
      // Retain the old config signature, but neither legacy switch may bypass suit identity.
      return getUsableProfile(player);
   }

   public static boolean hasFullSet(Player player, boolean ignoredAllowLegacy) {
      return hasFullSet(player);
   }

   public static boolean hasFullExperimentalSet(Player player) {
      return getEquippedVariant(player) == NanosuitArmorVariant.NANOGEO;
   }

   public static boolean hasFullProductionSet(Player player) {
      return getEquippedVariant(player) == NanosuitArmorVariant.NANOGEO;
   }

   public static boolean isNanosuitArmorPiece(ItemStack stack) {
      return getVariant(stack) != null;
   }

   public static boolean isNanosuitArmorPiece(ItemStack stack, EquipmentSlot slot) {
      return getPieceGeneration(stack, slot) != null;
   }

   private static NanosuitGeneration getPieceGeneration(ItemStack stack, EquipmentSlot slot) {
      NanosuitArmorVariant variant = getPieceVariant(stack, slot);
      return variant == null ? null : variant.generation();
   }

   private static NanosuitArmorVariant getPieceVariant(ItemStack stack, EquipmentSlot slot) {
      if (slot == null || stack.isEmpty()) {
         return null;
      }
      if (NanosuitArmorVariant.NANOGEO.matches(stack, slot)) {
         return NanosuitArmorVariant.NANOGEO;
      }
      return NanosuitArmorVariant.NANOSUIT_2.matches(stack, slot) ? NanosuitArmorVariant.NANOSUIT_2 : null;
   }

   public static boolean isNanosuitHelmet(ItemStack stack) {
      return isNanosuitArmorPiece(stack, EquipmentSlot.HEAD);
   }

   public static boolean isNanosuitChestplate(ItemStack stack) {
      return isNanosuitArmorPiece(stack, EquipmentSlot.CHEST);
   }

   public static boolean isNanosuitLeggings(ItemStack stack) {
      return isNanosuitArmorPiece(stack, EquipmentSlot.LEGS);
   }

   public static boolean isNanosuitBoots(ItemStack stack) {
      return isNanosuitArmorPiece(stack, EquipmentSlot.FEET);
   }

   public static NanosuitArmorVariant getVariant(ItemStack stack) {
      return stack.getItem() instanceof ArmorItem armor ? getPieceVariant(stack, armor.getEquipmentSlot()) : null;
   }

   public static NanosuitArmorVariant getEquippedVariant(Player player) {
      NanosuitProfile profile = getEquippedProfile(player);
      return profile == null ? null : NanosuitArmorVariant.forGeneration(profile.generation());
   }

   public static boolean isFullSetSameVariant(Player player) {
      return hasFullSet(player);
   }

   public static boolean isNanoGeoArmorPiece(ItemStack stack) {
      return getVariant(stack) == NanosuitArmorVariant.NANOGEO;
   }

   public static boolean isExperimentalHelmet(ItemStack stack) {
      return isNanoGeoHelmet(stack);
   }

   public static boolean isExperimentalChestplate(ItemStack stack) {
      return isNanoGeoChestplate(stack);
   }

   public static boolean isExperimentalLeggings(ItemStack stack) {
      return isNanoGeoLeggings(stack);
   }

   public static boolean isExperimentalBoots(ItemStack stack) {
      return isNanoGeoBoots(stack);
   }

   public static boolean isProductionHelmet(ItemStack stack) {
      return isNanoGeoHelmet(stack);
   }

   public static boolean isProductionChestplate(ItemStack stack) {
      return isNanoGeoChestplate(stack);
   }

   public static boolean isProductionLeggings(ItemStack stack) {
      return isNanoGeoLeggings(stack);
   }

   public static boolean isProductionBoots(ItemStack stack) {
      return isNanoGeoBoots(stack);
   }

   public static boolean isNanoGeoHelmet(ItemStack stack) {
      return NanosuitArmorVariant.NANOGEO.matches(stack, EquipmentSlot.HEAD);
   }

   public static boolean isNanoGeoChestplate(ItemStack stack) {
      return NanosuitArmorVariant.NANOGEO.matches(stack, EquipmentSlot.CHEST);
   }

   public static boolean isNanoGeoLeggings(ItemStack stack) {
      return NanosuitArmorVariant.NANOGEO.matches(stack, EquipmentSlot.LEGS);
   }

   public static boolean isNanoGeoBoots(ItemStack stack) {
      return NanosuitArmorVariant.NANOGEO.matches(stack, EquipmentSlot.FEET);
   }

   private static NanosuitArmorUtil.Evaluation evaluate(Player player) {
      NanosuitGeneration head = getPieceGeneration(player.getItemBySlot(EquipmentSlot.HEAD), EquipmentSlot.HEAD);
      NanosuitGeneration chest = getPieceGeneration(player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST);
      NanosuitGeneration legs = getPieceGeneration(player.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS);
      NanosuitGeneration feet = getPieceGeneration(player.getItemBySlot(EquipmentSlot.FEET), EquipmentSlot.FEET);
      return new NanosuitArmorUtil.Evaluation(head, chest, legs, feet);
   }

   private static void maybeLogIdentity(Player player, NanosuitArmorUtil.Evaluation evaluation) {
      if (!NanosuitArmorIdentityConfig.debugNanosuitArmorIdentity()) {
         DEBUG_SIGNATURE_BY_PLAYER.remove(player.getUUID());
      } else {
         String signature = evaluation.signature();
         UUID playerId = player.getUUID();
         String previous = DEBUG_SIGNATURE_BY_PLAYER.put(playerId, signature);
         if (!signature.equals(previous)) {
            LOGGER.info("[CrysisMod][NanosuitIdentity] player={} {}", player.getScoreboardName(), signature);
         }
      }
   }

   private record Evaluation(NanosuitGeneration head, NanosuitGeneration chest, NanosuitGeneration legs, NanosuitGeneration feet) {
      private NanosuitProfile profile() {
         return NanosuitProfile.forGeneration(NanosuitGeneration.fullSet(head, chest, legs, feet));
      }

      private String signature() {
         return "profile=" + profile() + " head=" + head + " chest=" + chest + " legs=" + legs + " boots=" + feet;
      }
   }
}
