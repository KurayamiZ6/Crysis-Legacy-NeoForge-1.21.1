package io.github.painworks.crysismod.mixin.client.nanosuit;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitCloakRender;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerRenderer.class)
public abstract class CloakPlayerRenderMixin {
   @WrapMethod(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
   private void crysismod$renderCloakedPlayer(AbstractClientPlayer player, float yaw, float partialTick, PoseStack pose,
                                              MultiBufferSource buffers, int light, Operation<Void> original) {
      if (NanosuitCloakRender.isActive(player) && !NanovisionState.isActive()) {
         NanosuitCloakRender.draw(false, surface -> original.call(player, yaw, partialTick, pose, surface, light));
      } else {
         original.call(player, yaw, partialTick, pose, buffers, light);
      }
   }
}
