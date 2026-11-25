package io.github.painworks.crysismod.nanosuit.cradle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
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
import io.github.painworks.crysismod.registry.ModBlocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class NanosuitCradleBlock extends BaseEntityBlock {
   private static final MapCodec<NanosuitCradleBlock> CODEC = simpleCodec(NanosuitCradleBlock::new);
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

   public NanosuitCradleBlock(Properties properties) {
      super(properties);
      // Old single-block Cradles have no saved part property and keep their inventories.
      registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, Part.LEGACY));
   }

   @Override
   protected MapCodec<NanosuitCradleBlock> codec() {
      return CODEC;
   }

   @Override
   public NanosuitCradleBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return state.getValue(PART) == Part.EXTENSION ? null : new NanosuitCradleBlockEntity(pos, state);
   }

   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      if (level.isClientSide || state.getValue(PART) != Part.MASTER) return null;
      return createTickerHelper(type, ModBlocks.NANOSUIT_CRADLE_ENTITY.get(), (world, pos, block, cradle) -> cradle.tickProcedure());
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(FACING, PART);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return placementState(context, defaultBlockState());
   }

   static BlockState placementState(BlockPlaceContext context, BlockState base) {
      Direction facing = context.getHorizontalDirection();
      BlockPos extension = context.getClickedPos().relative(facing);
      BlockState master = base.setValue(FACING, facing).setValue(PART, Part.MASTER);
      return canPlaceExtension(context, extension, master) ? master : null;
   }

   private static boolean canPlaceExtension(BlockPlaceContext context, BlockPos pos, BlockState state) {
      Level level = context.getLevel();
      Player player = context.getPlayer();
      return !level.isOutsideBuildHeight(pos) && level.getWorldBorder().isWithinBounds(pos)
         && (player == null || level.mayInteract(player, pos)) && level.isEmptyBlock(pos)
         && level.isUnobstructed(state, pos, player == null ? CollisionContext.empty() : CollisionContext.of(player));
   }

   static BlockPos otherPos(BlockPos pos, BlockState state) {
      Direction facing = state.getValue(FACING);
      return pos.relative(state.getValue(PART) == Part.MASTER ? facing : facing.getOpposite());
   }

   static boolean isPartner(BlockState state, BlockState candidate) {
      Part expected = state.getValue(PART) == Part.MASTER ? Part.EXTENSION : Part.MASTER;
      return candidate.is(state.getBlock()) && candidate.getValue(PART) == expected && candidate.getValue(FACING) == state.getValue(FACING);
   }

   private NanosuitCradleBlockEntity resolve(Level level, BlockPos pos, BlockState state) {
      Part part = state.getValue(PART);
      BlockPos master = pos;
      if (part != Part.LEGACY) {
         BlockPos other = otherPos(pos, state);
         if (!isPartner(state, level.getBlockState(other))) return null;
         if (part == Part.EXTENSION) master = other;
      }
      return level.getBlockEntity(master) instanceof NanosuitCradleBlockEntity cradle ? cradle : null;
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
   protected RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Override
   protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
      Player player, InteractionHand hand, BlockHitResult hit) {
      if (hand != InteractionHand.MAIN_HAND) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
      if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      if (player instanceof ServerPlayer serverPlayer) {
         var cradle = resolve(level, pos, state);
         String result;
         if (cradle == null) result = "denied";
         else if (state.getValue(PART) != Part.LEGACY && BatteryKind.of(stack) != null) result = cradle.exchangeBattery(serverPlayer, pos);
         else result = cradle.insert(serverPlayer, pos);
         notifyPlayer(serverPlayer, result);
      }
      return ItemInteractionResult.sidedSuccess(level.isClientSide);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (player instanceof ServerPlayer serverPlayer) {
         var cradle = resolve(level, pos, state);
         String result;
         if (cradle == null) result = "denied";
         else if (player.isSecondaryUseActive()) {
            boolean batteryFace = state.getValue(PART) != Part.LEGACY && hit.getDirection() == state.getValue(FACING).getClockWise();
            result = batteryFace ? cradle.retrieveBattery(serverPlayer, pos) : cradle.retrievePiece(serverPlayer, pos);
         }
         else if (hit.getDirection() == Direction.UP && cradle.useProcedure(serverPlayer, pos)) return InteractionResult.CONSUME;
         else result = cradle.status(serverPlayer, pos);
         notifyPlayer(serverPlayer, result);
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   private static void notifyPlayer(ServerPlayer player, String result) {
      player.displayClientMessage(Component.translatable("block.crysismod.nanosuit_cradle." + result), true);
   }

   @Override
   protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean movedByPiston) {
      if (!state.is(next.getBlock()) && level instanceof ServerLevel server) {
         if (level.getBlockEntity(pos) instanceof NanosuitCradleBlockEntity cradle) cradle.dropContents(server);
         removePartner(server, pos, state);
      }
      super.onRemove(state, level, pos, next, movedByPiston);
   }

   static void removePartner(ServerLevel server, BlockPos pos, BlockState state) {
      if (state.getValue(PART) == Part.LEGACY) return;
      BlockPos other = otherPos(pos, state);
      if (isPartner(state, server.getBlockState(other))) server.removeBlock(other, false);
   }

   public enum Part implements StringRepresentable {
      LEGACY("legacy"), MASTER("master"), EXTENSION("extension");

      private final String serializedName;
      Part(String serializedName) { this.serializedName = serializedName; }
      @Override public String getSerializedName() { return serializedName; }
   }

   public static final class PlacementItem extends BlockItem {
      public PlacementItem(Block block, Properties properties) { super(block, properties); }

      @Override
      protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
         Level level = context.getLevel();
         BlockPos master = context.getClickedPos();
         BlockPos extension = otherPos(master, state);
         if (!canPlaceExtension(context, extension, state)) return false;
         BlockState prior = level.getBlockState(master);
         // Place both parts inside the same vanilla/NeoForge item placement transaction.
         if (!super.placeBlock(context, state)) return false;
         if (level.setBlock(extension, state.setValue(PART, Part.EXTENSION), Block.UPDATE_ALL_IMMEDIATE)) return true;
         level.setBlock(master, prior, Block.UPDATE_ALL);
         return false;
      }
   }
}
