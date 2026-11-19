package io.github.painworks.crysismod.mixin.client.nanosuit;

import io.github.painworks.crysismod.nanosuit.client.ClassicEnemyContacts;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class ClassicEnemyOutlineMixin {
   @Inject(method="shouldEntityAppearGlowing",at=@At("HEAD"),cancellable=true)
   private void crysis$classicEnemy(Entity entity,CallbackInfoReturnable<Boolean> cir) {
      if(ClassicEnemyContacts.outline(entity))cir.setReturnValue(true);
   }
}
