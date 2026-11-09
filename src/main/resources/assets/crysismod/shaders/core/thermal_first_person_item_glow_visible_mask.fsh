#version 150

uniform sampler2D PreHandDepthSampler;
uniform sampler2D FinalHandDepthSampler;
uniform sampler2D ScopeBodySampler;
uniform sampler2D PreHandColorSampler;
uniform sampler2D FinalHandColorSampler;
uniform int ScopeBodyEnabled;
uniform int UseColorDelta;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    float beforeHands = texture(PreHandDepthSampler, texCoord0).r;
    float afterHands = texture(FinalHandDepthSampler, texCoord0).r;
    float scopeBody = ScopeBodyEnabled != 0 ? texture(ScopeBodySampler, texCoord0).a : 0.0;
    vec3 beforeColor = texture(PreHandColorSampler, texCoord0).rgb;
    vec3 afterColor = texture(FinalHandColorSampler, texCoord0).rgb;
    float colorDelta = UseColorDelta != 0
            ? max(max(abs(afterColor.r - beforeColor.r), abs(afterColor.g - beforeColor.g)), abs(afterColor.b - beforeColor.b))
            : 0.0;
    // Smaller depth is closer. A tolerance rejects precision noise; ocular
    // holes retain the pre-hand depth and therefore write no silhouette.
    if (afterHands + 0.000012 >= beforeHands && scopeBody < 0.015 && colorDelta < 0.012) discard;
    fragColor = vec4(0.0, 0.0, 0.0, 1.0);
}
