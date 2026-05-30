package com.autoflow.controller.usuario.response;

import com.autoflow.domain.usuario.RoleEnum;


public record UsuarioResponse(Long id,
                              String nome,
                              String email,
                              String cpfCnpj,
                              String telefone,
                              RoleEnum role) {
}
