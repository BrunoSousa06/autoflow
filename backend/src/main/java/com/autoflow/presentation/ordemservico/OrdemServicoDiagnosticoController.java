package com.autoflow.presentation.ordemservico;

import com.autoflow.application.port.in.ordemservico.FinalizarDiagnosticoUseCase;
import com.autoflow.application.port.in.ordemservico.IniciarDiagnosticoUseCase;
import com.autoflow.application.port.in.ordemservico.RegistrarItensNecessariosUseCase;
import com.autoflow.application.port.in.ordemservico.RegistrarLaudoUseCase;
import com.autoflow.presentation.ordemservico.request.ItensNecessariosRequest;
import com.autoflow.presentation.ordemservico.request.RegistrarLaudoRequest;
import com.autoflow.presentation.ordemservico.response.FinalizarDiagnosticoResponse;
import com.autoflow.presentation.ordemservico.response.OrdemServicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordens-servico")
@Tag(name = "diagnóstico", description = "Endpoints para diagnóstico das ordens de serviço")
@SecurityRequirement(name = "bearerAuth")
public class OrdemServicoDiagnosticoController {

    private final IniciarDiagnosticoUseCase iniciarDiagnostico;
    private final RegistrarItensNecessariosUseCase registrarItens;
    private final RegistrarLaudoUseCase registrarLaudo;
    private final FinalizarDiagnosticoUseCase finalizarDiagnostico;
    private final ItensNecessariosControllerMapper itensMapper;

    public OrdemServicoDiagnosticoController(
            IniciarDiagnosticoUseCase iniciarDiagnostico,
            RegistrarItensNecessariosUseCase registrarItens,
            RegistrarLaudoUseCase registrarLaudo,
            FinalizarDiagnosticoUseCase finalizarDiagnostico,
            ItensNecessariosControllerMapper itensMapper) {
        this.iniciarDiagnostico = iniciarDiagnostico;
        this.registrarItens = registrarItens;
        this.registrarLaudo = registrarLaudo;
        this.finalizarDiagnostico = finalizarDiagnostico;
        this.itensMapper = itensMapper;
    }

    @Operation(summary = "Iniciar diagnóstico",
            description = "Inicia o diagnóstico da ordem de serviço")
    @PatchMapping("/{numeroOs}/diagnostico/iniciar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse iniciar(@PathVariable String numeroOs, @AuthenticationPrincipal UserDetails userDetails) {
        return OrdemServicoResponse.fromDomain(iniciarDiagnostico.execute(numeroOs, userDetails.getUsername()));
    }

    @Operation(summary = "Registrar itens necessários",
            description = "Registra os itens necessários para um serviço da ordem de serviço")
    @PatchMapping("/{numeroOs}/servicos/{servicoId}/itens-necessarios")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse registrarItens(
            @PathVariable String numeroOs,
            @PathVariable Long servicoId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody List<ItensNecessariosRequest> request) {
        return OrdemServicoResponse.fromDomain(registrarItens.execute(
                numeroOs, servicoId, userDetails.getUsername(), itensMapper.mapToEntities(request)));
    }

    @Operation(summary = "Registrar laudo do diagnóstico",
            description = "Registra o laudo do diagnóstico da ordem de serviço")
    @PatchMapping("/{numeroOs}/diagnostico/laudo")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse laudo(
            @PathVariable String numeroOs,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RegistrarLaudoRequest request) {
        return OrdemServicoResponse.fromDomain(registrarLaudo.execute(numeroOs, userDetails.getUsername(), request.laudo()));
    }

    @Operation(summary = "Finalizar diagnóstico e gerar orçamento",
            description = "Finaliza o diagnóstico e gera o orçamento da ordem de serviço")
    @PatchMapping("/{numeroOs}/diagnostico/finalizar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public FinalizarDiagnosticoResponse finalizar(
            @PathVariable String numeroOs,
            @AuthenticationPrincipal UserDetails userDetails) {
        return FinalizarDiagnosticoResponse.from(finalizarDiagnostico.execute(numeroOs, userDetails.getUsername()));
    }
}
