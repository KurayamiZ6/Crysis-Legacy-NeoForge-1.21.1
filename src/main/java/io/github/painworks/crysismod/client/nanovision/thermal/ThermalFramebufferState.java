package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

final class ThermalFramebufferState {
   private final int readFramebuffer;
   private final int drawFramebuffer;
   private final int viewportX;
   private final int viewportY;
   private final int viewportWidth;
   private final int viewportHeight;
   private final boolean framebufferSrgb;

   private ThermalFramebufferState(
      int readFramebuffer, int drawFramebuffer, int viewportX, int viewportY, int viewportWidth, int viewportHeight, boolean framebufferSrgb
   ) {
      this.readFramebuffer = readFramebuffer;
      this.drawFramebuffer = drawFramebuffer;
      this.viewportX = viewportX;
      this.viewportY = viewportY;
      this.viewportWidth = viewportWidth;
      this.viewportHeight = viewportHeight;
      this.framebufferSrgb = framebufferSrgb;
   }

   static ThermalFramebufferState capture() {
      RenderSystem.assertOnRenderThread();
      int[] viewport = new int[4];
      GL11.glGetIntegerv(2978, viewport);
      return new ThermalFramebufferState(
         GL11.glGetInteger(36010), GL11.glGetInteger(36006), viewport[0], viewport[1], viewport[2], viewport[3], GL11.glIsEnabled(36281)
      );
   }

   void restore() {
      RenderSystem.assertOnRenderThread();
      GL30.glBindFramebuffer(36008, this.readFramebuffer);
      GL30.glBindFramebuffer(36009, this.drawFramebuffer);
      RenderSystem.viewport(this.viewportX, this.viewportY, this.viewportWidth, this.viewportHeight);
      if (this.framebufferSrgb) {
         GL11.glEnable(36281);
      } else {
         GL11.glDisable(36281);
      }
   }

   boolean restoreAndValidateDrawTarget() {
      if (this.viewportWidth > 0 && this.viewportHeight > 0) {
         this.restore();
         return GL30.glCheckFramebufferStatus(36009) == 36053;
      } else {
         return false;
      }
   }

   int drawFramebuffer() {
      return this.drawFramebuffer;
   }

   int viewportX() {
      return this.viewportX;
   }

   int viewportY() {
      return this.viewportY;
   }

   int viewportWidth() {
      return this.viewportWidth;
   }

   int viewportHeight() {
      return this.viewportHeight;
   }
}
