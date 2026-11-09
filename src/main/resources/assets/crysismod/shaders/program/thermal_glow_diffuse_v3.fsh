#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform vec2 InSize;
uniform vec2 Direction;
uniform float EncodedHeightRangePx;
uniform float OuterGlowRatio;
uniform float GlowRadiusMultiplier;
uniform int ThermalQuality;

out vec4 fragColor;

float tapOffset(int distance) {
    if (distance <= 1) return 1.0;
    if (distance == 2) return 3.0;
    if (distance == 3) return 7.0;
    return 13.0;
}

float tapWeight(int distance) {
    if (distance <= 1) return 0.240;
    if (distance == 2) return 0.150;
    if (distance == 3) return 0.070;
    return 0.025;
}

float kernelNormalization(int maxTap) {
    float normalization = 0.320;
    if (maxTap >= 1) normalization += 0.480;
    if (maxTap >= 2) normalization += 0.300;
    if (maxTap >= 3) normalization += 0.140;
    if (maxTap >= 4) normalization += 0.050;
    return normalization;
}

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);
    if (center.g > 0.5) {
        fragColor = center;
        return;
    }

    int maxTap = ThermalQuality <= 1 ? 2 : (ThermalQuality == 2 ? 3 : 4);
    float maxRadius = ThermalQuality <= 1 ? 10.0 : (ThermalQuality == 2 ? 16.0 : 24.0);
    float normalization = kernelNormalization(maxTap);
    vec2 texel = Direction / InSize;
    float accumulatedGlow = center.b * 0.320;
    float strongestContribution = center.b * 0.320;
    float strongestScale = center.r;
    float strongestType = center.a;

    for (int tap = -4; tap <= 4; tap++) {
        int distanceIndex = tap < 0 ? -tap : tap;
        if (distanceIndex == 0 || distanceIndex > maxTap) continue;

        float distancePx = tapOffset(distanceIndex);
        vec4 sampleData = texture(DiffuseSampler, texCoord + texel * distancePx * sign(float(tap)));
        if (sampleData.a < 0.1) continue;

        float sourceScale = sampleData.g > 0.5 ? sampleData.b : sampleData.r;
        float sourceGlow = sampleData.g > 0.5 ? 1.0 : sampleData.b;
        if (sourceGlow <= 0.0) continue;

        float sourceRadius = clamp(
                sourceScale * EncodedHeightRangePx * OuterGlowRatio * GlowRadiusMultiplier,
                1.5,
                maxRadius
        );
        float contribution = sourceGlow
                * (1.0 - smoothstep(0.0, sourceRadius, distancePx))
                * tapWeight(distanceIndex);
        accumulatedGlow += contribution;
        if (contribution > strongestContribution) {
            strongestContribution = contribution;
            strongestScale = sourceScale;
            strongestType = sampleData.a;
        }
    }

    float glow = accumulatedGlow / normalization;
    fragColor = vec4(strongestScale, 0.0, clamp(glow * 1.35, 0.0, 1.0), strongestType);
}
