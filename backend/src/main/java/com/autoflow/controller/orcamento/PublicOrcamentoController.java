package com.autoflow.controller.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.service.orcamento.OrcamentoPdfService;
import com.autoflow.service.orcamento.OrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/orcamentos")
@RequiredArgsConstructor
@Tag(name = "orçamentos", description = "Endpoints para gerenciamento de orçamentos")
public class PublicOrcamentoController {


    private final OrcamentoService orcamentoService;
    private final OrcamentoPdfService orcamentoPdfService;

    @Operation(summary = "Baixar o pdf com orçamento do cliente", description = "Retorna o pdf com as informações do orçamento do cliente")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @GetMapping(value = "/{orcamentoId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> baixarPdf(
            @PathVariable Long orcamentoId,
            @RequestParam String token
    ) {
        OrcamentoEntity orcamento = orcamentoService.consultarPorToken(orcamentoId, token);

        byte[] pdf = orcamentoPdfService.gerarPdf(orcamento);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"orcamento-" + orcamentoId + ".pdf\""
                )
                .body(pdf);
    }
}
