//
// Created by Artem on 17.12.2023.
//

#include <GLES2/gl2.h>
#include "shader/plain_shader.h"
#include "shader/shaders_bucket.h"

PlainShader::PlainShader(const char* vertex, const char* fragment)
    : Shader(vertex, fragment) {
    if (!valid) {
        fprintf(stderr, "invalid plain shader\n");
        return;
    }
    a_pos = glGetAttribLocation(program, "a_pos");
    u_matrix = glGetUniformLocation(program, "u_matrix");
    u_color = glGetUniformLocation(program, "u_color");
}

void PlainShader::bind(char *offset) {
    glEnableVertexAttribArray(a_pos);
    glVertexAttribPointer(a_pos, 2, GL_SHORT, false, 0, offset);
}

void PlainShader::setColor(float r, float g, float b, float a) {
    setColor({{ r, g, b, a }});
}

void PlainShader::setColor(const std::array<float, 4> &new_color) {
    if (color != new_color) {
        glUniform4fv(u_color, 1, new_color.data());
        color = new_color;
    }
}
