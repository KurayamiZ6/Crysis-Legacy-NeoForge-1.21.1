package io.github.painworks.crysismod.mixin.nanosuit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPowerMelee;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class PowerMeleeKnockbackMixin {
   @WrapOperation(method = "hurt", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"), require = 1)
   private void crysismod$deferPowerMeleeImpulse(LivingEntity target, double strength, double x, double z,
         Operation<Void> original, DamageSource source, float amount) {
      if (!NanosuitPowerMelee.deferHurtKnockback(target, source, strength, x, z)) {
         original.call(target, strength, x, z);
      }
   }
}
