package io.github.painworks.crysismod.mixin.client.nanovision;

import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.model.bedrock.BedrockModel", remap = false, priority = 900)
public abstract class TaczBedrockHeldItemCaptureMixin {
   @ModifyVariable(
      method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIFFFFLnet/minecraft/client/renderer/MultiBufferSource;)V",
      at = @At("HEAD"),
      argsOnly = true,
      require = 1,
      remap = false
   )
   private MultiBufferSource crysismod$wrapFinalShaderOffSource(MultiBufferSource source) {
      return source instanceof BufferSource immediate ? ThermalFirstPersonItemCapture.wrapShaderOffTaczBedrockModelSource(immediate, this) : source;
   }
}
