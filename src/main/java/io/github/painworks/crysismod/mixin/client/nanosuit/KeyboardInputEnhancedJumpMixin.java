package io.github.painworks.crysismod.mixin.client.nanosuit;

import io.github.painworks.crysismod.nanosuit.client.EnhancedJumpClientState;
import io.github.painworks.crysismod.nanosuit.cradle.CradlePatient;
import net.minecraft.client.Minecraft;
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
      if (CradlePatient.active(Minecraft.getInstance().player)) {
         input.leftImpulse = input.forwardImpulse = 0;
         input.up = input.down = input.left = input.right = input.jumping = input.shiftKeyDown = false;
         return;
      }
      input.jumping = EnhancedJumpClientState.handleJumpInput(input.jumping);

      if (EnhancedJumpClientState.shouldSuppressCrouchInput()) {
         input.shiftKeyDown = false;
      }
   }
}
