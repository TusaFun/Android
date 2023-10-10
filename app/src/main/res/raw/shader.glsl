#ifdef GL_ES
precision mediump float;
#endif

#define PI 3.14159265359
#define TWO_PI 6.28318530718

uniform vec2 u_resolution;
uniform vec2 u_mouse;
uniform float u_time;

float random (in vec2 st) {
    return fract(sin(dot(st.xy,
                         vec2(12.9898,78.233)))
                 * 43758.5453123);
}

float noise (in vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);

    // Four corners in 2D of a tile
    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));

    // Smooth Interpolation

    // Cubic Hermine Curve.  Same as SmoothStep()
    vec2 u = f*f*(3.0-2.0*f);
    // u = smoothstep(0.,1.,f);

    // Mix 4 coorners percentages
    return mix(a, b, u.x) +
            (c - a)* u.y * (1.0 - u.x) +
            (d - b) * u.x * u.y;
}


float circle(vec2 st, vec2 loc, float r) {
    vec2 pos = st - loc;
    return step(length(pos), r);
}

float box(vec2 _st, vec2 _size){
    _size = vec2(0.5)-_size*0.5;
    vec2 uv = smoothstep(_size,_size+vec2(0.01),_st);
    uv *= smoothstep(_size,_size+vec2(0.01),vec2(1.0)-_st);
    return uv.x*uv.y;
}

void main() {
    vec2 st = gl_FragCoord.xy / u_resolution.xy;
    st.x *= u_resolution.x / u_resolution.y;
    vec2 pos = vec2(st * 5. * u_mouse.x);
    float n = noise(pos);

    st += vec2(0.35, 0.);
    float box1 = box(st, vec2(0.2, 0.8));
    box1 = mix(0., n * box1 / 2., box1);

    st -= vec2(0.25, 0.);
    float box2 = box(st, vec2(0.2, 0.8));

    st -= vec2(0.25, 0.);
    float box3 = box(st, vec2(0.2, 0.8));

    vec3 background = vec3(0.1451, 0.0039, 0.0039);
    vec3 color = box1 + box2 + box3 + background;

    gl_FragColor = vec4(color, 1.0);
}
