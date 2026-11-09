package io.github.painworks.crysisscopeadspatch.config;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import org.slf4j.Logger;

public final class LegacySpectrumConfig {
   public static final double DEFAULT_HELD_ITEM_OPACITY = 1.0;
   public static final boolean DEFAULT_OPTIMIZED_TACZ_CAPTURE = true;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Path CONFIG_PATH = Path.of(System.getProperty("user.dir", "."), "config", "crysisscopefix-client.properties");
   private static final Properties LOADED = loadProperties();
   private static volatile double heldItemOpacity = loadHeldItemOpacity(LOADED);
   private static volatile boolean optimizedTaczCapture = loadBoolean(LOADED, "optimized_tacz_capture", true);

   private LegacySpectrumConfig() {
   }

   public static double heldItemOpacity() {
      return heldItemOpacity;
   }

   public static synchronized void setHeldItemOpacity(double value) {
      heldItemOpacity = clamp(value, 1.0);
      save();
   }

   public static boolean optimizedTaczCapture() {
      return optimizedTaczCapture;
   }

   public static synchronized void setOptimizedTaczCapture(boolean enabled) {
      optimizedTaczCapture = enabled;
      save();
   }

   private static void save() {
      String contents = "# Legacy first-person NanoVision spectrum settings.\n# 0.0 = physical item only, 1.0 = fully opaque thermal overlay.\nheld_item_opacity=%s\n# Uses a smaller capture vertex only for supported TACZ entity geometry.\n# Unsupported/scope paths automatically keep the legacy capture.\noptimized_tacz_capture=%s\n"
         .formatted(Double.toString(heldItemOpacity), Boolean.toString(optimizedTaczCapture));

      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         Files.writeString(
            CONFIG_PATH, contents, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
         );
         LOGGER.info(
            "[CrysisScopeFix] client config saved: held-item opacity={}, optimized TACZ capture={}, path={}",
            new Object[]{heldItemOpacity, optimizedTaczCapture, CONFIG_PATH.toAbsolutePath()}
         );
      } catch (IOException var2) {
         LOGGER.error("[CrysisScopeFix] failed to save client config {}", CONFIG_PATH.toAbsolutePath(), var2);
      }
   }

   private static Properties loadProperties() {
      Properties properties = new Properties();
      if (Files.isRegularFile(CONFIG_PATH)) {
         try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            properties.load(input);
         } catch (IOException var6) {
            LOGGER.warn("[CrysisScopeFix] failed to read {}; using defaults", CONFIG_PATH.toAbsolutePath(), var6);
         }
      }

      return properties;
   }

   private static double loadHeldItemOpacity(Properties properties) {
      return loadDouble(properties, "held_item_opacity", 1.0);
   }

   private static double loadDouble(Properties properties, String key, double defaultValue) {
      String raw = properties.getProperty(key, Double.toString(defaultValue));

      try {
         return clamp(Double.parseDouble(raw.trim()), defaultValue);
      } catch (NumberFormatException var6) {
         return defaultValue;
      }
   }

   private static boolean loadBoolean(Properties properties, String key, boolean defaultValue) {
      String raw = properties.getProperty(key);
      return raw == null ? defaultValue : Boolean.parseBoolean(raw.trim());
   }

   private static double clamp(double value, double defaultValue) {
      return !Double.isFinite(value) ? defaultValue : Math.max(0.0, Math.min(1.0, value));
   }
}
