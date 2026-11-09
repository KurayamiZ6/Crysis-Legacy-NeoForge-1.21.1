package io.github.painworks.crysisscopeadspatch.mixin;

import io.github.painworks.crysisscopeadspatch.config.LegacySpectrumConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemShaders", remap = false, priority = 1200)
public abstract class ThermalFirstPersonItemOpacityMixin {
   @Redirect(
      method = "prepare(Lnet/minecraft/client/renderer/ShaderInstance;Lcom/mojang/blaze3d/pipeline/RenderTarget;)V",
      at = @At(value = "INVOKE", target = "Lio/github/painworks/crysismod/client/nanovision/NanovisionConfig;thermalMarkerModelOpacity()D"),
      require = 1,
      remap = false
   )
   private static double crysisscopefix$useIndependentHeldItemOpacity() {
      return LegacySpectrumConfig.heldItemOpacity();
   }
}
