package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.application.port.in.ordemservico.EntregarOrdemServicoUseCase;
import com.autoflow.domain.ordemservico.HistoricoStatusOs;
import com.autoflow.domain.ordemservico.OrdemServico;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class EntregarOrdemServicoUseCaseImpl implements EntregarOrdemServicoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final HistoricoStatusOsGateway historicoStatusOsGateway;

    @TransactionalUseCase
    @Override
    public OrdemServico execute(String numeroOs) {
        OrdemServico os = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        os.entregar();
        OrdemServico salva = ordemServicoGateway.save(os);
        historicoStatusOsGateway.save(HistoricoStatusOs.criar(salva.getId(), salva.getStatus(),
                StatusOrdemServicoMensagemPolicy.mensagem(salva.getStatus()), salva.getNumeroOs()));
        return salva;
    }
}
