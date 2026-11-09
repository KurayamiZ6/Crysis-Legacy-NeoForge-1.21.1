#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D ThermalFieldSampler;

in vec2 texCoord;

uniform vec2 InSize;
uniform float ActivationFlash;
uniform float ActivationWorldFlashStrength;
uniform float EncodedHeightRangePx;
uniform float OuterGlowRatio;
uniform float GlowRadiusMultiplier;
uniform int ThermalQuality;
uniform int NanovisionMode;
uniform int ThermalPresetMode;
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
uniform int DebugGeometryReliefOnly;

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

bool isCold(float sourceType) { return sourceType < 0.215; }
bool isCool(float sourceType) { return sourceType >= 0.215 && sourceType < 0.36; }
bool isUnknown(float sourceType) { return sourceType >= 0.36 && sourceType < 0.50; }
bool isWarmBlooded(float sourceType) { return sourceType >= 0.50 && sourceType < 0.645; }
bool isHot(float sourceType) { return sourceType >= 0.645 && sourceType < 0.79; }
bool isHotEmitter(float sourceType) { return sourceType >= 0.79 && sourceType < 0.93; }
bool isHotBlock(float sourceType) { return sourceType >= 0.93; }

vec2 heatBand(float sourceType) {
    if (isCold(sourceType)) return vec2(0.06, 0.36);
    if (isCool(sourceType)) return vec2(0.20, 0.52);
    if (isUnknown(sourceType)) return vec2(0.24, 0.70);
    if (isWarmBlooded(sourceType)) return vec2(0.46, 0.98);
    if (isHot(sourceType)) return vec2(0.72, 0.99);
    if (isHotEmitter(sourceType)) return vec2(0.88, 1.00);
    return vec2(0.0, 1.0);
}

float constrainClassHeat(float heat, float sourceType) {
    vec2 band = heatBand(sourceType);
    return clamp(heat, band.x, band.y);
}

vec3 applyGrayscaleNight(vec3 color) {
    float luminance = dot(color, vec3(0.299, 0.587, 0.114));
    luminance = pow(max(luminance, 0.0001), 0.9);
    luminance = clamp(luminance * 0.85 + 0.08, 0.0, 1.0);
    return vec3(luminance);
}

vec3 preserveSurfaceDetail(vec3 sourceColor, vec3 grayscale) {
    float luminance = dot(sourceColor, vec3(0.299, 0.587, 0.114));
    float contrast = clamp((luminance - 0.5) * 1.20 + 0.5, 0.0, 1.0);
    return clamp(grayscale * mix(0.84, 1.16, contrast), 0.0, 1.0);
}

vec3 fullThermalPalette(float heat) {
    heat = clamp(heat, 0.0, 1.0);
    vec3 deepBlue = vec3(0.00, 0.02, 0.18);
    vec3 blue = vec3(0.00, 0.12, 0.95);
    vec3 cyan = vec3(0.00, 0.78, 1.00);
    vec3 green = vec3(0.04, 0.96, 0.16);
    vec3 yellow = vec3(1.00, 0.91, 0.00);
    vec3 red = vec3(1.00, 0.015, 0.010);
    vec3 pink = vec3(1.00, 0.50, 0.62);
    vec3 whiteHot = vec3(1.00, 0.97, 0.93);

    if (heat < 0.16) return mix(deepBlue, blue, smoothstep(0.00, 0.16, heat));
    if (heat < 0.30) return mix(blue, cyan, smoothstep(0.16, 0.30, heat));
    if (heat < 0.45) return mix(cyan, green, smoothstep(0.30, 0.45, heat));
    if (heat < 0.60) return mix(green, yellow, smoothstep(0.45, 0.60, heat));
    if (heat < 0.76) return mix(yellow, red, smoothstep(0.60, 0.76, heat));
    if (heat < 0.90) return mix(red, pink, smoothstep(0.76, 0.90, heat));
    return mix(pink, whiteHot, smoothstep(0.90, 1.00, heat));
}

vec3 warmBodyPalette(float heat) {
    heat = clamp(heat, 0.0, 1.0);
    vec3 green = vec3(0.00, 0.96, 0.075);
    vec3 red = vec3(1.00, 0.018, 0.012);
    vec3 pink = vec3(1.00, 0.48, 0.60);
    vec3 whiteHot = vec3(1.00, 0.97, 0.94);

    // Zone widths are normalized together, so expanding one zone shifts the
    // later zones instead of allowing palette intervals to overlap or invert.
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

    vec3 color = mix(green, red, smoothstep(greenEnd - greenRedSoftness, greenEnd + greenRedSoftness, heat));
    color = mix(color, pink, smoothstep(redEnd - redPinkSoftness, redEnd + redPinkSoftness, heat));
    return mix(color, whiteHot, smoothstep(pinkEnd - pinkWhiteSoftness, pinkEnd + pinkWhiteSoftness, heat));
}

vec3 warmBodyPaletteV2(float heat) {
    heat = clamp(heat, 0.0, 1.0);
    vec3 cyanBlue = vec3(0.00, 0.70, 1.00);
    vec3 green = vec3(0.00, 0.96, 0.075);
    vec3 yellow = vec3(1.00, 0.91, 0.00);
    vec3 orange = vec3(1.00, 0.30, 0.00);
    vec3 red = vec3(1.00, 0.018, 0.012);
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
    float greenRedSpan = min(0.14, max(0.035, redWidth / totalWidth * 0.72));
    float cyanEnd = min(0.12, greenEnd * 0.18);
    float greenStop = max(cyanEnd + 0.02, greenEnd - greenRedSpan);
    float yellowStop = greenEnd;
    float orangeStop = mix(greenEnd, redEnd, 0.38);

    if (heat < cyanEnd) {
        return mix(cyanBlue, green, smoothstep(0.0, cyanEnd, heat));
    }
    if (heat < greenStop) return green;
    if (heat < yellowStop) {
        return mix(green, yellow, smoothstep(greenStop, yellowStop, heat));
    }
    if (heat < orangeStop) {
        return mix(yellow, orange, smoothstep(yellowStop, orangeStop, heat));
    }
    if (heat < redEnd) {
        return mix(orange, red, smoothstep(orangeStop, redEnd, heat));
    }
    if (heat < pinkEnd) {
        return mix(red, pink, smoothstep(redEnd, pinkEnd, heat));
    }
    return mix(pink, whiteHot, smoothstep(pinkEnd, 1.0, heat));
}

vec3 hotEmitterPalette(float heat) {
    heat = clamp(heat, 0.0, 1.0);
    vec3 orange = vec3(1.00, 0.25, 0.00);
    vec3 red = vec3(1.00, 0.015, 0.010);
    vec3 pink = vec3(1.00, 0.46, 0.56);
    vec3 whiteHot = vec3(1.00, 0.98, 0.92);
    vec3 color = mix(orange, red, smoothstep(0.70, 0.84, heat));
    color = mix(color, pink, smoothstep(0.82, 0.93, heat));
    return mix(color, whiteHot, smoothstep(0.90, 1.00, heat));
}

float glowResponse(float sourceType) {
    if (isHotBlock(sourceType) || isHotEmitter(sourceType)) return 0.22;
    if (isHot(sourceType)) return 0.30;
    if (isWarmBlooded(sourceType)) return 1.00;
    if (isUnknown(sourceType)) return 0.62;
    if (isCool(sourceType)) return 0.42;
    return 0.30;
}

float glowTapOffset(int distance) {
    if (distance <= 1) return 1.0;
    if (distance == 2) return 3.0;
    if (distance == 3) return 7.0;
    return 13.0;
}

float glowTapWeight(int distance) {
    if (distance <= 1) return 0.240;
    if (distance == 2) return 0.150;
    if (distance == 3) return 0.070;
    return 0.025;
}

float glowKernelNormalization(int maxTap) {
    float normalization = 0.320;
    if (maxTap >= 1) normalization += 0.480;
    if (maxTap >= 2) normalization += 0.300;
    if (maxTap >= 3) normalization += 0.140;
    if (maxTap >= 4) normalization += 0.050;
    return normalization;
}

vec4 resolveVerticalGlow(vec4 center) {
    if (center.g > 0.5) return center;

    int maxTap = ThermalQuality <= 1 ? 2 : (ThermalQuality == 2 ? 3 : 4);
    vec2 texel = vec2(0.0, 1.0) / InSize;
    float accumulatedGlow = center.b * 0.320;
    float strongestContribution = center.b * 0.320;
    float strongestScale = center.r;
    float strongestType = center.a;

    for (int tap = -4; tap <= 4; tap++) {
        int distanceIndex = tap < 0 ? -tap : tap;
        if (distanceIndex == 0 || distanceIndex > maxTap) continue;

        float distancePx = glowTapOffset(distanceIndex);
        vec4 sampleData = texture(ThermalFieldSampler,
                texCoord + texel * distancePx * sign(float(tap)));
        float sourceScale = sampleData.g > 0.5 ? sampleData.b : sampleData.r;
        float sourceGlow = sampleData.g > 0.5 ? 1.0 : sampleData.b;
        if (sampleData.a < 0.1 || sourceGlow <= 0.0) continue;

        float maxRadius = ThermalQuality <= 1 ? 10.0 : (ThermalQuality == 2 ? 16.0 : 24.0);
        float sourceRadius = clamp(
                sourceScale * EncodedHeightRangePx * OuterGlowRatio * GlowRadiusMultiplier,
                1.5,
                maxRadius
        );
        float contribution = sourceGlow
                * (1.0 - smoothstep(0.0, sourceRadius, distancePx))
                * glowTapWeight(distanceIndex);
        accumulatedGlow += contribution;
        if (contribution > strongestContribution) {
            strongestContribution = contribution;
            strongestScale = sourceScale;
            strongestType = sampleData.a;
        }
    }

    float glow = accumulatedGlow / glowKernelNormalization(maxTap);
    return vec4(strongestScale, 0.0, clamp(glow * 1.35, 0.0, 1.0), strongestType);
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

vec3 crysisRowPattern(vec2 uv) {
    vec2 pixel = uv * InSize;
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

float warmSensorResponseV2(float coordinate) {
    float greenWidth = max(WarmGreenWidth, 0.001);
    float redWidth = max(WarmRedWidth, 0.001);
    float pinkWidth = max(WarmPinkWidth, 0.001);
    float whiteWidth = max(WarmWhiteWidth, 0.001);
    float totalWidth = greenWidth + redWidth + pinkWidth + whiteWidth;
    float pinkEnd = (greenWidth + redWidth + pinkWidth) / totalWidth;
    float thermalPeakMask = smoothstep(
            max(pinkEnd + 0.030, 0.955),
            max(pinkEnd + 0.065, 0.982),
            coordinate
    );
    float suppressionBlend = smoothstep(
            0.0,
            0.90,
            clamp(ScanlineWhiteHotSuppression, 0.0, 1.0)
    );
    // thermal_field now creates the coherent peak in the actual WARM surface.
    // Row suppression is therefore a direct consequence of final temperature.
    return 1.0 - thermalPeakMask * suppressionBlend;
}

float warmV2RowCoordinate(float coordinate, float rowCarrier) {
    float greenWidth = max(WarmGreenWidth, 0.001);
    float redWidth = max(WarmRedWidth, 0.001);
    float pinkWidth = max(WarmPinkWidth, 0.001);
    float whiteWidth = max(WarmWhiteWidth, 0.001);
    float totalWidth = greenWidth + redWidth + pinkWidth + whiteWidth;
    float greenEnd = greenWidth / totalWidth;
    float redEnd = (greenWidth + redWidth) / totalWidth;
    float pinkEnd = (greenWidth + redWidth + pinkWidth) / totalWidth;

    // Use a bounded palette-space step instead of a percentage of the full
    // distance. The active row can therefore push an already warm green/yellow
    // boundary visibly toward orange/red while the outermost cyan/green edge
    // remains coherent. Adjacent rows form the reference brick-like rim.
    float redAnchor = mix(redEnd, pinkEnd, 0.055);
    float configuredStrength = clamp(ScanlinePaletteShift / 0.11, 0.0, 1.25);
    float maximumStep = mix(
            0.105,
            0.190,
            smoothstep(greenEnd * 0.58, min(0.98, pinkEnd + 0.025), coordinate)
    ) * configuredStrength;
    float targetDelta = redAnchor - coordinate;
    float boundedDelta = clamp(targetDelta, -maximumStep, maximumStep);
    return coordinate + boundedDelta * clamp(rowCarrier, 0.0, 1.0);
}

float v2EntityClassPosition(float coordinate, float sourceType) {
    vec2 band = heatBand(sourceType);
    return clamp((coordinate - band.x) / max(band.y - band.x, 0.001), 0.0, 1.0);
}

float v2EntitySensorResponse(float coordinate, float sourceType) {
    float classPosition = v2EntityClassPosition(coordinate, sourceType);
    float thermalPeakMask = smoothstep(0.900, 0.978, classPosition);
    float suppressionBlend = smoothstep(
            0.0,
            0.90,
            clamp(ScanlineWhiteHotSuppression, 0.0, 1.0)
    );
    // Every V2 entity class gets a coherent relative peak, but its absolute
    // color remains constrained to the class band. Thus a COOL peak can lose
    // its rows while staying cyan/green instead of becoming biologically hot.
    return 1.0 - thermalPeakMask * suppressionBlend;
}

float v2EntityRowCoordinate(
        float coordinate,
        float sourceType,
        float rowCarrier
) {
    vec2 band = heatBand(sourceType);
    float classSpan = max(band.y - band.x, 0.001);
    float classPosition = v2EntityClassPosition(coordinate, sourceType);
    float rowAnchorPosition = 0.48;
    if (isCold(sourceType)) {
        rowAnchorPosition = 0.30;
    } else if (isCool(sourceType)) {
        rowAnchorPosition = 0.25;
    } else if (isHot(sourceType)) {
        rowAnchorPosition = 0.16;
    } else if (isHotEmitter(sourceType)) {
        rowAnchorPosition = 0.08;
    }

    float configuredStrength = clamp(ScanlinePaletteShift / 0.11, 0.0, 1.25);
    float maximumStep = classSpan * mix(
            0.30,
            0.50,
            smoothstep(0.35, 0.90, classPosition)
    ) * configuredStrength;
    float targetCoordinate = mix(band.x, band.y, rowAnchorPosition);
    float boundedDelta = clamp(
            targetCoordinate - coordinate,
            -maximumStep,
            maximumStep
    );
    return constrainClassHeat(
            coordinate + boundedDelta * clamp(rowCarrier, 0.0, 1.0),
            sourceType
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
    float luminance = dot(color, vec3(0.299, 0.587, 0.114));
    float brightnessWeight = mix(0.75, 1.08, clamp(luminance, 0.0, 1.0));
    float darken = rowPattern.y * clamp(ScanlineStrength, 0.0, 0.25)
            * brightnessWeight * clamp(visibility, 0.0, 1.0) * 0.58;
    return clamp(color * (1.0 - darken), 0.0, 1.0);
}

vec3 applyUnifiedV2SensorRows(vec3 color, vec3 rowPattern, float visibility) {
    float luminance = dot(color, vec3(0.299, 0.587, 0.114));
    float brightnessWeight = mix(0.78, 1.05, clamp(luminance, 0.0, 1.0));
    float contrast = clamp(ScanlineStrength, 0.0, 0.25)
            * brightnessWeight * clamp(visibility, 0.0, 1.0) * 0.32;
    // The chromatic thermal row and the darker global sensor row share the
    // exact same physical framebuffer band. The opposite band is brightened
    // by the same amount, preserving average exposure.
    float rowPolarity = rowPattern.x * 2.0 - 1.0;
    return clamp(color * (1.0 - rowPolarity * contrast), 0.0, 1.0);
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
    float cyanEnd = ThermalPresetMode == 2 ? min(0.12, greenEnd * 0.18) : 0.0;
    float heatMask = ThermalPresetMode == 2
            ? smoothstep(cyanEnd, cyanEnd + 0.055, coordinate)
            : 1.0;
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
    return mix(color, hotter, amount * heatMask);
}

void main() {
    vec4 source = texture(DiffuseSampler, texCoord);
    vec4 field = texture(ThermalFieldSampler, texCoord);
    if (DebugGeometryReliefOnly == 0 && DisableGlow == 0 && field.g <= 0.5) {
        field = resolveVerticalGlow(field);
    }
    vec3 grayscale = applyGrayscaleNight(source.rgb);

    if (DebugPassthrough != 0 || NanovisionMode == MODE_OFF) {
        fragColor = vec4(source.rgb, 1.0);
        return;
    }
    if (DebugOpaqueGrayOnly != 0 || NanovisionMode == MODE_GRAYSCALE_NIGHT) {
        fragColor = vec4(grayscale, 1.0);
        return;
    }

    bool insideThermal = field.g > 0.5;
    if (DebugGeometryReliefOnly != 0) {
        // Pure unified WARM-field view. It intentionally bypasses source
        // texture detail, glow and scanlines while retaining the configured
        // green/red/pink/white width semantics.
        if (!insideThermal || !isWarmBlooded(field.a)) {
            fragColor = vec4(vec3(0.012), 1.0);
            return;
        }
        fragColor = vec4(
                ThermalPresetMode == 2
                        ? warmBodyPaletteV2(clamp(field.r, 0.0, 1.0))
                        : warmBodyPalette(clamp(field.r, 0.0, 1.0)),
                1.0
        );
        return;
    }
    if (DebugRawMarker != 0 || NanovisionMode == MODE_DEBUG_MARKER) {
        vec3 debugColor = insideThermal ? vec3(0.02, 1.00, 0.00) : grayscale;
        fragColor = vec4(debugColor, 1.0);
        return;
    }
    if (DebugDetectedMask != 0) {
        fragColor = vec4(insideThermal ? vec3(1.0) : vec3(0.0), 1.0);
        return;
    }
    if (DebugDistance != 0 || DisablePalette != 0 || NanovisionMode == MODE_DEBUG_DISTANCE) {
        fragColor = vec4(insideThermal ? vec3(field.r) : vec3(0.0), 1.0);
        return;
    }

    vec3 result = grayscale;
    vec3 rowPattern = crysisRowPattern(texCoord);
    float v2GlobalRowVisibility = mix(0.45, 0.75, clamp(field.b, 0.0, 1.0));
    if (insideThermal) {
        float heat = clamp(field.r, 0.0, 1.0);
        heat = constrainClassHeat(heat, field.a);
        bool useV2EntitySurface = ThermalPresetMode == 2 && !isHotBlock(field.a);
        float paletteCoordinate = heat;
        float sensorRowResponse;
        if (isWarmBlooded(field.a)) {
            paletteCoordinate = clamp((field.g - 0.51) / 0.48, 0.0, 1.0);
            sensorRowResponse = ThermalPresetMode == 2
                    ? warmSensorResponseV2(paletteCoordinate)
                    : warmSensorResponse(paletteCoordinate);
        } else if (useV2EntitySurface) {
            paletteCoordinate = constrainClassHeat(
                    clamp((field.g - 0.51) / 0.48, 0.0, 1.0),
                    field.a
            );
            sensorRowResponse = v2EntitySensorResponse(
                    paletteCoordinate,
                    field.a
            );
        } else {
            sensorRowResponse = fullPaletteSensorResponse(paletteCoordinate);
        }
        if (ThermalPresetMode == 2) {
            // The extreme hot peak suppresses both the red palette row and the
            // weak global luminance carrier. Ordinary thermal surfaces retain
            // the common screen-space phase at a much lower brightness gain.
            v2GlobalRowVisibility = sensorRowResponse * 0.30;
        }
        float bandMix = clamp(BandingStrength * 0.06, 0.0, 0.075);
        float banded = floor(paletteCoordinate * 18.0) / 18.0;
        paletteCoordinate = mix(paletteCoordinate, banded, bandMix);

        float coldShift = clamp(ScanlinePaletteShift, 0.0, 0.18);
        float coldCoordinate = isWarmBlooded(field.a)
                ? clamp(paletteCoordinate - coldShift, 0.0, 1.0)
                : constrainClassHeat(paletteCoordinate - coldShift, field.a);
        vec3 baseThermal = fullThermalPalette(paletteCoordinate);
        vec3 coldThermal = fullThermalPalette(coldCoordinate);
        if (isWarmBlooded(field.a)) {
            baseThermal = ThermalPresetMode == 2
                    ? warmBodyPaletteV2(paletteCoordinate)
                    : warmBodyPalette(paletteCoordinate);
            coldThermal = ThermalPresetMode == 2
                    ? warmBodyPaletteV2(coldCoordinate)
                    : warmBodyPalette(coldCoordinate);
        } else if (isHotEmitter(field.a)) {
            baseThermal = hotEmitterPalette(paletteCoordinate);
            coldThermal = hotEmitterPalette(coldCoordinate);
        }
        float thermalRowBlend = rowPattern.x
                * (useV2EntitySurface ? 1.0 : rowPattern.z)
                * sensorRowResponse;
        vec3 thermal;
        float flashCoordinate;
        if (ThermalPresetMode == 2 && isWarmBlooded(field.a)) {
            float rowCoordinate = warmV2RowCoordinate(paletteCoordinate, thermalRowBlend);
            thermal = warmBodyPaletteV2(rowCoordinate);
            flashCoordinate = rowCoordinate;
        } else if (useV2EntitySurface) {
            float rowCoordinate = v2EntityRowCoordinate(
                    paletteCoordinate,
                    field.a,
                    thermalRowBlend
            );
            thermal = isHotEmitter(field.a)
                    ? hotEmitterPalette(rowCoordinate)
                    : fullThermalPalette(rowCoordinate);
            flashCoordinate = rowCoordinate;
        } else {
            thermal = mix(baseThermal, coldThermal, clamp(thermalRowBlend, 0.0, 1.0));
            flashCoordinate = mix(
                    paletteCoordinate,
                    coldCoordinate,
                    clamp(thermalRowBlend, 0.0, 1.0)
            );
        }
        thermal = isWarmBlooded(field.a)
                ? applyActivationWarmPaletteFlash(flashCoordinate, thermal)
                : applyActivationFullPaletteFlash(flashCoordinate, thermal);

        float opacityControl = isHotBlock(field.a)
                ? clamp(HotBlockOpacity, 0.0, 1.0)
                : clamp(ThermalModelOpacity, 0.0, 1.0);
        float opacityCurve = 1.0 - pow(1.0 - opacityControl, 1.45);
        float opacity = mix(0.60, 0.84, clamp(InnerStrength, 0.0, 1.0)) * opacityCurve;
        opacity *= mix(0.80, 1.0, heat);

        vec3 textureBase = preserveSurfaceDetail(source.rgb, grayscale);
        result = mix(textureBase, thermal, clamp(opacity, 0.0, 0.90));
        result += thermal * (0.025 + heat * 0.045);
    } else if (DisableGlow == 0 && field.b > 0.0001) {
        float response = glowResponse(field.a);
        float glow = pow(clamp(field.b, 0.0, 1.0), 0.62)
                * clamp(OuterGlowStrength * GlowStrengthMultiplier, 0.0, 1.0)
                * response;
        vec3 deepBlue = vec3(0.00, 0.035, 0.78);
        vec3 cyanBlue = vec3(0.00, 0.58, 1.00);
        vec3 glowColor = mix(deepBlue, cyanBlue, smoothstep(0.35, 1.0, field.b));
        vec3 luminousGlow = clamp(grayscale * 0.42 + glowColor * 1.10, 0.0, 1.0);
        result = mix(grayscale, luminousGlow, clamp(glow * 1.65, 0.0, 0.92));
    }

    float backgroundMask = (insideThermal ? 0.0 : 1.0)
            * (1.0 - smoothstep(0.0001, 0.16, field.b));
    float worldFlash = clamp(ActivationFlash * ActivationWorldFlashStrength * backgroundMask, 0.0, 0.35);
    result = mix(result, max(result, vec3(0.82, 0.90, 1.00)), worldFlash);

    result = ThermalPresetMode == 2
            ? applyUnifiedV2SensorRows(result, rowPattern, v2GlobalRowVisibility)
            : applyGlobalSensorRows(
                    result,
                    rowPattern,
                    insideThermal ? 0.0 : mix(0.32, 0.65, clamp(field.b, 0.0, 1.0))
            );
    fragColor = vec4(clamp(result, 0.0, 1.0), 1.0);
}
