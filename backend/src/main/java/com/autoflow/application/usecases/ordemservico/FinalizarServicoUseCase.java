package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.ordemservico.HistoricoStatusOs;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FinalizarServicoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final HistoricoStatusOsGateway historicoStatusOsGateway;

    @TransactionalUseCase
    public OrdemServico execute(String numeroOs, Long servicoId) {
        OrdemServico os = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        os.buscarServicoSolicitado(servicoId).finalizar();
        os.atualizarUltimaAtualizacao();
        os.finalizarSeTodosServicosFinalizados();
        OrdemServico salva = ordemServicoGateway.save(os);
        if (StatusOrdemServico.FINALIZADA.equals(salva.getStatus())) {
            historicoStatusOsGateway.save(HistoricoStatusOs.criar(salva.getId(), salva.getStatus(),
                    StatusOrdemServicoMensagemPolicy.mensagem(salva.getStatus()), salva.getNumeroOs()));
        }
        return salva;
    }
}
