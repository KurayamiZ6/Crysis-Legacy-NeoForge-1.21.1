package io.github.painworks.crysismod.mixin.client.nanovision;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import io.github.painworks.crysismod.client.nanovision.debug.NanovisionPerformanceProfiler;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemGlowCapture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemInHandRenderer.class, priority = 800)
public abstract class ItemInHandRendererItemMaskMixin {
   @Unique
   private long crysismod$firstPersonHandCpuStarted;
   @Unique
   private int crysismod$firstPersonHandGpuQuery;

   @Inject(method = "renderHandsWithItems", at = @At("HEAD"), require = 0)
   private void crysismod$beginFirstPersonProfile(
      float partialTick, PoseStack poseStack, BufferSource bufferSource, LocalPlayer player, int packedLight, CallbackInfo callback
   ) {
      Minecraft minecraft = Minecraft.getInstance();
      int width = minecraft.getMainRenderTarget() == null ? 0 : minecraft.getMainRenderTarget().width;
      int height = minecraft.getMainRenderTarget() == null ? 0 : minecraft.getMainRenderTarget().height;
      NanovisionPerformanceProfiler.beginFirstPersonFrame(width, height, player != null && NanovisionState.isActive(player));
      this.crysismod$firstPersonHandCpuStarted = NanovisionPerformanceProfiler.beginCpu();
      this.crysismod$firstPersonHandGpuQuery = NanovisionPerformanceProfiler.beginGpu(NanovisionPerformanceProfiler.Section.FIRST_PERSON_HAND_RENDER_GPU);
   }

   @ModifyVariable(method = "renderHandsWithItems", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
   private BufferSource crysismod$beginAndWrapItemMask(BufferSource original) {
      ThermalFirstPersonItemGlowCapture.capturePreHandDepth();
      ThermalFirstPersonItemCapture.beginHandPass(Minecraft.getInstance());
      return ThermalFirstPersonItemCapture.wrapSource(original);
   }

   @Inject(method = "renderHandsWithItems", at = @At("RETURN"), require = 0)
   private void crysismod$finishItemMaskScope(
      float partialTick, PoseStack poseStack, BufferSource bufferSource, LocalPlayer player, int packedLight, CallbackInfo callback
   ) {
      ThermalFirstPersonItemCapture.endHandPass();
      NanovisionPerformanceProfiler.endGpu(NanovisionPerformanceProfiler.Section.FIRST_PERSON_HAND_RENDER_GPU, this.crysismod$firstPersonHandGpuQuery);
      NanovisionPerformanceProfiler.endCpu(NanovisionPerformanceProfiler.Section.FIRST_PERSON_HAND_RENDER, this.crysismod$firstPersonHandCpuStarted);
   }
}
