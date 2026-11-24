package io.github.painworks.crysismod.nanosuit.cradle;

import io.github.painworks.crysismod.registry.ModDataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public enum BatteryKind {
   CELL(1_000), CEPH(2_000);

   private final int capacity;

   BatteryKind(int capacity) {
      this.capacity = capacity;
   }

   public int capacity() { return capacity; }

   public static BatteryKind of(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof BatteryItem item ? item.kind() : null;
   }

   public int charge(ItemStack stack) {
      return clamp(stack.getOrDefault(ModDataComponents.BATTERY_CHARGE.get(), capacity));
   }

   void writeCharge(ItemStack stack, int charge) {
      stack.set(ModDataComponents.BATTERY_CHARGE.get(), clamp(charge));
   }

   int clamp(int charge) {
      return Mth.clamp(charge, 0, capacity);
   }
}
