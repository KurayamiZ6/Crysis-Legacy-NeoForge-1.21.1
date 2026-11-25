package io.github.painworks.crysismod.nanosuit.energy;

import io.github.painworks.crysismod.nanosuit.integration.BiologicalSuitAttachment;
import io.github.painworks.crysismod.nanosuit.cradle.CradlePatient;
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
   public static final Supplier<AttachmentType<BiologicalSuitAttachment>> BIOLOGICAL_SUIT =
      ATTACHMENT_TYPES.register("biological_suit", () -> AttachmentType.serializable(BiologicalSuitAttachment::new)
         .sync((holder, recipient) -> holder == recipient, BiologicalSuitAttachment.LOCK_SYNC).build());
   public static final Supplier<AttachmentType<CradlePatient>> CRADLE_PATIENT =
      ATTACHMENT_TYPES.register("cradle_patient", () -> AttachmentType.serializable(CradlePatient::new)
         .sync((holder, recipient) -> holder == recipient, CradlePatient.SYNC).build());

   private NanosuitCapabilities() {
   }

   public static NanosuitEnergy get(Player player) {
      return player.getData(NANOSUIT_ENERGY);
   }
}
