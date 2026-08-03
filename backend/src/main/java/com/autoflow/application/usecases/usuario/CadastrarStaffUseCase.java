package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.domain.usuario.RoleEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CadastrarStaffUseCase {

    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;

    @Transactional
    public UsuarioOutput execute(
            RegistroInput request,
            RoleEnum callerRole) {

        if (RoleEnum.ATENDENTE.equals(callerRole)
                && (RoleEnum.ADMIN.equals(request.role())
                || RoleEnum.MECANICO.equals(request.role()))) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Atendente não pode cadastrar usuários com esta função");
        }

        return cadastrarUsuarioUseCase.execute(request);
    }
}
