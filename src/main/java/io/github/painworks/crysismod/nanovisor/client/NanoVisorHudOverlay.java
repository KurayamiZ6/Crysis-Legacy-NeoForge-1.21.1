package io.github.painworks.crysismod.nanovisor.client;

import io.github.painworks.crysismod.nanosuit.energy.INanosuitEnergy;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.github.painworks.crysismod.client.compat.GnetumHudCompat;
import io.github.painworks.crysismod.nanosuit.client.NanosuitHudMasterCompositor;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanoVisorHudOverlay {
   private static final float MIN_HUD_OPACITY = 0.0F;
   private static final float MAX_HUD_OPACITY = 1.0F;
   private static final float MIN_HUD_SCALE = 0.25F;
   private static final float MAX_HUD_SCALE = 4.0F;
   private static final double MIN_DISTANCE_LIMIT = 1.0;
   private static final double MAX_DISTANCE_LIMIT = 512.0;
   private static final int TEXT_COLOR_RGB = 10157055;
   private static final int SHADOW_COLOR_RGB = 666158;
   private static final int REFERENCE_WIDTH = 1920;
   private static final int REFERENCE_HEIGHT = 1080;
   private static final int VISOR_LAYER_SHIFT_X_PX = -1;
   private static final float ZOOM_ARROW_SMOOTHING = 0.2F;
   private static final int ARROW_TEXTURE_WIDTH = 20;
   private static final int ARROW_TEXTURE_HEIGHT = 20;
   private static final float MIN_VIGNETTE_INTENSITY = 0.0F;
   private static final float MAX_VIGNETTE_INTENSITY = 1.0F;
   private static final float INV_SQRT_TWO = 0.70710677F;
   private static final float VIGNETTE_INNER_START_MIN = 0.2F;
   private static final float VIGNETTE_INNER_START_MAX = 0.82F;
   private static final float VIGNETTE_CORNER_BOOST_MIN = 0.52F;
   private static final float VIGNETTE_CORNER_BOOST_MAX = 1.0F;
   private static final float VIGNETTE_CORNER_CURVE = 0.55F;
   private static final int VIGNETTE_GRID_X = 72;
   private static final int VIGNETTE_GRID_Y = 40;
   private static final ResourceLocation VISOR_HUD_BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath("crysismod", "textures/gui/nanovisor/visor_hud_base.png");
   private static final ResourceLocation VISOR_ZOOM_ARROW_TEXTURE = ResourceLocation.fromNamespaceAndPath("crysismod", "textures/gui/nanovisor/visor_zoom_arrow.png");
   private static float currentArrowX = 702.0F;
   private static float currentArrowY = 568.0F;
   private static boolean arrowInitialized;
   private static float[] vignetteAlphaCache = new float[0];
   private static int vignetteCacheGridX;
   private static int vignetteCacheGridY;
   private static float vignetteCacheAlphaScale = Float.NaN;
   private static float vignetteCacheInnerStart = Float.NaN;

   private NanoVisorHudOverlay() {
   }

   @SubscribeEvent
   public static void onPreRenderGuiOverlay(Pre event) {
      if (event.getName().equals(VanillaGuiLayers.CROSSHAIR)) {
         if (NanoVisorState.isVisorActive()) {
            event.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   public static void onRenderGuiOverlay(Post event) {
      if (NanosuitHudMasterCompositor.shouldLegacyRenderNanoVisorHud()) {
         renderHotbarOverlay(event);
      }
   }

   public static void renderFromMaster(Post event) {
      renderHotbarOverlay(event);
   }

   public static void resetArrowInterpolation() {
      arrowInitialized = false;
   }

   private static void renderHotbarOverlay(Post event) {
      if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
         GnetumHudCompat.disableCachingForCurrentHudElement();
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer player = minecraft.player;
         if (player != null && !minecraft.options.hideGui && NanoVisorState.shouldRenderOverlay()) {
            float opacity = (float)Mth.clamp(NanoVisorSettings.Hud.opacity(), 0.0, 1.0);
            if (!(opacity <= 0.0F)) {
               GuiGraphics guiGraphics = event.getGuiGraphics();
               int screenWidth = guiGraphics.guiWidth();
               int screenHeight = guiGraphics.guiHeight();
               renderVisorZoomLayers(guiGraphics, screenWidth, screenHeight, opacity);
               renderVisorVignette(guiGraphics, screenWidth, screenHeight, opacity);
               List<Component> lines = buildHudLines(minecraft, player, event.getPartialTick().getGameTimeDeltaPartialTick(false));
               if (!lines.isEmpty()) {
                  float scale = (float)Mth.clamp(NanoVisorSettings.Hud.scale(), 0.25, 4.0);
                  int offsetX = NanoVisorSettings.Hud.offsetX();
                  int offsetY = NanoVisorSettings.Hud.offsetY();
                  float anchorX = screenWidth * 0.5F + offsetX;
                  float anchorY = screenHeight * 0.5F + 22.0F + offsetY;
                  int textColor = withAlpha(10157055, opacity);
                  int shadowColor = withAlpha(666158, opacity * 0.85F);
                  guiGraphics.pose().pushPose();
                  guiGraphics.pose().translate(anchorX, anchorY, 0.0F);
                  guiGraphics.pose().scale(scale, scale, 1.0F);
                  int y = 0;

                  for (Component line : lines) {
                     int width = minecraft.font.width(line);
                     int x = -width / 2;
                     guiGraphics.drawString(minecraft.font, line, x + 1, y + 1, shadowColor, false);
                     guiGraphics.drawString(minecraft.font, line, x, y, textColor, false);
                     y += 9 + 2;
                  }

                  guiGraphics.pose().popPose();
               }
            }
         }
      }
   }

   private static void renderVisorZoomLayers(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float opacity) {
      float normalizedZoom = NanoVisorZoomHandler.getNormalizedZoom();
      float targetArrowX = (float)Mth.lerp(normalizedZoom, NanoVisorSettings.Hud.visorZoomArrowStartX(), NanoVisorSettings.Hud.visorZoomArrowEndX());
      float targetArrowY = (float)Mth.lerp(normalizedZoom, NanoVisorSettings.Hud.visorZoomArrowStartY(), NanoVisorSettings.Hud.visorZoomArrowEndY());
      if (!arrowInitialized) {
         currentArrowX = targetArrowX;
         currentArrowY = targetArrowY;
         arrowInitialized = true;
      } else {
         currentArrowX = currentArrowX + (targetArrowX - currentArrowX) * 0.2F;
         currentArrowY = currentArrowY + (targetArrowY - currentArrowY) * 0.2F;
      }

      float scaleX = screenWidth / 1920.0F;
      float scaleY = screenHeight / 1080.0F;
      int arrowX = Math.round(currentArrowX) + -1;
      int arrowY = Math.round(currentArrowY);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);
      guiGraphics.pose().pushPose();
      guiGraphics.pose().scale(scaleX, scaleY, 1.0F);
      guiGraphics.blit(VISOR_HUD_BASE_TEXTURE, -1, 0, 0.0F, 0.0F, 1920, 1080, 1920, 1080);
      guiGraphics.pose().popPose();
      guiGraphics.pose().pushPose();
      guiGraphics.pose().scale(scaleX, scaleY, 1.0F);
      guiGraphics.blit(VISOR_ZOOM_ARROW_TEXTURE, arrowX, arrowY, 0.0F, 0.0F, 20, 20, 20, 20);
      guiGraphics.pose().popPose();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
   }

   private static void renderVisorVignette(GuiGraphics guiGraphics, int screenWidth, int screenHeight, float hudOpacity) {
      if (NanoVisorSettings.Hud.vignetteEnabled()) {
         float vignetteIntensity = (float)Mth.clamp(NanoVisorSettings.Hud.vignetteIntensity(), 0.0, 1.0);
         float vignetteStrength = (float)Mth.clamp(NanoVisorSettings.Hud.vignetteStrength(), 0.1, 3.0);
         float vignetteSizeToCenter = (float)Mth.clamp(NanoVisorSettings.Hud.vignetteSizeToCenter(), 0.0, 1.0);
         float innerStart = Mth.lerp(vignetteSizeToCenter, 0.82F, 0.2F);
         float alphaScale = vignetteIntensity * vignetteStrength * hudOpacity;
         if (!(alphaScale <= 0.0F)) {
            ensureVignetteAlphaCache(alphaScale, innerStart);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferBuilder buffer = Tesselator.getInstance().begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            Matrix4f poseMatrix = guiGraphics.pose().last().pose();

            for (int y = 0; y < 40; y++) {
               float v0 = y / 40.0F;
               float v1 = (y + 1) / 40.0F;
               float py0 = v0 * screenHeight;
               float py1 = v1 * screenHeight;

               for (int x = 0; x < 72; x++) {
                  float u0 = x / 72.0F;
                  float u1 = (x + 1) / 72.0F;
                  float px0 = u0 * screenWidth;
                  float px1 = u1 * screenWidth;
                  float a00 = getVignetteCachedAlpha(x, y);
                  float a10 = getVignetteCachedAlpha(x + 1, y);
                  float a11 = getVignetteCachedAlpha(x + 1, y + 1);
                  float a01 = getVignetteCachedAlpha(x, y + 1);
                  buffer.addVertex(poseMatrix, px0, py0, 0.0F).setColor(0.0F, 0.0F, 0.0F, a00);
                  buffer.addVertex(poseMatrix, px1, py0, 0.0F).setColor(0.0F, 0.0F, 0.0F, a10);
                  buffer.addVertex(poseMatrix, px1, py1, 0.0F).setColor(0.0F, 0.0F, 0.0F, a11);
                  buffer.addVertex(poseMatrix, px0, py0, 0.0F).setColor(0.0F, 0.0F, 0.0F, a00);
                  buffer.addVertex(poseMatrix, px1, py1, 0.0F).setColor(0.0F, 0.0F, 0.0F, a11);
                  buffer.addVertex(poseMatrix, px0, py1, 0.0F).setColor(0.0F, 0.0F, 0.0F, a01);
               }
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
         }
      }
   }

   private static void ensureVignetteAlphaCache(float alphaScale, float innerStart) {
      if (vignetteCacheGridX != 72
         || vignetteCacheGridY != 40
         || !(Math.abs(vignetteCacheAlphaScale - alphaScale) < 5.0E-4F)
         || !(Math.abs(vignetteCacheInnerStart - innerStart) < 5.0E-4F)) {
         int width = 73;
         int height = 41;
         int size = width * height;
         if (vignetteAlphaCache.length != size) {
            vignetteAlphaCache = new float[size];
         }

         int idx = 0;

         for (int y = 0; y <= 40; y++) {
            float v = y / 40.0F;

            for (int x = 0; x <= 72; x++) {
               float u = x / 72.0F;
               vignetteAlphaCache[idx++] = sampleVignetteAlpha(u, v, alphaScale, innerStart);
            }
         }

         vignetteCacheGridX = 72;
         vignetteCacheGridY = 40;
         vignetteCacheAlphaScale = alphaScale;
         vignetteCacheInnerStart = innerStart;
      }
   }

   private static float getVignetteCachedAlpha(int gridX, int gridY) {
      int x = Mth.clamp(gridX, 0, 72);
      int y = Mth.clamp(gridY, 0, 40);
      int width = 73;
      int index = y * width + x;
      return index >= 0 && index < vignetteAlphaCache.length ? vignetteAlphaCache[index] : 0.0F;
   }

   private static float sampleVignetteAlpha(float u, float v, float alphaScale, float innerStart) {
      float nx = u * 2.0F - 1.0F;
      float ny = v * 2.0F - 1.0F;
      float ax = Math.abs(nx);
      float ay = Math.abs(ny);
      float radial = Mth.clamp((float)Math.sqrt(ax * ax + ay * ay) * 0.70710677F, 0.0F, 1.0F);
      float edgeFalloff = smoothstep(innerStart, 1.0F, radial);
      float cornerMask = (float)Math.pow(ax * ay, 0.55F);
      float cornerBoost = Mth.lerp(cornerMask, 0.52F, 1.0F);
      return Mth.clamp(edgeFalloff * cornerBoost * alphaScale, 0.0F, 1.0F);
   }

   private static float smoothstep(float edge0, float edge1, float x) {
      float t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0F, 1.0F);
      return t * t * (3.0F - 2.0F * t);
   }

   private static List<Component> buildHudLines(Minecraft minecraft, LocalPlayer player, float partialTick) {
      List<Component> lines = new ArrayList<>();
      Vec3 eyePos = player.getEyePosition(partialTick);
      Vec3 lookVector = player.getViewVector(partialTick);
      double maxEntityDistance = Mth.clamp(NanoVisorSettings.Hud.maxDistanceToTarget(), 1.0, 512.0);
      double maxBlockDistance = Mth.clamp(NanoVisorSettings.Hud.maxDistanceToBlock(), 1.0, 512.0);
      NanoVisorHudOverlay.AimTarget aimTarget = resolveAimTarget(player, eyePos, lookVector, maxEntityDistance, maxBlockDistance);
      if (aimTarget.entityTarget() != null) {
         Entity target = aimTarget.entityTarget();
         if (target.isAlive()) {
            if (NanoVisorSettings.Hud.showTargetName()) {
               lines.add(Component.translatable("hud.crysismod.nanovisor.target", new Object[]{target.getDisplayName()}));
            }

            if (NanoVisorSettings.Hud.showDistanceToTarget()) {
               lines.add(Component.translatable("hud.crysismod.nanovisor.distance_entity", new Object[]{formatDistance(aimTarget.entityDistance())}));
            }
         }
      } else if (aimTarget.blockDistance() >= 0.0 && NanoVisorSettings.Hud.showDistanceToBlock()) {
         lines.add(Component.translatable("hud.crysismod.nanovisor.distance_block", new Object[]{formatDistance(aimTarget.blockDistance())}));
      }

      if (NanoVisorSettings.Hud.showEnergyPercent() && NanosuitArmorUtil.hasFullSet(player)) {
         int energyPercent = resolveEnergyPercent(player);
         lines.add(Component.translatable("hud.crysismod.nanovisor.energy_percent", new Object[]{energyPercent}));
      }

      return lines;
   }

   private static String formatDistance(double distance) {
      return String.format(Locale.ROOT, "%.1f", distance);
   }

   private static int resolveEnergyPercent(LocalPlayer player) {
      float energy = java.util.Optional.<INanosuitEnergy>of(NanosuitCapabilities.get(player)).map(store -> store.getEnergy()).orElse(0.0F);
      float normalized = Mth.clamp(energy / 100.0F, 0.0F, 1.0F);
      return Mth.clamp(Math.round(normalized * 100.0F), 0, 100);
   }

   private static int withAlpha(int rgb, float opacity) {
      int alpha = Mth.clamp(Math.round(opacity * 255.0F), 0, 255);
      return (alpha & 0xFF) << 24 | rgb & 16777215;
   }

   private static NanoVisorHudOverlay.AimTarget resolveAimTarget(
      LocalPlayer player, Vec3 eyePos, Vec3 lookVector, double maxEntityDistance, double maxBlockDistance
   ) {
      double blockDistance = Double.POSITIVE_INFINITY;
      Vec3 blockEnd = eyePos.add(lookVector.scale(maxBlockDistance));
      BlockHitResult blockHitResult = player.level().clip(new ClipContext(eyePos, blockEnd, Block.OUTLINE, Fluid.NONE, player));
      if (blockHitResult.getType() == Type.BLOCK) {
         blockDistance = eyePos.distanceTo(blockHitResult.getLocation());
      }

      double entityDistance = Double.POSITIVE_INFINITY;
      Entity targetEntity = null;
      Vec3 entityEnd = eyePos.add(lookVector.scale(maxEntityDistance));
      AABB searchBox = player.getBoundingBox().expandTowards(lookVector.scale(maxEntityDistance)).inflate(1.0);
      EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
         player, eyePos, entityEnd, searchBox, entity -> isValidHudEntityTarget(player, entity), maxEntityDistance * maxEntityDistance
      );
      if (entityHitResult != null && entityHitResult.getType() == Type.ENTITY) {
         targetEntity = entityHitResult.getEntity();
         entityDistance = eyePos.distanceTo(entityHitResult.getLocation());
      }

      if (targetEntity != null && entityDistance <= maxEntityDistance && entityDistance <= blockDistance) {
         return new NanoVisorHudOverlay.AimTarget(targetEntity, entityDistance, -1.0);
      } else {
         return blockDistance <= maxBlockDistance
            ? new NanoVisorHudOverlay.AimTarget(null, -1.0, blockDistance)
            : new NanoVisorHudOverlay.AimTarget(null, -1.0, -1.0);
      }
   }

   private static boolean isValidHudEntityTarget(LocalPlayer player, Entity entity) {
      return entity != null && entity.isAlive() && entity != player && !entity.isSpectator() && entity.isPickable();
   }

   private record AimTarget(Entity entityTarget, double entityDistance, double blockDistance) {
   }
}
