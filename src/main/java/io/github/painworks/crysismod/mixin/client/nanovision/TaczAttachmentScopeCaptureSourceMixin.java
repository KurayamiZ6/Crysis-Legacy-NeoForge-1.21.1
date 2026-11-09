package io.github.painworks.crysismod.mixin.client.nanovision;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalThirdPersonHeldItemCapture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.model.BedrockAttachmentModel", remap = false, priority = 900)
public abstract class TaczAttachmentScopeCaptureSourceMixin {
   private static final String RENDER_TEMP_PART_WITH_ALPHA =
      "renderTempPart(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILjava/util/List;FLnet/minecraft/client/renderer/MultiBufferSource;)V";

   @ModifyVariable(method = RENDER_TEMP_PART_WITH_ALPHA, at = @At("HEAD"), argsOnly = true, require = 1, remap = false)
   private MultiBufferSource crysismod$wrapDirectTaczAttachmentSource(MultiBufferSource source) {
      return source instanceof BufferSource immediate ? ThermalFirstPersonItemCapture.wrapTaczAttachmentSource(immediate) : source;
   }

   @Redirect(
      method = RENDER_TEMP_PART_WITH_ALPHA,
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
      require = 1,
      remap = false
   )
   private VertexConsumer crysismod$teeThirdPersonAttachmentModel(MultiBufferSource source, RenderType renderType) {
      // The source may already own the first-person outline. Adding it again writes two vertices to the same builder.
      return ThermalThirdPersonHeldItemCapture.wrapTaczBedrockConsumer(source, renderType, source.getBuffer(renderType));
   }
}
