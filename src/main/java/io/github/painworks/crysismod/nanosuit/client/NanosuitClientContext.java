package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanovisor.client.*;
import io.github.painworks.crysismod.client.nanovision.*;
import io.github.painworks.crysismod.nanosuit.client.radial.Nanosuit1RadialScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/** Cancels owned transient input/visuals; never edits energy, cooldowns or vanilla key state. */
@EventBusSubscriber(modid="crysismod",value=Dist.CLIENT)
public final class NanosuitClientContext {
   private static java.lang.ref.WeakReference<Object> player=new java.lang.ref.WeakReference<>(null),level=new java.lang.ref.WeakReference<>(null);
   private static NanosuitProfile profile;
   private NanosuitClientContext(){}
   @SubscribeEvent(priority=EventPriority.HIGHEST) public static void tick(ClientTickEvent.Pre event){
      var mc=Minecraft.getInstance();var current=NanosuitArmorUtil.getUsableProfile(mc.player);
      boolean ownerChanged=player.get()!=mc.player || profile!=current;
      boolean worldChanged=level.get()!=mc.level;
      if(ownerChanged || worldChanged || current==null || mc.screen!=null){
         NanoVisorState.invalidate();EnhancedJumpClientState.cancelTransientState();
         if(ownerChanged || worldChanged){
            while(NanoVisorKeyMappings.TOGGLE_NANO_VISOR.consumeClick()){}
            while(NanoVisorKeyMappings.CLASSIC_BINOCULAR_ZOOM.consumeClick()){}
         }
      }
      if(ownerChanged || current==null){
         ClassicNightVisionClient.cancelVisuals();NanovisionController.reset();NanovisionState.clearClientEffects(mc.player);
         NanosuitPowerFeedback.clearTransientEffects();
      }
      if(current!=NanosuitProfile.NANOSUIT_1 && mc.screen instanceof Nanosuit1RadialScreen)mc.setScreen(null);
      player=new java.lang.ref.WeakReference<>(mc.player);level=new java.lang.ref.WeakReference<>(mc.level);profile=current;
   }
   @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){
      NanoVisorState.invalidate();EnhancedJumpClientState.cancelTransientState();
      player.clear();level.clear();profile=null;
   }
}
