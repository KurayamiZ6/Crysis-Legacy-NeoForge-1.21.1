package io.github.painworks.crysismod.mixin.client.minimap;

import io.github.painworks.crysismod.client.minimap.ClassicMapCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "xaero.hud.minimap.module.MinimapRenderer", remap = false)
abstract class XaeroMinimapDisplayMixin {
   @Inject(method = "render(Lxaero/hud/minimap/module/MinimapSession;Lxaero/hud/render/module/ModuleRenderContext;Lnet/minecraft/client/gui/GuiGraphics;F)V",
      at = @At("HEAD"), cancellable = true, require = 0)
   private void crysismod$hideMinimap(CallbackInfo callback) {
      if (ClassicMapCompatibility.hidesCompetingMinimap()) callback.cancel();
   }
}
