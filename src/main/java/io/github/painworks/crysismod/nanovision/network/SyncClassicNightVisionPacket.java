package io.github.painworks.crysismod.nanovision.network;

import io.github.painworks.crysismod.client.nanovision.ClassicNightVisionClient;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncClassicNightVisionPacket(boolean active, int charge) implements CustomPacketPayload {
   public static final Type<SyncClassicNightVisionPacket> TYPE = NanosuitNetwork.type("sync_classic_night_vision");
   public static final StreamCodec<FriendlyByteBuf, SyncClassicNightVisionPacket> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.BOOL, SyncClassicNightVisionPacket::active,
      ByteBufCodecs.VAR_INT, SyncClassicNightVisionPacket::charge,
      SyncClassicNightVisionPacket::new
   );

   @Override
   public Type<SyncClassicNightVisionPacket> type() {
      return TYPE;
   }

   public static void handle(SyncClassicNightVisionPacket packet, IPayloadContext context) {
      ClassicNightVisionClient.sync(packet.active(), packet.charge());
   }
}
