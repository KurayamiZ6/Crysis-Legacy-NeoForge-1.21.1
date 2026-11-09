package io.github.painworks.crysismod.client.minimap.cache;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record NanoMinimapTileKey(ResourceKey<Level> dimension, int tileX, int tileZ) {
}
