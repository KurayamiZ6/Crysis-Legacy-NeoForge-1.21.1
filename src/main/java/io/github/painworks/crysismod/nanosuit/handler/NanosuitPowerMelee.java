package io.github.painworks.crysismod.nanosuit.handler;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.config.NanosuitServerConfig;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.network.NanosuitNetwork;
import io.github.painworks.crysismod.nanosuit.network.SyncPowerFeedbackPacket;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitCombatRules.Melee;
import io.github.painworks.crysismod.nanosuit.rules.NanosuitControls.Ability;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.registry.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class NanosuitPowerMelee {
   private static final ThreadLocal<MeleeImpulse> HURT_IMPULSE = new ThreadLocal<>();
   private static final ThreadLocal<ClassicAction> GUN_ACTION = new ThreadLocal<>();

   private NanosuitPowerMelee() {
   }

   public static boolean isBareHanded(ServerPlayer player) {
      return player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()
         && !player.isAutoSpinAttack() && !player.isUsingItem();
   }

   public static boolean beginClassicPunch(ServerPlayer player) {
      return beginClassicAction(player, false, isBareHanded(player)) == ClassicAction.POWERED;
   }

   public static ClassicAction beginClassicGunAction(ServerPlayer player) {
      return beginClassicAction(player, true, true);
   }

   private static ClassicAction beginClassicAction(ServerPlayer player, boolean gunStock, boolean eligible) {
      NanosuitProfile profile = NanosuitArmorUtil.getEquippedProfile(player);
      if (profile != NanosuitProfile.NANOSUIT_1 || !player.isAlive() || player.isSpectator()) {
         return null;
      }
      if (!NanosuitPlayerHandler.breakClassicCloakOnAttack(player) && eligible
         && profile.controls().supports(Ability.POWER_MELEE, NanosuitCapabilities.get(player).getMode())
         && NanosuitPlayerHandler.trySpendSuitEnergy(player, profile.combat().melee().energyCost())) {
         feedback(player, gunStock, true);
         return ClassicAction.POWERED;
      }
      return ClassicAction.PLAIN;
   }

   public static void withGunAction(ClassicAction action, Runnable melee) {
      ClassicAction previous = GUN_ACTION.get();
      if (action != null) {
         GUN_ACTION.set(action);
      } else {
         GUN_ACTION.remove();
      }
      try {
         melee.run();
      } finally {
         if (previous == null) {
            GUN_ACTION.remove();
         } else {
            GUN_ACTION.set(previous);
         }
      }
   }

   public static boolean hitClassicPunch(Entity target, DamageSource source, float damage, boolean powered, Operation<Boolean> original) {
      if (powered && target instanceof LivingEntity living && source.getEntity() instanceof ServerPlayer player
         && source.getDirectEntity() == player && damage > 0.0F) {
         return poweredHit(living, source, damage, player, NanosuitProfile.NANOSUIT_1.combat().melee(), false, true, original);
      }
      return original.call(target, source, damage);
   }

   public static boolean hit(Entity target, DamageSource source, float damage, boolean gunStock, Operation<Boolean> original) {
      if (!(target instanceof LivingEntity living)
         || !(source.getEntity() instanceof ServerPlayer player)
         || source.getDirectEntity() != player
         || !(damage > 0.0F)) {
         return original.call(target, source, damage);
      }

      NanosuitProfile profile = NanosuitArmorUtil.getEquippedProfile(player);
      ClassicAction classic = gunStock ? GUN_ACTION.get() : null;
      if (classic != null || profile == NanosuitProfile.NANOSUIT_1) {
         if (classic == ClassicAction.POWERED && profile == NanosuitProfile.NANOSUIT_1
            && profile.controls().supports(Ability.POWER_MELEE, NanosuitCapabilities.get(player).getMode())) {
            return poweredHit(living, source, damage, player, profile.combat().melee(), true, true, original);
         }
         return original.call(target, source, damage);
      }
      if (profile == null || !profile.controls().supports(Ability.POWER_MELEE, NanosuitCapabilities.get(player).getMode())) {
         return original.call(target, source, damage);
      }
      Melee rules = profile.combat().melee();
      if (!NanosuitPlayerHandler.canSpendSuitEnergy(player, rules.energyCost())) {
         return original.call(target, source, damage);
      }

      return poweredHit(living, source, damage, player, rules, gunStock, false, original);
   }

   private static boolean poweredHit(LivingEntity target, DamageSource source, float damage, ServerPlayer player, Melee rules,
         boolean gunStock, boolean alreadyPaid, Operation<Boolean> original) {
      double bonus = NanosuitServerConfig.powerMeleeKnockback();
      MeleeImpulse previous = HURT_IMPULSE.get();
      MeleeImpulse impulse = bonus > 0.0 && !source.is(DamageTypeTags.NO_KNOCKBACK) ? new MeleeImpulse(target, source) : null;
      if (impulse != null) {
         HURT_IMPULSE.set(impulse);
      }
      boolean hit;
      try {
         hit = original.call(target, source, damage * rules.damageMultiplier());
      } finally {
         if (impulse != null) {
            if (previous == null) {
               HURT_IMPULSE.remove();
            } else {
               HURT_IMPULSE.set(previous);
            }
         }
      }
      boolean paid = hit && (alreadyPaid || NanosuitPlayerHandler.trySpendSuitEnergy(player, rules.energyCost()));
      if (impulse != null && (impulse.captured || paid)) {
         float yaw = player.getYRot() * Mth.DEG_TO_RAD;
         target.knockback(impulse.strength + (paid ? bonus : 0.0),
            impulse.captured ? impulse.x : Mth.sin(yaw), impulse.captured ? impulse.z : -Mth.cos(yaw));
      }
      if (paid && !alreadyPaid) {
         feedback(player, gunStock, false);
      }
      return hit;
   }

   // Merge the bonus into the hit's ordinary LivingEntity.hurt impulse.
   public static boolean deferHurtKnockback(LivingEntity target, DamageSource source, double strength, double x, double z) {
      MeleeImpulse impulse = HURT_IMPULSE.get();
      if (impulse == null || impulse.target != target || impulse.source != source || impulse.captured) {
         return false;
      }
      impulse.captured = true;
      impulse.strength = strength;
      impulse.x = x;
      impulse.z = z;
      return true;
   }

   private static void feedback(ServerPlayer player, boolean gunStock, boolean classic) {
      var sound = gunStock ? ModSounds.POWER_MELEE_STOCK : classic ? ModSounds.CLASSIC_POWER_MELEE : ModSounds.POWER_MELEE;
      player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound.get(), SoundSource.PLAYERS, 0.65F, 1.0F);
      NanosuitNetwork.sendToPlayer(player, new SyncPowerFeedbackPacket(SyncPowerFeedbackPacket.Feedback.STRIKE));
   }

   public enum ClassicAction {
      PLAIN,
      POWERED
   }

   private static final class MeleeImpulse {
      private final LivingEntity target;
      private final DamageSource source;
      private boolean captured;
      private double strength;
      private double x;
      private double z;

      private MeleeImpulse(LivingEntity target, DamageSource source) {
         this.target = target;
         this.source = source;
      }
   }
}
