package io.github.painworks.crysismod.mixin.nanosuit;

import io.github.painworks.crysismod.nanosuit.integration.BiologicalSuitIntegration;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Equipable.class)
interface BiologicalEquipableMixin {
   @Inject(method = "swapWithEquipmentSlot", at = @At("HEAD"), cancellable = true, require = 1)
   private void crysismod$preventManualReplacement(Item item, Level level, Player player, InteractionHand hand,
      CallbackInfoReturnable<InteractionResultHolder<ItemStack>> result) {
      if (BiologicalSuitIntegration.isArmorLocked(player, ((Equipable)this).getEquipmentSlot())) {
         result.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(hand)));
      }
   }
}
