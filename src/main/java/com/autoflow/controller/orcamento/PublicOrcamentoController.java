package com.autoflow.controller.orcamento;

import com.autoflow.controller.orcamento.request.AprovarOrcamentoRequest;
import com.autoflow.controller.orcamento.request.RecusarOrcamentoRequest;
import com.autoflow.controller.orcamento.response.OrcamentoPublicoResponse;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.service.orcamento.PublicOrcamentoService;
import com.autoflow.service.ordemservico.reparoadicional.ReparoAdicionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/orcamentos")
@RequiredArgsConstructor
@Tag(name = "orçamentos", description = "Endpoints para gerenciamento de orçamentos")
public class PublicOrcamentoController {


    private final PublicOrcamentoService publicOrcamentoService;
    private final ReparoAdicionalService reparoAdicionalService;

    @Operation(summary = "Listar o orçamento da ordem de serviço", description = "Retorna as informações do orçamento da ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @GetMapping("/{orcamentoId}")
    public OrcamentoPublicoResponse consultar(@PathVariable Long orcamentoId,
                                              @RequestParam String token) {
        return OrcamentoPublicoResponse.from(publicOrcamentoService.consultar(orcamentoId, token));
    }

    @Operation(summary = "Aprovar o orçamento da ordem de serviço", description = "Retorna as informações do orçamento aprovado da ordem de serviço com o status atualizado")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Orçamento não está disponivel")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @PostMapping("/{orcamentoId}/aprovar")
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
    public OrcamentoPublicoResponse recusar(@PathVariable Long orcamentoId,
                                            @RequestParam String token,
                                            @RequestBody(required = false) RecusarOrcamentoRequest req) {
        String motivo = (req == null) ? null : req.motivo();
        return OrcamentoPublicoResponse.from(publicOrcamentoService.recusar(orcamentoId, token, motivo));
    }

}
