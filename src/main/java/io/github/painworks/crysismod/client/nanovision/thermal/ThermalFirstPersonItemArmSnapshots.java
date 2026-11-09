package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.HumanoidArm;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class ThermalFirstPersonItemArmSnapshots {
   private static final Map<HumanoidArm, ThermalFirstPersonItemArmSnapshots.Snapshot> SNAPSHOTS = new EnumMap<>(HumanoidArm.class);

   private ThermalFirstPersonItemArmSnapshots() {
   }

   public static void beginFrame() {
      RenderSystem.assertOnRenderThread();

      for (ThermalFirstPersonItemArmSnapshots.Snapshot snapshot : SNAPSHOTS.values()) {
         snapshot.valid = false;
      }
   }

   public static void capture(HumanoidArm arm, RenderTarget source) {
      RenderSystem.assertOnRenderThread();
      if (arm != null && source != null && ThermalFirstPersonItemCapture.shouldPreserveArmMasks()) {
         ThermalFirstPersonItemArmSnapshots.Snapshot snapshot = SNAPSHOTS.computeIfAbsent(arm, ignored -> new ThermalFirstPersonItemArmSnapshots.Snapshot());
         snapshot.ensure(source.width, source.height);
         ThermalFramebufferState previous = ThermalFramebufferState.capture();

         try {
            GL30.glBindFramebuffer(36008, source.frameBufferId);
            GL30.glBindFramebuffer(36009, snapshot.target.frameBufferId);
            GL30.glBlitFramebuffer(0, 0, source.width, source.height, 0, 0, snapshot.target.width, snapshot.target.height, 16640, 9728);
            if (GL11.glGetError() == 0) {
               snapshot.valid = true;
               return;
            }

            snapshot.valid = false;
         } finally {
            previous.restore();
         }
      }
   }

   static boolean isValid(HumanoidArm arm) {
      ThermalFirstPersonItemArmSnapshots.Snapshot snapshot = SNAPSHOTS.get(arm);
      return snapshot != null && snapshot.valid && snapshot.target != null;
   }

   static RenderTarget target(HumanoidArm arm) {
      ThermalFirstPersonItemArmSnapshots.Snapshot snapshot = SNAPSHOTS.get(arm);
      return snapshot == null ? null : snapshot.target;
   }

   public static void close() {
      for (ThermalFirstPersonItemArmSnapshots.Snapshot snapshot : SNAPSHOTS.values()) {
         snapshot.close();
      }

      SNAPSHOTS.clear();
   }

   private static final class Snapshot {
      private RenderTarget target;
      private boolean valid;

      private void ensure(int width, int height) {
         if (this.target == null || this.target.width != width || this.target.height != height) {
            this.close();
            this.target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
            this.target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            this.target.setFilterMode(9728);
         }
      }

      private void close() {
         if (this.target != null) {
            this.target.destroyBuffers();
         }

         this.target = null;
         this.valid = false;
      }
   }
}
