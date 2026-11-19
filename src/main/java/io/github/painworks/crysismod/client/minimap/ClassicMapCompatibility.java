package io.github.painworks.crysismod.client.minimap;

import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

/** Only the separately registered minimap layer of the inspected Diorama release. */
@EventBusSubscriber(modid="crysismod",value=Dist.CLIENT)
public final class ClassicMapCompatibility {
   private ClassicMapCompatibility(){}
   @SubscribeEvent
   public static void beforeLayer(RenderGuiLayerEvent.Pre event) {
      if (!event.getName().toString().equals("diorama:minimap") || !NanosuitClientConfig.classicHideDiorama()) return;
      boolean supported = ModList.get().getModContainerById("diorama")
         .map(c -> c.getModInfo().getVersion().toString().startsWith("0.10.2+")).orElse(false);
      if (supported && NanoMinimapManager.get().classicTerrainReady()) event.setCanceled(true);
   }
}
