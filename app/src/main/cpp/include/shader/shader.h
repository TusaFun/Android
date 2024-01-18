//
// Created by Artem on 17.12.2023.
//

#ifndef TUSA_SHADER_H
#define TUSA_SHADER_H

#include <cstdint>
#include <array>

class Shader {
public:
    Shader(const char *vertex, const char *fragment);
    ~Shader();
    bool valid;
    uint32_t program;

    void setMatrix(const std::array<float, 16>& matrix);

private:
    bool compileShader(uint32_t *shader, uint32_t type, const char *source);

protected:
    std::array<float, 16> matrix = {{}};
    int32_t u_matrix = -1;
};



#endif //TUSA_SHADER_H
