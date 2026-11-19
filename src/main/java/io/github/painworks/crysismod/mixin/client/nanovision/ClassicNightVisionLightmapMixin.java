package io.github.painworks.crysismod.mixin.client.nanovision;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.painworks.crysismod.client.nanovision.ClassicNightVisionClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Enable vanilla's light normalization for N1, without adding or modifying a status effect. */
@Mixin(value=LightTexture.class,priority=1100)
public abstract class ClassicNightVisionLightmapMixin {
   @Shadow private boolean updateLightTexture;
   @Unique private float crysis$lastNightStrength;
   @Inject(method="updateLightTexture",at=@At("HEAD"))
   private void crysis$refreshChangedStrength(float partial,CallbackInfo ci){
      float strength=ClassicNightVisionClient.lightIntensity();
      if(Math.abs(strength-crysis$lastNightStrength)>.0001f)updateLightTexture=true;
      crysis$lastNightStrength=strength;
   }
   @WrapOperation(method="updateLightTexture",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z"))
   private boolean crysis$useNightLighting(LocalPlayer player,Holder<MobEffect> effect,Operation<Boolean> original){
      return original.call(player,effect) || effect==MobEffects.NIGHT_VISION && ClassicNightVisionClient.lightIntensity()>0;
   }
}
