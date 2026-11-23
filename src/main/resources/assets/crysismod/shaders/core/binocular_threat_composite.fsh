#version 150

uniform sampler2D Mask;
uniform sampler2D MaskDepth;
uniform sampler2D SceneDepth;
uniform int DrawContour;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 surface = texture(Mask, texCoord);
    if (surface.a <= 0.0 || texture(MaskDepth, texCoord).r > texture(SceneDepth, texCoord).r + 0.00001) discard;

    // N2 uses a fine contour inside the visible silhouette. N1 already has its own outline.
    if (DrawContour != 0) {
        float edge = min(min(textureOffset(Mask, texCoord, ivec2(-1, 0)).a, textureOffset(Mask, texCoord, ivec2(1, 0)).a),
                         min(textureOffset(Mask, texCoord, ivec2(0, -1)).a, textureOffset(Mask, texCoord, ivec2(0, 1)).a));
        if (edge == 0.0) surface.a = min(0.64, surface.a * 3.2);
    }
    fragColor = surface;
}
