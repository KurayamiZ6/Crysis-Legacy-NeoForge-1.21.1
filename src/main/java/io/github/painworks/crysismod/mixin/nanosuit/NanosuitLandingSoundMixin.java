package io.github.painworks.crysismod.mixin.nanosuit;

import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.registry.ModSounds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntity.class)
public abstract class NanosuitLandingSoundMixin {
   @ModifyArgs(method = "playBlockFallSound", at = @At(value = "INVOKE",
         target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
   private void crysismod$playSuitLanding(Args args) {
      if ((Object)this instanceof Player player && NanosuitArmorUtil.hasFullSet(player)) {
         args.set(0, ModSounds.NANOSUIT_LANDING.get());
         args.set(2, 1.0F);
      }
   }
}
