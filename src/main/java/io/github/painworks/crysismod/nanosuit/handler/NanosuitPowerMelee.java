package io.github.painworks.crysismod.nanosuit.handler;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanosuit.network.SyncPowerFeedbackPacket;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitCombatRules.Melee;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitControls.Ability;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.registry.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class NanosuitPowerMelee {
   private NanosuitPowerMelee() {
   }

   public static boolean hit(Entity target, DamageSource source, float damage, boolean gunStock, Operation<Boolean> original) {
      if (!(target instanceof LivingEntity)
         || !(source.getEntity() instanceof ServerPlayer player)
         || source.getDirectEntity() != player
         || !(damage > 0.0F)) {
         return original.call(target, source, damage);
      }

      NanosuitProfile profile = NanosuitArmorUtil.getEquippedProfile(player);
      if (profile == null || !profile.controls().supports(Ability.POWER_MELEE, NanosuitCapabilities.get(player).getMode())) {
         return original.call(target, source, damage);
      }
      Melee rules = profile.combat().melee();
      if (!NanosuitPlayerHandler.canSpendSuitEnergy(player, rules.energyCost())) {
         return original.call(target, source, damage);
      }

      boolean hit = original.call(target, source, damage * rules.damageMultiplier());
      if (hit && NanosuitPlayerHandler.trySpendSuitEnergy(player, rules.energyCost())) {
         player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            (gunStock ? ModSounds.POWER_MELEE_STOCK : ModSounds.POWER_MELEE).get(), SoundSource.PLAYERS, 0.65F, 1.0F);
         NanosuitNetwork.sendToPlayer(player, new SyncPowerFeedbackPacket(SyncPowerFeedbackPacket.Feedback.STRIKE));
      }
      return hit;
   }
}
