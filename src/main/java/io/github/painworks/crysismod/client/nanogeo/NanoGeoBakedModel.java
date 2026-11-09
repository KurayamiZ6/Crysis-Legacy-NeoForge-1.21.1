package io.github.painworks.crysismod.client.nanogeo;

import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;

public final class NanoGeoBakedModel {
   private final NanoGeoModel sourceModel;
   private final List<NanoGeoBakedModel.NanoGeoBakedBone> rootBones;
   private final Map<String, NanoGeoBakedModel.NanoGeoBakedBone> bakedBonesByName;
   private final int boneCount;

   public NanoGeoBakedModel(
      NanoGeoModel sourceModel,
      List<NanoGeoBakedModel.NanoGeoBakedBone> rootBones,
      Map<String, NanoGeoBakedModel.NanoGeoBakedBone> bakedBonesByName,
      int boneCount
   ) {
      this.sourceModel = sourceModel;
      this.rootBones = rootBones;
      this.bakedBonesByName = bakedBonesByName;
      this.boneCount = boneCount;
   }

   public NanoGeoModel sourceModel() {
      return this.sourceModel;
   }

   public List<NanoGeoBakedModel.NanoGeoBakedBone> rootBones() {
      return this.rootBones;
   }

   public Map<String, NanoGeoBakedModel.NanoGeoBakedBone> bakedBonesByName() {
      return this.bakedBonesByName;
   }

   public int boneCount() {
      return this.boneCount;
   }

   public record NanoGeoBakedBone(
      int boneId,
      String name,
      String parentName,
      float pivotX,
      float pivotY,
      float pivotZ,
      float rotationX,
      float rotationY,
      float rotationZ,
      List<NanoGeoBakedModel.NanoGeoBakedCube> cubes,
      List<NanoGeoBakedModel.NanoGeoBakedBone> children
   ) {
   }

   public record NanoGeoBakedCube(
      float pivotX, float pivotY, float pivotZ, float rotationX, float rotationY, float rotationZ, List<NanoGeoBakedModel.NanoGeoBakedQuad> quads
   ) {
      public boolean hasRotation() {
         return this.rotationX != 0.0F || this.rotationY != 0.0F || this.rotationZ != 0.0F;
      }
   }

   public record NanoGeoBakedQuad(Direction direction, float normalX, float normalY, float normalZ, float[] positions, float[] uvs) {
   }
}
