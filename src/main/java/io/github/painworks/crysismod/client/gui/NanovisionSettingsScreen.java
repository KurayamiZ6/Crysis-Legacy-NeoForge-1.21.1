package io.github.painworks.crysismod.client.gui;

import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.ThermalMarkerQuality;
import io.github.painworks.crysismod.client.nanovision.ThermalVisualPreset;
import io.github.painworks.crysismod.client.nanovision.preview.NanovisionSettingsPreviewRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class NanovisionSettingsScreen extends Screen {
   private static final int MARGIN = 12;
   private static final int GAP = 10;
   private static final int ROW_HEIGHT = 20;
   private static final int ROW_GAP = 4;
   private final Screen parent;
   private final List<NanovisionSettingsScreen.PlacedWidget> pageWidgets = new ArrayList<>();
   private final List<NanovisionSettingsScreen.SectionLabel> sectionLabels = new ArrayList<>();
   private final List<Runnable> pageResets = new ArrayList<>();
   private NanovisionSettingsScreen.Page page = NanovisionSettingsScreen.Page.SPECTRUM;
   private float previewZoom = 1.0F;
   private int leftX;
   private int leftY;
   private int leftWidth;
   private int panelHeight;
   private int rightX;
   private int rightWidth;
   private int settingsTop;
   private int settingsBottom;
   private int scroll;
   private int maxScroll;
   private int nextRowY;

   public NanovisionSettingsScreen(Screen parent) {
      super(Component.literal("Nanovision Settings"));
      this.parent = parent;
   }

   protected void init() {
      this.rebuildEditorWidgets();
   }

   private void rebuildEditorWidgets() {
      this.clearWidgets();
      this.pageWidgets.clear();
      this.sectionLabels.clear();
      this.pageResets.clear();
      int available = this.width - 24 - 10;
      int minimumLeft = this.width < 650 ? 180 : 250;
      int minimumRight = this.width < 650 ? 230 : 300;
      this.leftWidth = Mth.clamp(
         Math.round(available * 0.44F), Math.min(minimumLeft, Math.max(120, available - minimumRight)), Math.max(120, available - minimumRight)
      );
      this.rightWidth = available - this.leftWidth;
      this.leftX = 12;
      this.rightX = this.leftX + this.leftWidth + 10;
      this.leftY = 48;
      this.panelHeight = Math.max(180, this.height - this.leftY - 36);
      int tabGap = 3;
      int tabWidth = Math.max(
         52, (this.rightWidth - tabGap * (NanovisionSettingsScreen.Page.values().length - 1)) / NanovisionSettingsScreen.Page.values().length
      );
      int tabX = this.rightX;

      for (NanovisionSettingsScreen.Page candidate : NanovisionSettingsScreen.Page.values()) {
         int width = candidate == NanovisionSettingsScreen.Page.DEBUG ? this.rightX + this.rightWidth - tabX : tabWidth;
         this.addRenderableWidget(
            Button.builder(Component.literal((candidate == this.page ? "[" : "") + candidate.label + (candidate == this.page ? "]" : "")), button -> {
               if (this.page != candidate) {
                  this.page = candidate;
                  this.scroll = 0;
                  this.rebuildEditorWidgets();
               }
            }).bounds(tabX, this.leftY, width, 20).build()
         );
         tabX += tabWidth + tabGap;
      }

      this.settingsTop = this.leftY + 27;
      this.settingsBottom = this.leftY + this.panelHeight - 30;
      this.nextRowY = this.settingsTop;
      this.buildCurrentPage();
      this.maxScroll = Math.max(0, this.nextRowY - this.settingsTop - Math.max(1, this.settingsBottom - this.settingsTop));
      this.scroll = Mth.clamp(this.scroll, 0, this.maxScroll);
      this.updatePageWidgetPositions();
      this.addRenderableWidget(Button.builder(Component.literal("Reset page"), button -> {
         for (Runnable reset : List.copyOf(this.pageResets)) {
            reset.run();
         }

         this.rebuildEditorWidgets();
      }).bounds(this.rightX, this.leftY + this.panelHeight - 23, Math.min(110, this.rightWidth / 2), 20).build());
      this.addRenderableWidget(
         Button.builder(Component.literal("Done"), button -> this.onClose())
            .bounds(this.rightX + this.rightWidth - 90, this.leftY + this.panelHeight - 23, 90, 20)
            .build()
      );
   }

   private void buildCurrentPage() {
      int width = this.rightWidth - 10;
      switch (this.page) {
         case GENERAL:
            this.section("Availability and world filter");
            this.toggle(width, "Nanovision enabled", NanovisionConfig::nanovisionEnabled, NanovisionConfig::setNanovisionEnabled, true);
            this.toggle(width, "Requires helmet", NanovisionConfig::nanovisionRequiresHelmet, NanovisionConfig::setNanovisionRequiresHelmet, true);
            this.toggle(width, "Requires full set", NanovisionConfig::nanovisionRequiresFullSet, NanovisionConfig::setNanovisionRequiresFullSet, false);
            this.toggle(width, "Use night vision", NanovisionConfig::nanovisionUseNightVision, NanovisionConfig::setNanovisionUseNightVision, true);
            this.toggle(width, "Post shader enabled", NanovisionConfig::postShaderEnabled, NanovisionConfig::setPostShaderEnabled, true);
            this.enumButton(
               width,
               "Visual preset",
               ThermalVisualPreset.values(),
               NanovisionConfig::thermalVisualPreset,
               NanovisionConfig::setThermalVisualPreset,
               NanovisionConfig.DEFAULT_THERMAL_VISUAL_PRESET
            );
            this.section("Motion and activation UI");
            this.toggle(width, "Motion blur", NanovisionConfig::nanovisionMotionBlurEnabled, NanovisionConfig::setNanovisionMotionBlurEnabled, true);
            this.decimal(
               width,
               "Motion blur strength",
               NanovisionConfig::nanovisionMotionBlurStrength,
               NanovisionConfig::setNanovisionMotionBlurStrength,
               0.0,
               1.0,
               0.22,
               2
            );
            this.toggle(
               width, "Activation HUD wave", NanovisionConfig::nanovisionActivationWaveEnabled, NanovisionConfig::setNanovisionActivationWaveEnabled, false
            );
            this.decimal(
               width,
               "Activation world flash",
               NanovisionConfig::nanovisionActivationWorldFlashStrength,
               NanovisionConfig::setNanovisionActivationWorldFlashStrength,
               0.0,
               0.35,
               0.19789719626168223,
               2
            );
            this.integer(
               width,
               "Enabled text offset X",
               NanovisionConfig::nanovisionEnabledTextOffsetX,
               NanovisionConfig::setNanovisionEnabledTextOffsetX,
               -1000,
               1000,
               0
            );
            this.integer(
               width,
               "Enabled text offset Y",
               NanovisionConfig::nanovisionEnabledTextOffsetY,
               NanovisionConfig::setNanovisionEnabledTextOffsetY,
               -1000,
               1000,
               103
            );
            this.decimal(width, "Preview zoom", () -> this.previewZoom, value -> this.previewZoom = (float)value, 0.55, 1.65, 1.0, 2, false);
            break;
         case SPECTRUM:
            this.section("Warm player / villager spectrum");
            this.toggle(width, "Thermal marker enabled", NanovisionConfig::thermalMarkerEnabled, NanovisionConfig::setThermalMarkerEnabled, true);
            this.decimal(width, "Green layer width", NanovisionConfig::thermalWarmGreenWidth, NanovisionConfig::setThermalWarmGreenWidth, 0.25, 4.0, 4.0, 2);
            this.decimal(width, "Red layer width", NanovisionConfig::thermalWarmRedWidth, NanovisionConfig::setThermalWarmRedWidth, 0.25, 4.0, 0.9, 2);
            this.decimal(width, "Pink layer width", NanovisionConfig::thermalWarmPinkWidth, NanovisionConfig::setThermalWarmPinkWidth, 0.25, 4.0, 0.95, 2);
            this.decimal(
               width, "White-hot layer width", NanovisionConfig::thermalWarmWhiteWidth, NanovisionConfig::setThermalWarmWhiteWidth, 0.25, 4.0, 0.55, 2
            );
            this.decimal(
               width, "White-hot center curve", NanovisionConfig::thermalMarkerHeatPower, NanovisionConfig::setThermalMarkerHeatPower, 0.25, 4.0, 2.46, 2
            );
            this.decimal(
               width, "Banding strength", NanovisionConfig::thermalMarkerBandingStrength, NanovisionConfig::setThermalMarkerBandingStrength, 0.0, 1.0, 0.18, 2
            );
            this.section("Spatial layers and composition");
            this.decimal(
               width,
               "Inner radius ratio",
               NanovisionConfig::thermalMarkerInnerRadiusRatio,
               NanovisionConfig::setThermalMarkerInnerRadiusRatio,
               0.05,
               1.0,
               0.48,
               2
            );
            this.section("Classic legacy relief");
            this.decimal(
               width,
               "Geometry depth strength",
               NanovisionConfig::thermalGeometryDepthStrength,
               NanovisionConfig::setThermalGeometryDepthStrength,
               0.0,
               2.0,
               1.0,
               2
            );
            this.decimal(
               width, "Geometry depth radius", NanovisionConfig::thermalGeometryDepthRadius, NanovisionConfig::setThermalGeometryDepthRadius, 0.5, 3.0, 1.35, 2
            );
            this.decimal(
               width,
               "Geometry depth contrast",
               NanovisionConfig::thermalGeometryDepthContrast,
               NanovisionConfig::setThermalGeometryDepthContrast,
               0.25,
               3.0,
               1.15,
               2
            );
            this.section("Classic Improved - Crysis surface model");
            this.decimal(
               width,
               "Improved silhouette width",
               NanovisionConfig::thermalImprovedSilhouetteWidth,
               NanovisionConfig::setThermalImprovedSilhouetteWidth,
               0.4,
               2.0,
               0.9,
               2
            );
            this.decimal(
               width, "Improved rim curve", NanovisionConfig::thermalImprovedRimCurve, NanovisionConfig::setThermalImprovedRimCurve, 0.3, 1.5, 0.82, 2
            );
            this.decimal(
               width, "Improved hot core", NanovisionConfig::thermalImprovedCoreHeat, NanovisionConfig::setThermalImprovedCoreHeat, 0.55, 0.98, 0.79, 2
            );
            this.decimal(
               width,
               "Improved facing strength",
               NanovisionConfig::thermalImprovedFacingStrength,
               NanovisionConfig::setThermalImprovedFacingStrength,
               0.0,
               0.55,
               0.08,
               2
            );
            this.decimal(
               width,
               "Improved smooth normals",
               NanovisionConfig::thermalImprovedSmoothNormalStrength,
               NanovisionConfig::setThermalImprovedSmoothNormalStrength,
               0.0,
               1.0,
               0.92,
               2
            );
            this.decimal(
               width, "Improved relief scale", NanovisionConfig::thermalImprovedReliefScale, NanovisionConfig::setThermalImprovedReliefScale, 0.5, 3.0, 1.15, 2
            );
            this.decimal(
               width,
               "Improved relief threshold",
               NanovisionConfig::thermalImprovedReliefThreshold,
               NanovisionConfig::setThermalImprovedReliefThreshold,
               0.5,
               3.0,
               1.0,
               2
            );
            this.decimal(
               width,
               "Improved protrusion cooling",
               NanovisionConfig::thermalImprovedProtrusionCooling,
               NanovisionConfig::setThermalImprovedProtrusionCooling,
               0.0,
               0.5,
               0.12,
               2
            );
            this.decimal(
               width,
               "Improved recess heating",
               NanovisionConfig::thermalImprovedRecessCooling,
               NanovisionConfig::setThermalImprovedRecessCooling,
               0.0,
               0.4,
               0.1,
               2
            );
            this.decimal(
               width,
               "Improved relief smoothing",
               NanovisionConfig::thermalImprovedReliefSmoothing,
               NanovisionConfig::setThermalImprovedReliefSmoothing,
               0.0,
               1.0,
               0.88,
               2
            );
            this.section("Classic Improved V2 - hot peak");
            this.decimal(
               width,
               "V2 hot-peak area",
               NanovisionConfig::thermalImprovedV2HotPeakArea,
               NanovisionConfig::setThermalImprovedV2HotPeakArea,
               0.0,
               1.0,
               0.8660436137071651,
               2
            );
            this.section("Composition");
            this.decimal(
               width, "Outer glow ratio", NanovisionConfig::thermalMarkerOuterGlowRatio, NanovisionConfig::setThermalMarkerOuterGlowRatio, 0.0, 0.2, 0.06, 3
            );
            this.decimal(
               width, "Inner strength", NanovisionConfig::thermalMarkerInnerStrength, NanovisionConfig::setThermalMarkerInnerStrength, 0.0, 1.0, 1.0, 2
            );
            this.decimal(width, "Model opacity", NanovisionConfig::thermalMarkerModelOpacity, NanovisionConfig::setThermalMarkerModelOpacity, 0.0, 1.0, 0.9, 2);
            this.decimal(
               width,
               "Outer glow strength",
               NanovisionConfig::thermalMarkerOuterGlowStrength,
               NanovisionConfig::setThermalMarkerOuterGlowStrength,
               0.0,
               1.0,
               0.66,
               2
            );
            this.integer(
               width, "Maximum search radius", NanovisionConfig::thermalMarkerMaxSearchRadiusPx, NanovisionConfig::setThermalMarkerMaxSearchRadiusPx, 8, 96, 80
            );
            this.enumButton(
               width,
               "Thermal quality",
               ThermalMarkerQuality.values(),
               NanovisionConfig::thermalMarkerQuality,
               NanovisionConfig::setThermalMarkerQuality,
               NanovisionConfig.DEFAULT_THERMAL_MARKER_QUALITY
            );
            this.section("Sensor scanlines");
            this.decimal(
               width, "Scanline strength", NanovisionConfig::thermalScanlineStrength, NanovisionConfig::setThermalScanlineStrength, 0.0, 0.25, 0.08, 3
            );
            this.decimal(width, "Scanline spacing", NanovisionConfig::thermalScanlineSpacingPx, NanovisionConfig::setThermalScanlineSpacingPx, 1.0, 8.0, 4.0, 2);
            this.decimal(
               width, "Scanline sharpness", NanovisionConfig::thermalScanlineSharpness, NanovisionConfig::setThermalScanlineSharpness, 0.5, 8.0, 2.4, 2
            );
            this.decimal(
               width,
               "Scanline irregularity",
               NanovisionConfig::thermalScanlineIrregularityPx,
               NanovisionConfig::setThermalScanlineIrregularityPx,
               0.0,
               1.5,
               0.55,
               2
            );
            this.decimal(
               width,
               "Scanline palette shift",
               NanovisionConfig::thermalScanlinePaletteShift,
               NanovisionConfig::setThermalScanlinePaletteShift,
               0.0,
               0.18,
               0.11,
               3
            );
            this.decimal(width, "Scanline breakup", NanovisionConfig::thermalScanlineBreakup, NanovisionConfig::setThermalScanlineBreakup, 0.0, 1.0, 0.96, 2);
            this.decimal(
               width,
               "White-hot line suppression",
               NanovisionConfig::thermalScanlineWhiteHotSuppression,
               NanovisionConfig::setThermalScanlineWhiteHotSuppression,
               0.0,
               1.0,
               0.96,
               2
            );
            break;
         case TARGETS:
            this.section("Entity selection");
            this.decimal(
               width, "Maximum distance", NanovisionConfig::thermalMarkerMaxDistance, NanovisionConfig::setThermalMarkerMaxDistance, 1.0, 256.0, 96.0, 1
            );
            this.toggle(width, "Render players", NanovisionConfig::thermalMarkerRenderPlayers, NanovisionConfig::setThermalMarkerRenderPlayers, true);
            this.toggle(width, "Render animals", NanovisionConfig::thermalMarkerRenderAnimals, NanovisionConfig::setThermalMarkerRenderAnimals, true);
            this.toggle(width, "Render monsters", NanovisionConfig::thermalMarkerRenderMonsters, NanovisionConfig::setThermalMarkerRenderMonsters, true);
            this.toggle(
               width, "Render invisible entities", NanovisionConfig::thermalMarkerRenderInvisible, NanovisionConfig::setThermalMarkerRenderInvisible, false
            );
            this.toggle(
               width,
               "Skip first-person player",
               NanovisionConfig::thermalMarkerSkipFirstPersonPlayer,
               NanovisionConfig::setThermalMarkerSkipFirstPersonPlayer,
               true
            );
            this.section("Thermal class overrides");
            this.classOverrides(width);
            this.section("Mod compatibility pass");
            this.toggle(
               width,
               "Compatibility entity pass",
               NanovisionConfig::thermalMarkerCompatibilityEntityPass,
               NanovisionConfig::setThermalMarkerCompatibilityEntityPass,
               true
            );
            this.section("Hot blocks");
            this.toggle(width, "Hot blocks enabled", NanovisionConfig::thermalHotBlocksEnabled, NanovisionConfig::setThermalHotBlocksEnabled, false);
            this.integer(
               width, "Hot block max distance", NanovisionConfig::thermalHotBlockMaxDistance, NanovisionConfig::setThermalHotBlockMaxDistance, 8, 48, 32
            );
            this.decimal(width, "Hot block opacity", NanovisionConfig::thermalHotBlockOpacity, NanovisionConfig::setThermalHotBlockOpacity, 0.0, 1.0, 0.88, 2);
            this.section("Held items");
            this.toggle(
               width, "Cold held-item spectrum (1st/3rd person)", NanovisionConfig::thermalHeldItemsEnabled, NanovisionConfig::setThermalHeldItemsEnabled, true
            );
            this.decimal(
               width, "Hand contact heat", NanovisionConfig::thermalHeldItemContactHeat, NanovisionConfig::setThermalHeldItemContactHeat, 0.0, 1.0, 0.32, 2
            );
            break;
         case DEBUG:
            this.section("Thermal diagnostics");
            this.toggle(
               width, "Improved relief only", NanovisionConfig::thermalGeometryReliefDebugOnly, NanovisionConfig::setThermalGeometryReliefDebugOnly, false
            );
            this.toggle(width, "Nanovision debug", NanovisionConfig::nanovisionDebug, NanovisionConfig::setNanovisionDebug, false);
            this.toggle(width, "Raw marker", NanovisionConfig::thermalMarkerDebugShowRawMarker, NanovisionConfig::setThermalMarkerDebugShowRawMarker, false);
            this.toggle(
               width, "Detected mask", NanovisionConfig::thermalMarkerDebugShowDetectedMask, NanovisionConfig::setThermalMarkerDebugShowDetectedMask, false
            );
            this.toggle(
               width, "Heat / distance field", NanovisionConfig::thermalMarkerDebugShowDistance, NanovisionConfig::setThermalMarkerDebugShowDistance, false
            );
            this.toggle(
               width, "Disable palette", NanovisionConfig::thermalMarkerDebugDisablePalette, NanovisionConfig::setThermalMarkerDebugDisablePalette, false
            );
            this.toggle(width, "Disable glow", NanovisionConfig::thermalMarkerDebugDisableGlow, NanovisionConfig::setThermalMarkerDebugDisableGlow, false);
            this.toggle(width, "Log marker pass", NanovisionConfig::thermalMarkerDebugLog, NanovisionConfig::setThermalMarkerDebugLog, false);
            this.toggle(width, "Post passthrough", NanovisionConfig::nanovisionDebugPostPassthrough, NanovisionConfig::setNanovisionDebugPostPassthrough, false);
            this.toggle(
               width, "Opaque grayscale only", NanovisionConfig::nanovisionDebugOpaqueGrayOnly, NanovisionConfig::setNanovisionDebugOpaqueGrayOnly, false
            );
      }
   }

   private void section(String label) {
      if (this.nextRowY > this.settingsTop) {
         this.nextRowY += 4;
      }

      this.sectionLabels.add(new NanovisionSettingsScreen.SectionLabel(label, this.nextRowY));
      this.nextRowY += 15;
   }

   private void toggle(int width, String label, BooleanSupplier getter, Consumer<Boolean> setter, boolean defaultValue) {
      Button[] reference = new Button[]{Button.builder(toggleText(label, getter.getAsBoolean()), button -> {
         setter.accept(!getter.getAsBoolean());
         button.setMessage(toggleText(label, getter.getAsBoolean()));
      }).bounds(this.rightX, this.nextRowY, width, 20).build()};
      this.addPageWidget(reference[0], this.nextRowY);
      this.pageResets.add(() -> setter.accept(defaultValue));
      this.nextRowY += 24;
   }

   private void decimal(int width, String label, DoubleSupplier getter, DoubleConsumer setter, double min, double max, double defaultValue, int decimals) {
      this.decimal(width, label, getter, setter, min, max, defaultValue, decimals, true);
   }

   private void decimal(
      int width, String label, DoubleSupplier getter, DoubleConsumer setter, double min, double max, double defaultValue, int decimals, boolean recordReset
   ) {
      NanovisionSettingsScreen.DoubleSlider slider = new NanovisionSettingsScreen.DoubleSlider(
         this.rightX, this.nextRowY, width, label, getter.getAsDouble(), min, max, decimals, setter
      );
      this.addPageWidget(slider, this.nextRowY);
      if (recordReset) {
         this.pageResets.add(() -> setter.accept(defaultValue));
      }

      this.nextRowY += 24;
   }

   private void integer(int width, String label, IntSupplier getter, IntConsumer setter, int min, int max, int defaultValue) {
      NanovisionSettingsScreen.IntSlider slider = new NanovisionSettingsScreen.IntSlider(
         this.rightX, this.nextRowY, width, label, getter.getAsInt(), min, max, setter
      );
      this.addPageWidget(slider, this.nextRowY);
      this.pageResets.add(() -> setter.accept(defaultValue));
      this.nextRowY += 24;
   }

   private <T extends Enum<T>> void enumButton(int width, String label, T[] values, Supplier<T> getter, Consumer<T> setter, T defaultValue) {
      Button[] reference = new Button[]{Button.builder(enumText(label, getter.get()), button -> {
         T current = getter.get();
         T next = values[(current.ordinal() + 1) % values.length];
         setter.accept(next);
         button.setMessage(enumText(label, getter.get()));
      }).bounds(this.rightX, this.nextRowY, width, 20).build()};
      this.addPageWidget(reference[0], this.nextRowY);
      this.pageResets.add(() -> setter.accept(defaultValue));
      this.nextRowY += 24;
   }

   private void addPageWidget(AbstractWidget widget, int baseY) {
      this.addRenderableWidget(widget);
      this.pageWidgets.add(new NanovisionSettingsScreen.PlacedWidget(widget, baseY));
   }

   private void classOverrides(int width) {
      int applyWidth = 54;
      EditBox input = new EditBox(this.font, this.rightX, this.nextRowY, width - applyWidth - 4, 20, Component.literal("registry_id=CLASS; namespace:*=CLASS"));
      input.setMaxLength(2048);
      input.setValue(String.join("; ", NanovisionConfig.thermalEntityClassOverrides()));
      input.setHint(Component.literal("minecraft:zombie=COOL; modid:*=UNKNOWN"));
      Button apply = Button.builder(Component.literal("Apply"), button -> {
         List<String> values = List.of(input.getValue().split(";"));
         NanovisionConfig.setThermalEntityClassOverrides(values);
         input.setValue(String.join("; ", NanovisionConfig.thermalEntityClassOverrides()));
      }).bounds(this.rightX + width - applyWidth, this.nextRowY, applyWidth, 20).build();
      this.addPageWidget(input, this.nextRowY);
      this.addPageWidget(apply, this.nextRowY);
      this.pageResets.add(() -> NanovisionConfig.setThermalEntityClassOverrides(List.of()));
      this.nextRowY += 24;
   }

   private void updatePageWidgetPositions() {
      for (NanovisionSettingsScreen.PlacedWidget placement : this.pageWidgets) {
         int y = placement.baseY - this.scroll;
         placement.widget.setY(y);
         boolean visible = y + placement.widget.getHeight() > this.settingsTop && y < this.settingsBottom;
         placement.widget.visible = visible;
         placement.widget.active = visible;
      }
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
      fillPanel(guiGraphics, this.leftX, this.leftY, this.leftWidth, this.panelHeight);
      fillPanel(guiGraphics, this.rightX, this.leftY, this.rightWidth, this.panelHeight);
      int previewX = this.leftX + 8;
      int previewY = this.leftY + 22;
      int previewWidth = this.leftWidth - 16;
      int previewHeight = this.panelHeight - 44;
      guiGraphics.fill(previewX, previewY, previewX + previewWidth, previewY + previewHeight, -16250097);
      boolean previewOk = NanovisionSettingsPreviewRenderer.render(
         guiGraphics, this.width, this.height, previewX, previewY, previewWidth, previewHeight, mouseX, mouseY, this.previewZoom
      );
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 16777215);
      guiGraphics.drawString(this.font, "Live warm-class preview", this.leftX + 8, this.leftY + 7, 13625343, false);
      guiGraphics.drawCenteredString(this.font, "Player / Villager class cube", this.leftX + this.leftWidth / 2, this.leftY + this.panelHeight - 17, 9677244);
      if (!previewOk) {
         guiGraphics.drawCenteredString(this.font, "Preview shader unavailable", this.leftX + this.leftWidth / 2, this.leftY + this.panelHeight / 2, 16744576);
      }

      for (NanovisionSettingsScreen.SectionLabel label : this.sectionLabels) {
         int y = label.baseY - this.scroll;
         if (y >= this.settingsTop - 2 && y < this.settingsBottom) {
            guiGraphics.drawString(this.font, label.text, this.rightX + 3, y + 2, 8375295, false);
         }
      }

      this.drawScrollbar(guiGraphics);
   }

   private void drawScrollbar(GuiGraphics guiGraphics) {
      if (this.maxScroll > 0) {
         int x0 = this.rightX + this.rightWidth - 5;
         int trackHeight = this.settingsBottom - this.settingsTop;
         int thumbHeight = Math.max(18, trackHeight * trackHeight / (trackHeight + this.maxScroll));
         int travel = Math.max(1, trackHeight - thumbHeight);
         int offset = Math.round((float)this.scroll / this.maxScroll * travel);
         guiGraphics.fill(x0, this.settingsTop, x0 + 3, this.settingsBottom, 1613774928);
         guiGraphics.fill(x0, this.settingsTop + offset, x0 + 3, this.settingsTop + offset + thumbHeight, -1066354200);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalDelta, double delta) {
      if (mouseX >= this.rightX && mouseX <= this.rightX + this.rightWidth && mouseY >= this.settingsTop && mouseY <= this.settingsBottom && this.maxScroll > 0
         )
       {
         this.scroll = Mth.clamp(this.scroll - (int)Math.round(delta * 22.0), 0, this.maxScroll);
         this.updatePageWidgetPositions();
         return true;
      } else if (mouseX >= this.leftX && mouseX <= this.leftX + this.leftWidth) {
         this.previewZoom = Mth.clamp(this.previewZoom + (float)delta * 0.08F, 0.55F, 1.65F);
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontalDelta, delta);
      }
   }

   public void onClose() {
      NanovisionSettingsPreviewRenderer.close();
      if (this.minecraft != null) {
         this.minecraft.setScreen(this.parent);
      }
   }

   public void removed() {
      NanovisionSettingsPreviewRenderer.close();
      super.removed();
   }

   public boolean isPauseScreen() {
      return false;
   }

   private static void fillPanel(GuiGraphics graphics, int x, int y, int width, int height) {
      graphics.fill(x, y, x + width, y + height, -804120800);
      graphics.fill(x, y, x + width, y + 1, -1604281659);
      graphics.fill(x, y + height - 1, x + width, y + height, -1604281659);
   }

   private static Component toggleText(String label, boolean value) {
      return Component.literal(label + ": " + (value ? "ON" : "OFF"));
   }

   private static Component enumText(String label, Enum<?> value) {
      return Component.literal(label + ": " + value.name().toLowerCase(Locale.ROOT));
   }

   private static final class DoubleSlider extends AbstractSliderButton {
      private final String label;
      private final double min;
      private final double max;
      private final int decimals;
      private final DoubleConsumer consumer;

      private DoubleSlider(int x, int y, int width, String label, double current, double min, double max, int decimals, DoubleConsumer consumer) {
         super(x, y, width, 20, Component.empty(), Mth.clamp((current - min) / (max - min), 0.0, 1.0));
         this.label = label;
         this.min = min;
         this.max = max;
         this.decimals = decimals;
         this.consumer = consumer;
         this.updateMessage();
      }

      private double current() {
         return this.min + this.value * (this.max - this.min);
      }

      protected void updateMessage() {
         this.setMessage(Component.literal(this.label + ": " + String.format(Locale.ROOT, "%." + this.decimals + "f", this.current())));
      }

      protected void applyValue() {
         this.consumer.accept(this.current());
      }
   }

   private static final class IntSlider extends AbstractSliderButton {
      private final String label;
      private final int min;
      private final int max;
      private final IntConsumer consumer;

      private IntSlider(int x, int y, int width, String label, int current, int min, int max, IntConsumer consumer) {
         super(x, y, width, 20, Component.empty(), Mth.clamp((double)(current - min) / (max - min), 0.0, 1.0));
         this.label = label;
         this.min = min;
         this.max = max;
         this.consumer = consumer;
         this.updateMessage();
      }

      private int current() {
         return Mth.clamp((int)Math.round(this.min + this.value * (this.max - this.min)), this.min, this.max);
      }

      protected void updateMessage() {
         this.setMessage(Component.literal(this.label + ": " + this.current()));
      }

      protected void applyValue() {
         this.consumer.accept(this.current());
      }
   }

   private static enum Page {
      GENERAL("General"),
      SPECTRUM("Spectrum"),
      TARGETS("Targets"),
      DEBUG("Debug");

      private final String label;

      private Page(String label) {
         this.label = label;
      }
   }

   private record PlacedWidget(AbstractWidget widget, int baseY) {
   }

   private record SectionLabel(String text, int baseY) {
   }
}
