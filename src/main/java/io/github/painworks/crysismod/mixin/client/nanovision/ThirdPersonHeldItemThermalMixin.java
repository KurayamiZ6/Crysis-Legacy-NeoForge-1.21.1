package io.github.painworks.crysismod.mixin.client.nanovision;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalThirdPersonHeldItemCapture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemInHandLayer.class, priority = 1100)
public abstract class ThirdPersonHeldItemThermalMixin {
   @Inject(method = "render", at = @At("HEAD"), require = 0)
   private void crysismod$beginThirdPersonHeldItemCapture(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      LivingEntity livingEntity,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch,
      CallbackInfo callback
   ) {
      ThermalThirdPersonHeldItemCapture.beginLayer(livingEntity);
   }

   @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
   private MultiBufferSource crysismod$wrapThirdPersonHeldItemSource(MultiBufferSource original) {
      return ThermalThirdPersonHeldItemCapture.wrap(original);
   }

   @Inject(method = "render", at = @At("RETURN"), require = 0)
   private void crysismod$endThirdPersonHeldItemCapture(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      LivingEntity livingEntity,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch,
      CallbackInfo callback
   ) {
      ThermalThirdPersonHeldItemCapture.endLayer();
   }
}
