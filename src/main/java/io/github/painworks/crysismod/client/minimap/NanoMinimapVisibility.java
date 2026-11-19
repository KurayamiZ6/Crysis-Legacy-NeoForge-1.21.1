package io.github.painworks.crysismod.client.minimap;

import io.github.painworks.crysismod.client.minimap.config.NanoMinimapClientConfig;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class NanoMinimapVisibility {
   private NanoMinimapVisibility() {
   }

   public static boolean shouldRender(Minecraft minecraft) {
      if (!NanoMinimapClientConfig.minimapEnabled()) {
         return false;
      } else if (minecraft.options.hideGui) {
         return false;
      } else {
         LocalPlayer player = minecraft.player;
         if(player!=null&&ClassicMinimapRenderer.isClassic()
            &&(!io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig.classicMinimapEnabled()
               || !io.github.painworks.crysismod.nanosuit.client.NanosuitHudOverlay.isHudEnabled()))return false;
         return player != null && minecraft.level != null ? NanosuitArmorUtil.hasFullSet(player) : false;
      }
   }
}
