package io.github.painworks.crysismod.client.minimap;

import net.minecraft.util.Mth;

public final class NanoMinimapCoordinateMapper {
   private final double centerX;
   private final double centerZ;
   private final double blocksPerPixel;
   private final int width;
   private final int height;

   public NanoMinimapCoordinateMapper(double centerX, double centerZ, double blocksPerPixel, int width, int height) {
      this.centerX = centerX;
      this.centerZ = centerZ;
      this.blocksPerPixel = Math.max(1.0E-6, blocksPerPixel);
      this.width = Math.max(1, width);
      this.height = Math.max(1, height);
   }

   public NanoMinimapCoordinateMapper.Point worldToLocal(double worldX, double worldZ) {
      double dx = worldX - this.centerX;
      double dz = worldZ - this.centerZ;
      double x = this.centerLocalX() + dx / this.blocksPerPixel;
      double y = this.centerLocalY() + dz / this.blocksPerPixel;
      return new NanoMinimapCoordinateMapper.Point(x, y);
   }

   public NanoMinimapCoordinateMapper.Point playerFractionalOffset(double playerX, double playerZ) {
      double dx = this.centerX - playerX;
      double dz = this.centerZ - playerZ;
      double pxPerBlock = 1.0 / this.blocksPerPixel;
      return new NanoMinimapCoordinateMapper.Point(dx * pxPerBlock, dz * pxPerBlock);
   }

   public NanoMinimapCoordinateMapper.Point worldToViewport(
      double worldX, double worldZ, double fractionalOffsetX, double fractionalOffsetY, boolean rotateWithPlayer, float playerYawDeg
   ) {
      NanoMinimapCoordinateMapper.Point local = this.worldToLocal(worldX, worldZ);
      double lx = local.x + fractionalOffsetX;
      double ly = local.y + fractionalOffsetY;
      if (!rotateWithPlayer) {
         return new NanoMinimapCoordinateMapper.Point(lx, ly);
      } else {
         double cx = this.centerLocalX();
         double cy = this.centerLocalY();
         double headingRotationDeg = 180.0 - playerYawDeg;
         double angleRad = headingRotationDeg * (float) (Math.PI / 180.0);
         double cos = Math.cos(angleRad);
         double sin = Math.sin(angleRad);
         double relX = lx - cx;
         double relY = ly - cy;
         double rx = relX * cos - relY * sin;
         double ry = relX * sin + relY * cos;
         return new NanoMinimapCoordinateMapper.Point(cx + rx, cy + ry);
      }
   }

   public void worldToViewport(
      double worldX,
      double worldZ,
      double fractionalOffsetX,
      double fractionalOffsetY,
      boolean rotateWithPlayer,
      float playerYawDeg,
      NanoMinimapCoordinateMapper.MutablePoint out
   ) {
      if (out != null) {
         double dx = worldX - this.centerX;
         double dz = worldZ - this.centerZ;
         double lx = this.centerLocalX() + dx / this.blocksPerPixel + fractionalOffsetX;
         double ly = this.centerLocalY() + dz / this.blocksPerPixel + fractionalOffsetY;
         if (!rotateWithPlayer) {
            out.x = lx;
            out.y = ly;
         } else {
            double cx = this.centerLocalX();
            double cy = this.centerLocalY();
            double headingRotationDeg = 180.0 - playerYawDeg;
            double angleRad = headingRotationDeg * (float) (Math.PI / 180.0);
            double cos = Math.cos(angleRad);
            double sin = Math.sin(angleRad);
            double relX = lx - cx;
            double relY = ly - cy;
            out.x = cx + (relX * cos - relY * sin);
            out.y = cy + (relX * sin + relY * cos);
         }
      }
   }

   public boolean isInsideViewport(NanoMinimapCoordinateMapper.Point localPoint) {
      return localPoint.x >= 0.0 && localPoint.y >= 0.0 && localPoint.x <= this.width && localPoint.y <= this.height;
   }

   public boolean isInsideViewport(double x, double y) {
      return x >= 0.0 && y >= 0.0 && x <= this.width && y <= this.height;
   }

   public NanoMinimapCoordinateMapper.Point clampToViewport(NanoMinimapCoordinateMapper.Point localPoint, double edgePaddingPixels) {
      double padding = Math.max(0.0, edgePaddingPixels);
      double maxX = Math.max(padding, this.width - padding);
      double maxY = Math.max(padding, this.height - padding);
      double clampedX = Mth.clamp(localPoint.x, padding, maxX);
      double clampedY = Mth.clamp(localPoint.y, padding, maxY);
      return new NanoMinimapCoordinateMapper.Point(clampedX, clampedY);
   }

   public void clampToViewport(double x, double y, double edgePaddingPixels, NanoMinimapCoordinateMapper.MutablePoint out) {
      if (out != null) {
         double padding = Math.max(0.0, edgePaddingPixels);
         double maxX = Math.max(padding, this.width - padding);
         double maxY = Math.max(padding, this.height - padding);
         out.x = Mth.clamp(x, padding, maxX);
         out.y = Mth.clamp(y, padding, maxY);
      }
   }

   private double centerLocalX() {
      return this.width / 2;
   }

   private double centerLocalY() {
      return this.height / 2;
   }

   public static final class MutablePoint {
      public double x;
      public double y;

      public MutablePoint() {
      }

      public MutablePoint(double x, double y) {
         this.x = x;
         this.y = y;
      }
   }

   public record Point(double x, double y) {
   }
}
