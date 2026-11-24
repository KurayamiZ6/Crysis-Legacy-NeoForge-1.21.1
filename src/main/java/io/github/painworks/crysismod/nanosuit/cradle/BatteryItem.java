package io.github.painworks.crysismod.nanosuit.cradle;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class BatteryItem extends BlockItem {
   public BatteryItem(BatteryBlock block, Properties properties) {
      super(block, properties.stacksTo(1));
   }

   BatteryKind kind() { return ((BatteryBlock) getBlock()).kind(); }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      super.appendHoverText(stack, context, tooltip, flag);
      tooltip.add(Component.translatable("item.crysismod.power_cell.charge", kind().charge(stack), kind().capacity())
         .withStyle(ChatFormatting.GRAY));
   }
}
