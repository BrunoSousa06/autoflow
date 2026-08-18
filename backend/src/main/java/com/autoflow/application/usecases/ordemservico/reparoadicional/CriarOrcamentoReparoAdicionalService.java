package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.gateway.OrcamentoComplementarGateway;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CriarOrcamentoReparoAdicionalService {

    private final OrcamentoComplementarGateway gateway;

    public CriarOrcamentoReparoAdicionalService(OrcamentoComplementarGateway gateway) {
        this.gateway = gateway;
    }

    public Orcamento criar(OrdemServico ordemServico, ReparoAdicional reparo, LocalDateTime dataHora) {
        return gateway.criarESalvar(ordemServico, reparo, dataHora);
    }
}
