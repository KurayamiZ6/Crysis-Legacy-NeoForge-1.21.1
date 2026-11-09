package io.github.painworks.crysismod.mixin.nanosuit;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitCloak;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class CloakFootstepsMixin {
   @Shadow
   protected abstract void playMuffledStepSound(BlockState state, BlockPos pos);

   @ModifyVariable(method = "vibrationAndSoundEffectsFromBlock", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 1)
   private boolean crysismod$muffleCloakStepSound(boolean playSound) {
      return playSound && !crysismod$isMuffledStep();
   }

   @ModifyVariable(method = "vibrationAndSoundEffectsFromBlock", at = @At("HEAD"), argsOnly = true, ordinal = 1, require = 1)
   private boolean crysismod$muffleCloakStepVibration(boolean sendVibration) {
      return sendVibration && !crysismod$isMuffledStep();
   }

   @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
   private void crysismod$playNanosuitStep(BlockPos pos, BlockState state, CallbackInfo ci) {
      if (crysismod$replaceStepSound(pos, state)) {
         ci.cancel();
      }
   }

   @Inject(method = "playCombinationStepSounds", at = @At("HEAD"), cancellable = true)
   private void crysismod$playNanosuitCombinationStep(BlockState primary, BlockState secondary,
         BlockPos primaryPos, BlockPos secondaryPos, CallbackInfo ci) {
      if (crysismod$replaceStepSound(primaryPos, primary)) {
         playMuffledStepSound(secondary, secondaryPos);
         ci.cancel();
      }
   }

   @Unique
   private boolean crysismod$replaceStepSound(BlockPos pos, BlockState state) {
      if (!((Object)this instanceof Player player) || !NanosuitArmorUtil.hasFullSet(player) || player.isSilent()) {
         return false;
      }
      var suit = NanosuitCapabilities.get(player);
      boolean sprinting = player.isSprinting();
      boolean armored = suit.getMode() == NanosuitMode.ARMOR && suit.getEnergy() > 0;
      SoundType type = state.getSoundType(player.level(), pos, player);
      SoundEvent step = crysismod$findStep(state, type.getStepSound(), sprinting);
      if (step == null) {
         return false;
      }
      if (armored && sprinting) {
         step = ModSounds.ARMOR_RUN_STEP.get();
      }
      boolean cloaked = suit.getMode() == NanosuitMode.CLOAK && suit.getEnergy() > 0;
      float volume = type.getVolume() * (sprinting ? 0.26F : 0.18F);
      if (cloaked) {
         volume *= sprinting ? 0.7F : 0.45F;
      }
      float pitch = armored && !sprinting ? 0.85F : 1.0F;
      player.playSound(step, volume, pitch);
      return true;
   }

   @Unique
   private static SoundEvent crysismod$findStep(BlockState state, SoundEvent vanilla, boolean sprinting) {
      if (!vanilla.getLocation().getNamespace().equals("minecraft")) {
         return null;
      }
      String sound = vanilla.getLocation().getPath();
      if (sound.equals("block.wool.step")) {
         return ModSounds.FABRIC_STEP.get();
      }
      ModSounds.StepSounds family = switch (sound) {
         case "block.grass.step", "block.wet_grass.step", "block.moss.step", "block.moss_carpet.step" -> ModSounds.GRASS_STEPS;
         case "block.gravel.step" -> state.is(BlockTags.DIRT) ? ModSounds.DIRT_STEPS : ModSounds.GRAVEL_STEPS;
         case "block.mud.step", "block.mud_bricks.step", "block.rooted_dirt.step", "block.muddy_mangrove_roots.step" -> ModSounds.DIRT_STEPS;
         case "block.stone.step", "block.deepslate.step", "block.tuff.step", "block.calcite.step" -> ModSounds.STONE_STEPS;
         case "block.metal.step", "block.copper.step", "block.chain.step", "block.anvil.step", "block.lantern.step",
               "block.netherite_block.step" -> ModSounds.METAL_STEPS;
         case "block.sand.step" -> ModSounds.SAND_STEPS;
         case "block.wood.step", "block.bamboo_wood.step", "block.cherry_wood.step", "block.nether_wood.step",
               "block.stem.step" -> ModSounds.WOOD_STEPS;
         default -> null;
      };
      return family == null ? null : family.forPace(sprinting);
   }

   @Unique
   private boolean crysismod$isMuffledStep() {
      return (Object)this instanceof Player player && NanosuitCloak.mufflesSteps(player);
   }
}
