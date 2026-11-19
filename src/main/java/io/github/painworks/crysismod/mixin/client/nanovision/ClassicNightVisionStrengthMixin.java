package io.github.painworks.crysismod.mixin.client.nanovision;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.painworks.crysismod.client.nanovision.ClassicNightVisionClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Also supplies Iris' existing nightVision uniform through the vanilla query it already uses. */
@Mixin(value=GameRenderer.class,priority=1100)
public abstract class ClassicNightVisionStrengthMixin {
   @Inject(method="getNightVisionScale",at=@At("HEAD"),cancellable=true)
   private static void crysis$withoutPotion(LivingEntity entity,float partial,CallbackInfoReturnable<Float> cir){
      if(entity!=Minecraft.getInstance().player)return;
      float strength=ClassicNightVisionClient.lightIntensity();
      // Vanilla dereferences the effect; Iris has its own early zero return for an absent effect.
      if(strength>0 && entity.getEffect(MobEffects.NIGHT_VISION)==null)cir.setReturnValue(strength);
   }
   @ModifyReturnValue(method="getNightVisionScale",at=@At("RETURN"))
   private static float crysis$preserveRealPotion(float original,LivingEntity entity,float partial){
      return entity==Minecraft.getInstance().player?Math.max(original,ClassicNightVisionClient.lightIntensity()):original;
   }
}
