package io.github.painworks.crysismod.mixin.nanosuit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPowerMelee;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.tacz.guns.item.ModernKineticGunItem", remap = false)
public abstract class TaczPowerMeleeMixin {
   @WrapOperation(method = "doPerLivingHurt", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
   private static boolean crysismod$powerGunMelee(LivingEntity target, DamageSource source, float amount, Operation<Boolean> original) {
      return NanosuitPowerMelee.hit(target, source, amount, true, original);
   }
}
