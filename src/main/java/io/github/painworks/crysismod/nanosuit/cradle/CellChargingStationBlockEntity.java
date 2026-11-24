package io.github.painworks.crysismod.nanosuit.cradle;

import io.github.painworks.crysismod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CellChargingStationBlockEntity extends BlockEntity {
   private static final int TICKS_PER_UNIT = 4;
   private static final String MESSAGE = "block.crysismod.cell_charging_station.";
   private ItemStack battery = ItemStack.EMPTY;
   private int chargingTicks;

   public CellChargingStationBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlocks.CELL_CHARGING_STATION_ENTITY.get(), pos, state);
   }

   public boolean hasBattery() { return !battery.isEmpty(); }

   public int charge() { return hasBattery() ? BatteryKind.CELL.charge(battery) : 0; }

   private boolean charging() {
      return hasBattery() && charge() < BatteryKind.CELL.capacity() && level.hasNeighborSignal(worldPosition);
   }

   static void tick(Level level, BlockPos pos, BlockState state, CellChargingStationBlockEntity station) {
      if (station.charging()) {
         if (++station.chargingTicks == TICKS_PER_UNIT) {
            station.chargingTicks = 0;
            BatteryKind.CELL.writeCharge(station.battery, station.charge() + 1);
         }
         station.setChanged();
      }
      station.updateIndicator();
   }

   private void updateIndicator() {
      var state = getBlockState();
      boolean active = charging();
      if (state.getValue(CellChargingStationBlock.LIT) != active) {
         level.setBlock(worldPosition, state.setValue(CellChargingStationBlock.LIT, active), Block.UPDATE_CLIENTS);
      }
   }

   private boolean canUse(ServerPlayer player) {
      return level instanceof ServerLevel server && server.getServer().isSameThread() && player.level() == level
         && player.isAlive() && !player.isSpectator() && !isRemoved() && server.mayInteract(player, worldPosition)
         && player.canInteractWithBlock(worldPosition, 0);
   }

   void exchange(ServerPlayer player) {
      if (!canUse(player)) return;
      ItemStack incoming = player.getMainHandItem();
      if (BatteryKind.of(incoming) != BatteryKind.CELL || incoming.getCount() != 1) {
         message(player, "cell_only");
         return;
      }
      ItemStack outgoing = battery;
      battery = incoming;
      player.setItemInHand(InteractionHand.MAIN_HAND, outgoing);
      changed(player);
      inspect(player);
   }

   void retrieve(ServerPlayer player) {
      if (!canUse(player) || !player.getMainHandItem().isEmpty()) return;
      if (battery.isEmpty()) {
         message(player, "empty");
         return;
      }
      ItemStack outgoing = battery;
      battery = ItemStack.EMPTY;
      player.setItemInHand(InteractionHand.MAIN_HAND, outgoing);
      changed(player);
      message(player, "retrieved");
   }

   void inspect(ServerPlayer player) {
      if (!canUse(player)) return;
      if (!hasBattery()) {
         message(player, "empty");
         return;
      }
      int capacity = BatteryKind.CELL.capacity();
      int charge = charge();
      String status = charge >= capacity ? "full" : charging() ? "charging" : "no_signal";
      message(player, "status", charge, capacity, Component.translatable(MESSAGE + status));
   }

   private static void message(ServerPlayer player, String key, Object... args) {
      player.displayClientMessage(Component.translatable(MESSAGE + key, args), true);
   }

   private void changed(ServerPlayer player) {
      chargingTicks = 0;
      player.getInventory().setChanged();
      setChanged();
      updateIndicator();
   }

   void dropBattery(ServerLevel server) {
      ItemStack outgoing = battery;
      battery = ItemStack.EMPTY;
      if (!outgoing.isEmpty()) Containers.dropItemStack(server, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), outgoing);
   }

   @Override
   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.saveAdditional(tag, registries);
      if (!battery.isEmpty()) {
         tag.put("Battery", battery.save(registries));
         tag.putInt("ChargingTicks", chargingTicks);
      }
   }

   @Override
   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.loadAdditional(tag, registries);
      battery = ItemStack.parseOptional(registries, tag.getCompound("Battery"));
      chargingTicks = Mth.clamp(tag.getInt("ChargingTicks"), 0, TICKS_PER_UNIT - 1);
   }
}
