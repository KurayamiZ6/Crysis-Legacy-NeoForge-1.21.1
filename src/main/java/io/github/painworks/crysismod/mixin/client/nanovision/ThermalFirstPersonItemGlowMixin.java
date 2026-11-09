package io.github.painworks.crysismod.mixin.client.nanovision;

import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemGlowCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ThermalFirstPersonItemCapture.class, priority = 900, remap = false)
public abstract class ThermalFirstPersonItemGlowMixin {
   @Inject(method = "beginFrame", at = @At("HEAD"), require = 1)
   private static void crysismod$beginGlowFrame(CallbackInfo callback) {
      ThermalFirstPersonItemGlowCapture.beginFrame();
   }

   @Inject(method = "flushDeferred", at = @At("RETURN"), require = 1)
   private static void crysismod$compositeGlowAfterAcceptedSpectrum(CallbackInfoReturnable<Boolean> callback) {
      ThermalFirstPersonItemGlowCapture.flush(Boolean.TRUE.equals(callback.getReturnValue()));
   }

   @Inject(method = "discardDeferred", at = @At("HEAD"), require = 1)
   private static void crysismod$discardGlow(CallbackInfo callback) {
      ThermalFirstPersonItemGlowCapture.discard();
   }
}
