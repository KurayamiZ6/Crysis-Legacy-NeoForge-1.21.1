package io.github.painworks.crysismod.mixin.client.nanosuit;

import io.github.painworks.crysismod.nanosuit.client.EnhancedJumpClientState;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputEnhancedJumpMixin {
   @Inject(method = "tick", at = @At("TAIL"))
   private void crysismod$clearCrouchOnlyAfterConfirmedTakeoff(boolean slowDown, float slowDownFactor, CallbackInfo callback) {
      Input input = (Input)(Object)this;
      input.jumping = EnhancedJumpClientState.handleJumpInput(input.jumping);

      if (EnhancedJumpClientState.shouldSuppressCrouchInput()) {
         input.shiftKeyDown = false;
      }
   }
}
