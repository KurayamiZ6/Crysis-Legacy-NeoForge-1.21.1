package io.github.painworks.crysismod.client.nanovision.thermal;

import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ThermalMobProfileResolver {
   private static final TagKey<EntityType<?>> COLD_TAG = thermalTag("cold");
   private static final TagKey<EntityType<?>> COOL_TAG = thermalTag("cool");
   private static final TagKey<EntityType<?>> UNKNOWN_TAG = thermalTag("unknown");
   private static final TagKey<EntityType<?>> WARM_TAG = thermalTag("warm");
   private static final TagKey<EntityType<?>> HOT_TAG = thermalTag("hot");
   private static final TagKey<EntityType<?>> HOT_EMITTER_TAG = thermalTag("hot_emitters");

   private ThermalMobProfileResolver() {
   }

   public static ThermalMobProfile resolve(LivingEntity entity) {
      EntityType<?> type = entity.getType();
      ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
      String path = key != null ? key.getPath() : "";
      String namespace = key != null ? key.getNamespace() : "";
      ThermalMobProfile inferredProfile = inferProfile(entity, type, namespace, path);
      ThermalEmissionClass assignedClass = resolveConfigOverride(key);
      if (assignedClass == null) {
         assignedClass = resolveTaggedClass(type);
      }

      return assignedClass != null && assignedClass != inferredProfile.emissionClass() ? ThermalMobProfile.defaultFor(assignedClass) : inferredProfile;
   }

   private static ThermalMobProfile inferProfile(LivingEntity entity, EntityType<?> type, String namespace, String path) {
      if (!(entity instanceof Player) && !(entity instanceof AbstractVillager)) {
         if (entity instanceof Blaze || containsAny(path, "blaze", "fire_elemental", "inferno")) {
            return ThermalMobProfile.BLAZING_HOT;
         } else if (entity instanceof MagmaCube || containsAny(path, "magma_cube", "magma_golem", "lava", "phoenix", "dragon")) {
            return ThermalMobProfile.MAGMATIC_HOT;
         } else if (entity instanceof Slime || path.contains("slime")) {
            return ThermalMobProfile.SLIME_GEL;
         } else if (path.contains("skeleton") || path.contains("stray") || namespace.contains("skeleton")) {
            return ThermalMobProfile.SKELETAL_COLD;
         } else if (containsAny(path, "zombie", "husk", "drowned", "undead") || containsAny(namespace, "zombie", "undead", "deceased")) {
            return ThermalMobProfile.UNDEAD_COOL;
         } else if (path.contains("spider") || path.contains("arthropod") || path.contains("silverfish") || path.contains("endermite")) {
            return ThermalMobProfile.ARTHROPOD_COOL;
         } else if (containsAny(path, "enderman", "shulker", "enderling", "void_worm")) {
            return ThermalMobProfile.GENERIC_COOL;
         } else if (entity instanceof IronGolem || entity instanceof SnowGolem) {
            return ThermalMobProfile.SKELETAL_COLD;
         } else if (entity instanceof Animal
            || type.getCategory() == MobCategory.CREATURE
            || type.getCategory() == MobCategory.AXOLOTLS
            || type.getCategory() == MobCategory.WATER_CREATURE) {
            return ThermalMobProfile.WARM_BLOODED;
         } else {
            return type.getCategory() == MobCategory.MONSTER ? ThermalMobProfile.UNKNOWN_MEDIUM : ThermalMobProfile.UNKNOWN_MEDIUM;
         }
      } else {
         return ThermalMobProfile.CLOTHED_WARM;
      }
   }

   private static ThermalEmissionClass resolveTaggedClass(EntityType<?> type) {
      if (type.is(HOT_EMITTER_TAG)) {
         return ThermalEmissionClass.HOT_EMITTER;
      } else if (type.is(HOT_TAG)) {
         return ThermalEmissionClass.HOT;
      } else if (type.is(WARM_TAG)) {
         return ThermalEmissionClass.WARM;
      } else if (type.is(COOL_TAG)) {
         return ThermalEmissionClass.COOL;
      } else if (type.is(COLD_TAG)) {
         return ThermalEmissionClass.COLD;
      } else {
         return type.is(UNKNOWN_TAG) ? ThermalEmissionClass.UNKNOWN : null;
      }
   }

   private static ThermalEmissionClass resolveConfigOverride(ResourceLocation entityTypeId) {
      if (entityTypeId == null) {
         return null;
      } else {
         String targetId = entityTypeId.toString();
         List<? extends String> overrides = NanovisionConfig.thermalEntityClassOverrides();

         for (int index = overrides.size() - 1; index >= 0; index--) {
            String entry = overrides.get(index);
            int separator = entry.lastIndexOf(61);
            if (separator > 0) {
               String selector = entry.substring(0, separator).trim();
               boolean matches = selector.endsWith(":*")
                  ? entityTypeId.getNamespace().equals(selector.substring(0, selector.length() - 2))
                  : targetId.equals(selector);
               if (matches) {
                  try {
                     return ThermalEmissionClass.valueOf(entry.substring(separator + 1).trim().toUpperCase(Locale.ROOT));
                  } catch (IllegalArgumentException var9) {
                     return null;
                  }
               }
            }
         }

         return null;
      }
   }

   private static TagKey<EntityType<?>> thermalTag(String path) {
      ResourceLocation tagId = ResourceLocation.tryParse("crysismod:thermal/" + path);
      if (tagId == null) {
         throw new IllegalArgumentException("Invalid thermal entity tag path: " + path);
      } else {
         return TagKey.create(Registries.ENTITY_TYPE, tagId);
      }
   }

   private static boolean containsAny(String path, String... fragments) {
      for (String fragment : fragments) {
         if (path.contains(fragment)) {
            return true;
         }
      }

      return false;
   }

   public static boolean needsFullRendererFallback(LivingEntity entity, LivingEntityRenderer<?, ?> renderer) {
      if (ThermalExternalModelCompatibility.requiresFullRendererFallback(renderer.getModel())) {
         return true;
      } else {
         ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
         if (key != null && "minecraft".equals(key.getNamespace())) {
            String path = key.getPath();
            return "zombie".equals(path)
               ? hasExternalRendererOrModel(renderer)
               : "zombie_villager".equals(path) || "husk".equals(path) || "drowned".equals(path) || "zombified_piglin".equals(path);
         } else {
            return false;
         }
      }
   }

   private static boolean hasExternalRendererOrModel(LivingEntityRenderer<?, ?> renderer) {
      return !renderer.getClass().getName().startsWith("net.minecraft.") || !renderer.getModel().getClass().getName().startsWith("net.minecraft.");
   }
}
