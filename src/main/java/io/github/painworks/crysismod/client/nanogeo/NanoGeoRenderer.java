package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import java.util.Locale;
import java.util.function.Predicate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class NanoGeoRenderer {
   private final NanoGeoRenderProfiler.NanoGeoRenderMetrics perfMetrics = new NanoGeoRenderProfiler.NanoGeoRenderMetrics();
   private NanoGeoRenderer.OverlayWaveState overlayWaveState;
   private static final float[] WHITE_TINT = new float[]{1.0F, 1.0F, 1.0F};
   private static final float[] NORTH_TINT = new float[]{1.0F, 0.35F, 0.35F};
   private static final float[] SOUTH_TINT = new float[]{0.35F, 1.0F, 0.35F};
   private static final float[] EAST_TINT = new float[]{0.35F, 0.35F, 1.0F};
   private static final float[] WEST_TINT = new float[]{1.0F, 1.0F, 0.35F};
   private static final float[] UP_TINT = new float[]{1.0F, 0.35F, 1.0F};
   private static final float[] DOWN_TINT = new float[]{0.35F, 1.0F, 1.0F};
   private static final float[] VERTEX0_TINT = new float[]{1.0F, 0.5F, 0.5F};
   private static final float[] VERTEX1_TINT = new float[]{0.5F, 1.0F, 0.5F};
   private static final float[] VERTEX2_TINT = new float[]{0.5F, 0.5F, 1.0F};
   private static final float[] VERTEX3_TINT = new float[]{1.0F, 1.0F, 0.5F};
   private static final NanoGeoBakedModel.NanoGeoBakedBone[] EMPTY_BONES = new NanoGeoBakedModel.NanoGeoBakedBone[0];
   private static final float[] LEFT_TINT = new float[]{0.8F, 0.92F, 1.0F};
   private static final float[] RIGHT_TINT = new float[]{1.0F, 0.85F, 0.85F};
   private static final float AXIS_MARKER_LENGTH = 0.15F;
   private static final float PIVOT_MARKER_SIZE = 0.03F;

   public void beginOverlayWave(boolean enabled, float intensity, float width, float speed, float frequency, float time) {
      if (enabled && !(intensity <= 0.0F)) {
         this.overlayWaveState = new NanoGeoRenderer.OverlayWaveState(
            this.clamp01(intensity), this.clamp(width, 0.01F, 1.0F), Math.max(0.0F, speed), Math.max(0.2F, frequency), time
         );
      } else {
         this.overlayWaveState = null;
      }
   }

   public void endOverlayWave() {
      this.overlayWaveState = null;
   }

   public void renderStatic(
      NanoGeoBakedModel bakedModel, NanoGeoRenderContext context, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha
   ) {
      this.renderStatic(
         bakedModel, context, vertexConsumer, red, green, blue, alpha, bone -> true, bone -> true, (NanoGeoRenderer.IndexedBoneTransformProvider)null
      );
   }

   public void renderStatic(
      NanoGeoBakedModel bakedModel,
      NanoGeoRenderContext context,
      VertexConsumer vertexConsumer,
      float red,
      float green,
      float blue,
      float alpha,
      Predicate<String> visibleBoneFilter
   ) {
      if (NanoGeoClientConfig.nanoGeoPerfTrackAllocations()) {
         NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.ALLOCATIONS_SUSPECTED);
      }

      NanoGeoRenderer.BoneVisibilityFilter indexedVisibilityFilter = bone -> visibleBoneFilter.test(bone.name());
      this.renderStatic(
         bakedModel,
         context,
         vertexConsumer,
         red,
         green,
         blue,
         alpha,
         indexedVisibilityFilter,
         bone -> true,
         (NanoGeoRenderer.IndexedBoneTransformProvider)null
      );
   }

   public void renderStatic(
      NanoGeoBakedModel bakedModel,
      NanoGeoRenderContext context,
      VertexConsumer vertexConsumer,
      float red,
      float green,
      float blue,
      float alpha,
      Predicate<String> visibleBoneFilter,
      NanoGeoRenderer.BoneTransformProvider boneTransformProvider
   ) {
      if (NanoGeoClientConfig.nanoGeoPerfTrackAllocations()) {
         NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.ALLOCATIONS_SUSPECTED);
      }

      NanoGeoRenderer.BoneVisibilityFilter indexedVisibilityFilter = bone -> visibleBoneFilter.test(bone.name());
      NanoGeoRenderer.IndexedBoneTransformProvider indexedTransformProvider = null;
      if (boneTransformProvider != null) {
         if (NanoGeoClientConfig.nanoGeoPerfTrackAllocations()) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.ALLOCATIONS_SUSPECTED);
         }

         indexedTransformProvider = bone -> boneTransformProvider.transformFor(bone.name());
      }

      this.renderStatic(bakedModel, context, vertexConsumer, red, green, blue, alpha, indexedVisibilityFilter, bone -> true, indexedTransformProvider);
   }

   public void renderStaticIndexedFast(
      NanoGeoBakedModel bakedModel,
      NanoGeoRenderContext context,
      VertexConsumer vertexConsumer,
      float red,
      float green,
      float blue,
      float alpha,
      boolean[] visibleBonesById,
      boolean[] subtreeVisibleById,
      NanoGeoRenderer.NanoGeoBoneTransform[] transformsById
   ) {
      this.renderStaticIndexedFast(
         bakedModel,
         context.poseStack(),
         context.bufferSource(),
         context.packedLight(),
         context.packedOverlay(),
         vertexConsumer,
         red,
         green,
         blue,
         alpha,
         visibleBonesById,
         subtreeVisibleById,
         transformsById
      );
   }

   public void renderStaticIndexedFast(
      NanoGeoBakedModel bakedModel,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      int packedOverlay,
      VertexConsumer vertexConsumer,
      float red,
      float green,
      float blue,
      float alpha,
      boolean[] visibleBonesById,
      boolean[] subtreeVisibleById,
      NanoGeoRenderer.NanoGeoBoneTransform[] transformsById
   ) {
      boolean ownedProfilerFrame = NanoGeoProfiler.INSTANCE
         .beginFrameIfNeeded(bufferSource == null ? NanoGeoProfiler.PassType.ARMOR : NanoGeoProfiler.PassType.STATIC);
      boolean profileEnabled = ownedProfilerFrame || NanoGeoProfiler.INSTANCE.isActive();
      boolean perfEnabled = NanoGeoClientConfig.nanoGeoDebugPerfEnabled();
      NanoGeoRenderProfiler.RenderPass perfPass = bufferSource == null ? NanoGeoRenderProfiler.beginArmorPass() : NanoGeoRenderProfiler.beginStaticPass();
      if (perfEnabled) {
         this.perfMetrics.reset();
      }

      long traversalSection = profileEnabled ? NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.BONE_TRAVERSAL) : 0L;

      try {
         for (NanoGeoBakedModel.NanoGeoBakedBone rootBone : bakedModel.rootBones()) {
            this.renderBoneFast(
               rootBone,
               poseStack,
               vertexConsumer,
               packedLight,
               packedOverlay,
               red,
               green,
               blue,
               alpha,
               visibleBonesById,
               subtreeVisibleById,
               transformsById,
               profileEnabled,
               perfEnabled,
               this.perfMetrics
            );
         }
      } finally {
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.BONE_TRAVERSAL, traversalSection);
         }

         NanoGeoRenderProfiler.endPass(perfPass, this.perfMetrics);
         NanoGeoProfiler.INSTANCE.endFrameIfOwned(ownedProfilerFrame);
      }
   }

   public void renderStaticIndexedPlanned(
      NanoGeoBakedModel bakedModel,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      int packedOverlay,
      VertexConsumer vertexConsumer,
      float red,
      float green,
      float blue,
      float alpha,
      boolean[] visibleBonesById,
      NanoGeoBakedModel.NanoGeoBakedBone[] traversableRoots,
      NanoGeoBakedModel.NanoGeoBakedBone[][] traversableChildrenByBoneId,
      NanoGeoRenderer.NanoGeoBoneTransform[] transformsById
   ) {
      boolean ownedProfilerFrame = NanoGeoProfiler.INSTANCE
         .beginFrameIfNeeded(bufferSource == null ? NanoGeoProfiler.PassType.ARMOR : NanoGeoProfiler.PassType.STATIC);
      boolean profileEnabled = ownedProfilerFrame || NanoGeoProfiler.INSTANCE.isActive();
      boolean perfEnabled = NanoGeoClientConfig.nanoGeoDebugPerfEnabled();
      NanoGeoRenderProfiler.RenderPass perfPass = bufferSource == null ? NanoGeoRenderProfiler.beginArmorPass() : NanoGeoRenderProfiler.beginStaticPass();
      if (perfEnabled) {
         this.perfMetrics.reset();
      }

      long traversalSection = profileEnabled ? NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.BONE_TRAVERSAL) : 0L;

      try {
         for (NanoGeoBakedModel.NanoGeoBakedBone rootBone : traversableRoots) {
            this.renderBonePlanned(
               rootBone,
               poseStack,
               vertexConsumer,
               packedLight,
               packedOverlay,
               red,
               green,
               blue,
               alpha,
               visibleBonesById,
               traversableChildrenByBoneId,
               transformsById,
               profileEnabled,
               perfEnabled,
               this.perfMetrics
            );
         }
      } finally {
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.BONE_TRAVERSAL, traversalSection);
         }

         NanoGeoRenderProfiler.endPass(perfPass, this.perfMetrics);
         NanoGeoProfiler.INSTANCE.endFrameIfOwned(ownedProfilerFrame);
      }
   }

   public void renderStatic(
      NanoGeoBakedModel bakedModel,
      NanoGeoRenderContext context,
      VertexConsumer vertexConsumer,
      float red,
      float green,
      float blue,
      float alpha,
      NanoGeoRenderer.BoneVisibilityFilter visibleBoneFilter,
      NanoGeoRenderer.BoneTraversalFilter traversalFilter,
      NanoGeoRenderer.IndexedBoneTransformProvider boneTransformProvider
   ) {
      boolean ownedProfilerFrame = NanoGeoProfiler.INSTANCE
         .beginFrameIfNeeded(context.bufferSource() == null ? NanoGeoProfiler.PassType.ARMOR : NanoGeoProfiler.PassType.STATIC);
      boolean profileEnabled = ownedProfilerFrame || NanoGeoProfiler.INSTANCE.isActive();
      String selectedBone = NanoGeoClientConfig.nanoGeoDebugRenderOnlyBone();
      boolean hasSelection = !selectedBone.isBlank();
      Direction faceFilter = this.resolveFaceFilter();
      int cubeIndexFilter = NanoGeoClientConfig.nanoGeoDebugRenderOnlyCubeIndex();
      boolean hideChildrenForSelectedBone = hasSelection && NanoGeoClientConfig.nanoGeoDebugHideChildren();
      boolean debugUvEnabled = NanoGeoClientConfig.nanoGeoDebugUv();
      boolean faceTintEnabled = debugUvEnabled && NanoGeoClientConfig.nanoGeoDebugUvFaceColors();
      boolean vertexTintEnabled = debugUvEnabled && NanoGeoClientConfig.nanoGeoDebugVertexColors();
      boolean sideTintEnabled = NanoGeoClientConfig.nanoGeoDebugTintLeftRight();
      boolean pivotMarkersEnabled = NanoGeoClientConfig.nanoGeoDebugShowBonePivots();
      boolean axesMarkersEnabled = NanoGeoClientConfig.nanoGeoDebugShowBoneAxes();
      boolean boneMarkersEnabled = context.bufferSource() != null && (pivotMarkersEnabled || axesMarkersEnabled);
      String markerFocusBone = boneMarkersEnabled ? NanoGeoClientConfig.nanoGeoDebugBoneName() : "";
      boolean perfEnabled = NanoGeoClientConfig.nanoGeoDebugPerfEnabled();
      NanoGeoRenderProfiler.RenderPass perfPass = context.bufferSource() == null
         ? NanoGeoRenderProfiler.beginArmorPass()
         : NanoGeoRenderProfiler.beginStaticPass();
      if (perfEnabled) {
         this.perfMetrics.reset();
      }

      long traversalSection = profileEnabled ? NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.BONE_TRAVERSAL) : 0L;

      try {
         for (NanoGeoBakedModel.NanoGeoBakedBone rootBone : bakedModel.rootBones()) {
            this.renderBone(
               rootBone,
               context,
               context.poseStack(),
               vertexConsumer,
               context.packedLight(),
               context.packedOverlay(),
               red,
               green,
               blue,
               alpha,
               visibleBoneFilter,
               traversalFilter,
               boneTransformProvider,
               hasSelection,
               selectedBone,
               false,
               cubeIndexFilter,
               hideChildrenForSelectedBone,
               faceFilter,
               faceTintEnabled,
               vertexTintEnabled,
               sideTintEnabled,
               boneMarkersEnabled,
               pivotMarkersEnabled,
               axesMarkersEnabled,
               markerFocusBone,
               profileEnabled,
               perfEnabled,
               this.perfMetrics
            );
         }
      } finally {
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.BONE_TRAVERSAL, traversalSection);
         }

         NanoGeoRenderProfiler.endPass(perfPass, this.perfMetrics);
         NanoGeoProfiler.INSTANCE.endFrameIfOwned(ownedProfilerFrame);
      }
   }

   private void renderBone(
      NanoGeoBakedModel.NanoGeoBakedBone bone,
      NanoGeoRenderContext context,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight,
      int packedOverlay,
      float red,
      float green,
      float blue,
      float alpha,
      NanoGeoRenderer.BoneVisibilityFilter visibleBoneFilter,
      NanoGeoRenderer.BoneTraversalFilter traversalFilter,
      NanoGeoRenderer.IndexedBoneTransformProvider boneTransformProvider,
      boolean hasSelection,
      String selectedBone,
      boolean selectedAncestor,
      int cubeIndexFilter,
      boolean hideChildrenForSelectedBone,
      Direction faceFilter,
      boolean faceTintEnabled,
      boolean vertexTintEnabled,
      boolean sideTintEnabled,
      boolean boneMarkersEnabled,
      boolean pivotMarkersEnabled,
      boolean axesMarkersEnabled,
      String markerFocusBone,
      boolean profileEnabled,
      boolean perfEnabled,
      NanoGeoRenderProfiler.NanoGeoRenderMetrics metrics
   ) {
      if (!traversalFilter.shouldTraverse(bone)) {
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.SKIPPED_BONES);
         }
      } else {
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.RENDERED_BONES);
         }

         if (perfEnabled) {
            metrics.visitedBones++;
         }

         NanoGeoRenderer.NanoGeoBoneTransform externalTransform = boneTransformProvider == null
            ? NanoGeoRenderer.NanoGeoBoneTransform.IDENTITY
            : this.safeTransform(boneTransformProvider.transformFor(bone));
         float combinedRotX = bone.rotationX() + externalTransform.rotationX();
         float combinedRotY = bone.rotationY() + externalTransform.rotationY();
         float combinedRotZ = bone.rotationZ() + externalTransform.rotationZ();
         boolean hasExternalTranslation = externalTransform.translateX != 0.0F || externalTransform.translateY != 0.0F || externalTransform.translateZ != 0.0F;
         boolean hasCombinedRotation = combinedRotX != 0.0F || combinedRotY != 0.0F || combinedRotZ != 0.0F;
         boolean needsPoseScope = hasExternalTranslation || hasCombinedRotation;
         if (needsPoseScope) {
            poseStack.pushPose();
            if (profileEnabled) {
               NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_PUSH);
            }
         }

         if (hasExternalTranslation) {
            poseStack.translate(externalTransform.translateX, externalTransform.translateY, externalTransform.translateZ);
         }

         if (hasCombinedRotation) {
            if (profileEnabled) {
               NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MATRIX_MULTIPLY, 3L);
            }

            this.applyPivotedRotation(poseStack, bone.pivotX(), bone.pivotY(), bone.pivotZ(), combinedRotX, combinedRotY, combinedRotZ);
         }

         if (boneMarkersEnabled) {
            this.renderBoneDebugMarkers(context, poseStack, bone, packedLight, pivotMarkersEnabled, axesMarkersEnabled, markerFocusBone);
         }

         float sideTintR = 1.0F;
         float sideTintG = 1.0F;
         float sideTintB = 1.0F;
         if (sideTintEnabled) {
            float[] sideTint = this.resolveSideTint(bone.name());
            sideTintR = sideTint[0];
            sideTintG = sideTint[1];
            sideTintB = sideTint[2];
         }

         boolean boneMatchesSelection = hasSelection && bone.name().equals(selectedBone);
         boolean renderBySelection = !hasSelection || boneMatchesSelection || selectedAncestor && !hideChildrenForSelectedBone;
         if (renderBySelection && visibleBoneFilter.isVisible(bone)) {
            if (perfEnabled) {
               metrics.renderedBones++;
            }

            for (int cubeIndex = 0; cubeIndex < bone.cubes().size(); cubeIndex++) {
               if (cubeIndexFilter < 0 || cubeIndex == cubeIndexFilter) {
                  NanoGeoBakedModel.NanoGeoBakedCube cube = bone.cubes().get(cubeIndex);
                  this.renderCube(
                     cube,
                     poseStack,
                     vertexConsumer,
                     packedLight,
                     packedOverlay,
                     red * sideTintR,
                     green * sideTintG,
                     blue * sideTintB,
                     alpha,
                     faceFilter,
                     faceTintEnabled,
                     vertexTintEnabled,
                     profileEnabled,
                     perfEnabled,
                     metrics
                  );
               } else if (profileEnabled) {
                  NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.SKIPPED_CUBES);
               }
            }
         } else if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.SKIPPED_CUBES, bone.cubes().size());
         }

         boolean childSelectedAncestor = selectedAncestor || boneMatchesSelection;
         boolean hideChildren = hideChildrenForSelectedBone && boneMatchesSelection;

         for (NanoGeoBakedModel.NanoGeoBakedBone child : bone.children()) {
            if (hideChildren) {
               break;
            }

            this.renderBone(
               child,
               context,
               poseStack,
               vertexConsumer,
               packedLight,
               packedOverlay,
               red,
               green,
               blue,
               alpha,
               visibleBoneFilter,
               traversalFilter,
               boneTransformProvider,
               hasSelection,
               selectedBone,
               childSelectedAncestor,
               cubeIndexFilter,
               hideChildrenForSelectedBone,
               faceFilter,
               faceTintEnabled,
               vertexTintEnabled,
               sideTintEnabled,
               boneMarkersEnabled,
               pivotMarkersEnabled,
               axesMarkersEnabled,
               markerFocusBone,
               profileEnabled,
               perfEnabled,
               metrics
            );
         }

         if (needsPoseScope) {
            poseStack.popPose();
            if (profileEnabled) {
               NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_POP);
            }
         }
      }
   }

   private void renderBoneFast(
      NanoGeoBakedModel.NanoGeoBakedBone bone,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight,
      int packedOverlay,
      float red,
      float green,
      float blue,
      float alpha,
      boolean[] visibleBonesById,
      boolean[] subtreeVisibleById,
      NanoGeoRenderer.NanoGeoBoneTransform[] transformsById,
      boolean profileEnabled,
      boolean perfEnabled,
      NanoGeoRenderProfiler.NanoGeoRenderMetrics metrics
   ) {
      int boneId = bone.boneId();
      if (!this.isFlagEnabled(subtreeVisibleById, boneId, true)) {
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.SKIPPED_BONES);
         }
      } else {
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.RENDERED_BONES);
         }

         if (perfEnabled) {
            metrics.visitedBones++;
         }

         NanoGeoRenderer.NanoGeoBoneTransform externalTransform = this.transformById(transformsById, boneId);
         float combinedRotX = bone.rotationX() + externalTransform.rotationX();
         float combinedRotY = bone.rotationY() + externalTransform.rotationY();
         float combinedRotZ = bone.rotationZ() + externalTransform.rotationZ();
         boolean hasExternalTranslation = externalTransform.translateX != 0.0F || externalTransform.translateY != 0.0F || externalTransform.translateZ != 0.0F;
         boolean hasCombinedRotation = combinedRotX != 0.0F || combinedRotY != 0.0F || combinedRotZ != 0.0F;
         boolean needsPoseScope = hasExternalTranslation || hasCombinedRotation;
         if (needsPoseScope) {
            poseStack.pushPose();
            if (profileEnabled) {
               NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_PUSH);
            }
         }

         if (hasExternalTranslation) {
            poseStack.translate(externalTransform.translateX, externalTransform.translateY, externalTransform.translateZ);
         }

         if (hasCombinedRotation) {
            if (profileEnabled) {
               NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MATRIX_MULTIPLY, 3L);
            }

            this.applyPivotedRotation(poseStack, bone.pivotX(), bone.pivotY(), bone.pivotZ(), combinedRotX, combinedRotY, combinedRotZ);
         }

         if (this.isFlagEnabled(visibleBonesById, boneId, true)) {
            if (perfEnabled) {
               metrics.renderedBones++;
            }

            for (int cubeIndex = 0; cubeIndex < bone.cubes().size(); cubeIndex++) {
               NanoGeoBakedModel.NanoGeoBakedCube cube = bone.cubes().get(cubeIndex);
               if (!profileEnabled && !perfEnabled) {
                  this.renderCubeUltraFast(cube, poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
               } else {
                  this.renderCubeFast(
                     cube, poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha, profileEnabled, perfEnabled, metrics
                  );
               }
            }
         } else if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.SKIPPED_CUBES, bone.cubes().size());
         }

         for (NanoGeoBakedModel.NanoGeoBakedBone child : bone.children()) {
            this.renderBoneFast(
               child,
               poseStack,
               vertexConsumer,
               packedLight,
               packedOverlay,
               red,
               green,
               blue,
               alpha,
               visibleBonesById,
               subtreeVisibleById,
               transformsById,
               profileEnabled,
               perfEnabled,
               metrics
            );
         }

         if (needsPoseScope) {
            poseStack.popPose();
            if (profileEnabled) {
               NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_POP);
            }
         }
      }
   }

   private void renderBonePlanned(
      NanoGeoBakedModel.NanoGeoBakedBone bone,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight,
      int packedOverlay,
      float red,
      float green,
      float blue,
      float alpha,
      boolean[] visibleBonesById,
      NanoGeoBakedModel.NanoGeoBakedBone[][] traversableChildrenByBoneId,
      NanoGeoRenderer.NanoGeoBoneTransform[] transformsById,
      boolean profileEnabled,
      boolean perfEnabled,
      NanoGeoRenderProfiler.NanoGeoRenderMetrics metrics
   ) {
      if (profileEnabled) {
         NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.RENDERED_BONES);
      }

      if (perfEnabled) {
         metrics.visitedBones++;
      }

      int boneId = bone.boneId();
      NanoGeoRenderer.NanoGeoBoneTransform externalTransform = this.transformById(transformsById, boneId);
      float combinedRotX = bone.rotationX() + externalTransform.rotationX();
      float combinedRotY = bone.rotationY() + externalTransform.rotationY();
      float combinedRotZ = bone.rotationZ() + externalTransform.rotationZ();
      boolean hasExternalTranslation = externalTransform.translateX != 0.0F || externalTransform.translateY != 0.0F || externalTransform.translateZ != 0.0F;
      boolean hasCombinedRotation = combinedRotX != 0.0F || combinedRotY != 0.0F || combinedRotZ != 0.0F;
      boolean needsPoseScope = hasExternalTranslation || hasCombinedRotation;
      if (needsPoseScope) {
         poseStack.pushPose();
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_PUSH);
         }
      }

      if (hasExternalTranslation) {
         poseStack.translate(externalTransform.translateX, externalTransform.translateY, externalTransform.translateZ);
      }

      if (hasCombinedRotation) {
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MATRIX_MULTIPLY, 3L);
         }

         this.applyPivotedRotation(poseStack, bone.pivotX(), bone.pivotY(), bone.pivotZ(), combinedRotX, combinedRotY, combinedRotZ);
      }

      if (this.isFlagEnabled(visibleBonesById, boneId, true)) {
         if (perfEnabled) {
            metrics.renderedBones++;
         }

         for (int cubeIndex = 0; cubeIndex < bone.cubes().size(); cubeIndex++) {
            NanoGeoBakedModel.NanoGeoBakedCube cube = bone.cubes().get(cubeIndex);
            if (!profileEnabled && !perfEnabled) {
               this.renderCubeUltraFast(cube, poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            } else {
               this.renderCubeFast(cube, poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha, profileEnabled, perfEnabled, metrics);
            }
         }
      } else if (profileEnabled) {
         NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.SKIPPED_CUBES, bone.cubes().size());
      }

      NanoGeoBakedModel.NanoGeoBakedBone[] children = boneId >= 0 && boneId < traversableChildrenByBoneId.length
         ? traversableChildrenByBoneId[boneId]
         : EMPTY_BONES;
      if (children != null) {
         for (NanoGeoBakedModel.NanoGeoBakedBone child : children) {
            this.renderBonePlanned(
               child,
               poseStack,
               vertexConsumer,
               packedLight,
               packedOverlay,
               red,
               green,
               blue,
               alpha,
               visibleBonesById,
               traversableChildrenByBoneId,
               transformsById,
               profileEnabled,
               perfEnabled,
               metrics
            );
         }
      }

      if (needsPoseScope) {
         poseStack.popPose();
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_POP);
         }
      }
   }

   private void renderCube(
      NanoGeoBakedModel.NanoGeoBakedCube cube,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight,
      int packedOverlay,
      float red,
      float green,
      float blue,
      float alpha,
      Direction faceFilter,
      boolean faceTintEnabled,
      boolean vertexTintEnabled,
      boolean profileEnabled,
      boolean perfEnabled,
      NanoGeoRenderProfiler.NanoGeoRenderMetrics metrics
   ) {
      long cubeSection = profileEnabled ? NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.CUBE_RENDER) : 0L;
      boolean hasRotation = cube.hasRotation();
      if (hasRotation) {
         poseStack.pushPose();
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_PUSH);
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MATRIX_MULTIPLY, 3L);
         }

         this.applyPivotedRotation(poseStack, cube.pivotX(), cube.pivotY(), cube.pivotZ(), cube.rotationX(), cube.rotationY(), cube.rotationZ());
      }

      if (profileEnabled) {
         NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.RENDERED_CUBES);
      }

      if (perfEnabled) {
         metrics.renderedCubes++;
         if (hasRotation) {
            metrics.rotatedCubes++;
         }
      }

      Pose pose = poseStack.last();
      Matrix4f poseMatrix = pose.pose();
      Matrix3f normalMatrix = pose.normal();
      float nm00 = normalMatrix.m00();
      float nm01 = normalMatrix.m01();
      float nm02 = normalMatrix.m02();
      float nm10 = normalMatrix.m10();
      float nm11 = normalMatrix.m11();
      float nm12 = normalMatrix.m12();
      float nm20 = normalMatrix.m20();
      float nm21 = normalMatrix.m21();
      float nm22 = normalMatrix.m22();
      long vertexSection = profileEnabled ? NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.VERTEX_EMISSION) : 0L;

      for (NanoGeoBakedModel.NanoGeoBakedQuad quad : cube.quads()) {
         if (faceFilter != null && quad.direction() != faceFilter) {
            if (profileEnabled) {
               NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.SKIPPED_CUBES);
            }
         } else {
            if (profileEnabled) {
               NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.RENDERED_QUADS);
            }

            float nx = quad.normalX();
            float ny = quad.normalY();
            float nz = quad.normalZ();
            float tnx = this.transformNormalX(nm00, nm10, nm20, nx, ny, nz);
            float tny = this.transformNormalY(nm01, nm11, nm21, nx, ny, nz);
            float tnz = this.transformNormalZ(nm02, nm12, nm22, nx, ny, nz);
            float faceRed = red;
            float faceGreen = green;
            float faceBlue = blue;
            if (faceTintEnabled) {
               float[] faceTint = this.resolveFaceTint(quad.direction());
               faceRed = red * faceTint[0];
               faceGreen = green * faceTint[1];
               faceBlue = blue * faceTint[2];
            }

            float[] p = quad.positions();
            float[] uv = quad.uvs();
            if (perfEnabled) {
               metrics.renderedQuads++;
               metrics.renderedVertices += 4;
            }

            if (!vertexTintEnabled) {
               this.emitQuad(poseMatrix, vertexConsumer, packedLight, packedOverlay, faceRed, faceGreen, faceBlue, alpha, tnx, tny, tnz, p, uv);
               if (profileEnabled) {
                  NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.EMITTED_VERTICES, 4L);
               }
            } else {
               for (int i = 0; i < 4; i++) {
                  int pIndex = i * 3;
                  int uvIndex = i * 2;
                  float[] vertexTint = this.resolveVertexTint(i);
                  float uvU = uv[uvIndex];
                  float uvV = uv[uvIndex + 1];
                  float vertexAlpha = alpha * this.waveAlphaForUv(uvU, uvV);
                  vertexConsumer.addVertex(poseMatrix, p[pIndex], p[pIndex + 1], p[pIndex + 2])
                     .setColor(faceRed * vertexTint[0], faceGreen * vertexTint[1], faceBlue * vertexTint[2], vertexAlpha)
                     .setUv(uvU, uvV)
                     .setOverlay(packedOverlay)
                     .setLight(packedLight)
                     .setNormal(tnx, tny, tnz);
               }

               if (profileEnabled) {
                  NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.EMITTED_VERTICES, 4L);
               }
            }
         }
      }

      if (profileEnabled) {
         NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.VERTEX_EMISSION, vertexSection);
      }

      if (hasRotation) {
         poseStack.popPose();
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_POP);
         }
      }

      if (profileEnabled) {
         NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.CUBE_RENDER, cubeSection);
      }
   }

   private void renderCubeFast(
      NanoGeoBakedModel.NanoGeoBakedCube cube,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight,
      int packedOverlay,
      float red,
      float green,
      float blue,
      float alpha,
      boolean profileEnabled,
      boolean perfEnabled,
      NanoGeoRenderProfiler.NanoGeoRenderMetrics metrics
   ) {
      long cubeSection = profileEnabled ? NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.CUBE_RENDER) : 0L;
      boolean hasRotation = cube.hasRotation();
      if (hasRotation) {
         poseStack.pushPose();
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_PUSH);
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MATRIX_MULTIPLY, 3L);
         }

         this.applyPivotedRotation(poseStack, cube.pivotX(), cube.pivotY(), cube.pivotZ(), cube.rotationX(), cube.rotationY(), cube.rotationZ());
      }

      if (profileEnabled) {
         NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.RENDERED_CUBES);
      }

      if (perfEnabled) {
         metrics.renderedCubes++;
         if (hasRotation) {
            metrics.rotatedCubes++;
         }
      }

      Pose pose = poseStack.last();
      Matrix4f poseMatrix = pose.pose();
      Matrix3f normalMatrix = pose.normal();
      float nm00 = normalMatrix.m00();
      float nm01 = normalMatrix.m01();
      float nm02 = normalMatrix.m02();
      float nm10 = normalMatrix.m10();
      float nm11 = normalMatrix.m11();
      float nm12 = normalMatrix.m12();
      float nm20 = normalMatrix.m20();
      float nm21 = normalMatrix.m21();
      float nm22 = normalMatrix.m22();
      long vertexSection = profileEnabled ? NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.VERTEX_EMISSION) : 0L;

      for (NanoGeoBakedModel.NanoGeoBakedQuad quad : cube.quads()) {
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.RENDERED_QUADS);
         }

         float nx = quad.normalX();
         float ny = quad.normalY();
         float nz = quad.normalZ();
         float tnx = this.transformNormalX(nm00, nm10, nm20, nx, ny, nz);
         float tny = this.transformNormalY(nm01, nm11, nm21, nx, ny, nz);
         float tnz = this.transformNormalZ(nm02, nm12, nm22, nx, ny, nz);
         float[] p = quad.positions();
         float[] uv = quad.uvs();
         if (perfEnabled) {
            metrics.renderedQuads++;
            metrics.renderedVertices += 4;
         }

         this.emitQuad(poseMatrix, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha, tnx, tny, tnz, p, uv);
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.EMITTED_VERTICES, 4L);
         }
      }

      if (profileEnabled) {
         NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.VERTEX_EMISSION, vertexSection);
      }

      if (hasRotation) {
         poseStack.popPose();
         if (profileEnabled) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_POP);
         }
      }

      if (profileEnabled) {
         NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.CUBE_RENDER, cubeSection);
      }
   }

   private void renderCubeUltraFast(
      NanoGeoBakedModel.NanoGeoBakedCube cube,
      PoseStack poseStack,
      VertexConsumer vertexConsumer,
      int packedLight,
      int packedOverlay,
      float red,
      float green,
      float blue,
      float alpha
   ) {
      boolean hasRotation = cube.hasRotation();
      if (hasRotation) {
         poseStack.pushPose();
         this.applyPivotedRotation(poseStack, cube.pivotX(), cube.pivotY(), cube.pivotZ(), cube.rotationX(), cube.rotationY(), cube.rotationZ());
      }

      Pose pose = poseStack.last();
      Matrix4f poseMatrix = pose.pose();
      Matrix3f normalMatrix = pose.normal();
      float nm00 = normalMatrix.m00();
      float nm01 = normalMatrix.m01();
      float nm02 = normalMatrix.m02();
      float nm10 = normalMatrix.m10();
      float nm11 = normalMatrix.m11();
      float nm12 = normalMatrix.m12();
      float nm20 = normalMatrix.m20();
      float nm21 = normalMatrix.m21();
      float nm22 = normalMatrix.m22();

      for (NanoGeoBakedModel.NanoGeoBakedQuad quad : cube.quads()) {
         float nx = quad.normalX();
         float ny = quad.normalY();
         float nz = quad.normalZ();
         float tnx = this.transformNormalX(nm00, nm10, nm20, nx, ny, nz);
         float tny = this.transformNormalY(nm01, nm11, nm21, nx, ny, nz);
         float tnz = this.transformNormalZ(nm02, nm12, nm22, nx, ny, nz);
         float[] p = quad.positions();
         float[] uv = quad.uvs();
         this.emitQuad(poseMatrix, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha, tnx, tny, tnz, p, uv);
      }

      if (hasRotation) {
         poseStack.popPose();
      }
   }

   private void applyPivotedRotation(PoseStack poseStack, float pivotX, float pivotY, float pivotZ, float rotationX, float rotationY, float rotationZ) {
      if (rotationX != 0.0F || rotationY != 0.0F || rotationZ != 0.0F) {
         float px = NanoGeoCoordinateSystem.modelUnits(pivotX);
         float py = NanoGeoCoordinateSystem.modelUnits(pivotY);
         float pz = NanoGeoCoordinateSystem.modelUnits(pivotZ);
         poseStack.translate(px, py, pz);
         poseStack.mulPose(Axis.ZP.rotation(rotationZ));
         poseStack.mulPose(Axis.YP.rotation(rotationY));
         poseStack.mulPose(Axis.XP.rotation(rotationX));
         poseStack.translate(-px, -py, -pz);
      }
   }

   private void emitQuad(
      Matrix4f poseMatrix,
      VertexConsumer vertexConsumer,
      int packedLight,
      int packedOverlay,
      float red,
      float green,
      float blue,
      float alpha,
      float normalX,
      float normalY,
      float normalZ,
      float[] positions,
      float[] uvs
   ) {
      for (int i = 0; i < 4; i++) {
         int pIndex = i * 3;
         int uvIndex = i * 2;
         float uvU = uvs[uvIndex];
         float uvV = uvs[uvIndex + 1];
         this.emitVertex(
            poseMatrix,
            vertexConsumer,
            packedLight,
            packedOverlay,
            red,
            green,
            blue,
            alpha * this.waveAlphaForUv(uvU, uvV),
            positions[pIndex],
            positions[pIndex + 1],
            positions[pIndex + 2],
            uvU,
            uvV,
            normalX,
            normalY,
            normalZ
         );
      }
   }

   private void emitVertex(
      Matrix4f poseMatrix,
      VertexConsumer vertexConsumer,
      int packedLight,
      int packedOverlay,
      float red,
      float green,
      float blue,
      float alpha,
      float x,
      float y,
      float z,
      float uvU,
      float uvV,
      float normalX,
      float normalY,
      float normalZ
   ) {
      vertexConsumer.addVertex(poseMatrix, x, y, z)
         .setColor(red, green, blue, alpha)
         .setUv(uvU, uvV)
         .setOverlay(packedOverlay)
         .setLight(packedLight)
         .setNormal(normalX, normalY, normalZ);
   }

   private float[] resolveFaceTint(Direction direction) {
      return switch (direction) {
         case NORTH -> NORTH_TINT;
         case SOUTH -> SOUTH_TINT;
         case EAST -> EAST_TINT;
         case WEST -> WEST_TINT;
         case UP -> UP_TINT;
         case DOWN -> DOWN_TINT;
         default -> throw new IncompatibleClassChangeError();
      };
   }

   private float[] resolveVertexTint(int vertexIndex) {
      return switch (vertexIndex) {
         case 0 -> VERTEX0_TINT;
         case 1 -> VERTEX1_TINT;
         case 2 -> VERTEX2_TINT;
         default -> VERTEX3_TINT;
      };
   }

   private float waveAlphaForUv(float u, float v) {
      NanoGeoRenderer.OverlayWaveState wave = this.overlayWaveState;
      if (wave == null) {
         return 1.0F;
      } else {
         float phase = (u + v) * wave.frequency - wave.time * wave.speed;
         float wave01 = 0.5F + 0.5F * (float)Math.sin(phase * (float) (Math.PI * 2));
         float edgeStart = 1.0F - wave.width;
         float band = this.smoothstep(edgeStart, 1.0F, wave01);
         return this.clamp01(1.0F - wave.intensity * band);
      }
   }

   private float smoothstep(float edge0, float edge1, float value) {
      if (edge0 >= edge1) {
         return value >= edge1 ? 1.0F : 0.0F;
      } else {
         float t = this.clamp01((value - edge0) / (edge1 - edge0));
         return t * t * (3.0F - 2.0F * t);
      }
   }

   private float clamp01(float value) {
      return this.clamp(value, 0.0F, 1.0F);
   }

   private float clamp(float value, float min, float max) {
      if (value < min) {
         return min;
      } else {
         return value > max ? max : value;
      }
   }

   private Direction resolveFaceFilter() {
      String filter = NanoGeoClientConfig.nanoGeoDebugRenderOnlyFace();
      if (filter.isBlank()) {
         return null;
      } else {
         String var2 = filter.trim().toLowerCase(Locale.ROOT);

         return switch (var2) {
            case "north" -> Direction.NORTH;
            case "south" -> Direction.SOUTH;
            case "east" -> Direction.EAST;
            case "west" -> Direction.WEST;
            case "up" -> Direction.UP;
            case "down" -> Direction.DOWN;
            default -> null;
         };
      }
   }

   private float[] resolveSideTint(String boneName) {
      if (boneName.contains("Left")) {
         return LEFT_TINT;
      } else {
         return boneName.contains("Right") ? RIGHT_TINT : WHITE_TINT;
      }
   }

   private float transformNormalX(float m00, float m10, float m20, float x, float y, float z) {
      return m00 * x + m10 * y + m20 * z;
   }

   private float transformNormalY(float m01, float m11, float m21, float x, float y, float z) {
      return m01 * x + m11 * y + m21 * z;
   }

   private float transformNormalZ(float m02, float m12, float m22, float x, float y, float z) {
      return m02 * x + m12 * y + m22 * z;
   }

   private void renderBoneDebugMarkers(
      NanoGeoRenderContext context,
      PoseStack poseStack,
      NanoGeoBakedModel.NanoGeoBakedBone bone,
      int packedLight,
      boolean pivotMarkersEnabled,
      boolean axesMarkersEnabled,
      String focusBone
   ) {
      if (context.bufferSource() != null) {
         if (focusBone.isBlank() || focusBone.equals(bone.name())) {
            MultiBufferSource bufferSource = context.bufferSource();
            VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());
            Matrix4f pose = poseStack.last().pose();
            float pivotX = NanoGeoCoordinateSystem.modelUnits(bone.pivotX());
            float pivotY = NanoGeoCoordinateSystem.modelUnits(bone.pivotY());
            float pivotZ = NanoGeoCoordinateSystem.modelUnits(bone.pivotZ());
            if (pivotMarkersEnabled) {
               this.drawLine(lineConsumer, pose, pivotX - 0.03F, pivotY, pivotZ, pivotX + 0.03F, pivotY, pivotZ, 1.0F, 1.0F, 1.0F, packedLight);
               this.drawLine(lineConsumer, pose, pivotX, pivotY - 0.03F, pivotZ, pivotX, pivotY + 0.03F, pivotZ, 1.0F, 1.0F, 1.0F, packedLight);
               this.drawLine(lineConsumer, pose, pivotX, pivotY, pivotZ - 0.03F, pivotX, pivotY, pivotZ + 0.03F, 1.0F, 1.0F, 1.0F, packedLight);
            }

            if (axesMarkersEnabled) {
               this.drawLine(lineConsumer, pose, pivotX, pivotY, pivotZ, pivotX + 0.15F, pivotY, pivotZ, 1.0F, 0.15F, 0.15F, packedLight);
               this.drawLine(lineConsumer, pose, pivotX, pivotY, pivotZ, pivotX, pivotY + 0.15F, pivotZ, 0.15F, 1.0F, 0.15F, packedLight);
               this.drawLine(lineConsumer, pose, pivotX, pivotY, pivotZ, pivotX, pivotY, pivotZ + 0.15F, 0.15F, 0.5F, 1.0F, packedLight);
            }
         }
      }
   }

   private void drawLine(
      VertexConsumer consumer, Matrix4f pose, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, int packedLight
   ) {
      consumer.addVertex(pose, x1, y1, z1).setColor(red, green, blue, 1.0F).setLight(packedLight).setNormal(0.0F, 1.0F, 0.0F);
      consumer.addVertex(pose, x2, y2, z2).setColor(red, green, blue, 1.0F).setLight(packedLight).setNormal(0.0F, 1.0F, 0.0F);
   }

   private NanoGeoRenderer.NanoGeoBoneTransform safeTransform(NanoGeoRenderer.NanoGeoBoneTransform transform) {
      return transform == null ? NanoGeoRenderer.NanoGeoBoneTransform.IDENTITY : transform;
   }

   private boolean isFlagEnabled(boolean[] flagsByBoneId, int boneId, boolean defaultValue) {
      return flagsByBoneId == null ? defaultValue : boneId >= 0 && boneId < flagsByBoneId.length && flagsByBoneId[boneId];
   }

   private NanoGeoRenderer.NanoGeoBoneTransform transformById(NanoGeoRenderer.NanoGeoBoneTransform[] transformsByBoneId, int boneId) {
      if (transformsByBoneId != null && boneId >= 0 && boneId < transformsByBoneId.length) {
         NanoGeoRenderer.NanoGeoBoneTransform transform = transformsByBoneId[boneId];
         return transform == null ? NanoGeoRenderer.NanoGeoBoneTransform.IDENTITY : transform;
      } else {
         return NanoGeoRenderer.NanoGeoBoneTransform.IDENTITY;
      }
   }

   @FunctionalInterface
   public interface BoneTransformProvider {
      NanoGeoRenderer.NanoGeoBoneTransform transformFor(String var1);
   }

   @FunctionalInterface
   public interface BoneTraversalFilter {
      boolean shouldTraverse(NanoGeoBakedModel.NanoGeoBakedBone var1);
   }

   @FunctionalInterface
   public interface BoneVisibilityFilter {
      boolean isVisible(NanoGeoBakedModel.NanoGeoBakedBone var1);
   }

   @FunctionalInterface
   public interface IndexedBoneTransformProvider {
      NanoGeoRenderer.NanoGeoBoneTransform transformFor(NanoGeoBakedModel.NanoGeoBakedBone var1);
   }

   public record NanoGeoBoneTransform(float translateX, float translateY, float translateZ, float rotationX, float rotationY, float rotationZ) {
      public static final NanoGeoRenderer.NanoGeoBoneTransform IDENTITY = new NanoGeoRenderer.NanoGeoBoneTransform(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
   }

   private record OverlayWaveState(float intensity, float width, float speed, float frequency, float time) {
   }
}
