package io.github.painworks.crysismod.nanosuit.network;

import io.github.painworks.crysismod.nanosuit.client.NanosuitFeedbackSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSuitEnergySoundPacket(Cue cue, float volume) implements CustomPacketPayload {
   public static final Type<SyncSuitEnergySoundPacket> TYPE = NanosuitNetwork.type("suit_energy_sound");
   public static final StreamCodec<FriendlyByteBuf, SyncSuitEnergySoundPacket> STREAM_CODEC = StreamCodec.ofMember(
      (packet, buffer) -> {
         buffer.writeEnum(packet.cue());
         buffer.writeFloat(packet.volume());
      },
      buffer -> new SyncSuitEnergySoundPacket(buffer.readEnum(Cue.class), buffer.readFloat())
   );

   @Override
   public Type<SyncSuitEnergySoundPacket> type() {
      return TYPE;
   }

   public static void handle(SyncSuitEnergySoundPacket packet, IPayloadContext context) {
      NanosuitFeedbackSounds.play(packet);
   }

   public enum Cue {
      CRITICAL,
      DISABLED,
      WAIT,
      CHARGE,
      RESTORED
   }
}
