
varying vec2 v_TexCoord;

uniform sampler2D u_Texture;

void main() {
    vec4 textureColor = texture2D(u_Texture, v_TexCoord);

    gl_FragColor = textureColor;
}