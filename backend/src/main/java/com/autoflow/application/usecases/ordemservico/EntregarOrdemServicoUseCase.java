package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class EntregarOrdemServicoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final HistoricoStatusOsGateway historicoStatusOsGateway;

    @TransactionalUseCase
    public OrdemServicoEntity execute(String numeroOs) {
        OrdemServicoEntity os = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        os.entregar();
        OrdemServicoEntity salva = ordemServicoGateway.save(os);
        historicoStatusOsGateway.save(HistoricoStatusOsEntity.criar(salva.getId(), salva.getStatus(),
                StatusOrdemServicoMensagemPolicy.mensagem(salva.getStatus()), salva.getNumeroOs()));
        return salva;
    }
}
