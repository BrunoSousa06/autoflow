package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.domain.usuario.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CadastrarUsuarioPublicoUseCase {

    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;

    public UsuarioOutput execute(RegistroInput input) {
        if (!RoleEnum.CLIENTE.equals(input.role())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cadastro público permite apenas a role CLIENTE"
            );
        }

        return cadastrarUsuarioUseCase.execute(input);
    }
}
