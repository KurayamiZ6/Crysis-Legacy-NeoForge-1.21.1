#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform vec2 Direction;
uniform float EncodedHeightRangePx;
uniform float EdgeWidthRatio;
uniform float OuterGlowRatio;
uniform float GlowRadiusMultiplier;
uniform int ThermalQuality;
in vec2 texCoord;
out vec4 fragColor;

float offsetFor(int i) {
    if (i == 1) return 1.0;
    if (i == 2) return 3.0;
    if (i == 3) return 6.0;
    return 10.0;
}

float weightFor(int i) {
    if (i == 0) return 0.30;
    if (i == 1) return 0.24;
    if (i == 2) return 0.15;
    if (i == 3) return 0.075;
    return 0.03;
}

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);
    vec2 texel = Direction / InSize;
    int limit = ThermalQuality <= 1 ? 2 : (ThermalQuality == 2 ? 3 : 4);

    if (center.g > 0.5) {
        // The diffused geometry field also drives the WARM spatial layer
        // coordinate. Keep it broad enough for every configured color width
        // to remain visible between the silhouette and the hot center.
        float targetRadius = clamp(center.b * EncodedHeightRangePx * EdgeWidthRatio * 0.28, 4.0, 48.0);
        float radiusScale = targetRadius / 10.0;
        float sum = center.r * weightFor(0);
        float weights = weightFor(0);
        for (int tap = -4; tap <= 4; tap++) {
            int d = tap < 0 ? -tap : tap;
            if (d == 0 || d > limit) continue;
            vec4 sampleData = texture(DiffuseSampler, texCoord + texel * offsetFor(d) * radiusScale * sign(float(tap)));
            float sameType = sampleData.g * (1.0 - step(0.075, abs(sampleData.a - center.a)));
            float w = weightFor(d) * sameType;
            sum += sampleData.r * w;
            weights += w;
        }
        float softEdge = clamp(sum / max(weights, 0.0001) * 1.45, 0.0, 1.0);
        fragColor = vec4(max(center.r, softEdge), 1.0, center.b, center.a);
        return;
    }

    float accumulated = center.b * weightFor(0);
    float strongest = accumulated;
    float sourceScale = center.r;
    float sourceType = center.a;
    for (int tap = -4; tap <= 4; tap++) {
        int d = tap < 0 ? -tap : tap;
        if (d == 0 || d > limit) continue;
        float distancePx = offsetFor(d);
        vec4 sampleData = texture(DiffuseSampler, texCoord + texel * distancePx * sign(float(tap)));
        float scale = sampleData.g > 0.5 ? sampleData.b : sampleData.r;
        float glowSeed = sampleData.g > 0.5 ? 1.0 : sampleData.b;
        if (sampleData.a < 0.1 || glowSeed <= 0.0) continue;
        float radius = clamp(scale * EncodedHeightRangePx * OuterGlowRatio * GlowRadiusMultiplier, 1.5, 24.0);
        float contribution = glowSeed * (1.0 - smoothstep(0.0, radius, distancePx)) * weightFor(d);
        accumulated += contribution;
        if (contribution > strongest) {
            strongest = contribution;
            sourceScale = scale;
            sourceType = sampleData.a;
        }
    }
    fragColor = vec4(sourceScale, 0.0, clamp(accumulated * 1.45, 0.0, 1.0), sourceType);
}
