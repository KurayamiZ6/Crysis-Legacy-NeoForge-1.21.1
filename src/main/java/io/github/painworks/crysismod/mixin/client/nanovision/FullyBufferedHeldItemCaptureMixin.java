package io.github.painworks.crysismod.mixin.client.nanovision;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource", priority = 900, remap = false)
public abstract class FullyBufferedHeldItemCaptureMixin {
   @Inject(method = "getBuffer", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
   private void crysismod$teeFullyBufferedItemConsumer(RenderType renderType, CallbackInfoReturnable<VertexConsumer> callback) {
      VertexConsumer original = (VertexConsumer)callback.getReturnValue();
      VertexConsumer wrapped = ThermalFirstPersonItemCapture.wrapBackend(renderType, original);
      if (wrapped != original) {
         callback.setReturnValue(wrapped);
      }
   }
}
