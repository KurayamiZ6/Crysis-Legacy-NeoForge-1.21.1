package io.github.painworks.crysismod.client.nanovision.thermal;

import java.lang.reflect.Method;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.fml.ModList;

final class ThermalExternalModelCompatibility {
   private static final String EMF_MOD_ID = "entity_model_features";
   private static final String EMF_MODEL_INTERFACE = "traben.entity_model_features.models.IEMFModel";
   private static volatile boolean emfAccessResolved;
   private static volatile ThermalExternalModelCompatibility.EmfAccess emfAccess;

   private ThermalExternalModelCompatibility() {
   }

   static boolean requiresFullRendererFallback(Object model) {
      if (model != null && isEmfLoaded()) {
         ThermalExternalModelCompatibility.EmfAccess access = emfAccess();
         return access != null && access.isCustomModel(model);
      } else {
         return false;
      }
   }

   static RenderType unwrapIrisRenderType(RenderType type) {
      Method unwrap = IrisRenderType.UNWRAP;
      if (unwrap != null) {
         try {
            for (int depth = 0; depth < 4 && unwrap.getDeclaringClass().isInstance(type); depth++) {
               if (!(unwrap.invoke(type) instanceof RenderType inner) || inner == type) {
                  break;
               }
               type = inner;
            }
         } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            // Leave unsupported render types to the replay buffer's normal rejection path.
         }
      }
      return type;
   }

   private static final class IrisRenderType {
      private static final Method UNWRAP = resolve();

      private static Method resolve() {
         try {
            ClassLoader loader = ThermalExternalModelCompatibility.class.getClassLoader();
            return Class.forName("net.irisshaders.batchedentityrendering.impl.WrappableRenderType", false, loader).getMethod("unwrap");
         } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return null;
         }
      }
   }

   private static boolean isEmfLoaded() {
      try {
         return ModList.get().isLoaded("entity_model_features");
      } catch (RuntimeException var1) {
         return false;
      }
   }

   private static ThermalExternalModelCompatibility.EmfAccess emfAccess() {
      if (!emfAccessResolved) {
         synchronized (ThermalExternalModelCompatibility.class) {
            if (!emfAccessResolved) {
               emfAccess = resolveEmfAccess();
               emfAccessResolved = true;
            }
         }
      }

      return emfAccess;
   }

   private static ThermalExternalModelCompatibility.EmfAccess resolveEmfAccess() {
      try {
         ClassLoader loader = ThermalExternalModelCompatibility.class.getClassLoader();
         Class<?> modelType = Class.forName("traben.entity_model_features.models.IEMFModel", false, loader);
         Method isEmfModel = modelType.getMethod("emf$isEMFModel");
         return new ThermalExternalModelCompatibility.EmfAccess(modelType, isEmfModel);
      } catch (LinkageError | ReflectiveOperationException var3) {
         return null;
      }
   }

   private record EmfAccess(Class<?> modelType, Method isEmfModel) {
      private boolean isCustomModel(Object model) {
         if (!this.modelType.isInstance(model)) {
            return false;
         } else {
            try {
               return Boolean.TRUE.equals(this.isEmfModel.invoke(model));
            } catch (RuntimeException | ReflectiveOperationException var3) {
               return false;
            }
         }
      }
   }
}
