package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ConsultarOrcamentoAutenticadoUseCase {
    private final OrcamentoGateway orcamentoGateway;
    private final UsuarioGateway usuarioGateway;

    public OrcamentoEntity execute(Long id, String email) {
        OrcamentoEntity orcamento = orcamentoGateway.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orçamento não encontrado"));
        var usuario = usuarioGateway.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário autenticado não encontrado"));
        if (usuario.getRole() == RoleEnum.CLIENTE && !email.equals(orcamento.getCliente().getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return orcamento;
    }
}
