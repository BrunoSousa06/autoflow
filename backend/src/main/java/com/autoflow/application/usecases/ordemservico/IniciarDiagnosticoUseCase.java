package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class IniciarDiagnosticoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;
    private final HistoricoStatusOsGateway historicoStatusOsGateway;
    private final OrdemServicoAccessPolicy accessPolicy;

    @Transactional
    public OrdemServicoEntity execute(String numeroOs, String emailUsuarioLogado) {
        OrdemServicoEntity ordemServico = buscar(numeroOs);
        UsuarioEntity usuario = usuarioGateway.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário autenticado não encontrado."));
        if (!RoleEnum.ADMIN.equals(usuario.getRole())) {
            accessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuario);
        }
        ordemServico.iniciarDiagnostico();
        OrdemServicoEntity salva = ordemServicoGateway.save(ordemServico);
        historicoStatusOsGateway.save(HistoricoStatusOsEntity.criar(
                salva.getId(), salva.getStatus(), StatusOrdemServicoMensagemPolicy.mensagem(salva.getStatus()), salva.getNumeroOs()));
        return salva;
    }

    private OrdemServicoEntity buscar(String numeroOs) {
        return ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada."));
    }
}
