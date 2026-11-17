package io.github.painworks.crysismod.nanovision;

import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.world.entity.player.Player;

public final class ClassicNightVision {
   private ClassicNightVision() {}

   public static boolean canUse(Player player) {
      return player != null && player.isAlive() && !player.isSpectator() && NanovisionConfig.nanovisionEnabled()
         && NanosuitArmorUtil.getNanovisionProfile(
            player, NanovisionConfig.nanovisionRequiresFullSet(), NanovisionConfig.nanovisionRequiresHelmet()
         ) == NanosuitProfile.NANOSUIT_1;
   }
}
