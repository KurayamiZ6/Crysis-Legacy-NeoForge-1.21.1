package io.github.painworks.crysismod.client.compat;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

public final class GnetumHudCompat {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String GNETUM_MODID = "gnetum";
   private static final String GNETUM_CLASS = "me.decce.gnetum.Gnetum";
   private static final String DISABLE_REASON = "crysismod dynamic HUD requires per-frame rendering";
   private static boolean initialized;
   private static boolean available;
   private static boolean warnedUnavailable;
   private static boolean loggedActive;
   private static Method disableCachingForCurrentElement;
   private static Field renderingField;

   private GnetumHudCompat() {
   }

   public static void disableCachingForCurrentHudElement() {
      ensureInitialized();
      if (available && disableCachingForCurrentElement != null) {
         try {
            if (renderingField != null && !Boolean.TRUE.equals(renderingField.get(null))) {
               return;
            }

            disableCachingForCurrentElement.invoke(null, "crysismod dynamic HUD requires per-frame rendering");
            if (!loggedActive) {
               loggedActive = true;
               LOGGER.info("[{}] Gnetum detected: disabling Gnetum HUD caching for Crysis Mod HUD elements.", "crysismod");
            }
         } catch (RuntimeException | ReflectiveOperationException var1) {
            if (!warnedUnavailable) {
               warnedUnavailable = true;
               LOGGER.warn("[{}] Gnetum HUD compatibility hook failed; Crysis Mod HUD may be cached by Gnetum.", "crysismod", var1);
            }

            available = false;
         }
      }
   }

   public static boolean isGnetumLoaded() {
      ensureInitialized();
      return available;
   }

   private static void ensureInitialized() {
      if (!initialized) {
         initialized = true;
         if (ModList.get().isLoaded("gnetum")) {
            try {
               Class<?> gnetum = Class.forName("me.decce.gnetum.Gnetum");
               disableCachingForCurrentElement = gnetum.getMethod("disableCachingForCurrentElement", String.class);
               renderingField = gnetum.getField("rendering");
               available = true;
            } catch (LinkageError | ReflectiveOperationException var1) {
               if (!warnedUnavailable) {
                  warnedUnavailable = true;
                  LOGGER.warn("[{}] Gnetum is loaded, but its HUD caching API is unavailable.", "crysismod", var1);
               }
            }
         }
      }
   }
}
