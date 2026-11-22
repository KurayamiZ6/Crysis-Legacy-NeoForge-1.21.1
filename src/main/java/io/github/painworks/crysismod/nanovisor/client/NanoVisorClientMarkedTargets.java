package io.github.painworks.crysismod.nanovisor.client;

import io.github.painworks.crysismod.nanovisor.network.SyncNanoVisorMarkedTargetsPacket;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class NanoVisorClientMarkedTargets {
   private static final int LOCAL_GLOW_REFRESH_TICKS = 8;
   private static final Map<UUID, NanoVisorClientMarkedTargets.LocalMark> ACTIVE_MARKS = new HashMap<>();
   private static final Method ENTITY_SET_SHARED_FLAG = resolveSetSharedFlagMethod();

   private NanoVisorClientMarkedTargets() {
   }

   public static void handlePacket(SyncNanoVisorMarkedTargetsPacket packet) {
      switch (packet.action()) {
         case UPSERT:
            upsert(packet.targetUuid(), packet.targetEntityId(), packet.durationTicks());
            break;
         case REMOVE:
            remove(packet.targetUuid());
            break;
         case CLEAR_ALL:
            clear();
      }
   }

   public static void tick(Minecraft minecraft) {
      if (minecraft == null) {
         clear();
      } else {
         ClientLevel level = minecraft.level;
         Player player = minecraft.player;
         if (level != null && player != null) {
            long now = level.getGameTime();
            tickInternal(level, now, true);
         } else {
            clear();
         }
      }
   }

   public static void applyForRender(ClientLevel level) {
      if (level != null && !ACTIVE_MARKS.isEmpty()) {
         tickInternal(level, level.getGameTime(), false);
      }
   }

   public static boolean isMarkedActive(Entity entity) {
      if (entity == null) {
         return false;
      } else {
         NanoVisorClientMarkedTargets.LocalMark mark = ACTIVE_MARKS.get(entity.getUUID());
         if (mark == null) {
            return false;
         } else {
            Minecraft minecraft = Minecraft.getInstance();
            ClientLevel level = minecraft != null ? minecraft.level : null;
            return level == null ? false : mark.expiresAtTick() > level.getGameTime();
         }
      }
   }

   public static void clear() {
      Minecraft minecraft = Minecraft.getInstance();
      ClientLevel level = minecraft != null ? minecraft.level : null;
      if (level != null) {
         for (NanoVisorClientMarkedTargets.LocalMark mark : ACTIVE_MARKS.values()) {
            releaseLocalGlow(level, mark);
         }
      }

      ACTIVE_MARKS.clear();
   }

   public static void forEachActiveMarkedEntity(ClientLevel level, Consumer<Entity> consumer) {
      if (level != null && consumer != null && !ACTIVE_MARKS.isEmpty()) {
         long now = level.getGameTime();

         for (var entry : ACTIVE_MARKS.entrySet()) {
            LocalMark mark = entry.getValue();
            if (shouldRenderMarkedFrame(level, mark, now)) {
               Entity entity = level.getEntity(mark.entityId());
               if (entity != null && entity.isAlive() && entity.getUUID().equals(entry.getKey())) {
                  consumer.accept(entity);
               }
            }
         }
      }
   }

   private static void tickInternal(ClientLevel level, long now, boolean releaseExpired) {
      Iterator<Entry<UUID, NanoVisorClientMarkedTargets.LocalMark>> it = ACTIVE_MARKS.entrySet().iterator();

      while (it.hasNext()) {
         Entry<UUID, NanoVisorClientMarkedTargets.LocalMark> entry = it.next();
         NanoVisorClientMarkedTargets.LocalMark mark = entry.getValue();
         if (mark.persistent()) {
            if (mark.glowApplied() && releaseLocalGlow(level, mark)) {
               entry.setValue(new LocalMark(mark.entityId(), mark.expiresAtTick(), false, false, true));
            }
         } else if (mark.expiresAtTick() <= now) {
            NanoVisorClientMarkedTargets.LocalMark effective = mark;
            if (!mark.releaseApplied() && releaseExpired) {
               boolean released = releaseLocalGlow(level, mark);
               if (released) {
                  effective = new NanoVisorClientMarkedTargets.LocalMark(
                     mark.entityId(), mark.expiresAtTick(), mark.hadGlowingBefore(), mark.glowApplied(), true
                  );
                  entry.setValue(effective);
               }
            }

            if (releaseExpired && !shouldRenderMarkedFrame(level, effective, now)) {
               it.remove();
            }
         } else {
            Entity entity = level.getEntity(mark.entityId());
            if (entity != null && entity.isAlive() && entity.getUUID().equals(entry.getKey())) {
               applyLocalGlow(entity, mark);
            }
         }
      }
   }

   private static void upsert(UUID uuid, int entityId, int durationTicks) {
      Minecraft minecraft = Minecraft.getInstance();
      ClientLevel level = minecraft != null ? minecraft.level : null;
      long now = level != null ? level.getGameTime() : 0L;
      long expiresAt = durationTicks == SyncNanoVisorMarkedTargetsPacket.PERSISTENT_SCAN ? Long.MAX_VALUE
         : now + Math.max(1L, (long)durationTicks);
      NanoVisorClientMarkedTargets.LocalMark previous = ACTIVE_MARKS.get(uuid);
      if (previous == null) {
         ACTIVE_MARKS.put(uuid, new NanoVisorClientMarkedTargets.LocalMark(entityId, expiresAt, false, false, false));
      } else {
         ACTIVE_MARKS.put(
            uuid,
            new NanoVisorClientMarkedTargets.LocalMark(
               entityId, Math.max(previous.expiresAtTick(), expiresAt), previous.hadGlowingBefore(), previous.glowApplied(), false
            )
         );
      }
   }

   private static void remove(UUID uuid) {
      Minecraft minecraft = Minecraft.getInstance();
      ClientLevel level = minecraft != null ? minecraft.level : null;
      NanoVisorClientMarkedTargets.LocalMark mark = ACTIVE_MARKS.remove(uuid);
      if (level != null && mark != null) {
         releaseLocalGlow(level, mark);
      }
   }

   private static void applyLocalGlow(Entity entity, NanoVisorClientMarkedTargets.LocalMark mark) {
      if (!mark.glowApplied()) {
         boolean hadGlowingBefore = entity.isCurrentlyGlowing();
         NanoVisorClientMarkedTargets.LocalMark effective = new NanoVisorClientMarkedTargets.LocalMark(
            mark.entityId(), mark.expiresAtTick(), hadGlowingBefore, true, mark.releaseApplied()
         );
         ACTIVE_MARKS.put(entity.getUUID(), effective);
      }

      setClientGlowSharedFlag(entity, true);
      if (entity instanceof LivingEntity living) {
         MobEffectInstance current = living.getEffect(MobEffects.GLOWING);
         if (current == null || current.getDuration() <= 3) {
            living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 8, 0, false, false, false));
         }
      }
   }

   private static boolean releaseLocalGlow(ClientLevel level, NanoVisorClientMarkedTargets.LocalMark mark) {
      if (mark.persistent() && !mark.glowApplied()) return true;
      Entity entity = level.getEntity(mark.entityId());
      if (entity == null) {
         return true;
      } else if (mark.hadGlowingBefore()) {
         return true;
      } else if (entity instanceof LivingEntity living && living.hasEffect(MobEffects.GLOWING)) {
         return false;
      } else {
         setClientGlowSharedFlag(entity, false);
         return true;
      }
   }

   private static boolean shouldRenderMarkedFrame(ClientLevel level, NanoVisorClientMarkedTargets.LocalMark mark, long now) {
      if (mark.expiresAtTick() > now || mark.visibleUntilTick() > now) {
         return true;
      } else {
         return mark.hadGlowingBefore() ? false : level.getEntity(mark.entityId()) instanceof LivingEntity living && living.hasEffect(MobEffects.GLOWING);
      }
   }

   private static Method resolveSetSharedFlagMethod() {
      for (Method method : Entity.class.getDeclaredMethods()) {
         Class<?>[] parameterTypes = method.getParameterTypes();
         if (parameterTypes.length == 2 && parameterTypes[0] == int.class && parameterTypes[1] == boolean.class && method.getReturnType() == void.class) {
            method.setAccessible(true);
            return method;
         }
      }

      return null;
   }

   private static void setClientGlowSharedFlag(Entity entity, boolean enabled) {
      if (entity != null) {
         if (ENTITY_SET_SHARED_FLAG != null) {
            try {
               ENTITY_SET_SHARED_FLAG.invoke(entity, 6, enabled);
               return;
            } catch (ReflectiveOperationException var3) {
            }
         }

         entity.setGlowingTag(enabled);
      }
   }

   private record LocalMark(int entityId, long expiresAtTick, boolean hadGlowingBefore, boolean glowApplied, boolean releaseApplied) {
      boolean persistent() {
         return expiresAtTick == Long.MAX_VALUE;
      }

      long visibleUntilTick() {
         return this.expiresAtTick + 8L;
      }
   }
}
