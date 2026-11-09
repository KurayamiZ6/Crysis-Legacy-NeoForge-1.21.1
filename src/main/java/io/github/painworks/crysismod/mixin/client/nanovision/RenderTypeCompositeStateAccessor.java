package io.github.painworks.crysismod.mixin.client.nanovision;

import net.minecraft.client.renderer.RenderStateShard.EmptyTextureStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CompositeState.class)
public interface RenderTypeCompositeStateAccessor {
   @Accessor("textureState")
   EmptyTextureStateShard crysismod$getTextureState();
}
