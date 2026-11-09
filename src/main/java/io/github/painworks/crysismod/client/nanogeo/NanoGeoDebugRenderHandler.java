package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderPlayerEvent.Post;
import net.neoforged.bus.api.SubscribeEvent;
import org.slf4j.Logger;

public final class NanoGeoDebugRenderHandler {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ResourceLocation DEBUG_MODEL = ResourceLocation.fromNamespaceAndPath("crysismod", "nanogeo/nanosuit_armor.json");
   private static final ResourceLocation DEBUG_TEXTURE = ResourceLocation.fromNamespaceAndPath("crysismod", "textures/armor/nanosuit_texture.png");
   private final NanoGeoRenderer renderer = new NanoGeoRenderer();
   private final NanoGeoTextureCache textureCache = new NanoGeoTextureCache();
   private boolean loggedRenderType;

   @SubscribeEvent
   public void onRenderPlayerPost(Post event) {
      if (NanoGeoClientConfig.nanoGeoDebugRenderEnabled()) {
         Player player = event.getEntity();
         if (player != null && player == Minecraft.getInstance().player) {
            NanoGeoBakedModel bakedModel = NanoGeoBakedModelCache.INSTANCE.getBakedModelOrNull(DEBUG_MODEL);
            if (bakedModel != null) {
               PoseStack poseStack = event.getPoseStack();
               MultiBufferSource bufferSource = event.getMultiBufferSource();
               boolean ownedProfilerFrame = NanoGeoProfiler.INSTANCE.beginFrameIfNeeded(NanoGeoProfiler.PassType.STATIC);
               ResourceLocation texture = NanoGeoClientConfig.nanoGeoDebugUv() && NanoGeoClientConfig.nanoGeoDebugUvRenderChecker()
                  ? MissingTextureAtlasSprite.getLocation()
                  : DEBUG_TEXTURE;
               NanoGeoClientConfig.NanoGeoDebugRenderType renderType = NanoGeoClientConfig.nanoGeoDebugRenderType();
               long textureLookupSection = NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.TEXTURE_LOOKUP);
               RenderType resolvedRenderType = this.textureCache.getOrCreateRenderType(texture, renderType);
               NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.TEXTURE_LOOKUP, textureLookupSection);
               long bufferLookupSection = NanoGeoProfiler.INSTANCE.beginSection(NanoGeoProfilerSection.BUFFER_LOOKUP);
               VertexConsumer vertexConsumer = bufferSource.getBuffer(resolvedRenderType);
               NanoGeoProfiler.INSTANCE.incrementCounter(NanoGeoProfiler.Counter.RENDERTYPE_LOOKUPS);
               NanoGeoProfiler.INSTANCE.endSection(NanoGeoProfilerSection.BUFFER_LOOKUP, bufferLookupSection);
               if (NanoGeoClientConfig.nanoGeoDebugLogResources() && !this.loggedRenderType) {
                  this.loggedRenderType = true;
                  LOGGER.info("[CrysisMod][NanoGeo] Debug static render model={} texture={} renderType={}", new Object[]{DEBUG_MODEL, texture, renderType});
               }

               poseStack.pushPose();
               poseStack.translate(0.7, 0.0, 0.0);
               this.renderer
                  .renderStatic(
                     bakedModel, new NanoGeoRenderContext(poseStack, bufferSource, event.getPackedLight(), 0), vertexConsumer, 1.0F, 1.0F, 1.0F, 1.0F
                  );
               poseStack.popPose();
               NanoGeoProfiler.INSTANCE.endFrameIfOwned(ownedProfilerFrame);
            }
         }
      }
   }
}
