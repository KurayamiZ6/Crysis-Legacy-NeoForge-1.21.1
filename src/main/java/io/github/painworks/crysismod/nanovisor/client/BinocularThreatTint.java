package io.github.painworks.crysismod.nanovisor.client;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import io.github.painworks.crysismod.mixin.client.nanovision.RenderTypeCompositeAccessor;
import io.github.painworks.crysismod.mixin.client.nanovision.RenderTypeCompositeStateAccessor;
import io.github.painworks.crysismod.mixin.client.nanovision.TextureStateShardAccessor;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.client.ClassicEnemyContacts;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanovisor.network.SyncNanoVisorMarkedTargetsPacket;
import java.io.IOException;
import java.util.LinkedHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

/** Collects the polygons already submitted by the world renderer, then tints the visible surface once. */
@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT)
public final class BinocularThreatTint extends RenderStateShard {
   private static final LinkedHashMap<ResourceLocation, RenderType> MASK_TYPES = new LinkedHashMap<>(32, .75F, true);
   private static final LinkedHashMap<RenderType, Batch> BATCHES = new LinkedHashMap<>();
   private static ShaderInstance maskShader;
   private static ShaderInstance compositeShader;
   private static TextureTarget mask;
   private static int compositeFramebuffer;
   private static NanosuitProfile optic;
   private static boolean drawContour;
   private static Matrix4f projection, modelView;
   private static VertexSorting sorting;

   private BinocularThreatTint() {
      super("binocular_threat_tint", () -> {}, () -> {});
   }

   @SubscribeEvent
   public static void registerShaders(RegisterShadersEvent event) throws IOException {
      event.registerShader(new ShaderInstance(event.getResourceProvider(), id("binocular_threat_mask"),
         DefaultVertexFormat.NEW_ENTITY), shader -> maskShader = shader);
      event.registerShader(new ShaderInstance(event.getResourceProvider(), id("binocular_threat_composite"),
         DefaultVertexFormat.POSITION_TEX), shader -> compositeShader = shader);
   }

   private static ResourceLocation id(String name) {
      return ResourceLocation.fromNamespaceAndPath("crysismod", name);
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void renderWorld(RenderLevelStageEvent event) {
      var stage = event.getStage();
      if (stage == RenderLevelStageEvent.Stage.AFTER_SKY) {
         optic = activeOptic();
         drawContour = optic == NanosuitProfile.NANOSUIT_2;
      } else if (stage == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
         optic = null;
      } else if (stage == RenderLevelStageEvent.Stage.AFTER_LEVEL && projection != null) {
         drawTint();
      }
   }

   private static NanosuitProfile activeOptic() {
      var mc = Minecraft.getInstance();
      if (maskShader == null || compositeShader == null || mc.options.hideGui || mc.screen != null
         || mc.getOverlay() != null || !NanoVisorState.shouldRenderOverlay()) return null;
      return NanosuitArmorUtil.getUsableProfile(mc.player);
   }

   public static MultiBufferSource capture(Entity entity, MultiBufferSource original) {
      int color;
      if (optic == NanosuitProfile.NANOSUIT_1 && ClassicEnemyContacts.outline(entity)) {
         color = 0x76000000 | ClassicEnemyContacts.color(entity);
      } else if (optic == NanosuitProfile.NANOSUIT_2 && entity instanceof Enemy && entity.isAlive()
         && !entity.isInvisible() && NanoVisorClientMarkedTargets.isMarkedActive(entity)) {
         var player = Minecraft.getInstance().player;
         double range = SyncNanoVisorMarkedTargetsPacket.N2_SCAN_RANGE;
         if (player == null || player.distanceToSqr(entity) > range * range) return original;
         color = 0x33000000 | Nanosuit2Optic.contactColor(entity);
      } else {
         return original;
      }
      return layer -> {
         VertexConsumer normal = original.getBuffer(layer);
         if (layer.format() != DefaultVertexFormat.NEW_ENTITY || layer.mode() != VertexFormat.Mode.QUADS
            || layer.outline().isEmpty() || !(layer instanceof RenderTypeCompositeAccessor composite)) return normal;
         var state = (RenderTypeCompositeStateAccessor)(Object)composite.crysismod$getState();
         var texture = ((TextureStateShardAccessor)state.crysismod$getTextureState()).crysismod$cutoutTexture();
         if (texture.isEmpty()) return normal;
         if (projection == null) {
            projection = new Matrix4f(RenderSystem.getProjectionMatrix());
            modelView = new Matrix4f(RenderSystem.getModelViewMatrix());
            sorting = RenderSystem.getVertexSorting();
         }
         RenderType type = maskType(texture.get());
         if (!BATCHES.containsKey(type) && BATCHES.size() >= 128) return normal;
         Batch batch = BATCHES.computeIfAbsent(type, ignored -> new Batch(new ByteBufferBuilder(1536)));
         return VertexMultiConsumer.create(normal, new TintVertex(batch.vertices(), color));
      };
   }

   private static RenderType maskType(ResourceLocation texture) {
      RenderType type = MASK_TYPES.get(texture);
      if (type != null) return type;
      // All mask textures have their own batch. They are replayed in order against a shared depth buffer.
      type = RenderType.create("crysismod_binocular_threat_mask", DefaultVertexFormat.NEW_ENTITY,
         VertexFormat.Mode.QUADS, 1536, false, false, RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> maskShader))
            .setTextureState(new TextureStateShard(texture, false, false))
            .setTransparencyState(NO_TRANSPARENCY)
            .setCullState(NO_CULL)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .createCompositeState(false));
      if (MASK_TYPES.size() >= 128) MASK_TYPES.pollFirstEntry();
      MASK_TYPES.put(texture, type);
      return type;
   }

   private static void drawTint() {
      var main = Minecraft.getInstance().getMainRenderTarget();
      var oldProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
      var oldSorting = RenderSystem.getVertexSorting();
      var view = RenderSystem.getModelViewStack();
      var oldShader = RenderSystem.getShader();
      int drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
      int readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
      boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
      boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
      boolean depthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
      view.pushMatrix();
      try (var stack = MemoryStack.stackPush()) {
         var viewport = stack.mallocInt(4);
         var writeMask = stack.malloc(4);
         GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
         GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, writeMask);
         try {
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
            if (mask == null || mask.width != main.width || mask.height != main.height) {
               if (mask != null) mask.destroyBuffers();
               mask = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
               mask.setClearColor(0, 0, 0, 0);
            }
            mask.clear(Minecraft.ON_OSX);
            mask.bindWrite(false);
            RenderSystem.setProjectionMatrix(projection, sorting);
            view.set(modelView);
            RenderSystem.applyModelViewMatrix();
            for (var entry : BATCHES.entrySet()) {
               var mesh = entry.getValue().vertices().build();
               if (mesh != null) entry.getKey().draw(mesh);
            }
            // Iris and vanilla can leave a different framebuffer bound at AFTER_LEVEL.
            // Attach only the final scene's color so the shader can safely sample the scene depth.
            if (compositeFramebuffer == 0) compositeFramebuffer = GL30.glGenFramebuffers();
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, compositeFramebuffer);
            GL30.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
               GL11.GL_TEXTURE_2D, main.getColorTextureId(), 0);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            compositeShader.setSampler("Mask", mask.getColorTextureId());
            compositeShader.setSampler("MaskDepth", mask.getDepthTextureId());
            compositeShader.setSampler("SceneDepth", main.getDepthTextureId());
            compositeShader.safeGetUniform("DrawContour").set(drawContour ? 1 : 0);
            RenderSystem.setShader(() -> compositeShader);
            var quad = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            quad.addVertex(-1, -1, 0).setUv(0, 0);
            quad.addVertex(1, -1, 0).setUv(1, 0);
            quad.addVertex(1, 1, 0).setUv(1, 1);
            quad.addVertex(-1, 1, 0).setUv(0, 1);
            BufferUploader.drawWithShader(quad.buildOrThrow());
         } finally {
            RenderSystem.viewport(viewport.get(0), viewport.get(1), viewport.get(2), viewport.get(3));
            RenderSystem.colorMask(writeMask.get(0) != 0, writeMask.get(1) != 0, writeMask.get(2) != 0, writeMask.get(3) != 0);
         }
      } finally {
         GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
         GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
         view.popMatrix();
         RenderSystem.applyModelViewMatrix();
         RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
         RenderSystem.depthMask(depthWrite);
         if (depth) RenderSystem.enableDepthTest();
         else RenderSystem.disableDepthTest();
         if (!blend) RenderSystem.disableBlend();
         RenderSystem.setShader(() -> oldShader);
         projection = null;
         clearBatches();
      }
   }

   @SubscribeEvent
   public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
      clearBatches();
      projection = null;
      optic = null;
      if (mask != null) mask.destroyBuffers();
      mask = null;
      if (compositeFramebuffer != 0) GL30.glDeleteFramebuffers(compositeFramebuffer);
      compositeFramebuffer = 0;
      MASK_TYPES.clear();
   }

   private static void clearBatches() {
      BATCHES.values().forEach(batch -> batch.storage().close());
      BATCHES.clear();
   }

   private record Batch(ByteBufferBuilder storage, BufferBuilder vertices) {
      Batch(ByteBufferBuilder storage) {
         this(storage, new BufferBuilder(storage, VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY));
      }
   }

   private record TintVertex(VertexConsumer target, int color) implements VertexConsumer {
      public VertexConsumer addVertex(float x, float y, float z) { target.addVertex(x, y, z).setColor(color); return this; }
      public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
      public VertexConsumer setUv(float u, float v) { target.setUv(u, v); return this; }
      public VertexConsumer setUv1(int u, int v) { target.setUv1(u, v); return this; }
      public VertexConsumer setUv2(int u, int v) { target.setUv2(u, v); return this; }
      public VertexConsumer setNormal(float x, float y, float z) { target.setNormal(x, y, z); return this; }
   }
}
