package io.github.painworks.crysismod.mixin.client.nanosuit;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSoundInstance.class)
public interface AbstractSoundVolumeAccessor {
   @Accessor("volume")
   float crysismod$getBaseVolume();

   @Accessor("volume")
   void crysismod$setBaseVolume(float volume);
}
