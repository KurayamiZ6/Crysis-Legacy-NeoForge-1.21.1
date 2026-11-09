package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemShaders", remap = false)
public interface ThermalItemShadersInvoker {
   @Invoker("prepare")
   static void crysisscopefix$prepare(ShaderInstance shader, RenderTarget target) {
      throw new AssertionError();
   }
}
