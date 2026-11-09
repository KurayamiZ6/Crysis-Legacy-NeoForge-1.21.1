#version 150

uniform sampler2D MaskSampler;
uniform vec2 InSize;
uniform float GlowRadiusPx;
uniform float GlowStrength;

in vec2 texCoord0;
out vec4 fragColor;

float maskAt(vec2 offsetPx) {
    return texture(MaskSampler, texCoord0 + offsetPx / max(InSize, vec2(1.0))).a;
}

float ringCoverage(float radiusPx) {
    const float D = 0.70710678;
    float coverage = 0.0;
    coverage = max(coverage, maskAt(vec2( radiusPx, 0.0)));
    coverage = max(coverage, maskAt(vec2(-radiusPx, 0.0)));
    coverage = max(coverage, maskAt(vec2(0.0,  radiusPx)));
    coverage = max(coverage, maskAt(vec2(0.0, -radiusPx)));
    coverage = max(coverage, maskAt(vec2( radiusPx * D,  radiusPx * D)));
    coverage = max(coverage, maskAt(vec2(-radiusPx * D,  radiusPx * D)));
    coverage = max(coverage, maskAt(vec2( radiusPx * D, -radiusPx * D)));
    return max(coverage, maskAt(vec2(-radiusPx * D, -radiusPx * D)));
}

void main() {
    if (maskAt(vec2(0.0)) > 0.01) discard;
    float radius = max(2.0, GlowRadiusPx);
    float nearGlow = ringCoverage(max(2.0, radius * 0.45));
    float farGlow = ringCoverage(radius);
    float glow = max(nearGlow * 0.78, farGlow * 0.42) * clamp(GlowStrength, 0.0, 1.0);
    if (glow <= 0.003) discard;
    vec3 color = mix(vec3(0.00, 0.035, 0.78), vec3(0.00, 0.58, 1.00), clamp(nearGlow, 0.0, 1.0));
    fragColor = vec4(color, clamp(glow * 0.72, 0.0, 0.68));
}
