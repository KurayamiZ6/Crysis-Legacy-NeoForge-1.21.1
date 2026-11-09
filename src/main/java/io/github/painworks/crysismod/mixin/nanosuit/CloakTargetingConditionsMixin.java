package io.github.painworks.crysismod.mixin.nanosuit;

import io.github.painworks.crysismod.nanosuit.handler.NanosuitCloak;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetingConditions.class)
public abstract class CloakTargetingConditionsMixin {
   @Shadow @Final private boolean isCombat;

   @Inject(method = "test", at = @At("HEAD"), cancellable = true, require = 1)
   private void crysismod$filterCloakedCandidates(LivingEntity observer, LivingEntity candidate, CallbackInfoReturnable<Boolean> callback) {
      if (this.isCombat && observer instanceof Mob && NanosuitCloak.hidesFrom(observer, candidate)) {
         callback.setReturnValue(false);
      }
   }

   @ModifyVariable(method = "test", at = @At("STORE"), ordinal = 0, require = 1)
   private double crysismod$useCloakDetectionRange(double visibility, LivingEntity observer, LivingEntity candidate) {
      if (this.isCombat && observer instanceof Mob && candidate instanceof ServerPlayer player && NanosuitCloak.isActive(player)) {
         return 1.0;
      }
      return visibility;
   }
}
