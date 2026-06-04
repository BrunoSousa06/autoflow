package com.autoflow.controller.orcamento;

import com.autoflow.controller.orcamento.request.AprovarOrcamentoRequest;
import com.autoflow.controller.orcamento.request.RecusarOrcamentoRequest;
import com.autoflow.controller.orcamento.response.OrcamentoPublicoResponse;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.service.orcamento.PublicOrcamentoService;
import com.autoflow.service.ordemservico.reparoadicional.ReparoAdicionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/orcamentos")
@RequiredArgsConstructor
public class PublicOrcamentoController {


    private final PublicOrcamentoService publicOrcamentoService;
    private final ReparoAdicionalService reparoAdicionalService;

    @GetMapping("/{orcamentoId}")
    public OrcamentoPublicoResponse consultar(@PathVariable Long orcamentoId,
                                              @RequestParam String token) {
        return OrcamentoPublicoResponse.from(publicOrcamentoService.consultar(orcamentoId, token));
    }

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

    @PostMapping("/{orcamentoId}/recusar")
    public OrcamentoPublicoResponse recusar(@PathVariable Long orcamentoId,
                                            @RequestParam String token,
                                            @RequestBody(required = false) RecusarOrcamentoRequest req) {
        String motivo = (req == null) ? null : req.motivo();
        return OrcamentoPublicoResponse.from(publicOrcamentoService.recusar(orcamentoId, token, motivo));
    }

}
