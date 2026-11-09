package io.github.painworks.crysismod.nanosuit.network;

import io.github.painworks.crysismod.nanosuit.client.EnhancedJumpClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SyncPowerJumpDebugPacket implements CustomPacketPayload {
   public static final Type<SyncPowerJumpDebugPacket> TYPE = NanosuitNetwork.type("sync_power_jump_debug");
   public static final StreamCodec<FriendlyByteBuf, SyncPowerJumpDebugPacket> STREAM_CODEC =
      StreamCodec.ofMember((packet, buffer) -> encode(packet, buffer), SyncPowerJumpDebugPacket::decode);

   @Override
   public Type<SyncPowerJumpDebugPacket> type() {
      return TYPE;
   }

   private final boolean enabled;

   public SyncPowerJumpDebugPacket(boolean enabled) {
      this.enabled = enabled;
   }

   public static void encode(SyncPowerJumpDebugPacket packet, FriendlyByteBuf buffer) {
      buffer.writeBoolean(packet.enabled);
   }

   public static SyncPowerJumpDebugPacket decode(FriendlyByteBuf buffer) {
      return new SyncPowerJumpDebugPacket(buffer.readBoolean());
   }

   public static void handle(SyncPowerJumpDebugPacket packet, IPayloadContext context) {
      EnhancedJumpClientState.setTestLogging(packet.enabled);
   }
}
