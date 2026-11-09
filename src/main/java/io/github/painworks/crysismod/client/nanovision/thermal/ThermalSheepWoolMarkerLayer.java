package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;

public final class ThermalSheepWoolMarkerLayer extends RenderLayer<Sheep, SheepModel<Sheep>> {
   private static final ResourceLocation SHEEP_FUR_TEXTURE = ResourceLocation.parse("textures/entity/sheep/sheep_fur.png");
   private final SheepFurModel<Sheep> model;

   public ThermalSheepWoolMarkerLayer(RenderLayerParent<Sheep, SheepModel<Sheep>> parent, EntityModelSet modelSet) {
      super(parent);
      this.model = new SheepFurModel(modelSet.bakeLayer(ModelLayers.SHEEP_FUR));
   }

   public void render(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      Sheep sheep,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      LocalPlayer viewer = Minecraft.getInstance().player;
      if (ThermalMarkerEntityRenderer.shouldRenderMarker(viewer, sheep) && !sheep.isSheared() && !sheep.isInvisible()) {
         ((SheepModel)this.getParentModel()).copyPropertiesTo(this.model);
         this.model.prepareMobModel(sheep, limbSwing, limbSwingAmount, partialTick);
         this.model.setupAnim(sheep, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         ThermalLayerProfile profile = ThermalLayerProfile.insulatedOuter(ThermalMobProfileResolver.resolve(sheep), 0.18F, 0.44F);
         ThermalMarkerModelRenderer.renderTexturedMarker(
            this.model, sheep, poseStack, bufferSource, SHEEP_FUR_TEXTURE, profile, ThermalScreenProjection.encodedProjectedHeight(sheep)
         );
      }
   }
}
