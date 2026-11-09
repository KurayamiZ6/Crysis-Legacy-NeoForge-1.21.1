package io.github.painworks.crysisscopeadspatch.mixin;

import io.github.painworks.crysisscopeadspatch.debug.NanovisionComparativeProfiler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmOcclusionDepth", remap = false, priority = 800)
public abstract class ArmOcclusionDepthComparativeProfilerMixin {
   private static final ThreadLocal<NanovisionComparativeProfiler.Token> CRYSISSCOPEFIX$TOKEN = new ThreadLocal<>();

   @Inject(method = "captureAfterLevel", at = @At("HEAD"), require = 1)
   private static void crysisscopefix$beginDepthProfile(Minecraft minecraft, CallbackInfo ci) {
      CRYSISSCOPEFIX$TOKEN.set(NanovisionComparativeProfiler.beginSection(NanovisionComparativeProfiler.Section.ARM_OCCLUSION_DEPTH));
   }

   @Inject(method = "captureAfterLevel", at = @At("RETURN"), require = 1)
   private static void crysisscopefix$endDepthProfile(Minecraft minecraft, CallbackInfo ci) {
      NanovisionComparativeProfiler.endSection(CRYSISSCOPEFIX$TOKEN.get(), true);
      CRYSISSCOPEFIX$TOKEN.remove();
   }
}
