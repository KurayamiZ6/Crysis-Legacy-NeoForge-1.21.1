package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;

public final class ThermalFirstPersonArmRenderTypes extends RenderStateShard {
   private static final Map<ResourceLocation, RenderType> WARM_TYPES = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> V3_WARM_TYPES = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> DEPTH_TYPES = new ConcurrentHashMap<>();

   private ThermalFirstPersonArmRenderTypes() {
      super("nanovision_first_person_arm_render_types", () -> {}, () -> {});
   }

   public static RenderType warm(ResourceLocation texture) {
      return NanovisionConfig.thermalVisualPreset().usesOptimizedV3Pipeline()
         ? V3_WARM_TYPES.computeIfAbsent(texture, ThermalFirstPersonArmRenderTypes::createV3WarmType)
         : WARM_TYPES.computeIfAbsent(texture, ThermalFirstPersonArmRenderTypes::createWarmType);
   }

   public static RenderType depthOnly(ResourceLocation texture) {
      return DEPTH_TYPES.computeIfAbsent(texture, ThermalFirstPersonArmRenderTypes::createDepthType);
   }

   private static RenderType createWarmType(ResourceLocation texture) {
      return createWarmType(texture, false);
   }

   private static RenderType createV3WarmType(ResourceLocation texture) {
      return createWarmType(texture, true);
   }

   private static RenderType createWarmType(ResourceLocation texture, boolean optimizedV3) {
      CompositeState state = CompositeState.builder()
         .setShaderState(new ShaderStateShard(ThermalFirstPersonArmShaders::warmArmShader))
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(NO_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setDepthTestState(LEQUAL_DEPTH_TEST)
         .setWriteMaskState(COLOR_DEPTH_WRITE)
         .setOutputState(ThermalFirstPersonArmRenderTarget.OUTPUT)
         .createCompositeState(true);
      return RenderType.create(
         optimizedV3 ? "crysismod_nanovision_first_person_warm_arm_v3" : "crysismod_nanovision_first_person_warm_arm",
         DefaultVertexFormat.NEW_ENTITY,
         Mode.QUADS,
         256,
         true,
         !optimizedV3,
         state
      );
   }

   private static RenderType createDepthType(ResourceLocation texture) {
      CompositeState state = CompositeState.builder()
         .setShaderState(RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER)
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(NO_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setDepthTestState(LEQUAL_DEPTH_TEST)
         .setWriteMaskState(DEPTH_WRITE)
         .createCompositeState(true);
      return RenderType.create("crysismod_nanovision_first_person_arm_depth", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, false, state);
   }
}
