package io.github.painworks.crysismod.registry;

import io.github.painworks.crysismod.nanosuit.cradle.BatteryBlock;
import io.github.painworks.crysismod.nanosuit.cradle.BatteryBlockEntity;
import io.github.painworks.crysismod.nanosuit.cradle.BatteryKind;
import io.github.painworks.crysismod.nanosuit.cradle.CellChargingStationBlock;
import io.github.painworks.crysismod.nanosuit.cradle.CellChargingStationBlockEntity;
import io.github.painworks.crysismod.nanosuit.cradle.CephCrystalClusterBlock;
import io.github.painworks.crysismod.nanosuit.cradle.CephCrystalCoreBlock;
import io.github.painworks.crysismod.nanosuit.cradle.CradleControllerBlock;
import io.github.painworks.crysismod.nanosuit.cradle.CradleControllerBlockEntity;
import io.github.painworks.crysismod.nanosuit.cradle.CradleControllerMenu;
import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlock;
import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
   public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("crysismod");
   public static final DeferredHolder<net.minecraft.world.level.block.Block, NanosuitCradleBlock> NANOSUIT_CRADLE =
      BLOCKS.register("nanosuit_cradle", () -> new NanosuitCradleBlock(
         BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL).pushReaction(PushReaction.BLOCK)));
   public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "crysismod");
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NanosuitCradleBlockEntity>> NANOSUIT_CRADLE_ENTITY =
      BLOCK_ENTITIES.register("nanosuit_cradle", () -> BlockEntityType.Builder.of(NanosuitCradleBlockEntity::new, NANOSUIT_CRADLE.get()).build(null));
   public static final DeferredHolder<net.minecraft.world.level.block.Block, CradleControllerBlock> CRADLE_CONTROLLER =
      BLOCKS.register("cradle_controller", () -> new CradleControllerBlock(
         BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL).pushReaction(PushReaction.BLOCK)));
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CradleControllerBlockEntity>> CRADLE_CONTROLLER_ENTITY =
      BLOCK_ENTITIES.register("cradle_controller",
         () -> BlockEntityType.Builder.of(CradleControllerBlockEntity::new, CRADLE_CONTROLLER.get()).build(null));
   public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "crysismod");
   public static final DeferredHolder<MenuType<?>, MenuType<CradleControllerMenu>> CRADLE_CONTROLLER_MENU =
      MENUS.register("cradle_controller", () -> new MenuType<>(CradleControllerMenu::new, FeatureFlags.DEFAULT_FLAGS));
   public static final DeferredHolder<net.minecraft.world.level.block.Block, BatteryBlock> CELL_POWER_CELL =
      BLOCKS.register("cell_power_cell", () -> new BatteryBlock(BatteryKind.CELL,
         BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().pushReaction(PushReaction.BLOCK)));
   public static final DeferredHolder<net.minecraft.world.level.block.Block, BatteryBlock> CEPH_ENERGY_CELL =
      BLOCKS.register("ceph_energy_cell", () -> new BatteryBlock(BatteryKind.CEPH,
         BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().pushReaction(PushReaction.BLOCK)));
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatteryBlockEntity>> BATTERY_ENTITY =
      BLOCK_ENTITIES.register("power_cell",
         () -> BlockEntityType.Builder.of(BatteryBlockEntity::new, CELL_POWER_CELL.get(), CEPH_ENERGY_CELL.get()).build(null));
   public static final DeferredHolder<net.minecraft.world.level.block.Block, CellChargingStationBlock> CELL_CHARGING_STATION =
      BLOCKS.register("cell_charging_station", () -> new CellChargingStationBlock(BlockBehaviour.Properties.of()
         .strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().pushReaction(PushReaction.BLOCK)
         .lightLevel(state -> state.getValue(CellChargingStationBlock.LIT) ? 6 : 0)));
   public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CellChargingStationBlockEntity>> CELL_CHARGING_STATION_ENTITY =
      BLOCK_ENTITIES.register("cell_charging_station",
         () -> BlockEntityType.Builder.of(CellChargingStationBlockEntity::new, CELL_CHARGING_STATION.get()).build(null));
   public static final DeferredHolder<net.minecraft.world.level.block.Block, CephCrystalCoreBlock> CEPH_CRYSTAL_CORE =
      BLOCKS.register("ceph_crystal_core", () -> new CephCrystalCoreBlock(BlockBehaviour.Properties.of()
         .strength(3.0F).sound(SoundType.AMETHYST).requiresCorrectToolForDrops().randomTicks().pushReaction(PushReaction.BLOCK)));
   public static final DeferredHolder<net.minecraft.world.level.block.Block, CephCrystalClusterBlock> CEPH_CRYSTAL_CLUSTER =
      BLOCKS.register("ceph_crystal_cluster", () -> new CephCrystalClusterBlock(BlockBehaviour.Properties.of()
         .strength(1.5F).sound(SoundType.AMETHYST_CLUSTER).requiresCorrectToolForDrops().noOcclusion()
         .lightLevel(state -> state.getValue(CephCrystalClusterBlock.AGE) + 1)));

   private ModBlocks() {}
}
