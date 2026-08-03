package com.autoflow.application.gateway;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;

import java.time.LocalDateTime;

public interface OrcamentoComplementarGateway {

    OrcamentoEntity criarESalvar(
            OrdemServicoEntity ordemServico,
            ReparoAdicionalEntity reparo,
            LocalDateTime criadoEm
    );
}
