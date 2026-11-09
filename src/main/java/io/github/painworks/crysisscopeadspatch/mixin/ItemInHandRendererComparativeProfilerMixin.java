package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.painworks.crysisscopeadspatch.debug.NanovisionComparativeProfiler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemInHandRenderer.class, priority = 800)
public abstract class ItemInHandRendererComparativeProfilerMixin {
   private NanovisionComparativeProfiler.Token crysisscopefix$handsProfile;

   @Inject(method = "renderHandsWithItems", at = @At("HEAD"), require = 1)
   private void crysisscopefix$beginHandsProfile(
      float partialTick, PoseStack poseStack, BufferSource bufferSource, LocalPlayer player, int packedLight, CallbackInfo ci
   ) {
      this.crysisscopefix$handsProfile = NanovisionComparativeProfiler.beginSection(NanovisionComparativeProfiler.Section.HANDS_TOTAL);
   }

   @Inject(method = "renderHandsWithItems", at = @At("RETURN"), require = 1)
   private void crysisscopefix$endHandsProfile(
      float partialTick, PoseStack poseStack, BufferSource bufferSource, LocalPlayer player, int packedLight, CallbackInfo ci
   ) {
      NanovisionComparativeProfiler.endSection(this.crysisscopefix$handsProfile, true);
      this.crysisscopefix$handsProfile = null;
   }
}
