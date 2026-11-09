package io.github.painworks.crysismod.mixin.client.nanovision;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerRenderer.class, priority = 800)
public abstract class PlayerRendererHeldItemCaptureMixin {
   @Inject(method = {"renderRightHand", "renderLeftHand"}, at = @At("HEAD"), require = 0)
   private void crysismod$beforeDirectArm(
      PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player, CallbackInfo callback
   ) {
      ThermalFirstPersonItemCapture.pushArmSuppression();
   }

   @Inject(method = {"renderRightHand", "renderLeftHand"}, at = @At("RETURN"), require = 0)
   private void crysismod$afterDirectArm(
      PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player, CallbackInfo callback
   ) {
      ThermalFirstPersonItemCapture.popArmSuppression();
   }
}
