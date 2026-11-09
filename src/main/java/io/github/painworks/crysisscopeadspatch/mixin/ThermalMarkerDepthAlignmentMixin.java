package io.github.painworks.crysisscopeadspatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.painworks.crysismod.client.nanovision.thermal.ThermalLayerProfile;
import io.github.painworks.crysisscopeadspatch.render.ShaderpackRenderPhase;
import io.github.painworks.crysisscopeadspatch.render.ThermalMarkerDepthFlagVertexConsumer;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "io.github.painworks.crysismod.client.nanovision.thermal.ThermalMarkerModelRenderer", remap = false, priority = 1100)
public abstract class ThermalMarkerDepthAlignmentMixin {
   private static final int COMPLEMENTARY_CHICKEN_DEPTH = 1;
   private static final int VIEW_OFFSET_Z_LAYERING = 2;
   private static final ThreadLocal<Integer> DEPTH_FLAGS = ThreadLocal.withInitial(() -> 0);
   private static final Map<EntityModel<?>, Boolean> VIEW_OFFSET_MODE = Collections.synchronizedMap(new WeakHashMap<>());

   @Inject(method = "renderTexturedMarkerInternal", at = @At("HEAD"), require = 1, remap = false)
   private static void crysisscopefix$capturePhysicalDepthMode(
      EntityModel<?> model,
      LivingEntity entity,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      ResourceLocation texture,
      ThermalLayerProfile profile,
      float encodedHeight,
      boolean trace,
      CallbackInfoReturnable<?> cir
   ) {
      int flags = 0;
      if (VIEW_OFFSET_MODE.computeIfAbsent(model, ignored -> crysisscopefix$usesViewOffset(model, texture))) {
         flags |= 2;
      }

      ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
      if (entityId != null
         && "minecraft".equals(entityId.getNamespace())
         && ("chicken".equals(entityId.getPath()) || "slime".equals(entityId.getPath()))
         && ShaderpackRenderPhase.usesComplementary()) {
         flags |= 1;
      }

      DEPTH_FLAGS.set(flags);
   }

   private static boolean crysisscopefix$usesViewOffset(EntityModel<?> model, ResourceLocation texture) {
      String physicalRenderType = String.valueOf(model.renderType(texture)).toLowerCase(Locale.ROOT);
      return physicalRenderType.contains("view_offset_z_layering") || physicalRenderType.contains("entity_cutout_no_cull_z_offset");
   }

   @ModifyArg(
      method = "renderTexturedMarkerInternal",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
         remap = true
      ),
      index = 1,
      require = 1
   )
   private static VertexConsumer crysisscopefix$carryDepthMode(VertexConsumer original) {
      int flags = DEPTH_FLAGS.get();
      return (VertexConsumer)(flags == 0 ? original : new ThermalMarkerDepthFlagVertexConsumer(original, flags));
   }

   @Inject(method = "renderTexturedMarkerInternal", at = @At("RETURN"), require = 1, remap = false)
   private static void crysisscopefix$clearPhysicalDepthMode(CallbackInfoReturnable<?> cir) {
      DEPTH_FLAGS.remove();
   }
}
