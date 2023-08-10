
attribute vec4 a_Position;
uniform mat4 u_MVPMatrix;
attribute vec2 a_TexCoord;

varying vec2 v_TexCoord;

void main() {
    v_TexCoord = a_TexCoord;

    glP_Position = u_MVPMatrix * a_Position;
}