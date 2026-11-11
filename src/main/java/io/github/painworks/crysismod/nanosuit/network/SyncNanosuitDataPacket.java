package io.github.painworks.crysismod.nanosuit.network;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.client.ClientNanosuitPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncNanosuitDataPacket implements CustomPacketPayload {
   public static final Type<SyncNanosuitDataPacket> TYPE = NanosuitNetwork.type("sync_nanosuit_data");
   public static final StreamCodec<FriendlyByteBuf, SyncNanosuitDataPacket> STREAM_CODEC =
      StreamCodec.ofMember((packet, buffer) -> encode(packet, buffer), SyncNanosuitDataPacket::decode);

   @Override
   public Type<SyncNanosuitDataPacket> type() {
      return TYPE;
   }

   private final float energy;
   private final NanosuitMode mode;
   private final boolean sprintBoostReady;
   private final boolean recoveryLocked;

   public SyncNanosuitDataPacket(float energy, NanosuitMode mode, boolean sprintBoostReady, boolean recoveryLocked) {
      this.energy = energy;
      this.mode = mode;
      this.sprintBoostReady = sprintBoostReady;
      this.recoveryLocked = recoveryLocked;
   }

   public float getEnergy() {
      return this.energy;
   }

   public NanosuitMode getMode() {
      return this.mode;
   }

   public boolean isSprintBoostReady() {
      return this.sprintBoostReady;
   }

   public boolean isRecoveryLocked() {
      return this.recoveryLocked;
   }

   public static void encode(SyncNanosuitDataPacket packet, FriendlyByteBuf buffer) {
      buffer.writeFloat(packet.energy);
      buffer.writeVarInt(packet.mode.wireId());
      buffer.writeBoolean(packet.sprintBoostReady);
      buffer.writeBoolean(packet.recoveryLocked);
   }

   public static SyncNanosuitDataPacket decode(FriendlyByteBuf buffer) {
      return new SyncNanosuitDataPacket(
         buffer.readFloat(), NanosuitMode.fromWireId(buffer.readVarInt()), buffer.readBoolean(), buffer.readBoolean()
      );
   }

   public static void handle(SyncNanosuitDataPacket packet, IPayloadContext context) {
      ClientNanosuitPacketHandlers.handleSyncNanosuitData(packet);
   }
}
