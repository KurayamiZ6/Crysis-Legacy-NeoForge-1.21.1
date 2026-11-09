package io.github.painworks.crysismod.client.nanogeo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public final class NanoGeoArmorModePulseCenterExtractor {
   public static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath("crysismod", "textures/armor/nanosuit_armor_mode.png");
   public static final ResourceLocation MARKER_TEXTURE = ResourceLocation.fromNamespaceAndPath("crysismod", "textures/armor/nanosuit_armor_mode.png");
   public static final Path FROZEN_OUTPUT_PATH = Path.of("config", "crysismod", "pulse_centers.json");
   public static final Path RESOURCE_OUTPUT_PATH = Path.of("src", "main", "resources", "assets", "crysismod", "armor_mode", "pulse_centers.json");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final int R_MIN = 240;
   private static final int G_MAX = 40;
   private static final int B_MAX = 40;
   private static final int A_MIN = 128;

   private NanoGeoArmorModePulseCenterExtractor() {
   }

   public static NanoGeoArmorModePulseCenterExtractor.ExtractionResult extractAndWrite() throws Exception {
      Minecraft minecraft = Minecraft.getInstance();

      NanoGeoArmorModePulseCenterExtractor.ExtractionResult var12;
      try (InputStream stream = minecraft.getResourceManager().open(MARKER_TEXTURE)) {
         BufferedImage image = ImageIO.read(stream);
         if (image == null) {
            throw new IllegalStateException("ImageIO returned null for " + MARKER_TEXTURE);
         }

         NanoGeoArmorModePulseCenterSet centerSet = extract(image);
         JsonObject json = toJson(centerSet, image.getWidth(), image.getHeight());
         Files.createDirectories(FROZEN_OUTPUT_PATH.getParent());

         try (Writer writer = Files.newBufferedWriter(FROZEN_OUTPUT_PATH, StandardCharsets.UTF_8)) {
            GSON.toJson(json, writer);
         }

         writeResourceCopy(json);
         NanoGeoArmorModePulseCenters.INSTANCE.replaceCache(centerSet);
         var12 = new NanoGeoArmorModePulseCenterExtractor.ExtractionResult(centerSet.centerCount(), countMarkerPixels(image), FROZEN_OUTPUT_PATH);
      }

      return var12;
   }

   public static NanoGeoArmorModePulseCenterSet extract(BufferedImage image) {
      int width = image.getWidth();
      int height = image.getHeight();
      boolean[] marker = new boolean[width * height];
      boolean[] visited = new boolean[width * height];

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            marker[y * width + x] = isRedMarker(image.getRGB(x, y));
         }
      }

      List<NanoGeoArmorModePulseCenterSet.Center> centers = new ArrayList<>();
      ArrayDeque<int[]> queue = new ArrayDeque<>();

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int startIndex = y * width + x;
            if (marker[startIndex] && !visited[startIndex]) {
               visited[startIndex] = true;
               queue.add(new int[]{x, y});
               int pixelCount = 0;
               double sumX = 0.0;
               double sumY = 0.0;

               while (!queue.isEmpty()) {
                  int[] pixel = queue.removeFirst();
                  int px = pixel[0];
                  int py = pixel[1];
                  pixelCount++;
                  sumX += px + 0.5;
                  sumY += py + 0.5;

                  for (int dy = -1; dy <= 1; dy++) {
                     for (int dx = -1; dx <= 1; dx++) {
                        if (dx != 0 || dy != 0) {
                           int nx = px + dx;
                           int ny = py + dy;
                           if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                              int nextIndex = ny * width + nx;
                              if (marker[nextIndex] && !visited[nextIndex]) {
                                 visited[nextIndex] = true;
                                 queue.add(new int[]{nx, ny});
                              }
                           }
                        }
                     }
                  }
               }

               centers.add(new NanoGeoArmorModePulseCenterSet.Center((float)(sumX / pixelCount), (float)(sumY / pixelCount), pixelCount));
            }
         }
      }

      return new NanoGeoArmorModePulseCenterSet(OVERLAY_TEXTURE.toString(), MARKER_TEXTURE.toString(), width, height, List.copyOf(centers));
   }

   private static JsonObject toJson(NanoGeoArmorModePulseCenterSet centerSet, int width, int height) {
      JsonObject root = new JsonObject();
      root.addProperty("texture", OVERLAY_TEXTURE.toString());
      root.addProperty("marker_texture", MARKER_TEXTURE.toString());
      root.addProperty("texture_width", width);
      root.addProperty("texture_height", height);
      root.addProperty("source", "red_pixel_markers");
      JsonObject threshold = new JsonObject();
      threshold.addProperty("r_min", 240);
      threshold.addProperty("g_max", 40);
      threshold.addProperty("b_max", 40);
      threshold.addProperty("a_min", 128);
      root.add("red_threshold", threshold);
      JsonArray centers = new JsonArray();

      for (NanoGeoArmorModePulseCenterSet.Center center : centerSet.centers()) {
         JsonObject centerObject = new JsonObject();
         centerObject.addProperty("x", center.x());
         centerObject.addProperty("y", center.y());
         centerObject.addProperty("pixel_count", center.pixelCount());
         centers.add(centerObject);
      }

      root.add("centers", centers);
      return root;
   }

   private static void writeResourceCopy(JsonObject json) {
      try {
         Files.createDirectories(RESOURCE_OUTPUT_PATH.getParent());

         try (Writer writer = Files.newBufferedWriter(RESOURCE_OUTPUT_PATH, StandardCharsets.UTF_8)) {
            GSON.toJson(json, writer);
         }
      } catch (Exception var6) {
      }
   }

   private static int countMarkerPixels(BufferedImage image) {
      int count = 0;

      for (int y = 0; y < image.getHeight(); y++) {
         for (int x = 0; x < image.getWidth(); x++) {
            if (isRedMarker(image.getRGB(x, y))) {
               count++;
            }
         }
      }

      return count;
   }

   private static boolean isRedMarker(int argb) {
      int a = argb >>> 24 & 0xFF;
      int r = argb >>> 16 & 0xFF;
      int g = argb >>> 8 & 0xFF;
      int b = argb & 0xFF;
      return r >= 240 && g <= 40 && b <= 40 && a >= 128;
   }

   public record ExtractionResult(int centerCount, int markerPixelCount, Path outputPath) {
   }
}
