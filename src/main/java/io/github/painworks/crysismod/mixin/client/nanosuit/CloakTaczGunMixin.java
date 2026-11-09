package io.github.painworks.crysismod.mixin.client.nanosuit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitCloakRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.model.BedrockGunModel", remap = false)
public abstract class CloakTaczGunMixin {
   @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/compat/ar/ARCompat;shouldAccelerate()Z"), remap = false)
   private boolean crysismod$keepAnimatedGunInCloakPass(Operation<Boolean> original) {
      return !NanosuitCloakRender.isDrawing() && original.call();
   }
}
