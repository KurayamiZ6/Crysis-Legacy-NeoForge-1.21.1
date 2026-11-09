package io.github.painworks.crysismod.nanosuit.client.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanovision.NanovisionState;
import io.github.painworks.crysismod.mixin.client.nanovision.RenderTypeCompositeAccessor;
import io.github.painworks.crysismod.mixin.client.nanovision.RenderTypeCompositeStateAccessor;
import io.github.painworks.crysismod.mixin.client.nanovision.TextureStateShardAccessor;
import io.github.painworks.crysismod.nanosuit.NanosuitMode;
import io.github.painworks.crysismod.nanosuit.energy.NanosuitCapabilities;
import io.github.painworks.crysismod.nanosuit.util.NanosuitArmorUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;

/** Routes real animated geometry into the cloak surface and suppresses unsupported layers. */
public final class NanosuitCloakRender {
   private static final int MAX_TEXTURES_PER_DRAW = 32;
   private static final VertexConsumer HIDDEN = new HiddenConsumer();
   private static final CloakBuffers HANDS = new CloakBuffers(true);
   private static final CloakBuffers BODY = new CloakBuffers(false);
   private static CloakBuffers activeBuffers;
   private static boolean warned;

   private NanosuitCloakRender() {
   }

   public static boolean isActive(AbstractClientPlayer player) {
      if (player == null || player.isSpectator() || !NanosuitArmorUtil.hasFullSet(player)) {
         return false;
      }
      if (player instanceof LocalPlayer localPlayer) {
         var suit = NanosuitCapabilities.get(localPlayer);
         return suit.getMode() == NanosuitMode.CLOAK && suit.getEnergy() > 0.0F;
      }
      // The server synchronizes the invisible flag for other players; vanilla potions stay independent.
      return player.isInvisible() && !player.hasEffect(MobEffects.INVISIBILITY);
   }

   public static boolean isDrawing() {
      return activeBuffers != null;
   }

   public static boolean keepsArmGeometry(AbstractClientPlayer player) {
      return isDrawing() || isActive(player) && NanovisionState.isActive();
   }

   public static void draw(boolean hands, java.util.function.Consumer<BufferSource> renderer) {
      if (activeBuffers != null) {
         renderer.accept(activeBuffers);
         return;
      }
      CloakBuffers buffers = hands ? HANDS : BODY;
      buffers.surface = NanosuitCloakRefraction.prepare(hands);
      buffers.defer = buffers.surface && NanosuitCloakRefraction.isDeferring();
      activeBuffers = buffers;
      try {
         renderer.accept(buffers);
         buffers.endBatch();
         if (hands && buffers.surface) {
            NanosuitCloakRefraction.finishHands();
         }
      } finally {
         buffers.reset();
         activeBuffers = null;
      }
   }

   private static ResourceLocation texture(RenderType type) {
      if (type instanceof RenderTypeCompositeAccessor composite
         && (Object)composite.crysismod$getState() instanceof RenderTypeCompositeStateAccessor state
         && (Object)state.crysismod$getTextureState() instanceof TextureStateShardAccessor texture) {
         return texture.crysismod$cutoutTexture().orElse(null);
      }
      return null;
   }

   private static final class CloakBuffers extends BufferSource {
      private final List<ByteBufferBuilder> storage = new ArrayList<>();
      private final boolean hands;
      private boolean surface;
      private boolean defer;

      private CloakBuffers(boolean hands) {
         super(new ByteBufferBuilder(256), new LinkedHashMap<>());
         this.hands = hands;
      }

      @Override
      public VertexConsumer getBuffer(RenderType type) {
         RenderType mapped = map(type);
         if (mapped == null) {
            return HIDDEN;
         }
         if (!this.fixedBuffers.containsKey(mapped)) {
            int index = this.fixedBuffers.size();
            if (index >= MAX_TEXTURES_PER_DRAW) {
               return HIDDEN;
            }
            if (index == this.storage.size()) {
               this.storage.add(new ByteBufferBuilder(2048));
            }
            this.fixedBuffers.put(mapped, this.storage.get(index));
         }
         return new CloakVertexConsumer(super.getBuffer(mapped), type.format());
      }

      private RenderType map(RenderType type) {
         if (!this.surface) {
            return null;
         }
         ResourceLocation texture = texture(type);
         if (texture == null || type.mode() != VertexFormat.Mode.QUADS || !type.format().hasUV(0)
            || type.format() == com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX
            || texture.getPath().contains("glint")) {
            return null;
         }
         return NanosuitCloakRefraction.maskType(texture);
      }

      @Override
      public void endBatch(RenderType type) {
         // BufferSource.endBatch() iterates the already mapped keys. Other renderers may ask to flush an original type.
         RenderType mapped = this.fixedBuffers.containsKey(type) ? type : map(type);
         if (mapped != null) {
            try {
               if (this.defer) {
                  var builder = this.startedBuilders.remove(mapped);
                  if (builder != null) {
                     var mesh = builder.build();
                     if (mesh != null) {
                        NanosuitCloakRefraction.deferBatch(this.hands, mapped, mesh);
                     }
                  }
               } else {
                  super.endBatch(mapped);
               }
            } catch (RuntimeException exception) {
               if (this.defer) {
                  NanosuitCloakRefraction.discardDeferred();
               }
               ByteBufferBuilder backing = this.fixedBuffers.get(mapped);
               if (backing != null) {
                  backing.clear();
               }
               if (!warned) {
                  warned = true;
                  LogUtils.getLogger().warn("An unsupported render layer was hidden during Nanosuit camouflage", exception);
               }
            }
         }
      }

      private void reset() {
         if (!this.startedBuilders.isEmpty()) {
            if (this.defer) {
               NanosuitCloakRefraction.discardDeferred();
            }
            for (ByteBufferBuilder backing : this.fixedBuffers.values()) {
               backing.clear();
            }
         }
         this.startedBuilders.clear();
         this.fixedBuffers.clear();
         this.lastSharedType = null;
         this.surface = false;
         this.defer = false;
      }
   }

   private static final class CloakVertexConsumer implements VertexConsumer {
      private final VertexConsumer output;
      private final VertexFormat format;

      private CloakVertexConsumer(VertexConsumer output, VertexFormat format) {
         this.output = output;
         this.format = format;
      }

      @Override
      public VertexConsumer addVertex(float x, float y, float z) {
         this.output.addVertex(x, y, z);
         if (!this.format.hasColor()) {
            this.setColor(255, 255, 255, 255);
         }
         if (!this.format.hasUV(1)) {
            this.output.setOverlay(OverlayTexture.NO_OVERLAY);
         }
         if (!this.format.hasUV(2)) {
            this.output.setLight(LightTexture.FULL_BRIGHT);
         }
         if (!this.format.hasNormal()) {
            this.output.setNormal(0.0F, 1.0F, 0.0F);
         }
         return this;
      }

      @Override
      public VertexConsumer setColor(int red, int green, int blue, int alpha) {
         this.output.setColor(red, green, blue, alpha);
         return this;
      }

      @Override
      public VertexConsumer setUv(float u, float v) { this.output.setUv(u, v); return this; }

      @Override
      public VertexConsumer setUv1(int u, int v) { this.output.setUv1(u, v); return this; }

      @Override
      public VertexConsumer setUv2(int u, int v) { this.output.setUv2(u, v); return this; }

      @Override
      public VertexConsumer setNormal(float x, float y, float z) { this.output.setNormal(x, y, z); return this; }
   }

   private static final class HiddenConsumer implements VertexConsumer {
      @Override
      public VertexConsumer addVertex(float x, float y, float z) { return this; }
      @Override
      public VertexConsumer setColor(int red, int green, int blue, int alpha) { return this; }
      @Override
      public VertexConsumer setUv(float u, float v) { return this; }
      @Override
      public VertexConsumer setUv1(int u, int v) { return this; }
      @Override
      public VertexConsumer setUv2(int u, int v) { return this; }
      @Override
      public VertexConsumer setNormal(float x, float y, float z) { return this; }
   }
}
