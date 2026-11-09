package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public final class NanoGeoBakedModelCache implements PreparableReloadListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final NanoGeoBakedModelCache INSTANCE = new NanoGeoBakedModelCache();
   private final NanoGeoModelLoader modelLoader = new NanoGeoModelLoader();
   private final Map<ResourceLocation, NanoGeoModel> parsedModels = new ConcurrentHashMap<>();
   private final Map<ResourceLocation, NanoGeoBakedModel> bakedModels = new ConcurrentHashMap<>();
   private volatile List<ResourceLocation> preloadModels = List.of();

   private NanoGeoBakedModelCache() {
   }

   public void setPreloadModels(List<ResourceLocation> modelResources) {
      this.preloadModels = List.copyOf(modelResources);
   }

   public Optional<NanoGeoBakedModel> getBakedModel(ResourceLocation modelResource) {
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MAP_LOOKUPS);
      return Optional.ofNullable(this.bakedModels.get(modelResource));
   }

   public NanoGeoBakedModel getBakedModelOrNull(ResourceLocation modelResource) {
      long sectionStart = NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.MODEL_CACHE_LOOKUP);
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MAP_LOOKUPS);
      NanoGeoBakedModel bakedModel = this.bakedModels.get(modelResource);
      if (bakedModel == null) {
         NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MODEL_CACHE_MISSES);
      } else {
         NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MODEL_CACHE_HITS);
      }

      NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.MODEL_CACHE_LOOKUP, sectionStart);
      return bakedModel;
   }

   public Optional<NanoGeoModel> getParsedModel(ResourceLocation modelResource) {
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MAP_LOOKUPS);
      return Optional.ofNullable(this.parsedModels.get(modelResource));
   }

   public void clear() {
      this.parsedModels.clear();
      this.bakedModels.clear();
   }

   public CompletableFuture<Void> reload(
      PreparationBarrier barrier,
      ResourceManager manager,
      ProfilerFiller prepareProfiler,
      ProfilerFiller applyProfiler,
      Executor backgroundExecutor,
      Executor gameExecutor
   ) {
      return CompletableFuture.runAsync(
            () -> {
               this.clear();

               for (ResourceLocation modelResource : this.preloadModels) {
                  NanoGeoModel parsed = this.modelLoader.load(manager, modelResource);
                  NanoGeoBakedModel baked = this.bake(parsed);
                  this.parsedModels.put(modelResource, parsed);
                  this.bakedModels.put(modelResource, baked);
                  if (NanoGeoClientConfig.nanoGeoLogModelLoad()) {
                     NanoGeoBakedModelCache.UvStats uvStats = this.collectUvStats(parsed);
                     LOGGER.info(
                        "[CrysisMod][NanoGeo] Loaded model {} (id={}, bones={}, texture={}x{}, cubes={}, perFaceUv={}, boxUv={})",
                        new Object[]{
                           modelResource,
                           parsed.identifier(),
                           parsed.bonesByName().size(),
                           parsed.textureWidth(),
                           parsed.textureHeight(),
                           uvStats.cubeCount(),
                           uvStats.perFaceCubeCount(),
                           uvStats.boxCubeCount()
                        }
                     );
                  }

                  if (NanoGeoClientConfig.nanoGeoDebugUv() && NanoGeoClientConfig.nanoGeoDebugUvLogCubes()) {
                     this.logUvCubeDiagnostics(modelResource, parsed);
                  }

                  if (NanoGeoClientConfig.nanoGeoDebugLogResources()) {
                     LOGGER.info(
                        "[CrysisMod][NanoGeo] Resource debug model={} identifier={} textureSize={}x{} bones={} rootBones={}",
                        new Object[]{
                           modelResource,
                           parsed.identifier(),
                           parsed.textureWidth(),
                           parsed.textureHeight(),
                           parsed.bonesByName().size(),
                           parsed.rootBones().size()
                        }
                     );
                  }
               }
            },
            backgroundExecutor
         )
         .thenCompose(barrier::wait)
         .thenRunAsync(() -> {}, gameExecutor);
   }

   private NanoGeoBakedModel bake(NanoGeoModel model) {
      Map<String, NanoGeoBakedModel.NanoGeoBakedBone> byName = new HashMap<>();
      List<NanoGeoBakedModel.NanoGeoBakedBone> rootBones = new ArrayList<>();
      int[] nextBoneId = new int[]{0};

      for (NanoGeoBone bone : model.rootBones()) {
         rootBones.add(this.bakeBone(model, bone, byName, nextBoneId));
      }

      return new NanoGeoBakedModel(model, Collections.unmodifiableList(rootBones), Collections.unmodifiableMap(byName), nextBoneId[0]);
   }

   private NanoGeoBakedModel.NanoGeoBakedBone bakeBone(
      NanoGeoModel model, NanoGeoBone bone, Map<String, NanoGeoBakedModel.NanoGeoBakedBone> byName, int[] nextBoneId
   ) {
      List<NanoGeoBakedModel.NanoGeoBakedCube> cubes = new ArrayList<>(bone.cubes().size());

      for (NanoGeoCube cube : bone.cubes()) {
         cubes.add(this.bakeCube(model, cube));
      }

      List<NanoGeoBakedModel.NanoGeoBakedBone> children = new ArrayList<>(bone.children().size());

      for (NanoGeoBone child : bone.children()) {
         children.add(this.bakeBone(model, child, byName, nextBoneId));
      }

      NanoGeoBakedModel.NanoGeoBakedBone bakedBone = new NanoGeoBakedModel.NanoGeoBakedBone(
         (int)(nextBoneId[0]++),
         bone.name(),
         bone.parentName(),
         NanoGeoCoordinateSystem.geoPivotX(bone.pivotX()),
         NanoGeoCoordinateSystem.geoPivotY(bone.pivotY()),
         NanoGeoCoordinateSystem.geoPivotZ(bone.pivotZ()),
         NanoGeoCoordinateSystem.geoBoneRotationXRad(bone.rotationX()),
         NanoGeoCoordinateSystem.geoBoneRotationYRad(bone.rotationY()),
         NanoGeoCoordinateSystem.geoBoneRotationZRad(bone.rotationZ()),
         Collections.unmodifiableList(cubes),
         Collections.unmodifiableList(children)
      );
      byName.put(bone.name(), bakedBone);
      return bakedBone;
   }

   private NanoGeoBakedModel.NanoGeoBakedCube bakeCube(NanoGeoModel model, NanoGeoCube cube) {
      float inflate = NanoGeoCoordinateSystem.modelUnits(cube.inflate());
      float originX = NanoGeoCoordinateSystem.modelUnits(-(cube.originX() + cube.sizeX()));
      float originY = NanoGeoCoordinateSystem.modelUnits(cube.originY());
      float originZ = NanoGeoCoordinateSystem.modelUnits(cube.originZ());
      float sizeX = NanoGeoCoordinateSystem.modelUnits(cube.sizeX());
      float sizeY = NanoGeoCoordinateSystem.modelUnits(cube.sizeY());
      float sizeZ = NanoGeoCoordinateSystem.modelUnits(cube.sizeZ());
      float x1 = originX - inflate;
      float y1 = originY - inflate;
      float z1 = originZ - inflate;
      float x2 = originX + sizeX + inflate;
      float y2 = originY + sizeY + inflate;
      float z2 = originZ + sizeZ + inflate;
      List<NanoGeoBakedModel.NanoGeoBakedQuad> quads = new ArrayList<>(6);
      this.addQuad(quads, this.bakeQuad(model, cube, Direction.WEST, x1, y1, z1, x2, y2, z2));
      this.addQuad(quads, this.bakeQuad(model, cube, Direction.EAST, x1, y1, z1, x2, y2, z2));
      this.addQuad(quads, this.bakeQuad(model, cube, Direction.NORTH, x1, y1, z1, x2, y2, z2));
      this.addQuad(quads, this.bakeQuad(model, cube, Direction.SOUTH, x1, y1, z1, x2, y2, z2));
      this.addQuad(quads, this.bakeQuad(model, cube, Direction.UP, x1, y1, z1, x2, y2, z2));
      this.addQuad(quads, this.bakeQuad(model, cube, Direction.DOWN, x1, y1, z1, x2, y2, z2));
      return new NanoGeoBakedModel.NanoGeoBakedCube(
         NanoGeoCoordinateSystem.geoPivotX(cube.pivotX()),
         NanoGeoCoordinateSystem.geoPivotY(cube.pivotY()),
         NanoGeoCoordinateSystem.geoPivotZ(cube.pivotZ()),
         NanoGeoCoordinateSystem.geoBoneRotationXRad(cube.rotationX()),
         NanoGeoCoordinateSystem.geoBoneRotationYRad(cube.rotationY()),
         NanoGeoCoordinateSystem.geoBoneRotationZRad(cube.rotationZ()),
         Collections.unmodifiableList(quads)
      );
   }

   private void addQuad(List<NanoGeoBakedModel.NanoGeoBakedQuad> quads, NanoGeoBakedModel.NanoGeoBakedQuad quad) {
      if (quad != null) {
         quads.add(quad);
      }
   }

   private NanoGeoBakedModel.NanoGeoBakedQuad bakeQuad(
      NanoGeoModel model, NanoGeoCube cube, Direction direction, float x1, float y1, float z1, float x2, float y2, float z2
   ) {
      NanoGeoBakedModelCache.FaceUv faceUv = this.resolveFaceUv(cube, direction);
      if (faceUv == null) {
         return null;
      } else {
         float[] pos = this.buildQuadPositions(direction, cube.mirror(), faceUv.source(), x1, y1, z1, x2, y2, z2);
         float[] uv = this.resolveQuadUvs(model, direction, faceUv, cube.mirror());
         return new NanoGeoBakedModel.NanoGeoBakedQuad(direction, direction.getStepX(), direction.getStepY(), direction.getStepZ(), pos, uv);
      }
   }

   private NanoGeoBakedModelCache.FaceUv resolveFaceUv(NanoGeoCube cube, Direction direction) {
      float sx = (float)Math.floor(cube.sizeX());
      float sy = (float)Math.floor(cube.sizeY());
      float sz = (float)Math.floor(cube.sizeZ());
      if (cube.uvMode() == NanoGeoCube.NanoGeoUvMode.PER_FACE) {
         NanoGeoCube.NanoGeoFaceUv faceUv = cube.faceUvs().get(direction);
         return faceUv == null
            ? null
            : new NanoGeoBakedModelCache.FaceUv(
               faceUv.u(), faceUv.v(), faceUv.uSize(), faceUv.vSize(), faceUv.uvRotation(), NanoGeoBakedModelCache.UvSource.PER_FACE
            );
      } else {
         float u = cube.boxUvU();
         float v = cube.boxUvV();

         return switch (direction) {
            case WEST -> new NanoGeoBakedModelCache.FaceUv(u + sz + sx, v + sz, sz, sy, 0, NanoGeoBakedModelCache.UvSource.BOX);
            case EAST -> new NanoGeoBakedModelCache.FaceUv(u, v + sz, sz, sy, 0, NanoGeoBakedModelCache.UvSource.BOX);
            case NORTH -> new NanoGeoBakedModelCache.FaceUv(u + sz, v + sz, sx, sy, 0, NanoGeoBakedModelCache.UvSource.BOX);
            case SOUTH -> new NanoGeoBakedModelCache.FaceUv(u + sz + sx + sz, v + sz, sx, sy, 0, NanoGeoBakedModelCache.UvSource.BOX);
            case UP -> new NanoGeoBakedModelCache.FaceUv(u + sz, v, sx, sz, 0, NanoGeoBakedModelCache.UvSource.BOX);
            case DOWN -> new NanoGeoBakedModelCache.FaceUv(u + sz + sx, v + sz, sx, -sz, 0, NanoGeoBakedModelCache.UvSource.BOX);
            default -> throw new IncompatibleClassChangeError();
         };
      }
   }

   private float[] resolveQuadUvs(NanoGeoModel model, Direction direction, NanoGeoBakedModelCache.FaceUv faceUv, boolean mirror) {
      float tw = Math.max(1.0F, (float)model.textureWidth());
      float th = Math.max(1.0F, (float)model.textureHeight());
      return this.normalizeFaceUv(faceUv, tw, th, mirror);
   }

   private float[] normalizeFaceUv(NanoGeoBakedModelCache.FaceUv faceUv, float tw, float th, boolean mirror) {
      float u0 = faceUv.u() / tw;
      float v0 = faceUv.v() / th;
      float u1 = (faceUv.u() + faceUv.uSize()) / tw;
      float v1 = (faceUv.v() + faceUv.vSize()) / th;
      if (!mirror) {
         float tmp = u0;
         u0 = u1;
         u1 = tmp;
      }

      return this.rotateUvCorners(u0, v0, u1, v1, faceUv.uvRotation());
   }

   private float[] rotateUvCorners(float u0, float v0, float u1, float v1, int uvRotationDegrees) {
      int rotation = this.normalizeUvRotation(uvRotationDegrees);

      return switch (rotation) {
         case 90 -> new float[]{u1, v0, u1, v1, u0, v1, u0, v0};
         case 180 -> new float[]{u1, v1, u0, v1, u0, v0, u1, v0};
         case 270 -> new float[]{u0, v1, u0, v0, u1, v0, u1, v1};
         default -> new float[]{u0, v0, u1, v0, u1, v1, u0, v1};
      };
   }

   private int normalizeUvRotation(int rawRotationDegrees) {
      int rotation = rawRotationDegrees % 360;
      if (rotation < 0) {
         rotation += 360;
      }

      return rotation;
   }

   private float[] buildQuadPositions(
      Direction direction,
      boolean mirror,
      NanoGeoBakedModelCache.UvSource uvSource,
      float x1Raw,
      float y1Raw,
      float z1Raw,
      float x2Raw,
      float y2Raw,
      float z2Raw
   ) {
      float x1 = Math.min(x1Raw, x2Raw);
      float x2 = Math.max(x1Raw, x2Raw);
      float y1 = Math.min(y1Raw, y2Raw);
      float y2 = Math.max(y1Raw, y2Raw);
      float z1 = Math.min(z1Raw, z2Raw);
      float z2 = Math.max(z1Raw, z2Raw);
      NanoGeoBakedModelCache.Vertex bottomLeftBack = new NanoGeoBakedModelCache.Vertex(x1, y1, z1);
      NanoGeoBakedModelCache.Vertex bottomRightBack = new NanoGeoBakedModelCache.Vertex(x1, y1, z2);
      NanoGeoBakedModelCache.Vertex topLeftBack = new NanoGeoBakedModelCache.Vertex(x1, y2, z1);
      NanoGeoBakedModelCache.Vertex topRightBack = new NanoGeoBakedModelCache.Vertex(x1, y2, z2);
      NanoGeoBakedModelCache.Vertex topLeftFront = new NanoGeoBakedModelCache.Vertex(x2, y2, z1);
      NanoGeoBakedModelCache.Vertex topRightFront = new NanoGeoBakedModelCache.Vertex(x2, y2, z2);
      NanoGeoBakedModelCache.Vertex bottomLeftFront = new NanoGeoBakedModelCache.Vertex(x2, y1, z1);
      NanoGeoBakedModelCache.Vertex bottomRightFront = new NanoGeoBakedModelCache.Vertex(x2, y1, z2);
      NanoGeoBakedModelCache.Vertex[] quadWest = new NanoGeoBakedModelCache.Vertex[]{topRightBack, topLeftBack, bottomLeftBack, bottomRightBack};
      NanoGeoBakedModelCache.Vertex[] quadEast = new NanoGeoBakedModelCache.Vertex[]{topLeftFront, topRightFront, bottomRightFront, bottomLeftFront};
      NanoGeoBakedModelCache.Vertex[] quadNorth = new NanoGeoBakedModelCache.Vertex[]{topLeftBack, topLeftFront, bottomLeftFront, bottomLeftBack};
      NanoGeoBakedModelCache.Vertex[] quadSouth = new NanoGeoBakedModelCache.Vertex[]{topRightFront, topRightBack, bottomRightBack, bottomRightFront};
      NanoGeoBakedModelCache.Vertex[] quadUp = new NanoGeoBakedModelCache.Vertex[]{topRightBack, topRightFront, topLeftFront, topLeftBack};
      NanoGeoBakedModelCache.Vertex[] quadDown = new NanoGeoBakedModelCache.Vertex[]{bottomLeftBack, bottomLeftFront, bottomRightFront, bottomRightBack};
      boolean swapUpDownForMirror = mirror && uvSource == NanoGeoBakedModelCache.UvSource.PER_FACE;

      NanoGeoBakedModelCache.Vertex[] selected = switch (direction) {
         case WEST -> mirror ? quadEast : quadWest;
         case EAST -> mirror ? quadWest : quadEast;
         case NORTH -> quadNorth;
         case SOUTH -> quadSouth;
         case UP -> swapUpDownForMirror ? quadDown : quadUp;
         case DOWN -> swapUpDownForMirror ? quadUp : quadDown;
         default -> throw new IncompatibleClassChangeError();
      };
      float[] out = new float[12];

      for (int i = 0; i < selected.length; i++) {
         int idx = i * 3;
         out[idx] = selected[i].x();
         out[idx + 1] = selected[i].y();
         out[idx + 2] = selected[i].z();
      }

      return out;
   }

   private NanoGeoBakedModelCache.UvStats collectUvStats(NanoGeoModel model) {
      int cubeCount = 0;
      int boxCount = 0;
      int perFaceCount = 0;

      for (NanoGeoBone root : model.rootBones()) {
         cubeCount += this.countBoneCubes(root);
         boxCount += this.countBoneCubesByUvMode(root, NanoGeoCube.NanoGeoUvMode.BOX);
         perFaceCount += this.countBoneCubesByUvMode(root, NanoGeoCube.NanoGeoUvMode.PER_FACE);
      }

      return new NanoGeoBakedModelCache.UvStats(cubeCount, boxCount, perFaceCount);
   }

   private int countBoneCubes(NanoGeoBone bone) {
      int count = bone.cubes().size();

      for (NanoGeoBone child : bone.children()) {
         count += this.countBoneCubes(child);
      }

      return count;
   }

   private int countBoneCubesByUvMode(NanoGeoBone bone, NanoGeoCube.NanoGeoUvMode mode) {
      int count = 0;

      for (NanoGeoCube cube : bone.cubes()) {
         if (cube.uvMode() == mode) {
            count++;
         }
      }

      for (NanoGeoBone child : bone.children()) {
         count += this.countBoneCubesByUvMode(child, mode);
      }

      return count;
   }

   private void logUvCubeDiagnostics(ResourceLocation modelResource, NanoGeoModel model) {
      int maxCubeLogs = 20;
      String focusBone = NanoGeoClientConfig.nanoGeoDebugLogUvForBone();
      int[] logged = new int[]{0};

      for (NanoGeoBone root : model.rootBones()) {
         this.logUvCubeDiagnostics(root, logged, 20, focusBone);
         if (logged[0] >= 20) {
            break;
         }
      }

      LOGGER.info(
         "[CrysisMod][NanoGeo][UV] {} debug cubes logged for {}{}",
         new Object[]{logged[0], modelResource, focusBone.isBlank() ? "" : " (boneFilter=" + focusBone + ")"}
      );
   }

   private void logUvCubeDiagnostics(NanoGeoBone bone, int[] logged, int maxCubeLogs, String focusBone) {
      boolean boneAllowed = focusBone.isBlank() || focusBone.equals(bone.name());
      if (boneAllowed) {
         for (NanoGeoCube cube : bone.cubes()) {
            if (logged[0] >= maxCubeLogs) {
               return;
            }

            Map<String, String> faceUvRectangles = new LinkedHashMap<>();

            for (Direction direction : Direction.values()) {
               NanoGeoBakedModelCache.FaceUv faceUv = this.resolveFaceUv(cube, direction);
               if (faceUv != null) {
                  faceUvRectangles.put(
                     direction.getName(),
                     String.format(
                        "u=%.3f v=%.3f w=%.3f h=%.3f rot=%d src=%s",
                        faceUv.u(),
                        faceUv.v(),
                        faceUv.uSize(),
                        faceUv.vSize(),
                        faceUv.uvRotation(),
                        faceUv.source()
                     )
                  );
               } else {
                  faceUvRectangles.put(direction.getName(), "missing");
               }
            }

            LOGGER.info(
               "[CrysisMod][NanoGeo][UV] bone={} origin=({},{},{}) size=({},{},{}) uvMode={} mirror={} inflate={} faces={}",
               new Object[]{
                  bone.name(),
                  cube.originX(),
                  cube.originY(),
                  cube.originZ(),
                  cube.sizeX(),
                  cube.sizeY(),
                  cube.sizeZ(),
                  cube.uvMode(),
                  cube.mirror(),
                  cube.inflate(),
                  faceUvRectangles
               }
            );
            logged[0]++;
         }
      }

      for (NanoGeoBone child : bone.children()) {
         this.logUvCubeDiagnostics(child, logged, maxCubeLogs, focusBone);
         if (logged[0] >= maxCubeLogs) {
            return;
         }
      }
   }

   private record FaceUv(float u, float v, float uSize, float vSize, int uvRotation, NanoGeoBakedModelCache.UvSource source) {
   }

   private static enum UvSource {
      BOX,
      PER_FACE;
   }

   private record UvStats(int cubeCount, int boxCubeCount, int perFaceCubeCount) {
   }

   private record Vertex(float x, float y, float z) {
   }
}
