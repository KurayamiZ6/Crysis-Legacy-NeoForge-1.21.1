package io.github.painworks.crysisscopefix.mixin;

import io.github.painworks.crysisscopefix.render.ScopeDeferredCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture", remap = false, priority = 1100)
public abstract class CrysisThermalCaptureFixMixin {
   @Inject(method = "beginFrame()V", at = @At("HEAD"), require = 0)
   private static void crysisscopefix$beginFrame(CallbackInfo ci) {
      ScopeDeferredCapture.beginFrame();
   }

   @Inject(method = "flushDeferred()Z", at = @At("RETURN"), require = 0)
   private static void crysisscopefix$flushAfterStableCapture(CallbackInfoReturnable<Boolean> cir) {
      ScopeDeferredCapture.flushDeferred();
   }

   @Inject(method = "discardDeferred()V", at = @At("RETURN"), require = 0)
   private static void crysisscopefix$discard(CallbackInfo ci) {
      ScopeDeferredCapture.discard();
   }
}
