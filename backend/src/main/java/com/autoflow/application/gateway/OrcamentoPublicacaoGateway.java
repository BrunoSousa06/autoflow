package com.autoflow.application.gateway;

import com.autoflow.domain.orcamento.OrcamentoEntity;

public interface OrcamentoPublicacaoGateway {

    String publicar(Long orcamentoId);

    boolean validarToken(OrcamentoEntity orcamento, String token);
}
