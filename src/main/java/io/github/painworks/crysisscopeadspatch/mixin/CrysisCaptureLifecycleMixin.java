package io.github.painworks.crysisscopeadspatch.mixin;

import io.github.painworks.crysisscopeadspatch.render.TaczAdsStencilMask;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture", remap = false, priority = 1200)
public abstract class CrysisCaptureLifecycleMixin {
   @Inject(method = "beginFrame()V", at = @At("HEAD"), require = 0)
   private static void crysisscopeadspatch$beginFrame(CallbackInfo ci) {
      TaczAdsStencilMask.beginFrame();
   }

   @Redirect(
      method = "flushDeferred()Z",
      at = @At(value = "INVOKE", target = "Lio/github/painworks/crysismod/client/nanovision/thermal/ThermalDeferredBufferSource;endBatch()V"),
      require = 1
   )
   private static void crysisscopeadspatch$drawOutsideAdsAperture(@Coerce Object source) {
      boolean exclusionActive = TaczAdsStencilMask.beginStableThermalExclusion();

      try {
         ((BufferSource) source).endBatch();
      } finally {
         if (exclusionActive) {
            TaczAdsStencilMask.endStableThermalExclusion();
         }
      }
   }

   @Inject(method = "discardDeferred()V", at = @At("RETURN"), require = 0)
   private static void crysisscopeadspatch$discard(CallbackInfo ci) {
      TaczAdsStencilMask.discard();
   }
}
