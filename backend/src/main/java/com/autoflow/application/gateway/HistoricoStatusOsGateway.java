package com.autoflow.application.gateway;

import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;

import java.util.List;

public interface HistoricoStatusOsGateway {

    HistoricoStatusOsEntity save(HistoricoStatusOsEntity historico);

    List<HistoricoStatusOsEntity> findByOrdemServicoIdOrderByRegistradoEmAsc(
            Long ordemServicoId);

    List<HistoricoStatusOsEntity> findByNumeroOsOrderByRegistradoEmAsc(
            String numeroOs);
}
