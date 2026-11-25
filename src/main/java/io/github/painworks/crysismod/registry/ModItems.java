package io.github.painworks.crysismod.registry;

import io.github.painworks.crysismod.nanosuit.item.ExperimentalNanoGeoArmorItem;
import io.github.painworks.crysismod.nanosuit.cradle.BatteryItem;
import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlock;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
   public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, "crysismod");
   private static final DeferredHolder<ArmorMaterial, ArmorMaterial> UNDERSUIT_MATERIAL =
      ARMOR_MATERIALS.register("crynet_undersuit", () -> new ArmorMaterial(
         Map.of(Type.HELMET, 0, Type.CHESTPLATE, 0, Type.LEGGINGS, 0, Type.BOOTS, 0), 0,
         SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.EMPTY,
         List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("crysismod", "crynet_undersuit"))), 0, 0));
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, "crysismod");
   public static final DeferredHolder<Item, ArmorItem> UNDERSUIT_HOOD = registerUndersuit("crynet_undersuit_hood", Type.HELMET, 27);
   public static final DeferredHolder<Item, ArmorItem> UNDERSUIT_TOP = registerUndersuit("crynet_undersuit_top", Type.CHESTPLATE, 40);
   public static final DeferredHolder<Item, ArmorItem> UNDERSUIT_LEGGINGS = registerUndersuit("crynet_undersuit_leggings", Type.LEGGINGS, 37);
   public static final DeferredHolder<Item, ArmorItem> UNDERSUIT_BOOTS = registerUndersuit("crynet_undersuit_boots", Type.BOOTS, 32);
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
   public static final DeferredHolder<Item, BlockItem> CRADLE_CONTROLLER = ITEMS.register(
      "cradle_controller", () -> new BlockItem(ModBlocks.CRADLE_CONTROLLER.get(), new Properties())
   );
   public static final DeferredHolder<Item, BlockItem> ROBOTIC_ARM_ASSEMBLY = ITEMS.register(
      "robotic_arm_assembly", () -> new NanosuitCradleBlock.PlacementItem(ModBlocks.ROBOTIC_ARM_ASSEMBLY.get(), new Properties())
   );
   public static final DeferredHolder<Item, BatteryItem> CELL_POWER_CELL = ITEMS.register(
      "cell_power_cell", () -> new BatteryItem(ModBlocks.CELL_POWER_CELL.get(), new Properties())
   );
   public static final DeferredHolder<Item, BatteryItem> CEPH_ENERGY_CELL = ITEMS.register(
      "ceph_energy_cell", () -> new BatteryItem(ModBlocks.CEPH_ENERGY_CELL.get(), new Properties())
   );
   public static final DeferredHolder<Item, BlockItem> CELL_CHARGING_STATION = ITEMS.register(
      "cell_charging_station", () -> new BlockItem(ModBlocks.CELL_CHARGING_STATION.get(), new Properties())
   );
   public static final DeferredHolder<Item, BlockItem> CEPH_CRYSTAL_CORE = ITEMS.register(
      "ceph_crystal_core", () -> new BlockItem(ModBlocks.CEPH_CRYSTAL_CORE.get(), new Properties())
   );
   public static final DeferredHolder<Item, BlockItem> CEPH_CRYSTAL_CLUSTER = ITEMS.register(
      "ceph_crystal_cluster", () -> new BlockItem(ModBlocks.CEPH_CRYSTAL_CLUSTER.get(), new Properties())
   );
   public static final DeferredHolder<Item, Item> CEPH_ENERGY_CRYSTAL = ITEMS.register(
      "ceph_energy_crystal", () -> new Item(new Properties())
   );

   private ModItems() {
   }

   private static DeferredHolder<Item, ArmorItem> registerUndersuit(String name, Type type, int durability) {
      return ITEMS.register(name, () -> new ArmorItem(UNDERSUIT_MATERIAL, type, new Properties().durability(durability)));
   }

   private static DeferredHolder<Item, Item> registerArmor(String name, Type type) {
      return ITEMS.register(name, () -> new ExperimentalNanoGeoArmorItem(
         ArmorMaterials.NETHERITE, type, new Properties().durability(type.getDurability(37))
      ));
   }
}
