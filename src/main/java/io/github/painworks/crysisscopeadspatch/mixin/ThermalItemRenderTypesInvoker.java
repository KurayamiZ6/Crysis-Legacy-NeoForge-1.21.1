package io.github.painworks.crysisscopeadspatch.mixin;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemRenderTypes", remap = false)
public interface ThermalItemRenderTypesInvoker {
   @Invoker("capture")
   static RenderType crysisscopefix$capture(RenderType original) {
      throw new AssertionError();
   }
}
