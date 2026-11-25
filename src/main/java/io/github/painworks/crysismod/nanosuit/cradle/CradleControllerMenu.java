package io.github.painworks.crysismod.nanosuit.cradle;

import io.github.painworks.crysismod.nanosuit.cradle.CradleControllerBlockEntity.Connection;
import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlockEntity.MachineState;
import io.github.painworks.crysismod.nanosuit.cradle.RoboticArmAssemblyBlockEntity.Availability;
import io.github.painworks.crysismod.nanosuit.cradle.RoboticArmAssemblyBlockEntity.State;
import io.github.painworks.crysismod.nanosuit.cradle.CradleProcedure.Issue;
import io.github.painworks.crysismod.nanosuit.cradle.CradleProcedure.Operation;
import io.github.painworks.crysismod.registry.ModBlocks;
import net.minecraft.server.level.ServerPlayer;
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
   public static final int INTEGRATION = 4;
   public static final int EXTRACTION = 5;
   private static final Connection[] CONNECTIONS = Connection.values();
   private static final MachineState[] MACHINE_STATES = MachineState.values();
   private static final Availability[] ARM_AVAILABILITY = Availability.values();
   private static final State[] ARM_STATES = State.values();
   private static final Issue[] ISSUES = Issue.values();
   private static final int ARM_AVAILABILITY_SHIFT = 11;
   private static final int ARM_STATE_SHIFT = 13;
   private static final int CHARGE_BITS = 11;
   private static final int CHARGE_MASK = (1 << CHARGE_BITS) - 1;
   private final CradleControllerBlockEntity controller;
   private final DataSlot status;
   private final DataSlot energy;
   private final DataSlot procedure;
   private final Player viewer;

   public CradleControllerMenu(int id, Inventory inventory) {
      this(id, inventory, null);
   }

   CradleControllerMenu(int id, Inventory inventory, CradleControllerBlockEntity controller) {
      super(ModBlocks.CRADLE_CONTROLLER_MENU.get(), id);
      this.controller = controller;
      viewer = inventory == null ? null : inventory.player;
      status = addDataSlot(DataSlot.standalone());
      energy = addDataSlot(DataSlot.standalone());
      procedure = addDataSlot(DataSlot.standalone());
      procedure.set(Issue.NO_CRADLE.ordinal() | Issue.NO_CRADLE.ordinal() << 3);
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
         var arms = cradle.roboticArms();
         flags |= arms.availability().ordinal() << ARM_AVAILABILITY_SHIFT;
         if (arms.assembly() != null) flags |= arms.assembly().armState().ordinal() << ARM_STATE_SHIFT;
      } else {
         flags |= Availability.UNAVAILABLE.ordinal() << ARM_AVAILABILITY_SHIFT;
      }
      status.set(flags);
      var kind = cradle == null ? null : cradle.batteryKind();
      energy.set(kind == null ? 0 : ((kind.ordinal() + 1) << CHARGE_BITS) | cradle.batteryCharge());
      int actions = CradleProcedure.issue(cradle, Operation.INTEGRATION).ordinal()
         | CradleProcedure.issue(cradle, Operation.EXTRACTION).ordinal() << 3;
      var plan = cradle == null ? null : cradle.procedure();
      if (plan != null) {
         actions |= (plan.operation().ordinal() + 1) << 6;
         if (!plan.running() || viewer instanceof ServerPlayer player && plan.wornBy(player)) actions |= 1 << 8;
      }
      procedure.set(actions);
   }

   public Connection connection() { return CONNECTIONS[status.get() & 3]; }
   public MachineState machineState() { return MACHINE_STATES[(status.get() >> 2) & 3]; }
   public int occupiedSlots() { return (status.get() >> 4) & 15; }
   public int generations() { return (status.get() >> 8) & 3; }
   public boolean complete() { return (status.get() & (1 << 10)) != 0; }
   public Availability armAvailability() { return ARM_AVAILABILITY[(status.get() >> ARM_AVAILABILITY_SHIFT) & 3]; }
   public State armState() { return ARM_STATES[(status.get() >> ARM_STATE_SHIFT) & 3]; }
   public BatteryKind batteryKind() {
      return switch (energy.get() >> CHARGE_BITS) {
         case 1 -> BatteryKind.CELL;
         case 2 -> BatteryKind.CEPH;
         default -> null;
      };
   }
   public int batteryCharge() { return energy.get() & CHARGE_MASK; }
   public Issue issue(Operation operation) {
      return ISSUES[(procedure.get() >> (operation == Operation.INTEGRATION ? 0 : 3)) & 7];
   }
   public Operation selectedOperation() {
      return switch ((procedure.get() >> 6) & 3) {
         case 1 -> Operation.INTEGRATION;
         case 2 -> Operation.EXTRACTION;
         default -> null;
      };
   }
   public boolean canCancelProcedure() { return (procedure.get() & (1 << 8)) != 0; }

   @Override
   public boolean clickMenuButton(Player player, int command) {
      if (controller == null || !(player instanceof ServerPlayer serverPlayer) || player.containerMenu != this || !stillValid(player)) return false;
      if (command == RECONNECT) return controller.reconnect();
      var cradle = controller.link().cradle();
      if (cradle == null || !player.level().mayInteract(player, cradle.getBlockPos())) return false;
      var state = cradle.machineState();
      return switch (command) {
         case POWER_ON -> state == MachineState.OFF && cradle.setMachineState(MachineState.STANDBY);
         case POWER_OFF, CANCEL -> stop(cradle, state, command, serverPlayer);
         case INTEGRATION -> CradleProcedure.program(cradle, Operation.INTEGRATION);
         case EXTRACTION -> CradleProcedure.program(cradle, Operation.EXTRACTION);
         default -> false;
      };
   }

   private static boolean stop(NanosuitCradleBlockEntity cradle, MachineState state, int command, ServerPlayer player) {
      var plan = cradle.procedure();
      if (plan != null) {
         if (plan.running() && (command != CANCEL || !plan.wornBy(player))) return false;
         boolean canceled = plan.cancel();
         if (command == POWER_OFF) cradle.setMachineState(MachineState.OFF);
         return canceled;
      }
      if (state.locksBattery()) return false;
      boolean folded = cradle.foldRoboticArms();
      boolean changed = command == POWER_OFF ? cradle.setMachineState(MachineState.OFF)
         : state == MachineState.STANDBY && cradle.setMachineState(MachineState.ABORTED);
      return folded || changed;
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
