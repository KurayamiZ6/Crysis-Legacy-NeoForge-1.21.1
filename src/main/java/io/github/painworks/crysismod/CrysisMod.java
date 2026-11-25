package io.github.painworks.crysismod;

import io.github.painworks.crysismod.client.CrysisModClientBootstrap;
import io.github.painworks.crysismod.client.minimap.config.NanoMinimapClientConfig;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoClientConfig;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.nanosuit.config.NanosuitArmorIdentityConfig;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.config.NanosuitServerConfig;
import io.github.painworks.crysismod.nanosuit.config.NanosuitRadialConfig;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitCloak;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorClientConfig;
import io.github.painworks.crysismod.registry.ModCreativeTabs;
import io.github.painworks.crysismod.registry.ModBlocks;
import io.github.painworks.crysismod.registry.ModDataComponents;
import io.github.painworks.crysismod.registry.ModItems;
import io.github.painworks.crysismod.registry.ModSounds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;

@Mod("crysismod")
public class CrysisMod {
   public static final String MODID = "crysismod";

   public CrysisMod(IEventBus modEventBus, ModContainer context) {
      NanosuitCapabilities.ATTACHMENT_TYPES.register(modEventBus);
      modEventBus.addListener(NanosuitNetwork::register);
      if (FMLEnvironment.dist == Dist.CLIENT) CrysisModClientBootstrap.init(modEventBus, context);
      ModItems.ITEMS.register(modEventBus);
      ModItems.ARMOR_MATERIALS.register(modEventBus);
      ModDataComponents.TYPES.register(modEventBus);
      ModBlocks.BLOCKS.register(modEventBus);
      ModBlocks.BLOCK_ENTITIES.register(modEventBus);
      ModBlocks.MENUS.register(modEventBus);
      ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
      ModSounds.SOUND_EVENTS.register(modEventBus);
      context.registerConfig(Type.COMMON, NanosuitClientConfig.SPEC, "nanosuit-client.toml");
      context.registerConfig(Type.COMMON, NanoVisorClientConfig.SPEC, "nanovisor-client.toml");
      context.registerConfig(Type.COMMON, NanovisionConfig.SPEC, "nanovision-client.toml");
      context.registerConfig(Type.COMMON, NanoMinimapClientConfig.SPEC, "nano-minimap-client.toml");
      context.registerConfig(Type.COMMON, NanoGeoClientConfig.SPEC, "nanogeo-client.toml");
      context.registerConfig(Type.COMMON, NanosuitArmorIdentityConfig.SPEC, "nanosuit-armor-identity.toml");
      context.registerConfig(Type.SERVER, NanosuitServerConfig.SPEC, "nanosuit-server.toml");
      context.registerConfig(Type.CLIENT, NanosuitRadialConfig.SPEC, "nanosuit1-radial-client.toml");
      NeoForge.EVENT_BUS.register(new NanosuitPlayerHandler());
      NeoForge.EVENT_BUS.register(new NanosuitCloak());
   }

}
