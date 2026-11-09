package io.github.painworks.crysismod.client.nanovision.thermal.block;

import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class ThermalHotBlockScanner {
   private static final MutableBlockPos CURSOR_POS = new MutableBlockPos();
   private static Long2ByteOpenHashMap activeSources = new Long2ByteOpenHashMap(512);
   private static Long2ByteOpenHashMap buildingSources = new Long2ByteOpenHashMap(512);
   private static ClientLevel scannedLevel;
   private static int centerX;
   private static int centerY;
   private static int centerZ;
   private static int radius;
   private static int side;
   private static int cursor;
   private static int totalPositions;
   private static boolean scanning;

   private ThermalHotBlockScanner() {
   }

   public static void tick(Minecraft minecraft) {
      ClientLevel level = minecraft.level;
      if (level != null && minecraft.player != null) {
         if (level != scannedLevel) {
            clear();
            scannedLevel = level;
         }

         if (NanovisionConfig.thermalHotBlocksEnabled() && NanovisionState.isActive(minecraft.player)) {
            if (!scanning) {
               beginCycle(minecraft);
            }

            int budget = NanovisionConfig.thermalMarkerQuality().hotBlockScanBudget();
            int radiusSquared = radius * radius;
            int processed = 0;

            while (cursor < totalPositions && processed < budget) {
               int index = cursor++;
               processed++;
               int localX = index % side - radius;
               int yz = index / side;
               int localZ = yz % side - radius;
               int localY = yz / side - radius;
               if (localX * localX + localY * localY + localZ * localZ <= radiusSquared) {
                  int worldY = centerY + localY;
                  if (worldY >= level.getMinBuildHeight() && worldY < level.getMaxBuildHeight()) {
                     CURSOR_POS.set(centerX + localX, worldY, centerZ + localZ);
                     if (level.hasChunkAt(CURSOR_POS)) {
                        BlockState state = level.getBlockState(CURSOR_POS);
                        ThermalBlockHeatProfile profile = ThermalBlockHeatResolver.resolve(state);
                        if (ThermalBlockHeatResolver.isExposed(level, CURSOR_POS, state, profile)) {
                           buildingSources.put(CURSOR_POS.asLong(), profile.id());
                        }
                     }
                  }
               }
            }

            if (cursor >= totalPositions) {
               Long2ByteOpenHashMap previous = activeSources;
               activeSources = buildingSources;
               buildingSources = previous;
               buildingSources.clear();
               scanning = false;
            }
         } else {
            scanning = false;
            buildingSources.clear();
         }
      } else {
         clear();
      }
   }

   static Long2ByteOpenHashMap sources() {
      return activeSources.isEmpty() && scanning ? buildingSources : activeSources;
   }

   public static void clear() {
      activeSources.clear();
      buildingSources.clear();
      scannedLevel = null;
      scanning = false;
      cursor = 0;
      totalPositions = 0;
   }

   private static void beginCycle(Minecraft minecraft) {
      radius = NanovisionConfig.thermalHotBlockMaxDistance();
      side = radius * 2 + 1;
      totalPositions = side * side * side;
      centerX = minecraft.player.blockPosition().getX();
      centerY = minecraft.player.blockPosition().getY();
      centerZ = minecraft.player.blockPosition().getZ();
      cursor = 0;
      buildingSources.clear();
      scanning = true;
   }
}
