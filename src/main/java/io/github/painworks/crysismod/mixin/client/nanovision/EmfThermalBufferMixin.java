package io.github.painworks.crysismod.mixin.client.nanovision;

import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerCompatibilityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "traben.entity_model_features.models.parts.EMFModelPart", remap = false)
public abstract class EmfThermalBufferMixin {
   @ModifyArg(method = "renderWithTextureOverride", index = 0, require = 0, at = @At(
      value = "INVOKE",
      target = "Ltraben/entity_texture_features/utils/URenderTypeToVertexConsumer;<init>(Lnet/minecraft/client/renderer/MultiBufferSource;)V"
   ))
   private MultiBufferSource crysismod$routeThermalTextureOverride(MultiBufferSource vanillaBuffer) {
      return ThermalMarkerCompatibilityRenderer.resolveReplayBuffer(vanillaBuffer);
   }
}
