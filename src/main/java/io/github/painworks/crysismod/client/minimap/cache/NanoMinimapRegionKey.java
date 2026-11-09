package io.github.painworks.crysismod.client.minimap.cache;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record NanoMinimapRegionKey(ResourceKey<Level> dimension, int regionX, int regionZ) {
}
