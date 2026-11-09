package io.github.painworks.crysismod.client.nanogeo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class NanoGeoTextureCache {
   private final Map<ResourceLocation, RenderType> genericRenderTypes = new ConcurrentHashMap<>();
   private final Map<ResourceLocation, RenderType> armorRenderTypes = new ConcurrentHashMap<>();
   private final Map<ResourceLocation, RenderType> shaderpackStableArmorRenderTypes = new ConcurrentHashMap<>();
   private final Map<ResourceLocation, RenderType> translucentRenderTypes = new ConcurrentHashMap<>();
   private final Map<ResourceLocation, RenderType> cutoutCulledRenderTypes = new ConcurrentHashMap<>();

   public RenderType getOrCreateRenderType(ResourceLocation texture) {
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.RENDERTYPE_LOOKUPS);
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.TEXTURE_LOOKUPS);
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MAP_LOOKUPS);
      return this.genericRenderTypes.computeIfAbsent(texture, RenderType::entityCutoutNoCull);
   }

   public RenderType getOrCreateArmorRenderType(ResourceLocation texture) {
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.RENDERTYPE_LOOKUPS);
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.TEXTURE_LOOKUPS);
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MAP_LOOKUPS);
      return NanoGeoShaderpackCompatibility.shouldUseArmorModeCompatibilityFallback()
         ? this.shaderpackStableArmorRenderTypes.computeIfAbsent(texture, RenderType::entityCutoutNoCull)
         : this.armorRenderTypes.computeIfAbsent(texture, RenderType::armorCutoutNoCull);
   }

   public RenderType getOrCreateRenderType(ResourceLocation texture, NanoGeoClientConfig.NanoGeoDebugRenderType mode) {
      long sectionStart = NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.TEXTURE_LOOKUP);

      return switch (mode) {
         case ENTITY_CUTOUT_NO_CULL -> {
            RenderType renderType = this.getOrCreateRenderType(texture);
            NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.TEXTURE_LOOKUP, sectionStart);
            yield renderType;
         }
         case ENTITY_TRANSLUCENT -> {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.RENDERTYPE_LOOKUPS);
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.TEXTURE_LOOKUPS);
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MAP_LOOKUPS);
            RenderType renderType = this.translucentRenderTypes.computeIfAbsent(texture, RenderType::entityTranslucent);
            NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.TEXTURE_LOOKUP, sectionStart);
            yield renderType;
         }
         case ARMOR_CUTOUT_NO_CULL -> {
            RenderType renderType = this.getOrCreateArmorRenderType(texture);
            NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.TEXTURE_LOOKUP, sectionStart);
            yield renderType;
         }
      };
   }

   public RenderType getOrCreateEntityCutout(ResourceLocation texture) {
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.RENDERTYPE_LOOKUPS);
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.TEXTURE_LOOKUPS);
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MAP_LOOKUPS);
      return this.cutoutCulledRenderTypes.computeIfAbsent(texture, RenderType::entityCutout);
   }

   public RenderType getOrCreateEntityTranslucent(ResourceLocation texture) {
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.RENDERTYPE_LOOKUPS);
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.TEXTURE_LOOKUPS);
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MAP_LOOKUPS);
      return this.translucentRenderTypes.computeIfAbsent(texture, RenderType::entityTranslucent);
   }

   public void clear() {
      this.genericRenderTypes.clear();
      this.armorRenderTypes.clear();
      this.shaderpackStableArmorRenderTypes.clear();
      this.translucentRenderTypes.clear();
      this.cutoutCulledRenderTypes.clear();
   }
}
