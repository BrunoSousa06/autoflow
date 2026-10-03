package com.autoflow.application.port.in.ordemservico;

import com.autoflow.domain.ordemservico.ItemNecessario;
import com.autoflow.domain.ordemservico.OrdemServico;

import java.util.List;

public interface RegistrarItensNecessariosUseCase {
    OrdemServico execute(
            String numeroOs,
            Long servicoId,
            String emailUsuarioLogado,
            List<ItemNecessario> itensNecessarios);
}
