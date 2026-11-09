package io.github.painworks.crysismod.mixin.client.nanovision;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonScopeGlowMask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.github.painworks.crysisscopefix.render.ScopeDeferredCapture", remap = false, priority = 700)
public abstract class ScopeDeferredFirstPersonGlowMaskMixin {
   @Shadow(remap = false)
   private static RenderTarget target;

   @Inject(method = "composite", at = @At("HEAD"), require = 0)
   private static void crysismod$outlineFinalVisibleScope(CallbackInfo callback) {
      ThermalFirstPersonScopeGlowMask.compositeVisibleThermalTarget(target);
   }
}
