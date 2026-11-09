package io.github.painworks.crysismod.mixin.client.nanosuit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.renderer.item.StaticGunRenderResult;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitCloakRender;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.renderer.item.ExternalGunItemVboRenderer", remap = false)
public abstract class CloakTaczStaticGunMixin {
   @Inject(method = "tryRender", at = @At("HEAD"), cancellable = true, remap = false)
   private static void crysismod$keepStaticGunInCloakPass(ItemStack item, ItemDisplayContext context, boolean lod,
                                                        PoseStack pose, MultiBufferSource buffers, int light, int overlay,
                                                        CallbackInfoReturnable<StaticGunRenderResult> callback) {
      if (NanosuitCloakRender.isDrawing()) {
         callback.setReturnValue(StaticGunRenderResult.UNSUPPORTED);
      }
   }
}
