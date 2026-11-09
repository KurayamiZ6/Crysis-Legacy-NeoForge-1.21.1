package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture;
import io.github.painworks.crysisscopeadspatch.render.TaczAdsStencilMask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.model.BedrockAttachmentModel", remap = false, priority = 1200)
public abstract class TaczOpticalStencilCompletionMixin {
   private static final String ARGUMENTS = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;II";

   @Inject(
      method = "renderOcularAndDivision(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIZLnet/minecraft/client/renderer/MultiBufferSource;)V",
      at = @At("RETURN"),
      require = 0
   )
   private void crysisscopeadspatch$markScopeOptics(
      PoseStack poseStack, ItemDisplayContext context, RenderType renderType, int light, int overlay, boolean selective, MultiBufferSource source, CallbackInfo ci
   ) {
      crysisscopeadspatch$mark(context);
   }

   private static void crysisscopeadspatch$mark(ItemDisplayContext context) {
      Minecraft minecraft = Minecraft.getInstance();
      if (context.firstPerson()
         && minecraft.player != null
         && IClientPlayerGunOperator.fromLocalPlayer(minecraft.player).getClientAimingProgress(minecraft.getTimer().getGameTimeDeltaPartialTick(false)) > 0.01F
         && NanoGeoShaderpackCompatibility.isShaderPackActive()
         && ThermalFirstPersonItemCapture.shouldPreserveArmMasks()) {
         TaczAdsStencilMask.markOpticalStencilReady();
      }
   }
}
