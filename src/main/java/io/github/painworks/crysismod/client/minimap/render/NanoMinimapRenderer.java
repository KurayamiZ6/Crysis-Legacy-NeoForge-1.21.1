package io.github.painworks.crysismod.client.minimap.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import io.github.painworks.crysismod.client.minimap.NanoMinimapCoordinateMapper;
import io.github.painworks.crysismod.client.minimap.NanoMinimapHudTransform;
import io.github.painworks.crysismod.client.minimap.NanoMinimapRenderTrace;
import io.github.painworks.crysismod.client.minimap.NanoMinimapViewport;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import io.github.painworks.crysismod.client.minimap.config.NanoMinimapClientConfig;
import io.github.painworks.crysismod.client.minimap.marker.NanoMinimapEntityIcon;
import io.github.painworks.crysismod.client.minimap.marker.NanoMinimapEntityIconAtlas;
import io.github.painworks.crysismod.client.minimap.marker.NanoMinimapEntityIconRegistry;
import io.github.painworks.crysismod.client.minimap.marker.NanoMinimapMarker;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public final class NanoMinimapRenderer {
   private static final double MARKER_EDGE_PADDING = 2.0;
   private static final float MARKED_ICON_FRAME_WIDTH_SCALE = 0.7F;
   private static final int MARKED_ENTITY_HIGHLIGHT_RGB = 16777215;
   private final NanoMinimapEntityIconRegistry entityIconRegistry = new NanoMinimapEntityIconRegistry();

   public void render(
      GuiGraphics guiGraphics,
      NanoMinimapViewport viewport,
      ResourceLocation terrainTextureId,
      boolean hasTerrainTexture,
      int terrainSourceSizePixels,
      int terrainTextureResolution,
      NanoMinimapCoordinateMapper mapper,
      double terrainOffsetX,
      double terrainOffsetY,
      double markerOffsetX,
      double markerOffsetY,
      boolean rotateWithPlayer,
      float playerYawDeg,
      List<NanoMinimapMarker> markers,
      boolean clampMarkersToBounds,
      float markerScale,
      float markerOpacity,
      boolean useEntityIcons,
      int entityIconSize,
      float entityIconOpacity,
      boolean fallbackEntityMarkers,
      float partialTick,
      boolean frameEnabled,
      float frameOpacity,
      boolean perspectiveEnabled,
      float perspectiveStrength,
      float perspectiveTopScale,
      float perspectiveBottomScale,
      float perspectiveXSkew,
      float perspectiveYSkew,
      float perspectiveTiltDeg,
      int perspectiveOffsetX,
      int perspectiveOffsetY,
      float edgeArcTopBottom,
      boolean perspectiveDebugBounds,
      boolean debugBounds,
      boolean debugForceSolidTerrain,
      boolean debugForceCheckerboardTerrain,
      boolean debugShowOnlyFrontTexture,
      boolean debugShowTerrainOnly,
      boolean useSharedHudInertia,
      float inertiaOffsetX,
      float inertiaOffsetY,
      float inertiaRollDeg,
      int screenCenterX,
      int screenCenterY,
      boolean debugDisablePoseStackInMinimap,
      boolean debugForceManualViewportCoordinates,
      boolean debugDrawTerrainBounds,
      boolean debugDrawFrameBounds,
      boolean debugDrawMarkerBounds,
      boolean debugDrawScissorBounds,
      boolean debugDisableMinimapMask,
      boolean debugDrawMinimapMaskBounds,
      NanoMinimapRenderTrace trace
   ) {
      boolean modern=io.github.painworks.crysismod.nanosuit.client.Nanosuit2Hud.equipped();
      if(modern&&io.github.painworks.crysismod.nanosuit.client.Nanosuit2Hud.panelDropped(true)) return;
      if (viewport.width() > 0 && viewport.height() > 0) {
         Minecraft minecraftInstance = Minecraft.getInstance();
         if (minecraftInstance != null) {
            this.entityIconRegistry.ensureLoaded(minecraftInstance.getResourceManager());
         }

         boolean manualViewportCoordinates = debugDisablePoseStackInMinimap || debugForceManualViewportCoordinates;
         NanoMinimapViewport drawViewport = viewport;
         if (manualViewportCoordinates && useSharedHudInertia) {
            int manualX = viewport.x() + Math.round(inertiaOffsetX);
            int manualY = viewport.y() + Math.round(inertiaOffsetY);
            drawViewport = new NanoMinimapViewport(manualX, manualY, viewport.width(), viewport.height(), viewport.opacity());
         }

         float clampedPerspectiveStrength = Mth.clamp(perspectiveStrength, 0.0F, 1.0F);
         boolean applyPerspective = !modern && perspectiveEnabled && clampedPerspectiveStrength > 1.0E-4F;
         float edgeArcPixels = computeEdgeArcPixels(drawViewport, edgeArcTopBottom);
         float renderInertiaOffsetX = inertiaOffsetX;
         float renderInertiaOffsetY = inertiaOffsetY;
         boolean applyInertiaBounds = !manualViewportCoordinates && useSharedHudInertia;
         float[] transformedBounds = NanoMinimapHudTransform.transformedBounds(
            applyInertiaBounds ? viewport : drawViewport,
            screenCenterX,
            screenCenterY,
            applyInertiaBounds ? inertiaOffsetX : 0.0F,
            applyInertiaBounds ? inertiaOffsetY : 0.0F,
            applyInertiaBounds ? inertiaRollDeg : 0.0F,
            applyInertiaBounds,
            applyPerspective ? clampedPerspectiveStrength : 0.0F,
            perspectiveTopScale,
            perspectiveBottomScale,
            perspectiveXSkew,
            perspectiveYSkew,
            perspectiveTiltDeg,
            perspectiveOffsetX,
            perspectiveOffsetY
         );
         int scissorMinX = (int)Math.floor(transformedBounds[0]);
         int scissorMinY = (int)Math.floor(transformedBounds[1]);
         int scissorMaxX = (int)Math.ceil(transformedBounds[2]);
         int scissorMaxY = (int)Math.ceil(transformedBounds[3]);
         boolean useStencilMask = !debugShowTerrainOnly && !debugDisableMinimapMask;
         boolean stencilReady = false;
         if (useStencilMask) {
            ensureStencilAvailable();
            Minecraft minecraft = Minecraft.getInstance();
            stencilReady = minecraft != null && minecraft.getMainRenderTarget() != null && minecraft.getMainRenderTarget().isStencilEnabled();
         }

         boolean optiFineShaderStencilRisk = NanoGeoShaderpackCompatibility.isOptiFineShaderPackActive();
         boolean scissorClipEnabled = useStencilMask && (!stencilReady || optiFineShaderStencilRisk) && scissorMaxX > scissorMinX && scissorMaxY > scissorMinY;
         if (scissorClipEnabled) {
            guiGraphics.enableScissor(scissorMinX, scissorMinY, scissorMaxX, scissorMaxY);
         }

         guiGraphics.pose().pushPose();
         boolean poseStackInertiaApplied = useSharedHudInertia && !manualViewportCoordinates;

         try {
            if (poseStackInertiaApplied) {
               applyHudInertiaTransform(guiGraphics, screenCenterX, screenCenterY, renderInertiaOffsetX, renderInertiaOffsetY, inertiaRollDeg);
            }

            if (applyPerspective) {
               applyPerspectiveTransform(
                  guiGraphics,
                  viewportCenterX(drawViewport),
                  viewportCenterY(drawViewport),
                  clampedPerspectiveStrength,
                  perspectiveTopScale,
                  perspectiveBottomScale,
                  perspectiveXSkew,
                  perspectiveYSkew,
                  perspectiveTiltDeg,
                  perspectiveOffsetX,
                  perspectiveOffsetY
               );
            }

            if(modern) {
               guiGraphics.pose().translate(io.github.painworks.crysismod.nanosuit.client.Nanosuit2Hud.crtShift(),0,0);
               io.github.painworks.crysismod.nanosuit.client.Nanosuit2Art.shear(guiGraphics,
                  drawViewport.x()+drawViewport.width()*231f/340,drawViewport.y()+drawViewport.height()*67f/184,-.122785f,1);
            }
            if (useStencilMask) {
               beginStencilMask(guiGraphics, drawViewport, edgeArcPixels,modern);
            }

            int frameColor = this.withOpacity(-16381684, drawViewport.opacity() * 0.66F * Mth.clamp(frameOpacity, 0.0F, 1.0F));
            int backColor = this.withOpacity(modern ? 0xff203b33 : -15789288, drawViewport.opacity() * 0.8F);
            if (!debugShowTerrainOnly) {
               if (useStencilMask && Math.abs(edgeArcPixels) >= 0.001F) {
                  drawStencilBackedBackground(guiGraphics, drawViewport, edgeArcPixels, backColor);
               } else {
                  guiGraphics.fill(drawViewport.x(), drawViewport.y(), drawViewport.maxX(), drawViewport.maxY(), backColor);
               }
            }

            guiGraphics.pose().pushPose();
            float centerX = modern ? drawViewport.x() + drawViewport.width() * 0.5F + Nanosuit2Cone.offsetX(drawViewport)
               : viewportCenterX(drawViewport);
            float centerY = modern ? drawViewport.y() + drawViewport.height() * 0.5F + Nanosuit2Cone.offsetY(drawViewport)
               : viewportCenterY(drawViewport);
            if (rotateWithPlayer) {
               float pivotX = centerX;
               float pivotY = centerY;
               float headingRotationDeg = 180.0F - playerYawDeg;
               guiGraphics.pose().translate(pivotX, pivotY, 0.0);
               guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(headingRotationDeg));
               guiGraphics.pose().translate(-pivotX, -pivotY, 0.0);
            }

            guiGraphics.pose().translate(terrainOffsetX, terrainOffsetY, 0.0);
            int sourceSize = Math.max(Math.max(drawViewport.width(), drawViewport.height()), terrainSourceSizePixels);
            float drawX = centerX - sourceSize * 0.5F;
            float drawY = centerY - sourceSize * 0.5F;
            if (debugForceSolidTerrain) {
               guiGraphics.fill(
                  (int)Math.floor(drawX), (int)Math.floor(drawY), (int)Math.ceil(drawX + sourceSize), (int)Math.ceil(drawY + sourceSize), -11642266
               );
            } else if (!debugForceCheckerboardTerrain) {
               if (hasTerrainTexture && terrainTextureId != null) {
                  int texRes = Math.max(16, terrainTextureResolution);
                  RenderSystem.enableBlend();
                  RenderSystem.defaultBlendFunc();
                  guiGraphics.setColor(1.0F, 1.0F, 1.0F, Mth.clamp(drawViewport.opacity(), 0.0F, 1.0F));
                  drawTexturedQuad(guiGraphics, terrainTextureId, drawX, drawY, sourceSize, sourceSize, texRes, texRes);
                  guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                  RenderSystem.disableBlend();
               }
            } else {
               int drawXi = (int)Math.floor(drawX);
               int drawYi = (int)Math.floor(drawY);
               int step = 8;

               for (int y = 0; y < sourceSize; y += step) {
                  for (int x = 0; x < sourceSize; x += step) {
                     boolean dark = (x / step + y / step & 1) == 0;
                     int color = dark ? -13813950 : -11116176;
                     guiGraphics.fill(drawXi + x, drawYi + y, drawXi + x + step, drawYi + y + step, color);
                  }
               }
            }

            guiGraphics.pose().popPose();
            if (!debugShowOnlyFrontTexture && !debugShowTerrainOnly) {
               this.drawMarkers(
                  guiGraphics,
                  drawViewport,
                  mapper,
                  markers,
                  markerOffsetX,
                  markerOffsetY,
                  rotateWithPlayer,
                  playerYawDeg,
                  clampMarkersToBounds,
                  markerScale,
                  markerOpacity,
                  useEntityIcons,
                  entityIconSize,
                  entityIconOpacity,
                  fallbackEntityMarkers,
                  partialTick
               );
            }

            if (useStencilMask) {
               endStencilMask();
            }

            if (!debugShowOnlyFrontTexture && !debugShowTerrainOnly && frameEnabled && !modern) {
               drawFrameContour(guiGraphics, drawViewport, frameColor, edgeArcPixels);
            }

            if (!debugShowOnlyFrontTexture && !debugShowTerrainOnly && debugBounds) {
               drawRect(guiGraphics, drawViewport.x(), drawViewport.y(), drawViewport.width(), drawViewport.height(), -16711681);
            }

            if (debugDrawTerrainBounds) {
               drawRect(guiGraphics, (int)Math.floor(drawX), (int)Math.floor(drawY), sourceSize, sourceSize, -14496666);
            }

            if (debugDrawFrameBounds && frameEnabled) {
               drawRect(guiGraphics, drawViewport.x() - 2, drawViewport.y() - 2, drawViewport.width() + 4, drawViewport.height() + 4, -16320);
            }

            if (debugDrawMarkerBounds) {
               drawRect(guiGraphics, drawViewport.x(), drawViewport.y(), drawViewport.width(), drawViewport.height(), -10580481);
            }

            if (debugDrawMinimapMaskBounds) {
               drawRect(guiGraphics, drawViewport.x(), drawViewport.y(), drawViewport.width(), drawViewport.height(), -4698625);
            }

            if (debugDrawScissorBounds) {
               drawRect(guiGraphics, scissorMinX, scissorMinY, Math.max(1, scissorMaxX - scissorMinX), Math.max(1, scissorMaxY - scissorMinY), -45747);
            }

            if (perspectiveDebugBounds && applyPerspective) {
               drawRect(guiGraphics, scissorMinX, scissorMinY, Math.max(1, scissorMaxX - scissorMinX), Math.max(1, scissorMaxY - scissorMinY), -12458336);
            }

            if (trace != null) {
               trace.poseStackInertiaApplied = poseStackInertiaApplied;
               trace.inertiaOffsetX = renderInertiaOffsetX;
               trace.inertiaOffsetY = renderInertiaOffsetY;
               trace.inertiaRollDeg = inertiaRollDeg;
               trace.baseViewportX = viewport.x();
               trace.baseViewportY = viewport.y();
               trace.baseViewportWidth = viewport.width();
               trace.baseViewportHeight = viewport.height();
               if (poseStackInertiaApplied) {
                  trace.finalViewportX = scissorMinX;
                  trace.finalViewportY = scissorMinY;
                  trace.finalViewportWidth = Math.max(1, scissorMaxX - scissorMinX);
                  trace.finalViewportHeight = Math.max(1, scissorMaxY - scissorMinY);
               } else {
                  trace.finalViewportX = drawViewport.x();
                  trace.finalViewportY = drawViewport.y();
                  trace.finalViewportWidth = drawViewport.width();
                  trace.finalViewportHeight = drawViewport.height();
               }

               trace.terrainDrawX = drawX;
               trace.terrainDrawY = drawY;
               trace.terrainDrawWidth = sourceSize;
               trace.terrainDrawHeight = sourceSize;
               trace.terrainSourceOffsetX = terrainOffsetX;
               trace.terrainSourceOffsetY = terrainOffsetY;
               trace.terrainCoverageWidth = sourceSize;
               trace.terrainCoverageHeight = sourceSize;
               trace.terrainRounded = false;
               trace.markerLayerBaseX = viewport.x();
               trace.markerLayerBaseY = viewport.y();
               trace.markerLayerFinalX = drawViewport.x();
               trace.markerLayerFinalY = drawViewport.y();
               trace.markerRounded = true;
               trace.frameDrawX = drawViewport.x() - 2;
               trace.frameDrawY = drawViewport.y() - 2;
               trace.frameDrawWidth = drawViewport.width() + 4;
               trace.frameDrawHeight = drawViewport.height() + 4;
               trace.frameEnabled = frameEnabled;
               trace.frameRounded = true;
               trace.scissorX = scissorMinX;
               trace.scissorY = scissorMinY;
               trace.scissorWidth = Math.max(0, scissorMaxX - scissorMinX);
               trace.scissorHeight = Math.max(0, scissorMaxY - scissorMinY);
               trace.scissorUsesFinalViewport = true;
               trace.terrainSourceU = 0;
               trace.terrainSourceV = 0;
               trace.terrainSourceRectW = sourceSize;
               trace.terrainSourceRectH = sourceSize;
               trace.terrainDestX = (int)Math.floor(drawX);
               trace.terrainDestY = (int)Math.floor(drawY);
               trace.terrainDestW = sourceSize;
               trace.terrainDestH = sourceSize;
               trace.perspectiveEnabled = perspectiveEnabled;
               trace.perspectiveStrength = clampedPerspectiveStrength;
               trace.perspectiveTopScale = perspectiveTopScale;
               trace.perspectiveBottomScale = perspectiveBottomScale;
               trace.perspectiveSkewX = perspectiveXSkew;
               trace.perspectiveSkewY = perspectiveYSkew;
               trace.perspectiveTiltDeg = perspectiveTiltDeg;
               trace.perspectiveOffsetX = perspectiveOffsetX;
               trace.perspectiveOffsetY = perspectiveOffsetY;
               trace.edgeArcTopBottom = edgeArcTopBottom;
               trace.perspectiveDebugBounds = perspectiveDebugBounds;
            }

            if (!debugShowOnlyFrontTexture && !debugShowTerrainOnly && minecraftInstance != null) {
               if (scissorClipEnabled) {
                  guiGraphics.disableScissor();
                  scissorClipEnabled = false;
               }
               if(modern&&minecraftInstance.player!=null) {
                  io.github.painworks.crysismod.nanosuit.client.Nanosuit2Hud.rememberRadar(drawViewport.x(),drawViewport.y(),drawViewport.width(),drawViewport.height());
                  Nanosuit2Radar.frame(guiGraphics,drawViewport,minecraftInstance.player,playerYawDeg,useStencilMask,frameEnabled);
                  Nanosuit2Radar.waypoint(guiGraphics,drawViewport,mapper,markerOffsetX,markerOffsetY,
                     rotateWithPlayer,playerYawDeg,useStencilMask);
               } else ThreatGauge.render(guiGraphics, drawViewport, minecraftInstance);
            }
         } finally {
            if (useStencilMask) {
               endStencilMask();
            }

            if (scissorClipEnabled) {
               guiGraphics.disableScissor();
            }

            guiGraphics.pose().popPose();
         }
      }
   }

   private final Nanosuit2Cone nanosuit2Cone = new Nanosuit2Cone();

   private void drawMarkers(
      GuiGraphics guiGraphics,
      NanoMinimapViewport viewport,
      NanoMinimapCoordinateMapper mapper,
      List<NanoMinimapMarker> markers,
      double fractionalOffsetX,
      double fractionalOffsetY,
      boolean rotateWithPlayer,
      float playerYawDeg,
      boolean clampMarkersToBounds,
      float markerScale,
      float markerOpacity,
      boolean useEntityIcons,
      int entityIconSize,
      float entityIconOpacity,
      boolean fallbackEntityMarkers,
      float partialTick
   ) {
      boolean modern = io.github.painworks.crysismod.nanosuit.client.Nanosuit2Hud.equipped();
      float mapOffsetX = modern ? Nanosuit2Cone.offsetX(viewport) : 0;
      float mapOffsetY = modern ? Nanosuit2Cone.offsetY(viewport) : 0;
      double cx = viewport.x() + viewport.width() / 2.0 + mapOffsetX;
      double cy = viewport.y() + viewport.height() / 2.0 + mapOffsetY;
      float scale = Mth.clamp(markerScale, 0.25F, 4.0F);
      float opacity = Mth.clamp(markerOpacity * viewport.opacity(), 0.05F, 1.0F);
      float iconOpacity = Mth.clamp(entityIconOpacity * viewport.opacity(), 0.05F, 1.0F);
      int iconDrawSize = Mth.clamp(entityIconSize, 8, 16);
      NanoMinimapEntityIconAtlas iconAtlas = this.entityIconRegistry.atlas();
      Minecraft minecraft = Minecraft.getInstance();
      ClientLevel level = minecraft != null ? minecraft.level : null;
      float playerYaw = playerYawDeg;
      int playerColor = 8321023;
      NanoMinimapCoordinateMapper.MutablePoint markerPoint = new NanoMinimapCoordinateMapper.MutablePoint();
      NanoMinimapCoordinateMapper.MutablePoint clampedPoint = new NanoMinimapCoordinateMapper.MutablePoint();
      boolean blendEnabledForIcons = false;
      IntOpenHashSet modernContacts = modern ? new IntOpenHashSet() : null;
      if (modern) {
         float heading = rotateWithPlayer ? 0 : playerYawDeg - 180;
         this.nanosuit2Cone.draw(guiGraphics, viewport, minecraft, (float) cx, (float) cy, heading);
      }

      for (NanoMinimapMarker marker : markers) {
         if (marker.type() == NanoMinimapMarker.Type.PLAYER) {
            playerYaw = marker.yawDeg();
            playerColor = marker.rgbColor();
         } else {
            double worldX = marker.worldX();
            double worldZ = marker.worldZ();
            ResourceLocation markerEntityTypeId = marker.entityTypeId();
            float markerAlpha = Mth.clamp(marker.opacityMultiplier(), 0.0F, 1.0F);
            Entity entity=null;
            if (marker.entityId() >= 0 && level != null) {
               entity = level.getEntity(marker.entityId());
               if (entity != null && entity.isAlive()) {
                  worldX = Mth.lerp(partialTick, entity.xo, entity.getX());
                  worldZ = Mth.lerp(partialTick, entity.zo, entity.getZ());
               }
            }

            if (!(markerAlpha <= 0.001F)) {
               mapper.worldToViewport(worldX, worldZ, fractionalOffsetX, fractionalOffsetY, rotateWithPlayer, playerYawDeg, markerPoint);
               markerPoint.x += mapOffsetX;
               markerPoint.y += mapOffsetY;
               boolean inside = mapper.isInsideViewport(markerPoint.x, markerPoint.y);
               if (inside || clampMarkersToBounds) {
                  double drawLocalX;
                  double drawLocalY;
                  if (inside) {
                     drawLocalX = markerPoint.x;
                     drawLocalY = markerPoint.y;
                  } else {
                     mapper.clampToViewport(markerPoint.x, markerPoint.y, 2.0, clampedPoint);
                     drawLocalX = clampedPoint.x;
                     drawLocalY = clampedPoint.y;
                  }

                  double sx = viewport.x() + drawLocalX;
                  double sy = viewport.y() + drawLocalY;
                  if (modern) {
                     if (entity != null && entity.isAlive() && modernContacts.add(entity.getId())) {
                        if (entity instanceof net.minecraft.world.entity.monster.Enemy) {
                           float heading = marker.yawDeg() - (rotateWithPlayer ? playerYawDeg : 180);
                           Nanosuit2Radar.contact(guiGraphics, viewport, entity, (float) sx, (float) sy, heading);
                        } else if (entity instanceof net.minecraft.world.entity.Mob) {
                           Nanosuit2Radar.passive(guiGraphics, viewport, (float) sx, (float) sy);
                        }
                     }
                     continue;
                  }
                  boolean renderedIcon = false;
                  if (useEntityIcons && markerEntityTypeId != null) {
                     NanoMinimapEntityIcon icon = iconAtlas.resolve(markerEntityTypeId);
                     if (icon != null) {
                        if (!blendEnabledForIcons) {
                           RenderSystem.enableBlend();
                           RenderSystem.defaultBlendFunc();
                           blendEnabledForIcons = true;
                        }

                        int argb = this.withOpacity(-1, iconOpacity * markerAlpha);
                        this.drawEntityIcon(guiGraphics, iconAtlas, icon, sx, sy, iconDrawSize, argb);
                        if (marker.type() == NanoMinimapMarker.Type.MARKED_ENTITY) {
                           float markedHighlightOpacity = (float)Mth.clamp(NanoMinimapClientConfig.minimapMarkedHighlightOpacity(), 0.0, 1.0);
                           if (markedHighlightOpacity <= 0.001F) {
                              renderedIcon = true;
                              continue;
                           }

                           int frameArgb = this.withOpacity(16777215, markedHighlightOpacity);
                           this.drawMarkedIconFrame(guiGraphics, sx, sy, iconDrawSize, frameArgb);
                        }

                        renderedIcon = true;
                     }
                  }

                  if (!renderedIcon && fallbackEntityMarkers) {
                     int argb = this.withOpacity(0xFF000000 | marker.rgbColor() & 16777215, opacity * markerAlpha);
                     this.drawEntityMarker(guiGraphics, marker.type(), sx, sy, argb, scale, !inside && clampMarkersToBounds);
                  }
               }
            }
         }
      }

      if (blendEnabledForIcons) {
         RenderSystem.disableBlend();
         guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      if(modern) {
         float heading = rotateWithPlayer ? 0 : playerYaw - 180;
         Nanosuit2Radar.player(guiGraphics, viewport, (float) cx, (float) cy, heading);
      } else this.drawPlayerMarker(guiGraphics, cx, cy, rotateWithPlayer, playerYaw, playerColor, opacity, scale);
   }

   private static void ensureStencilAvailable() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft != null) {
         if (minecraft.getMainRenderTarget() != null && !minecraft.getMainRenderTarget().isStencilEnabled()) {
            minecraft.getMainRenderTarget().enableStencil();
         }
      }
   }

   private static void beginStencilMask(GuiGraphics guiGraphics, NanoMinimapViewport viewport, float edgeArcPixels,boolean modern) {
      RenderSystem.clearStencil(0);
      RenderSystem.clear(1024, false);
      GL11.glEnable(2960);
      GL11.glStencilMask(255);
      GL11.glStencilFunc(519, 1, 255);
      GL11.glStencilOp(7680, 7680, 7681);
      RenderSystem.colorMask(false, false, false, false);
      if(modern) Nanosuit2Radar.mask(guiGraphics,viewport);
      else drawCurvedMaskFill(guiGraphics, viewport, edgeArcPixels);
      RenderSystem.colorMask(true, true, true, true);
      GL11.glStencilMask(0);
      GL11.glStencilFunc(514, 1, 255);
      GL11.glStencilOp(7680, 7680, 7680);
   }

   private static void endStencilMask() {
      GL11.glStencilMask(255);
      GL11.glDisable(2960);
   }

   private void drawEntityMarker(GuiGraphics guiGraphics, NanoMinimapMarker.Type type, double x, double y, int argb, float scale, boolean clamped) {
      int cxi = (int)Math.round(x);
      int cyi = (int)Math.round(y);
      int half = Math.max(1, Math.round(2.0F * scale));
      switch (type) {
         case TARGET:
            this.drawDiamond(guiGraphics, cxi, cyi, Math.max(2, Math.round(3.0F * scale)), argb);
            break;
         case MARKED_ENTITY:
            this.drawMarkedFallbackMarker(guiGraphics, cxi, cyi, half, argb);
            break;
         case NEARBY_ENTITY:
            this.drawDiamond(guiGraphics, cxi, cyi, Math.max(1, Math.round(2.0F * scale)), argb);
            break;
         default:
            guiGraphics.fill(cxi - 1, cyi - 1, cxi + 2, cyi + 2, argb);
      }

      if (clamped) {
         int clampedColor = -520093697;
         guiGraphics.fill(cxi - 1, cyi - 1, cxi + 2, cyi + 2, clampedColor);
      }
   }

   private void drawEntityIcon(
      GuiGraphics guiGraphics, NanoMinimapEntityIconAtlas atlas, NanoMinimapEntityIcon icon, double x, double y, int drawSize, int argb
   ) {
      float drawX = (float)(x - drawSize / 2.0);
      float drawY = (float)(y - drawSize / 2.0);
      float a = (argb >>> 24 & 0xFF) / 255.0F;
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, a);
      drawTexturedSubQuad(
         guiGraphics,
         atlas.textureId(),
         drawX,
         drawY,
         drawSize,
         drawSize,
         icon.u(),
         icon.v(),
         icon.width(),
         icon.height(),
         atlas.textureWidth(),
         atlas.textureHeight()
      );
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void drawMarkedIconFrame(GuiGraphics guiGraphics, double centerX, double centerY, int iconDrawSize, int frameArgb) {
      float half = iconDrawSize / 2.0F;
      float left = (float)centerX - half - 1.0F;
      float top = (float)centerY - half - 1.0F;
      float right = left + iconDrawSize + 2.0F;
      float bottom = top + iconDrawSize + 2.0F;
      drawRectRingFloat(guiGraphics, left, top, right, bottom, 0.7F, frameArgb);
   }

   private void drawMarkedFallbackMarker(GuiGraphics guiGraphics, int cxi, int cyi, int half, int argb) {
      guiGraphics.fill(cxi - half, cyi - half, cxi + half + 1, cyi + half + 1, argb);
      float markedHighlightOpacity = (float)Mth.clamp(NanoMinimapClientConfig.minimapMarkedHighlightOpacity(), 0.0, 1.0);
      if (!(markedHighlightOpacity <= 0.001F)) {
         int frameArgb = this.withOpacity(16777215, markedHighlightOpacity);
         float left = cxi - half - 0.5F;
         float top = cyi - half - 0.5F;
         float right = cxi + half + 1.5F;
         float bottom = cyi + half + 1.5F;
         drawRectRingFloat(guiGraphics, left, top, right, bottom, 0.595F, frameArgb);
      }
   }

   private void drawPlayerMarker(
      GuiGraphics guiGraphics, double cx, double cy, boolean rotateWithPlayer, float playerYawDeg, int rgbColor, float opacity, float markerScale
   ) {
      int argb = this.withOpacity(0xFF000000 | rgbColor & 16777215, opacity);
      int coreHalf = Math.max(1, Math.round(markerScale));
      int cxi = (int)Math.round(cx);
      int cyi = (int)Math.round(cy);
      guiGraphics.fill(cxi - coreHalf, cyi - coreHalf, cxi + coreHalf + 1, cyi + coreHalf + 1, argb);
      float dx;
      float dy;
      if (rotateWithPlayer) {
         dx = 0.0F;
         dy = -1.0F;
      } else {
         float rad = playerYawDeg * (float) (Math.PI / 180.0);
         dx = -Mth.sin(rad);
         dy = Mth.cos(rad);
      }

      int length = Math.max(5, Math.round(8.0F * markerScale));
      int tipX = cxi + Math.round(dx * length);
      int tipY = cyi + Math.round(dy * length);
      this.drawLine(guiGraphics, cxi, cyi, tipX, tipY, argb);
   }

   private void drawDiamond(GuiGraphics guiGraphics, int cx, int cy, int radius, int color) {
      for (int row = -radius; row <= radius; row++) {
         int span = radius - Math.abs(row);
         guiGraphics.fill(cx - span, cy + row, cx + span + 1, cy + row + 1, color);
      }
   }

   private void drawLine(GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, int argb) {
      int dx = Math.abs(x1 - x0);
      int dy = Math.abs(y1 - y0);
      int sx = x0 < x1 ? 1 : -1;
      int sy = y0 < y1 ? 1 : -1;
      int err = dx - dy;
      int x = x0;
      int y = y0;

      while (true) {
         guiGraphics.fill(x, y, x + 1, y + 1, argb);
         if (x == x1 && y == y1) {
            return;
         }

         int e2 = err << 1;
         if (e2 > -dy) {
            err -= dy;
            x += sx;
         }

         if (e2 < dx) {
            err += dx;
            y += sy;
         }
      }
   }

   private int withOpacity(int argbColor, float opacity) {
      int alpha = Mth.clamp(Math.round(Mth.clamp(opacity, 0.0F, 1.0F) * 255.0F), 0, 255);
      return alpha << 24 | argbColor & 16777215;
   }

   private static void applyHudInertiaTransform(GuiGraphics guiGraphics, int screenCenterX, int screenCenterY, float offsetX, float offsetY, float rollDeg) {
      if (!(Math.abs(offsetX) < 0.001F) || !(Math.abs(offsetY) < 0.001F) || !(Math.abs(rollDeg) < 0.001F)) {
         guiGraphics.pose().translate(screenCenterX + offsetX, screenCenterY + offsetY, 0.0F);
         guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(rollDeg));
         guiGraphics.pose().translate(-screenCenterX, -screenCenterY, 0.0F);
      }
   }

   private static void applyPerspectiveTransform(
      GuiGraphics guiGraphics,
      int pivotX,
      int pivotY,
      float strength,
      float topScale,
      float bottomScale,
      float skewX,
      float skewY,
      float tiltDeg,
      int offsetX,
      int offsetY
   ) {
      float avgScaleX = (topScale + bottomScale) * 0.5F;
      float scaleX = Mth.lerp(strength, 1.0F, avgScaleX);
      float topBottomSkew = (bottomScale - topScale) * 0.75F;
      float effectiveSkewX = (skewX + topBottomSkew) * strength;
      float effectiveSkewY = skewY * strength;
      float effectiveTilt = tiltDeg * strength;
      guiGraphics.pose().translate(pivotX + offsetX, pivotY + offsetY, 0.0F);
      if (Math.abs(effectiveTilt) > 1.0E-4F) {
         guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(effectiveTilt));
      }

      Matrix4f affine = new Matrix4f().identity();
      affine.m00(scaleX);
      affine.m01(effectiveSkewX);
      affine.m10(effectiveSkewY);
      affine.m11(1.0F);
      guiGraphics.pose().mulPose(affine);
      guiGraphics.pose().translate(-pivotX, -pivotY, 0.0F);
   }

   private static void drawTexturedQuad(
      GuiGraphics guiGraphics, ResourceLocation textureId, float x, float y, int width, int height, int texWidth, int texHeight
   ) {
      float u0 = 0.0F;
      float v0 = 0.0F;
      float u1 = (float)width / texWidth;
      float v1 = (float)height / texHeight;
      Matrix4f matrix = guiGraphics.pose().last().pose();
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, textureId);
      BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      buffer.addVertex(matrix, x, y + height, 0.0F).setUv(u0, v1);
      buffer.addVertex(matrix, x + width, y + height, 0.0F).setUv(u1, v1);
      buffer.addVertex(matrix, x + width, y, 0.0F).setUv(u1, v0);
      buffer.addVertex(matrix, x, y, 0.0F).setUv(u0, v0);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
   }

   private static void drawTexturedSubQuad(
      GuiGraphics guiGraphics,
      ResourceLocation textureId,
      float x,
      float y,
      int width,
      int height,
      int srcX,
      int srcY,
      int srcWidth,
      int srcHeight,
      int texWidth,
      int texHeight
   ) {
      float u0 = (float)srcX / texWidth;
      float v0 = (float)srcY / texHeight;
      float u1 = (float)(srcX + srcWidth) / texWidth;
      float v1 = (float)(srcY + srcHeight) / texHeight;
      Matrix4f matrix = guiGraphics.pose().last().pose();
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, textureId);
      BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      buffer.addVertex(matrix, x, y + height, 0.0F).setUv(u0, v1);
      buffer.addVertex(matrix, x + width, y + height, 0.0F).setUv(u1, v1);
      buffer.addVertex(matrix, x + width, y, 0.0F).setUv(u1, v0);
      buffer.addVertex(matrix, x, y, 0.0F).setUv(u0, v0);
      BufferUploader.drawWithShader(buffer.buildOrThrow());
   }

   public void onResourceReload() {
      this.entityIconRegistry.invalidate();
   }

   private static int viewportCenterX(NanoMinimapViewport viewport) {
      return viewport.x() + viewport.width() / 2;
   }

   private static int viewportCenterY(NanoMinimapViewport viewport) {
      return viewport.y() + viewport.height() / 2;
   }

   private static void drawFrameContour(GuiGraphics guiGraphics, NanoMinimapViewport viewport, int frameColor, float edgeArcPixels) {
      int innerX0 = viewport.x();
      int innerX1 = viewport.maxX();
      int innerY0 = viewport.y();
      int innerY1 = viewport.maxY();
      int outerX0 = innerX0 - 2;
      int outerX1 = innerX1 + 2;
      int outerY0 = innerY0 - 2;
      int outerY1 = innerY1 + 2;
      float clampedArc = clampMaskArcPixels(edgeArcPixels, viewport.height());
      if (Math.abs(clampedArc) < 0.001F) {
         guiGraphics.fill(outerX0, outerY0, outerX1, innerY0, frameColor);
         guiGraphics.fill(outerX0, innerY1, outerX1, outerY1, frameColor);
         guiGraphics.fill(outerX0, innerY0, innerX0, innerY1, frameColor);
         guiGraphics.fill(innerX1, innerY0, outerX1, innerY1, frameColor);
      } else {
         for (int x = outerX0; x < outerX1; x++) {
            int innerSampleX = Mth.clamp(x, innerX0, innerX1 - 1);
            float profile = edgeProfile(innerX0, innerX1, innerSampleX);
            int innerTop = Math.round(innerY0 + clampedArc * profile);
            int innerBottom = Math.round(innerY1 + clampedArc * profile);
            int outerTop = innerTop - 2;
            int outerBottom = innerBottom + 2;
            guiGraphics.fill(x, outerTop, x + 1, innerTop, frameColor);
            guiGraphics.fill(x, innerBottom, x + 1, outerBottom, frameColor);
         }

         int leftTop = Math.round(innerY0 + clampedArc * edgeProfile(innerX0, innerX1, innerX0));
         int leftBottom = Math.round(innerY1 + clampedArc * edgeProfile(innerX0, innerX1, innerX0));
         int rightTop = Math.round(innerY0 + clampedArc * edgeProfile(innerX0, innerX1, innerX1 - 1));
         int rightBottom = Math.round(innerY1 + clampedArc * edgeProfile(innerX0, innerX1, innerX1 - 1));
         guiGraphics.fill(outerX0, leftTop, innerX0, leftBottom, frameColor);
         guiGraphics.fill(innerX1, rightTop, outerX1, rightBottom, frameColor);
      }
   }

   private static void drawCurvedMaskFill(GuiGraphics guiGraphics, NanoMinimapViewport viewport, float edgeArcPixels) {
      int x0 = viewport.x();
      int x1 = viewport.maxX();
      int y0 = viewport.y();
      int y1 = viewport.maxY();
      float clampedArc = clampMaskArcPixels(edgeArcPixels, viewport.height());
      if (Math.abs(clampedArc) < 0.001F) {
         guiGraphics.fill(x0, y0, x1, y1, -1);
      } else {
         Matrix4f matrix = guiGraphics.pose().last().pose();
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

         for (int x = x0; x < x1; x++) {
            float profile = edgeProfile(x0, x1, x);
            float top = y0 + clampedArc * profile;
            float bottom = y1 + clampedArc * profile;
            if (bottom <= top) {
               bottom = top + 1.0F;
            }

            buffer.addVertex(matrix, x, bottom, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.addVertex(matrix, x + 1.0F, bottom, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.addVertex(matrix, x + 1.0F, top, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
            buffer.addVertex(matrix, x, top, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
         }

         BufferUploader.drawWithShader(buffer.buildOrThrow());
      }
   }

   private static void drawStencilBackedBackground(GuiGraphics guiGraphics, NanoMinimapViewport viewport, float edgeArcPixels, int argb) {
      float clampedArc = clampMaskArcPixels(edgeArcPixels, viewport.height());
      int fillTop = (int)Math.floor(viewport.y() + Math.min(0.0F, clampedArc));
      int fillBottom = (int)Math.ceil(viewport.maxY() + Math.max(0.0F, clampedArc));
      guiGraphics.fill(viewport.x(), fillTop, viewport.maxX(), fillBottom, argb);
   }

   private static float edgeProfile(int x0, int x1, int x) {
      float width = Math.max(1.0F, x1 - x0 - 1.0F);
      float nx = (x - x0) / width * 2.0F - 1.0F;
      float p = 1.0F - nx * nx;
      return Math.max(0.0F, p);
   }

   private static float computeEdgeArcPixels(NanoMinimapViewport viewport, float edgeArcTopBottom) {
      float clamped = Mth.clamp(edgeArcTopBottom, -1.0F, 1.0F);
      float maxArc = Math.max(0.0F, viewport.height() * 0.42F - 2.0F);
      return clamped * maxArc;
   }

   private static float clampMaskArcPixels(float arcPixels, int viewportHeight) {
      float max = Math.max(0.0F, viewportHeight * 0.45F - 1.0F);
      return Mth.clamp(arcPixels, -max, max);
   }

   private static void drawRectRingFloat(GuiGraphics guiGraphics, float left, float top, float right, float bottom, float thickness, int argb) {
      float t = Math.max(0.5F, thickness);
      drawSolidQuadFloat(guiGraphics, left, top, right, top + t, argb);
      drawSolidQuadFloat(guiGraphics, left, bottom - t, right, bottom, argb);
      drawSolidQuadFloat(guiGraphics, left, top + t, left + t, bottom - t, argb);
      drawSolidQuadFloat(guiGraphics, right - t, top + t, right, bottom - t, argb);
   }

   private static void drawSolidQuadFloat(GuiGraphics guiGraphics, float x0, float y0, float x1, float y1, int argb) {
      if (!(x1 <= x0) && !(y1 <= y0)) {
         float a = (argb >>> 24 & 0xFF) / 255.0F;
         float r = (argb >>> 16 & 0xFF) / 255.0F;
         float g = (argb >>> 8 & 0xFF) / 255.0F;
         float b = (argb & 0xFF) / 255.0F;
         Matrix4f matrix = guiGraphics.pose().last().pose();
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
         buffer.addVertex(matrix, x0, y1, 0.0F).setColor(r, g, b, a);
         buffer.addVertex(matrix, x1, y1, 0.0F).setColor(r, g, b, a);
         buffer.addVertex(matrix, x1, y0, 0.0F).setColor(r, g, b, a);
         buffer.addVertex(matrix, x0, y0, 0.0F).setColor(r, g, b, a);
         BufferUploader.drawWithShader(buffer.buildOrThrow());
      }
   }

   private static void drawRect(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
      int maxX = x + Math.max(1, width);
      int maxY = y + Math.max(1, height);
      guiGraphics.fill(x, y, maxX, y + 1, color);
      guiGraphics.fill(x, maxY - 1, maxX, maxY, color);
      guiGraphics.fill(x, y, x + 1, maxY, color);
      guiGraphics.fill(maxX - 1, y, maxX, maxY, color);
   }
}
