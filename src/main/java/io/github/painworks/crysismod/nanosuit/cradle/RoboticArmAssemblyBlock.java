package io.github.painworks.crysismod.nanosuit.cradle;

import static io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlock.FACING;
import static io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlock.PART;

import com.mojang.serialization.MapCodec;
import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlock.Part;
import io.github.painworks.crysismod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RoboticArmAssemblyBlock extends BaseEntityBlock {
   private static final MapCodec<RoboticArmAssemblyBlock> CODEC = simpleCodec(RoboticArmAssemblyBlock::new);
   private static final VoxelShape SHAPE = box(0, 3, 0, 16, 16, 16);

   public RoboticArmAssemblyBlock(Properties properties) {
      super(properties);
      registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, Part.MASTER));
   }

   @Override
   protected MapCodec<RoboticArmAssemblyBlock> codec() { return CODEC; }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(FACING, PART);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return NanosuitCradleBlock.placementState(context, defaultBlockState());
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
   protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

   @Override
   protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }

   @Override
   public RoboticArmAssemblyBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return state.getValue(PART) == Part.MASTER ? new RoboticArmAssemblyBlockEntity(pos, state) : null;
   }

   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      if (level.isClientSide || state.getValue(PART) != Part.MASTER) return null;
      return createTickerHelper(type, ModBlocks.ROBOTIC_ARM_ASSEMBLY_ENTITY.get(), RoboticArmAssemblyBlockEntity::tick);
   }

   @Override
   protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
      if (level instanceof ServerLevel server && !state.is(next.getBlock())) {
         NanosuitCradleBlock.removePartner(server, pos, state);
      }
      super.onRemove(state, level, pos, next, moving);
   }
}
