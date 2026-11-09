package io.github.painworks.crysismod.nanosuit.client;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class EnhancedJumpDebugOverlay {
   private EnhancedJumpDebugOverlay() {
   }

   @SubscribeEvent
   public static void onRenderGuiOverlay(Post event) {
      if (event.getName().equals(VanillaGuiLayers.HOTBAR) && NanosuitKeyMappings.isEnhancedJumpDebugEnabled()) {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer player = minecraft.player;
         if (player != null && !minecraft.options.hideGui) {
            GuiGraphics graphics = event.getGuiGraphics();
            int x = 6;
            int y = 6;
            int lineHeight = 9 + 2;
            double horizontalMotion = player.getDeltaMovement().horizontalDistance();
            double horizontalPositionDelta = Math.sqrt(
               (player.getX() - player.xo) * (player.getX() - player.xo) + (player.getZ() - player.zo) * (player.getZ() - player.zo)
            );
            String[] lines = new String[]{
               "Enhanced Jump Debug [F8]",
               "state pending="
                  + EnhancedJumpClientState.isRequestPending()
                  + " confirmed="
                  + EnhancedJumpClientState.isServerConfirmed()
                  + " remain="
                  + EnhancedJumpClientState.activeTicksRemaining(),
               "slow vanilla=" + EnhancedJumpClientState.lastVanillaSlowdown() + " bypass=" + EnhancedJumpClientState.lastSlowdownBypassApplied(),
               "shift key=" + minecraft.options.keyShift.isDown() + " input=" + player.input.shiftKeyDown + " crouch=" + player.isCrouching(),
               String.format(
                  Locale.ROOT,
                  "input forward=%.3f left=%.3f player[xxa=%.3f zza=%.3f]",
                  player.input.forwardImpulse,
                  player.input.leftImpulse,
                  player.xxa,
                  player.zza
               ),
               String.format(
                  Locale.ROOT, "motion horizontal=%.4f y=%.4f positionHorizontal=%.4f", horizontalMotion, player.getDeltaMovement().y, horizontalPositionDelta
               ),
               String.format(
                  Locale.ROOT,
                  "speed attribute=%.4f sprint=%s ground=%s",
                  player.getAttributeValue(Attributes.MOVEMENT_SPEED),
                  player.isSprinting(),
                  player.onGround()
               )
            };
            int widestLine = 0;

            for (String line : lines) {
               widestLine = Math.max(widestLine, minecraft.font.width(line));
            }

            graphics.fill(x - 3, y - 3, x + widestLine + 3, y + lines.length * lineHeight - 1, -1341124592);

            for (String line : lines) {
               graphics.drawString(minecraft.font, line, x, y, -1640705, false);
               y += lineHeight;
            }
         }
      }
   }
}
