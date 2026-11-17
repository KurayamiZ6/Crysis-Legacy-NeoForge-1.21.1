package io.github.painworks.crysismod.nanosuit.energy;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;

public interface INanosuitEnergy {
   float getEnergy();

   void setEnergy(float var1);

   void setEnergyWithoutStartingRecovery(float energy);

   default void addEnergy(float amount) {
      this.setEnergy(this.getEnergy() + amount);
   }

   default void consumeEnergy(float amount) {
      this.setEnergy(this.getEnergy() - amount);
   }

   NanosuitMode getMode();

   void setMode(NanosuitMode var1);

   boolean isSprintBoostReady();

   void setSprintBoostReady(boolean var1);

   int getRegenDelayTicks();

   void setRegenDelayTicks(int var1);

   int getRecoveryTicks();

   void setRecoveryTicks(int ticks);

   default boolean isRecoveryLocked() {
      return this.getRecoveryTicks() >= 0;
   }

   void copyFrom(INanosuitEnergy var1);
}
