package io.github.painworks.crysismod.client;

import io.github.painworks.crysisscopeadspatch.config.LegacySpectrumConfigScreen;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoClientHooks;
import io.github.painworks.crysismod.client.nanovision.NanovisionClientEvents;
import io.github.painworks.crysismod.client.nanovision.NanovisionKeybinds;
import io.github.painworks.crysismod.nanosuit.client.NanosuitKeyMappings;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitArmRenderHandler;
import io.github.painworks.crysismod.nanovisor.client.NanoVisorKeyMappings;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class CrysisModClientBootstrap {
   private static boolean initialized;

   private CrysisModClientBootstrap() {
   }

   public static void init(IEventBus modEventBus, ModContainer context) {
      if (!initialized) {
         context.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> new io.github.painworks.crysismod.nanosuit.client.CrysisSettingsScreen(parent));
         modEventBus.addListener(NanosuitKeyMappings::register);
         modEventBus.addListener(NanoVisorKeyMappings::register);
         modEventBus.addListener(NanosuitCradleRenderer::register);
         modEventBus.addListener(CradleControllerScreen::register);
         modEventBus.addListener(NanovisionKeybinds::register);
         NanovisionClientEvents.init(modEventBus);
         NanoGeoClientHooks.init(modEventBus);
         NeoForge.EVENT_BUS.register(new NanosuitArmRenderHandler());
         initialized = true;
      }
   }
}
