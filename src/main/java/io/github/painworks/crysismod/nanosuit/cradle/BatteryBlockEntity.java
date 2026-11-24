package io.github.painworks.crysismod.nanosuit.cradle;

import io.github.painworks.crysismod.registry.ModBlocks;
import io.github.painworks.crysismod.registry.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BatteryBlockEntity extends BlockEntity {
   private static final String CHARGE_TAG = "Charge";
   private final BatteryKind kind;
   private int charge;

   public BatteryBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlocks.BATTERY_ENTITY.get(), pos, state);
      kind = ((BatteryBlock) state.getBlock()).kind();
      charge = kind.capacity();
   }

   public BatteryKind kind() { return kind; }
   public int charge() { return charge; }
   public boolean canDebit(int amount) { return amount >= 0 && amount <= charge; }

   public boolean tryDebit(int amount) {
      if (!canMutate() || !canDebit(amount)) return false;
      updateCharge(charge - amount);
      return true;
   }

   /** Returns the amount accepted by the battery. */
   public int addEnergy(int amount) {
      if (!canMutate() || amount <= 0) return 0;
      int accepted = Math.min(amount, kind.capacity() - charge);
      updateCharge(charge + accepted);
      return accepted;
   }

   private boolean canMutate() {
      return level instanceof ServerLevel server && server.getServer().isSameThread() && !isRemoved();
   }

   private void updateCharge(int next) {
      if (next == charge) return;
      charge = next;
      setChanged();
      var state = getBlockState();
      level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
   }

   @Override
   protected void applyImplicitComponents(DataComponentInput input) {
      super.applyImplicitComponents(input);
      charge = kind.clamp(input.getOrDefault(ModDataComponents.BATTERY_CHARGE.get(), charge));
   }

   @Override
   protected void collectImplicitComponents(DataComponentMap.Builder builder) {
      super.collectImplicitComponents(builder);
      builder.set(ModDataComponents.BATTERY_CHARGE.get(), charge);
   }

   @Override
   public void removeComponentsFromTag(CompoundTag tag) {
      super.removeComponentsFromTag(tag);
      tag.remove(CHARGE_TAG);
   }

   @Override
   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
      var tag = new CompoundTag();
      tag.putInt(CHARGE_TAG, charge);
      return tag;
   }

   @Override
   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.saveAdditional(tag, registries);
      tag.putInt(CHARGE_TAG, charge);
   }

   @Override
   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.loadAdditional(tag, registries);
      charge = tag.contains(CHARGE_TAG, Tag.TAG_ANY_NUMERIC) ? kind.clamp(tag.getInt(CHARGE_TAG)) : kind.capacity();
   }
}
