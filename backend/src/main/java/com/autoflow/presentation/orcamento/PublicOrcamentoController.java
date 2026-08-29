package com.autoflow.presentation.orcamento;

import com.autoflow.application.gateway.OrcamentoDocumentoGateway;
import com.autoflow.application.port.in.ordemservico.acompanhamento.AcessarOrcamentoAcompanhamentoUseCase;
import com.autoflow.application.port.in.orcamento.ConsultarOrcamentoPorTokenUseCase;
import com.autoflow.application.port.in.orcamento.DecidirOrcamentoUseCase;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.presentation.orcamento.request.AprovarOrcamentoRequest;
import com.autoflow.presentation.orcamento.request.RecusarOrcamentoRequest;
import com.autoflow.presentation.orcamento.response.OrcamentoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/orcamentos")
@RequiredArgsConstructor
@Tag(name = "orçamentos públicos", description = "Endpoints para consulta e decisão de orçamentos pelo link público")
public class PublicOrcamentoController {


    private final OrcamentoDocumentoGateway orcamentoDocumentoGateway;
    private final ConsultarOrcamentoPorTokenUseCase consultarOrcamentoPorTokenUseCase;
    private final DecidirOrcamentoUseCase decidirOrcamentoUseCase;
    private final AcessarOrcamentoAcompanhamentoUseCase acessarOrcamentoAcompanhamentoUseCase;

    @Operation(summary = "Consultar orçamento pelo link público", description = "Retorna os dados não sensíveis do orçamento usando o token público")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @GetMapping("/{orcamentoId}")
    public OrcamentoResponse consultar(
            @PathVariable Long orcamentoId,
            @RequestParam(required = false) String token
    ) {
        return OrcamentoResponse.from(consultarOrcamentoPorTokenUseCase.execute(orcamentoId, token));
    }

    @Operation(summary = "Baixar o pdf com orçamento do cliente", description = "Retorna o pdf com as informações do orçamento do cliente")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    @GetMapping(value = "/{orcamentoId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> baixarPdf(
            @PathVariable Long orcamentoId,
            @RequestParam String token
    ) {
        Orcamento orcamento = consultarOrcamentoPorTokenUseCase.execute(orcamentoId, token);

        byte[] pdf = orcamentoDocumentoGateway.gerarPdf(orcamento);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"orcamento-" + orcamentoId + ".pdf\""
                )
                .body(pdf);
    }

    @Operation(summary = "Aprovar orçamento pelo link público", description = "Registra a aprovação usando o token público, sem autenticação")
    @ApiResponse(responseCode = "200", description = "Orçamento aprovado ou aprovação já registrada")
    @ApiResponse(responseCode = "400", description = "Orçamento não está disponível ou decisão conflitante")
    @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @PostMapping("/{orcamentoId}/aprovar")
    public OrcamentoResponse aprovar(
            @PathVariable Long orcamentoId,
            @RequestParam(required = false) String token,
            @RequestBody(required = false) @Valid AprovarOrcamentoRequest req
    ) {
        String nome = req == null ? null : req.nome();
        return OrcamentoResponse.from(
                decidirOrcamentoUseCase.aprovarComoToken(orcamentoId, token, nome));
    }

    @Operation(summary = "Recusar orçamento pelo link público", description = "Registra a recusa usando o token público, sem autenticação")
    @ApiResponse(responseCode = "200", description = "Orçamento recusado ou recusa já registrada")
    @ApiResponse(responseCode = "400", description = "Orçamento não está disponível ou decisão conflitante")
    @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @PostMapping("/{orcamentoId}/recusar")
    public OrcamentoResponse recusar(
            @PathVariable Long orcamentoId,
            @RequestParam(required = false) String token,
            @RequestBody(required = false) @Valid RecusarOrcamentoRequest req
    ) {
        String motivo = req == null ? null : req.motivo();
        String nome = req == null ? null : req.nome();
        return OrcamentoResponse.from(
                decidirOrcamentoUseCase.recusarComoToken(orcamentoId, token, motivo, nome));
    }

    @Operation(summary = "Baixar o PDF do orçamento pelo acompanhamento",
            description = "Retorna o PDF do orçamento usando o token de acompanhamento público")
    @GetMapping(value = "/{orcamentoId}/pdf/acompanhamento", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> baixarPdfPorAcompanhamento(
            @PathVariable Long orcamentoId,
            @RequestParam String token
    ) {
        Orcamento orcamento = acessarOrcamentoAcompanhamentoUseCase.consultar(orcamentoId, token);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"orcamento-" + orcamentoId + ".pdf\"")
                .body(orcamentoDocumentoGateway.gerarPdf(orcamento));
    }

    @Operation(summary = "Aprovar orçamento pelo acompanhamento",
            description = "Registra a aprovação do orçamento usando o token de acompanhamento público")
    @PostMapping("/{orcamentoId}/aprovar/acompanhamento")
    public OrcamentoResponse aprovarPorAcompanhamento(
            @PathVariable Long orcamentoId,
            @RequestParam String token
    ) {
        return OrcamentoResponse.from(acessarOrcamentoAcompanhamentoUseCase.aprovar(orcamentoId, token));
    }
}
