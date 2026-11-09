package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.preview.NanovisionSettingsPreviewContext;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalLayerProfile;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerEntityRenderer;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerRenderTypes;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMobProfile;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalScreenProjection;
import io.github.painworks.crysismod.nanosuit.item.ExperimentalNanoGeoArmorItem;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class NanoGeoThermalArmorMarkerLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   private static final ThermalLayerProfile ARMOR_PROFILE = ThermalLayerProfile.of(ThermalMobProfile.CLOTHED_WARM);
   private NanoGeoArmorRenderer markerRenderer;

   public NanoGeoThermalArmorMarkerLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
      super(parent);
   }

   public void render(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      AbstractClientPlayer player,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      boolean settingsPreview = NanovisionSettingsPreviewContext.isRendering();
      if (NanosuitArmorUtil.hasFullSet(player)
         && (
            settingsPreview
               ? NanovisionConfig.thermalMarkerEnabled() && NanovisionConfig.thermalMarkerRenderPlayers()
               : ThermalMarkerEntityRenderer.shouldRenderMarker(Minecraft.getInstance().player, player)
         )) {
         ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
         ResourceLocation texture = resolveTexture(chestStack, player);
         NanoGeoArmorRenderer renderer = this.getMarkerRenderer();
         renderer.prepForRender(player, chestStack, EquipmentSlot.CHEST, (HumanoidModel<?>)this.getParentModel());
         renderer.setSuppressRender(false);
         renderer.beginFullModelRenderOverride();

         try {
            VertexConsumer consumer = bufferSource.getBuffer(ThermalMarkerRenderTypes.marker(texture));
            renderer.renderToBuffer(
               poseStack,
               consumer,
               15728880,
               OverlayTexture.NO_OVERLAY,
               settingsPreview ? NanovisionSettingsPreviewContext.encodedProjectedHeight() : ThermalScreenProjection.encodedProjectedHeight(player),
               ARMOR_PROFILE.baseHeat(),
               ARMOR_PROFILE.coldInsulation(),
               ARMOR_PROFILE.emissionClass().encoded()
            );
         } finally {
            renderer.endFullModelRenderOverride();
            renderer.setSuppressRender(false);
         }
      }
   }

   private NanoGeoArmorRenderer getMarkerRenderer() {
      if (this.markerRenderer == null) {
         this.markerRenderer = new NanoGeoArmorRenderer();
      }

      return this.markerRenderer;
   }

   private static ResourceLocation resolveTexture(ItemStack chestStack, AbstractClientPlayer player) {
      if (chestStack.getItem() instanceof ExperimentalNanoGeoArmorItem armorItem) {
         try {
            ResourceLocation resolved = armorItem.getArmorTexture(
               chestStack, player, EquipmentSlot.CHEST, armorItem.getMaterial().value().layers().getFirst(), false
            );
            if (resolved != null) {
               return resolved;
            }
         } catch (RuntimeException var5) {
         }
      }

      return NanoGeoArmorTextures.BASE_ARMOR;
   }
}
