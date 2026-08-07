package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.application.gateway.CurrentUserGateway;
import com.autoflow.application.dto.security.CurrentUser;
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
    private final CurrentUserGateway currentUserGateway;

    @Transactional
    public UsuarioOutput execute(RegistroInput request) {
        CurrentUser caller = currentUserGateway.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário não autenticado"
                ));

        RoleEnum callerRole = caller.role();

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
