package io.github.painworks.crysismod.nanosuit.cradle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class NanosuitCradleBlock extends BaseEntityBlock {
   private static final MapCodec<NanosuitCradleBlock> CODEC = simpleCodec(NanosuitCradleBlock::new);

   public NanosuitCradleBlock(Properties properties) {
      super(properties);
   }

   @Override
   protected MapCodec<NanosuitCradleBlock> codec() {
      return CODEC;
   }

   @Override
   public NanosuitCradleBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new NanosuitCradleBlockEntity(pos, state);
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
      if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof NanosuitCradleBlockEntity cradle) {
         notifyPlayer(serverPlayer, cradle.insert(serverPlayer));
      }
      return ItemInteractionResult.sidedSuccess(level.isClientSide);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof NanosuitCradleBlockEntity cradle) {
         if (!player.isSecondaryUseActive()) notifyPlayer(serverPlayer, cradle.equip(serverPlayer));
         else if (hit.getDirection() == Direction.UP) notifyPlayer(serverPlayer, cradle.retrievePiece(serverPlayer));
         else notifyPlayer(serverPlayer, cradle.storeSuit(serverPlayer));
      }
      return InteractionResult.sidedSuccess(level.isClientSide);
   }

   private static void notifyPlayer(ServerPlayer player, String result) {
      player.displayClientMessage(Component.translatable("block.crysismod.nanosuit_cradle." + result), true);
   }

   @Override
   protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean movedByPiston) {
      if (!state.is(next.getBlock()) && level instanceof ServerLevel server
         && level.getBlockEntity(pos) instanceof NanosuitCradleBlockEntity cradle) {
         cradle.dropContents(server);
      }
      super.onRemove(state, level, pos, next, movedByPiston);
   }
}
