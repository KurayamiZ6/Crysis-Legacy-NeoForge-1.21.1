package io.github.painworks.crysismod.mixin.client.nanovision;

import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmComposite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ThermalFirstPersonArmComposite.class, priority = 1100)
public abstract class ThermalFirstPersonArmScissorMixin {
   @Redirect(method = "renderInternal", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableScissor(IIII)V"), require = 1)
   private static void crysismod$removeFixedArmScreenScissor(int x, int y, int width, int height) {
   }
}
