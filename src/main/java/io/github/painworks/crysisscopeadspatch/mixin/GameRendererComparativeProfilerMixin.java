package io.github.painworks.crysisscopeadspatch.mixin;

import io.github.painworks.crysisscopeadspatch.debug.NanovisionComparativeProfiler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 800)
public abstract class GameRendererComparativeProfilerMixin {
   private NanovisionComparativeProfiler.Token crysisscopefix$profileFrame;

   @Inject(method = "render", at = @At("HEAD"), require = 1)
   private void crysisscopefix$beginProfileFrame(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
      this.crysisscopefix$profileFrame = NanovisionComparativeProfiler.beginFrame();
   }

   @Inject(method = "render", at = @At("RETURN"), require = 1)
   private void crysisscopefix$endProfileFrame(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
      NanovisionComparativeProfiler.endFrame(this.crysisscopefix$profileFrame);
      this.crysisscopefix$profileFrame = null;
   }
}
