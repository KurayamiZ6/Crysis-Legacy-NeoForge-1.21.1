package io.github.painworks.crysisscopeadspatch.mixin;

import io.github.painworks.crysisscopeadspatch.render.OculusPreHandDepth;
import java.util.function.IntSupplier;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.NanovisionPostProcessor", remap = false, priority = 1200)
public abstract class NanovisionSceneDepthMixin {
   @Redirect(
      method = {"createClassicPasses", "createCinematicPasses"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/renderer/PostPass;addAuxAsset(Ljava/lang/String;Ljava/util/function/IntSupplier;II)V",
         remap = true
      ),
      require = 1
   )
   private void crysisscopeadspatch$usePreHandDepthForEntityExtraction(PostPass pass, String name, IntSupplier originalTexture, int width, int height) {
      IntSupplier selected = originalTexture;
      if ("SceneDepthSampler".equals(name)) {
         selected = () -> OculusPreHandDepth.textureOr(originalTexture.getAsInt());
      }

      pass.addAuxAsset(name, selected, width, height);
   }
}
