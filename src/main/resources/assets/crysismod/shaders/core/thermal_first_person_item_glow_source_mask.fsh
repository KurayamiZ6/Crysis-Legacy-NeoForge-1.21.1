#version 150

uniform sampler2D ThermalSampler;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec4 thermal = texture(ThermalSampler, texCoord0);
    // The exact scope thermal target is cleared transparent.  Some render
    // types preserve alpha while others only write RGB, so accept either.
    float coverage = max(thermal.a, max(thermal.r, max(thermal.g, thermal.b)));
    if (coverage < 0.004) discard;
    fragColor = vec4(0.0, 0.0, 0.0, 1.0);
}
