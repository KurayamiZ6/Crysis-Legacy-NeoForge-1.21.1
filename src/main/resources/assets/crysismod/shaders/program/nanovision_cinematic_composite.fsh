#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D HeatSampler;
uniform sampler2D GeometrySampler;
uniform vec2 InSize;
uniform float ActivationFlash;
uniform float ActivationWorldFlashStrength;
uniform int NanovisionMode;
uniform float InnerStrength;
uniform float ThermalModelOpacity;
uniform float HotBlockOpacity;
uniform float OuterGlowStrength;
uniform float GlowStrengthMultiplier;
uniform float BandingStrength;
uniform float WarmGreenWidth;
uniform float WarmRedWidth;
uniform float WarmPinkWidth;
uniform float WarmWhiteWidth;
uniform float ScanlineStrength;
uniform float ScanlineSpacingPx;
uniform float ScanlineSharpness;
uniform float ScanlineIrregularityPx;
uniform float ScanlinePaletteShift;
uniform float ScanlineBreakup;
uniform float ScanlineWhiteHotSuppression;
uniform int DebugRawMarker;
uniform int DebugDetectedMask;
uniform int DebugDistance;
uniform int DisablePalette;
uniform int DisableGlow;
uniform int DebugPassthrough;
uniform int DebugOpaqueGrayOnly;
in vec2 texCoord;
out vec4 fragColor;

const int MODE_OFF = 0;
const int MODE_GRAYSCALE_NIGHT = 1;
const int MODE_DEBUG_MARKER = 3;
const int MODE_DEBUG_DISTANCE = 4;
const float PI = 3.14159265358979323846;
const float INV_TWO_PI = 0.15915494309189535;

float fastSin(float angle) {
    float wrapped = fract(angle * INV_TWO_PI + 0.5) * (2.0 * PI) - PI;
    float approximation = 1.27323954473516 * wrapped
            - 0.405284734569351 * wrapped * abs(wrapped);
    return approximation + 0.225 * (approximation * abs(approximation) - approximation);
}

bool isCold(float type) { return type < 0.215; }
bool isCool(float type) { return type >= 0.215 && type < 0.36; }
bool isUnknown(float type) { return type >= 0.36 && type < 0.50; }
bool isWarm(float type) { return type >= 0.50 && type < 0.645; }
bool isHot(float type) { return type >= 0.645 && type < 0.79; }
bool isHotEmitter(float type) { return type >= 0.79 && type < 0.93; }
bool isHotBlock(float type) { return type >= 0.93; }

vec2 heatBand(float type) {
    if (isCold(type)) return vec2(0.06, 0.36);
    if (isCool(type)) return vec2(0.20, 0.52);
    if (isUnknown(type)) return vec2(0.24, 0.70);
    if (isWarm(type)) return vec2(0.46, 0.98);
    if (isHot(type)) return vec2(0.72, 0.99);
    if (isHotEmitter(type)) return vec2(0.88, 1.00);
    return vec2(0.0, 1.0);
}

float constrainClassHeat(float heat, float type) {
    vec2 band = heatBand(type);
    return clamp(heat, band.x, band.y);
}

vec3 grayscaleNight(vec3 color) {
    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    luma = pow(max(luma, 0.0001), 0.9);
    return vec3(clamp(luma * 0.85 + 0.08, 0.0, 1.0));
}

vec3 palette(float heat) {
    vec3 navy = vec3(0.00, 0.02, 0.20);
    vec3 blue = vec3(0.00, 0.13, 1.00);
    vec3 cyan = vec3(0.00, 0.78, 1.00);
    vec3 green = vec3(0.00, 1.00, 0.10);
    vec3 yellow = vec3(1.00, 0.92, 0.00);
    vec3 red = vec3(1.00, 0.012, 0.008);
    vec3 pink = vec3(1.00, 0.50, 0.62);
    vec3 whiteHot = vec3(1.00, 0.97, 0.94);
    heat = clamp(heat, 0.0, 1.0);
    if (heat < 0.16) return mix(navy, blue, smoothstep(0.0, 0.16, heat));
    if (heat < 0.30) return mix(blue, cyan, smoothstep(0.16, 0.30, heat));
    if (heat < 0.50) return mix(cyan, green, smoothstep(0.30, 0.50, heat));
    if (heat < 0.61) return mix(green, yellow, smoothstep(0.50, 0.61, heat));
    if (heat < 0.75) return mix(yellow, red, smoothstep(0.61, 0.75, heat));
    if (heat < 0.90) return mix(red, pink, smoothstep(0.75, 0.90, heat));
    return mix(pink, whiteHot, smoothstep(0.90, 1.0, heat));
}

vec3 warmPalette(float heat) {
    vec3 green = vec3(0.00, 1.00, 0.075);
    vec3 warmGreen = vec3(0.22, 1.00, 0.035);
    vec3 red = vec3(1.00, 0.012, 0.008);
    vec3 pink = vec3(1.00, 0.48, 0.60);
    vec3 whiteHot = vec3(1.00, 0.97, 0.94);
    float greenWidth = max(WarmGreenWidth, 0.001);
    float redWidth = max(WarmRedWidth, 0.001);
    float pinkWidth = max(WarmPinkWidth, 0.001);
    float whiteWidth = max(WarmWhiteWidth, 0.001);
    float totalWidth = greenWidth + redWidth + pinkWidth + whiteWidth;
    float greenEnd = greenWidth / totalWidth;
    float redEnd = (greenWidth + redWidth) / totalWidth;
    float pinkEnd = (greenWidth + redWidth + pinkWidth) / totalWidth;
    float greenRedSoftness = min(0.12, min(greenWidth, redWidth) / totalWidth * 0.72);
    float redPinkSoftness = min(0.12, min(redWidth, pinkWidth) / totalWidth * 0.72);
    float pinkWhiteSoftness = min(0.12, min(pinkWidth, whiteWidth) / totalWidth * 0.72);

    vec3 edgeGreen = mix(green, warmGreen, 0.35);
    vec3 color = mix(edgeGreen, red, smoothstep(greenEnd - greenRedSoftness, greenEnd + greenRedSoftness, heat));
    color = mix(color, pink, smoothstep(redEnd - redPinkSoftness, redEnd + redPinkSoftness, heat));
    return mix(color, whiteHot, smoothstep(pinkEnd - pinkWhiteSoftness, pinkEnd + pinkWhiteSoftness, heat));
}

float glowResponse(float type) {
    if (isWarm(type)) return 1.0;
    if (isHotEmitter(type) || isHotBlock(type)) return 0.22;
    if (isHot(type)) return 0.30;
    if (isUnknown(type)) return 0.60;
    if (isCool(type)) return 0.42;
    return 0.30;
}

float sensorHash(vec2 cell) {
    return fract(fastSin(dot(cell, vec2(12.9898, 78.233))) * 43758.5453);
}

float sensorValueNoise(float coordinate, float rowSeed) {
    float cell = floor(coordinate);
    float local = fract(coordinate);
    float blend = local * local * (3.0 - 2.0 * local);
    float first = sensorHash(vec2(cell, rowSeed));
    float second = sensorHash(vec2(cell + 1.0, rowSeed));
    return mix(first, second, blend);
}

vec3 crysisRowPattern() {
    vec2 pixel = texCoord * InSize;
    float spacing = max(ScanlineSpacingPx, 1.0);
    float bandHeight = max(1.0, spacing * 0.50);
    float bandIndex = floor(pixel.y / bandHeight);
    float activeRow = 1.0 - step(0.5, mod(bandIndex, 2.0));
    float globalPhase = fract(pixel.y / spacing);
    float globalLine = pow(
            clamp(1.0 - abs(globalPhase * 2.0 - 1.0), 0.0, 1.0),
            max(ScanlineSharpness, 0.5)
    );
    float irregularMix = sqrt(clamp(ScanlineIrregularityPx / 1.5, 0.0, 1.0));
    float activeIndex = floor(bandIndex * 0.5);
    float rowSeed = sensorHash(vec2(activeIndex, 19.73));
    float coarseSegments = sensorValueNoise(
            (pixel.x + rowSeed * 91.0) / 44.0,
            activeIndex * 1.37 + 3.1
    );
    float fineSegments = sensorValueNoise(
            (pixel.x - rowSeed * 37.0) / 11.0,
            activeIndex * 2.11 + 7.9
    );
    float coarseCoverage = smoothstep(0.16, 0.46, coarseSegments);
    float fineCoverage = mix(0.74, 1.0, smoothstep(0.18, 0.82, fineSegments));
    float hardGap = step(0.12 + irregularMix * 0.035, coarseSegments);
    float pixelGrain = sensorHash(vec2(floor(pixel.x * 0.5), activeIndex * 3.17 + 29.0));
    float grainOpacity = mix(1.0, mix(0.68, 1.0, pixelGrain), irregularMix);
    float rowAmplitude = mix(
            1.0,
            mix(0.78, 1.0, sensorHash(vec2(activeIndex, 41.17))),
            irregularMix
    );
    float dropoutBlend = smoothstep(
            0.10,
            0.95,
            clamp(ScanlineBreakup, 0.0, 1.0)
    );
    float brokenLine = hardGap
            * mix(0.62, 1.0, coarseCoverage)
            * fineCoverage * grainOpacity * rowAmplitude;
    float segmentMask = mix(
            1.0,
            brokenLine,
            dropoutBlend
    );
    return vec3(activeRow, globalLine, segmentMask);
}

float warmSensorResponse(float coordinate) {
    float greenWidth = max(WarmGreenWidth, 0.001);
    float redWidth = max(WarmRedWidth, 0.001);
    float pinkWidth = max(WarmPinkWidth, 0.001);
    float whiteWidth = max(WarmWhiteWidth, 0.001);
    float totalWidth = greenWidth + redWidth + pinkWidth + whiteWidth;
    float pinkEnd = (greenWidth + redWidth + pinkWidth) / totalWidth;
    float whiteVisibility = 1.0 - smoothstep(
            max(0.0, pinkEnd - 0.015),
            min(1.0, pinkEnd + 0.065),
            coordinate
    );
    float suppressionBlend = smoothstep(
            0.0,
            0.90,
            clamp(ScanlineWhiteHotSuppression, 0.0, 1.0)
    );
    return mix(
            1.0,
            whiteVisibility,
            suppressionBlend
    );
}

float fullPaletteSensorResponse(float coordinate) {
    float whiteVisibility = 1.0 - smoothstep(0.885, 0.985, coordinate);
    float suppressionBlend = smoothstep(
            0.0,
            0.90,
            clamp(ScanlineWhiteHotSuppression, 0.0, 1.0)
    );
    return mix(
            1.0,
            whiteVisibility,
            suppressionBlend
    );
}

vec3 applyGlobalSensorRows(vec3 color, vec3 rowPattern, float visibility) {
    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    float brightnessWeight = mix(0.75, 1.08, clamp(luma, 0.0, 1.0));
    float darken = rowPattern.y * clamp(ScanlineStrength, 0.0, 0.25)
            * brightnessWeight * clamp(visibility, 0.0, 1.0) * 0.58;
    return clamp(color * (1.0 - darken), 0.0, 1.0);
}

vec3 applyActivationFullPaletteFlash(float coordinate, vec3 color) {
    float amount = clamp(ActivationFlash, 0.0, 1.0);
    if (amount <= 0.0001) return color;
    float heatMask = smoothstep(0.30, 0.44, coordinate);
    vec3 hotter = mix(
            vec3(1.00, 0.91, 0.00),
            vec3(1.00, 0.018, 0.012),
            smoothstep(0.50, 0.66, coordinate)
    );
    hotter = mix(
            hotter,
            vec3(1.00, 0.97, 0.94),
            smoothstep(0.70, 0.82, coordinate)
    );
    return mix(color, hotter, amount * heatMask);
}

vec3 applyActivationWarmPaletteFlash(float coordinate, vec3 color) {
    float amount = clamp(ActivationFlash, 0.0, 1.0);
    if (amount <= 0.0001) return color;
    float totalWidth = max(WarmGreenWidth, 0.001)
            + max(WarmRedWidth, 0.001)
            + max(WarmPinkWidth, 0.001)
            + max(WarmWhiteWidth, 0.001);
    float greenEnd = max(WarmGreenWidth, 0.001) / totalWidth;
    float redEnd = (max(WarmGreenWidth, 0.001) + max(WarmRedWidth, 0.001)) / totalWidth;
    vec3 hotter = mix(
            vec3(1.00, 0.91, 0.00),
            vec3(1.00, 0.018, 0.012),
            smoothstep(greenEnd - 0.08, greenEnd, coordinate)
    );
    hotter = mix(
            hotter,
            vec3(1.00, 0.97, 0.94),
            smoothstep(greenEnd + 0.035, redEnd + 0.02, coordinate)
    );
    return mix(color, hotter, amount);
}

void main() {
    vec4 source = texture(DiffuseSampler, texCoord);
    vec4 heatData = texture(HeatSampler, texCoord);
    vec4 geometry = texture(GeometrySampler, texCoord);
    vec3 gray = grayscaleNight(source.rgb);
    if (DebugPassthrough != 0 || NanovisionMode == MODE_OFF) {
        fragColor = vec4(source.rgb, 1.0);
        return;
    }
    if (DebugOpaqueGrayOnly != 0 || NanovisionMode == MODE_GRAYSCALE_NIGHT) {
        fragColor = vec4(gray, 1.0);
        return;
    }

    bool inside = geometry.g > 0.5;
    if (DebugRawMarker != 0 || NanovisionMode == MODE_DEBUG_MARKER) {
        fragColor = vec4(inside ? vec3(0.02, 1.0, 0.0) : gray, 1.0);
        return;
    }
    if (DebugDetectedMask != 0) {
        fragColor = vec4(vec3(inside ? 1.0 : 0.0), 1.0);
        return;
    }

    float edgeField = clamp(geometry.r, 0.0, 1.0);
    float adjustedHeat = constrainClassHeat(
            heatData.r - edgeField * (isWarm(geometry.a) ? 0.35 : 0.22),
            geometry.a
    );
    if (DebugDistance != 0 || DisablePalette != 0 || NanovisionMode == MODE_DEBUG_DISTANCE) {
        fragColor = vec4(inside ? vec3(adjustedHeat) : vec3(0.0), 1.0);
        return;
    }

    vec3 result = gray;
    vec3 rowPattern = crysisRowPattern();
    if (inside) {
        float paletteCoordinate = adjustedHeat;
        float sensorRowResponse;
        if (isWarm(geometry.a)) {
            float classHeat = clamp((adjustedHeat - 0.46) / 0.52, 0.0, 1.0);
            float centerReach = mix(0.78, 0.96, smoothstep(0.10, 0.95, classHeat));
            float layerProgress = smoothstep(0.0, 1.0, 1.0 - edgeField);
            paletteCoordinate = clamp(layerProgress * centerReach, 0.0, 0.985);
            sensorRowResponse = warmSensorResponse(paletteCoordinate);
        } else {
            sensorRowResponse = fullPaletteSensorResponse(paletteCoordinate);
        }
        float banded = floor(paletteCoordinate * 24.0) / 24.0;
        paletteCoordinate = mix(
                paletteCoordinate,
                banded,
                clamp(BandingStrength * 0.035, 0.0, 0.05)
        );
        float coldShift = clamp(ScanlinePaletteShift, 0.0, 0.18);
        float coldCoordinate = isWarm(geometry.a)
                ? clamp(paletteCoordinate - coldShift, 0.0, 1.0)
                : constrainClassHeat(paletteCoordinate - coldShift, geometry.a);
        vec3 baseThermal = isWarm(geometry.a)
                ? warmPalette(paletteCoordinate)
                : palette(paletteCoordinate);
        vec3 coldThermal = isWarm(geometry.a)
                ? warmPalette(coldCoordinate)
                : palette(coldCoordinate);
        float thermalRowBlend = rowPattern.x * rowPattern.z * sensorRowResponse;
        vec3 thermal = mix(baseThermal, coldThermal, clamp(thermalRowBlend, 0.0, 1.0));
        float flashCoordinate = mix(
                paletteCoordinate,
                coldCoordinate,
                clamp(thermalRowBlend, 0.0, 1.0)
        );
        thermal = isWarm(geometry.a)
                ? applyActivationWarmPaletteFlash(flashCoordinate, thermal)
                : applyActivationFullPaletteFlash(flashCoordinate, thermal);
        float control = isHotBlock(geometry.a) ? HotBlockOpacity : ThermalModelOpacity;
        float opacity = mix(0.62, 0.86, clamp(InnerStrength, 0.0, 1.0))
                * (1.0 - pow(1.0 - clamp(control, 0.0, 1.0), 1.45));
        float sourceLuma = dot(source.rgb, vec3(0.299, 0.587, 0.114));
        vec3 texturedGray = clamp(gray * mix(0.86, 1.14, sourceLuma), 0.0, 1.0);
        result = mix(texturedGray, thermal, clamp(opacity * mix(0.86, 1.0, adjustedHeat), 0.0, 0.92));
        result += thermal * (0.025 + adjustedHeat * 0.04);
    } else if (DisableGlow == 0 && geometry.b > 0.0001) {
        float glow = pow(clamp(geometry.b, 0.0, 1.0), 0.68)
                * OuterGlowStrength * GlowStrengthMultiplier * glowResponse(geometry.a);
        vec3 glowColor = mix(vec3(0.00, 0.03, 0.85), vec3(0.00, 0.66, 1.00), geometry.b);
        result = mix(gray, clamp(gray * 0.38 + glowColor * 1.15, 0.0, 1.0), clamp(glow * 1.75, 0.0, 0.94));
    }
    float backgroundMask = (inside ? 0.0 : 1.0)
            * (1.0 - smoothstep(0.0001, 0.16, geometry.b));
    float worldFlash = clamp(ActivationFlash * ActivationWorldFlashStrength * backgroundMask, 0.0, 0.35);
    result = mix(result, max(result, vec3(0.82, 0.90, 1.00)), worldFlash);
    fragColor = vec4(applyGlobalSensorRows(
            clamp(result, 0.0, 1.0),
            rowPattern,
            inside ? 0.0 : mix(0.32, 0.65, clamp(geometry.b, 0.0, 1.0))
    ), 1.0);
}
