package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.service.orcamento.dto.PublicacaoOrcamentoResult;

public interface OrcamentoPublicacaoService {
    PublicacaoOrcamentoResult publicar(Long orcamentoId);

    boolean validarToken(OrcamentoEntity orc, String token);
}
