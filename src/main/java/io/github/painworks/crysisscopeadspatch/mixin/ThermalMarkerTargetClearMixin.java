package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerRenderTarget", remap = false, priority = 1200)
public abstract class ThermalMarkerTargetClearMixin {
   @Redirect(
      method = "prepareFrame",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V", remap = false),
      require = 1,
      remap = false
   )
   private static void crysisscopefix$clearWholeMarkerTarget(int mask, boolean checkError) {
      boolean scissorEnabled = GL11.glIsEnabled(3089);

      try {
         GL11.glDisable(3089);
         RenderSystem.colorMask(true, true, true, true);
         RenderSystem.depthMask(true);
         RenderSystem.clearDepth(1.0);
         RenderSystem.clear(mask, checkError);
      } finally {
         if (scissorEnabled) {
            GL11.glEnable(3089);
         } else {
            GL11.glDisable(3089);
         }
      }
   }
}
