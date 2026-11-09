package io.github.painworks.crysismod.nanovisor.network;

import io.github.painworks.crysismod.nanovisor.server.NanoVisorServerState;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SetNanoVisorActivePacket implements CustomPacketPayload {
   public static final Type<SetNanoVisorActivePacket> TYPE = NanosuitNetwork.type("set_nano_visor_active");
   public static final StreamCodec<FriendlyByteBuf, SetNanoVisorActivePacket> STREAM_CODEC =
      StreamCodec.ofMember((packet, buffer) -> encode(packet, buffer), SetNanoVisorActivePacket::decode);

   @Override
   public Type<SetNanoVisorActivePacket> type() {
      return TYPE;
   }

   private final boolean active;

   public SetNanoVisorActivePacket(boolean active) {
      this.active = active;
   }

   public static void encode(SetNanoVisorActivePacket packet, FriendlyByteBuf buffer) {
      buffer.writeBoolean(packet.active);
   }

   public static SetNanoVisorActivePacket decode(FriendlyByteBuf buffer) {
      return new SetNanoVisorActivePacket(buffer.readBoolean());
   }

   public static void handle(SetNanoVisorActivePacket packet, IPayloadContext context) {
      if (context.player() instanceof ServerPlayer player) {
         NanoVisorServerState.setActiveFromClient(player, packet.active);
      }
   }
}
