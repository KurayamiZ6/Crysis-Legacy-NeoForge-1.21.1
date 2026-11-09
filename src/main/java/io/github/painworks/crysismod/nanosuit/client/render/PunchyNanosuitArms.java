package io.github.painworks.crysismod.nanosuit.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoBakedModelCache;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoClientConfig;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorVariant;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import punchy.api.PunchyApi;

/** Submits NanoGeo's arm at Punchy's animated arm pivot. */
@EventBusSubscriber(modid = "crysismod", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class PunchyNanosuitArms {
   private static final ResourceLocation ARMOR_MODEL = ResourceLocation.fromNamespaceAndPath("crysismod", "nanogeo/nanosuit_armor.json");

   private PunchyNanosuitArms() {
   }

   @SubscribeEvent
   public static void onClientSetup(FMLClientSetupEvent event) {
      if (ModList.get().isLoaded("punchy")) {
         event.enqueueWork(() -> {
            try {
               Bridge.register();
            } catch (LinkageError | RuntimeException exception) {
               LogUtils.getLogger().warn("Punchy's arm API is unavailable; using its standard arms", exception);
            }
         });
      }
   }

   // Keep the optional API's types out of the class registered with NeoForge.
   private static final class Bridge {
      private static boolean failed;

      private static void register() {
         PunchyApi.drawOnArm("crysismod:nanosuit", Bridge::draw);
         PunchyApi.hideLayers("crysismod:nanosuit", Bridge::hiddenLayers);
      }

      private static int hiddenLayers(Object player, boolean left) {
         if (!(player instanceof AbstractClientPlayer clientPlayer) || !eligible(clientPlayer, left)) {
            return 0;
         }
         return clientPlayer.isInvisible() && !NanosuitCloakRender.keepsArmGeometry(clientPlayer) ? PunchyApi.LAYER_ARM
            : PunchyApi.LAYER_SKIN | PunchyApi.LAYER_SLEEVE | PunchyApi.LAYER_ARMOR;
      }

      private static boolean eligible(AbstractClientPlayer player, boolean left) {
         return !failed && NanosuitClientConfig.nanosuitFirstPersonArmsEnabled()
            && NanosuitClientConfig.nanosuitFirstPersonArmsUseNanoGeo()
            && NanoGeoClientConfig.nanoGeoFpvArmsEnabled()
            && (NanoGeoClientConfig.nanoGeoFpvArmsRenderBothHands() || player.getMainArm() == (left ? HumanoidArm.LEFT : HumanoidArm.RIGHT))
            && NanosuitArmorUtil.hasFullSet(player)
            && NanosuitArmorUtil.getVariant(player.getItemBySlot(EquipmentSlot.CHEST)) == NanosuitArmorVariant.NANOGEO
            && NanoGeoBakedModelCache.INSTANCE.getBakedModelOrNull(ARMOR_MODEL) != null;
      }

      private static void draw(PunchyApi.ArmDraw draw) {
         if (draw.throughMinecraft() || !(draw.player() instanceof AbstractClientPlayer player)
            || player.isInvisible() && !NanosuitCloakRender.keepsArmGeometry(player) || !eligible(player, draw.leftArm())
            || !(draw.armPart() instanceof ModelPart armPart)
            || !(draw.poseStack() instanceof PoseStack poseStack)
            || !(draw.sink() instanceof MultiBufferSource buffers)) {
            return;
         }

         HumanoidArm arm = draw.leftArm() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
         float pivotY = player.getSkin().model() == PlayerSkin.Model.SLIM ? 2.5F : 2.0F;
         poseStack.pushPose();
         try {
            // Punchy uses an explicit ModelPart matrix for its animations. Remove the
            // vanilla pivot already applied by the standard Nanosuit arm renderer.
            armPart.translateAndRotate(poseStack);
            poseStack.translate((draw.leftArm() ? -5.0F : 5.0F) / 16.0F, -pivotY / 16.0F, 0.0F);
            failed = !NanosuitFirstPersonArmRenderDispatcher.renderArm(new RenderArmEvent(poseStack, buffers, draw.light(), player, arm));
         } catch (RuntimeException | LinkageError exception) {
            failed = true;
            LogUtils.getLogger().warn("Nanosuit arm could not be drawn through Punchy; restoring its standard arms", exception);
         } finally {
            poseStack.popPose();
         }
      }
   }
}
