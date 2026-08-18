package com.autoflow.infrastructure.orcamento;

import com.autoflow.application.gateway.OrcamentoComplementarGateway;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrcamentoVersioningGateway;
import com.autoflow.application.usecases.orcamento.OrcamentoFactory;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OrcamentoComplementarAdapter implements OrcamentoComplementarGateway {

    private final OrcamentoVersioningGateway versioningGateway;
    private final OrcamentoFactory factory;
    private final OrcamentoGateway orcamentoGateway;

    @Override
    public Orcamento criarESalvar(
            OrdemServico ordemServico,
            ReparoAdicional reparo,
            LocalDateTime criadoEm
    ) {
        int versao = versioningGateway.proximaVersao(ordemServico.getId(), TipoOrcamento.COMPLEMENTAR);
        Orcamento orcamento = factory.criarAdicionalDisponivel(
                ordemServico,
                reparo,
                versao,
                criadoEm
        );
        return orcamentoGateway.save(orcamento);
    }
}
