package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class IniciarDiagnosticoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;
    private final HistoricoStatusOsGateway historicoStatusOsGateway;
    private final OrdemServicoAccessPolicy accessPolicy;

    @TransactionalUseCase
    public OrdemServicoEntity execute(String numeroOs, String emailUsuarioLogado) {
        OrdemServicoEntity ordemServico = buscar(numeroOs);
        UsuarioEntity usuario = usuarioGateway.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> ApplicationException.notFound("Usuário autenticado não encontrado."));
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
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
    }
}
