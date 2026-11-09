package io.github.painworks.crysismod.nanovisor.network;

import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanovisor.server.NanoVisorServerState;
import io.github.painworks.crysismod.nanovisor.server.NanoVisorTargetMarkService;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestNanoVisorMarkTargetPacket implements CustomPacketPayload {
   public static final Type<RequestNanoVisorMarkTargetPacket> TYPE = NanosuitNetwork.type("request_nano_visor_mark_target");
   public static final StreamCodec<FriendlyByteBuf, RequestNanoVisorMarkTargetPacket> STREAM_CODEC =
      StreamCodec.ofMember((packet, buffer) -> encode(packet, buffer), RequestNanoVisorMarkTargetPacket::decode);

   @Override
   public Type<RequestNanoVisorMarkTargetPacket> type() {
      return TYPE;
   }

   private static final double MIN_MARK_RANGE = 1.0;
   private static final double MAX_MARK_RANGE = 256.0;
   private static final int MIN_GLOW_TICKS = 1;
   private static final int MAX_GLOW_TICKS = 12000;
   private static final int MIN_MAX_TARGETS = 1;
   private static final int MAX_MAX_TARGETS = 64;
   private final int targetEntityId;
   private final double markRange;
   private final int glowDurationTicks;
   private final boolean markOnlyLivingEntities;
   private final boolean requireLineOfSight;
   private final int maxMarkedTargets;

   public RequestNanoVisorMarkTargetPacket(
      int targetEntityId, double markRange, int glowDurationTicks, boolean markOnlyLivingEntities, boolean requireLineOfSight, int maxMarkedTargets
   ) {
      this.targetEntityId = targetEntityId;
      this.markRange = markRange;
      this.glowDurationTicks = glowDurationTicks;
      this.markOnlyLivingEntities = markOnlyLivingEntities;
      this.requireLineOfSight = requireLineOfSight;
      this.maxMarkedTargets = maxMarkedTargets;
   }

   public static void encode(RequestNanoVisorMarkTargetPacket packet, FriendlyByteBuf buffer) {
      buffer.writeInt(packet.targetEntityId);
      buffer.writeDouble(packet.markRange);
      buffer.writeVarInt(packet.glowDurationTicks);
      buffer.writeBoolean(packet.markOnlyLivingEntities);
      buffer.writeBoolean(packet.requireLineOfSight);
      buffer.writeVarInt(packet.maxMarkedTargets);
   }

   public static RequestNanoVisorMarkTargetPacket decode(FriendlyByteBuf buffer) {
      return new RequestNanoVisorMarkTargetPacket(
         buffer.readInt(), buffer.readDouble(), buffer.readVarInt(), buffer.readBoolean(), buffer.readBoolean(), buffer.readVarInt()
      );
   }

   public static void handle(RequestNanoVisorMarkTargetPacket packet, IPayloadContext context) {
      if (!(context.player() instanceof ServerPlayer player)) {
         return;
      }
      if (!hasServerMarkPermission(player)) {
         reject(player, "message.crysismod.nanovisor.mark_rejected.no_permission");
         return;
      }

      Entity target = player.serverLevel().getEntity(packet.targetEntityId);
      if (target == null || !target.isAlive() || target == player) {
         reject(player, "message.crysismod.nanovisor.mark_rejected.invalid_target");
         return;
      }

      double range = Mth.clamp(packet.markRange, MIN_MARK_RANGE, MAX_MARK_RANGE);
      int glowTicks = Mth.clamp(packet.glowDurationTicks, MIN_GLOW_TICKS, MAX_GLOW_TICKS);
      int maxTargets = Mth.clamp(packet.maxMarkedTargets, MIN_MAX_TARGETS, MAX_MAX_TARGETS);
      if (packet.markOnlyLivingEntities && !(target instanceof LivingEntity)) {
         reject(player, "message.crysismod.nanovisor.mark_rejected.only_living");
      } else if (player.distanceToSqr(target) > range * range) {
         reject(player, "message.crysismod.nanovisor.mark_rejected.out_of_range");
      } else if (packet.requireLineOfSight && !player.hasLineOfSight(target)) {
         reject(player, "message.crysismod.nanovisor.mark_rejected.no_line_of_sight");
      } else if (!NanoVisorTargetMarkService.tryApplyMark(player, target, glowTicks, maxTargets)) {
         reject(player, "message.crysismod.nanovisor.mark_rejected.limit_reached");
      }
   }

   private static boolean hasServerMarkPermission(ServerPlayer player) {
      if (!player.isSpectator() && NanosuitArmorUtil.hasFullSet(player)) {
         boolean hasEnergy = NanosuitCapabilities.get(player).getEnergy() > 0.0F;
         return hasEnergy && NanoVisorServerState.isActive(player);
      } else {
         return false;
      }
   }

   private static void reject(ServerPlayer player, String translationKey) {
      player.displayClientMessage(Component.translatable(translationKey), true);
   }
}
