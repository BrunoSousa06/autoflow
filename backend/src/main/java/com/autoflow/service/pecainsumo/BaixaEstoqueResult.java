package com.autoflow.service.pecainsumo;

import com.autoflow.domain.ordemservico.ItemNecessarioEntity;

import java.util.List;

public record BaixaEstoqueResult(
        List<ItemNecessarioEntity> itensAtualizados
) {}