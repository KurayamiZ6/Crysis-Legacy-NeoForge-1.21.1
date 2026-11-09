package io.github.painworks.crysismod.client.minimap;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NanoMinimapRenderTrace {
   public long frameIndex;
   public float partialTick;
   public int screenWidth;
   public int screenHeight;
   public double minimapScale;
   public double blocksPerPixel;
   public boolean minimapUseHudInertia;
   public boolean rotateWithPlayer;
   public boolean poseStackInertiaApplied;
   public float inertiaOffsetX;
   public float inertiaOffsetY;
   public float inertiaRollDeg;
   public int baseViewportX;
   public int baseViewportY;
   public int baseViewportWidth;
   public int baseViewportHeight;
   public int finalViewportX;
   public int finalViewportY;
   public int finalViewportWidth;
   public int finalViewportHeight;
   public float terrainDrawX;
   public float terrainDrawY;
   public int terrainDrawWidth;
   public int terrainDrawHeight;
   public double terrainSourceOffsetX;
   public double terrainSourceOffsetY;
   public int terrainCoverageWidth;
   public int terrainCoverageHeight;
   public boolean terrainRounded;
   public int markerLayerBaseX;
   public int markerLayerBaseY;
   public int markerLayerFinalX;
   public int markerLayerFinalY;
   public boolean markerRounded;
   public int frameDrawX;
   public int frameDrawY;
   public int frameDrawWidth;
   public int frameDrawHeight;
   public boolean frameEnabled;
   public boolean frameRounded;
   public int scissorX;
   public int scissorY;
   public int scissorWidth;
   public int scissorHeight;
   public boolean scissorUsesFinalViewport;
   public int terrainSourceU;
   public int terrainSourceV;
   public int terrainSourceRectW;
   public int terrainSourceRectH;
   public int terrainDestX;
   public int terrainDestY;
   public int terrainDestW;
   public int terrainDestH;
   public boolean debugFreezeHudInertia;
   public boolean debugFreezeMapWorldMovement;
   public boolean debugFreezeTerrainSourceOffset;
   public boolean debugDisablePoseStackInMinimap;
   public boolean debugForceManualViewportCoordinates;
   public boolean perspectiveEnabled;
   public float perspectiveStrength;
   public float perspectiveTopScale;
   public float perspectiveBottomScale;
   public float perspectiveSkewX;
   public float perspectiveSkewY;
   public float perspectiveTiltDeg;
   public int perspectiveOffsetX;
   public int perspectiveOffsetY;
   public float edgeArcTopBottom;
   public boolean perspectiveDebugBounds;

   public void reset() {
      this.frameIndex = 0L;
      this.partialTick = 0.0F;
      this.screenWidth = 0;
      this.screenHeight = 0;
      this.minimapScale = 1.0;
      this.blocksPerPixel = 0.0;
      this.minimapUseHudInertia = false;
      this.rotateWithPlayer = false;
      this.poseStackInertiaApplied = false;
      this.inertiaOffsetX = 0.0F;
      this.inertiaOffsetY = 0.0F;
      this.inertiaRollDeg = 0.0F;
      this.baseViewportX = 0;
      this.baseViewportY = 0;
      this.baseViewportWidth = 0;
      this.baseViewportHeight = 0;
      this.finalViewportX = 0;
      this.finalViewportY = 0;
      this.finalViewportWidth = 0;
      this.finalViewportHeight = 0;
      this.terrainDrawX = 0.0F;
      this.terrainDrawY = 0.0F;
      this.terrainDrawWidth = 0;
      this.terrainDrawHeight = 0;
      this.terrainSourceOffsetX = 0.0;
      this.terrainSourceOffsetY = 0.0;
      this.terrainCoverageWidth = 0;
      this.terrainCoverageHeight = 0;
      this.terrainRounded = false;
      this.markerLayerBaseX = 0;
      this.markerLayerBaseY = 0;
      this.markerLayerFinalX = 0;
      this.markerLayerFinalY = 0;
      this.markerRounded = false;
      this.frameDrawX = 0;
      this.frameDrawY = 0;
      this.frameDrawWidth = 0;
      this.frameDrawHeight = 0;
      this.frameEnabled = false;
      this.frameRounded = false;
      this.scissorX = 0;
      this.scissorY = 0;
      this.scissorWidth = 0;
      this.scissorHeight = 0;
      this.scissorUsesFinalViewport = false;
      this.terrainSourceU = 0;
      this.terrainSourceV = 0;
      this.terrainSourceRectW = 0;
      this.terrainSourceRectH = 0;
      this.terrainDestX = 0;
      this.terrainDestY = 0;
      this.terrainDestW = 0;
      this.terrainDestH = 0;
      this.debugFreezeHudInertia = false;
      this.debugFreezeMapWorldMovement = false;
      this.debugFreezeTerrainSourceOffset = false;
      this.debugDisablePoseStackInMinimap = false;
      this.debugForceManualViewportCoordinates = false;
      this.perspectiveEnabled = false;
      this.perspectiveStrength = 0.0F;
      this.perspectiveTopScale = 1.0F;
      this.perspectiveBottomScale = 1.0F;
      this.perspectiveSkewX = 0.0F;
      this.perspectiveSkewY = 0.0F;
      this.perspectiveTiltDeg = 0.0F;
      this.perspectiveOffsetX = 0;
      this.perspectiveOffsetY = 0;
      this.edgeArcTopBottom = 0.0F;
      this.perspectiveDebugBounds = false;
   }

   public List<String> briefLines() {
      ArrayList<String> lines = new ArrayList<>(8);
      lines.add(
         String.format(Locale.ROOT, "MM trace frame:%d pt:%.3f bpp:%.4f scale:%.3f", this.frameIndex, this.partialTick, this.blocksPerPixel, this.minimapScale)
      );
      lines.add(
         String.format(
            Locale.ROOT,
            "MM trace vp base:%d,%d %dx%d final:%d,%d %dx%d",
            this.baseViewportX,
            this.baseViewportY,
            this.baseViewportWidth,
            this.baseViewportHeight,
            this.finalViewportX,
            this.finalViewportY,
            this.finalViewportWidth,
            this.finalViewportHeight
         )
      );
      lines.add(
         String.format(
            Locale.ROOT,
            "MM trace inertia %.3f,%.3f roll:%.3f pose:%s",
            this.inertiaOffsetX,
            this.inertiaOffsetY,
            this.inertiaRollDeg,
            this.poseStackInertiaApplied
         )
      );
      lines.add(
         String.format(
            Locale.ROOT,
            "MM trace terrain draw:%.2f,%.2f %dx%d srcOff:%.3f,%.3f",
            this.terrainDrawX,
            this.terrainDrawY,
            this.terrainDrawWidth,
            this.terrainDrawHeight,
            this.terrainSourceOffsetX,
            this.terrainSourceOffsetY
         )
      );
      lines.add(
         String.format(
            Locale.ROOT,
            "MM trace frame draw:%d,%d %dx%d marker:%d,%d",
            this.frameDrawX,
            this.frameDrawY,
            this.frameDrawWidth,
            this.frameDrawHeight,
            this.markerLayerFinalX,
            this.markerLayerFinalY
         )
      );
      lines.add(
         String.format(
            Locale.ROOT,
            "MM trace scissor:%d,%d %dx%d usesFinal:%s",
            this.scissorX,
            this.scissorY,
            this.scissorWidth,
            this.scissorHeight,
            this.scissorUsesFinalViewport
         )
      );
      lines.add(
         String.format(
            Locale.ROOT,
            "MM trace delta terrain-frame:%.3f,%.3f terrain-marker:%.3f,%.3f",
            this.terrainDrawX - this.frameDrawX,
            this.terrainDrawY - this.frameDrawY,
            this.terrainDrawX - this.markerLayerFinalX,
            this.terrainDrawY - this.markerLayerFinalY
         )
      );
      lines.add(
         String.format(
            Locale.ROOT,
            "MM trace persp en:%s s:%.2f top:%.2f bottom:%.2f skew:%.2f/%.2f tilt:%.2f off:%d,%d arc:%.2f dbg:%s",
            this.perspectiveEnabled,
            this.perspectiveStrength,
            this.perspectiveTopScale,
            this.perspectiveBottomScale,
            this.perspectiveSkewX,
            this.perspectiveSkewY,
            this.perspectiveTiltDeg,
            this.perspectiveOffsetX,
            this.perspectiveOffsetY,
            this.edgeArcTopBottom,
            this.perspectiveDebugBounds
         )
      );
      lines.add(
         String.format(
            Locale.ROOT,
            "MM trace debug freezeHud:%s freezeMap:%s freezeSrc:%s noPose:%s manual:%s",
            this.debugFreezeHudInertia,
            this.debugFreezeMapWorldMovement,
            this.debugFreezeTerrainSourceOffset,
            this.debugDisablePoseStackInMinimap,
            this.debugForceManualViewportCoordinates
         )
      );
      return lines;
   }
}
