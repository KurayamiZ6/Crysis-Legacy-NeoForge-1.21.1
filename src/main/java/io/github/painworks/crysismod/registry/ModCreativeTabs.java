package io.github.painworks.crysismod.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModCreativeTabs {
   public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "crysismod");
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NANOSUIT_TAB = CREATIVE_MODE_TABS.register(
      "nanosuit_tab",
      () -> CreativeModeTab.builder()
         .title(Component.literal("Nanosuit"))
         .icon(() -> new ItemStack((ItemLike)ModItems.NANOGEO_NANOSUIT_HELMET.get()))
         .displayItems((parameters, output) -> {
            output.accept((ItemLike)ModItems.NANOGEO_NANOSUIT_HELMET.get());
            output.accept((ItemLike)ModItems.NANOGEO_NANOSUIT_CHESTPLATE.get());
            output.accept((ItemLike)ModItems.NANOGEO_NANOSUIT_LEGGINGS.get());
            output.accept((ItemLike)ModItems.NANOGEO_NANOSUIT_BOOTS.get());
            output.accept((ItemLike)ModItems.NANOSUIT_2_HELMET.get());
            output.accept((ItemLike)ModItems.NANOSUIT_2_CHESTPLATE.get());
            output.accept((ItemLike)ModItems.NANOSUIT_2_LEGGINGS.get());
            output.accept((ItemLike)ModItems.NANOSUIT_2_BOOTS.get());
            output.accept(ModItems.NANOSUIT_CRADLE.get());
            output.accept(ModItems.CRADLE_CONTROLLER.get());
            output.accept(ModItems.ROBOTIC_ARM_ASSEMBLY.get());
            output.accept(ModItems.CELL_POWER_CELL.get());
            output.accept(ModItems.CEPH_ENERGY_CELL.get());
            output.accept(ModItems.CELL_CHARGING_STATION.get());
            output.accept(ModItems.CEPH_CRYSTAL_CORE.get());
            output.accept(ModItems.CEPH_CRYSTAL_CLUSTER.get());
            output.accept(ModItems.CEPH_ENERGY_CRYSTAL.get());
            output.accept(ModItems.UNDERSUIT_HOOD.get());
            output.accept(ModItems.UNDERSUIT_TOP.get());
            output.accept(ModItems.UNDERSUIT_LEGGINGS.get());
            output.accept(ModItems.UNDERSUIT_BOOTS.get());
         })
         .build()
   );

   private ModCreativeTabs() {
   }
}
