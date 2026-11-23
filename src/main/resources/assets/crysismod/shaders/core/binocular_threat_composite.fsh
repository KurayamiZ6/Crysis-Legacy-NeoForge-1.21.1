#version 150

uniform sampler2D Mask;
uniform sampler2D MaskDepth;
uniform sampler2D SceneDepth;
uniform int ClassicContour;
in vec2 texCoord;
out vec4 fragColor;

const float depthTolerance = 0.00001;
const ivec2 neighbors[4] = ivec2[4](ivec2(-1, 0), ivec2(1, 0), ivec2(0, -1), ivec2(0, 1));

vec4 classicThreat(vec4 surface, float sceneDepth) {
    ivec2 pixel = ivec2(gl_FragCoord.xy);
    ivec2 lastPixel = textureSize(Mask, 0) - 1;
    bool inside = surface.a > 0.0;
    bool edge = false;
    vec4 glow = vec4(0.0);
    float glowDepth = 1.0;
    for (int i = 0; i < 4; ++i) {
        ivec2 adjacent = clamp(pixel + neighbors[i], ivec2(0), lastPixel);
        vec4 sampleColor = texelFetch(Mask, adjacent, 0);
        if (sampleColor.a > 0.0) {
            float sampleDepth = texelFetch(MaskDepth, adjacent, 0).r;
            float adjacentSceneDepth = texelFetch(SceneDepth, adjacent, 0).r;
            if (sampleDepth <= adjacentSceneDepth + depthTolerance) {
                if (!inside && sampleDepth <= sceneDepth + depthTolerance && sampleDepth <= glowDepth) {
                    glow = vec4(sampleColor.rgb, 0.22);
                    glowDepth = sampleDepth;
                }
                continue;
            }
        }
        edge = true;
    }
    if (inside && edge) return vec4(mix(surface.rgb, vec3(1.0), 0.29), 0.94);
    return inside ? surface : glow;
}

void main() {
    vec4 surface = texture(Mask, texCoord);
    float sceneDepth = texture(SceneDepth, texCoord).r;
    if (surface.a > 0.0 && texture(MaskDepth, texCoord).r > sceneDepth + depthTolerance) discard;

    if (ClassicContour != 0) {
        fragColor = classicThreat(surface, sceneDepth);
        if (fragColor.a <= 0.0) discard;
        return;
    }

    if (surface.a <= 0.0) discard;
    float edge = min(min(textureOffset(Mask, texCoord, ivec2(-1, 0)).a, textureOffset(Mask, texCoord, ivec2(1, 0)).a),
                     min(textureOffset(Mask, texCoord, ivec2(0, -1)).a, textureOffset(Mask, texCoord, ivec2(0, 1)).a));
    if (edge == 0.0) surface.a = min(0.64, surface.a * 3.2);
    fragColor = surface;
}
