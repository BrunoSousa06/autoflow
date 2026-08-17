package com.autoflow.application.port.in.orcamento;

import com.autoflow.application.input.orcamento.OrcamentoFiltro;
import com.autoflow.domain.orcamento.OrcamentoEntity;

import java.util.List;

public interface ConsultarOrcamentosUseCase {
    List<OrcamentoEntity> execute(String emailUsuario, OrcamentoFiltro filtro);
}
