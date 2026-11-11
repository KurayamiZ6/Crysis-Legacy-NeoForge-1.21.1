package io.github.painworks.crysismod.client.nanogeo;

import io.github.painworks.crysismod.nanosuit.NanosuitPresentation;
import io.github.painworks.crysismod.registry.ModItems;
import java.util.List;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;

public final class NanoGeoClientHooks {
   private static boolean initialized;

   private NanoGeoClientHooks() {
   }

   public static void init(IEventBus modEventBus) {
      if (!initialized) {
         modEventBus.addListener(NanoGeoClientHooks::onRegisterClientReloadListeners);
         modEventBus.addListener(NanoGeoClientHooks::onRegisterShaders);
         modEventBus.addListener(NanoGeoClientHooks::onAddLayers);
         modEventBus.addListener(NanoGeoClientHooks::onRegisterClientExtensions);
         NeoForge.EVENT_BUS.register(new NanoGeoDebugRenderHandler());
         NeoForge.EVENT_BUS.register(new NanoGeoArmorModeCompatPrewarmHandler());
         initialized = true;
      }
   }

   private static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
      NanoGeoBakedModelCache.INSTANCE.setPreloadModels(List.of(NanosuitPresentation.LEGACY_CURRENT.armorModel()));
      event.registerReloadListener(NanoGeoBakedModelCache.INSTANCE);
      event.registerReloadListener(NanoGeoArmorModePulseCenters.INSTANCE);
   }

   private static void onRegisterShaders(RegisterShadersEvent event) {
      NanoGeoArmorModeXShimmerShader.register(event);
   }

   private static void onAddLayers(AddLayers event) {
      for (PlayerSkin.Model skinModel : event.getSkins()) {
         PlayerRenderer playerRenderer = event.getSkin(skinModel);
         if (playerRenderer != null) {
            playerRenderer.addLayer(new NanoGeoArmorModeOverlayLayer(playerRenderer));
            playerRenderer.addLayer(new NanoGeoThermalArmorMarkerLayer(playerRenderer));
         }
      }
   }

   private static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
      event.registerItem(NanoGeoArmorClientExtensions.create(), ModItems.NANOGEO_NANOSUIT_HELMET);
      event.registerItem(NanoGeoArmorClientExtensions.create(), ModItems.NANOGEO_NANOSUIT_CHESTPLATE);
      event.registerItem(NanoGeoArmorClientExtensions.create(), ModItems.NANOGEO_NANOSUIT_LEGGINGS);
      event.registerItem(NanoGeoArmorClientExtensions.create(), ModItems.NANOGEO_NANOSUIT_BOOTS);
      event.registerItem(NanoGeoArmorClientExtensions.create(), ModItems.NANOSUIT_2_HELMET);
      event.registerItem(NanoGeoArmorClientExtensions.create(), ModItems.NANOSUIT_2_CHESTPLATE);
      event.registerItem(NanoGeoArmorClientExtensions.create(), ModItems.NANOSUIT_2_LEGGINGS);
      event.registerItem(NanoGeoArmorClientExtensions.create(), ModItems.NANOSUIT_2_BOOTS);
   }
}
