package com.autoflow.presentation.ordemservico;

import com.autoflow.application.usecases.ordemservico.AtribuirMecanicoUseCase;
import com.autoflow.application.usecases.ordemservico.CriarOrdemServicoUseCase;
import com.autoflow.application.usecases.ordemservico.IncluirServicosUseCase;
import org.springframework.stereotype.Component;

@Component
public record OrdemServicoCommandUseCases(
        CriarOrdemServicoUseCase criar,
        IncluirServicosUseCase incluir,
        AtribuirMecanicoUseCase atribuir) {
}
