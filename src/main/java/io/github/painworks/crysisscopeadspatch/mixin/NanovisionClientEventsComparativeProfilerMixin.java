package io.github.painworks.crysisscopeadspatch.mixin;

import io.github.painworks.crysisscopeadspatch.debug.NanovisionComparativeProfiler;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.NanovisionClientEvents", remap = false, priority = 800)
public abstract class NanovisionClientEventsComparativeProfilerMixin {
   private NanovisionComparativeProfiler.Token crysisscopefix$stageProfile;

   @Inject(method = "onRenderLevelStage", at = @At("HEAD"), require = 1)
   private void crysisscopefix$beginStageProfile(RenderLevelStageEvent event, CallbackInfo ci) {
      NanovisionComparativeProfiler.Section section = crysisscopefix$section(event);
      this.crysisscopefix$stageProfile = section == null ? null : NanovisionComparativeProfiler.beginSection(section);
   }

   @Inject(method = "onRenderLevelStage", at = @At("RETURN"), require = 1)
   private void crysisscopefix$endStageProfile(RenderLevelStageEvent event, CallbackInfo ci) {
      NanovisionComparativeProfiler.endSection(this.crysisscopefix$stageProfile, true);
      this.crysisscopefix$stageProfile = null;
   }

   private static NanovisionComparativeProfiler.Section crysisscopefix$section(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_SKY) {
         return NanovisionComparativeProfiler.Section.MARKER_PREPARE;
      } else if (event.getStage() == Stage.AFTER_ENTITIES) {
         return NanovisionComparativeProfiler.Section.MARKER_ENTITIES;
      } else {
         return event.getStage() == Stage.AFTER_LEVEL ? NanovisionComparativeProfiler.Section.AFTER_LEVEL : null;
      }
   }
}
