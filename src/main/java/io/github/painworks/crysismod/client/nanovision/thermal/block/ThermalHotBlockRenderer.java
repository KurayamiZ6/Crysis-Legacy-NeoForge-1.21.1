package io.github.painworks.crysismod.client.nanovision.thermal.block;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.client.nanovision.NanovisionController;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import io.github.painworks.crysismod.client.nanovision.debug.NanovisionPerformanceProfiler;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerRenderTarget;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerRenderTypes;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalScreenProjection;
import it.unimi.dsi.fastutil.longs.Long2ByteMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public final class ThermalHotBlockRenderer {
   private static final int BLOCK_MARKER_ALPHA = 217;
   private static final MutableBlockPos BLOCK_POS = new MutableBlockPos();
   private static final Map<BlockState, List<AABB>> SHAPE_CACHE = new IdentityHashMap<>();

   private ThermalHotBlockRenderer() {
   }

   public static void render(Minecraft minecraft, PoseStack poseStack) {
      long cpuStarted = NanovisionPerformanceProfiler.beginCpu();
      int gpuQuery = 0;

      try {
         ClientLevel level = minecraft.level;
         if (level == null
            || minecraft.player == null
            || !NanovisionConfig.thermalHotBlocksEnabled()
            || !NanovisionState.isActive(minecraft.player)
            || !NanovisionController.shouldRenderMarkerPass()) {
            return;
         }

         RenderTarget target = ThermalMarkerRenderTarget.getOrCreate();
         if (target != null) {
            Camera camera = minecraft.gameRenderer.getMainCamera();
            double cameraX = camera.getPosition().x;
            double cameraY = camera.getPosition().y;
            double cameraZ = camera.getPosition().z;
            double maxDistance = NanovisionConfig.thermalHotBlockMaxDistance();
            double maxDistanceSquared = maxDistance * maxDistance;
            int renderCap = NanovisionConfig.thermalMarkerQuality().hotBlockRenderCap();
            int rendered = 0;
            BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
            VertexConsumer consumer = bufferSource.getBuffer(ThermalMarkerRenderTypes.hotBlock());
            Matrix4f pose = poseStack.last().pose();
            target.bindWrite(false);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            gpuQuery = NanovisionPerformanceProfiler.beginGpu(NanovisionPerformanceProfiler.Section.HOT_BLOCK_GPU);

            try {
               double previousBandSquared = 0.0;

               for (int band = 1; band <= 4 && rendered < renderCap; band++) {
                  double bandDistance = maxDistance * band / 4.0;
                  double bandSquared = band == 4 ? maxDistanceSquared : bandDistance * bandDistance;
                  ObjectIterator<Entry> iterator = ThermalHotBlockScanner.sources().long2ByteEntrySet().fastIterator();

                  while (iterator.hasNext() && rendered < renderCap) {
                     Entry entry = (Entry)iterator.next();
                     BLOCK_POS.set(entry.getLongKey());
                     double centerX = BLOCK_POS.getX() + 0.5;
                     double centerY = BLOCK_POS.getY() + 0.5;
                     double centerZ = BLOCK_POS.getZ() + 0.5;
                     double deltaX = centerX - cameraX;
                     double deltaY = centerY - cameraY;
                     double deltaZ = centerZ - cameraZ;
                     double distanceSquared = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
                     if (!(distanceSquared <= previousBandSquared) && !(distanceSquared > bandSquared) && level.hasChunkAt(BLOCK_POS)) {
                        BlockState state = level.getBlockState(BLOCK_POS);
                        ThermalBlockHeatProfile profile = ThermalBlockHeatResolver.resolve(state);
                        if (profile != ThermalBlockHeatProfile.NONE && ThermalBlockHeatResolver.isExposed(level, BLOCK_POS, state, profile)) {
                           float encodedSize = ThermalScreenProjection.encodedProjectedHeight(1.0, Math.sqrt(distanceSquared));
                           int red = Mth.clamp((int)(encodedSize * 255.0F), 1, 255);
                           int green = Mth.clamp((int)(profile.heat() * 255.0F), 0, 255);
                           int blue = Mth.clamp((int)(profile.coldMaterial() * 255.0F), 0, 255);
                           double x = BLOCK_POS.getX() - cameraX;
                           double y = BLOCK_POS.getY() - cameraY;
                           double z = BLOCK_POS.getZ() - cameraZ;
                           if (ThermalBlockHeatResolver.isLava(state)) {
                              double fluidHeight = Math.max(0.05, (double)state.getFluidState().getHeight(level, BLOCK_POS));
                              emitBox(consumer, pose, x, y, z, 0.0, 0.0, 0.0, 1.0, fluidHeight, 1.0, red, green, blue);
                           } else if (ThermalBlockHeatResolver.isFire(state)) {
                              emitFireCross(consumer, pose, x, y, z, red, green, blue);
                           } else {
                              for (AABB box : SHAPE_CACHE.computeIfAbsent(state, ignored -> state.getShape(level, BLOCK_POS).toAabbs())) {
                                 emitBox(consumer, pose, x, y, z, box, red, green, blue);
                              }
                           }

                           ThermalMarkerRenderTarget.markSourceRendered();
                           rendered++;
                        }
                     }
                  }

                  previousBandSquared = bandSquared;
               }

               return;
            } finally {
               bufferSource.endBatch(ThermalMarkerRenderTypes.hotBlock());
               minecraft.getMainRenderTarget().bindWrite(false);
               RenderSystem.colorMask(true, true, true, true);
               RenderSystem.depthMask(true);
               RenderSystem.enableDepthTest();
               RenderSystem.disableBlend();
            }
         }
      } finally {
         NanovisionPerformanceProfiler.endGpu(NanovisionPerformanceProfiler.Section.HOT_BLOCK_GPU, gpuQuery);
         NanovisionPerformanceProfiler.endCpu(NanovisionPerformanceProfiler.Section.HOT_BLOCK_RENDER, cpuStarted);
      }
   }

   public static void clear() {
      SHAPE_CACHE.clear();
      ThermalHotBlockScanner.clear();
   }

   private static void emitFireCross(VertexConsumer consumer, Matrix4f pose, double x, double y, double z, int red, int green, int blue) {
      emitQuad(consumer, pose, x + 0.12, y, z + 0.12, x + 0.88, y, z + 0.88, x + 0.88, y + 1.0, z + 0.88, x + 0.12, y + 1.0, z + 0.12, red, green, blue);
      emitQuad(consumer, pose, x + 0.88, y, z + 0.12, x + 0.12, y, z + 0.88, x + 0.12, y + 1.0, z + 0.88, x + 0.88, y + 1.0, z + 0.12, red, green, blue);
   }

   private static void emitBox(VertexConsumer consumer, Matrix4f pose, double x, double y, double z, AABB box, int red, int green, int blue) {
      emitBox(consumer, pose, x, y, z, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, red, green, blue);
   }

   private static void emitBox(
      VertexConsumer consumer,
      Matrix4f pose,
      double x,
      double y,
      double z,
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      int red,
      int green,
      int blue
   ) {
      double x0 = x + minX;
      double y0 = y + minY;
      double z0 = z + minZ;
      double x1 = x + maxX;
      double y1 = y + maxY;
      double z1 = z + maxZ;
      emitQuad(consumer, pose, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, red, green, blue);
      emitQuad(consumer, pose, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1, red, green, blue);
      emitQuad(consumer, pose, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1, red, green, blue);
      emitQuad(consumer, pose, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, red, green, blue);
      emitQuad(consumer, pose, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, red, green, blue);
      emitQuad(consumer, pose, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, red, green, blue);
   }

   private static void emitQuad(
      VertexConsumer consumer,
      Matrix4f pose,
      double x0,
      double y0,
      double z0,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      double x3,
      double y3,
      double z3,
      int red,
      int green,
      int blue
   ) {
      vertex(consumer, pose, x0, y0, z0, red, green, blue);
      vertex(consumer, pose, x1, y1, z1, red, green, blue);
      vertex(consumer, pose, x2, y2, z2, red, green, blue);
      vertex(consumer, pose, x3, y3, z3, red, green, blue);
   }

   private static void vertex(VertexConsumer consumer, Matrix4f pose, double x, double y, double z, int red, int green, int blue) {
      consumer.addVertex(pose, (float)x, (float)y, (float)z).setColor(red, green, blue, 217);
   }
}
