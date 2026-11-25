package io.github.painworks.crysismod.nanosuit.integration;

import io.github.painworks.crysismod.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class Undersuit {
   private Undersuit() {}

   public static boolean isPiece(ItemStack stack, EquipmentSlot slot) {
      return stack.getCount() == 1 && switch (slot) {
         case HEAD -> stack.is(ModItems.UNDERSUIT_HOOD.get());
         case CHEST -> stack.is(ModItems.UNDERSUIT_TOP.get());
         case LEGS -> stack.is(ModItems.UNDERSUIT_LEGGINGS.get());
         case FEET -> stack.is(ModItems.UNDERSUIT_BOOTS.get());
         default -> false;
      };
   }

   public static boolean isComplete(Player player) {
      return isPiece(player.getItemBySlot(EquipmentSlot.HEAD), EquipmentSlot.HEAD)
         && isPiece(player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST)
         && isPiece(player.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS)
         && isPiece(player.getItemBySlot(EquipmentSlot.FEET), EquipmentSlot.FEET);
   }
}
