#version 150

uniform sampler2D Sampler0;

in vec2 texCoord;
in vec3 viewNormal;
in float vertexAlpha;

out vec4 fragColor;

void main() {
    if (texture(Sampler0, texCoord).a * vertexAlpha < 0.1) {
        discard;
    }
    vec3 n = dot(viewNormal, viewNormal) > 0.0001 ? normalize(viewNormal) : vec3(0.0, 0.0, 1.0);
    if (!gl_FrontFacing) {
        n = -n;
    }
    fragColor = vec4(n * 0.5 + 0.5, 1.0);
}
