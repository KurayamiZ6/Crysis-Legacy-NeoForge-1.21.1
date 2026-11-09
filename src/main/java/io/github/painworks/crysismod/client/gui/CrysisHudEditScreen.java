package io.github.painworks.crysismod.client.gui;

import io.github.painworks.crysismod.client.minimap.NanoMinimapHudTransform;
import io.github.painworks.crysismod.client.minimap.NanoMinimapViewport;
import io.github.painworks.crysismod.client.minimap.config.NanoMinimapClientConfig;
import io.github.painworks.crysismod.nanosuit.client.NanosuitHudOverlay;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanovisor.client.NanoVisorHudOverlay;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorClientConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class CrysisHudEditScreen extends Screen {
   private static final int PANEL_BG = -1341123560;
   private static final int PANEL_BORDER = -2144544513;
   private static final int SELECTED = -1377281;
   private static final int IDLE = -1439901441;
   private static final int CHILD = -2004293377;
   private static final int HANDLE = -7314;
   private static final int HANDLE_SIZE = 4;
   private static final int SNAP = 5;
   private final Screen parent;
   private final List<CrysisHudEditScreen.EditableHudElement> elements = new ArrayList<>();
   private CrysisHudEditScreen.EditableHudElement selected;
   private boolean dragging;
   private CrysisHudEditScreen.TransformHandle activeHandle = CrysisHudEditScreen.TransformHandle.NONE;
   private double dragStartMouseX;
   private double dragStartMouseY;
   private int dragStartX;
   private int dragStartY;
   private int resizeStartW;
   private int resizeStartH;
   private double resizeStartScale;
   private CrysisHudEditScreen.HudShape resizeStartShape;
   private int propertyIndex;
   private CrysisHudEditScreen.EditableHudElement hovered;

   public CrysisHudEditScreen(Screen parent) {
      super(Component.literal("Crysis Mod HUD Editor"));
      this.parent = parent;
   }

   protected void init() {
      this.rebuildElements();
      if (this.selected == null && !this.elements.isEmpty()) {
         this.selected = this.elements.get(0);
      }

      this.addRenderableWidget(Button.builder(Component.literal("Reset selected"), button -> {
         if (this.selected != null) {
            this.selected.reset();
         }
      }).bounds(20, 106, 110, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Reset all build"), button -> resetAllToBuildDefaults()).bounds(140, 106, 115, 20).build());
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      guiGraphics.fill(0, 0, this.width, this.height, 1426063360);
      this.hovered = this.findBodyHover(mouseX, mouseY);

      for (CrysisHudEditScreen.EditableHudElement element : this.elements) {
         CrysisHudEditScreen.HudShape shape = element.shape(this.width, this.height);
         if (element == this.selected) {
            this.drawShape(guiGraphics, shape, -1377281);
            this.drawTransformHandles(guiGraphics, shape, element.canTransformSize());
            guiGraphics.drawString(this.font, element.label(), shape.labelX(), shape.labelY(), -1377281, false);
         } else if (element == this.hovered) {
            this.drawShape(guiGraphics, shape, -1439901441);
            guiGraphics.drawString(this.font, element.label(), shape.labelX(), shape.labelY(), -1439901441, false);
         }
      }

      this.drawPanel(guiGraphics);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (super.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else if (button != 0) {
         return false;
      } else {
         CrysisHudEditScreen.HitTarget hit = this.findTopmostTarget(mouseX, mouseY);
         if (hit == null) {
            return true;
         } else {
            this.selected = hit.element();
            this.propertyIndex = this.clampPropertyIndex(this.propertyIndex);
            CrysisHudEditScreen.HudShape shape = this.selected.shape(this.width, this.height);
            this.dragging = true;
            this.activeHandle = hit.handle();
            this.dragStartMouseX = mouseX;
            this.dragStartMouseY = mouseY;
            this.dragStartX = this.selected.configX();
            this.dragStartY = this.selected.configY();
            this.resizeStartW = this.selected.configWidth();
            this.resizeStartH = this.selected.configHeight();
            this.resizeStartScale = this.selected.scale();
            this.resizeStartShape = shape;
            return true;
         }
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.dragging && this.selected != null && button == 0) {
         int dx = (int)Math.round(mouseX - this.dragStartMouseX);
         int dy = (int)Math.round(mouseY - this.dragStartMouseY);
         if (hasShiftDown()) {
            dx = snap(dx);
            dy = snap(dy);
         }

         if (this.activeHandle != CrysisHudEditScreen.TransformHandle.NONE) {
            this.selected
               .transformFrom(
                  this.activeHandle,
                  this.resizeStartShape,
                  this.dragStartX,
                  this.dragStartY,
                  this.resizeStartW,
                  this.resizeStartH,
                  this.resizeStartScale,
                  dx,
                  dy,
                  hasControlDown()
               );
         } else {
            this.selected.dragFrom(this.dragStartX, this.dragStartY, dx, dy);
         }

         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.dragging = false;
      this.activeHandle = CrysisHudEditScreen.TransformHandle.NONE;
      this.resizeStartShape = null;
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      if (this.selected == null) {
         return true;
      } else if (hasControlDown()) {
         this.adjustSelectedProperty(delta > 0.0 ? 1 : -1);
         return true;
      } else {
         double step = hasShiftDown() ? 0.01 : 0.05;
         this.selected.addScale(delta * step);
         return true;
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.selected != null) {
         int step = hasShiftDown() ? 5 : 1;
         if (keyCode == 262) {
            this.selected.dragFrom(this.selected.configX(), this.selected.configY(), step, 0);
            return true;
         }

         if (keyCode == 263) {
            this.selected.dragFrom(this.selected.configX(), this.selected.configY(), -step, 0);
            return true;
         }

         if (keyCode == 264) {
            this.selected.dragFrom(this.selected.configX(), this.selected.configY(), 0, step);
            return true;
         }

         if (keyCode == 265) {
            this.selected.dragFrom(this.selected.configX(), this.selected.configY(), 0, -step);
            return true;
         }

         if (keyCode == 82) {
            this.selected.rotateBy(hasShiftDown() ? -1.0 : 1.0);
            return true;
         }

         if (keyCode == 258) {
            this.selectNext(hasShiftDown() ? -1 : 1);
            return true;
         }

         if (keyCode == 91) {
            this.propertyIndex = this.clampPropertyIndex(this.propertyIndex - 1);
            return true;
         }

         if (keyCode == 93) {
            this.propertyIndex = this.clampPropertyIndex(this.propertyIndex + 1);
            return true;
         }

         if (keyCode == 44) {
            this.adjustSelectedProperty(-1);
            return true;
         }

         if (keyCode == 46) {
            this.adjustSelectedProperty(1);
            return true;
         }
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   public void onClose() {
      this.minecraft.setScreen(this.parent);
   }

   public boolean isPauseScreen() {
      return false;
   }

   private void rebuildElements() {
      this.elements.clear();
      this.elements.add(new CrysisHudEditScreen.NanosuitEnergyElement());
      this.elements.add(new CrysisHudEditScreen.NanosuitModesElement());
      this.elements.add(new CrysisHudEditScreen.TaczGroupElement());
      this.elements.add(new CrysisHudEditScreen.TaczIconElement());
      this.elements.add(new CrysisHudEditScreen.TaczAmmoGunElement());
      this.elements.add(new CrysisHudEditScreen.TaczAmmoInvElement());
      this.elements.add(new CrysisHudEditScreen.MinimapElement());
      this.elements.add(new CrysisHudEditScreen.NanoVisorTextElement());
      this.elements.add(new CrysisHudEditScreen.NanoVisorArrowStartElement());
      this.elements.add(new CrysisHudEditScreen.NanoVisorArrowEndElement());
   }

   private static void resetAllToBuildDefaults() {
      NanosuitHudOverlay.setHudOffsetX(-28);
      NanosuitHudOverlay.setHudOffsetY(-8);
      NanosuitHudOverlay.setHudScale(0.2F);
      NanosuitClientConfig.setTaczHudGroupOffsetX(-42);
      NanosuitClientConfig.setTaczHudGroupOffsetY(-30);
      NanosuitClientConfig.setTaczHudGroupScaleX(0.82F);
      NanosuitClientConfig.setTaczHudGroupScaleY(0.82F);
      NanosuitClientConfig.setTaczHudGroupRotation(0.0F);
      NanosuitClientConfig.setTaczHudGroupVerticalSkewDown(0.17F);
      NanosuitClientConfig.setTaczHudIconOffsetX(0);
      NanosuitClientConfig.setTaczHudIconOffsetY(1);
      NanosuitClientConfig.setTaczHudIconScale(1.0F);
      NanosuitClientConfig.setTaczHudAmmoGunOffsetX(47);
      NanosuitClientConfig.setTaczHudAmmoGunOffsetY(1);
      NanosuitClientConfig.setTaczHudAmmoGunScale(1.69F);
      NanosuitClientConfig.setTaczHudAmmoInvOffsetX(79);
      NanosuitClientConfig.setTaczHudAmmoInvOffsetY(1);
      NanosuitClientConfig.setTaczHudAmmoInvScale(1.0F);
      NanoMinimapClientConfig.setMinimapOffsetX(40);
      NanoMinimapClientConfig.setMinimapOffsetY(16);
      NanoMinimapClientConfig.setMinimapWidth(150);
      NanoMinimapClientConfig.setMinimapHeight(100);
      NanoMinimapClientConfig.setMinimapScale(0.7);
      NanoMinimapClientConfig.setMinimapOpacity(0.5);
      NanoMinimapClientConfig.setMinimapPerspectiveRightSide(-0.19);
      NanoMinimapClientConfig.setMinimapPerspectiveAxisX(0.07);
      NanoMinimapClientConfig.setMinimapPerspectiveAxisY(0.0);
      NanoMinimapClientConfig.setMinimapEdgeArcTopBottom(-0.09);
      NanoVisorClientConfig.setHudOffsetX(0);
      NanoVisorClientConfig.setHudOffsetY(0);
      NanoVisorClientConfig.setHudScale(0.7);
      NanoVisorClientConfig.setHudOpacity(0.95);
      NanoVisorClientConfig.setVisorZoomArrowStartX(702.0);
      NanoVisorClientConfig.setVisorZoomArrowStartY(568.0);
      NanoVisorClientConfig.setVisorZoomArrowEndX(702.0);
      NanoVisorClientConfig.setVisorZoomArrowEndY(493.0);
      NanoVisorHudOverlay.resetArrowInterpolation();
   }

   private CrysisHudEditScreen.HitTarget findTopmostTarget(double mouseX, double mouseY) {
      if (this.selected != null) {
         CrysisHudEditScreen.HudShape shape = this.selected.shape(this.width, this.height);
         CrysisHudEditScreen.TransformHandle handle = this.detectHandle(shape, mouseX, mouseY, this.selected.canTransformSize());
         if (handle != CrysisHudEditScreen.TransformHandle.NONE) {
            return new CrysisHudEditScreen.HitTarget(this.selected, handle);
         }
      }

      for (int i = this.elements.size() - 1; i >= 0; i--) {
         CrysisHudEditScreen.EditableHudElement element = this.elements.get(i);
         if (element != this.selected) {
            CrysisHudEditScreen.HudShape shape = element.shape(this.width, this.height);
            if (shape.contains(mouseX, mouseY)) {
               return new CrysisHudEditScreen.HitTarget(element, CrysisHudEditScreen.TransformHandle.NONE);
            }
         }
      }

      return this.selected != null && this.selected.shape(this.width, this.height).contains(mouseX, mouseY)
         ? new CrysisHudEditScreen.HitTarget(this.selected, CrysisHudEditScreen.TransformHandle.NONE)
         : null;
   }

   private CrysisHudEditScreen.EditableHudElement findBodyHover(double mouseX, double mouseY) {
      for (int i = this.elements.size() - 1; i >= 0; i--) {
         CrysisHudEditScreen.EditableHudElement element = this.elements.get(i);
         if (element != this.selected && element.shape(this.width, this.height).contains(mouseX, mouseY)) {
            return element;
         }
      }

      return null;
   }

   private void selectNext(int direction) {
      if (!this.elements.isEmpty()) {
         int index = this.selected == null ? -1 : this.elements.indexOf(this.selected);
         this.selected = this.elements.get(Math.floorMod(index + direction, this.elements.size()));
         this.propertyIndex = this.clampPropertyIndex(this.propertyIndex);
      }
   }

   private int clampPropertyIndex(int index) {
      return this.selected != null && !this.selected.properties().isEmpty() ? Math.floorMod(index, this.selected.properties().size()) : 0;
   }

   private void adjustSelectedProperty(int direction) {
      if (this.selected != null && !this.selected.properties().isEmpty()) {
         this.propertyIndex = this.clampPropertyIndex(this.propertyIndex);
         this.selected.properties().get(this.propertyIndex).adjust(direction, hasShiftDown());
      }
   }

   private void drawPanel(GuiGraphics guiGraphics) {
      int x = 10;
      int y = 10;
      int w = 360;
      int h = 118;
      guiGraphics.fill(x, y, x + w, y + h, -1341123560);
      this.drawRectOutline(guiGraphics, x, y, w, h, -2144544513);
      guiGraphics.drawString(this.font, "Crysis HUD Editor", x + 8, y + 8, -1377281, false);
      guiGraphics.drawString(this.font, "Drag: move | corner: resize | wheel: scale | Ctrl+wheel: property", x + 8, y + 22, -6561793, false);
      guiGraphics.drawString(this.font, "Arrows: nudge | Shift: snap/fine | Tab: next | R: rotate | Esc: close", x + 8, y + 34, -6561793, false);
      guiGraphics.drawString(this.font, "[ / ] property | , / . adjust property", x + 8, y + 46, -6561793, false);
      if (this.selected != null) {
         guiGraphics.drawString(this.font, this.selected.label(), x + 8, y + 64, -7314, false);
         guiGraphics.drawString(this.font, this.selected.status(), x + 8, y + 76, -1377281, false);
         List<CrysisHudEditScreen.EditorProperty> properties = this.selected.properties();
         if (!properties.isEmpty()) {
            this.propertyIndex = this.clampPropertyIndex(this.propertyIndex);
            guiGraphics.drawString(this.font, properties.get(this.propertyIndex).status(), x + 8, y + 88, -7314, false);
         } else {
            guiGraphics.drawString(this.font, "No advanced properties for this element", x + 8, y + 88, -8351584, false);
         }
      }
   }

   private void drawShape(GuiGraphics guiGraphics, CrysisHudEditScreen.HudShape shape, int color) {
      this.drawRectOutline(guiGraphics, shape.x(), shape.y(), shape.w(), shape.h(), color);
      if (shape.hasPolygon()) {
         for (int i = 0; i < shape.polyX().length; i++) {
            int x = Math.round(shape.polyX()[i]);
            int y = Math.round(shape.polyY()[i]);
            guiGraphics.fill(x - 2, y - 2, x + 3, y + 3, color);
         }
      }
   }

   private void drawHandle(GuiGraphics guiGraphics, int x, int y, int color) {
      int half = 2;
      guiGraphics.fill(x - half, y - half, x - half + 4, y - half + 4, color);
   }

   private void drawTransformHandles(GuiGraphics guiGraphics, CrysisHudEditScreen.HudShape shape, boolean active) {
      int color = active ? -7314 : -8616048;

      for (CrysisHudEditScreen.TransformHandle handle : CrysisHudEditScreen.TransformHandle.values()) {
         if (handle != CrysisHudEditScreen.TransformHandle.NONE) {
            int[] point = handle.point(shape);
            this.drawHandle(guiGraphics, point[0], point[1], color);
         }
      }
   }

   private void drawRectOutline(GuiGraphics guiGraphics, int x, int y, int w, int h, int color) {
      guiGraphics.fill(x, y, x + w, y + 1, color);
      guiGraphics.fill(x, y + h - 1, x + w, y + h, color);
      guiGraphics.fill(x, y, x + 1, y + h, color);
      guiGraphics.fill(x + w - 1, y, x + w, y + h, color);
   }

   private CrysisHudEditScreen.TransformHandle detectHandle(CrysisHudEditScreen.HudShape shape, double mouseX, double mouseY, boolean enabled) {
      if (!enabled) {
         return CrysisHudEditScreen.TransformHandle.NONE;
      } else {
         int half = 8;

         for (CrysisHudEditScreen.TransformHandle handle : CrysisHudEditScreen.TransformHandle.values()) {
            if (handle != CrysisHudEditScreen.TransformHandle.NONE) {
               int[] point = handle.point(shape);
               if (mouseX >= point[0] - half && mouseX <= point[0] + half && mouseY >= point[1] - half && mouseY <= point[1] + half) {
                  return handle;
               }
            }
         }

         return CrysisHudEditScreen.TransformHandle.NONE;
      }
   }

   private static int snap(int value) {
      return Math.round(value / 5.0F) * 5;
   }

   private static int clampInt(int value, int min, int max) {
      return Mth.clamp(value, min, max);
   }

   private static String fmt(double value) {
      return String.format(Locale.ROOT, "%.3f", value);
   }

   private static final class BuildDefaults {
      private static final int HUD_OFFSET_X = -28;
      private static final int HUD_OFFSET_Y = -8;
      private static final double HUD_SCALE = 0.2;
      private static final int TACZ_GROUP_OFFSET_X = -42;
      private static final int TACZ_GROUP_OFFSET_Y = -30;
      private static final double TACZ_GROUP_SCALE_X = 0.82F;
      private static final double TACZ_GROUP_SCALE_Y = 0.82F;
      private static final double TACZ_GROUP_ROTATION = 0.0;
      private static final double TACZ_GROUP_VERTICAL_SKEW_DOWN = 0.17F;
      private static final int TACZ_ICON_OFFSET_X = 0;
      private static final int TACZ_ICON_OFFSET_Y = 1;
      private static final double TACZ_ICON_SCALE = 1.0;
      private static final int TACZ_AMMO_GUN_OFFSET_X = 47;
      private static final int TACZ_AMMO_GUN_OFFSET_Y = 1;
      private static final double TACZ_AMMO_GUN_SCALE = 1.69F;
      private static final int TACZ_AMMO_INV_OFFSET_X = 79;
      private static final int TACZ_AMMO_INV_OFFSET_Y = 1;
      private static final double TACZ_AMMO_INV_SCALE = 1.0;
      private static final int MINIMAP_WIDTH = 150;
      private static final int MINIMAP_HEIGHT = 100;
      private static final int MINIMAP_OFFSET_X = 40;
      private static final int MINIMAP_OFFSET_Y = 16;
      private static final double MINIMAP_OPACITY = 0.5;
      private static final double MINIMAP_SCALE = 0.7;
      private static final double MINIMAP_PERSPECTIVE_RIGHT_SIDE = -0.19;
      private static final double MINIMAP_PERSPECTIVE_AXIS_X = 0.07;
      private static final double MINIMAP_PERSPECTIVE_AXIS_Y = 0.0;
      private static final double MINIMAP_EDGE_ARC_TOP_BOTTOM = -0.09;
      private static final int NANOVISOR_HUD_OFFSET_X = 0;
      private static final int NANOVISOR_HUD_OFFSET_Y = 0;
      private static final double NANOVISOR_HUD_SCALE = 0.7;
      private static final double NANOVISOR_HUD_OPACITY = 0.95;
      private static final double VISOR_ARROW_START_X = 702.0;
      private static final double VISOR_ARROW_START_Y = 568.0;
      private static final double VISOR_ARROW_END_X = 702.0;
      private static final double VISOR_ARROW_END_Y = 493.0;
   }

   private interface EditableHudElement {
      String label();

      CrysisHudEditScreen.HudShape shape(int var1, int var2);

      int configX();

      int configY();

      void setPosition(int var1, int var2);

      default void dragFrom(int startX, int startY, int dx, int dy) {
         this.setPosition(startX + dx, startY + dy);
      }

      double scale();

      void setScale(double var1);

      default void addScale(double delta) {
         this.setScale(this.scale() + delta);
      }

      default boolean resizable() {
         return false;
      }

      default boolean canTransformSize() {
         return this.resizable() || this.supportsScale();
      }

      default boolean supportsScale() {
         return true;
      }

      default boolean childElement() {
         return false;
      }

      default int configWidth() {
         return this.shape(Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight()).w();
      }

      default int configHeight() {
         return this.shape(Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight()).h();
      }

      default void setSize(int width, int height) {
      }

      default void transformFrom(
         CrysisHudEditScreen.TransformHandle handle,
         CrysisHudEditScreen.HudShape startShape,
         int startX,
         int startY,
         int startW,
         int startH,
         double startScale,
         int dx,
         int dy,
         boolean keepAspect
      ) {
         if (this.resizable()) {
            int newW = startW;
            int newH = startH;
            if (handle.affectsLeft()) {
               newW = startW - dx;
            } else if (handle.affectsRight()) {
               newW = startW + dx;
            }

            if (handle.affectsTop()) {
               newH = startH - dy;
            } else if (handle.affectsBottom()) {
               newH = startH + dy;
            }

            if (keepAspect && startW > 0 && startH > 0 && handle.isCorner()) {
               double aspect = (double)startW / startH;
               if (Math.abs(newW - startW) >= Math.abs(newH - startH)) {
                  newH = (int)Math.round(newW / aspect);
               } else {
                  newW = (int)Math.round(newH * aspect);
               }
            }

            this.setSize(newW, newH);
         } else {
            if (this.supportsScale() && startShape != null) {
               int widthDelta = handle.affectsLeft() ? -dx : (handle.affectsRight() ? dx : 0);
               int heightDelta = handle.affectsTop() ? -dy : (handle.affectsBottom() ? dy : 0);
               double widthFactor = startShape.w() <= 0 ? 1.0 : (double)(startShape.w() + widthDelta) / startShape.w();
               double heightFactor = startShape.h() <= 0 ? 1.0 : (double)(startShape.h() + heightDelta) / startShape.h();
               double factor;
               if (handle.isHorizontalOnly()) {
                  factor = widthFactor;
               } else if (handle.isVerticalOnly()) {
                  factor = heightFactor;
               } else {
                  factor = keepAspect ? Math.max(widthFactor, heightFactor) : (widthFactor + heightFactor) * 0.5;
               }

               this.setScale(startScale * Math.max(0.05, factor));
            }
         }
      }

      default List<CrysisHudEditScreen.EditorProperty> properties() {
         return List.of();
      }

      default void rotateBy(double degrees) {
      }

      void reset();

      default String status() {
         return "x=" + this.configX() + " y=" + this.configY() + " scale=" + CrysisHudEditScreen.fmt(this.scale());
      }
   }

   private record EditorProperty(
      String name, CrysisHudEditScreen.ValueReader reader, CrysisHudEditScreen.ValueWriter writer, double normalStep, double fineStep, double min, double max
   ) {
      void adjust(int direction, boolean fine) {
         double step = fine ? this.fineStep : this.normalStep;
         this.writer.set(Mth.clamp(this.reader.get() + direction * step, this.min, this.max));
      }

      String status() {
         return this.name + "=" + CrysisHudEditScreen.fmt(this.reader.get());
      }
   }

   private record HitTarget(CrysisHudEditScreen.EditableHudElement element, CrysisHudEditScreen.TransformHandle handle) {
   }

   private record HudShape(int x, int y, int w, int h, float[] polyX, float[] polyY) {
      static CrysisHudEditScreen.HudShape rect(int x, int y, int w, int h) {
         return new CrysisHudEditScreen.HudShape(x, y, Math.max(1, w), Math.max(1, h), null, null);
      }

      static CrysisHudEditScreen.HudShape polygon(float[] xs, float[] ys) {
         float minX = xs[0];
         float minY = ys[0];
         float maxX = xs[0];
         float maxY = ys[0];

         for (int i = 1; i < xs.length; i++) {
            minX = Math.min(minX, xs[i]);
            minY = Math.min(minY, ys[i]);
            maxX = Math.max(maxX, xs[i]);
            maxY = Math.max(maxY, ys[i]);
         }

         return new CrysisHudEditScreen.HudShape(
            Math.round(minX), Math.round(minY), Math.max(1, Math.round(maxX - minX)), Math.max(1, Math.round(maxY - minY)), xs, ys
         );
      }

      int right() {
         return this.x + this.w;
      }

      int bottom() {
         return this.y + this.h;
      }

      int labelX() {
         return this.x + 3;
      }

      int labelY() {
         return this.y - 10;
      }

      boolean hasPolygon() {
         return this.polyX != null && this.polyY != null && this.polyX.length == this.polyY.length;
      }

      boolean contains(double px, double py) {
         return px >= this.x && px <= this.right() && py >= this.y && py <= this.bottom();
      }
   }

   private static final class MinimapElement implements CrysisHudEditScreen.EditableHudElement {
      private static final int ANCHOR_PADDING = 12;

      @Override
      public String label() {
         return "Minimap";
      }

      @Override
      public CrysisHudEditScreen.HudShape shape(int screenWidth, int screenHeight) {
         double scale = NanoMinimapClientConfig.minimapScale();
         int w = Math.max(8, Math.round((float)(NanoMinimapClientConfig.minimapWidth() * scale)));
         int h = Math.max(8, Math.round((float)(NanoMinimapClientConfig.minimapHeight() * scale)));
         int x = 12 + NanoMinimapClientConfig.minimapOffsetX();
         int y = screenHeight - 12 - h - NanoMinimapClientConfig.minimapOffsetY();
         NanoMinimapViewport viewport = new NanoMinimapViewport(x, y, w, h, (float)NanoMinimapClientConfig.minimapOpacity());
         float xSkew = (float)Mth.clamp(NanoMinimapClientConfig.minimapPerspectiveRightSide() + NanoMinimapClientConfig.minimapPerspectiveAxisX(), -1.5, 1.5);
         float ySkew = (float)NanoMinimapClientConfig.minimapPerspectiveAxisY();
         float strength = NanoMinimapClientConfig.minimapPerspectiveEnabled() ? 1.0F : 0.0F;
         float[][] corners = this.transformedCorners(viewport, screenWidth, screenHeight, strength, xSkew, ySkew);
         return CrysisHudEditScreen.HudShape.polygon(
            new float[]{corners[0][0], corners[1][0], corners[3][0], corners[2][0]}, new float[]{corners[0][1], corners[1][1], corners[3][1], corners[2][1]}
         );
      }

      private float[][] transformedCorners(NanoMinimapViewport viewport, int screenWidth, int screenHeight, float strength, float xSkew, float ySkew) {
         float[][] raw = new float[][]{
            {viewport.x(), viewport.y()}, {viewport.maxX(), viewport.y()}, {viewport.x(), viewport.maxY()}, {viewport.maxX(), viewport.maxY()}
         };
         float[] bounds = NanoMinimapHudTransform.transformedBounds(
            viewport, screenWidth / 2, screenHeight / 2, 0.0F, 0.0F, 0.0F, false, strength, 1.0F, 1.0F, xSkew, ySkew, 0.0F, 0.0F, 0.0F
         );
         if (strength <= 1.0E-4F) {
            return raw;
         } else {
            float cx = (viewport.x() + viewport.maxX()) * 0.5F;
            float cy = (viewport.y() + viewport.maxY()) * 0.5F;
            float[][] out = new float[4][2];

            for (int i = 0; i < raw.length; i++) {
               float lx = raw[i][0] - cx;
               float ly = raw[i][1] - cy;
               out[i][0] = cx + lx + ly * xSkew * strength;
               out[i][1] = cy + lx * ySkew * strength + ly;
            }

            return !Float.isFinite(bounds[0]) ? raw : out;
         }
      }

      @Override
      public int configX() {
         return NanoMinimapClientConfig.minimapOffsetX();
      }

      @Override
      public int configY() {
         return NanoMinimapClientConfig.minimapOffsetY();
      }

      @Override
      public void setPosition(int x, int y) {
         NanoMinimapClientConfig.setMinimapOffsetX(CrysisHudEditScreen.clampInt(x, -3000, 3000));
         NanoMinimapClientConfig.setMinimapOffsetY(CrysisHudEditScreen.clampInt(y, -3000, 3000));
      }

      @Override
      public void dragFrom(int startX, int startY, int dx, int dy) {
         this.setPosition(startX + dx, startY - dy);
      }

      @Override
      public double scale() {
         return NanoMinimapClientConfig.minimapScale();
      }

      @Override
      public void setScale(double scale) {
         NanoMinimapClientConfig.setMinimapScale(Mth.clamp(scale, 0.25, 4.0));
      }

      @Override
      public boolean resizable() {
         return true;
      }

      @Override
      public int configWidth() {
         return NanoMinimapClientConfig.minimapWidth();
      }

      @Override
      public int configHeight() {
         return NanoMinimapClientConfig.minimapHeight();
      }

      @Override
      public void setSize(int width, int height) {
         NanoMinimapClientConfig.setMinimapWidth(CrysisHudEditScreen.clampInt(width, 64, 512));
         NanoMinimapClientConfig.setMinimapHeight(CrysisHudEditScreen.clampInt(height, 64, 512));
      }

      @Override
      public void transformFrom(
         CrysisHudEditScreen.TransformHandle handle,
         CrysisHudEditScreen.HudShape startShape,
         int startX,
         int startY,
         int startW,
         int startH,
         double startScale,
         int dx,
         int dy,
         boolean keepAspect
      ) {
         double scale = Math.max(0.25, startScale);
         int localDx = (int)Math.round(dx / scale);
         int localDy = (int)Math.round(dy / scale);
         int newX = startX;
         int newY = startY;
         int newW = startW;
         int newH = startH;
         if (handle.affectsLeft()) {
            newX = startX + dx;
            newW = startW - localDx;
         } else if (handle.affectsRight()) {
            newW = startW + localDx;
         }

         if (handle.affectsTop()) {
            newH = startH - localDy;
         } else if (handle.affectsBottom()) {
            newY = startY - dy;
            newH = startH + localDy;
         }

         if (keepAspect && startW > 0 && startH > 0 && handle.isCorner()) {
            double aspect = (double)startW / startH;
            if (Math.abs(newW - startW) >= Math.abs(newH - startH)) {
               newH = (int)Math.round(newW / aspect);
            } else {
               newW = (int)Math.round(newH * aspect);
            }
         }

         this.setPosition(newX, newY);
         this.setSize(newW, newH);
      }

      @Override
      public List<CrysisHudEditScreen.EditorProperty> properties() {
         return List.of(
            new CrysisHudEditScreen.EditorProperty(
               "perspectiveRight",
               NanoMinimapClientConfig::minimapPerspectiveRightSide,
               NanoMinimapClientConfig::setMinimapPerspectiveRightSide,
               0.02,
               0.005,
               -1.5,
               1.5
            ),
            new CrysisHudEditScreen.EditorProperty(
               "perspectiveAxisX",
               NanoMinimapClientConfig::minimapPerspectiveAxisX,
               NanoMinimapClientConfig::setMinimapPerspectiveAxisX,
               0.02,
               0.005,
               -1.5,
               1.5
            ),
            new CrysisHudEditScreen.EditorProperty(
               "perspectiveAxisY",
               NanoMinimapClientConfig::minimapPerspectiveAxisY,
               NanoMinimapClientConfig::setMinimapPerspectiveAxisY,
               0.02,
               0.005,
               -1.5,
               1.5
            ),
            new CrysisHudEditScreen.EditorProperty(
               "edgeArc", NanoMinimapClientConfig::minimapEdgeArcTopBottom, NanoMinimapClientConfig::setMinimapEdgeArcTopBottom, 0.01, 0.002, -0.5, 0.5
            ),
            new CrysisHudEditScreen.EditorProperty(
               "opacity", NanoMinimapClientConfig::minimapOpacity, NanoMinimapClientConfig::setMinimapOpacity, 0.05, 0.01, 0.05, 1.0
            )
         );
      }

      @Override
      public void reset() {
         NanoMinimapClientConfig.setMinimapOffsetX(40);
         NanoMinimapClientConfig.setMinimapOffsetY(16);
         NanoMinimapClientConfig.setMinimapWidth(150);
         NanoMinimapClientConfig.setMinimapHeight(100);
         NanoMinimapClientConfig.setMinimapScale(0.7);
         NanoMinimapClientConfig.setMinimapOpacity(0.5);
         NanoMinimapClientConfig.setMinimapPerspectiveRightSide(-0.19);
         NanoMinimapClientConfig.setMinimapPerspectiveAxisX(0.07);
         NanoMinimapClientConfig.setMinimapPerspectiveAxisY(0.0);
         NanoMinimapClientConfig.setMinimapEdgeArcTopBottom(-0.09);
      }

      @Override
      public String status() {
         return "x="
            + this.configX()
            + " y="
            + this.configY()
            + " w="
            + this.configWidth()
            + " h="
            + this.configHeight()
            + " scale="
            + CrysisHudEditScreen.fmt(this.scale());
      }
   }

   private abstract static class NanoVisorArrowElement implements CrysisHudEditScreen.EditableHudElement {
      private static final int REFERENCE_W = 1920;
      private static final int REFERENCE_H = 1080;
      private static final int SIZE = 22;

      @Override
      public CrysisHudEditScreen.HudShape shape(int screenWidth, int screenHeight) {
         float scaleX = screenWidth / 1920.0F;
         float scaleY = screenHeight / 1080.0F;
         int x = Math.round(this.configX() * scaleX) - 11;
         int y = Math.round(this.configY() * scaleY) - 11;
         return CrysisHudEditScreen.HudShape.rect(x, y, 22, 22);
      }

      @Override
      public double scale() {
         return 1.0;
      }

      @Override
      public boolean supportsScale() {
         return false;
      }

      @Override
      public void setScale(double scale) {
      }

      @Override
      public void addScale(double delta) {
      }

      @Override
      public void dragFrom(int startX, int startY, int dx, int dy) {
         int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
         int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();
         int refDx = Math.round(dx * (1920.0F / Math.max(1, sw)));
         int refDy = Math.round(dy * (1080.0F / Math.max(1, sh)));
         this.setPosition(startX + refDx, startY + refDy);
      }

      @Override
      public String status() {
         return "refX=" + this.configX() + " refY=" + this.configY();
      }
   }

   private static final class NanoVisorArrowEndElement extends CrysisHudEditScreen.NanoVisorArrowElement {
      @Override
      public String label() {
         return "NanoVisor arrow end";
      }

      @Override
      public int configX() {
         return (int)Math.round(NanoVisorClientConfig.visorZoomArrowEndX());
      }

      @Override
      public int configY() {
         return (int)Math.round(NanoVisorClientConfig.visorZoomArrowEndY());
      }

      @Override
      public void setPosition(int x, int y) {
         NanoVisorClientConfig.setVisorZoomArrowEndX(CrysisHudEditScreen.clampInt(x, -4000, 4000));
         NanoVisorClientConfig.setVisorZoomArrowEndY(CrysisHudEditScreen.clampInt(y, -4000, 4000));
         NanoVisorHudOverlay.resetArrowInterpolation();
      }

      @Override
      public void reset() {
         NanoVisorClientConfig.setVisorZoomArrowEndX(702.0);
         NanoVisorClientConfig.setVisorZoomArrowEndY(493.0);
         NanoVisorHudOverlay.resetArrowInterpolation();
      }
   }

   private static final class NanoVisorArrowStartElement extends CrysisHudEditScreen.NanoVisorArrowElement {
      @Override
      public String label() {
         return "NanoVisor arrow start";
      }

      @Override
      public int configX() {
         return (int)Math.round(NanoVisorClientConfig.visorZoomArrowStartX());
      }

      @Override
      public int configY() {
         return (int)Math.round(NanoVisorClientConfig.visorZoomArrowStartY());
      }

      @Override
      public void setPosition(int x, int y) {
         NanoVisorClientConfig.setVisorZoomArrowStartX(CrysisHudEditScreen.clampInt(x, -4000, 4000));
         NanoVisorClientConfig.setVisorZoomArrowStartY(CrysisHudEditScreen.clampInt(y, -4000, 4000));
         NanoVisorHudOverlay.resetArrowInterpolation();
      }

      @Override
      public void reset() {
         NanoVisorClientConfig.setVisorZoomArrowStartX(702.0);
         NanoVisorClientConfig.setVisorZoomArrowStartY(568.0);
         NanoVisorHudOverlay.resetArrowInterpolation();
      }
   }

   private static final class NanoVisorTextElement implements CrysisHudEditScreen.EditableHudElement {
      private static final int BASE_W = 260;
      private static final int BASE_H = 55;

      @Override
      public String label() {
         return "NanoVisor text";
      }

      @Override
      public CrysisHudEditScreen.HudShape shape(int screenWidth, int screenHeight) {
         double scale = NanoVisorClientConfig.hudScale();
         int w = Math.max(1, Math.round((float)(260.0 * scale)));
         int h = Math.max(1, Math.round((float)(55.0 * scale)));
         int x = Math.round(screenWidth * 0.5F + NanoVisorClientConfig.hudOffsetX() - w * 0.5F);
         int y = Math.round(screenHeight * 0.5F + 22.0F + NanoVisorClientConfig.hudOffsetY() - h * 0.5F);
         return CrysisHudEditScreen.HudShape.rect(x, y, w, h);
      }

      @Override
      public int configX() {
         return NanoVisorClientConfig.hudOffsetX();
      }

      @Override
      public int configY() {
         return NanoVisorClientConfig.hudOffsetY();
      }

      @Override
      public void setPosition(int x, int y) {
         NanoVisorClientConfig.setHudOffsetX(CrysisHudEditScreen.clampInt(x, -2000, 2000));
         NanoVisorClientConfig.setHudOffsetY(CrysisHudEditScreen.clampInt(y, -2000, 2000));
      }

      @Override
      public double scale() {
         return NanoVisorClientConfig.hudScale();
      }

      @Override
      public void setScale(double scale) {
         NanoVisorClientConfig.setHudScale(Mth.clamp(scale, 0.25, 4.0));
      }

      @Override
      public List<CrysisHudEditScreen.EditorProperty> properties() {
         return List.of(
            new CrysisHudEditScreen.EditorProperty("textScale", NanoVisorClientConfig::hudScale, NanoVisorClientConfig::setHudScale, 0.05, 0.01, 0.25, 4.0),
            new CrysisHudEditScreen.EditorProperty("opacity", NanoVisorClientConfig::hudOpacity, NanoVisorClientConfig::setHudOpacity, 0.05, 0.01, 0.0, 1.0)
         );
      }

      @Override
      public void reset() {
         NanoVisorClientConfig.setHudOffsetX(0);
         NanoVisorClientConfig.setHudOffsetY(0);
         NanoVisorClientConfig.setHudScale(0.7);
         NanoVisorClientConfig.setHudOpacity(0.95);
      }
   }

   private abstract static class NanosuitBaseElement implements CrysisHudEditScreen.EditableHudElement {
      static final int ACTIVE_W = 450;
      static final int ACTIVE_H = 180;
      static final int MODES_W = 450;
      static final int MODES_H = 300;
      static final int ANCHOR_PADDING = 12;

      float groupCenterX(int screenWidth) {
         return screenWidth - 12 - 450.0F * NanosuitHudOverlay.getHudScale() * 0.5F + NanosuitHudOverlay.getHudOffsetX();
      }

      float groupCenterY(int screenHeight) {
         return screenHeight - 12 - 180.0F * NanosuitHudOverlay.getHudScale() * 0.5F + NanosuitHudOverlay.getHudOffsetY();
      }

      CrysisHudEditScreen.HudShape sourceBounds(int screenWidth, int screenHeight, int srcW, int srcH) {
         float scale = NanosuitHudOverlay.getHudScale();
         int w = Math.max(1, Math.round(srcW * scale));
         int h = Math.max(1, Math.round(srcH * scale));
         int x = Math.round(this.groupCenterX(screenWidth) - w * 0.5F);
         int y = Math.round(this.groupCenterY(screenHeight) - h * 0.5F);
         return CrysisHudEditScreen.HudShape.rect(x, y, w, h);
      }

      @Override
      public int configX() {
         return NanosuitHudOverlay.getHudOffsetX();
      }

      @Override
      public int configY() {
         return NanosuitHudOverlay.getHudOffsetY();
      }

      @Override
      public void setPosition(int x, int y) {
         NanosuitHudOverlay.setHudOffsetX(CrysisHudEditScreen.clampInt(x, -2000, 2000));
         NanosuitHudOverlay.setHudOffsetY(CrysisHudEditScreen.clampInt(y, -2000, 2000));
      }

      @Override
      public double scale() {
         return NanosuitHudOverlay.getHudScale();
      }

      @Override
      public void setScale(double scale) {
         NanosuitHudOverlay.setHudScale((float)Mth.clamp(scale, 0.05, 5.0));
      }

      @Override
      public List<CrysisHudEditScreen.EditorProperty> properties() {
         return List.of(
            new CrysisHudEditScreen.EditorProperty(
               "hudScale", NanosuitHudOverlay::getHudScale, v -> NanosuitHudOverlay.setHudScale((float)v), 0.05, 0.01, 0.05, 5.0
            )
         );
      }

      @Override
      public void reset() {
         NanosuitHudOverlay.setHudOffsetX(-28);
         NanosuitHudOverlay.setHudOffsetY(-8);
         NanosuitHudOverlay.setHudScale(0.2F);
      }
   }

   private static final class NanosuitEnergyElement extends CrysisHudEditScreen.NanosuitBaseElement {
      @Override
      public String label() {
         return "Nanosuit energy";
      }

      @Override
      public CrysisHudEditScreen.HudShape shape(int screenWidth, int screenHeight) {
         return this.sourceBounds(screenWidth, screenHeight, 450, 180);
      }
   }

   private static final class NanosuitModesElement extends CrysisHudEditScreen.NanosuitBaseElement {
      @Override
      public String label() {
         return "Nanosuit modes";
      }

      @Override
      public CrysisHudEditScreen.HudShape shape(int screenWidth, int screenHeight) {
         return this.sourceBounds(screenWidth, screenHeight, 450, 300);
      }
   }

   private static final class TaczAmmoGunElement extends CrysisHudEditScreen.TaczChildElement {
      @Override
      public String label() {
         return "TaCZ ammo gun";
      }

      @Override
      public CrysisHudEditScreen.HudShape shape(int screenWidth, int screenHeight) {
         return this.localRect(
            screenWidth,
            screenHeight,
            NanosuitClientConfig.taczHudAmmoGunOffsetX(),
            NanosuitClientConfig.taczHudAmmoGunOffsetY(),
            34.0F,
            10.0F,
            NanosuitClientConfig.taczHudAmmoGunScale()
         );
      }

      @Override
      public int configX() {
         return NanosuitClientConfig.taczHudAmmoGunOffsetX();
      }

      @Override
      public int configY() {
         return NanosuitClientConfig.taczHudAmmoGunOffsetY();
      }

      @Override
      public void setPosition(int x, int y) {
         NanosuitClientConfig.setTaczHudAmmoGunOffsetX(CrysisHudEditScreen.clampInt(x, -100, 100));
         NanosuitClientConfig.setTaczHudAmmoGunOffsetY(CrysisHudEditScreen.clampInt(y, -100, 100));
      }

      @Override
      public double scale() {
         return NanosuitClientConfig.taczHudAmmoGunScale();
      }

      @Override
      public void setScale(double scale) {
         NanosuitClientConfig.setTaczHudAmmoGunScale((float)Mth.clamp(scale, 0.2, 5.0));
      }

      @Override
      public void reset() {
         NanosuitClientConfig.setTaczHudAmmoGunOffsetX(47);
         NanosuitClientConfig.setTaczHudAmmoGunOffsetY(1);
         NanosuitClientConfig.setTaczHudAmmoGunScale(1.69F);
      }
   }

   private static final class TaczAmmoInvElement extends CrysisHudEditScreen.TaczChildElement {
      @Override
      public String label() {
         return "TaCZ ammo inventory";
      }

      @Override
      public CrysisHudEditScreen.HudShape shape(int screenWidth, int screenHeight) {
         return this.localRect(
            screenWidth,
            screenHeight,
            NanosuitClientConfig.taczHudAmmoInvOffsetX(),
            NanosuitClientConfig.taczHudAmmoInvOffsetY(),
            34.0F,
            10.0F,
            NanosuitClientConfig.taczHudAmmoInvScale()
         );
      }

      @Override
      public int configX() {
         return NanosuitClientConfig.taczHudAmmoInvOffsetX();
      }

      @Override
      public int configY() {
         return NanosuitClientConfig.taczHudAmmoInvOffsetY();
      }

      @Override
      public void setPosition(int x, int y) {
         NanosuitClientConfig.setTaczHudAmmoInvOffsetX(CrysisHudEditScreen.clampInt(x, -100, 100));
         NanosuitClientConfig.setTaczHudAmmoInvOffsetY(CrysisHudEditScreen.clampInt(y, -100, 100));
      }

      @Override
      public double scale() {
         return NanosuitClientConfig.taczHudAmmoInvScale();
      }

      @Override
      public void setScale(double scale) {
         NanosuitClientConfig.setTaczHudAmmoInvScale((float)Mth.clamp(scale, 0.2, 5.0));
      }

      @Override
      public void reset() {
         NanosuitClientConfig.setTaczHudAmmoInvOffsetX(79);
         NanosuitClientConfig.setTaczHudAmmoInvOffsetY(1);
         NanosuitClientConfig.setTaczHudAmmoInvScale(1.0F);
      }
   }

   private abstract static class TaczBaseElement implements CrysisHudEditScreen.EditableHudElement {
      float groupX(int screenWidth) {
         return NanosuitHudOverlay.getRightHudGroupCenterX(screenWidth) + NanosuitClientConfig.taczHudGroupOffsetX();
      }

      float groupY(int screenHeight) {
         return NanosuitHudOverlay.getRightHudGroupCenterY(screenHeight) + NanosuitClientConfig.taczHudGroupOffsetY();
      }

      float sx() {
         return NanosuitClientConfig.taczHudGroupScaleX();
      }

      float sy() {
         return NanosuitClientConfig.taczHudGroupScaleY();
      }

      CrysisHudEditScreen.HudShape localRect(int screenWidth, int screenHeight, float localX, float localY, float localW, float localH, float localScale) {
         float[][] corners = new float[][]{
            this.transformLocal(screenWidth, screenHeight, localX, localY),
            this.transformLocal(screenWidth, screenHeight, localX + localW * localScale, localY),
            this.transformLocal(screenWidth, screenHeight, localX, localY + localH * localScale),
            this.transformLocal(screenWidth, screenHeight, localX + localW * localScale, localY + localH * localScale)
         };
         return CrysisHudEditScreen.HudShape.polygon(
            new float[]{corners[0][0], corners[1][0], corners[3][0], corners[2][0]}, new float[]{corners[0][1], corners[1][1], corners[3][1], corners[2][1]}
         );
      }

      float[] transformLocal(int screenWidth, int screenHeight, float localX, float localY) {
         float skew = NanosuitClientConfig.taczHudGroupVerticalSkewDown();
         float shearedX = localX + localY * skew;
         float scaledX = shearedX * this.sx();
         float scaledY = localY * this.sy();
         float rotation = NanosuitClientConfig.taczHudGroupRotation() * (float) (Math.PI / 180.0);
         float sin = Mth.sin(rotation);
         float cos = Mth.cos(rotation);
         float rotatedX = scaledX * cos - scaledY * sin;
         float rotatedY = scaledX * sin + scaledY * cos;
         return new float[]{this.groupX(screenWidth) + rotatedX, this.groupY(screenHeight) + rotatedY};
      }

      @Override
      public int configX() {
         return NanosuitClientConfig.taczHudGroupOffsetX();
      }

      @Override
      public int configY() {
         return NanosuitClientConfig.taczHudGroupOffsetY();
      }

      @Override
      public void setPosition(int x, int y) {
         NanosuitClientConfig.setTaczHudGroupOffsetX(CrysisHudEditScreen.clampInt(x, -2000, 2000));
         NanosuitClientConfig.setTaczHudGroupOffsetY(CrysisHudEditScreen.clampInt(y, -2000, 2000));
      }

      @Override
      public double scale() {
         return (NanosuitClientConfig.taczHudGroupScaleX() + NanosuitClientConfig.taczHudGroupScaleY()) * 0.5;
      }

      @Override
      public void setScale(double scale) {
         float clamped = (float)Mth.clamp(scale, 0.1, 4.0);
         NanosuitClientConfig.setTaczHudGroupScaleX(clamped);
         NanosuitClientConfig.setTaczHudGroupScaleY(clamped);
      }

      @Override
      public List<CrysisHudEditScreen.EditorProperty> properties() {
         return List.of(
            new CrysisHudEditScreen.EditorProperty(
               "groupScaleX", NanosuitClientConfig::taczHudGroupScaleX, v -> NanosuitClientConfig.setTaczHudGroupScaleX((float)v), 0.05, 0.01, 0.1, 4.0
            ),
            new CrysisHudEditScreen.EditorProperty(
               "groupScaleY", NanosuitClientConfig::taczHudGroupScaleY, v -> NanosuitClientConfig.setTaczHudGroupScaleY((float)v), 0.05, 0.01, 0.1, 4.0
            ),
            new CrysisHudEditScreen.EditorProperty(
               "rotation", NanosuitClientConfig::taczHudGroupRotation, v -> NanosuitClientConfig.setTaczHudGroupRotation((float)v), 1.0, 0.1, -90.0, 90.0
            ),
            new CrysisHudEditScreen.EditorProperty(
               "verticalSkew",
               NanosuitClientConfig::taczHudGroupVerticalSkewDown,
               v -> NanosuitClientConfig.setTaczHudGroupVerticalSkewDown((float)v),
               0.02,
               0.005,
               -1.2,
               1.2
            )
         );
      }

      @Override
      public void rotateBy(double degrees) {
         NanosuitClientConfig.setTaczHudGroupRotation((float)Mth.clamp(NanosuitClientConfig.taczHudGroupRotation() + degrees, -90.0, 90.0));
      }

      @Override
      public void reset() {
         NanosuitClientConfig.setTaczHudGroupOffsetX(-42);
         NanosuitClientConfig.setTaczHudGroupOffsetY(-30);
         NanosuitClientConfig.setTaczHudGroupScaleX(0.82F);
         NanosuitClientConfig.setTaczHudGroupScaleY(0.82F);
         NanosuitClientConfig.setTaczHudGroupRotation(0.0F);
         NanosuitClientConfig.setTaczHudGroupVerticalSkewDown(0.17F);
      }
   }

   private abstract static class TaczChildElement extends CrysisHudEditScreen.TaczBaseElement {
      @Override
      public boolean childElement() {
         return true;
      }

      @Override
      public void dragFrom(int startX, int startY, int dx, int dy) {
         int localDx = Math.round(dx / Math.max(0.05F, this.sx()));
         int localDy = Math.round(dy / Math.max(0.05F, this.sy()));
         this.setPosition(startX + localDx, startY + localDy);
      }
   }

   private static final class TaczGroupElement extends CrysisHudEditScreen.TaczBaseElement {
      @Override
      public String label() {
         return "TaCZ group";
      }

      @Override
      public CrysisHudEditScreen.HudShape shape(int screenWidth, int screenHeight) {
         return this.localRect(screenWidth, screenHeight, 0.0F, 0.0F, 122.0F, 55.0F, 1.0F);
      }
   }

   private static final class TaczIconElement extends CrysisHudEditScreen.TaczChildElement {
      @Override
      public String label() {
         return "TaCZ icon";
      }

      @Override
      public CrysisHudEditScreen.HudShape shape(int screenWidth, int screenHeight) {
         return this.localRect(
            screenWidth,
            screenHeight,
            NanosuitClientConfig.taczHudIconOffsetX(),
            NanosuitClientConfig.taczHudIconOffsetY(),
            39.0F,
            13.0F,
            NanosuitClientConfig.taczHudIconScale()
         );
      }

      @Override
      public int configX() {
         return NanosuitClientConfig.taczHudIconOffsetX();
      }

      @Override
      public int configY() {
         return NanosuitClientConfig.taczHudIconOffsetY();
      }

      @Override
      public void setPosition(int x, int y) {
         NanosuitClientConfig.setTaczHudIconOffsetX(CrysisHudEditScreen.clampInt(x, -100, 100));
         NanosuitClientConfig.setTaczHudIconOffsetY(CrysisHudEditScreen.clampInt(y, -100, 100));
      }

      @Override
      public double scale() {
         return NanosuitClientConfig.taczHudIconScale();
      }

      @Override
      public void setScale(double scale) {
         NanosuitClientConfig.setTaczHudIconScale((float)Mth.clamp(scale, 0.2, 5.0));
      }

      @Override
      public void reset() {
         NanosuitClientConfig.setTaczHudIconOffsetX(0);
         NanosuitClientConfig.setTaczHudIconOffsetY(1);
         NanosuitClientConfig.setTaczHudIconScale(1.0F);
      }
   }

   private static enum TransformHandle {
      NONE,
      TOP_LEFT,
      TOP,
      TOP_RIGHT,
      RIGHT,
      BOTTOM_RIGHT,
      BOTTOM,
      BOTTOM_LEFT,
      LEFT;

      int[] point(CrysisHudEditScreen.HudShape shape) {
         int cx = shape.x() + shape.w() / 2;
         int cy = shape.y() + shape.h() / 2;

         return switch (this) {
            case TOP_LEFT -> new int[]{shape.x(), shape.y()};
            case TOP -> new int[]{cx, shape.y()};
            case TOP_RIGHT -> new int[]{shape.right(), shape.y()};
            case RIGHT -> new int[]{shape.right(), cy};
            case BOTTOM_RIGHT -> new int[]{shape.right(), shape.bottom()};
            case BOTTOM -> new int[]{cx, shape.bottom()};
            case BOTTOM_LEFT -> new int[]{shape.x(), shape.bottom()};
            case LEFT -> new int[]{shape.x(), cy};
            case NONE -> new int[]{cx, cy};
         };
      }

      boolean affectsLeft() {
         return this == TOP_LEFT || this == LEFT || this == BOTTOM_LEFT;
      }

      boolean affectsRight() {
         return this == TOP_RIGHT || this == RIGHT || this == BOTTOM_RIGHT;
      }

      boolean affectsTop() {
         return this == TOP_LEFT || this == TOP || this == TOP_RIGHT;
      }

      boolean affectsBottom() {
         return this == BOTTOM_LEFT || this == BOTTOM || this == BOTTOM_RIGHT;
      }

      boolean isCorner() {
         return this == TOP_LEFT || this == TOP_RIGHT || this == BOTTOM_RIGHT || this == BOTTOM_LEFT;
      }

      boolean isHorizontalOnly() {
         return this == LEFT || this == RIGHT;
      }

      boolean isVerticalOnly() {
         return this == TOP || this == BOTTOM;
      }
   }

   @FunctionalInterface
   private interface ValueReader {
      double get();
   }

   @FunctionalInterface
   private interface ValueWriter {
      void set(double var1);
   }
}
