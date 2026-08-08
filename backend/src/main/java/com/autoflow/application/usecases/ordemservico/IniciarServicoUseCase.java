package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.application.usecases.pecainsumo.BaixarEstoqueUseCase;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class IniciarServicoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final BaixarEstoqueUseCase baixarEstoqueUseCase;

    @TransactionalUseCase
    public OrdemServicoEntity execute(String numeroOs, Long servicoId) {
        OrdemServicoEntity os = ordemServicoGateway.findByNumeroOsForUpdate(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        if (!StatusOrdemServico.EM_EXECUCAO.equals(os.getStatus())) {
            throw new IllegalStateException("O serviço só pode ser iniciado após a aprovação do orçamento.");
        }
        ServicoSolicitadoEntity servico = os.buscarServicoSolicitado(servicoId);
        servico.validarPodeIniciar();
        servico.iniciar(baixarEstoqueUseCase.execute(servico.getItensNecessarios()));
        return ordemServicoGateway.save(os);
    }
}
