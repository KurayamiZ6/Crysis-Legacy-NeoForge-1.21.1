package io.github.painworks.crysismod.nanosuit.cradle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CephCrystalClusterBlock extends Block {
   public static final DirectionProperty FACING = BlockStateProperties.FACING;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
   public static final int MATURE = 3;
   private static final VoxelShape[][] SHAPES = shapes();

   public CephCrystalClusterBlock(Properties properties) {
      super(properties);
      registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(AGE, MATURE));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(FACING, AGE);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return defaultBlockState().setValue(FACING, context.getClickedFace());
   }

   @Override
   protected BlockState rotate(BlockState state, Rotation rotation) {
      return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
   }

   @Override
   protected BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation(state.getValue(FACING)));
   }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPES[state.getValue(AGE)][state.getValue(FACING).ordinal()];
   }

   private static VoxelShape[][] shapes() {
      var shapes = new VoxelShape[4][Direction.values().length];
      int[] heights = {3, 4, 5, 7};
      int[] insets = {4, 3, 3, 3};
      for (int age = 0; age < shapes.length; age++) {
         int height = heights[age];
         int lo = insets[age];
         int hi = 16 - lo;
         for (Direction face : Direction.values()) {
            shapes[age][face.ordinal()] = switch (face) {
               case UP -> box(lo, 0, lo, hi, height, hi);
               case DOWN -> box(lo, 16 - height, lo, hi, 16, hi);
               case NORTH -> box(lo, lo, 16 - height, hi, hi, 16);
               case SOUTH -> box(lo, lo, 0, hi, hi, height);
               case EAST -> box(0, lo, lo, height, hi, hi);
               case WEST -> box(16 - height, lo, lo, 16, hi, hi);
            };
         }
      }
      return shapes;
   }
}
