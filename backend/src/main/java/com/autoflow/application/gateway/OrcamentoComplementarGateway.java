package com.autoflow.application.gateway;

import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;

import java.time.LocalDateTime;

public interface OrcamentoComplementarGateway {

    Orcamento criarESalvar(
            OrdemServico ordemServico,
            ReparoAdicional reparo,
            LocalDateTime criadoEm
    );
}
