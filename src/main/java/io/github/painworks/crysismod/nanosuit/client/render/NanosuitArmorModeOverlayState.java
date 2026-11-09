package io.github.painworks.crysismod.nanosuit.client.render;

import io.github.painworks.crysismod.nanosuit.energy.INanosuitEnergy;

import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoArmorTextures;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoClientConfig;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public final class NanosuitArmorModeOverlayState {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation DEFAULT_OVERLAY_TEXTURE = NanoGeoArmorTextures.ARMOR_MODE_OVERLAY;
   private static final String LEGACY_OVERLAY_TEXTURE = "crysismod:textures/armor/armor_mode.png";
   private static final Map<UUID, String> DEBUG_SIGNATURE_BY_PLAYER = new ConcurrentHashMap<>();
   private static volatile String cachedTexturePath = DEFAULT_OVERLAY_TEXTURE.toString();
   private static volatile ResourceLocation cachedTexture = DEFAULT_OVERLAY_TEXTURE;

   private NanosuitArmorModeOverlayState() {
   }

   public static NanosuitArmorModeOverlayState.OverlayRenderState resolveForThirdPerson(LivingEntity entity) {
      return NanoGeoClientConfig.armorModeOverlayEnabled() && NanoGeoClientConfig.armorModeOverlayRenderInThirdPerson()
         ? resolve(entity, "third_person")
         : NanosuitArmorModeOverlayState.OverlayRenderState.DISABLED;
   }

   public static NanosuitArmorModeOverlayState.OverlayRenderState resolveForFirstPerson(LivingEntity entity) {
      return NanoGeoClientConfig.armorModeOverlayEnabled() && NanoGeoClientConfig.armorModeOverlayRenderInFirstPerson()
         ? resolve(entity, "first_person")
         : NanosuitArmorModeOverlayState.OverlayRenderState.DISABLED;
   }

   public static ResourceLocation overlayTexture() {
      String configured = NanoGeoClientConfig.armorModeOverlayTexture();
      if ("crysismod:textures/armor/armor_mode.png".equals(configured)) {
         configured = DEFAULT_OVERLAY_TEXTURE.toString();
      }

      if (!configured.equals(cachedTexturePath)) {
         ResourceLocation parsed = ResourceLocation.tryParse(configured);
         cachedTexture = parsed == null ? DEFAULT_OVERLAY_TEXTURE : parsed;
         cachedTexturePath = configured;
      }

      return cachedTexture;
   }

   private static NanosuitArmorModeOverlayState.OverlayRenderState resolve(LivingEntity entity, String renderPath) {
      if (entity instanceof Player player) {
         if (!NanosuitArmorUtil.hasFullSet(player)) {
            return NanosuitArmorModeOverlayState.OverlayRenderState.DISABLED;
         } else {
            NanosuitArmorModeOverlayState.EnergyAndMode state = readEnergyAndMode(player);
            float transitionProgress = NanosuitArmorModeOverlayTransitionState.resolveProgress(player, state.armorModeActive());
            if (!state.armorModeActive() && transitionProgress <= 1.0E-4F) {
               maybeDebugLog(player, renderPath, state, NanosuitArmorModeOverlayState.OverlayRenderState.DISABLED);
               return NanosuitArmorModeOverlayState.OverlayRenderState.DISABLED;
            } else {
               float shimmerTime = player.tickCount + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
               float alpha = clamp01(NanoGeoClientConfig.armorModeOverlayAlpha() / 255.0F);
               NanosuitArmorModeOverlayState.OverlayRenderState resolved = new NanosuitArmorModeOverlayState.OverlayRenderState(
                  true,
                  state.armorModeActive(),
                  overlayTexture(),
                  NanoGeoClientConfig.armorModeOverlayTintR(),
                  NanoGeoClientConfig.armorModeOverlayTintG(),
                  NanoGeoClientConfig.armorModeOverlayTintB(),
                  alpha,
                  0.0F,
                  0.0F,
                  NanoGeoClientConfig.armorModeOverlayFullbright() ? 15728880 : -1,
                  shimmerTime,
                  transitionProgress
               );
               maybeDebugLog(player, renderPath, state, resolved);
               return resolved;
            }
         }
      } else {
         return NanosuitArmorModeOverlayState.OverlayRenderState.DISABLED;
      }
   }

   private static NanosuitArmorModeOverlayState.EnergyAndMode readEnergyAndMode(Player player) {
      return java.util.Optional.<INanosuitEnergy>of(NanosuitCapabilities.get(player))
         .map(data -> new NanosuitArmorModeOverlayState.EnergyAndMode(data.getEnergy(), data.getMode() == NanosuitMode.ARMOR && data.getEnergy() > 0.0F))
         .orElse(NanosuitArmorModeOverlayState.EnergyAndMode.EMPTY);
   }

   private static void maybeDebugLog(
      Player player, String renderPath, NanosuitArmorModeOverlayState.EnergyAndMode energyState, NanosuitArmorModeOverlayState.OverlayRenderState state
   ) {
      if (!NanoGeoClientConfig.debugArmorModeOverlay()) {
         DEBUG_SIGNATURE_BY_PLAYER.remove(player.getUUID());
      } else {
         String signature = "path="
            + renderPath
            + " active="
            + state.enabled()
            + " fullSet="
            + NanosuitArmorUtil.hasFullSet(player)
            + " energy="
            + energyState.energy()
            + " modeArmor="
            + energyState.armorModeActive()
            + " texture="
            + state.texture()
            + " alpha="
            + state.alpha()
            + " transitionProgress="
            + state.transitionProgress()
            + " fullbright="
            + (state.overlayPackedLight() == 15728880);
         String previous = DEBUG_SIGNATURE_BY_PLAYER.put(player.getUUID(), signature);
         if (!signature.equals(previous)) {
            LOGGER.info("[CrysisMod][ArmorModeOverlay] player={} {}", player.getScoreboardName(), signature);
         }
      }
   }

   private static float clamp01(float value) {
      if (value < 0.0F) {
         return 0.0F;
      } else {
         return value > 1.0F ? 1.0F : value;
      }
   }

   private record EnergyAndMode(float energy, boolean armorModeActive) {
      private static final NanosuitArmorModeOverlayState.EnergyAndMode EMPTY = new NanosuitArmorModeOverlayState.EnergyAndMode(0.0F, false);
   }

   public record OverlayRenderState(
      boolean enabled,
      boolean armorModeActive,
      ResourceLocation texture,
      float red,
      float green,
      float blue,
      float alpha,
      float pulse,
      float activationBoost,
      int overlayPackedLight,
      float shimmerTime,
      float transitionProgress
   ) {
      private static final NanosuitArmorModeOverlayState.OverlayRenderState DISABLED = new NanosuitArmorModeOverlayState.OverlayRenderState(
         false, false, NanosuitArmorModeOverlayState.DEFAULT_OVERLAY_TEXTURE, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, -1, 0.0F, 0.0F
      );
   }
}
