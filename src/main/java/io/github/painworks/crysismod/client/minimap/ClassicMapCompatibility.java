package io.github.painworks.crysismod.client.minimap;

import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

/** Suppresses only the display of the competing minimap while a complete suit is active. */
@EventBusSubscriber(modid="crysismod",value=Dist.CLIENT)
public final class ClassicMapCompatibility {
   private static final ResourceLocation DIORAMA_MINIMAP = ResourceLocation.fromNamespaceAndPath("diorama", "minimap");

   private ClassicMapCompatibility(){}

   public static boolean hidesCompetingMinimap() {
      Minecraft minecraft = Minecraft.getInstance();
      return NanosuitClientConfig.hideCompetingMinimaps()
         && NanosuitArmorUtil.getUsableProfile(minecraft.player) != null
         && NanoMinimapVisibility.shouldRender(minecraft);
   }

   @SubscribeEvent
   public static void beforeLayer(RenderGuiLayerEvent.Pre event) {
      if (DIORAMA_MINIMAP.equals(event.getName()) && ModList.get().isLoaded("diorama")
         && (!ClassicMinimapRenderer.isClassic() || NanosuitClientConfig.classicHideDiorama())
         && hidesCompetingMinimap()) event.setCanceled(true);
   }
}
