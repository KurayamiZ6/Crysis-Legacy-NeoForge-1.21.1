package io.github.painworks.crysismod.nanosuit.client;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class NanosuitPerspectiveDebugSettingsScreen extends Screen {
   private static final int MIN_OFFSET = -500;
   private static final int MAX_OFFSET = 500;
   private static final float MIN_SCALE = 0.2F;
   private static final float MAX_SCALE = 4.0F;
   private static final float MIN_ROTATION = -80.0F;
   private static final float MAX_ROTATION = 80.0F;
   private static final float MIN_TILT = -3.0F;
   private static final float MAX_TILT = 3.0F;
   private static final float MIN_VERTICAL_SKEW_DOWN = -1.2F;
   private static final float MAX_VERTICAL_SKEW_DOWN = 1.2F;
   private static final float MIN_TEXT_SIZE = 0.5F;
   private static final float MAX_TEXT_SIZE = 6.0F;
   private static final float MIN_SPACING = 0.0F;
   private static final float MAX_SPACING = 6.0F;
   private final Screen parent;
   private NanosuitPerspectiveDebugSettingsScreen.IntSlider xSlider;
   private NanosuitPerspectiveDebugSettingsScreen.IntSlider ySlider;
   private NanosuitPerspectiveDebugSettingsScreen.FloatSlider scaleXSlider;
   private NanosuitPerspectiveDebugSettingsScreen.FloatSlider scaleYSlider;
   private NanosuitPerspectiveDebugSettingsScreen.FloatSlider rotationSlider;
   private NanosuitPerspectiveDebugSettingsScreen.FloatSlider tiltSlider;
   private NanosuitPerspectiveDebugSettingsScreen.FloatSlider verticalSkewSlider;
   private NanosuitPerspectiveDebugSettingsScreen.FloatSlider textSizeSlider;
   private NanosuitPerspectiveDebugSettingsScreen.FloatSlider spacingSlider;

   public NanosuitPerspectiveDebugSettingsScreen(Screen parent) {
      super(Component.literal("Perspective Debug HUD"));
      this.parent = parent;
   }

   protected void init() {
      int panelWidth = 360;
      int left = (this.width - panelWidth) / 2;
      int top = (this.height - 266) / 2;
      int sliderWidth = panelWidth - 20;
      int sliderX = left + 10;
      int row = top + 14;
      int step = 22;
      this.xSlider = (NanosuitPerspectiveDebugSettingsScreen.IntSlider)this.addRenderableWidget(
         new NanosuitPerspectiveDebugSettingsScreen.IntSlider(
            sliderX,
            row,
            sliderWidth,
            20,
            "Pos X (RightHud)",
            NanosuitPerspectiveDebugOverlay.getOffsetX(),
            -500,
            500,
            NanosuitPerspectiveDebugOverlay::setOffsetX
         )
      );
      row += step;
      this.ySlider = (NanosuitPerspectiveDebugSettingsScreen.IntSlider)this.addRenderableWidget(
         new NanosuitPerspectiveDebugSettingsScreen.IntSlider(
            sliderX,
            row,
            sliderWidth,
            20,
            "Pos Y (RightHud)",
            NanosuitPerspectiveDebugOverlay.getOffsetY(),
            -500,
            500,
            NanosuitPerspectiveDebugOverlay::setOffsetY
         )
      );
      row += step;
      this.scaleXSlider = (NanosuitPerspectiveDebugSettingsScreen.FloatSlider)this.addRenderableWidget(
         new NanosuitPerspectiveDebugSettingsScreen.FloatSlider(
            sliderX, row, sliderWidth, 20, "Scale X", NanosuitPerspectiveDebugOverlay.getScaleX(), 0.2F, 4.0F, NanosuitPerspectiveDebugOverlay::setScaleX
         )
      );
      row += step;
      this.scaleYSlider = (NanosuitPerspectiveDebugSettingsScreen.FloatSlider)this.addRenderableWidget(
         new NanosuitPerspectiveDebugSettingsScreen.FloatSlider(
            sliderX, row, sliderWidth, 20, "Scale Y", NanosuitPerspectiveDebugOverlay.getScaleY(), 0.2F, 4.0F, NanosuitPerspectiveDebugOverlay::setScaleY
         )
      );
      row += step;
      this.rotationSlider = (NanosuitPerspectiveDebugSettingsScreen.FloatSlider)this.addRenderableWidget(
         new NanosuitPerspectiveDebugSettingsScreen.FloatSlider(
            sliderX,
            row,
            sliderWidth,
            20,
            "Rotation",
            NanosuitPerspectiveDebugOverlay.getRotationDeg(),
            -80.0F,
            80.0F,
            NanosuitPerspectiveDebugOverlay::setRotationDeg
         )
      );
      row += step;
      this.tiltSlider = (NanosuitPerspectiveDebugSettingsScreen.FloatSlider)this.addRenderableWidget(
         new NanosuitPerspectiveDebugSettingsScreen.FloatSlider(
            sliderX, row, sliderWidth, 20, "Tilt", NanosuitPerspectiveDebugOverlay.getTilt(), -3.0F, 3.0F, NanosuitPerspectiveDebugOverlay::setTilt
         )
      );
      row += step;
      this.verticalSkewSlider = (NanosuitPerspectiveDebugSettingsScreen.FloatSlider)this.addRenderableWidget(
         new NanosuitPerspectiveDebugSettingsScreen.FloatSlider(
            sliderX,
            row,
            sliderWidth,
            20,
            "Vertical Skew Down",
            NanosuitPerspectiveDebugOverlay.getVerticalSkewDown(),
            -1.2F,
            1.2F,
            NanosuitPerspectiveDebugOverlay::setVerticalSkewDown
         )
      );
      row += step;
      this.textSizeSlider = (NanosuitPerspectiveDebugSettingsScreen.FloatSlider)this.addRenderableWidget(
         new NanosuitPerspectiveDebugSettingsScreen.FloatSlider(
            sliderX, row, sliderWidth, 20, "Text Size", NanosuitPerspectiveDebugOverlay.getTextSize(), 0.5F, 6.0F, NanosuitPerspectiveDebugOverlay::setTextSize
         )
      );
      row += step;
      this.spacingSlider = (NanosuitPerspectiveDebugSettingsScreen.FloatSlider)this.addRenderableWidget(
         new NanosuitPerspectiveDebugSettingsScreen.FloatSlider(
            sliderX,
            row,
            sliderWidth,
            20,
            "Layer Spacing",
            NanosuitPerspectiveDebugOverlay.getLayerSpacing(),
            0.0F,
            6.0F,
            NanosuitPerspectiveDebugOverlay::setLayerSpacing
         )
      );
      this.addRenderableWidget(Button.builder(Component.literal("Reset"), button -> {
         NanosuitPerspectiveDebugOverlay.resetDefaults();
         this.syncSlidersFromOverlay();
      }).bounds(left + 10, row + 28, 166, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> this.onClose()).bounds(left + panelWidth - 176, row + 28, 166, 20).build());
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 128, 16777215);
      guiGraphics.drawCenteredString(this.font, "Live preview follows RightHud inertia group", this.width / 2, this.height / 2 - 116, 9295837);
   }

   public void onClose() {
      if (this.minecraft != null) {
         NanosuitPerspectiveDebugOverlay.persistAllToConfig();
         this.minecraft.setScreen(this.parent);
      }
   }

   private void syncSlidersFromOverlay() {
      this.xSlider.setActualValue(NanosuitPerspectiveDebugOverlay.getOffsetX());
      this.ySlider.setActualValue(NanosuitPerspectiveDebugOverlay.getOffsetY());
      this.scaleXSlider.setActualValue(NanosuitPerspectiveDebugOverlay.getScaleX());
      this.scaleYSlider.setActualValue(NanosuitPerspectiveDebugOverlay.getScaleY());
      this.rotationSlider.setActualValue(NanosuitPerspectiveDebugOverlay.getRotationDeg());
      this.tiltSlider.setActualValue(NanosuitPerspectiveDebugOverlay.getTilt());
      this.verticalSkewSlider.setActualValue(NanosuitPerspectiveDebugOverlay.getVerticalSkewDown());
      this.textSizeSlider.setActualValue(NanosuitPerspectiveDebugOverlay.getTextSize());
      this.spacingSlider.setActualValue(NanosuitPerspectiveDebugOverlay.getLayerSpacing());
   }

   private static final class FloatSlider extends AbstractSliderButton {
      private final String label;
      private final float min;
      private final float max;
      private final Consumer<Float> onChanged;

      private FloatSlider(int x, int y, int width, int height, String label, float initialValue, float min, float max, Consumer<Float> onChanged) {
         super(x, y, width, height, Component.empty(), normalize(initialValue, min, max));
         this.label = label;
         this.min = min;
         this.max = max;
         this.onChanged = onChanged;
         this.updateMessage();
      }

      protected void updateMessage() {
         this.setMessage(Component.literal(this.label + ": " + String.format(Locale.ROOT, "%.2f", denormalize(this.value, this.min, this.max))));
      }

      protected void applyValue() {
         this.onChanged.accept(denormalize(this.value, this.min, this.max));
      }

      private void setActualValue(float actual) {
         this.value = normalize(actual, this.min, this.max);
         this.applyValue();
         this.updateMessage();
      }

      private static double normalize(float actual, float min, float max) {
         float clamped = Mth.clamp(actual, min, max);
         return (clamped - min) / (max - min);
      }

      private static float denormalize(double normalized, float min, float max) {
         return Mth.clamp((float)normalized, 0.0F, 1.0F) * (max - min) + min;
      }
   }

   private static final class IntSlider extends AbstractSliderButton {
      private final String label;
      private final int min;
      private final int max;
      private final IntConsumer onChanged;

      private IntSlider(int x, int y, int width, int height, String label, int initialValue, int min, int max, IntConsumer onChanged) {
         super(x, y, width, height, Component.empty(), normalize(initialValue, min, max));
         this.label = label;
         this.min = min;
         this.max = max;
         this.onChanged = onChanged;
         this.updateMessage();
      }

      protected void updateMessage() {
         this.setMessage(Component.literal(this.label + ": " + denormalize(this.value, this.min, this.max)));
      }

      protected void applyValue() {
         this.onChanged.accept(denormalize(this.value, this.min, this.max));
      }

      private void setActualValue(int actual) {
         this.value = normalize(actual, this.min, this.max);
         this.applyValue();
         this.updateMessage();
      }

      private static double normalize(int actual, int min, int max) {
         int clamped = Mth.clamp(actual, min, max);
         return (double)(clamped - min) / (max - min);
      }

      private static int denormalize(double normalized, int min, int max) {
         double clamped = Mth.clamp((float)normalized, 0.0F, 1.0F);
         return Math.round((float)(min + clamped * (max - min)));
      }
   }
}
