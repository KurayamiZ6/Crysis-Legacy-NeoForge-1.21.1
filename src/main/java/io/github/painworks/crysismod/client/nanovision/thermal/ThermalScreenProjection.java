package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.ThermalVisualPreset;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class ThermalScreenProjection {
   public static final float LEGACY_ENCODED_HEIGHT_RANGE_PX = 512.0F;
   public static final float IMPROVED_V2_ENCODED_HEIGHT_RANGE_PX = 1024.0F;

   private ThermalScreenProjection() {
   }

   public static float encodedProjectedHeight(LivingEntity entity) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.gameRenderer != null && entity != null) {
         double distanceToCamera = minecraft.gameRenderer.getMainCamera().getPosition().distanceTo(entity.getBoundingBox().getCenter());
         return encodedProjectedHeight(entity.getBbHeight(), distanceToCamera);
      } else {
         return 0.01F;
      }
   }

   public static float encodedProjectedHeight(double worldHeight, double distanceToCamera) {
      Minecraft minecraft = Minecraft.getInstance();
      int framebufferHeight = minecraft.getMainRenderTarget() != null ? minecraft.getMainRenderTarget().height : minecraft.getWindow().getHeight();
      double focalLengthPx = Math.max(1.0, (double)framebufferHeight) * 0.5 * currentVerticalProjectionScale(minecraft);
      double projectedHeightPx = worldHeight * focalLengthPx / Math.max(distanceToCamera, 0.5);
      return encodeProjectedHeightPixels(projectedHeightPx);
   }

   private static double currentVerticalProjectionScale(Minecraft minecraft) {
      if (RenderSystem.isOnRenderThread()) {
         float projectionScale = Math.abs(RenderSystem.getProjectionMatrix().m11());
         if (Float.isFinite(projectionScale) && projectionScale >= 0.05F && projectionScale <= 100.0F) {
            return projectionScale;
         }
      }

      double verticalFovDegrees = ((Integer)minecraft.options.fov().get()).intValue();
      return 1.0 / Math.tan(Math.toRadians(verticalFovDegrees) * 0.5);
   }

   public static float encodeProjectedHeightPixels(double projectedHeightPx) {
      float range = encodedHeightRangePx();
      return (float)(Mth.clamp(projectedHeightPx, 8.0, range) / range);
   }

   public static float encodedHeightRangePx() {
      return encodedHeightRangePx(NanovisionConfig.thermalVisualPreset());
   }

   public static float encodedHeightRangePx(ThermalVisualPreset preset) {
      return preset != null && preset.usesImprovedV2Model() ? 1024.0F : 512.0F;
   }
}
