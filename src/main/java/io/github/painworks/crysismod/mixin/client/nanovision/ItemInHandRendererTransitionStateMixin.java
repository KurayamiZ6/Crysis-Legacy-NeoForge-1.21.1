package io.github.painworks.crysismod.mixin.client.nanovision;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonHandTransitionState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemInHandRenderer.class, priority = 1100)
public abstract class ItemInHandRendererTransitionStateMixin {
   @Shadow
   private ItemStack mainHandItem;
   @Shadow
   private ItemStack offHandItem;

   @Inject(method = "renderHandsWithItems", at = @At("HEAD"), require = 1)
   private void crysismod$beginAnimatedItemSnapshot(
      float partialTick, PoseStack poseStack, BufferSource bufferSource, LocalPlayer player, int packedLight, CallbackInfo callback
   ) {
      ThermalFirstPersonHandTransitionState.beginHandRender(this.mainHandItem, this.offHandItem);
   }

   @Inject(method = "renderHandsWithItems", at = @At("RETURN"), require = 1)
   private void crysismod$clearAnimatedItemSnapshot(
      float partialTick, PoseStack poseStack, BufferSource bufferSource, LocalPlayer player, int packedLight, CallbackInfo callback
   ) {
      ThermalFirstPersonHandTransitionState.endHandRender();
   }
}
