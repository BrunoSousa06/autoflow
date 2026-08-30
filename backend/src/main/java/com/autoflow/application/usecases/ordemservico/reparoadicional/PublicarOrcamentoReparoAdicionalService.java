package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.gateway.OrcamentoPublicacaoGateway;
import com.autoflow.application.output.orcamento.OrcamentoPublicacao;
import org.springframework.stereotype.Service;

@Service
public class PublicarOrcamentoReparoAdicionalService {

    private final OrcamentoPublicacaoGateway gateway;

    public PublicarOrcamentoReparoAdicionalService(OrcamentoPublicacaoGateway gateway) {
        this.gateway = gateway;
    }

    public OrcamentoPublicacao publicar(Long orcamentoId) {
        return gateway.publicarComLinks(orcamentoId);
    }
}
