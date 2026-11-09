package io.github.painworks.crysismod.client.nanogeo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;

public final class NanoGeoArmorModePulseCenters implements ResourceManagerReloadListener {
   public static final ResourceLocation PULSE_CENTERS_JSON = ResourceLocation.fromNamespaceAndPath("crysismod", "armor_mode/pulse_centers.json");
   public static final Path EXTERNAL_FROZEN_JSON = Path.of("config", "crysismod", "pulse_centers.json");
   public static final int SHADER_MAX_CENTERS = 64;
   public static final NanoGeoArmorModePulseCenters INSTANCE = new NanoGeoArmorModePulseCenters();
   private static final Logger LOGGER = LogUtils.getLogger();
   private volatile NanoGeoArmorModePulseCenterSet centerSet = NanoGeoArmorModePulseCenterSet.EMPTY;
   private volatile boolean loaded;
   private volatile String sourceDescription = PULSE_CENTERS_JSON.toString();
   private volatile float[] cachedCenterX = new float[64];
   private volatile float[] cachedCenterY = new float[64];

   private NanoGeoArmorModePulseCenters() {
   }

   public void onResourceManagerReload(ResourceManager resourceManager) {
      this.reload(resourceManager);
   }

   public void reload(ResourceManager resourceManager) {
      try {
         this.centerSet = read(resourceManager);
         this.rebuildShaderUniformCache(this.centerSet);
         this.loaded = true;
         if (NanoGeoClientConfig.debugArmorModePlasmaField()) {
            LOGGER.info("[CrysisMod][ArmorModePulseCenters] loaded {} centers from {}", this.centerSet.centerCount(), this.sourceDescription);
         }
      } catch (Exception var3) {
         this.centerSet = NanoGeoArmorModePulseCenterSet.EMPTY;
         this.rebuildShaderUniformCache(this.centerSet);
         this.loaded = true;
         LOGGER.warn("[CrysisMod][ArmorModePulseCenters] failed to load {}, plasma pulses will have no centers", PULSE_CENTERS_JSON, var3);
      }
   }

   public void replaceCache(NanoGeoArmorModePulseCenterSet nextCenterSet) {
      this.centerSet = nextCenterSet == null ? NanoGeoArmorModePulseCenterSet.EMPTY : nextCenterSet;
      this.rebuildShaderUniformCache(this.centerSet);
      this.loaded = true;
      this.sourceDescription = EXTERNAL_FROZEN_JSON.toString();
   }

   public NanoGeoArmorModePulseCenterSet centers() {
      return this.centerSet;
   }

   public int centerCount() {
      return this.centerSet.centerCount();
   }

   public int shaderCenterCount(int configuredMaxCenters) {
      int safeMax = Math.max(0, Math.min(configuredMaxCenters, 64));
      return Math.min(this.centerSet.centerCount(), safeMax);
   }

   public float[] shaderCenterXUniformValues(int configuredMaxCenters) {
      return this.cachedCenterX;
   }

   public float[] shaderCenterYUniformValues(int configuredMaxCenters) {
      return this.cachedCenterY;
   }

   public boolean isLoaded() {
      return this.loaded;
   }

   public String dumpPulseCenterState(int configuredMaxCenters) {
      NanoGeoArmorModePulseCenterSet snapshot = this.centerSet;
      int used = this.shaderCenterCount(configuredMaxCenters);
      StringBuilder builder = new StringBuilder()
         .append("resource=")
         .append(PULSE_CENTERS_JSON)
         .append(" source=")
         .append(this.sourceDescription)
         .append(" loaded=")
         .append(this.loaded)
         .append(" totalCenters=")
         .append(snapshot.centerCount())
         .append(" usedCenters=")
         .append(used)
         .append(" texture=")
         .append(snapshot.texture())
         .append(" markerTexture=")
         .append(snapshot.markerTexture())
         .append(" textureSize=")
         .append(snapshot.textureWidth())
         .append('x')
         .append(snapshot.textureHeight())
         .append(" firstCenters=[");
      int count = Math.min(snapshot.centerCount(), 5);

      for (int i = 0; i < count; i++) {
         if (i > 0) {
            builder.append(", ");
         }

         NanoGeoArmorModePulseCenterSet.Center center = snapshot.centers().get(i);
         builder.append('(').append(center.x()).append(',').append(center.y()).append("; pixels=").append(center.pixelCount()).append(')');
      }

      return builder.append(']').toString();
   }

   private static NanoGeoArmorModePulseCenterSet read(ResourceManager resourceManager) throws Exception {
      if (Files.isRegularFile(EXTERNAL_FROZEN_JSON)) {
         NanoGeoArmorModePulseCenterSet var3;
         try (BufferedReader reader = Files.newBufferedReader(EXTERNAL_FROZEN_JSON, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            INSTANCE.sourceDescription = EXTERNAL_FROZEN_JSON.toString();
            var3 = parse(root);
         }

         return var3;
      } else {
         return resourceManager.getResource(PULSE_CENTERS_JSON).map(resource -> {
            try {
               NanoGeoArmorModePulseCenterSet var3x;
               try (BufferedReader readerx = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                  JsonObject rootx = JsonParser.parseReader(readerx).getAsJsonObject();
                  INSTANCE.sourceDescription = PULSE_CENTERS_JSON.toString();
                  var3x = parse(rootx);
               }

               return var3x;
            } catch (Exception var6) {
               throw new RuntimeException(var6);
            }
         }).orElse(NanoGeoArmorModePulseCenterSet.EMPTY);
      }
   }

   private static NanoGeoArmorModePulseCenterSet parse(JsonObject root) {
      String texture = stringOr(root, "texture", "");
      String markerTexture = stringOr(root, "marker_texture", "");
      int textureWidth = intOr(root, "texture_width", 512);
      int textureHeight = intOr(root, "texture_height", 512);
      List<NanoGeoArmorModePulseCenterSet.Center> centers = new ArrayList<>();

      for (JsonElement element : root.has("centers") && root.get("centers").isJsonArray() ? root.getAsJsonArray("centers") : new JsonArray()) {
         if (element.isJsonObject()) {
            JsonObject centerObject = element.getAsJsonObject();
            centers.add(
               new NanoGeoArmorModePulseCenterSet.Center(
                  floatOr(centerObject, "x", 0.0F), floatOr(centerObject, "y", 0.0F), intOr(centerObject, "pixel_count", 1)
               )
            );
         }
      }

      return new NanoGeoArmorModePulseCenterSet(texture, markerTexture, textureWidth, textureHeight, List.copyOf(centers));
   }

   private static String stringOr(JsonObject object, String member, String fallback) {
      return object.has(member) ? object.get(member).getAsString() : fallback;
   }

   private static int intOr(JsonObject object, String member, int fallback) {
      return object.has(member) ? object.get(member).getAsInt() : fallback;
   }

   private static float floatOr(JsonObject object, String member, float fallback) {
      return object.has(member) ? object.get(member).getAsFloat() : fallback;
   }

   private void rebuildShaderUniformCache(NanoGeoArmorModePulseCenterSet snapshot) {
      float[] nextX = new float[64];
      float[] nextY = new float[64];
      int count = Math.min(snapshot.centerCount(), 64);

      for (int i = 0; i < count; i++) {
         NanoGeoArmorModePulseCenterSet.Center center = snapshot.centers().get(i);
         nextX[i] = center.x();
         nextY[i] = center.y();
      }

      this.cachedCenterX = nextX;
      this.cachedCenterY = nextY;
   }
}
