package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.domain.usuario.RoleEnum;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CadastrarUsuarioPublicoUseCase {

    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;

    public UsuarioOutput execute(RegistroInput input) {
        if (!RoleEnum.CLIENTE.equals(input.role())) {
            throw ApplicationException.forbidden("Cadastro público permite apenas a role CLIENTE");
        }

        return cadastrarUsuarioUseCase.execute(input);
    }
}
