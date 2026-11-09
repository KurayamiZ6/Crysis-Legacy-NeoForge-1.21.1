package io.github.painworks.crysisscopeadspatch.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysisscopeadspatch.mixin.ThermalItemShadersInvoker;
import java.io.IOException;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.slf4j.Logger;

@EventBusSubscriber(modid = "crysisscopefix", bus = Bus.MOD, value = Dist.CLIENT)
public final class OptimizedTaczShaders {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation SHADER = ResourceLocation.fromNamespaceAndPath("crysisscopefix", "optimized_tacz_item");
   private static volatile ShaderInstance shader;

   private OptimizedTaczShaders() {
   }

   @SubscribeEvent
   public static void register(RegisterShadersEvent event) {
      try {
         event.registerShader(new ShaderInstance(event.getResourceProvider(), SHADER, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL), loaded -> shader = loaded);
      } catch (IOException var2) {
         shader = null;
         LOGGER.error("[CrysisScopeFix] compact TACZ shader registration failed; legacy capture remains available", var2);
      }
   }

   public static ShaderInstance shader() {
      return shader;
   }

   public static boolean isAvailable() {
      return shader != null;
   }

   public static void prepare(RenderTarget target) {
      ShaderInstance current = shader;
      if (current != null && target != null) {
         ThermalItemShadersInvoker.crysisscopefix$prepare(current, target);
      }
   }
}
