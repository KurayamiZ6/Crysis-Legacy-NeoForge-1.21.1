package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.github.painworks.crysismod.mixin.client.nanovision.RenderTypeCompositeAccessor;
import io.github.painworks.crysismod.mixin.client.nanovision.RenderTypeCompositeStateAccessor;
import io.github.painworks.crysismod.mixin.client.nanovision.TextureStateShardAccessor;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.EmptyTextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;

public final class ThermalFirstPersonItemRenderTypes extends RenderStateShard {
   private static final Map<ThermalFirstPersonItemRenderTypes.CaptureKey, RenderType> TYPES = new ConcurrentHashMap<>();

   private ThermalFirstPersonItemRenderTypes() {
      super("nanovision_first_person_item_render_types", () -> {}, () -> {});
   }

   static RenderType capture(RenderType original) {
      return resolve(original, TYPES, ThermalFirstPersonItemRenderTypes::create);
   }

   private static RenderType resolve(
      RenderType original,
      Map<ThermalFirstPersonItemRenderTypes.CaptureKey, RenderType> cache,
      Function<ThermalFirstPersonItemRenderTypes.CaptureKey, RenderType> factory
   ) {
      if (original instanceof RenderTypeCompositeAccessor composite && original.mode() == Mode.QUADS) {
         ThermalFirstPersonItemRenderTypes.Layout layout;
         if (original.format() == DefaultVertexFormat.NEW_ENTITY) {
            layout = ThermalFirstPersonItemRenderTypes.Layout.NEW_ENTITY;
         } else {
            if (original.format() != DefaultVertexFormat.BLOCK) {
               return null;
            }

            layout = ThermalFirstPersonItemRenderTypes.Layout.BLOCK;
         }

         EmptyTextureStateShard textureState = ((RenderTypeCompositeStateAccessor)(Object)composite.crysismod$getState()).crysismod$getTextureState();
         Optional<ResourceLocation> texture = ((TextureStateShardAccessor)textureState).crysismod$cutoutTexture();
         return texture.isEmpty() ? null : cache.computeIfAbsent(new ThermalFirstPersonItemRenderTypes.CaptureKey(texture.get(), layout), factory);
      } else {
         return null;
      }
   }

   private static RenderType create(ThermalFirstPersonItemRenderTypes.CaptureKey key) {
      VertexFormat format = key.layout() == ThermalFirstPersonItemRenderTypes.Layout.NEW_ENTITY ? DefaultVertexFormat.NEW_ENTITY : DefaultVertexFormat.BLOCK;
      ShaderStateShard shader = key.layout() == ThermalFirstPersonItemRenderTypes.Layout.NEW_ENTITY
         ? new ShaderStateShard(ThermalFirstPersonItemShaders::entityOverlayShader)
         : new ShaderStateShard(ThermalFirstPersonItemShaders::blockOverlayShader);
      CompositeState state = CompositeState.builder()
         .setShaderState(shader)
         .setTextureState(new TextureStateShard(key.texture(), false, false))
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setDepthTestState(LEQUAL_DEPTH_TEST)
         .setWriteMaskState(COLOR_DEPTH_WRITE)
         .setOutputState(MAIN_TARGET)
         .createCompositeState(true);
      return RenderType.create("crysismod_nanovision_first_person_item_" + key.layout().name().toLowerCase(), format, Mode.QUADS, 256, true, false, state);
   }

   private record CaptureKey(ResourceLocation texture, ThermalFirstPersonItemRenderTypes.Layout layout) {
   }

   private static enum Layout {
      NEW_ENTITY,
      BLOCK;
   }
}
