package io.github.painworks.crysismod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlock;
import io.github.painworks.crysismod.nanosuit.cradle.NanosuitCradleBlockEntity;
import io.github.painworks.crysismod.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

final class NanosuitCradleRenderer implements BlockEntityRenderer<NanosuitCradleBlockEntity> {
   static void register(EntityRenderersEvent.RegisterRenderers event) {
      event.registerBlockEntityRenderer(ModBlocks.NANOSUIT_CRADLE_ENTITY.get(), NanosuitCradleRenderer::new);
   }

   private NanosuitCradleRenderer(BlockEntityRendererProvider.Context context) {}

   @Override
   public void render(NanosuitCradleBlockEntity cradle, float partialTick, PoseStack pose,
      MultiBufferSource buffers, int light, int overlay) {
      var level = cradle.getLevel();
      if (level == null) return;
      var state = cradle.getBlockState();
      var direction = state.getValue(NanosuitCradleBlock.FACING);
      boolean legacy = state.getValue(NanosuitCradleBlock.PART) == NanosuitCradleBlock.Part.LEGACY;
      var items = Minecraft.getInstance().getItemRenderer();
      pose.pushPose();
      try {
         pose.translate(.5, 1.025, .5);
         pose.mulPose(Axis.YP.rotationDegrees(-direction.toYRot()));
         for (int i = 0; i < 4; i++) {
            var stack = cradle.displayedPiece(i);
            if (stack.isEmpty()) continue;
            int row = i / 2;
            var at = legacy ? cradle.getBlockPos() : cradle.getBlockPos().relative(direction, row);
            pose.pushPose();
            try {
               pose.translate(i % 2 == 0 ? -.24 : .24, 0, legacy ? (row == 0 ? -.24 : .24) : row);
               pose.mulPose(Axis.XP.rotationDegrees(90));
               pose.scale(.8f, .8f, .8f);
               items.renderStatic(stack, ItemDisplayContext.FIXED, LevelRenderer.getLightColor(level, at.above()),
                  overlay, pose, buffers, level, i);
            } finally {
               pose.popPose();
            }
         }
      } finally {
         pose.popPose();
      }
   }

   @Override
   public AABB getRenderBoundingBox(NanosuitCradleBlockEntity cradle) {
      var pos = cradle.getBlockPos();
      var state = cradle.getBlockState();
      var box = new AABB(pos);
      if (state.getValue(NanosuitCradleBlock.PART) == NanosuitCradleBlock.Part.MASTER) {
         box = box.minmax(new AABB(pos.relative(state.getValue(NanosuitCradleBlock.FACING))));
      }
      return box.expandTowards(0, .3, 0);
   }
}
