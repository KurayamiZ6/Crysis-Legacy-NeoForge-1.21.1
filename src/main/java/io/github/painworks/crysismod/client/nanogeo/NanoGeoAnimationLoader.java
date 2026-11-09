package io.github.painworks.crysismod.client.nanogeo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class NanoGeoAnimationLoader {
   public Map<String, NanoGeoAnimation> loadAnimations(ResourceManager resourceManager, ResourceLocation animationResource) {
      try {
         Resource resource = (Resource)resourceManager.getResource(animationResource)
            .orElseThrow(() -> new IllegalStateException("Animation resource not found: " + animationResource));

         Map var6;
         try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (!root.has("animations") || !root.get("animations").isJsonObject()) {
               return Collections.emptyMap();
            }

            var6 = Collections.emptyMap();
         }

         return var6;
      } catch (IOException var9) {
         throw new IllegalStateException("Failed to load animation resource " + animationResource + ": " + var9.getMessage(), var9);
      }
   }
}
