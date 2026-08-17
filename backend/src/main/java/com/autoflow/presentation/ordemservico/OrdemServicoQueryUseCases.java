package com.autoflow.presentation.ordemservico;

import com.autoflow.application.port.in.ordemservico.CalcularTempoMedioOrdemServicoUseCase;
import com.autoflow.application.port.in.ordemservico.ConsultarStatusOrdemServicoUseCase;
import com.autoflow.application.port.in.ordemservico.DetalharOrdemServicoUseCase;
import com.autoflow.application.port.in.ordemservico.ListarOrdensServicoUseCase;
import org.springframework.stereotype.Component;

@Component
public record OrdemServicoQueryUseCases(
        CalcularTempoMedioOrdemServicoUseCase calcularTempoMedio,
        ListarOrdensServicoUseCase listar,
        DetalharOrdemServicoUseCase detalhar,
        ConsultarStatusOrdemServicoUseCase consultarStatus) {
}
