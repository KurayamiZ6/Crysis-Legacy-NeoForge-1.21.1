#version 150

uniform sampler2D Sampler0;
uniform sampler2D LeftArmMaskSampler;
uniform sampler2D LeftArmDepthSampler;
uniform sampler2D RightArmMaskSampler;
uniform sampler2D RightArmDepthSampler;
uniform vec2 InSize;
uniform int LeftArmEnabled;
uniform int RightArmEnabled;

in vec2 texCoord0;
out vec4 fragColor;

bool armCoversItem(vec2 screenUv, float itemDepth) {
    if (LeftArmEnabled != 0 && texture(LeftArmMaskSampler, screenUv).a > 0.06) {
        if (texture(LeftArmDepthSampler, screenUv).r + 0.000002 < itemDepth) return true;
    }
    if (RightArmEnabled != 0 && texture(RightArmMaskSampler, screenUv).a > 0.06) {
        if (texture(RightArmDepthSampler, screenUv).r + 0.000002 < itemDepth) return true;
    }
    return false;
}

void main() {
    float alpha = texture(Sampler0, texCoord0).a;
    if (alpha < 0.10) discard;
    if (armCoversItem(gl_FragCoord.xy / max(InSize, vec2(1.0)), gl_FragCoord.z)) discard;
    // Only alpha matters: this target is a geometry silhouette for the
    // separate full-screen blue glow composite.
    fragColor = vec4(0.0, 0.0, 0.0, alpha);
}
