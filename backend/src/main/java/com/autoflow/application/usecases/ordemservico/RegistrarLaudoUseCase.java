package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class RegistrarLaudoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;
    private final OrdemServicoAccessPolicy accessPolicy;

    @Transactional
    public OrdemServicoEntity execute(String numeroOs, String emailUsuarioLogado, String laudo) {
        OrdemServicoEntity ordemServico = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada."));
        UsuarioEntity usuario = usuarioGateway.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário autenticado não encontrado."));
        accessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuario);
        ordemServico.registrarLaudo(laudo);
        return ordemServicoGateway.save(ordemServico);
    }
}
