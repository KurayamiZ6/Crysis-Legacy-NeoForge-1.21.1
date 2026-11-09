package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;

public final class ThermalVillagerClothingMarkerLayer extends RenderLayer<Villager, VillagerModel<Villager>> {
   private static final ResourceLocation DEFAULT_TYPE_TEXTURE = ResourceLocation.parse("textures/entity/villager/type/plains.png");

   public ThermalVillagerClothingMarkerLayer(RenderLayerParent<Villager, VillagerModel<Villager>> parent) {
      super(parent);
   }

   public void render(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      Villager villager,
      float limbSwing,
      float limbSwingAmount,
      float partialTick,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      LocalPlayer viewer = Minecraft.getInstance().player;
      if (ThermalMarkerEntityRenderer.shouldRenderMarker(viewer, villager) && !villager.isInvisible()) {
         ThermalLayerProfile profile = ThermalLayerProfile.insulatedOuter(ThermalMobProfileResolver.resolve(villager), 0.09F, 0.18F);
         float encodedHeight = ThermalScreenProjection.encodedProjectedHeight(villager);
         ThermalMarkerModelRenderer.renderTexturedMarker(
            this.getParentModel(), villager, poseStack, bufferSource, typeTexture(villager.getVillagerData()), profile, encodedHeight
         );
         if (villager.getVillagerData().getProfession() != VillagerProfession.NONE) {
            ThermalMarkerModelRenderer.renderTexturedMarker(
               this.getParentModel(), villager, poseStack, bufferSource, professionTexture(villager.getVillagerData()), profile, encodedHeight
            );
            ThermalMarkerModelRenderer.renderTexturedMarker(
               this.getParentModel(), villager, poseStack, bufferSource, professionLevelTexture(villager.getVillagerData()), profile, encodedHeight
            );
         }
      }
   }

   private static ResourceLocation typeTexture(VillagerData data) {
      ResourceLocation key = BuiltInRegistries.VILLAGER_TYPE.getKey(data.getType());
      return key == null ? DEFAULT_TYPE_TEXTURE : ResourceLocation.fromNamespaceAndPath(key.getNamespace(), "textures/entity/villager/type/" + key.getPath() + ".png");
   }

   private static ResourceLocation professionTexture(VillagerData data) {
      ResourceLocation key = BuiltInRegistries.VILLAGER_PROFESSION.getKey(data.getProfession());
      String path = key != null ? key.getPath() : "none";
      return ResourceLocation.parse("textures/entity/villager/profession/" + path + ".png");
   }

   private static ResourceLocation professionLevelTexture(VillagerData data) {
      return switch (data.getLevel()) {
         case 2 -> ResourceLocation.parse("textures/entity/villager/profession_level/iron.png");
         case 3 -> ResourceLocation.parse("textures/entity/villager/profession_level/gold.png");
         case 4 -> ResourceLocation.parse("textures/entity/villager/profession_level/emerald.png");
         case 5 -> ResourceLocation.parse("textures/entity/villager/profession_level/diamond.png");
         default -> ResourceLocation.parse("textures/entity/villager/profession_level/stone.png");
      };
   }
}
