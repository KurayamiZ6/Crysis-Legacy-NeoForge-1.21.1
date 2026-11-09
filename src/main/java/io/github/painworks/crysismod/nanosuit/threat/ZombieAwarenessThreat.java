package io.github.painworks.crysismod.nanosuit.threat;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.neoforged.fml.ModList;

final class ZombieAwarenessThreat {
   private ZombieAwarenessThreat() {
   }

   static boolean isInvestigating(Mob mob, long now) {
      Object entry = Data.BY_MOB.get(mob.getUUID());
      if (!(entry instanceof CompoundTag tag) || mob.getNavigation().isDone()) {
         return false;
      }
      // ZA 1.13.2 stamps successful searches for sounds, scents and lights, without naming a player.
      long lastAction = tag.getLong("za_last_action");
      return lastAction > 0 && now >= lastAction && now - lastAction <= 80;
   }

   private static final class Data {
      private static final Map<?, ?> BY_MOB = findMap();

      private static Map<?, ?> findMap() {
         if (ModList.get().isLoaded("zombieawareness")) {
            try {
               Class<?> zombieAwareness = Class.forName("com.corosus.zombieawareness.ZombieAwareness");
               Object value = zombieAwareness.getField("entityData").get(null);
               if (value instanceof Map<?, ?> map) {
                  return map;
               }
            } catch (ReflectiveOperationException | LinkageError | SecurityException ignored) {
               // The vanilla signals remain available if ZA changes its internal storage.
            }
         }
         return Map.of();
      }
   }
}
