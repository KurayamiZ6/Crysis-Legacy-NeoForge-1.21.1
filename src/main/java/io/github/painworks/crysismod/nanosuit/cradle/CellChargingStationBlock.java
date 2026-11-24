package io.github.painworks.crysismod.nanosuit.cradle;

import com.mojang.serialization.MapCodec;
import io.github.painworks.crysismod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class CellChargingStationBlock extends BaseEntityBlock {
   private static final MapCodec<CellChargingStationBlock> CODEC = simpleCodec(CellChargingStationBlock::new);
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final BooleanProperty LIT = BlockStateProperties.LIT;

   public CellChargingStationBlock(Properties properties) {
      super(properties);
      registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
   }

   @Override
   protected MapCodec<CellChargingStationBlock> codec() { return CODEC; }

   @Override
   protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(FACING, LIT);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
   public CellChargingStationBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new CellChargingStationBlockEntity(pos, state);
   }

   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide ? null : createTickerHelper(type, ModBlocks.CELL_CHARGING_STATION_ENTITY.get(),
         CellChargingStationBlockEntity::tick);
   }

   @Override
   protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
      Player player, InteractionHand hand, BlockHitResult hit) {
      if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      if (hand != InteractionHand.MAIN_HAND || BatteryKind.of(stack) == null) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
      if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof CellChargingStationBlockEntity station) {
         station.exchange(serverPlayer);
      }
      return ItemInteractionResult.sidedSuccess(level.isClientSide);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof CellChargingStationBlockEntity station) {
         if (player.isSecondaryUseActive()) station.retrieve(serverPlayer);
         else station.inspect(serverPlayer);
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   @Override
   protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean movedByPiston) {
      if (!state.is(next.getBlock()) && level instanceof ServerLevel server
         && level.getBlockEntity(pos) instanceof CellChargingStationBlockEntity station) {
         station.dropBattery(server);
      }
      super.onRemove(state, level, pos, next, movedByPiston);
   }
}
