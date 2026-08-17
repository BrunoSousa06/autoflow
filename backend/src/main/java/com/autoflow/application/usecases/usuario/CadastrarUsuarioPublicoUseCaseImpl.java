package com.autoflow.application.usecases.usuario;

import com.autoflow.application.input.usuario.RegistroInput;
import com.autoflow.application.output.usuario.UsuarioOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.port.in.usuario.CadastrarUsuarioPublicoUseCase;
import com.autoflow.application.port.in.usuario.CadastrarUsuarioUseCase;
import com.autoflow.domain.usuario.RoleEnum;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CadastrarUsuarioPublicoUseCaseImpl implements CadastrarUsuarioPublicoUseCase {

    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;

    @Override
    public UsuarioOutput execute(RegistroInput input) {
        if (!RoleEnum.CLIENTE.equals(input.role())) {
            throw ApplicationException.forbidden("Cadastro público permite apenas a role CLIENTE");
        }

        return cadastrarUsuarioUseCase.execute(input);
    }
}
