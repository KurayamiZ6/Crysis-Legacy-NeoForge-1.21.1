#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D SceneDepthSampler;
uniform sampler2D MarkerSampler;
uniform sampler2D MarkerDepthSampler;
uniform vec2 InSize;
uniform vec2 Direction;
uniform int ThermalQuality;
uniform float DiffusionStrength;
uniform float TapSpacing;
uniform float HeatPower;

in vec2 texCoord;
out vec4 fragColor;

vec3 decodeSurface(float packedValue) {
    float packedByte = floor(clamp(packedValue, 0.0, 1.0) * 255.0 + 0.5);
    return vec3(
            mod(packedByte, 4.0) / 3.0,
            mod(floor(packedByte / 4.0), 8.0) / 7.0,
            floor(packedByte / 32.0)
    );
}

float encodeType(float emissionClass) {
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

float textureVariation(vec3 color) {
    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    return clamp((luma - 0.5) * 0.16, -0.07, 0.07);
}

float diffusionWeight(int distance) {
    if (distance == 0) return 0.30;
    if (distance == 1) return 0.22;
    if (distance == 2) return 0.11;
    if (distance == 3) return 0.045;
    return 0.015;
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

    float detail = textureVariation(texture(DiffuseSampler, uv).rgb);
    if (marker.a < 0.95) {
        float cooling = clamp(marker.b, 0.0, 1.0);
        float heat = clamp(marker.g + detail - cooling * 0.055, 0.0, 1.0);
        return vec4(pow(heat, max(HeatPower * 0.42, 0.01)), cooling, clamp(marker.r, 0.01, 1.0), 1.0);
    }

    vec3 surface = decodeSurface(marker.b);
    float material = surface.x;
    float heatSeed = clamp(marker.g, 0.0, 1.0);
    float heat;
    if (surface.z > 4.5) {
        heat = clamp(0.82 + heatSeed * 0.18 + detail * 0.35, 0.80, 1.0);
    } else if (surface.z > 3.5) {
        heat = clamp(0.70 + heatSeed * 0.28 - material * 0.045 + detail * 0.32, 0.70, 0.99);
    } else if (surface.z > 2.5) {
        float insulation = smoothstep(0.10, 0.48, material);
        heat = clamp(0.65 + (heatSeed - 0.60) * 1.25 - insulation * 0.14 + detail, 0.54, 0.98);
    } else if (surface.z > 1.5) {
        heat = clamp(heatSeed * 0.91 - material * 0.09 + detail * 0.55, 0.12, 0.87);
    } else if (surface.z > 0.5) {
        heat = clamp(heatSeed * 0.83 - material * 0.12 + detail * 0.48, 0.08, 0.72);
    } else if (surface.z < 0.5) {
        heat = clamp(heatSeed * 0.78 - material * 0.18 + detail * 0.45, 0.05, 0.66);
    }

    heat = constrainClassHeat(
            pow(heat, max(HeatPower * 0.28, 0.01)),
            surface.z
    );

    return vec4(heat, material,
            clamp(marker.r, 0.01, 1.0), encodeType(surface.z));
}

void main() {
    vec4 center = extractHeat(texCoord);
    if (center.a < 0.1) {
        fragColor = vec4(0.0);
        return;
    }

    int limit = ThermalQuality <= 1 ? 2 : (ThermalQuality == 2 ? 3 : 4);
    vec2 texel = Direction * TapSpacing / InSize;
    float sum = 0.0;
    float weights = 0.0;
    for (int tap = -4; tap <= 4; tap++) {
        int distance = tap < 0 ? -tap : tap;
        if (distance > limit) continue;
        vec4 sampleData = tap == 0 ? center : extractHeat(texCoord + texel * float(tap));
        float sameType = step(0.1, sampleData.a)
                * (1.0 - step(0.075, abs(sampleData.a - center.a)));
        float materialGate = 1.0 - smoothstep(0.16, 0.62, abs(sampleData.g - center.g));
        float weight = diffusionWeight(distance) * sameType * mix(0.45, 1.0, materialGate);
        sum += sampleData.r * weight;
        weights += weight;
    }
    float blurred = sum / max(weights, 0.0001);
    fragColor = vec4(
            mix(center.r, blurred, clamp(DiffusionStrength * 0.82, 0.0, 0.92)),
            center.gba
    );
}

