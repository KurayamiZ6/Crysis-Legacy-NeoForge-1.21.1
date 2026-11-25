package io.github.painworks.crysismod.nanosuit.cradle;

import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.integration.BiologicalSuitAttachment;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid = "crysismod", bus = EventBusSubscriber.Bus.GAME)
final class CradleTransferRecovery {
   private CradleTransferRecovery() {}

   static Path prepare(NanosuitCradleBlockEntity cradle, ServerPlayer player, Vec3 exit) throws IOException {
      var server = player.serverLevel().getServer();
      Path path = journal(server, player.getUUID());
      if (Files.exists(path)) throw new IOException("Previous Cradle transfer still needs recovery");
      // Save the existing installation and inventories before recording the undo image.
      server.saveEverything(true, true, true);
      var tag = new CompoundTag();
      tag.putString("Dimension", player.level().dimension().location().toString());
      tag.putLong("CradlePos", cradle.getBlockPos().asLong());
      tag.put("Cradle", cradle.saveWithoutMetadata(player.registryAccess()));
      tag.put("Inventory", player.getInventory().save(new net.minecraft.nbt.ListTag()));
      var link = player.getExistingDataOrNull(NanosuitCapabilities.BIOLOGICAL_SUIT);
      if (link != null) tag.put("BiologicalSuit", link.serializeNBT(player.registryAccess()));
      tag.putFloat("Health", player.getHealth());
      tag.putDouble("X", exit.x);
      tag.putDouble("Y", exit.y);
      tag.putDouble("Z", exit.z);
      write(path, tag);
      return path;
   }

   static void complete(MinecraftServer server, Path path) {
      server.saveEverything(true, true, true);
      try {
         Files.delete(path);
      } catch (IOException exception) {
         throw new UncheckedIOException("Cannot finalize Cradle transfer journal", exception);
      }
   }

   static void reportFailure(IOException exception) {
      LogUtils.getLogger().error("Could not prepare durable Cradle transfer", exception);
   }

   @SubscribeEvent
   public static void restoreCradles(ServerStartedEvent event) {
      var server = event.getServer();
      Path folder = directory(server);
      if (!Files.isDirectory(folder)) return;
      try (var files = Files.list(folder)) {
         for (Path path : files.filter(file -> file.getFileName().toString().endsWith(".nbt")).toList()) {
            var tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            if (tag.getBoolean("CradleRestored")) continue;
            var pos = BlockPos.of(tag.getLong("CradlePos"));
            var level = server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("Dimension"))));
            if (level == null || !(level.getBlockEntity(pos) instanceof NanosuitCradleBlockEntity cradle)) {
               throw new IOException("Cannot find Cradle for pending transfer at " + pos);
            }
            cradle.loadWithComponents(tag.getCompound("Cradle"), level.registryAccess());
            cradle.publishProcedureInventory();
            level.getChunkSource().save(true);
            tag.putBoolean("CradleRestored", true);
            write(path, tag);
         }
      } catch (IOException exception) {
         throw new UncheckedIOException("Cradle transfer recovery failed", exception);
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public static void restorePlayer(PlayerEvent.PlayerLoggedInEvent event) {
      if (!(event.getEntity() instanceof ServerPlayer player)) return;
      var server = player.serverLevel().getServer();
      Path path = journal(server, player.getUUID());
      if (!Files.isRegularFile(path)) return;
      try {
         var tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
         if (!tag.getBoolean("CradleRestored")) throw new IOException("Cradle has not been recovered");
         player.getInventory().load(tag.getList("Inventory", Tag.TAG_COMPOUND));
         if (tag.contains("BiologicalSuit", Tag.TAG_COMPOUND)) {
            var link = new BiologicalSuitAttachment();
            link.deserializeNBT(player.registryAccess(), tag.getCompound("BiologicalSuit"));
            player.setData(NanosuitCapabilities.BIOLOGICAL_SUIT, link);
         } else {
            player.removeData(NanosuitCapabilities.BIOLOGICAL_SUIT);
         }
         player.setHealth(tag.getFloat("Health"));
         player.removeData(NanosuitCapabilities.CRADLE_PATIENT);
         var level = server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("Dimension"))));
         if (level != null) player.teleportTo(level, tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"), player.getYRot(), player.getXRot());
         complete(server, path);
      } catch (IOException exception) {
         throw new UncheckedIOException("Cradle wearer recovery failed", exception);
      }
   }

   private static Path directory(MinecraftServer server) {
      return server.getWorldPath(LevelResource.ROOT).resolve("crysismod-cradle-transfers");
   }

   private static Path journal(MinecraftServer server, UUID player) {
      return directory(server).resolve(player + ".nbt");
   }

   private static void write(Path path, CompoundTag tag) throws IOException {
      Files.createDirectories(path.getParent());
      Path temporary = Files.createTempFile(path.getParent(), "cradle-", ".tmp");
      try {
         NbtIo.writeCompressed(tag, temporary);
         try (var file = FileChannel.open(temporary, StandardOpenOption.WRITE)) { file.force(true); }
         Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } finally {
         Files.deleteIfExists(temporary);
      }
   }
}
