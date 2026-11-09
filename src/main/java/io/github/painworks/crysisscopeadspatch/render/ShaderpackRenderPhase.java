package io.github.painworks.crysisscopeadspatch.render;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public final class ShaderpackRenderPhase {
   private static volatile boolean resolved;
   private static ShaderpackRenderPhase.ShadowProbe shadowProbe = () -> false;
   private static volatile boolean packNameResolved;
   private static ShaderpackRenderPhase.PackNameProbe packNameProbe = () -> "";

   private ShaderpackRenderPhase() {
   }

   public static boolean isShadowPass() {
      if (!resolved) {
         synchronized (ShaderpackRenderPhase.class) {
            if (!resolved) {
               shadowProbe = resolveShadowProbe();
               resolved = true;
            }
         }
      }

      return shadowProbe.isShadowPass();
   }

   public static boolean usesComplementary() {
      if (!packNameResolved) {
         synchronized (ShaderpackRenderPhase.class) {
            if (!packNameResolved) {
               packNameProbe = resolvePackNameProbe();
               packNameResolved = true;
            }
         }
      }

      String name = packNameProbe.currentName();
      return name != null && name.toLowerCase(Locale.ROOT).contains("complementary");
   }

   private static ShaderpackRenderPhase.ShadowProbe resolveShadowProbe() {
      ShaderpackRenderPhase.ShadowProbe iris = resolveIrisApi("net.irisshaders.iris.api.v0.IrisApi");
      if (iris != null) {
         return iris;
      } else {
         iris = resolveIrisApi("net.coderbot.iris.api.v0.IrisApi");
         if (iris != null) {
            return iris;
         } else {
            ShaderpackRenderPhase.ShadowProbe optiFine = resolveOptiFine();
            return optiFine != null ? optiFine : () -> false;
         }
      }
   }

   private static ShaderpackRenderPhase.ShadowProbe resolveIrisApi(String className) {
      try {
         Class<?> apiClass = Class.forName(className, false, ShaderpackRenderPhase.class.getClassLoader());
         Method getInstance = apiClass.getMethod("getInstance");
         Object api = getInstance.invoke(null);
         Method shadowPass = apiClass.getMethod("isRenderingShadowPass");
         return () -> invokeBoolean(shadowPass, api);
      } catch (LinkageError | ReflectiveOperationException var5) {
         return null;
      }
   }

   private static ShaderpackRenderPhase.ShadowProbe resolveOptiFine() {
      try {
         Class<?> shaders = Class.forName("net.optifine.shaders.Shaders", false, ShaderpackRenderPhase.class.getClassLoader());
         Field shadowPass = shaders.getField("isShadowPass");
         return () -> readBoolean(shadowPass);
      } catch (LinkageError | ReflectiveOperationException var2) {
         return null;
      }
   }

   private static ShaderpackRenderPhase.PackNameProbe resolvePackNameProbe() {
      for (String className : new String[]{"net.irisshaders.iris.Iris", "net.coderbot.iris.Iris"}) {
         try {
            Class<?> iris = Class.forName(className, false, ShaderpackRenderPhase.class.getClassLoader());
            Method currentPackName = iris.getMethod("getCurrentPackName");
            return () -> invokeString(currentPackName);
         } catch (LinkageError | ReflectiveOperationException var6) {
         }
      }

      return () -> "";
   }

   private static boolean invokeBoolean(Method method, Object receiver) {
      try {
         return Boolean.TRUE.equals(method.invoke(receiver));
      } catch (RuntimeException | ReflectiveOperationException var3) {
         return false;
      }
   }

   private static boolean readBoolean(Field field) {
      try {
         return field.getBoolean(null);
      } catch (RuntimeException | ReflectiveOperationException var2) {
         return false;
      }
   }

   private static String invokeString(Method method) {
      try {
         return method.invoke(null) instanceof String string ? string : "";
      } catch (RuntimeException | ReflectiveOperationException var3) {
         return "";
      }
   }

   @FunctionalInterface
   private interface PackNameProbe {
      String currentName();
   }

   @FunctionalInterface
   private interface ShadowProbe {
      boolean isShadowPass();
   }
}
