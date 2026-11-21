package io.github.painworks.crysismod.nanosuit.network;

import io.github.painworks.crysismod.nanosuit.client.ClassicMalfunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClassicMalfunctionPacket() implements CustomPacketPayload {
   public static final Type<ClassicMalfunctionPacket> TYPE = NanosuitNetwork.type("classic_malfunction");
   public static final StreamCodec<FriendlyByteBuf, ClassicMalfunctionPacket> STREAM_CODEC =
      StreamCodec.unit(new ClassicMalfunctionPacket());

   @Override
   public Type<ClassicMalfunctionPacket> type() { return TYPE; }

   public static void handle(ClassicMalfunctionPacket packet, IPayloadContext context) {
      ClassicMalfunction.trigger(context.player());
   }
}
