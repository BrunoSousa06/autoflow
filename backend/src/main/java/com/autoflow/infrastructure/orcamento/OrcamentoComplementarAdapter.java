package com.autoflow.infrastructure.orcamento;

import com.autoflow.application.gateway.OrcamentoComplementarGateway;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.service.orcamento.OrcamentoFactory;
import com.autoflow.service.orcamento.OrcamentoVersioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OrcamentoComplementarAdapter implements OrcamentoComplementarGateway {

    private final OrcamentoVersioningService versioningService;
    private final OrcamentoFactory factory;
    private final OrcamentoGateway orcamentoGateway;

    @Override
    public OrcamentoEntity criarESalvar(
            OrdemServicoEntity ordemServico,
            ReparoAdicionalEntity reparo,
            LocalDateTime criadoEm
    ) {
        int versao = versioningService.proximaVersaoPrincipalNumeroOs(ordemServico.getNumeroOs());
        OrcamentoEntity orcamento = factory.criarAdicionalDisponivel(
                ordemServico,
                reparo,
                versao,
                criadoEm
        );
        return orcamentoGateway.save(orcamento);
    }
}
