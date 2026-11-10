package io.github.painworks.crysismod.nanosuit.client.render;

import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoClientConfig;
import io.github.painworks.crysismod.nanosuit.NanosuitPresentation;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorVariant;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import org.slf4j.Logger;

public final class NanosuitFirstPersonArmRenderDispatcher {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static boolean loggedNanoGeoPath;
   private static boolean loggedNonePath;

   private NanosuitFirstPersonArmRenderDispatcher() {
   }

   public static boolean renderArm(RenderArmEvent event) {
      if (!NanosuitClientConfig.nanosuitFirstPersonArmsEnabled()) {
         return false;
      }
      AbstractClientPlayer player = event.getPlayer();
      if (player == null) {
         return false;
      }
      ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
      if (NanosuitPresentation.forPiece(chestStack, EquipmentSlot.CHEST) == null) {
         return false;
      }
      NanosuitArmorVariant variant = NanosuitArmorUtil.getVariant(chestStack);
      if (!NanosuitClientConfig.nanosuitFirstPersonArmsUseNanoGeo() || !NanoGeoClientConfig.nanoGeoFpvArmsEnabled()) {
         logSelectionIfNeeded("NONE", variant, event.getArm(), "nanogeo path disabled by config");
         return false;
      }
      if (!NanoGeoClientConfig.nanoGeoFpvArmsRenderBothHands() && !isMainArm(player, event.getArm())) {
         logSelectionIfNeeded("NONE", variant, event.getArm(), "offhand fpv arm disabled");
         return false;
      }
      boolean rendered = NanoGeoFirstPersonArmRenderer.renderArmFromArmorModel(event);
      logSelectionIfNeeded(rendered ? "NANOGEO_FPV" : "NONE", variant, event.getArm(), rendered ? "selected" : "nanogeo renderer returned false");
      return rendered;
   }

   public static void resetTransientRenderState() {
      NanoGeoFirstPersonArmRenderer.resetTransientRenderState();
   }

   private static boolean isMainArm(AbstractClientPlayer player, HumanoidArm renderArm) {
      return player.getMainArm() == renderArm;
   }

   private static void logSelectionIfNeeded(String selectedRenderer, NanosuitArmorVariant variant, HumanoidArm arm, String reason) {
      if (!NanoGeoClientConfig.nanoGeoFpvArmsDebug()) {
         loggedNanoGeoPath = false;
         loggedNonePath = false;
      } else {
         byte var5 = -1;
         switch (selectedRenderer.hashCode()) {
            case -976671350:
               if (selectedRenderer.equals("NANOGEO_FPV")) {
                  var5 = 0;
               }
            default:
               switch (var5) {
                  case 0:
                     if (loggedNanoGeoPath) {
                        return;
                     }

                     loggedNanoGeoPath = true;
                     break;
                  default:
                     if (loggedNonePath) {
                        return;
                     }

                     loggedNonePath = true;
               }

               LOGGER.info(
                  "[CrysisMod][FPV Arms] variant={} arm={} renderer={} reason={} (TaCZ/held-item render path untouched)",
                  new Object[]{variant == null ? "none" : variant.name(), arm, selectedRenderer, reason}
               );
         }
      }
   }
}
