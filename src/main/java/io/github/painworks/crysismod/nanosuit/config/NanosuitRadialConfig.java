package io.github.painworks.crysismod.nanosuit.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client presentation only; never used to decide whether a mode is allowed. */
public final class NanosuitRadialConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.DoubleValue SCALE = BUILDER.defineInRange("scale", 1.0, 0.4, 2.0);
    public static final ModConfigSpec.DoubleValue BACKGROUND_ALPHA = BUILDER.defineInRange("backgroundAlpha", .30, 0, 1);
    public static final ModConfigSpec.DoubleValue HOVER_BACKGROUND_ALPHA = BUILDER.defineInRange("hoverBackgroundAlpha", .48, 0, 1);
    public static final ModConfigSpec.DoubleValue HEX_ALPHA = BUILDER.defineInRange("hexAlpha", .62, 0, 1);
    public static final ModConfigSpec.DoubleValue HOVER_FLASH_SECONDS = BUILDER.defineInRange("hoverFlashSeconds", .30, .05, 1.0);
    public static final ModConfigSpec.DoubleValue HOVER_FLASH_INTENSITY = BUILDER.defineInRange("hoverFlashIntensity", .65, 0, 1);
    public static final ModConfigSpec SPEC = BUILDER.build();

    private NanosuitRadialConfig() {}
}
