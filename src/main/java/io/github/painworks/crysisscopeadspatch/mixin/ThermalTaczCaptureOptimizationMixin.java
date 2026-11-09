package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.painworks.crysisscopeadspatch.config.LegacySpectrumConfig;
import io.github.painworks.crysisscopeadspatch.render.CompactEntityVertexConsumer;
import io.github.painworks.crysisscopeadspatch.render.OptimizedTaczCaptureContext;
import io.github.painworks.crysisscopeadspatch.render.OptimizedTaczRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture", remap = false, priority = 1100)
public abstract class ThermalTaczCaptureOptimizationMixin {
   @Redirect(
      method = "wrap(Lio/github/painworks/crysismod/client/nanovision/thermal/CaptureChannel;Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/VertexConsumer;)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
      at = @At(
         value = "INVOKE",
         target = "Lio/github/painworks/crysismod/client/nanovision/thermal/ThermalFirstPersonItemRenderTypes;capture(Lnet/minecraft/client/renderer/RenderType;)Lnet/minecraft/client/renderer/RenderType;"
      ),
      require = 1
   )
   private static RenderType crysisscopefix$selectCompactType(RenderType original) {
      RenderType legacy = ThermalItemRenderTypesInvoker.crysisscopefix$capture(original);
      return LegacySpectrumConfig.optimizedTaczCapture() && OptimizedTaczCaptureContext.isActive()
         ? OptimizedTaczRenderTypes.resolve(original, legacy)
         : legacy;
   }

   @Redirect(
      method = "wrap(Lio/github/painworks/crysismod/client/nanovision/thermal/CaptureChannel;Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/VertexConsumer;)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
      at = @At(
         value = "INVOKE",
         target = "Lio/github/painworks/crysismod/client/nanovision/thermal/ThermalDeferredBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
      ),
      require = 1
   )
   private static VertexConsumer crysisscopefix$adaptCompactConsumer(@Coerce Object source, RenderType type) {
      VertexConsumer consumer = ((BufferSource) source).getBuffer(type);
      return OptimizedTaczRenderTypes.isOptimized(type) ? new CompactEntityVertexConsumer(consumer) : consumer;
   }
}
