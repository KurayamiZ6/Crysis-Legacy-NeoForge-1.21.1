package io.github.painworks.crysismod.mixin.nanosuit;

import io.github.painworks.crysismod.nanosuit.integration.BiologicalSuitIntegration;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
abstract class BiologicalDurabilityMixin {
   // Limit the final damage after enchantments and before vanilla checks for destruction.
   @ModifyVariable(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
      at = @At("STORE"), ordinal = 1, require = 1)
   private int crysismod$capWear(int proposed, int amount, ServerLevel level, LivingEntity wearer, Consumer<Item> onBreak) {
      ItemStack piece = (ItemStack)(Object)this;
      if (amount <= 0 || !(wearer instanceof ServerPlayer player)
         || !BiologicalSuitIntegration.isIntegratedArmorPiece(player, piece)) return proposed;
      int limit = piece.getMaxDamage() - 1;
      return proposed < 0 ? limit : Math.min(proposed, limit);
   }
}
