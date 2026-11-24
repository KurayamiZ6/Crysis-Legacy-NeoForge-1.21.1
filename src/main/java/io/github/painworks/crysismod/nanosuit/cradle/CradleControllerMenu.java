package io.github.painworks.crysismod.nanosuit.cradle;

import io.github.painworks.crysismod.nanosuit.cradle.CradleControllerBlockEntity.Connection;
import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlockEntity.MachineState;
import io.github.painworks.crysismod.registry.ModBlocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;

public final class CradleControllerMenu extends AbstractContainerMenu {
   public static final int POWER_ON = 0;
   public static final int POWER_OFF = 1;
   public static final int CANCEL = 2;
   public static final int RECONNECT = 3;
   private static final Connection[] CONNECTIONS = Connection.values();
   private static final MachineState[] MACHINE_STATES = MachineState.values();
   private final CradleControllerBlockEntity controller;
   private final DataSlot status;

   public CradleControllerMenu(int id, Inventory inventory) {
      this(id, inventory, null);
   }

   CradleControllerMenu(int id, Inventory inventory, CradleControllerBlockEntity controller) {
      super(ModBlocks.CRADLE_CONTROLLER_MENU.get(), id);
      this.controller = controller;
      status = addDataSlot(DataSlot.standalone());
   }

   @Override
   public void broadcastChanges() {
      refresh();
      super.broadcastChanges();
   }

   @Override
   public void sendAllDataToRemote() {
      refresh();
      super.sendAllDataToRemote();
   }

   private void refresh() {
      if (controller == null) return;
      var link = controller.link();
      var cradle = link.cradle();
      // One short-sized property keeps all fields from the same server snapshot.
      int flags = link.state().ordinal();
      if (cradle != null) {
         flags |= cradle.machineState().ordinal() << 2;
         flags |= cradle.occupiedSlots() << 4;
         flags |= cradle.storedGenerations() << 8;
         if (cradle.storedProfile() != null) flags |= 1 << 10;
      }
      status.set(flags);
   }

   public Connection connection() { return CONNECTIONS[status.get() & 3]; }
   public MachineState machineState() { return MACHINE_STATES[(status.get() >> 2) & 3]; }
   public int occupiedSlots() { return (status.get() >> 4) & 15; }
   public int generations() { return (status.get() >> 8) & 3; }
   public boolean complete() { return (status.get() & (1 << 10)) != 0; }

   @Override
   public boolean clickMenuButton(Player player, int command) {
      if (controller == null || player.containerMenu != this || !stillValid(player)) return false;
      if (command == RECONNECT) return controller.reconnect();
      var cradle = controller.link().cradle();
      if (cradle == null || !player.level().mayInteract(player, cradle.getBlockPos())) return false;
      var state = cradle.machineState();
      return switch (command) {
         case POWER_ON -> state == MachineState.OFF && cradle.setMachineState(MachineState.STANDBY);
         case POWER_OFF -> cradle.setMachineState(MachineState.OFF);
         case CANCEL -> state == MachineState.STANDBY && cradle.setMachineState(MachineState.ABORTED);
         default -> false;
      };
   }

   @Override
   public boolean stillValid(Player player) {
      return controller == null || controller.canUse(player);
   }

   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      return ItemStack.EMPTY;
   }
}
