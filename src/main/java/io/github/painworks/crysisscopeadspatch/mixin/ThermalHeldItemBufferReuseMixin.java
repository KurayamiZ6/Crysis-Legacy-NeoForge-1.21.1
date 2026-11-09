package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import java.util.SequencedMap;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture", remap = false, priority = 1150)
public abstract class ThermalHeldItemBufferReuseMixin {
   @Shadow
   private static ByteBufferBuilder fallbackBuffer;

   @Redirect(
      method = "resetStorage()V",
      at = @At(value = "INVOKE", target = "Lio/github/painworks/crysismod/client/nanovision/thermal/ThermalFirstPersonItemCapture;discard(Ljava/util/SequencedMap;Lcom/mojang/blaze3d/vertex/ByteBufferBuilder;)V"),
      require = 1
   )
   private static void crysisscopefix$keepStorage(SequencedMap<RenderType, ByteBufferBuilder> buffers, ByteBufferBuilder fallback) {
      for (ByteBufferBuilder buffer : buffers.values()) {
         crysisscopefix$discardWrites(buffer);
      }
      crysisscopefix$discardWrites(fallback);
   }

   private static void crysisscopefix$discardWrites(ByteBufferBuilder storage) {
      // Include any writes from a capture that was interrupted before building its mesh.
      ByteBufferBuilder.Result abandoned = storage.build();
      if (abandoned != null) {
         abandoned.close();
      }
      storage.discard();
   }

   @Redirect(method = "resetStorage()V", at = @At(value = "NEW", target = "(I)Lcom/mojang/blaze3d/vertex/ByteBufferBuilder;"), require = 1)
   private static ByteBufferBuilder crysisscopefix$reuseFallbackBuilder(int initialCapacity) {
      return fallbackBuffer;
   }
}
