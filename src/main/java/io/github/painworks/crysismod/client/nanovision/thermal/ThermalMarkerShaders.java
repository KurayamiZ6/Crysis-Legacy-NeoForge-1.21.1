package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import java.io.IOException;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.slf4j.Logger;

public final class ThermalMarkerShaders {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation MARKER_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_marker_mask");
   private static final ResourceLocation SOLID_MARKER_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_marker_solid");
   private static final ResourceLocation BLOCK_MARKER_SHADER = ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_block_marker");
   private static volatile ShaderInstance markerShader;
   private static volatile ShaderInstance solidMarkerShader;
   private static volatile ShaderInstance blockMarkerShader;
   private static boolean warnedFallback;
   private static boolean warnedSolidFallback;
   private static boolean warnedBlockFallback;

   private ThermalMarkerShaders() {
   }

   public static void register(RegisterShadersEvent event) {
      try {
         event.registerShader(new ShaderInstance(event.getResourceProvider(), MARKER_SHADER, DefaultVertexFormat.NEW_ENTITY), shader -> {
            markerShader = shader;
            warnedFallback = false;
            if (NanovisionConfig.nanovisionDebug()) {
               LOGGER.info("[CrysisMod][Nanovision] registered marker shader {}", MARKER_SHADER);
            }
         });
         event.registerShader(new ShaderInstance(event.getResourceProvider(), SOLID_MARKER_SHADER, DefaultVertexFormat.NEW_ENTITY), shader -> {
            solidMarkerShader = shader;
            warnedSolidFallback = false;
            if (NanovisionConfig.nanovisionDebug()) {
               LOGGER.info("[CrysisMod][Nanovision] registered solid marker shader {}", SOLID_MARKER_SHADER);
            }
         });
         event.registerShader(new ShaderInstance(event.getResourceProvider(), BLOCK_MARKER_SHADER, DefaultVertexFormat.POSITION_COLOR), shader -> {
            blockMarkerShader = shader;
            warnedBlockFallback = false;
            if (NanovisionConfig.nanovisionDebug()) {
               LOGGER.info("[CrysisMod][Nanovision] registered block marker shader {}", BLOCK_MARKER_SHADER);
            }
         });
      } catch (IOException var2) {
         markerShader = null;
         solidMarkerShader = null;
         blockMarkerShader = null;
         LOGGER.error("[CrysisMod][Nanovision] failed to register marker shader {}", MARKER_SHADER, var2);
      }
   }

   public static ShaderInstance markerShader() {
      if (markerShader == null) {
         if (!warnedFallback) {
            warnedFallback = true;
            LOGGER.warn("[CrysisMod][Nanovision] marker shader unavailable, using vanilla fallback");
         }

         return GameRenderer.getRendertypeEntityCutoutNoCullShader();
      } else {
         return markerShader;
      }
   }

   public static ShaderInstance solidMarkerShader() {
      if (solidMarkerShader == null) {
         if (!warnedSolidFallback) {
            warnedSolidFallback = true;
            LOGGER.warn("[CrysisMod][Nanovision] solid marker shader unavailable, using textured marker fallback");
         }

         return markerShader();
      } else {
         return solidMarkerShader;
      }
   }

   public static ShaderInstance blockMarkerShader() {
      if (blockMarkerShader == null) {
         if (!warnedBlockFallback) {
            warnedBlockFallback = true;
            LOGGER.warn("[CrysisMod][Nanovision] block marker shader unavailable, using position-color fallback");
         }

         return GameRenderer.getPositionColorShader();
      } else {
         return blockMarkerShader;
      }
   }
}
