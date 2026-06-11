package com.autoflow.controller.orcamento;

import com.autoflow.controller.orcamento.request.AprovarOrcamentoRequest;
import com.autoflow.controller.orcamento.request.RecusarOrcamentoRequest;
import com.autoflow.controller.orcamento.response.OrcamentoPublicoResponse;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.service.orcamento.OrcamentoPdfService;
import com.autoflow.service.orcamento.PublicOrcamentoService;
import com.autoflow.service.ordemservico.reparoadicional.ReparoAdicionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/public/orcamentos")
@RequiredArgsConstructor
@Tag(name = "orçamentos", description = "Endpoints para gerenciamento de orçamentos")
public class PublicOrcamentoController {


    private final PublicOrcamentoService publicOrcamentoService;
    private final ReparoAdicionalService reparoAdicionalService;
    private final OrcamentoPdfService orcamentoPdfService;

    @Operation(summary = "Listar o orçamento da ordem de serviço", description = "Retorna as informações do orçamento da ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @GetMapping("/{orcamentoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public OrcamentoPublicoResponse consultar(@PathVariable Long orcamentoId,
                                              @RequestParam String token) {
        return OrcamentoPublicoResponse.from(publicOrcamentoService.consultar(orcamentoId, token));
    }

    @Operation(summary = "Baixar o pdf com orçamento do cliente", description = "Retorna o pdf com as informações do orçamento do cliente")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @GetMapping(value = "/{orcamentoId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE', 'MECANICO')")
    public ResponseEntity<byte[]> baixarPdf(
            @PathVariable Long orcamentoId,
            @RequestParam String token
    ) {
        OrcamentoEntity orcamento = publicOrcamentoService.consultar(orcamentoId, token);

        byte[] pdf = orcamentoPdfService.gerarPdf(orcamento);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"orcamento-" + orcamentoId + ".pdf\""
                )
                .body(pdf);
    }

    @Operation(summary = "Aprovar o orçamento da ordem de serviço", description = "Retorna as informações do orçamento aprovado da ordem de serviço com o status atualizado")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Orçamento não está disponivel")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @PostMapping("/{orcamentoId}/aprovar")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public OrcamentoPublicoResponse aprovar(@PathVariable Long orcamentoId,
                                            @RequestParam String token,
                                            @RequestBody AprovarOrcamentoRequest req) {
        OrcamentoEntity orcamento = publicOrcamentoService.aprovar(orcamentoId, token, req.nome());

        if (TipoOrcamento.ADICIONAL.equals(orcamento.getTipo())) {
            reparoAdicionalService.aprovarPorOrcamentoId(orcamento.getId());
        }
        return OrcamentoPublicoResponse.from(orcamento);
    }

    @Operation(summary = "Recusar o orçamento da ordem de serviço", description = "Retorna as informações do orçamento recusado da ordem de serviço com o status atualizado")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Orçamento não está disponivel ou ja foi aprovado")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @PostMapping("/{orcamentoId}/recusar")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public OrcamentoPublicoResponse recusar(@PathVariable Long orcamentoId,
                                            @RequestParam String token,
                                            @RequestBody(required = false) RecusarOrcamentoRequest req) {
        String motivo = (req == null) ? null : req.motivo();
        return OrcamentoPublicoResponse.from(publicOrcamentoService.recusar(orcamentoId, token, motivo));
    }


    @Operation(summary = "Listar todos orçamentos", description = "Retorna as informações de todos orçamentos baseado nos status")
    @ApiResponse(responseCode = "200", description = "Orçamentos encontrados com sucesso")
    @ApiResponse(responseCode = "400", description = "Status incorreto")
    @GetMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE', 'ATENDENTE')")
    public List<OrcamentoPublicoResponse> listarOrcamentos(@RequestParam String token,
                                                           @RequestParam(required = false) StatusOrcamento statusOrcamento) {

        return publicOrcamentoService.consultarOrcamentos(statusOrcamento)
                .stream()
                .map(OrcamentoPublicoResponse::from)
                .toList();
    }
}
