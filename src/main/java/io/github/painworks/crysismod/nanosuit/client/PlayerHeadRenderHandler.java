package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitCloakRender;
import net.minecraft.client.player.AbstractClientPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderPlayerEvent.Post;
import net.neoforged.neoforge.client.event.RenderPlayerEvent.Pre;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class PlayerHeadRenderHandler {
   private static final Map<UUID, Boolean> PREV_HEAD_VISIBLE = new HashMap<>();
   private static final Map<UUID, Boolean> PREV_HAT_VISIBLE = new HashMap<>();
   private static final Map<UUID, Boolean> PREV_BODY_VISIBLE = new HashMap<>();
   private static final Map<UUID, Boolean> PREV_JACKET_VISIBLE = new HashMap<>();
   private static final Map<UUID, Boolean> PREV_LEFT_ARM_VISIBLE = new HashMap<>();
   private static final Map<UUID, Boolean> PREV_RIGHT_ARM_VISIBLE = new HashMap<>();
   private static final Map<UUID, Boolean> PREV_LEFT_SLEEVE_VISIBLE = new HashMap<>();
   private static final Map<UUID, Boolean> PREV_RIGHT_SLEEVE_VISIBLE = new HashMap<>();

   private PlayerHeadRenderHandler() {
   }

   @SubscribeEvent
   public static void onRenderPlayerPre(Pre event) {
      if (shouldVanishCompletely(event)) {
         event.setCanceled(true);
      } else {
         UUID playerId = event.getEntity().getUUID();
         PREV_HEAD_VISIBLE.put(playerId, ((PlayerModel)event.getRenderer().getModel()).head.visible);
         PREV_HAT_VISIBLE.put(playerId, ((PlayerModel)event.getRenderer().getModel()).hat.visible);
         PREV_BODY_VISIBLE.put(playerId, ((PlayerModel)event.getRenderer().getModel()).body.visible);
         PREV_JACKET_VISIBLE.put(playerId, ((PlayerModel)event.getRenderer().getModel()).jacket.visible);
         PREV_LEFT_ARM_VISIBLE.put(playerId, ((PlayerModel)event.getRenderer().getModel()).leftArm.visible);
         PREV_RIGHT_ARM_VISIBLE.put(playerId, ((PlayerModel)event.getRenderer().getModel()).rightArm.visible);
         PREV_LEFT_SLEEVE_VISIBLE.put(playerId, ((PlayerModel)event.getRenderer().getModel()).leftSleeve.visible);
         PREV_RIGHT_SLEEVE_VISIBLE.put(playerId, ((PlayerModel)event.getRenderer().getModel()).rightSleeve.visible);
         if (NanosuitArmorUtil.isNanosuitHelmet(event.getEntity().getItemBySlot(EquipmentSlot.HEAD))) {
            ((PlayerModel)event.getRenderer().getModel()).head.visible = false;
            ((PlayerModel)event.getRenderer().getModel()).hat.visible = false;
         }
         // Hide skin only where the corresponding armor piece actually replaces it.
         if (NanosuitArmorUtil.isNanosuitChestplate(event.getEntity().getItemBySlot(EquipmentSlot.CHEST))) {
            ((PlayerModel)event.getRenderer().getModel()).body.visible = false;
            ((PlayerModel)event.getRenderer().getModel()).jacket.visible = false;
            ((PlayerModel)event.getRenderer().getModel()).leftArm.visible = false;
            ((PlayerModel)event.getRenderer().getModel()).rightArm.visible = false;
            ((PlayerModel)event.getRenderer().getModel()).leftSleeve.visible = false;
            ((PlayerModel)event.getRenderer().getModel()).rightSleeve.visible = false;
         }
      }
   }

   @SubscribeEvent
   public static void onRenderPlayerPost(Post event) {
      UUID playerId = event.getEntity().getUUID();
      ((PlayerModel)event.getRenderer().getModel()).head.visible = PREV_HEAD_VISIBLE.getOrDefault(playerId, true);
      ((PlayerModel)event.getRenderer().getModel()).hat.visible = PREV_HAT_VISIBLE.getOrDefault(playerId, true);
      ((PlayerModel)event.getRenderer().getModel()).body.visible = PREV_BODY_VISIBLE.getOrDefault(playerId, true);
      ((PlayerModel)event.getRenderer().getModel()).jacket.visible = PREV_JACKET_VISIBLE.getOrDefault(playerId, true);
      ((PlayerModel)event.getRenderer().getModel()).leftArm.visible = PREV_LEFT_ARM_VISIBLE.getOrDefault(playerId, true);
      ((PlayerModel)event.getRenderer().getModel()).rightArm.visible = PREV_RIGHT_ARM_VISIBLE.getOrDefault(playerId, true);
      ((PlayerModel)event.getRenderer().getModel()).leftSleeve.visible = PREV_LEFT_SLEEVE_VISIBLE.getOrDefault(playerId, true);
      ((PlayerModel)event.getRenderer().getModel()).rightSleeve.visible = PREV_RIGHT_SLEEVE_VISIBLE.getOrDefault(playerId, true);
      PREV_HEAD_VISIBLE.remove(playerId);
      PREV_HAT_VISIBLE.remove(playerId);
      PREV_BODY_VISIBLE.remove(playerId);
      PREV_JACKET_VISIBLE.remove(playerId);
      PREV_LEFT_ARM_VISIBLE.remove(playerId);
      PREV_RIGHT_ARM_VISIBLE.remove(playerId);
      PREV_LEFT_SLEEVE_VISIBLE.remove(playerId);
      PREV_RIGHT_SLEEVE_VISIBLE.remove(playerId);
   }

   private static boolean shouldVanishCompletely(Pre event) {
      return event.getEntity().isInvisible() && NanosuitArmorUtil.hasFullSet(event.getEntity()) && !event.getEntity().isSpectator()
         && !(event.getEntity() instanceof AbstractClientPlayer player && NanosuitCloakRender.isActive(player));
   }
}
