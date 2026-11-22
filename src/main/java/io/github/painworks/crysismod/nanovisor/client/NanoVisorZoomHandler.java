package io.github.painworks.crysismod.nanovisor.client;

import io.github.painworks.crysismod.nanovisor.config.NanoVisorSettings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanoVisorZoomHandler {
   private static final float MIN_ZOOM_MULTIPLIER = 0.05F;
   private static final float MAX_ZOOM_MULTIPLIER = 2.0F;
   private static final float MIN_SMOOTHNESS = 0.0F;
   private static final float MAX_SMOOTHNESS = 1.0F;
   private static final float EPSILON = 1.0E-4F;
   private static float currentZoomMultiplier = 1.0F;
   private static boolean zoomInputActive;
   private static Boolean scrollTarget;
   private static Boolean rightClickTarget;
   private static long zoomStarted;
   private static float zoomFrom=1,zoomDestination=1;
   private static double lastWorldFov=70;

   @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST)
   public static void rightClick(net.neoforged.neoforge.client.event.InputEvent.MouseButton.Pre event) {
      var mc=Minecraft.getInstance();
      if(event.getButton()!=org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT || mc.screen!=null || mc.getOverlay()!=null
         || !io.github.painworks.crysismod.nanosuit.client.Nanosuit2Hud.equipped() || !NanoVisorState.isVisorActive()) return;
      event.setCanceled(true);
      mc.options.keyUse.setDown(false);
      while(mc.options.keyUse.consumeClick()) {}
      if(event.getAction()==org.lwjgl.glfw.GLFW.GLFW_PRESS && NanoVisorSettings.Zoom.enabled())
         rightClickTarget=!(rightClickTarget==null?zoomInputActive:rightClickTarget);
   }

   private NanoVisorZoomHandler() {
   }

   public static void reset() {
      currentZoomMultiplier=1;zoomInputActive=false;scrollTarget=null;rightClickTarget=null;zoomFrom=zoomDestination=1;
   }

   @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST)
   public static void scroll(net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent event) {
      var mc=Minecraft.getInstance();
      if(mc.screen==null && mc.getOverlay()==null && !ClassicBinoculars.isN1Context()
         && !io.github.painworks.crysismod.nanosuit.client.Nanosuit2Hud.equipped()
         && NanoVisorState.canUse(mc) && NanoVisorState.isVisorActive() && NanoVisorSettings.Zoom.enabled()
         && event.getScrollDeltaY()!=0) {
         scrollTarget=event.getScrollDeltaY()>0;
         event.setCanceled(true); // Includes scrolling farther at a zoom limit: never select a hotbar slot.
      }
   }

   @SubscribeEvent
   public static void onComputeFovModifier(ComputeFovModifierEvent event) {
      Minecraft minecraft = Minecraft.getInstance();
      if (ClassicBinoculars.isN1Context()) { currentZoomMultiplier=1; return; }
      if (minecraft.player == null || !NanoVisorState.canUse(minecraft)) {
         currentZoomMultiplier = 1.0F;
         zoomInputActive = false;
         return;
      } else {
         if(io.github.painworks.crysismod.nanosuit.client.Nanosuit2Hud.equipped()) return;
         float targetMultiplier = resolveTargetZoomMultiplier();
         float smoothness = (float)Mth.clamp(NanoVisorSettings.Zoom.smoothness(), 0.0, 1.0);
         float interpolation = smoothness <= 1.0E-4F ? 1.0F : smoothness;
         currentZoomMultiplier = currentZoomMultiplier + (targetMultiplier - currentZoomMultiplier) * interpolation;
         if (Math.abs(currentZoomMultiplier - targetMultiplier) <= 1.0E-4F) {
            currentZoomMultiplier = targetMultiplier;
         }

         event.setNewFovModifier(event.getNewFovModifier() * currentZoomMultiplier);
      }
   }

   @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
   public static void applyNanosuit2Fov(net.neoforged.neoforge.client.event.ViewportEvent.ComputeFov event) {
      if(!event.usedConfiguredFov() || !io.github.painworks.crysismod.nanosuit.client.Nanosuit2Hud.equipped()) return;
      long now=net.minecraft.Util.getMillis();
      float target=NanoVisorState.isVisorActive()&&zoomInputActive&&NanoVisorSettings.Zoom.enabled()?3:1;
      float progress=Mth.clamp((now-zoomStarted)/220f,0,1);
      float magnification=Mth.lerp(progress*progress*(3-2*progress),zoomFrom,zoomDestination);
      if(target!=zoomDestination) {zoomFrom=magnification;zoomDestination=target;zoomStarted=now;}
      double baseFov=event.getFOV();
      if(!Double.isFinite(baseFov)||baseFov<=0) return;
      lastWorldFov=ClassicBinocularAnimation.zoomedFov(baseFov,magnification);
      currentZoomMultiplier=(float)(lastWorldFov/baseFov);
      event.setFOV(lastWorldFov);
   }

   static double worldFov() {return lastWorldFov;}

   public static void updateZoomInput(KeyMapping zoomKey) {
      if(!NanoVisorState.canUse(Minecraft.getInstance())){
         reset();while(zoomKey.consumeClick()){}while(NanoVisorKeyMappings.CLASSIC_BINOCULAR_ZOOM.consumeClick()){}return;
      }
      if (ClassicBinoculars.isN1Context()) {
         while(zoomKey.consumeClick()){} // Clear our legacy queue without changing TACZ's separate C binding.
         ClassicBinoculars.updateZoomInput(NanoVisorKeyMappings.CLASSIC_BINOCULAR_ZOOM);
         zoomInputActive=ClassicBinoculars.zoom()==4;
         return;
      }
      while(NanoVisorKeyMappings.CLASSIC_BINOCULAR_ZOOM.consumeClick()){}
      if (io.github.painworks.crysismod.nanosuit.client.Nanosuit2Hud.equipped()) {
         var mc=Minecraft.getInstance();
         if(mc.screen==null && NanoVisorState.isVisorActive()) mc.options.keyUse.setDown(false);
         while(zoomKey.consumeClick()) {}
         if(!NanoVisorState.isVisorActive() || !NanoVisorSettings.Zoom.enabled()) zoomInputActive=false;
         else if(rightClickTarget!=null) zoomInputActive=rightClickTarget;
         rightClickTarget=null;
         scrollTarget=null;
         return;
      }
      if (NanoVisorState.isVisorActive() && NanoVisorSettings.Zoom.enabled()) {
         while(zoomKey.consumeClick())zoomInputActive=!zoomInputActive; // Optional user-bound fallback.
         if(scrollTarget!=null)zoomInputActive=scrollTarget;
      } else {
         while (zoomKey.consumeClick()) {
         }

         zoomInputActive = false;
      }
      scrollTarget=null;
   }

   public static float getNormalizedZoom() {
      float baseMultiplier = getBaseZoomMultiplier();
      float boostedMultiplier = getBoostedZoomMultiplier();
      float denominator = baseMultiplier - boostedMultiplier;
      if (Math.abs(denominator) <= 1.0E-4F) {
         return 0.0F;
      } else {
         float normalized = (baseMultiplier - currentZoomMultiplier) / denominator;
         return Mth.clamp(normalized, 0.0F, 1.0F);
      }
   }

   public static boolean isZoomInputActive() {
      return zoomInputActive;
   }

   public static float getCurrentZoomMultiplier() {
      return currentZoomMultiplier;
   }

   public static float getSensitivityZoomFactor() {
      if (NanoVisorState.isVisorActive() && NanoVisorSettings.Zoom.enabled()) {
         if(io.github.painworks.crysismod.nanosuit.client.Nanosuit2Hud.equipped()) return currentZoomMultiplier;
         float baseMultiplier = getBaseZoomMultiplier();
         if (baseMultiplier <= 1.0E-4F) {
            return 1.0F;
         } else {
            float ratio = currentZoomMultiplier / baseMultiplier;
            return Mth.clamp(ratio, 0.05F, 1.0F);
         }
      } else {
         return 1.0F;
      }
   }

   private static float getBaseZoomMultiplier() {
      float configuredMin = (float)Mth.clamp(NanoVisorSettings.Zoom.minFovMultiplier(), 0.05F, 2.0);
      float configuredMax = (float)Mth.clamp(NanoVisorSettings.Zoom.maxFovMultiplier(), 0.05F, 2.0);
      return Math.max(configuredMin, configuredMax);
   }

   private static float getBoostedZoomMultiplier() {
      float configuredMin = (float)Mth.clamp(NanoVisorSettings.Zoom.minFovMultiplier(), 0.05F, 2.0);
      float configuredMax = (float)Mth.clamp(NanoVisorSettings.Zoom.maxFovMultiplier(), 0.05F, 2.0);
      return Math.min(configuredMin, configuredMax);
   }

   private static float resolveTargetZoomMultiplier() {
      if (!NanoVisorState.isVisorActive() || !NanoVisorSettings.Zoom.enabled()) {
         return 1.0F;
      } else {
         return !zoomInputActive ? getBaseZoomMultiplier() : getBoostedZoomMultiplier();
      }
   }
}
