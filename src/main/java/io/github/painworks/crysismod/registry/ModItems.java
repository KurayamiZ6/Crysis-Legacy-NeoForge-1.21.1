package io.github.painworks.crysismod.registry;

import io.github.painworks.crysismod.nanosuit.item.ExperimentalNanoGeoArmorItem;
import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, "crysismod");
   public static final DeferredHolder<Item, Item> NANOGEO_NANOSUIT_HELMET = registerArmor("nanogeo_nanosuit_helmet", Type.HELMET);
   public static final DeferredHolder<Item, Item> NANOGEO_NANOSUIT_CHESTPLATE = registerArmor("nanogeo_nanosuit_chestplate", Type.CHESTPLATE);
   public static final DeferredHolder<Item, Item> NANOGEO_NANOSUIT_LEGGINGS = registerArmor("nanogeo_nanosuit_leggings", Type.LEGGINGS);
   public static final DeferredHolder<Item, Item> NANOGEO_NANOSUIT_BOOTS = registerArmor("nanogeo_nanosuit_boots", Type.BOOTS);
   public static final DeferredHolder<Item, Item> NANOSUIT_2_HELMET = registerArmor("nanosuit_2_helmet", Type.HELMET);
   public static final DeferredHolder<Item, Item> NANOSUIT_2_CHESTPLATE = registerArmor("nanosuit_2_chestplate", Type.CHESTPLATE);
   public static final DeferredHolder<Item, Item> NANOSUIT_2_LEGGINGS = registerArmor("nanosuit_2_leggings", Type.LEGGINGS);
   public static final DeferredHolder<Item, Item> NANOSUIT_2_BOOTS = registerArmor("nanosuit_2_boots", Type.BOOTS);
   public static final DeferredHolder<Item, BlockItem> NANOSUIT_CRADLE = ITEMS.register(
      "nanosuit_cradle", () -> new NanosuitCradleBlock.PlacementItem(ModBlocks.NANOSUIT_CRADLE.get(), new Properties())
   );

   private ModItems() {
   }

   private static DeferredHolder<Item, Item> registerArmor(String name, Type type) {
      return ITEMS.register(name, () -> new ExperimentalNanoGeoArmorItem(
         ArmorMaterials.NETHERITE, type, new Properties().durability(type.getDurability(37))
      ));
   }
}
