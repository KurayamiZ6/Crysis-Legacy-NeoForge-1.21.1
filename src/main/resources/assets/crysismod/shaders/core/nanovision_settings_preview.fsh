#version 150

uniform sampler2D SourceSampler;
uniform sampler2D DepthSampler;
uniform vec2 InSize;
uniform int MarkerEnabled;
uniform float InnerRadiusRatio;
uniform float OuterGlowRatio;
uniform float InnerStrength;
uniform float ModelOpacity;
uniform float OuterGlowStrength;
uniform float HeatPower;
uniform float BandingStrength;
uniform float WarmGreenWidth;
uniform float WarmRedWidth;
uniform float WarmPinkWidth;
uniform float WarmWhiteWidth;
uniform float MaxSearchRadiusPx;
uniform int ThermalQuality;
uniform int CinematicPreset;
uniform int ThermalPresetMode;
uniform float ImprovedV2HotPeakArea;
uniform float ScanlineStrength;
uniform float ScanlineSpacingPx;
uniform float ScanlineSharpness;
uniform float ScanlineIrregularityPx;
uniform float ScanlinePaletteShift;
uniform float ScanlineBreakup;
uniform float ScanlineWhiteHotSuppression;
uniform int DebugRawMarker;
uniform int DebugMask;
uniform int DebugDistance;
uniform int DisablePalette;

in vec2 texCoord0;
out vec4 fragColor;

float alphaAt(vec2 offsetPx) {
    return texture(SourceSampler, texCoord0 + offsetPx / InSize).a;
}

float ringMaximum(float radiusPx) {
    const float D = 0.70710678;
    float value = 0.0;
    value = max(value, alphaAt(vec2( radiusPx, 0.0)));
    value = max(value, alphaAt(vec2(-radiusPx, 0.0)));
    value = max(value, alphaAt(vec2(0.0,  radiusPx)));
    value = max(value, alphaAt(vec2(0.0, -radiusPx)));
    value = max(value, alphaAt(vec2( radiusPx * D,  radiusPx * D)));
    value = max(value, alphaAt(vec2(-radiusPx * D,  radiusPx * D)));
    value = max(value, alphaAt(vec2( radiusPx * D, -radiusPx * D)));
    return max(value, alphaAt(vec2(-radiusPx * D, -radiusPx * D)));
}

float ringAverage(float radiusPx) {
    const float D = 0.70710678;
    float value = 0.0;
    value += alphaAt(vec2( radiusPx, 0.0));
    value += alphaAt(vec2(-radiusPx, 0.0));
    value += alphaAt(vec2(0.0,  radiusPx));
    value += alphaAt(vec2(0.0, -radiusPx));
    if (ThermalQuality >= 2) {
        value += alphaAt(vec2( radiusPx * D,  radiusPx * D));
        value += alphaAt(vec2(-radiusPx * D,  radiusPx * D));
        value += alphaAt(vec2( radiusPx * D, -radiusPx * D));
        value += alphaAt(vec2(-radiusPx * D, -radiusPx * D));
        return value * 0.125;
    }
    return value * 0.25;
}

float internalDepthEdge(float radiusPx) {
    float center = texture(DepthSampler, texCoord0).r;
    vec2 texel = 1.0 / InSize;
    float strongest = 0.0;
    int limit = ThermalQuality <= 1 ? 2 : 4;
    float threshold = max(0.0000035, max(1.0 - center, 0.00005) * 0.0026) * (1.0 + radiusPx * 0.28);
    for (int i = 0; i < 4; i++) {
        if (i >= limit) break;
        vec2 direction = i == 0 ? vec2(1.0, 0.0)
                : (i == 1 ? vec2(0.0, 1.0)
                : (i == 2 ? vec2(0.70710678, 0.70710678)
                : vec2(0.70710678, -0.70710678)));
        vec2 offset = direction * radiusPx * texel;
        float valid = step(0.1, texture(SourceSampler, texCoord0 + offset).a)
                * step(0.1, texture(SourceSampler, texCoord0 - offset).a);
        if (valid < 0.5) continue;
        float positive = texture(DepthSampler, texCoord0 + offset).r;
        float negative = texture(DepthSampler, texCoord0 - offset).r;
        float curvature = abs(positive + negative - center * 2.0);
        strongest = max(strongest, smoothstep(threshold, threshold * 6.0, curvature));
    }
    return strongest;
}

vec4 warmBounds() {
    float green = max(WarmGreenWidth, 0.001);
    float red = max(WarmRedWidth, 0.001);
    float pink = max(WarmPinkWidth, 0.001);
    float white = max(WarmWhiteWidth, 0.001);
    float total = green + red + pink + white;
    return vec4(green / total, (green + red) / total, (green + red + pink) / total, total);
}

vec3 warmPalette(float heat, vec4 bounds) {
    vec3 green = vec3(0.00, 0.96, 0.075);
    vec3 red = vec3(1.00, 0.018, 0.012);
    vec3 pink = vec3(1.00, 0.48, 0.60);
    vec3 white = vec3(1.00, 0.97, 0.94);
    float a = min(0.12, min(WarmGreenWidth, WarmRedWidth) / bounds.w * 0.72);
    float b = min(0.12, min(WarmRedWidth, WarmPinkWidth) / bounds.w * 0.72);
    float c = min(0.12, min(WarmPinkWidth, WarmWhiteWidth) / bounds.w * 0.72);
    vec3 result = mix(green, red, smoothstep(bounds.x - a, bounds.x + a, heat));
    result = mix(result, pink, smoothstep(bounds.y - b, bounds.y + b, heat));
    return mix(result, white, smoothstep(bounds.z - c, bounds.z + c, heat));
}

vec3 warmPaletteV2(float heat, vec4 bounds) {
    heat = clamp(heat, 0.0, 1.0);
    vec3 cyanBlue = vec3(0.00, 0.70, 1.00);
    vec3 green = vec3(0.00, 0.96, 0.075);
    vec3 yellow = vec3(1.00, 0.91, 0.00);
    vec3 orange = vec3(1.00, 0.30, 0.00);
    vec3 red = vec3(1.00, 0.018, 0.012);
    vec3 pink = vec3(1.00, 0.48, 0.60);
    vec3 white = vec3(1.00, 0.97, 0.94);
    float transitionSpan = min(0.14, max(0.035, WarmRedWidth / bounds.w * 0.72));
    float cyanEnd = min(0.12, bounds.x * 0.18);
    float greenStop = max(cyanEnd + 0.02, bounds.x - transitionSpan);
    float orangeStop = mix(bounds.x, bounds.y, 0.38);
    if (heat < cyanEnd) return mix(cyanBlue, green, smoothstep(0.0, cyanEnd, heat));
    if (heat < greenStop) return green;
    if (heat < bounds.x) return mix(green, yellow, smoothstep(greenStop, bounds.x, heat));
    if (heat < orangeStop) return mix(yellow, orange, smoothstep(bounds.x, orangeStop, heat));
    if (heat < bounds.y) return mix(orange, red, smoothstep(orangeStop, bounds.y, heat));
    if (heat < bounds.z) return mix(red, pink, smoothstep(bounds.y, bounds.z, heat));
    return mix(pink, white, smoothstep(bounds.z, 1.0, heat));
}

float sensorHash(vec2 cell) {
    return fract(sin(dot(cell, vec2(12.9898, 78.233))) * 43758.5453);
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
    float spacing = max(ScanlineSpacingPx, 1.0);
    float bandHeight = max(1.0, spacing * 0.50);
    float bandIndex = floor(gl_FragCoord.y / bandHeight);
    float activeRow = 1.0 - step(0.5, mod(bandIndex, 2.0));
    float globalPhase = fract(gl_FragCoord.y / spacing);
    float globalLine = pow(
            clamp(1.0 - abs(globalPhase * 2.0 - 1.0), 0.0, 1.0),
            max(ScanlineSharpness, 0.5)
    );
    float irregularMix = sqrt(clamp(ScanlineIrregularityPx / 1.5, 0.0, 1.0));
    float activeIndex = floor(bandIndex * 0.5);
    float rowSeed = sensorHash(vec2(activeIndex, 19.73));
    float coarseSegments = sensorValueNoise(
            (gl_FragCoord.x + rowSeed * 91.0) / 44.0,
            activeIndex * 1.37 + 3.1
    );
    float fineSegments = sensorValueNoise(
            (gl_FragCoord.x - rowSeed * 37.0) / 11.0,
            activeIndex * 2.11 + 7.9
    );
    float coarseCoverage = smoothstep(0.16, 0.46, coarseSegments);
    float fineCoverage = mix(0.74, 1.0, smoothstep(0.18, 0.82, fineSegments));
    float hardGap = step(0.12 + irregularMix * 0.035, coarseSegments);
    float pixelGrain = sensorHash(vec2(floor(gl_FragCoord.x * 0.5), activeIndex * 3.17 + 29.0));
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

float warmSensorResponse(float coordinate, vec4 bounds) {
    float whiteVisibility = 1.0 - smoothstep(
            max(0.0, bounds.z - 0.015),
            min(1.0, bounds.z + 0.065),
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

float warmSensorResponseV2(float coordinate, vec4 bounds) {
    float thermalPeakMask = smoothstep(
            max(bounds.z + 0.030, 0.955),
            max(bounds.z + 0.065, 0.982),
            coordinate
    );
    float suppressionBlend = smoothstep(
            0.0,
            0.90,
            clamp(ScanlineWhiteHotSuppression, 0.0, 1.0)
    );
    return 1.0 - thermalPeakMask * suppressionBlend;
}

float warmV2RowCoordinate(float coordinate, float rowCarrier, vec4 bounds) {
    float redAnchor = mix(bounds.y, bounds.z, 0.055);
    float configuredStrength = clamp(ScanlinePaletteShift / 0.11, 0.0, 1.25);
    float maximumStep = mix(
            0.105,
            0.190,
            smoothstep(bounds.x * 0.58, min(0.98, bounds.z + 0.025), coordinate)
    ) * configuredStrength;
    float boundedDelta = clamp(redAnchor - coordinate, -maximumStep, maximumStep);
    return coordinate + boundedDelta * clamp(rowCarrier, 0.0, 1.0);
}

vec3 applyGlobalSensorRows(vec3 color, vec3 rowPattern, float visibility) {
    float luma = clamp(dot(color, vec3(0.299, 0.587, 0.114)), 0.0, 1.0);
    float darken = rowPattern.y * clamp(ScanlineStrength, 0.0, 0.25)
            * mix(0.75, 1.08, luma)
            * clamp(visibility, 0.0, 1.0) * 0.58;
    return color * (1.0 - darken);
}

vec3 applyUnifiedV2SensorRows(vec3 color, vec3 rowPattern, float visibility) {
    float luma = clamp(dot(color, vec3(0.299, 0.587, 0.114)), 0.0, 1.0);
    float contrast = clamp(ScanlineStrength, 0.0, 0.25)
            * mix(0.78, 1.05, luma) * clamp(visibility, 0.0, 1.0) * 0.32;
    float rowPolarity = rowPattern.x * 2.0 - 1.0;
    return clamp(color * (1.0 - rowPolarity * contrast), 0.0, 1.0);
}

void main() {
    vec4 source = texture(SourceSampler, texCoord0);
    float mask = source.a;
    vec3 rowPattern = crysisRowPattern();

    if (mask < 0.01) {
        if (MarkerEnabled == 0 || OuterGlowStrength <= 0.001 || OuterGlowRatio <= 0.0001) discard;
        float qualityRadius = ThermalQuality <= 1 ? 0.72 : (ThermalQuality == 2 ? 1.0 : 1.28);
        float radius = clamp(min(InSize.x, InSize.y) * OuterGlowRatio * 0.14 * qualityRadius, 2.0, min(30.0, MaxSearchRadiusPx));
        float nearGlow = ringMaximum(max(2.0, radius * 0.46));
        float farGlow = ringMaximum(radius);
        float glow = max(nearGlow * 0.78, farGlow * 0.42) * OuterGlowStrength;
        if (glow <= 0.003) discard;
        vec3 blue = mix(vec3(0.00, 0.035, 0.78), vec3(0.00, 0.58, 1.00), nearGlow);
        vec3 stripedGlow = ThermalPresetMode == 2
                ? applyUnifiedV2SensorRows(blue, rowPattern, mix(0.45, 0.75, nearGlow))
                : applyGlobalSensorRows(blue, rowPattern, mix(0.32, 0.65, nearGlow));
        fragColor = vec4(
                stripedGlow,
                clamp(glow * 0.72, 0.0, 0.68));
        return;
    }

    float luma = dot(source.rgb, vec3(0.299, 0.587, 0.114));
    if (MarkerEnabled == 0) {
        fragColor = vec4(vec3(luma), mask);
        return;
    }

    float edgeRadius = clamp(4.0 + InnerRadiusRatio * 32.0, 4.0, min(36.0, MaxSearchRadiusPx));
    float edgeNear = 1.0 - ringAverage(max(1.5, edgeRadius * 0.42));
    float edgeFar = 1.0 - ringAverage(edgeRadius);
    float silhouette = clamp(max(edgeNear, edgeFar * 0.78), 0.0, 1.0);
    float depthEdge = max(internalDepthEdge(1.5), internalDepthEdge(ThermalQuality >= 3 ? 4.5 : 3.0) * 0.72);

    vec4 bounds = warmBounds();
    float cooling = clamp(max(silhouette, depthEdge * (CinematicPreset != 0 ? 0.88 : 0.72)), 0.0, 0.94);
    float heat = 0.86 + clamp((luma - 0.5) * 0.12, -0.06, 0.06);
    heat = pow(clamp(heat, 0.01, 0.99), max(HeatPower * 0.34, 0.01));
    float classHeat = clamp((heat - 0.46) / 0.52, 0.0, 1.0);
    float centerReach = mix(0.78, 0.96, smoothstep(0.10, 0.95, classHeat));
    float layerProgress = 1.0 - cooling;
    float paletteCoordinate;
    if (ThermalPresetMode == 2) {
        float ordinaryPlateau = clamp(0.940 + (luma - 0.5) * 0.012, 0.928, 0.952);
        float biologicalSupport = mix(0.72, 1.0, smoothstep(0.28, 0.90, classHeat));
        float configuredArea = clamp(ImprovedV2HotPeakArea, 0.0, 1.0);
        float peakExponent = mix(3.40, 1.00, configuredArea);
        float centralHotPeak = pow(clamp(layerProgress, 0.0, 1.0), peakExponent)
                * (1.0 - smoothstep(0.08, 0.72, depthEdge))
                * biologicalSupport * smoothstep(0.0, 0.06, configuredArea);
        float interiorCoordinate = ordinaryPlateau + centralHotPeak * 0.045
                - depthEdge * 0.16;
        float rimProgress = pow(clamp(layerProgress, 0.0, 1.0), 0.72);
        paletteCoordinate = clamp(mix(0.045, interiorCoordinate, rimProgress), 0.0, 0.985);
    } else {
        paletteCoordinate = clamp(layerProgress * centerReach - depthEdge * 0.025, 0.0, 0.985);
    }
    float sensorRowResponse = ThermalPresetMode == 2
            ? warmSensorResponseV2(paletteCoordinate, bounds)
            : warmSensorResponse(paletteCoordinate, bounds);
    paletteCoordinate = mix(
            paletteCoordinate,
            floor(paletteCoordinate * 18.0) / 18.0,
            clamp(BandingStrength * 0.06, 0.0, 0.075)
    );

    if (DebugMask != 0) {
        fragColor = vec4(vec3(1.0), mask);
        return;
    }
    if (DebugDistance != 0 || DisablePalette != 0) {
        fragColor = vec4(vec3(heat), mask);
        return;
    }
    if (DebugRawMarker != 0) {
        fragColor = vec4(luma, depthEdge, silhouette, mask);
        return;
    }

    float coldCoordinate = clamp(
            paletteCoordinate - clamp(ScanlinePaletteShift, 0.0, 0.18),
            0.0,
            1.0
    );
    vec3 baseThermal = ThermalPresetMode == 2
            ? warmPaletteV2(paletteCoordinate, bounds)
            : warmPalette(paletteCoordinate, bounds);
    vec3 coldThermal = ThermalPresetMode == 2
            ? warmPaletteV2(coldCoordinate, bounds)
            : warmPalette(coldCoordinate, bounds);
    float thermalRowBlend = rowPattern.x
            * (ThermalPresetMode == 2 ? 1.0 : rowPattern.z)
            * sensorRowResponse;
    vec3 thermal = ThermalPresetMode == 2
            ? warmPaletteV2(warmV2RowCoordinate(paletteCoordinate, thermalRowBlend, bounds), bounds)
            : mix(baseThermal, coldThermal, clamp(thermalRowBlend, 0.0, 1.0));
    float opacityCurve = 1.0 - pow(1.0 - clamp(ModelOpacity, 0.0, 1.0), 1.45);
    float opacity = mix(0.60, 0.90, clamp(InnerStrength, 0.0, 1.0)) * opacityCurve;
    vec3 base = vec3(pow(max(luma, 0.0001), 0.9));
    vec3 result = mix(base, thermal, clamp(opacity, 0.0, 0.94));
    result += thermal * (0.025 + heat * 0.045);
    if (ThermalPresetMode == 2) {
        result = applyUnifiedV2SensorRows(result, rowPattern, sensorRowResponse * 0.30);
    }
    fragColor = vec4(clamp(result, 0.0, 1.0), mask);
}
