package io.github.painworks.crysismod.client.nanovision.thermal;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.logging.LogUtils;
import io.github.painworks.crysismod.client.nanovision.NanovisionConfig;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;

public final class ThermalThirdPersonHeldItemCapture {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int ROUTE_TRACE_LIMIT = 20;
   private static final ThreadLocal<ThermalThirdPersonHeldItemCapture.CaptureState> ACTIVE_CAPTURE = new ThreadLocal<>();
   private static final Set<String> LOGGED_ROUTE_SIGNATURES = new LinkedHashSet<>();

   private ThermalThirdPersonHeldItemCapture() {
   }

   public static void beginLayer(LivingEntity holder) {
      ACTIVE_CAPTURE.remove();
      if (holder != null
         && (!holder.getMainHandItem().isEmpty() || !holder.getOffhandItem().isEmpty())
         && !ThermalMarkerCompatibilityRenderer.isEntityReplayActive()
         && NanovisionConfig.thermalHeldItemsEnabled()) {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer viewer = minecraft.player;
         if (ThermalMarkerEntityRenderer.shouldRenderMarker(viewer, holder)) {
            float encodedHeight = ThermalScreenProjection.encodedProjectedHeight(
               Math.max(0.45, holder.getBbHeight() * 0.3), minecraft.gameRenderer.getMainCamera().getPosition().distanceTo(holder.getBoundingBox().getCenter())
            );
            ACTIVE_CAPTURE.set(
               new ThermalThirdPersonHeldItemCapture.CaptureState(
                  encodedHeight,
                  ThermalMobProfile.GENERIC_COLD,
                  String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(holder.getType())),
                  String.valueOf(BuiltInRegistries.ITEM.getKey(holder.getMainHandItem().getItem())),
                  String.valueOf(BuiltInRegistries.ITEM.getKey(holder.getOffhandItem().getItem()))
               )
            );
         }
      }
   }

   public static MultiBufferSource wrap(MultiBufferSource original) {
      ThermalThirdPersonHeldItemCapture.CaptureState state = ACTIVE_CAPTURE.get();
      return (MultiBufferSource)(state != null && original != null && !(original instanceof ThermalThirdPersonHeldItemCapture.ThermalHeldItemBufferSource)
         ? new ThermalThirdPersonHeldItemCapture.ThermalHeldItemBufferSource(original, state)
         : original);
   }

   public static VertexConsumer wrapTaczBedrockConsumer(MultiBufferSource source, RenderType renderType, VertexConsumer original) {
      ThermalThirdPersonHeldItemCapture.CaptureState state = ACTIVE_CAPTURE.get();
      return state != null && source != null && !(source instanceof ThermalHeldItemBufferSource) && original != null && !isNonModelRoute(renderType)
         ? teeTexturedMarker(original, renderType, state)
         : original;
   }

   public static VertexConsumer wrapScopeDeferredTaczConsumer(VertexConsumer original) {
      ThermalThirdPersonHeldItemCapture.CaptureState state = ACTIVE_CAPTURE.get();
      if (state != null && original != null) {
         ThermalMarkerRenderTarget.markSourceRendered();
         VertexConsumer marker = new ThermalEncodedVertexConsumer(
            ThermalMarkerBufferSource.fallback(ThermalMarkerRenderTypes.markerSolid()), DefaultVertexFormat.NEW_ENTITY, state.encodedHeight(), state.profile()
         );
         return VertexMultiConsumer.create(original, marker);
      } else {
         return original;
      }
   }

   public static void endLayer() {
      ACTIVE_CAPTURE.remove();
   }

   private static void traceRoute(ThermalThirdPersonHeldItemCapture.CaptureState state, RenderType physicalType, RenderType markerType, String route) {
      if (state != null && physicalType != null) {
         String signature = route
            + "|"
            + state.holderType()
            + "|"
            + state.mainHandItem()
            + "|"
            + state.offhandItem()
            + "|"
            + physicalType.mode()
            + "|"
            + physicalType.format()
            + "|"
            + physicalType;
         synchronized (LOGGED_ROUTE_SIGNATURES) {
            if (LOGGED_ROUTE_SIGNATURES.contains(signature) || LOGGED_ROUTE_SIGNATURES.size() >= 20) {
               return;
            }

            LOGGED_ROUTE_SIGNATURES.add(signature);
         }

         LOGGER.info(
            "[CrysisMod][Nanovision][ThirdPersonItemAlpha] route={} holder={} main={} offhand={} mode={} format={} alphaMarker={} source={}",
            new Object[]{
               route,
               state.holderType(),
               state.mainHandItem(),
               state.offhandItem(),
               physicalType.mode(),
               physicalType.format(),
               markerType == null ? "rejected" : markerType,
               physicalType
            }
         );
      }
   }

   private static boolean isNonModelRoute(RenderType renderType) {
      String name = String.valueOf(renderType).toLowerCase(Locale.ROOT);
      return name.contains("shadow") || name.contains("leash") || name.contains("glint") || name.contains("debug") || name.contains("beam");
   }

   private static VertexConsumer teeTexturedMarker(VertexConsumer original, RenderType physicalType, ThermalThirdPersonHeldItemCapture.CaptureState state) {
      RenderType markerType = ThermalMarkerRenderTypes.alphaAwareMarker(physicalType);
      traceRoute(state, physicalType, markerType, "direct");
      if (markerType == null) {
         return original;
      } else {
         ThermalMarkerRenderTarget.markSourceRendered();
         VertexConsumer marker = new ThermalEncodedVertexConsumer(
            ThermalMarkerBufferSource.fallback(markerType), physicalType.format(), state.encodedHeight(), state.profile()
         );
         return VertexMultiConsumer.create(original, marker);
      }
   }

   private record CaptureState(float encodedHeight, ThermalMobProfile profile, String holderType, String mainHandItem, String offhandItem) {
   }

   private static final class ThermalHeldItemBufferSource implements MultiBufferSource {
      private final MultiBufferSource delegate;
      private final ThermalThirdPersonHeldItemCapture.CaptureState state;
      private final Map<RenderType, VertexConsumer> markerConsumers = new IdentityHashMap<>();

      private ThermalHeldItemBufferSource(MultiBufferSource delegate, ThermalThirdPersonHeldItemCapture.CaptureState state) {
         this.delegate = delegate;
         this.state = state;
      }

      public VertexConsumer getBuffer(RenderType renderType) {
         VertexConsumer original = this.delegate.getBuffer(renderType);
         if (original != null && !ThermalThirdPersonHeldItemCapture.isNonModelRoute(renderType)) {
            VertexConsumer marker = this.markerConsumers.get(renderType);
            if (marker == null) {
               RenderType markerType = ThermalMarkerRenderTypes.alphaAwareMarker(renderType);
               ThermalThirdPersonHeldItemCapture.traceRoute(this.state, renderType, markerType, "item_in_hand_layer");
               if (markerType == null) {
                  return original;
               }

               marker = new ThermalEncodedVertexConsumer(
                  ThermalMarkerBufferSource.fallback(markerType), renderType.format(), this.state.encodedHeight(), this.state.profile()
               );
               this.markerConsumers.put(renderType, marker);
            }

            ThermalMarkerRenderTarget.markSourceRendered();
            return VertexMultiConsumer.create(original, marker);
         } else {
            return original;
         }
      }
   }
}
