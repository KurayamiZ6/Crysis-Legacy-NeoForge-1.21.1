package io.github.painworks.crysismod.nanosuit.util;

import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.nanosuit.config.NanosuitArmorIdentityConfig;
import io.github.painworks.crysismod.registry.ModItems;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public final class NanosuitArmorUtil {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<UUID, String> DEBUG_SIGNATURE_BY_PLAYER = new ConcurrentHashMap<>();

   private NanosuitArmorUtil() {
   }

   public static boolean hasFullSet(Player player) {
      NanosuitArmorUtil.Evaluation evaluation = evaluate(player);
      maybeLogIdentity(player, evaluation);
      return evaluation.hasFullSet;
   }

   public static boolean hasFullSet(Player player, boolean ignoredAllowLegacy) {
      return hasFullSet(player);
   }

   public static boolean hasFullExperimentalSet(Player player) {
      return hasFullSet(player);
   }

   public static boolean hasFullProductionSet(Player player) {
      return hasFullSet(player);
   }

   public static boolean isNanosuitArmorPiece(ItemStack stack) {
      return isNanoGeoArmorPiece(stack);
   }

   public static boolean isNanosuitArmorPiece(ItemStack stack, EquipmentSlot slot) {
      if (slot == null) {
         return false;
      } else {
         return switch (slot) {
            case HEAD -> isNanosuitHelmet(stack);
            case CHEST -> isNanosuitChestplate(stack);
            case LEGS -> isNanosuitLeggings(stack);
            case FEET -> isNanosuitBoots(stack);
            default -> false;
         };
      }
   }

   public static boolean isNanosuitHelmet(ItemStack stack) {
      return isNanoGeoHelmet(stack);
   }

   public static boolean isNanosuitChestplate(ItemStack stack) {
      return isNanoGeoChestplate(stack);
   }

   public static boolean isNanosuitLeggings(ItemStack stack) {
      return isNanoGeoLeggings(stack);
   }

   public static boolean isNanosuitBoots(ItemStack stack) {
      return isNanoGeoBoots(stack);
   }

   public static NanosuitArmorVariant getVariant(ItemStack stack) {
      return isNanoGeoArmorPiece(stack) ? NanosuitArmorVariant.NANOGEO : null;
   }

   public static NanosuitArmorVariant getEquippedVariant(Player player) {
      return hasFullSet(player) ? NanosuitArmorVariant.NANOGEO : null;
   }

   public static boolean isFullSetSameVariant(Player player) {
      return hasFullSet(player);
   }

   public static boolean isNanoGeoArmorPiece(ItemStack stack) {
      return isNanoGeoHelmet(stack) || isNanoGeoChestplate(stack) || isNanoGeoLeggings(stack) || isNanoGeoBoots(stack);
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
      return stack.is((Item)ModItems.NANOGEO_NANOSUIT_HELMET.get());
   }

   public static boolean isNanoGeoChestplate(ItemStack stack) {
      return stack.is((Item)ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get());
   }

   public static boolean isNanoGeoLeggings(ItemStack stack) {
      return stack.is((Item)ModItems.NANOGEO_NANOSUIT_LEGGINGS.get());
   }

   public static boolean isNanoGeoBoots(ItemStack stack) {
      return stack.is((Item)ModItems.NANOGEO_NANOSUIT_BOOTS.get());
   }

   private static NanosuitArmorUtil.Evaluation evaluate(Player player) {
      boolean head = isNanoGeoHelmet(player.getItemBySlot(EquipmentSlot.HEAD));
      boolean chest = isNanoGeoChestplate(player.getItemBySlot(EquipmentSlot.CHEST));
      boolean legs = isNanoGeoLeggings(player.getItemBySlot(EquipmentSlot.LEGS));
      boolean feet = isNanoGeoBoots(player.getItemBySlot(EquipmentSlot.FEET));
      return new NanosuitArmorUtil.Evaluation(head, chest, legs, feet, head && chest && legs && feet);
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

   private record Evaluation(boolean head, boolean chest, boolean legs, boolean feet, boolean hasFullSet) {
      private String signature() {
         return "variant=NANOGEO head=" + this.head + " chest=" + this.chest + " legs=" + this.legs + " boots=" + this.feet + " fullSet=" + this.hasFullSet;
      }
   }
}
