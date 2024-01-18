//
// Created by Artem on 17.12.2023.
//

#ifndef TUSA_PLAIN_SHADER_H
#define TUSA_PLAIN_SHADER_H

#include "shader.h"
#include <GLES2/gl2.h>

class PlainShader : public Shader {
public:
    PlainShader(const char* vertex, const char* fragment);
    void bind(char *offset);
    void setColor(float r, float g, float b, float a);
    void setColor(const std::array<float, 4>& color);

private:
    std::array<float, 4> color = {{}};
    GLint a_pos;
    GLint u_color;
};


#endif //TUSA_PLAIN_SHADER_H
