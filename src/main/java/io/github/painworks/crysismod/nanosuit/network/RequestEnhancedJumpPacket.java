package io.github.painworks.crysismod.nanosuit.network;

import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestEnhancedJumpPacket(Phase phase) implements CustomPacketPayload {
   public static final Type<RequestEnhancedJumpPacket> TYPE = NanosuitNetwork.type("request_enhanced_jump");
   public static final StreamCodec<FriendlyByteBuf, RequestEnhancedJumpPacket> STREAM_CODEC =
      StreamCodec.ofMember((packet, buffer) -> encode(packet, buffer), RequestEnhancedJumpPacket::decode);

   @Override
   public Type<RequestEnhancedJumpPacket> type() {
      return TYPE;
   }

   public static void encode(RequestEnhancedJumpPacket packet, FriendlyByteBuf buffer) {
      buffer.writeEnum(packet.phase);
   }

   public static RequestEnhancedJumpPacket decode(FriendlyByteBuf buffer) {
      return new RequestEnhancedJumpPacket(buffer.readEnum(Phase.class));
   }

   public static void handle(RequestEnhancedJumpPacket packet, IPayloadContext context) {
      if (context.player() instanceof ServerPlayer player) {
         switch (packet.phase) {
            case BEGIN -> NanosuitPlayerHandler.beginEnhancedJumpCharge(player);
            case RELEASE -> NanosuitPlayerHandler.cancelEnhancedJumpCharge(player);
            case LAUNCH -> NanosuitPlayerHandler.requestChargedEnhancedJump(player);
            case INSTANT -> NanosuitPlayerHandler.requestInstantClassicJump(player);
         }
      }
   }

   public enum Phase {
      BEGIN,
      RELEASE,
      LAUNCH,
      INSTANT
   }
}
