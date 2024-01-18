//
// Created by Artem on 17.12.2023.
//

#include <GLES2/gl2.h>
#include "shader/shader.h"


Shader::Shader(const GLchar *vertSource, const GLchar *fragSource)
    : valid(false), program(0) {
    GLuint vertShader;
    if (!compileShader(&vertShader, GL_VERTEX_SHADER, vertSource)) {
        return;
    }

    GLuint fragShader;
    if (!compileShader(&fragShader, GL_FRAGMENT_SHADER, fragSource)) {
        return;
    }

    program = glCreateProgram();
    glAttachShader(program, vertShader);
    glAttachShader(program, fragShader);

    {
        GLint status;
        glLinkProgram(program);
        glGetProgramiv(program, GL_LINK_STATUS, &status);
        if (status == 0) {
            fprintf(stderr, "Program failed to link\n");
            glDeleteShader(vertShader);
            vertShader = 0;
            glDeleteShader(fragShader);
            fragShader = 0;
            glDeleteProgram(program);
            program = 0;
            return;
        }
    }

    {
        GLint status;
        glValidateProgram(program);
        glGetProgramiv(program, GL_VALIDATE_STATUS, &status);
        if (status == 0) {
            glDeleteShader(vertShader);
            vertShader = 0;
            glDeleteShader(fragShader);
            fragShader = 0;
            glDeleteProgram(program);
            program = 0;
        }
    }

    // Remove the compiled shaders; they are now part of the program.
    glDetachShader(program, vertShader);
    glDeleteShader(vertShader);
    glDetachShader(program, fragShader);
    glDeleteShader(fragShader);

    valid = true;
}

void Shader::setMatrix(const std::array<float, 16> &newMatrix) {
    if (matrix != newMatrix) {
        glUniformMatrix4fv(u_matrix, 1, GL_FALSE, newMatrix.data());
        matrix = newMatrix;
    }
}

bool Shader::compileShader(uint32_t *shader, uint32_t type, const char *source) {
    GLint status;
    *shader = glCreateShader(type);
    const GLchar *strings[] = { source };
    const GLint lengths[] = { (GLint)strlen(source) };
    glShaderSource(*shader, 1, strings, lengths);
    glCompileShader(*shader);
    glGetShaderiv(*shader, GL_COMPILE_STATUS, &status);
    if (status == 0) {
        glDeleteShader(*shader);
        *shader = 0;
        return false;
    }

    return true;
}

Shader::~Shader() {
    if (program) {
        glDeleteProgram(program);
        program = 0;
        valid = false;
    }
}
