package io.github.painworks.crysismod.nanosuit.item;

import io.github.painworks.crysismod.client.nanogeo.NanoGeoClientConfig;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackStableTextures;
import io.github.painworks.crysismod.nanosuit.NanosuitPresentation;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class ExperimentalNanoGeoArmorItem extends ArmorItem {
   private static final String UV_DEBUG_GRADIENT_TEXTURE = "crysismod:textures/armor/nanosuit_uv_gradient_test.png";

   public ExperimentalNanoGeoArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
      super(material, type, properties);
   }

   @Override
   public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel) {
      NanosuitPresentation presentation = NanosuitPresentation.forPiece(stack, slot);
      ResourceLocation armorTexture = (presentation != null ? presentation : NanosuitPresentation.LEGACY_CURRENT).armorTexture();
      return FMLEnvironment.dist == Dist.CLIENT ? ResourceLocation.parse(resolveArmorTextureClient(armorTexture)) : armorTexture;
   }

   private static String resolveArmorTextureClient(ResourceLocation armorTexture) {
      String requestedTexture = NanoGeoClientConfig.nanoGeoDebugUseGradientArmorTexture()
         ? UV_DEBUG_GRADIENT_TEXTURE
         : armorTexture.toString();
      return NanoGeoShaderpackStableTextures.resolveArmorTexturePath(requestedTexture);
   }
}
