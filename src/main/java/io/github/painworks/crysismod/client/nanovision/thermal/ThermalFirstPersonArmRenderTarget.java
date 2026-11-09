package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.OutputStateShard;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

public final class ThermalFirstPersonArmRenderTarget extends RenderStateShard {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static RenderTarget target;
   private static int width;
   private static int height;
   private static ThermalFramebufferState armOwnerState;
   private static ThermalFramebufferState outputPreviousState;
   private static boolean loggedForeignOwner;
   public static final OutputStateShard OUTPUT = new OutputStateShard("crysismod_nanovision_fpv_arm_target", () -> {
      outputPreviousState = ThermalFramebufferState.capture();
      RenderTarget current = getOrCreate();
      if (current != null) {
         current.bindWrite(true);
      }

      GL11.glDisable(36281);
   }, () -> {
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
      if (outputPreviousState != null) {
         outputPreviousState.restore();
         outputPreviousState = null;
      }
   });

   private ThermalFirstPersonArmRenderTarget() {
      super("nanovision_fpv_arm_target", () -> {}, () -> {});
   }

   public static boolean prepare() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.getMainRenderTarget() == null) {
         return false;
      } else {
         ThermalFramebufferState callerState = ThermalFramebufferState.capture();

         boolean var3;
         try {
            RenderTarget current = getOrCreate();
            if (current != null) {
               armOwnerState = callerState;
               current.bindWrite(true);
               GL11.glDisable(36281);
               RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
               RenderSystem.clear(16640, Minecraft.ON_OSX);
               return true;
            }

            armOwnerState = null;
            var3 = false;
         } finally {
            callerState.restore();
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
         }

         return var3;
      }
   }

   static ThermalFramebufferState armOwnerState() {
      return armOwnerState;
   }

   static void logOwnerOnce(RenderTarget mainTarget) {
      ThermalFramebufferState owner = armOwnerState;
      if (owner != null && mainTarget != null && !loggedForeignOwner && owner.drawFramebuffer() != mainTarget.frameBufferId) {
         loggedForeignOwner = true;
         LOGGER.info(
            "[CrysisMod][Nanovision] first-person thermal arm preserving caller framebuffer draw={} instead of vanilla main={} viewport={}x{}",
            new Object[]{owner.drawFramebuffer(), mainTarget.frameBufferId, owner.viewportWidth(), owner.viewportHeight()}
         );
      }
   }

   public static RenderTarget getOrCreate() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.getWindow() != null && minecraft.getMainRenderTarget() != null) {
         int targetWidth = minecraft.getMainRenderTarget().width;
         int targetHeight = minecraft.getMainRenderTarget().height;
         if (target != null && targetWidth == width && targetHeight == height) {
            return target;
         } else {
            close();
            target = new TextureTarget(targetWidth, targetHeight, true, Minecraft.ON_OSX);
            target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            target.setFilterMode(9729);
            width = targetWidth;
            height = targetHeight;
            return target;
         }
      } else {
         return null;
      }
   }

   public static void close() {
      if (target != null) {
         target.destroyBuffers();
         target = null;
      }

      width = 0;
      height = 0;
      armOwnerState = null;
      outputPreviousState = null;
      loggedForeignOwner = false;
   }
}
