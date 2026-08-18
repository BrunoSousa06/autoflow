package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.application.port.in.ordemservico.FinalizarServicoUseCase;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;


@RequiredArgsConstructor
public class FinalizarServicoUseCaseImpl implements FinalizarServicoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final RegistrarHistoricoStatusOsService registrarHistoricoStatusOs;
    private final Clock clock;

    @TransactionalUseCase
    @Override
    public OrdemServico execute(String numeroOs, Long servicoId) {
        OrdemServico os = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        LocalDateTime agora = LocalDateTime.now(clock);
        os.buscarServicoSolicitado(servicoId).finalizar(agora);
        os.atualizarUltimaAtualizacao(agora);
        os.finalizarSeTodosServicosFinalizados(agora);
        OrdemServico salva = ordemServicoGateway.save(os);
        if (StatusOrdemServico.FINALIZADA.equals(salva.getStatus())) {
            registrarHistoricoStatusOs.registrar(salva);
        }
        return salva;
    }
}
