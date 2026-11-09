package io.github.painworks.crysismod.nanosuit.client;

import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class NanosuitHudPositionSettingsScreen extends Screen {
   private static final int MIN_VALUE = -2000;
   private static final int MAX_VALUE = 2000;
   private static final int DEFAULT_X = -28;
   private static final int DEFAULT_Y = -8;
   private final Screen parent;
   private NanosuitHudPositionSettingsScreen.IntRangeSlider xSlider;
   private NanosuitHudPositionSettingsScreen.IntRangeSlider ySlider;

   public NanosuitHudPositionSettingsScreen(Screen parent) {
      super(Component.literal("Nanosuit RightHud Position"));
      this.parent = parent;
   }

   protected void init() {
      int panelWidth = 280;
      int left = (this.width - panelWidth) / 2;
      int top = (this.height - 120) / 2;
      this.xSlider = (NanosuitHudPositionSettingsScreen.IntRangeSlider)this.addRenderableWidget(
         new NanosuitHudPositionSettingsScreen.IntRangeSlider(
            left + 10, top + 14, 260, 20, "RightHud X", NanosuitHudOverlay.getHudOffsetX(), NanosuitHudOverlay::setHudOffsetX
         )
      );
      this.ySlider = (NanosuitHudPositionSettingsScreen.IntRangeSlider)this.addRenderableWidget(
         new NanosuitHudPositionSettingsScreen.IntRangeSlider(
            left + 10, top + 40, 260, 20, "RightHud Y", NanosuitHudOverlay.getHudOffsetY(), NanosuitHudOverlay::setHudOffsetY
         )
      );
      this.addRenderableWidget(Button.builder(Component.literal("Reset"), button -> {
         this.xSlider.setActualValue(-28);
         this.ySlider.setActualValue(-8);
      }).bounds(left + 10, top + 76, 126, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose()).bounds(left + 144, top + 76, 126, 20).build());
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 70, 16777215);
   }

   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.setScreen(this.parent);
      }
   }

   private static final class IntRangeSlider extends AbstractSliderButton {
      private final String label;
      private final IntConsumer onValueChanged;

      private IntRangeSlider(int x, int y, int width, int height, String label, int initialValue, IntConsumer onValueChanged) {
         super(x, y, width, height, Component.empty(), normalize(initialValue));
         this.label = label;
         this.onValueChanged = onValueChanged;
         this.updateMessage();
      }

      protected void updateMessage() {
         this.setMessage(Component.literal(this.label + ": " + denormalize(this.value)));
      }

      protected void applyValue() {
         this.onValueChanged.accept(denormalize(this.value));
      }

      private void setActualValue(int actual) {
         this.value = normalize(actual);
         this.applyValue();
         this.updateMessage();
      }

      private static double normalize(int actual) {
         float clamped = Mth.clamp(actual, -2000, 2000);
         return (clamped - -2000.0F) / 4000.0;
      }

      private static int denormalize(double normalized) {
         double clamped = Mth.clamp((float)normalized, 0.0F, 1.0F);
         return Math.round((float)(-2000.0 + clamped * 4000.0));
      }
   }
}
