package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.service.orcamento.dto.OrcamentoFiltro;

import java.util.List;

public interface OrcamentoService {
    OrcamentoEntity consultarAutenticado(Long orcamentoId, String emailUsuario);

    OrcamentoEntity consultarPorToken(Long orcamentoId, String token);

    OrcamentoEntity aprovar(Long orcamentoId, String emailUsuario);

    OrcamentoEntity recusar(Long orcamentoId, String motivo, String emailUsuario);

    List<OrcamentoEntity> consultarOrcamentos(String emailUsuario, OrcamentoFiltro filtro);

    void validarToken(OrcamentoEntity orcamento, String token);
}
