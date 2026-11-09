package io.github.painworks.crysisscopeadspatch.render;

import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoShaderpackCompatibility;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.slf4j.Logger;

public final class OculusPreHandDepth {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static boolean resolved;
   private static boolean unavailable;
   private static boolean logged;
   private static Method getPipelineManager;
   private static Method getPipelineNullable;
   private static Field renderTargets;
   private static Method getDepthTextureNoHand;
   private static Method getCurrentDepthTexture;
   private static Method getTextureId;

   private OculusPreHandDepth() {
   }

   public static int textureOr(int fallbackTexture) {
      Object targets = targetsOrNull();
      if (targets == null) {
         return fallbackTexture;
      } else if (NanoGeoShaderpackCompatibility.isShaderPackActive() && !unavailable) {
         try {
            Object depth = getDepthTextureNoHand.invoke(targets);
            int texture = (Integer)getTextureId.invoke(depth);
            if (texture <= 0) {
               return fallbackTexture;
            } else {
               if (!logged) {
                  logged = true;
                  LOGGER.info("[CrysisScopeFix] NanoVision entity extraction uses Oculus pre-hand scene depth");
               }

               return texture;
            }
         } catch (Throwable var4) {
            unavailable = true;
            LOGGER.warn("[CrysisScopeFix] Oculus pre-hand depth unavailable; keeping Minecraft scene depth", var4);
            return fallbackTexture;
         }
      } else {
         return fallbackTexture;
      }
   }

   public static int currentTextureOr(int fallbackTexture) {
      Object targets = targetsOrNull();
      if (targets == null) {
         return fallbackTexture;
      } else {
         try {
            int texture = (Integer)getCurrentDepthTexture.invoke(targets);
            return texture > 0 ? texture : fallbackTexture;
         } catch (Throwable var3) {
            unavailable = true;
            LOGGER.warn("[CrysisScopeFix] Oculus current hand depth unavailable; keeping Minecraft scene depth", var3);
            return fallbackTexture;
         }
      }
   }

   private static Object targetsOrNull() {
      if (NanoGeoShaderpackCompatibility.isShaderPackActive() && !unavailable) {
         try {
            resolve();
            Object manager = getPipelineManager.invoke(null);
            Object pipeline = getPipelineNullable.invoke(manager);
            return pipeline != null && "net.irisshaders.iris.pipeline.IrisRenderingPipeline".equals(pipeline.getClass().getName())
               ? renderTargets.get(pipeline)
               : null;
         } catch (Throwable var2) {
            unavailable = true;
            LOGGER.warn("[CrysisScopeFix] Oculus render targets unavailable; keeping Minecraft scene depth", var2);
            return null;
         }
      } else {
         return null;
      }
   }

   private static void resolve() throws ReflectiveOperationException {
      if (!resolved) {
         Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
         getPipelineManager = irisClass.getMethod("getPipelineManager");
         Class<?> managerClass = Class.forName("net.irisshaders.iris.pipeline.PipelineManager");
         getPipelineNullable = managerClass.getMethod("getPipelineNullable");
         Class<?> pipelineClass = Class.forName("net.irisshaders.iris.pipeline.IrisRenderingPipeline");
         renderTargets = pipelineClass.getDeclaredField("renderTargets");
         renderTargets.setAccessible(true);
         Class<?> targetsClass = Class.forName("net.irisshaders.iris.targets.RenderTargets");
         getDepthTextureNoHand = targetsClass.getMethod("getDepthTextureNoHand");
         getCurrentDepthTexture = targetsClass.getMethod("getDepthTexture");
         Class<?> depthTextureClass = Class.forName("net.irisshaders.iris.targets.DepthTexture");
         getTextureId = depthTextureClass.getMethod("getTextureId");
         resolved = true;
      }
   }
}
