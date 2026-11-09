package io.github.painworks.crysismod.client.nanovision.preview;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerRenderTypes;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMobProfile;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalScreenProjection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;
import org.slf4j.Logger;

public final class NanovisionSettingsPreviewRenderer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation WHITE_CONCRETE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/white_concrete.png");
   private static final float CALIBRATION_CUBE_HEAT = 0.64F;
   private static final float CALIBRATION_CUBE_INSULATION = 0.12F;
   private static final ModelPart WARM_TEST_CUBE = createReferenceCube();
   private static final NanovisionSettingsPreviewPostProcessor POST_PROCESSOR = new NanovisionSettingsPreviewPostProcessor();
   private static boolean failedForSession;

   private NanovisionSettingsPreviewRenderer() {
   }

   public static boolean render(
      GuiGraphics guiGraphics,
      int screenWidth,
      int screenHeight,
      int previewX,
      int previewY,
      int previewWidth,
      int previewHeight,
      int mouseX,
      int mouseY,
      float zoom
   ) {
      if (failedForSession) {
         return false;
      } else {
         try {
            return renderInternal(guiGraphics, screenWidth, screenHeight, previewX, previewY, previewWidth, previewHeight, mouseX, mouseY, zoom);
         } catch (RuntimeException var11) {
            failedForSession = true;
            LOGGER.error("[CrysisMod][Nanovision] settings preview was disabled after a rendering error", var11);
            restoreMainRenderState();
            POST_PROCESSOR.close();
            NanovisionSettingsPreviewTarget.close();
            return false;
         }
      }
   }

   private static boolean renderInternal(
      GuiGraphics guiGraphics,
      int screenWidth,
      int screenHeight,
      int previewX,
      int previewY,
      int previewWidth,
      int previewHeight,
      int mouseX,
      int mouseY,
      float zoom
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer player = minecraft.player;
      RenderTarget sceneTarget = NanovisionSettingsPreviewTarget.prepareScene();
      RenderTarget markerTarget = NanovisionSettingsPreviewTarget.markerTarget();
      if (player != null && sceneTarget != null && markerTarget != null && minecraft.getMainRenderTarget() != null) {
         int playerAreaHeight = Math.max(80, Math.round(previewHeight * 0.72F));
         int anchorX = previewX + previewWidth / 2;
         int anchorY = previewY + playerAreaHeight - 2;
         int scale = Math.max(28, Math.round(Math.min(previewWidth, playerAreaHeight) * 0.34F * zoom));
         float followX = anchorX - mouseX;
         float followY = previewY + playerAreaHeight * 0.45F - mouseY;
         int scissorHalfWidth = Math.max(anchorX - previewX, previewX + previewWidth - anchorX);
         int scissorHalfHeight = Math.max(anchorY - previewY, previewY + previewHeight - anchorY);
         // The inventory helper centers its bounds and offsets the model by half its height.
         float playerYOffset = -player.getBbHeight() / (2.0F * player.getScale());
         float encodedPlayerHeight = ThermalScreenProjection.encodeProjectedHeightPixels(scale * 2.0F);
         NanovisionSettingsPreviewContext.begin(sceneTarget, markerTarget, encodedPlayerHeight);

         try {
            guiGraphics.enableScissor(previewX, previewY, previewX + previewWidth, previewY + previewHeight);
            InventoryScreen.renderEntityInInventoryFollowsMouse(
               guiGraphics,
               anchorX - scissorHalfWidth,
               anchorY - scissorHalfHeight,
               anchorX + scissorHalfWidth,
               anchorY + scissorHalfHeight,
               scale,
               playerYOffset,
               anchorX - followX,
               anchorY - followY,
               player
            );
            renderReferenceCube(guiGraphics, previewX, previewY, previewWidth, previewHeight);
         } finally {
            guiGraphics.disableScissor();
            NanovisionSettingsPreviewContext.end();
            minecraft.getMainRenderTarget().bindWrite(true);
         }

         if (!POST_PROCESSOR.process(sceneTarget, markerTarget, 0.0F)) {
            minecraft.getMainRenderTarget().bindWrite(true);
            return false;
         } else {
            minecraft.getMainRenderTarget().bindWrite(true);
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, sceneTarget.getColorTextureId());
            float framebufferScaleX = (float)minecraft.getMainRenderTarget().width / Math.max(1, screenWidth);
            float framebufferScaleY = (float)minecraft.getMainRenderTarget().height / Math.max(1, screenHeight);
            RenderSystem.enableScissor(
               Math.round(previewX * framebufferScaleX),
               Math.round((screenHeight - previewY - previewHeight) * framebufferScaleY),
               Math.max(1, Math.round(previewWidth * framebufferScaleX)),
               Math.max(1, Math.round(previewHeight * framebufferScaleY))
            );
            float u0 = (float)previewX / screenWidth;
            float u1 = (float)(previewX + previewWidth) / screenWidth;
            float v0 = 1.0F - (float)previewY / screenHeight;
            float v1 = 1.0F - (float)(previewY + previewHeight) / screenHeight;
            Matrix4f pose = guiGraphics.pose().last().pose();
            BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.addVertex(pose, previewX, previewY + previewHeight, 0.0F).setUv(u0, v1);
            buffer.addVertex(pose, previewX + previewWidth, previewY + previewHeight, 0.0F).setUv(u1, v1);
            buffer.addVertex(pose, previewX + previewWidth, previewY, 0.0F).setUv(u1, v0);
            buffer.addVertex(pose, previewX, previewY, 0.0F).setUv(u0, v0);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
            RenderSystem.disableScissor();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            return true;
         }
      } else {
         if (minecraft.getMainRenderTarget() != null) {
            minecraft.getMainRenderTarget().bindWrite(true);
         }

         return false;
      }
   }

   private static void renderReferenceCube(GuiGraphics guiGraphics, int previewX, int previewY, int previewWidth, int previewHeight) {
      Minecraft minecraft = Minecraft.getInstance();
      BufferSource buffers = minecraft.renderBuffers().bufferSource();
      int cubeScale = Math.max(22, Math.round(Math.min(previewWidth, previewHeight) * 0.13F));
      float cubeCenterX = previewX + previewWidth * 0.24F;
      float cubeCenterY = previewY + previewHeight - cubeScale * 0.72F - 10.0F;
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(cubeCenterX, cubeCenterY, 180.0F);
      guiGraphics.pose().scale(cubeScale, -cubeScale, cubeScale);
      guiGraphics.pose().mulPose(Axis.XP.rotationDegrees(24.0F));
      guiGraphics.pose().mulPose(Axis.YP.rotationDegrees(38.0F));
      RenderType normalType = RenderType.entityCutoutNoCull(WHITE_CONCRETE);
      WARM_TEST_CUBE.render(guiGraphics.pose(), buffers.getBuffer(normalType), 15728880, OverlayTexture.NO_OVERLAY);
      buffers.endBatch(normalType);
      if (NanovisionConfig.thermalMarkerEnabled()) {
         ThermalMobProfile profile = ThermalMobProfile.CLOTHED_WARM;
         float encodedHeight = ThermalScreenProjection.encodeProjectedHeightPixels(cubeScale * 1.42F);
         RenderType markerType = ThermalMarkerRenderTypes.markerSolid();
         WARM_TEST_CUBE.render(
            guiGraphics.pose(),
            buffers.getBuffer(markerType),
            15728880,
            OverlayTexture.NO_OVERLAY,
            FastColor.ARGB32.colorFromFloat(profile.emissionClass().encoded(), encodedHeight, CALIBRATION_CUBE_HEAT, CALIBRATION_CUBE_INSULATION)
         );
         buffers.endBatch(markerType);
      }

      guiGraphics.pose().popPose();
   }

   private static ModelPart createReferenceCube() {
      MeshDefinition mesh = new MeshDefinition();
      mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.ZERO);
      return LayerDefinition.create(mesh, 64, 32).bakeRoot().getChild("cube");
   }

   public static void close() {
      POST_PROCESSOR.close();
      NanovisionSettingsPreviewTarget.close();
      failedForSession = false;
   }

   private static void restoreMainRenderState() {
      NanovisionSettingsPreviewContext.end();
      RenderSystem.disableScissor();
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.getMainRenderTarget() != null) {
         minecraft.getMainRenderTarget().bindWrite(true);
      }
   }
}
