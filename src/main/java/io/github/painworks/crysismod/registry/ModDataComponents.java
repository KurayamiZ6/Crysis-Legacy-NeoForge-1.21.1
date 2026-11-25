package io.github.painworks.crysismod.registry;

import com.mojang.serialization.Codec;
import io.github.painworks.crysismod.nanosuit.cradle.BatteryKind;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
   public static final DeferredRegister<DataComponentType<?>> TYPES =
      DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "crysismod");
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BATTERY_CHARGE =
      TYPES.register("battery_charge", () -> DataComponentType.<Integer>builder()
         .persistent(Codec.intRange(0, BatteryKind.CEPH.capacity())).networkSynchronized(ByteBufCodecs.VAR_INT).build());
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> NANOSUIT_IDENTITY =
      TYPES.register("nanosuit_identity", () -> DataComponentType.<UUID>builder()
         .persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build());

   private ModDataComponents() {}
}
