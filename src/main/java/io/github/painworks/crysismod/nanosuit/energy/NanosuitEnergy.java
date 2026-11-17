package io.github.painworks.crysismod.nanosuit.energy;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class NanosuitEnergy implements INanosuitEnergy, INBTSerializable<CompoundTag> {
   public static final float MAX_ENERGY = 100.0F;
   private float energy = 100.0F;
   private NanosuitMode mode = NanosuitMode.NONE;
   private boolean sprintBoostReady;
   private int regenDelayTicks = 0;
   private int recoveryTicks = -1;

   @Override
   public float getEnergy() {
      return this.energy;
   }

   @Override
   public void setEnergy(float energy) {
      this.energy = Math.max(0.0F, Math.min(100.0F, energy));
      if (this.energy <= 0.0F) {
         this.mode = NanosuitMode.NONE;
         this.sprintBoostReady = false;
         if (this.recoveryTicks < 0) {
            this.recoveryTicks = 0;
         }
      } else if (this.energy == MAX_ENERGY) {
         this.recoveryTicks = -1;
      }
   }

   @Override
   public void setEnergyWithoutStartingRecovery(float energy) {
      if (this.isRecoveryLocked()) {
         return;
      }
      this.energy = Math.max(0.0F, Math.min(MAX_ENERGY, energy));
      if (this.energy == 0.0F) {
         this.sprintBoostReady = false;
      }
   }

   @Override
   public NanosuitMode getMode() {
      return this.mode;
   }

   @Override
   public void setMode(NanosuitMode mode) {
      this.mode = mode == null ? NanosuitMode.NONE : mode;
      if (this.isRecoveryLocked()) {
         this.mode = NanosuitMode.NONE;
      }
   }

   @Override
   public boolean isSprintBoostReady() {
      return this.sprintBoostReady;
   }

   @Override
   public void setSprintBoostReady(boolean sprintBoostReady) {
      this.sprintBoostReady = sprintBoostReady && !this.isRecoveryLocked();
   }

   @Override
   public int getRegenDelayTicks() {
      return this.regenDelayTicks;
   }

   @Override
   public void setRegenDelayTicks(int ticks) {
      this.regenDelayTicks = Math.max(0, ticks);
   }

   @Override
   public int getRecoveryTicks() {
      return this.recoveryTicks;
   }

   @Override
   public void setRecoveryTicks(int ticks) {
      this.recoveryTicks = Math.max(-1, ticks);
      if (this.isRecoveryLocked()) {
         this.mode = NanosuitMode.NONE;
         this.sprintBoostReady = false;
      }
   }

   @Override
   public void copyFrom(INanosuitEnergy other) {
      this.energy = other.getEnergy();
      this.mode = other.getMode();
      this.sprintBoostReady = false;
      this.regenDelayTicks = other.getRegenDelayTicks();
      this.setRecoveryTicks(other.getRecoveryTicks());
   }

   @Override
   public CompoundTag serializeNBT(HolderLookup.Provider registries) {
      CompoundTag tag = new CompoundTag();
      tag.putFloat("Energy", this.energy);
      tag.putString("Mode", this.mode.name());
      tag.putInt("RegenDelayTicks", this.regenDelayTicks);
      tag.putInt("RecoveryTicks", this.recoveryTicks);
      return tag;
   }

   @Override
   public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
      this.energy = Math.max(0.0F, Math.min(100.0F, nbt.getFloat("Energy")));

      try {
         this.mode = NanosuitMode.valueOf(nbt.getString("Mode"));
      } catch (IllegalArgumentException var3) {
         this.mode = NanosuitMode.NONE;
      }

      this.sprintBoostReady = false;
      this.regenDelayTicks = nbt.contains("RegenDelayTicks") ? Math.max(0, nbt.getInt("RegenDelayTicks")) : 0;
      this.setRecoveryTicks(this.energy == MAX_ENERGY ? -1
         : nbt.contains("RecoveryTicks") ? nbt.getInt("RecoveryTicks") : this.energy == 0.0F ? 0 : -1);
   }
}
