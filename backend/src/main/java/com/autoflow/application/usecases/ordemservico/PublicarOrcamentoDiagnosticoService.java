package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.OrcamentoPublicacaoGateway;
import com.autoflow.application.output.orcamento.OrcamentoPublicacao;
import org.springframework.stereotype.Service;

@Service
public class PublicarOrcamentoDiagnosticoService {

    private final OrcamentoPublicacaoGateway publicacaoGateway;

    public PublicarOrcamentoDiagnosticoService(OrcamentoPublicacaoGateway publicacaoGateway) {
        this.publicacaoGateway = publicacaoGateway;
    }

    public OrcamentoPublicacao publicar(Long orcamentoId) {
        return publicacaoGateway.publicarComLinks(orcamentoId);
    }
}
