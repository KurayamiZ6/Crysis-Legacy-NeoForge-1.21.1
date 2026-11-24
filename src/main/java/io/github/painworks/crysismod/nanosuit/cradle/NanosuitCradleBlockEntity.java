package io.github.painworks.crysismod.nanosuit.cradle;

import io.github.painworks.crysismod.nanosuit.NanosuitGeneration;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class NanosuitCradleBlockEntity extends BlockEntity {
   private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
   private final NonNullList<ItemStack> pieces = NonNullList.withSize(SLOTS.length, ItemStack.EMPTY);
   private MachineState machineState = MachineState.OFF;

   public enum MachineState {
      OFF("off"), STANDBY("standby"), ABORTED("aborted");

      final String key;
      MachineState(String key) { this.key = key; }
      public String key() { return key; }
   }

   public NanosuitCradleBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlocks.NANOSUIT_CRADLE_ENTITY.get(), pos, state);
   }

   private boolean canUse(ServerPlayer player, BlockPos clicked) {
      return level instanceof ServerLevel server && server.getServer().isSameThread() && player.level() == level
         && player.isAlive() && !player.isSpectator() && server.getBlockEntity(worldPosition) == this
         && server.mayInteract(player, worldPosition) && server.mayInteract(player, clicked) && player.canInteractWithBlock(clicked, 0);
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

   int occupiedSlots() {
      int mask = 0;
      for (int i = 0; i < SLOTS.length; i++) {
         if (!pieces.get(i).isEmpty()) mask |= 1 << i;
      }
      return mask;
   }

   int storedGenerations() {
      int mask = 0;
      for (int i = 0; i < SLOTS.length; i++) {
         var generation = generation(i);
         if (generation == NanosuitGeneration.NANOSUIT_1) mask |= 1;
         if (generation == NanosuitGeneration.NANOSUIT_2) mask |= 2;
      }
      return mask;
   }

   MachineState machineState() { return machineState; }

   boolean setMachineState(MachineState next) {
      if (machineState == next) return false;
      machineState = next;
      setChanged();
      return true;
   }

   String status(ServerPlayer player, BlockPos clicked) {
      if (!canUse(player, clicked)) return "denied";
      NanosuitProfile complete = storedProfile();
      if (complete != null) return complete == NanosuitProfile.NANOSUIT_1 ? "complete_n1" : "complete_n2";
      NanosuitGeneration first = null;
      for (int i = 0; i < SLOTS.length; i++) {
         NanosuitGeneration piece = generation(i);
         if (piece == null) continue;
         if (first != null && first != piece) return "mixed";
         first = piece;
      }
      return isEmpty() ? "empty" : "incomplete";
   }

   String insert(ServerPlayer player, BlockPos clicked) {
      if (!canUse(player, clicked)) return "denied";
      ItemStack held = player.getMainHandItem();
      for (int i = 0; i < SLOTS.length; i++) {
         NanosuitProfile profile = NanosuitArmorUtil.getPieceProfile(held, SLOTS[i]);
         if (profile == null) continue;
         if (!pieces.get(i).isEmpty()) return "slot_occupied";
         for (int j = 0; j < SLOTS.length; j++) {
            if (!pieces.get(j).isEmpty() && generation(j) != profile.generation()) return "mixed";
         }
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

   String retrievePiece(ServerPlayer player, BlockPos clicked) {
      if (!canUse(player, clicked) || !player.getMainHandItem().isEmpty()) return "denied";
      for (int i = SLOTS.length - 1; i >= 0; i--) {
         if (pieces.get(i).isEmpty()) continue;
         player.setItemInHand(InteractionHand.MAIN_HAND, pieces.set(i, ItemStack.EMPTY));
         changed(player);
         return "retrieved";
      }
      return "empty";
   }

   private void changed(ServerPlayer player) {
      player.getInventory().setChanged();
      setChanged();
      var state = getBlockState();
      player.serverLevel().sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
   }

   /** The client display renders the synchronized stack without modifying it. */
   public ItemStack displayedPiece(int index) {
      return pieces.get(index);
   }

   @Override
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
      return ContainerHelper.saveAllItems(new CompoundTag(), pieces, registries);
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
      tag.putString("MachineState", machineState.key());
   }

   @Override
   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.loadAdditional(tag, registries);
      pieces.clear();
      ContainerHelper.loadAllItems(tag, pieces, registries);
      machineState = switch (tag.getString("MachineState")) {
         case "standby" -> MachineState.STANDBY;
         case "aborted" -> MachineState.ABORTED;
         default -> MachineState.OFF;
      };
   }
}
