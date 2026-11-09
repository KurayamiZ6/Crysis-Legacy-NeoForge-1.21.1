#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform vec2 Direction;
uniform int ThermalQuality;
uniform float DiffusionStrength;
uniform float TapSpacing;
in vec2 texCoord;
out vec4 fragColor;

float weightFor(int d) {
    if (d == 0) return 0.30;
    if (d == 1) return 0.22;
    if (d == 2) return 0.11;
    if (d == 3) return 0.045;
    return 0.015;
}

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);
    if (center.a < 0.1) {
        fragColor = vec4(0.0);
        return;
    }
    int limit = ThermalQuality <= 1 ? 2 : (ThermalQuality == 2 ? 3 : 4);
    vec2 texel = Direction * TapSpacing / InSize;
    float sum = 0.0;
    float weights = 0.0;
    for (int tap = -4; tap <= 4; tap++) {
        int d = tap < 0 ? -tap : tap;
        if (d > limit) continue;
        vec4 sampleData = texture(DiffuseSampler, texCoord + texel * float(tap));
        float sameType = 1.0 - step(0.075, abs(sampleData.a - center.a));
        float materialGate = 1.0 - smoothstep(0.16, 0.62, abs(sampleData.g - center.g));
        float w = weightFor(d) * sameType * mix(0.45, 1.0, materialGate);
        sum += sampleData.r * w;
        weights += w;
    }
    float blurred = sum / max(weights, 0.0001);
    fragColor = vec4(mix(center.r, blurred, clamp(DiffusionStrength * 0.82, 0.0, 0.92)), center.gba);
}
