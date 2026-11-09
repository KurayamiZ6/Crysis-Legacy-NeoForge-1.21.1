#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MarkerDepthSampler;
uniform vec2 InSize;
uniform float EncodedHeightRangePx;
uniform int ThermalQuality;
in vec2 texCoord;
out vec4 fragColor;

float sameSource(vec4 sampleData, float sourceType) {
    return step(0.1, sampleData.a) * (1.0 - step(0.075, abs(sampleData.a - sourceType)));
}

vec2 directionFor(int i) {
    if (i == 0) return vec2(1.0, 0.0);
    if (i == 1) return vec2(0.0, 1.0);
    if (i == 2) return vec2(0.70710678, 0.70710678);
    return vec2(0.70710678, -0.70710678);
}

float bridgedSource(vec2 uv, vec2 direction, float radius, float seamBridgePx, float sourceType) {
    vec2 texel = 1.0 / InSize;
    float direct = sameSource(texture(DiffuseSampler, uv + direction * radius * texel), sourceType);
    if (direct >= 0.5 || seamBridgePx <= 0.0) {
        return direct;
    }
    return sameSource(
            texture(DiffuseSampler, uv + direction * (radius + seamBridgePx) * texel),
            sourceType
    );
}

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);
    if (center.a < 0.1) {
        fragColor = vec4(0.0);
        return;
    }
    vec2 texel = 1.0 / InSize;
    float heightPx = max(2.0, center.b * EncodedHeightRangePx);
    float seamBridgePx = clamp(heightPx * 0.006, 1.25, 3.5);
    float silhouette = 0.0;
    for (int i = 0; i < 4; i++) {
        vec2 direction = directionFor(i);
        silhouette = max(silhouette, 1.0 - bridgedSource(
                texCoord, direction, 1.25, seamBridgePx, center.a));
        silhouette = max(silhouette, 1.0 - bridgedSource(
                texCoord, -direction, 1.25, seamBridgePx, center.a));
    }

    float centerDepth = texture(MarkerDepthSampler, texCoord).r;
    float depthScale = max(1.0 - centerDepth, 0.00005);
    float internalEdge = 0.0;
    int directionLimit = ThermalQuality <= 1 ? 2 : 4;
    int radiusLimit = ThermalQuality <= 1 ? 1 : (ThermalQuality == 2 ? 2 : 3);
    for (int radiusIndex = 0; radiusIndex < 3; radiusIndex++) {
        if (radiusIndex >= radiusLimit) break;
        float radius = radiusIndex == 0 ? 1.5 : (radiusIndex == 1 ? 3.25 : 5.5);
        float threshold = max(0.0000035, depthScale * 0.0032) * (1.0 + radius * 0.30);
        for (int directionIndex = 0; directionIndex < 4; directionIndex++) {
            if (directionIndex >= directionLimit) break;
            vec2 offset = directionFor(directionIndex) * texel * radius;
            vec4 positiveData = texture(DiffuseSampler, texCoord + offset);
            vec4 negativeData = texture(DiffuseSampler, texCoord - offset);
            float valid = sameSource(positiveData, center.a) * sameSource(negativeData, center.a);
            float positiveDepth = texture(MarkerDepthSampler, texCoord + offset).r;
            float negativeDepth = texture(MarkerDepthSampler, texCoord - offset).r;
            float curvature = abs(positiveDepth + negativeDepth - centerDepth * 2.0);
            internalEdge = max(internalEdge, smoothstep(threshold, threshold * 6.0, curvature) * valid);
        }
    }
    // Internal geometry only seeds a faint softening field. The full field is
    // reserved for the true outer silhouette, so protrusions do not gain a
    // second, equally strong thermal outline.
    float edgeSeed = max(silhouette, internalEdge * 0.14);
    fragColor = vec4(edgeSeed, 1.0, center.b, center.a);
}
