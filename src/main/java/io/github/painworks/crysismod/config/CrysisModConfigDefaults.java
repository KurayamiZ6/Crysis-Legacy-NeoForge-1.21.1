package io.github.painworks.crysismod.config;

import io.github.painworks.crysismod.client.minimap.config.NanoMinimapClientConfig;
import io.github.painworks.crysismod.client.nanogeo.NanoGeoClientConfig;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import io.github.painworks.crysismod.nanosuit.config.NanosuitArmorIdentityConfig;
import io.github.painworks.crysismod.nanosuit.config.NanosuitClientConfig;
import io.github.painworks.crysismod.nanosuit.config.NanosuitServerConfig;
import io.github.painworks.crysismod.nanovisor.config.NanoVisorClientConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public final class CrysisModConfigDefaults {
   private CrysisModConfigDefaults() {
   }

   public static CrysisModConfigDefaults.ResetResult resetAllRegisteredConfigs() {
      List<String> errors = new ArrayList<>();
      int resetValues = 0;
      resetValues += resetConfigClass(NanosuitClientConfig.class, errors);
      resetValues += resetConfigClass(NanoVisorClientConfig.class, errors);
      resetValues += resetConfigClass(NanovisionConfig.class, errors);
      resetValues += resetConfigClass(NanoMinimapClientConfig.class, errors);
      resetValues += resetConfigClass(NanoGeoClientConfig.class, errors);
      resetValues += resetConfigClass(NanosuitArmorIdentityConfig.class, errors);
      resetValues += resetConfigClass(NanosuitServerConfig.class, errors);
      NanosuitServerConfig.resetCachedValuesToDefaults();
      return new CrysisModConfigDefaults.ResetResult(resetValues, List.copyOf(errors));
   }

   private static int resetConfigClass(Class<?> configClass, List<String> errors) {
      int resetValues = 0;
      ConfigValue saveAnchor = null;

      for (Field field : configClass.getDeclaredFields()) {
         if (Modifier.isStatic(field.getModifiers())) {
            try {
               field.setAccessible(true);
               if (field.get(null) instanceof ConfigValue configValue) {
                  Field defaultField = configClass.getDeclaredField("DEFAULT_" + field.getName());
                  defaultField.setAccessible(true);
                  configValue.set(defaultField.get(null));
                  saveAnchor = configValue;
                  resetValues++;
               }
            } catch (NoSuchFieldException var12) {
            } catch (ReflectiveOperationException | RuntimeException var13) {
               errors.add(configClass.getSimpleName() + "." + field.getName() + ": " + var13.getMessage());
            }
         }
      }

      if (saveAnchor != null) {
         try {
            saveAnchor.save();
         } catch (RuntimeException var11) {
            errors.add(configClass.getSimpleName() + ": save failed: " + var11.getMessage());
         }
      }

      return resetValues;
   }

   public record ResetResult(int resetValues, List<String> errors) {
      public boolean success() {
         return this.errors.isEmpty();
      }
   }
}
