package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.painworks.crysisscopeadspatch.debug.NanovisionComparativeProfiler;
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
@Mixin(targets = "com.tacz.guns.client.renderer.item.GunItemRendererWrapper", remap = false, priority = 800)
public abstract class TaczGunComparativeProfilerMixin {
   private NanovisionComparativeProfiler.Token crysisscopefix$taczProfile;

   @Inject(method = "renderFirstPerson", at = @At("HEAD"), require = 1)
   private void crysisscopefix$beginTaczProfile(
      LocalPlayer player,
      ItemStack stack,
      ItemDisplayContext context,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int light,
      float partialTick,
      CallbackInfo ci
   ) {
      this.crysisscopefix$taczProfile = NanovisionComparativeProfiler.beginSection(NanovisionComparativeProfiler.Section.TACZ_GUN);
   }

   @Inject(method = "renderFirstPerson", at = @At("RETURN"), require = 1)
   private void crysisscopefix$endTaczProfile(
      LocalPlayer player,
      ItemStack stack,
      ItemDisplayContext context,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int light,
      float partialTick,
      CallbackInfo ci
   ) {
      NanovisionComparativeProfiler.endSection(this.crysisscopefix$taczProfile, true);
      this.crysisscopefix$taczProfile = null;
   }
}
