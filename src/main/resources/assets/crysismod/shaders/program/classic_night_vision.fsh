#version 150

uniform sampler2D DiffuseSampler;
uniform float Intensity;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 source = texture(DiffuseSampler, texCoord).rgb;
    float luminance = dot(source, vec3(0.2126, 0.7152, 0.0722));
    float lifted = pow(luminance, 0.76);
    vec3 green = vec3(lifted * 0.63, lifted * 1.04, lifted * 0.67);
    // Both the green tint and existing luminance amplification follow the same easing.
    fragColor = vec4(mix(source, clamp(mix(source, green, 0.78), 0.0, 1.0), clamp(Intensity, 0.0, 1.0)), 1.0);
}
