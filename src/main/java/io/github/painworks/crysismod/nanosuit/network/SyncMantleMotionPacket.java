package io.github.painworks.crysismod.nanosuit.network;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Applies the horizontal part of the climb without restarting the vertical impulse. */
public record SyncMantleMotionPacket(double x, double z) implements CustomPacketPayload {
   public static final Type<SyncMantleMotionPacket> TYPE = NanosuitNetwork.type("sync_mantle_motion");
   public static final StreamCodec<FriendlyByteBuf, SyncMantleMotionPacket> STREAM_CODEC = StreamCodec.ofMember(
      (packet, buffer) -> {
         buffer.writeDouble(packet.x);
         buffer.writeDouble(packet.z);
      },
      buffer -> new SyncMantleMotionPacket(buffer.readDouble(), buffer.readDouble())
   );

   @Override
   public Type<SyncMantleMotionPacket> type() {
      return TYPE;
   }

   public static void handle(SyncMantleMotionPacket packet, IPayloadContext context) {
      if (context.player() instanceof LocalPlayer player) {
         player.setDeltaMovement(packet.x, player.getDeltaMovement().y, packet.z);
         player.hasImpulse = true;
      }
   }
}
