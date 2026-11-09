package io.github.painworks.crysismod.client.nanogeo;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.EquipmentSlot;

public final class NanoGeoHumanoidBoneMapper {
   private static final Map<EquipmentSlot, Set<String>> SLOT_VISIBLE_BONES;
   private static final Map<String, NanoGeoHumanoidBoneMapper.BonePoseBinding> BONE_BINDINGS;
   private static final Set<String> FPV_RIGHT_ARM_VISIBLE_BONES = Set.of("bipedRightArm", "armorRightArm");
   private static final Set<String> FPV_LEFT_ARM_VISIBLE_BONES = Set.of("bipedLeftArm", "armorLeftArm");

   private NanoGeoHumanoidBoneMapper() {
   }

   public static Set<String> visibleBonesForSlot(EquipmentSlot slot) {
      return SLOT_VISIBLE_BONES.getOrDefault(slot, Set.of());
   }

   public static Map<EquipmentSlot, Set<String>> slotVisibilityMap() {
      return SLOT_VISIBLE_BONES;
   }

   public static Set<String> visibleBonesForFirstPersonRightArm() {
      return FPV_RIGHT_ARM_VISIBLE_BONES;
   }

   public static Set<String> visibleBonesForFirstPersonLeftArm() {
      return FPV_LEFT_ARM_VISIBLE_BONES;
   }

   public static NanoGeoHumanoidBoneMapper.BonePoseBinding bindingForBone(String boneName) {
      return BONE_BINDINGS.get(boneName);
   }

   public static Map<String, NanoGeoHumanoidBoneMapper.BonePoseBinding> bindings() {
      return BONE_BINDINGS;
   }

   static {
      Map<EquipmentSlot, Set<String>> slotBones = new EnumMap<>(EquipmentSlot.class);
      slotBones.put(EquipmentSlot.HEAD, Set.of("armorHead"));
      slotBones.put(EquipmentSlot.CHEST, Set.of("armorBody", "armorRightArm", "armorLeftArm"));
      slotBones.put(EquipmentSlot.LEGS, Set.of("armorRightLeg", "armorLeftLeg"));
      slotBones.put(EquipmentSlot.FEET, Set.of("armorRightBoot", "armorLeftBoot"));
      SLOT_VISIBLE_BONES = Collections.unmodifiableMap(slotBones);
      Map<String, NanoGeoHumanoidBoneMapper.BonePoseBinding> bindings = new HashMap<>();
      bindings.put("bipedHead", new NanoGeoHumanoidBoneMapper.BonePoseBinding(NanoGeoHumanoidBoneMapper.HumanoidPart.HEAD, 0.0F, 0.0F));
      bindings.put("bipedBody", new NanoGeoHumanoidBoneMapper.BonePoseBinding(NanoGeoHumanoidBoneMapper.HumanoidPart.BODY, 0.0F, 0.0F));
      bindings.put("bipedRightArm", new NanoGeoHumanoidBoneMapper.BonePoseBinding(NanoGeoHumanoidBoneMapper.HumanoidPart.RIGHT_ARM, 5.0F, 2.0F));
      bindings.put("bipedLeftArm", new NanoGeoHumanoidBoneMapper.BonePoseBinding(NanoGeoHumanoidBoneMapper.HumanoidPart.LEFT_ARM, -5.0F, 2.0F));
      bindings.put("bipedRightLeg", new NanoGeoHumanoidBoneMapper.BonePoseBinding(NanoGeoHumanoidBoneMapper.HumanoidPart.RIGHT_LEG, 2.0F, 12.0F));
      bindings.put("bipedLeftLeg", new NanoGeoHumanoidBoneMapper.BonePoseBinding(NanoGeoHumanoidBoneMapper.HumanoidPart.LEFT_LEG, -2.0F, 12.0F));
      BONE_BINDINGS = Collections.unmodifiableMap(bindings);
   }

   public record BonePoseBinding(NanoGeoHumanoidBoneMapper.HumanoidPart part, float xOffset, float yBase) {
   }

   public static enum HumanoidPart {
      HEAD,
      BODY,
      RIGHT_ARM,
      LEFT_ARM,
      RIGHT_LEG,
      LEFT_LEG;
   }
}
