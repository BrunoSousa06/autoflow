package com.autoflow.controller.orcamento;

import com.autoflow.controller.orcamento.request.AceitarOrcamentoRequest;
import com.autoflow.controller.orcamento.request.RecusarOrcamentoRequest;
import com.autoflow.controller.orcamento.response.OrcamentoPublicoResponse;
import com.autoflow.service.orcamento.PublicOrcamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/orcamentos")
@RequiredArgsConstructor
public class PublicOrcamentoController {


    private final PublicOrcamentoService publicOrcamentoService;


    @GetMapping("/{orcamentoId}")
    public OrcamentoPublicoResponse consultar(@PathVariable Long orcamentoId,
                                              @RequestParam String token) {
        return OrcamentoPublicoResponse.from(publicOrcamentoService.consultar(orcamentoId, token));
    }

    @PostMapping("/{orcamentoId}/aceitar")
    public OrcamentoPublicoResponse aceitar(@PathVariable Long orcamentoId,
                                            @RequestParam String token,
                                            @RequestBody AceitarOrcamentoRequest req) {
        return OrcamentoPublicoResponse.from(publicOrcamentoService.aceitar(orcamentoId, token, req.nome()));
    }

    @PostMapping("/{orcamentoId}/recusar")
    public OrcamentoPublicoResponse recusar(@PathVariable Long orcamentoId,
                                            @RequestParam String token,
                                            @RequestBody(required = false) RecusarOrcamentoRequest req) {
        String motivo = (req == null) ? null : req.motivo();
        return OrcamentoPublicoResponse.from(publicOrcamentoService.recusar(orcamentoId, token, motivo));
    }

}
