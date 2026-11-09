#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform vec2 InSize;
uniform vec2 Direction;
uniform int ThermalQuality;
uniform float DiffusionStrength;
uniform float TapSpacing;

out vec4 fragColor;

float gaussianWeight(int tap) {
    int distance = tap < 0 ? -tap : tap;
    if (distance == 0) return 0.220;
    if (distance == 1) return 0.190;
    if (distance == 2) return 0.120;
    if (distance == 3) return 0.060;
    return 0.025;
}

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);
    if (center.a < 0.1) {
        fragColor = vec4(0.0);
        return;
    }

    int maxTap = ThermalQuality <= 1 ? 2 : (ThermalQuality == 2 ? 3 : 4);
    vec2 texel = Direction * TapSpacing / InSize;
    float weightedHeat = 0.0;
    float weightSum = 0.0;

    for (int tap = -4; tap <= 4; tap++) {
        int distance = tap < 0 ? -tap : tap;
        if (distance > maxTap) {
            continue;
        }
        vec4 sampleData = texture(DiffuseSampler, texCoord + texel * float(tap));
        float sameSource = step(0.1, sampleData.a)
                * (1.0 - step(0.075, abs(sampleData.a - center.a)));
        // Preserve hard source boundaries, but permit a controlled heat exchange
        // between adjacent skin/clothing parts of the same model. This creates
        // broad temperature transitions without bleeding between entities.
        float materialSimilarity = 1.0 - smoothstep(0.16, 0.70, abs(sampleData.g - center.g));
        float scaleSimilarity = 1.0 - smoothstep(0.20, 0.76, abs(sampleData.b - center.b));
        float materialWeight = mix(0.52, 1.0, materialSimilarity);
        float scaleWeight = mix(0.62, 1.0, scaleSimilarity);
        float weight = gaussianWeight(tap) * sameSource * materialWeight * scaleWeight;
        weightedHeat += sampleData.r * weight;
        weightSum += weight;
    }

    float blurredHeat = weightedHeat / max(weightSum, 0.0001);
    float strength = clamp(DiffusionStrength, 0.0, 1.0);
    fragColor = vec4(mix(center.r, blurredHeat, strength), center.gba);
}
