package io.github.painworks.crysismod.nanosuit.network;

import io.github.painworks.crysismod.client.minimap.render.ThreatGauge;
import io.github.painworks.crysismod.nanosuit.threat.ThreatLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncThreatLevelPacket(ThreatLevel threat) implements CustomPacketPayload {
   public static final Type<SyncThreatLevelPacket> TYPE = NanosuitNetwork.type("threat_level");
   public static final StreamCodec<FriendlyByteBuf, SyncThreatLevelPacket> STREAM_CODEC = StreamCodec.ofMember(
      (packet, buffer) -> buffer.writeEnum(packet.threat()),
      buffer -> new SyncThreatLevelPacket(buffer.readEnum(ThreatLevel.class))
   );

   @Override
   public Type<SyncThreatLevelPacket> type() {
      return TYPE;
   }

   public static void handle(SyncThreatLevelPacket packet, IPayloadContext context) {
      ThreatGauge.update(context.player(), packet.threat());
   }
}
