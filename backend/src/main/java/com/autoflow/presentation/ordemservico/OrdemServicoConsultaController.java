package com.autoflow.presentation.ordemservico;

import com.autoflow.application.input.PageQuery;
import com.autoflow.application.input.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.application.output.ordemservico.TempoMedioOrdemServicoOutput;
import com.autoflow.application.port.in.ordemservico.CalcularTempoMedioOrdemServicoUseCase;
import com.autoflow.application.port.in.ordemservico.ConsultarStatusOrdemServicoUseCase;
import com.autoflow.application.port.in.ordemservico.DetalharOrdemServicoUseCase;
import com.autoflow.application.port.in.ordemservico.ListarOrdensServicoUseCase;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.presentation.ordemservico.response.OrdemServicoDetalheResponse;
import com.autoflow.presentation.ordemservico.response.OrdemServicoResponse;
import com.autoflow.presentation.ordemservico.response.StatusOrdemServicoResponse;
import com.autoflow.presentation.ordemservico.response.TempoMedioOrdemServicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ordens-servico")
@Tag(name = "ordens de serviço", description = "Endpoints para consulta, criação e administração de ordens de serviço")
@SecurityRequirement(name = "bearerAuth")
public class OrdemServicoConsultaController {

    private final CalcularTempoMedioOrdemServicoUseCase calcularTempoMedio;
    private final ListarOrdensServicoUseCase listar;
    private final DetalharOrdemServicoUseCase detalhar;
    private final ConsultarStatusOrdemServicoUseCase consultarStatus;

    public OrdemServicoConsultaController(
            CalcularTempoMedioOrdemServicoUseCase calcularTempoMedio,
            ListarOrdensServicoUseCase listar,
            DetalharOrdemServicoUseCase detalhar,
            ConsultarStatusOrdemServicoUseCase consultarStatus) {
        this.calcularTempoMedio = calcularTempoMedio;
        this.listar = listar;
        this.detalhar = detalhar;
        this.consultarStatus = consultarStatus;
    }

    @Operation(summary = "Listar ordens de serviço",
            description = "Lista as ordens de serviço acessíveis ao usuário autenticado com filtros opcionais")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    public Page<OrdemServicoResponse> listar(
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String numeroOs,
            @RequestParam(required = false) StatusOrdemServico status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        var resultado = listar.execute(new OrdemServicoFiltroInput(cliente, numeroOs, status),
                new PageQuery(page, size), userDetails.getUsername());
        return new PageImpl<>(resultado.content().stream().map(OrdemServicoResponse::fromDomain).toList(),
                PageRequest.of(page, size), resultado.totalElements());
    }

    @Operation(summary = "Detalhar ordem de serviço",
            description = "Retorna os detalhes de uma ordem de serviço pelo número da OS")
    @GetMapping("/{numeroOs}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    public OrdemServicoDetalheResponse detalhar(@PathVariable String numeroOs) {
        var detalhe = detalhar.execute(numeroOs);
        return OrdemServicoDetalheResponse.fromDomain(detalhe.ordemServico(), detalhe.orcamentoAtual());
    }

    @Operation(summary = "Consultar status da ordem de serviço",
            description = "Retorna o status atual da ordem de serviço para o usuário autenticado")
    @GetMapping("/{numeroOs}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO', 'CLIENTE')")
    public StatusOrdemServicoResponse status(
            @PathVariable String numeroOs,
            @AuthenticationPrincipal UserDetails userDetails) {
        return StatusOrdemServicoResponse.from(consultarStatus.execute(numeroOs, userDetails.getUsername()));
    }

    @Operation(summary = "Consultar tempo médio de ordens de serviço",
            description = "Retorna o tempo médio das ordens de serviço finalizadas")
    @GetMapping("/metricas/tempo-medio")
    @PreAuthorize("hasRole('ADMIN')")
    public TempoMedioOrdemServicoResponse tempoMedio() {
        TempoMedioOrdemServicoOutput output = calcularTempoMedio.execute();
        return new TempoMedioOrdemServicoResponse(output.quantidadeOrdensFinalizadas(), output.tempoMedioSegundos(),
                output.tempoMedioMinutos(), output.tempoMedioHoras());
    }
}
