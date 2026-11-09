package io.github.painworks.crysismod.client.nanogeo;

import io.github.painworks.crysismod.nanosuit.client.render.NanosuitArmorModeOverlayState;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorVariant;
import net.minecraft.world.entity.player.Player;

final class NanoGeoArmorModeBaseCompositeCompat {
   private static final float VISIBLE_TRANSITION_PROGRESS = 1.0E-4F;

   private NanoGeoArmorModeBaseCompositeCompat() {
   }

   static boolean shouldReplaceBaseArmor(Player player) {
      return player != null
            && replacementEligible()
            && NanosuitArmorUtil.hasFullSet(player)
            && NanosuitArmorUtil.getEquippedVariant(player) == NanosuitArmorVariant.NANOGEO
         ? shouldReplaceBaseArmor(NanosuitArmorModeOverlayState.resolveForThirdPerson(player))
         : false;
   }

   static boolean shouldReplaceBaseArmor(NanosuitArmorModeOverlayState.OverlayRenderState overlayState) {
      return replacementEligible()
         && overlayState != null
         && overlayState.enabled()
         && (overlayState.armorModeActive() || overlayState.transitionProgress() > 1.0E-4F);
   }

   static boolean replacementEligible() {
      return presetEligible() && !NanoGeoShaderpackCompatibility.shouldUseArmorModeCompatibilityFallback();
   }

   private static boolean presetEligible() {
      return NanoGeoClientConfig.armorModeCompatPerformancePreset() == NanoGeoClientConfig.ArmorModeCompatPerformancePreset.BASE_COMPOSITE_FAST
         && NanoGeoShaderpackCompatibility.resolveArmorModeOverlayBackend() == NanoGeoClientConfig.ArmorModeOverlayBackend.DYNAMIC_TEXTURE_COMPAT
         && !NanoGeoClientConfig.armorModePulseDebugOutputOnlyPulse();
   }
}
