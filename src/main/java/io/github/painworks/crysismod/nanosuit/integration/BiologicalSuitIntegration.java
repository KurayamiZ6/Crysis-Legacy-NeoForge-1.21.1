package io.github.painworks.crysismod.nanosuit.integration;

import io.github.painworks.crysismod.nanosuit.NanosuitGeneration;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.registry.ModDataComponents;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class BiologicalSuitIntegration {
   private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

   public enum Result {
      SUCCESS, INVALID_PLAYER, ALREADY_LINKED, NOT_LINKED, INVALID_SUIT, UNDERSUIT_REQUIRED, ARMOR_OCCUPIED, OUTPUT_OCCUPIED
   }

   private BiologicalSuitIntegration() {}

   public static UUID linkedIdentity(Player player) {
      var link = player.getExistingDataOrNull(NanosuitCapabilities.BIOLOGICAL_SUIT);
      return link == null ? null : link.identity;
   }

   public static boolean isArmorLocked(Player player, EquipmentSlot slot) {
      return slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !player.isCreative()
         && !player.isSpectator() && linkedIdentity(player) != null;
   }

   /**
    * Moves four exclusively owned stacks ordered head, chest, legs, feet out of a mutable server-side storage view.
    * The chest is the anchor for future suit data; the four identity markers keep the set together.
    */
   public static Result integrate(ServerPlayer player, List<ItemStack> input) {
      if (!canOperate(player)) return Result.INVALID_PLAYER;
      if (linkedIdentity(player) != null) return Result.ALREADY_LINKED;
      if (input.size() != SLOTS.length) return Result.INVALID_SUIT;
      var profile = NanosuitArmorUtil.getPieceProfile(input.getFirst(), EquipmentSlot.HEAD);
      if (profile != NanosuitProfile.NANOSUIT_1 && profile != NanosuitProfile.NANOSUIT_2) return Result.INVALID_SUIT;
      UUID identity = pieceIdentity(input.get(1));
      for (int i = 0; i < SLOTS.length; i++) {
         var piece = input.get(i);
         if (piece.getCount() != 1 || NanosuitArmorUtil.getPieceProfile(piece, SLOTS[i]) != profile
            || !Objects.equals(identity, pieceIdentity(piece)) || piece == player.getItemBySlot(SLOTS[i])) {
            return Result.INVALID_SUIT;
         }
      }
      if (profile == NanosuitProfile.NANOSUIT_1 && !Undersuit.isComplete(player)) return Result.UNDERSUIT_REQUIRED;
      for (EquipmentSlot slot : SLOTS) {
         var equipped = player.getItemBySlot(slot);
         if (!equipped.isEmpty() && !Undersuit.isPiece(equipped, slot)) return Result.ARMOR_OCCUPIED;
      }
      var link = new BiologicalSuitAttachment(identity == null ? UUID.randomUUID() : identity);
      for (int i = 0; i < SLOTS.length; i++) {
         var equipped = player.getItemBySlot(SLOTS[i]);
         if (profile == NanosuitProfile.NANOSUIT_1) link.undersuit.set(i, equipped);
         var piece = input.set(i, ItemStack.EMPTY);
         piece.set(ModDataComponents.NANOSUIT_IDENTITY.get(), link.identity);
         setArmor(player, SLOTS[i], piece);
      }
      player.setData(NanosuitCapabilities.BIOLOGICAL_SUIT, link);
      player.getInventory().setChanged();
      return Result.SUCCESS;
   }

   /** Moves the actual linked set into four empty, mutable storage slots. */
   public static Result extract(ServerPlayer player, List<ItemStack> output) {
      if (!canOperate(player)) return Result.INVALID_PLAYER;
      var link = player.getExistingDataOrNull(NanosuitCapabilities.BIOLOGICAL_SUIT);
      if (link == null || link.identity == null) return Result.NOT_LINKED;
      if (!matches(player, link.identity)) return Result.INVALID_SUIT;
      if (output.size() != SLOTS.length || output.stream().anyMatch(stack -> !stack.isEmpty())) return Result.OUTPUT_OCCUPIED;
      for (int i = 0; i < SLOTS.length; i++) {
         output.set(i, player.getItemBySlot(SLOTS[i]));
         setArmor(player, SLOTS[i], link.undersuit.set(i, ItemStack.EMPTY));
      }
      player.removeData(NanosuitCapabilities.BIOLOGICAL_SUIT);
      player.getInventory().setChanged();
      return Result.SUCCESS;
   }

   private static boolean canOperate(ServerPlayer player) {
      return player.getServer() != null && player.getServer().isSameThread() && player.isAlive() && !player.isSpectator();
   }

   private static UUID pieceIdentity(ItemStack stack) {
      return stack.get(ModDataComponents.NANOSUIT_IDENTITY.get());
   }

   private static boolean matches(Player player, UUID id) {
      var profile = NanosuitArmorUtil.getPieceProfile(player.getItemBySlot(EquipmentSlot.HEAD), EquipmentSlot.HEAD);
      if (profile == null || profile.generation() == NanosuitGeneration.LEGACY_CURRENT) return false;
      for (EquipmentSlot slot : SLOTS) {
         var stack = player.getItemBySlot(slot);
         if (stack.getCount() != 1 || !id.equals(pieceIdentity(stack)) || NanosuitArmorUtil.getPieceProfile(stack, slot) != profile) return false;
      }
      return true;
   }

   private static void setArmor(Player player, EquipmentSlot slot, ItemStack stack) {
      player.getInventory().armor.set(slot.getIndex(), stack);
   }

   // Called before vanilla processes vanishing or drops, including when keepInventory is enabled.
   public static void preserveAtDeath(ServerPlayer player) {
      var link = player.getExistingDataOrNull(NanosuitCapabilities.BIOLOGICAL_SUIT);
      if (link == null || link.identity == null) return;
      for (int i = 0; i < SLOTS.length; i++) {
         var piece = player.getItemBySlot(SLOTS[i]);
         if (link.identity.equals(pieceIdentity(piece)) && link.deathEquipment.get(i).isEmpty()) {
            link.deathEquipment.set(i, piece);
            setArmor(player, SLOTS[i], ItemStack.EMPTY);
         }
      }
   }

   static void transferOnClone(Player original, ServerPlayer player) {
      var link = original.removeData(NanosuitCapabilities.BIOLOGICAL_SUIT);
      if (link == null || link.identity == null) return;
      restoreDeathEquipment(player, link);
      player.setData(NanosuitCapabilities.BIOLOGICAL_SUIT, link);
   }

   static void reconcile(ServerPlayer player) {
      if (!player.isAlive()) return;
      var link = player.getExistingDataOrNull(NanosuitCapabilities.BIOLOGICAL_SUIT);
      if (link == null || link.identity == null) return;
      restoreDeathEquipment(player, link);
      if (!player.isCreative() || matches(player, link.identity)) return;

      // Creative edits can dissolve the link without replacing any of the edited equipment.
      player.removeData(NanosuitCapabilities.BIOLOGICAL_SUIT);
      for (int i = 0; i < SLOTS.length; i++) returnToInventory(player, link.undersuit.set(i, ItemStack.EMPTY));
      player.getInventory().setChanged();
   }

   private static void restoreDeathEquipment(ServerPlayer player, BiologicalSuitAttachment link) {
      for (int i = 0; i < SLOTS.length; i++) {
         var piece = link.deathEquipment.set(i, ItemStack.EMPTY);
         if (piece.isEmpty()) continue;
         var current = player.getItemBySlot(SLOTS[i]);
         setArmor(player, SLOTS[i], piece);
         if (current != piece) returnToInventory(player, current);
      }
   }

   private static void returnToInventory(ServerPlayer player, ItemStack stack) {
      if (!stack.isEmpty()) player.getInventory().placeItemBackInInventory(stack, false);
   }
}
