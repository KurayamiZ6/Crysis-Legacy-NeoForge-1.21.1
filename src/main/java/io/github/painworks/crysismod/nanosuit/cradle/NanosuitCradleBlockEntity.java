package io.github.painworks.crysismod.nanosuit.cradle;

import io.github.painworks.crysismod.nanosuit.NanosuitGeneration;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class NanosuitCradleBlockEntity extends BlockEntity {
   private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
   private final NonNullList<ItemStack> pieces = NonNullList.withSize(SLOTS.length, ItemStack.EMPTY);

   public NanosuitCradleBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlocks.NANOSUIT_CRADLE_ENTITY.get(), pos, state);
   }

   private boolean canUse(ServerPlayer player) {
      return level instanceof ServerLevel server && server.getServer().isSameThread() && player.level() == level
         && player.isAlive() && !player.isSpectator() && server.getBlockEntity(worldPosition) == this
         && server.mayInteract(player, worldPosition) && player.canInteractWithBlock(worldPosition, 0);
   }

   private boolean isEmpty() {
      return pieces.stream().allMatch(ItemStack::isEmpty);
   }

   private NanosuitGeneration generation(int index) {
      ItemStack piece = pieces.get(index);
      NanosuitProfile profile = piece.getCount() == 1 ? NanosuitArmorUtil.getPieceProfile(piece, SLOTS[index]) : null;
      return profile == null ? null : profile.generation();
   }

   public NanosuitProfile storedProfile() {
      return NanosuitProfile.forGeneration(NanosuitGeneration.fullSet(generation(0), generation(1), generation(2), generation(3)));
   }

   String unavailableReason() {
      NanosuitGeneration first = null;
      for (int i = 0; i < SLOTS.length; i++) {
         NanosuitGeneration piece = generation(i);
         if (piece == null) continue;
         if (first != null && first != piece) return "mixed";
         first = piece;
      }
      return isEmpty() ? "empty" : "incomplete";
   }

   String insert(ServerPlayer player) {
      if (!canUse(player)) return "denied";
      ItemStack held = player.getMainHandItem();
      for (int i = 0; i < SLOTS.length; i++) {
         if (NanosuitArmorUtil.getPieceProfile(held, SLOTS[i]) == null) continue;
         if (!pieces.get(i).isEmpty()) return "slot_occupied";
         if (held.getCount() == 1) {
            pieces.set(i, held);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
         } else {
            pieces.set(i, held.split(1));
         }
         changed(player);
         return "inserted";
      }
      return "invalid_item";
   }

   String retrievePiece(ServerPlayer player) {
      if (!canUse(player) || !player.getMainHandItem().isEmpty()) return "denied";
      for (int i = SLOTS.length - 1; i >= 0; i--) {
         if (pieces.get(i).isEmpty()) continue;
         player.setItemInHand(InteractionHand.MAIN_HAND, pieces.set(i, ItemStack.EMPTY));
         changed(player);
         return "retrieved";
      }
      return "empty";
   }

   String equip(ServerPlayer player) {
      if (!canUse(player) || !player.getMainHandItem().isEmpty()) return "denied";
      if (storedProfile() == null) return unavailableReason();
      for (EquipmentSlot slot : SLOTS) {
         if (!player.getItemBySlot(slot).isEmpty()) return "armor_occupied";
      }
      transfer(player, true);
      return "equipped";
   }

   String storeSuit(ServerPlayer player) {
      if (!canUse(player) || !player.getMainHandItem().isEmpty()) return "denied";
      if (!isEmpty()) return "cradle_occupied";
      if (NanosuitArmorUtil.getUsableProfile(player) == null) return "no_full_suit";
      for (EquipmentSlot slot : SLOTS) {
         if (player.getItemBySlot(slot).getCount() != 1) return "no_full_suit";
      }
      transfer(player, false);
      return "stored";
   }

   private void transfer(ServerPlayer player, boolean toPlayer) {
      // Commit to the vanilla armor inventory on the server thread after validation of all four slots.
      // The normal equipment tick observes the completed change and handles pose, profile and effect synchronization.
      var armor = player.getInventory().armor;
      for (int i = 0; i < SLOTS.length; i++) {
         int slotIndex = SLOTS[i].getIndex();
         if (toPlayer) armor.set(slotIndex, pieces.set(i, ItemStack.EMPTY));
         else pieces.set(i, armor.set(slotIndex, ItemStack.EMPTY));
      }
      changed(player);
   }

   private void changed(ServerPlayer player) {
      player.getInventory().setChanged();
      setChanged();
   }

   void dropContents(ServerLevel server) {
      var drops = NonNullList.withSize(SLOTS.length, ItemStack.EMPTY);
      for (int i = 0; i < SLOTS.length; i++) drops.set(i, pieces.set(i, ItemStack.EMPTY));
      Containers.dropContents(server, worldPosition, drops);
   }

   @Override
   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.saveAdditional(tag, registries);
      ContainerHelper.saveAllItems(tag, pieces, registries);
   }

   @Override
   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.loadAdditional(tag, registries);
      pieces.clear();
      ContainerHelper.loadAllItems(tag, pieces, registries);
   }
}
