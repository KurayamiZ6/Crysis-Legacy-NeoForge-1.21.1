package io.github.painworks.crysismod.nanosuit.integration;

import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

public final class BiologicalSuitAttachment implements INBTSerializable<CompoundTag> {
   // Clients receive only the identity needed to lock their armor interactions.
   public static final StreamCodec<RegistryFriendlyByteBuf, BiologicalSuitAttachment> LOCK_SYNC = StreamCodec.of(
      (buf, data) -> {
         buf.writeBoolean(data.identity != null);
         if (data.identity != null) buf.writeUUID(data.identity);
      }, buf -> new BiologicalSuitAttachment(buf.readBoolean() ? buf.readUUID() : null));

   UUID identity;
   final NonNullList<ItemStack> undersuit = NonNullList.withSize(4, ItemStack.EMPTY);
   final NonNullList<ItemStack> deathEquipment = NonNullList.withSize(4, ItemStack.EMPTY);

   public BiologicalSuitAttachment() {}

   BiologicalSuitAttachment(UUID identity) {
      this.identity = identity;
   }

   @Override
   public CompoundTag serializeNBT(HolderLookup.Provider registries) {
      var tag = new CompoundTag();
      if (identity != null) tag.putUUID("Suit", identity);
      tag.put("Undersuit", ContainerHelper.saveAllItems(new CompoundTag(), undersuit, false, registries));
      tag.put("DeathEquipment", ContainerHelper.saveAllItems(new CompoundTag(), deathEquipment, false, registries));
      return tag;
   }

   @Override
   public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
      identity = tag.hasUUID("Suit") ? tag.getUUID("Suit") : null;
      undersuit.clear();
      deathEquipment.clear();
      ContainerHelper.loadAllItems(tag.getCompound("Undersuit"), undersuit, registries);
      ContainerHelper.loadAllItems(tag.getCompound("DeathEquipment"), deathEquipment, registries);
   }
}
