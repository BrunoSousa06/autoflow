package com.autoflow.application.gateway;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;

public interface OrcamentoNotificacaoGateway {

    void notificar(OrcamentoEntity orcamento, OrdemServicoEntity ordemServico, String urlPublica);
}
