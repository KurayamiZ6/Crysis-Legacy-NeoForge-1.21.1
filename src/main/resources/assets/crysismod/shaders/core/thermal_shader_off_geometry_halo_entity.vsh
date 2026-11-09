#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 Viewport;
uniform float HaloRadiusPx;

out vec2 texCoord0;

void main() {
    vec4 viewPosition = ModelViewMat * vec4(Position, 1.0);
    vec4 clipPosition = ProjMat * viewPosition;
    vec3 viewNormal = normalize(mat3(ModelViewMat) * Normal);
    vec2 screenNormal = (ProjMat * vec4(viewNormal, 0.0)).xy;
    float normalLength = length(screenNormal);
    if (normalLength < 0.00001) {
        screenNormal = clipPosition.xy;
        normalLength = max(length(screenNormal), 0.00001);
    }
    clipPosition.xy += screenNormal / normalLength
            * (HaloRadiusPx * 2.0 / max(Viewport, vec2(1.0))) * clipPosition.w;
    gl_Position = clipPosition;
    texCoord0 = UV0;
}
