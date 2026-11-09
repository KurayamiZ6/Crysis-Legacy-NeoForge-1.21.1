package io.github.painworks.crysisscopeadspatch.mixin;

import io.github.painworks.crysisscopeadspatch.debug.NanovisionComparativeCommands;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.CrysisModClientCommands", remap = false, priority = 1100)
public abstract class ComparativeProfilerCommandMixin {
   @Inject(method = "onRegisterClientCommands", at = @At("RETURN"), require = 1)
   private static void crysisscopefix$registerComparativeProfiler(RegisterClientCommandsEvent event, CallbackInfo ci) {
      NanovisionComparativeCommands.register(event.getDispatcher());
   }
}
