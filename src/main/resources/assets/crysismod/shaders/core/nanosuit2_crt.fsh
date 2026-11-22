#version 150

uniform sampler2D HudSampler;
uniform float Time;
uniform vec3 Panel;
in vec2 texCoord;
out vec4 fragColor;

float circularDistance(float a, float b) {
    float offset = mod(abs(a - b), 1.0);
    return min(offset, 1.0 - offset);
}

float noise(float seed) {
    return fract(sin(seed * 12.9898 + 4.1414) * 43758.5453);
}

void main() {
    float y = (1.0 - texCoord.y - Panel.x) / Panel.y;
    float phase = mod(Time, 1.46) / 1.46;
    float rollback = max(0.0, 1.0 - abs(phase - .72) / .048);
    float main = 7.0 * phase - .53 * rollback;
    float echo = .62 + 6.0 * phase;
    float strength = mix(.91, 1.0, smoothstep(.105, .135, circularDistance(y, main)))
        * mix(.96, 1.0, smoothstep(.06, .09, circularDistance(y, echo)));

    // Each thin row has its own repeat interval and burst duration.
    float row = floor(y * 104.0);
    float slot = floor(row / 4.0);
    float tearRow = slot * 4.0 + floor(noise(slot + 1.0) * 3.0);
    float interval = mix(.36, .64, noise(slot + 53.0));
    float clock = Time + noise(slot + 105.0) * interval;
    float duration = mix(.09, .19, noise(slot + floor(clock / interval) * 31.0));
    float shift = row == tearRow && mod(clock, interval) < duration ? .5 * Panel.z : 0.0;
    vec4 ink = texture(HudSampler, texCoord + vec2(shift, 0.0));
    // Preserve the opaque cores and apply the rolling modulation to the translucent artwork.
    fragColor = ink * mix(strength, 1.0, smoothstep(.9, .999, ink.a));
}
