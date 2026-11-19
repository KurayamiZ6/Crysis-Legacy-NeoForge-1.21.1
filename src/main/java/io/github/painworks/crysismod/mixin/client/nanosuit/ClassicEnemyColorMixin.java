package io.github.painworks.crysismod.mixin.client.nanosuit;

import io.github.painworks.crysismod.nanosuit.client.ClassicEnemyContacts;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class ClassicEnemyColorMixin {
   @Inject(method="getTeamColor",at=@At("HEAD"),cancellable=true)
   private void crysis$classicColor(CallbackInfoReturnable<Integer> cir) {
      Entity entity=(Entity)(Object)this;
      if(ClassicEnemyContacts.outline(entity))cir.setReturnValue(ClassicEnemyContacts.color(entity));
   }
}
