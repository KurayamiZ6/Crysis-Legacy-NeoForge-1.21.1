package io.github.painworks.crysismod.mixin.client.nanosuit;

import io.github.painworks.crysismod.nanosuit.client.EnhancedJumpClientState;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerEnhancedJumpMixin {
   @Inject(method = "isMovingSlowly", at = @At("RETURN"), cancellable = true)
   private void crysismod$keepEnhancedJumpAtFullInputSpeed(CallbackInfoReturnable<Boolean> callback) {
      boolean bypassSlowdown = EnhancedJumpClientState.shouldBypassSneakSlowdown();
      EnhancedJumpClientState.recordSlowdownDecision((Boolean)callback.getReturnValue(), bypassSlowdown);
      if (bypassSlowdown) {
         callback.setReturnValue(false);
      }
   }
}
