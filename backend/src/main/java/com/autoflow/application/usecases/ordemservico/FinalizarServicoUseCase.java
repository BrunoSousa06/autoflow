package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FinalizarServicoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final HistoricoStatusOsGateway historicoStatusOsGateway;

    @TransactionalUseCase
    public OrdemServicoEntity execute(String numeroOs, Long servicoId) {
        OrdemServicoEntity os = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        os.buscarServicoSolicitado(servicoId).finalizar();
        os.atualizarUltimaAtualizacao();
        os.finalizarSeTodosServicosFinalizados();
        OrdemServicoEntity salva = ordemServicoGateway.save(os);
        if (StatusOrdemServico.FINALIZADA.equals(salva.getStatus())) {
            historicoStatusOsGateway.save(HistoricoStatusOsEntity.criar(salva.getId(), salva.getStatus(),
                    StatusOrdemServicoMensagemPolicy.mensagem(salva.getStatus()), salva.getNumeroOs()));
        }
        return salva;
    }
}
