package io.github.painworks.crysisscopeadspatch.mixin;

import io.github.painworks.crysisscopeadspatch.render.ShaderpackRenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerModelRenderer", remap = false, priority = 1200)
public abstract class ThermalMarkerShadowPassGuardMixin {
   @Inject(method = "renderTexturedMarkerInternal", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
   private static void crysisscopefix$rejectShaderpackShadowGeometry(CallbackInfoReturnable<?> cir) {
      if (ShaderpackRenderPhase.isShadowPass()) {
         cir.setReturnValue(null);
      }
   }
}
