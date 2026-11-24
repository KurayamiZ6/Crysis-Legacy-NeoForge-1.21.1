package io.github.painworks.crysismod.nanosuit.cradle;

import io.github.painworks.crysismod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class CephCrystalCoreBlock extends Block {
   private static final int GROWTH_CHANCE = 2;

   public CephCrystalCoreBlock(Properties properties) {
      super(properties);
   }

   @Override
   protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (random.nextInt(GROWTH_CHANCE) != 0) return;
      Direction face = Direction.getRandom(random);
      BlockPos next = pos.relative(face);
      if (level.isOutsideBuildHeight(next) || !level.getWorldBorder().isWithinBounds(next)) return;
      var chunk = level.getChunkSource().getChunkNow(next.getX() >> 4, next.getZ() >> 4);
      if (chunk == null) return;
      BlockState neighbor = chunk.getBlockState(next);
      var cluster = ModBlocks.CEPH_CRYSTAL_CLUSTER.get();
      if (neighbor.isAir()) {
         level.setBlockAndUpdate(next, cluster.defaultBlockState().setValue(CephCrystalClusterBlock.FACING, face)
            .setValue(CephCrystalClusterBlock.AGE, 0));
      } else if (neighbor.is(cluster) && neighbor.getValue(CephCrystalClusterBlock.FACING) == face
         && neighbor.getValue(CephCrystalClusterBlock.AGE) < CephCrystalClusterBlock.MATURE) {
         level.setBlockAndUpdate(next, neighbor.cycle(CephCrystalClusterBlock.AGE));
      }
   }
}
