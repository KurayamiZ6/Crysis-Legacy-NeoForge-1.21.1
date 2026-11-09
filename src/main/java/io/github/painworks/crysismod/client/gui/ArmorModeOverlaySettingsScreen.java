package io.github.painworks.crysismod.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoArmorModeDynamicTextureCompatRenderer;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoArmorModePulseCenterExtractor;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoArmorModePulseCenterSet;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoArmorModePulseCenters;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoClientConfig;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitArmorModeOverlayState;
import io.github.painworks.crysismod.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.joml.Matrix4f;

public final class ArmorModeOverlaySettingsScreen extends Screen {
   private static final int PANEL_MARGIN = 12;
   private static final int PANEL_GAP = 10;
   private static final int FOOTER_HEIGHT = 28;
   private static final int HEADER_HEIGHT = 22;
   private static final int RIGHT_PANEL_PADDING = 10;
   private static final int SETTINGS_SCROLL_STEP = 24;
   private static final int RESET_BUTTON_WIDTH = 20;
   private static final int RESET_BUTTON_GAP = 4;
   private static final long RUNTIME_APPLY_DEBOUNCE_MILLIS = 180L;
   private static final float SQRT3 = 1.7320508F;
   private static final float TAU = (float) (Math.PI * 2);
   private static final float DEFAULT_MODEL_PREVIEW_ZOOM = 1.0F;
   private static final float DEFAULT_TEXTURE_PREVIEW_ZOOM = 1.0F;
   private static final float DEFAULT_PULSE_PREVIEW_ZOOM = 1.0F;
   private static final float MIN_VISIBLE_PREVIEW_PULSE_CORE_WIDTH = 0.2F;
   private static final float MIN_VISIBLE_PREVIEW_PULSE_GLOW_WIDTH = 0.35F;
   private static final float MAX_COMPAT_SAFE_PULSE_RADIUS_PX = 96.0F;
   private final Screen parent;
   private final ArmorModeOverlaySettingsScreen.EditableSettings settings;
   private final ItemStack previewChestplate;
   private final ResourceLocation overlayTexture;
   private final List<ArmorModeOverlaySettingsScreen.PageWidgetPlacement> pageWidgets = new ArrayList<>();
   private final float[] previewPulseCenterX = new float[64];
   private final float[] previewPulseCenterY = new float[64];
   private ArmorModeOverlaySettingsScreen.Page currentPage = ArmorModeOverlaySettingsScreen.Page.PLASMA;
   private ArmorModeOverlaySettingsScreen.PreviewPane currentPreviewPane = ArmorModeOverlaySettingsScreen.PreviewPane.MODEL;
   private ArmorModeOverlaySettingsScreen.PreviewQuality previewQuality = ArmorModeOverlaySettingsScreen.PreviewQuality.MEDIUM;
   private boolean overlayTextureMissing;
   private boolean modelPreviewEnabled = true;
   private boolean texturePreviewEnabled = true;
   private boolean pulsePreviewEnabled = true;
   private float modelPreviewZoom = 1.0F;
   private float texturePreviewZoom = 1.0F;
   private float pulsePreviewZoom = 1.0F;
   private int leftPanelX;
   private int leftPanelY;
   private int leftPanelWidth;
   private int leftPanelHeight;
   private int centerPanelX;
   private int centerPanelY;
   private int centerPanelWidth;
   private int centerPanelHeight;
   private int rightPanelX;
   private int rightPanelY;
   private int rightPanelWidth;
   private int rightPanelHeight;
   private int settingsViewportTop;
   private int settingsViewportBottom;
   private int settingsScrollPx;
   private int settingsMaxScrollPx;
   private boolean pointerDown;
   private boolean pendingRuntimeApply;
   private long pendingRuntimeApplyAtMillis;

   public ArmorModeOverlaySettingsScreen(Screen parent) {
      super(Component.literal("Armor Mode Layer Settings"));
      this.parent = parent;
      this.settings = ArmorModeOverlaySettingsScreen.EditableSettings.fromConfig();
      this.previewChestplate = new ItemStack((ItemLike)ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get());
      this.overlayTexture = NanosuitArmorModeOverlayState.overlayTexture();
   }

   protected void init() {
      this.recomputeLayout();
      this.overlayTextureMissing = this.minecraft == null || this.minecraft.getResourceManager().getResource(this.overlayTexture).isEmpty();
      this.rebuildEditorWidgets();
   }

   private void recomputeLayout() {
      int contentX = 12;
      int contentY = 12;
      int contentWidth = Math.max(360, this.width - 24);
      int contentHeight = Math.max(220, this.height - 24 - 28);
      int availableWidth = contentWidth - 10;
      int previewWidth = Mth.clamp(Math.round(availableWidth * 0.43F), 240, 460);
      int settingsWidth = Math.max(250, availableWidth - previewWidth);
      if (settingsWidth < 280) {
         int deficit = 280 - settingsWidth;
         previewWidth = Math.max(220, previewWidth - deficit);
         settingsWidth = availableWidth - previewWidth;
      }

      this.leftPanelWidth = previewWidth;
      this.centerPanelWidth = 0;
      this.rightPanelWidth = settingsWidth;
      int totalWidth = this.leftPanelWidth + this.rightPanelWidth + 10;
      int offsetX = Math.max(0, (contentWidth - totalWidth) / 2);
      this.leftPanelX = contentX + offsetX;
      this.centerPanelX = this.leftPanelX;
      this.rightPanelX = this.leftPanelX + this.leftPanelWidth + 10;
      this.leftPanelY = contentY + 22;
      this.rightPanelY = this.leftPanelY;
      this.leftPanelHeight = contentHeight - 22;
      this.centerPanelY = this.leftPanelY;
      this.centerPanelHeight = 0;
      this.rightPanelHeight = this.leftPanelHeight;
   }

   private void rebuildEditorWidgets() {
      this.clearWidgets();
      this.pageWidgets.clear();
      int rightInnerX = this.rightPanelX + 10;
      int rightInnerY = this.rightPanelY + 10;
      int rightInnerWidth = this.rightPanelWidth - 20;
      int pageButtonGap = 4;
      ArmorModeOverlaySettingsScreen.Page[] pages = ArmorModeOverlaySettingsScreen.Page.values();
      int pageButtonWidth = (rightInnerWidth - pageButtonGap * Math.max(0, pages.length - 1)) / pages.length;

      for (int i = 0; i < pages.length; i++) {
         int pageX = rightInnerX + i * (pageButtonWidth + pageButtonGap);
         this.addRenderableWidget(this.pageButton(pages[i], pageX, rightInnerY, pageButtonWidth));
      }

      int previewInnerX = this.leftPanelX + 10;
      int previewTabY = this.leftPanelY + 10;
      int previewInnerWidth = this.leftPanelWidth - 20;
      int previewButtonWidth = (previewInnerWidth - pageButtonGap * 2) / 3;
      this.addRenderableWidget(this.previewPaneButton(ArmorModeOverlaySettingsScreen.PreviewPane.MODEL, previewInnerX, previewTabY, previewButtonWidth));
      this.addRenderableWidget(
         this.previewPaneButton(
            ArmorModeOverlaySettingsScreen.PreviewPane.TEXTURE, previewInnerX + previewButtonWidth + pageButtonGap, previewTabY, previewButtonWidth
         )
      );
      this.addRenderableWidget(
         this.previewPaneButton(
            ArmorModeOverlaySettingsScreen.PreviewPane.PULSE, previewInnerX + (previewButtonWidth + pageButtonGap) * 2, previewTabY, previewButtonWidth
         )
      );
      int pageY = rightInnerY + 28;
      this.settingsViewportTop = pageY;
      this.settingsViewportBottom = this.rightPanelY + this.rightPanelHeight - 28;

      int contentHeight = switch (this.currentPage) {
         case BASIC -> this.addBasicPageWidgets(rightInnerX, pageY, rightInnerWidth);
         case PLASMA -> this.addPlasmaPageWidgets(rightInnerX, pageY, rightInnerWidth);
         case COMPAT -> this.addCompatPageWidgets(rightInnerX, pageY, rightInnerWidth);
         case PREVIEW -> this.addPreviewPageWidgets(rightInnerX, pageY, rightInnerWidth);
      };
      int viewportHeight = Math.max(0, this.settingsViewportBottom - this.settingsViewportTop);
      this.settingsMaxScrollPx = Math.max(0, contentHeight - viewportHeight);
      this.settingsScrollPx = Mth.clamp(this.settingsScrollPx, 0, this.settingsMaxScrollPx);
      this.refreshPageWidgetPositions();
      int footerY = this.height - 12 - 22;
      int footerGap = 8;
      int footerAvailableWidth = this.width - 24;
      int buttonWidth = Mth.clamp((footerAvailableWidth - footerGap * 2) / 3, 90, 120);
      int totalFooterWidth = buttonWidth * 3 + footerGap * 2;
      int footerX = Math.max(12, (this.width - totalFooterWidth) / 2);
      this.addRenderableWidget(Button.builder(Component.literal("Reset Defaults"), button -> {
         this.settings.resetToDefaults();
         this.applyChangesImmediately();
         this.rebuildEditorWidgets();
      }).bounds(footerX, footerY, buttonWidth, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Reset Page"), button -> {
         this.settings.resetCurrentPage(this.currentPage);
         this.applyChangesImmediately();
         this.rebuildEditorWidgets();
      }).bounds(footerX + buttonWidth + footerGap, footerY, buttonWidth, 20).build());
      this.addRenderableWidget(
         Button.builder(Component.literal("Close"), button -> this.onClose()).bounds(footerX + (buttonWidth + footerGap) * 2, footerY, buttonWidth, 20).build()
      );
   }

   private Button pageButton(ArmorModeOverlaySettingsScreen.Page page, int x, int y, int width) {
      boolean selected = this.currentPage == page;
      return Button.builder(Component.literal(selected ? "> " + page.label + " <" : page.label), button -> {
         if (this.currentPage != page) {
            this.currentPage = page;
            this.settingsScrollPx = 0;
            this.rebuildEditorWidgets();
         }
      }).bounds(x, y, width, 20).build();
   }

   private Button previewPaneButton(ArmorModeOverlaySettingsScreen.PreviewPane pane, int x, int y, int width) {
      boolean selected = this.currentPreviewPane == pane;
      return Button.builder(Component.literal(selected ? "> " + pane.label + " <" : pane.label), button -> {
         if (this.currentPreviewPane != pane) {
            this.currentPreviewPane = pane;
            this.rebuildEditorWidgets();
         }
      }).bounds(x, y, width, 20).build();
   }

   private int mainControlWidth(int rowWidth) {
      return Math.max(110, rowWidth - 20 - 4);
   }

   private int resetButtonX(int x, int rowWidth) {
      return x + this.mainControlWidth(rowWidth) + 4;
   }

   private Button resetButton(int x, int y, String defaultValue, Runnable onReset) {
      Button button = Button.builder(Component.literal("R"), widget -> {
         onReset.run();
         this.rebuildEditorWidgets();
      }).bounds(x, y, 20, 20).build();
      button.setTooltip(Tooltip.create(Component.literal("Reset to default: " + defaultValue)));
      return button;
   }

   private void addSectionLabel(int x, int y, int width, String label) {
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.StaticTextWidget(this.font, x, y, width, 20, label));
   }

   private void addResettableIntSlider(int x, int y, int width, String label, int value, int minValue, int maxValue, IntConsumer consumer, int defaultValue) {
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.IntSlider(x, y, this.mainControlWidth(width), label, value, minValue, maxValue, consumer));
      this.addPageWidget(this.resetButton(this.resetButtonX(x, width), y, Integer.toString(defaultValue), () -> consumer.accept(defaultValue)));
   }

   private void addResettableFloatSlider(
      int x, int y, int width, String label, float value, float minValue, float maxValue, Consumer<Float> consumer, float defaultValue
   ) {
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.FloatSlider(x, y, this.mainControlWidth(width), label, value, minValue, maxValue, consumer));
      this.addPageWidget(
         this.resetButton(this.resetButtonX(x, width), y, String.format(Locale.ROOT, "%.2f", defaultValue), () -> consumer.accept(defaultValue))
      );
   }

   private void addResettableToggle(int x, int y, int width, Supplier<String> labelSupplier, Runnable onClick, String defaultValue, Runnable onReset) {
      this.addPageWidget(this.toggleButton(x, y, this.mainControlWidth(width), labelSupplier, onClick));
      this.addPageWidget(this.resetButton(this.resetButtonX(x, width), y, defaultValue, onReset));
   }

   private void addResettableIntInputRow(int x, int y, int width, String label, int value, int minValue, int maxValue, IntConsumer consumer, int defaultValue) {
      int controlWidth = this.mainControlWidth(width);
      this.addIntInputRow(x, y, controlWidth, label, value, minValue, maxValue, consumer);
      this.addPageWidget(this.resetButton(this.resetButtonX(x, width), y, Integer.toString(defaultValue), () -> consumer.accept(defaultValue)));
   }

   private void addResettableHexColorInputRow(int x, int y, int width, String label, String value, Consumer<String> consumer, String defaultValue) {
      int controlWidth = this.mainControlWidth(width);
      this.addHexColorInputRow(x, y, controlWidth, label, value, consumer);
      this.addPageWidget(this.resetButton(this.resetButtonX(x, width), y, defaultValue, () -> consumer.accept(defaultValue)));
   }

   private int addBasicPageWidgets(int x, int y, int width) {
      this.addSectionLabel(x, y, width, "General");
      int rowY = y + 24;
      this.addResettableIntSlider(x, rowY, width, "Overlay Alpha", this.settings.overlayAlpha, 0, 255, value -> {
         this.settings.overlayAlpha = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModeOverlayAlpha());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Overlay Brightness", this.settings.overlayBrightness, 0.0F, 4.0F, value -> {
         this.settings.overlayBrightness = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModeOverlayBrightness());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Overlay Filter R", this.settings.overlayTintR, 0.0F, 2.0F, value -> {
         this.settings.overlayTintR = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModeOverlayTintR());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Overlay Filter G", this.settings.overlayTintG, 0.0F, 2.0F, value -> {
         this.settings.overlayTintG = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModeOverlayTintG());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Overlay Filter B", this.settings.overlayTintB, 0.0F, 2.0F, value -> {
         this.settings.overlayTintB = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModeOverlayTintB());
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Fullbright: " + onOff(this.settings.fullbright), () -> {
         this.settings.fullbright = !this.settings.fullbright;
         this.applyChangesImmediately();
      }, onOff(NanoGeoClientConfig.defaultArmorModeOverlayFullbright()), () -> {
         this.settings.fullbright = NanoGeoClientConfig.defaultArmorModeOverlayFullbright();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Overlay In 1P: " + onOff(this.settings.renderInFirstPerson), () -> {
         this.settings.renderInFirstPerson = !this.settings.renderInFirstPerson;
         this.applyChangesImmediately();
      }, onOff(NanoGeoClientConfig.defaultArmorModeOverlayRenderInFirstPerson()), () -> {
         this.settings.renderInFirstPerson = NanoGeoClientConfig.defaultArmorModeOverlayRenderInFirstPerson();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Overlay In 3P: " + onOff(this.settings.renderInThirdPerson), () -> {
         this.settings.renderInThirdPerson = !this.settings.renderInThirdPerson;
         this.applyChangesImmediately();
      }, onOff(NanoGeoClientConfig.defaultArmorModeOverlayRenderInThirdPerson()), () -> {
         this.settings.renderInThirdPerson = NanoGeoClientConfig.defaultArmorModeOverlayRenderInThirdPerson();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Editor Debug: " + onOff(this.settings.debugSettingsScreen), () -> {
         this.settings.debugSettingsScreen = !this.settings.debugSettingsScreen;
         this.applyChangesImmediately();
      }, onOff(NanoGeoClientConfig.defaultDebugArmorModeSettingsScreen()), () -> {
         this.settings.debugSettingsScreen = NanoGeoClientConfig.defaultDebugArmorModeSettingsScreen();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addSectionLabel(x, rowY, width, "Transition");
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Transition Enabled: " + onOff(this.settings.transitionEnabled), () -> {
         this.settings.transitionEnabled = !this.settings.transitionEnabled;
         this.applyChangesImmediately();
      }, onOff(NanoGeoClientConfig.defaultArmorModeTransitionEnabled()), () -> {
         this.settings.transitionEnabled = NanoGeoClientConfig.defaultArmorModeTransitionEnabled();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Transition Duration", this.settings.transitionDurationSeconds, 0.1F, 5.0F, value -> {
         this.settings.transitionDurationSeconds = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModeTransitionDurationSeconds());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Pixel Block Size", this.settings.transitionPixelBlockSizePx, 1.0F, 2.0F, value -> {
         this.settings.transitionPixelBlockSizePx = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModeTransitionPixelBlockSizePx());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Pixel Stagger", this.settings.transitionStaggerAmount, 0.0F, 1.0F, value -> {
         this.settings.transitionStaggerAmount = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModeTransitionStaggerAmount());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Pixel Fade Window", this.settings.transitionFadeWindow, 0.05F, 1.0F, value -> {
         this.settings.transitionFadeWindow = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModeTransitionFadeWindow());
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Transition Affects Pulses: " + onOff(this.settings.transitionAffectsPulses), () -> {
         this.settings.transitionAffectsPulses = !this.settings.transitionAffectsPulses;
         this.applyChangesImmediately();
      }, onOff(NanoGeoClientConfig.defaultArmorModeTransitionAffectsPulses()), () -> {
         this.settings.transitionAffectsPulses = NanoGeoClientConfig.defaultArmorModeTransitionAffectsPulses();
         this.applyChangesImmediately();
      });
      return rowY + 20 - y;
   }

   private int addHexPageWidgets(int x, int y, int width) {
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.FloatSlider(x, y, width, "Hex Cell Size", this.settings.hexCellSizePx, 4.0F, 128.0F, value -> {
         this.settings.hexCellSizePx = value;
         this.applyChangesImmediately();
      }));
      int rowY = y + 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Hex Frequency", this.settings.hexFrequency, 0.25F, 8.0F, value -> {
         this.settings.hexFrequency = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Hex Line Width", this.settings.hexLineWidthPx, 0.25F, 16.0F, value -> {
            this.settings.hexLineWidthPx = value;
            this.applyChangesImmediately();
         })
      );
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Hex Edge Softness", this.settings.hexEdgeSoftnessPx, 0.0F, 8.0F, value -> {
            this.settings.hexEdgeSoftnessPx = value;
            this.applyChangesImmediately();
         })
      );
      rowY += 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.IntSlider(x, rowY, width, "Hex Inner Alpha", this.settings.hexInnerAlpha, 0, 255, value -> {
         this.settings.hexInnerAlpha = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.IntSlider(x, rowY, width, "Hex Glow Alpha", this.settings.hexGlowAlpha, 0, 255, value -> {
         this.settings.hexGlowAlpha = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Hex Glow Boost", this.settings.hexGlowBoost, 0.0F, 2.0F, value -> {
         this.settings.hexGlowBoost = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Hex Scroll Speed", this.settings.hexScrollSpeedPxPerSecond, 0.0F, 64.0F, value -> {
            this.settings.hexScrollSpeedPxPerSecond = value;
            this.applyChangesImmediately();
         })
      );
      rowY += 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Hex Direction X", this.settings.hexDirectionX, -1.0F, 1.0F, value -> {
         this.settings.hexDirectionX = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Hex Direction Y", this.settings.hexDirectionY, -1.0F, 1.0F, value -> {
         this.settings.hexDirectionY = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(this.toggleButton(x, rowY, width, () -> "Hex UV Match: " + hexTexturePresetLabel(this.settings.hexTextureSizePx), () -> {
         this.settings.hexTextureSizePx = nextHexTextureSizePreset(this.settings.hexTextureSizePx);
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Hex Pattern Scale", this.settings.hexPatternScale, 0.25F, 8.0F, value -> {
            this.settings.hexPatternScale = value;
            this.applyChangesImmediately();
         })
      );
      rowY += 24;
      this.addPageWidget(this.toggleButton(x, rowY, width, () -> "Hex Debug: " + onOff(this.settings.debugHexPattern), () -> {
         this.settings.debugHexPattern = !this.settings.debugHexPattern;
         this.applyChangesImmediately();
      }));
      return rowY + 20 - y;
   }

   private int addSheenPageWidgets(int x, int y, int width) {
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, y, width, "Breathing Amount", this.settings.sheenBreathingAmount, 0.0F, 1.0F, value -> {
            this.settings.sheenBreathingAmount = value;
            this.applyChangesImmediately();
         })
      );
      int rowY = y + 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Breathing Speed", this.settings.sheenBreathingSpeed, 0.0F, 2.0F, value -> {
            this.settings.sheenBreathingSpeed = value;
            this.applyChangesImmediately();
         })
      );
      rowY += 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Sheen Alpha", this.settings.sheenAlpha, 0.0F, 2.0F, value -> {
         this.settings.sheenAlpha = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Sheen Transparency", this.settings.sheenTransparency, 0.0F, 1.0F, value -> {
            this.settings.sheenTransparency = value;
            this.applyChangesImmediately();
         })
      );
      rowY += 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Sheen Width", this.settings.sheenWidthPx, 4.0F, 256.0F, value -> {
         this.settings.sheenWidthPx = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Sheen Softness", this.settings.sheenSoftnessPx, 0.0F, 256.0F, value -> {
            this.settings.sheenSoftnessPx = value;
            this.applyChangesImmediately();
         })
      );
      rowY += 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Sheen Repeat", this.settings.sheenRepeatPx, 32.0F, 1024.0F, value -> {
         this.settings.sheenRepeatPx = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Sheen Speed", this.settings.sheenSpeedPxPerSecond, 0.0F, 128.0F, value -> {
            this.settings.sheenSpeedPxPerSecond = value;
            this.applyChangesImmediately();
         })
      );
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Sheen Direction X", this.settings.sheenDirectionX, -1.0F, 1.0F, value -> {
            this.settings.sheenDirectionX = value;
            this.applyChangesImmediately();
         })
      );
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Sheen Direction Y", this.settings.sheenDirectionY, -1.0F, 1.0F, value -> {
            this.settings.sheenDirectionY = value;
            this.applyChangesImmediately();
         })
      );
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Sheen Color Boost", this.settings.sheenColorBoost, 0.0F, 2.0F, value -> {
            this.settings.sheenColorBoost = value;
            this.applyChangesImmediately();
         })
      );
      rowY += 24;
      this.addPageWidget(this.toggleButton(x, rowY, width, () -> "Fiber Flow: " + onOff(this.settings.fiberFlowEnabled), () -> {
         this.settings.fiberFlowEnabled = !this.settings.fiberFlowEnabled;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Fiber Strength", this.settings.fiberFlowStrength, 0.0F, 1.0F, value -> {
            this.settings.fiberFlowStrength = value;
            this.applyChangesImmediately();
         })
      );
      rowY += 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Fiber Scale", this.settings.fiberFlowScale, 0.001F, 0.25F, value -> {
         this.settings.fiberFlowScale = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "Fiber Speed", this.settings.fiberFlowSpeed, 0.0F, 8.0F, value -> {
         this.settings.fiberFlowSpeed = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(this.toggleButton(x, rowY, width, () -> "Sheen Debug: " + onOff(this.settings.debugUniversalSheen), () -> {
         this.settings.debugUniversalSheen = !this.settings.debugUniversalSheen;
         this.applyChangesImmediately();
      }));
      return rowY + 20 - y;
   }

   private int addPlasmaPageWidgets(int x, int y, int width) {
      this.addSectionLabel(x, y, width, "Pattern");
      int rowY = y + 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.StaticTextWidget(this.font, x, rowY, this.mainControlWidth(width), 20, "Pattern Mode: CHAOTIC_PATCH_FLOW")
      );
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Texture Space: " + hexTexturePresetLabel(this.settings.hexTextureSizePx), () -> {
         this.settings.hexTextureSizePx = nextHexTextureSizePreset(this.settings.hexTextureSizePx);
         this.applyChangesImmediately();
      }, hexTexturePresetLabel(NanoGeoClientConfig.defaultArmorModeHexTextureSizePx()), () -> {
         this.settings.hexTextureSizePx = NanoGeoClientConfig.defaultArmorModeHexTextureSizePx();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addSectionLabel(x, rowY, width, "Patch Flow");
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Patch Flow Variant: " + patchFlowVariantLabel(this.settings.patchFlowVariant), () -> {
         this.settings.patchFlowVariant = nextPatchFlowVariant(this.settings.patchFlowVariant);
         this.applyChangesImmediately();
      }, patchFlowVariantLabel(NanoGeoClientConfig.defaultArmorModePatchFlowVariant()), () -> {
         this.settings.patchFlowVariant = NanoGeoClientConfig.defaultArmorModePatchFlowVariant();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addResettableIntSlider(x, rowY, width, "Patch Texture Alpha", this.settings.patchTextureAlpha, 0, 255, value -> {
         this.settings.patchTextureAlpha = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePatchTextureAlpha());
      rowY += 24;
      this.addResettableIntSlider(x, rowY, width, "Patch Spot Alpha", this.settings.patchSpotAlpha, 0, 255, value -> {
         this.settings.patchSpotAlpha = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePatchSpotAlpha());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Patch Min Size", this.settings.patchMinSizePx, 1.0F, 16.0F, value -> {
         this.settings.patchMinSizePx = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePatchMinSizePx());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Patch Max Size", this.settings.patchMaxSizePx, 1.0F, 16.0F, value -> {
         this.settings.patchMaxSizePx = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePatchMaxSizePx());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Patch Frequency", this.settings.patchFrequency, 0.1F, 6.0F, value -> {
         this.settings.patchFrequency = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePatchFrequency());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Patch Density", this.settings.patchDensity, 0.0F, 1.0F, value -> {
         this.settings.patchDensity = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePatchDensity());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Cloud Edge Softness", this.settings.patchCloudEdgeSoftness, 0.25F, 3.0F, value -> {
         this.settings.patchCloudEdgeSoftness = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePatchCloudEdgeSoftness());
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.StaticTextWidget(
            this.font, x, rowY, this.mainControlWidth(width), 20, "Chaotic cells or soft cloud blotches fade in-place, no UV travel."
         )
      );
      rowY += 24;
      this.addSectionLabel(x, rowY, width, "Pulse");
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Pulse Enabled: " + onOff(this.settings.pulseEnabled), () -> {
         this.settings.pulseEnabled = !this.settings.pulseEnabled;
         this.applyChangesImmediately();
      }, onOff(NanoGeoClientConfig.defaultArmorModePulseEnabled()), () -> {
         this.settings.pulseEnabled = NanoGeoClientConfig.defaultArmorModePulseEnabled();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Pulse Visual: " + pulseVisualModeLabel(this.settings.pulseVisualMode), () -> {
         this.settings.pulseVisualMode = nextPulseVisualMode(this.settings.pulseVisualMode);
         this.applyChangesImmediately();
      }, pulseVisualModeLabel(NanoGeoClientConfig.defaultArmorModePulseVisualMode()), () -> {
         this.settings.pulseVisualMode = NanoGeoClientConfig.defaultArmorModePulseVisualMode();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Compat Pulse Preset: " + compatPulsePresetLabel(this.settings.pulseCompatPreset), () -> {
         this.settings.pulseCompatPreset = nextCompatPulsePreset(this.settings.pulseCompatPreset);
         this.applyChangesImmediately();
      }, compatPulsePresetLabel(NanoGeoClientConfig.defaultArmorModeCompatPulsePreset()), () -> {
         this.settings.pulseCompatPreset = NanoGeoClientConfig.defaultArmorModeCompatPulsePreset();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.StaticTextWidget(
            this.font, x, rowY, this.mainControlWidth(width), 20, "Pulse Centers: " + NanoGeoArmorModePulseCenters.INSTANCE.centerCount() + " / 64"
         )
      );
      rowY += 24;
      this.addPageWidget(Button.builder(Component.literal("Update And Freeze Pulse Centers"), button -> {
         try {
            NanoGeoArmorModePulseCenterExtractor.ExtractionResult result = NanoGeoArmorModePulseCenterExtractor.extractAndWrite();
            button.setMessage(Component.literal("Frozen: " + result.centerCount() + " centers"));
            this.rebuildEditorWidgets();
         } catch (Exception var3x) {
            button.setMessage(Component.literal("Extraction failed"));
         }
      }).bounds(x, rowY, this.mainControlWidth(width), 20).build());
      rowY += 24;
      this.addResettableIntInputRow(x, rowY, width, "Max Centers", this.settings.pulseMaxCenters, 1, 64, value -> {
         this.settings.pulseMaxCenters = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseMaxCenters());
      rowY += 24;
      this.addResettableIntSlider(x, rowY, width, "Center Marker Alpha", this.settings.pulseCenterMarkerAlpha, 0, 255, value -> {
         this.settings.pulseCenterMarkerAlpha = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseCenterMarkerAlpha());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Pulse Interval", this.settings.pulseIntervalSeconds, 0.25F, 10.0F, value -> {
         this.settings.pulseIntervalSeconds = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseIntervalSeconds());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Pulse Duration", this.settings.pulseDurationSeconds, 0.25F, 5.0F, value -> {
         this.settings.pulseDurationSeconds = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseDurationSeconds());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Start Radius", Math.min(this.settings.pulseStartRadiusPx, 96.0F), 0.0F, 96.0F, value -> {
         this.settings.pulseStartRadiusPx = value;
         this.requestRuntimeApply();
      }, NanoGeoClientConfig.defaultArmorModePulseStartRadiusPx());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "End Radius", Math.min(this.settings.pulseEndRadiusPx, 96.0F), 4.0F, 96.0F, value -> {
         this.settings.pulseEndRadiusPx = value;
         this.requestRuntimeApply();
      }, NanoGeoClientConfig.defaultArmorModePulseEndRadiusPx());
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.StaticTextWidget(
            this.font, x, rowY, this.mainControlWidth(width), 20, "Radius changes apply after releasing the slider to avoid live atlas rebuild stalls."
         )
      );
      rowY += 24;
      this.addSectionLabel(x, rowY, width, "Pulse Visual");
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Core Width", this.settings.pulseCoreWidthPx, 0.01F, 4.0F, value -> {
         this.settings.pulseCoreWidthPx = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseCoreWidthPx());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Glow Radius", this.settings.pulseGlowRadiusPx, 0.0F, 8.0F, value -> {
         this.settings.pulseGlowRadiusPx = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseGlowRadiusPx());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Core Softness", this.settings.pulseCoreSoftnessPx, 0.0F, 4.0F, value -> {
         this.settings.pulseCoreSoftnessPx = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseCoreSoftnessPx());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Glow Softness", this.settings.pulseGlowSoftnessPx, 0.0F, 8.0F, value -> {
         this.settings.pulseGlowSoftnessPx = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseGlowSoftnessPx());
      rowY += 24;
      this.addResettableHexColorInputRow(x, rowY, width, "Core Color", this.settings.pulseCoreColor, value -> {
         this.settings.pulseCoreColor = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseCoreColor());
      rowY += 24;
      this.addResettableHexColorInputRow(x, rowY, width, "Glow Color", this.settings.pulseGlowColor, value -> {
         this.settings.pulseGlowColor = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseGlowColor());
      rowY += 24;
      this.addResettableIntSlider(x, rowY, width, "Core Alpha", this.settings.pulseCoreAlpha, 0, 255, value -> {
         this.settings.pulseCoreAlpha = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseCoreAlpha());
      rowY += 24;
      this.addResettableIntSlider(x, rowY, width, "Glow Alpha", this.settings.pulseGlowAlpha, 0, 255, value -> {
         this.settings.pulseGlowAlpha = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseGlowAlpha());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Brightness", this.settings.pulseBrightness, 0.0F, 4.0F, value -> {
         this.settings.pulseBrightness = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseBrightness());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Fade Power", this.settings.pulseFadePower, 0.25F, 6.0F, value -> {
         this.settings.pulseFadePower = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseFadePower());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Ring Distortion", this.settings.pulseRingDistortionPx, 0.0F, 16.0F, value -> {
         this.settings.pulseRingDistortionPx = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseRingDistortionPx());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Distortion Frequency", this.settings.pulseRingDistortionFrequency, 0.0F, 32.0F, value -> {
         this.settings.pulseRingDistortionFrequency = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseRingDistortionFrequency());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Energy Variation", this.settings.pulseRingEnergyVariation, 0.0F, 1.0F, value -> {
         this.settings.pulseRingEnergyVariation = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseRingEnergyVariation());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Width Variation", this.settings.pulseWidthVariation, 0.0F, 1.0F, value -> {
         this.settings.pulseWidthVariation = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseWidthVariation());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Brightness Variation", this.settings.pulseBrightnessVariation, 0.0F, 1.0F, value -> {
         this.settings.pulseBrightnessVariation = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseBrightnessVariation());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Edge Roughness", this.settings.pulseEdgeRoughness, 0.0F, 1.0F, value -> {
         this.settings.pulseEdgeRoughness = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulseEdgeRoughness());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Pattern Rotation Rand", this.settings.pulsePatternRotationRandomness, 0.0F, 1.0F, value -> {
         this.settings.pulsePatternRotationRandomness = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModePulsePatternRotationRandomness());
      rowY += 24;
      this.addSectionLabel(x, rowY, width, "Pulse Merging");
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Pulse Ring Merging: " + onOff(this.settings.pulseMergingEnabled), () -> {
         this.settings.pulseMergingEnabled = !this.settings.pulseMergingEnabled;
         this.applyChangesImmediately();
      }, onOff(NanoGeoClientConfig.defaultArmorModePulseMergingEnabled()), () -> {
         this.settings.pulseMergingEnabled = NanoGeoClientConfig.defaultArmorModePulseMergingEnabled();
         this.applyChangesImmediately();
      });
      rowY += 24;
      if (this.settings.pulseMergingEnabled) {
         this.addPageWidget(
            new ArmorModeOverlaySettingsScreen.StaticTextWidget(
               this.font, x, rowY, this.mainControlWidth(width), 20, "Touching contours collapse into one outer contour."
            )
         );
         rowY += 24;
      }

      this.addSectionLabel(x, rowY, width, "Debug");
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Pulse Force Visible: " + onOff(this.settings.pulseDebugForceVisible), () -> {
         this.settings.pulseDebugForceVisible = !this.settings.pulseDebugForceVisible;
         this.applyChangesImmediately();
      }, onOff(NanoGeoClientConfig.defaultArmorModePulseDebugForceVisible()), () -> {
         this.settings.pulseDebugForceVisible = NanoGeoClientConfig.defaultArmorModePulseDebugForceVisible();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Output Only Pulse: " + onOff(this.settings.pulseDebugOutputOnlyPulse), () -> {
         this.settings.pulseDebugOutputOnlyPulse = !this.settings.pulseDebugOutputOnlyPulse;
         this.applyChangesImmediately();
      }, onOff(NanoGeoClientConfig.defaultArmorModePulseDebugOutputOnlyPulse()), () -> {
         this.settings.pulseDebugOutputOnlyPulse = NanoGeoClientConfig.defaultArmorModePulseDebugOutputOnlyPulse();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Plasma Debug: " + onOff(this.settings.debugPlasmaField), () -> {
         this.settings.debugPlasmaField = !this.settings.debugPlasmaField;
         this.applyChangesImmediately();
      }, onOff(NanoGeoClientConfig.defaultDebugArmorModePlasmaField()), () -> {
         this.settings.debugPlasmaField = NanoGeoClientConfig.defaultDebugArmorModePlasmaField();
         this.applyChangesImmediately();
      });
      return rowY + 20 - y;
   }

   private int addCompatPageWidgets(int x, int y, int width) {
      this.addSectionLabel(x, y, width, "Compatibility");
      int rowY = y + 24;
      this.addResettableToggle(x, rowY, width, () -> "Overlay Backend: " + armorModeOverlayBackendLabel(this.settings.overlayBackend), () -> {
         this.settings.overlayBackend = nextArmorModeOverlayBackend(this.settings.overlayBackend);
         this.applyChangesImmediately();
      }, armorModeOverlayBackendLabel(NanoGeoClientConfig.defaultArmorModeOverlayBackend()), () -> {
         this.settings.overlayBackend = NanoGeoClientConfig.defaultArmorModeOverlayBackend();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Compat Performance: " + compatPerformancePresetLabel(this.settings.compatPerformancePreset), () -> {
         this.settings.compatPerformancePreset = nextCompatPerformancePreset(this.settings.compatPerformancePreset);
         this.applyChangesImmediately();
      }, compatPerformancePresetLabel(NanoGeoClientConfig.defaultArmorModeCompatPerformancePreset()), () -> {
         this.settings.compatPerformancePreset = NanoGeoClientConfig.defaultArmorModeCompatPerformancePreset();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Dynamic Compat Shell: " + onOff(this.settings.dynamicTextureCompatShellEnabled), () -> {
         this.settings.dynamicTextureCompatShellEnabled = !this.settings.dynamicTextureCompatShellEnabled;
         this.applyChangesImmediately();
      }, onOff(NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatShellEnabled()), () -> {
         this.settings.dynamicTextureCompatShellEnabled = NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatShellEnabled();
         this.applyChangesImmediately();
      });
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Dynamic Shell Scale", this.settings.dynamicTextureCompatShellScale, 0.0F, 0.02F, value -> {
         this.settings.dynamicTextureCompatShellScale = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatShellScale());
      rowY += 24;
      this.addResettableIntSlider(x, rowY, width, "Dynamic Max Resolution", this.settings.dynamicTextureCompatMaxResolution, 64, 1024, value -> {
         this.settings.dynamicTextureCompatMaxResolution = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatMaxResolution());
      rowY += 24;
      this.addResettableIntSlider(x, rowY, width, "Dynamic Update Interval (ms)", this.settings.dynamicTextureCompatUpdateIntervalMillis, 16, 250, value -> {
         this.settings.dynamicTextureCompatUpdateIntervalMillis = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatUpdateIntervalMillis());
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Dynamic Glow Strength", this.settings.dynamicTextureCompatGlowStrength, 0.0F, 1.5F, value -> {
         this.settings.dynamicTextureCompatGlowStrength = value;
         this.applyChangesImmediately();
      }, NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatGlowStrength());
      rowY += 24;
      this.addResettableToggle(x, rowY, width, () -> "Compat UV Coverage: " + onOff(this.settings.dynamicTextureCompatUvCoverageMaskEnabled), () -> {
         this.settings.dynamicTextureCompatUvCoverageMaskEnabled = !this.settings.dynamicTextureCompatUvCoverageMaskEnabled;
         this.applyChangesNow();
      }, onOff(NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatUvCoverageMaskEnabled()), () -> {
         this.settings.dynamicTextureCompatUvCoverageMaskEnabled = NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatUvCoverageMaskEnabled();
         this.applyChangesNow();
      });
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.StaticTextWidget(
            this.font, x, rowY, width, 32, "Preview stays active on the left. Backend/compat changes apply immediately."
         )
      );
      return rowY + 32 - y;
   }

   private int addXPageWidgets(int x, int y, int width) {
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.IntSlider(x, y, width, "X Strength", this.settings.xAlpha, 0, 255, value -> {
         this.settings.xAlpha = value;
         this.applyChangesImmediately();
      }));
      int rowY = y + 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.IntSlider(x, rowY, width, "X Size", this.settings.xSizePx, 1, 32, value -> {
         this.settings.xSizePx = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.IntSlider(x, rowY, width, "X Spacing", this.settings.xSpacingPx, 5, 64, value -> {
         this.settings.xSpacingPx = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "X Line Width", this.settings.xLineWidthPx, 0.25F, 8.0F, value -> {
         this.settings.xLineWidthPx = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "X Edge Softness", this.settings.xEdgeSoftnessPx, 0.0F, 8.0F, value -> {
            this.settings.xEdgeSoftnessPx = value;
            this.applyChangesImmediately();
         })
      );
      rowY += 24;
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "X Scroll Speed", this.settings.xSpeedPxPerSecond, 0.0F, 64.0F, value -> {
            this.settings.xSpeedPxPerSecond = value;
            this.applyChangesImmediately();
         })
      );
      rowY += 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "X Direction X", this.settings.xDirectionX, -1.0F, 1.0F, value -> {
         this.settings.xDirectionX = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.FloatSlider(x, rowY, width, "X Direction Y", this.settings.xDirectionY, -1.0F, 1.0F, value -> {
         this.settings.xDirectionY = value;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(this.toggleButton(x, rowY, width, () -> "X As Cutout: " + onOff(this.settings.xAsCutout), () -> {
         this.settings.xAsCutout = !this.settings.xAsCutout;
         this.applyChangesImmediately();
      }));
      rowY += 24;
      this.addPageWidget(this.toggleButton(x, rowY, width, () -> "X Debug: " + onOff(this.settings.debugXShimmer), () -> {
         this.settings.debugXShimmer = !this.settings.debugXShimmer;
         this.applyChangesImmediately();
      }));
      return rowY + 20 - y;
   }

   private int addPreviewPageWidgets(int x, int y, int width) {
      this.addSectionLabel(x, y, width, "Preview");
      int rowY = y + 24;
      this.addResettableToggle(x, rowY, width, () -> "Active Preview: " + this.currentPreviewPane.label, () -> {
         this.currentPreviewPane = this.currentPreviewPane.next();
         this.rebuildEditorWidgets();
      }, ArmorModeOverlaySettingsScreen.PreviewPane.MODEL.label, () -> this.currentPreviewPane = ArmorModeOverlaySettingsScreen.PreviewPane.MODEL);
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Model Zoom", this.modelPreviewZoom, 0.5F, 3.0F, value -> this.modelPreviewZoom = value, 1.0F);
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Texture Zoom", this.texturePreviewZoom, 0.5F, 8.0F, value -> this.texturePreviewZoom = value, 1.0F);
      rowY += 24;
      this.addResettableFloatSlider(x, rowY, width, "Pulse Zoom", this.pulsePreviewZoom, 0.5F, 8.0F, value -> this.pulsePreviewZoom = value, 1.0F);
      rowY += 24;
      this.addResettableToggle(
         x,
         rowY,
         width,
         () -> "Preview Quality: " + this.previewQuality.label,
         () -> this.previewQuality = this.previewQuality.next(),
         ArmorModeOverlaySettingsScreen.PreviewQuality.MEDIUM.label,
         () -> this.previewQuality = ArmorModeOverlaySettingsScreen.PreviewQuality.MEDIUM
      );
      rowY += 24;
      this.addResettableToggle(
         x,
         rowY,
         width,
         () -> "Model Preview: " + onOff(this.modelPreviewEnabled),
         () -> this.modelPreviewEnabled = !this.modelPreviewEnabled,
         onOff(true),
         () -> this.modelPreviewEnabled = true
      );
      rowY += 24;
      this.addResettableToggle(
         x,
         rowY,
         width,
         () -> "Texture Preview: " + onOff(this.texturePreviewEnabled),
         () -> this.texturePreviewEnabled = !this.texturePreviewEnabled,
         onOff(true),
         () -> this.texturePreviewEnabled = true
      );
      rowY += 24;
      this.addResettableToggle(
         x,
         rowY,
         width,
         () -> "Pulse Preview: " + onOff(this.pulsePreviewEnabled),
         () -> this.pulsePreviewEnabled = !this.pulsePreviewEnabled,
         onOff(true),
         () -> this.pulsePreviewEnabled = true
      );
      return rowY + 20 - y;
   }

   private <T extends AbstractWidget> void addPageWidget(T widget) {
      this.pageWidgets.add(new ArmorModeOverlaySettingsScreen.PageWidgetPlacement(widget, widget.getY()));
      this.addRenderableWidget(widget);
   }

   private void refreshPageWidgetPositions() {
      for (ArmorModeOverlaySettingsScreen.PageWidgetPlacement placement : this.pageWidgets) {
         AbstractWidget widget = placement.widget();
         int adjustedY = placement.baseY() - this.settingsScrollPx;
         widget.setY(adjustedY);
         boolean visible = adjustedY < this.settingsViewportBottom && adjustedY + widget.getHeight() > this.settingsViewportTop;
         widget.visible = visible;
         widget.active = visible;
      }
   }

   private void addIntInputRow(int x, int y, int width, String label, int value, int minValue, int maxValue, IntConsumer consumer) {
      int labelWidth = Mth.clamp(width / 2, 92, 132);
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.StaticTextWidget(this.font, x, y, labelWidth, 20, label));
      this.addPageWidget(
         new ArmorModeOverlaySettingsScreen.IntInputBox(this.font, x + labelWidth + 4, y, width - labelWidth - 4, value, minValue, maxValue, consumer)
      );
   }

   private void addHexColorInputRow(int x, int y, int width, String label, String value, Consumer<String> consumer) {
      int labelWidth = Mth.clamp(width / 2, 92, 132);
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.StaticTextWidget(this.font, x, y, labelWidth, 20, label));
      this.addPageWidget(new ArmorModeOverlaySettingsScreen.HexColorInputBox(this.font, x + labelWidth + 4, y, width - labelWidth - 4, value, consumer));
   }

   private void applyChangesImmediately() {
      this.requestRuntimeApply();
   }

   private void requestRuntimeApply() {
      this.pendingRuntimeApply = true;
      this.pendingRuntimeApplyAtMillis = Util.getMillis() + 180L;
   }

   private void applyChangesNow() {
      this.pendingRuntimeApply = false;
      this.settings.applyToRuntimeConfig();
   }

   private void processPendingRuntimeApply() {
      if (this.pendingRuntimeApply && !this.pointerDown) {
         if (Util.getMillis() >= this.pendingRuntimeApplyAtMillis) {
            this.applyChangesNow();
         }
      }
   }

   private Button toggleButton(int x, int y, int width, Supplier<String> labelSupplier, Runnable onClick) {
      Button[] ref = new Button[]{Button.builder(Component.literal(labelSupplier.get()), button -> {
         onClick.run();
         button.setMessage(Component.literal(labelSupplier.get()));
      }).bounds(x, y, width, 20).build()};
      return ref[0];
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.processPendingRuntimeApply();
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
      this.drawPanels(guiGraphics);
      this.drawActivePreview(guiGraphics, mouseX, mouseY);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
      guiGraphics.drawString(this.font, "Preview", this.leftPanelX + 10, this.leftPanelY - 14, 14737632, false);
      guiGraphics.drawString(this.font, "Settings", this.rightPanelX + 10, this.rightPanelY - 14, 14737632, false);
      String textureLabel = this.font.plainSubstrByWidth("Texture: " + this.overlayTexture, Math.max(40, this.rightPanelWidth - 20));
      guiGraphics.drawString(this.font, textureLabel, this.rightPanelX + 10, this.rightPanelY + this.rightPanelHeight - 14, 10526880, false);
      this.drawSettingsScrollbar(guiGraphics);
      if (this.settings.debugSettingsScreen || NanoGeoClientConfig.debugArmorModeSettingsScreen()) {
         int debugY = this.height - 54;
         guiGraphics.drawString(this.font, "Preview: " + this.settings.patternMode + " alpha=" + this.settings.overlayAlpha, 12, debugY, 16765823, false);
         guiGraphics.drawString(this.font, "Changes apply instantly to runtime config while the editor is open.", 12, debugY + 10, 16765823, false);
      }
   }

   private void drawSettingsScrollbar(GuiGraphics guiGraphics) {
      if (this.settingsMaxScrollPx > 0) {
         int trackX0 = this.rightPanelX + this.rightPanelWidth - 6;
         int trackX1 = this.rightPanelX + this.rightPanelWidth - 3;
         int trackY0 = this.settingsViewportTop;
         int trackY1 = this.settingsViewportBottom;
         int trackHeight = Math.max(1, trackY1 - trackY0);
         int thumbHeight = Math.max(20, Math.round((float)trackHeight * trackHeight / (trackHeight + this.settingsMaxScrollPx)));
         int thumbTravel = Math.max(1, trackHeight - thumbHeight);
         int thumbOffset = Math.round((float)this.settingsScrollPx / this.settingsMaxScrollPx * thumbTravel);
         guiGraphics.fill(trackX0, trackY0, trackX1, trackY1, 1882210383);
         guiGraphics.fill(trackX0, trackY0 + thumbOffset, trackX1, trackY0 + thumbOffset + thumbHeight, -1065371674);
      }
   }

   private void drawPanels(GuiGraphics guiGraphics) {
      this.fillPanel(guiGraphics, this.leftPanelX, this.leftPanelY, this.leftPanelWidth, this.leftPanelHeight);
      this.fillPanel(guiGraphics, this.rightPanelX, this.rightPanelY, this.rightPanelWidth, this.rightPanelHeight);
   }

   private void drawActivePreview(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      switch (this.currentPreviewPane) {
         case MODEL:
            this.drawModelPreview(guiGraphics, mouseX, mouseY);
            break;
         case TEXTURE:
            this.drawTexturePreview(guiGraphics);
            break;
         case PULSE:
            this.drawPulsePreview(guiGraphics);
      }
   }

   private void drawTexturePreview(GuiGraphics guiGraphics) {
      if (!this.texturePreviewEnabled) {
         this.drawPreviewDisabled(guiGraphics, "Texture preview disabled");
      } else {
         int previewX = this.leftPanelX + 10;
         int previewY = this.leftPanelY + 38;
         int previewW = this.leftPanelWidth - 20;
         int previewH = this.leftPanelHeight - 50;
         int previewSize = Math.min(previewW, previewH - 16);
         previewX += (previewW - previewSize) / 2;
         previewY += (previewH - previewSize) / 2 - 8;
         int previewLabelY = this.leftPanelY + this.leftPanelHeight - 14;
         guiGraphics.fill(previewX - 1, previewY - 1, previewX + previewSize + 1, previewY + previewSize + 1, -8676673);
         guiGraphics.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, -15920357);
         if (this.overlayTextureMissing) {
            guiGraphics.drawCenteredString(this.font, "Missing overlay texture", previewX + previewSize / 2, previewY + previewSize / 2 - 4, 16746632);
         } else {
            boolean dynamicCompatPreviewRequested = this.currentPage == ArmorModeOverlaySettingsScreen.Page.COMPAT
               || this.settings.overlayBackend == NanoGeoClientConfig.ArmorModeOverlayBackend.DYNAMIC_TEXTURE_COMPAT;
            ResourceLocation previewTexture = this.overlayTexture;
            boolean dynamicCompatPreview = false;
            boolean baseCompositePreview = false;
            if (dynamicCompatPreviewRequested) {
               float shimmerTimeTicks = (float)Util.getMillis() / 50.0F;
               baseCompositePreview = this.settings.compatPerformancePreset == NanoGeoClientConfig.ArmorModeCompatPerformancePreset.BASE_COMPOSITE_FAST;
               ResourceLocation generatedTexture = baseCompositePreview
                  ? NanoGeoArmorModeDynamicTextureCompatRenderer.prepareAndGetPreviewBaseCompositeTexture(shimmerTimeTicks)
                  : NanoGeoArmorModeDynamicTextureCompatRenderer.prepareAndGetPreviewBaseFlowTexture(shimmerTimeTicks);
               if (generatedTexture != null) {
                  previewTexture = generatedTexture;
                  dynamicCompatPreview = true;
               } else {
                  baseCompositePreview = false;
               }
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            float previewTextureAlpha = baseCompositePreview ? 1.0F : this.settings.overlayAlpha / 255.0F;
            if (!dynamicCompatPreview) {
               previewTextureAlpha *= this.settings.patchTextureAlpha / 255.0F;
            }

            float overlayBrightness = dynamicCompatPreview ? 1.0F : Mth.clamp(this.settings.overlayBrightness, 0.0F, 4.0F);
            RenderSystem.setShaderColor(overlayBrightness, overlayBrightness, overlayBrightness, previewTextureAlpha);
            guiGraphics.enableScissor(previewX, previewY, previewX + previewSize, previewY + previewSize);
            int scaledSize = Math.max(1, Math.round(previewSize * this.texturePreviewZoom));
            int scaledX = previewX + (previewSize - scaledSize) / 2;
            int scaledY = previewY + (previewSize - scaledSize) / 2;
            this.drawPreviewTextureQuad(guiGraphics, previewTexture, scaledX, scaledY, scaledSize, scaledSize);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            if (!dynamicCompatPreview) {
               this.drawChaoticPatchFlowPreview(guiGraphics, scaledX, scaledY, scaledSize, scaledSize);
            }

            guiGraphics.disableScissor();
            String previewLabel = baseCompositePreview
               ? "Live base-composite preview"
               : (dynamicCompatPreview ? "Live DynamicTexture preview" : "Texture preview");
            guiGraphics.drawCenteredString(this.font, previewLabel, previewX + previewSize / 2, previewLabelY, 11519192);
         }
      }
   }

   private void drawPreviewTextureQuad(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height) {
      Matrix4f pose = guiGraphics.pose().last().pose();
      RenderSystem.setShaderTexture(0, texture);
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      buffer.addVertex(pose, x, y + height, 0.0F).setUv(0.0F, 1.0F);
      buffer.addVertex(pose, x + width, y + height, 0.0F).setUv(1.0F, 1.0F);
      buffer.addVertex(pose, x + width, y, 0.0F).setUv(1.0F, 0.0F);
      buffer.addVertex(pose, x, y, 0.0F).setUv(0.0F, 0.0F);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
   }

   private void drawModelPreview(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      if (!this.modelPreviewEnabled) {
         this.drawPreviewDisabled(guiGraphics, "Model preview disabled");
      } else {
         int previewX = this.leftPanelX + 12;
         int previewY = this.leftPanelY + 40;
         int previewW = this.leftPanelWidth - 24;
         int previewH = this.leftPanelHeight - 52;
         guiGraphics.fill(previewX, previewY, previewX + previewW, previewY + previewH, -1877995504);
         guiGraphics.drawCenteredString(this.font, "Model preview", previewX + previewW / 2, previewY + 12, 13625343);
         LocalPlayer player = this.minecraft != null ? this.minecraft.player : null;
         if (player == null) {
            this.drawModelPreviewFallback(guiGraphics, previewX, previewY, previewW, previewH);
         } else {
            int previewScale = Math.round(Mth.clamp(Math.min(previewW, previewH) / 3.0F * this.modelPreviewZoom, 28.0F, this.previewQuality.modelScaleCap));
            int anchorX = previewX + previewW / 2;
            int anchorY = previewY + previewH - 20;
            float followX = this.previewQuality == ArmorModeOverlaySettingsScreen.PreviewQuality.HIGH ? (float)anchorX - mouseX : 0.0F;
            float followY = this.previewQuality == ArmorModeOverlaySettingsScreen.PreviewQuality.HIGH ? (float)(previewY + previewH / 2) - mouseY : 0.0F;
            guiGraphics.enableScissor(previewX + 2, previewY + 24, previewX + previewW - 2, previewY + previewH - 2);
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, previewX + 2, previewY + 24, previewX + previewW - 2, previewY + previewH - 2, previewScale, 0.0625F, anchorX - followX, (previewY + previewH / 2) - followY, player);
            guiGraphics.disableScissor();
            guiGraphics.drawCenteredString(this.font, "Inventory-style live player preview", previewX + previewW / 2, previewY + previewH - 16, 9414332);
         }
      }
   }

   private void drawModelPreviewFallback(GuiGraphics guiGraphics, int previewX, int previewY, int previewW, int previewH) {
      guiGraphics.drawCenteredString(this.font, "Local player unavailable", previewX + previewW / 2, previewY + 36, 16757683);
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(previewX + previewW / 2.0F - 32.0F, previewY + previewH / 2.0F - 28.0F, 0.0F);
      guiGraphics.pose().scale(4.0F, 4.0F, 1.0F);
      guiGraphics.renderItem(this.previewChestplate, 0, 0);
      guiGraphics.pose().popPose();
      guiGraphics.drawCenteredString(this.font, "Fallback preview item", previewX + previewW / 2, previewY + previewH - 28, 11519192);
      guiGraphics.drawCenteredString(this.font, "Open this screen in-game for the live model", previewX + previewW / 2, previewY + previewH - 16, 9414332);
   }

   private void drawPulsePreview(GuiGraphics guiGraphics) {
      if (!this.pulsePreviewEnabled) {
         this.drawPreviewDisabled(guiGraphics, "Pulse preview disabled");
      } else {
         int previewX = this.leftPanelX + 12;
         int previewY = this.leftPanelY + 40;
         int previewW = this.leftPanelWidth - 24;
         int previewH = this.leftPanelHeight - 52;
         guiGraphics.fill(previewX, previewY, previewX + previewW, previewY + previewH, -16052200);
         guiGraphics.drawCenteredString(this.font, "Pulse preview", previewX + previewW / 2, previewY + 12, 13625343);
         guiGraphics.enableScissor(previewX + 2, previewY + 24, previewX + previewW - 2, previewY + previewH - 2);
         guiGraphics.pose().pushPose();
         float zoom = this.pulsePreviewZoom;
         float centerX = previewX + previewW / 2.0F;
         float centerY = previewY + previewH / 2.0F + 10.0F;
         guiGraphics.pose().translate(centerX, centerY, 0.0F);
         guiGraphics.pose().scale(zoom, zoom, 1.0F);
         guiGraphics.pose().translate(-centerX, -centerY, 0.0F);
         this.drawPulseMapPreview(guiGraphics, previewX + 8, previewY + 28, previewW - 16, previewH - 40);
         guiGraphics.pose().popPose();
         guiGraphics.disableScissor();
         guiGraphics.drawCenteredString(
            this.font, "Runtime pulse centers over armor-mode UV texture", previewX + previewW / 2, previewY + previewH - 16, 9414332
         );
      }
   }

   private void drawPreviewDisabled(GuiGraphics guiGraphics, String message) {
      int previewX = this.leftPanelX + 12;
      int previewY = this.leftPanelY + 40;
      int previewW = this.leftPanelWidth - 24;
      int previewH = this.leftPanelHeight - 52;
      guiGraphics.fill(previewX, previewY, previewX + previewW, previewY + previewH, -1877995504);
      guiGraphics.drawCenteredString(this.font, message, previewX + previewW / 2, previewY + previewH / 2 - 4, 11579568);
   }

   private void drawXPatternPreview(GuiGraphics guiGraphics, int x, int y, int width, int height) {
      float scale = width / Math.max(this.settings.hexTextureSizePx, 1.0F);
      float time = (float)Util.getMillis() / 1000.0F;
      float spacing = Math.max(this.settings.xSpacingPx * scale, 8.0F);
      float size = Math.max(this.settings.xSizePx * scale, 4.0F);
      float offsetX = positiveModulo(this.settings.xDirectionX * this.settings.xSpeedPxPerSecond * time * scale, spacing);
      float offsetY = positiveModulo(this.settings.xDirectionY * this.settings.xSpeedPxPerSecond * time * scale, spacing);
      int glowColor = argb((int)(this.settings.xAlpha * (this.settings.overlayAlpha / 255.0F)), 140, 220, 255);
      float thickness = Math.max(1.0F, this.settings.xLineWidthPx * scale);

      for (float py = y - spacing; py < y + height + spacing; py += spacing) {
         for (float px = x - spacing; px < x + width + spacing; px += spacing) {
            float cx = px + offsetX;
            float cy = py + offsetY;
            this.drawThickLine(guiGraphics, cx - size * 0.5F, cy - size * 0.5F, cx + size * 0.5F, cy + size * 0.5F, thickness, glowColor);
            this.drawThickLine(guiGraphics, cx - size * 0.5F, cy + size * 0.5F, cx + size * 0.5F, cy - size * 0.5F, thickness, glowColor);
         }
      }
   }

   private void drawHexPatternPreview(GuiGraphics guiGraphics, int x, int y, int width, int height) {
      this.drawHexPatternPreview(guiGraphics, x, y, width, height, false);
   }

   private void drawHexPatternPreview(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean pixelLocked) {
      float scale = width / Math.max(this.settings.hexTextureSizePx, 1.0F);
      float frequency = Math.max(this.settings.hexFrequency, 0.25F);
      float patternScale = Math.max(this.settings.hexPatternScale, 0.25F);
      float radius = Math.max(this.settings.hexCellSizePx * patternScale / frequency * 0.5F * scale, 6.0F);
      if (pixelLocked) {
         radius = Math.max((float)Math.round(radius), 1.0F);
      }

      float dx = 1.7320508F * radius;
      float dy = 1.5F * radius;
      float time = (float)Util.getMillis() / 1000.0F;
      float scrollX = this.settings.hexDirectionX * this.settings.hexScrollSpeedPxPerSecond * time * scale;
      float scrollY = this.settings.hexDirectionY * this.settings.hexScrollSpeedPxPerSecond * time * scale;
      float startX = x - dx * 2.0F + positiveModulo(scrollX, dx);
      float startY = y - dy * 2.0F + positiveModulo(scrollY, dy);
      float lineWidth = Math.max(1.0F, this.settings.hexLineWidthPx * patternScale * scale);
      float softness = Math.max(0.0F, this.settings.hexEdgeSoftnessPx * patternScale * scale);
      if (pixelLocked) {
         lineWidth = Math.max(1.0F, (float)Math.round(lineWidth));
         softness = Math.round(softness);
      }

      int interiorMaskAlpha = Mth.clamp(Math.round((255.0F - this.settings.hexInnerAlpha) * (this.settings.overlayAlpha / 255.0F)), 0, 255);
      int glowAlpha = Mth.clamp(Math.round(this.settings.hexGlowAlpha * (this.settings.overlayAlpha / 255.0F)), 0, 255);
      int interiorColor = argb(interiorMaskAlpha, 13, 19, 27);
      int softGlowColor = argb(Math.round(glowAlpha * 0.35F), 96, 184, 235);
      int hardGlowColor = argb(glowAlpha, 120, 220, 255);
      int rowCount = (int)Math.ceil((height + dy * 4.0F) / dy);
      int colCount = (int)Math.ceil((width + dx * 4.0F) / dx);

      for (int row = -1; row <= rowCount; row++) {
         float cy = startY + row * dy;
         float rowOffset = (row & 1) == 0 ? 0.0F : dx * 0.5F;

         for (int col = -1; col <= colCount; col++) {
            float cx = startX + rowOffset + col * dx;
            if (!(cx < x - dx) && !(cx > x + width + dx) && !(cy < y - dy) && !(cy > y + height + dy)) {
               this.drawFilledHex(guiGraphics, cx, cy, Math.max(1.0F, radius - lineWidth * 0.9F), interiorColor);
               if (softness > 0.05F) {
                  this.drawHexOutline(guiGraphics, cx, cy, radius, lineWidth + softness, softGlowColor);
               }

               this.drawHexOutline(guiGraphics, cx, cy, radius, lineWidth, hardGlowColor);
            }
         }
      }
   }

   private void drawUniversalSheenPreview(GuiGraphics guiGraphics, int x, int y, int width, int height) {
      float textureScale = width / Math.max(this.settings.hexTextureSizePx, 1.0F);
      float timeSeconds = (float)Util.getMillis() / 1000.0F;
      float overlayAlphaFactor = this.settings.overlayAlpha / 255.0F;
      float breath = (0.5F + 0.5F * Mth.sin(timeSeconds * this.settings.sheenBreathingSpeed * (float) (Math.PI * 2))) * this.settings.sheenBreathingAmount;
      int breathAlpha = Mth.clamp(Math.round(overlayAlphaFactor * breath * 72.0F), 0, 96);
      guiGraphics.fill(x, y, x + width, y + height, argb(breathAlpha, 102, 220, 255));
      float directionX = this.settings.sheenDirectionX;
      float directionY = this.settings.sheenDirectionY;
      float directionLength = Mth.sqrt(directionX * directionX + directionY * directionY);
      if (directionLength < 0.001F) {
         directionX = 1.0F;
         directionY = 0.35F;
         directionLength = Mth.sqrt(directionX * directionX + directionY * directionY);
      }

      directionX /= directionLength;
      directionY /= directionLength;
      float perpendicularX = -directionY;
      float perpendicularY = directionX;
      float bandRepeat = Math.max(this.settings.sheenRepeatPx * textureScale, 24.0F);
      float bandWidth = Math.max(this.settings.sheenWidthPx * textureScale, 6.0F);
      float bandSoftness = Math.max(this.settings.sheenSoftnessPx * textureScale, 1.0F);
      float bandTravel = positiveModulo(timeSeconds * this.settings.sheenSpeedPxPerSecond * textureScale, bandRepeat);
      float centerX = x + width * 0.5F;
      float centerY = y + height * 0.5F;
      float span = Math.max(width, height) * 1.35F;
      int hardSheenColor = argb(Mth.clamp(Math.round(overlayAlphaFactor * this.settings.sheenAlpha * 72.0F), 0, 132), 164, 242, 255);
      int softSheenColor = argb(Mth.clamp(Math.round(overlayAlphaFactor * this.settings.sheenAlpha * 32.0F), 0, 72), 128, 220, 255);
      int hardCutoutColor = argb(Mth.clamp(Math.round(this.settings.sheenTransparency * 170.0F), 0, 200), 8, 14, 20);
      int softCutoutColor = argb(Mth.clamp(Math.round(this.settings.sheenTransparency * 90.0F), 0, 120), 8, 14, 20);

      for (int bandIndex = -2; bandIndex <= 2; bandIndex++) {
         float bandOffset = bandIndex * bandRepeat + bandTravel - bandRepeat * 0.5F;
         float cx = centerX + directionX * bandOffset;
         float cy = centerY + directionY * bandOffset;
         this.drawThickLine(
            guiGraphics,
            cx - perpendicularX * span,
            cy - perpendicularY * span,
            cx + perpendicularX * span,
            cy + perpendicularY * span,
            bandWidth + bandSoftness,
            softCutoutColor
         );
         this.drawThickLine(
            guiGraphics,
            cx - perpendicularX * span,
            cy - perpendicularY * span,
            cx + perpendicularX * span,
            cy + perpendicularY * span,
            bandWidth,
            hardCutoutColor
         );
         this.drawThickLine(
            guiGraphics,
            cx - perpendicularX * span,
            cy - perpendicularY * span,
            cx + perpendicularX * span,
            cy + perpendicularY * span,
            bandWidth + bandSoftness,
            softSheenColor
         );
         this.drawThickLine(
            guiGraphics,
            cx - perpendicularX * span,
            cy - perpendicularY * span,
            cx + perpendicularX * span,
            cy + perpendicularY * span,
            bandWidth,
            hardSheenColor
         );
      }

      if (this.settings.fiberFlowEnabled && this.settings.fiberFlowStrength > 1.0E-4F) {
         float fiberScale = Mth.clamp(this.settings.fiberFlowScale, 0.001F, 0.25F);
         float fiberSpacing = Mth.clamp(26.0F / fiberScale, 10.0F, width * 0.75F);
         float fiberTravel = timeSeconds * this.settings.fiberFlowSpeed * textureScale * 2.0F;
         int fiberColor = argb(Mth.clamp(Math.round(overlayAlphaFactor * this.settings.fiberFlowStrength * 68.0F), 0, 80), 122, 210, 245);

         for (float offset = -height; offset < width + height; offset += fiberSpacing) {
            float animatedOffset = positiveModulo(offset + fiberTravel, width + height + fiberSpacing) - fiberSpacing;
            this.drawThickLine(guiGraphics, x + animatedOffset, y + height, x + animatedOffset + height * 0.55F, y, 1.5F, fiberColor);
         }
      }
   }

   private void drawPlasmaFieldPreview(GuiGraphics guiGraphics, int x, int y, int width, int height) {
      float timeSeconds = this.previewTimeSeconds();
      float textureScale = width / Math.max(this.settings.hexTextureSizePx, 1.0F);
      float overlayAlphaFactor = this.settings.overlayAlpha / 255.0F;
      int blobCount = Mth.clamp(this.settings.plasmaBlobCount, 0, 512);
      float minRadius = Math.min(this.settings.plasmaBlobMinSizePx, this.settings.plasmaBlobMaxSizePx) * textureScale * 0.5F;
      float maxRadius = Math.max(this.settings.plasmaBlobMinSizePx, this.settings.plasmaBlobMaxSizePx) * textureScale * 0.5F;
      int blobCutAlpha = Mth.clamp(Math.round((255.0F - this.settings.plasmaBlobInnerAlpha) * overlayAlphaFactor), 0, 255);
      int blobRimAlpha = Mth.clamp(Math.round(this.settings.plasmaBlobRimAlpha * overlayAlphaFactor * this.settings.plasmaBlobRimBrightness), 0, 255);

      for (int i = 0; i < blobCount; i++) {
         float seed = i + 11.0F;
         ArmorModeOverlaySettingsScreen.PlasmaPreviewCell cell = this.plasmaPreviewCell(i, blobCount, x, y, width, height);
         float edgeSoftness = Math.max(this.settings.plasmaBlobEdgeSoftnessPx * textureScale, 1.0E-4F);
         float rimWidth = Math.max(this.settings.plasmaBlobRimWidthPx * textureScale, 1.0F);
         float safetyMargin = edgeSoftness + rimWidth + 1.0F;
         float maxAllowedRadius = Math.max(Math.min(cell.cellWidth(), cell.cellHeight()) * 0.5F - safetyMargin, 0.5F);
         float radius = Mth.clamp(Mth.lerp(hash01(seed * 5.29F + 2.0F), minRadius, maxRadius), 0.5F, maxAllowedRadius);
         float driftLimitX = Math.max(cell.cellWidth() * 0.5F - radius - safetyMargin, 0.0F);
         float driftLimitY = Math.max(cell.cellHeight() * 0.5F - radius - safetyMargin, 0.0F);
         float driftRangeScale = Math.max(this.settings.plasmaBlobDriftRangeScale, 0.0F);
         float driftPhase = timeSeconds * Math.max(this.settings.plasmaBlobDriftSpeedPxPerSecond, 0.0F) * 0.12F;
         float cx = cell.centerX() + Mth.sin(driftPhase + seed * 1.37F) * driftLimitX * driftRangeScale;
         float cy = cell.centerY() + Mth.cos(driftPhase * 0.91F + seed * 2.11F) * driftLimitY * driftRangeScale;
         this.drawPixelBlobDisc(guiGraphics, cx, cy, radius, argb(blobCutAlpha, 10, 16, 24));
         this.drawPixelBlobDiscRing(guiGraphics, cx, cy, radius, rimWidth + edgeSoftness * 0.6F, argb(Math.round(blobRimAlpha * 0.45F), 150, 224, 255));
         this.drawPixelBlobDiscRing(guiGraphics, cx, cy, radius, rimWidth, argb(blobRimAlpha, 188, 242, 255));
      }

      if (this.settings.pulseEnabled) {
         float intervalSeconds = Math.max(this.settings.pulseIntervalSeconds, 0.001F);
         float durationSeconds = Math.max(this.settings.pulseDurationSeconds, 0.001F);
         float baseGeneration = Mth.floor(timeSeconds / intervalSeconds);
         NanoGeoArmorModePulseCenterSet pulseCenters = NanoGeoArmorModePulseCenters.INSTANCE.centers();
         int centerCount = Math.min(
            Math.min(pulseCenters.centerCount(), this.previewQuality.maxPulsePreviewCenters), Math.max(0, Math.min(this.settings.pulseMaxCenters, 64))
         );
         float markerTextureWidth = Math.max(pulseCenters.textureWidth(), 1);
         float markerTextureHeight = Math.max(pulseCenters.textureHeight(), 1);
         if (this.settings.pulseMergingEnabled) {
            this.drawPulseMergePreview(
               guiGraphics,
               x,
               y,
               width,
               height,
               timeSeconds,
               textureScale,
               overlayAlphaFactor,
               pulseCenters,
               centerCount,
               markerTextureWidth,
               markerTextureHeight
            );
         } else {
            for (int waveIndex = 0; waveIndex < 3; waveIndex++) {
               float generation = baseGeneration - waveIndex;
               float triggerTime = generation * intervalSeconds;
               float age = timeSeconds - triggerTime;
               if (!(age < 0.0F) && !(age > durationSeconds)) {
                  float progress = Mth.clamp(age / durationSeconds, 0.0F, 1.0F);
                  float fade = (float)Math.pow(Math.max(1.0F - progress, 0.0F), Math.max(this.settings.pulseFadePower, 1.0E-4F));
                  float radius = Mth.lerp(progress, this.settings.pulseStartRadiusPx, this.settings.pulseEndRadiusPx) * textureScale;
                  float coreWidth = Math.max(this.settings.pulseCoreWidthPx * textureScale, 0.2F);
                  float glowWidth = Math.max(
                     (this.settings.pulseCoreWidthPx + this.settings.pulseGlowRadiusPx * 2.0F) * textureScale, Math.max(coreWidth, 0.35F)
                  );
                  int coreAlpha = Mth.clamp(Math.round(this.settings.pulseCoreAlpha * overlayAlphaFactor * fade * this.settings.pulseBrightness), 0, 255);
                  int glowAlpha = Mth.clamp(Math.round(this.settings.pulseGlowAlpha * overlayAlphaFactor * fade * this.settings.pulseBrightness), 0, 180);

                  for (int i = 0; i < centerCount; i++) {
                     NanoGeoArmorModePulseCenterSet.Center center = pulseCenters.centers().get(i);
                     float cx = x + center.x() / markerTextureWidth * width;
                     float cy = y + center.y() / markerTextureHeight * height;
                     if (this.settings.pulseVisualMode == NanoGeoClientConfig.ArmorModePulseVisualMode.SIMPLE_RING) {
                        this.drawClosedRing(guiGraphics, cx, cy, radius, glowWidth, this.pulseGlowPreviewColor(glowAlpha));
                        this.drawClosedRing(guiGraphics, cx, cy, radius, coreWidth, this.pulseCorePreviewColor(coreAlpha));
                     } else {
                        this.drawElectricClosedRing(
                           guiGraphics,
                           cx,
                           cy,
                           radius,
                           coreWidth,
                           glowWidth,
                           Math.max(this.settings.pulseCoreSoftnessPx * textureScale, 0.0F),
                           Math.max(this.settings.pulseGlowSoftnessPx * textureScale, 0.0F),
                           this.settings.pulseRingDistortionPx * textureScale,
                           this.settings.pulseRingDistortionFrequency,
                           this.settings.pulseRingEnergyVariation,
                           this.settings.pulseWidthVariation,
                           this.settings.pulseBrightnessVariation,
                           this.settings.pulseEdgeRoughness,
                           this.settings.pulsePatternRotationRandomness,
                           i * 12.9898F + waveIndex * 4.141F,
                           glowAlpha,
                           coreAlpha
                        );
                     }
                  }
               }
            }
         }
      }
   }

   private void drawPulseMapPreview(GuiGraphics guiGraphics, int x, int y, int width, int height) {
      NanoGeoArmorModePulseCenterSet pulseCenters = NanoGeoArmorModePulseCenters.INSTANCE.centers();
      int configuredCenters = Math.max(0, Math.min(this.settings.pulseMaxCenters, 64));
      int centerCount = Math.min(pulseCenters.centerCount(), configuredCenters);
      if (centerCount <= 0) {
         guiGraphics.drawCenteredString(this.font, "No pulse centers loaded", x + width / 2, y + height / 2 - 4, 16746632);
         this.drawPulseOnlyPreview(guiGraphics, x, y, width, height);
      } else {
         float markerTextureWidth = Math.max(pulseCenters.textureWidth(), 1);
         float markerTextureHeight = Math.max(pulseCenters.textureHeight(), 1);
         int labelHeight = 26;
         int availableHeight = Math.max(24, height - labelHeight);
         int previewSize = Math.max(16, Math.min(width, availableHeight));
         int previewX = x + (width - previewSize) / 2;
         int previewY = y + Math.max(0, (availableHeight - previewSize) / 2);
         float textureScale = previewSize / Math.max(markerTextureWidth, markerTextureHeight);
         float overlayAlphaFactor = this.settings.overlayAlpha / 255.0F;
         float timeSeconds = this.previewTimeSeconds();
         guiGraphics.fill(previewX - 1, previewY - 1, previewX + previewSize + 1, previewY + previewSize + 1, -8676673);
         guiGraphics.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, -16248808);
         if (!this.overlayTextureMissing) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.42F);
            this.drawPreviewTextureQuad(guiGraphics, this.overlayTexture, previewX, previewY, previewSize, previewSize);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
         }

         this.drawPulseCenterMarkers(
            guiGraphics, previewX, previewY, previewSize, previewSize, pulseCenters, centerCount, markerTextureWidth, markerTextureHeight
         );
         if (this.settings.pulseMergingEnabled) {
            this.drawPulseMergePreview(
               guiGraphics,
               previewX,
               previewY,
               previewSize,
               previewSize,
               timeSeconds,
               textureScale,
               overlayAlphaFactor,
               pulseCenters,
               centerCount,
               markerTextureWidth,
               markerTextureHeight
            );
         } else {
            this.drawPulseRingsForCenters(
               guiGraphics,
               previewX,
               previewY,
               previewSize,
               previewSize,
               timeSeconds,
               textureScale,
               overlayAlphaFactor,
               pulseCenters,
               centerCount,
               markerTextureWidth,
               markerTextureHeight
            );
         }

         float currentRadiusPx = this.currentPulsePreviewRadiusPx(timeSeconds);
         String info = "centers "
            + centerCount
            + "/"
            + pulseCenters.centerCount()
            + "  radius "
            + formatOneDecimal(currentRadiusPx)
            + "px  scale "
            + formatOneDecimal(textureScale)
            + "x";
         guiGraphics.drawCenteredString(this.font, info, x + width / 2, y + height - 22, 11519192);
         guiGraphics.drawCenteredString(this.font, "red dots = pulse centers, rings use current config", x + width / 2, y + height - 11, 7902378);
      }
   }

   private void drawPulseCenterMarkers(
      GuiGraphics guiGraphics,
      int x,
      int y,
      int width,
      int height,
      NanoGeoArmorModePulseCenterSet pulseCenters,
      int centerCount,
      float markerTextureWidth,
      float markerTextureHeight
   ) {
      for (int i = 0; i < centerCount; i++) {
         NanoGeoArmorModePulseCenterSet.Center center = pulseCenters.centers().get(i);
         int cx = Math.round(x + center.x() / markerTextureWidth * width);
         int cy = Math.round(y + center.y() / markerTextureHeight * height);
         float markerAlphaFactor = this.settings.pulseCenterMarkerAlpha / 255.0F;
         int outerAlpha = Mth.clamp(Math.round(208.0F * markerAlphaFactor), 0, 255);
         int innerAlpha = Mth.clamp(Math.round(255.0F * markerAlphaFactor), 0, 255);
         guiGraphics.fill(cx - 2, cy - 2, cx + 3, cy + 3, outerAlpha << 24 | 16724016);
         guiGraphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, innerAlpha << 24 | 16769248);
      }
   }

   private void drawPulseRingsForCenters(
      GuiGraphics guiGraphics,
      int x,
      int y,
      int width,
      int height,
      float timeSeconds,
      float textureScale,
      float overlayAlphaFactor,
      NanoGeoArmorModePulseCenterSet pulseCenters,
      int centerCount,
      float markerTextureWidth,
      float markerTextureHeight
   ) {
      float intervalSeconds = Math.max(this.settings.pulseIntervalSeconds, 0.001F);
      float durationSeconds = Math.max(this.settings.pulseDurationSeconds, 0.001F);
      float baseGeneration = Mth.floor(timeSeconds / intervalSeconds);

      for (int waveIndex = 0; waveIndex < 3; waveIndex++) {
         float generation = baseGeneration - waveIndex;
         float triggerTime = generation * intervalSeconds;
         float age = timeSeconds - triggerTime;
         if (!(age < 0.0F) && !(age > durationSeconds)) {
            float progress = Mth.clamp(age / durationSeconds, 0.0F, 1.0F);
            float fade = (float)Math.pow(Math.max(1.0F - progress, 0.0F), Math.max(this.settings.pulseFadePower, 1.0E-4F));
            float radius = Mth.lerp(progress, this.settings.pulseStartRadiusPx, this.settings.pulseEndRadiusPx) * textureScale;
            float coreWidth = Math.max(this.settings.pulseCoreWidthPx * textureScale, 0.2F);
            float glowWidth = Math.max((this.settings.pulseCoreWidthPx + this.settings.pulseGlowRadiusPx * 2.0F) * textureScale, Math.max(coreWidth, 0.35F));
            int coreAlpha = Mth.clamp(Math.round(this.settings.pulseCoreAlpha * overlayAlphaFactor * fade * this.settings.pulseBrightness), 0, 255);
            int glowAlpha = Mth.clamp(Math.round(this.settings.pulseGlowAlpha * overlayAlphaFactor * fade * this.settings.pulseBrightness), 0, 180);

            for (int i = 0; i < centerCount; i++) {
               NanoGeoArmorModePulseCenterSet.Center center = pulseCenters.centers().get(i);
               float cx = x + center.x() / markerTextureWidth * width;
               float cy = y + center.y() / markerTextureHeight * height;
               if (this.settings.pulseVisualMode == NanoGeoClientConfig.ArmorModePulseVisualMode.SIMPLE_RING) {
                  this.drawClosedRing(guiGraphics, cx, cy, radius, glowWidth, this.pulseGlowPreviewColor(glowAlpha));
                  this.drawClosedRing(guiGraphics, cx, cy, radius, coreWidth, this.pulseCorePreviewColor(coreAlpha));
               } else {
                  this.drawElectricClosedRing(
                     guiGraphics,
                     cx,
                     cy,
                     radius,
                     coreWidth,
                     glowWidth,
                     Math.max(this.settings.pulseCoreSoftnessPx * textureScale, 0.0F),
                     Math.max(this.settings.pulseGlowSoftnessPx * textureScale, 0.0F),
                     this.settings.pulseRingDistortionPx * textureScale,
                     this.settings.pulseRingDistortionFrequency,
                     this.settings.pulseRingEnergyVariation,
                     this.settings.pulseWidthVariation,
                     this.settings.pulseBrightnessVariation,
                     this.settings.pulseEdgeRoughness,
                     this.settings.pulsePatternRotationRandomness,
                     i * 12.9898F + waveIndex * 4.141F,
                     glowAlpha,
                     coreAlpha
                  );
               }
            }
         }
      }
   }

   private float currentPulsePreviewRadiusPx(float timeSeconds) {
      float intervalSeconds = Math.max(this.settings.pulseIntervalSeconds, 0.001F);
      float durationSeconds = Math.max(this.settings.pulseDurationSeconds, 0.001F);
      float baseGeneration = Mth.floor(timeSeconds / intervalSeconds);

      for (int waveIndex = 0; waveIndex < 3; waveIndex++) {
         float generation = baseGeneration - waveIndex;
         float triggerTime = generation * intervalSeconds;
         float age = timeSeconds - triggerTime;
         if (age >= 0.0F && age <= durationSeconds) {
            float progress = Mth.clamp(age / durationSeconds, 0.0F, 1.0F);
            return Mth.lerp(progress, this.settings.pulseStartRadiusPx, this.settings.pulseEndRadiusPx);
         }
      }

      return 0.0F;
   }

   private void drawChaoticPatchFlowPreview(GuiGraphics guiGraphics, int x, int y, int width, int height) {
      float timeSeconds = this.previewTimeSeconds();
      float textureScale = width / Math.max(this.settings.hexTextureSizePx, 1.0F);
      float overlayAlphaFactor = this.settings.overlayAlpha / 255.0F;
      float patchTextureAlpha = this.settings.patchTextureAlpha / 255.0F;
      float patchSpotAlpha = this.settings.patchSpotAlpha / 255.0F;
      int dimAlpha = Mth.clamp(Math.round((1.0F - patchTextureAlpha) * overlayAlphaFactor * 120.0F), 0, 120);
      if (dimAlpha > 0) {
         guiGraphics.fill(x, y, x + width, y + height, argb(dimAlpha, 5, 10, 16));
      }

      float minSize = Math.max(Math.min(this.settings.patchMinSizePx, this.settings.patchMaxSizePx), 1.0F);
      float maxSize = Math.max(Math.max(this.settings.patchMinSizePx, this.settings.patchMaxSizePx), minSize);
      float midSize = Mth.lerp(0.5F, minSize, maxSize);
      float patchFrequency = Math.max(this.settings.patchFrequency, 0.1F);
      float patchThreshold = 1.0F - Mth.clamp(this.settings.patchDensity, 0.0F, 1.0F);
      this.drawPatchPreviewLayer(
         guiGraphics,
         x,
         y,
         width,
         height,
         textureScale,
         timeSeconds,
         maxSize,
         patchFrequency * 0.78F,
         71.0F,
         patchThreshold,
         patchSpotAlpha,
         overlayAlphaFactor,
         this.settings.patchFlowVariant
      );
      this.drawPatchPreviewLayer(
         guiGraphics,
         x,
         y,
         width,
         height,
         textureScale,
         timeSeconds,
         midSize,
         patchFrequency * 0.97F,
         43.0F,
         patchThreshold,
         patchSpotAlpha,
         overlayAlphaFactor,
         this.settings.patchFlowVariant
      );
      this.drawPatchPreviewLayer(
         guiGraphics,
         x,
         y,
         width,
         height,
         textureScale,
         timeSeconds,
         minSize,
         patchFrequency * 1.18F,
         19.0F,
         patchThreshold,
         patchSpotAlpha,
         overlayAlphaFactor,
         this.settings.patchFlowVariant
      );
   }

   private void drawPatchPreviewLayer(
      GuiGraphics guiGraphics,
      int x,
      int y,
      int width,
      int height,
      float textureScale,
      float timeSeconds,
      float blockSizePx,
      float speed,
      float seed,
      float threshold,
      float patchSpotAlpha,
      float overlayAlphaFactor,
      NanoGeoClientConfig.ArmorModePatchFlowVariant patchFlowVariant
   ) {
      if (patchFlowVariant == NanoGeoClientConfig.ArmorModePatchFlowVariant.SOFT_CLOUD_ARCHETYPES) {
         float inverseTextureScale = 1.0F / Math.max(textureScale, 1.0E-4F);

         for (int sampleY = y; sampleY < y + height; sampleY++) {
            float patchY = (sampleY - y + 0.5F) * inverseTextureScale;

            for (int sampleX = x; sampleX < x + width; sampleX++) {
               float patchX = (sampleX - x + 0.5F) * inverseTextureScale;
               float activity = NanoGeoArmorModeDynamicTextureCompatRenderer.samplePatchLayerPreviewMask(
                  patchX, patchY, timeSeconds, blockSizePx, speed, seed, threshold, patchFlowVariant
               );
               if (!(activity <= 0.001F)) {
                  int alpha = Mth.clamp(Math.round(activity * patchSpotAlpha * overlayAlphaFactor * 170.0F), 0, 170);
                  if (alpha > 0) {
                     guiGraphics.fill(sampleX, sampleY, sampleX + 1, sampleY + 1, argb(alpha, 6, 10, 16));
                  }
               }
            }
         }
      } else {
         float scaledBlock = Math.max(blockSizePx * textureScale, 1.0F);
         int columns = Math.max(1, Mth.ceil(width / scaledBlock));
         int rows = Math.max(1, Mth.ceil(height / scaledBlock));

         for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
               float px = x + column * scaledBlock;
               float py = y + row * scaledBlock;
               float activity = patchFlowVariant == NanoGeoClientConfig.ArmorModePatchFlowVariant.FIXED_ARCHETYPE_LOOP
                  ? this.previewPatchLayerActivityFixedArchetype(column, row, timeSeconds, speed, seed, threshold)
                  : this.previewPatchLayerActivity(column, row, timeSeconds, speed, seed, threshold);
               if (!(activity <= 0.001F)) {
                  int alpha = Mth.clamp(Math.round(activity * patchSpotAlpha * overlayAlphaFactor * 170.0F), 0, 170);
                  if (alpha > 0) {
                     int x0 = Mth.floor(px);
                     int y0 = Mth.floor(py);
                     int x1 = Math.min(x + width, Mth.ceil(px + scaledBlock));
                     int y1 = Math.min(y + height, Mth.ceil(py + scaledBlock));
                     guiGraphics.fill(x0, y0, x1, y1, argb(alpha, 6, 10, 16));
                  }
               }
            }
         }
      }
   }

   private void drawPulseOnlyPreview(GuiGraphics guiGraphics, int x, int y, int width, int height) {
      float timeSeconds = this.previewTimeSeconds();
      float overlayAlphaFactor = this.settings.overlayAlpha / 255.0F;
      float textureScale = Math.min(width, height) / Math.max(this.settings.hexTextureSizePx, 1.0F);
      float intervalSeconds = Math.max(this.settings.pulseIntervalSeconds, 0.001F);
      float durationSeconds = Math.max(this.settings.pulseDurationSeconds, 0.001F);
      float baseGeneration = Mth.floor(timeSeconds / intervalSeconds);
      float cxA = x + width * 0.44F;
      float cyA = y + height * 0.52F;
      float cxB = x + width * 0.56F;
      float cyB = y + height * 0.48F;
      if (this.settings.pulseMergingEnabled) {
         this.drawPulseMergePreviewSynthetic(guiGraphics, x, y, width, height, timeSeconds, textureScale, overlayAlphaFactor, cxA, cyA, cxB, cyB);
      } else {
         int previewCenters = 1;

         for (int waveIndex = 0; waveIndex < 3; waveIndex++) {
            float generation = baseGeneration - waveIndex;
            float triggerTime = generation * intervalSeconds;
            float age = timeSeconds - triggerTime;
            if (!(age < 0.0F) && !(age > durationSeconds)) {
               float progress = Mth.clamp(age / durationSeconds, 0.0F, 1.0F);
               float fade = (float)Math.pow(Math.max(1.0F - progress, 0.0F), Math.max(this.settings.pulseFadePower, 1.0E-4F));
               float radius = Mth.lerp(progress, this.settings.pulseStartRadiusPx, this.settings.pulseEndRadiusPx) * textureScale;
               float coreWidth = Math.max(this.settings.pulseCoreWidthPx * textureScale, 0.2F);
               float glowWidth = Math.max((this.settings.pulseCoreWidthPx + this.settings.pulseGlowRadiusPx * 2.0F) * textureScale, Math.max(coreWidth, 0.35F));
               int coreAlpha = Mth.clamp(Math.round(this.settings.pulseCoreAlpha * overlayAlphaFactor * fade * this.settings.pulseBrightness), 0, 255);
               int glowAlpha = Mth.clamp(Math.round(this.settings.pulseGlowAlpha * overlayAlphaFactor * fade * this.settings.pulseBrightness), 0, 180);

               for (int i = 0; i < previewCenters; i++) {
                  float cx = i == 0 ? cxA : cxB;
                  float cy = i == 0 ? cyA : cyB;
                  if (this.settings.pulseVisualMode == NanoGeoClientConfig.ArmorModePulseVisualMode.SIMPLE_RING) {
                     this.drawClosedRing(guiGraphics, cx, cy, radius, glowWidth, this.pulseGlowPreviewColor(glowAlpha));
                     this.drawClosedRing(guiGraphics, cx, cy, radius, coreWidth, this.pulseCorePreviewColor(coreAlpha));
                  } else {
                     this.drawElectricClosedRing(
                        guiGraphics,
                        cx,
                        cy,
                        radius,
                        coreWidth,
                        glowWidth,
                        Math.max(this.settings.pulseCoreSoftnessPx * textureScale, 0.0F),
                        Math.max(this.settings.pulseGlowSoftnessPx * textureScale, 0.0F),
                        this.settings.pulseRingDistortionPx * textureScale,
                        this.settings.pulseRingDistortionFrequency,
                        this.settings.pulseRingEnergyVariation,
                        this.settings.pulseWidthVariation,
                        this.settings.pulseBrightnessVariation,
                        this.settings.pulseEdgeRoughness,
                        this.settings.pulsePatternRotationRandomness,
                        i * 12.9898F + waveIndex * 4.141F,
                        glowAlpha,
                        coreAlpha
                     );
                  }
               }
            }
         }
      }
   }

   private void drawPulseMergePreview(
      GuiGraphics guiGraphics,
      int x,
      int y,
      int width,
      int height,
      float timeSeconds,
      float textureScale,
      float overlayAlphaFactor,
      NanoGeoArmorModePulseCenterSet pulseCenters,
      int centerCount,
      float markerTextureWidth,
      float markerTextureHeight
   ) {
      if (centerCount > 1) {
         for (int i = 0; i < centerCount; i++) {
            NanoGeoArmorModePulseCenterSet.Center center = pulseCenters.centers().get(i);
            this.previewPulseCenterX[i] = x + center.x() / markerTextureWidth * width;
            this.previewPulseCenterY[i] = y + center.y() / markerTextureHeight * height;
         }

         this.drawMergedPulseContoursFromBuffer(guiGraphics, timeSeconds, textureScale, overlayAlphaFactor, centerCount);
      }
   }

   private void drawPulseMergePreviewSynthetic(
      GuiGraphics guiGraphics,
      int x,
      int y,
      int width,
      int height,
      float timeSeconds,
      float textureScale,
      float overlayAlphaFactor,
      float centerAX,
      float centerAY,
      float centerBX,
      float centerBY
   ) {
      this.previewPulseCenterX[0] = centerAX;
      this.previewPulseCenterY[0] = centerAY;
      this.previewPulseCenterX[1] = centerBX;
      this.previewPulseCenterY[1] = centerBY;
      this.drawMergedPulseContoursFromBuffer(guiGraphics, timeSeconds, textureScale, overlayAlphaFactor, 2);
   }

   private void drawMergedPulseContoursFromBuffer(GuiGraphics guiGraphics, float timeSeconds, float textureScale, float overlayAlphaFactor, int centerCount) {
      float intervalSeconds = Math.max(this.settings.pulseIntervalSeconds, 0.001F);
      float durationSeconds = Math.max(this.settings.pulseDurationSeconds, 0.001F);
      float baseGeneration = Mth.floor(timeSeconds / intervalSeconds);

      for (int waveIndex = 0; waveIndex < 3; waveIndex++) {
         float generation = baseGeneration - waveIndex;
         float triggerTime = generation * intervalSeconds;
         float age = timeSeconds - triggerTime;
         if (!(age < 0.0F) && !(age > durationSeconds)) {
            float progress = Mth.clamp(age / durationSeconds, 0.0F, 1.0F);
            float fade = (float)Math.pow(Math.max(1.0F - progress, 0.0F), Math.max(this.settings.pulseFadePower, 1.0E-4F));
            float radius = Mth.lerp(progress, this.settings.pulseStartRadiusPx, this.settings.pulseEndRadiusPx) * textureScale;
            int segments = Mth.clamp(Math.round(radius * 2.4F), 42, this.previewQuality == ArmorModeOverlaySettingsScreen.PreviewQuality.HIGH ? 120 : 88);

            for (int centerIndex = 0; centerIndex < centerCount; centerIndex++) {
               float cx = this.previewPulseCenterX[centerIndex];
               float cy = this.previewPulseCenterY[centerIndex];

               for (int segmentIndex = 0; segmentIndex < segments; segmentIndex++) {
                  float angleA = (float) (Math.PI * 2) * segmentIndex / segments;
                  float angleB = (float) (Math.PI * 2) * (segmentIndex + 1) / segments;
                  float angleMid = (angleA + angleB) * 0.5F;
                  float radiusA = this.previewPulseContourRadiusAtAngle(centerIndex, waveIndex, radius, angleA, textureScale);
                  float radiusB = this.previewPulseContourRadiusAtAngle(centerIndex, waveIndex, radius, angleB, textureScale);
                  float radiusMid = this.previewPulseContourRadiusAtAngle(centerIndex, waveIndex, radius, angleMid, textureScale);
                  float x0 = cx + Mth.cos(angleA) * radiusA;
                  float y0 = cy + Mth.sin(angleA) * radiusA;
                  float x1 = cx + Mth.cos(angleB) * radiusB;
                  float y1 = cy + Mth.sin(angleB) * radiusB;
                  float midX = cx + Mth.cos(angleMid) * radiusMid;
                  float midY = cy + Mth.sin(angleMid) * radiusMid;
                  ArmorModeOverlaySettingsScreen.PreviewPulseContribution contribution = this.previewMergedContributionAtPoint(
                     midX, midY, waveIndex, radius, fade, textureScale, overlayAlphaFactor, centerCount
                  );
                  if (contribution != null) {
                     if (contribution.glowAlpha() > 0.5F) {
                        this.drawThickLine(
                           guiGraphics,
                           x0,
                           y0,
                           x1,
                           y1,
                           contribution.glowWidth(),
                           this.pulseGlowPreviewColor(Mth.clamp(Math.round(contribution.glowAlpha()), 0, 180))
                        );
                     }

                     if (contribution.coreAlpha() > 0.5F) {
                        this.drawThickLine(
                           guiGraphics,
                           x0,
                           y0,
                           x1,
                           y1,
                           contribution.coreWidth(),
                           this.pulseCorePreviewColor(Mth.clamp(Math.round(contribution.coreAlpha()), 0, 255))
                        );
                     }
                  }
               }
            }
         }
      }
   }

   private ArmorModeOverlaySettingsScreen.PreviewPulseContribution previewMergedContributionAtPoint(
      float sampleX, float sampleY, int waveIndex, float radius, float fade, float textureScale, float overlayAlphaFactor, int centerCount
   ) {
      ArmorModeOverlaySettingsScreen.PreviewPulseContour active = null;

      for (int centerIndex = 0; centerIndex < centerCount; centerIndex++) {
         ArmorModeOverlaySettingsScreen.PreviewPulseContour contour = this.previewPulseContourAtPoint(
            sampleX,
            sampleY,
            centerIndex,
            waveIndex,
            radius,
            fade,
            textureScale,
            overlayAlphaFactor,
            this.previewPulseCenterX[centerIndex],
            this.previewPulseCenterY[centerIndex]
         );
         if (active == null || contour.signedDistance() < active.signedDistance()) {
            active = contour;
         }
      }

      if (active == null) {
         return null;
      } else {
         float unionDistance = Math.abs(active.signedDistance());
         float coreMask = 1.0F - Mth.clamp((unionDistance - active.coreWidth()) / Math.max(active.coreSoftness(), 1.0E-4F), 0.0F, 1.0F);
         float glowMask = 1.0F - Mth.clamp((unionDistance - active.glowWidth()) / Math.max(active.glowSoftness(), 1.0E-4F), 0.0F, 1.0F);
         float coreAlpha = active.coreAlpha() * coreMask;
         float glowAlpha = active.glowAlpha() * glowMask;
         return coreAlpha <= 0.25F && glowAlpha <= 0.25F
            ? null
            : new ArmorModeOverlaySettingsScreen.PreviewPulseContribution(active.coreWidth(), active.glowWidth(), coreAlpha, glowAlpha);
      }
   }

   private ArmorModeOverlaySettingsScreen.PreviewPulseContour previewPulseContourAtPoint(
      float sampleX,
      float sampleY,
      int centerIndex,
      int waveIndex,
      float radius,
      float fade,
      float textureScale,
      float overlayAlphaFactor,
      float centerX,
      float centerY
   ) {
      float dx = sampleX - centerX;
      float dy = sampleY - centerY;
      float distance = Mth.sqrt(dx * dx + dy * dy);
      float authoredCoreWidth = Math.max(this.settings.pulseCoreWidthPx * textureScale, 0.2F);
      float authoredGlowWidth = Math.max(
         (this.settings.pulseCoreWidthPx + this.settings.pulseGlowRadiusPx * 2.0F) * textureScale, Math.max(authoredCoreWidth, 0.35F)
      );
      float authoredCoreSoftness = Math.max(this.settings.pulseCoreSoftnessPx * textureScale, 0.25F);
      float authoredGlowSoftness = Math.max(this.settings.pulseGlowSoftnessPx * textureScale, 0.35F);
      float coreAlpha = Mth.clamp(this.settings.pulseCoreAlpha * overlayAlphaFactor * fade * this.settings.pulseBrightness, 0.0F, 255.0F);
      float glowAlpha = Mth.clamp(this.settings.pulseGlowAlpha * overlayAlphaFactor * fade * this.settings.pulseBrightness, 0.0F, 180.0F);
      float coreWidth = authoredCoreWidth;
      float glowWidth = authoredGlowWidth;
      float coreSoftness = authoredCoreSoftness;
      float glowSoftness = authoredGlowSoftness;
      float signedDistance;
      if (this.settings.pulseVisualMode == NanoGeoClientConfig.ArmorModePulseVisualMode.CRYSIS_ELECTRIC_RING) {
         float angle = (float)Math.atan2(dy, dx);
         float centerSeed = centerIndex * 12.9898F + waveIndex * 4.141F;
         float rotation = this.previewFractSin(centerSeed) * (float) (Math.PI * 2) * Mth.clamp(this.settings.pulsePatternRotationRandomness, 0.0F, 1.0F);
         float profileAngle = angle + rotation;
         float frequencyControl = Math.max(this.settings.pulseRingDistortionFrequency, 0.0F);
         float radiusNoise = this.previewPulsePeriodicElectricNoise(profileAngle, centerSeed, frequencyControl);
         float widthNoise = this.previewPulsePeriodicElectricNoise01(profileAngle + 1.7F, centerSeed + 3.1F, frequencyControl);
         float brightnessNoise = this.previewPulsePeriodicElectricNoise01(profileAngle + 3.1F, centerSeed + 5.7F, frequencyControl);
         float energyNoise = this.previewPulsePeriodicElectricNoise01(profileAngle + 7.1F, centerSeed + 11.3F, frequencyControl);
         float edgeNoise = this.previewPulsePeriodicElectricNoise01(profileAngle + 5.3F, centerSeed + 7.9F, frequencyControl);
         float shapeStability = this.previewPulseShapeStability(
            radius,
            Math.max(this.settings.pulseRingDistortionPx * textureScale, 0.0F),
            this.settings.pulseCoreWidthPx * textureScale,
            this.settings.pulseGlowRadiusPx * textureScale
         );
         float distortionStrength = Mth.lerp(shapeStability, 0.1F, 1.0F);
         float widthVariationStrength = Mth.clamp(this.settings.pulseWidthVariation, 0.0F, 1.0F) * Mth.lerp(shapeStability, 0.25F, 1.0F);
         float brightnessVariationStrength = Mth.clamp(this.settings.pulseBrightnessVariation, 0.0F, 1.0F) * Mth.lerp(shapeStability, 0.55F, 1.0F);
         float energyVariationStrength = Mth.clamp(this.settings.pulseRingEnergyVariation, 0.0F, 1.0F) * Mth.lerp(shapeStability, 0.6F, 1.0F);
         float roughness = Mth.clamp(this.settings.pulseEdgeRoughness, 0.0F, 1.0F) * Mth.lerp(shapeStability, 0.2F, 1.0F);
         float distortedRadius = radius + radiusNoise * Math.max(this.settings.pulseRingDistortionPx * textureScale, 0.0F) * distortionStrength;
         signedDistance = distance - distortedRadius;
         float localWidthMul = Mth.lerp(widthNoise, 1.0F - widthVariationStrength * 0.45F, 1.0F + widthVariationStrength * 0.85F);
         localWidthMul = Math.max(localWidthMul, 0.55F);
         coreWidth = Math.max(authoredCoreWidth * localWidthMul, 0.2F);
         glowWidth = Math.max(authoredGlowWidth * localWidthMul, Math.max(coreWidth, 0.35F));
         coreSoftness = Math.max(authoredCoreSoftness * Mth.lerp(edgeNoise * roughness + (1.0F - roughness) * 0.5F, 0.75F, 1.5F), 0.25F);
         glowSoftness = Math.max(authoredGlowSoftness * Mth.lerp(edgeNoise * roughness + (1.0F - roughness) * 0.5F, 0.8F, 1.8F), 0.35F);
         float localBrightness = Math.max(Mth.lerp(brightnessNoise, 1.0F - brightnessVariationStrength, 1.0F + brightnessVariationStrength), 0.35F);
         float localEnergy = Math.max(Mth.lerp(energyNoise, 1.0F - energyVariationStrength * 0.45F, 1.0F + energyVariationStrength * 0.55F), 0.55F);
         float localFactor = localBrightness * localEnergy;
         coreAlpha = Mth.clamp(coreAlpha * localFactor, 0.0F, 255.0F);
         glowAlpha = Mth.clamp(glowAlpha * localFactor, 0.0F, 180.0F);
      } else {
         signedDistance = distance - radius;
      }

      float ringDistance = Math.abs(signedDistance);
      float contourRadius = distance - signedDistance;
      return new ArmorModeOverlaySettingsScreen.PreviewPulseContour(
         signedDistance, ringDistance, contourRadius, coreWidth, glowWidth, coreSoftness, glowSoftness, coreAlpha, glowAlpha, centerX, centerY
      );
   }

   private float previewPulseContourRadiusAtAngle(int centerIndex, int waveIndex, float radius, float angle, float textureScale) {
      if (this.settings.pulseVisualMode != NanoGeoClientConfig.ArmorModePulseVisualMode.CRYSIS_ELECTRIC_RING) {
         return radius;
      } else {
         float centerSeed = centerIndex * 12.9898F + waveIndex * 4.141F;
         float rotation = this.previewFractSin(centerSeed) * (float) (Math.PI * 2) * Mth.clamp(this.settings.pulsePatternRotationRandomness, 0.0F, 1.0F);
         float profileAngle = angle + rotation;
         float frequencyControl = Math.max(this.settings.pulseRingDistortionFrequency, 0.0F);
         float radiusNoise = this.previewPulsePeriodicElectricNoise(profileAngle, centerSeed, frequencyControl);
         float shapeStability = this.previewPulseShapeStability(
            radius,
            Math.max(this.settings.pulseRingDistortionPx * textureScale, 0.0F),
            this.settings.pulseCoreWidthPx * textureScale,
            this.settings.pulseGlowRadiusPx * textureScale
         );
         float distortionStrength = Mth.lerp(shapeStability, 0.1F, 1.0F);
         return radius + radiusNoise * Math.max(this.settings.pulseRingDistortionPx * textureScale, 0.0F) * distortionStrength;
      }
   }

   private ArmorModeOverlaySettingsScreen.PlasmaPreviewCell plasmaPreviewCell(int index, int count, int x, int y, int width, int height) {
      int safeCount = Math.max(count, 1);
      int columns = Math.max(Mth.ceil(Mth.sqrt(safeCount)), 1);
      int rows = Math.max(Mth.ceil((float)safeCount / columns), 1);
      int fullRows = safeCount / columns;
      int remainder = safeCount - fullRows * columns;
      int row;
      int column;
      if (remainder > 0 && index >= fullRows * columns) {
         row = fullRows;
         column = (columns - remainder) / 2 + (index - fullRows * columns);
      } else {
         row = index / columns;
         column = index % columns;
      }

      float cellWidth = (float)width / columns;
      float cellHeight = (float)height / rows;
      float centerX = x + (column + 0.5F) * cellWidth;
      float centerY = y + (row + 0.5F) * cellHeight;
      return new ArmorModeOverlaySettingsScreen.PlasmaPreviewCell(centerX, centerY, cellWidth, cellHeight);
   }

   private void drawPixelBlobDisc(GuiGraphics guiGraphics, float cx, float cy, float radius, int argb) {
      if ((argb >>> 24 & 0xFF) > 0) {
         int pixelRadius = Math.max(1, Math.round(radius));
         int minX = Mth.floor(cx - pixelRadius - 1.0F);
         int maxX = Mth.ceil(cx + pixelRadius + 1.0F);
         int minY = Mth.floor(cy - pixelRadius - 1.0F);
         int maxY = Mth.ceil(cy + pixelRadius + 1.0F);

         for (int py = minY; py <= maxY; py++) {
            for (int px = minX; px <= maxX; px++) {
               float qx = (float)Math.floor(px + 0.5F - cx) + 0.5F;
               float qy = (float)Math.floor(py + 0.5F - cy) + 0.5F;
               if (Mth.sqrt(qx * qx + qy * qy) <= pixelRadius) {
                  guiGraphics.fill(px, py, px + 1, py + 1, argb);
               }
            }
         }
      }
   }

   private void drawPixelBlobDiscRing(GuiGraphics guiGraphics, float cx, float cy, float radius, float thickness, int argb) {
      if (!(thickness <= 0.1F) && (argb >>> 24 & 0xFF) > 0) {
         int pixelRadius = Math.max(1, Math.round(radius));
         float halfThickness = Math.max(thickness * 0.5F, 0.5F);
         int minX = Mth.floor(cx - pixelRadius - halfThickness - 1.0F);
         int maxX = Mth.ceil(cx + pixelRadius + halfThickness + 1.0F);
         int minY = Mth.floor(cy - pixelRadius - halfThickness - 1.0F);
         int maxY = Mth.ceil(cy + pixelRadius + halfThickness + 1.0F);

         for (int py = minY; py <= maxY; py++) {
            for (int px = minX; px <= maxX; px++) {
               float qx = (float)Math.floor(px + 0.5F - cx) + 0.5F;
               float qy = (float)Math.floor(py + 0.5F - cy) + 0.5F;
               float ringDistance = Math.abs(Mth.sqrt(qx * qx + qy * qy) - pixelRadius);
               if (ringDistance <= halfThickness) {
                  guiGraphics.fill(px, py, px + 1, py + 1, argb);
               }
            }
         }
      }
   }

   private void drawFilledHex(GuiGraphics guiGraphics, float cx, float cy, float radius, int argb) {
      if (!(radius <= 0.5F) && (argb >>> 24 & 0xFF) > 0) {
         Matrix4f pose = guiGraphics.pose().last().pose();
         float[][] points = hexPoints(cx, cy, radius);
         float[] color = unpackColor(argb);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         BufferBuilder buffer = Tesselator.getInstance().begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

         for (int i = 0; i < 6; i++) {
            int next = (i + 1) % 6;
            buffer.addVertex(pose, cx, cy, 0.0F).setColor(color[0], color[1], color[2], color[3]);
            buffer.addVertex(pose, points[i][0], points[i][1], 0.0F).setColor(color[0], color[1], color[2], color[3]);
            buffer.addVertex(pose, points[next][0], points[next][1], 0.0F).setColor(color[0], color[1], color[2], color[3]);
         }

         BufferUploader.drawWithShader(buffer.buildOrThrow());
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
      }
   }

   private void drawHexOutline(GuiGraphics guiGraphics, float cx, float cy, float radius, float thickness, int argb) {
      if (!(thickness <= 0.1F) && (argb >>> 24 & 0xFF) > 0) {
         float[][] points = hexPoints(cx, cy, radius);

         for (int i = 0; i < 6; i++) {
            int next = (i + 1) % 6;
            this.drawThickLine(guiGraphics, points[i][0], points[i][1], points[next][0], points[next][1], thickness, argb);
         }
      }
   }

   private void drawFilledCircle(GuiGraphics guiGraphics, float cx, float cy, float radius, int argb) {
      if (!(radius <= 0.5F) && (argb >>> 24 & 0xFF) > 0) {
         Matrix4f pose = guiGraphics.pose().last().pose();
         float[] color = unpackColor(argb);
         int segments = Math.max(20, Math.round(radius * 0.9F));
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         BufferBuilder buffer = Tesselator.getInstance().begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

         for (int i = 0; i < segments; i++) {
            float angleA = (float) (Math.PI * 2) * i / segments;
            float angleB = (float) (Math.PI * 2) * (i + 1) / segments;
            buffer.addVertex(pose, cx, cy, 0.0F).setColor(color[0], color[1], color[2], color[3]);
            buffer.addVertex(pose, cx + Mth.cos(angleA) * radius, cy + Mth.sin(angleA) * radius, 0.0F).setColor(color[0], color[1], color[2], color[3]);
            buffer.addVertex(pose, cx + Mth.cos(angleB) * radius, cy + Mth.sin(angleB) * radius, 0.0F).setColor(color[0], color[1], color[2], color[3]);
         }

         BufferUploader.drawWithShader(buffer.buildOrThrow());
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
      }
   }

   private void drawCircleApprox(
      GuiGraphics guiGraphics, float cx, float cy, float radius, float thickness, float coverage, float distortion, float seed, int argb
   ) {
      if (!(radius <= 0.5F) && !(thickness <= 0.1F) && (argb >>> 24 & 0xFF) > 0) {
         int segments = 48;
         float startX = cx + radius;
         boolean drawing = false;
         float previousX = startX;
         float previousY = cy;

         for (int i = 1; i <= segments; i++) {
            float angle = (float) (Math.PI * 2) * i / segments;
            float angularWave = 0.5F + 0.5F * Mth.sin(angle * (6.0F + Mth.floor(hash01(seed + 5.0F) * 4.0F)) + hash01(seed + 17.0F) * 17.0F);
            boolean visible = coverage >= 0.999F || angularWave >= 1.0F - coverage;
            float localRadius = radius + Mth.sin(angle * 5.0F + seed * 0.31F) * distortion * radius * 0.18F;
            float x = cx + Mth.cos(angle) * localRadius;
            float y = cy + Mth.sin(angle) * localRadius;
            if (visible && drawing) {
               this.drawThickLine(guiGraphics, previousX, previousY, x, y, thickness, argb);
            }

            drawing = visible;
            previousX = x;
            previousY = y;
         }
      }
   }

   private void drawClosedRing(GuiGraphics guiGraphics, float cx, float cy, float radius, float thickness, int argb) {
      if ((argb >>> 24 & 0xFF) > 0) {
         this.drawCircleApprox(guiGraphics, cx, cy, radius, thickness, 1.0F, 0.0F, 0.0F, argb);
      }
   }

   private void drawElectricClosedRing(
      GuiGraphics guiGraphics,
      float cx,
      float cy,
      float radius,
      float coreWidth,
      float glowWidth,
      float coreSoftness,
      float glowSoftness,
      float distortionPx,
      float distortionFrequency,
      float energyVariation,
      float widthVariation,
      float brightnessVariation,
      float edgeRoughness,
      float patternRotationRandomness,
      float seed,
      int glowAlpha,
      int coreAlpha
   ) {
      if ((glowAlpha > 0 || coreAlpha > 0) && !(radius <= 0.1F)) {
         int segments = Math.max(48, Math.round(radius * 2.4F));
         float rotation = this.previewFractSin(seed) * (float) (Math.PI * 2) * Mth.clamp(patternRotationRandomness, 0.0F, 1.0F);
         float roughness = Mth.clamp(edgeRoughness, 0.0F, 1.0F);
         float widthVariationClamped = Mth.clamp(widthVariation, 0.0F, 1.0F);
         float brightnessVariationClamped = Mth.clamp(brightnessVariation, 0.0F, 1.0F);
         float frequencyControl = Math.max(distortionFrequency, 0.0F);
         float shapeStability = this.previewPulseShapeStability(radius, Math.max(distortionPx, 0.0F), coreWidth, glowWidth);
         float distortionStrength = Mth.lerp(shapeStability, 0.1F, 1.0F);
         float widthVariationStrength = widthVariationClamped * Mth.lerp(shapeStability, 0.25F, 1.0F);
         float brightnessVariationStrength = brightnessVariationClamped * Mth.lerp(shapeStability, 0.55F, 1.0F);
         float energyVariationStrength = Mth.clamp(energyVariation, 0.0F, 1.0F) * Mth.lerp(shapeStability, 0.6F, 1.0F);
         float roughnessStrength = roughness * Mth.lerp(shapeStability, 0.2F, 1.0F);

         for (int i = 0; i < segments; i++) {
            float angleA = (float) (Math.PI * 2) * i / segments;
            float angleB = (float) (Math.PI * 2) * (i + 1) / segments;
            float midAngle = (angleA + angleB) * 0.5F + rotation;
            float radiusNoise = this.previewPulsePeriodicElectricNoise(midAngle, seed, frequencyControl);
            float widthNoise = this.previewPulsePeriodicElectricNoise01(midAngle + 1.7F, seed + 3.1F, frequencyControl);
            float brightnessNoise = this.previewPulsePeriodicElectricNoise01(midAngle + 3.1F, seed + 5.7F, frequencyControl);
            float edgeNoise = this.previewPulsePeriodicElectricNoise01(midAngle + 5.3F, seed + 7.9F, frequencyControl);
            float energyNoise = this.previewPulsePeriodicElectricNoise01(midAngle + 7.1F, seed + 11.3F, frequencyControl);
            float localRadius = radius + radiusNoise * Math.max(distortionPx, 0.0F) * distortionStrength;
            float localWidthMul = Mth.lerp(widthNoise, 1.0F - widthVariationStrength * 0.45F, 1.0F + widthVariationStrength * 0.85F);
            float localBrightness = Math.max(Mth.lerp(brightnessNoise, 1.0F - brightnessVariationStrength, 1.0F + brightnessVariationStrength), 0.35F);
            float localEnergy = Math.max(Mth.lerp(energyNoise, 1.0F - energyVariationStrength * 0.45F, 1.0F + energyVariationStrength * 0.55F), 0.55F);
            localBrightness = Math.max(localBrightness * localEnergy, 0.35F);
            float roughMix = edgeNoise * roughnessStrength + (1.0F - roughnessStrength) * 0.5F;
            float localCoreWidth = Math.max(coreWidth * localWidthMul, 0.2F);
            float localGlowWidth = Math.max(glowWidth * localWidthMul, Math.max(localCoreWidth, 0.35F));
            float localCoreSoftness = Math.max(coreSoftness * Mth.lerp(roughMix, 0.75F, 1.5F), 0.0F);
            float localGlowSoftness = Math.max(glowSoftness * Mth.lerp(roughMix, 0.8F, 1.8F), 0.0F);
            float x0 = cx + Mth.cos(angleA + rotation) * localRadius;
            float y0 = cy + Mth.sin(angleA + rotation) * localRadius;
            float x1 = cx + Mth.cos(angleB + rotation) * localRadius;
            float y1 = cy + Mth.sin(angleB + rotation) * localRadius;
            if (glowAlpha > 0) {
               int localGlowAlpha = Mth.clamp(Math.round(glowAlpha * localBrightness), 0, 255);
               this.drawThickLine(guiGraphics, x0, y0, x1, y1, localGlowWidth + localGlowSoftness, this.pulseGlowPreviewColor(localGlowAlpha));
            }

            if (coreAlpha > 0) {
               int localCoreAlpha = Mth.clamp(Math.round(coreAlpha * localBrightness), 0, 255);
               this.drawThickLine(guiGraphics, x0, y0, x1, y1, localCoreWidth + localCoreSoftness, this.pulseCorePreviewColor(localCoreAlpha));
            }
         }
      }
   }

   private float previewPulseElectricNoise(float angle, float seed, float frequencyScale) {
      float a = angle * frequencyScale;
      float n1 = Mth.sin(a * 5.0F + seed * 1.17F);
      float n2 = Mth.sin(a * 11.0F + seed * 2.31F);
      float n3 = Mth.sin(a * 19.0F - seed * 0.73F);
      return n1 * 0.5F + n2 * 0.3F + n3 * 0.2F;
   }

   private float previewPulseElectricNoise01(float angle, float seed, float frequencyScale) {
      return Mth.clamp(this.previewPulseElectricNoise(angle, seed, frequencyScale) * 0.5F + 0.5F, 0.0F, 1.0F);
   }

   private float previewPulsePeriodicElectricNoise(float angle, float seed, float frequencyControl) {
      float safeControl = Mth.clamp(Math.max(frequencyControl, 0.0F), 0.0F, 32.0F);
      float harmonicScale = 1.0F + safeControl / 8.0F;
      float lowScale = Math.max((float)Math.floor(harmonicScale), 1.0F);
      float highScale = Math.max(Math.min(lowScale + 1.0F, 5.0F), lowScale);
      float blend = Mth.clamp(harmonicScale - lowScale, 0.0F, 1.0F);
      float lowNoise = this.previewPulseElectricNoise(angle, seed, lowScale);
      float highNoise = this.previewPulseElectricNoise(angle, seed, highScale);
      return Mth.lerp(blend, lowNoise, highNoise);
   }

   private float previewPulsePeriodicElectricNoise01(float angle, float seed, float frequencyControl) {
      return Mth.clamp(this.previewPulsePeriodicElectricNoise(angle, seed, frequencyControl) * 0.5F + 0.5F, 0.0F, 1.0F);
   }

   private float previewPulseShapeStability(float radius, float distortionPx, float coreWidthPx, float glowRadiusPx) {
      float targetRadius = Math.max(distortionPx * 6.0F + coreWidthPx * 4.0F + glowRadiusPx * 3.0F, 2.0F);
      return Mth.clamp((radius - 0.0F) / Math.max(targetRadius, 1.0E-4F), 0.0F, 1.0F);
   }

   private float previewFractSin(float seed) {
      return (float)Mth.frac(Math.sin(seed) * 43758.5453);
   }

   private float previewPatchLayerActivity(int column, int row, float timeSeconds, float speed, float seed, float threshold) {
      float cellX = column + seed * 0.19F;
      float cellY = row + seed * 0.37F;
      float phaseOffset = hash01(cellX * 17.0F + cellY * 31.0F + seed * 1.31F) * 4.0F;
      float timeline = timeSeconds * speed + phaseOffset;
      float cycleIndex = Mth.floor(timeline);
      float cyclePhase = Mth.frac(timeline);
      float gate = hash01(cellX * 11.0F + cellY * 23.0F + seed + cycleIndex * 0.6180339F) >= threshold ? 1.0F : 0.0F;
      float strength = Mth.lerp(hash01(cellX * 7.0F + cellY * 13.0F + seed * 3.11F + cycleIndex * 0.113F), 0.72F, 1.0F);
      return gate * this.previewPatchCycleEnvelope(cyclePhase) * strength;
   }

   private float previewPatchLayerActivityFixedArchetype(int column, int row, float timeSeconds, float speed, float seed, float threshold) {
      float cellX = column + seed * 0.19F;
      float cellY = row + seed * 0.37F;
      float archetypeIndex = Mth.floor(hash01(cellX * 19.0F + cellY * 29.0F + seed * 2.47F) * 10.0F);
      float archetypeNormalized = (archetypeIndex + 0.5F) / 10.0F;
      float phaseOffset = hash01(cellX * 17.0F + cellY * 31.0F + seed * 1.31F);
      float timeline = timeSeconds * speed + phaseOffset;
      float cycleIndex = Mth.floor(timeline);
      float cyclePhase = Mth.frac(timeline);
      float density = Mth.clamp(1.0F - threshold, 0.0F, 1.0F);
      int activeCycleCount = Mth.clamp(Mth.floor(density * 10.0F + 0.5F), 0, 10);
      if (activeCycleCount <= 0) {
         return 0.0F;
      } else {
         int cycleSlot = (int)Mth.positiveModulo(cycleIndex + (int)archetypeIndex, 10.0F);
         float cycleMask = cycleSlot < activeCycleCount ? 1.0F : 0.0F;
         float strength = Mth.lerp(archetypeNormalized, 0.72F, 1.0F);
         return this.previewPatchCycleEnvelope(cyclePhase) * cycleMask * strength;
      }
   }

   private float previewPatchCycleEnvelope(float cyclePhase) {
      return Mth.clamp((cyclePhase - 0.05F) / 0.23F, 0.0F, 1.0F) * (1.0F - Mth.clamp((cyclePhase - 0.56F) / 0.4F, 0.0F, 1.0F));
   }

   private void drawThickLine(GuiGraphics guiGraphics, float x0, float y0, float x1, float y1, float thickness, int argb) {
      float dx = x1 - x0;
      float dy = y1 - y0;
      float length = (float)Math.sqrt(dx * dx + dy * dy);
      if (!(length < 0.001F)) {
         float nx = -dy / length;
         float ny = dx / length;
         float half = thickness * 0.5F;
         float ax = x0 + nx * half;
         float ay = y0 + ny * half;
         float bx = x0 - nx * half;
         float by = y0 - ny * half;
         float cx = x1 - nx * half;
         float cy = y1 - ny * half;
         float dxp = x1 + nx * half;
         float dyp = y1 + ny * half;
         Matrix4f pose = guiGraphics.pose().last().pose();
         float[] color = unpackColor(argb);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         BufferBuilder buffer = Tesselator.getInstance().begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
         buffer.addVertex(pose, ax, ay, 0.0F).setColor(color[0], color[1], color[2], color[3]);
         buffer.addVertex(pose, bx, by, 0.0F).setColor(color[0], color[1], color[2], color[3]);
         buffer.addVertex(pose, cx, cy, 0.0F).setColor(color[0], color[1], color[2], color[3]);
         buffer.addVertex(pose, ax, ay, 0.0F).setColor(color[0], color[1], color[2], color[3]);
         buffer.addVertex(pose, cx, cy, 0.0F).setColor(color[0], color[1], color[2], color[3]);
         buffer.addVertex(pose, dxp, dyp, 0.0F).setColor(color[0], color[1], color[2], color[3]);
         BufferUploader.drawWithShader(buffer.buildOrThrow());
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
      }
   }

   private static float[][] hexPoints(float cx, float cy, float radius) {
      float[][] points = new float[6][2];

      for (int i = 0; i < 6; i++) {
         double angle = Math.toRadians(60.0 * i - 30.0);
         points[i][0] = cx + (float)Math.cos(angle) * radius;
         points[i][1] = cy + (float)Math.sin(angle) * radius;
      }

      return points;
   }

   private void fillPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
      guiGraphics.fill(x, y, x + width, y + height, -1072689136);
      guiGraphics.fill(x, y, x + width, y + 1, -7631989);
      guiGraphics.fill(x, y + height - 1, x + width, y + height, -12961222);
      guiGraphics.fill(x, y, x + 1, y + height, -7631989);
      guiGraphics.fill(x + width - 1, y, x + width, y + height, -12961222);
   }

   private static int argb(int a, int r, int g, int b) {
      return Mth.clamp(a, 0, 255) << 24 | Mth.clamp(r, 0, 255) << 16 | Mth.clamp(g, 0, 255) << 8 | Mth.clamp(b, 0, 255);
   }

   private int pulseCorePreviewColor(int alpha) {
      return argbWithRgb(alpha, parseRgbHex(this.settings.pulseCoreColor, NanoGeoClientConfig.defaultArmorModePulseCoreColor()));
   }

   private int pulseGlowPreviewColor(int alpha) {
      return argbWithRgb(alpha, parseRgbHex(this.settings.pulseGlowColor, NanoGeoClientConfig.defaultArmorModePulseGlowColor()));
   }

   private static int argbWithRgb(int alpha, int rgb) {
      return argb(alpha, rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
   }

   private static int parseRgbHex(String value, String fallback) {
      String normalized = normalizeRgbHex(value, fallback);

      try {
         return Integer.parseInt(normalized.substring(1), 16) & 16777215;
      } catch (RuntimeException var4) {
         return 16777215;
      }
   }

   private static String normalizeRgbHex(String value, String fallback) {
      String fallbackNormalized = normalizeRgbHexInternal(fallback);
      String normalized = normalizeRgbHexInternal(value);
      return normalized == null ? fallbackNormalized : normalized;
   }

   private static String normalizeRgbHexInternal(String value) {
      if (value == null) {
         return null;
      } else {
         String trimmed = value.trim();
         if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
         }

         if (trimmed.length() != 6) {
            return null;
         } else {
            for (int i = 0; i < trimmed.length(); i++) {
               char c = trimmed.charAt(i);
               boolean hex = c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
               if (!hex) {
                  return null;
               }
            }

            return "#" + trimmed.toUpperCase(Locale.ROOT);
         }
      }
   }

   private static float[] unpackColor(int argb) {
      return new float[]{(argb >> 16 & 0xFF) / 255.0F, (argb >> 8 & 0xFF) / 255.0F, (argb & 0xFF) / 255.0F, (argb >>> 24 & 0xFF) / 255.0F};
   }

   private static float positiveModulo(float value, float mod) {
      float result = value % mod;
      return result < 0.0F ? result + mod : result;
   }

   private static float hash01(float value) {
      return Mth.frac(Mth.sin(value * 12.9898F) * 43758.547F);
   }

   private static String formatOneDecimal(float value) {
      return String.format(Locale.ROOT, "%.1f", value);
   }

   private float previewTimeSeconds() {
      long frameStepMillis = switch (this.previewQuality) {
         case LOW -> 66L;
         case MEDIUM -> 33L;
         case HIGH -> 16L;
      };
      long now = Util.getMillis();
      long quantized = frameStepMillis > 0L ? now / frameStepMillis * frameStepMillis : now;
      return (float)quantized / 1000.0F;
   }

   private static String onOff(boolean value) {
      return value ? "ON" : "OFF";
   }

   private static String hexTexturePresetLabel(float textureSizePx) {
      return NanoGeoClientConfig.ArmorModeHexTexturePreset.closestTo(textureSizePx) == NanoGeoClientConfig.ArmorModeHexTexturePreset.UV_1024
         ? "1024x1024"
         : "512x512";
   }

   private static float nextHexTextureSizePreset(float currentTextureSizePx) {
      return NanoGeoClientConfig.ArmorModeHexTexturePreset.closestTo(currentTextureSizePx) == NanoGeoClientConfig.ArmorModeHexTexturePreset.UV_512
         ? NanoGeoClientConfig.ArmorModeHexTexturePreset.UV_1024.textureSizePx()
         : NanoGeoClientConfig.ArmorModeHexTexturePreset.UV_512.textureSizePx();
   }

   private static String pulseVisualModeLabel(NanoGeoClientConfig.ArmorModePulseVisualMode mode) {
      return mode == NanoGeoClientConfig.ArmorModePulseVisualMode.SIMPLE_RING ? "SIMPLE_RING" : "CRYSIS_ELECTRIC_RING";
   }

   private static String compatPulsePresetLabel(NanoGeoClientConfig.ArmorModeCompatPulsePreset preset) {
      return preset == NanoGeoClientConfig.ArmorModeCompatPulsePreset.LEGACY_SMOOTH ? "LEGACY_SMOOTH" : "CRISP_MASKED";
   }

   private static String compatPerformancePresetLabel(NanoGeoClientConfig.ArmorModeCompatPerformancePreset preset) {
      return switch (preset) {
         case QUALITY_OPTIMIZED -> "Quality Optimized";
         case THIRD_PERSON_FAST -> "Third Person Fast";
         case COMPOSITE_FAST -> "Composite Fast";
         case BASE_COMPOSITE_FAST -> "Base Composite Fast (3P)";
         case UNSHADED_ARMOR_LAYER -> "Unshaded Armor Layer";
         case QUALITY -> "Quality";
      };
   }

   private static String patternModeLabel(NanoGeoClientConfig.ArmorModeOverlayPatternMode mode) {
      return "CHAOTIC_PATCH_FLOW";
   }

   private static String armorModeOverlayBackendLabel(NanoGeoClientConfig.ArmorModeOverlayBackend backend) {
      return switch (backend) {
         case AUTO -> "Auto";
         case CUSTOM_SHADER -> "Custom Shader";
         case TEXTURE_FALLBACK -> "Texture Fallback";
         case DYNAMIC_TEXTURE_COMPAT -> "Dynamic Compat";
         case DISABLED -> "Disabled";
      };
   }

   private static NanoGeoClientConfig.ArmorModeOverlayBackend nextArmorModeOverlayBackend(NanoGeoClientConfig.ArmorModeOverlayBackend backend) {
      return switch (backend) {
         case AUTO -> NanoGeoClientConfig.ArmorModeOverlayBackend.CUSTOM_SHADER;
         case CUSTOM_SHADER -> NanoGeoClientConfig.ArmorModeOverlayBackend.TEXTURE_FALLBACK;
         case TEXTURE_FALLBACK -> NanoGeoClientConfig.ArmorModeOverlayBackend.DYNAMIC_TEXTURE_COMPAT;
         case DYNAMIC_TEXTURE_COMPAT -> NanoGeoClientConfig.ArmorModeOverlayBackend.DISABLED;
         case DISABLED -> NanoGeoClientConfig.ArmorModeOverlayBackend.AUTO;
      };
   }

   private static String patchFlowVariantLabel(NanoGeoClientConfig.ArmorModePatchFlowVariant variant) {
      return switch (variant) {
         case DYNAMIC_RESEED -> "DYNAMIC_RESEED";
         case FIXED_ARCHETYPE_LOOP -> "FIXED_ARCHETYPE_LOOP";
         case SOFT_CLOUD_ARCHETYPES -> "SOFT_CLOUD_ARCHETYPES";
      };
   }

   private static NanoGeoClientConfig.ArmorModePatchFlowVariant nextPatchFlowVariant(NanoGeoClientConfig.ArmorModePatchFlowVariant currentVariant) {
      NanoGeoClientConfig.ArmorModePatchFlowVariant[] values = NanoGeoClientConfig.ArmorModePatchFlowVariant.values();
      return values[(currentVariant.ordinal() + 1) % values.length];
   }

   private static NanoGeoClientConfig.ArmorModePulseVisualMode nextPulseVisualMode(NanoGeoClientConfig.ArmorModePulseVisualMode currentMode) {
      return currentMode == NanoGeoClientConfig.ArmorModePulseVisualMode.SIMPLE_RING
         ? NanoGeoClientConfig.ArmorModePulseVisualMode.CRYSIS_ELECTRIC_RING
         : NanoGeoClientConfig.ArmorModePulseVisualMode.SIMPLE_RING;
   }

   private static NanoGeoClientConfig.ArmorModeCompatPulsePreset nextCompatPulsePreset(NanoGeoClientConfig.ArmorModeCompatPulsePreset preset) {
      return preset == NanoGeoClientConfig.ArmorModeCompatPulsePreset.CRISP_MASKED
         ? NanoGeoClientConfig.ArmorModeCompatPulsePreset.LEGACY_SMOOTH
         : NanoGeoClientConfig.ArmorModeCompatPulsePreset.CRISP_MASKED;
   }

   private static NanoGeoClientConfig.ArmorModeCompatPerformancePreset nextCompatPerformancePreset(NanoGeoClientConfig.ArmorModeCompatPerformancePreset preset) {
      return switch (preset) {
         case QUALITY_OPTIMIZED -> NanoGeoClientConfig.ArmorModeCompatPerformancePreset.THIRD_PERSON_FAST;
         case THIRD_PERSON_FAST -> NanoGeoClientConfig.ArmorModeCompatPerformancePreset.COMPOSITE_FAST;
         case COMPOSITE_FAST -> NanoGeoClientConfig.ArmorModeCompatPerformancePreset.BASE_COMPOSITE_FAST;
         case BASE_COMPOSITE_FAST -> NanoGeoClientConfig.ArmorModeCompatPerformancePreset.UNSHADED_ARMOR_LAYER;
         case UNSHADED_ARMOR_LAYER -> NanoGeoClientConfig.ArmorModeCompatPerformancePreset.QUALITY;
         case QUALITY -> NanoGeoClientConfig.ArmorModeCompatPerformancePreset.QUALITY_OPTIMIZED;
      };
   }

   public void onClose() {
      this.applyChangesNow();
      this.settings.saveToConfig();
      if (this.minecraft != null) {
         this.minecraft.setScreen(this.parent);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.pointerDown = true;
      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.pointerDown = false;
      boolean handled = super.mouseReleased(mouseX, mouseY, button);
      if (this.pendingRuntimeApply) {
         this.applyChangesNow();
      }

      return handled;
   }

   public boolean isPauseScreen() {
      return false;
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalDelta, double delta) {
      if (mouseX >= this.rightPanelX
         && mouseX <= this.rightPanelX + this.rightPanelWidth
         && mouseY >= this.settingsViewportTop
         && mouseY <= this.settingsViewportBottom
         && this.settingsMaxScrollPx > 0) {
         int nextScroll = Mth.clamp(this.settingsScrollPx - (int)Math.round(delta * 24.0), 0, this.settingsMaxScrollPx);
         if (nextScroll != this.settingsScrollPx) {
            this.settingsScrollPx = nextScroll;
            this.refreshPageWidgetPositions();
         }

         return true;
      } else if (mouseX >= this.leftPanelX
         && mouseX <= this.leftPanelX + this.leftPanelWidth
         && mouseY >= this.leftPanelY
         && mouseY <= this.leftPanelY + this.leftPanelHeight) {
         float step = delta > 0.0 ? 0.1F : -0.1F;
         switch (this.currentPreviewPane) {
            case MODEL:
               this.modelPreviewZoom = Mth.clamp(this.modelPreviewZoom + step, 0.5F, 3.0F);
               break;
            case TEXTURE:
               this.texturePreviewZoom = Mth.clamp(this.texturePreviewZoom + step, 0.5F, 8.0F);
               break;
            case PULSE:
               this.pulsePreviewZoom = Mth.clamp(this.pulsePreviewZoom + step, 0.5F, 8.0F);
         }

         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontalDelta, delta);
      }
   }

   private static final class EditableSettings {
      private int overlayAlpha;
      private float overlayBrightness;
      private float overlayTintR;
      private float overlayTintG;
      private float overlayTintB;
      private NanoGeoClientConfig.ArmorModeOverlayPatternMode patternMode;
      private NanoGeoClientConfig.ArmorModeOverlayBackend overlayBackend;
      private boolean fullbright;
      private boolean renderInFirstPerson;
      private boolean renderInThirdPerson;
      private boolean debugSettingsScreen;
      private boolean dynamicTextureCompatShellEnabled;
      private float dynamicTextureCompatShellScale;
      private int dynamicTextureCompatMaxResolution;
      private int dynamicTextureCompatUpdateIntervalMillis;
      private float dynamicTextureCompatGlowStrength;
      private boolean dynamicTextureCompatUvCoverageMaskEnabled;
      private NanoGeoClientConfig.ArmorModeCompatPerformancePreset compatPerformancePreset;
      private boolean transitionEnabled;
      private float transitionDurationSeconds;
      private float transitionPixelBlockSizePx;
      private float transitionStaggerAmount;
      private float transitionFadeWindow;
      private boolean transitionAffectsPulses;
      private int xAlpha;
      private int xSizePx;
      private int xSpacingPx;
      private float xLineWidthPx;
      private float xEdgeSoftnessPx;
      private float xSpeedPxPerSecond;
      private float xDirectionX;
      private float xDirectionY;
      private boolean xAsCutout;
      private boolean debugXShimmer;
      private float hexCellSizePx;
      private float hexLineWidthPx;
      private float hexEdgeSoftnessPx;
      private int hexInnerAlpha;
      private int hexGlowAlpha;
      private float hexFrequency;
      private float hexScrollSpeedPxPerSecond;
      private float hexDirectionX;
      private float hexDirectionY;
      private float hexTextureSizePx;
      private float hexPatternScale;
      private float hexGlowBoost;
      private float sheenBreathingAmount;
      private float sheenBreathingSpeed;
      private float sheenAlpha;
      private float sheenTransparency;
      private float sheenWidthPx;
      private float sheenSoftnessPx;
      private float sheenRepeatPx;
      private float sheenSpeedPxPerSecond;
      private float sheenDirectionX;
      private float sheenDirectionY;
      private float sheenColorBoost;
      private boolean fiberFlowEnabled;
      private float fiberFlowStrength;
      private float fiberFlowScale;
      private float fiberFlowSpeed;
      private boolean debugUniversalSheen;
      private int plasmaBlobInnerAlpha;
      private int plasmaBlobCount;
      private float plasmaBlobMinSizePx;
      private float plasmaBlobMaxSizePx;
      private float plasmaBlobEdgeSoftnessPx;
      private int plasmaBlobRimAlpha;
      private float plasmaBlobRimWidthPx;
      private float plasmaBlobRimBrightness;
      private float plasmaBlobDriftSpeedPxPerSecond;
      private float plasmaBlobDriftRangeScale;
      private float plasmaBlobShapeDistortion;
      private int patchTextureAlpha;
      private int patchSpotAlpha;
      private float patchMinSizePx;
      private float patchMaxSizePx;
      private float patchFrequency;
      private float patchDensity;
      private float patchCloudEdgeSoftness;
      private NanoGeoClientConfig.ArmorModePatchFlowVariant patchFlowVariant;
      private boolean pulseEnabled;
      private NanoGeoClientConfig.ArmorModePulseVisualMode pulseVisualMode;
      private NanoGeoClientConfig.ArmorModeCompatPulsePreset pulseCompatPreset;
      private int pulseMaxCenters;
      private int pulseCenterMarkerAlpha;
      private float pulseIntervalSeconds;
      private float pulseDurationSeconds;
      private float pulseStartRadiusPx;
      private float pulseEndRadiusPx;
      private float pulseCoreWidthPx;
      private float pulseGlowRadiusPx;
      private float pulseCoreSoftnessPx;
      private float pulseGlowSoftnessPx;
      private String pulseCoreColor;
      private String pulseGlowColor;
      private int pulseCoreAlpha;
      private int pulseGlowAlpha;
      private float pulseBrightness;
      private float pulseFadePower;
      private float pulseRingDistortionPx;
      private float pulseRingDistortionFrequency;
      private float pulseRingEnergyVariation;
      private float pulseWidthVariation;
      private float pulseBrightnessVariation;
      private float pulseEdgeRoughness;
      private float pulsePatternRotationRandomness;
      private boolean pulseMergingEnabled;
      private float pulseMergeStrength;
      private float pulseMergeSoftnessPx;
      private float pulseMergeBrightnessBoost;
      private boolean pulseDebugForceVisible;
      private boolean pulseDebugOutputOnlyPulse;
      private boolean debugPlasmaField;
      private boolean debugHexPattern;

      private static ArmorModeOverlaySettingsScreen.EditableSettings fromConfig() {
         ArmorModeOverlaySettingsScreen.EditableSettings data = new ArmorModeOverlaySettingsScreen.EditableSettings();
         data.overlayAlpha = NanoGeoClientConfig.armorModeOverlayAlpha();
         data.overlayBrightness = NanoGeoClientConfig.armorModeOverlayBrightness();
         data.overlayTintR = NanoGeoClientConfig.armorModeOverlayTintR();
         data.overlayTintG = NanoGeoClientConfig.armorModeOverlayTintG();
         data.overlayTintB = NanoGeoClientConfig.armorModeOverlayTintB();
         data.patternMode = NanoGeoClientConfig.armorModeOverlayPatternMode();
         data.overlayBackend = NanoGeoClientConfig.armorModeOverlayBackend();
         data.fullbright = NanoGeoClientConfig.armorModeOverlayFullbright();
         data.renderInFirstPerson = NanoGeoClientConfig.armorModeOverlayRenderInFirstPerson();
         data.renderInThirdPerson = NanoGeoClientConfig.armorModeOverlayRenderInThirdPerson();
         data.debugSettingsScreen = NanoGeoClientConfig.debugArmorModeSettingsScreen();
         data.dynamicTextureCompatShellEnabled = NanoGeoClientConfig.armorModeDynamicTextureCompatShellEnabled();
         data.dynamicTextureCompatShellScale = NanoGeoClientConfig.armorModeDynamicTextureCompatShellScale();
         data.dynamicTextureCompatMaxResolution = NanoGeoClientConfig.armorModeDynamicTextureCompatMaxResolution();
         data.dynamicTextureCompatUpdateIntervalMillis = NanoGeoClientConfig.armorModeDynamicTextureCompatUpdateIntervalMillis();
         data.dynamicTextureCompatGlowStrength = NanoGeoClientConfig.armorModeDynamicTextureCompatGlowStrength();
         data.dynamicTextureCompatUvCoverageMaskEnabled = NanoGeoClientConfig.armorModeDynamicTextureCompatUvCoverageMaskEnabled();
         data.compatPerformancePreset = NanoGeoClientConfig.armorModeCompatPerformancePreset();
         data.transitionEnabled = NanoGeoClientConfig.armorModeTransitionEnabled();
         data.transitionDurationSeconds = NanoGeoClientConfig.armorModeTransitionDurationSeconds();
         data.transitionPixelBlockSizePx = NanoGeoClientConfig.armorModeTransitionPixelBlockSizePx();
         data.transitionStaggerAmount = NanoGeoClientConfig.armorModeTransitionStaggerAmount();
         data.transitionFadeWindow = NanoGeoClientConfig.armorModeTransitionFadeWindow();
         data.transitionAffectsPulses = NanoGeoClientConfig.armorModeTransitionAffectsPulses();
         data.hexTextureSizePx = NanoGeoClientConfig.armorModeHexTextureSizePx();
         data.plasmaBlobInnerAlpha = NanoGeoClientConfig.armorModePlasmaBlobInnerAlpha();
         data.plasmaBlobCount = NanoGeoClientConfig.armorModePlasmaBlobCount();
         data.plasmaBlobMinSizePx = NanoGeoClientConfig.armorModePlasmaBlobMinSizePx();
         data.plasmaBlobMaxSizePx = NanoGeoClientConfig.armorModePlasmaBlobMaxSizePx();
         data.plasmaBlobEdgeSoftnessPx = NanoGeoClientConfig.armorModePlasmaBlobEdgeSoftnessPx();
         data.plasmaBlobRimAlpha = NanoGeoClientConfig.armorModePlasmaBlobRimAlpha();
         data.plasmaBlobRimWidthPx = NanoGeoClientConfig.armorModePlasmaBlobRimWidthPx();
         data.plasmaBlobRimBrightness = NanoGeoClientConfig.armorModePlasmaBlobRimBrightness();
         data.plasmaBlobDriftSpeedPxPerSecond = NanoGeoClientConfig.armorModePlasmaBlobDriftSpeedPxPerSecond();
         data.plasmaBlobDriftRangeScale = NanoGeoClientConfig.armorModePlasmaBlobDriftRangeScale();
         data.plasmaBlobShapeDistortion = NanoGeoClientConfig.armorModePlasmaBlobShapeDistortion();
         data.patchTextureAlpha = NanoGeoClientConfig.armorModePatchTextureAlpha();
         data.patchSpotAlpha = NanoGeoClientConfig.armorModePatchSpotAlpha();
         data.patchMinSizePx = NanoGeoClientConfig.armorModePatchMinSizePx();
         data.patchMaxSizePx = NanoGeoClientConfig.armorModePatchMaxSizePx();
         data.patchFrequency = NanoGeoClientConfig.armorModePatchFrequency();
         data.patchDensity = NanoGeoClientConfig.armorModePatchDensity();
         data.patchCloudEdgeSoftness = NanoGeoClientConfig.armorModePatchCloudEdgeSoftness();
         data.patchFlowVariant = NanoGeoClientConfig.armorModePatchFlowVariant();
         data.pulseEnabled = NanoGeoClientConfig.armorModePulseEnabled();
         data.pulseVisualMode = NanoGeoClientConfig.armorModePulseVisualMode();
         data.pulseCompatPreset = NanoGeoClientConfig.armorModeCompatPulsePreset();
         data.pulseMaxCenters = NanoGeoClientConfig.armorModePulseMaxCenters();
         data.pulseCenterMarkerAlpha = NanoGeoClientConfig.armorModePulseCenterMarkerAlpha();
         data.pulseIntervalSeconds = NanoGeoClientConfig.armorModePulseIntervalSeconds();
         data.pulseDurationSeconds = NanoGeoClientConfig.armorModePulseDurationSeconds();
         data.pulseStartRadiusPx = Math.min(NanoGeoClientConfig.armorModePulseStartRadiusPx(), 96.0F);
         data.pulseEndRadiusPx = Math.min(NanoGeoClientConfig.armorModePulseEndRadiusPx(), 96.0F);
         data.pulseCoreWidthPx = NanoGeoClientConfig.armorModePulseCoreWidthPx();
         data.pulseGlowRadiusPx = NanoGeoClientConfig.armorModePulseGlowRadiusPx();
         data.pulseCoreSoftnessPx = NanoGeoClientConfig.armorModePulseCoreSoftnessPx();
         data.pulseGlowSoftnessPx = NanoGeoClientConfig.armorModePulseGlowSoftnessPx();
         data.pulseCoreColor = NanoGeoClientConfig.armorModePulseCoreColor();
         data.pulseGlowColor = NanoGeoClientConfig.armorModePulseGlowColor();
         data.pulseCoreAlpha = NanoGeoClientConfig.armorModePulseCoreAlpha();
         data.pulseGlowAlpha = NanoGeoClientConfig.armorModePulseGlowAlpha();
         data.pulseBrightness = NanoGeoClientConfig.armorModePulseBrightness();
         data.pulseFadePower = NanoGeoClientConfig.armorModePulseFadePower();
         data.pulseRingDistortionPx = NanoGeoClientConfig.armorModePulseRingDistortionPx();
         data.pulseRingDistortionFrequency = NanoGeoClientConfig.armorModePulseRingDistortionFrequency();
         data.pulseRingEnergyVariation = NanoGeoClientConfig.armorModePulseRingEnergyVariation();
         data.pulseWidthVariation = NanoGeoClientConfig.armorModePulseWidthVariation();
         data.pulseBrightnessVariation = NanoGeoClientConfig.armorModePulseBrightnessVariation();
         data.pulseEdgeRoughness = NanoGeoClientConfig.armorModePulseEdgeRoughness();
         data.pulsePatternRotationRandomness = NanoGeoClientConfig.armorModePulsePatternRotationRandomness();
         data.pulseMergingEnabled = NanoGeoClientConfig.armorModePulseMergingEnabled();
         data.pulseMergeStrength = NanoGeoClientConfig.armorModePulseMergeStrength();
         data.pulseMergeSoftnessPx = NanoGeoClientConfig.armorModePulseMergeSoftnessPx();
         data.pulseMergeBrightnessBoost = NanoGeoClientConfig.armorModePulseMergeBrightnessBoost();
         data.pulseDebugForceVisible = NanoGeoClientConfig.armorModePulseDebugForceVisible();
         data.pulseDebugOutputOnlyPulse = NanoGeoClientConfig.armorModePulseDebugOutputOnlyPulse();
         data.debugPlasmaField = NanoGeoClientConfig.debugArmorModePlasmaField();
         return data;
      }

      private void saveToConfig() {
         this.applyToRuntimeConfig();
         NanoGeoClientConfig.saveArmorModeOverlayEditorConfig();
      }

      private void applyToRuntimeConfig() {
         NanoGeoClientConfig.setArmorModeOverlayAlpha(this.overlayAlpha, false);
         NanoGeoClientConfig.setArmorModeOverlayBrightness(this.overlayBrightness, false);
         NanoGeoClientConfig.setArmorModeOverlayTintR(this.overlayTintR, false);
         NanoGeoClientConfig.setArmorModeOverlayTintG(this.overlayTintG, false);
         NanoGeoClientConfig.setArmorModeOverlayTintB(this.overlayTintB, false);
         NanoGeoClientConfig.setArmorModeOverlayPatternMode(this.patternMode, false);
         NanoGeoClientConfig.setArmorModeOverlayBackend(this.overlayBackend, false);
         NanoGeoClientConfig.setArmorModeOverlayFullbright(this.fullbright, false);
         NanoGeoClientConfig.setArmorModeOverlayRenderInFirstPerson(this.renderInFirstPerson, false);
         NanoGeoClientConfig.setArmorModeOverlayRenderInThirdPerson(this.renderInThirdPerson, false);
         NanoGeoClientConfig.setDebugArmorModeSettingsScreen(this.debugSettingsScreen, false);
         NanoGeoClientConfig.setArmorModeDynamicTextureCompatShellEnabled(this.dynamicTextureCompatShellEnabled, false);
         NanoGeoClientConfig.setArmorModeDynamicTextureCompatShellScale(this.dynamicTextureCompatShellScale, false);
         NanoGeoClientConfig.setArmorModeDynamicTextureCompatMaxResolution(this.dynamicTextureCompatMaxResolution, false);
         NanoGeoClientConfig.setArmorModeDynamicTextureCompatUpdateIntervalMillis(this.dynamicTextureCompatUpdateIntervalMillis, false);
         NanoGeoClientConfig.setArmorModeDynamicTextureCompatGlowStrength(this.dynamicTextureCompatGlowStrength, false);
         NanoGeoClientConfig.setArmorModeDynamicTextureCompatUvCoverageMaskEnabled(this.dynamicTextureCompatUvCoverageMaskEnabled, false);
         NanoGeoClientConfig.setArmorModeCompatPerformancePreset(this.compatPerformancePreset, false);
         NanoGeoClientConfig.setArmorModeTransitionEnabled(this.transitionEnabled, false);
         NanoGeoClientConfig.setArmorModeTransitionDurationSeconds(this.transitionDurationSeconds, false);
         NanoGeoClientConfig.setArmorModeTransitionPixelBlockSizePx(this.transitionPixelBlockSizePx, false);
         NanoGeoClientConfig.setArmorModeTransitionStaggerAmount(this.transitionStaggerAmount, false);
         NanoGeoClientConfig.setArmorModeTransitionFadeWindow(this.transitionFadeWindow, false);
         NanoGeoClientConfig.setArmorModeTransitionAffectsPulses(this.transitionAffectsPulses, false);
         NanoGeoClientConfig.setArmorModeHexTextureSizePx(this.hexTextureSizePx, false);
         NanoGeoClientConfig.setArmorModePlasmaBlobInnerAlpha(this.plasmaBlobInnerAlpha, false);
         NanoGeoClientConfig.setArmorModePlasmaBlobCount(this.plasmaBlobCount, false);
         NanoGeoClientConfig.setArmorModePlasmaBlobMinSizePx(this.plasmaBlobMinSizePx, false);
         NanoGeoClientConfig.setArmorModePlasmaBlobMaxSizePx(this.plasmaBlobMaxSizePx, false);
         NanoGeoClientConfig.setArmorModePlasmaBlobEdgeSoftnessPx(this.plasmaBlobEdgeSoftnessPx, false);
         NanoGeoClientConfig.setArmorModePlasmaBlobRimAlpha(this.plasmaBlobRimAlpha, false);
         NanoGeoClientConfig.setArmorModePlasmaBlobRimWidthPx(this.plasmaBlobRimWidthPx, false);
         NanoGeoClientConfig.setArmorModePlasmaBlobRimBrightness(this.plasmaBlobRimBrightness, false);
         NanoGeoClientConfig.setArmorModePlasmaBlobDriftSpeedPxPerSecond(this.plasmaBlobDriftSpeedPxPerSecond, false);
         NanoGeoClientConfig.setArmorModePlasmaBlobDriftRangeScale(this.plasmaBlobDriftRangeScale, false);
         NanoGeoClientConfig.setArmorModePlasmaBlobShapeDistortion(this.plasmaBlobShapeDistortion, false);
         NanoGeoClientConfig.setArmorModePatchTextureAlpha(this.patchTextureAlpha, false);
         NanoGeoClientConfig.setArmorModePatchSpotAlpha(this.patchSpotAlpha, false);
         NanoGeoClientConfig.setArmorModePatchMinSizePx(this.patchMinSizePx, false);
         NanoGeoClientConfig.setArmorModePatchMaxSizePx(this.patchMaxSizePx, false);
         NanoGeoClientConfig.setArmorModePatchFrequency(this.patchFrequency, false);
         NanoGeoClientConfig.setArmorModePatchDensity(this.patchDensity, false);
         NanoGeoClientConfig.setArmorModePatchCloudEdgeSoftness(this.patchCloudEdgeSoftness, false);
         NanoGeoClientConfig.setArmorModePatchFlowVariant(this.patchFlowVariant, false);
         NanoGeoClientConfig.setArmorModePulseEnabled(this.pulseEnabled, false);
         NanoGeoClientConfig.setArmorModePulseVisualMode(this.pulseVisualMode, false);
         NanoGeoClientConfig.setArmorModeCompatPulsePreset(this.pulseCompatPreset, false);
         NanoGeoClientConfig.setArmorModePulseMaxCenters(this.pulseMaxCenters, false);
         NanoGeoClientConfig.setArmorModePulseCenterMarkerAlpha(this.pulseCenterMarkerAlpha, false);
         NanoGeoClientConfig.setArmorModePulseIntervalSeconds(this.pulseIntervalSeconds, false);
         NanoGeoClientConfig.setArmorModePulseDurationSeconds(this.pulseDurationSeconds, false);
         NanoGeoClientConfig.setArmorModePulseStartRadiusPx(this.pulseStartRadiusPx, false);
         NanoGeoClientConfig.setArmorModePulseEndRadiusPx(this.pulseEndRadiusPx, false);
         NanoGeoClientConfig.setArmorModePulseCoreWidthPx(this.pulseCoreWidthPx, false);
         NanoGeoClientConfig.setArmorModePulseGlowRadiusPx(this.pulseGlowRadiusPx, false);
         NanoGeoClientConfig.setArmorModePulseCoreSoftnessPx(this.pulseCoreSoftnessPx, false);
         NanoGeoClientConfig.setArmorModePulseGlowSoftnessPx(this.pulseGlowSoftnessPx, false);
         NanoGeoClientConfig.setArmorModePulseCoreColor(this.pulseCoreColor, false);
         NanoGeoClientConfig.setArmorModePulseGlowColor(this.pulseGlowColor, false);
         NanoGeoClientConfig.setArmorModePulseCoreAlpha(this.pulseCoreAlpha, false);
         NanoGeoClientConfig.setArmorModePulseGlowAlpha(this.pulseGlowAlpha, false);
         NanoGeoClientConfig.setArmorModePulseBrightness(this.pulseBrightness, false);
         NanoGeoClientConfig.setArmorModePulseFadePower(this.pulseFadePower, false);
         NanoGeoClientConfig.setArmorModePulseRingDistortionPx(this.pulseRingDistortionPx, false);
         NanoGeoClientConfig.setArmorModePulseRingDistortionFrequency(this.pulseRingDistortionFrequency, false);
         NanoGeoClientConfig.setArmorModePulseRingEnergyVariation(this.pulseRingEnergyVariation, false);
         NanoGeoClientConfig.setArmorModePulseWidthVariation(this.pulseWidthVariation, false);
         NanoGeoClientConfig.setArmorModePulseBrightnessVariation(this.pulseBrightnessVariation, false);
         NanoGeoClientConfig.setArmorModePulseEdgeRoughness(this.pulseEdgeRoughness, false);
         NanoGeoClientConfig.setArmorModePulsePatternRotationRandomness(this.pulsePatternRotationRandomness, false);
         NanoGeoClientConfig.setArmorModePulseMergingEnabled(this.pulseMergingEnabled, false);
         NanoGeoClientConfig.setArmorModePulseMergeStrength(this.pulseMergeStrength, false);
         NanoGeoClientConfig.setArmorModePulseMergeSoftnessPx(this.pulseMergeSoftnessPx, false);
         NanoGeoClientConfig.setArmorModePulseMergeBrightnessBoost(this.pulseMergeBrightnessBoost, false);
         NanoGeoClientConfig.setArmorModePulseDebugForceVisible(this.pulseDebugForceVisible, false);
         NanoGeoClientConfig.setArmorModePulseDebugOutputOnlyPulse(this.pulseDebugOutputOnlyPulse, false);
         NanoGeoClientConfig.setDebugArmorModePlasmaField(this.debugPlasmaField, false);
      }

      private void resetToDefaults() {
         this.overlayAlpha = NanoGeoClientConfig.defaultArmorModeOverlayAlpha();
         this.overlayBrightness = NanoGeoClientConfig.defaultArmorModeOverlayBrightness();
         this.overlayTintR = NanoGeoClientConfig.defaultArmorModeOverlayTintR();
         this.overlayTintG = NanoGeoClientConfig.defaultArmorModeOverlayTintG();
         this.overlayTintB = NanoGeoClientConfig.defaultArmorModeOverlayTintB();
         this.patternMode = NanoGeoClientConfig.defaultArmorModeOverlayPatternMode();
         this.overlayBackend = NanoGeoClientConfig.defaultArmorModeOverlayBackend();
         this.fullbright = NanoGeoClientConfig.defaultArmorModeOverlayFullbright();
         this.renderInFirstPerson = NanoGeoClientConfig.defaultArmorModeOverlayRenderInFirstPerson();
         this.renderInThirdPerson = NanoGeoClientConfig.defaultArmorModeOverlayRenderInThirdPerson();
         this.debugSettingsScreen = NanoGeoClientConfig.defaultDebugArmorModeSettingsScreen();
         this.dynamicTextureCompatShellEnabled = NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatShellEnabled();
         this.dynamicTextureCompatShellScale = NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatShellScale();
         this.dynamicTextureCompatMaxResolution = NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatMaxResolution();
         this.dynamicTextureCompatUpdateIntervalMillis = NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatUpdateIntervalMillis();
         this.dynamicTextureCompatGlowStrength = NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatGlowStrength();
         this.dynamicTextureCompatUvCoverageMaskEnabled = NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatUvCoverageMaskEnabled();
         this.compatPerformancePreset = NanoGeoClientConfig.defaultArmorModeCompatPerformancePreset();
         this.transitionEnabled = NanoGeoClientConfig.defaultArmorModeTransitionEnabled();
         this.transitionDurationSeconds = NanoGeoClientConfig.defaultArmorModeTransitionDurationSeconds();
         this.transitionPixelBlockSizePx = NanoGeoClientConfig.defaultArmorModeTransitionPixelBlockSizePx();
         this.transitionStaggerAmount = NanoGeoClientConfig.defaultArmorModeTransitionStaggerAmount();
         this.transitionFadeWindow = NanoGeoClientConfig.defaultArmorModeTransitionFadeWindow();
         this.transitionAffectsPulses = NanoGeoClientConfig.defaultArmorModeTransitionAffectsPulses();
         this.hexTextureSizePx = NanoGeoClientConfig.defaultArmorModeHexTextureSizePx();
         this.plasmaBlobInnerAlpha = NanoGeoClientConfig.defaultArmorModePlasmaBlobInnerAlpha();
         this.plasmaBlobCount = NanoGeoClientConfig.defaultArmorModePlasmaBlobCount();
         this.plasmaBlobMinSizePx = NanoGeoClientConfig.defaultArmorModePlasmaBlobMinSizePx();
         this.plasmaBlobMaxSizePx = NanoGeoClientConfig.defaultArmorModePlasmaBlobMaxSizePx();
         this.plasmaBlobEdgeSoftnessPx = NanoGeoClientConfig.defaultArmorModePlasmaBlobEdgeSoftnessPx();
         this.plasmaBlobRimAlpha = NanoGeoClientConfig.defaultArmorModePlasmaBlobRimAlpha();
         this.plasmaBlobRimWidthPx = NanoGeoClientConfig.defaultArmorModePlasmaBlobRimWidthPx();
         this.plasmaBlobRimBrightness = NanoGeoClientConfig.defaultArmorModePlasmaBlobRimBrightness();
         this.plasmaBlobDriftSpeedPxPerSecond = NanoGeoClientConfig.defaultArmorModePlasmaBlobDriftSpeedPxPerSecond();
         this.plasmaBlobDriftRangeScale = NanoGeoClientConfig.defaultArmorModePlasmaBlobDriftRangeScale();
         this.plasmaBlobShapeDistortion = NanoGeoClientConfig.defaultArmorModePlasmaBlobShapeDistortion();
         this.patchTextureAlpha = NanoGeoClientConfig.defaultArmorModePatchTextureAlpha();
         this.patchSpotAlpha = NanoGeoClientConfig.defaultArmorModePatchSpotAlpha();
         this.patchMinSizePx = NanoGeoClientConfig.defaultArmorModePatchMinSizePx();
         this.patchMaxSizePx = NanoGeoClientConfig.defaultArmorModePatchMaxSizePx();
         this.patchFrequency = NanoGeoClientConfig.defaultArmorModePatchFrequency();
         this.patchDensity = NanoGeoClientConfig.defaultArmorModePatchDensity();
         this.patchCloudEdgeSoftness = NanoGeoClientConfig.defaultArmorModePatchCloudEdgeSoftness();
         this.patchFlowVariant = NanoGeoClientConfig.defaultArmorModePatchFlowVariant();
         this.pulseEnabled = NanoGeoClientConfig.defaultArmorModePulseEnabled();
         this.pulseVisualMode = NanoGeoClientConfig.defaultArmorModePulseVisualMode();
         this.pulseCompatPreset = NanoGeoClientConfig.defaultArmorModeCompatPulsePreset();
         this.pulseMaxCenters = NanoGeoClientConfig.defaultArmorModePulseMaxCenters();
         this.pulseCenterMarkerAlpha = NanoGeoClientConfig.defaultArmorModePulseCenterMarkerAlpha();
         this.pulseIntervalSeconds = NanoGeoClientConfig.defaultArmorModePulseIntervalSeconds();
         this.pulseDurationSeconds = NanoGeoClientConfig.defaultArmorModePulseDurationSeconds();
         this.pulseStartRadiusPx = NanoGeoClientConfig.defaultArmorModePulseStartRadiusPx();
         this.pulseEndRadiusPx = NanoGeoClientConfig.defaultArmorModePulseEndRadiusPx();
         this.pulseCoreWidthPx = NanoGeoClientConfig.defaultArmorModePulseCoreWidthPx();
         this.pulseGlowRadiusPx = NanoGeoClientConfig.defaultArmorModePulseGlowRadiusPx();
         this.pulseCoreSoftnessPx = NanoGeoClientConfig.defaultArmorModePulseCoreSoftnessPx();
         this.pulseGlowSoftnessPx = NanoGeoClientConfig.defaultArmorModePulseGlowSoftnessPx();
         this.pulseCoreColor = NanoGeoClientConfig.defaultArmorModePulseCoreColor();
         this.pulseGlowColor = NanoGeoClientConfig.defaultArmorModePulseGlowColor();
         this.pulseCoreAlpha = NanoGeoClientConfig.defaultArmorModePulseCoreAlpha();
         this.pulseGlowAlpha = NanoGeoClientConfig.defaultArmorModePulseGlowAlpha();
         this.pulseBrightness = NanoGeoClientConfig.defaultArmorModePulseBrightness();
         this.pulseFadePower = NanoGeoClientConfig.defaultArmorModePulseFadePower();
         this.pulseRingDistortionPx = NanoGeoClientConfig.defaultArmorModePulseRingDistortionPx();
         this.pulseRingDistortionFrequency = NanoGeoClientConfig.defaultArmorModePulseRingDistortionFrequency();
         this.pulseRingEnergyVariation = NanoGeoClientConfig.defaultArmorModePulseRingEnergyVariation();
         this.pulseWidthVariation = NanoGeoClientConfig.defaultArmorModePulseWidthVariation();
         this.pulseBrightnessVariation = NanoGeoClientConfig.defaultArmorModePulseBrightnessVariation();
         this.pulseEdgeRoughness = NanoGeoClientConfig.defaultArmorModePulseEdgeRoughness();
         this.pulsePatternRotationRandomness = NanoGeoClientConfig.defaultArmorModePulsePatternRotationRandomness();
         this.pulseMergingEnabled = NanoGeoClientConfig.defaultArmorModePulseMergingEnabled();
         this.pulseMergeStrength = NanoGeoClientConfig.defaultArmorModePulseMergeStrength();
         this.pulseMergeSoftnessPx = NanoGeoClientConfig.defaultArmorModePulseMergeSoftnessPx();
         this.pulseMergeBrightnessBoost = NanoGeoClientConfig.defaultArmorModePulseMergeBrightnessBoost();
         this.pulseDebugForceVisible = NanoGeoClientConfig.defaultArmorModePulseDebugForceVisible();
         this.pulseDebugOutputOnlyPulse = NanoGeoClientConfig.defaultArmorModePulseDebugOutputOnlyPulse();
         this.debugPlasmaField = NanoGeoClientConfig.defaultDebugArmorModePlasmaField();
      }

      private void resetCurrentPage(ArmorModeOverlaySettingsScreen.Page page) {
         switch (page) {
            case BASIC:
               this.overlayAlpha = NanoGeoClientConfig.defaultArmorModeOverlayAlpha();
               this.overlayBrightness = NanoGeoClientConfig.defaultArmorModeOverlayBrightness();
               this.overlayTintR = NanoGeoClientConfig.defaultArmorModeOverlayTintR();
               this.overlayTintG = NanoGeoClientConfig.defaultArmorModeOverlayTintG();
               this.overlayTintB = NanoGeoClientConfig.defaultArmorModeOverlayTintB();
               this.patternMode = NanoGeoClientConfig.defaultArmorModeOverlayPatternMode();
               this.fullbright = NanoGeoClientConfig.defaultArmorModeOverlayFullbright();
               this.renderInFirstPerson = NanoGeoClientConfig.defaultArmorModeOverlayRenderInFirstPerson();
               this.renderInThirdPerson = NanoGeoClientConfig.defaultArmorModeOverlayRenderInThirdPerson();
               this.debugSettingsScreen = NanoGeoClientConfig.defaultDebugArmorModeSettingsScreen();
               this.transitionEnabled = NanoGeoClientConfig.defaultArmorModeTransitionEnabled();
               this.transitionDurationSeconds = NanoGeoClientConfig.defaultArmorModeTransitionDurationSeconds();
               this.transitionPixelBlockSizePx = NanoGeoClientConfig.defaultArmorModeTransitionPixelBlockSizePx();
               this.transitionStaggerAmount = NanoGeoClientConfig.defaultArmorModeTransitionStaggerAmount();
               this.transitionFadeWindow = NanoGeoClientConfig.defaultArmorModeTransitionFadeWindow();
               this.transitionAffectsPulses = NanoGeoClientConfig.defaultArmorModeTransitionAffectsPulses();
               break;
            case PLASMA:
               this.hexTextureSizePx = NanoGeoClientConfig.defaultArmorModeHexTextureSizePx();
               this.plasmaBlobInnerAlpha = NanoGeoClientConfig.defaultArmorModePlasmaBlobInnerAlpha();
               this.plasmaBlobCount = NanoGeoClientConfig.defaultArmorModePlasmaBlobCount();
               this.plasmaBlobMinSizePx = NanoGeoClientConfig.defaultArmorModePlasmaBlobMinSizePx();
               this.plasmaBlobMaxSizePx = NanoGeoClientConfig.defaultArmorModePlasmaBlobMaxSizePx();
               this.plasmaBlobEdgeSoftnessPx = NanoGeoClientConfig.defaultArmorModePlasmaBlobEdgeSoftnessPx();
               this.plasmaBlobRimAlpha = NanoGeoClientConfig.defaultArmorModePlasmaBlobRimAlpha();
               this.plasmaBlobRimWidthPx = NanoGeoClientConfig.defaultArmorModePlasmaBlobRimWidthPx();
               this.plasmaBlobRimBrightness = NanoGeoClientConfig.defaultArmorModePlasmaBlobRimBrightness();
               this.plasmaBlobDriftSpeedPxPerSecond = NanoGeoClientConfig.defaultArmorModePlasmaBlobDriftSpeedPxPerSecond();
               this.plasmaBlobDriftRangeScale = NanoGeoClientConfig.defaultArmorModePlasmaBlobDriftRangeScale();
               this.plasmaBlobShapeDistortion = NanoGeoClientConfig.defaultArmorModePlasmaBlobShapeDistortion();
               this.patchTextureAlpha = NanoGeoClientConfig.defaultArmorModePatchTextureAlpha();
               this.patchSpotAlpha = NanoGeoClientConfig.defaultArmorModePatchSpotAlpha();
               this.patchMinSizePx = NanoGeoClientConfig.defaultArmorModePatchMinSizePx();
               this.patchMaxSizePx = NanoGeoClientConfig.defaultArmorModePatchMaxSizePx();
               this.patchFrequency = NanoGeoClientConfig.defaultArmorModePatchFrequency();
               this.patchDensity = NanoGeoClientConfig.defaultArmorModePatchDensity();
               this.patchCloudEdgeSoftness = NanoGeoClientConfig.defaultArmorModePatchCloudEdgeSoftness();
               this.patchFlowVariant = NanoGeoClientConfig.defaultArmorModePatchFlowVariant();
               this.pulseEnabled = NanoGeoClientConfig.defaultArmorModePulseEnabled();
               this.pulseVisualMode = NanoGeoClientConfig.defaultArmorModePulseVisualMode();
               this.pulseCompatPreset = NanoGeoClientConfig.defaultArmorModeCompatPulsePreset();
               this.pulseMaxCenters = NanoGeoClientConfig.defaultArmorModePulseMaxCenters();
               this.pulseCenterMarkerAlpha = NanoGeoClientConfig.defaultArmorModePulseCenterMarkerAlpha();
               this.pulseIntervalSeconds = NanoGeoClientConfig.defaultArmorModePulseIntervalSeconds();
               this.pulseDurationSeconds = NanoGeoClientConfig.defaultArmorModePulseDurationSeconds();
               this.pulseStartRadiusPx = NanoGeoClientConfig.defaultArmorModePulseStartRadiusPx();
               this.pulseEndRadiusPx = NanoGeoClientConfig.defaultArmorModePulseEndRadiusPx();
               this.pulseCoreWidthPx = NanoGeoClientConfig.defaultArmorModePulseCoreWidthPx();
               this.pulseGlowRadiusPx = NanoGeoClientConfig.defaultArmorModePulseGlowRadiusPx();
               this.pulseCoreSoftnessPx = NanoGeoClientConfig.defaultArmorModePulseCoreSoftnessPx();
               this.pulseGlowSoftnessPx = NanoGeoClientConfig.defaultArmorModePulseGlowSoftnessPx();
               this.pulseCoreColor = NanoGeoClientConfig.defaultArmorModePulseCoreColor();
               this.pulseGlowColor = NanoGeoClientConfig.defaultArmorModePulseGlowColor();
               this.pulseCoreAlpha = NanoGeoClientConfig.defaultArmorModePulseCoreAlpha();
               this.pulseGlowAlpha = NanoGeoClientConfig.defaultArmorModePulseGlowAlpha();
               this.pulseBrightness = NanoGeoClientConfig.defaultArmorModePulseBrightness();
               this.pulseFadePower = NanoGeoClientConfig.defaultArmorModePulseFadePower();
               this.pulseRingDistortionPx = NanoGeoClientConfig.defaultArmorModePulseRingDistortionPx();
               this.pulseRingDistortionFrequency = NanoGeoClientConfig.defaultArmorModePulseRingDistortionFrequency();
               this.pulseRingEnergyVariation = NanoGeoClientConfig.defaultArmorModePulseRingEnergyVariation();
               this.pulseWidthVariation = NanoGeoClientConfig.defaultArmorModePulseWidthVariation();
               this.pulseBrightnessVariation = NanoGeoClientConfig.defaultArmorModePulseBrightnessVariation();
               this.pulseEdgeRoughness = NanoGeoClientConfig.defaultArmorModePulseEdgeRoughness();
               this.pulsePatternRotationRandomness = NanoGeoClientConfig.defaultArmorModePulsePatternRotationRandomness();
               this.pulseMergingEnabled = NanoGeoClientConfig.defaultArmorModePulseMergingEnabled();
               this.pulseMergeStrength = NanoGeoClientConfig.defaultArmorModePulseMergeStrength();
               this.pulseMergeSoftnessPx = NanoGeoClientConfig.defaultArmorModePulseMergeSoftnessPx();
               this.pulseMergeBrightnessBoost = NanoGeoClientConfig.defaultArmorModePulseMergeBrightnessBoost();
               this.pulseDebugForceVisible = NanoGeoClientConfig.defaultArmorModePulseDebugForceVisible();
               this.pulseDebugOutputOnlyPulse = NanoGeoClientConfig.defaultArmorModePulseDebugOutputOnlyPulse();
               this.debugPlasmaField = NanoGeoClientConfig.defaultDebugArmorModePlasmaField();
               break;
            case COMPAT:
               this.overlayBackend = NanoGeoClientConfig.defaultArmorModeOverlayBackend();
               this.dynamicTextureCompatShellEnabled = NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatShellEnabled();
               this.dynamicTextureCompatShellScale = NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatShellScale();
               this.dynamicTextureCompatMaxResolution = NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatMaxResolution();
               this.dynamicTextureCompatUpdateIntervalMillis = NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatUpdateIntervalMillis();
               this.dynamicTextureCompatGlowStrength = NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatGlowStrength();
               this.dynamicTextureCompatUvCoverageMaskEnabled = NanoGeoClientConfig.defaultArmorModeDynamicTextureCompatUvCoverageMaskEnabled();
               this.compatPerformancePreset = NanoGeoClientConfig.defaultArmorModeCompatPerformancePreset();
         }
      }
   }

   private static final class FloatSlider extends AbstractSliderButton {
      private final String label;
      private final float minValue;
      private final float maxValue;
      private final Consumer<Float> consumer;

      private FloatSlider(int x, int y, int width, String label, float value, float minValue, float maxValue, Consumer<Float> consumer) {
         super(x, y, width, 20, Component.empty(), normalize(value, minValue, maxValue));
         this.label = label;
         this.minValue = minValue;
         this.maxValue = maxValue;
         this.consumer = consumer;
         this.updateMessage();
      }

      protected void updateMessage() {
         this.setMessage(Component.literal(this.label + ": " + String.format(Locale.ROOT, "%.2f", denormalize(this.value, this.minValue, this.maxValue))));
      }

      protected void applyValue() {
         this.consumer.accept(denormalize(this.value, this.minValue, this.maxValue));
      }

      private static double normalize(float value, float minValue, float maxValue) {
         return Mth.clamp((value - minValue) / (maxValue - minValue), 0.0F, 1.0F);
      }

      private static float denormalize(double normalized, float minValue, float maxValue) {
         return Mth.clamp((float)normalized, 0.0F, 1.0F) * (maxValue - minValue) + minValue;
      }
   }

   private static final class HexColorInputBox extends EditBox {
      private final Consumer<String> consumer;
      private String lastAppliedValue;

      private HexColorInputBox(Font font, int x, int y, int width, String value, Consumer<String> consumer) {
         super(font, x, y, width, 20, Component.literal("Hex color"));
         this.consumer = consumer;
         this.lastAppliedValue = ArmorModeOverlaySettingsScreen.normalizeRgbHex(value, "#FFFFFF");
         this.setMaxLength(7);
         this.setFilter(text -> {
            if (text.isEmpty()) {
               return true;
            } else if (text.length() > 7) {
               return false;
            } else {
               int start = text.startsWith("#") ? 1 : 0;
               if (start > 0 && text.indexOf(35, 1) >= 0) {
                  return false;
               } else {
                  for (int i = start; i < text.length(); i++) {
                     char c = text.charAt(i);
                     boolean hex = c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
                     if (!hex) {
                        return false;
                     }
                  }

                  return true;
               }
            }
         });
         this.setValue(this.lastAppliedValue);
         this.setResponder(this::handleTextChanged);
      }

      private void handleTextChanged(String text) {
         String normalized = ArmorModeOverlaySettingsScreen.normalizeRgbHexInternal(text);
         if (normalized != null) {
            this.lastAppliedValue = normalized;
            this.consumer.accept(normalized);
         }
      }

      public void setFocused(boolean focused) {
         boolean wasFocused = this.isFocused();
         super.setFocused(focused);
         if (wasFocused && !focused) {
            this.setValue(this.lastAppliedValue);
         }
      }
   }

   private static final class IntInputBox extends EditBox {
      private final int minValue;
      private final int maxValue;
      private final IntConsumer consumer;
      private int lastAppliedValue;

      private IntInputBox(Font font, int x, int y, int width, int value, int minValue, int maxValue, IntConsumer consumer) {
         super(font, x, y, width, 20, Component.literal("Integer value"));
         int clamped = Mth.clamp(value, minValue, maxValue);
         this.minValue = minValue;
         this.maxValue = maxValue;
         this.consumer = consumer;
         this.lastAppliedValue = clamped;
         this.setMaxLength(6);
         this.setFilter(text -> text.isEmpty() || text.chars().allMatch(Character::isDigit));
         this.setValue(Integer.toString(clamped));
         this.setResponder(this::handleTextChanged);
      }

      private void handleTextChanged(String text) {
         if (!text.isEmpty()) {
            try {
               int parsed = Integer.parseInt(text);
               int clamped = Mth.clamp(parsed, this.minValue, this.maxValue);
               this.lastAppliedValue = clamped;
               this.consumer.accept(clamped);
            } catch (NumberFormatException var4) {
            }
         }
      }

      public void setFocused(boolean focused) {
         boolean wasFocused = this.isFocused();
         super.setFocused(focused);
         if (wasFocused && !focused) {
            this.setValue(Integer.toString(this.lastAppliedValue));
         }
      }
   }

   private static final class IntSlider extends AbstractSliderButton {
      private final String label;
      private final int minValue;
      private final int maxValue;
      private final IntConsumer consumer;

      private IntSlider(int x, int y, int width, String label, int value, int minValue, int maxValue, IntConsumer consumer) {
         super(x, y, width, 20, Component.empty(), normalize(value, minValue, maxValue));
         this.label = label;
         this.minValue = minValue;
         this.maxValue = maxValue;
         this.consumer = consumer;
         this.updateMessage();
      }

      protected void updateMessage() {
         this.setMessage(Component.literal(this.label + ": " + denormalize(this.value, this.minValue, this.maxValue)));
      }

      protected void applyValue() {
         this.consumer.accept(denormalize(this.value, this.minValue, this.maxValue));
      }

      private static double normalize(int value, int minValue, int maxValue) {
         return (double)(Mth.clamp(value, minValue, maxValue) - minValue) / (maxValue - minValue);
      }

      private static int denormalize(double normalized, int minValue, int maxValue) {
         double clamped = Mth.clamp((float)normalized, 0.0F, 1.0F);
         return Math.round((float)(minValue + clamped * (maxValue - minValue)));
      }
   }

   private static enum Page {
      BASIC("Basic"),
      PLASMA("Pattern"),
      COMPAT("Compat"),
      PREVIEW("Preview");

      private final String label;

      private Page(String label) {
         this.label = label;
      }
   }

   private record PageWidgetPlacement(AbstractWidget widget, int baseY) {
   }

   private record PlasmaPreviewCell(float centerX, float centerY, float cellWidth, float cellHeight) {
   }

   private static enum PreviewPane {
      MODEL("Model"),
      TEXTURE("Texture"),
      PULSE("Pulse");

      private final String label;

      private PreviewPane(String label) {
         this.label = label;
      }

      private ArmorModeOverlaySettingsScreen.PreviewPane next() {
         return switch (this) {
            case MODEL -> TEXTURE;
            case TEXTURE -> PULSE;
            case PULSE -> MODEL;
         };
      }
   }

   private record PreviewPulseContour(
      float signedDistance,
      float ringDistance,
      float contourRadius,
      float coreWidth,
      float glowWidth,
      float coreSoftness,
      float glowSoftness,
      float coreAlpha,
      float glowAlpha,
      float centerX,
      float centerY
   ) {
   }

   private record PreviewPulseContribution(float coreWidth, float glowWidth, float coreAlpha, float glowAlpha) {
   }

   private static enum PreviewQuality {
      LOW("LOW", 6, 48.0F, 12),
      MEDIUM("MEDIUM", 9, 64.0F, 24),
      HIGH("HIGH", 12, 88.0F, 64);

      private final String label;
      private final int mergePreviewDivisor;
      private final float modelScaleCap;
      private final int maxPulsePreviewCenters;

      private PreviewQuality(String label, int mergePreviewDivisor, float modelScaleCap, int maxPulsePreviewCenters) {
         this.label = label;
         this.mergePreviewDivisor = mergePreviewDivisor;
         this.modelScaleCap = modelScaleCap;
         this.maxPulsePreviewCenters = maxPulsePreviewCenters;
      }

      private ArmorModeOverlaySettingsScreen.PreviewQuality next() {
         return switch (this) {
            case LOW -> MEDIUM;
            case MEDIUM -> HIGH;
            case HIGH -> LOW;
         };
      }
   }

   private static final class StaticTextWidget extends AbstractWidget {
      private final Font font;
      private final String label;

      private StaticTextWidget(Font font, int x, int y, int width, int height, String label) {
         super(x, y, width, height, Component.literal(label));
         this.font = font;
         this.label = label;
      }

      protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
         int textY = this.getY() + (this.height - 8) / 2;
         guiGraphics.drawString(this.font, this.label, this.getX(), textY, 13625343, false);
      }

      protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
      }
   }
}
