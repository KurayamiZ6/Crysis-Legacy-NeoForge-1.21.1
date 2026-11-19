package io.github.painworks.crysismod.nanosuit.network;

import io.github.painworks.crysismod.nanosuit.client.ClassicEnemyContacts;
import io.github.painworks.crysismod.nanosuit.threat.ThreatLevel;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Bounded, observer-specific snapshots; client never infers another mob's server target. */
public record SyncClassicThreatsPacket(List<Contact> contacts) implements CustomPacketPayload {
   public static final int MAX_CONTACTS = 128;
   public record Contact(int id, UUID uuid, ThreatLevel threat) {}
   public static final Type<SyncClassicThreatsPacket> TYPE = NanosuitNetwork.type("classic_threat_contacts");
   public static final StreamCodec<FriendlyByteBuf, SyncClassicThreatsPacket> STREAM_CODEC = StreamCodec.ofMember(
      (packet, buffer) -> {
         buffer.writeVarInt(packet.contacts.size());
         for (Contact contact : packet.contacts) {
            buffer.writeVarInt(contact.id()); buffer.writeUUID(contact.uuid()); buffer.writeEnum(contact.threat());
         }
      }, buffer -> {
         int count = buffer.readVarInt();
         if (count < 0 || count > MAX_CONTACTS) throw new IllegalArgumentException("Invalid contact count");
         var contacts = new java.util.ArrayList<Contact>(count);
         for (int i=0;i<count;i++) contacts.add(new Contact(buffer.readVarInt(),buffer.readUUID(),buffer.readEnum(ThreatLevel.class)));
         return new SyncClassicThreatsPacket(List.copyOf(contacts));
      });
   @Override public Type<SyncClassicThreatsPacket> type() { return TYPE; }
   public static void handle(SyncClassicThreatsPacket packet, IPayloadContext context) {
      ClassicEnemyContacts.update(context.player(), packet.contacts());
   }
}
