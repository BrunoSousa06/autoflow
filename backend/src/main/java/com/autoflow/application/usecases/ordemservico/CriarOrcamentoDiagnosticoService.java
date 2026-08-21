package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrcamentoVersioningGateway;
import com.autoflow.application.usecases.orcamento.OrcamentoFactory;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CriarOrcamentoDiagnosticoService {

    private final OrcamentoVersioningGateway versioningGateway;
    private final OrcamentoFactory orcamentoFactory;
    private final OrcamentoGateway orcamentoGateway;

    public CriarOrcamentoDiagnosticoService(
            OrcamentoVersioningGateway versioningGateway,
            OrcamentoFactory orcamentoFactory,
            OrcamentoGateway orcamentoGateway) {
        this.versioningGateway = versioningGateway;
        this.orcamentoFactory = orcamentoFactory;
        this.orcamentoGateway = orcamentoGateway;
    }

    public Orcamento criar(OrdemServico ordemServico, String numeroOs, LocalDateTime dataHora) {
        int versao = versioningGateway.proximaVersaoPorNumeroOs(numeroOs, TipoOrcamento.PRINCIPAL);
        Orcamento orcamento = orcamentoFactory.criarPrincipalDisponivel(ordemServico, versao, dataHora);
        return orcamentoGateway.save(orcamento);
    }
}
