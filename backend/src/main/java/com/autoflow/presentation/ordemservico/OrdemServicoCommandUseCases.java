package com.autoflow.presentation.ordemservico;

import com.autoflow.application.port.in.ordemservico.AtribuirMecanicoUseCase;
import com.autoflow.application.port.in.ordemservico.CriarOrdemServicoUseCase;
import com.autoflow.application.port.in.ordemservico.IncluirServicosUseCase;
import org.springframework.stereotype.Component;

@Component
public record OrdemServicoCommandUseCases(
        CriarOrdemServicoUseCase criar,
        IncluirServicosUseCase incluir,
        AtribuirMecanicoUseCase atribuir) {
}
