package io.github.painworks.crysisscopeadspatch.mixin;

import io.github.painworks.crysisscopeadspatch.debug.NanovisionComparativeProfiler;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 800)
public abstract class LevelRendererComparativeProfilerMixin {
   private NanovisionComparativeProfiler.Token crysisscopefix$worldProfile;

   @Inject(method = "renderLevel", at = @At("HEAD"), require = 1)
   private void crysisscopefix$beginWorldProfile(
      DeltaTracker deltaTracker,
      boolean renderBlockOutline,
      Camera camera,
      GameRenderer gameRenderer,
      LightTexture lightTexture,
      Matrix4f viewMatrix,
      Matrix4f projectionMatrix,
      CallbackInfo ci
   ) {
      this.crysisscopefix$worldProfile = NanovisionComparativeProfiler.beginSection(NanovisionComparativeProfiler.Section.WORLD_TOTAL);
   }

   @Inject(method = "renderLevel", at = @At("RETURN"), require = 1)
   private void crysisscopefix$endWorldProfile(
      DeltaTracker deltaTracker,
      boolean renderBlockOutline,
      Camera camera,
      GameRenderer gameRenderer,
      LightTexture lightTexture,
      Matrix4f viewMatrix,
      Matrix4f projectionMatrix,
      CallbackInfo ci
   ) {
      NanovisionComparativeProfiler.endSection(this.crysisscopefix$worldProfile, true);
      this.crysisscopefix$worldProfile = null;
   }
}
