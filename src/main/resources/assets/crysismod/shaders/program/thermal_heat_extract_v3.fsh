#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D SceneDepthSampler;
uniform sampler2D MarkerSampler;
uniform sampler2D MarkerDepthSampler;

in vec2 texCoord;

uniform vec2 InSize;
uniform float HeatPower;
uniform vec2 Direction;
uniform int ThermalQuality;
uniform float DiffusionStrength;
uniform float TapSpacing;

out vec4 fragColor;

bool isHotBlockMarker(vec4 marker) {
    return marker.a >= 0.1 && marker.a < 0.95;
}

vec3 decodeEntitySurfaceData(float packedValue) {
    float packedByte = floor(clamp(packedValue, 0.0, 1.0) * 255.0 + 0.5);
    float coldDetail = mod(packedByte, 4.0) / 3.0;
    float facing = mod(floor(packedByte / 4.0), 8.0) / 7.0;
    float emissionClass = floor(packedByte / 32.0);
    return vec3(coldDetail, facing, emissionClass);
}

float packFieldSurfaceData(float coldDetail, float facing) {
    float coldBits = floor(clamp(coldDetail, 0.0, 1.0) * 7.0 + 0.5);
    float facingBits = floor(clamp(facing, 0.0, 1.0) * 7.0 + 0.5);
    return (coldBits + facingBits * 8.0) / 63.0;
}

vec2 decodeFieldSurfaceData(float packedValue) {
    float packedSurface = floor(clamp(packedValue, 0.0, 1.0) * 63.0 + 0.5);
    return vec2(mod(packedSurface, 8.0) / 7.0, floor(packedSurface / 8.0) / 7.0);
}

float encodeSourceType(float emissionClass) {
    return clamp((emissionClass + 1.0) / 7.0, 1.0 / 7.0, 6.0 / 7.0);
}

vec2 heatBand(float emissionClass) {
    if (emissionClass < 0.5) return vec2(0.06, 0.36);
    if (emissionClass < 1.5) return vec2(0.20, 0.52);
    if (emissionClass < 2.5) return vec2(0.24, 0.70);
    if (emissionClass < 3.5) return vec2(0.46, 0.98);
    if (emissionClass < 4.5) return vec2(0.72, 0.99);
    return vec2(0.88, 1.00);
}

float constrainClassHeat(float heat, float emissionClass) {
    vec2 band = heatBand(emissionClass);
    return clamp(heat, band.x, band.y);
}

float gaussianWeight(int tap) {
    int distance = tap < 0 ? -tap : tap;
    if (distance == 0) return 0.220;
    if (distance == 1) return 0.190;
    if (distance == 2) return 0.120;
    if (distance == 3) return 0.060;
    return 0.025;
}

vec4 extractHeat(vec2 uv) {
    vec4 marker = texture(MarkerSampler, uv);
    if (marker.a < 0.1) {
        return vec4(0.0);
    }

    float sceneDepth = texture(SceneDepthSampler, uv).r;
    float markerDepth = texture(MarkerDepthSampler, uv).r;
    if (markerDepth > min(sceneDepth + 0.00002, 1.0)) {
        return vec4(0.0);
    }

    vec3 sceneColor = texture(DiffuseSampler, uv).rgb;
    float luminance = dot(sceneColor, vec3(0.299, 0.587, 0.114));
    float textureDetail = clamp((luminance - 0.5) * 0.18, -0.075, 0.075);

    if (isHotBlockMarker(marker)) {
        float cooling = clamp(marker.b, 0.0, 1.0);
        float heat = clamp(marker.g + textureDetail * mix(1.35, 0.55, cooling) - cooling * 0.06, 0.0, 1.0);
        heat = pow(heat, max(HeatPower * 0.46, 0.01));
        return vec4(heat, cooling, clamp(marker.r, 0.01, 1.0), 1.0);
    }

    vec3 surface = decodeEntitySurfaceData(marker.b);
    float coldDetail = surface.x;
    float emissionClass = surface.z;
    float heatSeed = clamp(marker.g, 0.0, 1.0);
    float heat;

    if (emissionClass > 4.5) {
        heat = clamp(0.78 + heatSeed * 0.22 + textureDetail * 0.35, 0.78, 1.0);
    } else if (emissionClass > 3.5) {
        heat = clamp(0.68 + heatSeed * 0.30 - coldDetail * 0.05 + textureDetail * 0.35, 0.68, 0.99);
    } else if (emissionClass > 2.5) {
        float insulation = smoothstep(0.12, 0.44, coldDetail);
        heat = 0.55 + (heatSeed - 0.60) * 1.58 - insulation * 0.15 + textureDetail;
        heat = clamp(heat, 0.52, 0.97);
    } else if (emissionClass > 1.5) {
        heat = clamp(heatSeed * 0.94 - coldDetail * 0.08 + textureDetail * 0.70, 0.12, 0.88);
    } else if (emissionClass > 0.5) {
        heat = clamp(heatSeed * 0.86 - coldDetail * 0.11 + textureDetail * 0.55, 0.08, 0.74);
    } else {
        heat = clamp(heatSeed * 0.82 - coldDetail * 0.16 + textureDetail * 0.50, 0.05, 0.68);
    }

    heat = constrainClassHeat(
            pow(clamp(heat, 0.0, 1.0), max(HeatPower * 0.34, 0.01)),
            emissionClass
    );

    return vec4(
            heat,
            packFieldSurfaceData(coldDetail, surface.y),
            clamp(marker.r, 0.01, 1.0),
            encodeSourceType(emissionClass)
    );
}

void main() {
    vec4 center = extractHeat(texCoord);
    if (center.a < 0.1) {
        fragColor = vec4(0.0);
        return;
    }

    int maxTap = ThermalQuality <= 1 ? 2 : (ThermalQuality == 2 ? 3 : 4);
    vec2 texel = Direction * TapSpacing / InSize;
    vec2 centerSurface = center.a >= 0.93
            ? vec2(center.g, 1.0)
            : decodeFieldSurfaceData(center.g);
    float weightedHeat = 0.0;
    float weightSum = 0.0;

    for (int tap = -4; tap <= 4; tap++) {
        int distance = tap < 0 ? -tap : tap;
        if (distance > maxTap) continue;

        vec4 sampleData = tap == 0 ? center : extractHeat(texCoord + texel * float(tap));
        float sameSource = step(0.1, sampleData.a)
                * (1.0 - step(0.075, abs(sampleData.a - center.a)));
        if (sameSource < 0.5) continue;

        vec2 sampleSurface = tap == 0
                ? centerSurface
                : (sampleData.a >= 0.93
                    ? vec2(sampleData.g, 1.0)
                    : decodeFieldSurfaceData(sampleData.g));
        float materialSimilarity = 1.0 - smoothstep(
                0.16, 0.70, abs(sampleSurface.x - centerSurface.x));
        float facingSimilarity = 1.0 - smoothstep(
                0.18, 0.72, abs(sampleSurface.y - centerSurface.y));
        float scaleSimilarity = 1.0 - smoothstep(0.20, 0.76, abs(sampleData.b - center.b));
        float materialWeight = mix(0.52, 1.0, materialSimilarity)
                * mix(0.82, 1.0, facingSimilarity);
        float scaleWeight = mix(0.62, 1.0, scaleSimilarity);
        float weight = gaussianWeight(tap) * materialWeight * scaleWeight;
        weightedHeat += sampleData.r * weight;
        weightSum += weight;
    }

    float blurredHeat = weightedHeat / max(weightSum, 0.0001);
    fragColor = vec4(
            mix(center.r, blurredHeat, clamp(DiffusionStrength, 0.0, 1.0)),
            center.gba
    );
}

