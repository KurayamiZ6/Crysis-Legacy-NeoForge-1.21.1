#version 150

uniform sampler2D DiffuseSampler;
uniform float Intensity;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 source = texture(DiffuseSampler, texCoord).rgb;
    float luminance = dot(source, vec3(0.2126, 0.7152, 0.0722));
    float lifted = pow(luminance, 0.76);
    // Yellow-green balance sampled from the supplied Remastered night-vision frames.
    vec3 green = vec3(lifted * 0.58, lifted * 1.04, lifted * 0.20);
    // Both the green tint and existing luminance amplification follow the same easing.
    float intensity = clamp(Intensity, 0.0, 1.0);
    vec3 result = mix(source, clamp(mix(source, green, 0.78), 0.0, 1.0), intensity);
    // Edge midpoints remain clear: only the distant corners fade slightly.
    float corner = smoothstep(1.02, 1.414214, length((texCoord - 0.5) * 2.0));
    fragColor = vec4(result * (1.0 - 0.32 * intensity * corner), 1.0);
}
