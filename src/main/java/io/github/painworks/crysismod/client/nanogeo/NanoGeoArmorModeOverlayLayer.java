package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.painworks.crysismod.nanosuit.NanosuitPresentation;
import io.github.painworks.crysismod.nanosuit.client.render.NanosuitArmorModeOverlayState;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class NanoGeoArmorModeOverlayLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   private static final float COMPAT_PULSE_MIN_SHELL_SCALE = 0.006F;
   private static NanoGeoArmorRenderer overlayRenderer;

   public NanoGeoArmorModeOverlayLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
      super(parent);
   }

   public void render(
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      AbstractClientPlayer player,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      if (NanoGeoClientConfig.armorModeOverlayRenderInThirdPerson()) {
         if (NanosuitArmorUtil.hasFullSet(player)) {
            if (NanosuitPresentation.forPiece(player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST) != null) {
               NanosuitArmorModeOverlayState.OverlayRenderState overlayState = NanosuitArmorModeOverlayState.resolveForThirdPerson(player);
               if (overlayState.enabled()) {
                  NanoGeoArmorRenderer renderer = getOverlayRenderer();
                  if (renderer != null) {
                     HumanoidModel<?> playerModel = (HumanoidModel<?>)this.getParentModel();
                     NanoGeoClientConfig.ArmorModeOverlayBackend backend = NanoGeoShaderpackCompatibility.resolveArmorModeOverlayBackend();
                     switch (backend) {
                        case DISABLED:
                           return;
                        case CUSTOM_SHADER: {
                           NanoGeoArmorModeXShimmerShader.prepare(overlayState, false);
                           VertexConsumer overlayConsumer = bufferSource.getBuffer(
                              NanoGeoArmorModeRenderTypes.armorModeOverlayCustomShader(overlayState.texture())
                           );
                           this.renderOverlayFullModel(
                              renderer,
                              player,
                              playerModel,
                              poseStack,
                              overlayConsumer,
                              overlayState.overlayPackedLight() >= 0 ? overlayState.overlayPackedLight() : packedLight,
                              overlayState.classicMode() ? 1.0F : overlayState.red(),
                              overlayState.classicMode() ? 1.0F : overlayState.green(),
                              overlayState.classicMode() ? 1.0F : overlayState.blue(),
                              1.0F
                           );
                           break;
                        }
                        case TEXTURE_FALLBACK: {
                           VertexConsumer overlayConsumer = bufferSource.getBuffer(
                              NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibility(overlayState.texture())
                           );
                           this.renderOverlayFullModel(
                              renderer,
                              player,
                              playerModel,
                              poseStack,
                              overlayConsumer,
                              overlayState.overlayPackedLight() >= 0 ? overlayState.overlayPackedLight() : packedLight,
                              overlayState.red(),
                              overlayState.green(),
                              overlayState.blue(),
                              effectiveTextureFallbackAlpha(overlayState)
                           );
                           break;
                        }
                        case DYNAMIC_TEXTURE_COMPAT:
                           if (isCompositeThirdPersonCompatPreset()) {
                              ResourceLocation compositeTexture = NanoGeoArmorModeDynamicTextureCompatRenderer.prepareAndGetCompositeTexture(
                                 player.getUUID(), overlayState
                              );
                              if (compositeTexture == null) {
                                 return;
                              }

                              VertexConsumer compositeConsumer = bufferSource.getBuffer(
                                 NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityCulled(compositeTexture)
                              );
                              int compositePackedLight = overlayState.overlayPackedLight() >= 0 ? overlayState.overlayPackedLight() : packedLight;
                              this.renderOverlayFullModel(
                                 renderer,
                                 player,
                                 playerModel,
                                 poseStack,
                                 compositeConsumer,
                                 compositePackedLight,
                                 overlayState.red(),
                                 overlayState.green(),
                                 overlayState.blue(),
                                 effectiveDynamicCompatAlpha(overlayState)
                              );
                              return;
                           }

                           boolean replaceBaseArmor = NanoGeoArmorModeBaseCompositeCompat.shouldReplaceBaseArmor(overlayState);
                           ResourceLocation baseTexture = replaceBaseArmor
                              ? NanoGeoArmorModeDynamicTextureCompatRenderer.prepareAndGetBaseCompositeTexture(player.getUUID(), overlayState)
                              : NanoGeoArmorModeDynamicTextureCompatRenderer.prepareAndGetBaseFlowTexture(player.getUUID(), overlayState);
                           NanoGeoArmorModeDynamicTextureCompatRenderer.PulseTextureSet pulseTextures = NanoGeoArmorModeDynamicTextureCompatRenderer.prepareAndGetPulseTextures(
                              player.getUUID(), overlayState
                           );
                           if (baseTexture == null && pulseTextures.coreTexture() == null && pulseTextures.glowTexture() == null) {
                              return;
                           }

                           if (baseTexture != null) {
                              if (replaceBaseArmor) {
                                 VertexConsumer baseConsumer = bufferSource.getBuffer(NanoGeoArmorModeRenderTypes.armorModeBaseComposite(baseTexture));
                                 this.renderOverlayFullModel(renderer, player, playerModel, poseStack, baseConsumer, 15728880, 1.0F, 1.0F, 1.0F, 1.0F);
                              } else {
                                 boolean culledBaseCompat = isCulledBaseThirdPersonCompatPreset();
                                 VertexConsumer baseConsumer = bufferSource.getBuffer(
                                    isUnshadedArmorLayerCompatPreset()
                                       ? NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityUnshaded(baseTexture)
                                       : (
                                          culledBaseCompat
                                             ? NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityCulled(baseTexture)
                                             : NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibility(baseTexture)
                                       )
                                 );
                                 int basePackedLight = isUnshadedArmorLayerCompatPreset()
                                    ? 15728880
                                    : (overlayState.overlayPackedLight() >= 0 ? overlayState.overlayPackedLight() : packedLight);
                                 this.renderOverlayFullModel(
                                    renderer,
                                    player,
                                    playerModel,
                                    poseStack,
                                    baseConsumer,
                                    basePackedLight,
                                    overlayState.red(),
                                    overlayState.green(),
                                    overlayState.blue(),
                                    effectiveDynamicCompatAlpha(overlayState)
                                 );
                              }
                           }

                           float configuredShell = NanoGeoClientConfig.armorModeDynamicTextureCompatShellEnabled()
                              ? NanoGeoClientConfig.armorModeDynamicTextureCompatShellScale()
                              : 0.0F;
                           float shellScale = 1.0F + Math.max(0.006F, configuredShell);
                           boolean smoothPulsePreset = NanoGeoClientConfig.armorModeCompatPulsePreset()
                              == NanoGeoClientConfig.ArmorModeCompatPulsePreset.LEGACY_SMOOTH;
                           boolean culledPulseCompat = isCulledPulseThirdPersonCompatPreset();
                           if (pulseTextures.glowTexture() != null) {
                              VertexConsumer pulseConsumer = bufferSource.getBuffer(
                                 resolveThirdPersonPulseRenderType(pulseTextures.glowTexture(), smoothPulsePreset, culledPulseCompat)
                              );
                              int pulsePackedLight = 15728880;
                              int glowRgb = overlayState.pulseGlowColorRgb();
                              poseStack.pushPose();
                              poseStack.scale(shellScale, shellScale, shellScale);
                              this.renderOverlayFullModel(
                                 renderer,
                                 player,
                                 playerModel,
                                 poseStack,
                                 pulseConsumer,
                                 pulsePackedLight,
                                 colorChannel(glowRgb, 16),
                                 colorChannel(glowRgb, 8),
                                 colorChannel(glowRgb, 0),
                                 1.0F
                              );
                              poseStack.popPose();
                           }

                           if (pulseTextures.coreTexture() != null) {
                              VertexConsumer pulseConsumer = bufferSource.getBuffer(
                                 resolveThirdPersonPulseRenderType(pulseTextures.coreTexture(), smoothPulsePreset, culledPulseCompat)
                              );
                              int pulsePackedLight = 15728880;
                              int coreRgb = overlayState.pulseCoreColorRgb(pulseTextures.glowTexture() != null);
                              poseStack.pushPose();
                              poseStack.scale(shellScale, shellScale, shellScale);
                              this.renderOverlayFullModel(
                                 renderer,
                                 player,
                                 playerModel,
                                 poseStack,
                                 pulseConsumer,
                                 pulsePackedLight,
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
            }
         }
      }
   }

   private void renderOverlayFullModel(
      NanoGeoArmorRenderer renderer,
      AbstractClientPlayer player,
      HumanoidModel<?> playerModel,
      PoseStack poseStack,
      VertexConsumer overlayConsumer,
      int renderPackedLight,
      float red,
      float green,
      float blue,
      float overlayAlpha
   ) {
      ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
      if (NanosuitPresentation.forPiece(chestStack, EquipmentSlot.CHEST) != null) {
         renderer.endFirstPersonArmOnly();
         renderer.beginFullModelRenderOverride();
         renderer.prepForRender(player, chestStack, EquipmentSlot.CHEST, playerModel);
         renderer.renderToBuffer(poseStack, overlayConsumer, renderPackedLight, 0, red, green, blue, overlayAlpha);
         renderer.endFullModelRenderOverride();
      }
   }

   private static float effectiveTextureFallbackAlpha(NanosuitArmorModeOverlayState.OverlayRenderState overlayState) {
      return overlayState.alpha() * Math.max(overlayState.transitionProgress(), 0.0F);
   }

   private static float effectiveDynamicCompatAlpha(NanosuitArmorModeOverlayState.OverlayRenderState overlayState) {
      return overlayState.alpha();
   }

   private static float colorChannel(int rgb, int shift) {
      return (rgb >> shift & 0xFF) / 255.0F;
   }

   private static boolean isCulledBaseThirdPersonCompatPreset() {
      NanoGeoClientConfig.ArmorModeCompatPerformancePreset preset = NanoGeoClientConfig.armorModeCompatPerformancePreset();
      return preset == NanoGeoClientConfig.ArmorModeCompatPerformancePreset.QUALITY_OPTIMIZED
         || preset == NanoGeoClientConfig.ArmorModeCompatPerformancePreset.THIRD_PERSON_FAST
         || preset == NanoGeoClientConfig.ArmorModeCompatPerformancePreset.COMPOSITE_FAST;
   }

   private static boolean isCulledPulseThirdPersonCompatPreset() {
      return NanoGeoClientConfig.armorModeCompatPerformancePreset() == NanoGeoClientConfig.ArmorModeCompatPerformancePreset.THIRD_PERSON_FAST;
   }

   private static boolean isCompositeThirdPersonCompatPreset() {
      return NanoGeoClientConfig.armorModeCompatPerformancePreset() == NanoGeoClientConfig.ArmorModeCompatPerformancePreset.COMPOSITE_FAST;
   }

   private static boolean isUnshadedArmorLayerCompatPreset() {
      return NanoGeoClientConfig.armorModeCompatPerformancePreset() == NanoGeoClientConfig.ArmorModeCompatPerformancePreset.UNSHADED_ARMOR_LAYER;
   }

   private static RenderType resolveThirdPersonPulseRenderType(ResourceLocation texture, boolean smoothPulsePreset, boolean fastThirdPersonCompat) {
      if (fastThirdPersonCompat) {
         return smoothPulsePreset
            ? NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityPulseUnshadedSmoothCulled(texture)
            : NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityPulseUnshadedCulled(texture);
      } else {
         return smoothPulsePreset
            ? NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityPulseUnshadedSmooth(texture)
            : NanoGeoArmorModeRenderTypes.armorModeOverlayCompatibilityPulseUnshaded(texture);
      }
   }

   private static NanoGeoArmorRenderer getOverlayRenderer() {
      if (overlayRenderer != null) {
         return overlayRenderer;
      } else {
         Minecraft minecraft = Minecraft.getInstance();
         if (minecraft != null && minecraft.getEntityModels() != null) {
            overlayRenderer = new NanoGeoArmorRenderer();
            return overlayRenderer;
         } else {
            return null;
         }
      }
   }
}
