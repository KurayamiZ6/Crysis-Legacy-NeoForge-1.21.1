package io.github.painworks.crysismod.nanosuit.cradle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public final class BatteryBlock extends BaseEntityBlock {
   private static final MapCodec<BatteryBlock> CELL_CODEC = simpleCodec(properties -> new BatteryBlock(BatteryKind.CELL, properties));
   private static final MapCodec<BatteryBlock> CEPH_CODEC = simpleCodec(properties -> new BatteryBlock(BatteryKind.CEPH, properties));
   private final BatteryKind kind;

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
   public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
      var stack = super.getCloneItemStack(level, pos, state);
      if (level.getBlockEntity(pos) instanceof BatteryBlockEntity battery) kind.writeCharge(stack, battery.charge());
      return stack;
   }
}
