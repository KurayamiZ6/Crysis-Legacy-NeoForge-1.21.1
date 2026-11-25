package io.github.painworks.crysismod.mixin.nanosuit;

import io.github.painworks.crysismod.nanosuit.integration.BiologicalSuitIntegration;
import io.github.painworks.crysismod.nanosuit.cradle.CradlePatient;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class BiologicalPlayerArmorMixin {
   @Inject(method = "dropEquipment", at = @At("HEAD"), require = 1)
   private void crysismod$saveIntegratedEquipment(CallbackInfo callback) {
      if ((Object)this instanceof ServerPlayer player) BiologicalSuitIntegration.preserveAtDeath(player);
   }

   @Inject(method = "canTakeItem", at = @At("HEAD"), cancellable = true, require = 1)
   private void crysismod$filterDispenser(ItemStack stack, CallbackInfoReturnable<Boolean> result) {
      var equipable = Equipable.get(stack);
      Player player = (Player)(Object)this;
      if (equipable != null && (BiologicalSuitIntegration.isArmorLocked(player, equipable.getEquipmentSlot())
         || BiologicalSuitIntegration.restrictManualArmor(player, stack))) {
         result.setReturnValue(false);
      }
   }

   @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true, require = 1)
   private void crysismod$cradlePose(CallbackInfo callback) {
      Player player = (Player)(Object)this;
      if (CradlePatient.active(player) && player.isAlive()) {
         player.setPose(Pose.SWIMMING);
         callback.cancel();
      }
   }
}
