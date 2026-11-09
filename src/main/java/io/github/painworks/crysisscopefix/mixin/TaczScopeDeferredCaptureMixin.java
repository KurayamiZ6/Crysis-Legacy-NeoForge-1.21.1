package io.github.painworks.crysisscopefix.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture;
import io.github.painworks.crysisscopefix.render.ScopeDeferredCapture;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.model.BedrockAttachmentModel", remap = false, priority = 1100)
public abstract class TaczScopeDeferredCaptureMixin {
   private static final String TEMP_PART = "renderTempPart(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILjava/util/List;Lnet/minecraft/client/renderer/MultiBufferSource;)V";
   private static final String SCOPE = "renderScope(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V";
   private static final String SIGHT = "renderSight(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V";
   private static final String BOTH = "renderBoth(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V";
   private static final String SCOPE_ACCELERATED = "renderScopeAccelerated(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V";
   private static final String SIGHT_ACCELERATED = "renderSightAccelerated(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V";
   private static final String BOTH_ACCELERATED = "renderBothAccelerated(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V";

   @Invoker(value = "renderTempPart", remap = false)
   protected abstract void crysisscopefix$invokeRenderTempPart(PoseStack var1, ItemDisplayContext var2, RenderType var3, int var4, int var5, List<?> var6, MultiBufferSource source);

   @Shadow(remap = false)
   public abstract boolean isSight();

   @Shadow(remap = false)
   public abstract boolean isScope();

   @Redirect(
      method = "renderOcularStencil",
      at = @At(
         value = "INVOKE",
         target = "Lcom/tacz/guns/client/model/BedrockAttachmentModel;renderTempPart(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILjava/util/List;Lnet/minecraft/client/renderer/MultiBufferSource;)V"
      ),
      require = 0,
      remap = false
   )
   private void crysisscopefix$captureExactOcularAperture(
      @Coerce Object ignoredReceiver,
      PoseStack poseStack,
      ItemDisplayContext displayContext,
      RenderType renderType,
      int packedLight,
      int packedOverlay,
      List<?> nodePath, MultiBufferSource source
   ) {
      this.crysisscopefix$invokeRenderTempPart(poseStack, displayContext, renderType, packedLight, packedOverlay, nodePath, source);
      if (displayContext.firstPerson() && isSight() && !isScope()
         && NanoGeoShaderpackCompatibility.isShaderPackActive() && ThermalFirstPersonItemCapture.shouldPreserveArmMasks()) {
         // TaCZ's sight-only path uses a regular ocular stencil, not the magnified ADS bit.
         ScopeDeferredCapture.captureSightAperture(poseStack, displayContext, renderType, packedLight, packedOverlay, nodePath);
      }
   }

   @Redirect(
      method = {
            "renderScope(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderBoth(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderScopeAccelerated(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderBothAccelerated(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V"
      },
      at = @At(
         value = "INVOKE",
         target = "Lcom/tacz/guns/client/model/BedrockAttachmentModel;renderTempPart(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILjava/util/List;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
         ordinal = 0
      ),
      require = 0,
      remap = false
   )
   private void crysisscopefix$renderScopeRing(
      @Coerce Object ignoredReceiver,
      PoseStack poseStack,
      ItemDisplayContext displayContext,
      RenderType renderType,
      int packedLight,
      int packedOverlay,
      List<?> nodePath, MultiBufferSource source
   ) {
      this.crysisscopefix$renderThermalPart(poseStack, displayContext, renderType, packedLight, packedOverlay, nodePath, source);
   }

   @Redirect(
      method = {
            "renderScope(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderBoth(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderScopeAccelerated(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderBothAccelerated(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V"
      },
      at = @At(
         value = "INVOKE",
         target = "Lcom/tacz/guns/client/model/BedrockAttachmentModel;renderTempPart(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILjava/util/List;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
         ordinal = 1
      ),
      require = 0,
      remap = false
   )
   private void crysisscopefix$renderScopeBody(
      @Coerce Object ignoredReceiver,
      PoseStack poseStack,
      ItemDisplayContext displayContext,
      RenderType renderType,
      int packedLight,
      int packedOverlay,
      List<?> nodePath, MultiBufferSource source
   ) {
      this.crysisscopefix$renderThermalPart(poseStack, displayContext, renderType, packedLight, packedOverlay, nodePath, source);
   }

   @Redirect(
      method = {
            "renderSight(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderSightAccelerated(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V"
      },
      at = @At(
         value = "INVOKE",
         target = "Lcom/tacz/guns/client/model/BedrockAttachmentModel;renderTempPart(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILjava/util/List;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
         ordinal = 0
      ),
      require = 0,
      remap = false
   )
   private void crysisscopefix$renderSightBody(
      @Coerce Object ignoredReceiver,
      PoseStack poseStack,
      ItemDisplayContext displayContext,
      RenderType renderType,
      int packedLight,
      int packedOverlay,
      List<?> nodePath, MultiBufferSource source
   ) {
      this.crysisscopefix$renderThermalPart(poseStack, displayContext, renderType, packedLight, packedOverlay, nodePath, source);
   }

   private void crysisscopefix$renderThermalPart(
      PoseStack poseStack, ItemDisplayContext displayContext, RenderType renderType, int packedLight, int packedOverlay, List<?> nodePath, MultiBufferSource source
   ) {
      ThermalFirstPersonItemCapture.pushScopeBodyStencilCapture();

      try {
         this.crysisscopefix$invokeRenderTempPart(poseStack, displayContext, renderType, packedLight, packedOverlay, nodePath, source);
         RenderType accepted = ThermalFirstPersonItemCapture.prepareTaczScopeBodyThermalPass(renderType);
         if (accepted == null) {
            return;
         }

         if (NanoGeoShaderpackCompatibility.isShaderPackActive()) {
            ScopeDeferredCapture.armOrdinaryModelCapture(accepted);
            ScopeDeferredCapture.captureThermal(poseStack, displayContext, accepted, packedLight, packedOverlay, nodePath);
         } else {
            this.crysisscopefix$invokeRenderTempPart(poseStack, displayContext, accepted, packedLight, packedOverlay, nodePath, source);
         }
      } finally {
         ThermalFirstPersonItemCapture.popScopeBodyStencilCapture();
      }
   }

   @Inject(
      method = {
            "renderScope(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderSight(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderBoth(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderScopeAccelerated(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderSightAccelerated(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V",
            "renderBothAccelerated(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/MultiBufferSource;)V"
      },
      at = @At("RETURN"),
      require = 0,
      remap = false
   )
   private void crysisscopefix$endScopeModelCapture(CallbackInfo ci) {
      ScopeDeferredCapture.endScopeModelCapture();
   }
}
