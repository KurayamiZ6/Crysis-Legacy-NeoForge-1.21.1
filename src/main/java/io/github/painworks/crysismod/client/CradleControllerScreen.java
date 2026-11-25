package io.github.painworks.crysismod.client;

import io.github.painworks.crysismod.nanosuit.cradle.CradleControllerBlockEntity.Connection;
import io.github.painworks.crysismod.nanosuit.cradle.BatteryKind;
import io.github.painworks.crysismod.nanosuit.cradle.CradleControllerMenu;
import io.github.painworks.crysismod.nanosuit.cradle.CradleProcedure.Issue;
import io.github.painworks.crysismod.nanosuit.cradle.CradleProcedure.Operation;
import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlockEntity.MachineState;
import io.github.painworks.crysismod.nanosuit.cradle.RoboticArmAssemblyBlockEntity.Availability;
import io.github.painworks.crysismod.nanosuit.cradle.RoboticArmAssemblyBlockEntity.State;
import io.github.painworks.crysismod.registry.ModBlocks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

final class CradleControllerScreen extends AbstractContainerScreen<CradleControllerMenu> {
   private static final String PREFIX = "screen.crysismod.cradle_controller.";
   private static final String[] PIECES = {"head", "chest", "legs", "feet"};
   private static final int WHITE = 0xFFE9EFED;
   private static final int MUTED = 0xFFAEBEB9;
   private static final int GREEN = 0xFFA7E7CB;
   private static final int AMBER = 0xFFFFCF80;
   private Button power;
   private Button cancel;
   private Button reconnect;
   private Button integration;
   private Button extraction;
   private Issue lastIntegration;
   private Issue lastExtraction;

   static void register(RegisterMenuScreensEvent event) {
      event.register(ModBlocks.CRADLE_CONTROLLER_MENU.get(), CradleControllerScreen::new);
   }

   CradleControllerScreen(CradleControllerMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      imageWidth = 248;
      imageHeight = 234;
   }

   @Override
   protected void init() {
      super.init();
      power = addRenderableWidget(Button.builder(label("power_on"),
         button -> send(menu.machineState() == MachineState.OFF ? CradleControllerMenu.POWER_ON : CradleControllerMenu.POWER_OFF))
         .bounds(leftPos + 12, topPos + 156, 72, 20).build());
      cancel = addRenderableWidget(Button.builder(label("cancel"), button -> send(CradleControllerMenu.CANCEL))
         .bounds(leftPos + 88, topPos + 156, 72, 20).build());
      reconnect = addRenderableWidget(Button.builder(label("reconnect"), button -> send(CradleControllerMenu.RECONNECT))
         .bounds(leftPos + 164, topPos + 156, 72, 20).build());

      integration = addRenderableWidget(Button.builder(operationLabel(Operation.INTEGRATION), ignored -> send(CradleControllerMenu.INTEGRATION))
         .bounds(leftPos + 12, topPos + 184, 110, 20).build());
      extraction = addRenderableWidget(Button.builder(operationLabel(Operation.EXTRACTION), ignored -> send(CradleControllerMenu.EXTRACTION))
         .bounds(leftPos + 126, topPos + 184, 110, 20).build());
      lastIntegration = lastExtraction = null;
      String[] interventions = {"maintenance", "evolution"};
      for (int i = 0; i < interventions.length; i++) {
         var button = addRenderableWidget(Button.builder(label(interventions[i]), ignored -> {})
            .bounds(leftPos + 12 + i * 114, topPos + 207, 110, 20).build());
         button.active = false;
         button.setTooltip(Tooltip.create(label("unavailable")));
      }
      refreshButtons();
   }

   private static Component operationLabel(Operation operation) {
      return label("operation_cost", label(operation.key()), operation.cost());
   }

   private static Issue updateOperation(Button button, Operation operation, Issue issue, Issue last) {
      if (issue != last) {
         button.active = issue == Issue.READY;
         String reason = issue == Issue.STORAGE ? "storage_" + operation.key() : issue.key();
         button.setTooltip(Tooltip.create(label("issue_" + reason)));
      }
      return issue;
   }

   private void send(int command) {
      if (minecraft != null && minecraft.gameMode != null) {
         minecraft.gameMode.handleInventoryButtonClick(menu.containerId, command);
      }
   }

   private void refreshButtons() {
      var connection = menu.connection();
      var state = menu.machineState();
      power.setMessage(label(state == MachineState.OFF ? "power_on" : "power_off"));
      power.active = connection == Connection.CONNECTED && !state.locksBattery();
      cancel.active = menu.canCancelProcedure() || power.active && (state == MachineState.STANDBY
         || menu.armAvailability() == Availability.CONNECTED && menu.armState() != State.FOLDED);
      reconnect.active = connection == Connection.DISCONNECTED || connection == Connection.MISSING;
      lastIntegration = updateOperation(integration, Operation.INTEGRATION, menu.issue(Operation.INTEGRATION), lastIntegration);
      lastExtraction = updateOperation(extraction, Operation.EXTRACTION, menu.issue(Operation.EXTRACTION), lastExtraction);
   }

   @Override
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      refreshButtons();
      super.render(graphics, mouseX, mouseY, partialTick);
      renderTooltip(graphics, mouseX, mouseY);
   }

   @Override
   protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
      graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF465956);
      graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFF182220);
      graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + 23, 0xFF2E413C);
      graphics.fill(leftPos + 12, topPos + 64, leftPos + imageWidth - 12, topPos + 100, 0xFF23332F);
      graphics.fill(leftPos + 12, topPos + 114, leftPos + imageWidth - 12, topPos + 150, 0xFF23332F);
   }

   @Override
   protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
      var connection = menu.connection();
      boolean linked = connection == Connection.CONNECTED;
      draw(graphics, title, 12, 8, WHITE);
      draw(graphics, label("connection", label("link." + connection.key())), 12, 29, linked ? GREEN : MUTED);

      var state = menu.machineState();
      Component powerText = label(linked ? state == MachineState.OFF ? "off" : "on" : "unknown");
      Component stateText = label(linked ? "state." + state.key() : "unknown");
      draw(graphics, label("power", powerText), 12, 43, WHITE);
      draw(graphics, label("machine", stateText), 111, 43, WHITE);
      var arms = menu.armAvailability();
      Component armsText = switch (arms) {
         case ABSENT -> label("arms_absent");
         case UNAVAILABLE -> label("arms_unavailable");
         case CONNECTED -> label("arms_connected", label(switch (menu.armState()) {
            case FOLDED -> "arms_folded";
            case DEPLOYED -> "arms_deployed";
            case OPERATING -> "arms_operating";
         }));
      };
      draw(graphics, armsText, 12, 54, arms == Availability.CONNECTED ? GREEN : MUTED);

      for (int i = 0; i < PIECES.length; i++) {
         boolean present = (menu.occupiedSlots() & (1 << i)) != 0;
         String presence = linked ? present ? "present" : "empty" : "unknown";
         draw(graphics, label("piece", label(PIECES[i]), label(presence)), 18 + i % 2 * 111,
            67 + i / 2 * 11, present ? GREEN : MUTED);
      }
      if (linked) {
         String generation = switch (menu.generations()) {
            case 1 -> "n1";
            case 2 -> "n2";
            case 3 -> "mixed";
            default -> "empty";
         };
         String fit = menu.complete() ? "complete" : menu.occupiedSlots() == 0 ? "empty" : "incomplete";
         var summary = menu.occupiedSlots() == 0 ? label("empty") : label("suit", label(generation), label(fit));
         draw(graphics, summary, 18, 89, menu.complete() ? GREEN : MUTED);
      }
      var selected = menu.selectedOperation();
      if (selected != null) {
         draw(graphics, label(state == MachineState.RUNNING ? "operation_running" : "operation_armed",
            label(selected.key())), 12, 103, GREEN);
      }
      drawEnergy(graphics, linked);
   }

   private void drawEnergy(GuiGraphics graphics, boolean linked) {
      var kind = linked ? menu.batteryKind() : null;
      if (kind == null) {
         draw(graphics, label("battery", label(linked ? "battery_none" : "unknown")), 18, 120, MUTED);
         draw(graphics, label("energy", label(linked ? "battery_none" : "unknown")), 18, 134, MUTED);
         return;
      }
      int charge = menu.batteryCharge();
      int capacity = kind.capacity();
      int color = charge > 0 ? GREEN : AMBER;
      draw(graphics, label("battery", label(kind == BatteryKind.CELL ? "battery_cell" : "battery_ceph")), 18, 119, WHITE);
      draw(graphics, label("energy", label(charge > 0 ? "energy_ready" : "energy_empty")), 112, 119, color);
      draw(graphics, label("charge", charge, capacity, charge * 100 / capacity), 18, 131, color);
      graphics.fill(18, 143, 230, 147, 0xFF111C1A);
      int filled = charge == 0 ? 0 : Math.max(1, 212 * charge / capacity);
      if (filled > 0) graphics.fill(18, 143, 18 + filled, 147, color);
   }

   private void draw(GuiGraphics graphics, Component text, int x, int y, int color) {
      graphics.drawString(font, text, x, y, color, false);
   }

   private static Component label(String key, Object... args) {
      return Component.translatable(PREFIX + key, args);
   }
}
