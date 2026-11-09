package io.github.painworks.crysismod.nanovision.network;

import io.github.painworks.crysismod.nanovision.server.NanovisionServerState;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SetNanovisionActivePacket implements CustomPacketPayload {
   public static final Type<SetNanovisionActivePacket> TYPE = NanosuitNetwork.type("set_nanovision_active");
   public static final StreamCodec<FriendlyByteBuf, SetNanovisionActivePacket> STREAM_CODEC =
      StreamCodec.ofMember((packet, buffer) -> encode(packet, buffer), SetNanovisionActivePacket::decode);

   @Override
   public Type<SetNanovisionActivePacket> type() {
      return TYPE;
   }

   private final boolean active;

   public SetNanovisionActivePacket(boolean active) {
      this.active = active;
   }

   public static void encode(SetNanovisionActivePacket packet, FriendlyByteBuf buffer) {
      buffer.writeBoolean(packet.active);
   }

   public static SetNanovisionActivePacket decode(FriendlyByteBuf buffer) {
      return new SetNanovisionActivePacket(buffer.readBoolean());
   }

   public static void handle(SetNanovisionActivePacket packet, IPayloadContext context) {
      if (context.player() instanceof ServerPlayer player) {
         NanovisionServerState.setActiveFromClient(player, packet.active);
      }
   }
}
