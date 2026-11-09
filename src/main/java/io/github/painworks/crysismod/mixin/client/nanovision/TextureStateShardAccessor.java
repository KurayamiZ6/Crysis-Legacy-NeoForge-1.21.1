package io.github.painworks.crysismod.mixin.client.nanovision;

import java.util.Optional;
import net.minecraft.client.renderer.RenderStateShard.EmptyTextureStateShard;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EmptyTextureStateShard.class)
public interface TextureStateShardAccessor {
   @Invoker("cutoutTexture")
   Optional<ResourceLocation> crysismod$cutoutTexture();
}
