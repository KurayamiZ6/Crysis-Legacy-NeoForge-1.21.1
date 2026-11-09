package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.painworks.crysisscopeadspatch.render.TaczAdsStencilMask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "io.github.painworks.crysisscopefix.render.ScopeDeferredCapture", remap = false, priority = 1200)
public abstract class ScopeDeferredCompositeMixin {
   @Shadow(remap = false)
   private static RenderTarget target;

   @Inject(
      method = "flushDeferred()Z",
      at = @At(
         value = "INVOKE",
         target = "Lio/github/painworks/crysisscopefix/render/ScopeDeferredCapture;composite(Lio/github/painworks/crysisscopefix/render/ScopeDeferredCapture$GlGlobalState;)V",
         shift = Shift.BEFORE
      ),
      require = 1
   )
   private static void crysisscopeadspatch$eraseOnlyActiveAdsAperture(CallbackInfoReturnable<Boolean> cir) {
      TaczAdsStencilMask.eraseFromThermalOverlay(target);
   }
}
