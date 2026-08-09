package com.autoflow.infrastructure.orcamento;

import com.autoflow.application.gateway.OrcamentoComplementarGateway;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrcamentoVersioningGateway;
import com.autoflow.application.usecases.orcamento.OrcamentoFactory;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
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
    public OrcamentoEntity criarESalvar(
            OrdemServicoEntity ordemServico,
            ReparoAdicionalEntity reparo,
            LocalDateTime criadoEm
    ) {
        int versao = versioningGateway.proximaVersao(ordemServico.getId(), TipoOrcamento.COMPLEMENTAR);
        OrcamentoEntity orcamento = factory.criarAdicionalDisponivel(
                ordemServico,
                reparo,
                versao,
                criadoEm
        );
        return orcamentoGateway.save(orcamento);
    }
}
