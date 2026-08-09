package com.autoflow.presentation.ordemservico;

import com.autoflow.application.usecases.ordemservico.CalcularTempoMedioOrdemServicoUseCase;
import com.autoflow.application.usecases.ordemservico.DetalharOrdemServicoUseCase;
import com.autoflow.application.usecases.ordemservico.ListarOrdensServicoUseCase;
import org.springframework.stereotype.Component;

@Component
public record OrdemServicoQueryUseCases(
        CalcularTempoMedioOrdemServicoUseCase calcularTempoMedio,
        ListarOrdensServicoUseCase listar,
        DetalharOrdemServicoUseCase detalhar) {
}
