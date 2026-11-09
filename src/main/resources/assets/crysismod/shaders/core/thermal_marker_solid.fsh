#version 150

in vec2 texCoord0;
in vec2 projectedEntitySize;
in vec2 thermalProfileData;
in vec3 viewNormal;
in float thermalEmissionClass;

out vec4 fragColor;

float packSurfaceData(float coldDetail, float facing, float emissionClass) {
    float coldBits = floor(clamp(coldDetail, 0.0, 1.0) * 3.0 + 0.5);
    float facingBits = floor(clamp(facing, 0.0, 1.0) * 7.0 + 0.5);
    float emissionBits = floor(clamp(emissionClass, 0.0, 1.0) * 7.0 + 0.5);
    return (coldBits + facingBits * 4.0 + emissionBits * 32.0) / 255.0;
}

float heatHash(vec2 cell) {
    return fract(sin(dot(cell, vec2(127.1, 311.7))) * 43758.5453);
}

float heatValueNoise(vec2 point) {
    vec2 cell = floor(point);
    vec2 local = fract(point);
    vec2 blend = local * local * (3.0 - 2.0 * local);
    float bottom = mix(heatHash(cell), heatHash(cell + vec2(1.0, 0.0)), blend.x);
    float top = mix(heatHash(cell + vec2(0.0, 1.0)), heatHash(cell + vec2(1.0)), blend.x);
    return mix(bottom, top, blend.y);
}

float organicThermalVariation(vec2 uv) {
    float broad = heatValueNoise(uv * 7.0 + vec2(2.7, 5.1));
    float medium = heatValueNoise(uv * 17.0 + vec2(11.3, 3.9));
    return ((broad * 0.72 + medium * 0.28) - 0.5) * 2.0;
}

void main() {
    vec3 normal = normalize(viewNormal);
    float facing = clamp(abs(normal.z), 0.0, 1.0);
    float grazingLoss = pow(1.0 - facing, 5.0);

    vec2 coarseUv = floor(texCoord0 * vec2(32.0, 32.0));
    float fineNoise = fract(sin(dot(coarseUv, vec2(12.9898, 78.233))) * 43758.5453) - 0.5;
    float surfaceNoise = organicThermalVariation(texCoord0) * 0.055 + fineNoise * 0.006;

    float profileHeat = clamp(thermalProfileData.x, 0.04, 0.96);
    float profileCold = clamp(thermalProfileData.y, 0.0, 1.0);
    float heatSeed = clamp(
            profileHeat + 0.10 - grazingLoss * 0.05 + surfaceNoise - profileCold * 0.10,
            0.05,
            0.91
    );
    float coldDetail = clamp(grazingLoss * 0.10 + profileCold * 0.58, 0.0, 1.0);

    fragColor = vec4(projectedEntitySize.x, heatSeed, packSurfaceData(coldDetail, facing, thermalEmissionClass), 1.0);
}
