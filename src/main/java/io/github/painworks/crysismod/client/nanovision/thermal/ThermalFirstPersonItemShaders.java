package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanovision.NanovisionActivationOverlay;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import java.io.IOException;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.slf4j.Logger;

public final class ThermalFirstPersonItemShaders {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation ENTITY_OVERLAY_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_first_person_item_entity");
   private static final ResourceLocation BLOCK_OVERLAY_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_first_person_item_block");
   private static volatile ShaderInstance entityOverlayShader;
   private static volatile ShaderInstance blockOverlayShader;

   private ThermalFirstPersonItemShaders() {
   }

   public static void register(RegisterShadersEvent event) {
      try {
         event.registerShader(
            new ShaderInstance(event.getResourceProvider(), ENTITY_OVERLAY_SHADER, DefaultVertexFormat.NEW_ENTITY), shader -> entityOverlayShader = shader
         );
      } catch (IOException var3) {
         entityOverlayShader = null;
         LOGGER.error("[CrysisMod][Nanovision] failed to register held-item entity overlay shader {}", ENTITY_OVERLAY_SHADER, var3);
      }

      try {
         event.registerShader(
            new ShaderInstance(event.getResourceProvider(), BLOCK_OVERLAY_SHADER, DefaultVertexFormat.BLOCK), shader -> blockOverlayShader = shader
         );
      } catch (IOException var2) {
         blockOverlayShader = null;
         LOGGER.error("[CrysisMod][Nanovision] failed to register held-item block overlay shader {}", BLOCK_OVERLAY_SHADER, var2);
      }
   }

   static ShaderInstance entityOverlayShader() {
      return entityOverlayShader;
   }

   static ShaderInstance blockOverlayShader() {
      return blockOverlayShader;
   }

   static boolean isAvailable() {
      return entityOverlayShader != null && blockOverlayShader != null;
   }

   static void prepareGeometryOverlay(RenderTarget destination) {
      if (destination != null) {
         prepare(entityOverlayShader, destination);
         prepare(blockOverlayShader, destination);
      }
   }

   static void prepareInlineGeometryOverlay(RenderTarget destination) {
      prepareGeometryOverlay(destination);
   }

   private static void prepare(ShaderInstance shader, RenderTarget destination) {
      if (shader != null) {
         prepareArm(shader, HumanoidArm.LEFT, "Left");
         prepareArm(shader, HumanoidArm.RIGHT, "Right");
         set(shader, "InSize", destination.width, destination.height);
         set(shader, "ContactHeatStrength", (float)NanovisionConfig.thermalHeldItemContactHeat());
         set(shader, "ModelOpacity", (float)NanovisionConfig.thermalMarkerModelOpacity());
         set(shader, "ScanlineStrength", (float)NanovisionConfig.thermalScanlineStrength());
         set(shader, "ScanlineSpacingPx", (float)NanovisionConfig.thermalScanlineSpacingPx());
         set(shader, "ActivationFlash", NanovisionActivationOverlay.thermalFlashIntensity());
      }
   }

   private static void prepareArm(ShaderInstance shader, HumanoidArm arm, String prefix) {
      RenderTarget snapshot = ThermalFirstPersonItemArmSnapshots.target(arm);
      boolean enabled = ThermalFirstPersonItemArmSnapshots.isValid(arm) && snapshot != null;
      RenderTarget fallback = ThermalFirstPersonArmRenderTarget.getOrCreate();
      RenderTarget selected = enabled ? snapshot : fallback;
      if (selected != null) {
         shader.setSampler(prefix + "ArmMaskSampler", selected.getColorTextureId());
         shader.setSampler(prefix + "ArmDepthSampler", selected.getDepthTextureId());
      }

      setInt(shader, prefix + "ArmEnabled", enabled ? 1 : 0);
   }

   private static void setInt(ShaderInstance shader, String name, int value) {
      Uniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
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
}
