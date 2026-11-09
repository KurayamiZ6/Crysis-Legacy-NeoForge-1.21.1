package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

public final class NanoGeoShaderpackCompatibility {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final long CACHE_INTERVAL_NANOS = 250000000L;
   private static volatile long lastCheckNanos;
   private static volatile boolean shaderPackActive;
   private static volatile boolean irisShaderPackActive;
   private static volatile boolean optiFineShaderPackActive;
   private static volatile boolean loggedCompatibilityMode;
   private static volatile String lastBackendSignature = "";

   private NanoGeoShaderpackCompatibility() {
   }

   public static boolean isShaderPackActive() {
      long now = System.nanoTime();
      if (now - lastCheckNanos <= 250000000L) {
         return shaderPackActive;
      } else {
         boolean irisActive = detectViaIrisApi("net.irisshaders.iris.api.v0.IrisApi") || detectViaIrisApi("net.coderbot.iris.api.v0.IrisApi");
         boolean optiFineActive = detectViaOptiFine();
         boolean active = irisActive || optiFineActive;
         irisShaderPackActive = irisActive;
         optiFineShaderPackActive = optiFineActive;
         shaderPackActive = active;
         lastCheckNanos = now;
         if (active && NanoGeoClientConfig.debugArmorModeOverlay() && !loggedCompatibilityMode) {
            loggedCompatibilityMode = true;
            LOGGER.info(
               "[CrysisMod][ArmorModeOverlay] shaderpack detected (iris={} optifine={}), using compatibility fallback render path", irisActive, optiFineActive
            );
         } else if (!active) {
            loggedCompatibilityMode = false;
         }

         return active;
      }
   }

   public static boolean isIrisShaderPackActive() {
      isShaderPackActive();
      return irisShaderPackActive;
   }

   public static boolean isOptiFineShaderPackActive() {
      isShaderPackActive();
      return optiFineShaderPackActive;
   }

   public static boolean shouldUseArmorModeCompatibilityFallback() {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft != null && minecraft.level != null ? isShaderPackActive() : false;
   }

   public static NanoGeoClientConfig.ArmorModeOverlayBackend resolveArmorModeOverlayBackend() {
      NanoGeoClientConfig.ArmorModeOverlayBackend configured = NanoGeoClientConfig.armorModeOverlayBackend();
      boolean shaderpackActiveNow = shouldUseArmorModeCompatibilityFallback();

      NanoGeoClientConfig.ArmorModeOverlayBackend resolved = switch (configured) {
         case AUTO -> shaderpackActiveNow
            ? NanoGeoClientConfig.ArmorModeOverlayBackend.DYNAMIC_TEXTURE_COMPAT
            : NanoGeoClientConfig.ArmorModeOverlayBackend.CUSTOM_SHADER;
         case CUSTOM_SHADER -> NanoGeoClientConfig.ArmorModeOverlayBackend.CUSTOM_SHADER;
         case TEXTURE_FALLBACK -> NanoGeoClientConfig.ArmorModeOverlayBackend.TEXTURE_FALLBACK;
         case DYNAMIC_TEXTURE_COMPAT -> NanoGeoClientConfig.ArmorModeOverlayBackend.DYNAMIC_TEXTURE_COMPAT;
         case DISABLED -> NanoGeoClientConfig.ArmorModeOverlayBackend.DISABLED;
      };
      if (NanoGeoClientConfig.debugArmorModeOverlay()) {
         String signature = "configured="
            + configured
            + " resolved="
            + resolved
            + " shaderpackActive="
            + shaderpackActiveNow
            + " iris="
            + irisShaderPackActive
            + " optifine="
            + optiFineShaderPackActive;
         if (!signature.equals(lastBackendSignature)) {
            lastBackendSignature = signature;
            LOGGER.info("[CrysisMod][ArmorModeOverlay] backend {}", signature);
         }
      } else {
         lastBackendSignature = "";
      }

      return resolved;
   }

   private static boolean detectViaIrisApi(String className) {
      try {
         Class<?> apiClass = Class.forName(className);
         Method getInstance = apiClass.getMethod("getInstance");
         Object api = getInstance.invoke(null);
         if (api == null) {
            return false;
         } else {
            Method isShaderPackInUse = apiClass.getMethod("isShaderPackInUse");
            return isShaderPackInUse.invoke(api) instanceof Boolean active && active;
         }
      } catch (ReflectiveOperationException var7) {
         return false;
      } catch (LinkageError var8) {
         return false;
      }
   }

   private static boolean detectViaOptiFine() {
      return invokeStaticBoolean("net.optifine.Config", "isShaders")
         || readStaticBoolean("net.optifine.shaders.Shaders", "shaderPackLoaded")
         || readStaticBoolean("net.optifine.shaders.Shaders", "shaderPackLoadedOnce");
   }

   private static boolean invokeStaticBoolean(String className, String methodName) {
      try {
         Class<?> type = Class.forName(className);
         Method method = type.getMethod(methodName);
         return method.invoke(null) instanceof Boolean active && active;
      } catch (ReflectiveOperationException var6) {
         return false;
      } catch (LinkageError var7) {
         return false;
      }
   }

   private static boolean readStaticBoolean(String className, String fieldName) {
      try {
         Class<?> type = Class.forName(className);
         return type.getField(fieldName).get(null) instanceof Boolean active && active;
      } catch (ReflectiveOperationException var5) {
         return false;
      } catch (LinkageError var6) {
         return false;
      }
   }
}
