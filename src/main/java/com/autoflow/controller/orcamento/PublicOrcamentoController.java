package com.autoflow.controller.orcamento;

import com.autoflow.controller.orcamento.request.AprovarOrcamentoRequest;
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

    @PostMapping("/{orcamentoId}/aprovar")
    public OrcamentoPublicoResponse aprovar(@PathVariable Long orcamentoId,
                                            @RequestParam String token,
                                            @RequestBody AprovarOrcamentoRequest req) {
        return OrcamentoPublicoResponse.from(publicOrcamentoService.aprovar(orcamentoId, token, req.nome()));
    }

    @PostMapping("/{orcamentoId}/recusar")
    public OrcamentoPublicoResponse recusar(@PathVariable Long orcamentoId,
                                            @RequestParam String token,
                                            @RequestBody(required = false) RecusarOrcamentoRequest req) {
        String motivo = (req == null) ? null : req.motivo();
        return OrcamentoPublicoResponse.from(publicOrcamentoService.recusar(orcamentoId, token, motivo));
    }

}
