#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform int PatternMode;
uniform float TimeSeconds;
uniform float OverlayAlpha;
uniform int ClassicModeTint;
uniform float OverlayBrightness;
uniform float XAlpha;
uniform float XSizePx;
uniform float XSpacingPx;
uniform float XLineWidthPx;
uniform float XEdgeSoftnessPx;
uniform float XSpeedPxPerSecond;
uniform float XDirectionX;
uniform float XDirectionY;
uniform float TextureSizePx;
uniform int TransitionEnabled;
uniform float TransitionProgress;
uniform float TransitionPixelBlockSizePx;
uniform float TransitionStaggerAmount;
uniform float TransitionFadeWindow;
uniform float TransitionSeed;
uniform int TransitionAffectsPulses;
uniform int XAsCutout;
uniform float HexCellSizePx;
uniform float HexLineWidthPx;
uniform float HexEdgeSoftnessPx;
uniform float HexInnerAlpha;
uniform float HexGlowAlpha;
uniform float HexFrequency;
uniform float HexScrollSpeedPxPerSecond;
uniform float HexDirectionX;
uniform float HexDirectionY;
uniform float HexPatternScale;
uniform float HexGlowBoost;
uniform float SheenBreathingAmount;
uniform float SheenBreathingSpeed;
uniform float SheenAlpha;
uniform float SheenTransparency;
uniform float SheenWidthPx;
uniform float SheenSoftnessPx;
uniform float SheenRepeatPx;
uniform float SheenSpeedPxPerSecond;
uniform float SheenDirectionX;
uniform float SheenDirectionY;
uniform float SheenColorBoost;
uniform int FiberFlowEnabled;
uniform float FiberFlowStrength;
uniform float FiberFlowScale;
uniform float FiberFlowSpeed;
uniform float PlasmaBlobInnerAlpha;
uniform int PlasmaBlobCount;
uniform float PlasmaBlobMinSizePx;
uniform float PlasmaBlobMaxSizePx;
uniform float PlasmaBlobEdgeSoftnessPx;
uniform float PlasmaBlobRimAlpha;
uniform float PlasmaBlobRimWidthPx;
uniform float PlasmaBlobRimBrightness;
uniform float PlasmaBlobDriftSpeedPxPerSecond;
uniform float PlasmaBlobDriftRangeScale;
uniform float PlasmaBlobShapeDistortion;
uniform float PatchTextureAlpha;
uniform float PatchSpotAlpha;
uniform float PatchMinSizePx;
uniform float PatchMaxSizePx;
uniform float PatchFrequency;
uniform float PatchDensity;
uniform int PatchFlowVariant;
uniform int PulseEnabled;
uniform int PulseVisualMode;
uniform int PulseCenterCount;
uniform float PulseCenterX[64];
uniform float PulseCenterY[64];
uniform float PulseIntervalSeconds;
uniform float PulseDurationSeconds;
uniform float PulseStartRadiusPx;
uniform float PulseEndRadiusPx;
uniform float PulseCoreWidthPx;
uniform float PulseGlowRadiusPx;
uniform float PulseCoreSoftnessPx;
uniform float PulseGlowSoftnessPx;
uniform float PulseCoreAlpha;
uniform float PulseGlowAlpha;
uniform float PulseBrightness;
uniform float PulseFadePower;
uniform float PulseRingDistortionPx;
uniform float PulseRingDistortionFrequency;
uniform float PulseRingEnergyVariation;
uniform float PulseWidthVariation;
uniform float PulseBrightnessVariation;
uniform float PulseEdgeRoughness;
uniform float PulsePatternRotationRandomness;
uniform int PulseRingMergingEnabled;
uniform int PulseDebugForceVisible;
uniform int PulseDebugOutputOnlyPulse;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

const float SQRT3 = 1.73205080757;
const float TAU = 6.28318530718;
const int MAX_PLASMA_OVERLAPPING_WAVES = 3;
const int MAX_RING_PULSE_CENTERS = 64;

vec4 sanitized_lightmap_color(vec4 sampledLightmap) {
    float maxChannel = max(max(sampledLightmap.r, sampledLightmap.g), max(sampledLightmap.b, sampledLightmap.a));
    if (maxChannel <= 0.0001) {
        return vec4(1.0);
    }
    return sampledLightmap;
}

float x_mask(vec2 uv) {
    float pixelX = uv.x * TextureSizePx;
    float pixelY = uv.y * TextureSizePx;
    float timeOffset = TimeSeconds * XSpeedPxPerSecond;
    float spacing = max(XSpacingPx, 1.0);
    float animatedX = pixelX + XDirectionX * timeOffset;
    float animatedY = pixelY + XDirectionY * timeOffset;
    float center = spacing * 0.5;
    float localX = mod(animatedX, spacing) - center;
    float localY = mod(animatedY, spacing) - center;
    float halfSize = max(XSizePx * 0.5, 0.5);

    if (abs(localX) > halfSize || abs(localY) > halfSize) {
        return 0.0;
    }

    float lineWidth = max(XLineWidthPx, 0.0001);
    float softness = max(XEdgeSoftnessPx, 0.0001);
    float diagA = abs(localX - localY);
    float diagB = abs(localX + localY);
    float lineA = 1.0 - smoothstep(lineWidth, lineWidth + softness, diagA);
    float lineB = 1.0 - smoothstep(lineWidth, lineWidth + softness, diagB);
    float boxFadeX = 1.0 - smoothstep(max(halfSize - softness, 0.0), halfSize, abs(localX));
    float boxFadeY = 1.0 - smoothstep(max(halfSize - softness, 0.0), halfSize, abs(localY));
    return clamp(max(lineA, lineB) * boxFadeX * boxFadeY, 0.0, 1.0);
}

vec2 hex_local(vec2 pixel, float radius) {
    vec2 axial = vec2(
        (SQRT3 / 3.0 * pixel.x - 1.0 / 3.0 * pixel.y) / radius,
        (2.0 / 3.0 * pixel.y) / radius
    );

    vec3 cube = vec3(axial.x, -axial.x - axial.y, axial.y);
    vec3 rounded = floor(cube + 0.5);
    vec3 diff = abs(rounded - cube);

    if (diff.x > diff.y && diff.x > diff.z) {
        rounded.x = -rounded.y - rounded.z;
    } else if (diff.y > diff.z) {
        rounded.y = -rounded.x - rounded.z;
    } else {
        rounded.z = -rounded.x - rounded.y;
    }

    vec2 center = vec2(
        radius * SQRT3 * (rounded.x + rounded.z * 0.5),
        radius * 1.5 * rounded.z
    );

    return pixel - center;
}

float sd_hexagon(vec2 p, float radius) {
    const vec3 k = vec3(-0.866025404, 0.5, 0.577350269);
    p = abs(p);
    p -= 2.0 * min(dot(k.xy, p), 0.0) * k.xy;
    p -= vec2(clamp(p.x, -k.z * radius, k.z * radius), radius);
    return length(p) * sign(p.y);
}

float hex_border_mask(vec2 uv) {
    float frequency = max(HexFrequency, 0.0001);
    float patternScale = max(HexPatternScale, 0.0001);
    float radius = max((HexCellSizePx * patternScale / frequency) * 0.5, 1.0);
    vec2 pixel = uv * TextureSizePx;
    pixel += vec2(HexDirectionX, HexDirectionY) * TimeSeconds * HexScrollSpeedPxPerSecond;
    vec2 local = hex_local(pixel, radius);
    float lineWidth = max(HexLineWidthPx * patternScale, 0.0001);
    float edgeSoftness = max(HexEdgeSoftnessPx * patternScale, 0.0001);
    float shrink = max(lineWidth + edgeSoftness * 0.5, 0.05);
    float innerRadius = max(radius - shrink, 0.1);
    float interiorDistance = sd_hexagon(local, innerRadius);
    float softness = edgeSoftness;
    float interiorMask = 1.0 - smoothstep(-softness, softness, interiorDistance);
    return clamp(1.0 - interiorMask, 0.0, 1.0);
}

float hex_border_mask_pixel_locked(vec2 uv) {
    float frequency = max(HexFrequency, 0.0001);
    float patternScale = max(HexPatternScale, 0.0001);
    float radius = max(round((HexCellSizePx * patternScale / frequency) * 0.5), 1.0);
    vec2 pixel = floor(uv * TextureSizePx) + vec2(0.5);
    vec2 scroll = vec2(HexDirectionX, HexDirectionY) * TimeSeconds * HexScrollSpeedPxPerSecond;
    pixel += floor(scroll + vec2(0.5));
    vec2 local = hex_local(pixel, radius);
    float lineWidth = max(round(HexLineWidthPx * patternScale), 1.0);
    float edgeSoftness = max(round(HexEdgeSoftnessPx * patternScale), 0.0);
    float shrink = max(lineWidth + edgeSoftness * 0.5, 1.0);
    float innerRadius = max(radius - shrink, 0.0);
    float interiorDistance = sd_hexagon(local, innerRadius);

    if (edgeSoftness <= 0.0) {
        float interiorMask = interiorDistance <= 0.0 ? 1.0 : 0.0;
        return clamp(1.0 - interiorMask, 0.0, 1.0);
    }

    float interiorMask = 1.0 - smoothstep(-edgeSoftness, edgeSoftness, interiorDistance);
    return clamp(1.0 - interiorMask, 0.0, 1.0);
}

vec2 safe_normalize(vec2 value, vec2 fallback) {
    float lengthSquared = dot(value, value);
    if (lengthSquared <= 0.000001) {
        return fallback;
    }
    return value * inversesqrt(lengthSquared);
}

float universal_sheen_mask(vec2 uv) {
    vec2 pixel = uv * TextureSizePx;
    vec2 direction = safe_normalize(vec2(SheenDirectionX, SheenDirectionY), normalize(vec2(1.0, 0.35)));
    float projected = dot(pixel, direction);
    float repeatPx = max(SheenRepeatPx, 1.0);
    float moving = projected - TimeSeconds * SheenSpeedPxPerSecond;
    float phase = mod(moving, repeatPx);
    float distanceToCenter = abs(phase - repeatPx * 0.5);
    float bandWidth = max(SheenWidthPx, 0.0);
    float softness = max(SheenSoftnessPx, 0.0001);
    return 1.0 - smoothstep(bandWidth, bandWidth + softness, distanceToCenter);
}

float universal_fiber_flow(vec2 uv) {
    if (FiberFlowEnabled == 0 || FiberFlowStrength <= 0.0001) {
        return 0.0;
    }

    vec2 pixel = uv * TextureSizePx;
    vec2 fiberDirectionA = safe_normalize(vec2(0.92, 0.38), normalize(vec2(0.92, 0.38)));
    vec2 fiberDirectionB = safe_normalize(vec2(-0.41, 0.91), normalize(vec2(-0.41, 0.91)));
    float scale = max(FiberFlowScale, 0.0001);
    float waveA = sin(dot(pixel, fiberDirectionA) * scale + TimeSeconds * FiberFlowSpeed);
    float waveB = sin(dot(pixel, fiberDirectionB) * (scale * 0.73) - TimeSeconds * FiberFlowSpeed * 0.6);
    float blended = 0.5 + 0.25 * waveA + 0.25 * waveB;
    float shaped = smoothstep(0.3, 0.9, blended);
    return shaped * FiberFlowStrength;
}

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float transition_pixel_mask(vec2 uv) {
    if (TransitionEnabled == 0) {
        return 1.0;
    }

    float progress = clamp(TransitionProgress, 0.0, 1.0);
    float blockSizePx = clamp(round(TransitionPixelBlockSizePx), 1.0, 2.0);
    float fadeWindow = clamp(TransitionFadeWindow, 0.0001, 1.0);
    float staggerAmount = clamp(TransitionStaggerAmount, 0.0, 1.0);
    float maxDelay = min(staggerAmount, max(1.0 - fadeWindow, 0.0));
    vec2 pixel = uv * TextureSizePx;
    vec2 blockCoord = floor(pixel / blockSizePx);
    float blockDelay = hash21(blockCoord + vec2(TransitionSeed, TransitionSeed * 0.6180339)) * maxDelay;
    return smoothstep(blockDelay, blockDelay + fadeWindow, progress);
}

float patch_cycle_envelope(float cyclePhase) {
    return smoothstep(0.05, 0.28, cyclePhase) * (1.0 - smoothstep(0.56, 0.96, cyclePhase));
}

float patch_layer_activity_dynamic(vec2 pixel, float blockSizePx, float speed, float seed, float threshold) {
    vec2 shiftedPixel = pixel + vec2(seed * 0.37, seed * 0.19);
    vec2 cell = floor(shiftedPixel / blockSizePx);
    float phaseOffset = hash21(cell + vec2(seed * 1.31, seed * 2.17)) * 4.0;
    float timeline = TimeSeconds * speed + phaseOffset;
    float cycleIndex = floor(timeline);
    float cyclePhase = fract(timeline);
    float gate = step(threshold, hash21(cell + vec2(seed + cycleIndex * 0.6180339, seed * 0.73 + cycleIndex * 0.113)));
    float strength = mix(0.72, 1.0, hash21(cell + vec2(seed * 3.11, cycleIndex + 17.0)));
    return gate * patch_cycle_envelope(cyclePhase) * strength;
}

float patch_layer_activity_fixed_archetype(vec2 pixel, float blockSizePx, float speed, float seed, float threshold) {
    vec2 shiftedPixel = pixel + vec2(seed * 0.37, seed * 0.19);
    vec2 cell = floor(shiftedPixel / blockSizePx);
    float archetypeIndex = floor(hash21(cell + vec2(seed * 2.47, seed * 0.61)) * 10.0);
    float archetypeNormalized = (archetypeIndex + 0.5) / 10.0;
    float phaseOffset = hash21(cell + vec2(seed * 1.31, seed * 2.17));
    float timeline = TimeSeconds * speed + phaseOffset;
    float cycleIndex = floor(timeline);
    float cyclePhase = fract(timeline);
    float density = clamp(1.0 - threshold, 0.0, 1.0);
    int activeCycleCount = int(clamp(floor(density * 10.0 + 0.5), 0.0, 10.0));
    if (activeCycleCount <= 0) {
        return 0.0;
    }
    int cycleSlot = int(mod(cycleIndex + archetypeIndex, 10.0));
    float cycleMask = cycleSlot < activeCycleCount ? 1.0 : 0.0;
    float strength = mix(0.72, 1.0, archetypeNormalized);
    return patch_cycle_envelope(cyclePhase) * cycleMask * strength;
}

float chaotic_patch_spot_mask(vec2 uv) {
    vec2 pixel = floor(uv * TextureSizePx) + vec2(0.5);
    float minSize = max(min(PatchMinSizePx, PatchMaxSizePx), 1.0);
    float maxSize = max(max(PatchMinSizePx, PatchMaxSizePx), minSize);
    float midSize = mix(minSize, maxSize, 0.5);
    float frequency = max(PatchFrequency, 0.1);
    float threshold = 1.0 - clamp(PatchDensity, 0.0, 1.0);
    float layerA = PatchFlowVariant == 1
            ? patch_layer_activity_fixed_archetype(pixel, minSize, frequency * 1.18, 19.0, threshold)
            : patch_layer_activity_dynamic(pixel, minSize, frequency * 1.18, 19.0, threshold);
    float layerB = PatchFlowVariant == 1
            ? patch_layer_activity_fixed_archetype(pixel, midSize, frequency * 0.97, 43.0, threshold)
            : patch_layer_activity_dynamic(pixel, midSize, frequency * 0.97, 43.0, threshold);
    float layerC = PatchFlowVariant == 1
            ? patch_layer_activity_fixed_archetype(pixel, maxSize, frequency * 0.78, 71.0, threshold)
            : patch_layer_activity_dynamic(pixel, maxSize, frequency * 0.78, 71.0, threshold);
    return clamp(max(layerC, max(layerB, layerA)), 0.0, 1.0);
}

float pulse_electric_noise(float angle, float seed, float frequencyScale) {
    float a = angle * frequencyScale;
    float n1 = sin(a * 5.0 + seed * 1.17);
    float n2 = sin(a * 11.0 + seed * 2.31);
    float n3 = sin(a * 19.0 - seed * 0.73);
    return n1 * 0.50 + n2 * 0.30 + n3 * 0.20;
}

float pulse_electric_noise01(float angle, float seed, float frequencyScale) {
    return clamp(pulse_electric_noise(angle, seed, frequencyScale) * 0.5 + 0.5, 0.0, 1.0);
}

float pulse_periodic_electric_noise(float angle, float seed, float frequencyControl) {
    float safeControl = clamp(max(frequencyControl, 0.0), 0.0, 32.0);
    float harmonicScale = 1.0 + safeControl / 8.0;
    float lowScale = max(floor(harmonicScale), 1.0);
    float highScale = max(min(lowScale + 1.0, 5.0), lowScale);
    float blend = clamp(harmonicScale - lowScale, 0.0, 1.0);
    float lowNoise = pulse_electric_noise(angle, seed, lowScale);
    float highNoise = pulse_electric_noise(angle, seed, highScale);
    return mix(lowNoise, highNoise, blend);
}

float pulse_periodic_electric_noise01(float angle, float seed, float frequencyControl) {
    return clamp(pulse_periodic_electric_noise(angle, seed, frequencyControl) * 0.5 + 0.5, 0.0, 1.0);
}

float pulse_shape_stability(float radius, float distortionPx, float coreWidthPx, float glowRadiusPx) {
    float targetRadius = max(distortionPx * 6.0 + coreWidthPx * 4.0 + glowRadiusPx * 3.0, 2.0);
    return smoothstep(0.0, targetRadius, max(radius, 0.0));
}

bool plasma_cell_for_pixel(vec2 pixel, int count, out int index, out vec2 center, out vec2 cellSize) {
    int safeCount = max(count, 1);
    int columns = max(int(ceil(sqrt(float(safeCount)))), 1);
    int rows = max(int(ceil(float(safeCount) / float(columns))), 1);
    int fullRows = safeCount / columns;
    int remainder = safeCount - fullRows * columns;
    vec2 clampedPixel = clamp(pixel, vec2(0.0), vec2(TextureSizePx - 0.0001));

    cellSize = vec2(TextureSizePx / float(columns), TextureSizePx / float(rows));
    int row = clamp(int(floor(clampedPixel.y / cellSize.y)), 0, rows - 1);
    int rowColumns = columns;
    int columnOffset = 0;

    if (remainder > 0 && row == rows - 1 && row >= fullRows) {
        rowColumns = remainder;
        columnOffset = (columns - remainder) / 2;
    }

    float rowStartX = float(columnOffset) * cellSize.x;
    float localX = clampedPixel.x - rowStartX;
    if (localX < 0.0 || localX >= float(rowColumns) * cellSize.x) {
        index = -1;
        center = vec2(0.0);
        return false;
    }

    int columnInRow = clamp(int(floor(localX / cellSize.x)), 0, rowColumns - 1);
    int column = columnOffset + columnInRow;
    index = row < fullRows ? row * columns + column : fullRows * columns + columnInRow;
    center = vec2((float(column) + 0.5) * cellSize.x, (float(row) + 0.5) * cellSize.y);
    return true;
}

float plasma_blob_distance(vec2 delta, float radius, float seed) {
    vec2 quantized = floor(delta) + vec2(0.5);
    float pixelRadius = max(floor(radius + 0.5), 1.0);
    return length(quantized) - pixelRadius;
}

void plasma_row_meta(int count, int columns, int rows, int fullRows, int remainder, int row, out int rowColumns, out int columnOffset) {
    rowColumns = columns;
    columnOffset = 0;
    if (remainder > 0 && row == rows - 1 && row >= fullRows) {
        rowColumns = remainder;
        columnOffset = (columns - remainder) / 2;
    }
}

bool plasma_blob_masks_for_cell(vec2 pixel, int count, int columns, int rows, int fullRows, int remainder, vec2 cellSize, int row, int column, out vec2 masks) {
    masks = vec2(0.0);
    if (row < 0 || row >= rows || column < 0 || column >= columns) {
        return false;
    }

    int rowColumns;
    int columnOffset;
    plasma_row_meta(count, columns, rows, fullRows, remainder, row, rowColumns, columnOffset);
    if (column < columnOffset || column >= columnOffset + rowColumns) {
        return false;
    }

    int columnInRow = column - columnOffset;
    int index = row < fullRows ? row * columns + column : fullRows * columns + columnInRow;
    if (index < 0 || index >= count) {
        return false;
    }

    vec2 center = vec2((float(column) + 0.5) * cellSize.x, (float(row) + 0.5) * cellSize.y);
    float minRadius = min(PlasmaBlobMinSizePx, PlasmaBlobMaxSizePx);
    float maxRadius = max(PlasmaBlobMinSizePx, PlasmaBlobMaxSizePx);
    float edgeSoftness = max(PlasmaBlobEdgeSoftnessPx, 0.0001);
    float rimWidth = max(PlasmaBlobRimWidthPx, 0.0001);
    float seed = float(index) + 11.0;
    float safetyMargin = edgeSoftness + rimWidth + 1.0;
    float maxAllowedRadius = max(min(cellSize.x, cellSize.y) * 0.5 - safetyMargin, 0.5);
    float radius = clamp(mix(minRadius, maxRadius, hash11(seed + 71.0)), 0.5, maxAllowedRadius);
    vec2 driftLimits = vec2(
        max(cellSize.x * 0.5 - radius - safetyMargin, 0.0),
        max(cellSize.y * 0.5 - radius - safetyMargin, 0.0)
    ) * max(PlasmaBlobDriftRangeScale, 0.0);
    float driftPhase = TimeSeconds * max(PlasmaBlobDriftSpeedPxPerSecond, 0.0) * 0.12;
    center += vec2(
        sin(driftPhase + seed * 1.37),
        cos(driftPhase * 0.91 + seed * 2.11)
    ) * driftLimits;

    vec2 delta = pixel - center;
    float distortedDistance = plasma_blob_distance(delta, radius, hash11(seed + 97.0));
    float blob = 1.0 - smoothstep(0.0, edgeSoftness, distortedDistance);
    float rimDistance = abs(distortedDistance);
    float rim = 1.0 - smoothstep(rimWidth, rimWidth + edgeSoftness, rimDistance);
    masks = vec2(
        clamp(blob, 0.0, 1.0),
        clamp(rim * (1.0 - blob * 0.35), 0.0, 1.0)
    );
    return true;
}

vec2 plasma_blob_masks(vec2 uv) {
    vec2 pixel = uv * TextureSizePx;
    float blobMask = 0.0;
    float rimMask = 0.0;
    int count = max(PlasmaBlobCount, 0);
    if (count <= 0) {
        return vec2(0.0);
    }

    int safeCount = max(count, 1);
    int columns = max(int(ceil(sqrt(float(safeCount)))), 1);
    int rows = max(int(ceil(float(safeCount) / float(columns))), 1);
    int fullRows = safeCount / columns;
    int remainder = safeCount - fullRows * columns;
    vec2 cellSize = vec2(TextureSizePx / float(columns), TextureSizePx / float(rows));
    vec2 clampedPixel = clamp(pixel, vec2(0.0), vec2(TextureSizePx - 0.0001));
    int baseRow = clamp(int(floor(clampedPixel.y / cellSize.y)), 0, rows - 1);
    int baseColumn = clamp(int(floor(clampedPixel.x / cellSize.x)), 0, columns - 1);

    for (int rowOffset = -2; rowOffset <= 2; rowOffset++) {
        int candidateRow = baseRow + rowOffset;
        if (candidateRow < 0 || candidateRow >= rows) {
            continue;
        }
        for (int columnOffset = -2; columnOffset <= 2; columnOffset++) {
            int candidateColumn = baseColumn + columnOffset;
            vec2 candidateMasks;
            if (!plasma_blob_masks_for_cell(pixel, count, columns, rows, fullRows, remainder, cellSize, candidateRow, candidateColumn, candidateMasks)) {
                continue;
            }
            blobMask = max(blobMask, candidateMasks.x);
            rimMask = max(rimMask, candidateMasks.y);
        }
    }

    return vec2(clamp(blobMask, 0.0, 1.0), clamp(rimMask, 0.0, 1.0));
}

void simple_ring_profile(
    vec2 pixel,
    vec2 center,
    float radius,
    float fade,
    float screenFootprint,
    out float signedDistance,
    out float effectiveCoreHalfWidth,
    out float effectiveGlowLimit,
    out float effectiveCoreSoftness,
    out float effectiveGlowSoftness,
    out float brightnessFactor
) {
    float authoredCoreHalfWidth = max(PulseCoreWidthPx, 0.0001) * 0.5;
    float authoredGlowLimit = authoredCoreHalfWidth + max(PulseGlowRadiusPx, 0.0);
    float authoredCoreSoftness = max(PulseCoreSoftnessPx, 0.0001);
    float authoredGlowSoftness = max(PulseGlowSoftnessPx, 0.0001);
    signedDistance = length(pixel - center) - radius;
    // Keep a small screen-space floor so thin rings survive minification,
    // but do not let it dominate authored widths in the 0.01..1.0 range.
    float minVisibleCoreHalfWidth = screenFootprint * 0.18;
    float minVisibleGlowLimit = screenFootprint * 0.35;
    effectiveCoreHalfWidth = max(authoredCoreHalfWidth, minVisibleCoreHalfWidth);
    effectiveGlowLimit = max(authoredGlowLimit, effectiveCoreHalfWidth + minVisibleGlowLimit);
    effectiveCoreSoftness = max(authoredCoreSoftness, screenFootprint * 0.25);
    effectiveGlowSoftness = max(authoredGlowSoftness, screenFootprint * 0.4);
    brightnessFactor = fade * max(PulseBrightness, 0.0);
}

vec2 simple_ring_pulse_components(vec2 pixel, vec2 center, float radius, float fade, float screenFootprint) {
    float signedDistance;
    float effectiveCoreHalfWidth;
    float effectiveGlowLimit;
    float effectiveCoreSoftness;
    float effectiveGlowSoftness;
    float brightnessFactor;
    simple_ring_profile(
        pixel,
        center,
        radius,
        fade,
        screenFootprint,
        signedDistance,
        effectiveCoreHalfWidth,
        effectiveGlowLimit,
        effectiveCoreSoftness,
        effectiveGlowSoftness,
        brightnessFactor
    );
    float ringDistance = abs(signedDistance);
    float core = 1.0 - smoothstep(effectiveCoreHalfWidth, effectiveCoreHalfWidth + effectiveCoreSoftness, ringDistance);
    float glow = 1.0 - smoothstep(effectiveGlowLimit, effectiveGlowLimit + effectiveGlowSoftness, ringDistance);
    float coreContribution = core * clamp(PulseCoreAlpha, 0.0, 1.0) * brightnessFactor;
    float glowContribution = glow * clamp(PulseGlowAlpha, 0.0, 1.0) * brightnessFactor;
    return vec2(coreContribution, glowContribution);
}

void crysis_electric_ring_profile(
    vec2 pixel,
    vec2 center,
    float radius,
    float fade,
    float angle,
    float centerSeed,
    float screenFootprint,
    out float signedDistance,
    out float effectiveCoreHalfWidth,
    out float effectiveGlowLimit,
    out float effectiveCoreSoftness,
    out float effectiveGlowSoftness,
    out float brightnessFactor
) {
    float authoredCoreHalfWidth = max(PulseCoreWidthPx, 0.0001) * 0.5;
    float authoredGlowLimit = authoredCoreHalfWidth + max(PulseGlowRadiusPx, 0.0);
    float authoredCoreSoftness = max(PulseCoreSoftnessPx, 0.0001);
    float authoredGlowSoftness = max(PulseGlowSoftnessPx, 0.0001);
    float ringRotation = fract(sin(centerSeed) * 43758.5453) * TAU;
    float profileAngle = angle + ringRotation * clamp(PulsePatternRotationRandomness, 0.0, 1.0);
    float frequencyControl = max(PulseRingDistortionFrequency, 0.0);
    float radiusNoise = pulse_periodic_electric_noise(profileAngle, centerSeed, frequencyControl);
    float widthNoise = pulse_periodic_electric_noise01(profileAngle + 1.7, centerSeed + 3.1, frequencyControl);
    float brightnessNoise = pulse_periodic_electric_noise01(profileAngle + 3.1, centerSeed + 5.7, frequencyControl);
    float edgeNoise = pulse_periodic_electric_noise01(profileAngle + 5.3, centerSeed + 7.9, frequencyControl);
    float shapeStability = pulse_shape_stability(radius, max(PulseRingDistortionPx, 0.0), PulseCoreWidthPx, PulseGlowRadiusPx);
    float distortionStrength = mix(0.10, 1.0, shapeStability);
    float widthVariationStrength = clamp(PulseWidthVariation, 0.0, 1.0) * mix(0.25, 1.0, shapeStability);
    float brightnessVariationStrength = clamp(PulseBrightnessVariation, 0.0, 1.0) * mix(0.55, 1.0, shapeStability);
    float energyVariationStrength = clamp(PulseRingEnergyVariation, 0.0, 1.0) * mix(0.60, 1.0, shapeStability);
    float roughness = clamp(PulseEdgeRoughness, 0.0, 1.0) * mix(0.20, 1.0, shapeStability);

    float distortion = radiusNoise * max(PulseRingDistortionPx, 0.0) * distortionStrength;
    float distortedRadius = radius + distortion;
    signedDistance = length(pixel - center) - distortedRadius;
    float localWidthMul = mix(
        1.0 - widthVariationStrength * 0.45,
        1.0 + widthVariationStrength * 0.85,
        widthNoise
    );
    localWidthMul = max(localWidthMul, 0.55);
    float localBrightness = mix(
        1.0 - brightnessVariationStrength,
        1.0 + brightnessVariationStrength,
        brightnessNoise
    );
    localBrightness = max(localBrightness, 0.35);
    float energyNoise = pulse_periodic_electric_noise01(profileAngle + 7.1, centerSeed + 11.3, frequencyControl);
    float localEnergy = mix(
        1.0 - energyVariationStrength * 0.45,
        1.0 + energyVariationStrength * 0.55,
        energyNoise
    );
    localEnergy = max(localEnergy, 0.55);
    float minVisibleCoreHalfWidth = screenFootprint * 0.18;
    float minVisibleGlowLimit = screenFootprint * 0.35;
    float authoredCoreHalfWidthLocal = authoredCoreHalfWidth * localWidthMul;
    float authoredGlowLimitLocal = authoredGlowLimit * localWidthMul;
    effectiveCoreHalfWidth = max(authoredCoreHalfWidthLocal, minVisibleCoreHalfWidth);
    effectiveGlowLimit = max(authoredGlowLimitLocal, effectiveCoreHalfWidth + minVisibleGlowLimit);
    effectiveCoreSoftness = max(authoredCoreSoftness * mix(0.75, 1.5, edgeNoise * roughness + (1.0 - roughness) * 0.5), screenFootprint * 0.25);
    effectiveGlowSoftness = max(authoredGlowSoftness * mix(0.8, 1.8, edgeNoise * roughness + (1.0 - roughness) * 0.5), screenFootprint * 0.4);
    brightnessFactor = fade * max(PulseBrightness, 0.0) * localBrightness * localEnergy;
}

vec2 crysis_electric_ring_pulse_components(vec2 pixel, vec2 center, float radius, float fade, float angle, float centerSeed, float screenFootprint) {
    float signedDistance;
    float effectiveCoreHalfWidth;
    float effectiveGlowLimit;
    float effectiveCoreSoftness;
    float effectiveGlowSoftness;
    float brightnessFactor;
    crysis_electric_ring_profile(
        pixel,
        center,
        radius,
        fade,
        angle,
        centerSeed,
        screenFootprint,
        signedDistance,
        effectiveCoreHalfWidth,
        effectiveGlowLimit,
        effectiveCoreSoftness,
        effectiveGlowSoftness,
        brightnessFactor
    );
    float ringDistance = abs(signedDistance);
    float core = 1.0 - smoothstep(effectiveCoreHalfWidth, effectiveCoreHalfWidth + effectiveCoreSoftness, ringDistance);
    float glow = 1.0 - smoothstep(effectiveGlowLimit, effectiveGlowLimit + effectiveGlowSoftness, ringDistance);
    float coreContribution = core * clamp(PulseCoreAlpha, 0.0, 1.0) * brightnessFactor;
    float glowContribution = glow * clamp(PulseGlowAlpha, 0.0, 1.0) * brightnessFactor;
    return vec2(coreContribution, glowContribution);
}

vec4 ring_pulse_components(vec2 uv) {
    if (PulseEnabled == 0 || PulseCenterCount <= 0) {
        return vec4(0.0);
    }

    float intervalSeconds = max(PulseIntervalSeconds, 0.0001);
    float durationSeconds = max(PulseDurationSeconds, 0.0001);
    vec4 total = vec4(0.0);
    float baseGeneration = floor(TimeSeconds / intervalSeconds);
    vec2 pixel = uv * TextureSizePx;
    int centerCount = min(max(PulseCenterCount, 0), MAX_RING_PULSE_CENTERS);

    for (int waveIndex = 0; waveIndex < MAX_PLASMA_OVERLAPPING_WAVES; waveIndex++) {
        float generation = baseGeneration - float(waveIndex);
        float triggerTime = generation * intervalSeconds;
        float age = TimeSeconds - triggerTime;
        if (age < 0.0 || age > durationSeconds) {
            continue;
        }

        float progress = clamp(age / durationSeconds, 0.0, 1.0);
        float fade = pow(max(1.0 - progress, 0.0), max(PulseFadePower, 0.0001));
        float radius = mix(PulseStartRadiusPx, PulseEndRadiusPx, progress);

        if (PulseRingMergingEnabled != 0) {
            float unionSignedDistance = 1000000.0;
            float unionCoreHalfWidth = 0.0;
            float unionGlowLimit = 0.0;
            float unionCoreSoftness = 0.0;
            float unionGlowSoftness = 0.0;
            float unionBrightnessFactor = 0.0;
            bool foundUnion = false;

            for (int centerIndex = 0; centerIndex < MAX_RING_PULSE_CENTERS; centerIndex++) {
                if (centerIndex >= centerCount) {
                    break;
                }

                vec2 center = vec2(PulseCenterX[centerIndex], PulseCenterY[centerIndex]);
                vec2 delta = pixel - center;
                float deltaLength = length(delta);
                float angle = atan(delta.y, delta.x);
                float centerSeed = float(centerIndex) * 12.9898 + float(waveIndex) * 4.141;
                float baseRingDistance = abs(deltaLength - radius);
                float screenFootprint = max(fwidth(baseRingDistance), 0.0001);
                float signedDistance;
                float coreHalfWidth;
                float glowLimit;
                float coreSoftness;
                float glowSoftness;
                float brightnessFactor;

                if (PulseVisualMode == 0) {
                    simple_ring_profile(
                        pixel,
                        center,
                        radius,
                        fade,
                        screenFootprint,
                        signedDistance,
                        coreHalfWidth,
                        glowLimit,
                        coreSoftness,
                        glowSoftness,
                        brightnessFactor
                    );
                } else {
                    crysis_electric_ring_profile(
                        pixel,
                        center,
                        radius,
                        fade,
                        angle,
                        centerSeed,
                        screenFootprint,
                        signedDistance,
                        coreHalfWidth,
                        glowLimit,
                        coreSoftness,
                        glowSoftness,
                        brightnessFactor
                    );
                }

                if (!foundUnion || signedDistance < unionSignedDistance) {
                    foundUnion = true;
                    unionSignedDistance = signedDistance;
                    unionCoreHalfWidth = coreHalfWidth;
                    unionGlowLimit = glowLimit;
                    unionCoreSoftness = coreSoftness;
                    unionGlowSoftness = glowSoftness;
                    unionBrightnessFactor = brightnessFactor;
                }
            }

            if (foundUnion) {
                float ringDistance = abs(unionSignedDistance);
                float core = 1.0 - smoothstep(unionCoreHalfWidth, unionCoreHalfWidth + unionCoreSoftness, ringDistance);
                float glow = 1.0 - smoothstep(unionGlowLimit, unionGlowLimit + unionGlowSoftness, ringDistance);
                total.xy = max(total.xy, vec2(
                    core * clamp(PulseCoreAlpha, 0.0, 1.0) * unionBrightnessFactor,
                    glow * clamp(PulseGlowAlpha, 0.0, 1.0) * unionBrightnessFactor
                ));
            }
            continue;
        }

        for (int centerIndex = 0; centerIndex < MAX_RING_PULSE_CENTERS; centerIndex++) {
            if (centerIndex >= centerCount) {
                break;
            }

            vec2 center = vec2(PulseCenterX[centerIndex], PulseCenterY[centerIndex]);
            vec2 delta = pixel - center;
            float angle = atan(delta.y, delta.x);
            float centerSeed = float(centerIndex) * 12.9898 + float(waveIndex) * 4.141;
            float baseRingDistance = abs(length(delta) - radius);
            float screenFootprint = max(fwidth(baseRingDistance), 0.0001);
            vec2 contributions = PulseVisualMode == 0
                ? simple_ring_pulse_components(pixel, center, radius, fade, screenFootprint)
                : crysis_electric_ring_pulse_components(pixel, center, radius, fade, angle, centerSeed, screenFootprint);
            total.xy = max(total.xy, contributions.xy);
        }
    }

    total.xy = clamp(total.xy, vec2(0.0), vec2(1.0));
    return total;
}

vec4 recolor_classic_effect(vec4 color) {
    if (ClassicModeTint != 0) {
        float brightness = max(color.r, max(color.g, color.b));
        color.rgb = brightness * (ClassicModeTint == 1 ? vec3(1.0, 0.0, 0.0) : vec3(1.0, 1.0, 0.0));
    }
    return color;
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    float textureAlphaMask = color.a > 0.0 ? 1.0 : 0.0;
    if (color.a < 0.1 && PatternMode != 5 && PulseDebugOutputOnlyPulse == 0) {
        discard;
    }

    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= sanitized_lightmap_color(lightMapColor);
    color.rgb *= clamp(OverlayBrightness, 0.0, 4.0);

    float transitionMask = transition_pixel_mask(texCoord0);
    float baseAlpha = color.a * clamp(OverlayAlpha, 0.0, 1.0) * transitionMask;
    if (PatternMode == 1) {
        float shimmerMask = x_mask(texCoord0);
        float xStrength = shimmerMask * clamp(XAlpha, 0.0, 1.0);
        float pixelX = texCoord0.x * TextureSizePx + XDirectionX * TimeSeconds * XSpeedPxPerSecond;
        float pixelY = texCoord0.y * TextureSizePx + XDirectionY * TimeSeconds * XSpeedPxPerSecond;
        float spacing = max(XSpacingPx, 1.0);
        float cellIndexX = floor(pixelX / spacing);
        float cellIndexY = floor(pixelY / spacing);
        float mixedModeSelector = mod(cellIndexX + cellIndexY, 2.0);

        if (XAsCutout != 0) {
            color.a = baseAlpha * (1.0 - xStrength);
        } else {
            if (mixedModeSelector < 1.0) {
                vec3 highlightColor = vec3(0.75, 0.95, 1.0);
                color.rgb = mix(color.rgb, highlightColor, xStrength * 0.5) + color.rgb * (xStrength * 0.35);
                color.a = clamp(baseAlpha + baseAlpha * xStrength, 0.0, 1.0);
            } else {
                color.a = baseAlpha * (1.0 - xStrength);
            }
        }
    } else if (PatternMode == 2 || PatternMode == 3) {
        bool pixelLocked = PatternMode == 3;
        float borderMask = hex_border_mask(texCoord0);
        if (pixelLocked) {
            borderMask = hex_border_mask_pixel_locked(texCoord0);
        }
        float alphaFactor = mix(clamp(HexInnerAlpha, 0.0, 1.0), clamp(HexGlowAlpha, 0.0, 1.0), borderMask);
        color.a = baseAlpha * alphaFactor;
        vec3 hexGlowColor = vec3(0.55, 0.90, 1.0);
        color.rgb += borderMask * clamp(HexGlowBoost, 0.0, 2.0) * (color.rgb * 0.35 + hexGlowColor * 0.25);
    } else if (PatternMode == 4) {
        float breathing = (0.5 + 0.5 * sin(TimeSeconds * max(SheenBreathingSpeed, 0.0) * TAU)) * clamp(SheenBreathingAmount, 0.0, 1.0);
        float sheen = universal_sheen_mask(texCoord0) * max(SheenAlpha, 0.0);
        float sheenTransparency = universal_sheen_mask(texCoord0) * clamp(SheenTransparency, 0.0, 1.0);
        float fiber = universal_fiber_flow(texCoord0);
        float totalBoost = breathing + fiber;
        color.a = clamp(baseAlpha + baseAlpha * totalBoost, 0.0, 1.0);
        color.a *= (1.0 - sheenTransparency);
        vec3 sheenColor = vec3(0.65, 0.95, 1.0);
        color.rgb = mix(color.rgb, sheenColor, clamp(sheen * max(SheenColorBoost, 0.0), 0.0, 1.0));
        color.rgb += color.rgb * clamp((breathing * 0.35) + fiber + sheen * 0.15, 0.0, 1.0);
    } else if (PatternMode == 5) {
        vec2 blobMasks = plasma_blob_masks(texCoord0);
        float blobMask = blobMasks.x;
        float blobRim = blobMasks.y;
        float alphaFactor = mix(1.0, clamp(PlasmaBlobInnerAlpha, 0.0, 1.0), blobMask);
        float rimContribution = blobRim * clamp(PlasmaBlobRimAlpha, 0.0, 1.0) * max(PlasmaBlobRimBrightness, 0.0);
        float pulseTransitionMask = TransitionAffectsPulses != 0 ? transitionMask : 1.0;
        vec4 pulseComponents = ring_pulse_components(texCoord0) * textureAlphaMask * pulseTransitionMask;
        float pulseCoreContribution = pulseComponents.x;
        float pulseGlowContribution = pulseComponents.y;
        float pulseContribution = pulseCoreContribution + pulseGlowContribution;
        float mergeContribution = 0.0;
        if (PulseDebugOutputOnlyPulse != 0) {
            vec3 pulseCoreColor = vec3(0.85, 0.98, 1.0);
            vec3 pulseGlowColor = vec3(0.35, 0.85, 1.0);
            vec3 pulseMergeColor = vec3(0.50, 0.90, 1.0);
            color.rgb = pulseCoreColor * pulseCoreContribution + pulseGlowColor * pulseGlowContribution + pulseMergeColor * mergeContribution;
            color.a = clamp(pulseContribution + mergeContribution, 0.0, 1.0);
            if (color.a < 0.0039215686) {
                discard;
            }
            fragColor = linear_fog(recolor_classic_effect(color), vertexDistance, FogStart, FogEnd, FogColor);
            return;
        }

        float pulseAlphaContribution = (pulseContribution + mergeContribution) * clamp(OverlayAlpha, 0.0, 1.0);
        color.a = baseAlpha * alphaFactor;
        color.a = clamp(color.a + rimContribution * baseAlpha + pulseAlphaContribution, 0.0, 1.0);
        vec3 rimColor = vec3(0.75, 0.95, 1.0);
        vec3 pulseCoreColor = vec3(0.85, 0.98, 1.0);
        vec3 pulseGlowColor = vec3(0.35, 0.85, 1.0);
        vec3 pulseMergeColor = vec3(0.50, 0.90, 1.0);
        color.rgb = mix(color.rgb, rimColor, clamp(rimContribution, 0.0, 1.0));
        color.rgb = clamp(color.rgb + pulseCoreColor * pulseCoreContribution + pulseGlowColor * pulseGlowContribution + pulseMergeColor * mergeContribution, 0.0, 1.5);
    } else if (PatternMode == 6) {
        float patchTextureAlpha = clamp(PatchTextureAlpha, 0.0, 1.0);
        float patchSpotAlpha = clamp(PatchSpotAlpha, 0.0, 1.0);
        float spotMask = chaotic_patch_spot_mask(texCoord0);
        float pulseTransitionMask = TransitionAffectsPulses != 0 ? transitionMask : 1.0;
        vec4 pulseComponents = ring_pulse_components(texCoord0) * textureAlphaMask * pulseTransitionMask;
        float pulseCoreContribution = pulseComponents.x;
        float pulseGlowContribution = pulseComponents.y;
        float pulseContribution = pulseCoreContribution + pulseGlowContribution;
        if (PulseDebugOutputOnlyPulse != 0) {
            vec3 pulseCoreColor = vec3(0.85, 0.98, 1.0);
            vec3 pulseGlowColor = vec3(0.35, 0.85, 1.0);
            color.rgb = pulseCoreColor * pulseCoreContribution + pulseGlowColor * pulseGlowContribution;
            color.a = clamp(pulseContribution, 0.0, 1.0);
            if (color.a < 0.0039215686) {
                discard;
            }
            fragColor = linear_fog(recolor_classic_effect(color), vertexDistance, FogStart, FogEnd, FogColor);
            return;
        }

        color.a = baseAlpha * patchTextureAlpha * (1.0 - spotMask * patchSpotAlpha);
        color.a = clamp(color.a + pulseContribution * clamp(OverlayAlpha, 0.0, 1.0), 0.0, 1.0);
        vec3 pulseCoreColor = vec3(0.85, 0.98, 1.0);
        vec3 pulseGlowColor = vec3(0.35, 0.85, 1.0);
        color.rgb = clamp(color.rgb + pulseCoreColor * pulseCoreContribution + pulseGlowColor * pulseGlowContribution, 0.0, 1.5);
    } else {
        color.a = baseAlpha;
    }

    if (color.a < 0.0039215686) {
        discard;
    }

    fragColor = linear_fog(recolor_classic_effect(color), vertexDistance, FogStart, FogEnd, FogColor);
}
