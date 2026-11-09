package io.github.painworks.crysismod.registry;

import io.github.painworks.crysismod.nanosuit.item.ExperimentalNanoGeoArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModItems {
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, "crysismod");
   public static final DeferredHolder<Item, Item> NANOGEO_NANOSUIT_HELMET = ITEMS.register(
      "nanogeo_nanosuit_helmet", () -> new ExperimentalNanoGeoArmorItem(ArmorMaterials.NETHERITE, Type.HELMET, new Properties().durability(Type.HELMET.getDurability(37)))
   );
   public static final DeferredHolder<Item, Item> NANOGEO_NANOSUIT_CHESTPLATE = ITEMS.register(
      "nanogeo_nanosuit_chestplate", () -> new ExperimentalNanoGeoArmorItem(ArmorMaterials.NETHERITE, Type.CHESTPLATE, new Properties().durability(Type.CHESTPLATE.getDurability(37)))
   );
   public static final DeferredHolder<Item, Item> NANOGEO_NANOSUIT_LEGGINGS = ITEMS.register(
      "nanogeo_nanosuit_leggings", () -> new ExperimentalNanoGeoArmorItem(ArmorMaterials.NETHERITE, Type.LEGGINGS, new Properties().durability(Type.LEGGINGS.getDurability(37)))
   );
   public static final DeferredHolder<Item, Item> NANOGEO_NANOSUIT_BOOTS = ITEMS.register(
      "nanogeo_nanosuit_boots", () -> new ExperimentalNanoGeoArmorItem(ArmorMaterials.NETHERITE, Type.BOOTS, new Properties().durability(Type.BOOTS.getDurability(37)))
   );

   private ModItems() {
   }
}
