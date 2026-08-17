package com.autoflow.presentation.ordemservico;

import com.autoflow.application.port.in.ordemservico.EntregarOrdemServicoUseCase;
import com.autoflow.application.port.in.ordemservico.FinalizarDiagnosticoUseCase;
import com.autoflow.application.port.in.ordemservico.FinalizarServicoUseCase;
import com.autoflow.application.port.in.ordemservico.IniciarDiagnosticoUseCase;
import com.autoflow.application.port.in.ordemservico.IniciarServicoUseCase;
import com.autoflow.application.port.in.ordemservico.RegistrarItensNecessariosUseCase;
import com.autoflow.application.port.in.ordemservico.RegistrarLaudoUseCase;
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
