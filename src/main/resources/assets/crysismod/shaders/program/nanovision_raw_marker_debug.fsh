#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D RawMarkerSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 source = texture(DiffuseSampler, texCoord).rgb;
    float grayscale = dot(source, vec3(0.299, 0.587, 0.114));
    vec4 rawMarker = texture(RawMarkerSampler, texCoord);

    if (rawMarker.a >= 0.1) {
        fragColor = vec4(0.02, 1.0, 0.0, 1.0);
        return;
    }

    fragColor = vec4(vec3(grayscale), 1.0);
}
