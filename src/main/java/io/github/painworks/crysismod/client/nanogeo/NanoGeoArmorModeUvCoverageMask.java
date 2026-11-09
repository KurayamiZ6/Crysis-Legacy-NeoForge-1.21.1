package io.github.painworks.crysismod.client.nanogeo;

import net.minecraft.resources.ResourceLocation;

final class NanoGeoArmorModeUvCoverageMask {
   private static final ResourceLocation ARMOR_MODEL = ResourceLocation.fromNamespaceAndPath("crysismod", "nanogeo/nanosuit_armor.json");

   private NanoGeoArmorModeUvCoverageMask() {
   }

   static NanoGeoBakedModel currentModel() {
      return NanoGeoBakedModelCache.INSTANCE.getBakedModelOrNull(ARMOR_MODEL);
   }

   static NanoGeoArmorModeUvCoverageMask.Coverage build(NanoGeoBakedModel model, int width, int height) {
      int safeWidth = Math.max(width, 1);
      int safeHeight = Math.max(height, 1);
      byte[] mask = new byte[safeWidth * safeHeight];
      int quadCount = 0;

      for (NanoGeoBakedModel.NanoGeoBakedBone rootBone : model.rootBones()) {
         quadCount += markBone(mask, safeWidth, safeHeight, rootBone);
      }

      int coveredPixelCount = 0;

      for (byte value : mask) {
         if (value != 0) {
            coveredPixelCount++;
         }
      }

      return new NanoGeoArmorModeUvCoverageMask.Coverage(mask, coveredPixelCount, quadCount);
   }

   private static int markBone(byte[] mask, int width, int height, NanoGeoBakedModel.NanoGeoBakedBone bone) {
      int quadCount = 0;

      for (NanoGeoBakedModel.NanoGeoBakedCube cube : bone.cubes()) {
         for (NanoGeoBakedModel.NanoGeoBakedQuad quad : cube.quads()) {
            markQuad(mask, width, height, quad.uvs());
            quadCount++;
         }
      }

      for (NanoGeoBakedModel.NanoGeoBakedBone child : bone.children()) {
         quadCount += markBone(mask, width, height, child);
      }

      return quadCount;
   }

   private static void markQuad(byte[] mask, int width, int height, float[] uvs) {
      if (uvs != null && uvs.length >= 8) {
         float minU = Float.POSITIVE_INFINITY;
         float minV = Float.POSITIVE_INFINITY;
         float maxU = Float.NEGATIVE_INFINITY;
         float maxV = Float.NEGATIVE_INFINITY;

         for (int index = 0; index + 1 < uvs.length; index += 2) {
            float u = uvs[index];
            float v = uvs[index + 1];
            minU = Math.min(minU, u);
            minV = Math.min(minV, v);
            maxU = Math.max(maxU, u);
            maxV = Math.max(maxV, v);
         }

         minU = clamp01(minU);
         minV = clamp01(minV);
         maxU = clamp01(maxU);
         maxV = clamp01(maxV);
         if (!(maxU <= minU) && !(maxV <= minV)) {
            int minX = clamp((int)Math.floor(minU * width), 0, width - 1);
            int minY = clamp((int)Math.floor(minV * height), 0, height - 1);
            int maxX = clamp((int)Math.ceil(maxU * width) - 1, 0, width - 1);
            int maxY = clamp((int)Math.ceil(maxV * height) - 1, 0, height - 1);

            for (int y = minY; y <= maxY; y++) {
               int rowOffset = y * width;

               for (int x = minX; x <= maxX; x++) {
                  mask[rowOffset + x] = 1;
               }
            }
         }
      }
   }

   private static float clamp01(float value) {
      return Math.max(0.0F, Math.min(value, 1.0F));
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(value, max));
   }

   record Coverage(byte[] mask, int coveredPixelCount, int quadCount) {
   }
}
