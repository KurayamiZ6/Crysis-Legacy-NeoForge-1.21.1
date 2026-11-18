package io.github.painworks.crysismod.nanosuit.client.radial;

import io.github.painworks.crysismod.nanosuit.NanosuitMode;

/** Coordinates are in the V3 artwork's units, independently of GUI scale. */
public final class RadialLayout {
    public static final double ART_HEIGHT = 235;
    public static final double SCREEN_HEIGHT_FRACTION = .35;
    public static final double DEAD_ZONE = 42;

    private RadialLayout() {}

    public static double scale(int height, double factor) {
        return height * SCREEN_HEIGHT_FRACTION / ART_HEIGHT * factor;
    }

    public static NanosuitMode select(double dx, double dy) {
        if (dx * dx + dy * dy < DEAD_ZONE * DEAD_ZONE) return NanosuitMode.NONE;
        if (Math.abs(dx) > Math.abs(dy)) return dx > 0 ? NanosuitMode.STRENGTH : NanosuitMode.ARMOR;
        return dy > 0 ? NanosuitMode.CLOAK : NanosuitMode.SPEED;
    }

    public static float ringDegrees(double elapsedSeconds) {
        return (float) ((elapsedSeconds % 10.0) * 36.0);
    }

    public static float hoverFlash(double elapsedSeconds, double duration) {
        double remaining = Math.max(0, 1 - Math.max(0, elapsedSeconds) / duration);
        return (float) (remaining * remaining);
    }
}
