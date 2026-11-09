#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord0;
out vec3 viewNormal;
out vec3 viewPosition;

void main() {
    vec4 viewPos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos;
    texCoord0 = UV0;
    viewNormal = normalize(mat3(ModelViewMat) * Normal);
    viewPosition = viewPos.xyz;
}
