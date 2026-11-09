package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;

public final class NanoGeoArmorModeRenderTypes extends RenderStateShard {
   private static final Map<ResourceLocation, RenderType> ARMOR_MODE_OVERLAY = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> ARMOR_MODE_OVERLAY_COMPATIBILITY = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> ARMOR_MODE_OVERLAY_COMPATIBILITY_ADDITIVE = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> ARMOR_MODE_OVERLAY_COMPATIBILITY_SMOOTH = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> ARMOR_MODE_OVERLAY_COMPATIBILITY_SMOOTH_ADDITIVE = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> ARMOR_MODE_OVERLAY_COMPATIBILITY_UNSHADED = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> ARMOR_MODE_OVERLAY_COMPATIBILITY_PULSE_UNSHADED = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> ARMOR_MODE_OVERLAY_COMPATIBILITY_PULSE_UNSHADED_SMOOTH = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> ARMOR_MODE_OVERLAY_COMPATIBILITY_CULLED = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> ARMOR_MODE_OVERLAY_COMPATIBILITY_PULSE_UNSHADED_CULLED = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> ARMOR_MODE_OVERLAY_COMPATIBILITY_PULSE_UNSHADED_SMOOTH_CULLED = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> ARMOR_MODE_BASE_COMPOSITE = new ConcurrentHashMap<>();
   private static final Map<ResourceLocation, RenderType> ARMOR_MODE_BASE_COMPOSITE_SHADERPACK_STABLE = new ConcurrentHashMap<>();

   private NanoGeoArmorModeRenderTypes() {
      super("nanogeo_armor_mode_render_types", () -> {}, () -> {});
   }

   public static RenderType armorModeOverlay(ResourceLocation texture) {
      return NanoGeoShaderpackCompatibility.shouldUseArmorModeCompatibilityFallback()
         ? armorModeOverlayCompatibility(texture)
         : armorModeOverlayCustomShader(texture);
   }

   public static RenderType armorModeOverlayCustomShader(ResourceLocation texture) {
      return ARMOR_MODE_OVERLAY.computeIfAbsent(texture, NanoGeoArmorModeRenderTypes::createArmorModeOverlay);
   }

   public static RenderType armorModeOverlayCompatibility(ResourceLocation texture) {
      return ARMOR_MODE_OVERLAY_COMPATIBILITY.computeIfAbsent(texture, NanoGeoArmorModeRenderTypes::createArmorModeOverlayCompatibility);
   }

   public static RenderType armorModeOverlayCompatibilityAdditive(ResourceLocation texture) {
      return ARMOR_MODE_OVERLAY_COMPATIBILITY_ADDITIVE.computeIfAbsent(texture, NanoGeoArmorModeRenderTypes::createArmorModeOverlayCompatibilityAdditive);
   }

   public static RenderType armorModeOverlayCompatibilitySmooth(ResourceLocation texture) {
      return ARMOR_MODE_OVERLAY_COMPATIBILITY_SMOOTH.computeIfAbsent(texture, NanoGeoArmorModeRenderTypes::createArmorModeOverlayCompatibilitySmooth);
   }

   public static RenderType armorModeOverlayCompatibilitySmoothAdditive(ResourceLocation texture) {
      return ARMOR_MODE_OVERLAY_COMPATIBILITY_SMOOTH_ADDITIVE.computeIfAbsent(
         texture, NanoGeoArmorModeRenderTypes::createArmorModeOverlayCompatibilitySmoothAdditive
      );
   }

   public static RenderType armorModeOverlayCompatibilityUnshaded(ResourceLocation texture) {
      return ARMOR_MODE_OVERLAY_COMPATIBILITY_UNSHADED.computeIfAbsent(texture, NanoGeoArmorModeRenderTypes::createArmorModeOverlayCompatibilityUnshaded);
   }

   public static RenderType armorModeOverlayCompatibilityPulseUnshaded(ResourceLocation texture) {
      return ARMOR_MODE_OVERLAY_COMPATIBILITY_PULSE_UNSHADED.computeIfAbsent(
         texture, NanoGeoArmorModeRenderTypes::createArmorModeOverlayCompatibilityPulseUnshaded
      );
   }

   public static RenderType armorModeOverlayCompatibilityPulseUnshadedSmooth(ResourceLocation texture) {
      return ARMOR_MODE_OVERLAY_COMPATIBILITY_PULSE_UNSHADED_SMOOTH.computeIfAbsent(
         texture, NanoGeoArmorModeRenderTypes::createArmorModeOverlayCompatibilityPulseUnshadedSmooth
      );
   }

   public static RenderType armorModeOverlayCompatibilityCulled(ResourceLocation texture) {
      return ARMOR_MODE_OVERLAY_COMPATIBILITY_CULLED.computeIfAbsent(texture, NanoGeoArmorModeRenderTypes::createArmorModeOverlayCompatibilityCulled);
   }

   public static RenderType armorModeOverlayCompatibilityPulseUnshadedCulled(ResourceLocation texture) {
      return ARMOR_MODE_OVERLAY_COMPATIBILITY_PULSE_UNSHADED_CULLED.computeIfAbsent(
         texture, NanoGeoArmorModeRenderTypes::createArmorModeOverlayCompatibilityPulseUnshadedCulled
      );
   }

   public static RenderType armorModeOverlayCompatibilityPulseUnshadedSmoothCulled(ResourceLocation texture) {
      return ARMOR_MODE_OVERLAY_COMPATIBILITY_PULSE_UNSHADED_SMOOTH_CULLED.computeIfAbsent(
         texture, NanoGeoArmorModeRenderTypes::createArmorModeOverlayCompatibilityPulseUnshadedSmoothCulled
      );
   }

   public static RenderType armorModeBaseComposite(ResourceLocation texture) {
      return NanoGeoShaderpackCompatibility.shouldUseArmorModeCompatibilityFallback()
         ? ARMOR_MODE_BASE_COMPOSITE_SHADERPACK_STABLE.computeIfAbsent(texture, RenderType::entityCutoutNoCull)
         : ARMOR_MODE_BASE_COMPOSITE.computeIfAbsent(texture, RenderType::armorCutoutNoCull);
   }

   private static RenderType createArmorModeOverlay(ResourceLocation texture) {
      CompositeState state = CompositeState.builder()
         .setShaderState(new ShaderStateShard(NanoGeoArmorModeXShimmerShader::resolveShader))
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setLayeringState(VIEW_OFFSET_Z_LAYERING)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(true);
      return RenderType.create("crysismod_nanogeo_armor_mode_overlay", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, true, state);
   }

   private static RenderType createArmorModeOverlayCompatibility(ResourceLocation texture) {
      CompositeState state = CompositeState.builder()
         .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setLayeringState(VIEW_OFFSET_Z_LAYERING)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(true);
      return RenderType.create("crysismod_nanogeo_armor_mode_overlay_compatibility", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, true, state);
   }

   private static RenderType createArmorModeOverlayCompatibilityCulled(ResourceLocation texture) {
      CompositeState state = CompositeState.builder()
         .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setCullState(CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setLayeringState(VIEW_OFFSET_Z_LAYERING)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(true);
      return RenderType.create("crysismod_nanogeo_armor_mode_overlay_compatibility_culled", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, true, state);
   }

   private static RenderType createArmorModeOverlayCompatibilityAdditive(ResourceLocation texture) {
      CompositeState state = CompositeState.builder()
         .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(ADDITIVE_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setLayeringState(VIEW_OFFSET_Z_LAYERING)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(true);
      return RenderType.create(
         "crysismod_nanogeo_armor_mode_overlay_compatibility_additive", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, true, state
      );
   }

   private static RenderType createArmorModeOverlayCompatibilitySmooth(ResourceLocation texture) {
      CompositeState state = CompositeState.builder()
         .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
         .setTextureState(new TextureStateShard(texture, true, false))
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setLayeringState(VIEW_OFFSET_Z_LAYERING)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(true);
      return RenderType.create("crysismod_nanogeo_armor_mode_overlay_compatibility_smooth", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, true, state);
   }

   private static RenderType createArmorModeOverlayCompatibilitySmoothAdditive(ResourceLocation texture) {
      CompositeState state = CompositeState.builder()
         .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
         .setTextureState(new TextureStateShard(texture, true, false))
         .setTransparencyState(ADDITIVE_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setLayeringState(VIEW_OFFSET_Z_LAYERING)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(true);
      return RenderType.create(
         "crysismod_nanogeo_armor_mode_overlay_compatibility_smooth_additive", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, true, state
      );
   }

   private static RenderType createArmorModeOverlayCompatibilityUnshaded(ResourceLocation texture) {
      CompositeState state = CompositeState.builder()
         .setShaderState(RENDERTYPE_EYES_SHADER)
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setLightmapState(NO_LIGHTMAP)
         .setOverlayState(NO_OVERLAY)
         .setLayeringState(VIEW_OFFSET_Z_LAYERING)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(true);
      return RenderType.create(
         "crysismod_nanogeo_armor_mode_overlay_compatibility_unshaded", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, true, state
      );
   }

   private static RenderType createArmorModeOverlayCompatibilityPulseUnshaded(ResourceLocation texture) {
      CompositeState state = CompositeState.builder()
         .setShaderState(RENDERTYPE_EYES_SHADER)
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(ADDITIVE_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setLightmapState(NO_LIGHTMAP)
         .setOverlayState(NO_OVERLAY)
         .setLayeringState(VIEW_OFFSET_Z_LAYERING)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(false);
      return RenderType.create(
         "crysismod_nanogeo_armor_mode_overlay_compatibility_pulse_unshaded", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, false, state
      );
   }

   private static RenderType createArmorModeOverlayCompatibilityPulseUnshadedCulled(ResourceLocation texture) {
      CompositeState state = CompositeState.builder()
         .setShaderState(RENDERTYPE_EYES_SHADER)
         .setTextureState(new TextureStateShard(texture, false, false))
         .setTransparencyState(ADDITIVE_TRANSPARENCY)
         .setCullState(CULL)
         .setLightmapState(NO_LIGHTMAP)
         .setOverlayState(NO_OVERLAY)
         .setLayeringState(VIEW_OFFSET_Z_LAYERING)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(false);
      return RenderType.create(
         "crysismod_nanogeo_armor_mode_overlay_compatibility_pulse_unshaded_culled", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, false, state
      );
   }

   private static RenderType createArmorModeOverlayCompatibilityPulseUnshadedSmooth(ResourceLocation texture) {
      CompositeState state = CompositeState.builder()
         .setShaderState(RENDERTYPE_EYES_SHADER)
         .setTextureState(new TextureStateShard(texture, true, false))
         .setTransparencyState(ADDITIVE_TRANSPARENCY)
         .setCullState(NO_CULL)
         .setLightmapState(NO_LIGHTMAP)
         .setOverlayState(NO_OVERLAY)
         .setLayeringState(VIEW_OFFSET_Z_LAYERING)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(false);
      return RenderType.create(
         "crysismod_nanogeo_armor_mode_overlay_compatibility_pulse_unshaded_smooth", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, false, state
      );
   }

   private static RenderType createArmorModeOverlayCompatibilityPulseUnshadedSmoothCulled(ResourceLocation texture) {
      CompositeState state = CompositeState.builder()
         .setShaderState(RENDERTYPE_EYES_SHADER)
         .setTextureState(new TextureStateShard(texture, true, false))
         .setTransparencyState(ADDITIVE_TRANSPARENCY)
         .setCullState(CULL)
         .setLightmapState(NO_LIGHTMAP)
         .setOverlayState(NO_OVERLAY)
         .setLayeringState(VIEW_OFFSET_Z_LAYERING)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(false);
      return RenderType.create(
         "crysismod_nanogeo_armor_mode_overlay_compatibility_pulse_unshaded_smooth_culled", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, false, state
      );
   }
}
