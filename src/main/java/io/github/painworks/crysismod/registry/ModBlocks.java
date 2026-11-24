package io.github.painworks.crysismod.registry;

import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlock;
import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlockEntity;
import net.minecraft.core.registries.Registries;
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

   private ModBlocks() {}
}
