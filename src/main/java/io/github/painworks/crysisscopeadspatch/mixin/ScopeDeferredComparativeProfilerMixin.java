package io.github.painworks.crysisscopeadspatch.mixin;

import io.github.painworks.crysisscopeadspatch.debug.NanovisionComparativeProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "io.github.painworks.crysisscopefix.render.ScopeDeferredCapture", remap = false, priority = 800)
public abstract class ScopeDeferredComparativeProfilerMixin {
   private static final ThreadLocal<NanovisionComparativeProfiler.Token> CRYSISSCOPEFIX$TOKEN = new ThreadLocal<>();

   @Inject(method = "flushDeferred", at = @At("HEAD"), require = 1)
   private static void crysisscopefix$beginScopeProfile(CallbackInfoReturnable<Boolean> cir) {
      CRYSISSCOPEFIX$TOKEN.set(NanovisionComparativeProfiler.beginSection(NanovisionComparativeProfiler.Section.SCOPE_DEFERRED));
   }

   @Inject(method = "flushDeferred", at = @At("RETURN"), require = 1)
   private static void crysisscopefix$endScopeProfile(CallbackInfoReturnable<Boolean> cir) {
      NanovisionComparativeProfiler.endSection(CRYSISSCOPEFIX$TOKEN.get(), Boolean.TRUE.equals(cir.getReturnValue()));
      CRYSISSCOPEFIX$TOKEN.remove();
   }
}
