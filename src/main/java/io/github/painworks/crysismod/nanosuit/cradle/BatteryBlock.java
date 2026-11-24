package io.github.painworks.crysismod.nanosuit.cradle;

import com.mojang.serialization.MapCodec;
import io.github.painworks.crysismod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class BatteryBlock extends BaseEntityBlock {
   private static final MapCodec<BatteryBlock> CELL_CODEC = simpleCodec(properties -> new BatteryBlock(BatteryKind.CELL, properties));
   private static final MapCodec<BatteryBlock> CEPH_CODEC = simpleCodec(properties -> new BatteryBlock(BatteryKind.CEPH, properties));
   private final BatteryKind kind;
   private static final int CRYSTAL_ENERGY = 400;

   public BatteryBlock(BatteryKind kind, Properties properties) {
      super(properties);
      this.kind = kind;
   }

   BatteryKind kind() { return kind; }

   @Override
   protected MapCodec<BatteryBlock> codec() { return kind == BatteryKind.CELL ? CELL_CODEC : CEPH_CODEC; }

   @Override
   protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

   @Override
   public BatteryBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new BatteryBlockEntity(pos, state);
   }

   @Override
   protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
      Player player, InteractionHand hand, BlockHitResult hit) {
      if (!stack.is(ModItems.CEPH_ENERGY_CRYSTAL.get())) {
         return super.useItemOn(stack, state, level, pos, player, hand, hit);
      }
      if (player instanceof ServerPlayer serverPlayer && serverPlayer.serverLevel().getServer().isSameThread()
         && player.isAlive() && !player.isSpectator() && player.canInteractWithBlock(pos, 0) && level.mayInteract(player, pos)
         && player.getItemInHand(hand) == stack && !stack.isEmpty()) {
         if (kind != BatteryKind.CEPH) {
            player.displayClientMessage(Component.translatable("block.crysismod.ceph_energy_cell.ceph_only"), true);
         } else if (level.getBlockEntity(pos) instanceof BatteryBlockEntity battery) {
            int accepted = battery.addEnergy(CRYSTAL_ENERGY);
            if (accepted > 0) stack.shrink(1);
            player.displayClientMessage(Component.translatable(accepted > 0
               ? "block.crysismod.ceph_energy_cell.recharged" : "block.crysismod.ceph_energy_cell.full",
               battery.charge(), battery.kind().capacity()), true);
         }
      }
      return ItemInteractionResult.sidedSuccess(level.isClientSide);
   }

   @Override
   public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
      var stack = super.getCloneItemStack(level, pos, state);
      if (level.getBlockEntity(pos) instanceof BatteryBlockEntity battery) kind.writeCharge(stack, battery.charge());
      return stack;
   }
}
