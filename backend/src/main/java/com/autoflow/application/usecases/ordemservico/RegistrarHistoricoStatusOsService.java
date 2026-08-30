package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.domain.ordemservico.HistoricoStatusOs;
import com.autoflow.domain.ordemservico.OrdemServico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegistrarHistoricoStatusOsService {

    private final HistoricoStatusOsGateway historicoGateway;
    private final Clock clock;

    public void registrar(OrdemServico ordemServico) {
        historicoGateway.save(HistoricoStatusOs.criar(
                ordemServico.getId(),
                ordemServico.getStatus(),
                StatusOrdemServicoMensagemPolicy.mensagem(ordemServico.getStatus()),
                ordemServico.getNumeroOs(), LocalDateTime.now(clock)));
    }
}
