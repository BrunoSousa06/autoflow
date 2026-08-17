package com.autoflow.application.gateway;

import com.autoflow.application.output.orcamento.OrcamentoPublicacao;
import com.autoflow.domain.orcamento.OrcamentoEntity;

public interface OrcamentoPublicacaoGateway {

    String publicar(Long orcamentoId);

    default OrcamentoPublicacao publicarComLinks(Long orcamentoId) {
        return new OrcamentoPublicacao(publicar(orcamentoId), null);
    }

    boolean validarToken(OrcamentoEntity orcamento, String token);
}
