package com.autoflow.controller.orcamento;

import com.autoflow.application.gateway.OrcamentoDocumentoGateway;
import com.autoflow.application.usecases.orcamento.ConsultarOrcamentoPorTokenUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.application.usecases.ordemservico.acompanhamento.AcessarOrcamentoAcompanhamentoUseCase;
import com.autoflow.controller.orcamento.response.OrcamentoResponse;
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


    private final OrcamentoDocumentoGateway orcamentoDocumentoGateway;
    private final ConsultarOrcamentoPorTokenUseCase consultarOrcamentoPorTokenUseCase;
    private final AcessarOrcamentoAcompanhamentoUseCase acessarOrcamentoAcompanhamentoUseCase;

    @Operation(summary = "Baixar o pdf com orçamento do cliente", description = "Retorna o pdf com as informações do orçamento do cliente")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @GetMapping(value = "/{orcamentoId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> baixarPdf(
            @PathVariable Long orcamentoId,
            @RequestParam String token
    ) {
        OrcamentoEntity orcamento = consultarOrcamentoPorTokenUseCase.execute(orcamentoId, token);

        byte[] pdf = orcamentoDocumentoGateway.gerarPdf(orcamento);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"orcamento-" + orcamentoId + ".pdf\""
                )
                .body(pdf);
    }

    @GetMapping(value = "/{orcamentoId}/pdf/acompanhamento", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> baixarPdfPorAcompanhamento(
            @PathVariable Long orcamentoId,
            @RequestParam String token
    ) {
        OrcamentoEntity orcamento = acessarOrcamentoAcompanhamentoUseCase.consultar(orcamentoId, token);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"orcamento-" + orcamentoId + ".pdf\"")
                .body(orcamentoDocumentoGateway.gerarPdf(orcamento));
    }

    @PostMapping("/{orcamentoId}/aprovar/acompanhamento")
    public OrcamentoResponse aprovarPorAcompanhamento(
            @PathVariable Long orcamentoId,
            @RequestParam String token
    ) {
        return OrcamentoResponse.from(acessarOrcamentoAcompanhamentoUseCase.aprovar(orcamentoId, token));
    }
}
