package io.github.painworks.crysismod.nanosuit.client;

import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class NanosuitWalkRangeSettingsScreen extends Screen {
   private static final float MIN_VALUE = 0.0F;
   private static final float MAX_VALUE = 80.0F;
   private static final float DEFAULT_X = 55.377357F;
   private static final float DEFAULT_Y = 64.245285F;
   private final Screen parent;
   private NanosuitWalkRangeSettingsScreen.RangeSlider xSlider;
   private NanosuitWalkRangeSettingsScreen.RangeSlider ySlider;

   public NanosuitWalkRangeSettingsScreen(Screen parent) {
      super(Component.literal("Nanosuit Walk Range"));
      this.parent = parent;
   }

   protected void init() {
      int panelWidth = 240;
      int left = (this.width - panelWidth) / 2;
      int top = (this.height - 110) / 2;
      this.xSlider = (NanosuitWalkRangeSettingsScreen.RangeSlider)this.addRenderableWidget(
         new NanosuitWalkRangeSettingsScreen.RangeSlider(
            left + 10, top + 14, 220, 20, Component.literal("Walk Range X"), NanosuitHudOverlay.getMaxWalkOffsetX(), NanosuitHudOverlay::setMaxWalkOffsetX
         )
      );
      this.ySlider = (NanosuitWalkRangeSettingsScreen.RangeSlider)this.addRenderableWidget(
         new NanosuitWalkRangeSettingsScreen.RangeSlider(
            left + 10, top + 40, 220, 20, Component.literal("Walk Range Y"), NanosuitHudOverlay.getMaxWalkOffsetY(), NanosuitHudOverlay::setMaxWalkOffsetY
         )
      );
      this.addRenderableWidget(Button.builder(Component.literal("Reset"), button -> {
         this.xSlider.setActualValue(55.377357F);
         this.ySlider.setActualValue(64.245285F);
      }).bounds(left + 10, top + 72, 106, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose()).bounds(left + 124, top + 72, 106, 20).build());
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 66, 16777215);
   }

   public void onClose() {
      this.minecraft.setScreen(this.parent);
   }

   private static final class RangeSlider extends AbstractSliderButton {
      private final Component baseLabel;
      private final Consumer<Float> onValueChanged;

      private RangeSlider(int x, int y, int width, int height, Component baseLabel, float initialValue, Consumer<Float> onValueChanged) {
         super(x, y, width, height, Component.empty(), normalize(initialValue));
         this.baseLabel = baseLabel;
         this.onValueChanged = onValueChanged;
         this.updateMessage();
      }

      protected void updateMessage() {
         this.setMessage(Component.literal(this.baseLabel.getString() + ": " + String.format(Locale.ROOT, "%.2f", denormalize(this.value))));
      }

      protected void applyValue() {
         this.onValueChanged.accept(denormalize(this.value));
      }

      private void setActualValue(float actual) {
         this.value = normalize(actual);
         this.applyValue();
         this.updateMessage();
      }

      private static double normalize(float actual) {
         return Mth.clamp((actual - 0.0F) / 80.0F, 0.0F, 1.0F);
      }

      private static float denormalize(double normalized) {
         return Mth.clamp((float)normalized, 0.0F, 1.0F) * 80.0F + 0.0F;
      }
   }
}
