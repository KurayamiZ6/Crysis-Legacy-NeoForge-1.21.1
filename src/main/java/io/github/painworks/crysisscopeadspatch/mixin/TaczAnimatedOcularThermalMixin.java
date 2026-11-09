package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture;
import io.github.painworks.crysisscopefix.render.ScopeDeferredCapture;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.model.BedrockAttachmentModel", remap = false, priority = 1250)
public abstract class TaczAnimatedOcularThermalMixin {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static boolean crysisscopeadspatch$logged;
   private static final String TEMP_PART = "renderTempPart(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILjava/util/List;Lnet/minecraft/client/renderer/MultiBufferSource;)V";
   private static final String OCULAR_AND_DIVISION = "renderOcularAndDivision(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIZLnet/minecraft/client/renderer/MultiBufferSource;)V";

   @Invoker(value = "renderTempPart", remap = false)
   protected abstract void crysisscopeadspatch$invokeRenderTempPart(PoseStack var1, ItemDisplayContext var2, RenderType var3, int var4, int var5, List<?> var6, MultiBufferSource source);

   @Redirect(
      method = "renderOcularAndDivision(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IIZLnet/minecraft/client/renderer/MultiBufferSource;)V",
      at = @At(
         value = "INVOKE",
         target = "Lcom/tacz/guns/client/model/BedrockAttachmentModel;renderTempPart(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;IILjava/util/List;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
         ordinal = 1
      ),
      require = 1,
      remap = false
   )
   private void crysisscopeadspatch$captureVisibleHipFireOcular(
      @Coerce Object ignoredReceiver, PoseStack poseStack, ItemDisplayContext context, RenderType physicalType, int light, int overlay, List<?> path, MultiBufferSource source
   ) {
      this.crysisscopeadspatch$invokeRenderTempPart(poseStack, context, physicalType, light, overlay, path, source);
      if (context.firstPerson() && ThermalFirstPersonItemCapture.shouldPreserveArmMasks()) {
         ThermalFirstPersonItemCapture.pushScopeBodyStencilCapture();

         try {
            RenderType thermalType = ThermalFirstPersonItemCapture.prepareTaczScopeBodyThermalPass(physicalType);
            if (thermalType != null) {
               if (NanoGeoShaderpackCompatibility.isShaderPackActive()) {
                  ScopeDeferredCapture.captureThermal(poseStack, context, thermalType, light, overlay, path);
               } else {
                  this.crysisscopeadspatch$invokeRenderTempPart(poseStack, context, thermalType, light, overlay, path, source);
               }

               if (!crysisscopeadspatch$logged) {
                  crysisscopeadspatch$logged = true;
                  LOGGER.info("[CrysisScopeFix] captured TACZ's animated ocular as thermal geometry with the active render backend; divisions remain physical");
               }
            }
         } finally {
            ThermalFirstPersonItemCapture.popScopeBodyStencilCapture();
         }
      }
   }
}
