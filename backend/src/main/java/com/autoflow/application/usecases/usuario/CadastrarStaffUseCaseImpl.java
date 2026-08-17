package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.security.CurrentUser;
import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.CurrentUserGateway;
import com.autoflow.application.port.in.usuario.CadastrarStaffUseCase;
import com.autoflow.application.port.in.usuario.CadastrarUsuarioUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.usuario.RoleEnum;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CadastrarStaffUseCaseImpl implements CadastrarStaffUseCase {

    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;
    private final CurrentUserGateway currentUserGateway;

    @TransactionalUseCase
    @Override
    public UsuarioOutput execute(RegistroInput request) {
        CurrentUser caller = currentUserGateway.getCurrentUser()
                .orElseThrow(() -> ApplicationException.unauthorized(
                        "Usuário não autenticado"
                ));

        RoleEnum callerRole = caller.role();

        if (RoleEnum.ATENDENTE.equals(callerRole)
                && (RoleEnum.ADMIN.equals(request.role())
                || RoleEnum.MECANICO.equals(request.role()))) {

            throw ApplicationException.forbidden(
                    "Atendente não pode cadastrar usuários com esta função");
        }

        return cadastrarUsuarioUseCase.execute(request);
    }
}
