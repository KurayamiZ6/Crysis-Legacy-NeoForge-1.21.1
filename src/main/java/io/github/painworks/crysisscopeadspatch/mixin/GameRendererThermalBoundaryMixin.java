package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonArmComposite;
import io.github.painworks.crysismod.nanosuit.client.ClassicTaczData;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalFirstPersonItemCapture;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 1200)
public abstract class GameRendererThermalBoundaryMixin {
   private static final Logger CRYSISSCOPEFIX$LOGGER = LogUtils.getLogger();
   private static boolean crysisscopefix$logged;

   @Inject(method = "renderLevel", at = @At("HEAD"), require = 1)
   private void crysisscopefix$prepareIrisGunStencil(DeltaTracker deltaTracker, CallbackInfo ci) {
      Minecraft minecraft = Minecraft.getInstance();
      var target = minecraft.getMainRenderTarget();
      var player = minecraft.player;
      if (target == null || target.isStencilEnabled() || player == null
         || !NanoGeoShaderpackCompatibility.isIrisShaderPackActive()
         || !ClassicTaczData.isGun(player.getMainHandItem())) {
         return;
      }
      // TaCZ can request stencil while rendering the gun. Allocate it before Iris observes this frame's depth attachment.
      target.enableStencil();
      target.bindWrite(true);
   }

   @Inject(
      method = "render",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V", ordinal = 0, shift = Shift.BEFORE, remap = false),
      require = 1
   )
   private void crysisscopefix$flushThermalBeforeGuiSetup(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
      if (ThermalFirstPersonArmComposite.hasPendingShaderpackComposite() || ThermalFirstPersonItemCapture.hasPending()) {
         boolean scissor = GL11.glIsEnabled(3089);
         boolean depthMask = GL11.glGetBoolean(2930);

         try {
            GL11.glDisable(3089);
            GL11.glDepthMask(true);
            RenderSystem.clearDepth(1.0);
            RenderSystem.clear(256, Minecraft.ON_OSX);
         } finally {
            GL11.glDepthMask(depthMask);
            if (scissor) {
               GL11.glEnable(3089);
            } else {
               GL11.glDisable(3089);
            }
         }

         boolean armRendered = ThermalFirstPersonArmComposite.renderPendingShaderpackComposite();
         boolean itemRendered = ThermalFirstPersonItemCapture.flushDeferred();
         if ((armRendered || itemRendered) && !crysisscopefix$logged) {
            crysisscopefix$logged = true;
            CRYSISSCOPEFIX$LOGGER.info("[CrysisScopeFix] completed arm and held-item thermal replay on isolated pre-GUI depth");
         }
      }
   }
}
