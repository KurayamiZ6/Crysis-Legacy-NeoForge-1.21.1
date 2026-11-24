package io.github.painworks.crysismod.nanosuit.cradle;

import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlock.Part;
import io.github.painworks.crysismod.registry.ModBlocks;
import java.util.Comparator;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CradleControllerBlockEntity extends BlockEntity implements MenuProvider {
   private static final int RANGE = 5;
   private static final Comparator<BlockPos> POSITION_ORDER = Comparator.<BlockPos>comparingInt(BlockPos::getX)
      .thenComparingInt(BlockPos::getY).thenComparingInt(BlockPos::getZ);
   private BlockPos linkedMaster;

   public enum Connection {
      DISCONNECTED("disconnected"), CONNECTED("connected"), UNLOADED("unloaded"), MISSING("missing");

      private final String key;
      Connection(String key) { this.key = key; }
      public String key() { return key; }
   }

   record Link(Connection state, NanosuitCradleBlockEntity cradle) {}

   public CradleControllerBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlocks.CRADLE_CONTROLLER_ENTITY.get(), pos, state);
   }

   @Override
   public Component getDisplayName() {
      return Component.translatable("block.crysismod.cradle_controller");
   }

   @Override
   public CradleControllerMenu createMenu(int id, Inventory inventory, Player player) {
      return new CradleControllerMenu(id, inventory, this);
   }

   boolean canUse(Player player) {
      if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer) || !server.getServer().isSameThread()
         || !player.isAlive() || player.isSpectator() || player.level() != server || isRemoved()
         || !player.canInteractWithBlock(worldPosition, 0) || !server.mayInteract(player, worldPosition)) return false;
      var chunk = server.getChunkSource().getChunkNow(worldPosition.getX() >> 4, worldPosition.getZ() >> 4);
      return chunk != null && chunk.getBlockEntity(worldPosition) == this;
   }

   Link link() {
      if (linkedMaster == null) return new Link(Connection.DISCONNECTED, null);
      return level instanceof ServerLevel server ? resolve(server, linkedMaster) : new Link(Connection.UNLOADED, null);
   }

   private static Link resolve(ServerLevel server, BlockPos master) {
      var chunk = server.getChunkSource().getChunkNow(master.getX() >> 4, master.getZ() >> 4);
      if (chunk == null) return new Link(Connection.UNLOADED, null);
      var state = chunk.getBlockState(master);
      if (!isPart(state, Part.MASTER)) return new Link(Connection.MISSING, null);
      var facing = state.getValue(NanosuitCradleBlock.FACING);
      var extension = master.relative(facing);
      var extensionState = loadedState(server, extension);
      if (extensionState == null) return new Link(Connection.UNLOADED, null);
      if (!isPart(extensionState, Part.EXTENSION) || extensionState.getValue(NanosuitCradleBlock.FACING) != facing
         || !(chunk.getBlockEntity(master) instanceof NanosuitCradleBlockEntity cradle) || cradle.isRemoved()) {
         return new Link(Connection.MISSING, null);
      }
      return new Link(Connection.CONNECTED, cradle);
   }

   private static BlockState loadedState(ServerLevel server, BlockPos pos) {
      var chunk = server.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
      return chunk == null ? null : chunk.getBlockState(pos);
   }

   private static boolean isPart(BlockState state, Part part) {
      return state.is(ModBlocks.NANOSUIT_CRADLE.get()) && state.getValue(NanosuitCradleBlock.PART) == part;
   }

   boolean reconnect() {
      if (!(level instanceof ServerLevel server) || !server.getServer().isSameThread()) return false;
      var connection = link().state();
      if (connection == Connection.CONNECTED || connection == Connection.UNLOADED) return false;

      BlockPos closest = null;
      double closestDistance = Double.POSITIVE_INFINITY;
      var checked = new HashSet<BlockPos>();
      for (var pos : BlockPos.betweenClosed(worldPosition.offset(-RANGE, -RANGE, -RANGE), worldPosition.offset(RANGE, RANGE, RANGE))) {
         if (worldPosition.distSqr(pos) > RANGE * RANGE) continue;
         var state = loadedState(server, pos);
         if (state == null || !state.is(ModBlocks.NANOSUIT_CRADLE.get())) continue;
         var facing = state.getValue(NanosuitCradleBlock.FACING);
         var part = state.getValue(NanosuitCradleBlock.PART);
         if (part == Part.LEGACY) continue;
         var master = part == Part.MASTER ? pos.immutable() : pos.relative(facing.getOpposite());
         if (!checked.add(master)) continue;
         var candidate = resolve(server, master);
         if (candidate.cradle() == null) continue;
         var extension = master.relative(candidate.cradle().getBlockState().getValue(NanosuitCradleBlock.FACING));
         double distance = Math.min(worldPosition.distSqr(master), worldPosition.distSqr(extension));
         if (distance <= RANGE * RANGE && (closest == null || distance < closestDistance
            || distance == closestDistance && POSITION_ORDER.compare(master, closest) < 0)) {
            closestDistance = distance;
            closest = master;
         }
      }
      if (closest == null) return false;
      linkedMaster = closest;
      setChanged();
      return true;
   }

   @Override
   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.saveAdditional(tag, registries);
      if (linkedMaster != null) tag.putLong("LinkedMaster", linkedMaster.asLong());
   }

   @Override
   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.loadAdditional(tag, registries);
      linkedMaster = tag.contains("LinkedMaster", Tag.TAG_LONG) ? BlockPos.of(tag.getLong("LinkedMaster")) : null;
   }
}
