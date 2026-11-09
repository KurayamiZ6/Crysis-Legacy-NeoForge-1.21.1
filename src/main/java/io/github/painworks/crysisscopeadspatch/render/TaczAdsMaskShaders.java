package io.github.painworks.crysisscopeadspatch.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.CrysisMod;
import java.io.IOException;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.slf4j.Logger;

@EventBusSubscriber(modid = CrysisMod.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public final class TaczAdsMaskShaders {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation MASK_REPLAY_SHADER = ResourceLocation.fromNamespaceAndPath("crysisscopefix", "ads_stencil_replay");
   private static volatile ShaderInstance maskReplayShader;

   private TaczAdsMaskShaders() {
   }

   @SubscribeEvent
   public static void register(RegisterShadersEvent event) {
      try {
         event.registerShader(
            new ShaderInstance(event.getResourceProvider(), MASK_REPLAY_SHADER, DefaultVertexFormat.POSITION_TEX), shader -> maskReplayShader = shader
         );
      } catch (IOException var2) {
         maskReplayShader = null;
         LOGGER.error("[CrysisScopeFix] failed to register ADS stencil replay shader", var2);
      }
   }

   static ShaderInstance maskReplayShader() {
      return maskReplayShader;
   }
}
