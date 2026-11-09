package io.github.painworks.crysismod.mixin.client.nanovision;

import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.model.BedrockAttachmentModel", remap = false, priority = 900)
public abstract class TaczScopeOpticsThermalExclusionMixin {
   private static final String OCULAR_STENCIL = "renderOcularStencil(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIZLnet/minecraft/client/renderer/MultiBufferSource;)V";
   private static final String OCULAR_AND_DIVISION = "renderOcularAndDivision(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIZLnet/minecraft/client/renderer/MultiBufferSource;)V";
   private static final String DIVISION_ONLY = "renderDivisionOnly(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V";
   private static final String SCOPE = "renderScope(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V";
   private static final String SIGHT = "renderSight(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V";
   private static final String BOTH = "renderBoth(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V";

   @Inject(
      method = {
            "renderScope(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderSight(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderBoth(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V"
      },
      at = @At("HEAD"),
      require = 0,
      remap = false
   )
   private void crysismod$excludeInheritedScopeModelFromDeferredCapture(CallbackInfo callback) {
      ThermalFirstPersonItemCapture.pushTaczScopeRender();
   }

   @Inject(
      method = {
            "renderScope(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderSight(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderBoth(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V"
      },
      at = @At("RETURN"),
      require = 0,
      remap = false
   )
   private void crysismod$restoreDeferredCaptureAfterScopeModel(CallbackInfo callback) {
      ThermalFirstPersonItemCapture.popTaczScopeRender();
   }

   @Inject(
      method = "renderOcularStencil(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIZLnet/minecraft/client/renderer/MultiBufferSource;)V",
      at = @At("HEAD"),
      require = 0,
      remap = false
   )
   private void crysismod$excludeOcularStencilFromDeferredCapture(CallbackInfo callback) {
      ThermalFirstPersonItemCapture.pushOpticsSuppression();
   }

   @Inject(
      method = "renderOcularStencil(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIZLnet/minecraft/client/renderer/MultiBufferSource;)V",
      at = @At("RETURN"),
      require = 0,
      remap = false
   )
   private void crysismod$restoreOcularStencilCapture(CallbackInfo callback) {
      ThermalFirstPersonItemCapture.popOpticsSuppression();
   }

   @Inject(
      method = {
            "renderOcularAndDivision(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIZLnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderDivisionOnly(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V"
      },
      at = @At("HEAD"),
      require = 0,
      remap = false
   )
   private void crysismod$excludeOpticalPass(CallbackInfo callback) {
      ThermalFirstPersonItemCapture.pushOpticsSuppression();
   }

   @Inject(
      method = {
            "renderOcularAndDivision(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIZLnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderDivisionOnly(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V"
      },
      at = @At("RETURN"),
      require = 0,
      remap = false
   )
   private void crysismod$restoreThermalCaptureAfterOpticalPass(CallbackInfo callback) {
      ThermalFirstPersonItemCapture.popOpticsSuppression();
   }
}
