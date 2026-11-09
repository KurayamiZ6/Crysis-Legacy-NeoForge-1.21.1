package io.github.painworks.crysismod.mixin.client.nanovision;

import net.minecraft.client.renderer.RenderType.CompositeState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.renderer.RenderType$CompositeRenderType")
public interface RenderTypeCompositeAccessor {
   @Accessor("state")
   CompositeState crysismod$getState();
}
