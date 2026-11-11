package io.github.painworks.crysismod.nanosuit.network;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ToggleNanosuitModePacket implements CustomPacketPayload {
   public static final Type<ToggleNanosuitModePacket> TYPE = NanosuitNetwork.type("toggle_nanosuit_mode");
   public static final StreamCodec<FriendlyByteBuf, ToggleNanosuitModePacket> STREAM_CODEC =
      StreamCodec.ofMember((packet, buffer) -> encode(packet, buffer), ToggleNanosuitModePacket::decode);

   @Override
   public Type<ToggleNanosuitModePacket> type() {
      return TYPE;
   }

   private final NanosuitMode mode;

   public ToggleNanosuitModePacket(NanosuitMode mode) {
      this.mode = mode;
   }

   public static void encode(ToggleNanosuitModePacket packet, FriendlyByteBuf buffer) {
      buffer.writeVarInt(packet.mode.wireId());
   }

   public static ToggleNanosuitModePacket decode(FriendlyByteBuf buffer) {
      return new ToggleNanosuitModePacket(NanosuitMode.fromWireId(buffer.readVarInt()));
   }

   public static void handle(ToggleNanosuitModePacket packet, IPayloadContext context) {
      if (context.player() instanceof ServerPlayer player) {
         NanosuitPlayerHandler.tryToggleMode(player, packet.mode);
      }
   }
}
