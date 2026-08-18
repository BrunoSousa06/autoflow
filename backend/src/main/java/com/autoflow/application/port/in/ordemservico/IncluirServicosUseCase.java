package com.autoflow.application.port.in.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.ServicoSolicitado;

import java.util.List;

public interface IncluirServicosUseCase {
    OrdemServico execute(String numeroOs, List<ServicoSolicitado> servicos, String emailUsuarioLogado);
}
