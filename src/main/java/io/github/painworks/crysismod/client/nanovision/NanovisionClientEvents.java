package io.github.painworks.crysismod.client.nanovision;

import io.github.painworks.crysismod.nanosuit.client.render.NanosuitCloakRefraction;

import io.github.painworks.crysismod.client.nanovision.debug.NanovisionPerformanceProfiler;
import io.github.painworks.crysismod.client.nanovision.preview.NanovisionSettingsPreviewRenderer;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmComposite;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmDeferredBuffer;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmOcclusionDepth;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmRenderTarget;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemArmSnapshots;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemGlowCapture;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonScopeGlowMask;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerBufferSource;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerCompatibilityRenderer;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerRenderLayer;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerRenderTarget;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerTextureAtlas;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalShaderOffGeometryHalo;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalShaderOffItemOutlineCapture;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalSheepWoolMarkerLayer;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalSlimeOuterMarkerLayer;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalVillagerClothingMarkerLayer;
import io.github.painworks.crysismod.client.nanovision.thermal.block.ThermalHotBlockRenderer;
import io.github.painworks.crysismod.client.nanovision.thermal.block.ThermalHotBlockScanner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Pre;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;

public final class NanovisionClientEvents {
   private static final NanovisionClientEvents INSTANCE = new NanovisionClientEvents();
   private static boolean initialized;
   private final NanovisionPostProcessor postProcessor = new NanovisionPostProcessor();

   private NanovisionClientEvents() {
   }

   public static void init(IEventBus modEventBus) {
      if (!initialized) {
         modEventBus.addListener(NanovisionClientEvents::onRegisterShaders);
         modEventBus.addListener(NanovisionClientEvents::onRegisterReloadListeners);
         modEventBus.addListener(NanovisionClientEvents::onAddLayers);
         NeoForge.EVENT_BUS.register(INSTANCE);
         initialized = true;
      }
   }

   private static void onRegisterShaders(RegisterShadersEvent event) {
      NanosuitCloakRefraction.registerShaders(event);
      NanovisionShaders.registerShaders(event);
      ThermalFirstPersonItemGlowCapture.registerShaders(event);
      ThermalFirstPersonScopeGlowMask.registerShaders(event);
      ThermalShaderOffGeometryHalo.registerShaders(event);
      ThermalShaderOffItemOutlineCapture.registerShaders(event);
   }

   private static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
      event.registerReloadListener(ThermalMarkerTextureAtlas.INSTANCE);
   }

   private static void onAddLayers(AddLayers event) {
      for (EntityType<?> type : event.getEntityTypes()) {
         try {
            if ((Object)event.getRenderer((EntityType)type) instanceof LivingEntityRenderer renderer) {
               renderer.addLayer(new ThermalMarkerRenderLayer(renderer, renderer));
            }
         } catch (IllegalArgumentException | ClassCastException var6) {
         }
      }

      for (PlayerSkin.Model skin : event.getSkins()) {
         try {
            EntityRenderer<?> skinRenderer = event.getSkin(skin);
            if (skinRenderer instanceof LivingEntityRenderer<?, ?> renderer) {
               addPlayerLayer(renderer);
            }
         } catch (ClassCastException var5) {
         }
      }

      addVanillaOuterThermalLayers(event);
   }

   private static void addPlayerLayer(LivingEntityRenderer<?, ?> renderer) {
      renderer.addLayer(new ThermalMarkerRenderLayer(renderer, renderer));
   }

   private static void addVanillaOuterThermalLayers(AddLayers event) {
      LivingEntityRenderer<Sheep, SheepModel<Sheep>> sheepRenderer = event.getRenderer(EntityType.SHEEP);
      if (sheepRenderer != null) {
         sheepRenderer.addLayer(new ThermalSheepWoolMarkerLayer(sheepRenderer, event.getEntityModels()));
      }

      LivingEntityRenderer<Slime, SlimeModel<Slime>> slimeRenderer = event.getRenderer(EntityType.SLIME);
      if (slimeRenderer != null) {
         slimeRenderer.addLayer(new ThermalSlimeOuterMarkerLayer(slimeRenderer, event.getEntityModels()));
      }

      LivingEntityRenderer<Villager, VillagerModel<Villager>> villagerRenderer = event.getRenderer(EntityType.VILLAGER);
      if (villagerRenderer != null) {
         villagerRenderer.addLayer(new ThermalVillagerClothingMarkerLayer(villagerRenderer));
      }
   }

   @SubscribeEvent
   public void onRenderLevelStage(RenderLevelStageEvent event) {
      Minecraft minecraft = Minecraft.getInstance();
      if (event.getStage() == Stage.AFTER_SKY) {
         NanosuitCloakRefraction.beginLevel();
         ThermalFirstPersonItemCapture.beginFrame();
         ThermalShaderOffGeometryHalo.beginFrame();
         ThermalShaderOffItemOutlineCapture.beginFrame();
         ThermalFirstPersonItemArmSnapshots.beginFrame();
         ThermalFirstPersonArmComposite.beginFrame();
         ThermalFirstPersonArmDeferredBuffer.beginFrame();
         ThermalFirstPersonArmOcclusionDepth.beginFrame();
         ThermalMarkerRenderTarget.prepareFrame(minecraft);
         ThermalMarkerCompatibilityRenderer.prepareFrame(minecraft);
      } else if (event.getStage() == Stage.AFTER_ENTITIES) {
         ThermalMarkerBufferSource.flush();
         ThermalMarkerCompatibilityRenderer.render(minecraft, event.getPoseStack(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
         ThermalHotBlockRenderer.render(minecraft, event.getPoseStack());
      } else {
         if (event.getStage() == Stage.AFTER_LEVEL) {
            NanosuitCloakRefraction.finishLevel();
            ThermalFirstPersonArmOcclusionDepth.captureAfterLevel(minecraft);
            this.postProcessor.renderAfterLevel(minecraft, event.getPartialTick().getGameTimeDeltaPartialTick(false));
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
   public void onRenderGui(Pre event) {
      ThermalFirstPersonArmComposite.renderPendingShaderpackComposite();
      long itemDeferredCpuStarted = NanovisionPerformanceProfiler.beginCpu();
      int itemDeferredGpuQuery = NanovisionPerformanceProfiler.beginGpu(NanovisionPerformanceProfiler.Section.FIRST_PERSON_ITEM_DEFERRED_GPU);

      try {
         ThermalFirstPersonItemCapture.flushDeferred();
      } finally {
         NanovisionPerformanceProfiler.endGpu(NanovisionPerformanceProfiler.Section.FIRST_PERSON_ITEM_DEFERRED_GPU, itemDeferredGpuQuery);
         NanovisionPerformanceProfiler.endCpu(NanovisionPerformanceProfiler.Section.FIRST_PERSON_ITEM_DEFERRED, itemDeferredCpuStarted);
      }

      ThermalShaderOffItemOutlineCapture.flush();
      NanovisionPerformanceProfiler.endFirstPersonFrame();
   }

   @SubscribeEvent
   public void onClientTick(ClientTickEvent.Post event) {
      ThermalHotBlockScanner.tick(Minecraft.getInstance());
   }

   @SubscribeEvent
   public void onClientLogout(LoggingOut event) {
      NanosuitCloakRefraction.close();
      NanovisionController.reset();
      NanovisionState.clearClientEffects(Minecraft.getInstance().player);
      ThermalHotBlockRenderer.clear();
      ThermalMarkerCompatibilityRenderer.clear();
      ThermalMarkerRenderTarget.close();
      ThermalFirstPersonArmRenderTarget.close();
      ThermalFirstPersonArmComposite.beginFrame();
      ThermalFirstPersonArmDeferredBuffer.discardDeferred();
      ThermalFirstPersonArmOcclusionDepth.close();
      ThermalFirstPersonItemCapture.discardDeferred();
      ThermalFirstPersonItemGlowCapture.close();
      ThermalShaderOffItemOutlineCapture.close();
      ThermalFirstPersonScopeGlowMask.close();
      ThermalFirstPersonItemArmSnapshots.close();
      NanovisionSettingsPreviewRenderer.close();
      this.postProcessor.close();
   }
}
