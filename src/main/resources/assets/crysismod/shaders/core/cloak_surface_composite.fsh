#version 150

uniform sampler2D SceneSampler;
uniform sampler2D SceneDepthSampler;
uniform sampler2D MaskSampler;
uniform sampler2D MaskDepthSampler;
uniform int CompareDepth;
uniform float CloakTime;

in vec2 texCoord;

out vec4 fragColor;

vec4 visibleSurface(vec2 uv) {
    vec4 surface = texture(MaskSampler, uv);
    if (surface.a > 0.0 && CompareDepth != 0
        && texture(MaskDepthSampler, uv).r > texture(SceneDepthSampler, uv).r + 0.00001) {
        surface.a = 0.0;
    }
    return surface;
}

void main() {
    vec4 surface = visibleSurface(texCoord);
    if (surface.a < 0.5) {
        discard;
    }

    vec2 resolution = vec2(textureSize(MaskSampler, 0));
    vec2 pixel = 1.0 / resolution;
    vec4 left = visibleSurface(texCoord - vec2(pixel.x, 0.0));
    vec4 right = visibleSurface(texCoord + vec2(pixel.x, 0.0));
    vec4 down = visibleSurface(texCoord - vec2(0.0, pixel.y));
    vec4 up = visibleSurface(texCoord + vec2(0.0, pixel.y));
    float rim = 1.0 - min(min(left.a, right.a), min(down.a, up.a));
    float crease = max(max(length(surface.rgb - left.rgb) * left.a, length(surface.rgb - right.rgb) * right.a),
                       max(length(surface.rgb - down.rgb) * down.a, length(surface.rgb - up.rgb) * up.a));
    crease = smoothstep(0.12, 0.65, crease);

    vec3 n = surface.rgb * 2.0 - 1.0;
    float grazing = 1.0 - abs(n.z);
    float sheen = 0.82 + 0.18 * sin(CloakTime * 1.9 + texCoord.y * 31.0 + n.y * 2.0);
    vec2 shimmer = vec2(sin(CloakTime * 2.4 + texCoord.y * 57.0 + n.x),
                        cos(CloakTime * 1.7 + texCoord.x * 39.0 + n.y));
    vec2 rimDirection = vec2(left.a - right.a, down.a - up.a);
    float strength = 7.0 + rim * 2.2 + crease * 1.5;
    float pixelScale = clamp(resolution.y / 720.0, 1.0, 2.0);
    vec2 offset = n.xy * strength + rimDirection * 2.4 + shimmer * 1.55;
    vec2 refractedUV = texCoord + pixel * pixelScale * offset;
    vec3 background = texture(SceneSampler, clamp(refractedUV, pixel * 0.5, 1.0 - pixel * 0.5)).rgb;
    float highlight = sheen * (0.025 + rim * 0.31 + crease * 0.105 + grazing * grazing * 0.085);
    fragColor = vec4(mix(background, vec3(0.32, 0.73, 1.0), min(highlight, 0.40)), 1.0);
}
