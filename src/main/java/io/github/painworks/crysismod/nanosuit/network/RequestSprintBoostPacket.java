package io.github.painworks.crysismod.nanosuit.network;

import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestSprintBoostPacket implements CustomPacketPayload {
   public static final Type<RequestSprintBoostPacket> TYPE = NanosuitNetwork.type("request_sprint_boost");
   public static final StreamCodec<FriendlyByteBuf, RequestSprintBoostPacket> STREAM_CODEC =
      StreamCodec.ofMember((packet, buffer) -> encode(packet, buffer), RequestSprintBoostPacket::decode);

   @Override
   public Type<RequestSprintBoostPacket> type() {
      return TYPE;
   }

   public static void encode(RequestSprintBoostPacket packet, FriendlyByteBuf buffer) {
   }

   public static RequestSprintBoostPacket decode(FriendlyByteBuf buffer) {
      return new RequestSprintBoostPacket();
   }

   public static void handle(RequestSprintBoostPacket packet, IPayloadContext context) {
      if (context.player() instanceof ServerPlayer player) {
         NanosuitPlayerHandler.requestPowerSprint(player);
      }
   }
}
