package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.SequencedMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.RenderStateShard.OutputStateShard;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.slf4j.Logger;

public final class ThermalShaderOffItemOutlineCapture extends RenderStateShard {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int BUFFER_SIZE = 32768;
   private static final ResourceLocation COMPOSITE_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_shader_off_item_outline_composite");
   private static final ResourceLocation ENTITY_MASK_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_first_person_item_glow_mask_entity");
   private static final ResourceLocation BLOCK_MASK_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_first_person_item_glow_mask_block");
   private static final SequencedMap<RenderType, ByteBufferBuilder> BUFFERS = new LinkedHashMap<>();
   private static final ByteBufferBuilder FALLBACK = new ByteBufferBuilder(32768);
   private static ThermalDeferredBufferSource source = createSource();
   private static RenderTarget target;
   private static ThermalFramebufferState outputPreviousState;
   private static ShaderInstance compositeShader;
   private static ShaderInstance entityMaskShader;
   private static ShaderInstance blockMaskShader;
   private static boolean pending;
   private static boolean loggedCapture;
   private static boolean loggedComposite;
   private static boolean warned;
   private static Matrix4f projection;
   private static Matrix4f modelView;
   public static final OutputStateShard OUTPUT = new OutputStateShard(
      "nanovision_shader_off_item_outline_target", ThermalShaderOffItemOutlineCapture::bindOutput, ThermalShaderOffItemOutlineCapture::restoreOutput
   );

   private ThermalShaderOffItemOutlineCapture() {
      super("nanovision_shader_off_item_outline_capture", () -> {}, () -> {});
   }

   public static void registerShaders(RegisterShadersEvent event) {
      try {
         event.registerShader(
            new ShaderInstance(event.getResourceProvider(), COMPOSITE_SHADER, DefaultVertexFormat.POSITION_TEX), shader -> compositeShader = shader
         );
         event.registerShader(
            new ShaderInstance(event.getResourceProvider(), ENTITY_MASK_SHADER, DefaultVertexFormat.NEW_ENTITY), shader -> entityMaskShader = shader
         );
         event.registerShader(new ShaderInstance(event.getResourceProvider(), BLOCK_MASK_SHADER, DefaultVertexFormat.BLOCK), shader -> blockMaskShader = shader);
      } catch (IOException var2) {
         compositeShader = null;
         entityMaskShader = null;
         blockMaskShader = null;
         LOGGER.error("[CrysisMod][Nanovision] failed to register shader-off item outline shaders", var2);
      }
   }

   public static void beginFrame() {
      RenderSystem.assertOnRenderThread();
      resetBuffers();
   }

   public static VertexConsumer capture(RenderType original) {
      if (isEnabled() && original != null) {
         RenderType maskType = ThermalShaderOffItemOutlineRenderTypes.capture(original);
         if (maskType == null) {
            return null;
         } else {
            BUFFERS.computeIfAbsent(maskType, ignored -> new ByteBufferBuilder(Math.max(32768, maskType.bufferSize())));
            if (!pending) {
               projection = new Matrix4f(RenderSystem.getProjectionMatrix());
               modelView = new Matrix4f(RenderSystem.getModelViewMatrix());
            }

            pending = true;
            if (!loggedCapture) {
               loggedCapture = true;
               LOGGER.info("[CrysisMod][Nanovision][ShaderOffOutline] retained complete held-item geometry in an owned silhouette target");
            }

            return source.getBuffer(maskType);
         }
      } else {
         return null;
      }
   }

   public static void flush() {
      RenderSystem.assertOnRenderThread();
      if (pending && projection != null && modelView != null && source.hasPendingBatches()) {
         ThermalFramebufferState caller = ThermalFramebufferState.capture();
         RenderSystem.backupProjectionMatrix();
         Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
         modelViewStack.pushMatrix();

         try {
            if (caller.restoreAndValidateDrawTarget() && ensureTarget(caller.viewportWidth(), caller.viewportHeight())) {
               target.bindWrite(false);
               RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
               RenderSystem.clear(16640, Minecraft.ON_OSX);
               caller.restore();
               RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);
               modelViewStack.set(modelView);
               RenderSystem.applyModelViewMatrix();
               prepareMaskShaders(caller.viewportWidth(), caller.viewportHeight());
               source.endBatch();
               composite(caller);
               if (!loggedComposite) {
                  loggedComposite = true;
                  LOGGER.info("[CrysisMod][Nanovision][ShaderOffOutline] composited complete held-item exterior before HUD");
               }

               return;
            }
         } catch (Throwable var6) {
            if (!warned) {
               warned = true;
               LOGGER.warn("[CrysisMod][Nanovision] shader-off held-item outline failed; base spectrum kept", var6);
            }

            return;
         } finally {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
            caller.restore();
            resetBuffers();
         }
      } else {
         resetBuffers();
      }
   }

   public static void close() {
      resetBuffers();
      if (target != null) {
         target.destroyBuffers();
      }

      target = null;
      outputPreviousState = null;
   }

   static ShaderInstance entityMaskShader() {
      return entityMaskShader;
   }

   static ShaderInstance blockMaskShader() {
      return blockMaskShader;
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
         && compositeShader != null
         && entityMaskShader != null
         && blockMaskShader != null;
   }

   private static boolean ensureTarget(int width, int height) {
      width = Math.max(1, width);
      height = Math.max(1, height);
      if (target != null && target.width == width && target.height == height) {
         return true;
      } else {
         if (target != null) {
            target.destroyBuffers();
         }

         target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
         target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         target.setFilterMode(9729);
         return true;
      }
   }

   private static void bindOutput() {
      if (target != null) {
         outputPreviousState = ThermalFramebufferState.capture();
         target.bindWrite(false);
      }
   }

   private static void restoreOutput() {
      if (outputPreviousState != null) {
         outputPreviousState.restore();
         outputPreviousState = null;
      }
   }

   private static void composite(ThermalFramebufferState caller) {
      if (target != null && compositeShader != null && caller.restoreAndValidateDrawTarget()) {
         compositeShader.setSampler("MaskSampler", target.getColorTextureId());
         set(compositeShader, "InSize", target.width, target.height);
         float radius = (float)Math.max(2.0, Math.min(Math.min(target.width, target.height) * NanovisionConfig.thermalHeldItemOuterGlowRatio() * 0.18, 24.0));
         set(compositeShader, "GlowRadiusPx", radius);
         set(compositeShader, "GlowStrength", (float)NanovisionConfig.thermalHeldItemOuterGlowStrength());
         int width = caller.viewportWidth();
         int height = caller.viewportHeight();
         RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, width, height, 0.0F, 1000.0F, 3000.0F), VertexSorting.ORTHOGRAPHIC_Z);
         Matrix4fStack stack = RenderSystem.getModelViewStack();
         stack.identity();
         stack.translate(0.0F, 0.0F, -2000.0F);
         RenderSystem.applyModelViewMatrix();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShader(() -> compositeShader);
         BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
         buffer.addVertex(0.0F, height, 0.0F).setUv(0.0F, 0.0F);
         buffer.addVertex(width, height, 0.0F).setUv(1.0F, 0.0F);
         buffer.addVertex(width, 0.0F, 0.0F).setUv(1.0F, 1.0F);
         buffer.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, 1.0F);
         BufferUploader.drawWithShader(buffer.buildOrThrow());
         compositeShader.clear();
      }
   }

   private static void prepareMaskShaders(int width, int height) {
      prepareMaskShader(entityMaskShader, width, height);
      prepareMaskShader(blockMaskShader, width, height);
   }

   private static void prepareMaskShader(ShaderInstance shader, int width, int height) {
      if (shader != null) {
         prepareArmMask(shader, HumanoidArm.LEFT, "Left");
         prepareArmMask(shader, HumanoidArm.RIGHT, "Right");
         set(shader, "InSize", width, height);
      }
   }

   private static void prepareArmMask(ShaderInstance shader, HumanoidArm arm, String prefix) {
      RenderTarget armTarget = ThermalFirstPersonItemArmSnapshots.target(arm);
      boolean valid = ThermalFirstPersonItemArmSnapshots.isValid(arm) && armTarget != null;
      RenderTarget fallback = Minecraft.getInstance().getMainRenderTarget();
      RenderTarget sampler = valid ? armTarget : fallback;
      if (sampler != null) {
         shader.setSampler(prefix + "ArmMaskSampler", sampler.getColorTextureId());
         shader.setSampler(prefix + "ArmDepthSampler", sampler.getDepthTextureId());
      }

      setInt(shader, prefix + "ArmEnabled", valid ? 1 : 0);
   }

   private static void resetBuffers() {
      for (ByteBufferBuilder buffer : BUFFERS.values()) {
         buffer.discard();
      }

      FALLBACK.discard();
      source = createSource();
      pending = false;
      projection = null;
      modelView = null;
   }

   private static ThermalDeferredBufferSource createSource() {
      return new ThermalDeferredBufferSource(BUFFERS, FALLBACK);
   }

   private static void set(ShaderInstance shader, String name, float value) {
      Uniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }

   private static void set(ShaderInstance shader, String name, float x, float y) {
      Uniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(x, y);
      }
   }

   private static void setInt(ShaderInstance shader, String name, int value) {
      Uniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }
}
