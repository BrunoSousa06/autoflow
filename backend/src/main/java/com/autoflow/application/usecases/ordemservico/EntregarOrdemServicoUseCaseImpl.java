package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.port.in.ordemservico.EntregarOrdemServicoUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.ordemservico.OrdemServico;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;


@RequiredArgsConstructor
public class EntregarOrdemServicoUseCaseImpl implements EntregarOrdemServicoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final RegistrarHistoricoStatusOsService registrarHistoricoStatusOs;
    private final Clock clock;

    @TransactionalUseCase
    @Override
    public OrdemServico execute(String numeroOs) {
        OrdemServico os = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        os.entregar(LocalDateTime.now(clock));
        OrdemServico salva = ordemServicoGateway.save(os);
        registrarHistoricoStatusOs.registrar(salva);
        return salva;
    }
}
