package io.github.painworks.crysismod.mixin.client.nanosuit;

import io.github.painworks.crysismod.nanosuit.client.render.NanosuitCloakRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "punchy.compat.InvisibleArms", remap = false)
public abstract class CloakPunchyArmsMixin {
   @Inject(method = "invisible", at = @At("HEAD"), cancellable = true, remap = false)
   private static void crysismod$keepAnimatedCloakArms(CallbackInfoReturnable<Boolean> callback) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (NanosuitCloakRender.isActive(player) && NanosuitCloakRender.keepsArmGeometry(player)) {
         callback.setReturnValue(false);
      }
   }
}
