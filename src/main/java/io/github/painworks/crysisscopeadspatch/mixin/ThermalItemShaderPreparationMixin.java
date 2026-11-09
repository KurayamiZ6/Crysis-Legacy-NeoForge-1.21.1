package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.painworks.crysisscopeadspatch.render.OptimizedTaczShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemShaders", remap = false, priority = 1100)
public abstract class ThermalItemShaderPreparationMixin {
   @Inject(method = "prepareGeometryOverlay(Lcom/mojang/blaze3d/pipeline/RenderTarget;)V", at = @At("RETURN"), require = 1)
   private static void crysisscopefix$prepareCompactTaczShader(RenderTarget target, CallbackInfo ci) {
      OptimizedTaczShaders.prepare(target);
   }
}
