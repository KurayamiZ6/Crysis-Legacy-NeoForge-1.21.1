package io.github.painworks.crysismod.mixin.client.minimap;

import io.github.painworks.crysismod.client.minimap.ClassicMapCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "journeymap.client.InternalStateHandler", remap = false)
abstract class JourneyMapMinimapDisplayMixin {
   @Inject(method = "isMinimapEnabledApi()Z", at = @At("HEAD"), cancellable = true, require = 0)
   private void crysismod$hideMinimap(CallbackInfoReturnable<Boolean> callback) {
      if (ClassicMapCompatibility.hidesCompetingMinimap()) callback.setReturnValue(false);
   }
}
