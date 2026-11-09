#version 150

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec2 projectedEntitySize;
in vec2 thermalProfileData;
in vec3 viewNormal;
in float thermalEmissionClass;

out vec4 fragColor;

float packSurfaceData(float coldDetail, float facing, float emissionClass) {
    // 2 bits material coldness, 3 bits view-facing, 3 bits class. Facing is
    // the primary internal shape signal and needs more precision than the
    // previous four-level encoding; material remains a secondary modifier.
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
    vec4 texel = texture(Sampler0, texCoord0);
    if (texel.a < 0.1) {
        discard;
    }

    vec3 normal = normalize(viewNormal);
    float facing = clamp(abs(normal.z), 0.0, 1.0);
    // Minecraft cuboids have flat normals per face. A linear incidence term
    // turns an entire angled face cold, so only retain a weak near-tangent loss.
    float grazingLoss = pow(1.0 - facing, 5.0);
    float luminance = dot(texel.rgb, vec3(0.299, 0.587, 0.114));
    float maxChannel = max(texel.r, max(texel.g, texel.b));
    float minChannel = min(texel.r, min(texel.g, texel.b));
    float chroma = maxChannel - minChannel;

    vec2 coarseUv = floor(texCoord0 * vec2(48.0, 48.0));
    float fineNoise = fract(sin(dot(coarseUv, vec2(12.9898, 78.233))) * 43758.5453) - 0.5;
    float surfaceNoise = organicThermalVariation(texCoord0) * 0.060 + fineNoise * 0.008;

    float textureHeat = 0.52 + (luminance - 0.45) * 0.26 + chroma * 0.12;
    float surfaceHeat = 0.12 - grazingLoss * 0.05 + surfaceNoise;
    float profileHeat = clamp(thermalProfileData.x, 0.04, 0.96);
    float profileCold = clamp(thermalProfileData.y, 0.0, 1.0);
    float materialHeat = textureHeat + surfaceHeat;
    float insulatedSurface = profileHeat + surfaceHeat * 0.62 + (luminance - 0.5) * 0.11 + chroma * 0.045;
    float heatSeed = clamp(mix(materialHeat, insulatedSurface, 0.68) - profileCold * 0.12, 0.05, 0.93);
    float coldDetail = clamp(
            grazingLoss * 0.10 + (1.0 - luminance) * 0.12 - chroma * 0.05 + profileCold * 0.55,
            0.0,
            1.0
    );

    // B packs material coldness, view-facing and the thermal emission class.
    fragColor = vec4(projectedEntitySize.x, heatSeed, packSurfaceData(coldDetail, facing, thermalEmissionClass), 1.0);
}
