package io.github.painworks.crysismod.client.nanogeo;

import java.util.List;
import java.util.Map;

public final class NanoGeoModel {
   private final String identifier;
   private final int textureWidth;
   private final int textureHeight;
   private final List<NanoGeoBone> rootBones;
   private final Map<String, NanoGeoBone> bonesByName;

   public NanoGeoModel(String identifier, int textureWidth, int textureHeight, List<NanoGeoBone> rootBones, Map<String, NanoGeoBone> bonesByName) {
      this.identifier = identifier;
      this.textureWidth = textureWidth;
      this.textureHeight = textureHeight;
      this.rootBones = rootBones;
      this.bonesByName = bonesByName;
   }

   public String identifier() {
      return this.identifier;
   }

   public int textureWidth() {
      return this.textureWidth;
   }

   public int textureHeight() {
      return this.textureHeight;
   }

   public List<NanoGeoBone> rootBones() {
      return this.rootBones;
   }

   public Map<String, NanoGeoBone> bonesByName() {
      return this.bonesByName;
   }
}
