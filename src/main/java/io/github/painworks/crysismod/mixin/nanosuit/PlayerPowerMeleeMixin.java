package io.github.painworks.crysismod.mixin.nanosuit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPowerMelee;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerPowerMeleeMixin {
   @Inject(method = "attack", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/entity/Entity;isAttackable()Z"), require = 1)
   private void crysismod$beginClassicPunch(Entity target, CallbackInfo callback,
         @Share("classicPowered") LocalBooleanRef powered) {
      if ((Object)this instanceof ServerPlayer player && !player.isAutoSpinAttack()) {
         powered.set(NanosuitPowerMelee.beginClassicPunch(player));
      }
   }

   @WrapOperation(method = "attack", at = @At(value = "INVOKE",
      target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), require = 1)
   private boolean crysismod$powerPunch(Entity target, DamageSource source, float amount, Operation<Boolean> original,
         @Share("classicPowered") LocalBooleanRef powered) {
      Player player = (Player)(Object)this;
      if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty() || player.isAutoSpinAttack()) {
         return original.call(target, source, amount);
      }
      if (powered.get() || NanosuitArmorUtil.getEquippedProfile(player) == NanosuitProfile.NANOSUIT_1) {
         return NanosuitPowerMelee.hitClassicPunch(target, source, amount, powered.get(), original);
      }
      return NanosuitPowerMelee.hit(target, source, amount, false, original);
   }
}
