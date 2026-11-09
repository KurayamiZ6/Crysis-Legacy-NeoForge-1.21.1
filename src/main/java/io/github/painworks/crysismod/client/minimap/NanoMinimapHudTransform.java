package io.github.painworks.crysismod.client.minimap;

import net.minecraft.util.Mth;

public final class NanoMinimapHudTransform {
   private NanoMinimapHudTransform() {
   }

   public static float[] transformedBounds(
      NanoMinimapViewport viewport,
      int screenCenterX,
      int screenCenterY,
      float offsetX,
      float offsetY,
      float rollDeg,
      boolean applyInertia,
      float perspectiveStrength,
      float perspectiveTopScale,
      float perspectiveBottomScale,
      float perspectiveXSkew,
      float perspectiveYSkew,
      float perspectiveTiltDeg,
      float perspectiveOffsetX,
      float perspectiveOffsetY
   ) {
      float[] c1 = transformPoint(
         viewport.x(),
         viewport.y(),
         viewport,
         screenCenterX,
         screenCenterY,
         offsetX,
         offsetY,
         rollDeg,
         applyInertia,
         perspectiveStrength,
         perspectiveTopScale,
         perspectiveBottomScale,
         perspectiveXSkew,
         perspectiveYSkew,
         perspectiveTiltDeg,
         perspectiveOffsetX,
         perspectiveOffsetY
      );
      float[] c2 = transformPoint(
         viewport.maxX(),
         viewport.y(),
         viewport,
         screenCenterX,
         screenCenterY,
         offsetX,
         offsetY,
         rollDeg,
         applyInertia,
         perspectiveStrength,
         perspectiveTopScale,
         perspectiveBottomScale,
         perspectiveXSkew,
         perspectiveYSkew,
         perspectiveTiltDeg,
         perspectiveOffsetX,
         perspectiveOffsetY
      );
      float[] c3 = transformPoint(
         viewport.x(),
         viewport.maxY(),
         viewport,
         screenCenterX,
         screenCenterY,
         offsetX,
         offsetY,
         rollDeg,
         applyInertia,
         perspectiveStrength,
         perspectiveTopScale,
         perspectiveBottomScale,
         perspectiveXSkew,
         perspectiveYSkew,
         perspectiveTiltDeg,
         perspectiveOffsetX,
         perspectiveOffsetY
      );
      float[] c4 = transformPoint(
         viewport.maxX(),
         viewport.maxY(),
         viewport,
         screenCenterX,
         screenCenterY,
         offsetX,
         offsetY,
         rollDeg,
         applyInertia,
         perspectiveStrength,
         perspectiveTopScale,
         perspectiveBottomScale,
         perspectiveXSkew,
         perspectiveYSkew,
         perspectiveTiltDeg,
         perspectiveOffsetX,
         perspectiveOffsetY
      );
      float minX = Math.min(Math.min(c1[0], c2[0]), Math.min(c3[0], c4[0]));
      float minY = Math.min(Math.min(c1[1], c2[1]), Math.min(c3[1], c4[1]));
      float maxX = Math.max(Math.max(c1[0], c2[0]), Math.max(c3[0], c4[0]));
      float maxY = Math.max(Math.max(c1[1], c2[1]), Math.max(c3[1], c4[1]));
      return new float[]{minX, minY, maxX, maxY};
   }

   public static float[] transformedBounds(NanoMinimapViewport viewport, int screenCenterX, int screenCenterY, float offsetX, float offsetY, float rollDeg) {
      return transformedBounds(viewport, screenCenterX, screenCenterY, offsetX, offsetY, rollDeg, true, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
   }

   private static float[] transformPoint(
      float x,
      float y,
      NanoMinimapViewport viewport,
      int screenCenterX,
      int screenCenterY,
      float offsetX,
      float offsetY,
      float rollDeg,
      boolean applyInertia,
      float perspectiveStrength,
      float perspectiveTopScale,
      float perspectiveBottomScale,
      float perspectiveXSkew,
      float perspectiveYSkew,
      float perspectiveTiltDeg,
      float perspectiveOffsetX,
      float perspectiveOffsetY
   ) {
      float outX = x;
      float outY = y;
      if (applyInertia) {
         float localX = x - screenCenterX;
         float localY = y - screenCenterY;
         if (Math.abs(rollDeg) > 1.0E-4F) {
            float sin = Mth.sin(rollDeg * (float) (Math.PI / 180.0));
            float cos = Mth.cos(rollDeg * (float) (Math.PI / 180.0));
            float rotatedX = localX * cos - localY * sin;
            float rotatedY = localX * sin + localY * cos;
            localX = rotatedX;
            localY = rotatedY;
         }

         outX = screenCenterX + offsetX + localX;
         outY = screenCenterY + offsetY + localY;
      }

      float strength = Mth.clamp(perspectiveStrength, 0.0F, 1.0F);
      if (strength <= 1.0E-4F) {
         return new float[]{outX, outY};
      } else {
         float centerX = (viewport.x() + viewport.maxX()) * 0.5F;
         float centerY = (viewport.y() + viewport.maxY()) * 0.5F;
         float avgScaleX = (perspectiveTopScale + perspectiveBottomScale) * 0.5F;
         float scaleX = Mth.lerp(strength, 1.0F, avgScaleX);
         float topBottomSkew = (perspectiveBottomScale - perspectiveTopScale) * 0.75F;
         float skewX = (perspectiveXSkew + topBottomSkew) * strength;
         float skewY = perspectiveYSkew * strength;
         float tilt = perspectiveTiltDeg * strength;
         float localX = outX - centerX;
         float localY = outY - centerY;
         if (Math.abs(tilt) > 1.0E-4F) {
            float sin = Mth.sin(tilt * (float) (Math.PI / 180.0));
            float cos = Mth.cos(tilt * (float) (Math.PI / 180.0));
            float rotatedX = localX * cos - localY * sin;
            float rotatedY = localX * sin + localY * cos;
            localX = rotatedX;
            localY = rotatedY;
         }

         float transformedX = localX * scaleX + localY * skewX;
         float transformedY = localX * skewY + localY;
         return new float[]{transformedX + centerX + perspectiveOffsetX, transformedY + centerY + perspectiveOffsetY};
      }
   }
}
