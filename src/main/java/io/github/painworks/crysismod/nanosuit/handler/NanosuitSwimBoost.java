package io.github.painworks.crysismod.nanosuit.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;

public final class NanosuitSwimBoost {
   private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("crysismod", "hydro_thruster");
   private static final AttributeModifier BOOST = new AttributeModifier(ID, 0.40, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

   private NanosuitSwimBoost() {
   }

   public static boolean isActive(Player player) {
      var attribute = player.getAttribute(NeoForgeMod.SWIM_SPEED);
      return isSwimmingSprint(player) && attribute != null && attribute.hasModifier(ID);
   }

   static boolean isSwimmingSprint(Player player) {
      return player.isSwimming() && player.isUnderWater() && player.isSprinting();
   }

   static void update(ServerPlayer player, boolean powerSprintActive) {
      var attribute = player.getAttribute(NeoForgeMod.SWIM_SPEED);
      if (attribute != null) {
         if (powerSprintActive && isSwimmingSprint(player)) {
            if (!attribute.hasModifier(ID)) {
               attribute.addTransientModifier(BOOST);
            }
         } else {
            attribute.removeModifier(ID);
         }
      }
   }
}
