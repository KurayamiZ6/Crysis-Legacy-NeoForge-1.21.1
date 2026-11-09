package io.github.painworks.crysismod.client.minimap.sampling;

import io.github.painworks.crysismod.client.minimap.config.MinimapHeightShadingMode;
import io.github.painworks.crysismod.client.minimap.config.MinimapTerrainQuality;
import io.github.painworks.crysismod.client.minimap.config.NanoMinimapClientConfig;
import java.util.Locale;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public final class NanoMinimapWorldSampler {
   private MinimapTerrainQuality terrainQuality = MinimapTerrainQuality.MEDIUM;
   private boolean heightShadingEnabled = true;
   private MinimapHeightShadingMode heightShadingMode = MinimapHeightShadingMode.SIMPLE;
   private float heightShadingStrength = 0.2F;
   private float brightness = 1.0F;
   private float contrast = 1.1F;
   private float saturation = 1.08F;
   private boolean waterTintEnabled = true;
   private boolean biomeTintEnabled = false;
   private boolean debugColorsEnabled = false;
   private int mapColorCount;
   private int fallbackColorCount;
   private int specialCaseColorCount;
   private int waterColorCount;
   private int lavaColorCount;
   private int heightShadingAppliedCount;
   private int biomeTintAppliedCount;
   private String lastColorMode = "MEDIUM";

   public void beginTick() {
      this.terrainQuality = NanoMinimapClientConfig.minimapTerrainQuality();
      this.heightShadingEnabled = NanoMinimapClientConfig.minimapHeightShadingEnabled();
      this.heightShadingMode = NanoMinimapClientConfig.minimapHeightShadingMode();
      this.heightShadingStrength = (float)Mth.clamp(NanoMinimapClientConfig.minimapHeightShadingStrength(), 0.0, 1.0);
      this.brightness = (float)Mth.clamp(NanoMinimapClientConfig.minimapTerrainBrightness(), 0.25, 2.0);
      this.contrast = (float)Mth.clamp(NanoMinimapClientConfig.minimapTerrainContrast(), 0.25, 2.0);
      this.saturation = (float)Mth.clamp(NanoMinimapClientConfig.minimapTerrainSaturation(), 0.0, 2.0);
      this.waterTintEnabled = NanoMinimapClientConfig.minimapWaterTintEnabled();
      this.biomeTintEnabled = NanoMinimapClientConfig.minimapBiomeTintEnabled();
      this.debugColorsEnabled = NanoMinimapClientConfig.debugMinimapColors();
      this.lastColorMode = this.terrainQuality.name();
      if (this.debugColorsEnabled) {
         this.mapColorCount = 0;
         this.fallbackColorCount = 0;
         this.specialCaseColorCount = 0;
         this.waterColorCount = 0;
         this.lavaColorCount = 0;
         this.heightShadingAppliedCount = 0;
         this.biomeTintAppliedCount = 0;
      }
   }

   public NanoMinimapWorldSampler.Stats stats() {
      return new NanoMinimapWorldSampler.Stats(
         this.mapColorCount,
         this.fallbackColorCount,
         this.specialCaseColorCount,
         this.waterColorCount,
         this.lavaColorCount,
         this.heightShadingAppliedCount,
         this.biomeTintAppliedCount,
         this.lastColorMode
      );
   }

   public int sampleArgb(BlockAndTintGetter level, BlockPos pos, BlockState state, int topY, int leftNeighborHeight, int upNeighborHeight) {
      int argb = this.resolveBaseArgb(level, pos, state);
      if (this.shouldApplyHeightShading()) {
         argb = this.applyHeightShading(argb, topY, leftNeighborHeight, upNeighborHeight);
         if (this.debugColorsEnabled) {
            this.heightShadingAppliedCount++;
         }
      }

      return this.applyTone(argb);
   }

   private int resolveBaseArgb(BlockAndTintGetter level, BlockPos pos, BlockState state) {
      int specialCase = this.resolveSpecialCaseColor(level, pos, state);
      if (specialCase != Integer.MIN_VALUE) {
         if (this.debugColorsEnabled) {
            this.specialCaseColorCount++;
         }

         return specialCase;
      } else {
         ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(state.getBlock());
         String path = blockKey == null ? "" : blockKey.getPath();
         MapColor mapColor = state.getMapColor(level, pos);
         if (this.isMapColorUsable(mapColor)) {
            if (this.debugColorsEnabled) {
               this.mapColorCount++;
            }

            int mapArgb = 0xFF000000 | mapColor.col & 16777215;
            return this.maybeBiomeTint(level, pos, state, path, mapArgb);
         } else {
            int categoryColor = this.resolveCategoryColor(state, path);
            if (categoryColor != Integer.MIN_VALUE) {
               if (this.debugColorsEnabled) {
                  this.fallbackColorCount++;
               }

               return this.maybeBiomeTint(level, pos, state, path, categoryColor);
            } else {
               if (this.debugColorsEnabled) {
                  this.fallbackColorCount++;
               }

               return -8486264;
            }
         }
      }
   }

   private int resolveSpecialCaseColor(BlockAndTintGetter level, BlockPos pos, BlockState state) {
      if (state.getFluidState().is(FluidTags.WATER)) {
         if (this.debugColorsEnabled) {
            this.waterColorCount++;
         }

         int waterBase = -12684337;
         int waterTint = this.maybeWaterBiomeTint(level, pos);
         return this.blend(waterBase, waterTint, this.terrainQuality == MinimapTerrainQuality.HIGH ? 0.35F : 0.22F);
      } else if (state.getFluidState().is(FluidTags.LAVA)) {
         if (this.debugColorsEnabled) {
            this.lavaColorCount++;
         }

         return -1741793;
      } else if (state.is(BlockTags.SNOW)) {
         return -1511689;
      } else if (state.is(BlockTags.LEAVES)) {
         return this.maybeBiomeTint(level, pos, state, "", -11560116);
      } else {
         return state.is(BlockTags.LOGS) ? -8232893 : Integer.MIN_VALUE;
      }
   }

   private boolean isMapColorUsable(MapColor mapColor) {
      if (mapColor != null && mapColor != MapColor.NONE) {
         int col = mapColor.col & 16777215;
         int r = col >>> 16 & 0xFF;
         int g = col >>> 8 & 0xFF;
         int b = col & 0xFF;
         int min = Math.min(r, Math.min(g, b));
         int max = Math.max(r, Math.max(g, b));
         int spread = max - min;
         int brightness = (r + g + b) / 3;
         return brightness < 20 ? false : spread >= 2;
      } else {
         return false;
      }
   }

   private int resolveCategoryColor(BlockState state, String path) {
      String lower = path.toLowerCase(Locale.ROOT);
      if (lower.contains("ice") || lower.contains("packed_ice") || lower.contains("blue_ice")) {
         return -5712138;
      } else if (lower.contains("planks")) {
         return -4220573;
      } else if (lower.contains("grass_block") || lower.contains("moss")) {
         return -10705331;
      } else if (lower.contains("farmland") || lower.contains("coarse_dirt")) {
         return -8297916;
      } else if (state.is(BlockTags.DIRT)) {
         return -7508920;
      } else if (lower.contains("deepslate")) {
         return -11842216;
      } else if (lower.contains("cobblestone")) {
         return -8552315;
      } else if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.STONE_ORE_REPLACEABLES)) {
         return -7367782;
      } else if (lower.contains("red_sand")) {
         return -3372972;
      } else if (state.is(BlockTags.SAND)) {
         return -1847150;
      } else if (lower.contains("gravel")) {
         return -7960435;
      } else if (lower.contains("glass")) {
         return -4598048;
      } else if (lower.contains("clay")) {
         return -5655359;
      } else if (lower.contains("terracotta")) {
         return -4687529;
      } else if (lower.contains("netherrack")) {
         return -8437442;
      } else if (lower.contains("basalt") || lower.contains("blackstone")) {
         return -11449012;
      } else if (lower.contains("end_stone")) {
         return -2238299;
      } else if (lower.contains("ore")
         || lower.contains("iron_block")
         || lower.contains("copper_block")
         || lower.contains("gold_block")
         || lower.contains("raw_")) {
         return -7300954;
      } else {
         return !lower.contains("crop")
               && !lower.contains("wheat")
               && !lower.contains("carrot")
               && !lower.contains("potato")
               && !lower.contains("beetroot")
               && !lower.contains("melon_stem")
               && !lower.contains("pumpkin_stem")
               && !lower.contains("sapling")
               && !lower.contains("flower")
               && !lower.contains("bush")
               && !lower.contains("fern")
            ? Integer.MIN_VALUE
            : -9850025;
      }
   }

   private int maybeBiomeTint(BlockAndTintGetter level, BlockPos pos, BlockState state, String path, int baseArgb) {
      if (this.biomeTintEnabled && this.terrainQuality != MinimapTerrainQuality.LOW) {
         String lower = path == null ? "" : path.toLowerCase(Locale.ROOT);
         boolean grassLike = lower.contains("grass") || lower.contains("moss") || lower.contains("fern") || lower.contains("crop") || lower.contains("sapling");
         boolean foliageLike = state.is(BlockTags.LEAVES) || lower.contains("leaves") || lower.contains("vine");
         if (!grassLike && !foliageLike) {
            return baseArgb;
         } else {
            int tint = grassLike ? BiomeColors.getAverageGrassColor(level, pos) : BiomeColors.getAverageFoliageColor(level, pos);
            if (this.debugColorsEnabled) {
               this.biomeTintAppliedCount++;
            }

            float blend = this.terrainQuality == MinimapTerrainQuality.HIGH ? 0.4F : 0.22F;
            return this.blend(baseArgb, 0xFF000000 | tint & 16777215, blend);
         }
      } else {
         return baseArgb;
      }
   }

   private int maybeWaterBiomeTint(BlockAndTintGetter level, BlockPos pos) {
      if (this.waterTintEnabled && this.terrainQuality != MinimapTerrainQuality.LOW) {
         int tint = BiomeColors.getAverageWaterColor(level, pos);
         if (this.debugColorsEnabled) {
            this.biomeTintAppliedCount++;
         }

         return 0xFF000000 | tint & 16777215;
      } else {
         return -12882743;
      }
   }

   private boolean shouldApplyHeightShading() {
      if (!this.heightShadingEnabled) {
         return false;
      } else {
         return this.heightShadingMode == MinimapHeightShadingMode.OFF ? false : this.terrainQuality != MinimapTerrainQuality.LOW;
      }
   }

   private int applyHeightShading(int argb, int height, int leftNeighborHeight, int upNeighborHeight) {
      int ref = leftNeighborHeight + upNeighborHeight >> 1;
      int delta = Mth.clamp(height - ref, -6, 6);
      float shade = 1.0F + delta * 0.04F * this.heightShadingStrength * 2.0F;
      return this.multiplyRgb(argb, shade);
   }

   private int applyTone(int argb) {
      int r = argb >>> 16 & 0xFF;
      int g = argb >>> 8 & 0xFF;
      int b = argb & 0xFF;
      float rf = r / 255.0F;
      float gf = g / 255.0F;
      float bf = b / 255.0F;
      rf = this.applyContrastAndBrightness(rf);
      gf = this.applyContrastAndBrightness(gf);
      bf = this.applyContrastAndBrightness(bf);
      float luma = rf * 0.299F + gf * 0.587F + bf * 0.114F;
      rf = luma + (rf - luma) * this.saturation;
      gf = luma + (gf - luma) * this.saturation;
      bf = luma + (bf - luma) * this.saturation;
      int ri = Mth.clamp(Math.round(rf * 255.0F), 0, 255);
      int gi = Mth.clamp(Math.round(gf * 255.0F), 0, 255);
      int bi = Mth.clamp(Math.round(bf * 255.0F), 0, 255);
      return 0xFF000000 | ri << 16 | gi << 8 | bi;
   }

   private float applyContrastAndBrightness(float v) {
      float contrasted = (v - 0.5F) * this.contrast + 0.5F;
      return Mth.clamp(contrasted * this.brightness, 0.0F, 1.0F);
   }

   private int multiplyRgb(int argb, float mul) {
      int r = Mth.clamp(Math.round((argb >>> 16 & 0xFF) * mul), 0, 255);
      int g = Mth.clamp(Math.round((argb >>> 8 & 0xFF) * mul), 0, 255);
      int b = Mth.clamp(Math.round((argb & 0xFF) * mul), 0, 255);
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   private int blend(int baseArgb, int tintArgb, float alpha) {
      float a = Mth.clamp(alpha, 0.0F, 1.0F);
      int br = baseArgb >>> 16 & 0xFF;
      int bg = baseArgb >>> 8 & 0xFF;
      int bb = baseArgb & 0xFF;
      int tr = tintArgb >>> 16 & 0xFF;
      int tg = tintArgb >>> 8 & 0xFF;
      int tb = tintArgb & 0xFF;
      int r = Mth.clamp(Math.round(br + (tr - br) * a), 0, 255);
      int g = Mth.clamp(Math.round(bg + (tg - bg) * a), 0, 255);
      int b = Mth.clamp(Math.round(bb + (tb - bb) * a), 0, 255);
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   public record Stats(
      int mapColorCount,
      int fallbackColorCount,
      int specialCaseColorCount,
      int waterColorCount,
      int lavaColorCount,
      int heightShadingAppliedCount,
      int biomeTintAppliedCount,
      String lastColorMode
   ) {
      public static NanoMinimapWorldSampler.Stats empty() {
         return new NanoMinimapWorldSampler.Stats(0, 0, 0, 0, 0, 0, 0, "MEDIUM");
      }
   }
}
