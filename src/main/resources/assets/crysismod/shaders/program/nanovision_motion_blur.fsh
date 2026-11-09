#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;

uniform vec2 InSize;
uniform vec2 MotionDirectionPx;
uniform float RadialMotionPx;
uniform float MotionAmount;

out vec4 fragColor;

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);
    float amount = clamp(MotionAmount, 0.0, 1.0);
    if (amount < 0.001) {
        fragColor = center;
        return;
    }

    vec2 radialDirection = (texCoord - vec2(0.5)) * RadialMotionPx;
    vec2 offset = (MotionDirectionPx + radialDirection) / max(InSize, vec2(1.0));
    vec4 trailA = texture(DiffuseSampler, clamp(texCoord + offset, vec2(0.0), vec2(1.0)));
    vec4 trailB = texture(DiffuseSampler, clamp(texCoord - offset * 0.65, vec2(0.0), vec2(1.0)));
    vec4 blurred = center * 0.56 + trailA * 0.24 + trailB * 0.20;
    fragColor = mix(center, blurred, amount);
}
