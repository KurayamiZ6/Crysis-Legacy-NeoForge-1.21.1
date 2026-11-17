package io.github.painworks.crysismod.nanovision.network;

import io.github.painworks.crysismod.nanovision.server.ClassicNightVisionServerState;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleClassicNightVisionPacket() implements CustomPacketPayload {
   public static final ToggleClassicNightVisionPacket INSTANCE = new ToggleClassicNightVisionPacket();
   public static final Type<ToggleClassicNightVisionPacket> TYPE = NanosuitNetwork.type("toggle_classic_night_vision");
   public static final StreamCodec<FriendlyByteBuf, ToggleClassicNightVisionPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

   @Override
   public Type<ToggleClassicNightVisionPacket> type() {
      return TYPE;
   }

   public static void handle(ToggleClassicNightVisionPacket packet, IPayloadContext context) {
      if (context.player() instanceof ServerPlayer player) {
         ClassicNightVisionServerState.toggle(player);
      }
   }
}
