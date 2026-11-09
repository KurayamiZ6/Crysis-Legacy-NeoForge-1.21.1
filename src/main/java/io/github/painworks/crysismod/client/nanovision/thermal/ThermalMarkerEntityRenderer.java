package io.github.painworks.crysismod.client.nanovision.thermal;

import io.github.painworks.crysismod.nanosuit.client.render.NanosuitCloakRender;
import net.minecraft.client.player.AbstractClientPlayer;

import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.NanovisionController;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;

public final class ThermalMarkerEntityRenderer {
   private ThermalMarkerEntityRenderer() {
   }

   public static boolean shouldRenderMarker(LocalPlayer viewer, LivingEntity entity) {
      if (!NanovisionController.shouldRenderMarkerPass() || viewer == null || entity == null) {
         return false;
      } else if (!NanovisionState.isActive(viewer) || entity.isRemoved() || !entity.isAlive()) {
         return false;
      } else if (entity.isInvisible() && !NanovisionConfig.thermalMarkerRenderInvisible()
         && !(entity instanceof AbstractClientPlayer player && NanosuitCloakRender.isActive(player))) {
         return false;
      } else if (viewer.distanceToSqr(entity) > NanovisionConfig.thermalMarkerMaxDistance() * NanovisionConfig.thermalMarkerMaxDistance()) {
         return false;
      } else {
         return entity == viewer && Minecraft.getInstance().options.getCameraType().isFirstPerson() && NanovisionConfig.thermalMarkerSkipFirstPersonPlayer()
            ? false
            : isEnabledEntityCategory(entity);
      }
   }

   private static boolean isEnabledEntityCategory(LivingEntity entity) {
      if (entity instanceof Player) {
         return NanovisionConfig.thermalMarkerRenderPlayers();
      } else if (entity instanceof Enemy) {
         return NanovisionConfig.thermalMarkerRenderMonsters();
      } else {
         return !(entity instanceof Animal) && !(entity instanceof WaterAnimal) && !(entity instanceof AbstractVillager)
            ? NanovisionConfig.thermalMarkerRenderMonsters() || NanovisionConfig.thermalMarkerRenderAnimals()
            : NanovisionConfig.thermalMarkerRenderAnimals();
      }
   }
}
