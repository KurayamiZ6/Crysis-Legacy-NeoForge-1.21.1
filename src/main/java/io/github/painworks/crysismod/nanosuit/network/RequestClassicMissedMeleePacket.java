package io.github.painworks.crysismod.nanosuit.network;

import io.github.painworks.crysismod.nanosuit.handler.NanosuitPowerMelee;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestClassicMissedMeleePacket() implements CustomPacketPayload {
   public static final Type<RequestClassicMissedMeleePacket> TYPE = NanosuitNetwork.type("request_classic_missed_melee");
   public static final StreamCodec<FriendlyByteBuf, RequestClassicMissedMeleePacket> STREAM_CODEC =
      StreamCodec.unit(new RequestClassicMissedMeleePacket());

   private static final Map<ServerPlayer, Integer> LAST_MISS_TICK = new WeakHashMap<>();

   @Override
   public Type<RequestClassicMissedMeleePacket> type() {
      return TYPE;
   }

   public static void handle(RequestClassicMissedMeleePacket packet, IPayloadContext context) {
      if (context.player() instanceof ServerPlayer player && NanosuitPowerMelee.isBareHanded(player) && acceptMiss(player)) {
         NanosuitPowerMelee.beginClassicPunch(player);
      }
   }

   private static synchronized boolean acceptMiss(ServerPlayer player) {
      Integer previous = LAST_MISS_TICK.put(player, player.tickCount);
      return previous == null || previous != player.tickCount;
   }
}
