package com.autoflow.presentation.ordemservico;

import com.autoflow.application.port.in.ordemservico.EntregarOrdemServicoUseCase;
import com.autoflow.application.port.in.ordemservico.FinalizarServicoUseCase;
import com.autoflow.application.port.in.ordemservico.IniciarServicoUseCase;
import com.autoflow.presentation.ordemservico.response.OrdemServicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ordens-servico")
@Tag(name = "execução", description = "Endpoints para execução e entrega das ordens de serviço")
@SecurityRequirement(name = "bearerAuth")
public class OrdemServicoExecucaoController {

    private final IniciarServicoUseCase iniciarServico;
    private final FinalizarServicoUseCase finalizarServico;
    private final EntregarOrdemServicoUseCase entregar;

    public OrdemServicoExecucaoController(
            IniciarServicoUseCase iniciarServico,
            FinalizarServicoUseCase finalizarServico,
            EntregarOrdemServicoUseCase entregar) {
        this.iniciarServico = iniciarServico;
        this.finalizarServico = finalizarServico;
        this.entregar = entregar;
    }

    @Operation(summary = "Iniciar serviço da ordem de serviço",
            description = "Inicia a execução de um serviço da ordem de serviço")
    @PatchMapping("/{numeroOs}/servicos/{servicoId}/iniciar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse iniciar(@PathVariable String numeroOs, @PathVariable Long servicoId) {
        return OrdemServicoResponse.fromDomain(iniciarServico.execute(numeroOs, servicoId));
    }

    @Operation(summary = "Finalizar serviço da ordem de serviço",
            description = "Finaliza a execução de um serviço da ordem de serviço")
    @PatchMapping("/{numeroOs}/servicos/{servicoId}/finalizar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse finalizar(@PathVariable String numeroOs, @PathVariable Long servicoId) {
        return OrdemServicoResponse.fromDomain(finalizarServico.execute(numeroOs, servicoId));
    }

    @Operation(summary = "Entregar veículo ao cliente",
            description = "Conclui a ordem de serviço e registra a entrega do veículo ao cliente")
    @PatchMapping("/{numeroOs}/entregar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public OrdemServicoResponse entregar(@PathVariable String numeroOs) {
        return OrdemServicoResponse.fromDomain(entregar.execute(numeroOs));
    }
}
