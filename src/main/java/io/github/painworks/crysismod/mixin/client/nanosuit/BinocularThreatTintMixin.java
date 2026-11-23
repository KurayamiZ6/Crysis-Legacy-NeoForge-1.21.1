package io.github.painworks.crysismod.mixin.client.nanosuit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.painworks.crysismod.nanovisor.client.BinocularThreatTint;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
abstract class BinocularThreatTintMixin {
   @WrapOperation(method = "render", at = @At(value = "INVOKE", target =
      "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
   private <T extends Entity> void crysismod$captureThreatTint(EntityRenderer<T> renderer, T entity,
      float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, Operation<Void> original) {
      original.call(renderer, entity, yaw, partialTick, pose, BinocularThreatTint.capture(entity, buffers), light);
   }
}
