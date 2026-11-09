package io.github.painworks.crysisscopeadspatch.debug;

import java.lang.reflect.Method;

final class ShaderpackStatus {
   private static volatile boolean resolved;
   private static ShaderpackStatus.Probe probe = new ShaderpackStatus.Probe() {
      @Override
      public boolean enabled() {
         return false;
      }

      @Override
      public String name() {
         return "unknown";
      }
   };

   private ShaderpackStatus() {
   }

   static boolean enabled() {
      resolve();
      return probe.enabled();
   }

   static String name() {
      resolve();
      String name = probe.name();
      return name != null && !name.isBlank() ? name : "unknown";
   }

   private static void resolve() {
      if (!resolved) {
         synchronized (ShaderpackStatus.class) {
            if (!resolved) {
               probe = resolveIris();
               resolved = true;
            }
         }
      }
   }

   private static ShaderpackStatus.Probe resolveIris() {
      for (String apiName : new String[]{"net.irisshaders.iris.api.v0.IrisApi", "net.coderbot.iris.api.v0.IrisApi"}) {
         try {
            Class<?> apiClass = Class.forName(apiName, false, ShaderpackStatus.class.getClassLoader());
            Method getInstance = apiClass.getMethod("getInstance");
            final Object api = getInstance.invoke(null);
            final Method isShaderPackInUse = apiClass.getMethod("isShaderPackInUse");
            final Method packName = findPackNameMethod(apiName);
            return new ShaderpackStatus.Probe() {
               @Override
               public boolean enabled() {
                  try {
                     return Boolean.TRUE.equals(isShaderPackInUse.invoke(api));
                  } catch (RuntimeException | ReflectiveOperationException var2) {
                     return false;
                  }
               }

               @Override
               public String name() {
                  if (packName == null) {
                     return this.enabled() ? "enabled (name unavailable)" : "disabled";
                  } else {
                     try {
                        return packName.invoke(null) instanceof String string ? string : "unknown";
                     } catch (RuntimeException | ReflectiveOperationException var3) {
                        return "unknown";
                     }
                  }
               }
            };
         } catch (LinkageError | ReflectiveOperationException var9) {
         }
      }

      return new ShaderpackStatus.Probe() {
         @Override
         public boolean enabled() {
            return false;
         }

         @Override
         public String name() {
            return "Iris/Oculus API unavailable";
         }
      };
   }

   private static Method findPackNameMethod(String apiName) {
      String irisName = apiName.startsWith("net.irisshaders") ? "net.irisshaders.iris.Iris" : "net.coderbot.iris.Iris";

      try {
         Class<?> iris = Class.forName(irisName, false, ShaderpackStatus.class.getClassLoader());
         return iris.getMethod("getCurrentPackName");
      } catch (LinkageError | ReflectiveOperationException var3) {
         return null;
      }
   }

   private interface Probe {
      boolean enabled();

      String name();
   }
}
