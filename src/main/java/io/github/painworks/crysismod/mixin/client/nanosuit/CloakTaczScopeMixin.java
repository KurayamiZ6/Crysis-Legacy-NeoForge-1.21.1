package io.github.painworks.crysismod.mixin.client.nanosuit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitCloakRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.model.BedrockAttachmentModel", remap = false)
public abstract class CloakTaczScopeMixin {
   @WrapOperation(method = {"renderBoth", "renderSight", "renderScope"}, at = @At(value = "INVOKE", target = "Lcom/tacz/guns/compat/ar/ARCompat;shouldAccelerate()Z"), remap = false)
   private boolean crysismod$keepScopeHousingInCloakPass(Operation<Boolean> original) {
      return !NanosuitCloakRender.isDrawing() && original.call();
   }

   // These are projected lens, reticle and stencil planes, not the physical scope housing.
   @Inject(method = {"renderOcularStencil", "renderOcularAndDivision", "renderDivisionOnly"}, at = @At("HEAD"), cancellable = true, remap = false)
   private void crysismod$ignoreScopeProjectionsInCloakPass(CallbackInfo callback) {
      if (NanosuitCloakRender.isDrawing()) {
         callback.cancel();
      }
   }
}
