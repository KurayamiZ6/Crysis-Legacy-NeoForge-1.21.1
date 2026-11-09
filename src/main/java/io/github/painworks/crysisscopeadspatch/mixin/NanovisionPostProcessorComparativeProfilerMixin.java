package io.github.painworks.crysisscopeadspatch.mixin;

import io.github.painworks.crysisscopeadspatch.debug.NanovisionComparativeProfiler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.NanovisionPostProcessor", remap = false, priority = 800)
public abstract class NanovisionPostProcessorComparativeProfilerMixin {
   private NanovisionComparativeProfiler.Token crysisscopefix$postProfile;

   @Inject(method = "renderAfterLevel", at = @At("HEAD"), require = 1)
   private void crysisscopefix$beginPostProfile(Minecraft minecraft, float partialTick, CallbackInfo ci) {
      this.crysisscopefix$postProfile = NanovisionComparativeProfiler.beginSection(NanovisionComparativeProfiler.Section.POST_PROCESSOR);
   }

   @Inject(method = "renderAfterLevel", at = @At("RETURN"), require = 1)
   private void crysisscopefix$endPostProfile(Minecraft minecraft, float partialTick, CallbackInfo ci) {
      NanovisionComparativeProfiler.endSection(this.crysisscopefix$postProfile, true);
      this.crysisscopefix$postProfile = null;
   }
}
