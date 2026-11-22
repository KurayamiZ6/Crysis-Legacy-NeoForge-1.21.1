package io.github.painworks.crysismod.mixin.client.nanosuit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitCloakRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.model.BedrockAttachmentModel", remap = false)
public abstract class CloakTaczScopeMixin {
   @WrapOperation(method = {"renderBoth", "renderSight", "renderScope"}, at = @At(value = "INVOKE", target = "Lcom/tacz/guns/compat/ar/ARCompat;shouldAccelerate()Z"), remap = false)
   private boolean crysismod$keepScopeHousingInCloakPass(Operation<Boolean> original) {
      return !NanosuitCloakRender.isDrawing() && original.call();
   }

   @Shadow protected List<List<BedrockPart>> divisionNodePaths;
   @Shadow protected List<Boolean> isScopeOcular;

   @Unique private static final String PARAMETERS = "(Lcom/mojang/blaze3d/vertex/PoseStack;"
      + "Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;II";
   @Unique private static final String BUFFER = "Lnet/minecraft/client/renderer/MultiBufferSource;";

   // Lens and stencil passes draw directly into the active scene. Keep them out of the cloak mask.
   @Inject(method = "renderOcularStencil", at = @At("HEAD"), cancellable = true, remap = false)
   private void crysismod$skipOcularStencil(CallbackInfo callback) {
      if (NanosuitCloakRender.isDrawing()) callback.cancel();
   }

   @Inject(method = "renderDivisionOnly" + PARAMETERS + BUFFER + ")V", at = @At("HEAD"), cancellable = true, remap = false)
   private void crysismod$captureSightReticle(PoseStack pose, ItemDisplayContext display, RenderType type,
      int light, int overlay, MultiBufferSource source, CallbackInfo callback) {
      if (!NanosuitCloakRender.isDrawing()) return;
      callback.cancel();
      crysismod$captureReticle(pose, display, type, light, overlay, false);
   }

   @Inject(method = "renderOcularAndDivision" + PARAMETERS + "Z" + BUFFER + ")V", at = @At("HEAD"), cancellable = true, remap = false)
   private void crysismod$captureScopeReticle(PoseStack pose, ItemDisplayContext display, RenderType type,
      int light, int overlay, boolean scopeOnly, MultiBufferSource source, CallbackInfo callback) {
      if (!NanosuitCloakRender.isDrawing()) return;
      callback.cancel();
      crysismod$captureReticle(pose, display, type, light, overlay, scopeOnly);
   }

   @Inject(method = "renderDivisionOnly" + PARAMETERS + ")V",
      at = @At("HEAD"), cancellable = true, remap = false)
   private void crysismod$captureLegacySightReticle(PoseStack pose, ItemDisplayContext display, RenderType type,
      int light, int overlay, CallbackInfo callback) {
      crysismod$captureSightReticle(pose, display, type, light, overlay, null, callback);
   }

   @Inject(method = "renderOcularAndDivision" + PARAMETERS + "Z)V",
      at = @At("HEAD"), cancellable = true, remap = false)
   private void crysismod$captureLegacyScopeReticle(PoseStack pose, ItemDisplayContext display, RenderType type,
      int light, int overlay, boolean scopeOnly, CallbackInfo callback) {
      crysismod$captureScopeReticle(pose, display, type, light, overlay, scopeOnly, null, callback);
   }

   @Unique
   private void crysismod$captureReticle(PoseStack pose, ItemDisplayContext display, RenderType type,
      int light, int overlay, boolean scopeOnly) {
      if (!display.firstPerson() || divisionNodePaths.isEmpty()) return;
      var client = Minecraft.getInstance();
      if (client.player == null) return;
      var gun = IClientPlayerGunOperator.fromLocalPlayer(client.player);
      float partialTick = client.getTimer().getGameTimeDeltaPartialTick(false);
      if (!gun.isAim() || gun.getClientAimingProgress(partialTick) < .9f) return;
      NanosuitCloakRender.captureReticle(buffers -> {
         for (int index = 0; index < divisionNodePaths.size(); index++) {
            if (scopeOnly && (index >= isScopeOcular.size() || !isScopeOcular.get(index))) continue;
            crysismod$drawDivision(pose, display, type, light, overlay, divisionNodePaths.get(index), buffers);
         }
      });
   }
   @Unique
   private static void crysismod$drawDivision(PoseStack pose, ItemDisplayContext display, RenderType type,
      int light, int overlay, List<BedrockPart> path, MultiBufferSource buffers) {
      BedrockPart division = path.getLast();
      boolean visible = division.visible;
      pose.pushPose();
      try {
         for (int i = 0; i < path.size() - 1; i++) path.get(i).translateAndRotateAndScale(pose);
         division.visible = true;
         division.render(pose, display, buffers.getBuffer(type), light, overlay);
      } finally {
         division.visible = visible;
         pose.popPose();
      }
   }
}
