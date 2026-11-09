package io.github.painworks.crysismod.mixin.nanosuit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPowerMelee;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerPowerMeleeMixin {
   @WrapOperation(method = "attack", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), require = 1)
   private boolean crysismod$powerPunch(Entity target, DamageSource source, float amount, Operation<Boolean> original) {
      Player player = (Player)(Object)this;
      if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty() || player.isAutoSpinAttack()) {
         return original.call(target, source, amount);
      }
      return NanosuitPowerMelee.hit(target, source, amount, false, original);
   }
}
