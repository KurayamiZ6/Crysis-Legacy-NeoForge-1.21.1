package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.painworks.crysisscopeadspatch.render.OptimizedTaczCaptureContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.renderer.item.GunItemRendererWrapper", remap = false, priority = 790)
public abstract class TaczCaptureOptimizationContextMixin {
   @Inject(method = "renderFirstPerson", at = @At("HEAD"), require = 1)
   private void crysisscopefix$enterCompactCapture(
      LocalPlayer player,
      ItemStack stack,
      ItemDisplayContext context,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int light,
      float partialTick,
      CallbackInfo ci
   ) {
      OptimizedTaczCaptureContext.enter();
   }

   @Inject(method = "renderFirstPerson", at = @At("RETURN"), require = 1)
   private void crysisscopefix$exitCompactCapture(
      LocalPlayer player,
      ItemStack stack,
      ItemDisplayContext context,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int light,
      float partialTick,
      CallbackInfo ci
   ) {
      OptimizedTaczCaptureContext.exit();
   }
}
