#version 150

uniform sampler2D Sampler0;
uniform sampler2D LeftArmMaskSampler;
uniform sampler2D LeftArmDepthSampler;
uniform sampler2D RightArmMaskSampler;
uniform sampler2D RightArmDepthSampler;
uniform vec2 InSize;
uniform int LeftArmEnabled;
uniform int RightArmEnabled;
uniform float ContactHeatStrength;
uniform float ModelOpacity;
uniform float ScanlineStrength;
uniform float ScanlineSpacingPx;
uniform float ActivationFlash;

in vec2 texCoord0;
in vec3 viewNormal;
in vec3 viewPosition;
out vec4 fragColor;

float continuousBlueRowMask() {
    float spacing = max(ScanlineSpacingPx, 1.0);
    float bandHeight = max(1.0, spacing * 0.50);
    float bandIndex = floor(gl_FragCoord.y / bandHeight);
    return 1.0 - step(0.5, mod(bandIndex, 2.0));
}

vec3 applyContinuousBlueRows(vec3 thermal) {
    float row = continuousBlueRowMask();
    float rowOpacity = row * clamp(ScanlineStrength * 3.2, 0.0, 0.72);
    float luminance = clamp(dot(thermal, vec3(0.299, 0.587, 0.114)), 0.0, 1.0);
    float flashAmount = clamp(ActivationFlash, 0.0, 1.0);
    vec3 blueCarrier = mix(
            vec3(0.0, 0.065, 0.58),
            vec3(0.0, 0.52, 0.94),
            flashAmount
    ) * mix(0.72, 1.04, luminance);
    return mix(thermal, blueCarrier, rowOpacity);
}

bool armCoversItem(vec2 screenUv, float itemDepth) {
    if (LeftArmEnabled != 0 && texture(LeftArmMaskSampler, screenUv).a > 0.06) {
        if (texture(LeftArmDepthSampler, screenUv).r + 0.000002 < itemDepth) return true;
    }
    if (RightArmEnabled != 0 && texture(RightArmMaskSampler, screenUv).a > 0.06) {
        if (texture(RightArmDepthSampler, screenUv).r + 0.000002 < itemDepth) return true;
    }
    return false;
}

float armContact(vec2 uv, float itemDepth) {
    float contact = 0.0;
    if (LeftArmEnabled != 0) {
        float mask = texture(LeftArmMaskSampler, uv).a;
        float depth = texture(LeftArmDepthSampler, uv).r;
        contact = max(contact, mask * (1.0 - smoothstep(0.00030, 0.0060, abs(itemDepth - depth))));
    }
    if (RightArmEnabled != 0) {
        float mask = texture(RightArmMaskSampler, uv).a;
        float depth = texture(RightArmDepthSampler, uv).r;
        contact = max(contact, mask * (1.0 - smoothstep(0.00030, 0.0060, abs(itemDepth - depth))));
    }
    return contact;
}

float contactHeat(vec2 screenUv, float itemDepth) {
    const float D = 0.70710678;
    vec2 invSize = 1.0 / max(InSize, vec2(1.0));
    float center = armContact(screenUv, itemDepth);

    vec2 nearPx = vec2(4.0) * invSize;
    float nearHeat = 0.0;
    nearHeat = max(nearHeat, armContact(screenUv + vec2( nearPx.x, 0.0), itemDepth));
    nearHeat = max(nearHeat, armContact(screenUv + vec2(-nearPx.x, 0.0), itemDepth));
    nearHeat = max(nearHeat, armContact(screenUv + vec2(0.0,  nearPx.y), itemDepth));
    nearHeat = max(nearHeat, armContact(screenUv + vec2(0.0, -nearPx.y), itemDepth));
    nearHeat = max(nearHeat, armContact(screenUv + vec2( nearPx.x * D,  nearPx.y * D), itemDepth));
    nearHeat = max(nearHeat, armContact(screenUv + vec2(-nearPx.x * D,  nearPx.y * D), itemDepth));
    nearHeat = max(nearHeat, armContact(screenUv + vec2( nearPx.x * D, -nearPx.y * D), itemDepth));
    nearHeat = max(nearHeat, armContact(screenUv + vec2(-nearPx.x * D, -nearPx.y * D), itemDepth));

    vec2 midPx = vec2(11.0) * invSize;
    float midHeat = 0.0;
    midHeat = max(midHeat, armContact(screenUv + vec2( midPx.x, 0.0), itemDepth));
    midHeat = max(midHeat, armContact(screenUv + vec2(-midPx.x, 0.0), itemDepth));
    midHeat = max(midHeat, armContact(screenUv + vec2(0.0,  midPx.y), itemDepth));
    midHeat = max(midHeat, armContact(screenUv + vec2(0.0, -midPx.y), itemDepth));

    vec2 farPx = vec2(22.0) * invSize;
    float farHeat = 0.0;
    farHeat = max(farHeat, armContact(screenUv + vec2( farPx.x, 0.0), itemDepth));
    farHeat = max(farHeat, armContact(screenUv + vec2(-farPx.x, 0.0), itemDepth));
    farHeat = max(farHeat, armContact(screenUv + vec2(0.0,  farPx.y), itemDepth));
    farHeat = max(farHeat, armContact(screenUv + vec2(0.0, -farPx.y), itemDepth));

    float heat = max(center, max(nearHeat * 0.90, max(midHeat * 0.62, farHeat * 0.34)));
    return smoothstep(0.035, 0.82, heat) * clamp(ContactHeatStrength, 0.0, 1.0);
}

vec3 itemThermalPalette(float coordinate) {
    vec3 deepBlue = vec3(0.003, 0.008, 0.16);
    vec3 blue = vec3(0.00, 0.10, 0.96);
    vec3 cyan = vec3(0.00, 0.92, 1.00);
    vec3 green = vec3(0.05, 1.00, 0.17);
    vec3 yellow = vec3(1.00, 0.96, 0.04);
    vec3 red = vec3(1.00, 0.07, 0.01);
    vec3 hotPink = vec3(1.00, 0.22, 0.38);
    vec3 whiteHot = vec3(1.00, 0.97, 0.93);

    float t = clamp(coordinate, 0.0, 1.0);
    if (t < 0.16) return mix(deepBlue, blue, smoothstep(0.0, 0.16, t));
    if (t < 0.36) return mix(blue, cyan, smoothstep(0.16, 0.36, t));
    if (t < 0.56) return mix(cyan, green, smoothstep(0.36, 0.56, t));
    if (t < 0.70) return mix(green, yellow, smoothstep(0.56, 0.70, t));
    if (t < 0.84) return mix(yellow, red, smoothstep(0.70, 0.84, t));
    if (t < 0.93) return mix(red, hotPink, smoothstep(0.84, 0.93, t));
    return mix(hotPink, whiteHot, smoothstep(0.93, 1.0, t));
}

float applyItemActivationFlash(float coordinate, float handHeat) {
    float amount = clamp(ActivationFlash, 0.0, 1.0);
    if (amount <= 0.0001) return clamp(coordinate, 0.0, 1.0);

    // Advance the complete item spectrum by roughly one palette interval:
    // deep blue -> blue/cyan, green -> yellow, yellow -> red and red -> white-hot.
    float globalShift = 0.14;
    float contactBoost = clamp(handHeat, 0.0, 1.0) * 0.18;
    return clamp(coordinate + amount * (globalShift + contactBoost), 0.0, 1.0);
}

void main() {
    vec4 texel = texture(Sampler0, texCoord0);
    if (texel.a < 0.10) discard;

    vec2 screenUv = gl_FragCoord.xy / max(InSize, vec2(1.0));
    float itemDepth = gl_FragCoord.z;
    if (armCoversItem(screenUv, itemDepth)) discard;

    vec3 normal = viewNormal / max(length(viewNormal), 0.00001);
    vec3 viewDirection = -viewPosition / max(length(viewPosition), 0.00001);
    float facing = clamp(abs(dot(normal, viewDirection)), 0.0, 1.0);
    float luminance = dot(texel.rgb, vec3(0.299, 0.587, 0.114));
    float broadFace = pow(smoothstep(0.04, 0.96, facing), 0.62);
    float grazingRim = 1.0 - smoothstep(0.10, 0.58, facing);
    float textureDetail = smoothstep(0.025, 0.18, fwidth(luminance));
    float normalDetail = smoothstep(0.06, 0.34, length(fwidth(normal)));
    float materialHeat = smoothstep(0.03, 0.94, luminance);
    float topBias = max(normal.y, 0.0);

    // Geometry-only approximation of the Crysis item spectrum: grazing faces,
    // texture seams and abrupt normal changes stay cold while broad readable
    // surfaces can advance through cyan into green.
    float coordinate = 0.10
            + broadFace * 0.33
            + materialHeat * 0.16
            + topBias * 0.035
            - grazingRim * 0.10
            - textureDetail * 0.075
            - normalDetail * 0.055;

    float handHeat = contactHeat(screenUv, itemDepth);
    coordinate += handHeat * 0.92;
    coordinate = applyItemActivationFlash(coordinate, handHeat);

    // Crysis-style item carrier: every row is continuous in framebuffer space
    // and blends blue into the existing temperature instead of replacing it.
    vec3 thermal = itemThermalPalette(coordinate);
    thermal = applyContinuousBlueRows(thermal);

    float opacity = clamp(ModelOpacity, 0.0, 1.0) * texel.a;
    fragColor = vec4(clamp(thermal, 0.0, 1.0), opacity);
}
