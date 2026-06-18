package com.autoflow.controller.orcamento;

import com.autoflow.controller.orcamento.request.RecusarOrcamentoRequest;
import com.autoflow.controller.orcamento.response.OrcamentoResponse;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.service.orcamento.OrcamentoPdfService;
import com.autoflow.service.orcamento.OrcamentoService;
import com.autoflow.service.orcamento.dto.OrcamentoFiltro;
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

    private final OrcamentoService orcamentoService;
    private final OrcamentoPdfService orcamentoPdfService;

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
                orcamentoService.consultarAutenticado(orcamentoId, userDetails.getUsername())
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
                orcamentoService.aprovar(orcamentoId, userDetails.getUsername())
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
                orcamentoService.recusar(orcamentoId, motivo, userDetails.getUsername())
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

        return orcamentoService.consultarOrcamentos(userDetails.getUsername(), filtro)
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
        OrcamentoEntity orcamento = orcamentoService.consultarAutenticado(orcamentoId, userDetails.getUsername());
        byte[] pdf = orcamentoPdfService.gerarPdf(orcamento);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"orcamento-" + orcamentoId + ".pdf\"")
                .body(pdf);
    }
}
