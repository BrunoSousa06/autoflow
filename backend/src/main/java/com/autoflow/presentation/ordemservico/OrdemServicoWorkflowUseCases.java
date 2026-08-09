package com.autoflow.presentation.ordemservico;

import com.autoflow.application.usecases.ordemservico.*;
import org.springframework.stereotype.Component;

@Component
public record OrdemServicoWorkflowUseCases(
        IniciarDiagnosticoUseCase iniciarDiagnostico,
        RegistrarItensNecessariosUseCase registrarItensNecessarios,
        RegistrarLaudoUseCase registrarLaudo,
        IniciarServicoUseCase iniciarServico,
        FinalizarServicoUseCase finalizarServico,
        EntregarOrdemServicoUseCase entregar,
        FinalizarDiagnosticoUseCase finalizarDiagnostico) {
}
