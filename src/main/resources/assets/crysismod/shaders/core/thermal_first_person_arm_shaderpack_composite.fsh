#version 150

uniform sampler2D MaskSampler;
uniform sampler2D ArmDepthSampler;
uniform sampler2D SceneDepthSampler;
uniform vec2 InSize;
uniform float ActivationFlash;
uniform int ArmOcclusionEnabled;
uniform vec4 CanonicalDepthProjection;
uniform vec4 ExternalDepthProjection;
uniform int ThermalQuality;
uniform float ArmProjectionScale;
uniform int ThermalPresetMode;
uniform float GeometryDepthStrength;
uniform float ImprovedSilhouetteWidth;
uniform float ImprovedRimCurve;
uniform float ImprovedCoreHeat;
uniform float ImprovedFacingStrength;
uniform float ImprovedSmoothNormalStrength;
uniform float ImprovedReliefScale;
uniform float ImprovedReliefThreshold;
uniform float ImprovedProtrusionCooling;
uniform float ImprovedRecessCooling;
uniform float ImprovedReliefSmoothing;
uniform float ImprovedV2HotPeakArea;
uniform float ThermalModelOpacity;
uniform float InnerStrength;
uniform float InnerRadiusRatio;
uniform float OuterGlowRatio;
uniform float OuterGlowStrength;
uniform float HeatPower;
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

in vec2 texCoord0;

out vec4 fragColor;

const float PI = 3.14159265358979323846;
const float INV_TWO_PI = 0.15915494309189535;

float fastSin(float angle) {
    float wrapped = fract(angle * INV_TWO_PI + 0.5) * (2.0 * PI) - PI;
    float approximation = 1.27323954473516 * wrapped
            - 0.405284734569351 * wrapped * abs(wrapped);
    return approximation + 0.225 * (approximation * abs(approximation) - approximation);
}

float maskAt(vec2 offsetPx) {
    return texture(MaskSampler, texCoord0 + offsetPx / InSize).a;
}

float unpackDepth(vec4 packedDepth) {
    vec3 encoded = floor(packedDepth.rgb * 255.0 + 0.5);
    return dot(encoded, vec3(65536.0, 256.0, 1.0)) / 16777215.0;
}

float reprojectCanonicalDepth(float canonicalDepth) {
    float canonicalNdc = canonicalDepth * 2.0 - 1.0;
    float solveDenominator = canonicalNdc * CanonicalDepthProjection.z
            - CanonicalDepthProjection.x;
    if (abs(solveDenominator) < 0.0000001) {
        return canonicalDepth;
    }
    float viewZ = (CanonicalDepthProjection.y
            - canonicalNdc * CanonicalDepthProjection.w) / solveDenominator;
    float externalW = ExternalDepthProjection.z * viewZ
            + ExternalDepthProjection.w;
    if (abs(externalW) < 0.0000001) {
        return canonicalDepth;
    }
    float externalNdc = (ExternalDepthProjection.x * viewZ
            + ExternalDepthProjection.y) / externalW;
    return clamp(externalNdc * 0.5 + 0.5, 0.0, 1.0);
}

bool heldItemCoversArm(vec2 sampleUv, float armMask) {
    if (ArmOcclusionEnabled == 0 || armMask < 0.01) {
        return false;
    }
    float canonicalArmDepth = texture(ArmDepthSampler, sampleUv).r;
    if (canonicalArmDepth >= 0.999999) {
        return false;
    }
    float externalArmDepth = reprojectCanonicalDepth(canonicalArmDepth);
    float resolvedSceneDepth = unpackDepth(texture(SceneDepthSampler, sampleUv));
    return resolvedSceneDepth + 0.00005 < externalArmDepth;
}

bool resolvedDepthCoversArmOffset(float resolvedSceneDepth, vec2 offsetPx) {
    vec2 armUv = texCoord0 + offsetPx / InSize;
    if (texture(MaskSampler, armUv).a < 0.01) {
        return false;
    }
    float canonicalArmDepth = texture(ArmDepthSampler, armUv).r;
    if (canonicalArmDepth >= 0.999999) {
        return false;
    }
    return resolvedSceneDepth + 0.00005 < reprojectCanonicalDepth(canonicalArmDepth);
}

bool heldItemCoversArmGlow(float radiusPx) {
    if (ArmOcclusionEnabled == 0) {
        return false;
    }
    float resolvedSceneDepth = unpackDepth(texture(SceneDepthSampler, texCoord0));
    if (resolvedSceneDepth >= 0.999999) {
        return false;
    }

    const float DIAGONAL = 0.70710678;
    float nearRadius = max(2.0, radiusPx * 0.45);
    for (int ringIndex = 0; ringIndex < 2; ringIndex++) {
        float radius = ringIndex == 0 ? nearRadius : radiusPx;
        if (resolvedDepthCoversArmOffset(resolvedSceneDepth, vec2( radius, 0.0))) return true;
        if (resolvedDepthCoversArmOffset(resolvedSceneDepth, vec2(-radius, 0.0))) return true;
        if (resolvedDepthCoversArmOffset(resolvedSceneDepth, vec2(0.0,  radius))) return true;
        if (resolvedDepthCoversArmOffset(resolvedSceneDepth, vec2(0.0, -radius))) return true;
        if (resolvedDepthCoversArmOffset(resolvedSceneDepth, vec2( radius * DIAGONAL,  radius * DIAGONAL))) return true;
        if (resolvedDepthCoversArmOffset(resolvedSceneDepth, vec2(-radius * DIAGONAL,  radius * DIAGONAL))) return true;
        if (resolvedDepthCoversArmOffset(resolvedSceneDepth, vec2( radius * DIAGONAL, -radius * DIAGONAL))) return true;
        if (resolvedDepthCoversArmOffset(resolvedSceneDepth, vec2(-radius * DIAGONAL, -radius * DIAGONAL))) return true;
    }
    return false;
}
float sameArmAt(vec2 offsetPx) {
    return step(0.5, maskAt(offsetPx));
}

float depthAt(vec2 offsetPx) {
    return texture(ArmDepthSampler, texCoord0 + offsetPx / InSize).r;
}

float bridgedArmAt(vec2 direction, float radiusPx, float seamBridgePx) {
    float direct = sameArmAt(direction * radiusPx);
    if (direct >= 0.5 || seamBridgePx <= 0.0) {
        return direct;
    }
    return sameArmAt(direction * (radiusPx + seamBridgePx));
}

vec2 armFieldDirection(int index) {
    if (index == 0) return vec2(1.0, 0.0);
    if (index == 1) return vec2(0.86602540, 0.5);
    if (index == 2) return vec2(0.5, 0.86602540);
    if (index == 3) return vec2(0.0, 1.0);
    if (index == 4) return vec2(-0.5, 0.86602540);
    return vec2(-0.86602540, 0.5);
}

float ringCoverage(float radiusPx) {
    const float DIAGONAL = 0.70710678;
    float coverage = 0.0;
    coverage = max(coverage, maskAt(vec2( radiusPx, 0.0)));
    coverage = max(coverage, maskAt(vec2(-radiusPx, 0.0)));
    coverage = max(coverage, maskAt(vec2(0.0,  radiusPx)));
    coverage = max(coverage, maskAt(vec2(0.0, -radiusPx)));
    coverage = max(coverage, maskAt(vec2( radiusPx * DIAGONAL,  radiusPx * DIAGONAL)));
    coverage = max(coverage, maskAt(vec2(-radiusPx * DIAGONAL,  radiusPx * DIAGONAL)));
    coverage = max(coverage, maskAt(vec2( radiusPx * DIAGONAL, -radiusPx * DIAGONAL)));
    return max(coverage, maskAt(vec2(-radiusPx * DIAGONAL, -radiusPx * DIAGONAL)));
}

float edgeExposure(float radiusPx) {
    const float DIAGONAL = 0.70710678;
    float coverage = 0.0;
    coverage += maskAt(vec2( radiusPx, 0.0));
    coverage += maskAt(vec2(-radiusPx, 0.0));
    coverage += maskAt(vec2(0.0,  radiusPx));
    coverage += maskAt(vec2(0.0, -radiusPx));
    coverage += maskAt(vec2( radiusPx * DIAGONAL,  radiusPx * DIAGONAL));
    coverage += maskAt(vec2(-radiusPx * DIAGONAL,  radiusPx * DIAGONAL));
    coverage += maskAt(vec2( radiusPx * DIAGONAL, -radiusPx * DIAGONAL));
    coverage += maskAt(vec2(-radiusPx * DIAGONAL, -radiusPx * DIAGONAL));
    return clamp(1.0 - coverage * 0.125, 0.0, 1.0);
}

vec2 armDirection(int index) {
    if (index == 0) return vec2(1.0, 0.0);
    if (index == 1) return vec2(0.0, 1.0);
    if (index == 2) return vec2(0.70710678, 0.70710678);
    return vec2(0.70710678, -0.70710678);
}

vec2 armBoundaryDistance(vec2 direction, float maxRadius) {
    float previousRadius = 0.0;
    for (int stepIndex = 0; stepIndex < 5; stepIndex++) {
        float stepT = float(stepIndex) * 0.25;
        float radius = mix(1.0, maxRadius, stepT * stepT);
        if (maskAt(direction * radius) < 0.5) {
            float lower = previousRadius;
            float upper = radius;
            float midpoint = (lower + upper) * 0.5;
            if (maskAt(direction * midpoint) >= 0.5) {
                lower = midpoint;
            } else {
                upper = midpoint;
            }
            return vec2((lower + upper) * 0.5, 1.0);
        }
        previousRadius = radius;
    }
    return vec2(maxRadius, 0.0);
}

float localArmDepth(float maxRadius) {
    float closestBoundary = maxRadius;
    float normalizedDepth = 1.0;
    for (int pairIndex = 0; pairIndex < 4; pairIndex++) {
        vec2 direction = armDirection(pairIndex);
        vec2 positive = armBoundaryDistance(direction, maxRadius);
        vec2 negative = armBoundaryDistance(-direction, maxRadius);
        float nearDistance = min(positive.x, negative.x);
        float farDistance = max(positive.x, negative.x);
        if (max(positive.y, negative.y) > 0.5 && nearDistance < closestBoundary) {
            closestBoundary = nearDistance;
            normalizedDepth = clamp(
                    (2.0 * nearDistance) / max(nearDistance + farDistance, 0.0001),
                    0.0,
                    1.0
            );
        }
    }
    return normalizedDepth;
}

float diffusedArmSilhouetteProgress(float maxRadius) {
    int pairLimit = ThermalQuality <= 1 ? 3 : (ThermalQuality == 2 ? 4 : 6);
    int ringLimit = ThermalQuality <= 1 ? 3 : (ThermalQuality == 2 ? 4 : 5);
    float seamBridgePx = clamp(
            1.25 * sqrt(clamp(ArmProjectionScale, 0.45, 3.5)),
            1.25,
            3.5
    );
    float accumulated = 0.0;
    float weightSum = 0.0;

    for (int ringIndex = 0; ringIndex < 5; ringIndex++) {
        if (ringIndex >= ringLimit) break;
        float ringT = float(ringIndex + 1) / float(ringLimit);
        float radius = max(1.0, maxRadius * pow(ringT, 1.35));
        float coverage = 0.0;
        for (int pairIndex = 0; pairIndex < 6; pairIndex++) {
            if (pairIndex >= pairLimit) break;
            vec2 direction = armFieldDirection(pairIndex);
            coverage += bridgedArmAt(
                    direction, radius, seamBridgePx);
            coverage += bridgedArmAt(
                    -direction, radius, seamBridgePx);
        }
        coverage /= max(float(pairLimit * 2), 1.0);
        float containment = smoothstep(0.34, 0.96, coverage);
        float ringWeight = mix(1.16, 0.80, ringT);
        accumulated += containment * ringWeight;
        weightSum += ringWeight;
    }

    return smoothstep(
            0.0,
            1.0,
            clamp(accumulated / max(weightSum, 0.0001), 0.0, 1.0)
    );
}

float coherentArmInteriorPeak(float projectedArmSizePx) {
    int pairLimit = ThermalQuality <= 1 ? 3 : (ThermalQuality == 2 ? 4 : 6);
    float seamBridgePx = clamp(
            1.25 * sqrt(clamp(ArmProjectionScale, 0.45, 3.5)),
            1.25,
            3.5
    );
    float configuredArea = clamp(ImprovedV2HotPeakArea, 0.0, 1.0);
    float containmentScale = mix(1.50, 0.50, configuredArea);
    float innerRadius = max(projectedArmSizePx * 0.055 * containmentScale, 1.0);
    float outerRadius = max(projectedArmSizePx * 0.120 * containmentScale, 1.5);
    float innerCoverage = 0.0;
    float outerCoverage = 0.0;

    for (int pairIndex = 0; pairIndex < 6; pairIndex++) {
        if (pairIndex >= pairLimit) break;
        vec2 direction = armFieldDirection(pairIndex);
        innerCoverage += bridgedArmAt(
                direction, innerRadius, seamBridgePx);
        innerCoverage += bridgedArmAt(
                -direction, innerRadius, seamBridgePx);
        outerCoverage += bridgedArmAt(
                direction, outerRadius, seamBridgePx);
        outerCoverage += bridgedArmAt(
                -direction, outerRadius, seamBridgePx);
    }

    float sampleCount = float(pairLimit * 2);
    innerCoverage /= max(sampleCount, 1.0);
    outerCoverage /= max(sampleCount, 1.0);
    float innerContainment = smoothstep(0.84, 0.995, innerCoverage);
    float outerContainment = smoothstep(0.68, 0.97, outerCoverage);
    return innerContainment
            * mix(0.25, 1.0, outerContainment)
            * smoothstep(0.0, 0.06, configuredArea);
}

vec4 improvedArmSurface(float markerFacing) {
    float centerDepth = depthAt(vec2(0.0));
    float depthScale = max(1.0 - centerDepth, 0.00005);
    float projectionScale = clamp(ArmProjectionScale, 0.45, 3.5);
    float normalRadius = clamp(
            1.40 * clamp(ImprovedReliefScale, 0.50, 3.0)
                    * sqrt(projectionScale),
            1.0,
            4.0
    );
    vec2 offsetX = vec2(normalRadius, 0.0);
    vec2 offsetY = vec2(0.0, normalRadius);
    float validXP = sameArmAt(offsetX);
    float validXN = sameArmAt(-offsetX);
    float validYP = sameArmAt(offsetY);
    float validYN = sameArmAt(-offsetY);
    float validCardinals = validXP + validXN + validYP + validYN;

    float depthXP = mix(centerDepth, depthAt(offsetX), validXP);
    float depthXN = mix(centerDepth, depthAt(-offsetX), validXN);
    float depthYP = mix(centerDepth, depthAt(offsetY), validYP);
    float depthYN = mix(centerDepth, depthAt(-offsetY), validYN);
    float slopeUnit = max(depthScale * 0.00215 * normalRadius, 0.0000025);
    vec2 depthGradient = vec2(depthXP - depthXN, depthYP - depthYN)
            / max(2.0 * slopeUnit, 0.0000001);
    float depthFacing = inversesqrt(
            1.0 + dot(depthGradient, depthGradient) * 0.62
    );

    float neighboringFacing = markerFacing;
    if (validCardinals > 0.5) {
        neighboringFacing = (
                texture(MaskSampler, texCoord0 + offsetX / InSize).b * validXP
                + texture(MaskSampler, texCoord0 - offsetX / InSize).b * validXN
                + texture(MaskSampler, texCoord0 + offsetY / InSize).b * validYP
                + texture(MaskSampler, texCoord0 - offsetY / InSize).b * validYN
        ) / validCardinals;
    }
    float smoothStrength = clamp(ImprovedSmoothNormalStrength, 0.0, 1.0);
    float smoothedMarkerFacing = mix(
            markerFacing,
            mix(markerFacing, neighboringFacing, 0.45),
            smoothStrength
    );
    float smoothFacing = clamp(
            mix(
                    smoothedMarkerFacing,
                    depthFacing,
                    smoothStrength * clamp(validCardinals * 0.25, 0.0, 1.0)
            ),
            0.0,
            1.0
    );

    float baseThreshold = max(0.0000020, depthScale * 0.00170)
            * clamp(ImprovedReliefThreshold, 0.50, 3.0);
    int directionLimit = ThermalQuality <= 1 ? 2 : 4;
    int radiusLimit = ThermalQuality <= 1 ? 1 : (ThermalQuality == 2 ? 2 : 3);
    float protrusionSum = 0.0;
    float recessSum = 0.0;
    float reliefWeightSum = 0.0;
    float strongestProtrusion = 0.0;
    float strongestRecess = 0.0;
    float baseReliefRadius = clamp(
            2.40 * clamp(ImprovedReliefScale, 0.50, 3.0)
                    * sqrt(projectionScale),
            1.5,
            8.0
    );

    for (int radiusIndex = 0; radiusIndex < 3; radiusIndex++) {
        if (radiusIndex >= radiusLimit) break;
        float radiusMultiplier = radiusIndex == 0
                ? 1.0
                : (radiusIndex == 1 ? 2.20 : 4.25);
        float radiusWeight = radiusIndex == 0
                ? 1.0
                : (radiusIndex == 1 ? 0.78 : 0.52);
        float radius = baseReliefRadius * radiusMultiplier;
        float threshold = baseThreshold * (1.0 + radius * 0.16);

        for (int directionIndex = 0; directionIndex < 4; directionIndex++) {
            if (directionIndex >= directionLimit) break;
            vec2 direction = armDirection(directionIndex);
            vec2 offsetPx = direction * radius;
            float validPair = sameArmAt(offsetPx) * sameArmAt(-offsetPx);
            if (validPair < 0.5) continue;

            float positiveDelta = depthAt(offsetPx) - centerDepth;
            float negativeDelta = depthAt(-offsetPx) - centerDepth;
            float protrusionDelta = max(min(positiveDelta, negativeDelta), 0.0);
            float recessDelta = max(min(-positiveDelta, -negativeDelta), 0.0);
            float positiveMagnitude = abs(positiveDelta);
            float negativeMagnitude = abs(negativeDelta);
            float agreement = min(positiveMagnitude, negativeMagnitude)
                    / max(
                            max(positiveMagnitude, negativeMagnitude),
                            threshold * 0.25
                    );
            agreement = sqrt(clamp(agreement, 0.0, 1.0));
            float protrusion = protrusionDelta
                    / max(protrusionDelta + threshold * 1.65, 0.0000001);
            float recess = recessDelta
                    / max(recessDelta + threshold * 1.65, 0.0000001);
            float pairWeight = radiusWeight * mix(0.30, 1.0, agreement);
            protrusionSum += protrusion * pairWeight;
            recessSum += recess * pairWeight;
            reliefWeightSum += pairWeight;
            strongestProtrusion = max(
                    strongestProtrusion,
                    protrusion * radiusWeight
            );
            strongestRecess = max(strongestRecess, recess * radiusWeight);
        }
    }

    vec2 averageRelief = vec2(protrusionSum, recessSum)
            / max(reliefWeightSum, 0.0001);
    vec2 strongestRelief = vec2(strongestProtrusion, strongestRecess);
    vec2 relief = mix(
            strongestRelief,
            averageRelief,
            mix(0.55, 1.0, clamp(ImprovedReliefSmoothing, 0.0, 1.0))
    );
    relief = clamp(relief, 0.0, 1.0);
    return vec4(smoothFacing, relief.x, relief.y, max(relief.x, relief.y));
}

vec4 warmPaletteBounds() {
    float greenWidth = max(WarmGreenWidth, 0.001);
    float redWidth = max(WarmRedWidth, 0.001);
    float pinkWidth = max(WarmPinkWidth, 0.001);
    float whiteWidth = max(WarmWhiteWidth, 0.001);
    float totalWidth = greenWidth + redWidth + pinkWidth + whiteWidth;
    return vec4(
            greenWidth / totalWidth,
            (greenWidth + redWidth) / totalWidth,
            (greenWidth + redWidth + pinkWidth) / totalWidth,
            totalWidth
    );
}

vec3 warmBodyPalette(float heat, vec4 bounds) {
    vec3 green = vec3(0.00, 0.96, 0.075);
    vec3 red = vec3(1.00, 0.018, 0.012);
    vec3 pink = vec3(1.00, 0.48, 0.60);
    vec3 whiteHot = vec3(1.00, 0.97, 0.94);
    float greenRedSoftness = min(0.12, min(WarmGreenWidth, WarmRedWidth) / bounds.w * 0.72);
    float redPinkSoftness = min(0.12, min(WarmRedWidth, WarmPinkWidth) / bounds.w * 0.72);
    float pinkWhiteSoftness = min(0.12, min(WarmPinkWidth, WarmWhiteWidth) / bounds.w * 0.72);

    vec3 color = mix(green, red, smoothstep(bounds.x - greenRedSoftness, bounds.x + greenRedSoftness, heat));
    color = mix(color, pink, smoothstep(bounds.y - redPinkSoftness, bounds.y + redPinkSoftness, heat));
    return mix(color, whiteHot, smoothstep(bounds.z - pinkWhiteSoftness, bounds.z + pinkWhiteSoftness, heat));
}

vec3 warmBodyPaletteV2(float heat, vec4 bounds) {
    heat = clamp(heat, 0.0, 1.0);
    vec3 cyanBlue = vec3(0.00, 0.70, 1.00);
    vec3 green = vec3(0.00, 0.96, 0.075);
    vec3 yellow = vec3(1.00, 0.91, 0.00);
    vec3 orange = vec3(1.00, 0.30, 0.00);
    vec3 red = vec3(1.00, 0.018, 0.012);
    vec3 pink = vec3(1.00, 0.48, 0.60);
    vec3 whiteHot = vec3(1.00, 0.97, 0.94);
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
    return mix(pink, whiteHot, smoothstep(bounds.z, 1.0, heat));
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
    vec2 pixel = gl_FragCoord.xy;
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
    float brightnessWeight = mix(0.75, 1.08, luma);
    float darken = rowPattern.y * clamp(ScanlineStrength, 0.0, 0.25)
            * brightnessWeight * clamp(visibility, 0.0, 1.0) * 0.58;
    return color * (1.0 - darken);
}

vec3 applyUnifiedV2SensorRows(vec3 color, vec3 rowPattern, float visibility) {
    float luma = clamp(dot(color, vec3(0.299, 0.587, 0.114)), 0.0, 1.0);
    float contrast = clamp(ScanlineStrength, 0.0, 0.25)
            * mix(0.78, 1.05, luma) * clamp(visibility, 0.0, 1.0) * 0.32;
    float rowPolarity = rowPattern.x * 2.0 - 1.0;
    return clamp(color * (1.0 - rowPolarity * contrast), 0.0, 1.0);
}

vec3 applyActivationWarmPaletteFlash(float coordinate, vec3 color, vec4 bounds) {
    float amount = clamp(ActivationFlash, 0.0, 1.0);
    if (amount <= 0.0001) return color;
    float cyanEnd = ThermalPresetMode == 2 ? min(0.12, bounds.x * 0.18) : 0.0;
    float heatMask = ThermalPresetMode == 2
            ? smoothstep(cyanEnd, cyanEnd + 0.055, coordinate)
            : 1.0;
    vec3 hotter = mix(
            vec3(1.00, 0.91, 0.00),
            vec3(1.00, 0.018, 0.012),
            smoothstep(bounds.x - 0.08, bounds.x, coordinate)
    );
    hotter = mix(
            hotter,
            vec3(1.00, 0.97, 0.94),
            smoothstep(bounds.x + 0.035, bounds.y + 0.02, coordinate)
    );
    return mix(color, hotter, amount * heatMask);
}

void main() {
    vec4 marker = texture(MaskSampler, texCoord0);
    float mask = marker.a;
    vec3 rowPattern = crysisRowPattern();

    // Calculate the V3 spectrum from the complete, unmodified arm mask. The
    // held item is only a final foreground cover and must not become a new arm
    // silhouette boundary or alter the arm's heat/relief field.
    if (heldItemCoversArm(texCoord0, mask)) {
        discard;
    }

    if (mask < 0.01) {
        if (OuterGlowStrength <= 0.001 || OuterGlowRatio <= 0.0001) {
            discard;
        }
        float radiusPx = clamp(min(InSize.x, InSize.y) * OuterGlowRatio * 0.18, 2.0, 24.0);
        float nearGlow = ringCoverage(max(2.0, radiusPx * 0.45));
        float farGlow = ringCoverage(radiusPx);
        float glow = max(nearGlow * 0.78, farGlow * 0.42) * clamp(OuterGlowStrength, 0.0, 1.0);
        if (glow <= 0.003) {
            discard;
        }
        if (heldItemCoversArmGlow(radiusPx)) {
            discard;
        }
        vec3 glowColor = mix(vec3(0.00, 0.035, 0.78), vec3(0.00, 0.58, 1.00), clamp(nearGlow, 0.0, 1.0));
        vec3 stripedGlow = ThermalPresetMode == 2
                ? applyUnifiedV2SensorRows(glowColor, rowPattern, mix(0.45, 0.75, nearGlow))
                : applyGlobalSensorRows(glowColor, rowPattern, mix(0.32, 0.65, nearGlow));
        fragColor = vec4(
                stripedGlow,
                clamp(glow * 0.72, 0.0, 0.68));
        return;
    }

    vec4 bounds = warmPaletteBounds();
    float materialDetail = (marker.r * 2.0 - 1.0);
    float heat = 0.86 + clamp(materialDetail, -0.12, 0.12) * 0.16;
    heat = pow(clamp(heat, 0.01, 0.99), max(HeatPower * 0.34, 0.01));
    float classHeat = clamp((heat - 0.46) / 0.52, 0.0, 1.0);
    float centerReach = mix(0.78, 0.96, smoothstep(0.10, 0.95, classHeat));
    float paletteCoordinate;
    if (ThermalPresetMode == 2) {
        float projectionScale = clamp(ArmProjectionScale, 0.45, 3.5);
        float projectedArmSizePx = clamp(
                min(InSize.x, InSize.y) * 0.32 * projectionScale,
                72.0,
                640.0
        );
        float thicknessSearchRadius = clamp(
                min(InSize.x, InSize.y)
                        * max(InnerRadiusRatio, 0.05) * 0.12
                        * clamp(ImprovedSilhouetteWidth, 0.40, 2.0)
                        * projectionScale,
                10.0,
                128.0
        );
        float layerProgress = diffusedArmSilhouetteProgress(
                thicknessSearchRadius
        );
        vec4 improvedSurface = improvedArmSurface(marker.b);
        float faceOn = smoothstep(0.08, 0.96, improvedSurface.x);
        float ordinaryPlateau = clamp(
                0.940
                        + (clamp(ImprovedCoreHeat, 0.55, 0.98) - 0.79) * 0.12
                        + materialDetail * 0.010,
                0.925,
                0.955
        );
        float biologicalSupport = mix(0.72, 1.0, smoothstep(0.28, 0.90, classHeat));
        float facePeak = smoothstep(0.72, 0.985, faceOn);
        float reliefPeakLoss = smoothstep(
                0.08,
                0.62,
                max(improvedSurface.y, improvedSurface.z)
        );
        float centralHotPeak = clamp(
                coherentArmInteriorPeak(projectedArmSizePx)
                        * facePeak * (1.0 - reliefPeakLoss)
                        * biologicalSupport,
                0.0,
                1.0
        );
        float reliefStrength = clamp(GeometryDepthStrength, 0.0, 2.0);
        float flankFactor = mix(
                0.25,
                1.0,
                pow(clamp(1.0 - faceOn, 0.0, 1.0), 0.62)
        );
        float facingCooling = pow(
                clamp(1.0 - faceOn, 0.0, 1.0),
                0.72
        ) * (0.12 + clamp(ImprovedFacingStrength, 0.0, 0.55) * 0.95);
        float protrusionCooling = improvedSurface.y * reliefStrength
                * (0.04 + clamp(ImprovedProtrusionCooling, 0.0, 0.50) * 1.20)
                * flankFactor;
        float recessCooling = improvedSurface.z * reliefStrength
                * (0.025 + clamp(ImprovedRecessCooling, 0.0, 0.40) * 0.85);
        float interiorCoordinate = ordinaryPlateau + centralHotPeak * 0.045
                - facingCooling - protrusionCooling - recessCooling;
        float rimProgress = pow(
                clamp(layerProgress, 0.0, 1.0),
                clamp(ImprovedRimCurve, 0.30, 1.50)
        );
        float silhouetteCoordinate = mix(0.025, 0.085, faceOn);
        paletteCoordinate = clamp(
                mix(silhouetteCoordinate, interiorCoordinate, rimProgress),
                0.0,
                0.985
        );
    } else {
        float thicknessSearchRadius = clamp(
                min(InSize.x, InSize.y) * max(InnerRadiusRatio, 0.05) * 0.12,
                12.0,
                96.0
        );
        float layerProgress = localArmDepth(thicknessSearchRadius);
        paletteCoordinate = clamp(layerProgress * centerReach, 0.0, 0.985);
    }
    float sensorRowResponse = ThermalPresetMode == 2
            ? warmSensorResponseV2(paletteCoordinate, bounds)
            : warmSensorResponse(paletteCoordinate, bounds);
    float bandMix = clamp(BandingStrength * 0.06, 0.0, 0.075);
    paletteCoordinate = mix(
            paletteCoordinate,
            floor(paletteCoordinate * 18.0) / 18.0,
            bandMix
    );

    float coldCoordinate = clamp(
            paletteCoordinate - clamp(ScanlinePaletteShift, 0.0, 0.18),
            0.0,
            1.0
    );
    vec3 baseThermal = ThermalPresetMode == 2
            ? warmBodyPaletteV2(paletteCoordinate, bounds)
            : warmBodyPalette(paletteCoordinate, bounds);
    vec3 coldThermal = ThermalPresetMode == 2
            ? warmBodyPaletteV2(coldCoordinate, bounds)
            : warmBodyPalette(coldCoordinate, bounds);
    float thermalRowBlend = rowPattern.x
            * (ThermalPresetMode == 2 ? 1.0 : rowPattern.z)
            * sensorRowResponse;
    float flashCoordinate = ThermalPresetMode == 2
            ? warmV2RowCoordinate(paletteCoordinate, thermalRowBlend, bounds)
            : mix(paletteCoordinate, coldCoordinate, clamp(thermalRowBlend, 0.0, 1.0));
    vec3 thermal = ThermalPresetMode == 2
            ? warmBodyPaletteV2(flashCoordinate, bounds)
            : mix(baseThermal, coldThermal, clamp(thermalRowBlend, 0.0, 1.0));
    thermal = applyActivationWarmPaletteFlash(flashCoordinate, thermal, bounds);
    float sourceLuma = pow(max(marker.g, 0.0001), 0.9);
    vec3 textureBase = vec3(clamp(sourceLuma * mix(0.84, 1.16, marker.g), 0.0, 1.0));
    float opacityCurve = 1.0 - pow(1.0 - clamp(ThermalModelOpacity, 0.0, 1.0), 1.45);
    float opacity = mix(0.78, 0.94, clamp(InnerStrength, 0.0, 1.0)) * opacityCurve;
    vec3 result = mix(textureBase, thermal, clamp(opacity, 0.0, 0.95));
    result += thermal * (0.025 + heat * 0.045);
    if (ThermalPresetMode == 2) {
        result = applyUnifiedV2SensorRows(result, rowPattern, sensorRowResponse * 0.30);
    }
    // The normal arm draw is intentionally replaced by this late composite.
    // Treat every surviving cutout texel as opaque model coverage; forwarding
    // texture alpha here can expose the already-rendered world through the arm.
    fragColor = vec4(clamp(result, 0.0, 1.0), 1.0);
}
