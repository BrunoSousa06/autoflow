package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.domain.ordemservico.HistoricoStatusOs;
import com.autoflow.domain.ordemservico.OrdemServico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrarHistoricoStatusOsService {

    private final HistoricoStatusOsGateway historicoGateway;

    public void registrar(OrdemServico ordemServico) {
        historicoGateway.save(HistoricoStatusOs.criar(
                ordemServico.getId(),
                ordemServico.getStatus(),
                StatusOrdemServicoMensagemPolicy.mensagem(ordemServico.getStatus()),
                ordemServico.getNumeroOs()));
    }
}
