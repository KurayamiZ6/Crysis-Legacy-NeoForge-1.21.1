package io.github.painworks.crysismod.client.nanovision.thermal;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class ThermalCameraVisibility {
   private static final double FOV_MARGIN_DEGREES = 18.0;
   private static final double MIN_VERTICAL_FOV_DEGREES = 30.0;
   private static final double MAX_VERTICAL_FOV_DEGREES = 165.0;
   private static final double BOUNDS_PADDING_BLOCKS = 0.75;
   private static final double MIN_FORWARD_DISTANCE = 0.05;

   private ThermalCameraVisibility() {
   }

   static ThermalCameraVisibility.View capture(Minecraft minecraft, Camera camera) {
      if (minecraft != null && camera != null) {
         Vec3 position = camera.getPosition();
         double yawRadians = Math.toRadians(camera.getYRot());
         double pitchRadians = Math.toRadians(camera.getXRot());
         double cosPitch = Math.cos(pitchRadians);
         double forwardX = -Math.sin(yawRadians) * cosPitch;
         double forwardY = -Math.sin(pitchRadians);
         double forwardZ = Math.cos(yawRadians) * cosPitch;
         double rightX = Math.cos(yawRadians);
         double rightY = 0.0;
         double rightZ = Math.sin(yawRadians);
         double upX = forwardY * rightZ - forwardZ * rightY;
         double upY = forwardZ * rightX - forwardX * rightZ;
         double upZ = forwardX * rightY - forwardY * rightX;
         double configuredFov = ((Integer)minecraft.options.fov().get()).intValue();
         double verticalFov = Mth.clamp(configuredFov + 18.0, 30.0, 165.0);
         double tanHalfVertical = Math.tan(Math.toRadians(verticalFov) * 0.5);
         int framebufferWidth = minecraft.getMainRenderTarget() != null ? minecraft.getMainRenderTarget().width : minecraft.getWindow().getWidth();
         int framebufferHeight = minecraft.getMainRenderTarget() != null ? minecraft.getMainRenderTarget().height : minecraft.getWindow().getHeight();
         double aspectRatio = (double)Math.max(framebufferWidth, 1) / Math.max(framebufferHeight, 1);
         double tanHalfHorizontal = tanHalfVertical * Mth.clamp(aspectRatio, 0.25, 4.0);
         boolean valid = position != null
            && finite(position.x, position.y, position.z)
            && finite(forwardX, forwardY, forwardZ)
            && finite(rightX, rightY, rightZ)
            && finite(upX, upY, upZ)
            && Double.isFinite(tanHalfVertical)
            && Double.isFinite(tanHalfHorizontal)
            && tanHalfVertical > 0.0
            && tanHalfHorizontal > 0.0;
         return !valid
            ? ThermalCameraVisibility.View.failOpen()
            : new ThermalCameraVisibility.View(
               position.x,
               position.y,
               position.z,
               forwardX,
               forwardY,
               forwardZ,
               rightX,
               rightY,
               rightZ,
               upX,
               upY,
               upZ,
               tanHalfHorizontal,
               tanHalfVertical,
               true
            );
      } else {
         return ThermalCameraVisibility.View.failOpen();
      }
   }

   private static boolean finite(double x, double y, double z) {
      return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
   }

   record View(
      double cameraX,
      double cameraY,
      double cameraZ,
      double forwardX,
      double forwardY,
      double forwardZ,
      double rightX,
      double rightY,
      double rightZ,
      double upX,
      double upY,
      double upZ,
      double tanHalfHorizontal,
      double tanHalfVertical,
      boolean valid
   ) {
      private static ThermalCameraVisibility.View failOpen() {
         return new ThermalCameraVisibility.View(0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0, false);
      }

      boolean isVisible(AABB bounds) {
         if (this.valid && bounds != null && !bounds.hasNaN()) {
            AABB padded = bounds.inflate(0.75);
            double centerX = (padded.minX + padded.maxX) * 0.5;
            double centerY = (padded.minY + padded.maxY) * 0.5;
            double centerZ = (padded.minZ + padded.maxZ) * 0.5;
            double halfX = (padded.maxX - padded.minX) * 0.5;
            double halfY = (padded.maxY - padded.minY) * 0.5;
            double halfZ = (padded.maxZ - padded.minZ) * 0.5;
            double radius = Math.sqrt(halfX * halfX + halfY * halfY + halfZ * halfZ);
            double offsetX = centerX - this.cameraX;
            double offsetY = centerY - this.cameraY;
            double offsetZ = centerZ - this.cameraZ;
            double forwardDistance = dot(offsetX, offsetY, offsetZ, this.forwardX, this.forwardY, this.forwardZ);
            if (forwardDistance + radius <= 0.05) {
               return false;
            } else {
               double horizontalDistance = Math.abs(dot(offsetX, offsetY, offsetZ, this.rightX, this.rightY, this.rightZ));
               double verticalDistance = Math.abs(dot(offsetX, offsetY, offsetZ, this.upX, this.upY, this.upZ));
               double positiveDepth = Math.max(forwardDistance, 0.0);
               return horizontalDistance <= positiveDepth * this.tanHalfHorizontal + radius
                  && verticalDistance <= positiveDepth * this.tanHalfVertical + radius;
            }
         } else {
            return true;
         }
      }

      private static double dot(double ax, double ay, double az, double bx, double by, double bz) {
         return ax * bx + ay * by + az * bz;
      }
   }
}
