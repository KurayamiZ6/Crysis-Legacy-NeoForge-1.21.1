#version 150
uniform sampler2D ModelSampler;
uniform float Opacity;
uniform float Phase;
in vec2 uv;
out vec4 fragColor;

void main() {
    vec2 hex = abs(uv - .5);
    if (hex.y > .5 || hex.x + .5 * hex.y > .5) discard;

    float rows = uv.y * 34.0;
    float scan = smoothstep(.34, .78, fract(rows));
    float sweep = exp(-34.0 * abs(fract(uv.y + Phase * .85) - .5));
    float jitter = sin(floor(rows) * 23.8 + Phase * 19.0) * sweep * .004;
    vec2 sampleUv = vec2((uv.x - .5) * 1.16 + .5 + jitter, uv.y);
    vec4 model = texture(ModelSampler, sampleUv);

    vec2 texel = 2.0 / vec2(textureSize(ModelSampler, 0));
    float around = texture(ModelSampler, sampleUv + vec2(texel.x, 0)).a;
    around += texture(ModelSampler, sampleUv - vec2(texel.x, 0)).a;
    around += texture(ModelSampler, sampleUv + vec2(0, texel.y)).a;
    around += texture(ModelSampler, sampleUv - vec2(0, texel.y)).a;
    float glow = max(0.0, around * .25 - model.a);
    float luma = dot(model.rgb, vec3(.2126, .7152, .0722));
    float signal = (.60 + .40 * smoothstep(.04, .62, luma)) * (1.0 - .29 * scan + .17 * sweep);
    vec3 mint = mix(vec3(.30,.86,.73),vec3(.91,1.0,.96),clamp(signal,0.0,1.0));
    float alpha = model.a * (.83 - .16 * scan + .13 * sweep) + glow * .57;
    fragColor = vec4(mint + glow * vec3(.07,.18,.13), clamp(alpha,0.0,1.0) * Opacity);
}
