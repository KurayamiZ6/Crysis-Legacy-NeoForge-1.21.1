package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Slime;

public final class ThermalSlimeOuterMarkerLayer extends RenderLayer<Slime, SlimeModel<Slime>> {
   private static final ResourceLocation SLIME_TEXTURE = ResourceLocation.parse("textures/entity/slime/slime.png");
   private final SlimeModel<Slime> model;

   public ThermalSlimeOuterMarkerLayer(RenderLayerParent<Slime, SlimeModel<Slime>> parent, EntityModelSet modelSet) {
      super(parent);
      this.model = new SlimeModel(modelSet.bakeLayer(ModelLayers.SLIME_OUTER));
   }

   public void render(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      Slime slime,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      LocalPlayer viewer = Minecraft.getInstance().player;
      if (ThermalMarkerEntityRenderer.shouldRenderMarker(viewer, slime) && !slime.isInvisible()) {
         ((SlimeModel)this.getParentModel()).copyPropertiesTo(this.model);
         this.model.prepareMobModel(slime, limbSwing, limbSwingAmount, partialTick);
         this.model.setupAnim(slime, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         ThermalLayerProfile profile = ThermalLayerProfile.insulatedOuter(ThermalMobProfileResolver.resolve(slime), 0.1F, 0.28F);
         ThermalMarkerModelRenderer.renderTexturedMarker(
            this.model, slime, poseStack, bufferSource, SLIME_TEXTURE, profile, ThermalScreenProjection.encodedProjectedHeight(slime)
         );
      }
   }
}
