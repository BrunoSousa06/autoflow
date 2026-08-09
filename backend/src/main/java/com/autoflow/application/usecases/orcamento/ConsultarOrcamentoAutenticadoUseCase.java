package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ConsultarOrcamentoAutenticadoUseCase {
    private final OrcamentoGateway orcamentoGateway;
    private final UsuarioGateway usuarioGateway;

    public OrcamentoEntity execute(Long id, String email) {
        OrcamentoEntity orcamento = orcamentoGateway.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Orçamento não encontrado"));
        var usuario = usuarioGateway.findByEmail(email)
                .orElseThrow(() -> ApplicationException.notFound("Usuário autenticado não encontrado"));
        if (usuario.getRole() == RoleEnum.CLIENTE && !email.equals(orcamento.getCliente().getEmail())) {
            throw ApplicationException.forbidden();
        }
        return orcamento;
    }
}
