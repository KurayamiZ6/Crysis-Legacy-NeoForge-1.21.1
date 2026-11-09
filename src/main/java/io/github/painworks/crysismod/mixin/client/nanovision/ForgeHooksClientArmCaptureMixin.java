package io.github.painworks.crysismod.mixin.client.nanovision;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientHooks.class, remap = false)
public abstract class ForgeHooksClientArmCaptureMixin {
   @Inject(method = "renderSpecificFirstPersonArm", at = @At("HEAD"), remap = false, require = 0)
   private static void crysismod$beforeForgeArm(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      AbstractClientPlayer player,
      HumanoidArm arm,
      CallbackInfoReturnable<Boolean> callback
   ) {
      ThermalFirstPersonItemCapture.pushArmSuppression();
   }

   @Inject(method = "renderSpecificFirstPersonArm", at = @At("RETURN"), remap = false, require = 0)
   private static void crysismod$afterForgeArm(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      AbstractClientPlayer player,
      HumanoidArm arm,
      CallbackInfoReturnable<Boolean> callback
   ) {
      ThermalFirstPersonItemCapture.popArmSuppression();
   }
}
