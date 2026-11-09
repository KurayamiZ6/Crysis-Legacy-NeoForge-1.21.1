package io.github.painworks.crysismod.mixin.client.nanovision;

import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonHandTransitionState;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ThermalFirstPersonItemCapture.class, priority = 1100)
public abstract class ThermalFirstPersonEmptySlotTransitionMixin {
   @Redirect(method = "canCapture", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0), require = 1)
   private static boolean crysismod$keepMainHandCaptureDuringEmptySlotTransition(ItemStack stack) {
      return stack.isEmpty() && !ThermalFirstPersonHandTransitionState.keepMainHandCaptureForOutgoingItem();
   }

   @Redirect(method = "canCapture", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1), require = 1)
   private static boolean crysismod$keepOffHandCaptureDuringEmptySlotTransition(ItemStack stack) {
      return stack.isEmpty() && !ThermalFirstPersonHandTransitionState.keepOffHandCaptureForOutgoingItem();
   }
}
