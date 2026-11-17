package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanosuit.network.RequestClassicMissedMeleePacket;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT)
public final class ClassicMeleeInput {
   private ClassicMeleeInput() {
   }

   @SubscribeEvent
   public static void onMiss(LeftClickEmpty event) {
      var player = event.getEntity();
      if (player.level().isClientSide() && !player.isSpectator()
         && player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()
         && NanosuitArmorUtil.getEquippedProfile(player) == NanosuitProfile.NANOSUIT_1) {
         var mode = NanosuitCapabilities.get(player).getMode();
         if (mode == NanosuitMode.STRENGTH || mode == NanosuitMode.CLOAK) {
            NanosuitNetwork.sendToServer(new RequestClassicMissedMeleePacket());
         }
      }
   }
}
