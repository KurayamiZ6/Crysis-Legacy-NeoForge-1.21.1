package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.energy.INanosuitEnergy;

import com.mojang.math.Axis;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanosuitPerspectiveDebugOverlay {
   private static final int DEFAULT_OFFSET_X = -56;
   private static final int DEFAULT_OFFSET_Y = -8;
   private static final float DEFAULT_SCALE_X = 0.89533126F;
   private static final float DEFAULT_SCALE_Y = 0.9F;
   private static final float DEFAULT_ROTATION_DEG = 0.0F;
   private static final float DEFAULT_TILT = 0.8F;
   private static final float DEFAULT_VERTICAL_SKEW_DOWN = 0.16084337F;
   private static final float DEFAULT_TEXT_SIZE = 1.1667922F;
   private static final float DEFAULT_LAYER_SPACING = 0.0F;
   private static final float DEFAULT_ENERGY_PERCENT_COLOR_R = 0.6039216F;
   private static final float DEFAULT_ENERGY_PERCENT_COLOR_G = 0.9843137F;
   private static final float DEFAULT_ENERGY_PERCENT_COLOR_B = 1.0F;
   private static final float DEFAULT_ENERGY_PERCENT_ALPHA = 1.0F;
   private static final int DEPTH_LAYERS = 6;
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
   private static final float MIN_LAYER_SPACING = 0.0F;
   private static final float MAX_LAYER_SPACING = 6.0F;
   private static int offsetX = -56;
   private static int offsetY = -8;
   private static float scaleX = 0.89533126F;
   private static float scaleY = 0.9F;
   private static float rotationDeg = 0.0F;
   private static float tilt = 0.8F;
   private static float verticalSkewDown = 0.16084337F;
   private static float textSize = 1.1667922F;
   private static float layerSpacing = 0.0F;
   private static float energyPercentColorR = 0.6039216F;
   private static float energyPercentColorG = 0.9843137F;
   private static float energyPercentColorB = 1.0F;
   private static float energyPercentAlpha = 1.0F;
   private static boolean configInitialized;

   private NanosuitPerspectiveDebugOverlay() {
   }

   @SubscribeEvent
   public static void onRenderGuiOverlay(Post event) {
      if (Nanosuit2Hud.equipped()) return;
      if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer player = minecraft.player;
         if (player != null && NanosuitArmorUtil.getEquippedProfile(player)
            == io.github.painworks.crysismod.nanosuit.NanosuitProfile.NANOSUIT_1) return;
         if (player != null && !minecraft.options.hideGui && NanosuitArmorUtil.hasFullSet(player)) {
            if (!configInitialized) {
               loadFromConfig();
            }

            int energyPercent = resolveEnergyPercent(player);
            int clampedPercent = Mth.clamp(energyPercent, 0, 100);
            String energyText = String.format(Locale.ROOT, "%03d%%", clampedPercent);
            GuiGraphics guiGraphics = event.getGuiGraphics();
            int screenWidth = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenHeight = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledHeight();
            int screenCenterX = screenWidth / 2;
            int screenCenterY = screenHeight / 2;
            float centerX = NanosuitHudOverlay.getRightHudGroupCenterX(screenWidth) + offsetX;
            float centerY = NanosuitHudOverlay.getRightHudGroupCenterY(screenHeight) + offsetY;
            int textWidth = minecraft.font.width(energyText);
            int lineHeight = 9;
            float baseX = -textWidth * 0.5F;
            float baseY = -lineHeight * 0.5F;
            guiGraphics.pose().pushPose();
            NanosuitHudMasterCompositor.prepareSharedHudFrame(event);
            NanosuitHudOverlay.applySharedRightHudInertiaTransform(guiGraphics, screenCenterX, screenCenterY);
            guiGraphics.pose().translate(centerX, centerY, 0.0F);
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(rotationDeg));
            guiGraphics.pose().scale(textSize * scaleX, textSize * scaleY, 1.0F);
            applyVerticalSkewDown(guiGraphics, baseX);
            int mainColor = toArgb(energyPercentColorR, energyPercentColorG, energyPercentColorB, energyPercentAlpha);
            int highlightColor = toArgb(energyPercentColorR, energyPercentColorG, energyPercentColorB, energyPercentAlpha * 0.3137255F);

            for (int layer = 6; layer >= 1; layer--) {
               float depth = layer * layerSpacing;
               float layerShiftX = tilt * depth;
               float layerShiftY = -0.55F * depth;
               float layerAlpha = Mth.clamp((24 + layer * 18) / 255.0F, 0.0F, 0.8627451F) * energyPercentAlpha;
               int color = toArgb(energyPercentColorR * 0.18F, energyPercentColorG * 0.18F, energyPercentColorB * 0.18F, layerAlpha);
               guiGraphics.drawString(minecraft.font, energyText, Math.round(baseX + layerShiftX), Math.round(baseY + layerShiftY), color, false);
            }

            guiGraphics.drawString(minecraft.font, energyText, Math.round(baseX), Math.round(baseY), mainColor, false);
            guiGraphics.drawString(minecraft.font, energyText, Math.round(baseX + tilt * 0.6F), Math.round(baseY - 1.0F), highlightColor, false);
            guiGraphics.pose().popPose();
         }
      }
   }

   private static void applyVerticalSkewDown(GuiGraphics guiGraphics, float pivotX) {
      if (!(Math.abs(verticalSkewDown) < 1.0E-4F)) {
         Matrix4f shear = new Matrix4f().identity().m01(verticalSkewDown);
         guiGraphics.pose().translate(-pivotX, 0.0F, 0.0F);
         guiGraphics.pose().mulPose(shear);
         guiGraphics.pose().translate(pivotX, 0.0F, 0.0F);
      }
   }

   private static int resolveEnergyPercent(LocalPlayer player) {
      float energy = java.util.Optional.<INanosuitEnergy>of(NanosuitCapabilities.get(player)).map(store -> store.getEnergy()).orElse(0.0F);
      float normalized = Mth.clamp(energy / 100.0F, 0.0F, 1.0F);
      return Mth.clamp(Math.round(normalized * 100.0F), 0, 100);
   }

   private static int toArgb(float red, float green, float blue, float alpha) {
      int a = (int)(Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F);
      int r = (int)(Mth.clamp(red, 0.0F, 1.0F) * 255.0F);
      int g = (int)(Mth.clamp(green, 0.0F, 1.0F) * 255.0F);
      int b = (int)(Mth.clamp(blue, 0.0F, 1.0F) * 255.0F);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static void loadFromConfig() {
      offsetX = Mth.clamp(NanosuitClientConfig.perspectiveDebugOffsetX(), -500, 500);
      offsetY = Mth.clamp(NanosuitClientConfig.perspectiveDebugOffsetY(), -500, 500);
      scaleX = Mth.clamp(NanosuitClientConfig.perspectiveDebugScaleX(), 0.2F, 4.0F);
      scaleY = Mth.clamp(NanosuitClientConfig.perspectiveDebugScaleY(), 0.2F, 4.0F);
      rotationDeg = Mth.clamp(NanosuitClientConfig.perspectiveDebugRotation(), -80.0F, 80.0F);
      tilt = Mth.clamp(NanosuitClientConfig.perspectiveDebugTilt(), -3.0F, 3.0F);
      verticalSkewDown = Mth.clamp(NanosuitClientConfig.perspectiveDebugVerticalSkewDown(), -1.2F, 1.2F);
      textSize = Mth.clamp(NanosuitClientConfig.perspectiveDebugTextSize(), 0.5F, 6.0F);
      layerSpacing = Mth.clamp(NanosuitClientConfig.perspectiveDebugLayerSpacing(), 0.0F, 6.0F);
      energyPercentColorR = Mth.clamp(NanosuitClientConfig.perspectiveDebugEnergyPercentColorR(), 0.0F, 1.0F);
      energyPercentColorG = Mth.clamp(NanosuitClientConfig.perspectiveDebugEnergyPercentColorG(), 0.0F, 1.0F);
      energyPercentColorB = Mth.clamp(NanosuitClientConfig.perspectiveDebugEnergyPercentColorB(), 0.0F, 1.0F);
      energyPercentAlpha = Mth.clamp(NanosuitClientConfig.perspectiveDebugEnergyPercentAlpha(), 0.0F, 1.0F);
      configInitialized = true;
   }

   public static int getOffsetX() {
      return offsetX;
   }

   public static int getOffsetY() {
      return offsetY;
   }

   public static float getScaleX() {
      return scaleX;
   }

   public static float getScaleY() {
      return scaleY;
   }

   public static float getRotationDeg() {
      return rotationDeg;
   }

   public static float getTilt() {
      return tilt;
   }

   public static float getTextSize() {
      return textSize;
   }

   public static float getVerticalSkewDown() {
      return verticalSkewDown;
   }

   public static float getLayerSpacing() {
      return layerSpacing;
   }

   public static float getEnergyPercentColorR() {
      return energyPercentColorR;
   }

   public static float getEnergyPercentColorG() {
      return energyPercentColorG;
   }

   public static float getEnergyPercentColorB() {
      return energyPercentColorB;
   }

   public static float getEnergyPercentAlpha() {
      return energyPercentAlpha;
   }

   public static String getEnergyPercentColorHex() {
      int argb = toArgb(energyPercentColorR, energyPercentColorG, energyPercentColorB, energyPercentAlpha);
      return String.format(Locale.ROOT, "#%08x", argb);
   }

   public static String defaultEnergyPercentColorHex() {
      int argb = toArgb(0.6039216F, 0.9843137F, 1.0F, 1.0F);
      return String.format(Locale.ROOT, "#%08x", argb);
   }

   public static void setOffsetX(int value) {
      offsetX = Mth.clamp(value, -500, 500);
   }

   public static void setOffsetY(int value) {
      offsetY = Mth.clamp(value, -500, 500);
   }

   public static void persistAllToConfig() {
      NanosuitClientConfig.setPerspectiveDebugOffsetX(offsetX);
      NanosuitClientConfig.setPerspectiveDebugOffsetY(offsetY);
      NanosuitClientConfig.setPerspectiveDebugScaleX(scaleX);
      NanosuitClientConfig.setPerspectiveDebugScaleY(scaleY);
      NanosuitClientConfig.setPerspectiveDebugRotation(rotationDeg);
      NanosuitClientConfig.setPerspectiveDebugTilt(tilt);
      NanosuitClientConfig.setPerspectiveDebugVerticalSkewDown(verticalSkewDown);
      NanosuitClientConfig.setPerspectiveDebugTextSize(textSize);
      NanosuitClientConfig.setPerspectiveDebugLayerSpacing(layerSpacing);
      NanosuitClientConfig.setPerspectiveDebugEnergyPercentColorR(energyPercentColorR);
      NanosuitClientConfig.setPerspectiveDebugEnergyPercentColorG(energyPercentColorG);
      NanosuitClientConfig.setPerspectiveDebugEnergyPercentColorB(energyPercentColorB);
      NanosuitClientConfig.setPerspectiveDebugEnergyPercentAlpha(energyPercentAlpha);
   }

   public static void setScaleX(float value) {
      scaleX = Mth.clamp(value, 0.2F, 4.0F);
   }

   public static void setScaleY(float value) {
      scaleY = Mth.clamp(value, 0.2F, 4.0F);
   }

   public static void setRotationDeg(float value) {
      rotationDeg = Mth.clamp(value, -80.0F, 80.0F);
   }

   public static void setTilt(float value) {
      tilt = Mth.clamp(value, -3.0F, 3.0F);
   }

   public static void setVerticalSkewDown(float value) {
      verticalSkewDown = Mth.clamp(value, -1.2F, 1.2F);
   }

   public static void setTextSize(float value) {
      textSize = Mth.clamp(value, 0.5F, 6.0F);
   }

   public static void setLayerSpacing(float value) {
      layerSpacing = Mth.clamp(value, 0.0F, 6.0F);
   }

   public static void setEnergyPercentColorR(float value) {
      energyPercentColorR = Mth.clamp(value, 0.0F, 1.0F);
   }

   public static void setEnergyPercentColorG(float value) {
      energyPercentColorG = Mth.clamp(value, 0.0F, 1.0F);
   }

   public static void setEnergyPercentColorB(float value) {
      energyPercentColorB = Mth.clamp(value, 0.0F, 1.0F);
   }

   public static void setEnergyPercentAlpha(float value) {
      energyPercentAlpha = Mth.clamp(value, 0.0F, 1.0F);
   }

   public static void setEnergyPercentColorHex(String value) {
      if (value != null) {
         String raw = value.trim();
         if (raw.length() == 9 && raw.charAt(0) == '#') {
            int argb;
            try {
               argb = (int)Long.parseLong(raw.substring(1), 16);
            } catch (NumberFormatException var7) {
               return;
            }

            float alpha = (argb >>> 24 & 0xFF) / 255.0F;
            float red = (argb >>> 16 & 0xFF) / 255.0F;
            float green = (argb >>> 8 & 0xFF) / 255.0F;
            float blue = (argb & 0xFF) / 255.0F;
            setEnergyPercentColorR(red);
            setEnergyPercentColorG(green);
            setEnergyPercentColorB(blue);
            setEnergyPercentAlpha(alpha);
            persistAllToConfig();
         }
      }
   }

   public static void resetDefaults() {
      setOffsetX(-56);
      setOffsetY(-8);
      setScaleX(0.89533126F);
      setScaleY(0.9F);
      setRotationDeg(0.0F);
      setTilt(0.8F);
      setVerticalSkewDown(0.16084337F);
      setTextSize(1.1667922F);
      setLayerSpacing(0.0F);
      setEnergyPercentColorR(0.6039216F);
      setEnergyPercentColorG(0.9843137F);
      setEnergyPercentColorB(1.0F);
      setEnergyPercentAlpha(1.0F);
   }
}
