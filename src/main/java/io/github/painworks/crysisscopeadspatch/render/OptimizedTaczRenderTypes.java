package io.github.painworks.crysisscopeadspatch.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.github.painworks.crysismod.mixin.client.nanovision.RenderTypeCompositeAccessor;
import io.github.painworks.crysismod.mixin.client.nanovision.RenderTypeCompositeStateAccessor;
import io.github.painworks.crysismod.mixin.client.nanovision.TextureStateShardAccessor;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.EmptyTextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;

public final class OptimizedTaczRenderTypes extends RenderStateShard {
   private static final Map<ResourceLocation, RenderType> TYPES = new ConcurrentHashMap<>();
   private static final Set<RenderType> OPTIMIZED = ConcurrentHashMap.newKeySet();

   private OptimizedTaczRenderTypes() {
      super("crysisscopefix_optimized_tacz_types", () -> {}, () -> {});
   }

   public static RenderType resolve(RenderType original, RenderType legacy) {
      if (OptimizedTaczShaders.isAvailable()
         && original != null
         && legacy != null
         && original.format() == DefaultVertexFormat.NEW_ENTITY
         && original.mode() == Mode.QUADS
         && original instanceof RenderTypeCompositeAccessor composite) {
         EmptyTextureStateShard textureState = ((RenderTypeCompositeStateAccessor)(Object)composite.crysismod$getState()).crysismod$getTextureState();
         Optional<ResourceLocation> texture = ((TextureStateShardAccessor)textureState).crysismod$cutoutTexture();
         return texture.isEmpty() ? legacy : TYPES.computeIfAbsent(texture.get(), OptimizedTaczRenderTypes::create);
      } else {
         return legacy;
      }
   }

   public static boolean isOptimized(RenderType type) {
      return OPTIMIZED.contains(type);
   }

   private static RenderType create(ResourceLocation texture) {
      CompositeState state = CompositeState.builder()
         .setShaderState(new ShaderStateShard(OptimizedTaczShaders::shader))
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setDepthTestState(LEQUAL_DEPTH_TEST)
         .setWriteMaskState(COLOR_DEPTH_WRITE)
         .setOutputState(MAIN_TARGET)
         .createCompositeState(true);
      RenderType type = RenderType.create(
         "crysisscopefix_optimized_tacz_entity", DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, Mode.QUADS, 256, true, false, state
      );
      OPTIMIZED.add(type);
      return type;
   }
}
