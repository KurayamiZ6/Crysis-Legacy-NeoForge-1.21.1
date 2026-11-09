package io.github.painworks.crysismod.client.nanovision.thermal.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class ThermalBlockHeatResolver {
   public static final TagKey<Block> THERMAL_HOT_BLOCKS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("crysismod", "thermal_hot_blocks"));
   private static final Direction[] DIRECTIONS = Direction.values();

   private ThermalBlockHeatResolver() {
   }

   public static ThermalBlockHeatProfile resolve(BlockState state) {
      if (state.getFluidState().is(FluidTags.LAVA)) {
         return ThermalBlockHeatProfile.EXTREME;
      } else {
         Block block = state.getBlock();
         if (block == Blocks.FIRE || block == Blocks.SOUL_FIRE || block == Blocks.LAVA_CAULDRON) {
            return ThermalBlockHeatProfile.EXTREME;
         } else if (block == Blocks.MAGMA_BLOCK) {
            return ThermalBlockHeatProfile.WARM;
         } else if (block == Blocks.REDSTONE_TORCH || block == Blocks.REDSTONE_WALL_TORCH) {
            return isLit(state) ? ThermalBlockHeatProfile.HOT : ThermalBlockHeatProfile.NONE;
         } else if (block == Blocks.TORCH
            || block == Blocks.WALL_TORCH
            || block == Blocks.SOUL_TORCH
            || block == Blocks.SOUL_WALL_TORCH
            || block == Blocks.LANTERN
            || block == Blocks.SOUL_LANTERN
            || block == Blocks.JACK_O_LANTERN) {
            return ThermalBlockHeatProfile.HOT;
         } else if (isLit(state) && state.getLightEmission() > 0) {
            return ThermalBlockHeatProfile.HOT;
         } else {
            return state.is(THERMAL_HOT_BLOCKS) ? ThermalBlockHeatProfile.HOT : ThermalBlockHeatProfile.NONE;
         }
      }
   }

   public static boolean isExposed(BlockGetter level, BlockPos pos, BlockState state, ThermalBlockHeatProfile profile) {
      if (profile == ThermalBlockHeatProfile.NONE) {
         return false;
      } else if (isLava(state)) {
         for (Direction direction : DIRECTIONS) {
            if (!level.getBlockState(pos.relative(direction)).getFluidState().is(FluidTags.LAVA)) {
               return true;
            }
         }

         return false;
      } else if (!state.canOcclude()) {
         return true;
      } else {
         for (Direction directionx : DIRECTIONS) {
            BlockPos neighborPos = pos.relative(directionx);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (!neighbor.isSolidRender(level, neighborPos)) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean isLava(BlockState state) {
      return state.getFluidState().is(FluidTags.LAVA);
   }

   public static boolean isFire(BlockState state) {
      return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE);
   }

   private static boolean isLit(BlockState state) {
      return state.hasProperty(BlockStateProperties.LIT) && (Boolean)state.getValue(BlockStateProperties.LIT);
   }
}
