package com.autoflow.controller.orcamento;

import com.autoflow.controller.orcamento.request.AprovarOrcamentoRequest;
import com.autoflow.controller.orcamento.request.RecusarOrcamentoRequest;
import com.autoflow.controller.orcamento.response.OrcamentoPublicoResponse;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.service.orcamento.OrcamentoPdfService;
import com.autoflow.service.orcamento.PublicOrcamentoService;
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


    private final PublicOrcamentoService publicOrcamentoService;
    private final OrcamentoPdfService orcamentoPdfService;

    @Operation(summary = "Listar o orçamento do cliente", description = "Retorna as informações do orçamento do cliente")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @GetMapping("/{orcamentoId}")
    public OrcamentoPublicoResponse consultar(@PathVariable Long orcamentoId,
                                              @RequestParam String token) {
        return OrcamentoPublicoResponse.from(publicOrcamentoService.consultar(orcamentoId, token));
    }

    @Operation(summary = "Baixar o pdf com orçamento do cliente", description = "Retorna o pdf com as informações do orçamento do cliente")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @GetMapping(value = "/{orcamentoId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
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

    @Operation(summary = "Aprovar o orçamento do cliente", description = "Retorna as informações do orçamento aprovado do cliente com os status atualizado")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Orçamento não está disponivel")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @PostMapping("/{orcamentoId}/aprovar")
    public OrcamentoPublicoResponse aprovar(@PathVariable Long orcamentoId,
                                            @RequestParam String token,
                                            @RequestBody AprovarOrcamentoRequest req) {
        return OrcamentoPublicoResponse.from(
                publicOrcamentoService.aprovar(orcamentoId, token, req.nome())
        );
    }

    @Operation(summary = "Recusar o orçamento do cliente", description = "Retorna as informações do orçamento recusado do cliente com os status atualizado")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Orçamento não está disponivel ou ja foi aprovado")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @PostMapping("/{orcamentoId}/recusar")
    public OrcamentoPublicoResponse recusar(@PathVariable Long orcamentoId,
                                            @RequestParam String token,
                                            @RequestBody(required = false) RecusarOrcamentoRequest req) {
        String motivo = (req == null) ? null : req.motivo();
        return OrcamentoPublicoResponse.from(publicOrcamentoService.recusar(orcamentoId, token, motivo));
    }

}
