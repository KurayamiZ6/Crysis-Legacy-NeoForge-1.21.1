package io.github.painworks.crysisscopeadspatch.mixin;

import io.github.painworks.crysisscopeadspatch.debug.NanovisionComparativeProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture", remap = false, priority = 1200)
public abstract class ThermalHeldItemCausalProfilerMixin {
   @Inject(method = "beginHandPass", at = @At("HEAD"), cancellable = true, require = 1)
   private static void crysisscopefix$skipHeldItemCapture(CallbackInfo ci) {
      if (NanovisionComparativeProfiler.suppressHeldItemCapture()) {
         ci.cancel();
      }
   }
}
