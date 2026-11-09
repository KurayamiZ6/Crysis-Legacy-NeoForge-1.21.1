package io.github.painworks.crysismod.client.nanogeo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

public record NanoGeoRenderContext(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
}
