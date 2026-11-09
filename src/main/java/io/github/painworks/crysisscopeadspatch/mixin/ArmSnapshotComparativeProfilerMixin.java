package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import io.github.painworks.crysisscopeadspatch.debug.NanovisionComparativeProfiler;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemArmSnapshots", remap = false, priority = 800)
public abstract class ArmSnapshotComparativeProfilerMixin {
   private static final ThreadLocal<NanovisionComparativeProfiler.Token> CRYSISSCOPEFIX$TOKEN = new ThreadLocal<>();

   @Inject(method = "capture", at = @At("HEAD"), require = 1)
   private static void crysisscopefix$beginSnapshotProfile(HumanoidArm arm, RenderTarget source, CallbackInfo ci) {
      CRYSISSCOPEFIX$TOKEN.set(NanovisionComparativeProfiler.beginSection(NanovisionComparativeProfiler.Section.ARM_SNAPSHOT));
   }

   @Inject(method = "capture", at = @At("RETURN"), require = 1)
   private static void crysisscopefix$endSnapshotProfile(HumanoidArm arm, RenderTarget source, CallbackInfo ci) {
      NanovisionComparativeProfiler.endSection(CRYSISSCOPEFIX$TOKEN.get(), true);
      CRYSISSCOPEFIX$TOKEN.remove();
   }
}
