package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import org.slf4j.Logger;

public final class NanoGeoArmorRenderer extends HumanoidModel<LivingEntity> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation MODEL_RESOURCE = ResourceLocation.fromNamespaceAndPath("crysismod", "nanogeo/nanosuit_armor.json");
   private static final ResourceLocation TEXTURE_RESOURCE = NanoGeoArmorTextures.BASE_ARMOR;
   private final NanoGeoRenderer renderer = new NanoGeoRenderer();
   private EquipmentSlot currentSlot = EquipmentSlot.CHEST;
   private String currentArmorTexture = TEXTURE_RESOURCE.toString();
   private boolean loggedCacheHit;
   private boolean loggedCacheMiss;
   private boolean loggedChestOnlyDebugMode;
   private boolean loggedGeoArmorTransform;
   private boolean loggedResourceSelection;
   private boolean loggedFastPathEnabled;
   private boolean loggedGenericPathEnabled;
   private boolean fpvArmOnlyMode;
   private boolean fullModelRenderOverride;
   private boolean suppressRender;
   private HumanoidArm fpvArm = HumanoidArm.RIGHT;
   private final Map<EquipmentSlot, Boolean> slotLogDone = new EnumMap<>(EquipmentSlot.class);
   private final Map<HumanoidArm, Boolean> fpvArmLogDone = new EnumMap<>(HumanoidArm.class);
   private final Map<EquipmentSlot, NanoGeoArmorRenderer.SlotVisibility> slotVisibilityCache = new EnumMap<>(EquipmentSlot.class);
   private final Map<HumanoidArm, NanoGeoArmorRenderer.SlotVisibility> fpvArmVisibilityCache = new EnumMap<>(HumanoidArm.class);
   private final Map<NanoGeoHumanoidBoneMapper.HumanoidPart, NanoGeoArmorRenderer.BonePose> partPoses = new EnumMap<>(
      NanoGeoHumanoidBoneMapper.HumanoidPart.class
   );
   private NanoGeoRenderer.NanoGeoBoneTransform[] frameBoneTransformsById = new NanoGeoRenderer.NanoGeoBoneTransform[0];
   private NanoGeoArmorRenderer.ResolvedBoneBinding[] resolvedBoneBindings = new NanoGeoArmorRenderer.ResolvedBoneBinding[0];
   private boolean[] activeVisibilityByBoneId = new boolean[0];
   private boolean[] activeSubtreeByBoneId = new boolean[0];
   private NanoGeoBakedModel.NanoGeoBakedBone[] activeTraversableRoots = new NanoGeoBakedModel.NanoGeoBakedBone[0];
   private NanoGeoBakedModel.NanoGeoBakedBone[][] activeTraversableChildrenByBoneId = new NanoGeoBakedModel.NanoGeoBakedBone[0][];
   private final Set<String> loggedBoneTransformNames = new HashSet<>();
   private NanoGeoBakedModel currentBakedModel;
   private NanoGeoBakedModel visibilityCacheModel;
   private NanoGeoBakedModel bindingCacheModel;

   public NanoGeoArmorRenderer() {
      super(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
      this.young = false;

      for (NanoGeoHumanoidBoneMapper.HumanoidPart part : NanoGeoHumanoidBoneMapper.HumanoidPart.values()) {
         this.partPoses.put(part, new NanoGeoArmorRenderer.BonePose());
      }
   }

   public void prepForRender(LivingEntity livingEntity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> baseModel) {
      this.currentSlot = slot;
      this.updateBasePoses(baseModel);
      this.resolveArmorTextureForLogs(livingEntity, stack, slot);
   }

   public void setSuppressRender(boolean suppressRender) {
      this.suppressRender = suppressRender;
   }

   public void beginFirstPersonArmOnly(HumanoidModel<?> baseModel, ModelPart armPart, HumanoidArm arm) {
      this.fpvArmOnlyMode = baseModel != null && armPart != null && arm != null;
      this.fpvArm = arm == null ? HumanoidArm.RIGHT : arm;
   }

   public void endFirstPersonArmOnly() {
      this.fpvArmOnlyMode = false;
      this.fpvArm = HumanoidArm.RIGHT;
   }

   public void beginFullModelRenderOverride() {
      this.fullModelRenderOverride = true;
   }

   public void endFullModelRenderOverride() {
      this.fullModelRenderOverride = false;
   }

   public void beginArmorModeOverlayWave(boolean enabled, float intensity, float width, float speed, float frequency, float time) {
      this.renderer.beginOverlayWave(enabled, intensity, width, speed, frequency, time);
   }

   public void endArmorModeOverlayWave() {
      this.renderer.endOverlayWave();
   }

   @Override
   public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
      this.renderToBuffer(
         poseStack,
         vertexConsumer,
         packedLight,
         packedOverlay,
         ARGB32.red(color) / 255.0F,
         ARGB32.green(color) / 255.0F,
         ARGB32.blue(color) / 255.0F,
         ARGB32.alpha(color) / 255.0F
      );
   }

   public void renderToBuffer(
      PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha
   ) {
      if (!this.suppressRender) {
         boolean ownedProfilerFrame = NanoGeoProfiler.INSTANCE.beginFrameIfNeeded(NanoGeoProfiler.PassType.ARMOR);
         long renderSection = NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.ARMOR_RENDERER_ENTRY);
         NanoGeoBakedModel bakedModel = NanoGeoBakedModelCache.INSTANCE.getBakedModelOrNull(MODEL_RESOURCE);
         if (bakedModel == null) {
            if (NanoGeoClientConfig.nanoGeoLogModelLoad() && !this.loggedCacheMiss) {
               this.loggedCacheMiss = true;
               LOGGER.warn("[CrysisMod][NanoGeo] Armor model cache miss for {} (renderer=NanoGeo experimental)", MODEL_RESOURCE);
            }

            NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.ARMOR_RENDERER_ENTRY, renderSection);
            NanoGeoProfiler.INSTANCE.endFrameIfOwned(ownedProfilerFrame);
         } else {
            this.currentBakedModel = bakedModel;
            this.ensureResolvedBoneBindings(bakedModel);
            if (NanoGeoClientConfig.nanoGeoLogModelLoad() && !this.loggedCacheHit) {
               this.loggedCacheHit = true;
               LOGGER.info("[CrysisMod][NanoGeo] Armor model cache hit for {} texture={} (renderer=NanoGeo experimental)", MODEL_RESOURCE, TEXTURE_RESOURCE);
            }

            if ((NanoGeoClientConfig.nanoGeoLogModelLoad() || NanoGeoClientConfig.nanoGeoDebugLogResources()) && !this.loggedResourceSelection) {
               this.loggedResourceSelection = true;
               LOGGER.info(
                  "[CrysisMod][NanoGeo] Experimental armor resources model={} texture(item)={} texture(rendererFallback)={}",
                  new Object[]{MODEL_RESOURCE, this.currentArmorTexture, TEXTURE_RESOURCE}
               );
            }

            boolean chestOnlyDebug = NanoGeoClientConfig.nanoGeoDebugArmorChestOnlyFullModel();
            if (!this.fpvArmOnlyMode && this.fullModelRenderOverride) {
               long poseSection = NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.POSE_MAPPING);
               this.prepareBoneTransformCache();
               NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.POSE_MAPPING, poseSection);
               poseStack.pushPose();
               NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_PUSH);
               this.applyGeoGlobalArmorTransform(poseStack);
               this.renderer
                  .renderStatic(
                     bakedModel,
                     new NanoGeoRenderContext(poseStack, null, packedLight, packedOverlay),
                     vertexConsumer,
                     red,
                     green,
                     blue,
                     alpha,
                     bone -> true,
                     bone -> true,
                     this::boneTransformForArmorPose
                  );
               poseStack.popPose();
               NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_POP);
               NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.ARMOR_RENDERER_ENTRY, renderSection);
               NanoGeoProfiler.INSTANCE.endFrameIfOwned(ownedProfilerFrame);
            } else {
               long slotVisibilitySection = NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.SLOT_VISIBILITY);
               NanoGeoArmorRenderer.SlotVisibility slotVisibility = this.fpvArmOnlyMode
                  ? this.visibilityForFirstPersonArm(this.fpvArm, bakedModel)
                  : this.visibilityForSlot(this.currentSlot, bakedModel);
               NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.SLOT_VISIBILITY, slotVisibilitySection);
               this.activeVisibilityByBoneId = slotVisibility.visibleById;
               this.activeSubtreeByBoneId = slotVisibility.subtreeById;
               this.activeTraversableRoots = slotVisibility.traversableRoots;
               this.activeTraversableChildrenByBoneId = slotVisibility.traversableChildrenByBoneId;
               if (!this.fpvArmOnlyMode && chestOnlyDebug && this.currentSlot != EquipmentSlot.CHEST) {
                  NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.ARMOR_RENDERER_ENTRY, renderSection);
                  NanoGeoProfiler.INSTANCE.endFrameIfOwned(ownedProfilerFrame);
               } else {
                  if ((NanoGeoClientConfig.nanoGeoLogModelLoad() || NanoGeoClientConfig.nanoGeoDebugLogVisibleBones())
                     && this.shouldLogVisibilityForCurrentMode()) {
                     LOGGER.info(
                        "[CrysisMod][NanoGeo] mode={} target={} visibleBones={}",
                        new Object[]{
                           this.fpvArmOnlyMode ? "FIRST_PERSON_ARMS" : "THIRD_PERSON_ARMOR",
                           this.fpvArmOnlyMode ? this.fpvArm : this.currentSlot,
                           this.visibleBoneNamesForLog(bakedModel, slotVisibility.visibleById)
                        }
                     );
                  }

                  long poseSection = NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.POSE_MAPPING);
                  this.prepareBoneTransformCache();
                  NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.POSE_MAPPING, poseSection);
                  if (!this.fpvArmOnlyMode && chestOnlyDebug) {
                     if (NanoGeoClientConfig.nanoGeoLogModelLoad() && !this.loggedChestOnlyDebugMode) {
                        this.loggedChestOnlyDebugMode = true;
                        LOGGER.info("[CrysisMod][NanoGeo] Debug armor mode enabled: CHEST slot renders full model; HEAD/LEGS/FEET skipped");
                     }

                     poseStack.pushPose();
                     NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_PUSH);
                     this.applyGeoGlobalArmorTransform(poseStack);
                     this.renderer
                        .renderStatic(
                           bakedModel,
                           new NanoGeoRenderContext(poseStack, null, packedLight, packedOverlay),
                           vertexConsumer,
                           red,
                           green,
                           blue,
                           alpha,
                           bone -> true,
                           bone -> true,
                           this::boneTransformForArmorPose
                        );
                     poseStack.popPose();
                     NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_POP);
                     NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.ARMOR_RENDERER_ENTRY, renderSection);
                     NanoGeoProfiler.INSTANCE.endFrameIfOwned(ownedProfilerFrame);
                  } else {
                     poseStack.pushPose();
                     NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_PUSH);
                     this.applyGeoGlobalArmorTransform(poseStack);
                     if (this.shouldUseFastIndexedPath()) {
                        if ((NanoGeoClientConfig.nanoGeoLogModelLoad() || NanoGeoClientConfig.nanoGeoDebugPerfEnabled()) && !this.loggedFastPathEnabled) {
                           this.loggedFastPathEnabled = true;
                           LOGGER.info("[CrysisMod][NanoGeo] Armor renderer using fast indexed path");
                        }

                        this.renderer
                           .renderStaticIndexedPlanned(
                              bakedModel,
                              poseStack,
                              null,
                              packedLight,
                              packedOverlay,
                              vertexConsumer,
                              red,
                              green,
                              blue,
                              alpha,
                              this.activeVisibilityByBoneId,
                              this.activeTraversableRoots,
                              this.activeTraversableChildrenByBoneId,
                              this.frameBoneTransformsById
                           );
                     } else {
                        if ((NanoGeoClientConfig.nanoGeoLogModelLoad() || NanoGeoClientConfig.nanoGeoDebugPerfEnabled()) && !this.loggedGenericPathEnabled) {
                           this.loggedGenericPathEnabled = true;
                           LOGGER.info("[CrysisMod][NanoGeo] Armor renderer using generic debug-capable path");
                        }

                        this.renderer
                           .renderStatic(
                              bakedModel,
                              new NanoGeoRenderContext(poseStack, null, packedLight, packedOverlay),
                              vertexConsumer,
                              red,
                              green,
                              blue,
                              alpha,
                              this::isVisibleByActiveSlot,
                              this::shouldTraverseActiveSubtree,
                              this::boneTransformForArmorPose
                           );
                     }

                     poseStack.popPose();
                     NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.POSESTACK_POP);
                     NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.ARMOR_RENDERER_ENTRY, renderSection);
                     NanoGeoProfiler.INSTANCE.endFrameIfOwned(ownedProfilerFrame);
                  }
               }
            }
         }
      }
   }

   private NanoGeoArmorRenderer.SlotVisibility visibilityForSlot(EquipmentSlot slot, NanoGeoBakedModel bakedModel) {
      if (bakedModel == null) {
         return NanoGeoArmorRenderer.SlotVisibility.EMPTY;
      } else {
         NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MAP_LOOKUPS);
         if (this.visibilityCacheModel != bakedModel) {
            this.visibilityCacheModel = bakedModel;
            this.slotVisibilityCache.clear();
            this.fpvArmVisibilityCache.clear();
            this.fpvArmLogDone.clear();
         }

         NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MAP_LOOKUPS);
         return this.slotVisibilityCache.computeIfAbsent(slot, targetSlot -> this.buildVisibilityForSlot(targetSlot, bakedModel));
      }
   }

   private NanoGeoArmorRenderer.SlotVisibility visibilityForFirstPersonArm(HumanoidArm arm, NanoGeoBakedModel bakedModel) {
      if (bakedModel == null) {
         return NanoGeoArmorRenderer.SlotVisibility.EMPTY;
      } else {
         NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MAP_LOOKUPS);
         if (this.visibilityCacheModel != bakedModel) {
            this.visibilityCacheModel = bakedModel;
            this.slotVisibilityCache.clear();
            this.fpvArmVisibilityCache.clear();
            this.fpvArmLogDone.clear();
         }

         NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MAP_LOOKUPS);
         return this.fpvArmVisibilityCache.computeIfAbsent(arm, targetArm -> this.buildVisibilityForFirstPersonArm(targetArm, bakedModel));
      }
   }

   private NanoGeoArmorRenderer.SlotVisibility buildVisibilityForSlot(EquipmentSlot slot, NanoGeoBakedModel bakedModel) {
      Set<String> anchors = NanoGeoHumanoidBoneMapper.visibleBonesForSlot(slot);
      return this.buildVisibilityForAnchors(anchors, bakedModel, "slot:" + slot);
   }

   private NanoGeoArmorRenderer.SlotVisibility buildVisibilityForFirstPersonArm(HumanoidArm arm, NanoGeoBakedModel bakedModel) {
      Set<String> anchors = arm == HumanoidArm.RIGHT
         ? NanoGeoHumanoidBoneMapper.visibleBonesForFirstPersonRightArm()
         : NanoGeoHumanoidBoneMapper.visibleBonesForFirstPersonLeftArm();
      return this.buildVisibilityForAnchors(anchors, bakedModel, "fpv:" + arm);
   }

   private NanoGeoArmorRenderer.SlotVisibility buildVisibilityForAnchors(Set<String> anchors, NanoGeoBakedModel bakedModel, String debugContext) {
      int boneCount = Math.max(0, bakedModel.boneCount());
      if (anchors.isEmpty()) {
         boolean[] empty = new boolean[boneCount];
         NanoGeoBakedModel.NanoGeoBakedBone[][] noChildren = new NanoGeoBakedModel.NanoGeoBakedBone[boneCount][];

         for (int i = 0; i < noChildren.length; i++) {
            noChildren[i] = NanoGeoArmorRenderer.SlotVisibility.EMPTY_CHILDREN;
         }

         return new NanoGeoArmorRenderer.SlotVisibility(empty, empty, NanoGeoArmorRenderer.SlotVisibility.EMPTY_CHILDREN, noChildren);
      } else {
         if (NanoGeoClientConfig.nanoGeoDebugFpvVisibleBones() && this.fpvArmOnlyMode) {
            LOGGER.info("[CrysisMod][NanoGeo][FPV Arms] visible anchor context={} anchors={}", debugContext, anchors);
         }

         boolean[] visible = new boolean[boneCount];

         for (String anchor : anchors) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.STRING_BONE_LOOKUPS);
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MAP_LOOKUPS);
            NanoGeoBakedModel.NanoGeoBakedBone root = bakedModel.bakedBonesByName().get(anchor);
            if (root != null) {
               this.collectBoneAndDescendants(root, visible);
            }
         }

         boolean[] subtree = new boolean[boneCount];

         for (NanoGeoBakedModel.NanoGeoBakedBone root : bakedModel.rootBones()) {
            this.markSubtreeVisibility(root, visible, subtree);
         }

         NanoGeoBakedModel.NanoGeoBakedBone[][] traversableChildrenById = new NanoGeoBakedModel.NanoGeoBakedBone[boneCount][];
         List<NanoGeoBakedModel.NanoGeoBakedBone> traversableRoots = new ArrayList<>();

         for (NanoGeoBakedModel.NanoGeoBakedBone root : bakedModel.rootBones()) {
            int rootId = root.boneId();
            if (rootId >= 0 && rootId < subtree.length && subtree[rootId]) {
               traversableRoots.add(root);
               this.buildTraversableChildren(root, subtree, traversableChildrenById);
            }
         }

         for (int i = 0; i < traversableChildrenById.length; i++) {
            if (traversableChildrenById[i] == null) {
               traversableChildrenById[i] = NanoGeoArmorRenderer.SlotVisibility.EMPTY_CHILDREN;
            }
         }

         return new NanoGeoArmorRenderer.SlotVisibility(
            visible, subtree, traversableRoots.toArray(new NanoGeoBakedModel.NanoGeoBakedBone[0]), traversableChildrenById
         );
      }
   }

   private void buildTraversableChildren(
      NanoGeoBakedModel.NanoGeoBakedBone parent, boolean[] subtreeById, NanoGeoBakedModel.NanoGeoBakedBone[][] traversableChildrenById
   ) {
      List<NanoGeoBakedModel.NanoGeoBakedBone> children = new ArrayList<>();

      for (NanoGeoBakedModel.NanoGeoBakedBone child : parent.children()) {
         int childId = child.boneId();
         if (childId >= 0 && childId < subtreeById.length && subtreeById[childId]) {
            children.add(child);
            this.buildTraversableChildren(child, subtreeById, traversableChildrenById);
         }
      }

      int parentId = parent.boneId();
      if (parentId >= 0 && parentId < traversableChildrenById.length) {
         traversableChildrenById[parentId] = children.isEmpty()
            ? NanoGeoArmorRenderer.SlotVisibility.EMPTY_CHILDREN
            : children.toArray(new NanoGeoBakedModel.NanoGeoBakedBone[0]);
      }
   }

   private void collectBoneAndDescendants(NanoGeoBakedModel.NanoGeoBakedBone bone, boolean[] target) {
      int boneId = bone.boneId();
      if (boneId >= 0 && boneId < target.length) {
         target[boneId] = true;
      }

      for (NanoGeoBakedModel.NanoGeoBakedBone child : bone.children()) {
         this.collectBoneAndDescendants(child, target);
      }
   }

   private boolean isVisibleByActiveSlot(NanoGeoBakedModel.NanoGeoBakedBone bone) {
      int boneId = bone.boneId();
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.VISIBLE_BONE_LOOKUPS);
      return boneId >= 0 && boneId < this.activeVisibilityByBoneId.length && this.activeVisibilityByBoneId[boneId];
   }

   private boolean shouldTraverseActiveSubtree(NanoGeoBakedModel.NanoGeoBakedBone bone) {
      int boneId = bone.boneId();
      NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.VISIBLE_BONE_LOOKUPS);
      return boneId >= 0 && boneId < this.activeSubtreeByBoneId.length && this.activeSubtreeByBoneId[boneId];
   }

   private boolean markSubtreeVisibility(NanoGeoBakedModel.NanoGeoBakedBone bone, boolean[] visibleById, boolean[] subtreeById) {
      int boneId = bone.boneId();
      boolean visibleSelf = boneId >= 0 && boneId < visibleById.length && visibleById[boneId];
      boolean anyVisible = visibleSelf;

      for (NanoGeoBakedModel.NanoGeoBakedBone child : bone.children()) {
         anyVisible |= this.markSubtreeVisibility(child, visibleById, subtreeById);
      }

      if (boneId >= 0 && boneId < subtreeById.length) {
         subtreeById[boneId] = anyVisible;
      }

      return anyVisible;
   }

   private List<String> visibleBoneNamesForLog(NanoGeoBakedModel bakedModel, boolean[] visibleById) {
      if (visibleById.length == 0) {
         return List.of();
      } else {
         String[] byId = new String[visibleById.length];

         for (NanoGeoBakedModel.NanoGeoBakedBone bone : bakedModel.bakedBonesByName().values()) {
            int boneId = bone.boneId();
            if (boneId >= 0 && boneId < byId.length) {
               byId[boneId] = bone.name();
            }
         }

         List<String> visible = new ArrayList<>();

         for (int i = 0; i < visibleById.length; i++) {
            if (visibleById[i] && byId[i] != null) {
               visible.add(byId[i]);
            }
         }

         return visible;
      }
   }

   private boolean shouldLogVisibilityForCurrentMode() {
      if (this.fpvArmOnlyMode) {
         if (!NanoGeoClientConfig.nanoGeoDebugFpvVisibleBones()) {
            this.fpvArmLogDone.clear();
            return false;
         } else if (this.fpvArmLogDone.containsKey(this.fpvArm)) {
            return false;
         } else {
            this.fpvArmLogDone.put(this.fpvArm, true);
            return true;
         }
      } else if (this.slotLogDone.containsKey(this.currentSlot)) {
         return false;
      } else {
         this.slotLogDone.put(this.currentSlot, true);
         return true;
      }
   }

   private void updateBasePoses(HumanoidModel<?> baseModel) {
      this.resetPoses();
      if (!NanoGeoClientConfig.nanoGeoDebugFreezePlayerPose()) {
         if (baseModel != null) {
            this.applyModelPartPose(this.partPoses.get(NanoGeoHumanoidBoneMapper.HumanoidPart.HEAD), baseModel.head, 0.0F, 0.0F);
            this.applyModelPartPose(this.partPoses.get(NanoGeoHumanoidBoneMapper.HumanoidPart.BODY), baseModel.body, 0.0F, 0.0F);
            this.applyModelPartPose(this.partPoses.get(NanoGeoHumanoidBoneMapper.HumanoidPart.RIGHT_ARM), baseModel.rightArm, 5.0F, 2.0F);
            this.applyModelPartPose(this.partPoses.get(NanoGeoHumanoidBoneMapper.HumanoidPart.LEFT_ARM), baseModel.leftArm, -5.0F, 2.0F);
            this.applyModelPartPose(this.partPoses.get(NanoGeoHumanoidBoneMapper.HumanoidPart.RIGHT_LEG), baseModel.rightLeg, 2.0F, 12.0F);
            this.applyModelPartPose(this.partPoses.get(NanoGeoHumanoidBoneMapper.HumanoidPart.LEFT_LEG), baseModel.leftLeg, -2.0F, 12.0F);
         }
      }
   }

   private void applyModelPartPose(NanoGeoArmorRenderer.BonePose target, ModelPart modelPart, float xOffset, float yBase) {
      if (modelPart == null) {
         target.reset();
      } else {
         target.set(modelPart.x + xOffset, yBase - modelPart.y, modelPart.z, modelPart.xRot, modelPart.yRot, modelPart.zRot);
      }
   }

   private void applyGeoGlobalArmorTransform(PoseStack poseStack) {
      poseStack.translate(0.0F, 1.5F, 0.0F);
      poseStack.scale(-1.0F, -1.0F, 1.0F);
      if (NanoGeoClientConfig.nanoGeoLogModelLoad() && !this.loggedGeoArmorTransform) {
         this.loggedGeoArmorTransform = true;
         LOGGER.info("[CrysisMod][NanoGeo] Applying Geo armor transform: translate(0,1.5,0) + scale(-1,-1,1)");
      }
   }

   private NanoGeoRenderer.NanoGeoBoneTransform boneTransformForArmorPose(NanoGeoBakedModel.NanoGeoBakedBone bone) {
      int boneId = bone.boneId();
      if (boneId >= 0 && boneId < this.frameBoneTransformsById.length) {
         NanoGeoRenderer.NanoGeoBoneTransform transform = this.frameBoneTransformsById[boneId];
         return transform == null ? NanoGeoRenderer.NanoGeoBoneTransform.IDENTITY : transform;
      } else {
         return NanoGeoRenderer.NanoGeoBoneTransform.IDENTITY;
      }
   }

   private void prepareBoneTransformCache() {
      if (this.currentBakedModel == null) {
         this.frameBoneTransformsById = new NanoGeoRenderer.NanoGeoBoneTransform[0];
      } else {
         int boneCount = Math.max(0, this.currentBakedModel.boneCount());
         if (this.frameBoneTransformsById.length != boneCount) {
            this.frameBoneTransformsById = new NanoGeoRenderer.NanoGeoBoneTransform[boneCount];
         } else {
            Arrays.fill(this.frameBoneTransformsById, null);
         }

         for (NanoGeoArmorRenderer.ResolvedBoneBinding resolvedBinding : this.resolvedBoneBindings) {
            NanoGeoRenderer.NanoGeoBoneTransform transform = this.computeBoneTransform(resolvedBinding);
            if (transform != NanoGeoRenderer.NanoGeoBoneTransform.IDENTITY) {
               int boneId = resolvedBinding.boneId;
               if (boneId < this.frameBoneTransformsById.length) {
                  this.frameBoneTransformsById[boneId] = transform;
               }
            }
         }
      }
   }

   private NanoGeoRenderer.NanoGeoBoneTransform computeBoneTransform(NanoGeoArmorRenderer.ResolvedBoneBinding resolvedBinding) {
      String boneName = resolvedBinding.boneName;
      NanoGeoHumanoidBoneMapper.BonePoseBinding binding = resolvedBinding.binding;
      NanoGeoArmorRenderer.BonePose pose = this.partPoses.get(binding.part());
      if (pose != null && pose.active) {
         float forcedXRotRad = 0.0F;
         float forcedYRotRad = 0.0F;
         float forcedZRotRad = 0.0F;
         if (this.isForcedRotationTarget(boneName)) {
            forcedXRotRad = (float)Math.toRadians(NanoGeoClientConfig.nanoGeoDebugForceBoneRotationX());
            forcedYRotRad = (float)Math.toRadians(NanoGeoClientConfig.nanoGeoDebugForceBoneRotationY());
            forcedZRotRad = (float)Math.toRadians(NanoGeoClientConfig.nanoGeoDebugForceBoneRotationZ());
         }

         float appliedXRot = -pose.xRot + forcedXRotRad;
         float appliedYRot = -pose.yRot + forcedYRotRad;
         float appliedZRot = pose.zRot + forcedZRotRad;
         float translateX = NanoGeoCoordinateSystem.geoPoseTranslationX(pose.x);
         float translateY = NanoGeoCoordinateSystem.geoPoseTranslationY(pose.y);
         float translateZ = NanoGeoCoordinateSystem.geoPoseTranslationZ(pose.z);
         if (NanoGeoClientConfig.nanoGeoDebugLogBoneTransforms() && this.shouldLogBoneTransform(boneName)) {
            NanoGeoBakedModel.NanoGeoBakedBone bakedBone = resolvedBinding.bakedBone;
            NanoGeoBone sourceBone = resolvedBinding.sourceBone;
            LOGGER.info(
               "[CrysisMod][NanoGeo][Pose] bone={} parent={} part={} slot={} posRaw=({},{},{}) posApplied=({},{},{}) rotRaw=({},{},{}) rotAppliedRad=({},{},{}) sourcePivotRaw=({},{},{}) bakedPivot=({},{},{}) sourceRotDeg=({},{},{}) bakedRotRad=({},{},{})",
               new Object[]{
                  boneName,
                  sourceBone == null ? "<unknown>" : sourceBone.parentName(),
                  binding.part(),
                  this.currentSlot,
                  pose.x,
                  pose.y,
                  pose.z,
                  translateX,
                  translateY,
                  translateZ,
                  pose.xRot,
                  pose.yRot,
                  pose.zRot,
                  appliedXRot,
                  appliedYRot,
                  appliedZRot,
                  sourceBone == null ? Float.NaN : sourceBone.pivotX(),
                  sourceBone == null ? Float.NaN : sourceBone.pivotY(),
                  sourceBone == null ? Float.NaN : sourceBone.pivotZ(),
                  bakedBone == null ? Float.NaN : bakedBone.pivotX(),
                  bakedBone == null ? Float.NaN : bakedBone.pivotY(),
                  bakedBone == null ? Float.NaN : bakedBone.pivotZ(),
                  sourceBone == null ? Float.NaN : sourceBone.rotationX(),
                  sourceBone == null ? Float.NaN : sourceBone.rotationY(),
                  sourceBone == null ? Float.NaN : sourceBone.rotationZ(),
                  bakedBone == null ? Float.NaN : bakedBone.rotationX(),
                  bakedBone == null ? Float.NaN : bakedBone.rotationY(),
                  bakedBone == null ? Float.NaN : bakedBone.rotationZ()
               }
            );
         }

         return new NanoGeoRenderer.NanoGeoBoneTransform(translateX, translateY, translateZ, appliedXRot, appliedYRot, appliedZRot);
      } else {
         return NanoGeoRenderer.NanoGeoBoneTransform.IDENTITY;
      }
   }

   private void ensureResolvedBoneBindings(NanoGeoBakedModel bakedModel) {
      if (this.bindingCacheModel != bakedModel) {
         this.bindingCacheModel = bakedModel;
         this.resolvedBoneBindings = this.resolveBoneBindingsForModel(bakedModel);
      }
   }

   private NanoGeoArmorRenderer.ResolvedBoneBinding[] resolveBoneBindingsForModel(NanoGeoBakedModel bakedModel) {
      if (bakedModel == null) {
         return new NanoGeoArmorRenderer.ResolvedBoneBinding[0];
      } else {
         List<NanoGeoArmorRenderer.ResolvedBoneBinding> resolved = new ArrayList<>();
         Map<String, NanoGeoBone> sourceBonesByName = bakedModel.sourceModel().bonesByName();

         for (Entry<String, NanoGeoHumanoidBoneMapper.BonePoseBinding> entry : NanoGeoHumanoidBoneMapper.bindings().entrySet()) {
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.MAP_LOOKUPS);
            NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.STRING_BONE_LOOKUPS);
            NanoGeoBakedModel.NanoGeoBakedBone bakedBone = bakedModel.bakedBonesByName().get(entry.getKey());
            if (bakedBone != null) {
               int boneId = bakedBone.boneId();
               if (boneId >= 0) {
                  resolved.add(
                     new NanoGeoArmorRenderer.ResolvedBoneBinding(entry.getKey(), boneId, entry.getValue(), bakedBone, sourceBonesByName.get(entry.getKey()))
                  );
               }
            }
         }

         return resolved.toArray(new NanoGeoArmorRenderer.ResolvedBoneBinding[0]);
      }
   }

   private void resolveArmorTextureForLogs(LivingEntity entity, ItemStack stack, EquipmentSlot slot) {
      this.currentArmorTexture = TEXTURE_RESOURCE.toString();
      if (stack.getItem() instanceof ArmorItem armorItem) {
         List<ArmorMaterial.Layer> layers = armorItem.getMaterial().value().layers();
         if (!layers.isEmpty()) {
            this.currentArmorTexture = ClientHooks.getArmorTexture(entity, stack, layers.getFirst(), slot == EquipmentSlot.LEGS, slot).toString();
         }
      }
   }

   private void resetPoses() {
      for (NanoGeoArmorRenderer.BonePose pose : this.partPoses.values()) {
         pose.reset();
      }
   }

   private boolean shouldLogBoneTransform(String boneName) {
      String focusBone = NanoGeoClientConfig.nanoGeoDebugBoneName();
      if (!focusBone.isBlank() && !focusBone.equals(boneName)) {
         return false;
      } else if (this.loggedBoneTransformNames.contains(boneName)) {
         return false;
      } else {
         this.loggedBoneTransformNames.add(boneName);
         return true;
      }
   }

   private boolean isForcedRotationTarget(String boneName) {
      String focusBone = NanoGeoClientConfig.nanoGeoDebugBoneName();
      if (!focusBone.isBlank()) {
         return focusBone.equals(boneName);
      } else {
         String onlyBone = NanoGeoClientConfig.nanoGeoDebugRenderOnlyBone();
         return !onlyBone.isBlank() && onlyBone.equals(boneName);
      }
   }

   private boolean shouldUseFastIndexedPath() {
      if (NanoGeoClientConfig.nanoGeoDebugShowBonePivots() || NanoGeoClientConfig.nanoGeoDebugShowBoneAxes()) {
         return false;
      } else if (NanoGeoClientConfig.nanoGeoDebugTintLeftRight()) {
         return false;
      } else if (!NanoGeoClientConfig.nanoGeoDebugRenderOnlyBone().isBlank()) {
         return false;
      } else if (!NanoGeoClientConfig.nanoGeoDebugRenderOnlyFace().isBlank()) {
         return false;
      } else if (NanoGeoClientConfig.nanoGeoDebugRenderOnlyCubeIndex() >= 0) {
         return false;
      } else {
         return NanoGeoClientConfig.nanoGeoDebugHideChildren() ? false : !NanoGeoClientConfig.nanoGeoDebugUv();
      }
   }

   private static final class BonePose {
      private boolean active;
      private float x;
      private float y;
      private float z;
      private float xRot;
      private float yRot;
      private float zRot;

      private void reset() {
         this.active = false;
         this.x = 0.0F;
         this.y = 0.0F;
         this.z = 0.0F;
         this.xRot = 0.0F;
         this.yRot = 0.0F;
         this.zRot = 0.0F;
      }

      private void set(float x, float y, float z, float xRot, float yRot, float zRot) {
         this.active = true;
         this.x = x;
         this.y = y;
         this.z = z;
         this.xRot = xRot;
         this.yRot = yRot;
         this.zRot = zRot;
      }
   }

   private static final class ResolvedBoneBinding {
      private final String boneName;
      private final int boneId;
      private final NanoGeoHumanoidBoneMapper.BonePoseBinding binding;
      private final NanoGeoBakedModel.NanoGeoBakedBone bakedBone;
      private final NanoGeoBone sourceBone;

      private ResolvedBoneBinding(
         String boneName, int boneId, NanoGeoHumanoidBoneMapper.BonePoseBinding binding, NanoGeoBakedModel.NanoGeoBakedBone bakedBone, NanoGeoBone sourceBone
      ) {
         this.boneName = boneName;
         this.boneId = boneId;
         this.binding = binding;
         this.bakedBone = bakedBone;
         this.sourceBone = sourceBone;
      }
   }

   private static final class SlotVisibility {
      private static final NanoGeoBakedModel.NanoGeoBakedBone[] EMPTY_CHILDREN = new NanoGeoBakedModel.NanoGeoBakedBone[0];
      private static final NanoGeoArmorRenderer.SlotVisibility EMPTY = new NanoGeoArmorRenderer.SlotVisibility(
         new boolean[0], new boolean[0], EMPTY_CHILDREN, new NanoGeoBakedModel.NanoGeoBakedBone[0][]
      );
      private final boolean[] visibleById;
      private final boolean[] subtreeById;
      private final NanoGeoBakedModel.NanoGeoBakedBone[] traversableRoots;
      private final NanoGeoBakedModel.NanoGeoBakedBone[][] traversableChildrenByBoneId;

      private SlotVisibility(
         boolean[] visibleById,
         boolean[] subtreeById,
         NanoGeoBakedModel.NanoGeoBakedBone[] traversableRoots,
         NanoGeoBakedModel.NanoGeoBakedBone[][] traversableChildrenByBoneId
      ) {
         this.visibleById = visibleById;
         this.subtreeById = subtreeById;
         this.traversableRoots = traversableRoots;
         this.traversableChildrenByBoneId = traversableChildrenByBoneId;
      }
   }
}
