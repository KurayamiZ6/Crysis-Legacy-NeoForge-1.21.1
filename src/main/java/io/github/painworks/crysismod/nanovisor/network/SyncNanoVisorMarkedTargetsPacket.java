package io.github.painworks.crysismod.nanovisor.network;

import io.github.painworks.crysismod.nanovisor.client.NanoVisorClientMarkedTargets;
import java.util.UUID;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncNanoVisorMarkedTargetsPacket implements CustomPacketPayload {
   public static final Type<SyncNanoVisorMarkedTargetsPacket> TYPE = NanosuitNetwork.type("sync_nano_visor_marked_targets");
   public static final StreamCodec<FriendlyByteBuf, SyncNanoVisorMarkedTargetsPacket> STREAM_CODEC =
      StreamCodec.ofMember((packet, buffer) -> encode(packet, buffer), SyncNanoVisorMarkedTargetsPacket::decode);

   @Override
   public Type<SyncNanoVisorMarkedTargetsPacket> type() {
      return TYPE;
   }

   public static final double N2_SCAN_RANGE = 100.0;
   // An upsert with zero ticks is an N2 contact that lasts until the server removes it.
   public static final int PERSISTENT_SCAN = 0;
   private static final int MAX_DURATION_TICKS = 12000;
   private final SyncNanoVisorMarkedTargetsPacket.Action action;
   private final UUID targetUuid;
   private final int targetEntityId;
   private final int durationTicks;

   private SyncNanoVisorMarkedTargetsPacket(SyncNanoVisorMarkedTargetsPacket.Action action, UUID targetUuid, int targetEntityId, int durationTicks) {
      this.action = action;
      this.targetUuid = targetUuid;
      this.targetEntityId = targetEntityId;
      this.durationTicks = durationTicks;
   }

   public static SyncNanoVisorMarkedTargetsPacket upsert(UUID targetUuid, int targetEntityId, int durationTicks) {
      int clamped = Mth.clamp(durationTicks, PERSISTENT_SCAN, MAX_DURATION_TICKS);
      return new SyncNanoVisorMarkedTargetsPacket(SyncNanoVisorMarkedTargetsPacket.Action.UPSERT, targetUuid, targetEntityId, clamped);
   }

   public static SyncNanoVisorMarkedTargetsPacket remove(UUID targetUuid, int targetEntityId) {
      return new SyncNanoVisorMarkedTargetsPacket(SyncNanoVisorMarkedTargetsPacket.Action.REMOVE, targetUuid, targetEntityId, 0);
   }

   public static SyncNanoVisorMarkedTargetsPacket clearAll() {
      return new SyncNanoVisorMarkedTargetsPacket(SyncNanoVisorMarkedTargetsPacket.Action.CLEAR_ALL, new UUID(0L, 0L), 0, 0);
   }

   public static void encode(SyncNanoVisorMarkedTargetsPacket packet, FriendlyByteBuf buffer) {
      buffer.writeEnum(packet.action);
      if (packet.action != SyncNanoVisorMarkedTargetsPacket.Action.CLEAR_ALL) {
         buffer.writeUUID(packet.targetUuid);
         buffer.writeVarInt(packet.targetEntityId);
         if (packet.action == SyncNanoVisorMarkedTargetsPacket.Action.UPSERT) {
            buffer.writeVarInt(packet.durationTicks);
         }
      }
   }

   public static SyncNanoVisorMarkedTargetsPacket decode(FriendlyByteBuf buffer) {
      SyncNanoVisorMarkedTargetsPacket.Action action = (SyncNanoVisorMarkedTargetsPacket.Action)buffer.readEnum(SyncNanoVisorMarkedTargetsPacket.Action.class);
      if (action == SyncNanoVisorMarkedTargetsPacket.Action.CLEAR_ALL) {
         return clearAll();
      } else {
         UUID targetUuid = buffer.readUUID();
         int targetEntityId = buffer.readVarInt();
         if (action == SyncNanoVisorMarkedTargetsPacket.Action.UPSERT) {
            return upsert(targetUuid, targetEntityId, buffer.readVarInt());
         } else {
            return remove(targetUuid, targetEntityId);
         }
      }
   }

   public static void handle(SyncNanoVisorMarkedTargetsPacket packet, IPayloadContext context) {
      NanoVisorClientMarkedTargets.handlePacket(packet);
   }

   public SyncNanoVisorMarkedTargetsPacket.Action action() {
      return this.action;
   }

   public UUID targetUuid() {
      return this.targetUuid;
   }

   public int targetEntityId() {
      return this.targetEntityId;
   }

   public int durationTicks() {
      return this.durationTicks;
   }

   public static enum Action {
      UPSERT,
      REMOVE,
      CLEAR_ALL;
   }
}
