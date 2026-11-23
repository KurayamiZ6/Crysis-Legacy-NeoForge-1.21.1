#version 150

uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 tint;
out vec4 fragColor;

void main() {
    float opacity = texture(Sampler0, texCoord).a;
    if (opacity < 0.1) discard;
    fragColor = vec4(tint.rgb, tint.a * opacity);
}
