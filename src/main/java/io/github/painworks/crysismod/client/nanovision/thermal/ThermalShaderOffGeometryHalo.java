package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import io.github.painworks.crysismod.mixin.client.nanovision.RenderTypeCompositeAccessor;
import io.github.painworks.crysismod.mixin.client.nanovision.RenderTypeCompositeStateAccessor;
import io.github.painworks.crysismod.mixin.client.nanovision.TextureStateShardAccessor;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderStateShard.EmptyTextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.OutputStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.WriteMaskStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.slf4j.Logger;

public final class ThermalShaderOffGeometryHalo extends RenderStateShard {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation ENTITY_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_shader_off_geometry_halo_entity");
   private static final ResourceLocation BLOCK_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_shader_off_geometry_halo_block");
   private static final Map<ThermalShaderOffGeometryHalo.HaloKey, RenderType> BODY_TYPES = new ConcurrentHashMap<>();
   private static final Map<ThermalShaderOffGeometryHalo.HaloKey, RenderType> SCOPE_TYPES = new ConcurrentHashMap<>();
   private static final Map<BufferSource, Set<RenderType>> PENDING_SCOPE_BATCHES = new IdentityHashMap<>();
   private static final OutputStateShard CALLER_TARGET = new OutputStateShard("nanovision_shader_off_halo_caller_target", () -> {}, () -> {});
   private static ShaderInstance entityShader;
   private static ShaderInstance blockShader;
   private static boolean loggedBodyRoute;
   private static boolean loggedScopeRoute;
   private static boolean loggedBodySubmit;
   private static boolean loggedScopeSubmit;
   private static boolean loggedRejectedRoute;
   private static final boolean DIRECT_GEOMETRY_ROUTE_ENABLED = false;

   private ThermalShaderOffGeometryHalo() {
      super("nanovision_shader_off_geometry_halo", () -> {}, () -> {});
   }

   public static void registerShaders(RegisterShadersEvent event) {
      try {
         event.registerShader(new ShaderInstance(event.getResourceProvider(), ENTITY_SHADER, DefaultVertexFormat.NEW_ENTITY), shader -> entityShader = shader);
         event.registerShader(new ShaderInstance(event.getResourceProvider(), BLOCK_SHADER, DefaultVertexFormat.BLOCK), shader -> blockShader = shader);
      } catch (IOException var2) {
         entityShader = null;
         blockShader = null;
         LOGGER.error("[CrysisMod][Nanovision] failed to register shader-off geometry halo shaders", var2);
      }
   }

   public static VertexConsumer captureBody(BufferSource source, RenderType original) {
      return null;
   }

   public static VertexConsumer captureScopeBody(BufferSource source, RenderType original) {
      return null;
   }

   public static RenderType typeForBody(RenderType original) {
      return null;
   }

   public static void beginFrame() {
      PENDING_SCOPE_BATCHES.clear();
   }

   public static void noteBodyBatchSubmitted(RenderType halo) {
      if (!loggedBodySubmit && halo != null) {
         loggedBodySubmit = true;
         LOGGER.info("[CrysisMod][Nanovision][ShaderOffHalo] body halo batch submitted: {}", halo);
      }
   }

   public static void flushScopeBatches() {
      if (!PENDING_SCOPE_BATCHES.isEmpty()) {
         Map<BufferSource, Set<RenderType>> pending = new IdentityHashMap<>(PENDING_SCOPE_BATCHES);
         PENDING_SCOPE_BATCHES.clear();

         for (Entry<BufferSource, Set<RenderType>> entry : pending.entrySet()) {
            ThermalFirstPersonItemCapture.beginSourceDelegation();

            try {
               for (RenderType halo : entry.getValue()) {
                  entry.getKey().endBatch(halo);
                  if (!loggedScopeSubmit) {
                     loggedScopeSubmit = true;
                     LOGGER.info("[CrysisMod][Nanovision][ShaderOffHalo] semantic scope halo batch submitted: {}", halo);
                  }
               }
            } finally {
               ThermalFirstPersonItemCapture.endSourceDelegation();
            }
         }
      }
   }

   public static boolean isScopeBodyRenderType(RenderType type) {
      return type != null && type.toString().contains("crysismod_nanovision_first_person_scope_body_");
   }

   private static RenderType typeForScope(RenderType original) {
      return isEnabled() ? resolve(original, SCOPE_TYPES, true) : null;
   }

   private static VertexConsumer capture(BufferSource source, RenderType halo) {
      if (source != null && halo != null) {
         ThermalFirstPersonItemCapture.beginSourceDelegation();

         VertexConsumer var2;
         try {
            var2 = source.getBuffer(halo);
         } finally {
            ThermalFirstPersonItemCapture.endSourceDelegation();
         }

         return var2;
      } else {
         return null;
      }
   }

   private static boolean isEnabled() {
      Minecraft minecraft = Minecraft.getInstance();
      return !NanoGeoShaderpackCompatibility.isShaderPackActive()
         && minecraft != null
         && minecraft.player != null
         && minecraft.options.getCameraType().isFirstPerson()
         && NanovisionState.isActive(minecraft.player)
         && NanovisionConfig.thermalHeldItemsEnabled()
         && !NanovisionConfig.thermalMarkerDebugDisableGlow()
         && NanovisionConfig.thermalHeldItemOuterGlowRatio() > 1.0E-4
         && NanovisionConfig.thermalHeldItemOuterGlowStrength() > 0.001
         && entityShader != null
         && blockShader != null;
   }

   private static void logRejected(String route, RenderType original) {
      if (!loggedRejectedRoute) {
         loggedRejectedRoute = true;
         Minecraft minecraft = Minecraft.getInstance();
         LOGGER.info(
            "[CrysisMod][Nanovision][ShaderOffHalo] {} route rejected: shaderpack={} player={} firstPerson={} nanovision={} items={} ratio={} strength={} entityShader={} blockShader={} renderType={}",
            new Object[]{
               route,
               NanoGeoShaderpackCompatibility.isShaderPackActive(),
               minecraft != null && minecraft.player != null,
               minecraft != null && minecraft.options.getCameraType().isFirstPerson(),
               minecraft != null && minecraft.player != null && NanovisionState.isActive(minecraft.player),
               NanovisionConfig.thermalHeldItemsEnabled(),
               NanovisionConfig.thermalHeldItemOuterGlowRatio(),
               NanovisionConfig.thermalHeldItemOuterGlowStrength(),
               entityShader != null,
               blockShader != null,
               original
            }
         );
      }
   }

   private static RenderType resolve(RenderType original, Map<ThermalShaderOffGeometryHalo.HaloKey, RenderType> types, boolean scope) {
      if (original instanceof RenderTypeCompositeAccessor composite && original.mode() == Mode.QUADS) {
         ThermalShaderOffGeometryHalo.Layout layout;
         if (original.format() == DefaultVertexFormat.NEW_ENTITY) {
            layout = ThermalShaderOffGeometryHalo.Layout.NEW_ENTITY;
         } else {
            if (original.format() != DefaultVertexFormat.BLOCK) {
               return null;
            }

            layout = ThermalShaderOffGeometryHalo.Layout.BLOCK;
         }

         EmptyTextureStateShard textureState = ((RenderTypeCompositeStateAccessor)(Object)composite.crysismod$getState()).crysismod$getTextureState();
         Optional<ResourceLocation> texture = ((TextureStateShardAccessor)textureState).crysismod$cutoutTexture();
         return texture.<RenderType>map(
               resource -> types.computeIfAbsent(new ThermalShaderOffGeometryHalo.HaloKey(resource, layout), key -> create(key, scope))
            )
            .orElse(null);
      } else {
         return null;
      }
   }

   private static RenderType create(ThermalShaderOffGeometryHalo.HaloKey key, boolean scope) {
      VertexFormat format = key.layout == ThermalShaderOffGeometryHalo.Layout.NEW_ENTITY ? DefaultVertexFormat.NEW_ENTITY : DefaultVertexFormat.BLOCK;
      ShaderStateShard shader = key.layout == ThermalShaderOffGeometryHalo.Layout.NEW_ENTITY
         ? new ShaderStateShard(ThermalShaderOffGeometryHalo::prepareEntityShader)
         : new ShaderStateShard(ThermalShaderOffGeometryHalo::prepareBlockShader);
      CompositeState state = CompositeState.builder()
         .setShaderState(shader)
         .setTextureState(new TextureStateShard(key.texture, false, false))
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setCullState(scope ? CULL : NO_CULL)
         .setDepthTestState(LEQUAL_DEPTH_TEST)
         .setWriteMaskState(new WriteMaskStateShard(true, false))
         .setOutputState(CALLER_TARGET)
         .createCompositeState(true);
      return RenderType.create(
         "crysismod_nanovision_shader_off_" + (scope ? "scope_halo" : "item_halo") + "_" + key.layout.name().toLowerCase(),
         format,
         Mode.QUADS,
         256,
         true,
         false,
         state
      );
   }

   private static ShaderInstance prepareEntityShader() {
      return prepare(entityShader);
   }

   private static ShaderInstance prepareBlockShader() {
      return prepare(blockShader);
   }

   private static ShaderInstance prepare(ShaderInstance shader) {
      if (shader == null) {
         return null;
      } else {
         Minecraft minecraft = Minecraft.getInstance();
         int width = minecraft.getMainRenderTarget() == null ? 1 : minecraft.getMainRenderTarget().width;
         int height = minecraft.getMainRenderTarget() == null ? 1 : minecraft.getMainRenderTarget().height;
         float radius = (float)Math.max(1.0, Math.min(Math.min(width, height) * NanovisionConfig.thermalHeldItemOuterGlowRatio() * 0.1, 7.0));
         float opacity = (float)Math.min(0.68, NanovisionConfig.thermalHeldItemOuterGlowStrength() * 0.52);
         set(shader, "Viewport", width, height);
         set(shader, "HaloRadiusPx", radius);
         set(shader, "HaloOpacity", opacity);
         return shader;
      }
   }

   private static void set(ShaderInstance shader, String name, float x, float y) {
      Uniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(x, y);
      }
   }

   private static void set(ShaderInstance shader, String name, float value) {
      Uniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }

   private record HaloKey(ResourceLocation texture, ThermalShaderOffGeometryHalo.Layout layout) {
   }

   private static enum Layout {
      NEW_ENTITY,
      BLOCK;
   }
}
