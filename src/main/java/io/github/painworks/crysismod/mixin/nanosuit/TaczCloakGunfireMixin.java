package io.github.painworks.crysismod.mixin.nanosuit;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitCloak;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.tacz.guns.item.ModernKineticGunScriptAPI", remap = false)
public abstract class TaczCloakGunfireMixin {
   @Unique
   private static final ResourceLocation CRYSISMOD_PREDATOR_BOW = ResourceLocation.fromNamespaceAndPath("sfms", "tb23");

   @Shadow
   private ResourceLocation gunId;

   @Shadow
   public abstract LivingEntity getShooter();

   @ModifyArg(method = "lambda$shootOnce$2", index = 0, require = 1, at = @At(value = "INVOKE",
      target = "Lcom/tacz/guns/entity/EntityKineticBullet;setShotDamageMultiplier(F)V"))
   private float crysismod$boostPredatorProjectile(float original) {
      if (CRYSISMOD_PREDATOR_BOW.equals(this.gunId) && this.getShooter() instanceof ServerPlayer player) {
         NanosuitProfile profile = NanosuitArmorUtil.getEquippedProfile(player);
         if (profile != null) {
            return original * profile.combat().predatorDamageMultiplier();
         }
      }
      return original;
   }

   // This server-only point follows projectile spawning and all cancellation checks, once per shot.
   @Inject(method = "lambda$shootOnce$2", require = 1, at = @At(value = "NEW",
      target = "com/tacz/guns/api/event/common/GunFirePostEvent"))
   private void crysismod$chargeConfirmedShot(CallbackInfoReturnable<Boolean> callback,
         @Local(argsOnly = true, ordinal = 1) boolean silenced) {
      if (!CRYSISMOD_PREDATOR_BOW.equals(this.gunId) && this.getShooter() instanceof ServerPlayer player) {
         NanosuitCloak.chargeConfirmedShot(player, silenced);
      }
   }
}
