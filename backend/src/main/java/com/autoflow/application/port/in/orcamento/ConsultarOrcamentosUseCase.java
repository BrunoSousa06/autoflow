package com.autoflow.application.port.in.orcamento;

import com.autoflow.application.input.orcamento.OrcamentoFiltro;
import com.autoflow.domain.orcamento.Orcamento;

import java.util.List;

public interface ConsultarOrcamentosUseCase {
    List<Orcamento> execute(String emailUsuario, OrcamentoFiltro filtro);
}
