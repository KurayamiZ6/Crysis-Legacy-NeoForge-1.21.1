#version 150

uniform sampler2D DepthSampler;

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    float depth = clamp(texture(DepthSampler, texCoord0).r, 0.0, 1.0);
    float encoded = floor(depth * 16777215.0 + 0.5);
    float high = floor(encoded / 65536.0);
    encoded -= high * 65536.0;
    float middle = floor(encoded / 256.0);
    float low = encoded - middle * 256.0;
    fragColor = vec4(high, middle, low, 255.0) / 255.0;
}
