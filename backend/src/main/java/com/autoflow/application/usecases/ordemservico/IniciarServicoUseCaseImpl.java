package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.application.port.in.pecainsumo.BaixarEstoqueUseCase;
import com.autoflow.application.port.in.ordemservico.IniciarServicoUseCase;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class IniciarServicoUseCaseImpl implements IniciarServicoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final BaixarEstoqueUseCase baixarEstoqueUseCase;

    @TransactionalUseCase
    @Override
    public OrdemServico execute(String numeroOs, Long servicoId) {
        OrdemServico os = ordemServicoGateway.findByNumeroOsForUpdate(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        if (!StatusOrdemServico.EM_EXECUCAO.equals(os.getStatus())) {
            throw new IllegalStateException("O serviço só pode ser iniciado após a aprovação do orçamento.");
        }
        ServicoSolicitado servico = os.buscarServicoSolicitado(servicoId);
        servico.validarPodeIniciar();
        servico.iniciar(baixarEstoqueUseCase.execute(servico.getItensNecessarios()));
        return ordemServicoGateway.save(os);
    }
}
