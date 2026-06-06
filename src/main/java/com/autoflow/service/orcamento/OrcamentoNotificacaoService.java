package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;

public interface OrcamentoNotificacaoService {

    void enviarLinkOrcamentoParaCliente(
            OrcamentoEntity orcamento,
            OrdemServicoEntity ordemServico,
            String urlPublica
    );
}