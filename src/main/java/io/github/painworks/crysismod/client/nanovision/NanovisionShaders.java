package io.github.painworks.crysismod.client.nanovision;

import io.github.painworks.crysismod.client.nanovision.preview.NanovisionSettingsPreviewShaders;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmOcclusionDepth;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmShaderpackShaders;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmShaders;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemShaders;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerShaders;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

public final class NanovisionShaders {
   public static final ResourceLocation CLASSIC_POST_CHAIN = ResourceLocation.fromNamespaceAndPath("crysismod", "shaders/post/nanovision_post.json");
   public static final ResourceLocation CLASSIC_IMPROVED_POST_CHAIN = ResourceLocation.fromNamespaceAndPath("crysismod", "shaders/post/nanovision_classic_improved.json");
   public static final ResourceLocation CLASSIC_IMPROVED_V2_POST_CHAIN = ResourceLocation.fromNamespaceAndPath("crysismod", "shaders/post/nanovision_classic_improved_v2.json");
   public static final ResourceLocation CLASSIC_IMPROVED_V3_POST_CHAIN = ResourceLocation.fromNamespaceAndPath("crysismod", "shaders/post/nanovision_classic_improved_v3.json");
   public static final ResourceLocation CINEMATIC_POST_CHAIN = ResourceLocation.fromNamespaceAndPath("crysismod", "shaders/post/nanovision_cinematic.json");

   private NanovisionShaders() {
   }

   public static void registerShaders(RegisterShadersEvent event) {
      ThermalMarkerShaders.register(event);
      ThermalFirstPersonArmShaders.register(event);
      ThermalFirstPersonArmShaderpackShaders.register(event);
      ThermalFirstPersonArmOcclusionDepth.register(event);
      ThermalFirstPersonItemShaders.register(event);
      NanovisionSettingsPreviewShaders.register(event);
   }
}
