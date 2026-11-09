#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord0;
out vec2 projectedEntitySize;
out vec2 thermalProfileData;
out vec3 viewNormal;
out float thermalEmissionClass;

void main() {
    vec4 viewPosition = ModelViewMat * vec4(Position, 1.0);
    if ((UV1.x & 2) != 0) {
        // Match entityCutoutNoCullZOffset's VIEW_OFFSET_Z_LAYERING.
        viewPosition.xyz *= 0.99975586;
    }
    gl_Position = ProjMat * viewPosition;
    if ((UV1.x & 1) != 0) {
        // Complementary FLICKERING_FIX for its shared slime/chicken ID.
        gl_Position.z -= 0.00015;
    }
    texCoord0 = UV0;
    projectedEntitySize = vec2(Color.r, 0.0);
    thermalProfileData = Color.gb;
    thermalEmissionClass = Color.a;
    viewNormal = normalize((ModelViewMat * vec4(Normal, 0.0)).xyz);
}
