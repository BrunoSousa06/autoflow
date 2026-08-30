package com.autoflow.application.port.in.pecainsumo;

import com.autoflow.domain.ordemservico.ItemNecessario;

import java.util.List;

public interface BaixarEstoqueUseCase {
    List<ItemNecessario> execute(List<ItemNecessario> itens);
}
