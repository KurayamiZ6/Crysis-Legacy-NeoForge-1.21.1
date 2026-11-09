#version 150

uniform sampler2D Sampler0;
in vec2 texCoord0;
in vec3 viewNormal;
in vec3 viewPosition;

out vec4 fragColor;

float heatHash(vec2 cell) {
    return fract(sin(dot(cell, vec2(127.1, 311.7))) * 43758.5453);
}

float heatNoise(vec2 point) {
    vec2 cell = floor(point);
    vec2 local = fract(point);
    vec2 blend = local * local * (3.0 - 2.0 * local);
    float bottom = mix(heatHash(cell), heatHash(cell + vec2(1.0, 0.0)), blend.x);
    float top = mix(heatHash(cell + vec2(0.0, 1.0)), heatHash(cell + vec2(1.0)), blend.x);
    return mix(bottom, top, blend.y);
}

void main() {
    vec4 texel = texture(Sampler0, texCoord0);
    if (texel.a < 0.1) {
        discard;
    }

    vec3 normal = normalize(viewNormal);
    vec3 viewDirection = normalize(-viewPosition);
    float facing = clamp(abs(dot(normal, viewDirection)), 0.0, 1.0);
    float luminance = dot(texel.rgb, vec3(0.299, 0.587, 0.114));
    float broadNoise = heatNoise(texCoord0 * 7.0 + vec2(2.7, 5.1)) - 0.5;
    float mediumNoise = heatNoise(texCoord0 * 17.0 + vec2(11.3, 3.9)) - 0.5;
    float variation = broadNoise * 0.0792 + mediumNoise * 0.0308;
    float heatDetail = clamp((luminance - 0.5) * 0.18 + variation, -0.12, 0.12);

    // R: signed material heat detail, G: source luminance, B: view-facing data,
    // A: exact arm mask. The screen-space composite reconstructs the palette.
    fragColor = vec4(heatDetail * 0.5 + 0.5, luminance, facing, texel.a);
}
