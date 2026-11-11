package io.github.painworks.crysismod.nanosuit;

import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Resource identifiers shared by the third-person and first-person renderers. */
public record NanosuitPresentation(ResourceLocation armorModel, ResourceLocation armorTexture, ResourceLocation armorModeOverlayTexture) {
   public static final NanosuitPresentation LEGACY_CURRENT = new NanosuitPresentation(
      ResourceLocation.fromNamespaceAndPath("crysismod", "nanogeo/nanosuit_armor.json"),
      ResourceLocation.fromNamespaceAndPath("crysismod", "textures/armor/nanosuit_texture.png"),
      ResourceLocation.fromNamespaceAndPath("crysismod", "textures/armor/nanosuit_armor_mode.png")
   );

   public static NanosuitPresentation forPiece(ItemStack stack, EquipmentSlot slot) {
      NanosuitProfile profile = stack == null ? null : NanosuitArmorUtil.getPieceProfile(stack, slot);
      // Both generations use the same model and textures while their equipment is validated.
      return profile != null ? LEGACY_CURRENT : null;
   }
}
