package io.github.painworks.crysismod.nanosuit.client;

import io.github.painworks.crysismod.nanosuit.config.NanosuitSoundVolumes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.NanosuitProfile;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.config.NanosuitServerConfig;
import io.github.painworks.crysismod.nanosuit.energy.INanosuitEnergy;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitPlayerHandler;
import io.github.painworks.crysismod.nanosuit.handler.NanosuitSwimBoost;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.registry.ModSounds;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import java.util.Optional;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = Bus.GAME)
public final class NanosuitHudOverlay {
   private static final int ENERGY_SEGMENTS = 20;
   private static final int ENERGY_SEGMENTS_LEFT_PADDING_PX = 16;
   private static final int ENERGY_SEGMENTS_RIGHT_PADDING_PX = 17;
   private static final int ENERGY_SEGMENT_GAP_PX = 3;
   private static final int ENERGY_SEGMENT_WIDTH_PX = 18;
   private static final int DEFAULT_HUD_OFFSET_X = -28;
   private static final int DEFAULT_HUD_OFFSET_Y = -8;
   private static final float DEFAULT_HUD_SCALE = 0.2F;
   private static final float MIN_HUD_SCALE = 0.05F;
   private static final float MAX_HUD_SCALE = 5.0F;
   private static final float DEFAULT_ARMOR_MODE_OVERLAY_SCALE = 1.0F;
   private static final float MIN_ARMOR_MODE_OVERLAY_SCALE = 0.25F;
   private static final float MAX_ARMOR_MODE_OVERLAY_SCALE = 2.0F;
   private static final float DEFAULT_OFF_LAYER_ALPHA = 1.0F;
   private static final float MIN_OFF_LAYER_ALPHA = 0.0F;
   private static final float MAX_OFF_LAYER_ALPHA = 1.0F;
   private static final float MIN_INERTIA_INTENSITY = 0.0F;
   private static final float MAX_INERTIA_INTENSITY = 3.0F;
   private static final float MIN_STEP_AMPLITUDE = 0.0F;
   private static final float MAX_STEP_AMPLITUDE = 12.0F;
   private static final float MIN_INERTIA_STRENGTH = 0.0F;
   private static final float MAX_INERTIA_STRENGTH = 0.2F;
   private static final float MIN_MAX_OFFSET = 0.0F;
   private static final float MAX_MAX_OFFSET = 40.0F;
   private static final float MIN_MAX_ROLL = 0.0F;
   private static final float MAX_MAX_ROLL = 10.0F;
   private static final float MIN_DAMPING = 0.1F;
   private static final float MAX_DAMPING = 60.0F;
   private static final float MIN_RETURN_SPEED = 0.1F;
   private static final float MAX_RETURN_SPEED = 60.0F;
   private static final float MIN_WALK_STEP_DURATION = 0.12F;
   private static final float MAX_WALK_STEP_DURATION = 1.0F;
   private static final float MIN_WALK_SPRING = 1.0F;
   private static final float MAX_WALK_SPRING = 80.0F;
   private static final float MIN_WALK_DAMPING = 0.1F;
   private static final float MAX_WALK_DAMPING = 60.0F;
   private static final float MIN_WALK_RETURN_SPEED = 0.1F;
   private static final float MAX_WALK_RETURN_SPEED = 60.0F;
   private static final float MIN_SPRINT_WALK_MULT = 1.0F;
   private static final float MAX_SPRINT_WALK_MULT = 3.0F;
   private static final float MIN_MAX_WALK_OFFSET = 0.0F;
   private static final float MAX_MAX_WALK_OFFSET = 80.0F;
   private static final float MIN_MAX_WALK_ROLL = 0.0F;
   private static final float MAX_MAX_WALK_ROLL = 8.0F;
   private static final float FULL_SEGMENT_PROGRESS_THRESHOLD = 0.9975F;
   private static final float FULL_ENERGY_EPSILON = 0.001F;
   private static final float ENERGY_PER_DIVISION = 5.0F;
   private static final int MIN_ARMOR_OVERLAY_INTRO_DURATION_TICKS = 4;
   private static final int MAX_ARMOR_OVERLAY_INTRO_DURATION_TICKS = 60;
   private static final float MIN_ARMOR_OVERLAY_INTRO_ELASTIC_STRENGTH = 0.0F;
   private static final float MAX_ARMOR_OVERLAY_INTRO_ELASTIC_STRENGTH = 1.0F;
   private static final int MIN_ARMOR_OVERLAY_INTRO_SEGMENT_COUNT = 8;
   private static final int MAX_ARMOR_OVERLAY_INTRO_SEGMENT_COUNT = 128;
   private static final float MIN_ARMOR_OVERLAY_INTRO_OVERSHOOT = 0.0F;
   private static final float MAX_ARMOR_OVERLAY_INTRO_OVERSHOOT = 0.3F;
   private static final int MIN_ARMOR_OVERLAY_INTRO_START_OUTSIDE_DISTANCE = 0;
   private static final int MAX_ARMOR_OVERLAY_INTRO_START_OUTSIDE_DISTANCE = 600;
   private static final float MIN_ARMOR_OVERLAY_INTRO_CENTER_DELAY_BIAS = 0.0F;
   private static final float MAX_ARMOR_OVERLAY_INTRO_CENTER_DELAY_BIAS = 1.0F;
   private static final float MIN_ARMOR_OVERLAY_MOTION_BLUR_STRENGTH = 0.0F;
   private static final float MAX_ARMOR_OVERLAY_MOTION_BLUR_STRENGTH = 1.0F;
   private static final int MIN_ARMOR_OVERLAY_MOTION_BLUR_SAMPLES = 1;
   private static final int MAX_ARMOR_OVERLAY_MOTION_BLUR_SAMPLES = 8;
   private static final float ARMOR_OVERLAY_OVAL_WIDTH_RATIO = 0.5F;
   private static final float ARMOR_OVERLAY_OVAL_HEIGHT_RATIO = 0.5F;
   private static final float ARMOR_OVERLAY_DEBUG_BG_ALPHA = 0.56F;
   private static final float MIN_EASING_SOFTNESS = 0.001F;
   private static final NanosuitHudInertia HUD_INERTIA = new NanosuitHudInertia();
   private static final NanosuitHudInertia.Params HUD_INERTIA_PARAMS = new NanosuitHudInertia.Params();
   private static float hudScale = 0.2F;
   private static float armorModeOverlayScale = 1.0F;
   private static float offLayerAlpha = 1.0F;
   private static boolean hudEnabled = true;
   private static boolean inertiaEnabled = true;
   private static float inertiaIntensity = 1.0F;
   private static float walkSideAmplitude = 2.4F;
   private static float walkVerticalAmplitude = 2.8F;
   private static float walkRollAmplitude = 0.95F;
   private static float walkStepDuration = 0.34F;
   private static float walkSpringStrength = 28.0F;
   private static float walkDamping = 11.5F;
   private static float walkReturnToCenterSpeed = 8.5F;
   private static float sprintWalkMultiplier = 1.3F;
   private static float maxWalkOffsetX = 55.377357F;
   private static float maxWalkOffsetY = 64.245285F;
   private static float maxWalkRoll = 2.0F;
   private static float yawInertiaStrength = 0.015F;
   private static float pitchInertiaStrength = 0.01F;
   private static float maxViewOffset = 6.0F;
   private static float maxRollDegrees = 1.2F;
   private static float dampingStrength = 12.0F;
   private static float returnSpeed = 8.0F;
   private static boolean textureSmooth = false;
   private static boolean textureMipmap = false;
   private static boolean textureFilterDirty = true;
   private static boolean configInitialized;
   private static long lastSharedInertiaFrameId = Long.MIN_VALUE;
   private static boolean armorOverlayIntroEnabled = true;
   private static int armorOverlayIntroDurationTicks = 18;
   private static float armorOverlayIntroElasticStrength = 0.35F;
   private static int armorOverlayIntroSegmentCount = 48;
   private static float armorOverlayIntroOvershoot = 0.08F;
   private static int armorOverlayIntroStartOutsideDistance = 260;
   private static float armorOverlayIntroCenterDelayBias = 0.35F;
   private static boolean armorOverlayIntroAlphaFade = true;
   private static boolean armorOverlayIntroGlowPulse = true;
   private static boolean armorOverlayMotionBlurEnabled = true;
   private static float armorOverlayMotionBlurStrength = 0.35F;
   private static int armorOverlayMotionBlurSamples = 3;
   private static boolean armorOverlayIntroDebug = false;
   private static boolean armorOverlayWasActiveLastFrame;
   private static boolean armorOverlayAnimPlaying;
   private static int armorOverlayAnimDirection = 1;
   private static float armorOverlayAnimElapsedTicks;
   private static float armorOverlayLastSampleTicks = Float.NaN;
   private static NanosuitHudOverlay.OverlayGridLayout armorOverlayGridLayout = NanosuitHudOverlay.OverlayGridLayout.EMPTY;
   private static int hudOffsetX = -28;
   private static int hudOffsetY = -8;

   private NanosuitHudOverlay() {
   }

   @SubscribeEvent
   public static void onRenderGuiOverlay(Post event) {
      if (NanosuitHudMasterCompositor.shouldLegacyRenderNanosuitHud()) {
         renderHotbarOverlay(event);
      }
   }

   public static void renderFromMaster(Post event) {
      renderHotbarOverlay(event);
   }

   private static void renderHotbarOverlay(Post event) {
      if (event.getName().equals(VanillaGuiLayers.HOTBAR)) {
         if (!configInitialized) {
            loadFromConfig();
         }

         refreshArmorOverlayIntroSettingsFromConfig();
         Minecraft minecraft = Minecraft.getInstance();
         NanosuitHudMasterCompositor.prepareSharedHudFrame(event);
         if (!hudEnabled) {
            resetArmorOverlayIntroState();
         } else {
            Player player = minecraft.player;
            NanosuitProfile profile = player != null && !minecraft.options.hideGui ? NanosuitArmorUtil.getEquippedProfile(player) : null;
            if (profile != null) {
               Optional<INanosuitEnergy> energyCap = java.util.Optional.<INanosuitEnergy>of(NanosuitCapabilities.get(player));
               if (!energyCap.isPresent()) {
                  resetArmorOverlayIntroState();
               } else {
                  energyCap.ifPresent(
                     data -> {
                        GuiGraphics guiGraphics = event.getGuiGraphics();
                        float targetEnergy = Mth.clamp(data.getEnergy(), 0.0F, 100.0F);
                        boolean classic = profile == NanosuitProfile.NANOSUIT_1;
                        if (classic) {
                           resetArmorOverlayIntroState();
                           ClassicNanosuitHud.render(guiGraphics, player, data);
                           return;
                        }
                        boolean armorModeActive = data.getMode() == NanosuitMode.ARMOR && targetEnergy > 0.0F;
                        boolean forceModeActive = NanosuitPowerFeedback.isForceActive();
                        boolean cloakModeActive = data.getMode() == NanosuitMode.CLOAK && targetEnergy > 0.0F;
                        float groupCenterX = NanosuitHudOverlay.RightHud.centerX(Minecraft.getInstance().getWindow().getGuiScaledWidth(), hudOffsetX, hudScale);
                        float groupCenterY = NanosuitHudOverlay.RightHud.centerY(Minecraft.getInstance().getWindow().getGuiScaledHeight(), hudOffsetY, hudScale);
                        int x = NanosuitHudOverlay.RightHud.topLeftXFromCenter(groupCenterX, 450, hudScale);
                        int y = NanosuitHudOverlay.RightHud.topLeftYFromCenter(groupCenterY, 180, hudScale);
                        int screenCenterX = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
                        int screenCenterY = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2;
                        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
                        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
                        if (classic) {
                           resetArmorOverlayIntroState();
                        } else {
                           renderArmorModeOverlay(guiGraphics, screenWidth, screenHeight, screenCenterX, screenCenterY,
                              event.getPartialTick().getGameTimeDeltaPartialTick(false), armorModeActive);
                        }
                        boolean lowEnergy = classic && targetEnergy <= 15.0F;
                        renderEnergyOffLayer(guiGraphics, x, y, screenCenterX, screenCenterY, lowEnergy);
                        renderEnergyActiveLayer(guiGraphics, x, y, screenCenterX, screenCenterY, targetEnergy, lowEnergy);
                        if (classic) {
                           renderNanosuit1Mode(guiGraphics, groupCenterX, groupCenterY, screenCenterX, screenCenterY, data);
                        } else {
                           renderModesLayer(
                              guiGraphics, groupCenterX, groupCenterY, screenCenterX, screenCenterY, armorModeActive, forceModeActive, cloakModeActive
                           );
                        }
                        if (NanosuitClientConfig.debugEnergyDrainInfo() && player instanceof LocalPlayer localPlayer) {
                           renderEnergyDrainDebug(guiGraphics, localPlayer, data, profile, screenCenterX, screenCenterY);
                        }
                     }
                  );
               }
            } else {
               resetArmorOverlayIntroState();
            }
         }
      }
   }

   private static void renderNanosuit1Mode(
      GuiGraphics guiGraphics, float groupCenterX, float groupCenterY, int screenCenterX, int screenCenterY, INanosuitEnergy data
   ) {
      boolean locked = data.isRecoveryLocked();
      String modeKey = locked ? "locked" : data.getMode().name().toLowerCase(Locale.ROOT);
      Component label = Component.translatable("hud.crysismod.nanosuit1.mode." + modeKey);
      guiGraphics.pose().pushPose();
      applyVisorInertiaTransform(guiGraphics, screenCenterX, screenCenterY);
      guiGraphics.pose().translate(groupCenterX, groupCenterY + 54.0F * hudScale, 0.0F);
      guiGraphics.pose().scale(5.0F * hudScale, 5.0F * hudScale, 1.0F);
      guiGraphics.drawCenteredString(Minecraft.getInstance().font, label, 0, 0, locked ? 0xA0A0A0 : 0xE0FFF8);
      guiGraphics.pose().popPose();
   }

   private static void renderEnergyDrainDebug(
      GuiGraphics guiGraphics, LocalPlayer player, INanosuitEnergy data, NanosuitProfile profile, int screenCenterX, int screenCenterY
   ) {
      boolean sprint = NanosuitPlayerHandler.isLandPowerSprintActive(player) || NanosuitSwimBoost.isActive(player);
      boolean moving = isMovingClient(player);
      NanosuitMode mode = data.getMode();
      String branch;
      String drainLabel;
      float drainPerTick;
      boolean classic = profile == NanosuitProfile.NANOSUIT_1;
      if (classic) {
         branch = data.isRecoveryLocked() ? "LOCKED" : mode.name() + (sprint ? "_BOOST" : "_IDLE");
         drainPerTick = 0.0F;
      } else if (mode == NanosuitMode.CLOAK) {
         if (sprint) {
            branch = "CLOAK_SPRINT";
            drainPerTick = 0.5F;
         } else if (moving) {
            branch = "CLOAK_WALK";
            drainPerTick = 0.25F;
         } else {
            branch = "CLOAK_STAND";
            drainPerTick = 0.0625F;
         }
      } else if (mode == NanosuitMode.ARMOR) {
         if (sprint) {
            branch = "ARMOR_SPRINT";
            drainPerTick = 0.25F;
         } else if (moving) {
            branch = "ARMOR_WALK";
            drainPerTick = 0.083333336F;
         } else {
            branch = "ARMOR_STAND";
            drainPerTick = 0.0625F;
         }
      } else {
         branch = sprint ? "NONE_SPRINT" : (moving ? "NONE_WALK" : "NONE_STAND");
         drainPerTick = sprint
            ? NanosuitServerConfig.sprintingDrainPerTick()
            : (moving ? NanosuitServerConfig.walkingDrainPerTick() : NanosuitServerConfig.standingDrainPerTick());
      }

      if (classic) {
         drainLabel = String.format(Locale.ROOT, "server energy=%.2f", data.getEnergy());
      } else {
         drainLabel = String.format(Locale.ROOT, "drain/tick=%.5f  drain/sec=%.3f  energy=%.2f",
            drainPerTick, drainPerTick * 20.0F, data.getEnergy());
      }
      float walkDistDelta = player.walkDist - player.walkDistO;
      double dx = player.getX() - player.xo;
      double dz = player.getZ() - player.zo;
      double posDelta = Math.sqrt(dx * dx + dz * dz);
      int x = screenCenterX - 180;
      int y = 16;
      int bg = -1879048192;
      guiGraphics.fill(x - 4, y - 4, x + 330, y + 44, bg);
      guiGraphics.drawString(
         Minecraft.getInstance().font, "Drain: " + branch + " | mode=" + mode + " | sprint=" + sprint + " | moving=" + moving, x, y, -1509121, false
      );
      guiGraphics.drawString(
         Minecraft.getInstance().font,
         drainLabel,
         x,
         y + 12,
         -6620161,
         false
      );
      guiGraphics.drawString(
         Minecraft.getInstance().font,
         String.format(Locale.ROOT, "walkDistΔ=%.5f  posΔ=%.5f  input[xxa=%.3f zza=%.3f]", walkDistDelta, posDelta, player.xxa, player.zza),
         x,
         y + 24,
         -7417640,
         false
      );
   }

   private static boolean isMovingClient(LocalPlayer player) {
      if (Math.abs(player.walkDist - player.walkDistO) > 0.001F) {
         return true;
      } else if (!(Math.abs(player.xxa) > 0.01F) && !(Math.abs(player.zza) > 0.01F)) {
         double dx = player.getX() - player.xo;
         double dz = player.getZ() - player.zo;
         return dx * dx + dz * dz > 6.25E-4 ? true : player.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4;
      } else {
         return true;
      }
   }

   private static void renderEnergyOffLayer(GuiGraphics guiGraphics, int x, int y, int screenCenterX, int screenCenterY, boolean lowEnergy) {
      applyConfiguredTextureFilterIfNeeded();
      enableDefaultHudBlend();
      renderHudSlice(guiGraphics, NanosuitHudOverlay.RightHud.ENERGY_BAR_OFF, x, y, screenCenterX, screenCenterY,
         0, 450, 450, 180, offLayerAlpha, 1.0F, 1.0F, lowEnergy);
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
   }

   private static void renderEnergyActiveLayer(GuiGraphics guiGraphics, int x, int y, int screenCenterX, int screenCenterY,
      float energy, boolean lowEnergy) {
      float progress = Mth.clamp(energy / 100.0F, 0.0F, 1.0F);
      boolean treatAsFull = energy >= 99.999F;
      int filledSegments = !treatAsFull && !(progress >= 0.9975F) ? Mth.clamp(Mth.floor(progress * 20.0F + 1.0E-6F), 0, 20) : 20;
      if (filledSegments > 0) {
         int srcFillWidth = fillWidthForSegments(filledSegments);
         if (srcFillWidth > 0) {
            applyConfiguredTextureFilterIfNeeded();
            enableDefaultHudBlend();
            renderHudSlice(
               guiGraphics, NanosuitHudOverlay.RightHud.ENERGY_BAR_ACTIVE, x, y, screenCenterX, screenCenterY,
               0, srcFillWidth, 450, 180, 1.0F, 1.0F, 1.0F, lowEnergy
            );
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
         }
      }
   }

   private static void renderModesLayer(
      GuiGraphics guiGraphics,
      float groupCenterX,
      float groupCenterY,
      int screenCenterX,
      int screenCenterY,
      boolean armorModeActive,
      boolean forceModeActive,
      boolean cloakModeActive
   ) {
      applyConfiguredTextureFilterIfNeeded();
      enableDefaultHudBlend();
      int modesX = NanosuitHudOverlay.RightHud.topLeftXFromCenter(groupCenterX, 450, hudScale);
      int modesY = NanosuitHudOverlay.RightHud.topLeftYFromCenter(groupCenterY, 300, hudScale);
      renderHudSlice(guiGraphics, NanosuitHudOverlay.RightHud.MODES_OFF, modesX, modesY, screenCenterX, screenCenterY, 0, 450, 450, 300, 1.0F, 1.0F, 1.0F, false);
      if (armorModeActive) {
         renderModeSegment(guiGraphics, modesX, modesY, screenCenterX, screenCenterY, 0);
      }

      if (forceModeActive) {
         renderModeSegment(guiGraphics, modesX, modesY, screenCenterX, screenCenterY, 1);
      }

      if (cloakModeActive) {
         renderModeSegment(guiGraphics, modesX, modesY, screenCenterX, screenCenterY, 2);
      }

      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
   }

   private static void renderModeSegment(GuiGraphics guiGraphics, int x, int y, int screenCenterX, int screenCenterY, int segmentIndex) {
      int clampedIndex = Mth.clamp(segmentIndex, 0, 2);
      int srcX = clampedIndex * 150;
      int srcW = Mth.clamp(450 - srcX, 0, 150);
      if (srcW > 0) {
         renderHudSlice(guiGraphics, NanosuitHudOverlay.RightHud.MODES_ACTIVE, x, y, screenCenterX, screenCenterY, srcX, srcW, 450, 300, 1.0F, 1.0F, 1.0F, false);
      }
   }

   private static void enableDefaultHudBlend() {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
   }

   private static void renderArmorModeOverlay(
      GuiGraphics guiGraphics, int screenWidth, int screenHeight, int screenCenterX, int screenCenterY, float partialTick, boolean armorModeActive
   ) {
      NanosuitHudOverlay.OverlayAnimationFrame frame = updateArmorOverlayAnimationFrame(partialTick, armorModeActive);
      if (frame.shouldRender()) {
         applyConfiguredTextureFilterIfNeeded();
         enableDefaultHudBlend();
         guiGraphics.pose().pushPose();
         applyVisorInertiaTransform(guiGraphics, screenCenterX, screenCenterY);
         guiGraphics.pose().translate(screenCenterX, screenCenterY, 0.0F);
         guiGraphics.pose().scale(armorModeOverlayScale, armorModeOverlayScale, 1.0F);
         guiGraphics.pose().translate(-screenCenterX, -screenCenterY, 0.0F);
         if (frame.animated()) {
            renderArmorOverlayAnimated(guiGraphics, screenWidth, screenHeight, frame);
         } else {
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.blit(NanosuitHudOverlay.RightHud.ARMOR_MODE_OVERLAY, 0, 0, 0.0F, 0.0F, screenWidth, screenHeight, screenWidth, screenHeight);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         }

         guiGraphics.pose().popPose();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.disableBlend();
         if (armorOverlayIntroDebug) {
            renderArmorOverlayIntroDebug(guiGraphics, screenWidth, screenHeight, frame);
         }
      }
   }

   private static void renderArmorOverlayAnimated(GuiGraphics guiGraphics, int screenWidth, int screenHeight, NanosuitHudOverlay.OverlayAnimationFrame frame) {
      ensureArmorOverlayGridLayout(screenWidth, screenHeight, armorOverlayIntroSegmentCount);
      NanosuitHudOverlay.OverlayGridLayout layout = armorOverlayGridLayout;
      float progress = frame.progress();
      float alphaBase = armorOverlayIntroAlphaFade ? easeOutCubic(progress) : 1.0F;
      renderArmorOverlayWarpPass(guiGraphics, layout, progress, alphaBase, false);
   }

   private static void renderArmorOverlayWarpPass(
      GuiGraphics guiGraphics, NanosuitHudOverlay.OverlayGridLayout layout, float progress, float alpha, boolean glowTint
   ) {
      layout.ensureWarpCapacity();
      computeArmorOverlayWarpVertices(layout, progress);
      Matrix4f matrix = guiGraphics.pose().last().pose();
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, NanosuitHudOverlay.RightHud.ARMOR_MODE_OVERLAY);
      if (glowTint) {
         RenderSystem.setShaderColor(0.62F, 0.92F, 1.0F, Mth.clamp(alpha, 0.0F, 1.0F));
      } else {
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, Mth.clamp(alpha, 0.0F, 1.0F));
      }

      BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

      for (int row = 0; row < layout.rows; row++) {
         int rowOffset = row * (layout.cols + 1);
         int nextRowOffset = (row + 1) * (layout.cols + 1);

         for (int col = 0; col < layout.cols; col++) {
            int i00 = rowOffset + col;
            int i10 = rowOffset + col + 1;
            int i11 = nextRowOffset + col + 1;
            int i01 = nextRowOffset + col;
            buffer.addVertex(matrix, layout.warpX[i01], layout.warpY[i01], 0.0F).setUv(layout.u[i01], layout.v[i01]);
            buffer.addVertex(matrix, layout.warpX[i11], layout.warpY[i11], 0.0F).setUv(layout.u[i11], layout.v[i11]);
            buffer.addVertex(matrix, layout.warpX[i10], layout.warpY[i10], 0.0F).setUv(layout.u[i10], layout.v[i10]);
            buffer.addVertex(matrix, layout.warpX[i00], layout.warpY[i00], 0.0F).setUv(layout.u[i00], layout.v[i00]);
         }
      }

      BufferUploader.drawWithShader(buffer.buildOrThrow());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private static void computeArmorOverlayWarpVertices(NanosuitHudOverlay.OverlayGridLayout layout, float progress) {
      for (int i = 0; i < layout.vertexCount; i++) {
         float localProgress = computeArmorOverlayLocalProgress(layout, i, progress);
         float eased = computeArmorOverlayElasticProgress(localProgress);
         float startDistance = armorOverlayIntroStartOutsideDistance * (1.0F + layout.normalizedRadius[i] * 0.3F);
         float pull = 1.0F - eased;
         float offsetX = layout.dirX[i] * startDistance * pull;
         float offsetY = layout.dirY[i] * startDistance * pull;
         layout.warpX[i] = layout.baseX[i] + offsetX;
         layout.warpY[i] = layout.baseY[i] + offsetY;
      }
   }

   private static NanosuitHudOverlay.OverlayAnimationFrame updateArmorOverlayAnimationFrame(float partialTick, boolean armorModeActive) {
      float now = currentTimeTicks(partialTick);
      if (armorModeActive != armorOverlayWasActiveLastFrame) {
         armorOverlayWasActiveLastFrame = armorModeActive;
         armorOverlayLastSampleTicks = now;
         if (armorOverlayIntroEnabled) {
            armorOverlayAnimPlaying = true;
            armorOverlayAnimDirection = armorModeActive ? 1 : -1;
            armorOverlayAnimElapsedTicks = armorModeActive ? 0.0F : armorOverlayIntroDurationTicks;
         } else {
            armorOverlayAnimPlaying = false;
            armorOverlayAnimDirection = armorModeActive ? 1 : -1;
            armorOverlayAnimElapsedTicks = armorModeActive ? armorOverlayIntroDurationTicks : 0.0F;
         }

         if (armorOverlayAnimDirection < 0) {
            playUiSound((SoundEvent)ModSounds.ARMOR_MODE_DISABLE.get(), 1.0F);
         } else {
            playUiSound((SoundEvent)ModSounds.ARMOR_MODE_ACTIVATION.get(), 1.0F);
            playUiSound((SoundEvent)ModSounds.MAXIMUM_ARMOR.get(), 1.0F);
         }
      }

      float delta = now - armorOverlayLastSampleTicks;
      armorOverlayLastSampleTicks = now;
      if (!Float.isFinite(delta) || delta < 0.0F) {
         delta = 0.0F;
      }

      if (delta > 4.0F) {
         delta = 4.0F;
      }

      if (armorOverlayAnimPlaying) {
         if (armorOverlayAnimDirection > 0) {
            armorOverlayAnimElapsedTicks += delta;
            if (armorOverlayAnimElapsedTicks >= armorOverlayIntroDurationTicks) {
               armorOverlayAnimElapsedTicks = armorOverlayIntroDurationTicks;
               armorOverlayAnimPlaying = false;
            }
         } else {
            armorOverlayAnimElapsedTicks -= delta;
            if (armorOverlayAnimElapsedTicks <= 0.0F) {
               armorOverlayAnimElapsedTicks = 0.0F;
               armorOverlayAnimPlaying = false;
            }
         }
      }

      float duration = Math.max(1.0F, (float)armorOverlayIntroDurationTicks);
      float progress = Mth.clamp(armorOverlayAnimElapsedTicks / duration, 0.0F, 1.0F);
      boolean shouldRender = armorModeActive || progress > 1.0E-4F || armorOverlayAnimPlaying && armorOverlayAnimDirection < 0;
      boolean animated = armorOverlayIntroEnabled && (armorOverlayAnimPlaying || armorOverlayAnimDirection < 0 && progress > 1.0E-4F);
      int direction = armorOverlayAnimDirection;
      return new NanosuitHudOverlay.OverlayAnimationFrame(shouldRender, animated, progress, direction, armorOverlayAnimPlaying);
   }

   private static void playUiSound(SoundEvent soundEvent, float pitch) {
      Minecraft minecraft = Minecraft.getInstance();
      float volume = 0.65F * NanosuitSoundVolumes.modes();
      if (minecraft.getSoundManager() != null && volume > 0.0F) {
         minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, pitch, volume));
      }
   }

   private static float currentTimeTicks(float partialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft.level == null ? partialTick : (float)minecraft.level.getGameTime() + partialTick;
   }

   private static float computeArmorOverlayLocalProgress(NanosuitHudOverlay.OverlayGridLayout layout, int vertexIndex, float globalProgress) {
      float edgeOutside = Mth.clamp((layout.ellipseDistance[vertexIndex] - 1.0F) / 1.6F, 0.0F, 1.0F);
      float outerWeight = 0.3F + 0.7F * layout.normalizedRadius[vertexIndex];
      float delay = Mth.clamp(armorOverlayIntroCenterDelayBias * edgeOutside * outerWeight, 0.0F, 0.92F);
      float denom = Math.max(0.001F, 1.0F - delay);
      return Mth.clamp((globalProgress - delay) / denom, 0.0F, 1.0F);
   }

   private static float computeArmorOverlayElasticProgress(float t) {
      if (t <= 0.0F) {
         return 0.0F;
      } else if (t >= 1.0F) {
         return 1.0F;
      } else {
         float smooth = easeOutCubic(t);
         float back = easeOutBack(t, 1.70158F + armorOverlayIntroOvershoot * 6.0F);
         return Mth.lerp(armorOverlayIntroElasticStrength, smooth, back);
      }
   }

   private static float easeOutCubic(float t) {
      float oneMinus = 1.0F - t;
      return 1.0F - oneMinus * oneMinus * oneMinus;
   }

   private static float easeOutBack(float t, float s) {
      float inv = t - 1.0F;
      return 1.0F + inv * inv * ((s + 1.0F) * inv + s);
   }

   private static void ensureArmorOverlayGridLayout(int screenWidth, int screenHeight, int segmentCount) {
      if (!armorOverlayGridLayout.matches(screenWidth, screenHeight, segmentCount)) {
         armorOverlayGridLayout = NanosuitHudOverlay.OverlayGridLayout.build(screenWidth, screenHeight, segmentCount);
      }
   }

   private static void resetArmorOverlayIntroState() {
      armorOverlayWasActiveLastFrame = false;
      armorOverlayAnimPlaying = false;
      armorOverlayAnimDirection = 1;
      armorOverlayAnimElapsedTicks = 0.0F;
      armorOverlayLastSampleTicks = Float.NaN;
   }

   private static void refreshArmorOverlayIntroSettingsFromConfig() {
      armorOverlayIntroEnabled = NanosuitClientConfig.armorOverlayIntroEnabled();
      armorOverlayIntroDurationTicks = Mth.clamp(NanosuitClientConfig.armorOverlayIntroDurationTicks(), 4, 60);
      armorOverlayIntroElasticStrength = Mth.clamp(NanosuitClientConfig.armorOverlayIntroElasticStrength(), 0.0F, 1.0F);
      int targetSegmentCount = Mth.clamp(NanosuitClientConfig.armorOverlayIntroSegmentCount(), 8, 128);
      if (targetSegmentCount != armorOverlayIntroSegmentCount) {
         armorOverlayGridLayout = NanosuitHudOverlay.OverlayGridLayout.EMPTY;
      }

      armorOverlayIntroSegmentCount = targetSegmentCount;
      armorOverlayIntroOvershoot = Mth.clamp(NanosuitClientConfig.armorOverlayIntroOvershoot(), 0.0F, 0.3F);
      armorOverlayIntroStartOutsideDistance = Mth.clamp(NanosuitClientConfig.armorOverlayIntroStartOutsideDistance(), 0, 600);
      armorOverlayIntroCenterDelayBias = Mth.clamp(NanosuitClientConfig.armorOverlayIntroCenterDelayBias(), 0.0F, 1.0F);
      armorOverlayIntroAlphaFade = NanosuitClientConfig.armorOverlayIntroAlphaFade();
      armorOverlayIntroGlowPulse = NanosuitClientConfig.armorOverlayIntroGlowPulse();
      armorOverlayMotionBlurEnabled = NanosuitClientConfig.armorOverlayMotionBlurEnabled();
      armorOverlayMotionBlurStrength = Mth.clamp(NanosuitClientConfig.armorOverlayMotionBlurStrength(), 0.0F, 1.0F);
      armorOverlayMotionBlurSamples = Mth.clamp(NanosuitClientConfig.armorOverlayMotionBlurSamples(), 1, 8);
      armorOverlayIntroDebug = NanosuitClientConfig.armorOverlayIntroDebug();
   }

   private static void renderArmorOverlayIntroDebug(GuiGraphics guiGraphics, int screenWidth, int screenHeight, NanosuitHudOverlay.OverlayAnimationFrame frame) {
      int x = 10;
      int y = 10;
      int bg = -1912602624;
      guiGraphics.fill(x - 4, y - 4, x + 440, y + 64, bg);
      guiGraphics.drawString(
         Minecraft.getInstance().font,
         "Armor Overlay animPlaying=" + frame.playing() + " dir=" + frame.direction() + " progress=" + String.format(Locale.ROOT, "%.3f", frame.progress()),
         x,
         y,
         -1638401,
         false
      );
      guiGraphics.drawString(
         Minecraft.getInstance().font,
         "dur="
            + armorOverlayIntroDurationTicks
            + " seg="
            + armorOverlayIntroSegmentCount
            + " elastic="
            + String.format(Locale.ROOT, "%.2f", armorOverlayIntroElasticStrength)
            + " over="
            + String.format(Locale.ROOT, "%.2f", armorOverlayIntroOvershoot),
         x,
         y + 12,
         -6620161,
         false
      );
      guiGraphics.drawString(
         Minecraft.getInstance().font,
         "outside="
            + armorOverlayIntroStartOutsideDistance
            + " delayBias="
            + String.format(Locale.ROOT, "%.2f", armorOverlayIntroCenterDelayBias)
            + " alphaFade="
            + armorOverlayIntroAlphaFade
            + " glowPulse="
            + armorOverlayIntroGlowPulse,
         x,
         y + 24,
         -6499104,
         false
      );
      guiGraphics.drawString(
         Minecraft.getInstance().font,
         "blurEnabled="
            + armorOverlayMotionBlurEnabled
            + " blurStrength="
            + String.format(Locale.ROOT, "%.2f", armorOverlayMotionBlurStrength)
            + " blurSamples="
            + armorOverlayMotionBlurSamples,
         x,
         y + 36,
         -6499104,
         false
      );
      guiGraphics.drawString(
         Minecraft.getInstance().font,
         "screen="
            + screenWidth
            + "x"
            + screenHeight
            + " grid="
            + armorOverlayGridLayout.cols
            + "x"
            + armorOverlayGridLayout.rows
            + " vertices="
            + armorOverlayGridLayout.vertexCount,
         x,
         y + 48,
         -7622976,
         false
      );
   }

   private static void renderHudSlice(
      GuiGraphics guiGraphics,
      ResourceLocation texture,
      int x,
      int y,
      int screenCenterX,
      int screenCenterY,
      int srcX,
      int srcFillWidth,
      int textureW,
      int textureH,
      float alpha,
      float scaleX,
      float scaleY,
      boolean lowEnergy
   ) {
      guiGraphics.pose().pushPose();
      applyVisorInertiaTransform(guiGraphics, screenCenterX, screenCenterY);
      guiGraphics.pose().translate(x, y, 0.0F);
      guiGraphics.pose().scale(hudScale * scaleX, hudScale * scaleY, 1.0F);
      guiGraphics.setColor(1.0F, lowEnergy ? 0.24F : 1.0F, lowEnergy ? 0.18F : 1.0F, alpha);
      guiGraphics.blit(texture, srcX, 0, srcX, 0.0F, srcFillWidth, textureH, textureW, textureH);
      guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      guiGraphics.pose().popPose();
   }

   private static void applyVisorInertiaTransform(GuiGraphics guiGraphics, int screenCenterX, int screenCenterY) {
      float offsetX = HUD_INERTIA.getOffsetX();
      float offsetY = HUD_INERTIA.getOffsetY();
      float rollDeg = HUD_INERTIA.getRollDeg();
      if (!(Math.abs(offsetX) < 0.001F) || !(Math.abs(offsetY) < 0.001F) || !(Math.abs(rollDeg) < 0.001F)) {
         guiGraphics.pose().translate(screenCenterX + offsetX, screenCenterY + offsetY, 0.0F);
         guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(rollDeg));
         guiGraphics.pose().translate(-screenCenterX, -screenCenterY, 0.0F);
      }
   }

   public static void applySharedRightHudInertiaTransform(GuiGraphics guiGraphics, int screenCenterX, int screenCenterY) {
      applyVisorInertiaTransform(guiGraphics, screenCenterX, screenCenterY);
   }

   public static NanosuitHudOverlay.HudInertiaSnapshot sharedRightHudInertiaSnapshot() {
      return new NanosuitHudOverlay.HudInertiaSnapshot(HUD_INERTIA.getOffsetX(), HUD_INERTIA.getOffsetY(), HUD_INERTIA.getRollDeg());
   }

   public static void beginSharedHudFrame(long frameId, float partialTick) {
      if (frameId != lastSharedInertiaFrameId) {
         syncSharedHudInertia(partialTick);
         lastSharedInertiaFrameId = frameId;
      }
   }

   public static void ensureSharedHudInertiaSynced(float partialTick) {
      syncSharedHudInertia(partialTick);
   }

   private static void syncSharedHudInertia(float partialTick) {
      if (!configInitialized) {
         loadFromConfig();
      }

      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer localPlayer = minecraft.player;
      HUD_INERTIA.update(
         localPlayer,
         partialTick,
         HUD_INERTIA_PARAMS.set(
            inertiaEnabled,
            inertiaIntensity,
            yawInertiaStrength,
            pitchInertiaStrength,
            maxViewOffset,
            maxRollDegrees,
            dampingStrength,
            returnSpeed,
            walkSideAmplitude,
            walkVerticalAmplitude,
            walkRollAmplitude,
            walkStepDuration,
            walkSpringStrength,
            walkDamping,
            walkReturnToCenterSpeed,
            sprintWalkMultiplier,
            maxWalkOffsetX,
            maxWalkOffsetY,
            maxWalkRoll
         )
      );
   }

   private static void applyConfiguredTextureFilterIfNeeded() {
      if (textureFilterDirty) {
         applyTextureFilter(NanosuitHudOverlay.RightHud.ENERGY_BAR_ACTIVE, textureSmooth, textureMipmap);
         applyTextureFilter(NanosuitHudOverlay.RightHud.ENERGY_BAR_OFF, textureSmooth, textureMipmap);
         applyTextureFilter(NanosuitHudOverlay.RightHud.MODES_ACTIVE, textureSmooth, textureMipmap);
         applyTextureFilter(NanosuitHudOverlay.RightHud.MODES_OFF, textureSmooth, textureMipmap);
         applyTextureFilter(NanosuitHudOverlay.RightHud.ARMOR_MODE_OVERLAY, textureSmooth, textureMipmap);
         textureFilterDirty = false;
      }
   }

   private static void applyTextureFilter(ResourceLocation textureId, boolean smooth, boolean mipmap) {
      AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(textureId);
      texture.setFilter(smooth, mipmap);
   }

   private static int fillWidthForSegments(int segmentCount) {
      int count = Mth.clamp(segmentCount, 0, 20);
      if (count <= 0) {
         return 0;
      } else {
         return count >= 20 ? 450 : 16 + count * 18 + count * 3;
      }
   }

   public static void loadFromConfig() {
      hudEnabled = NanosuitClientConfig.hudEnabled();
      hudOffsetX = NanosuitClientConfig.hudOffsetX();
      hudOffsetY = NanosuitClientConfig.hudOffsetY();
      hudScale = Mth.clamp(NanosuitClientConfig.hudScale(), 0.05F, 5.0F);
      armorModeOverlayScale = Mth.clamp(NanosuitClientConfig.armorModeOverlayScale(), 0.25F, 2.0F);
      offLayerAlpha = Mth.clamp(NanosuitClientConfig.offLayerAlpha(), 0.0F, 1.0F);
      textureSmooth = NanosuitClientConfig.textureSmooth();
      textureMipmap = NanosuitClientConfig.textureMipmap();
      inertiaEnabled = NanosuitClientConfig.inertiaEnabled();
      inertiaIntensity = Mth.clamp(NanosuitClientConfig.inertiaIntensity(), 0.0F, 3.0F);
      walkSideAmplitude = Mth.clamp(NanosuitClientConfig.walkSideAmplitude(), 0.0F, 12.0F);
      walkVerticalAmplitude = Mth.clamp(NanosuitClientConfig.walkVerticalAmplitude(), 0.0F, 12.0F);
      walkRollAmplitude = Mth.clamp(NanosuitClientConfig.walkRollAmplitude(), 0.0F, 10.0F);
      walkStepDuration = Mth.clamp(NanosuitClientConfig.walkStepDuration(), 0.12F, 1.0F);
      walkSpringStrength = Mth.clamp(NanosuitClientConfig.walkSpringStrength(), 1.0F, 80.0F);
      walkDamping = Mth.clamp(NanosuitClientConfig.walkDamping(), 0.1F, 60.0F);
      walkReturnToCenterSpeed = Mth.clamp(NanosuitClientConfig.walkReturnToCenterSpeed(), 0.1F, 60.0F);
      sprintWalkMultiplier = Mth.clamp(NanosuitClientConfig.sprintWalkMultiplier(), 1.0F, 3.0F);
      maxWalkOffsetX = Mth.clamp(NanosuitClientConfig.maxWalkOffsetX(), 0.0F, 80.0F);
      maxWalkOffsetY = Mth.clamp(NanosuitClientConfig.maxWalkOffsetY(), 0.0F, 80.0F);
      maxWalkRoll = Mth.clamp(NanosuitClientConfig.maxWalkRoll(), 0.0F, 8.0F);
      yawInertiaStrength = Mth.clamp(NanosuitClientConfig.yawInertiaStrength(), 0.0F, 0.2F);
      pitchInertiaStrength = Mth.clamp(NanosuitClientConfig.pitchInertiaStrength(), 0.0F, 0.2F);
      maxViewOffset = Mth.clamp(NanosuitClientConfig.maxViewOffset(), 0.0F, 40.0F);
      maxRollDegrees = Mth.clamp(NanosuitClientConfig.maxRollDegrees(), 0.0F, 10.0F);
      dampingStrength = Mth.clamp(NanosuitClientConfig.dampingStrength(), 0.1F, 60.0F);
      returnSpeed = Mth.clamp(NanosuitClientConfig.returnSpeed(), 0.1F, 60.0F);
      refreshArmorOverlayIntroSettingsFromConfig();
      armorOverlayGridLayout = NanosuitHudOverlay.OverlayGridLayout.EMPTY;
      resetArmorOverlayIntroState();
      textureFilterDirty = true;
      configInitialized = true;
   }

   public static void setHudOffsetX(int offset) {
      hudOffsetX = offset;
      NanosuitClientConfig.setHudOffsetX(hudOffsetX);
   }

   public static void setHudOffsetY(int offset) {
      hudOffsetY = offset;
      NanosuitClientConfig.setHudOffsetY(hudOffsetY);
   }

   public static void moveHudOffset(int dx, int dy) {
      setHudOffsetX(hudOffsetX + dx);
      setHudOffsetY(hudOffsetY + dy);
   }

   public static void resetHudOffset() {
      setHudOffsetX(-28);
      setHudOffsetY(-8);
   }

   public static int getHudOffsetX() {
      return hudOffsetX;
   }

   public static int getHudOffsetY() {
      return hudOffsetY;
   }

   public static float getRightHudGroupCenterX(int screenWidth) {
      return NanosuitHudOverlay.RightHud.centerX(screenWidth, hudOffsetX, hudScale);
   }

   public static float getRightHudGroupCenterY(int screenHeight) {
      return NanosuitHudOverlay.RightHud.centerY(screenHeight, hudOffsetY, hudScale);
   }

   public static void setTextureFilter(boolean smooth, boolean mipmap) {
      if (textureSmooth != smooth || textureMipmap != mipmap) {
         textureSmooth = smooth;
         textureMipmap = mipmap;
         NanosuitClientConfig.setTextureSmooth(textureSmooth);
         NanosuitClientConfig.setTextureMipmap(textureMipmap);
         textureFilterDirty = true;
      }
   }

   public static boolean isTextureSmooth() {
      return textureSmooth;
   }

   public static boolean isTextureMipmap() {
      return textureMipmap;
   }

   public static void setHudScale(float scale) {
      hudScale = Mth.clamp(scale, 0.05F, 5.0F);
      NanosuitClientConfig.setHudScale(hudScale);
   }

   public static float addHudScale(float delta) {
      setHudScale(hudScale + delta);
      return hudScale;
   }

   public static void resetHudScale() {
      setHudScale(0.2F);
   }

   public static float getHudScale() {
      return hudScale;
   }

   public static void setArmorModeOverlayScale(float scale) {
      armorModeOverlayScale = Mth.clamp(scale, 0.25F, 2.0F);
      NanosuitClientConfig.setArmorModeOverlayScale(armorModeOverlayScale);
   }

   public static float getArmorModeOverlayScale() {
      return armorModeOverlayScale;
   }

   public static void setOffLayerAlpha(float alpha) {
      offLayerAlpha = Mth.clamp(alpha, 0.0F, 1.0F);
      NanosuitClientConfig.setOffLayerAlpha(offLayerAlpha);
   }

   public static float addOffLayerAlpha(float delta) {
      setOffLayerAlpha(offLayerAlpha + delta);
      return offLayerAlpha;
   }

   public static void resetOffLayerAlpha() {
      setOffLayerAlpha(1.0F);
   }

   public static float getOffLayerAlpha() {
      return offLayerAlpha;
   }

   public static void setMaxWalkOffsetX(float value) {
      maxWalkOffsetX = Mth.clamp(value, 0.0F, 80.0F);
      NanosuitClientConfig.setMaxWalkOffsetX(maxWalkOffsetX);
   }

   public static void setMaxWalkOffsetY(float value) {
      maxWalkOffsetY = Mth.clamp(value, 0.0F, 80.0F);
      NanosuitClientConfig.setMaxWalkOffsetY(maxWalkOffsetY);
   }

   public static float getMaxWalkOffsetX() {
      return maxWalkOffsetX;
   }

   public static float getMaxWalkOffsetY() {
      return maxWalkOffsetY;
   }

   public static void setHudEnabled(boolean enabled) {
      hudEnabled = enabled;
      NanosuitClientConfig.setHudEnabled(hudEnabled);
   }

   public static boolean toggleHudEnabled() {
      setHudEnabled(!hudEnabled);
      return hudEnabled;
   }

   public static boolean isHudEnabled() {
      return hudEnabled;
   }

   public record HudInertiaSnapshot(float offsetX, float offsetY, float rollDeg) {
      public static final NanosuitHudOverlay.HudInertiaSnapshot ZERO = new NanosuitHudOverlay.HudInertiaSnapshot(0.0F, 0.0F, 0.0F);
   }

   private record OverlayAnimationFrame(boolean shouldRender, boolean animated, float progress, int direction, boolean playing) {
   }

   private static final class OverlayGridLayout {
      private static final NanosuitHudOverlay.OverlayGridLayout EMPTY = new NanosuitHudOverlay.OverlayGridLayout(
         0, 0, 0, 0, 0, new float[0], new float[0], new float[0], new float[0], new float[0], new float[0], new float[0], new float[0]
      );
      private final int screenWidth;
      private final int screenHeight;
      private final int segmentTarget;
      private final int cols;
      private final int rows;
      private final int vertexCount;
      private final float[] baseX;
      private final float[] baseY;
      private final float[] u;
      private final float[] v;
      private final float[] dirX;
      private final float[] dirY;
      private final float[] normalizedRadius;
      private final float[] ellipseDistance;
      private float[] warpX;
      private float[] warpY;

      private OverlayGridLayout(
         int screenWidth,
         int screenHeight,
         int segmentTarget,
         int cols,
         int rows,
         float[] baseX,
         float[] baseY,
         float[] u,
         float[] v,
         float[] dirX,
         float[] dirY,
         float[] normalizedRadius,
         float[] ellipseDistance
      ) {
         this.screenWidth = screenWidth;
         this.screenHeight = screenHeight;
         this.segmentTarget = segmentTarget;
         this.cols = cols;
         this.rows = rows;
         this.vertexCount = baseX.length;
         this.baseX = baseX;
         this.baseY = baseY;
         this.u = u;
         this.v = v;
         this.dirX = dirX;
         this.dirY = dirY;
         this.normalizedRadius = normalizedRadius;
         this.ellipseDistance = ellipseDistance;
         this.warpX = new float[this.vertexCount];
         this.warpY = new float[this.vertexCount];
      }

      private boolean matches(int width, int height, int segmentCount) {
         return width == this.screenWidth && height == this.screenHeight && segmentCount == this.segmentTarget;
      }

      private void ensureWarpCapacity() {
         if (this.warpX.length != this.vertexCount || this.warpY.length != this.vertexCount) {
            this.warpX = new float[this.vertexCount];
            this.warpY = new float[this.vertexCount];
         }
      }

      private static NanosuitHudOverlay.OverlayGridLayout build(int screenWidth, int screenHeight, int segmentCount) {
         int clampedSegments = Mth.clamp(segmentCount, 8, 128);
         float aspect = screenHeight <= 0 ? 1.0F : (float)screenWidth / screenHeight;
         int cols = Math.max(1, Math.round((float)Math.sqrt(clampedSegments * Math.max(0.25F, aspect))));
         int rows = Math.max(1, Mth.ceil((float)clampedSegments / cols));
         int vCols = cols + 1;
         int vRows = rows + 1;
         int total = vCols * vRows;
         float[] baseX = new float[total];
         float[] baseY = new float[total];
         float[] u = new float[total];
         float[] v = new float[total];
         float[] dirX = new float[total];
         float[] dirY = new float[total];
         float[] normalizedRadius = new float[total];
         float[] ellipseDistance = new float[total];
         float centerX = screenWidth * 0.5F;
         float centerY = screenHeight * 0.5F;
         float maxRadius = (float)Math.sqrt(centerX * centerX + centerY * centerY);
         float ovalRadiusX = Math.max(1.0F, screenWidth * 0.5F * 0.5F);
         float ovalRadiusY = Math.max(1.0F, screenHeight * 0.5F * 0.5F);
         int index = 0;

         for (int row = 0; row < vRows; row++) {
            float y = (float)row / rows * screenHeight;

            for (int col = 0; col < vCols; col++) {
               float x = (float)col / cols * screenWidth;
               baseX[index] = x;
               baseY[index] = y;
               u[index] = screenWidth <= 0 ? 0.0F : x / screenWidth;
               v[index] = screenHeight <= 0 ? 0.0F : y / screenHeight;
               float dx = x - centerX;
               float dy = y - centerY;
               float len = (float)Math.sqrt(dx * dx + dy * dy);
               if (len < 0.001F) {
                  dirX[index] = 0.0F;
                  dirY[index] = -1.0F;
               } else {
                  dirX[index] = dx / len;
                  dirY[index] = dy / len;
               }

               normalizedRadius[index] = maxRadius <= 0.001F ? 0.0F : Mth.clamp(len / maxRadius, 0.0F, 1.0F);
               float nx = dx / ovalRadiusX;
               float ny = dy / ovalRadiusY;
               ellipseDistance[index] = (float)Math.sqrt(nx * nx + ny * ny);
               index++;
            }
         }

         return new NanosuitHudOverlay.OverlayGridLayout(
            screenWidth, screenHeight, clampedSegments, cols, rows, baseX, baseY, u, v, dirX, dirY, normalizedRadius, ellipseDistance
         );
      }
   }

   private static final class RightHud {
      private static final ResourceLocation ENERGY_BAR_ACTIVE = ResourceLocation.fromNamespaceAndPath("crysismod", "textures/gui/energy_bar_active.png");
      private static final ResourceLocation ENERGY_BAR_OFF = ResourceLocation.fromNamespaceAndPath("crysismod", "textures/gui/energy_bar_off.png");
      private static final ResourceLocation MODES_ACTIVE = ResourceLocation.fromNamespaceAndPath("crysismod", "textures/gui/light_mods.png");
      private static final ResourceLocation MODES_OFF = ResourceLocation.fromNamespaceAndPath("crysismod", "textures/gui/off_mods.png");
      private static final ResourceLocation ARMOR_MODE_OVERLAY = ResourceLocation.fromNamespaceAndPath("crysismod", "textures/gui/armor_mode_overlay.png");
      private static final int ANCHOR_PADDING = 12;
      private static final int ACTIVE_SRC_W = 450;
      private static final int ACTIVE_SRC_H = 180;
      private static final int OFF_SRC_W = 450;
      private static final int OFF_SRC_H = 180;
      private static final int MODES_SRC_W = 450;
      private static final int MODES_SRC_H = 300;
      private static final int MODES_COUNT = 3;
      private static final int MODES_SEGMENT_W = 150;
      private static final int ARMOR_MODE_INDEX = 0;
      private static final int SPRINT_MODE_INDEX = 1;
      private static final int CLOAK_MODE_INDEX = 2;

      private static float centerX(int screenWidth, int offsetX, float hudScale) {
         return screenWidth - 12 - 450.0F * hudScale * 0.5F + offsetX;
      }

      private static float centerY(int screenHeight, int offsetY, float hudScale) {
         return screenHeight - 12 - 180.0F * hudScale * 0.5F + offsetY;
      }

      private static int topLeftXFromCenter(float centerX, int srcWidth, float hudScale) {
         return Math.round(centerX - srcWidth * hudScale * 0.5F);
      }

      private static int topLeftYFromCenter(float centerY, int srcHeight, float hudScale) {
         return Math.round(centerY - srcHeight * hudScale * 0.5F);
      }
   }
}
