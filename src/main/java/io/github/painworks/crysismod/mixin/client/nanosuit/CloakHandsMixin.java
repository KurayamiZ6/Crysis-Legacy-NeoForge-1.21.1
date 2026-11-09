package io.github.painworks.crysismod.mixin.client.nanosuit;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitCloakRender;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public abstract class CloakHandsMixin {
   @WrapMethod(method = "renderHandsWithItems")
   private void crysismod$renderCloakedHands(float partialTick, PoseStack pose, BufferSource buffers, LocalPlayer player,
                                             int light, Operation<Void> original) {
      if (NanosuitCloakRender.isActive(player) && !NanovisionState.isActive(player)) {
         NanosuitCloakRender.draw(true, surface -> original.call(partialTick, pose, surface, player, light));
      } else {
         original.call(partialTick, pose, buffers, player, light);
      }
   }

   @WrapOperation(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;isInvisible()Z"))
   private boolean crysismod$keepArmGeometry(AbstractClientPlayer player, Operation<Boolean> original) {
      return !NanosuitCloakRender.keepsArmGeometry(player) && original.call(player);
   }

   @WrapOperation(method = {"renderOneHandedMap", "renderTwoHandedMap"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInvisible()Z"))
   private boolean crysismod$keepMapHandGeometry(LocalPlayer player, Operation<Boolean> original) {
      return !NanosuitCloakRender.keepsArmGeometry(player) && original.call(player);
   }
}
