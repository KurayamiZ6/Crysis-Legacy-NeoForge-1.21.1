package io.github.painworks.crysismod.nanosuit.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoArmorModeDynamicTextureCompatRenderer;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoArmorModeRenderTypes;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoArmorModeXShimmerShader;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoArmorRenderer;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoClientConfig;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoTextureCache;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import io.github.painworks.crysismod.client.nanovision.debug.NanovisionPerformanceProfiler;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmComposite;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmDeferredBuffer;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmRenderTarget;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmRenderTypes;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmShaderpackShaders;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmShaders;
import io.github.painworks.crysismod.nanosuit.item.ExperimentalNanoGeoArmorItem;
import io.github.painworks.crysismod.nanosuit.NanosuitPresentation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import org.slf4j.Logger;

public final class NanoGeoFirstPersonArmRenderer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final float COMPAT_PULSE_MIN_SHELL_SCALE = 0.006F;
   private static NanoGeoArmorRenderer armorRenderer;
   private static final NanoGeoTextureCache TEXTURE_CACHE = new NanoGeoTextureCache();
   private static boolean loggedModelData;
   private static boolean warnedFallback;

   private NanoGeoFirstPersonArmRenderer() {
   }

   public static boolean renderArmFromArmorModel(RenderArmEvent event) {
      AbstractClientPlayer player = event.getPlayer();
      if (player == null) {
         return false;
      } else if (player.isInvisible() && !NanosuitCloakRender.keepsArmGeometry(player)) {
         return false;
      } else {
         ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
         NanosuitPresentation presentation = NanosuitPresentation.forPiece(chestStack, EquipmentSlot.CHEST);
         if (presentation == null) {
            return false;
         } else {
            PlayerModel<AbstractClientPlayer> playerModel = getPlayerModel(player);
            if (playerModel == null) {
               return false;
            } else {
               MultiBufferSource bufferSource = event.getMultiBufferSource();
               if (bufferSource == null) {
                  return false;
               } else {
                  ModelPart selectedArmPart = event.getArm() == HumanoidArm.RIGHT ? playerModel.rightArm : playerModel.leftArm;
                  NanoGeoFirstPersonArmRenderer.ModelPartStateSnapshot selectedArmSnapshot = NanoGeoFirstPersonArmRenderer.ModelPartStateSnapshot.capture(
                     selectedArmPart
                  );
                  PoseStack poseStack = event.getPoseStack();
                  long profileCpuStarted = NanovisionPerformanceProfiler.beginCpu();
                  poseStack.pushPose();

                  boolean var32;
                  try {
                     NanoGeoArmorRenderer renderer = getArmorRenderer();
                     if (renderer != null) {
                        stabilizeArmPartForFirstPerson(selectedArmPart, event.getArm(), player);
                        renderer.beginFirstPersonArmOnly(playerModel, selectedArmPart, event.getArm());
                        renderer.prepForRender(player, chestStack, EquipmentSlot.CHEST, playerModel);
                        renderer.setSuppressRender(false);
                        ResourceLocation texture = resolveArmorTexture(chestStack, player, event.getArm(), presentation.armorTexture());
                        BufferSource immediateBuffer = bufferSource instanceof BufferSource source ? source : null;
                        boolean deferThermalComposite = NanoGeoShaderpackCompatibility.isShaderPackActive();
                        boolean thermalFirstPersonArm = !NanosuitCloakRender.isDrawing() && NanovisionState.isActive()
                           && ThermalFirstPersonArmShaders.isAvailable()
                           && (!deferThermalComposite || ThermalFirstPersonArmShaderpackShaders.isAvailable())
                           && (deferThermalComposite || immediateBuffer != null && ThermalFirstPersonArmRenderTarget.prepare());
                        RenderType thermalMarkerType = null;
                        VertexConsumer vertexConsumer;
                        if (thermalFirstPersonArm) {
                           thermalMarkerType = ThermalFirstPersonArmRenderTypes.warm(texture);
                           vertexConsumer = deferThermalComposite
                              ? ThermalFirstPersonArmDeferredBuffer.getBuffer(event.getArm(), thermalMarkerType)
                              : bufferSource.getBuffer(thermalMarkerType);
                        } else {
                           vertexConsumer = bufferSource.getBuffer(TEXTURE_CACHE.getOrCreateArmorRenderType(texture));
                        }

                        applyCommonTransform(poseStack);
                        applySideTransform(poseStack, event.getArm());
                        renderer.renderToBuffer(poseStack, vertexConsumer, event.getPackedLight(), OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                        if (thermalFirstPersonArm) {
                           float armProjectionScale = ThermalFirstPersonArmShaders.captureCurrentArmProjectionScale();
                           if (deferThermalComposite) {
                              ThermalFirstPersonArmDeferredBuffer.deferCurrentDraw(event.getArm());
                              VertexConsumer fallbackConsumer = bufferSource.getBuffer(TEXTURE_CACHE.getOrCreateArmorRenderType(texture));
                              renderer.renderToBuffer(poseStack, fallbackConsumer, event.getPackedLight(), OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                              ThermalFirstPersonArmComposite.markShaderpackCompositePending(armProjectionScale);
                           } else {
                              immediateBuffer.endBatch(thermalMarkerType);
                              RenderType depthType = ThermalFirstPersonArmRenderTypes.depthOnly(texture);
                              VertexConsumer depthConsumer = bufferSource.getBuffer(depthType);
                              renderer.renderToBuffer(poseStack, depthConsumer, event.getPackedLight(), OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                              immediateBuffer.endBatch(depthType);
                              if (!ThermalFirstPersonArmComposite.render(event.getArm(), armProjectionScale)) {
                                 thermalFirstPersonArm = false;
                                 VertexConsumer fallbackConsumer = bufferSource.getBuffer(TEXTURE_CACHE.getOrCreateArmorRenderType(texture));
                                 renderer.renderToBuffer(poseStack, fallbackConsumer, event.getPackedLight(), OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                              }
                           }
                        }

                        NanosuitArmorModeOverlayState.OverlayRenderState overlayState = NanosuitArmorModeOverlayState.resolveForFirstPerson(player);
                        if (thermalFirstPersonArm && overlayState.enabled()) {
                           renderThermalPulseOnly(renderer, player, poseStack, bufferSource, overlayState);
                        } else if (overlayState.enabled()) {
                           NanoGeoClientConfig.ArmorModeOverlayBackend backend = NanoGeoShaderpackCompatibility.resolveArmorModeOverlayBackend();
                           if (overlayState.usesClassicFibers() && backend != NanoGeoClientConfig.ArmorModeOverlayBackend.DISABLED) {
                              backend = NanoGeoClientConfig.ArmorModeOverlayBackend.TEXTURE_FALLBACK;
                           }
                           if (backend == NanoGeoClientConfig.ArmorModeOverlayBackend.CUSTOM_SHADER) {
                              NanoGeoArmorModeXShimmerShader.prepare(overlayState, true);
                              VertexConsumer overlayConsumer = bufferSource.getBuffer(
                                 NanoGeoArmorModeRenderTypes.armorModeOverlayCustomShader(overlayState.texture())
                              );
                              renderer.renderToBuffer(
                                 poseStack,
                                 overlayConsumer,
                                 overlayState.overlayPackedLight() >= 0 ? overlayState.overlayPackedLight() : event.getPackedLight(),
                                 OverlayTexture.NO_OVERLAY,
                                 overlayState.red(),
                                 overlayState.green(),
                                 overlayState.blue(),
                                 1.0F
                              );
                           } else if (backend == NanoGeoClientConfig.ArmorModeOverlayBackend.TEXTURE_FALLBACK) {
                              VertexConsumer overlayConsumer = bufferSource.getBuffer(
                                 NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibility(overlayState.texture())
                              );
                              renderer.renderToBuffer(
                                 poseStack,
                                 overlayConsumer,
                                 overlayState.overlayPackedLight() >= 0 ? overlayState.overlayPackedLight() : event.getPackedLight(),
                                 OverlayTexture.NO_OVERLAY,
                                 overlayState.red(),
                                 overlayState.green(),
                                 overlayState.blue(),
                                 overlayState.alpha() * Math.max(overlayState.transitionProgress(), 0.0F)
                              );
                           } else if (backend == NanoGeoClientConfig.ArmorModeOverlayBackend.DYNAMIC_TEXTURE_COMPAT) {
                              ResourceLocation baseTexture = NanoGeoArmorModeDynamicTextureCompatRenderer.prepareAndGetBaseFlowTexture(
                                 player.getUUID(), overlayState
                              );
                              NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet pulseTextures = NanoGeoArmorModeDynamicTextureCompatRenderer.prepareAndGetPulseTextures(
                                 player.getUUID(), overlayState
                              );
                              if (baseTexture != null) {
                                 VertexConsumer baseConsumer = bufferSource.getBuffer(NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibility(baseTexture));
                                 int basePackedLight = overlayState.overlayPackedLight() >= 0 ? overlayState.overlayPackedLight() : event.getPackedLight();
                                 renderer.renderToBuffer(
                                    poseStack,
                                    baseConsumer,
                                    basePackedLight,
                                    OverlayTexture.NO_OVERLAY,
                                    overlayState.red(),
                                    overlayState.green(),
                                    overlayState.blue(),
                                    overlayState.alpha()
                                 );
                              }

                              float configuredShell = NanoGeoClientConfig.armorModeDynamicTextureCompatShellEnabled()
                                 ? NanoGeoClientConfig.armorModeDynamicTextureCompatShellScale()
                                 : 0.0F;
                              float shellScale = 1.0F + Math.max(0.006F, configuredShell);
                              boolean smoothPulsePreset = NanoGeoClientConfig.armorModeCompatPulsePreset()
                                 == NanoGeoClientConfig.ArmorModeCompatPulsePreset.LEGACY_SMOOTH;
                              if (pulseTextures.glowTexture() != null) {
                                 VertexConsumer pulseConsumer = bufferSource.getBuffer(
                                    smoothPulsePreset
                                       ? NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityPulseUnshadedSmooth(pulseTextures.glowTexture())
                                       : NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityPulseUnshaded(pulseTextures.glowTexture())
                                 );
                                 int pulsePackedLight = 15728880;
                                 int glowRgb = NanoGeoClientConfig.armorModePulseGlowColorRgb();
                                 poseStack.pushPose();
                                 poseStack.scale(shellScale, shellScale, shellScale);
                                 renderer.renderToBuffer(
                                    poseStack,
                                    pulseConsumer,
                                    pulsePackedLight,
                                    OverlayTexture.NO_OVERLAY,
                                    colorChannel(glowRgb, 16),
                                    colorChannel(glowRgb, 8),
                                    colorChannel(glowRgb, 0),
                                    1.0F
                                 );
                                 poseStack.popPose();
                              }

                              if (pulseTextures.coreTexture() != null) {
                                 VertexConsumer pulseConsumer = bufferSource.getBuffer(
                                    smoothPulsePreset
                                       ? NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityPulseUnshadedSmooth(pulseTextures.coreTexture())
                                       : NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityPulseUnshaded(pulseTextures.coreTexture())
                                 );
                                 int pulsePackedLight = 15728880;
                                 int coreRgb = pulseTextures.glowTexture() == null ? 16777215 : NanoGeoClientConfig.armorModePulseCoreColorRgb();
                                 poseStack.pushPose();
                                 poseStack.scale(shellScale, shellScale, shellScale);
                                 renderer.renderToBuffer(
                                    poseStack,
                                    pulseConsumer,
                                    pulsePackedLight,
                                    OverlayTexture.NO_OVERLAY,
                                    colorChannel(coreRgb, 16),
                                    colorChannel(coreRgb, 8),
                                    colorChannel(coreRgb, 0),
                                    1.0F
                                 );
                                 poseStack.popPose();
                              }
                           } else if (backend != NanoGeoClientConfig.ArmorModeOverlayBackend.DISABLED) {
                              VertexConsumer overlayConsumer = bufferSource.getBuffer(
                                 NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibility(overlayState.texture())
                              );
                              renderer.renderToBuffer(
                                 poseStack,
                                 overlayConsumer,
                                 overlayState.overlayPackedLight() >= 0 ? overlayState.overlayPackedLight() : event.getPackedLight(),
                                 OverlayTexture.NO_OVERLAY,
                                 overlayState.red(),
                                 overlayState.green(),
                                 overlayState.blue(),
                                 overlayState.alpha() * Math.max(overlayState.transitionProgress(), 0.0F)
                              );
                           }
                        }

                        if (NanoGeoClientConfig.nanoGeoFpvArmsDebug() && !loggedModelData) {
                           loggedModelData = true;
                           LOGGER.info(
                              "[CrysisMod][NanoGeo][FPV Arms] renderer=NANOGEO_FPV arm={} model={} texture={}",
                              new Object[]{event.getArm(), presentation.armorModel(), texture}
                           );
                        }

                        return true;
                     }

                     var32 = false;
                  } catch (Throwable var30) {
                     ThermalFirstPersonArmDeferredBuffer.discardDeferred();
                     ThermalFirstPersonArmComposite.beginFrame();
                     if (!warnedFallback) {
                        warnedFallback = true;
                        LOGGER.warn("[CrysisMod][NanoGeo][FPV Arms] Failed to render first-person NanoGeo arm, using vanilla fallback", var30);
                     }

                     return false;
                  } finally {
                     selectedArmSnapshot.restore(selectedArmPart);
                     resetTransientRenderState();
                     poseStack.popPose();
                     NanovisionPerformanceProfiler.endCpu(NanovisionPerformanceProfiler.Section.FIRST_PERSON_ARM_TOTAL, profileCpuStarted);
                  }

                  return var32;
               }
            }
         }
      }
   }

   private static void renderThermalPulseOnly(
      NanoGeoArmorRenderer renderer,
      AbstractClientPlayer player,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      NanosuitArmorModeOverlayState.OverlayRenderState overlayState
   ) {
      if (NanoGeoShaderpackCompatibility.resolveArmorModeOverlayBackend() != NanoGeoClientConfig.ArmorModeOverlayBackend.DISABLED) {
         NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet pulseTextures = NanoGeoArmorModeDynamicTextureCompatRenderer.prepareAndGetPulseTextures(
            player.getUUID(), overlayState
         );
         if (pulseTextures.glowTexture() != null || pulseTextures.coreTexture() != null) {
            float configuredShell = NanoGeoClientConfig.armorModeDynamicTextureCompatShellEnabled()
               ? NanoGeoClientConfig.armorModeDynamicTextureCompatShellScale()
               : 0.0F;
            float shellScale = 1.0F + Math.max(0.006F, configuredShell);
            boolean smoothPulsePreset = NanoGeoClientConfig.armorModeCompatPulsePreset() == NanoGeoClientConfig.ArmorModeCompatPulsePreset.LEGACY_SMOOTH;
            if (pulseTextures.glowTexture() != null) {
               VertexConsumer pulseConsumer = bufferSource.getBuffer(
                  smoothPulsePreset
                     ? NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityPulseUnshadedSmooth(pulseTextures.glowTexture())
                     : NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityPulseUnshaded(pulseTextures.glowTexture())
               );
               int glowRgb = NanoGeoClientConfig.armorModePulseGlowColorRgb();
               poseStack.pushPose();
               poseStack.scale(shellScale, shellScale, shellScale);
               renderer.renderToBuffer(
                  poseStack,
                  pulseConsumer,
                  15728880,
                  OverlayTexture.NO_OVERLAY,
                  colorChannel(glowRgb, 16),
                  colorChannel(glowRgb, 8),
                  colorChannel(glowRgb, 0),
                  1.0F
               );
               poseStack.popPose();
            }

            if (pulseTextures.coreTexture() != null) {
               VertexConsumer pulseConsumer = bufferSource.getBuffer(
                  smoothPulsePreset
                     ? NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityPulseUnshadedSmooth(pulseTextures.coreTexture())
                     : NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityPulseUnshaded(pulseTextures.coreTexture())
               );
               int coreRgb = pulseTextures.glowTexture() == null ? 16777215 : NanoGeoClientConfig.armorModePulseCoreColorRgb();
               poseStack.pushPose();
               poseStack.scale(shellScale, shellScale, shellScale);
               renderer.renderToBuffer(
                  poseStack,
                  pulseConsumer,
                  15728880,
                  OverlayTexture.NO_OVERLAY,
                  colorChannel(coreRgb, 16),
                  colorChannel(coreRgb, 8),
                  colorChannel(coreRgb, 0),
                  1.0F
               );
               poseStack.popPose();
            }
         }
      }
   }

   public static void resetTransientRenderState() {
      NanoGeoArmorRenderer renderer = armorRenderer;
      if (renderer != null) {
         renderer.endArmorModeOverlayWave();
         renderer.endFirstPersonArmOnly();
      }
   }

   private static NanoGeoArmorRenderer getArmorRenderer() {
      if (armorRenderer != null) {
         return armorRenderer;
      } else {
         Minecraft minecraft = Minecraft.getInstance();
         if (minecraft != null && minecraft.getEntityModels() != null) {
            armorRenderer = new NanoGeoArmorRenderer();
            return armorRenderer;
         } else {
            return null;
         }
      }
   }

   private static PlayerModel<AbstractClientPlayer> getPlayerModel(AbstractClientPlayer player) {
      return Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player) instanceof PlayerRenderer playerRenderer
         ? (PlayerModel)playerRenderer.getModel()
         : null;
   }

   private static float colorChannel(int rgb, int shift) {
      return (rgb >> shift & 0xFF) / 255.0F;
   }

   private static ResourceLocation resolveArmorTexture(
      ItemStack chestStack, AbstractClientPlayer player, HumanoidArm arm, ResourceLocation fallbackTexture
   ) {
      if (chestStack.getItem() instanceof ExperimentalNanoGeoArmorItem armorItem) {
         try {
            ResourceLocation resolved = armorItem.getArmorTexture(
               chestStack, player, EquipmentSlot.CHEST, armorItem.getMaterial().value().layers().getFirst(), false
            );
            if (resolved != null) {
               if (NanoGeoClientConfig.nanoGeoFpvArmsDebug()) {
                  LOGGER.info("[CrysisMod][NanoGeo][FPV Arms] arm={} using armor texture {}", arm, resolved);
               }
               return resolved;
            }
         } catch (RuntimeException var6) {
         }
      }

      return fallbackTexture;
   }

   private static void applyCommonTransform(PoseStack poseStack) {
      poseStack.translate(0.0F, 0.0F, 0.0F);
      poseStack.mulPose(Axis.XP.rotationDegrees(0.0F));
      poseStack.mulPose(Axis.YP.rotationDegrees(0.0F));
      poseStack.mulPose(Axis.ZP.rotationDegrees(0.0F));
      poseStack.scale(1.0F, 1.0F, 1.0F);
   }

   private static void applySideTransform(PoseStack poseStack, HumanoidArm arm) {
      if (arm == HumanoidArm.RIGHT) {
         poseStack.translate(0.0F, 0.0F, 0.0F);
         poseStack.mulPose(Axis.XP.rotationDegrees(0.0F));
         poseStack.mulPose(Axis.YP.rotationDegrees(0.0F));
         poseStack.mulPose(Axis.ZP.rotationDegrees(0.0F));
         poseStack.scale(1.0F, 1.0F, 1.0F);
      } else {
         poseStack.translate(0.0F, 0.0F, 0.0F);
         poseStack.mulPose(Axis.XP.rotationDegrees(0.0F));
         poseStack.mulPose(Axis.YP.rotationDegrees(0.0F));
         poseStack.mulPose(Axis.ZP.rotationDegrees(0.0F));
         poseStack.scale(1.0F, 1.0F, 1.0F);
      }
   }

   private static void stabilizeArmPartForFirstPerson(ModelPart armPart, HumanoidArm arm, AbstractClientPlayer player) {
      boolean slimModel = player.getSkin().model() == PlayerSkin.Model.SLIM;
      armPart.visible = true;
      armPart.xRot = 0.0F;
      armPart.yRot = 0.0F;
      armPart.zRot = 0.0F;
      armPart.z = 0.0F;
      armPart.y = slimModel ? 2.5F : 2.0F;
      armPart.x = arm == HumanoidArm.RIGHT ? -5.0F : 5.0F;
      armPart.xScale = 1.0F;
      armPart.yScale = 1.0F;
      armPart.zScale = 1.0F;
   }

   private record ModelPartStateSnapshot(
      float x, float y, float z, float xRot, float yRot, float zRot, float xScale, float yScale, float zScale, boolean visible
   ) {
      private static NanoGeoFirstPersonArmRenderer.ModelPartStateSnapshot capture(ModelPart modelPart) {
         return new NanoGeoFirstPersonArmRenderer.ModelPartStateSnapshot(
            modelPart.x,
            modelPart.y,
            modelPart.z,
            modelPart.xRot,
            modelPart.yRot,
            modelPart.zRot,
            modelPart.xScale,
            modelPart.yScale,
            modelPart.zScale,
            modelPart.visible
         );
      }

      private void restore(ModelPart modelPart) {
         modelPart.x = this.x;
         modelPart.y = this.y;
         modelPart.z = this.z;
         modelPart.xRot = this.xRot;
         modelPart.yRot = this.yRot;
         modelPart.zRot = this.zRot;
         modelPart.xScale = this.xScale;
         modelPart.yScale = this.yScale;
         modelPart.zScale = this.zScale;
         modelPart.visible = this.visible;
      }
   }
}
