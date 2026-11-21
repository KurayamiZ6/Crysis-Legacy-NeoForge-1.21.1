package io.github.painworks.crysismod.nanosuit.network;

import io.github.painworks.crysismod.nanovision.network.SetNanovisionActivePacket;
import io.github.painworks.crysismod.nanovision.network.SyncClassicNightVisionPacket;
import io.github.painworks.crysismod.nanovision.network.ToggleClassicNightVisionPacket;
import io.github.painworks.crysismod.nanovisor.network.RequestNanoVisorMarkTargetPacket;
import io.github.painworks.crysismod.nanovisor.network.SetNanoVisorActivePacket;
import io.github.painworks.crysismod.nanovisor.network.SyncNanoVisorMarkedTargetsPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NanosuitNetwork {
   // Protocol 8 adds the server-triggered N1 malfunction display.
   private static final String PROTOCOL_VERSION = "8";

   private NanosuitNetwork() {
   }

   public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String path) {
      return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("crysismod", path));
   }

   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

      registrar.playToServer(ToggleNanosuitModePacket.TYPE, ToggleNanosuitModePacket.STREAM_CODEC, ToggleNanosuitModePacket::handle);
      registrar.playToClient(SyncNanosuitDataPacket.TYPE, SyncNanosuitDataPacket.STREAM_CODEC, SyncNanosuitDataPacket::handle);
      registrar.playToClient(SyncSuitEnergySoundPacket.TYPE, SyncSuitEnergySoundPacket.STREAM_CODEC, SyncSuitEnergySoundPacket::handle);
      registrar.playToClient(ClassicMalfunctionPacket.TYPE, ClassicMalfunctionPacket.STREAM_CODEC, ClassicMalfunctionPacket::handle);
      registrar.playToClient(SyncThreatLevelPacket.TYPE, SyncThreatLevelPacket.STREAM_CODEC, SyncThreatLevelPacket::handle);
      registrar.playToClient(SyncClassicThreatsPacket.TYPE, SyncClassicThreatsPacket.STREAM_CODEC, SyncClassicThreatsPacket::handle);
      registrar.playToClient(SyncPowerFeedbackPacket.TYPE, SyncPowerFeedbackPacket.STREAM_CODEC, SyncPowerFeedbackPacket::handle);
      registrar.playToServer(RequestSprintBoostPacket.TYPE, RequestSprintBoostPacket.STREAM_CODEC, RequestSprintBoostPacket::handle);
      registrar.playToServer(RequestEnhancedJumpPacket.TYPE, RequestEnhancedJumpPacket.STREAM_CODEC, RequestEnhancedJumpPacket::handle);
      registrar.playToClient(SyncEnhancedJumpPacket.TYPE, SyncEnhancedJumpPacket.STREAM_CODEC, SyncEnhancedJumpPacket::handle);
      registrar.playToClient(SyncMantleMotionPacket.TYPE, SyncMantleMotionPacket.STREAM_CODEC, SyncMantleMotionPacket::handle);
      registrar.playToClient(SyncPowerJumpDebugPacket.TYPE, SyncPowerJumpDebugPacket.STREAM_CODEC, SyncPowerJumpDebugPacket::handle);
      registrar.playToServer(SetNanoVisorActivePacket.TYPE, SetNanoVisorActivePacket.STREAM_CODEC, SetNanoVisorActivePacket::handle);
      registrar.playToServer(RequestNanoVisorMarkTargetPacket.TYPE, RequestNanoVisorMarkTargetPacket.STREAM_CODEC, RequestNanoVisorMarkTargetPacket::handle);
      registrar.playToClient(SyncNanoVisorMarkedTargetsPacket.TYPE, SyncNanoVisorMarkedTargetsPacket.STREAM_CODEC, SyncNanoVisorMarkedTargetsPacket::handle);
      registrar.playToServer(SetNanovisionActivePacket.TYPE, SetNanovisionActivePacket.STREAM_CODEC, SetNanovisionActivePacket::handle);
      registrar.playToServer(ToggleClassicNightVisionPacket.TYPE, ToggleClassicNightVisionPacket.STREAM_CODEC, ToggleClassicNightVisionPacket::handle);
      registrar.playToClient(SyncClassicNightVisionPacket.TYPE, SyncClassicNightVisionPacket.STREAM_CODEC, SyncClassicNightVisionPacket::handle);
      registrar.playToServer(RequestClassicMissedMeleePacket.TYPE, RequestClassicMissedMeleePacket.STREAM_CODEC, RequestClassicMissedMeleePacket::handle);
   }

   public static void sendToServer(CustomPacketPayload message) {
      PacketDistributor.sendToServer(message);
   }

   public static void sendToPlayer(ServerPlayer player, CustomPacketPayload message) {
      PacketDistributor.sendToPlayer(player, message);
   }
}
