package io.github.painworks.crysismod.nanosuit.network;

import io.github.painworks.crysismod.nanosuit.client.EnhancedJumpClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SyncEnhancedJumpPacket implements CustomPacketPayload {
   public static final Type<SyncEnhancedJumpPacket> TYPE = NanosuitNetwork.type("sync_enhanced_jump");
   public static final StreamCodec<FriendlyByteBuf, SyncEnhancedJumpPacket> STREAM_CODEC =
      StreamCodec.ofMember((packet, buffer) -> encode(packet, buffer), SyncEnhancedJumpPacket::decode);

   @Override
   public Type<SyncEnhancedJumpPacket> type() {
      return TYPE;
   }

   private final double verticalVelocity;

   public SyncEnhancedJumpPacket(double verticalVelocity) {
      this.verticalVelocity = verticalVelocity;
   }

   public static void encode(SyncEnhancedJumpPacket packet, FriendlyByteBuf buffer) {
      buffer.writeDouble(packet.verticalVelocity);
   }

   public static SyncEnhancedJumpPacket decode(FriendlyByteBuf buffer) {
      return new SyncEnhancedJumpPacket(buffer.readDouble());
   }

   public static void handle(SyncEnhancedJumpPacket packet, IPayloadContext context) {
      EnhancedJumpClientState.activate(packet.verticalVelocity);
   }
}
