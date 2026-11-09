package io.github.painworks.crysisscopeadspatch.mixin;

import io.github.painworks.crysisscopeadspatch.debug.NanovisionComparativeProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
   targets = {
         "io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerRenderLayer",
         "io.github.painworks.crysismod.client.nanovision.thermal.ThermalSheepWoolMarkerLayer",
         "io.github.painworks.crysismod.client.nanovision.thermal.ThermalSlimeOuterMarkerLayer",
         "io.github.painworks.crysismod.client.nanovision.thermal.ThermalVillagerClothingMarkerLayer"
   },
   remap = false,
   priority = 1200
)
public abstract class ThermalWorldLayerCausalProfilerMixin {
   @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
   private void crysisscopefix$skipThermalWorldLayer(CallbackInfo ci) {
      if (NanovisionComparativeProfiler.suppressWorldThermalCapture()) {
         ci.cancel();
      }
   }
}
