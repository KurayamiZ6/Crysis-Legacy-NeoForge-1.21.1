package io.github.painworks.crysismod.client.minimap.render;

import io.github.painworks.crysismod.client.minimap.NanoMinimapViewport;
import io.github.painworks.crysismod.nanosuit.threat.ThreatLevel;
import java.lang.ref.WeakReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class ThreatGauge {
   private static final int SEGMENTS = 8;
   private static final int WIDTH = 8;
   private static final int TRACK = 0x071820;
   private static final int RAIL = 0x91CDD9;
   private static final int INACTIVE = 0x375E68;
   private static final int[] ALERT_COLORS = {0x73EDB2, 0xF3D764, 0xFF9953, 0xFF525E};
   private static WeakReference<Player> receiver = new WeakReference<>(null);
   private static WeakReference<Level> receivedLevel = new WeakReference<>(null);
   private static ThreatLevel receivedThreat = ThreatLevel.CALM;
   private static int lastUpdateTick;

   private ThreatGauge() {
   }

   public static void update(Player player, ThreatLevel threat) {
      receiver = new WeakReference<>(player);
      receivedLevel = new WeakReference<>(player.level());
      receivedThreat = threat;
      lastUpdateTick = player.tickCount;
   }

   public static ThreatLevel current(Player player) {
      return receiver.get() == player && receivedLevel.get() == player.level()
         && player.tickCount - lastUpdateTick <= 60 ? receivedThreat : ThreatLevel.CALM;
   }

   static void render(GuiGraphics graphics, NanoMinimapViewport viewport, Minecraft minecraft) {
      Player player = minecraft.player;
      if (player == null || !player.isAlive() || player.isSpectator()) {
         return;
      }
      ThreatLevel threat = current(player);
      int segmentHeight = Mth.clamp(viewport.height() / 21, 2, 5);
      int height = SEGMENTS * (segmentHeight + 1) + 3;
      boolean right = viewport.maxX() + WIDTH + 4 < graphics.guiWidth();
      int x = right ? viewport.maxX() + 3 : viewport.x() - WIDTH - 3;
      int y = viewport.y() + (viewport.height() - height) / 2;
      float opacity = Mth.clamp(viewport.opacity(), 0.0F, 1.0F);
      int bottom = y + height;
      int railColor = color(RAIL, opacity * 0.65F);
      int nearRailX = right ? x : x + WIDTH - 1;
      int mapEdge = right ? viewport.maxX() : viewport.x() - 1;
      int alertColor = ALERT_COLORS[threat.ordinal()];
      int litSegments = 2 * (threat.ordinal() + 1);

      // Open bracket shared with the map: the outward edge stays dark instead of boxing in the gauge.
      graphics.fill(x + 1, y + 1, x + WIDTH - 1, bottom - 1, color(TRACK, opacity * 0.82F));
      graphics.fill(nearRailX, y + 1, nearRailX + 1, bottom - 1, railColor);
      int capFrom = Math.min(nearRailX, mapEdge);
      int capTo = Math.max(nearRailX, mapEdge) + 1;
      graphics.fill(capFrom, y, capTo, y + 1, railColor);
      graphics.fill(capFrom, bottom - 1, capTo, bottom, railColor);

      for (int segment = 0; segment < SEGMENTS; segment++) {
         int segmentBottom = bottom - 2 - segment * (segmentHeight + 1);
         int segmentColor = segment < litSegments
            ? color(alertColor, opacity * (segment == litSegments - 1 ? 1.0F : 0.75F))
            : color(INACTIVE, opacity * 0.50F);
         graphics.fill(x + 2, segmentBottom - segmentHeight, x + WIDTH - 2, segmentBottom, segmentColor);
      }
   }

   private static int color(int rgb, float opacity) {
      return Math.round(255.0F * opacity) << 24 | rgb;
   }
}
