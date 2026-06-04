package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;

import java.time.LocalDateTime;

public interface OrcamentoFactory {
    OrcamentoEntity criarPrincipalDisponivel(OrdemServicoEntity ordemServico, int versao, LocalDateTime now);
    OrcamentoEntity criarAdicionalDisponivel(OrdemServicoEntity ordemServico, ReparoAdicionalEntity reparoSalvo, int versao, LocalDateTime now);

}
