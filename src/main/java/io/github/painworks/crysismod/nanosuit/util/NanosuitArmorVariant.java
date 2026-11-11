package io.github.painworks.crysismod.nanosuit.util;

import io.github.painworks.crysismod.nanosuit.NanosuitGeneration;
import io.github.painworks.crysismod.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public enum NanosuitArmorVariant {
   NANOGEO(NanosuitGeneration.NANOSUIT_1,
      ModItems.NANOGEO_NANOSUIT_HELMET, ModItems.NANOGEO_NANOSUIT_CHESTPLATE,
      ModItems.NANOGEO_NANOSUIT_LEGGINGS, ModItems.NANOGEO_NANOSUIT_BOOTS),
   NANOSUIT_2(NanosuitGeneration.NANOSUIT_2,
      ModItems.NANOSUIT_2_HELMET, ModItems.NANOSUIT_2_CHESTPLATE,
      ModItems.NANOSUIT_2_LEGGINGS, ModItems.NANOSUIT_2_BOOTS);

   private final NanosuitGeneration generation;
   private final Holder<Item> helmet;
   private final Holder<Item> chestplate;
   private final Holder<Item> leggings;
   private final Holder<Item> boots;

   NanosuitArmorVariant(NanosuitGeneration generation, Holder<Item> helmet, Holder<Item> chestplate,
      Holder<Item> leggings, Holder<Item> boots) {
      this.generation = generation;
      this.helmet = helmet;
      this.chestplate = chestplate;
      this.leggings = leggings;
      this.boots = boots;
   }

   NanosuitGeneration generation() {
      return generation;
   }

   boolean matches(ItemStack stack, EquipmentSlot slot) {
      return switch (slot) {
         case HEAD -> stack.is(helmet);
         case CHEST -> stack.is(chestplate);
         case LEGS -> stack.is(leggings);
         case FEET -> stack.is(boots);
         default -> false;
      };
   }

   static NanosuitArmorVariant forGeneration(NanosuitGeneration generation) {
      if (generation == null) {
         return null;
      }
      return switch (generation) {
         case NANOSUIT_1 -> NANOGEO;
         case NANOSUIT_2 -> NANOSUIT_2;
         case LEGACY_CURRENT -> null;
      };
   }
}
