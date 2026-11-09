#version 150

uniform sampler2D MaskSampler;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    if (texture(MaskSampler, texCoord0).a < 0.5) {
        discard;
    }
    fragColor = vec4(1.0);
}
