package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.painworks.crysisscopeadspatch.render.TaczAdsStencilMask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderSystem.class, priority = 1200)
public abstract class RenderSystemStencilClearMixin {
   @Inject(method = "clear(IZ)V", at = @At("HEAD"))
   private static void crysisscopeadspatch$captureBeforeStencilClear(int mask, boolean checkError, CallbackInfo ci) {
      if ((mask & 1024) != 0) {
         TaczAdsStencilMask.captureBeforeStencilClear();
      }
   }
}
