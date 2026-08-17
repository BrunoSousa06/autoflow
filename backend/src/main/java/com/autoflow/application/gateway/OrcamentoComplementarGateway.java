package com.autoflow.application.gateway;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;

import java.time.LocalDateTime;

public interface OrcamentoComplementarGateway {

    OrcamentoEntity criarESalvar(
            OrdemServico ordemServico,
            ReparoAdicional reparo,
            LocalDateTime criadoEm
    );
}
