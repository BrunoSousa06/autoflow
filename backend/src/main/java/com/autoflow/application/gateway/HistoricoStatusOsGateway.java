package com.autoflow.application.gateway;

import com.autoflow.domain.ordemservico.HistoricoStatusOs;

import java.util.List;

public interface HistoricoStatusOsGateway {

    HistoricoStatusOs save(HistoricoStatusOs historico);

    List<HistoricoStatusOs> findByOrdemServicoIdOrderByRegistradoEmAsc(
            Long ordemServicoId);

    List<HistoricoStatusOs> findByNumeroOsOrderByRegistradoEmAsc(
            String numeroOs);
}
