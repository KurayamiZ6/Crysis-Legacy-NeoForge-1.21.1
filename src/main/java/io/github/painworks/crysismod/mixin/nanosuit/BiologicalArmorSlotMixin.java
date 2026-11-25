package io.github.painworks.crysismod.mixin.nanosuit;

import io.github.painworks.crysismod.nanosuit.integration.BiologicalSuitIntegration;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.ArmorSlot")
abstract class BiologicalArmorSlotMixin {
   @Shadow @Final private LivingEntity owner;
   @Shadow @Final private EquipmentSlot slot;

   @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true, require = 1)
   private void crysismod$keepIntegratedPiece(Player actor, CallbackInfoReturnable<Boolean> result) {
      if (locked()) result.setReturnValue(false);
   }

   @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true, require = 1)
   private void crysismod$keepArmorSlot(ItemStack candidate, CallbackInfoReturnable<Boolean> result) {
      if (locked()) result.setReturnValue(false);
   }

   private boolean locked() {
      return owner instanceof Player player && BiologicalSuitIntegration.isArmorLocked(player, slot);
   }
}
