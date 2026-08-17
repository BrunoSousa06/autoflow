package com.autoflow.presentation.orcamento;

import com.autoflow.application.input.orcamento.OrcamentoFiltro;
import com.autoflow.application.gateway.OrcamentoDocumentoGateway;
import com.autoflow.application.port.in.orcamento.ConsultarOrcamentoAutenticadoUseCase;
import com.autoflow.application.port.in.orcamento.ConsultarOrcamentosUseCase;
import com.autoflow.application.port.in.orcamento.DecidirOrcamentoUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.presentation.orcamento.request.RecusarOrcamentoRequest;
import com.autoflow.presentation.orcamento.response.OrcamentoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orcamentos")
@RequiredArgsConstructor
@Tag(name = "orçamentos", description = "Endpoints para gerenciamento de orçamentos")
@SecurityRequirement(name = "bearerAuth")
public class OrcamentoController {

    private final OrcamentoDocumentoGateway orcamentoDocumentoGateway;
    private final ConsultarOrcamentoAutenticadoUseCase consultarOrcamentoAutenticadoUseCase;
    private final ConsultarOrcamentosUseCase consultarOrcamentosUseCase;
    private final DecidirOrcamentoUseCase decidirOrcamentoUseCase;

    @Operation(summary = "Listar o orçamento da ordem de serviço", description = "Retorna as informações do orçamento da ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping("/{orcamentoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE', 'ATENDENTE')")
    public OrcamentoResponse consultar(
            @PathVariable Long orcamentoId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return OrcamentoResponse.from(
                consultarOrcamentoAutenticadoUseCase.execute(orcamentoId, userDetails.getUsername())
        );
    }

    @Operation(summary = "Aprovar o orçamento da ordem de serviço", description = "Retorna as informações do orçamento aprovado da ordem de serviço com o status atualizado")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Orçamento não está disponivel")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PostMapping("/{orcamentoId}/aprovar")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public OrcamentoResponse aprovar(
            @PathVariable Long orcamentoId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return OrcamentoResponse.from(
                decidirOrcamentoUseCase.aprovarComoUsuario(orcamentoId, userDetails.getUsername())
        );
    }

    @Operation(summary = "Recusar o orçamento da ordem de serviço", description = "Recusa o orçamento da ordem de serviço e retorna o orçamento com status atualizado")
    @ApiResponse(responseCode = "200", description = "Orçamento recusado com sucesso")
    @ApiResponse(responseCode = "400", description = "Orçamento não está disponivel")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PostMapping("/{orcamentoId}/recusar")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public OrcamentoResponse recusar(
            @PathVariable Long orcamentoId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) RecusarOrcamentoRequest req
    ) {
        String motivo = (req == null) ? null : req.motivo();

        return OrcamentoResponse.from(
                decidirOrcamentoUseCase.recusarComoUsuario(orcamentoId, motivo, userDetails.getUsername())
        );
    }

    @Operation(summary = "Listar orçamentos", description = "Lista orçamentos acessíveis ao usuário autenticado com filtros opcionais por status, OS, placa, cliente e tipo")
    @ApiResponse(responseCode = "200", description = "Orçamentos listados com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE', 'ATENDENTE')")
    public List<OrcamentoResponse> listarOrcamentos(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) StatusOrcamento statusOrcamento,
            @RequestParam(required = false) String numeroOs,
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) String clienteEmail,
            @RequestParam(required = false) String clienteDocumento,
            @RequestParam(required = false) TipoOrcamento tipo
    ) {
        OrcamentoFiltro filtro = new OrcamentoFiltro(
                statusOrcamento,
                numeroOs,
                placa,
                clienteEmail,
                clienteDocumento,
                tipo
        );

        return consultarOrcamentosUseCase.execute(userDetails.getUsername(), filtro)
                .stream()
                .map(OrcamentoResponse::from)
                .toList();
    }

    @Operation(summary = "Baixar PDF do orçamento", description = "Retorna o PDF do orçamento para o usuário autenticado. ADMIN e ATENDENTE acessam qualquer orçamento; CLIENTE só acessa orçamentos da sua própria OS.")
    @ApiResponse(responseCode = "200", description = "PDF gerado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping(value = "/{orcamentoId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE', 'ATENDENTE')")
    public ResponseEntity<byte[]> baixarPdf(
            @PathVariable Long orcamentoId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        OrcamentoEntity orcamento = consultarOrcamentoAutenticadoUseCase.execute(orcamentoId, userDetails.getUsername());
        byte[] pdf = orcamentoDocumentoGateway.gerarPdf(orcamento);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"orcamento-" + orcamentoId + ".pdf\"")
                .body(pdf);
    }
}
