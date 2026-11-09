package io.github.painworks.crysismod.mixin.client.nanovision;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalThirdPersonHeldItemCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "io.github.painworks.crysisscopefix.render.ScopeDeferredCapture", remap = false, priority = 900)
public abstract class ScopeDeferredThirdPersonTaczThermalMixin {
   @Inject(method = "wrapOrdinaryModelConsumer", at = @At("RETURN"), cancellable = true, require = 1, remap = false)
   private static void crysismod$teeThirdPersonTaczGun(VertexConsumer physicalOrScopeConsumer, CallbackInfoReturnable<VertexConsumer> callback) {
      callback.setReturnValue(ThermalThirdPersonHeldItemCapture.wrapScopeDeferredTaczConsumer((VertexConsumer)callback.getReturnValue()));
   }
}
