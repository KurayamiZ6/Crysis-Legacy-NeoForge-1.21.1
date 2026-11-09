#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MarkerDepthSampler;

in vec2 texCoord;

uniform vec2 InSize;
uniform float EncodedHeightRangePx;
uniform float InnerRadiusRatio;
uniform float MaxSearchRadiusPx;
uniform int ThermalQuality;
uniform float DiffusionStrength;
uniform float TapSpacing;
uniform float GeometryDepthStrength;
uniform float GeometryDepthRadius;
uniform float GeometryDepthContrast;
uniform int ImprovedReliefMode;
uniform int DebugGeometryReliefOnly;
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

out vec4 fragColor;

const int FIELD_DIRECTIONS = 12;
const int FIELD_STEPS = 5;
const float PI = 3.14159265358979323846;

bool isThermalSource(vec4 data) {
    return data.a >= 0.1;
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

float sameSurface(vec4 sampleData, float sourceType) {
    return step(0.1, sampleData.a) * (1.0 - step(0.075, abs(sampleData.a - sourceType)));
}

vec2 decodeFieldSurfaceData(float packedValue) {
    float packedSurface = floor(clamp(packedValue, 0.0, 1.0) * 63.0 + 0.5);
    return vec2(mod(packedSurface, 8.0) / 7.0, floor(packedSurface / 8.0) / 7.0);
}

vec2 fieldSurfaceData(vec4 data) {
    // Hot blocks retain their direct cooling scalar. Entity field data packs
    // material coldness in X and the higher-precision view-facing term in Y.
    return isHotBlock(data.a) ? vec2(data.g, 1.0) : decodeFieldSurfaceData(data.g);
}

float heatDiffusionWeight(int tap) {
    int distance = tap < 0 ? -tap : tap;
    if (distance == 0) return 0.220;
    if (distance == 1) return 0.190;
    if (distance == 2) return 0.120;
    if (distance == 3) return 0.060;
    return 0.025;
}

float resolveVerticalHeat(vec2 uv, vec2 texel, vec4 center) {
    int maxTap = ThermalQuality <= 1 ? 2 : (ThermalQuality == 2 ? 3 : 4);
    float weightedHeat = 0.0;
    float weightSum = 0.0;
    vec2 centerSurface = fieldSurfaceData(center);

    for (int tap = -4; tap <= 4; tap++) {
        int distance = tap < 0 ? -tap : tap;
        if (distance > maxTap) continue;
        vec4 sampleData = tap == 0
                ? center
                : texture(DiffuseSampler, uv + vec2(0.0, texel.y * TapSpacing * float(tap)));
        float sameSource = step(0.1, sampleData.a)
                * (1.0 - step(0.075, abs(sampleData.a - center.a)));
        vec2 sampleSurface = fieldSurfaceData(sampleData);
        float materialSimilarity = 1.0 - smoothstep(
                0.16, 0.70, abs(sampleSurface.x - centerSurface.x));
        float facingSimilarity = 1.0 - smoothstep(
                0.18, 0.72, abs(sampleSurface.y - centerSurface.y));
        float scaleSimilarity = 1.0 - smoothstep(0.20, 0.76, abs(sampleData.b - center.b));
        float materialWeight = mix(0.52, 1.0, materialSimilarity)
                * mix(0.82, 1.0, facingSimilarity);
        float scaleWeight = mix(0.62, 1.0, scaleSimilarity);
        float weight = heatDiffusionWeight(tap) * sameSource * materialWeight * scaleWeight;
        weightedHeat += sampleData.r * weight;
        weightSum += weight;
    }

    float blurredHeat = weightedHeat / max(weightSum, 0.0001);
    return mix(center.r, blurredHeat, clamp(DiffusionStrength, 0.0, 1.0));
}

vec2 fieldDirection(int index) {
    // Exact 30-degree directions used by the previous cos/sin calculation,
    // stored explicitly to remove trigonometry from the inner sample loop.
    if (index == 0) return vec2(1.0, 0.0);
    if (index == 1) return vec2(0.86602540, 0.5);
    if (index == 2) return vec2(0.5, 0.86602540);
    if (index == 3) return vec2(0.0, 1.0);
    if (index == 4) return vec2(-0.5, 0.86602540);
    if (index == 5) return vec2(-0.86602540, 0.5);
    if (index == 6) return vec2(-1.0, 0.0);
    if (index == 7) return vec2(-0.86602540, -0.5);
    if (index == 8) return vec2(-0.5, -0.86602540);
    if (index == 9) return vec2(0.0, -1.0);
    if (index == 10) return vec2(0.5, -0.86602540);
    return vec2(0.86602540, -0.5);
}

float bridgedSurface(
        vec2 uv,
        vec2 texel,
        vec2 direction,
        float radius,
        float seamBridgePx,
        float sourceType
) {
    float direct = sameSurface(texture(DiffuseSampler, uv + direction * radius * texel), sourceType);
    if (direct >= 0.5 || seamBridgePx <= 0.0) {
        return direct;
    }

    // NanoGeo is assembled from many adjacent cuboids. A one-to-three-pixel
    // gap between two pieces is an internal seam, not exposure to the cold
    // background. Looking just beyond the gap closes only those narrow seams;
    // a true outer silhouette has no same-class surface behind it.
    return sameSurface(
            texture(DiffuseSampler, uv + direction * (radius + seamBridgePx) * texel),
            sourceType
    );
}

float distanceToBoundary(
        vec2 uv,
        vec2 texel,
        float maxRadius,
        float seamBridgePx,
        float sourceType
) {
    float previousRadius = 0.0;
    int directionLimit = ThermalQuality <= 1 ? 8 : (ThermalQuality == 2 ? 10 : 12);

    for (int stepIndex = 0; stepIndex < FIELD_STEPS; stepIndex++) {
        float stepT = float(stepIndex) / float(FIELD_STEPS - 1);
        float radius = mix(1.0, maxRadius, stepT * stepT * stepT);
        float coverage = 0.0;

        for (int directionIndex = 0; directionIndex < FIELD_DIRECTIONS; directionIndex++) {
            if (directionIndex >= directionLimit) break;
            vec2 direction = fieldDirection(directionIndex);
            coverage += bridgedSurface(uv, texel, direction, radius, seamBridgePx, sourceType);
        }

        coverage /= float(directionLimit);
        if (coverage < 0.999) {
            float withinRing = clamp(-cos(PI * coverage), 0.0, 1.0);
            return max(previousRadius, radius * withinRing);
        }
        previousRadius = radius;
    }

    return maxRadius;
}

float diffusedSilhouetteProgress(
        vec2 uv,
        vec2 texel,
        float maxRadius,
        float seamBridgePx,
        float sourceType
) {
    int directionLimit = ThermalQuality <= 1 ? 8 : (ThermalQuality == 2 ? 10 : 12);
    int ringLimit = ThermalQuality <= 1 ? 3 : (ThermalQuality == 2 ? 4 : 5);
    float accumulated = 0.0;
    float weightSum = 0.0;

    // Integrate several contained rings instead of returning the first ring
    // that touches the background. The old early exit quantized the silhouette
    // into thin contours. This kernel behaves like heat diffusion: broad forms
    // warm gradually, while thin limbs naturally retain a cooler interior.
    for (int ringIndex = 0; ringIndex < 5; ringIndex++) {
        if (ringIndex >= ringLimit) break;
        float ringT = float(ringIndex + 1) / float(ringLimit);
        float radius = maxRadius * mix(0.10, 1.0, ringT * ringT * 0.72 + ringT * 0.28);
        float coverage = 0.0;
        for (int directionIndex = 0; directionIndex < FIELD_DIRECTIONS; directionIndex++) {
            if (directionIndex >= directionLimit) break;
            coverage += bridgedSurface(
                    uv, texel, fieldDirection(directionIndex), radius, seamBridgePx, sourceType);
        }
        coverage /= float(directionLimit);
        float containment = smoothstep(0.78, 0.995, coverage);
        float ringWeight = mix(1.18, 0.82, ringT);
        accumulated += containment * ringWeight;
        weightSum += ringWeight;
    }

    return smoothstep(0.0, 1.0, clamp(accumulated / max(weightSum, 0.0001), 0.0, 1.0));
}

float coherentInteriorPeak(
        vec2 uv,
        vec2 texel,
        float projectedHeightPx,
        float seamBridgePx,
        float sourceType
) {
    int pairLimit = ThermalQuality <= 1 ? 3 : (ThermalQuality == 2 ? 4 : 6);
    // Keep both containment rings proportional to the projected entity size.
    // Absolute upper clamps made a close target outgrow its kernel, turning
    // most of the torso into one giant row-free peak. Large lower clamps did
    // the inverse to distant targets and collapsed the peak completely.
    // A larger user-facing area value reduces the erosion radii, allowing the
    // coherent peak to grow outward from its center without shifting it.
    float configuredArea = clamp(ImprovedV2HotPeakArea, 0.0, 1.0);
    float containmentScale = mix(1.50, 0.50, configuredArea);
    float innerRadius = max(projectedHeightPx * 0.055 * containmentScale, 1.0);
    float outerRadius = max(projectedHeightPx * 0.120 * containmentScale, 1.5);
    float innerCoverage = 0.0;
    float outerCoverage = 0.0;

    // A hot center must be a coherent two-dimensional region, not a UV-noise
    // maximum. Two projected-size rings test whether the current fragment is
    // deeply surrounded by the same WARM silhouette. Sampling opposite sides
    // of several axes keeps the result connected across adjacent screen rows.
    for (int pairIndex = 0; pairIndex < 6; pairIndex++) {
        if (pairIndex >= pairLimit) break;
        vec2 direction = fieldDirection(pairIndex);
        innerCoverage += bridgedSurface(
                uv, texel, direction, innerRadius, seamBridgePx, sourceType);
        innerCoverage += bridgedSurface(
                uv, texel, -direction, innerRadius, seamBridgePx, sourceType);
        outerCoverage += bridgedSurface(
                uv, texel, direction, outerRadius, seamBridgePx, sourceType);
        outerCoverage += bridgedSurface(
                uv, texel, -direction, outerRadius, seamBridgePx, sourceType);
    }

    float sampleCount = float(pairLimit * 2);
    innerCoverage /= max(sampleCount, 1.0);
    outerCoverage /= max(sampleCount, 1.0);
    float innerContainment = smoothstep(0.84, 0.995, innerCoverage);
    float outerContainment = smoothstep(0.68, 0.97, outerCoverage);
    float areaEnable = smoothstep(0.0, 0.06, configuredArea);
    return innerContainment * mix(0.25, 1.0, outerContainment) * areaEnable;
}

vec2 directionalBoundaryDistance(
        vec2 uv,
        vec2 texel,
        vec2 direction,
        float maxRadius,
        float seamBridgePx,
        float sourceType
) {
    int stepLimit = ThermalQuality <= 1 ? 4 : (ThermalQuality == 2 ? 5 : 6);
    float previousRadius = 0.0;

    for (int stepIndex = 0; stepIndex < 6; stepIndex++) {
        if (stepIndex >= stepLimit) break;
        float stepT = float(stepIndex) / float(stepLimit - 1);
        float radius = mix(1.0, maxRadius, stepT * stepT);
        float coverage = bridgedSurface(
                uv, texel, direction, radius, seamBridgePx, sourceType);
        if (coverage < 0.5) {
            // One midpoint refinement keeps the local span smooth without a
            // full distance-transform pass or additional framebuffer.
            float lower = previousRadius;
            float upper = radius;
            float midpoint = (lower + upper) * 0.5;
            float midpointCoverage = bridgedSurface(
                    uv, texel, direction, midpoint, seamBridgePx, sourceType);
            if (midpointCoverage >= 0.5) {
                lower = midpoint;
            } else {
                upper = midpoint;
            }
            return vec2((lower + upper) * 0.5, 1.0);
        }
        previousRadius = radius;
    }

    // The opposite edge can legitimately be farther than the configured
    // search limit. Retaining maxRadius still produces a conservative local
    // thickness instead of falling back to the old fixed edge corridor.
    return vec2(maxRadius, 0.0);
}

vec2 localThicknessField(
        vec2 uv,
        vec2 texel,
        float maxRadius,
        float seamBridgePx,
        float sourceType
) {
    int pairLimit = ThermalQuality <= 1 ? 4 : (ThermalQuality == 2 ? 5 : 6);
    float closestBoundary = maxRadius;
    float normalizedDepth = 1.0;

    // For each axis, measure both sides of the visible model. The axis whose
    // nearer side is closest approximates the local surface normal. Along that
    // axis, 2*near/(near+far) is zero at the silhouette and one at the local
    // center, independent of the limb/head/torso size in screen pixels.
    for (int pairIndex = 0; pairIndex < 6; pairIndex++) {
        if (pairIndex >= pairLimit) break;
        vec2 direction = fieldDirection(pairIndex);
        vec2 positive = directionalBoundaryDistance(
                uv, texel, direction, maxRadius, seamBridgePx, sourceType);
        vec2 negative = directionalBoundaryDistance(
                uv, texel, -direction, maxRadius, seamBridgePx, sourceType);
        float nearDistance = min(positive.x, negative.x);
        float farDistance = max(positive.x, negative.x);
        float foundBoundary = max(positive.y, negative.y);
        if (foundBoundary > 0.5 && nearDistance < closestBoundary) {
            closestBoundary = nearDistance;
            normalizedDepth = clamp(
                    (2.0 * nearDistance) / max(nearDistance + farDistance, 0.0001),
                    0.0,
                    1.0
            );
        }
    }

    return vec2(closestBoundary, normalizedDepth);
}

vec2 geometryDirection(int index) {
    if (index == 0) return vec2(1.0, 0.0);
    if (index == 1) return vec2(0.0, 1.0);
    if (index == 2) return vec2(0.70710678, 0.70710678);
    return vec2(0.70710678, -0.70710678);
}

float geometryRadius(int index) {
    if (index == 0) return 1.25;
    if (index == 1) return 2.75;
    return 5.0;
}

float geometryRadiusWeight(int index) {
    if (index == 0) return 1.0;
    if (index == 1) return 0.72;
    return 0.42;
}

float internalGeometryEdge(vec2 uv, vec2 texel, float sourceType) {
    float centerDepth = texture(MarkerDepthSampler, uv).r;
    float depthScale = max(1.0 - centerDepth, 0.00005);
    float baseThreshold = max(0.0000035, depthScale * 0.0035);
    int directionLimit = ThermalQuality <= 1 ? 2 : 4;
    int radiusLimit = ThermalQuality <= 1 ? 2 : 3;
    float strongestEdge = 0.0;

    for (int radiusIndex = 0; radiusIndex < 3; radiusIndex++) {
        if (radiusIndex >= radiusLimit) break;
        float radius = geometryRadius(radiusIndex);
        float radiusWeight = geometryRadiusWeight(radiusIndex);
        float threshold = baseThreshold * (1.0 + radius * 0.35);

        for (int directionIndex = 0; directionIndex < 4; directionIndex++) {
            if (directionIndex >= directionLimit) break;
            vec2 offset = geometryDirection(directionIndex) * radius * texel;
            vec4 positiveData = texture(DiffuseSampler, uv + offset);
            vec4 negativeData = texture(DiffuseSampler, uv - offset);
            float validPair = sameSurface(positiveData, sourceType) * sameSurface(negativeData, sourceType);
            if (validPair < 0.5) continue;

            float positiveDepth = texture(MarkerDepthSampler, uv + offset).r;
            float negativeDepth = texture(MarkerDepthSampler, uv - offset).r;
            // Second depth derivative rejects a planar slope but responds when
            // samples straddle an arm/body or model-part occlusion boundary.
            float curvature = abs(positiveDepth + negativeDepth - centerDepth * 2.0);
            float edge = smoothstep(threshold, threshold * 7.0, curvature) * radiusWeight;
            strongestEdge = max(strongestEdge, edge);
        }
    }

    return clamp(strongestEdge, 0.0, 1.0);
}

vec2 signedGeometryRelief(
        vec2 uv,
        vec2 texel,
        float sourceType,
        float projectedHeightPx
) {
    float centerDepth = texture(MarkerDepthSampler, uv).r;
    float depthScale = max(1.0 - centerDepth, 0.00005);
    float baseThreshold = max(0.0000035, depthScale * 0.0030);
    float projectedScale = clamp(projectedHeightPx / 180.0, 0.75, 1.80);
    float radiusScale = clamp(GeometryDepthRadius, 0.50, 3.0) * projectedScale;
    float contrast = clamp(GeometryDepthContrast, 0.25, 3.0);
    int directionLimit = ThermalQuality <= 1 ? 2 : 4;
    int radiusLimit = ThermalQuality <= 1 ? 2 : 3;
    float strongestProtrusion = 0.0;
    float strongestRecess = 0.0;

    for (int radiusIndex = 0; radiusIndex < 3; radiusIndex++) {
        if (radiusIndex >= radiusLimit) break;
        float radius = geometryRadius(radiusIndex) * radiusScale;
        float radiusWeight = geometryRadiusWeight(radiusIndex);
        float threshold = baseThreshold * (1.0 + radius * 0.30);

        for (int directionIndex = 0; directionIndex < 4; directionIndex++) {
            if (directionIndex >= directionLimit) break;
            vec2 offset = geometryDirection(directionIndex) * radius * texel;
            vec4 positiveData = texture(DiffuseSampler, uv + offset);
            vec4 negativeData = texture(DiffuseSampler, uv - offset);
            float validPair = sameSurface(positiveData, sourceType)
                    * sameSurface(negativeData, sourceType);
            if (validPair < 0.5) continue;

            float positiveDelta = texture(MarkerDepthSampler, uv + offset).r - centerDepth;
            float negativeDelta = texture(MarkerDepthSampler, uv - offset).r - centerDepth;

            // Both opposite samples must agree on the sign. A one-sided depth
            // step is a cuboid seam; two farther samples mean a broad raised
            // surface, while two nearer samples mean a genuine recess.
            float protrusionDelta = max(min(positiveDelta, negativeDelta), 0.0);
            float recessDelta = max(min(-positiveDelta, -negativeDelta), 0.0);
            float protrusion = smoothstep(
                    1.0, 5.0, protrusionDelta / threshold * contrast) * radiusWeight;
            float recess = smoothstep(
                    1.0, 5.0, recessDelta / threshold * contrast) * radiusWeight;
            strongestProtrusion = max(strongestProtrusion, protrusion);
            strongestRecess = max(strongestRecess, recess);
        }
    }

    return clamp(vec2(strongestProtrusion, strongestRecess), 0.0, 1.0);
}

vec4 improvedCrysisSurface(
        vec2 uv,
        vec2 texel,
        float sourceType,
        float projectedHeightPx,
        float markerFacing
) {
    float centerDepth = texture(MarkerDepthSampler, uv).r;
    float depthScale = max(1.0 - centerDepth, 0.00005);
    float projectedScale = clamp(projectedHeightPx / 220.0, 0.72, 1.85);
    float normalRadius = clamp(
            1.40 * clamp(ImprovedReliefScale, 0.50, 3.0) * sqrt(projectedScale),
            1.0,
            4.0
    );
    vec2 offsetX = vec2(normalRadius * texel.x, 0.0);
    vec2 offsetY = vec2(0.0, normalRadius * texel.y);
    vec4 dataXP = texture(DiffuseSampler, uv + offsetX);
    vec4 dataXN = texture(DiffuseSampler, uv - offsetX);
    vec4 dataYP = texture(DiffuseSampler, uv + offsetY);
    vec4 dataYN = texture(DiffuseSampler, uv - offsetY);
    float validXP = sameSurface(dataXP, sourceType);
    float validXN = sameSurface(dataXN, sourceType);
    float validYP = sameSurface(dataYP, sourceType);
    float validYN = sameSurface(dataYN, sourceType);
    float validCardinals = validXP + validXN + validYP + validYN;

    float depthXP = mix(centerDepth, texture(MarkerDepthSampler, uv + offsetX).r, validXP);
    float depthXN = mix(centerDepth, texture(MarkerDepthSampler, uv - offsetX).r, validXN);
    float depthYP = mix(centerDepth, texture(MarkerDepthSampler, uv + offsetY).r, validYP);
    float depthYN = mix(centerDepth, texture(MarkerDepthSampler, uv - offsetY).r, validYN);
    float slopeUnit = max(depthScale * 0.00215 * normalRadius, 0.0000025);
    vec2 depthGradient = vec2(depthXP - depthXN, depthYP - depthYN)
            / max(2.0 * slopeUnit, 0.0000001);
    float depthFacing = inversesqrt(1.0 + dot(depthGradient, depthGradient) * 0.62);

    float neighboringFacing = markerFacing;
    if (validCardinals > 0.5) {
        neighboringFacing = (
                fieldSurfaceData(dataXP).y * validXP
                + fieldSurfaceData(dataXN).y * validXN
                + fieldSurfaceData(dataYP).y * validYP
                + fieldSurfaceData(dataYN).y * validYN
        ) / validCardinals;
    }
    float smoothStrength = clamp(ImprovedSmoothNormalStrength, 0.0, 1.0);
    float smoothedMarkerFacing = mix(
            markerFacing,
            mix(markerFacing, neighboringFacing, 0.45),
            smoothStrength
    );
    float cardinalConfidence = clamp(validCardinals * 0.25, 0.0, 1.0);
    // Screen-depth incidence is continuous as the camera turns. Prefer it over
    // the marker's eight quantized facing levels whenever all cardinal samples
    // belong to the same surface; the marker remains a stable fallback at a
    // silhouette or a missing cuboid seam.
    float reconstructedFacing = mix(
            smoothedMarkerFacing,
            depthFacing,
            smoothStrength * cardinalConfidence
    );
    float smoothFacing = clamp(
            reconstructedFacing,
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
            projectedHeightPx * 0.012 * clamp(ImprovedReliefScale, 0.50, 3.0),
            1.5,
            8.0
    );

    // Paired samples separate broad relief from one-sided cuboid seams. Both
    // neighbors farther than the center mean a raised/convex feature; both
    // nearer mean a recess. A slope is handled by the reconstructed facing and
    // is deliberately not promoted to a second complete thermal silhouette.
    for (int radiusIndex = 0; radiusIndex < 3; radiusIndex++) {
        if (radiusIndex >= radiusLimit) break;
        float radiusMultiplier = radiusIndex == 0 ? 1.0 : (radiusIndex == 1 ? 2.20 : 4.25);
        float radiusWeight = radiusIndex == 0 ? 1.0 : (radiusIndex == 1 ? 0.78 : 0.52);
        float radius = baseReliefRadius * radiusMultiplier;
        float threshold = baseThreshold * (1.0 + radius * 0.16);

        for (int directionIndex = 0; directionIndex < 4; directionIndex++) {
            if (directionIndex >= directionLimit) break;
            vec2 direction = geometryDirection(directionIndex);
            vec2 offsetUv = direction * radius * texel;
            vec4 positiveData = texture(DiffuseSampler, uv + offsetUv);
            vec4 negativeData = texture(DiffuseSampler, uv - offsetUv);
            float positiveValid = sameSurface(positiveData, sourceType);
            float negativeValid = sameSurface(negativeData, sourceType);
            float validPair = positiveValid * negativeValid;
            if (validPair < 0.5) continue;

            float positiveDelta = texture(MarkerDepthSampler, uv + offsetUv).r - centerDepth;
            float negativeDelta = texture(MarkerDepthSampler, uv - offsetUv).r - centerDepth;
            float protrusionDelta = max(min(positiveDelta, negativeDelta), 0.0);
            float recessDelta = max(min(-positiveDelta, -negativeDelta), 0.0);
            float positiveMagnitude = abs(positiveDelta);
            float negativeMagnitude = abs(negativeDelta);
            float agreement = min(positiveMagnitude, negativeMagnitude)
                    / max(max(positiveMagnitude, negativeMagnitude), threshold * 0.25);
            agreement = sqrt(clamp(agreement, 0.0, 1.0));
            // A rational response has no on/off threshold. Tiny depth noise
            // remains tiny, while a genuine paired height change approaches a
            // stable plateau without popping as a sample crosses one cutoff.
            float protrusion = protrusionDelta
                    / max(protrusionDelta + threshold * 1.65, 0.0000001);
            float recess = recessDelta
                    / max(recessDelta + threshold * 1.65, 0.0000001);
            float pairWeight = radiusWeight * mix(0.30, 1.0, agreement);
            protrusionSum += protrusion * pairWeight;
            recessSum += recess * pairWeight;
            reliefWeightSum += pairWeight;
            strongestProtrusion = max(strongestProtrusion, protrusion * radiusWeight);
            strongestRecess = max(strongestRecess, recess * radiusWeight);
        }
    }

    vec2 averageRelief = vec2(protrusionSum, recessSum) / max(reliefWeightSum, 0.0001);
    vec2 strongestRelief = vec2(strongestProtrusion, strongestRecess);
    vec2 relief = mix(
            strongestRelief,
            averageRelief,
            mix(0.55, 1.0, clamp(ImprovedReliefSmoothing, 0.0, 1.0))
    );
    relief = clamp(relief, 0.0, 1.0);
    return vec4(smoothFacing, relief.x, relief.y, max(relief.x, relief.y));
}

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);
    vec2 texel = 1.0 / InSize;

    if (isThermalSource(center)) {
        float heightPx = max(2.0, center.b * EncodedHeightRangePx);
        vec2 surfaceData = fieldSurfaceData(center);
        float surfaceCold = surfaceData.x;
        float surfaceFacing = surfaceData.y;
        bool useV2EntitySurface = ImprovedReliefMode == 2 && !isHotBlock(center.a);
        // Keep physical edge cooling narrow, but give WARM palette layers a
        // separate, wider distance coordinate. Color-width controls are
        // spatial weights, not partitions of the absolute heat value.
        float improvedWidth = ((ImprovedReliefMode != 0 && isWarmBlooded(center.a))
                || useV2EntitySurface)
                ? clamp(ImprovedSilhouetteWidth, 0.40, 2.0)
                : 1.0;
        float edgeScale = (isWarmBlooded(center.a) || useV2EntitySurface)
                ? 0.135 * improvedWidth
                : 0.09;
        float coolingRadius = clamp(
                heightPx * InnerRadiusRatio * edgeScale,
                2.5,
                min(MaxSearchRadiusPx, 48.0)
        );
        float seamBridgePx = clamp(heightPx * 0.006, 1.25, 3.5);
        float v2InteriorPeak = useV2EntitySurface
                ? coherentInteriorPeak(
                        texCoord, texel, heightPx, seamBridgePx, center.a)
                : 0.0;
        float edgeDistance;
        float layerProgress;
        if (isWarmBlooded(center.a)) {
            if (ImprovedReliefMode != 0) {
                // Only the true entity silhouette receives the full internal
                // green-edge gradient. Internal model-part boundaries are
                // handled later as subtle curvature, never as new silhouettes.
                layerProgress = diffusedSilhouetteProgress(
                        texCoord, texel, coolingRadius, seamBridgePx, center.a);
                edgeDistance = layerProgress * coolingRadius;
            } else {
                float radiusLimit = max(8.0, min(MaxSearchRadiusPx, 96.0));
                float minimumRadius = min(12.0, radiusLimit);
                float thicknessSearchRadius = clamp(
                        heightPx * max(InnerRadiusRatio, 0.05) * 1.15,
                        minimumRadius,
                        radiusLimit
                );
                vec2 localField = localThicknessField(
                        texCoord, texel, thicknessSearchRadius, seamBridgePx, center.a);
                edgeDistance = localField.x;
                layerProgress = localField.y;
            }
        } else if (useV2EntitySurface) {
            layerProgress = diffusedSilhouetteProgress(
                    texCoord, texel, coolingRadius, seamBridgePx, center.a);
            edgeDistance = layerProgress * coolingRadius;
        } else {
            edgeDistance = distanceToBoundary(
                    texCoord, texel, coolingRadius, seamBridgePx, center.a);
            layerProgress = smoothstep(
                    0.0, 1.0, clamp(edgeDistance / coolingRadius, 0.0, 1.0));
        }
        float edgeProgress = ((ImprovedReliefMode != 0 && isWarmBlooded(center.a))
                || useV2EntitySurface)
                ? layerProgress
                : smoothstep(0.0, 1.0, clamp(edgeDistance / coolingRadius, 0.0, 1.0));
        float improvedRimProgress = edgeProgress;
        if ((ImprovedReliefMode != 0 && isWarmBlooded(center.a))
                || useV2EntitySurface) {
            improvedRimProgress = pow(
                    clamp(edgeProgress, 0.0, 1.0),
                    clamp(ImprovedRimCurve, 0.30, 1.50)
            );
        }
        float heat = resolveVerticalHeat(texCoord, texel, center);
        vec2 warmGeometryRelief = vec2(0.0);
        vec4 improvedSurface = vec4(surfaceFacing, 0.0, 0.0, 0.0);

        if (useV2EntitySurface && !isWarmBlooded(center.a)) {
            float reliefFade = smoothstep(0.08, 0.30, edgeProgress);
            if (reliefFade > 0.001) {
                improvedSurface = improvedCrysisSurface(
                        texCoord, texel, center.a, heightPx, surfaceFacing);
                improvedSurface.yzw *= reliefFade;
            }
        }

        if (isWarmBlooded(center.a)) {
            float edgeEmissivity = mix(0.50, 0.58, 1.0 - surfaceCold);
            float silhouetteEmissivity = mix(edgeEmissivity, 1.0, edgeProgress);
            // Internal depth curvature is not a silhouette: a nose, arm seam,
            // or other protrusion should retain the face/body heat and only
            // receive a soft local emissivity loss. Keeping this contribution
            // shallow prevents a full green-to-red contour around every cube.
            float internalEdgeEmissivity = mix(0.925, 0.955, 1.0 - surfaceCold);
            float reliefFade = ImprovedReliefMode != 0
                    ? smoothstep(0.08, 0.30, edgeProgress)
                    : smoothstep(0.08, 0.30, layerProgress);
            if (ImprovedReliefMode != 0 && reliefFade > 0.001) {
                improvedSurface = improvedCrysisSurface(
                        texCoord, texel, center.a, heightPx, surfaceFacing);
                improvedSurface.yzw *= reliefFade;
            } else if (GeometryDepthStrength > 0.001 && reliefFade > 0.001) {
                warmGeometryRelief = signedGeometryRelief(
                        texCoord, texel, center.a, heightPx) * reliefFade;
            }
            float geometryEmissivity = 1.0;
            if (ImprovedReliefMode != 0) {
                // The palette carries the stylized Crysis relief. Physical heat
                // loses only a little energy so a fold cannot change class.
                float detailCooling = improvedSurface.y
                        * clamp(ImprovedProtrusionCooling, 0.0, 0.50) * 0.08;
                geometryEmissivity = 1.0 - clamp(detailCooling, 0.0, 0.08);
                heat += improvedSurface.z
                        * clamp(ImprovedRecessCooling, 0.0, 0.40) * 0.018;
            } else {
                float reliefStrength = clamp(GeometryDepthStrength, 0.0, 2.0);
                float protrusionCooling = clamp(
                        warmGeometryRelief.x * reliefStrength * 0.55, 0.0, 0.90);
                geometryEmissivity = mix(1.0, internalEdgeEmissivity, protrusionCooling);
            }
            float emissivity = min(silhouetteEmissivity, geometryEmissivity);
            heat = max(0.0, heat * emissivity - (1.0 - edgeProgress) * 0.012);
            if (ImprovedReliefMode == 0) {
                heat += warmGeometryRelief.y
                        * clamp(GeometryDepthStrength, 0.0, 2.0) * 0.015;
            }
        } else if (isHot(center.a) || isHotEmitter(center.a) || isHotBlock(center.a)) {
            float internalEdge = isHotBlock(center.a) ? 0.0 : internalGeometryEdge(texCoord, texel, center.a);
            heat = mix(heat * 0.88, heat, edgeProgress) * mix(1.0, 0.90, internalEdge);
        } else {
            float internalEdge = internalGeometryEdge(texCoord, texel, center.a);
            heat = max(0.0, heat - (1.0 - edgeProgress) * 0.16 - internalEdge * 0.08);
        }

        float constrainedHeat = constrainClassHeat(heat, center.a);
        float insideData = 1.0;
        float warmLayerCoordinate = 0.0;
        if (isWarmBlooded(center.a)) {
            // Heat remains responsible for biological temperature and surface
            // intensity. Palette selection follows silhouette-to-center depth,
            // so widening a color pushes its neighbors instead of repainting
            // the entire warm model with the last enlarged interval.
            float classHeat = clamp((constrainedHeat - 0.46) / 0.52, 0.0, 1.0);
            float centerReach = mix(0.78, 0.96, smoothstep(0.10, 0.95, classHeat));
            float reliefStrength = clamp(GeometryDepthStrength, 0.0, 2.0);
            if (ImprovedReliefMode != 0) {
                // Crysis-style surface model: biological heat and diffused
                // silhouette distance define the stable plateau. View incidence
                // is only a mild continuous modifier. Convex flanks cool within
                // the WARM palette, while a concavity gains heat without being
                // promoted to a second green/blue silhouette.
                float faceOn = smoothstep(0.08, 0.96, improvedSurface.x);
                float flankFactor = mix(
                        0.25,
                        1.0,
                        pow(clamp(1.0 - faceOn, 0.0, 1.0), 0.62)
                );
                float interiorCoordinate;
                if (ImprovedReliefMode == 2) {
                    // Ordinary V2 interior is a white plateau that still shows
                    // the red sensor rows. Only a coherent, face-on, low-relief
                    // interior region receives the positive boost into the
                    // extreme white-hot interval where rows disappear.
                    float ordinaryPlateau = clamp(
                            0.940
                                    + (clamp(ImprovedCoreHeat, 0.55, 0.98) - 0.79) * 0.12
                                    + (classHeat - 0.62) * 0.020,
                            0.925,
                            0.955
                    );
                    float facePeak = smoothstep(0.72, 0.985, faceOn);
                    float reliefPeakLoss = smoothstep(
                            0.08,
                            0.62,
                            max(improvedSurface.y, improvedSurface.z)
                    );
                    float biologicalSupport = mix(
                            0.72,
                            1.0,
                            smoothstep(0.28, 0.90, classHeat)
                    );
                    float centralHotPeak = clamp(
                            v2InteriorPeak * facePeak
                                    * (1.0 - reliefPeakLoss) * biologicalSupport,
                            0.0,
                            1.0
                    );
                    float facingCooling = pow(clamp(1.0 - faceOn, 0.0, 1.0), 0.72)
                            * (0.12 + clamp(ImprovedFacingStrength, 0.0, 0.55) * 0.95);
                    float materialCooling = surfaceCold * 0.035;
                    float protrusionCooling = improvedSurface.y
                            * (0.04 + clamp(ImprovedProtrusionCooling, 0.0, 0.50) * 1.20)
                            * flankFactor;
                    float recessCooling = improvedSurface.z
                            * (0.025 + clamp(ImprovedRecessCooling, 0.0, 0.40) * 0.85);
                    interiorCoordinate = clamp(
                            ordinaryPlateau + centralHotPeak * 0.045
                                    - facingCooling - materialCooling
                                    - protrusionCooling - recessCooling,
                            0.08,
                            0.985
                    );
                } else {
                    float heatBase = clamp(ImprovedCoreHeat, 0.55, 0.98)
                            + (classHeat - 0.62) * 0.20;
                    float facingShift = (faceOn - 0.68)
                            * clamp(ImprovedFacingStrength, 0.0, 0.55) * 0.36;
                    float materialShift = -surfaceCold * 0.012;
                    float protrusionShift = improvedSurface.y
                            * clamp(ImprovedProtrusionCooling, 0.0, 0.50)
                            * flankFactor;
                    float recessShift = improvedSurface.z
                            * clamp(ImprovedRecessCooling, 0.0, 0.40);
                    interiorCoordinate = clamp(
                            heatBase + facingShift + materialShift
                                    - protrusionShift + recessShift,
                            0.08,
                            0.985
                    );
                }
                float silhouetteCoordinate = mix(0.025, 0.085, faceOn);
                warmLayerCoordinate = clamp(
                        mix(silhouetteCoordinate, interiorCoordinate, improvedRimProgress),
                        0.0,
                        0.985
                );
            } else {
                float reliefShift = warmGeometryRelief.x * reliefStrength * 0.24
                        - warmGeometryRelief.y * reliefStrength * 0.10;
                warmLayerCoordinate = clamp(
                        layerProgress * centerReach - reliefShift,
                        0.0,
                        0.985
                );
            }
            // Preserve the existing g > 0.5 inside-mask contract while using
            // the remaining range to carry the WARM spatial coordinate.
            insideData = 0.51 + warmLayerCoordinate * 0.48;
        } else if (useV2EntitySurface) {
            // V2 uses the same spatial surface model for every living thermal
            // class. The class heat band still defines the actual colors, so a
            // skeleton cannot become white-hot merely because it has a broad,
            // face-on surface.
            vec2 classBand = heatBand(center.a);
            float classSpan = max(classBand.y - classBand.x, 0.001);
            float classHeat = clamp(
                    (constrainedHeat - classBand.x) / classSpan,
                    0.0,
                    1.0
            );
            float faceOn = smoothstep(0.08, 0.96, improvedSurface.x);
            float facePeak = smoothstep(0.72, 0.985, faceOn);
            float reliefPeakLoss = smoothstep(
                    0.08,
                    0.62,
                    max(improvedSurface.y, improvedSurface.z)
            );
            float centralHotPeak = clamp(
                    v2InteriorPeak * facePeak * (1.0 - reliefPeakLoss),
                    0.0,
                    1.0
            );
            // Keep the ordinary surface below the peak interval. This makes
            // scanline-free regions emerge from coherent surface heat instead
            // of suppressing rows across an entire HOT/HOT_EMITTER model.
            float ordinaryClassPosition = mix(
                    0.66,
                    0.84,
                    smoothstep(0.08, 0.96, classHeat)
            );
            float facingCooling = pow(clamp(1.0 - faceOn, 0.0, 1.0), 0.72)
                    * (0.10 + clamp(ImprovedFacingStrength, 0.0, 0.55) * 0.72);
            float materialCooling = surfaceCold * 0.035;
            float protrusionCooling = improvedSurface.y
                    * (0.06 + clamp(ImprovedProtrusionCooling, 0.0, 0.50) * 0.90);
            float recessCooling = improvedSurface.z
                    * (0.035 + clamp(ImprovedRecessCooling, 0.0, 0.40) * 0.62);
            float interiorClassPosition = clamp(
                    ordinaryClassPosition + centralHotPeak * 0.14
                            - facingCooling - materialCooling
                            - protrusionCooling - recessCooling,
                    0.04,
                    0.985
            );
            float silhouetteClassPosition = mix(0.02, 0.07, faceOn);
            float spatialClassPosition = clamp(
                    mix(
                            silhouetteClassPosition,
                            interiorClassPosition,
                            improvedRimProgress
                    ),
                    0.0,
                    0.985
            );
            float classLayerCoordinate = mix(
                    classBand.x,
                    classBand.y,
                    spatialClassPosition
            );
            insideData = 0.51 + classLayerCoordinate * 0.48;
        }

        if (DebugGeometryReliefOnly != 0 && isWarmBlooded(center.a)) {
            // Carry the final unified WARM coordinate directly. The composite
            // can now show the exact geometry-only green-to-white result rather
            // than a technical blue/orange signed-depth visualization.
            fragColor = vec4(warmLayerCoordinate, insideData, center.b, center.a);
            return;
        }

        fragColor = vec4(constrainedHeat, insideData, center.b, center.a);
        return;
    }

    fragColor = vec4(0.0);
}
