package io.github.painworks.crysismod.client.nanogeo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class NanoGeoModelLoader {
   public NanoGeoModel load(ResourceManager resourceManager, ResourceLocation modelResource) {
      try {
         Resource resource = (Resource)resourceManager.getResource(modelResource)
            .orElseThrow(() -> new IllegalStateException("Model resource not found: " + modelResource));

         NanoGeoModel var6;
         try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            var6 = this.parseModel(root, modelResource);
         }

         return var6;
      } catch (IOException var9) {
         throw new IllegalStateException("Failed to read model " + modelResource + ": " + var9.getMessage(), var9);
      } catch (RuntimeException var10) {
         throw new IllegalStateException("Failed to parse model " + modelResource + ": " + var10.getMessage(), var10);
      }
   }

   private NanoGeoModel parseModel(JsonObject root, ResourceLocation modelResource) {
      JsonArray geometries = this.requiredArray(root, "minecraft:geometry");
      if (geometries.isEmpty()) {
         throw new IllegalStateException("minecraft:geometry is empty");
      } else {
         JsonObject geometry = geometries.get(0).getAsJsonObject();
         JsonObject description = this.requiredObject(geometry, "description");
         String identifier = this.optionalString(description, "identifier", modelResource.toString());
         int textureWidth = this.requiredInt(description, "texture_width");
         int textureHeight = this.requiredInt(description, "texture_height");
         JsonArray bonesJson = this.requiredArray(geometry, "bones");
         Map<String, NanoGeoModelLoader.BoneDraft> drafts = new HashMap<>();

         for (JsonElement element : bonesJson) {
            JsonObject boneJson = element.getAsJsonObject();
            String name = this.requiredString(boneJson, "name");
            if (drafts.containsKey(name)) {
               throw new IllegalStateException("Duplicate bone name: " + name);
            }

            String parentName = this.optionalString(boneJson, "parent", null);
            float[] pivot = this.optionalVec3(boneJson, "pivot", 0.0F, 0.0F, 0.0F);
            float[] rotation = this.optionalVec3(boneJson, "rotation", 0.0F, 0.0F, 0.0F);
            List<NanoGeoCube> cubes = new ArrayList<>();
            if (boneJson.has("cubes")) {
               for (JsonElement cubeElement : this.requiredArray(boneJson, "cubes")) {
                  cubes.add(this.parseCube(cubeElement.getAsJsonObject(), pivot));
               }
            }

            drafts.put(name, new NanoGeoModelLoader.BoneDraft(name, parentName, pivot, rotation, cubes));
         }

         for (NanoGeoModelLoader.BoneDraft draft : drafts.values()) {
            if (draft.parentName != null && !drafts.containsKey(draft.parentName)) {
               throw new IllegalStateException("Bone " + draft.name + " references unknown parent " + draft.parentName);
            }
         }

         Map<String, NanoGeoBone> built = new HashMap<>();
         List<NanoGeoBone> roots = new ArrayList<>();

         for (NanoGeoModelLoader.BoneDraft draftx : drafts.values()) {
            if (draftx.parentName == null) {
               roots.add(this.buildBoneTree(draftx, drafts, built));
            }
         }

         return new NanoGeoModel(identifier, textureWidth, textureHeight, Collections.unmodifiableList(roots), Collections.unmodifiableMap(built));
      }
   }

   private NanoGeoBone buildBoneTree(NanoGeoModelLoader.BoneDraft draft, Map<String, NanoGeoModelLoader.BoneDraft> draftsByName, Map<String, NanoGeoBone> built) {
      NanoGeoBone cached = built.get(draft.name);
      if (cached != null) {
         return cached;
      } else {
         List<NanoGeoBone> children = new ArrayList<>();

         for (NanoGeoModelLoader.BoneDraft candidate : draftsByName.values()) {
            if (draft.name.equals(candidate.parentName)) {
               children.add(this.buildBoneTree(candidate, draftsByName, built));
            }
         }

         NanoGeoBone bone = new NanoGeoBone(
            draft.name,
            draft.parentName,
            draft.pivot[0],
            draft.pivot[1],
            draft.pivot[2],
            draft.rotation[0],
            draft.rotation[1],
            draft.rotation[2],
            Collections.unmodifiableList(draft.cubes),
            Collections.unmodifiableList(children)
         );
         built.put(draft.name, bone);
         return bone;
      }
   }

   private NanoGeoCube parseCube(JsonObject cubeJson, float[] inheritedPivot) {
      float[] origin = this.requiredVec3(cubeJson, "origin");
      float[] size = this.requiredVec3(cubeJson, "size");
      float[] pivot = this.optionalVec3(cubeJson, "pivot", inheritedPivot[0], inheritedPivot[1], inheritedPivot[2]);
      float[] rotation = this.optionalVec3(cubeJson, "rotation", 0.0F, 0.0F, 0.0F);
      float inflate = this.optionalFloat(cubeJson, "inflate", 0.0F);
      boolean mirror = this.optionalBoolean(cubeJson, "mirror", false);
      NanoGeoCube.NanoGeoUvMode uvMode = NanoGeoCube.NanoGeoUvMode.BOX;
      float boxU = 0.0F;
      float boxV = 0.0F;
      EnumMap<Direction, NanoGeoCube.NanoGeoFaceUv> faces = NanoGeoCube.emptyFaceUvMap();
      JsonElement uvElement = cubeJson.get("uv");
      if (uvElement == null) {
         throw new IllegalStateException("Cube is missing uv");
      } else {
         if (uvElement.isJsonArray()) {
            JsonArray arr = uvElement.getAsJsonArray();
            if (arr.size() < 2) {
               throw new IllegalStateException("Cube uv array requires at least 2 values");
            }

            boxU = arr.get(0).getAsFloat();
            boxV = arr.get(1).getAsFloat();
            uvMode = NanoGeoCube.NanoGeoUvMode.BOX;
         } else {
            if (!uvElement.isJsonObject()) {
               throw new IllegalStateException("Unsupported cube uv value");
            }

            uvMode = NanoGeoCube.NanoGeoUvMode.PER_FACE;
            JsonObject uvObj = uvElement.getAsJsonObject();
            this.readFaceUv(uvObj, "north", Direction.NORTH, faces);
            this.readFaceUv(uvObj, "south", Direction.SOUTH, faces);
            this.readFaceUv(uvObj, "east", Direction.EAST, faces);
            this.readFaceUv(uvObj, "west", Direction.WEST, faces);
            this.readFaceUv(uvObj, "up", Direction.UP, faces);
            this.readFaceUv(uvObj, "down", Direction.DOWN, faces);
         }

         return new NanoGeoCube(
            origin[0],
            origin[1],
            origin[2],
            size[0],
            size[1],
            size[2],
            pivot[0],
            pivot[1],
            pivot[2],
            rotation[0],
            rotation[1],
            rotation[2],
            inflate,
            mirror,
            uvMode,
            boxU,
            boxV,
            Collections.unmodifiableMap(faces)
         );
      }
   }

   private void readFaceUv(JsonObject uvObject, String faceKey, Direction direction, EnumMap<Direction, NanoGeoCube.NanoGeoFaceUv> target) {
      JsonElement faceElement = uvObject.get(faceKey);
      if (faceElement != null && faceElement.isJsonObject()) {
         JsonObject face = faceElement.getAsJsonObject();
         float[] uv = this.requiredVec2(face, "uv");
         float[] uvSize = this.requiredVec2(face, "uv_size");
         int uvRotation = this.normalizeUvRotationDegrees(this.optionalFloat(face, "uv_rotation", 0.0F));
         target.put(direction, new NanoGeoCube.NanoGeoFaceUv(uv[0], uv[1], uvSize[0], uvSize[1], uvRotation));
      }
   }

   private int normalizeUvRotationDegrees(float rawRotation) {
      int rounded = Math.round(rawRotation);
      int normalized = rounded % 360;
      if (normalized < 0) {
         normalized = -normalized;
      }

      int snapped = Math.round(normalized / 90.0F) * 90;
      return snapped % 360;
   }

   private JsonObject requiredObject(JsonObject object, String key) {
      JsonElement e = object.get(key);
      if (e != null && e.isJsonObject()) {
         return e.getAsJsonObject();
      } else {
         throw new IllegalStateException("Required object field missing: " + key);
      }
   }

   private JsonArray requiredArray(JsonObject object, String key) {
      JsonElement e = object.get(key);
      if (e != null && e.isJsonArray()) {
         return e.getAsJsonArray();
      } else {
         throw new IllegalStateException("Required array field missing: " + key);
      }
   }

   private String requiredString(JsonObject object, String key) {
      JsonElement e = object.get(key);
      if (e != null && e.isJsonPrimitive()) {
         return e.getAsString();
      } else {
         throw new IllegalStateException("Required string field missing: " + key);
      }
   }

   private String optionalString(JsonObject object, String key, String fallback) {
      JsonElement e = object.get(key);
      return e != null && e.isJsonPrimitive() ? e.getAsString() : fallback;
   }

   private int requiredInt(JsonObject object, String key) {
      JsonElement e = object.get(key);
      if (e != null && e.isJsonPrimitive()) {
         return e.getAsInt();
      } else {
         throw new IllegalStateException("Required integer field missing: " + key);
      }
   }

   private float optionalFloat(JsonObject object, String key, float fallback) {
      JsonElement e = object.get(key);
      return e != null && e.isJsonPrimitive() ? e.getAsFloat() : fallback;
   }

   private boolean optionalBoolean(JsonObject object, String key, boolean fallback) {
      JsonElement e = object.get(key);
      return e != null && e.isJsonPrimitive() ? e.getAsBoolean() : fallback;
   }

   private float[] requiredVec3(JsonObject object, String key) {
      JsonArray arr = this.requiredArray(object, key);
      if (arr.size() < 3) {
         throw new IllegalStateException(String.format(Locale.ROOT, "Field %s must contain 3 elements", key));
      } else {
         return new float[]{arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat()};
      }
   }

   private float[] optionalVec3(JsonObject object, String key, float x, float y, float z) {
      JsonElement e = object.get(key);
      if (e != null && e.isJsonArray()) {
         JsonArray arr = e.getAsJsonArray();
         return arr.size() < 3 ? new float[]{x, y, z} : new float[]{arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat()};
      } else {
         return new float[]{x, y, z};
      }
   }

   private float[] requiredVec2(JsonObject object, String key) {
      JsonArray arr = this.requiredArray(object, key);
      if (arr.size() < 2) {
         throw new IllegalStateException("Field " + key + " must contain 2 elements");
      } else {
         return new float[]{arr.get(0).getAsFloat(), arr.get(1).getAsFloat()};
      }
   }

   private static final class BoneDraft {
      private final String name;
      private final String parentName;
      private final float[] pivot;
      private final float[] rotation;
      private final List<NanoGeoCube> cubes;

      private BoneDraft(String name, String parentName, float[] pivot, float[] rotation, List<NanoGeoCube> cubes) {
         this.name = name;
         this.parentName = parentName;
         this.pivot = pivot;
         this.rotation = rotation;
         this.cubes = cubes;
      }
   }
}
