package io.github.painworks.crysismod.nanosuit.cradle;

import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.common.util.INBTSerializable;

@EventBusSubscriber(modid = "crysismod", bus = EventBusSubscriber.Bus.GAME)
public final class CradlePatient implements INBTSerializable<CompoundTag> {
   public static final StreamCodec<RegistryFriendlyByteBuf, CradlePatient> SYNC = StreamCodec.of(
      (buffer, ignored) -> buffer.writeBoolean(true), buffer -> {
         buffer.readBoolean();
         return new CradlePatient();
      });

   private CradleProcedure procedure;
   private ResourceLocation dimension;
   private Vec3 exit;

   public CradlePatient() {}

   CradlePatient(CradleProcedure procedure, ResourceLocation dimension, Vec3 exit) {
      this.procedure = procedure;
      this.dimension = dimension;
      this.exit = exit;
   }

   public static boolean active(Player player) {
      return player != null && player.hasData(NanosuitCapabilities.CRADLE_PATIENT);
   }

   private static void release(Player player) {
      var data = player.getExistingDataOrNull(NanosuitCapabilities.CRADLE_PATIENT);
      if (data != null && data.procedure != null) data.procedure.cancel();
   }

   @Override
   public CompoundTag serializeNBT(HolderLookup.Provider registries) {
      var tag = new CompoundTag();
      if (dimension != null && exit != null) {
         tag.putString("Dimension", dimension.toString());
         tag.putDouble("X", exit.x);
         tag.putDouble("Y", exit.y);
         tag.putDouble("Z", exit.z);
      }
      return tag;
   }

   @Override
   public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
      dimension = ResourceLocation.tryParse(tag.getString("Dimension"));
      exit = dimension == null ? null : new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
   }

   @SubscribeEvent(priority = EventPriority.HIGH)
   public static void login(PlayerEvent.PlayerLoggedInEvent event) {
      if (!(event.getEntity() instanceof ServerPlayer player)) return;
      var data = player.getExistingDataOrNull(NanosuitCapabilities.CRADLE_PATIENT);
      if (data == null || data.procedure != null) return;
      player.removeData(NanosuitCapabilities.CRADLE_PATIENT);
      if (data.dimension == null || data.exit == null) return;
      var level = player.serverLevel().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, data.dimension));
      if (level != null) player.teleportTo(level, data.exit.x, data.exit.y, data.exit.z, player.getYRot(), player.getXRot());
   }

   @SubscribeEvent
   public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { release(event.getEntity()); }

   @SubscribeEvent
   public static void dimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) { release(event.getEntity()); }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public static void death(LivingDeathEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) release(player);
   }

   @SubscribeEvent
   public static void stop(ServerStoppingEvent event) {
      for (var player : event.getServer().getPlayerList().getPlayers()) release(player);
   }
}
