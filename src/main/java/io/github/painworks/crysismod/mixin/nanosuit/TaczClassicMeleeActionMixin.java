package io.github.painworks.crysismod.mixin.nanosuit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPowerMelee;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPowerMelee.ClassicAction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.tacz.guns.entity.shooter.LivingEntityMelee", remap = false)
public abstract class TaczClassicMeleeActionMixin {
   @Shadow @Final
   private LivingEntity shooter;

   @Unique
   private ClassicAction crysismod$classicMelee;

   // TaCZ writes the timestamp after its cooldown, firearm and event cancellation checks.
   @Inject(method = "melee", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER,
      target = "Lcom/tacz/guns/entity/shooter/ShooterDataHolder;meleeTimestamp:J"), require = 1)
   private void crysismod$beginClassicMelee(CallbackInfo callback) {
      if (this.shooter instanceof ServerPlayer player) {
         this.crysismod$classicMelee = NanosuitPowerMelee.beginClassicGunAction(player);
      }
   }

   @WrapOperation(method = "scheduleTickMelee", at = @At(value = "INVOKE",
      target = "Lcom/tacz/guns/api/item/gun/AbstractGunItem;melee(Lcom/tacz/guns/entity/shooter/ShooterDataHolder;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V"),
      require = 1)
   private void crysismod$carryClassicMelee(AbstractGunItem item, ShooterDataHolder data,
         LivingEntity shooter, ItemStack stack, Operation<Void> original) {
      ClassicAction action = this.crysismod$classicMelee;
      this.crysismod$classicMelee = null;
      NanosuitPowerMelee.withGunAction(action, () -> original.call(item, data, shooter, stack));
   }
}
