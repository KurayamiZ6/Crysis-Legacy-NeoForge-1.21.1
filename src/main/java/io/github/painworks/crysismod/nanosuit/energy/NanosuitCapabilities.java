package io.github.painworks.crysismod.nanosuit.energy;

import java.util.function.Supplier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class NanosuitCapabilities {
   public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
      DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "crysismod");
   private static final Supplier<AttachmentType<NanosuitEnergy>> NANOSUIT_ENERGY =
      ATTACHMENT_TYPES.register("nanosuit_energy", () -> AttachmentType.serializable(NanosuitEnergy::new).copyOnDeath().build());

   private NanosuitCapabilities() {
   }

   public static NanosuitEnergy get(Player player) {
      return player.getData(NANOSUIT_ENERGY);
   }
}
