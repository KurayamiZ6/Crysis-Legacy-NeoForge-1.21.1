package io.github.painworks.crysismod.nanosuit.network;

import io.github.painworks.crysismod.nanosuit.client.NanosuitPowerFeedback;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncPowerFeedbackPacket(Feedback feedback) implements CustomPacketPayload {
   public static final Type<SyncPowerFeedbackPacket> TYPE = NanosuitNetwork.type("power_feedback");
   public static final StreamCodec<FriendlyByteBuf, SyncPowerFeedbackPacket> STREAM_CODEC = StreamCodec.ofMember(
      (packet, buffer) -> buffer.writeEnum(packet.feedback()), buffer -> new SyncPowerFeedbackPacket(buffer.readEnum(Feedback.class))
   );

   @Override
   public Type<SyncPowerFeedbackPacket> type() {
      return TYPE;
   }

   public static void handle(SyncPowerFeedbackPacket packet, IPayloadContext context) {
      switch (packet.feedback()) {
         case SUSTAINED_START -> NanosuitPowerFeedback.setSustainedForce(true);
         case SUSTAINED_STOP -> NanosuitPowerFeedback.setSustainedForce(false);
         case STRIKE -> NanosuitPowerFeedback.triggerForce();
      }
   }

   public enum Feedback {
      SUSTAINED_START,
      SUSTAINED_STOP,
      STRIKE
   }
}
