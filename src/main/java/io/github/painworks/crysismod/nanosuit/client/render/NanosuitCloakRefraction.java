package io.github.painworks.crysismod.nanosuit.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/** Captures the nearest cloak surface, then samples the scene behind that surface once. */
public final class NanosuitCloakRefraction extends RenderStateShard {
   private static final int MAX_DEFERRED_BATCHES = 128;
   private static final int MAX_DEFERRED_VERTICES = 131_072;
   private static final List<DeferredBatch> DEFERRED_WORLD = new ArrayList<>();
   private static final List<DeferredBatch> DEFERRED_HANDS = new ArrayList<>();
   private static final List<DeferredBatch> DEFERRED_RETICLE = new ArrayList<>();
   private static final Map<ResourceLocation, RenderType> MASK_TYPES = new LinkedHashMap<>(64, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<ResourceLocation, RenderType> entry) {
         return size() > 128;
      }
   };
   private static final Map<ResourceLocation, RenderType> RETICLE_TYPES = new LinkedHashMap<>(16, .75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<ResourceLocation, RenderType> entry) {
         return size() > 32;
      }
   };
   private static ShaderInstance maskShader;
   private static ShaderInstance compositeShader;
   private static RenderTarget mask;
   private static RenderTarget sceneCopy;
   private static int compositeFramebuffer;
   private static int compositeColorTexture;
   private static int previousMaskDrawFramebuffer;
   private static int deferredVertices;
   private static boolean levelCaptureOpen;
   private static boolean deferredForIris;
   private static boolean maskPrepared;
   private static boolean hasGeometry;
   private static boolean failed;
   private static boolean warnedReticle;
   private static final OutputStateShard MASK_OUTPUT = new OutputStateShard("crysismod_cloak_mask", () -> {
      previousMaskDrawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
      GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, mask.frameBufferId);
      hasGeometry = true;
   }, () -> GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousMaskDrawFramebuffer));

   private NanosuitCloakRefraction() {
      super("crysismod_cloak_refraction", () -> {}, () -> {});
   }

   public static void registerShaders(RegisterShadersEvent event) {
      try {
         event.registerShader(new ShaderInstance(event.getResourceProvider(), id("cloak_surface_mask"), DefaultVertexFormat.NEW_ENTITY), shader -> {
            maskShader = shader;
            failed = false;
         });
         event.registerShader(new ShaderInstance(event.getResourceProvider(), id("cloak_surface_composite"), DefaultVertexFormat.POSITION_TEX), shader -> {
            compositeShader = shader;
            failed = false;
         });
      } catch (IOException | RuntimeException exception) {
         fail(exception);
      }
   }

   public static void beginLevel() {
      discardDeferred();
      levelCaptureOpen = true;
      deferredForIris = false;
      resetMask();
   }

   public static void finishLevel() {
      levelCaptureOpen = false;
      if (deferredForIris) {
         try {
            renderDeferred(DEFERRED_WORLD, true);
            renderDeferred(DEFERRED_HANDS, false);
            replayReticle();
         } finally {
            discardDeferred();
         }
      } else {
         composite(true);
      }
   }

   static boolean prepare(boolean hands) {
      if (failed || maskShader == null || compositeShader == null || !hands && !levelCaptureOpen) {
         return false;
      }
      if (NanoGeoShaderpackCompatibility.isShaderPackActive()) {
         // Iris renders its hands inside LevelRenderer, before NeoForge's AFTER_LEVEL event.
         // Keep their meshes until Iris has written the final scene to Minecraft's framebuffer.
         deferredForIris = levelCaptureOpen && NanoGeoShaderpackCompatibility.isIrisShaderPackActive();
         return deferredForIris;
      }
      return prepareTargets();
   }

   static boolean isDeferring() {
      return deferredForIris && levelCaptureOpen;
   }

   static void deferBatch(boolean hands, boolean reticle, RenderType type, MeshData mesh) {
      int vertices = mesh.drawState().vertexCount();
      if (DEFERRED_WORLD.size() + DEFERRED_HANDS.size() + DEFERRED_RETICLE.size() >= MAX_DEFERRED_BATCHES
         || vertices > MAX_DEFERRED_VERTICES - deferredVertices) {
         mesh.close();
         return;
      }
      var batches = reticle ? DEFERRED_RETICLE : hands ? DEFERRED_HANDS : DEFERRED_WORLD;
      batches.add(new DeferredBatch(type, mesh, new Matrix4f(RenderSystem.getProjectionMatrix()),
         new Matrix4f(RenderSystem.getModelViewMatrix()), RenderSystem.getVertexSorting()));
      deferredVertices += vertices;
   }

   static void discardDeferred() {
      DEFERRED_WORLD.forEach(batch -> batch.mesh.close());
      DEFERRED_HANDS.forEach(batch -> batch.mesh.close());
      DEFERRED_RETICLE.forEach(batch -> batch.mesh.close());
      DEFERRED_WORLD.clear();
      DEFERRED_HANDS.clear();
      DEFERRED_RETICLE.clear();
      deferredVertices = 0;
   }

   private static void renderDeferred(List<DeferredBatch> batches, boolean compareSceneDepth) {
      if (failed || batches.isEmpty() || !prepareTargets()) {
         return;
      }
      try {
         replay(batches);
      } catch (RuntimeException exception) {
         fail(exception);
      }
      composite(compareSceneDepth);
   }

   private static void replay(List<DeferredBatch> batches) {
      if (batches.isEmpty()) return;
      var originalProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
      var originalSorting = RenderSystem.getVertexSorting();
      var viewStack = RenderSystem.getModelViewStack();
      int originalDrawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
      viewStack.pushMatrix();
      try {
         for (DeferredBatch batch : batches) {
            RenderSystem.setProjectionMatrix(batch.projection, batch.sorting);
            viewStack.set(batch.modelView);
            RenderSystem.applyModelViewMatrix();
            batch.type.draw(batch.mesh);
         }
      } finally {
         GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, originalDrawFramebuffer);
         viewStack.popMatrix();
         RenderSystem.applyModelViewMatrix();
         RenderSystem.setProjectionMatrix(originalProjection, originalSorting);
      }
   }

   private static void replayReticle() {
      try {
         replay(DEFERRED_RETICLE);
      } catch (RuntimeException exception) {
         if (!warnedReticle) {
            warnedReticle = true;
            LogUtils.getLogger().warn("Could not render the TaCZ reticle during camouflage", exception);
         }
      }
   }

   private static boolean prepareTargets() {
      if (maskPrepared) {
         return true;
      }

      RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
      if (main == null || main.width < 1 || main.height < 1 || !main.useDepth) {
         return false;
      }

      int readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
      int drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
      try {
         if (mask == null || sceneCopy == null || mask.width != main.width || mask.height != main.height) {
            destroyTargets();
            mask = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
            sceneCopy = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
            sceneCopy.setFilterMode(GL11.GL_LINEAR);
         }
         GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, mask.frameBufferId);
         RenderSystem.colorMask(true, true, true, true);
         RenderSystem.depthMask(true);
         RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
         RenderSystem.clearDepth(1.0);
         RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
         maskPrepared = true;
         return true;
      } catch (RuntimeException exception) {
         fail(exception);
         return false;
      } finally {
         GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
         GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
      }
   }

   static RenderType maskType(ResourceLocation texture) {
      return MASK_TYPES.computeIfAbsent(texture, key -> RenderType.create(
         "crysismod_cloak_surface", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, false,
         RenderType.CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> maskShader))
            .setTextureState(new TextureStateShard(key, false, false))
            .setTransparencyState(NO_TRANSPARENCY)
            .setCullState(NO_CULL)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setWriteMaskState(COLOR_DEPTH_WRITE)
            .setOutputState(MASK_OUTPUT)
            .createCompositeState(false)
      ));
   }

   static RenderType reticleType(ResourceLocation texture) {
      return RETICLE_TYPES.computeIfAbsent(texture, key -> RenderType.create(
         "crysismod_cloak_reticle", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, false, false,
         RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
            .setTextureState(new TextureStateShard(key, false, false))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setCullState(NO_CULL)
            .setDepthTestState(NO_DEPTH_TEST)
            .setOverlayState(OVERLAY)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false)
      ));
   }

   static void finishHands() {
      if (!isDeferring()) {
         try {
            composite(false);
            replayReticle();
         } finally {
            discardDeferred();
         }
      }
   }

   private static void composite(boolean compareSceneDepth) {
      if (!hasGeometry || failed) {
         resetMask();
         return;
      }

      RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
      int readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
      int drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
      boolean hadDepth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
      boolean hadBlend = GL11.glIsEnabled(GL11.GL_BLEND);
      boolean wroteDepth = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
      try {
         bindCompositeTarget(main.getColorTextureId());
         GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.frameBufferId);
         GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, sceneCopy.frameBufferId);
         GL30.glBlitFramebuffer(0, 0, main.width, main.height, 0, 0, main.width, main.height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
         GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
         GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, compositeFramebuffer);

         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.disableBlend();
         compositeShader.setSampler("SceneSampler", sceneCopy.getColorTextureId());
         compositeShader.setSampler("SceneDepthSampler", main.getDepthTextureId());
         compositeShader.setSampler("MaskSampler", mask.getColorTextureId());
         compositeShader.setSampler("MaskDepthSampler", mask.getDepthTextureId());
         compositeShader.safeGetUniform("CompareDepth").set(compareSceneDepth ? 1 : 0);
         compositeShader.safeGetUniform("CloakTime").set((System.nanoTime() % 100_000_000_000L) / 1_000_000_000.0F);
         RenderSystem.setShader(() -> compositeShader);
         BufferBuilder vertices = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
         vertices.addVertex(-1.0F, -1.0F, 0.0F).setUv(0.0F, 0.0F);
         vertices.addVertex(1.0F, -1.0F, 0.0F).setUv(1.0F, 0.0F);
         vertices.addVertex(1.0F, 1.0F, 0.0F).setUv(1.0F, 1.0F);
         vertices.addVertex(-1.0F, 1.0F, 0.0F).setUv(0.0F, 1.0F);
         BufferUploader.drawWithShader(vertices.buildOrThrow());
      } catch (RuntimeException exception) {
         fail(exception);
      } finally {
         if (hadDepth) RenderSystem.enableDepthTest();
         else RenderSystem.disableDepthTest();
         if (hadBlend) RenderSystem.enableBlend();
         else RenderSystem.disableBlend();
         RenderSystem.depthMask(wroteDepth);
         GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
         GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
         resetMask();
      }
   }

   private static void bindCompositeTarget(int colorTexture) {
      if (compositeFramebuffer == 0) {
         compositeFramebuffer = GL30.glGenFramebuffers();
      }
      GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, compositeFramebuffer);
      if (compositeColorTexture != colorTexture) {
         // Use the main color attachment but leave depth unattached so the shader can sample it directly.
         // Iris, TaCZ and vanilla can choose different depth formats; no depth copy is required.
         GL30.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colorTexture, 0);
         if (GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Nanosuit cloak output framebuffer is incomplete");
         }
         compositeColorTexture = colorTexture;
      }
   }

   public static void close() {
      discardDeferred();
      destroyTargets();
      resetMask();
      levelCaptureOpen = false;
      deferredForIris = false;
   }

   private static void destroyTargets() {
      if (compositeFramebuffer != 0) {
         GL30.glDeleteFramebuffers(compositeFramebuffer);
         compositeFramebuffer = 0;
         compositeColorTexture = 0;
      }
      if (mask != null) {
         mask.destroyBuffers();
         mask = null;
      }
      if (sceneCopy != null) {
         sceneCopy.destroyBuffers();
         sceneCopy = null;
      }
   }

   private static void resetMask() {
      maskPrepared = false;
      hasGeometry = false;
   }

   private static void fail(Exception exception) {
      if (!failed) {
         LogUtils.getLogger().warn("Nanosuit cloak refraction unavailable; hiding camouflaged geometry", exception);
      }
      failed = true;
   }

   private static ResourceLocation id(String path) {
      return ResourceLocation.fromNamespaceAndPath("crysismod", path);
   }

   private record DeferredBatch(RenderType type, MeshData mesh, Matrix4f projection, Matrix4f modelView, VertexSorting sorting) {
   }
}
