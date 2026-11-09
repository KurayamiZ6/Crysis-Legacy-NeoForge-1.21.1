package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
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

public final class ThermalMarkerRenderTypes extends RenderStateShard {
   private static final String MARKER_RENDER_TYPE_NAME = "crysismod_nanovision_thermal_marker";
   private static final String SOLID_MARKER_RENDER_TYPE_NAME = "crysismod_nanovision_thermal_marker_solid";
   private static final String HOT_BLOCK_RENDER_TYPE_NAME = "crysismod_nanovision_thermal_hot_block";
   private static final Map<ResourceLocation, RenderType> MARKER_TYPES = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> V3_FALLBACK_MARKER_TYPES = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> V3_ATLAS_MARKER_TYPES = new ConcurrentHashMap<>();
   private static final RenderType SOLID_MARKER_TYPE = createSolidMarkerType();
   private static final RenderType V3_SOLID_MARKER_TYPE = createV3SolidMarkerType();
   private static final RenderType HOT_BLOCK_MARKER_TYPE = createHotBlockMarkerType();
   private static final RenderType V3_HOT_BLOCK_MARKER_TYPE = createV3HotBlockMarkerType();

   private ThermalMarkerRenderTypes() {
      super("nanovision_marker_render_types", () -> {}, () -> {});
   }

   public static RenderType marker(ResourceLocation texture) {
      return MARKER_TYPES.computeIfAbsent(texture, ThermalMarkerRenderTypes::createMarkerType);
   }

   public static RenderType markerV3Fallback(ResourceLocation texture) {
      return V3_FALLBACK_MARKER_TYPES.computeIfAbsent(texture, ThermalMarkerRenderTypes::createV3FallbackMarkerType);
   }

   public static RenderType markerAtlas(ResourceLocation atlasTexture) {
      return V3_ATLAS_MARKER_TYPES.computeIfAbsent(atlasTexture, ThermalMarkerRenderTypes::createV3AtlasMarkerType);
   }

   public static RenderType markerSolid() {
      return NanovisionConfig.thermalVisualPreset().usesOptimizedV3Pipeline() ? V3_SOLID_MARKER_TYPE : SOLID_MARKER_TYPE;
   }

   public static RenderType compatibilityMarker(ResourceLocation texture) {
      return NanovisionConfig.thermalVisualPreset().usesOptimizedV3Pipeline() ? markerV3Fallback(texture) : marker(texture);
   }

   public static RenderType alphaAwareMarker(RenderType sourceType) {
      if (sourceType instanceof RenderTypeCompositeAccessor composite && sourceType.mode() == Mode.QUADS) {
         EmptyTextureStateShard textureState = ((RenderTypeCompositeStateAccessor)(Object)composite.crysismod$getState()).crysismod$getTextureState();
         Optional<ResourceLocation> texture = ((TextureStateShardAccessor)textureState).crysismod$cutoutTexture();
         return texture.map(ThermalMarkerRenderTypes::compatibilityMarker).orElse(null);
      } else {
         return null;
      }
   }

   public static RenderType hotBlock() {
      return NanovisionConfig.thermalVisualPreset().usesOptimizedV3Pipeline() ? V3_HOT_BLOCK_MARKER_TYPE : HOT_BLOCK_MARKER_TYPE;
   }

   private static RenderType createMarkerType(ResourceLocation texture) {
      return createTexturedMarkerType(texture, "crysismod_nanovision_thermal_marker", true);
   }

   private static RenderType createV3FallbackMarkerType(ResourceLocation texture) {
      return createTexturedMarkerType(texture, "crysismod_nanovision_thermal_marker_v3_fallback", false);
   }

   private static RenderType createV3AtlasMarkerType(ResourceLocation texture) {
      return createTexturedMarkerType(texture, "crysismod_nanovision_thermal_marker_v3_atlas", false);
   }

   private static RenderType createTexturedMarkerType(ResourceLocation texture, String name, boolean sortOnUpload) {
      CompositeState state = CompositeState.builder()
         .setShaderState(new ShaderStateShard(ThermalMarkerShaders::markerShader))
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(NO_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setDepthTestState(LEQUAL_DEPTH_TEST)
         .setWriteMaskState(COLOR_DEPTH_WRITE)
         .setOutputState(ThermalMarkerRenderTarget.OUTPUT)
         .createCompositeState(true);
      return RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, sortOnUpload, state);
   }

   private static RenderType createSolidMarkerType() {
      return createSolidMarkerType("crysismod_nanovision_thermal_marker_solid", true);
   }

   private static RenderType createV3SolidMarkerType() {
      return createSolidMarkerType("crysismod_nanovision_thermal_marker_solid_v3", false);
   }

   private static RenderType createSolidMarkerType(String name, boolean sortOnUpload) {
      CompositeState state = CompositeState.builder()
         .setShaderState(new ShaderStateShard(ThermalMarkerShaders::solidMarkerShader))
         .setTransparencyState(NO_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setDepthTestState(LEQUAL_DEPTH_TEST)
         .setWriteMaskState(COLOR_DEPTH_WRITE)
         .setOutputState(ThermalMarkerRenderTarget.OUTPUT)
         .createCompositeState(true);
      return RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, sortOnUpload, state);
   }

   private static RenderType createHotBlockMarkerType() {
      return createHotBlockMarkerType("crysismod_nanovision_thermal_hot_block", true);
   }

   private static RenderType createV3HotBlockMarkerType() {
      return createHotBlockMarkerType("crysismod_nanovision_thermal_hot_block_v3", false);
   }

   private static RenderType createHotBlockMarkerType(String name, boolean sortOnUpload) {
      CompositeState state = CompositeState.builder()
         .setShaderState(new ShaderStateShard(ThermalMarkerShaders::blockMarkerShader))
         .setTransparencyState(NO_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setDepthTestState(LEQUAL_DEPTH_TEST)
         .setWriteMaskState(COLOR_DEPTH_WRITE)
         .setOutputState(ThermalMarkerRenderTarget.OUTPUT)
         .createCompositeState(true);
      return RenderType.create(name, DefaultVertexFormat.POSITION_COLOR, Mode.QUADS, 512, false, sortOnUpload, state);
   }
}
