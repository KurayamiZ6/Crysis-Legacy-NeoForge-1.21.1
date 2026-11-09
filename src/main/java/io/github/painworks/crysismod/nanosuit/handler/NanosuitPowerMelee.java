package io.github.painworks.crysismod.nanosuit.handler;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanosuit.network.SyncPowerFeedbackPacket;
import io.github.painworks.crysismod.registry.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class NanosuitPowerMelee {
   private static final float ENERGY_COST = 15.0F;

   private NanosuitPowerMelee() {
   }

   public static boolean hit(Entity target, DamageSource source, float damage, boolean gunStock, Operation<Boolean> original) {
      if (!(target instanceof LivingEntity)
         || !(source.getEntity() instanceof ServerPlayer player)
         || source.getDirectEntity() != player
         || !(damage > 0.0F)
         || !NanosuitPlayerHandler.canSpendSuitEnergy(player, ENERGY_COST)) {
         return original.call(target, source, damage);
      }

      boolean hit = original.call(target, source, damage * 2.0F);
      if (hit && NanosuitPlayerHandler.trySpendSuitEnergy(player, ENERGY_COST)) {
         player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            (gunStock ? ModSounds.POWER_MELEE_STOCK : ModSounds.POWER_MELEE).get(), SoundSource.PLAYERS, 0.65F, 1.0F);
         NanosuitNetwork.sendToPlayer(player, new SyncPowerFeedbackPacket(SyncPowerFeedbackPacket.Feedback.STRIKE));
      }
      return hit;
   }
}
