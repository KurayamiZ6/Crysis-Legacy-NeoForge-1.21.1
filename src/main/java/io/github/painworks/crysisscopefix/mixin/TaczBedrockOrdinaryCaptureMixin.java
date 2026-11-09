package io.github.painworks.crysisscopefix.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.painworks.crysisscopefix.render.ScopeDeferredCapture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.model.bedrock.BedrockModel", remap = false, priority = 1100)
public abstract class TaczBedrockOrdinaryCaptureMixin {
   private static final String RENDER = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIFFFFLnet/minecraft/client/renderer/MultiBufferSource;)V";

   @Redirect(
      method = RENDER,
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
      ),
      require = 0,
      remap = false
   )
   private VertexConsumer crysisscopefix$teeOrdinaryScopeGeometry(MultiBufferSource source, RenderType physicalType) {
      return ScopeDeferredCapture.wrapOrdinaryModelConsumer(source.getBuffer(physicalType));
   }

   @Inject(
      method = RENDER,
      at = @At("RETURN"),
      require = 0,
      remap = false
   )
   private void crysisscopefix$finishOrdinaryScopeGeometry(CallbackInfo ci) {
      ScopeDeferredCapture.finishOrdinaryModelCapture();
   }
}
