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
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.EmptyTextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;

final class ThermalShaderOffItemOutlineRenderTypes extends RenderStateShard {
   private static final Map<ThermalShaderOffItemOutlineRenderTypes.Key, RenderType> TYPES = new ConcurrentHashMap<>();

   private ThermalShaderOffItemOutlineRenderTypes() {
      super("nanovision_shader_off_item_outline_render_types", () -> {}, () -> {});
   }

   static RenderType capture(RenderType original) {
      if (original instanceof RenderTypeCompositeAccessor composite && original.mode() == Mode.QUADS) {
         ThermalShaderOffItemOutlineRenderTypes.Layout layout;
         if (original.format() == DefaultVertexFormat.NEW_ENTITY) {
            layout = ThermalShaderOffItemOutlineRenderTypes.Layout.ENTITY;
         } else {
            if (original.format() != DefaultVertexFormat.BLOCK) {
               return null;
            }

            layout = ThermalShaderOffItemOutlineRenderTypes.Layout.BLOCK;
         }

         EmptyTextureStateShard textureState = ((RenderTypeCompositeStateAccessor)(Object)composite.crysismod$getState()).crysismod$getTextureState();
         Optional<ResourceLocation> texture = ((TextureStateShardAccessor)textureState).crysismod$cutoutTexture();
         return texture.<RenderType>map(
               value -> TYPES.computeIfAbsent(new ThermalShaderOffItemOutlineRenderTypes.Key(value, layout), ThermalShaderOffItemOutlineRenderTypes::create)
            )
            .orElse(null);
      } else {
         return null;
      }
   }

   private static RenderType create(ThermalShaderOffItemOutlineRenderTypes.Key key) {
      VertexFormat format = key.layout == ThermalShaderOffItemOutlineRenderTypes.Layout.ENTITY ? DefaultVertexFormat.NEW_ENTITY : DefaultVertexFormat.BLOCK;
      ShaderStateShard shader = key.layout == ThermalShaderOffItemOutlineRenderTypes.Layout.ENTITY
         ? new ShaderStateShard(ThermalShaderOffItemOutlineCapture::entityMaskShader)
         : new ShaderStateShard(ThermalShaderOffItemOutlineCapture::blockMaskShader);
      CompositeState state = CompositeState.builder()
         .setShaderState(shader)
         .setTextureState(new TextureStateShard(key.texture, false, false))
         .setTransparencyState(NO_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setDepthTestState(LEQUAL_DEPTH_TEST)
         .setWriteMaskState(COLOR_DEPTH_WRITE)
         .setOutputState(ThermalShaderOffItemOutlineCapture.OUTPUT)
         .createCompositeState(true);
      return RenderType.create("crysismod_nanovision_shader_off_item_outline_" + key.layout.name().toLowerCase(), format, Mode.QUADS, 256, true, false, state);
   }

   private record Key(ResourceLocation texture, ThermalShaderOffItemOutlineRenderTypes.Layout layout) {
   }

   private static enum Layout {
      ENTITY,
      BLOCK;
   }
}
